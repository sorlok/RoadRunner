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
public abstract class Message {
	//The actual type of the message.
	private MessageType msgType;
	
    public enum MessageType {
        WhoAreYou,
        TimeData,
        Ready,
        LocationData
    }

    public MessageType getMessageType() {
        return msgType;
    }

    public void setMessageType(MessageType MessageType) {
        this.msgType = MessageType;
    }
}
