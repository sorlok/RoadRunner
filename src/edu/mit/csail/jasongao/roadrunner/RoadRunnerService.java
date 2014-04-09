package edu.mit.csail.jasongao.roadrunner;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import edu.mit.csail.jasongao.roadrunner.ext.RegionAndPathHandler;
import edu.mit.csail.jasongao.roadrunner.ext.RegionMessages;
import edu.mit.csail.jasongao.roadrunner.ext.TokenRandomizer;
import edu.mit.csail.jasongao.roadrunner.ext.RegionMessages.RerouteRequest;
import edu.mit.csail.jasongao.roadrunner.util.Geometry;
import edu.mit.csail.jasongao.roadrunner.util.InterfaceMap;
import edu.mit.csail.jasongao.roadrunner.util.IpIdentifier;
import edu.mit.csail.jasongao.roadrunner.util.LinkViability;
import edu.mit.csail.jasongao.roadrunner.util.LoggerI;
import edu.mit.csail.jasongao.roadrunner.util.LoggingRuntimeException;
import edu.mit.smart.sm4and.SimMobilityBroker;
import edu.mit.smart.sm4and.connector.Connector;
import edu.mit.smart.sm4and.handler.AbstractMessageHandler;
import edu.mit.smart.sm4and.message.MessageParser;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PointF;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.speech.tts.TextToSpeech;
import android.telephony.TelephonyManager;

public class RoadRunnerService extends Service implements LocationListener, LoggerI {
	public static final String TAG = "RoadRunnerService";
	
	//Used for communicating with Sim Mobility.
	SimMobilityBroker simmob = SimMobilityBroker.getInstance();

	// Android system
	PowerManager.WakeLock wl = null;
	LocationManager lm;
	TelephonyManager tm;
	TextToSpeech mTts = null;
	
	//List of free Regions (no token needed). Note that Sim Mobility currently doesn't use these (except "FREE")
	private static final Set<String> FreeRegions = new HashSet<String>();
	static {
		FreeRegions.add("FREE");
		FreeRegions.add("Mass-1");
		FreeRegions.add("Mass-2");
		FreeRegions.add("Mass-3");
		FreeRegions.add("Main-1");
		FreeRegions.add("Main-2");
		FreeRegions.add("Main-3");
		FreeRegions.add("Main-4");
	}
			
	public void say(String msg) {
		if (mTts != null) {
			mTts.speak(msg, TextToSpeech.QUEUE_ADD, null);
		}
	}

	// Communication threads
	private AdhocPacketThread aat;
	private AdhocServerThread ast;

	/***********************************************
	 * RoadRunner state
	 ***********************************************/

	/** TODO Nonces for idempotent UDP tokens */
	private int nonce = 0;
	private HashMap<Long, HashSet<Long>> noncesHeard;

	private boolean adhocEnabled = false;
	private boolean onDemand = false;
	private boolean directionCcw = false;

	/**
	 * When was the cellular data access? It goes dormant after 10 seconds
	 */
	private long lastDataActivity = 0;

	/**
	 * Set of known Regions, indexed by Region ID.
	 */
	private Hashtable<String, Region> regions;

	private Location mLoc;
	private String mRegion = "FREE";
	private long mId = -1000;
	private String mIdStr;
	private Random rand = new Random();
	
	//The last Region we've requested a reroute around (to avoid spamming the server).
	private String lastRequestedReroute;

	private long udpStartTime = 0;

	/** Reservations we are using/will use. Map regionId to done ResRequest */
	private Map<String, ResRequest> reservationsInUse;

	/** Reservations we can give away. Will be sent to cloud eventually. */
	public Queue<ResRequest> offers;

	/** Penalty reservations */
	public Queue<ResRequest> penalties;

	/** Pending GET RES_REQUESTS that can be sent to either cloud or to adhoc */
	private Queue<ResRequest> getsPending;

	
	//Schedule like normal (includes a bit of randomness).
	private long calcNextRegionRerouteCheck() {
		return simmob.getRand().nextInt(Globals.SM_REROUTE_CHECK_ADDITIVE) + Globals.SM_REROUTE_CHECK_BASE;
	}
	
	
	/***********************************************
	 * Queue helpers
	 ***********************************************/

	/** Returns a Set containing all Region keys in a queue. */
	public static Set<String> queueKeySet(Queue<ResRequest> q) {
		Set<String> keys = new HashSet<String>();

		for (Iterator<ResRequest> it = q.iterator(); it.hasNext();) {
			ResRequest req = it.next();
			keys.add(req.regionId);
		}

		return keys;
	}

	/** Removes a ResRequest from the Queue and returns it. Null if not found. */
	public static ResRequest queuePoll(Queue<ResRequest> q, String rid) {
		for (Iterator<ResRequest> it = q.iterator(); it.hasNext();) {
			ResRequest req = it.next();
			if (req.regionId.equals(rid)) {
				it.remove();
				return req;
			}
		}

		return null;
	}	
	
	
	//Handler for SM region messages.
	public class RegionSetter  {
		public void setRegions(Hashtable<String, Region> regions) {
	        if (regions!=null) { 
	        	RoadRunnerService.this.regions = regions; 
	        }
		}
	}
	
	//Handler for SM path messages.
	public class PathSetter {
		public void setPath(ArrayList<ResRequest> path, ArrayList<ResRequest> gratis) {
			//Can't set a Path without actual, defined Regions.
			if (RoadRunnerService.this.regions==null) {
				throw new LoggingRuntimeException("PathSetter.setPath() - Can't set a path without pre-defined Regions.");
			}
			
			//Set the gratis Regions if this is the first time our path is being set.
			if (getsPending.isEmpty() && gratis!=null) {
				for (ResRequest rr : gratis) {
					log("Got a free token for: " + rr.regionId);
					reservationsInUse.put(rr.regionId, rr);
				}
			}
			
			//Set the path.
			if (path!=null) {
				getsPending.clear();
				for (int i=0; i<path.size(); i++) {
					//Don't add the token if we already have it.
					if (!reservationsInUse.containsKey(path.get(i).regionId)) {
						makeRequest(path.get(i), i==path.size()-1); //Announce on the last one only.
					}
				}
			}
		}
	}
	
	

	/***********************************************
	 * Handle messages from other components and threads
	 ***********************************************/
	public final static int ADHOC_PACKET_RECV = 4;

	public final Handler myHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case ADHOC_PACKET_RECV:
				if (!adhocEnabled) {
					break;
				}
				AdhocPacket other = (AdhocPacket) msg.obj;
				
