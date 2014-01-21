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
	//Set by Gson:
	protected String MESSAGE_TYPE;
    public String SENDER;       //Sender ID
    public String SENDER_TYPE;  //Almost always "SIMMOBILITY"
    
    //Sub-classes should call this.
	protected Message(String msgType, String sender) {
		this.MESSAGE_TYPE = msgType;
		this.SENDER = sender;
		this.SENDER_TYPE = "ANDROID_EMULATOR";
	}
	
	//Types of messages.
	public static class Type {
    	//From server.
		public static final String WHOAREYOU = "WHOAREYOU";
		public static final String TIME_DATA = "TIME_DATA";
		public static final String READY = "READY";
		public static final String LOCATION_DATA = "LOCATION_DATA";
		public static final String READY_TO_RECEIVE = "READY_TO_RECEIVE";

        //Not sure; might be both.
		public static final String MULTICAST = "MULTICAST";
		public static final String UNICAST = "UNICAST";
        
        //To server
		public static final String WHOAMI = "WHOAMI";
		public static final String CLIENT_MESSAGES_DONE = "CLIENT_MESSAGES_DONE";
		public static final String REMOTE_LOG = "REMOTE_LOG";
	}

    public String getMessageType() {
        return MESSAGE_TYPE;
    }
    
	//Only GSON should call this.
    @SuppressWarnings("unused")
	private Message() { this("UNDEFINED", "0"); }
}
