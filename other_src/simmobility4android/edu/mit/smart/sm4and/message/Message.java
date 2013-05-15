//Copyright (c) 2013 Singapore-MIT Alliance for Research and Technology
//Licensed under the terms of the MIT License, as described in the file:
//   license.txt   (http://opensource.org/licenses/MIT)

package edu.mit.smart.sm4and.message;

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
	protected String MessageType;
	
	//Hide from everything except Gson
	protected Message() {}
	
    public enum Type {
    	//From server.
        WhoAreYou,
        TimeData,
        Ready,
        LocationData,
        
        //To server
        WhoAmI,
    }

    public Type getMessageType() {
        return Type.valueOf(MessageType);
    }

    /*public void setMessageType(MessageType msgType) {
        this.msgType = msgType;
    }*/
}
