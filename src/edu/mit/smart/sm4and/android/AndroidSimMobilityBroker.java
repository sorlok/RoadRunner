//Copyright (c) 2013 Singapore-MIT Alliance for Research and Technology
//Licensed under the terms of the MIT License, as described in the file:
//   license.txt   (http://opensource.org/licenses/MIT)

package edu.mit.smart.sm4and.android;

import java.io.BufferedReader;
import java.io.BufferedWriter;

import android.os.Handler;
import edu.mit.csail.jasongao.roadrunner.*;
import edu.mit.csail.jasongao.roadrunner.RoadRunnerService.AdHocAnnouncer;
import edu.mit.csail.jasongao.roadrunner.RoadRunnerService.LocationSpoofer;
import edu.mit.csail.jasongao.roadrunner.RoadRunnerService.PathSetter;
import edu.mit.csail.jasongao.roadrunner.RoadRunnerService.RegionChecker;
import edu.mit.csail.jasongao.roadrunner.util.LoggerI;
import edu.mit.csail.jasongao.roadrunner.util.LoggingRuntimeException;
import edu.mit.smart.sm4and.SimMobilityBroker;
import edu.mit.smart.sm4and.android.HandleOnMainThread;
import edu.mit.smart.sm4and.android.SimMobServerConnectTask.PostExecuteAction;
import edu.mit.smart.sm4and.connector.Connector;
import edu.mit.smart.sm4and.connector.MinaConnector;
import edu.mit.smart.sm4and.handler.AbstractMessageHandler;
import edu.mit.smart.sm4and.handler.LocationHandler;
import edu.mit.smart.sm4and.handler.MessageHandlerFactory;
import edu.mit.smart.sm4and.handler.MulticastHandler;
import edu.mit.smart.sm4and.handler.ReadyHandler;
import edu.mit.smart.sm4and.handler.ReadyToReceiveHandler;
import edu.mit.smart.sm4and.handler.TimeHandler;
import edu.mit.smart.sm4and.handler.UnicastHandler;
import edu.mit.smart.sm4and.handler.WhoAreYouHandler;
import edu.mit.smart.sm4and.message.DefaultMessageTypes.LocationMessage;
import edu.mit.smart.sm4and.message.DefaultMessageTypes.MulticastMessage;
import edu.mit.smart.sm4and.message.DefaultMessageTypes.ReadyMessage;
import edu.mit.smart.sm4and.message.DefaultMessageTypes.ReadyToReceiveMessage;
import edu.mit.smart.sm4and.message.DefaultMessageTypes.TimeMessage;
import edu.mit.smart.sm4and.message.DefaultMessageTypes.UnicastMessage;
import edu.mit.smart.sm4and.message.DefaultMessageTypes.WhoAreYouMessage;
import edu.mit.smart.sm4and.json.ByteArraySerialization;
import edu.mit.smart.sm4and.json.JsonMessageParser;
import edu.mit.smart.sm4and.message.Message;
import edu.mit.smart.sm4and.message.MessageParser;

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
public class AndroidSimMobilityBroker extends SimMobilityBroker {
	//Have we started yet?
	protected boolean activated = false;
	
	//We need to maintain an open connection to the Sim Mobility server, since we are in a 
	//  tight time loop.
	protected Connector conn;
	
	//For communicating back to the RoadRunner service.
	protected Handler myHandler;
	protected LoggerI logger;
	protected AdHocAnnouncer adhoc;
	protected LocationSpoofer locspoof;
	protected RegionChecker regcheck;
	
	
	/**
	 * Register a custom message type and corresponding callback action.  
	 * This callback will be triggered if a message of the given type is received.
	 * @param msgType The unique identifier of the Message to handle.
	 * @param msgClass The type of this message (e.g., MyCustomMessage.class). This will 
	 *        be parsed via Gson into an appropriate message.
	 * @param handler The handler to be called when this message arrives.
	 */
	public void addCustomMessageType(String msgType, Class<? extends Message> msgClass, AbstractMessageHandler handler) {
		if (handlerFactory==null || messageParser==null) { 
			throw new LoggingRuntimeException("Cannot add a custom message type; the Sim Mobility Broker has not been initialized yet.");
		}
		
		//Add the message type.
		messageParser.addMessagetype(msgType, msgClass);

		//Add the handler.
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
	
	protected MessageParser makeJsonMessageParser() {
		 MessageParser res = new JsonMessageParser();
		 res.addMessagetype(Message.Type.WHOAREYOU, WhoAreYouMessage.class);
		 res.addMessagetype(Message.Type.TIME_DATA, TimeMessage.class);
		 res.addMessagetype(Message.Type.READY, ReadyMessage.class);
		 res.addMessagetype(Message.Type.LOCATION_DATA, LocationMessage.class);
		 res.addMessagetype(Message.Type.MULTICAST, MulticastMessage.class);
		 res.addMessagetype(Message.Type.UNICAST, UnicastMessage.class);
		 res.addMessagetype(Message.Type.READY_TO_RECEIVE, ReadyToReceiveMessage.class);
		 return res;
	}
	
	//We send announce packets every 2 seconds (2000 ms)
	private long lastAnnouncePacket;
	
	//We check the current route's token feasibilty every N + rand(0,M) seconds.
	private long nextRegionRerouteCheck;
	
	private MessageParser messageParser;
	private MessageHandlerFactory handlerFactory;
	
	
	/**
	 * Initialize the Broker. Can be called multiple times, until "Connect" is called.
	 */
	public void initialize(String uniqueId, Handler myHandler, LoggerI logger, AdHocAnnouncer adhoc, LocationSpoofer locspoof, PathSetter pathSet, RegionChecker regcheck) {
		if (this.activated) {
			throw new LoggingRuntimeException("Can't re-initialize; SimMobilityBroker has already been activated.");
		}
		
		this.messageParser = makeJsonMessageParser();
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
		myHandler.post(new HandleOnMainThread(handler, message, conn, messageParser));
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
			AndroidSimMobilityBroker.this.currTimeMs = 0;
			AndroidSimMobilityBroker.this.lastAnnouncePacket = -1 * Globals.ADHOC_ANNOUNCE_PERIOD; //Immediately dispatch an announce packet.
			AndroidSimMobilityBroker.this.nextRegionRerouteCheck = calcNextRegionRerouteCheck();  //Schedule like normal (includes a bit of randomness).
		}
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
	

	public void postMessage(Message obj) {
        //Append it.
        conn.addMessage(obj);
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
		MulticastMessage obj = new MulticastMessage(uniqueId, ByteArraySerialization.Serialize(packet));
		
		//Save it for later.
		conn.addMessage(obj);
	}
	
    //Helper: Same, but a bit easier to use given an object.
    public void ReflectToServer(String msg) {
    	SimMobilityBroker.ReflectToServer(conn, uniqueId, msg);
    }

}
