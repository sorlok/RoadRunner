//Copyright (c) 2013 Singapore-MIT Alliance for Research and Technology
//Licensed under the terms of the MIT License, as described in the file:
//   license.txt   (http://opensource.org/licenses/MIT)

package edu.mit.smart.sm4and.handler;

import edu.mit.smart.sm4and.connector.Connector;
import edu.mit.smart.sm4and.message.Message;
import edu.mit.smart.sm4and.message.MessageParser;


/**
 * Handle a "who are you?" query from the server.
 * @author Pedro Gandola
 * @author Vahid
 */
public class WhoAreYouHandler extends AbstractMessageHandler {
	/** A message from the server requesting that the client identify itself. */
	public static class WhoAreYouMessage extends Message {
		public WhoAreYouMessage() { this.MESSAGE_TYPE = Type.WHOAREYOU; }
	}
	
	/** A response to the server identifying oneself. */
	public static class WhoAmIResponse extends Message {
		public WhoAmIResponse() { this.MESSAGE_TYPE = Type.WHOAMI; }
	    public String ID;
	    public String TYPE;
	    public String[] REQUIRED_SERVICES;
	}
	
    private String clientID;
    
    public WhoAreYouHandler(String clientID) {
        this.clientID = clientID;
        System.out.println("creating WhoAreYouHandler");
    }

    @Override
    public void handle(Message message, Connector connector, MessageParser parser) {
        System.out.println("WhoAreYouHandler is handling");
        
        //Prepare a response.
        WhoAmIResponse obj = new WhoAmIResponse();
        obj.SENDER = clientID;
        obj.ID = String.valueOf(clientID);
        obj.TYPE = "ANDROID_EMULATOR";
        obj.SENDER_TYPE = "ANDROID_EMULATOR";
        obj.REQUIRED_SERVICES = new String[]{"SIMMOB_SRV_TIME","SIMMOB_SRV_LOCATION","SIMMOB_SRV_REGIONS_AND_PATH"};
        
        //The "WhoAmIResponse" is unique in that it *always* triggers a send.
        connector.addMessage(obj);
        connector.sendAll(parser.serialize(connector.getAndClearMessages()));
    }
}
