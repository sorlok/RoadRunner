//Copyright (c) 2013 Singapore-MIT Alliance for Research and Technology
//Licensed under the terms of the MIT License, as described in the file:
//   license.txt   (http://opensource.org/licenses/MIT)

package edu.mit.smart.sm4and.handler;

import edu.mit.csail.jasongao.roadrunner.RoadRunnerService.LocationSpoofer;
import edu.mit.smart.sm4and.Connector;
import edu.mit.smart.sm4and.AbstractMessageHandler;
import edu.mit.smart.sm4and.MessageParser;
import edu.mit.smart.sm4and.message.Message;

/**
 * Handle a location-update message.
 * @author Vahid
 */
public class LocationHandler extends AbstractMessageHandler {
	/** A location update message. Uses projected (x,y) coordinates, not Latitude/Longitude. */
	public static class LocationMessage extends Message { 
		public LocationMessage() { this.MESSAGE_TYPE = Type.LOCATION_DATA; }
	    private int x;
	    private int y;
	}
	
    private LocationSpoofer locspoof;
    
    public LocationHandler(LocationSpoofer locspoof) {
        this.locspoof = locspoof;
    }

    @Override
    public void handle(Message message, Connector connector, MessageParser parser) {
        /*
         * TODO: This currently uses X/Y coordinates. We need to reverse project back to lat/lng
         *       (but randomized networks need a fallback, as there is no projection matrix). 
         */  
        LocationMessage locMsg = (LocationMessage)message;
        System.out.println("Current location is "+ locMsg.x + ":" + locMsg.y);
        locspoof.setLocation(locMsg.x, locMsg.y);
    }
    
}
