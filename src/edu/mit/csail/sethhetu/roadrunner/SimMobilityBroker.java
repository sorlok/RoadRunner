//Copyright (c) 2013 Singapore-MIT Alliance for Research and Technology
//Licensed under the terms of the MIT License, as described in the file:
//   license.txt   (http://opensource.org/licenses/MIT)

package edu.mit.csail.sethhetu.roadrunner;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.util.Random;

import android.os.Handler;
import edu.mit.csail.jasongao.roadrunner.*;
import edu.mit.csail.jasongao.roadrunner.RoadRunnerService.AdHocAnnouncer;
import edu.mit.csail.jasongao.roadrunner.RoadRunnerService.LocationSpoofer;
import edu.mit.csail.jasongao.roadrunner.RoadRunnerService.PathSetter;
import edu.mit.csail.jasongao.roadrunner.RoadRunnerService.RegionChecker;
import edu.mit.csail.jasongao.roadrunner.ext.SendRegionHandler.RemoteLogMessage;
import edu.mit.csail.jasongao.roadrunner.ext.SendRegionHandler.RerouteRequest;
import edu.mit.csail.sethhetu.roadrunner.SimMobServerConnectTask.PostExecuteAction;
import edu.mit.csail.sethhetu.roadrunner.impl.SimMobilityBrokerImpl;
import edu.mit.smart.sm4and.AbstractMessageHandler;
import edu.mit.smart.sm4and.Connector;
import edu.mit.smart.sm4and.MessageHandlerFactory;
import edu.mit.smart.sm4and.MessageParser;
import edu.mit.smart.sm4and.handler.LocationHandler;
import edu.mit.smart.sm4and.handler.MulticastHandler;
import edu.mit.smart.sm4and.handler.ReadyHandler;
import edu.mit.smart.sm4and.handler.ReadyToReceiveHandler;
import edu.mit.smart.sm4and.handler.SimpleAndroidHandler;
import edu.mit.smart.sm4and.handler.TimeHandler;
import edu.mit.smart.sm4and.handler.UnicastHandler;
import edu.mit.smart.sm4and.handler.WhoAreYouHandler;
import edu.mit.smart.sm4and.handler.MulticastHandler.MulticastMessage;
import edu.mit.smart.sm4and.json.JsonMessageParser;
import edu.mit.smart.sm4and.message.Message;
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
public class SimMobilityBroker extends SimMobilityBrokerImpl {
	//Singleton stuff
	private static SimMobilityBroker instance = new SimMobilityBroker();
	private SimMobilityBroker() {}
	public static SimMobilityBroker getInstance() {
		return instance;
	}
	
	/**
	 * Register a custom MessageHandler with the current MessageHandlerFactory.
	 * This handler will be called if a message of the given type is received.
	 * @param msgType The unique identifier of the Message to handle.
	 * @param handler The handler to be called when this message arrives.
	 */
	public void addCustomMessageHandler(String msgType, AbstractMessageHandler handler) {
		if (handlerFactory==null) { return; }
		handler.setBroker(this);
		handlerFactory.addCustomHandler(msgType, handler);
	}
	
	
	/**
	 * Create a MessageHandlerFactory with the appropriate callbacks for Android messages.
	 * @return
	 */
	protected MessageHandlerFactory makeAndroidHandlerFactory(String clientId, LocationSpoofer locSpoof) {
		//Set up our list of default handlers.
		MessageHandlerFactory res = new MessageHandlerFactory();
		res.addDefaultHandler(Message.Type.WHOAREYOU, new WhoAreYouHandler(clientId), this);
		res.addDefaultHandler(Message.Type.TIME_DATA, new TimeHandler(new TimeAdvancer()), this);
		res.addDefaultHandler(Message.Type.READY, new ReadyHandler(), this);
		res.addDefaultHandler(Message.Type.LOCATION_DATA, new LocationHandler(locSpoof), this);
		res.addDefaultHandler(Message.Type.MULTICAST, new MulticastHandler(new MultiCastReceiver()), this);
		res.addDefaultHandler(Message.Type.UNICAST, new UnicastHandler(), this);
		res.addDefaultHandler(Message.Type.READY_TO_RECEIVE, new ReadyToReceiveHandler(clientId), this);
		return res;
	}
	
	
	
	//What's the time according to Sim Mobility?
	private long currTimeMs;
	
	//We send announce packets every 2 seconds (2000 ms)
	private long lastAnnouncePacket;
	
	//We check the current route's token feasibilty every N + rand(0,M) seconds.
	private long nextRegionRerouteCheck;
	
