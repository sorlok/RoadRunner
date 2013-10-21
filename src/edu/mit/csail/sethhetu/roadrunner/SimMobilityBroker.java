//Copyright (c) 2013 Singapore-MIT Alliance for Research and Technology
//Licensed under the terms of the MIT License, as described in the file:
//   license.txt   (http://opensource.org/licenses/MIT)

package edu.mit.csail.sethhetu.roadrunner;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.net.Inet4Address;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import android.os.Handler;
import android.util.Base64;
import edu.mit.csail.jasongao.roadrunner.*;
import edu.mit.csail.jasongao.roadrunner.RoadRunnerService.AdHocAnnouncer;
import edu.mit.csail.jasongao.roadrunner.RoadRunnerService.LocationSpoofer;
import edu.mit.csail.sethhetu.roadrunner.SimMobServerConnectTask.PostExecuteAction;
import edu.mit.smart.sm4and.Connector;
import edu.mit.smart.sm4and.MessageHandlerFactory;
import edu.mit.smart.sm4and.MessageParser;
import edu.mit.smart.sm4and.handler.AndroidHandlerFactory;
import edu.mit.smart.sm4and.handler.MulticastHandler.MulticastMessage;
import edu.mit.smart.sm4and.json.JsonMessageParser;
import edu.mit.smart.sm4and.mina.MinaConnector;

/**
 * The SimMobilityBroker is used by RoadRunner to communicate with Sim Mobility. 
 * The basic structure of this communication is as follows:
 *     1) Android entity establishes the connection (similar to how it connects to a Cloud).
 *     2) Sim Mobility signals that the connection was successful. 
 *     3) Sim Mobility signals that time tick 1 is complete.
 *        A) Any number of additional messages may be bundled with this update.
 *     4) Android entity confirms that time tick 1 is complete.
 *        A) Any number of additional messages may be bundled with this update.
 *     5) Steps 3 and 4 continue for time ticks 2, 3, 4, etc.
 * This behavior can be enabled by setting Globals.SIM_MOBILITY to "true".
 * 
 * @author Seth N. Hetu
 * 
 * TODO: At the moment, the Broker will handle all messages via the post-back methods
 *       of the MinaConnector, meaning that it will NOT operate in lock step.
 *       Changing this requires modifying the fundamental underlying architecture.
 */
public class SimMobilityBroker  implements PostExecuteAction {	
	//We need to maintain an open connection to the Sim Mobility server, since we are in a 
	//  tight time loop.
	//private Socket smSocket;
	Connector conn;
	
	//Open streams for communicating with the server.
	//TODO: Currently communication is decentralized and asynchronous, thus difficult to 
	//      debug. We might want to change how we organize things.
	//private BufferedReader reader;
	//private BufferedWriter writer;
	
	//For communicating back to the RoadRunner service.
	private Handler myHandler;
	private LoggerI logger;
	private AdHocAnnouncer adhoc;
	private LocationSpoofer locspoof;
	
	//What's the time according to Sim Mobility?
	private long currTimeMs;
	
	//We send announce packets every 2 seconds (2000 ms)
	private long lastAnnouncePacket;
	
	//Let's make this non-deterministic.
	private static Random RandGen = new Random();
	
	//Returned messages.
	//private ArrayList<String> returnedMessages;
	
	//Same as the one in RoadRunnerService
	private String uniqueId;
	
	private MessageParser messageParser;
	private MessageHandlerFactory handlerFactory;
	
	private static final long ToU(byte b) {
		return ((int)b)&0xFF;
	}
	
	public static final long GenerateIdFromInet(Inet4Address addr) {
		//If we've got a null address, just fake it.
		byte[] elements = null;
		if (addr!=null) {
			elements = addr.getAddress();
		} else {
			elements = new byte[4];
			SimMobilityBroker.RandGen.nextBytes(elements);
		}
		
		//Try to make something semi-recognizable:
		long res = ToU(elements[0])*1000000000
				 + ToU(elements[1])*1000000
				 + ToU(elements[2])*1000
				 + ToU(elements[3]);
		return res;
	}
	

