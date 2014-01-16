//Copyright (c) 2013 Singapore-MIT Alliance for Research and Technology
//Licensed under the terms of the MIT License, as described in the file:
//   license.txt   (http://opensource.org/licenses/MIT)

package edu.mit.smart.sm4and.message;

import edu.mit.csail.sethhetu.roadrunner.LoggingRuntimeException;
import edu.mit.smart.sm4and.handler.LocationHandler.LocationMessage;
import edu.mit.smart.sm4and.handler.MulticastHandler.MulticastMessage;
import edu.mit.smart.sm4and.handler.ReadyHandler.ReadyMessage;
import edu.mit.smart.sm4and.handler.ReadyToReceiveHandler.ReadyToReceiveMessage;
import edu.mit.smart.sm4and.handler.SendRegionHandler;
import edu.mit.smart.sm4and.handler.TimeHandler.TimeMessage;
import edu.mit.smart.sm4and.handler.UnicastHandler.UnicastMessage;
import edu.mit.smart.sm4and.handler.WhoAreYouHandler.WhoAreYouMessage;

/**
 * Parent class for all Messages.
 * 
 * TODO: Enum-switching defeats the entire purpose of object-oriented inheritance.
 * 
 * @author Pedro Gandola
 */
public class Message {
	//The actual type of the message.
	//(Stored as a string because I'm not 100% sure how json handles Enums.).
	//protected String MessageType;
	
	//Set by Gson:
	protected String MESSAGE_TYPE;
    public String SENDER;       //Sender ID
    public String SENDER_TYPE;  //Almost always "SIMMOBILITY"
	
	//Hide from everything except Gson
	protected Message() {}
	
	//Types of messages.
	public static class Type {
    	//From server.
		public static final String WHOAREYOU = "WHOAREYOU";
		public static final String TIME_DATA = "TIME_DATA";
		public static final String READY = "READY";
		public static final String LOCATION_DATA = "LOCATION_DATA";
		public static final String READY_TO_RECEIVE = "READY_TO_RECEIVE";
		public static final String REGIONS_AND_PATH_DATA = "REGIONS_AND_PATH_DATA";

        //Not sure; might be both.
		public static final String MULTICAST = "MULTICAST";
		public static final String UNICAST = "UNICAST";
        
        //To server
		public static final String WHOAMI = "WHOAMI";
		public static final String CLIENT_MESSAGES_DONE = "CLIENT_MESSAGES_DONE";
		public static final String REMOTE_LOG = "REMOTE_LOG";
		public static final String REROUTE_REQUEST = "REROUTE_REQUEST";
	}
    
    //Convert a "MultiCast" into "MultiCastMessage.class"
    public static Class<? extends Message> GetClassFromType(String msgType) {
    	//Sanity check; the switch will explode otherwise.
    	if (msgType==null) {
    		throw new LoggingRuntimeException("Message.GetClassFromType() - Can't switch on a null Message type.");
    	}
    	    	
    	//Dispatch.
    	if (msgType.equals(Type.WHOAREYOU)) {
    		return WhoAreYouMessage.class;
    	} else if (msgType.equals(Type.TIME_DATA)) {
    		return TimeMessage.class;
    	} else if (msgType.equals(Type.READY)) {
    		return ReadyMessage.class;
    	} else if (msgType.equals(Type.LOCATION_DATA)) {
    		return LocationMessage.class;
    	} else if (msgType.equals(Type.MULTICAST)) {
    		return MulticastMessage.class;
    	} else if (msgType.equals(Type.UNICAST)) {
    		return UnicastMessage.class;
    	} else if (msgType.equals(Type.READY_TO_RECEIVE)) {
    		return ReadyToReceiveMessage.class;
    	} else if (msgType.equals(Type.REGIONS_AND_PATH_DATA)) {
    		return SendRegionHandler.SendRegionResponse.class;
    	} else {
   			throw new LoggingRuntimeException("Message.GetClassFromType() - Unknown message type: " + msgType.toString());
    	}
    }

    public String getMessageType() {
        return MESSAGE_TYPE;
    }
}