	//Let's make this non-deterministic.
	private static Random RandGen = new Random();
	public Random getRand() { return SimMobilityBroker.RandGen; }
	
	//Synced to the "m_id" field in RoadRunner.
	private String uniqueId;
	public String getUniqueId() { return uniqueId; }
	
	private MessageParser messageParser;
	private MessageHandlerFactory handlerFactory;
	
	
	/**
	 * Initialize the Broker. Can be called multiple times, until "Connect" is called.
	 */
	public void initialize(String uniqueId, Handler myHandler, LoggerI logger, AdHocAnnouncer adhoc, LocationSpoofer locspoof, PathSetter pathSet, RegionChecker regcheck) {
		if (this.activated) {
			throw new LoggingRuntimeException("Can't re-initialize; SimMobilityBroker has already been activated.");
		}
		
		this.messageParser = new JsonMessageParser();
		this.myHandler = myHandler;
		this.logger = logger;
		this.adhoc = adhoc;
		this.locspoof = locspoof;
		this.regcheck = regcheck;
		
		//Check that we have a unique ID.
		this.uniqueId = uniqueId;
		if (uniqueId==null) { throw new LoggingRuntimeException("Unique Id cannot be null."); }
		
		this.handlerFactory = makeAndroidHandlerFactory(uniqueId, locspoof);
		this.conn = new MinaConnector(this, handlerFactory);
	}
	
	public MessageParser getParser() {
		return messageParser;
	}
	
	//Handle a Message as received from MINA.
	public void handleMessage(AbstractMessageHandler handler, Message message) {
		//We want to process this on the main thread, as we may want to interact with the user.
		//Thus, we post it to the message queue.
		SimpleAndroidHandler sam = new SimpleAndroidHandler(handler, message, conn, messageParser);
		myHandler.post(sam);
	}
	
	
	/**
	 * Start the SimMobility Broker (sets it to "active" and connects to the server).
	 * initialize() must be called first.
	 */
	public void activate() {
		//Silently skip if called twice.
		if (this.activated) { return; }
		
		//Connect our socket.
		//NOTE: Currently, this task will *only* end if the session is closed. 
		SimMobServerConnectTask task = new SimMobServerConnectTask(new OnConnectAction(), this.handlerFactory, logger);
		task.execute(this.conn);
		this.activated = true;
	}
	
	public boolean isActive() {
		return activated;
	}
	
	
	private class OnConnectAction implements PostExecuteAction {
		@Override
		public void onPostExecute(Exception thrownException, BufferedReader reader, BufferedWriter writer) {
			//Was there an error?
			if (thrownException!=null) {
				throw new LoggingRuntimeException(thrownException);
			}
					
			//Assuming everything went ok, start our simulation loop. It looks like this:
			//   1) Send out an Async task waiting for the "tick done" message.
			//   2) When that's done, process it and send "android done" for this time step.
			//As always, messages are sent before the final "done" message.
			SimMobilityBroker.this.currTimeMs = 0;
			SimMobilityBroker.this.lastAnnouncePacket = -1 * Globals.ADHOC_ANNOUNCE_PERIOD; //Immediately dispatch an announce packet.
			SimMobilityBroker.this.nextRegionRerouteCheck = calcNextRegionRerouteCheck();  //Schedule like normal (includes a bit of randomness).
		}
	}
	
	public long getCurrTimeMs() {
		return currTimeMs;
	}
	
	private long calcNextRegionRerouteCheck() {
		return RandGen.nextInt(Globals.SM_REROUTE_CHECK_ADDITIVE) + Globals.SM_REROUTE_CHECK_BASE;
	}
	
	public class TimeAdvancer {
		public void advance(int tick, int elapsed_ms) {
	        if (elapsed_ms<=0) {
	        	throw new LoggingRuntimeException("Error: elapsed time cannot be negative.");
	        }
	        
	        //Advance
			currTimeMs += elapsed_ms;
			
/////////////////////////
/// TODO: These two checks should be realized with our new "Timer" class which 
//        mimics the Android timer.
/////////////////////////
			
			//Time for an announce packet?
			if (currTimeMs-lastAnnouncePacket >= Globals.ADHOC_ANNOUNCE_PERIOD) {
				lastAnnouncePacket += Globals.ADHOC_ANNOUNCE_PERIOD;
				
				//Send announce packet!
				adhoc.announce(false);
			}
			
			//Time for a Region rerouting check?
			nextRegionRerouteCheck -= currTimeMs;
			if (nextRegionRerouteCheck <= 0) {
				//Carry over the remainder.
				long nextCheck = calcNextRegionRerouteCheck();
				nextRegionRerouteCheck += nextCheck;
				
				//Perform a region check.
				regcheck.checkAndReroute();
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
			byte[] packet = ByteArraySerialization.Deserialize(base64Data);
			AdhocPacket p = AdhocPacketThread.ReadPacket(logger, packet, packet.length);
			
			//Send it to Road Runner's message loop as a ADHOC_PACKET_RECV.
			myHandler.obtainMessage(RoadRunnerService.ADHOC_PACKET_RECV, p).sendToTarget();
		}
	}
	

