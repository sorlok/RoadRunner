//Copyright (c) 2013 Singapore-MIT Alliance for Research and Technology
//Licensed under the terms of the MIT License, as described in the file:
//   license.txt   (http://opensource.org/licenses/MIT)

package edu.mit.smart.sm4and.handler;

import edu.mit.csail.jasongao.roadrunner.RoadRunnerService.LocationSpoofer;
import edu.mit.smart.sm4and.Connector;
import edu.mit.smart.sm4and.AbstractMessageHandler;
import edu.mit.smart.sm4and.MessageParser;
import edu.mit.smart.sm4and.message.Message;

/**
 * Handle a unicast message.
 * @author Vahid
 */
public class UnicastHandler extends AbstractMessageHandler {
	/** A unicast message. Contains an opaque block of Base64-encoded data. */
	public static class UnicastMessage extends Message { 
		public UnicastMessage() { this.MESSAGE_TYPE = Type.UniCast; }
		private String UNICAST_DATA;
	}
	    
    public UnicastHandler() {
    }

    @Override
    public void handle(Message message, Connector connector, MessageParser parser) { 
    	UnicastMessage ucMsg = (UnicastMessage)message;
        System.out.println("Unicast message received of length: " + ucMsg.UNICAST_DATA.length());
    }
    
}
