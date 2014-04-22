//Copyright (c) 2013 Singapore-MIT Alliance for Research and Technology
//Licensed under the terms of the MIT License, as described in the file:
//   license.txt   (http://opensource.org/licenses/MIT)

package edu.mit.smart.sm4and.message;

/**
 * Parent class for all Messages.
 * 
 * @author Pedro Gandola
 */
public class Message {
	//Set by Gson:
	protected String msg_type; 
    
    //Sub-classes should call this.
	protected Message(String msgType) {
		this.msg_type = msgType;
	}
	
	//Types of messages.
	public static class Type {
    	//From server.
		public static final String id_request = "id_request";
		public static final String id_ack = "id_ack";
		public static final String ticked_simmob = "ticked_simmob";
		public static final String location = "location";
		public static final String opaque_receive = "opaque_receive";
        
        //To server
		public static final String id_response = "id_response";
		public static final String ticked_client = "ticked_client";
		public static final String opaque_send = "opaque_send";
		public static final String remote_log = "remote_log";
		public static final String tcp_connect = "tcp_connect";
		public static final String tcp_disconnect = "tcp_disconnect";
		
        //Deprecated
		//public static final String MULTICAST = "MULTICAST";
		//public static final String UNICAST = "UNICAST";
	}

    public String getMessageType() {
        return msg_type;
    }
    
	//Only GSON should call this.
    @SuppressWarnings("unused")
	private Message() { this("UNDEFINED"); }
}
