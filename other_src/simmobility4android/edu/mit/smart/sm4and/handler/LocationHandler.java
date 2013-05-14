//Copyright (c) 2013 Singapore-MIT Alliance for Research and Technology
//Licensed under the terms of the MIT License, as described in the file:
//   license.txt   (http://opensource.org/licenses/MIT)

package edu.mit.smart.sm4and.handler;

import edu.mit.csail.jasongao.roadrunner.RoadRunnerService.LocationSpoofer;
import edu.mit.smart.sm4and.Connector;
import edu.mit.smart.sm4and.Handler;
import edu.mit.smart.sm4and.message.Message;

/**
 * Handle a location-update message.
 * @author Vahid
 */
public class LocationHandler extends Handler {
	/** A location update message. Uses projected (x,y) coordinates, not Latitude/Longitude. */
	public static class LocationMessage extends Message { 
	    private int x;
	    private int y;
	}
	
    private LocationSpoofer locspoof;
    
    public LocationHandler(LocationSpoofer locspoof, LocationMessage message, Connector connector) {
        super(message, connector);
        this.locspoof = locspoof;
    }

    @Override
    public void handle() {
        /*
         * TODO: This currently uses X/Y coordinates. We need to reverse project back to lat/lng
         *       (but randomized networks need a fallback, as there is no projection matrix). 
         */  
        LocationMessage message = (LocationMessage)getMessage();
        System.out.println("Current location is "+ message.x + ":" + message.y);
        locspoof.setLocation(message.x, message.y);
    }
    
}