				// filter out messages not addressed to us or broadcast
				if (other.dst != -1 && other.dst != mId) {
					break;
				}
				
				long now = getTime();
				log_nodisplay(String.format("Received UDP %s", other));

				if (other.triggerAnnounce) {
					adhocAnnounce(false);
				}
				
				boolean linkIsViable = LinkViability.IsViableDSRC(mLoc, other.getLocation(), (LoggerI)RoadRunnerService.this);
				if (!linkIsViable) {
					break;
				}

				if (other.type == AdhocPacket.TOKEN_REQUEST) {
					// Someone wants a token from us; give them it if possible
					ResRequest req = queuePoll(offers, other.region);
					if (req != null) {
						AdhocPacket p = new AdhocPacket(mId, mLoc);
						p.dst = other.src;
						p.type = AdhocPacket.TOKEN_SEND;
						p.tokenString = req.tokenString;
						p.region = req.regionId;
						p.signature = req.signature;
						p.issued = req.issued;
						p.expires = req.expires;

						// Idempotently send over UDP a few times
						log(String
								.format("Responding to GET request from %d with an offered reservation. Over UDP.",
										other.src));
						p.nonce = nonce++;
						new SendPacketsTask().execute(p, p, p);
					}

				} else if (other.type == AdhocPacket.TOKEN_SEND) {
					long udpStopTime = System.currentTimeMillis();
					// Check if already received this packet copy
					if (!noncesHeard.containsKey(other.src)) {
						noncesHeard.put(other.src, new HashSet<Long>());
					}

					if (noncesHeard.get(other.src).contains(other.nonce)) {
						// Already heard this packet, so ignore
						log("Nonce seen before, ignoring duplicate token sent.");
						break;
					} else {
						long udpLatency = udpStopTime - udpStartTime;
						log(String
								.format("Nonce NOT seen before, receiving token sent, UDP token transfer round-trip latency %d ms",
										udpLatency));
						noncesHeard.get(other.src).add(other.nonce);
					}

					// Other car sent a token to us
					ResRequest req = queuePoll(getsPending, other.region);
					if (req != null) {
						// It was a pending GET, so add to our in-use store
						req.done = true;
						req.completed = getTime();
						log(String
								.format("GET request for %s completed after %d ms Over UDP",
										req.regionId, req.completed
												- req.created));
						/* Use reservation if we don't have it, otherwise extras */
						if (!reservationsInUse.containsKey(req.regionId)) {
							reservationsInUse.put(req.regionId, req);
							log(String.format("Added to reservationsInUse: %s",
									reservationsInUse));
						} else {
							req.hardDeadline = req.completed
									+ Globals.REQUEST_DIRECT_PUT_DEADLINE_FROM_NOW;
							offers.add(req);
							log(String.format("Added to offers: %s",
									req.regionId));
						}
					} else {
						req = new ResRequest(mId, ResRequest.RES_GET,
								other.region);
						req.done = true;
						req.completed = getTime();
						req.tokenString = other.tokenString;
						req.signature = other.signature;
						String[] parts = other.tokenString.split(" ");
						req.issued = Long.parseLong(parts[1]);
						req.expires = Long.parseLong(parts[2]);
						req.hardDeadline = req.completed
								+ Globals.REQUEST_DIRECT_PUT_DEADLINE_FROM_NOW;
						offers.add(req);

						log(String.format(
								"%s sent unwanted token for %s Over UDP",
								other.src, req.regionId));

						log(String.format("Added to offers: %s", req.regionId));
					}

				} else if (other.type == AdhocPacket.ANNOUNCE) {
					for (Iterator<ResRequest> it = getsPending.iterator(); it.hasNext();) {
						ResRequest req = it.next();

						// try to get token from other vehicle?
						if (other.tokensOffered.contains(req.regionId)) {
							if (Globals.ADHOC_UDP_ONLY) { // UDP pathway
								// Send a TOKEN_REQUEST
								AdhocPacket p = new AdhocPacket(mId, mLoc);
								p.type = AdhocPacket.TOKEN_REQUEST;
								p.region = req.regionId;
								log(String
										.format("Other vehicle %d offers %s, I want %s, GET %s Over UDP",
												other.src, other.tokensOffered,
												queueKeySet(getsPending),
												req.regionId));

								udpStartTime = System.currentTimeMillis();
								new SendPacketsTask().execute(p);
							} else { // TCP pathway

								log(String
										.format("Other vehicle %d offers %s, I want %s, GET %s",
												other.src, other.tokensOffered,
												queueKeySet(getsPending),
												req.regionId));

								it.remove(); // ConcurrentModificationException?
								new ResRequestTask().execute(req, "192.168.42."
										+ other.src);
							}

						}

						// try to relay through other vehicle?
						// DEPRECATED: doesn't work well currently
						else if (Globals.RELAY_ENABLED
								&& other.dataActivity != TelephonyManager.DATA_ACTIVITY_DORMANT
								&& req.softDeadline < now) {
							log(String
									.format("Request soft deadline %d expired, relaying through vehicle %d to cloud: %s",
											req.softDeadline, other.src, req));
							getsPending.remove(req);
							new ResRequestTask().execute(req, "192.168.42."
									+ other.src);
						}
					}
				}

				updateDisplay();

