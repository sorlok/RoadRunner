//Copyright (c) 2013 Singapore-MIT Alliance for Research and Technology
//Licensed under the terms of the MIT License, as described in the file:
//   license.txt   (http://opensource.org/licenses/MIT)

package edu.mit.smart.sm4and.handler;

import edu.mit.smart.sm4and.connector.Connector;
import edu.mit.smart.sm4and.message.DefaultMessageTypes.WhoAmIResponse;
import edu.mit.smart.sm4and.message.Message;
import edu.mit.smart.sm4and.message.MessageParser;


/**
 * Handle a "who are you?" query from the server.
 * @author Pedro Gandola
 * @author Vahid
 */
public class WhoAreYouHandler extends AbstractMessageHandler {	
    private String clientID;
    
    public WhoAreYouHandler(String clientID) {
        this.clientID = clientID;
    }

    @Override
    public void handle(Message message, Connector connector, MessageParser parser) {
        //Prepare a response.
        connector.addMessage(new WhoAmIResponse(clientID, new String[]{"SIMMOB_SRV_TIME","SIMMOB_SRV_LOCATION","SIMMOB_SRV_REGIONS_AND_PATH"}));
        
        //The "WhoAmIResponse" is unique in that it *always* triggers a send.
        connector.sendAll(parser.serialize(connector.getAndClearMessages()));
    }
}