	@Override
	public void onPostExecute(Exception thrownException, BufferedReader reader, BufferedWriter writer) {
		//Was there an error?
		if (thrownException!=null) {
			SimMobilityBroker.this.closeStreams();
			throw new RuntimeException(thrownException);
		}
		
		//Save objects locally.
		//TODO: reader and writer are currently null.
		//this.reader = reader;
		//this.writer = writer;
				
		//Assuming everything went ok, start our simulation loop. It looks like this:
		//   1) Send out an Async task waiting for the "tick done" message.
		//   2) When that's done, process it and send "android done" for this time step.
		//As always, messages are sent before the final "done" message.
		this.currTimeMs = 0;
		this.lastAnnouncePacket = -1 * Globals.ADHOC_ANNOUNCE_PERIOD; //Immediately dispatch an announce packet.
		//new SimMobTickRequest(myHandler, new ServerTickDoneRunnable()).execute(reader);
	}
	
	public class TimeAdvancer {
		public void advance(int elapsedMs) {
	        if (elapsedMs<=0) {
	        	throw new RuntimeException("Error: elapsed time cannot be negative.");
	        }
	        
			currTimeMs += elapsedMs;
			
			//Time for an announce packet?
			if (currTimeMs-lastAnnouncePacket >= Globals.ADHOC_ANNOUNCE_PERIOD) {
				lastAnnouncePacket += Globals.ADHOC_ANNOUNCE_PERIOD;
				
				//Send announce packet!
				adhoc.announce(false);
			}
		}
	}
	
	public class MultiCastReceiver {
		public void receive(String id, String base64Data) {
			//Ignore messages sent to yourself.
			if (id.equals(uniqueId)) {
				logger.log("Ignoring packet sent to self.");
				return;
			}
			
			//Extract the packet.
			byte[] packet = string2bytes(base64Data);
			AdhocPacket p = AdhocPacketThread.ReadPacket(logger, packet, packet.length);
			
			//Send it to Road Runner's message loop as a ADHOC_PACKET_RECV.
			myHandler.obtainMessage(RoadRunnerService.ADHOC_PACKET_RECV, p).sendToTarget();
		}
	}
	
	
	/**
	 * Create the broker entity and connect to the server.
	 */
	public SimMobilityBroker(String uniqueId, Handler myHandler, LoggerI logger, AdHocAnnouncer adhoc, LocationSpoofer locspoof) {
		this.messageParser = new JsonMessageParser();
		this.myHandler = myHandler;
		this.logger = logger;
		this.adhoc = adhoc;
		this.locspoof = locspoof;
		
		//Check that we have a unique ID.
		this.uniqueId = uniqueId;
		if (uniqueId==null) { throw new RuntimeException("Unique Id cannot be null."); }
		
		this.handlerFactory = new AndroidHandlerFactory(uniqueId, locspoof, new TimeAdvancer(), new MultiCastReceiver());
		this.conn = new MinaConnector(myHandler, messageParser, handlerFactory, locspoof, logger);
				
		//Connect our socket.
		//NOTE: Currently, this task will *only* end if the session is closed. 
		SimMobServerConnectTask task = new SimMobServerConnectTask(this, this.handlerFactory);
		task.execute(this.conn);
	}
	
	/**
	 * Convert a byte array to a String, escaping the semicolons.
	 */
	private static String bytes2string(byte[] bytes) {
		//First, just convert using Base64
		String raw = Base64.encodeToString(bytes, Base64.NO_WRAP);
		
		//Now escape as follows:
		//  . becomes ".."
		//  ; becomes ".1"
		//  : becomes ".2"
		//  \n becomes ".3"
		//(some of these are leftover from UTF-8, and won't occur in Base64)
		StringBuilder res = new StringBuilder();
		for (int i=0; i<raw.length(); i++) {
			char c = raw.charAt(i);
			if (c=='.') { res.append(".."); }
			else if (c==';') { res.append(".1"); }
			else if (c==':') { res.append(".2"); }
			else if (c=='\n') { res.append(".3"); }
			else { res.append(c); }
		}
		return res.toString();
	}
	
