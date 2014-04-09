//Copyright (c) 2013 Singapore-MIT Alliance for Research and Technology
//Licensed under the terms of the MIT License, as described in the file:
//   license.txt   (http://opensource.org/licenses/MIT)

package edu.mit.smart.sm4and.handler;

import edu.mit.csail.jasongao.roadrunner.Globals;
import edu.mit.smart.sm4and.android.AndroidSimMobilityBroker.TimeAdvancer;
import edu.mit.smart.sm4and.connector.Connector;
import edu.mit.smart.sm4and.connector.MinaConnector;
import edu.mit.smart.sm4and.message.DefaultMessageTypes.TickedSimMobMessage;
import edu.mit.smart.sm4and.message.DefaultMessageTypes.TickedClientResponse;
import edu.mit.smart.sm4and.message.MessageParser.MessageBundle;
import edu.mit.smart.sm4and.message.Message;
import edu.mit.smart.sm4and.message.MessageParser;
import edu.mit.smart.sm4and.message.RemoteLogMessage;

/**
 * Handle a time-tick update from the server.
 * @author Pedro Gandola
 * @author Vahid
 */
public class TimeHandler extends AbstractMessageHandler {	
	private TimeAdvancer timeTicker;
	private String clientID;
	
    public TimeHandler(String clientID, TimeAdvancer timeTicker) {
    	this.timeTicker = timeTicker;
    	this.clientID = clientID;
    }

    @Override
    public void handle(Message message, Connector connector, MessageParser parser) {  
        //Ensure that some amount of time has elapsed.
    	TickedSimMobMessage timeMsg= (TickedSimMobMessage)message;
        timeTicker.advance(timeMsg.tick, timeMsg.elapsed);
        
    	//Send a response.
        connector.addMessage(new TickedClientResponse());
        
        //Need to append the log here.
        MessageBundle out = new MessageBundle();
        out.sendId = clientID;
        out.destId = "0";
        out.messages = connector.getAndClearMessages();
        if (Globals.SM_LOG_TRACE_ALL_MESSAGES) {
        	//TODO: It's not clear how this would work with v1 messages; maybe save the original string somewhere?
        	RemoteLogMessage log = new RemoteLogMessage();
        	log.log_msg = "SEND: " + MinaConnector.escape_invalid_json("(todo)");
        	out.messages.add(log);
        }
        
        //This means we're done; instruct the connector to send all remaining messages.
        connector.sendAll(out);
    }
}
