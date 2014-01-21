//Copyright (c) 2013 Singapore-MIT Alliance for Research and Technology
//Licensed under the terms of the MIT License, as described in the file:
//   license.txt   (http://opensource.org/licenses/MIT)

package edu.mit.smart.sm4and.handler;

import edu.mit.smart.sm4and.connector.Connector;
import edu.mit.smart.sm4and.message.Message;
import edu.mit.smart.sm4and.message.MessageParser;

/**
 * Handle a unicast message.
 * @author Vahid
 */
public class UnicastHandler extends AbstractMessageHandler {
	/** A unicast message. Contains an opaque block of Base64-encoded data. */
	public static class UnicastMessage extends Message { 
		public UnicastMessage(String uniqueId, String ucData) {
			super(Type.UNICAST, uniqueId);
			this.UNICAST_DATA = ucData;
		}
		private String UNICAST_DATA;
		
		//This constructor is only used by GSON
		@SuppressWarnings("unused")
		private UnicastMessage() { this("0", ""); }
	}
	    
    public UnicastHandler() {
    }

    @Override
    public void handle(Message message, Connector connector, MessageParser parser) { 
    	UnicastMessage ucMsg = (UnicastMessage)message;
        System.out.println("Unicast message received of length: " + ucMsg.UNICAST_DATA.length());
        
        //TODO: Unicast messages are specific to-agent messages. Currently, they are not implemented,
        //      but will be needed for key exchanges.
    }
    
}