	/**
	 * Convert a String to a byte array, un-escaping the semicolons.
	 */
	private static byte[] string2bytes(String str) {
		//First, remove our escape sequences.
		StringBuilder unescaped = new StringBuilder();
		for (int i=0; i<str.length(); i++) {
			char c = str.charAt(i);
			if (c=='.') {
				char next = str.charAt(i+1);
				if (next=='.') { unescaped.append("."); }
				else if (next=='1') { unescaped.append(";"); }
				else if (next=='2') { unescaped.append(":"); }
				else if (next=='3') { unescaped.append("\n"); }
				else { throw new RuntimeException("Bad escape sequence."); }
			} else {
				unescaped.append(c);
			}
		}
		
		//Next, just convert using Base64
		return Base64.decode(unescaped.toString(), Base64.NO_WRAP);
	}
	
	
	
	/**
	 * Generate a random set of tokens (usually sampled from 'A' through 'Z').
	 */
	public static Set<String> RandomTokens(String token_range, int lower, int upper) {
		Set<String> res = new HashSet<String>();
		
		//How many tokens?
		int numTokens = RandGen.nextInt(upper-lower)+lower;		
		for (int i=0; i<numTokens; i++) {
			//Which letter?
			res.add(Character.toString(token_range.charAt(RandGen.nextInt(token_range.length()))));
		}
		
		return res;
	}
	
	
	/**
	 * We don't use speed and heading in our Locations, so I'm replace "viable" with a 
	 *  simple probability test, rather than take the risk that *all* agents will determine
	 *  that they aren't viable to each other.
	 */
	public boolean linkIsViable() {
		return RandGen.nextDouble() <= Globals.SM_VIABILITY_PERCENT;
	}
	
	
	/**
	 * Spoof regions too. Returns null for no region (e.g., "FREE")
	 */
	public String spoofRandomRegion() {
		if (RandGen.nextDouble() <= Globals.SM_FREE_REGION_PERCENT) { return null; }
		return Character.toString((Globals.SM_TOKEN_RANGE.charAt(RandGen.nextInt(Globals.SM_TOKEN_RANGE.length()))));
	}
	
	
	//TODO: This does nothing while reader/writer are null.
	private void closeStreams() {
		/*try {
			if (this.reader!=null) { this.reader.close(); }
		} catch (IOException e2) {}
		try {
			if (this.writer!=null) { this.writer.close(); }
		} catch (IOException e2) {}*/
	}
	
	
	//Buffer this packet for broadcast at the end of the current time tick.
	public void sendBroadcastPacket(String myId, byte[] packet) {
		//A "broadcast" packet looks something like this:
		//ANDROID_BROADCAST:ID:[DATA]
		//...where "DATA" is just a byte-array serialized in some form and opaque to Sim Mobility
		//   (it just passes it along), and "ID" is the ID of the emulator in question, so we
		//   can ignore packets to ourselves.
		//We use brackets around DATA to help ensure that we're not deserializing random junk.
		if (myId==null || packet==null) { throw new RuntimeException("Can't broadcast without data or an id."); }
		
		//Prepare the packet.
		MulticastMessage obj = new MulticastMessage();
		obj.SENDER = String.valueOf(uniqueId);
		obj.SENDER_TYPE = "ANDROID_EMULATOR";
		obj.MULTICAST_DATA = bytes2string(packet);
		
		//Save it for later.
		conn.addMessage(obj);
	}
	
	
	/*private synchronized void bufferMessage(String msg) {
		returnedMessages.add(msg);
	}*/
	
	
	//Called when the server states that a time tick has completed.
	public class ServerTickDoneRunnable implements Runnable {
		//The line received from the server.
		private String line;
		
		public ServerTickDoneRunnable() {}
		
		public void setLine(String line) {
			this.line = line;
		}
		