				break;
			}
		}
	};

	/** Send an ResRequest to a TCP/IP endpoint (whether peer or cloud) */
	public class ResRequestTask extends AsyncTask<Object, Integer, ResRequest> {

		@Override
		protected ResRequest doInBackground(Object... params) {
			ResRequest req = (ResRequest) params[0];
			String mHost = (String) params[1];

			long startTime = getTime();

			Socket s = new Socket();
			try {
				s.connect(new InetSocketAddress(mHost, Globals.CLOUD_PORT),
						Globals.CLOUD_SOCKET_TIMEOUT);

				InputStream in = s.getInputStream();
				OutputStream out = s.getOutputStream();

				BufferedReader reader = new BufferedReader(
						new InputStreamReader(in));
				Writer writer = new OutputStreamWriter(out);

				String response;

				// Dispatch request correctly
				switch (req.type) {
				case ResRequest.RES_GET:
					writer.write(String.format("GET %d %s\r\n", mId,
							req.regionId));
					writer.flush();

					response = reader.readLine();
					log("Response: " + response);
					if ("GET 200 OK".equals(response)) {
						req.tokenString = reader.readLine();
						req.signature = reader.readLine();
						String[] parts = req.tokenString.split(" ");
						req.issued = Long.parseLong(parts[1]);
						req.expires = Long.parseLong(parts[2]);

						log_nodisplay(String
								.format("Received:\nTOKEN %s\nSIG %s\nISSUED: %d\nEXPIRES: %d",
										req.tokenString, req.signature,
										req.issued, req.expires));

						// TODO verify signature
						if (!req.tokenIsValid()) {
							// log("Token signature verification FAILED!");
							// failed to verify token, put back into pending q?
						}

						req.done = true;
					} else {
						log("GET request failed: " + response);
						req.done = false;
					}
					break;
				case ResRequest.RES_PUT:
					writer.write(String.format("PUT %d %s\r\n", mId,
							req.regionId));
					writer.flush();

					response = reader.readLine();
					log("Response: " + response);
					if ("PUT 200 OK".equals(response)) {
						req.done = true;
					} else {
						log("PUT request failed: " + response);
						req.done = false;
					}
					break;
				case ResRequest.DEBUG_RESET:
					writer.write(String.format("DEBUG-RESET %d %s\r\n", mId,
							req.regionId));
					writer.flush();
					log("Response: " + reader.readLine());
					req.done = true;
					break;
				}

			} catch (Exception e) {
				log("Unexpected exception: " + e.toString());
			} finally {
				try {
					s.shutdownOutput();
				} catch (Exception e) {
				}

				try {
					s.shutdownInput();
				} catch (Exception e) {
				}

				try {
					s.close();
				} catch (Exception e) {
				}
			}

			long stopTime = getTime();

			if (req.type == ResRequest.RES_GET) {
				log(String
						.format("GET request for %s on %s network access completed in %d ms",
								req.regionId, mHost, stopTime - startTime));
			} else if (req.type == ResRequest.RES_PUT) {
				log(String
						.format("PUT request for %s on %s network access completed in %d ms",
								req.regionId, mHost, stopTime - startTime));
			} else {
				log(String
						.format("OTHER request for %s on %s network access completed in %d ms",
								req.regionId, mHost, stopTime - startTime));
			}

			// Update last cellular access
			myHandler.post(updateLastDataActivity);

			return req;
		}

		protected void onProgressUpdate(Integer... progress) {
		}

		protected void onPostExecute(ResRequest req) {
			/* GET */
			if (req.type == ResRequest.RES_GET) {
				/* GET SUCCESSFUL */
				if (req.done) {
					req.completed = getTime();
					log(String.format(
							"GET request for %s completed after %d ms",
							req.regionId, req.completed - req.created));
					/* Use reservation if we don't have it, otherwise extras */
					if (!reservationsInUse.containsKey(req.regionId)) {
						reservationsInUse.put(req.regionId, req);
						log(String.format("Added to reservationsInUse: %s",
								reservationsInUse));
					} else {
						req.hardDeadline = req.completed
								+ Globals.REQUEST_DIRECT_PUT_DEADLINE_FROM_NOW;
						offers.add(req);
						log(String.format("Added to offers: %s", req.regionId));
					}
				}
				/* GET FAILED */
				else {
					log(String
							.format("GET request on %s failed, adding back to pending queue.",
									req.regionId));
					// reset deadlines
					long now = getTime();
					req.softDeadline = now
							+ Globals.REQUEST_RELAY_GET_DEADLINE_FROM_NOW;
					req.hardDeadline = now
							+ Globals.REQUEST_DIRECT_GET_DEADLINE_FROM_NOW;
					getsPending.add(req);
				}
			}
			/* PUT */
			else if (req.type == ResRequest.RES_PUT) {
				/* PUT SUCCESSFUL */
				if (req.done) {
					req.completed = getTime();
					log(String.format(
							"PUT request for %s completed after %d ms",
							req.regionId, req.completed - req.created));
				}
				/* PUT FAILED */
				else {
					log(String.format(
							"PUT request on %s failed, adding back to offers.",
							req.regionId));
					// Reset request time and type
					long now = getTime();
					req.hardDeadline = now
							+ Globals.REQUEST_DIRECT_PUT_DEADLINE_FROM_NOW;
					req.type = ResRequest.RES_GET;
					offers.add(req);
				}
			}

			updateDisplay();
		}
	}

	/***********************************************
	 * Runnables
	 ***********************************************/

	public Runnable updateLastDataActivity = new Runnable() {
		public void run() {
			lastDataActivity = getTime();
		}
	};

	/** Periodic check for penalty reservations to clear */
	private Runnable penaltyCheck = new Runnable() {
		public void run() {
			long now = getTime();

			// send timed-out PUT requests to cloud
			for (Iterator<ResRequest> it = penalties.iterator(); it.hasNext();) {
				ResRequest req = it.next();
				if (req.hardDeadline < now) {
					log(String.format(
							"Penalty reservation expired, removing %s",
							req.hardDeadline, req));
					it.remove();
				}
			}

			myHandler.postDelayed(this, Globals.REQUEST_PENALTY_CHECK_PERIOD);
		}
	};

	/** Periodic check for GET requests that need to be sent to the cloud */
	private Runnable cloudDirectGetRequestCheck = new Runnable() {
		public void run() {
			long now = getTime();

			// send expired requests directly to cloud
			// for (ResRequest resRequest : getsPending) {
			for (Iterator<ResRequest> it = getsPending.iterator(); it.hasNext();) {
				ResRequest req = it.next();
				if (req.hardDeadline < now) {
					log(String
							.format("Request hard deadline %d expired, direct to cloud: %s",
									req.hardDeadline, req));
					it.remove();
					new ResRequestTask().execute(req, Globals.CLOUD_HOST);
				}
			}

			myHandler.postDelayed(this, Globals.REQUEST_DEADLINE_CHECK_PERIOD);
		}
	};

	/** Periodic check for PUT that need to be sent to the cloud */
	private Runnable cloudDirectPutRequestCheck = new Runnable() {
		public void run() {
			long now = getTime();

			// send timed-out PUT requests to cloud
			for (Iterator<ResRequest> it = offers.iterator(); it.hasNext();) {
				ResRequest req = it.next();
				if (req.hardDeadline < now) {
					log(String
							.format("PUT request hard deadline %d expired, direct to cloud: %s",
									req.hardDeadline, req));
					it.remove();
					req.type = ResRequest.RES_PUT;
					new ResRequestTask().execute(req, Globals.CLOUD_HOST);
				}
			}

			myHandler.postDelayed(this, Globals.REQUEST_DEADLINE_CHECK_PERIOD);
		}
	};
	
	
	/**
	 * Region checker
	 */	
	
	//Check if we are too close to our next Region and must re-route.
	private void checkNextRegionAndReroute() {
		//Only matters if we're waiting on at lest one token.
		ResRequest nextRes = getsPending.peek();
		if (nextRes==null || nextRes.regionId==null || mLoc==null) { return; }
		
		//This relies on SimMob regions.
		if (this.regions==null) {
			throw new LoggingRuntimeException("RoadRunnerService.checkNextRegionAndReroute() - SimMob regions should be on by now.");
		}
		
		//Retrieve the region.
		Region region = this.regions.get(nextRes.regionId);
		if (region==null) {
			throw new LoggingRuntimeException("RoadRunnerService.checkNextRegionAndReroute() - Unexpected null region for key: " + nextRes.regionId);
		}
		
		//Don't check if we've already posted a rerouting request for this Region.
		if (nextRes.regionId == lastRequestedReroute) {
			return;
		}
		
		//Check how close we are to that Region. To do this, we check the intersection point to each of the Region's segments.
		//There are more efficient ways to do this, but it hardly matters.
		double minDist = Globals.SM_REROUTE_DISTANCE * 2.0; //Start way off.
		Location prevPt = region.vertices.get(region.vertices.size()-1);
		for (Location currPt : region.vertices) {
			//"Normalize" these points to get an idea of their rough distances in meters.
			PointF ourPos = new PointF();
			PointF currPos = new PointF();
			PointF prevPos = new PointF();
			Geometry.normalize_to_meters(mLoc, currPt, prevPt, ourPos, currPos, prevPos);
			
			//Calculate the intersection.
			double ptLineDist = Geometry.pt_line_dist(prevPos, currPos, ourPos);
			minDist = Math.min(minDist, ptLineDist);
			if (minDist<Globals.SM_REROUTE_DISTANCE) { break; }
			
			//Update
			prevPt = currPt;
		}
		
		//Are we too close?
		//System.out.println("Current distance to next Region (" + nextRes.regionId + ") is: " + minDist);
		if (minDist<Globals.SM_REROUTE_DISTANCE) {
			//Request a re-route from the server.
			String blRegion = nextRes.regionId;
			RerouteRequest rr = new RerouteRequest();
			rr.blacklisted = blRegion;
			simmob.forwardMessageToServer(rr);
			
			//Log
			log("No token; requesting that the server re-route around region: " + blRegion);
			simmob.ReflectToServer("Requested re-route from Sim Mobility, blacklisting Region: " + blRegion);
			
			lastRequestedReroute = nextRes.regionId;
		} 
	}
	
	
	/** Periodically check if we need to re-route. */
	private Runnable rerouteCheckR = new Runnable() {
		public void run() {
			checkNextRegionAndReroute();
			
			//TODO: Check why RoadRunner doesn't use this.
			if (simmob.isActive()) {
				simmob.postOnHandlerDelayed(this, calcNextRegionRerouteCheck());
			}
		}
	};
	

	/***********************************************
	 * Adhoc announcements
	 ***********************************************/

	private void adhocAnnounce(boolean triggerAnnounce_) {
		if (!this.adhocEnabled) {
			return;
		}
		
		//Just need to make sure we're not tied to the 2s clock but rather simulation time.
		if (Globals.SIM_MOBILITY) {
			//log("ad-hoc announcement sending..."); //Too wordy
		}
		
		AdhocPacket p = new AdhocPacket(mId, mLoc);

		// AdhocPacket the state of our data link
		if (this.lastDataActivity + Globals.LAST_DATA_ACTIVITY_THRESHOLD < System
				.currentTimeMillis()) {
			p.dataActivity = tm.getDataActivity();
		} else {
			p.dataActivity = TelephonyManager.DATA_ACTIVITY_DORMANT;
		}

		// AdhocPacket what tokens we are offering
		if (!Globals.SIM_MOBILITY) {
			p.tokensOffered = queueKeySet(this.offers);
		} else {
			p.tokensOffered = TokenRandomizer.RandomTokens(simmob.getRand(), Globals.SM_TOKEN_RANGE, Globals.SM_NUM_TOKENS_LOWER, Globals.SM_NUM_TOKENS_UPPER);
		}

		p.triggerAnnounce = triggerAnnounce_;

		new SendPacketsTask().execute(p);
	}

	/** Periodic status announcements over adhoc */
	private Runnable adhocAnnounceR = new Runnable() {
		public void run() {
			adhocAnnounce(false);
			
			if (simmob.isActive()) {
				simmob.postOnHandlerDelayed(this, Globals.ADHOC_ANNOUNCE_PERIOD);
			} else {
				myHandler.postDelayed(this, Globals.ADHOC_ANNOUNCE_PERIOD);
			}
		}
	};

	/***********************************************
	 * Interface to MainActivity
	 ***********************************************/

	// So we can send messages back to the MainActivity
	private Handler mainHandler;

	// Binder given to clients (the MainActivity)
	private final IBinder mBinder = new LocalBinder();

	/** This service runs in the same process as activity, don't need IPC. */
	public class LocalBinder extends Binder {
		RoadRunnerService getService(Handler h) {
			mainHandler = h;

			log("Got main activity handler, returning service instance...");

			// Return this instance so clients can call public methods
			return RoadRunnerService.this;
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	public void log_nodisplay(String message) {
		mainHandler.obtainMessage(MainActivity.LOG_NODISPLAY, message).sendToTarget();
	}

	public void log(String message) {
		mainHandler.obtainMessage(MainActivity.LOG, message).sendToTarget();
	}
	

	public void updateDisplay() {
		List<String> update = new ArrayList<String>();
		update.add(String.format("%s", this.mRegion));
		update.add(String.format("%s", this.reservationsInUse.keySet()));
		update.add(String.format("%s", queueKeySet(this.offers)));
		mainHandler.obtainMessage(MainActivity.UPDATE_DISPLAY, update).sendToTarget();
	}

	/***********************************************
	 * Activity-Service interface
	 ***********************************************/

	///If "announce" is true, an ad-hoc announce is sent after this request is added.
	///This is usually desirable, unless you are making multiple requests in a row.
	public void makeRequest(ResRequest r1) {
		makeRequest(r1, true);
	}
	public void makeRequest(ResRequest r1, boolean announce) {
		log(String.format("Adding new pending request for %s.", r1.regionId));
		if (this.adhocEnabled) {
			getsPending.add(r1); // queue up requests
			if (announce) {
				adhocAnnounce(true); // ask nearby vehicles to announce their offers
			}
		} else {
			// send directly to cloud
			new ResRequestTask().execute(r1, Globals.CLOUD_HOST);
		}
	}	


	public void resetCloud() {
		log(String.format("Sending ResRequest for DEBUG-RESET"));
		ResRequest r1 = new ResRequest(mId, ResRequest.DEBUG_RESET, "Vassar-1");
		new ResRequestTask().execute(r1, Globals.CLOUD_HOST);
	}

	/*** DEBUG make a fake request to test token transfers */
	public void makeReservationRouteStata() {
		log(String.format("Making DEBUG ResRequest for Stata-1"));
		makeRequest(new ResRequest(mId, ResRequest.RES_GET, "Stata-1"));
	}

	/*** DEBUG make a fake offer to test token transfers */
	public void makeOfferRouteStata() {
		log(String.format("Making DEBUG Offer for Vassar-1"));
		
		ResRequest res = new ResRequest(mId, ResRequest.RES_GET, "Stata-1");
		res = new ResRequest(mId, ResRequest.RES_GET, "Stata-1");
		res.done = true;
		res.completed = getTime();
		res.tokenString = "DEBUG";
		res.signature = "DEBUG";
		res.issued = getTime();
		res.expires = getTime() + 30*60*1000;
		res.hardDeadline = res.completed
				+ Globals.REQUEST_DIRECT_PUT_DEADLINE_FROM_NOW;
		this.offers.add(res);

		log(String.format("Added to offers: %s", res.regionId));
	}

	public void makeReservationRouteA() {
		log(String.format("Making ResRequests for route A"));
		makeRequest(new ResRequest(mId, ResRequest.RES_GET, "Vassar-1"));
		makeRequest(new ResRequest(mId, ResRequest.RES_GET, "Main-1"));
		makeRequest(new ResRequest(mId, ResRequest.RES_GET, "Main-2"));
		makeRequest(new ResRequest(mId, ResRequest.RES_GET, "Main-3"));
		makeRequest(new ResRequest(mId, ResRequest.RES_GET, "Windsor-1"));
		makeRequest(new ResRequest(mId, ResRequest.RES_GET, "Mass-1"));
		makeRequest(new ResRequest(mId, ResRequest.RES_GET, "Mass-2"));
	}

	public void makeReservationRouteB() {
		log(String.format("Making ResRequests for route B"));
		makeRequest(new ResRequest(mId, ResRequest.RES_GET, "Albany-1"));
		makeRequest(new ResRequest(mId, ResRequest.RES_GET, "Albany-2"));
		makeRequest(new ResRequest(mId, ResRequest.RES_GET, "Vassar-1"));
		makeRequest(new ResRequest(mId, ResRequest.RES_GET, "Main-3"));
		makeRequest(new ResRequest(mId, ResRequest.RES_GET, "Mass-2"));
	}

	public void makeReservationRouteC() {
		makeRequest(new ResRequest(mId, ResRequest.RES_GET, "Albany-1"));
		makeRequest(new ResRequest(mId, ResRequest.RES_GET, "Portland-1"));
		makeRequest(new ResRequest(mId, ResRequest.RES_GET, "Main-2"));
		makeRequest(new ResRequest(mId, ResRequest.RES_GET, "Vassar-1"));
		makeRequest(new ResRequest(mId, ResRequest.RES_GET, "Main-3"));
		makeRequest(new ResRequest(mId, ResRequest.RES_GET, "Mass-2"));
		log(String.format("Adding ResRequests for route C"));

	}

	/***********************************************
	 * Interface to adhoc network
	 ***********************************************/

	/** Asynchronous background task for sending packets to the network */
	public class SendPacketsTask extends AsyncTask<AdhocPacket, Integer, Long> {
		@Override
		protected Long doInBackground(AdhocPacket... packets) {
			long count = packets.length;
			long sent = 0;
			for (int i = 0; i < count; i++) {
				AdhocPacket adhocPacket = packets[i];

				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				try {
					ObjectOutput out = new ObjectOutputStream(bos);
					out.writeObject(adhocPacket);
					out.close();
					byte[] data = bos.toByteArray();
					publishProgress((int) i + 1, (int) count);
					
					//Dispatch through Sim Mobility, or through the AdHoc's broadcast port.
					if (Globals.SIM_MOBILITY) {
						simmob.sendBroadcastPacket(mIdStr, data);
					} else if (aat != null) {
						aat.sendData(data);
					}
					
					sent++;
					/*log_nodisplay(String.format(
							"sent %d byte adhoc packet type %d", data.length,
							adhocPacket.type));*/
					
				} catch (Exception e) {
					log("error sending adhoc announcement:" + e.getMessage());
				}
			}
			return sent;
		}

		protected void onProgressUpdate(Integer... progress) {
			// log("Sending " + progress[0] + " of " + progress[1] +
			// " packets...");
		}

		protected void onPostExecute(Long result) {
			// log("Sent " + result + " adhoc UDP packets");
		}
	}

	/***********************************************
	 * Android lifecycle
	 ***********************************************/

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// We want this service to continue running until explicitly stopped
		return START_STICKY;
	}

	@Override
	public void onCreate() {
		super.onCreate();

		// Get a wakelock to keep everything running
		PowerManager pm = (PowerManager) getApplicationContext().getSystemService(Context.POWER_SERVICE);
		wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE, TAG);
		wl.acquire();

		lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		
		//Sim Mobility *only* uses spoofed location updates.
		if (!Globals.SIM_MOBILITY) {
			lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1f, this);
		}

		tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
	}

	@Override
	public void onDestroy() {
		log("Service destroyed");

		if (wl != null) {
			wl.release();
		}

		if (lm != null) {
			lm.removeUpdates(this);
		}

		stop();
	}
	
	
	private void retrieveUniqueId() {
		//Get my Inet address and use it to generate a unique ID
		Inet4Address addr = null;
		if (Globals.SIM_MOBILITY) {
			//Currently, "eth0" or "wlan0" can identify this phone.
			addr = InterfaceMap.GetInstance().getAddress(Globals.SM_IDENTIFYING_INTERFACES);
		} else {
			//"eth0" only.
			addr = InterfaceMap.GetInstance().getAddress(Globals.ADHOC_IFACE_NAME);
		}
		
		//Sim Mobility requires longer IDs (so we use the entire IP address). 
		if (Globals.SIM_MOBILITY) {
			mId = IpIdentifier.GenerateIdFromInet(addr);			
		} else {
			//take last octet of IPv4 address as my id
			if (addr!=null) {
				byte[] addrses = addr.getAddress();
				mId = (addrses[3] & 0xff);
			} else {
				//If none, just make a Random ID and hope there are no collisions.
				mId = rand.nextInt(0xFF);
			}
		}
		
		//Inform the user of their ID.
		mIdStr = String.valueOf(mId);
		log("mId=" + mId);
	}
	

	public synchronized void start(TextToSpeech mTts_, boolean adhocEnabled_, boolean onDemand_, boolean directionCcw_) {
		this.mTts = mTts_;
		this.adhocEnabled = adhocEnabled_;
		this.onDemand = onDemand_;
		this.directionCcw = directionCcw_;
		
		log(String
				.format("Service started with adhocEnabled %b, onDemand %b, directionCcw %b",
						this.adhocEnabled, this.onDemand, this.directionCcw));

		// Set up regions
		if (Globals.SIM_MOBILITY) {
			//"Null" means "randomize regions".
			this.regions = null;
			//this.regions = TestRegions.MakeSimMobilityRegions();
		} else {
			this.regions = TestRegions.MakeExperimentARegions();
			TestRegions.TestExperimentARegions(this.regions, this);
		}

		// Initialize state
		this.reservationsInUse = new ConcurrentHashMap<String, ResRequest>();
		this.getsPending = new ConcurrentLinkedQueue<ResRequest>();
		this.offers = new ConcurrentLinkedQueue<ResRequest>();
		this.penalties = new ConcurrentLinkedQueue<ResRequest>();
		this.noncesHeard = new HashMap<Long, HashSet<Long>>();
		
		//Connect to the Sim Mobility server.
		if (Globals.SIM_MOBILITY) {
			//We need this now.
			retrieveUniqueId();
			
			//Initialize.
			simmob.initialize(mIdStr, myHandler, this, new LocationSpoofer(), new PathSetter());
			
			//Register our "Regions and Paths" handler, which is specific to Road Runner.
			simmob.addCustomMessageType(
				RegionMessages.SendRegionResponse.MessageType,
				RegionMessages.SendRegionResponse.class, 
				new RegionAndPathHandler(new RegionSetter(), new PathSetter())
			);
			
			//We also need a custom handler for the "WHOAREYOU" message type; this is when we will start our recurring tasks.
			simmob.addCustomMessageHandler(edu.mit.smart.sm4and.message.Message.Type.id_request, new AbstractMessageHandler() {
				@Override
				public void handle(edu.mit.smart.sm4and.message.Message message, Connector connector, MessageParser parser) {
					//TODO: This should occur in the same place in RoadRunner.
					myHandler.post(adhocAnnounceR);
					
					//TODO: Figure out why this initialization only occurs in SimMobility mode. When does the normal app check?
					simmob.postOnHandlerDelayed(rerouteCheckR, calcNextRegionRerouteCheck());  
				}
			});
			
			
			//Activate.
			simmob.activate();
			log("Sim Mobility server connected.");
		}

		// Start recurring runnables
		myHandler.postDelayed(cloudDirectGetRequestCheck,
				Globals.REQUEST_DEADLINE_CHECK_PERIOD);
		myHandler.postDelayed(cloudDirectPutRequestCheck,
				Globals.REQUEST_DEADLINE_CHECK_PERIOD);
		myHandler.postDelayed(penaltyCheck,
				Globals.REQUEST_PENALTY_CHECK_PERIOD);

		if (this.adhocEnabled) {
			// Start the adhoc UDP announcement thread
			log("Starting adhoc announce thread...");
			aat = new AdhocPacketThread(myHandler, this);
			aat.start();

			if (!Globals.ADHOC_UDP_ONLY) {
				// Start the adhoc TCP server thread
				log("Starting adhoc server thread...");
				ast = new AdhocServerThread(mainHandler, this);
				ast.start();
			}
			
			//This is actually done earlier if SimMobility is enabled, 
			//but doing it twice is harmless.
			retrieveUniqueId();

			// Start recurring UDP adhoc announcements
			//TODO: This should also push the item to the broker. However, we can't push it now, since
			//      the Broker's not ready to send data. We *should* pend it (later).
			if (!Globals.SIM_MOBILITY) {
				myHandler.post(adhocAnnounceR);
			}
		} else {
			mId = 255; // cloud-only doesn't need unique IDs
		}

		updateDisplay();
	}

	/**
	 * TODO: This may not work exactly as expected; rrs.getRegion() *must* be called
	 *       if you want the application to be aware of Sim Mobility server integration.
	 */
	private String getRegion(Hashtable<String, Region> rs, Location loc) {	
		//Randomized regions only work in a very specific case.
		boolean randReg = Globals.SIM_MOBILITY && simmob.isActive() && (rs==null);
		if (randReg) {
			if (Globals.SM_ALLOW_RANDOM_REGIONS && firstSecondPassed()) {
				String newRegionId = TokenRandomizer.SpoofRandomRegion(simmob.getRand(), Globals.SM_TOKEN_RANGE, Globals.SM_FREE_REGION_PERCENT);
				if (newRegionId!=null) {
					return newRegionId; //The Region was successfully spoofed.
				}
			}
			return Globals.FREE_REGION_TAG; //Fall-back when attempting to spoof Regions.
		}
		
		//If we *have* a Region dataset, use it.
		return Region.Loc2Region(rs, loc, Globals.FREE_REGION_TAG);
	}
	
	
	///Have we passed the first second of execution time?
	///This is used as a very coarse-grained test to avoid spurious regions while Region negotiation is still taking place.
	private boolean firstSecondPassed() {
		if (simmob.isActive()) {
			return simmob.getCurrTimeMs() > 1000;
		}
		return false; //Arbitrary; should never be called.
	}

	public synchronized void stop() {
		log("Stopping service...");

		myHandler.removeCallbacks(adhocAnnounceR);
		myHandler.removeCallbacks(cloudDirectGetRequestCheck);
		myHandler.removeCallbacks(cloudDirectPutRequestCheck);

		log("Terminating adhoc announce thread...");
		if (aat != null) {
			aat.close();
			aat = null;
		}

		log("Terminating adhoc server thread WiFi...");
		if (ast != null) {
			ast.close();
			ast = null;
		}
	}

	/***********************************************
	 * GPS
	 ***********************************************/

	public long getTime() {
		return MainActivity.getTime();
	}
	
	
	//Spoof location changed, as an object.
	public class LocationSpoofer {
		public void setLocation(double lat, double lng) {
			Location l = new Location("simmob");
			l.setLatitude(lat);
			l.setLongitude(lng);
			l.setTime(System.currentTimeMillis());
			locationChanged(l, true);
		}
	}
	
	//Location changed (with a "spoofed" flag)
	@SuppressWarnings("unused")
	private void locationChanged(Location loc, boolean spoofed) {
		//Special case: Sim Mobility is bound ONLY by the spoofed messages. 
		// (These shouldn't arrive anyway, but we check again just to be safe.)
		if (Globals.SIM_MOBILITY && !spoofed) { 
			return; 
		}
		
		
		// log GPS traces
		if (Globals.SM_VERBOSE_TRACE || !Globals.SIM_MOBILITY) {
			log_nodisplay(String.format("loc=%s", loc.toString()));
		}
		
		// sync internal clock to GPS on first fix
		/*
		 * if (!MainActivity.clockSynced) { MainActivity.clockOffset =
		 * loc.getTime() - System.currentTimeMillis(); MainActivity.clockSynced
		 * = true; log(String.format("CLOCK SYNCED TO GPS with offset %d",
		 * MainActivity.clockOffset)); }
		 */

		this.mLoc = loc;

		// did we enter a new region?
		String oldRegion = this.mRegion;
		String newRegion = getRegion(this.regions, loc);
		
		if (Globals.SIM_MOBILITY && (newRegion != oldRegion)) {
			log("My location updated to: " + loc.getLatitude() + "," + loc.getLongitude() + " ; region: " + newRegion);
		}
		
		if (!oldRegion.equals(newRegion)) {
			regionTransition(oldRegion, newRegion);
		}
		updateDisplay();
	}
	

	/** Location - location changed */
	@Override
	public void onLocationChanged(Location loc) {
		locationChanged(loc, false);
	}

	public void offerReservationIfInUse(String oldRegion) {
		long now = getTime();
		if (this.reservationsInUse.containsKey(oldRegion)) {
			ResRequest oldRes = this.reservationsInUse.remove(oldRegion);
			if (oldRes.type == ResRequest.PENALTY) {
				// Penalty reservation expires in 10 min
				oldRes.hardDeadline = now + 600000;
				this.penalties.add(oldRes);
			} else {
				oldRes.hardDeadline = now
						+ Globals.REQUEST_DIRECT_PUT_DEADLINE_FROM_NOW;
				this.offers.add(oldRes);
			}
		}
	}

	public boolean canDriveOn(String newRegion) {
		return queueKeySet(offers).contains(newRegion)
				|| this.reservationsInUse.containsKey(newRegion);
	}

	
	public void regionTransition(String oldRegion, String newRegion) {
		this.mRegion = newRegion;
		
		// Log whether old reservation was a penalty reservation or not
		if (this.reservationsInUse.containsKey(oldRegion)) {
			ResRequest oldRes = this.reservationsInUse.get(oldRegion);
			if (oldRes.type == ResRequest.PENALTY) {
				log(String.format("exited region %s with PENALTY reservation", oldRegion));
			} else {
				log(String.format("exited region %s with VALID reservation", oldRegion));
			}
		}

		// Offer old reservation
		offerReservationIfInUse(oldRegion);

		// Check for reservation
		if (FreeRegions.contains(newRegion)) {
			log(String.format("Moved from %s to %s, no reservation needed.",
					oldRegion, newRegion));
		} else if (this.reservationsInUse.containsKey(newRegion)) {
			log(String.format(
					"Moved from %s to %s, reservation from in-use store.",
					oldRegion, newRegion));
		} else if (queueKeySet(offers).contains(newRegion)) {
			log(String.format(
					"Moved from %s to %s, reservation from offer store.",
					oldRegion, newRegion));
			ResRequest res = queuePoll(offers, newRegion);
			if (res != null) {
				this.reservationsInUse.put(newRegion, res);
			} else {
				log("ERROR getting reservation from offer store, NULL.");
			}
		} else if (queueKeySet(penalties).contains(newRegion)) {
			log(String
					.format("Moved from %s to %s, penalty already incurred within last 10 minutes.",
							oldRegion, newRegion));
			ResRequest res = queuePoll(penalties, newRegion);
			if (res != null) {
				this.reservationsInUse.put(newRegion, res);
			} else {
				log("ERROR getting reservation from penalty store, NULL.");
			}
		} else {
			log(String
					.format("Moved from %s to %s, no reservation, PENALTY reservation created.",
							oldRegion, newRegion));
			ResRequest penaltyRes = new ResRequest(mId, ResRequest.PENALTY,
					newRegion);
			this.reservationsInUse.put(newRegion, penaltyRes);
		}

		//Navigation speech logic (not for Sim Mobility).
		informUserViaNavSpeech(newRegion);
		
		//Request making logic related to SuperDenseRequests
		handleSuperDenseRequests(newRegion);

		//Update your list of required and offered tokens, under certain conditions. 
		if (Globals.NAV_REQUESTS) {
			if (!Globals.SIM_MOBILITY) {
				//Many of the non-SimMob versions are hard-coded here.
				nonSimMobReservationLogic(newRegion);
				nonSimMobOfferUnused(newRegion);
			}
		}
	}
	
	
	///Use nav-speech (and log) functionality to tell the user where to go.
	///Only if NavSpeech is enabled, and SimMobility is not.
	private void informUserViaNavSpeech(String newRegion) {
		if (!Globals.NAV_SPEECH) { return; }
		if (Globals.SIM_MOBILITY) { return; }

		if (!directionCcw) { // CW Main-Vassar-Mass
			if ("Main-1".equals(newRegion) && canDriveOn("Windsor-1")) {
				log("Divert onto Windsor-1.");
				say("Turn right onto Windsor, then continue to Mass Avenue.");
			}
			if ("Main-2".equals(newRegion) && canDriveOn("Albany-2")) {
				log("Divert onto Albany-2.");
				say("Turn right onto Albany, then continue to Mass Avenue.");
			}
			if ("Main-4".equals(newRegion)) {
				log("Default onto Vassar-1.");
				say("Turn right onto Vassar, then continue to Mass Avenue.");
			}
		} else { // CCW Mass-Vassar-Main
			if ("Mass-1".equals(newRegion) && canDriveOn("Windsor-1")) {
				log("Divert onto Windsor-1.");
				say("Turn left onto Windsor, then continue to Main Street.");
			}
			if ("Mass-2".equals(newRegion) && canDriveOn("Albany-1")) {
				log("Divert onto Albany-1.");
				say("Turn left onto Albany, then continue to Main Street.");
			}
			if ("Mass-3".equals(newRegion)) {
				log("Default onto Vassar-1.");
				say("Turn left onto Vassar, then continue to Main Street.");
			}
		}
	}
	
	private void handleSuperDenseRequests(String newRegion) {
		if (!Globals.SUPER_DENSE_REQUESTS) { return; }
		
		if ("FREE".equals(newRegion)) {
			log("Cleared old pending GETs in FREE.");
			getsPending.clear();
			// Add request to pending queue
			ResRequest r1 = new ResRequest(mId, ResRequest.RES_GET,
					"Stata-1");
			log(String.format("Adding new pending request for %s.",
					r1.regionId));
			// send directly to cloud
			new ResRequestTask().execute(r1, Globals.CLOUD_HOST);
		} else if ("Stata-1".equals(newRegion)) {
			log("Cleared old pending GETs in Stata-1.");
			getsPending.clear();
		}
	}
	
	private void nonSimMobReservationLogic(String newRegion) {
		//ON-DEMAND ADHOC and CLOUD-ONLY reservation logic
		if (!this.adhocEnabled || (this.adhocEnabled && this.onDemand)) {
			if (!directionCcw) { // Main-Vassar-Mass
				if ("Mass-1".equals(newRegion)) {
					log("Cleared old pending GETs.");
					getsPending.clear();
					makeRequest(new ResRequest(mId, ResRequest.RES_GET,
							"Windsor-1"));
				} else if ("Main-1".equals(newRegion)
						&& !canDriveOn("Windsor-1")) {
					log("Cleared old pending GETs.");
					getsPending.clear();
					makeRequest(new ResRequest(mId, ResRequest.RES_GET,
							"Albany-1"));
					makeRequest(new ResRequest(mId, ResRequest.RES_GET,
							"Albany-2"));
				} else if ("Main-2".equals(newRegion)
						&& !canDriveOn("Albany-1")) {
					log("Cleared old pending GETs.");
					getsPending.clear();
					makeRequest(new ResRequest(mId, ResRequest.RES_GET,
							"Vassar-1"));
				}
				// extra logic for clearing old GETs.
				else if ("Mass-2".equals(newRegion)
						|| "Mass-3".equals(newRegion)) {
					log("Cleared old pending GETs.");
					getsPending.clear();
				}
			} else { // Mass-Vassar-Main
				if ("Main-1".equals(newRegion)) {
					log("Cleared old pending GETs.");
					getsPending.clear();
					makeRequest(new ResRequest(mId, ResRequest.RES_GET,
							"Windsor-1"));
				} else if ("Mass-1".equals(newRegion)
						&& !canDriveOn("Windsor-1")) {
					log("Cleared old pending GETs.");
					getsPending.clear();
					makeRequest(new ResRequest(mId, ResRequest.RES_GET,
							"Albany-1"));
					makeRequest(new ResRequest(mId, ResRequest.RES_GET,
							"Albany-2"));
				} else if ("Mass-2".equals(newRegion)
						&& !canDriveOn("Albany-1")) {
					log("Cleared old pending GETs.");
					getsPending.clear();
					makeRequest(new ResRequest(mId, ResRequest.RES_GET,
							"Vassar-1"));
				}
				// extra logic
				else if ("Main-2".equals(newRegion)
						|| "Main-3".equals(newRegion)) {
					log("Cleared old pending GETs.");
					getsPending.clear();
				}
			}
		}
		// PRERESERVE reservation logic
		else {
			if (!directionCcw) { // Main-Vassar-Mass
				if ("Main-1".equals(newRegion)) {
					log("PRERESERVE: Making reservations while in Main-1.");
					makeRequest(new ResRequest(mId, ResRequest.RES_GET,
							"Windsor-1"));
					makeRequest(new ResRequest(mId, ResRequest.RES_GET,
							"Albany-1"));
					makeRequest(new ResRequest(mId, ResRequest.RES_GET,
							"Albany-2"));
					makeRequest(new ResRequest(mId, ResRequest.RES_GET,
							"Vassar-1"));
					say("Please slow down for 1 minute.");
				}
			} else { // Mass-Vassar-Main
				if ("Mass-1".equals(newRegion)) {
					log("PRERESERVE: Making reservations while in Mass-1.");
					makeRequest(new ResRequest(mId, ResRequest.RES_GET,
							"Windsor-1"));
					makeRequest(new ResRequest(mId, ResRequest.RES_GET,
							"Albany-1"));
					makeRequest(new ResRequest(mId, ResRequest.RES_GET,
							"Albany-2"));
					makeRequest(new ResRequest(mId, ResRequest.RES_GET,
							"Vassar-1"));
					say("Please slow down for 1 minute.");
				}
			}
		}
	}
	
	private void nonSimMobOfferUnused(String newRegion) {
		/** PUT and/or offer logic */
		// Offer / PUT unnecessary reservations once we're on a reserved
		// stretch.
		if ("Windsor-1".equals(newRegion)) {
			log("Cleared old pending GETs.");
			getsPending.clear();
			log("PUT all except Windsor-1");
			offerReservationIfInUse("Vassar-1");
			// offerReservationIfInUse("Windsor-1");
			offerReservationIfInUse("Albany-1");
			offerReservationIfInUse("Albany-2");
		} else if ("Albany-1".equals(newRegion)) {
			log("Cleared old pending GETs.");
			getsPending.clear();
			log("PUT all except Albany-1");
			offerReservationIfInUse("Vassar-1");
			offerReservationIfInUse("Windsor-1");
			// offerReservationIfInUse("Albany-1");
			// offerReservationIfInUse("Albany-2");
		} else if ("Vassar-1".equals(newRegion)) {
			log("Cleared old pending GETs.");
			getsPending.clear();
			log("PUT all except Vassar-1");
			// offerReservationIfInUse("Vassar-1");
			offerReservationIfInUse("Windsor-1");
			offerReservationIfInUse("Albany-1");
			offerReservationIfInUse("Albany-2");
		}
	}


	/** Location - provider disabled */
	@Override
	public void onProviderDisabled(String arg0) {
	}

	/** Location - provider disabled */
	@Override
	public void onProviderEnabled(String arg0) {
	}

	/** Location - provider status changed */
	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		switch (status) {
		case LocationProvider.OUT_OF_SERVICE:
			log("LocationProvider out of service, stopping adhoc announcements.");
			// myHandler.removeCallbacks(adhocAnnounceR);
			break;
		case LocationProvider.TEMPORARILY_UNAVAILABLE:
			log("LocationProvider temporarily unavailable, stopping adhoc announcements.");
			// myHandler.removeCallbacks(adhocAnnounceR);
			break;
		case LocationProvider.AVAILABLE:
			log("LocationProvider available, starting adhoc announcements.");
			// myHandler.postDelayed(adhocAnnounceR,
			// Globals.ADHOC_ANNOUNCE_PERIOD);
			break;
		}
	}
}

