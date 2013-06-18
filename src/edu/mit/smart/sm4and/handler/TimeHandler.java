//Copyright (c) 2013 Singapore-MIT Alliance for Research and Technology
//Licensed under the terms of the MIT License, as described in the file:
//   license.txt   (http://opensource.org/licenses/MIT)

package edu.mit.smart.sm4and.handler;

import edu.mit.smart.sm4and.Connector;
import edu.mit.smart.sm4and.AbstractMessageHandler;
import edu.mit.smart.sm4and.MessageParser;
import edu.mit.smart.sm4and.message.Message;

/**
 * Handle a time-tick update from the server.
 * @author Pedro Gandola
 * @author Vahid
 */
public class TimeHandler extends AbstractMessageHandler {
	/** A message from the server indicating that the current time has advanced. */
	public static class TimeMessage extends Message {
		public TimeMessage() { this.MessageType = Type.TimeData.toString(); }
	    private int tick;    
	}
	
    public TimeHandler() {
    }

    @Override
    public void handle(Message message, Connector connector, MessageParser parser) {
        /*
         * if you need to use the simMobility current time, here is your chance.
         * you have a "TimeMessage message" filled with the data you need
         */  
        System.out.println("current tick is " + ((TimeMessage)message).tick);
    }
}
