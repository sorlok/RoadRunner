//Copyright (c) 2013 Singapore-MIT Alliance for Research and Technology
//Licensed under the terms of the MIT License, as described in the file:
//   license.txt   (http://opensource.org/licenses/MIT)

package edu.mit.smart.sm4and.handler;

import edu.mit.csail.jasongao.roadrunner.RoadRunnerService.LocationSpoofer;
import edu.mit.smart.sm4and.connector.Connector;
import edu.mit.smart.sm4and.message.DefaultMessageTypes.LocationMessage;
import edu.mit.smart.sm4and.message.Message;
import edu.mit.smart.sm4and.message.MessageParser;

/**
 * Handle a location-update message.
 * @author Vahid
 */
public class LocationHandler extends AbstractMessageHandler {	
    private LocationSpoofer locspoof;
    
    public LocationHandler(LocationSpoofer locspoof) {
        this.locspoof = locspoof;
    }

    @Override
    public void handle(Message message, Connector connector, MessageParser parser) {
        LocationMessage locMsg = (LocationMessage)message;
        locspoof.setLocation(locMsg.lat, locMsg.lng);
    }    
}
