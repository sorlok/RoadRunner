//Copyright (c) 2013 Singapore-MIT Alliance for Research and Technology
//Licensed under the terms of the MIT License, as described in the file:
//   license.txt   (http://opensource.org/licenses/MIT)

package edu.mit.smart.sm4and.message;

/**
 * @author Pedro Gandola
 */
public class Message {

    public enum MessageType {
        WhoAreYou,
        TimeData,
        Ready,
        LocationData
    }

    public MessageType getMessageType() {
        return MessageType;
    }

    public void setMessageType(MessageType MessageType) {
        this.MessageType = MessageType;
    }
    
    private MessageType MessageType;
}
