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
 * Handle a multicast message.
 * @author Vahid
 */
public class MulticastHandler extends AbstractMessageHandler {
	/** A multicast message. Contains an opaque block of Base64-encoded data. */
	public static class MulticastMessage extends Message { 
		public MulticastMessage() { this.MESSAGE_TYPE = Type.MultiCast; }
		private String MULTICAST_DATA;
	}
	    
    public MulticastHandler() {
    }

    @Override
    public void handle(Message message, Connector connector, MessageParser parser) { 
    	MulticastMessage mcMsg = (MulticastMessage)message;
        System.out.println("Multicast message received of length: " + mcMsg.MULTICAST_DATA.length());
    }
    
}
