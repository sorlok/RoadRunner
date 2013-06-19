//Copyright (c) 2013 Singapore-MIT Alliance for Research and Technology
//Licensed under the terms of the MIT License, as described in the file:
//   license.txt   (http://opensource.org/licenses/MIT)

package edu.mit.smart.sm4and.message;

import edu.mit.smart.sm4and.handler.LocationHandler.LocationMessage;
import edu.mit.smart.sm4and.handler.MulticastHandler.MulticastMessage;
import edu.mit.smart.sm4and.handler.ReadyHandler.ReadyMessage;
import edu.mit.smart.sm4and.handler.ReadyToReceiveHandler.ReadyToReceiveMessage;
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
        WhoAreYou,
        TimeData,
        Ready,
        LocationData,
        
        //Not sure.
        MultiCast,
        UniCast,
        ReadyToReceive,
        
        //To server
        WhoAmI,  //TODO: Keep?
    }
    
    //Convert a "MultiCast" into "MultiCastMessage.class"
    public static Class<? extends Message> GetClassFromType(Type msgType) {
    	switch (msgType) {
    		case WhoAreYou:
    			return WhoAreYouMessage.class;
    		case TimeData:
    			return TimeMessage.class;
    		case Ready:
    			return ReadyMessage.class;
    		case LocationData:
    			return LocationMessage.class;
    		case MultiCast:
    			return MulticastMessage.class;
    		case UniCast:
    			return UnicastMessage.class;
    		case ReadyToReceive:
    			return ReadyToReceiveMessage.class;
    		default:
    			throw new RuntimeException("Unknown message type: " + msgType.toString());
    	}
    }

    public Type getMessageType() {
        return MESSAGE_TYPE;
    }

    /*public void setMessageType(MessageType msgType) {
        this.msgType = msgType;
    }*/
}