		public void run() {
			if (line==null) { throw new RuntimeException("ServerTick line ignored!"); }
			
			//Handle ticks last
			int curr_tick_len = 0;
			
			//Each communication from the server can span multiple messages, separated by ";"
			String[] messages = line.split(";");
			for (String msg : messages) {
				//A trailing ";" may generate an empty message; that's ok.
				msg = msg.trim();
				if (msg.isEmpty()) { continue; }
				
				//Messages are usually defined as "TYPE:BODY", where body is type-defined. 
				String[] temp = msg.split(":");
				if (temp.length!=2) { throw new RuntimeException("Unexpected message: \"" + msg + "\""); }
				
				//Dispatch on the type. Switches are for losers!
				String type = temp[0];
				String body = temp[1];
				if (type.equals("SM_TICK_DONE")) {
					//body="curr_tick_len", in ms
					curr_tick_len = Integer.parseInt(body);
				} else if (type.equals("LOC_UPDATE")) {
					//body="lat,lng", in N/E latitude/longitude coordinates
					String[] latlng = body.split(",");
					if (latlng.length!=2) { throw new RuntimeException("lat/lng pair missing"); }
					double lat = Double.parseDouble(latlng[0]);
					double lng = Double.parseDouble(latlng[1]);
					
					//Propagate.
					locspoof.setLocation(lat, lng);
				} else if (type.equals("SM_ADHOC_BROADCAST")) {
					
					throw new RuntimeException("Disabled");
					
					
					//body="ag_id,[packet]"
					//The result of an ad-hoc announce message.
					/*String[] parts = body.split(",", 2);
					if (parts.length!=2) { throw new RuntimeException("Bad broadcast message body: " + body); }
					
					//Get the ID of the agent sending this message, along with the packet.
					String agId = parts[0];
					String packet = parts[1];
					if (packet.charAt(0)!='[' || packet.charAt(packet.length()-1)!=']') {
						throw new RuntimeException("Incorrect broadcast packet string: " + packet);
					}
					
					//Ignore packets sent to yourself.
					if (agId==uniqueId) {
						logger.log("Ignoring packet sent to self.");
						continue;
					}
					
					//Extract the packet.
					packet = packet.substring(1, packet.length()-1);
					byte[] packetB = string2bytes(packet);
					AdhocPacket p = AdhocPacketThread.ReadPacket(logger, packetB, packetB.length);
					
					
					myHandler.obtainMessage(
							RoadRunnerService.ADHOC_PACKET_RECV, p).sendToTarget();
					
					*/
					//TODO
				} else {
					throw new RuntimeException("Unknown message type: \"" + type + "\""); 
				}
			}
			
			//Move the tick forward.
			if (curr_tick_len==0) {
				throw new RuntimeException("Server sent messages, but no tick update!");
			} else {
				currTimeMs += curr_tick_len;
				
				//Time for an announce packet?
				if (currTimeMs-lastAnnouncePacket >= Globals.ADHOC_ANNOUNCE_PERIOD) {
					lastAnnouncePacket += Globals.ADHOC_ANNOUNCE_PERIOD;
					
					//Send announce packet!
					adhoc.announce(false);
				}
			}
			
			//TODO: It's *possible* that "returnedMessages" may receive messages for 
			//      a time tick that has already passed, but only if they result from the 
			//      Android app (e.g., "every 2 seconds" the old way). We would ideally 
			//      remove these, but they are effectively harmless (and *shouldn't* really
			//      happen in practice).
			
			throw new RuntimeException("NO LONGER SUPPORTED.");
			
			/*StringBuilder sb = new StringBuilder();
			synchronized (this) {
				//Combine all messages.
				String sep = "";
				for (String msg : returnedMessages) {
					//TODO: This is risky; the "packet" in a broadcast may randomly contain a ";". 
					//      We can reduce/eliminate the risk of this later via escaping or choosing a better
					//      control code. For now we just close the emulator.
					if (msg.contains(";")) { throw new RuntimeException("Message contains a separator character: \"" + msg + "\""); }
					sb.append(sep+msg);
					sep = ";";
				}
				
				//Empty the list.
				returnedMessages.clear();
			}
			
			//Send all messages.
			BufferedWriter writer = null; //TEMP
			new SimMobTickResponse(sb.toString()).execute(writer);*/
		}
	};
	
	
	
	
	
	
	

}