	public void requestReroute(String blacklistRegion) {
		RerouteRequest obj = new RerouteRequest();
		obj.blacklist_region = blacklistRegion;
        obj.SENDER_TYPE = "ANDROID_EMULATOR";
        obj.SENDER = uniqueId;
        
        //Append it.
        conn.addMessage(obj);

        //Remote-log all rerouting requests.
        SimMobilityBroker.ReflectToServer(conn, uniqueId, "Requested re-route from Sim Mobility, blacklisting Region: " + blacklistRegion);
	}
	
	
	//Buffer this packet for broadcast at the end of the current time tick.
	public void sendBroadcastPacket(String myId, byte[] packet) {
		//A "broadcast" packet looks something like this:
		//ANDROID_BROADCAST:ID:[DATA]
		//...where "DATA" is just a byte-array serialized in some form and opaque to Sim Mobility
		//   (it just passes it along), and "ID" is the ID of the emulator in question, so we
		//   can ignore packets to ourselves.
		//We use brackets around DATA to help ensure that we're not deserializing random junk.
		if (myId==null || packet==null) { throw new LoggingRuntimeException("Can't broadcast without data or an id."); }
		
		//Prepare the packet.
		MulticastMessage obj = new MulticastMessage();
		obj.SENDER = String.valueOf(uniqueId);
		obj.SENDER_TYPE = "ANDROID_EMULATOR";
		obj.MULTICAST_DATA = ByteArraySerialization.Serialize(packet);
		
		//Save it for later.
		conn.addMessage(obj);
	}

	
	//Called when the server states that a time tick has completed.
	public class ServerTickDoneRunnable implements Runnable {
		//The line received from the server.
		private String line;
		
		public ServerTickDoneRunnable() {}
		
		public void setLine(String line) {
			this.line = line;
		}
		
		public void run() {
			if (line==null) { throw new LoggingRuntimeException("ServerTick line ignored!"); }
			
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
				if (temp.length!=2) { throw new LoggingRuntimeException("Unexpected message: \"" + msg + "\""); }
				
				//Dispatch on the type. Switches are for losers!
				String type = temp[0];
				String body = temp[1];
				if (type.equals("SM_TICK_DONE")) {
					//body="curr_tick_len", in ms
					curr_tick_len = Integer.parseInt(body);
				} else if (type.equals("LOC_UPDATE")) {
					//body="lat,lng", in N/E latitude/longitude coordinates
					String[] latlng = body.split(",");
					if (latlng.length!=2) { throw new LoggingRuntimeException("lat/lng pair missing"); }
					double lat = Double.parseDouble(latlng[0]);
					double lng = Double.parseDouble(latlng[1]);
					
					//Propagate.
					locspoof.setLocation(lat, lng);
				} else if (type.equals("SM_ADHOC_BROADCAST")) {					
					throw new LoggingRuntimeException("Disabled");
				} else {
					throw new LoggingRuntimeException("Unknown message type: \"" + type + "\""); 
				}
			}
			
			//Move the tick forward.
			if (curr_tick_len==0) {
				throw new LoggingRuntimeException("Server sent messages, but no tick update!");
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
			throw new LoggingRuntimeException("NO LONGER SUPPORTED.");
		}
	};

	
	
	//Helper: Log to server (remote)
    public static void ReflectToServer(Connector connector, String clientID, String msg) {
        //Prepare a response.
    	RemoteLogMessage obj = new RemoteLogMessage();
        obj.log_message = msg;
        obj.SENDER_TYPE = "ANDROID_EMULATOR";
        obj.SENDER = clientID;
        
        //Append it.
        connector.addMessage(obj);
    }
	
    //Helper: Same, but a bit easier to use given an object.
    public void ReflectToServer(String msg) {
    	SimMobilityBroker.ReflectToServer(conn, uniqueId, msg);
    }

}
