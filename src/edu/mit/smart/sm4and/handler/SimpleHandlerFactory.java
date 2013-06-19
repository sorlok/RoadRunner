//Copyright (c) 2013 Singapore-MIT Alliance for Research and Technology
//Licensed under the terms of the MIT License, as described in the file:
//   license.txt   (http://opensource.org/licenses/MIT)

package edu.mit.smart.sm4and.handler;

import edu.mit.smart.sm4and.AbstractMessageHandler;
import edu.mit.smart.sm4and.MessageHandlerFactory;
import edu.mit.smart.sm4and.message.Message.Type;

import edu.mit.csail.jasongao.roadrunner.RoadRunnerService.LocationSpoofer;

/**
 * Returns to messages by simply returning their default Handlers.
 * 
 * @author Pedro Gandola
 * @author Vahid
 */
public class SimpleHandlerFactory implements MessageHandlerFactory {
	private LocationSpoofer locspoof;
	private int clientID;
	
	public SimpleHandlerFactory(LocationSpoofer locspoof, int clientID) {
		this.locspoof = locspoof;
		this.clientID = clientID;
	}

    @Override
    public AbstractMessageHandler create(Type msgType) {
    	//Respond to the given message.
    	switch (msgType) {
            case WHOAREYOU:
                return new WhoAreYouHandler(clientID);
            case TIME_DATA: 
                return new TimeHandler();
            case READY: 
                return new ReadyHandler();
            case LOCATION_DATA: 
            	return new LocationHandler(locspoof);
            case MULTICAST: 
            	return new MulticastHandler();
            case UNICAST:
            	return new UnicastHandler();
            case READY_TO_RECEIVE:
            	return new ReadyToReceiveHandler();
            default:
                throw new RuntimeException("Unknown message type: " + msgType.toString());
        }
    }
}
