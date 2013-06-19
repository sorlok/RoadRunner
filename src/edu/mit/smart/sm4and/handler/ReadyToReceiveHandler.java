//Copyright (c) 2013 Singapore-MIT Alliance for Research and Technology
//Licensed under the terms of the MIT License, as described in the file:
//   license.txt   (http://opensource.org/licenses/MIT)

package edu.mit.smart.sm4and.handler;

import edu.mit.smart.sm4and.Connector;
import edu.mit.smart.sm4and.AbstractMessageHandler;
import edu.mit.smart.sm4and.MessageParser;
import edu.mit.smart.sm4and.message.Message;

/**
 * Handle a "ready to receive" message from the server.
 * @author Vahid
 */
public class ReadyToReceiveHandler extends AbstractMessageHandler {
	/** A message from the server indicating that the server is now ready for this time tick. */
	public static class ReadyToReceiveMessage extends Message {
		public ReadyToReceiveMessage() { this.MESSAGE_TYPE = Type.READY_TO_RECEIVE; }
	}
	
    public ReadyToReceiveHandler() {
    }
    
    @Override
    public void handle(Message message, Connector connector, MessageParser parser) {
        System.out.println("Server is now ready to receive.");
    }
    
}
