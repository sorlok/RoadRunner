//Copyright (c) 2013 Singapore-MIT Alliance for Research and Technology
//Licensed under the terms of the MIT License, as described in the file:
//   license.txt   (http://opensource.org/licenses/MIT)

package edu.mit.smart.sm4and.handler;

import java.util.ArrayList;

import edu.mit.csail.jasongao.roadrunner.Globals;
import edu.mit.smart.sm4and.connector.Connector;
import edu.mit.smart.sm4and.connector.MinaConnector;
import edu.mit.smart.sm4and.json.JsonMessageParser;
import edu.mit.smart.sm4and.message.DefaultMessageTypes.IdRequestMessage;
import edu.mit.smart.sm4and.message.DefaultMessageTypes.IdResponse;
import edu.mit.smart.sm4and.message.MessageParser.MessageBundle;
import edu.mit.smart.sm4and.message.Message;
import edu.mit.smart.sm4and.message.MessageParser;
import edu.mit.smart.sm4and.message.RemoteLogMessage;


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
    	//Pass back the token as-is.
    	IdRequestMessage whoMsg = (IdRequestMessage)message;
    	
        //Prepare a response.
    	IdResponse res = new IdResponse();
    	res.id = clientID;
    	res.token = whoMsg.token;
    	res.type = "android";
    	res.services = new String[]{"srv_location","srv_regions_and_path"};
        connector.addMessage(res);
        
        //This has to be done here.
        MessageBundle out = new MessageBundle();
        out.sendId = clientID;
        out.destId = "0";
        out.messages = connector.getAndClearMessages();
        if (Globals.SM_LOG_TRACE_ALL_MESSAGES) {
        	RemoteLogMessage log = new RemoteLogMessage();
        	log.log_msg = "SEND: " + MinaConnector.escape_invalid_json(JsonMessageParser.FilterJson(parser.serialize(out)[1]));
        	out.messages.add(log);
        }
        
        //The "WhoAmIResponse" is unique in that it *always* triggers a send.
        String[] ser = parser.serialize(out);
        connector.sendAll(ser[0], ser[1]);
    }
}
