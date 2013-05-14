//Copyright (c) 2013 Singapore-MIT Alliance for Research and Technology
//Licensed under the terms of the MIT License, as described in the file:
//   license.txt   (http://opensource.org/licenses/MIT)

package sg.smart.mit.simmobility4android.handler;

import sg.smart.mit.simmobility4android.connector.Connector;
import sg.smart.mit.simmobility4android.message.WhoAreYouMessage;
import com.google.gson.JsonObject;


/**
 * @author Pedro Gandola
 * @author Vahid
 */
public class WhoAreYouHandler extends Handler {
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
