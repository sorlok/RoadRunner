//Copyright (c) 2013 Singapore-MIT Alliance for Research and Technology
//Licensed under the terms of the MIT License, as described in the file:
//   license.txt   (http://opensource.org/licenses/MIT)

package edu.mit.smart.sm4and.handler;

import edu.mit.smart.sm4and.connector.Connector;
import edu.mit.smart.sm4and.message.DefaultMessageTypes.ClientDoneResponse;
import edu.mit.smart.sm4and.message.Message;
import edu.mit.smart.sm4and.message.MessageParser;

/**
 * Handle a "ready to receive" message from the server.
 * @author Vahid
 */
public class ReadyToReceiveHandler extends AbstractMessageHandler {	
	private String clientID;
	
    public ReadyToReceiveHandler(String clientID) {
    	this.clientID = clientID;
    }
    
    @Override
    public void handle(Message message, Connector connector, MessageParser parser) {
    	//Send a response.
        connector.addMessage(new ClientDoneResponse(clientID));
        
        //This means we're done; instruct the connector to send all remaining messages.
        connector.sendAll(parser.serialize(connector.getAndClearMessages()));
    }
    
}
