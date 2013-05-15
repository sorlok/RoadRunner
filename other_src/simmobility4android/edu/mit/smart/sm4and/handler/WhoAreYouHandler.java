//Copyright (c) 2013 Singapore-MIT Alliance for Research and Technology
//Licensed under the terms of the MIT License, as described in the file:
//   license.txt   (http://opensource.org/licenses/MIT)

package edu.mit.smart.sm4and.handler;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import edu.mit.smart.sm4and.Connector;
import edu.mit.smart.sm4and.AbstractMessageHandler;
import edu.mit.smart.sm4and.message.Message;


/**
 * Handle a "who are you?" query from the server.
 * @author Pedro Gandola
 * @author Vahid
 */
public class WhoAreYouHandler extends AbstractMessageHandler {
	/** A message from the server requesting that the client identify itself. */
	public static class WhoAreYouMessage extends Message {
		public WhoAreYouMessage() { this.MessageType = Type.WhoAreYou.toString(); }
	}
	
	/** A response to the server identifying oneself. */
	public static class WhoAmIResponse extends Message {
		public WhoAmIResponse() { this.MessageType = Type.WhoAmI.toString(); }
	    private int ID;    
	}
	
    private int clientID;
    
    public WhoAreYouHandler(WhoAreYouMessage message, Connector connector, int clientID) {
        this.clientID = clientID;
        System.out.println("creating WhoAreYouHandler");
    }

    @Override
    public void handle(Message message, Connector connector) {
        System.out.println("WhoAreYouHandler is handling");
        
        WhoAmIResponse obj = new WhoAmIResponse();
        obj.ID = clientID;
        String msg = new Gson().toJson(obj);
        
      // obj2 = new JsonObject();
        
        /*
         * seth please add your emulator's ID here. 
         * Note that it will be received as unsidned int 
         * on the other side
         */
      //  obj2.addProperty("ID", clientID);
        
     //   if (true) throw new RuntimeException("TESTING: **" + msg + "**  **" + obj2.toString() + "**");
        
        connector.send(msg);
    }
}
