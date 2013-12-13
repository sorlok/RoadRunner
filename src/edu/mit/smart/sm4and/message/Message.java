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
	protected Type MESSAGE_TYPE;
    public String SENDER;       //Sender ID
    public String SENDER_TYPE;  //Almost always "SIMMOBILITY"
	
	//Hide from everything except Gson
	protected Message() {}
	
    public enum Type {
    	//From server.
        WHOAREYOU,
        TIME_DATA,
        READY,
        LOCATION_DATA,
        READY_TO_RECEIVE,
        REGIONS_AND_PATH_DATA,
        
        //Not sure; might be both.
        MULTICAST,
        UNICAST,
        
        //To server
        WHOAMI,
        CLIENT_MESSAGES_DONE,
        //SEND_REGIONS,
        REMOTE_LOG,
    }
    
    //Convert a "MultiCast" into "MultiCastMessage.class"
    public static Class<? extends Message> GetClassFromType(Type msgType) {
    	//Sanity check; the switch will explode otherwise.
    	if (msgType==null) {
    		throw new LoggingRuntimeException("Message.GetClassFromType() - Can't switch on a null Message type.");
    	}
    	    	
    	//Dispatch.
    	switch (msgType) {
    		case WHOAREYOU:
    			return WhoAreYouMessage.class;
    		case TIME_DATA:
    			return TimeMessage.class;
    		case READY:
    			return ReadyMessage.class;
    		case LOCATION_DATA:
    			return LocationMessage.class;
    		case MULTICAST:
    			return MulticastMessage.class;
    		case UNICAST:
    			return UnicastMessage.class;
    		case READY_TO_RECEIVE:
    			return ReadyToReceiveMessage.class;
    		case REGIONS_AND_PATH_DATA:
    			return SendRegionHandler.SendRegionResponse.class;
    		default:
    			throw new LoggingRuntimeException("Message.GetClassFromType() - Unknown message type: " + msgType.toString());
    	}
    }

    public Type getMessageType() {
        return MESSAGE_TYPE;
    }
}
