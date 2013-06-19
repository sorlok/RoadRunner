//Copyright (c) 2013 Singapore-MIT Alliance for Research and Technology
//Licensed under the terms of the MIT License, as described in the file:
//   license.txt   (http://opensource.org/licenses/MIT)

package edu.mit.smart.sm4and.handler;

import edu.mit.smart.sm4and.Connector;
import edu.mit.smart.sm4and.AbstractMessageHandler;
import edu.mit.smart.sm4and.MessageParser;
import edu.mit.smart.sm4and.message.Message;


/**
 * Handle a "who are you?" query from the server.
 * @author Pedro Gandola
 * @author Vahid
 */
public class WhoAreYouHandler extends AbstractMessageHandler {
	/** A message from the server requesting that the client identify itself. */
	public static class WhoAreYouMessage extends Message {
		public WhoAreYouMessage() { this.MESSAGE_TYPE = Type.WhoAreYou; }
	}
	
	/** A response to the server identifying oneself. */
	public static class WhoAmIResponse extends Message {
		public WhoAmIResponse() { this.MESSAGE_TYPE = Type.WhoAmI; }
	    public int id;
	}
	
    private int clientID;
    
    public WhoAreYouHandler( int clientID) {
        this.clientID = clientID;
        System.out.println("creating WhoAreYouHandler");
    }

    @Override
    public void handle(Message message, Connector connector, MessageParser parser) {
        System.out.println("WhoAreYouHandler is handling");
        
        WhoAmIResponse obj = new WhoAmIResponse();
        obj.id = clientID;
        
        connector.send(parser.serialize(obj));
    }
}
