//Copyright (c) 2013 Singapore-MIT Alliance for Research and Technology
//Licensed under the terms of the MIT License, as described in the file:
//   license.txt   (http://opensource.org/licenses/MIT)

package edu.mit.smart.sm4and.handler;

import edu.mit.csail.jasongao.roadrunner.Globals;
import edu.mit.csail.sethhetu.roadrunner.SimMobilityBroker;
import edu.mit.csail.sethhetu.roadrunner.SimMobilityBroker.TimeAdvancer;
import edu.mit.smart.sm4and.Connector;
import edu.mit.smart.sm4and.AbstractMessageHandler;
import edu.mit.smart.sm4and.MessageParser;
import edu.mit.smart.sm4and.message.Message;

/**
 * Handle a time-tick update from the server.
 * @author Pedro Gandola
 * @author Vahid
 */
public class TimeHandler extends AbstractMessageHandler {
	/** A message from the server indicating that the current time has advanced. */
	public static class TimeMessage extends Message {
		public TimeMessage() { this.MESSAGE_TYPE = Type.TIME_DATA; }
		private int tick;    
		private int elapsed_ms;
	}
	
	private TimeAdvancer timeTicker;
	
    public TimeHandler(TimeAdvancer timeTicker) {
    	this.timeTicker = timeTicker;
    }

    @Override
    public void handle(Message message, Connector connector, MessageParser parser) {  
    	TimeMessage timeMsg= (TimeMessage)message;
        System.out.println("current tick is " + timeMsg.tick + " +" + timeMsg.elapsed_ms + "ms");
        
        //Ensure that some amount of time has elapsed.
        timeTicker.advance(timeMsg.elapsed_ms);
    }
}
