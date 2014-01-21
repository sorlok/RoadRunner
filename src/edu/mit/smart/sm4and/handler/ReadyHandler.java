//Copyright (c) 2013 Singapore-MIT Alliance for Research and Technology
//Licensed under the terms of the MIT License, as described in the file:
//   license.txt   (http://opensource.org/licenses/MIT)

package edu.mit.smart.sm4and.handler;

import edu.mit.smart.sm4and.connector.Connector;
import edu.mit.smart.sm4and.message.Message;
import edu.mit.smart.sm4and.message.MessageParser;

/**
 * Handle a "ready" message from the server.
 * @author Vahid
 */
public class ReadyHandler extends AbstractMessageHandler {	
    @Override
    public void handle(Message message, Connector connector, MessageParser parser) {
        System.out.println("Server Knows you. You can send and receive now.");
    }
    
}
