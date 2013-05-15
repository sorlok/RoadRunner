//Copyright (c) 2013 Singapore-MIT Alliance for Research and Technology
//Licensed under the terms of the MIT License, as described in the file:
//   license.txt   (http://opensource.org/licenses/MIT)

package edu.mit.smart.sm4and.handler;

import edu.mit.smart.sm4and.Connector;
import edu.mit.smart.sm4and.AbstractMessageHandler;
import edu.mit.smart.sm4and.message.Message;
import edu.mit.smart.sm4and.message.Message.Type;

/**
 * Handle a "ready" message from the server.
 * @author Vahid
 */
public class ReadyHandler extends AbstractMessageHandler {
	/** A message from the server indicating that the client may proceed. */
	public static class ReadyMessage extends Message {
		public ReadyMessage() { this.MessageType = Type.Ready.toString(); }
	}
	
    public ReadyHandler(ReadyMessage message, Connector connector) {
    }
    
    @Override
    public void handle(Message message, Connector connector) {
        /*Seth, if you want your emulator set any flag to make sure 
         * it is successfully registered with the simMobility server(Broker)
         * here is your chance
         */
        System.out.println("Server Knows you. You can send and receive now.");
    }
    
}
