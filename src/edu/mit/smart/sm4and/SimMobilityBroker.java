//Copyright (c) 2013 Singapore-MIT Alliance for Research and Technology
//Licensed under the terms of the MIT License, as described in the file:
//   license.txt   (http://opensource.org/licenses/MIT)

package edu.mit.smart.sm4and;

import java.util.Random;

import android.os.Handler;
import edu.mit.csail.jasongao.roadrunner.RoadRunnerService.AdHocAnnouncer;
import edu.mit.csail.jasongao.roadrunner.RoadRunnerService.LocationSpoofer;
import edu.mit.csail.jasongao.roadrunner.RoadRunnerService.PathSetter;
import edu.mit.csail.jasongao.roadrunner.RoadRunnerService.RegionChecker;
import edu.mit.csail.sethhetu.roadrunner.LoggerI;
import edu.mit.smart.sm4and.android.AndroidSimMobilityBroker;
import edu.mit.smart.sm4and.connector.Connector;
import edu.mit.smart.sm4and.handler.AbstractMessageHandler;
import edu.mit.smart.sm4and.message.Message;
import edu.mit.smart.sm4and.message.MessageParser;
import edu.mit.smart.sm4and.message.RemoteLogMessage;

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
public abstract class SimMobilityBroker {
	//Singleton stuff
	private static SimMobilityBroker instance = new AndroidSimMobilityBroker();
	protected SimMobilityBroker() {}
	public static SimMobilityBroker getInstance() {
		return instance;
	}
	
	//Let's make this non-deterministic.
	protected Random RandGen = new Random();
	public Random getRand() { return RandGen; }
	
	//What's the time according to Sim Mobility?
	protected long currTimeMs;
	public long getCurrTimeMs() {
		return currTimeMs;
	}
	
	//Synced to whatever unique identifier the Android client provides. Usually based on IP address. 
	protected String uniqueId;
	public String getUniqueId() { 
		return uniqueId; 
	}
	
	
	/**
	 * Register a custom message type and corresponding callback action.  
	 * This callback will be triggered if a message of the given type is received.
	 * @param msgType The unique identifier of the Message to handle.
	 * @param msgClass The type of this message (e.g., MyCustomMessage.class). This will 
	 *        be parsed via Gson into an appropriate message.
	 * @param handler The handler to be called when this message arrives.
	 */
	public abstract void addCustomMessageType(String msgType, Class<? extends Message> msgClass, AbstractMessageHandler handler);
	
	
	/**
	 * Initialize the Broker. Can be called multiple times, until "Connect" is called.
	 */
	public abstract void initialize(String uniqueId, Handler myHandler, LoggerI logger, AdHocAnnouncer adhoc, LocationSpoofer locspoof, PathSetter pathSet, RegionChecker regcheck);
	
	
	public abstract MessageParser getParser();
	
	
	//Handle a Message as received from MINA.
	public abstract void handleMessage(AbstractMessageHandler handler, Message message);
	
	
	/**
	 * Start the SimMobility Broker (sets it to "active" and connects to the server).
	 * initialize() must be called first.
	 */
	public abstract void activate();
	
	public abstract boolean isActive();
		
	//Buffer this packet for broadcast at the end of the current time tick.
	public abstract void sendBroadcastPacket(String myId, byte[] packet);
	
	public abstract void postMessage(Message obj);
		
    //Helper: Log to server (remote)
    public abstract void ReflectToServer(String msg);
    
	//Helper: Log to server (remote)
    public static void ReflectToServer(Connector connector, String clientID, String msg) {
    	//Append a response.
        connector.addMessage(new RemoteLogMessage(clientID, msg));
    }

}
