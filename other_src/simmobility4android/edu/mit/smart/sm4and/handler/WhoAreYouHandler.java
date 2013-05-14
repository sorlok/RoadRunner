//Copyright (c) 2013 Singapore-MIT Alliance for Research and Technology
//Licensed under the terms of the MIT License, as described in the file:
//   license.txt   (http://opensource.org/licenses/MIT)

package edu.mit.smart.sm4and.handler;

import com.google.gson.JsonObject;

import edu.mit.smart.sm4and.Connector;
import edu.mit.smart.sm4and.Handler;
import edu.mit.smart.sm4and.message.Message;


/**
 * Handle a "who are you?" query from the server.
 * @author Pedro Gandola
 * @author Vahid
 */
public class WhoAreYouHandler extends Handler {
	/** A message from the server requesting that the client identify itself. */
	public static class WhoAreYouMessage extends Message { }
	
    private int clientID;
    
    public WhoAreYouHandler(WhoAreYouMessage message, Connector connector, int clientID) {
        super(message, connector);
        this.clientID = clientID;
        System.out.println("creating WhoAreYouHandler");
    }

    @Override
    public void handle() {
        System.out.println("WhoAreYouHandler is handling");
        
        JsonObject obj = new JsonObject();
        
        /*
         * seth please add your emulator's ID here. 
         * Note that it will be received as unsidned int 
         * on the other side
         */
        obj.addProperty("ID", clientID);
        getConnector().send(obj.toString());
    }
}
