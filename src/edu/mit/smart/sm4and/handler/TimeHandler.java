//Copyright (c) 2013 Singapore-MIT Alliance for Research and Technology
//Licensed under the terms of the MIT License, as described in the file:
//   license.txt   (http://opensource.org/licenses/MIT)

package edu.mit.smart.sm4and.handler;

import edu.mit.smart.sm4and.android.AndroidSimMobilityBroker.TimeAdvancer;
import edu.mit.smart.sm4and.connector.Connector;
import edu.mit.smart.sm4and.message.DefaultMessageTypes.TimeMessage;
import edu.mit.smart.sm4and.message.Message;
import edu.mit.smart.sm4and.message.MessageParser;

/**
 * Handle a time-tick update from the server.
 * @author Pedro Gandola
 * @author Vahid
 */
public class TimeHandler extends AbstractMessageHandler {	
	private TimeAdvancer timeTicker;
	
    public TimeHandler(TimeAdvancer timeTicker) {
    	this.timeTicker = timeTicker;
    }

    @Override
    public void handle(Message message, Connector connector, MessageParser parser) {  
        //Ensure that some amount of time has elapsed.
    	TimeMessage timeMsg= (TimeMessage)message;
        timeTicker.advance(timeMsg.tick, timeMsg.elapsed_ms);
    }
}
