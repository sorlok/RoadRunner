//Copyright (c) 2013 Singapore-MIT Alliance for Research and Technology
//Licensed under the terms of the MIT License, as described in the file:
//   license.txt   (http://opensource.org/licenses/MIT)

package edu.mit.smart.sm4and.handler;

import edu.mit.smart.sm4and.Connector;
import edu.mit.smart.sm4and.Handler;
import edu.mit.smart.sm4and.message.Message;

/**
 * Handle a time-tick update from the server.
 * @author Pedro Gandola
 * @author Vahid
 */
public class TimeHandler extends Handler {
	/** A message from the server indicating that the current time has advanced. */
	public static class TimeMessage extends Message {
	    private int tick;    
	}
	
    public TimeHandler(TimeMessage message, Connector connector) {
        super (message, connector);
    }

    @Override
    public void handle() {
        /*
         * if you need to use the simMobility current time, here is your chance.
         * you have a "TimeMessage message" filled with the data you need
         */  
        System.out.println("current tick is " + ((TimeMessage)getMessage()).tick);
    }
}
