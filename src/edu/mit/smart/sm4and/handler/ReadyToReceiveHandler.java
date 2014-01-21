//Copyright (c) 2013 Singapore-MIT Alliance for Research and Technology
//Licensed under the terms of the MIT License, as described in the file:
//   license.txt   (http://opensource.org/licenses/MIT)

package edu.mit.smart.sm4and.handler;

import edu.mit.smart.sm4and.connector.Connector;
import edu.mit.smart.sm4and.message.Message;
import edu.mit.smart.sm4and.message.MessageParser;

/**
 * Handle a "ready to receive" message from the server.
 * @author Vahid
 */
public class ReadyToReceiveHandler extends AbstractMessageHandler {
	/** A message from the server indicating that the server is now ready for this time tick. */
	public static class ReadyToReceiveMessage extends Message {
		public ReadyToReceiveMessage(String uniqueId) {
			super(Type.READY_TO_RECEIVE, uniqueId); 
		}
		
		//This constructor is only used by GSON
		@SuppressWarnings("unused")
		private ReadyToReceiveMessage() { this("0"); }
	}
	
	/** Client tells the server to continue. */
	public static class ClientDoneResponse extends Message {
		public ClientDoneResponse(String uniqueId) {
			super(Type.CLIENT_MESSAGES_DONE, uniqueId); 
		}
		
		//This constructor is only used by GSON
		@SuppressWarnings("unused")
		private ClientDoneResponse() { this("0"); }
	}
	
	private String clientID;
	
    public ReadyToReceiveHandler(String clientID) {
    	this.clientID = clientID;
    }
    
    @Override
    public void handle(Message message, Connector connector, MessageParser parser) {        
        ClientDoneResponse obj = new ClientDoneResponse(clientID);
        
        //Done
        connector.addMessage(obj);
        connector.sendAll(parser.serialize(connector.getAndClearMessages()));
    }
    
}
