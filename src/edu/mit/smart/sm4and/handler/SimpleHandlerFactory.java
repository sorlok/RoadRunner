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
            case WhoAreYou: {
                return new WhoAreYouHandler(clientID);
            }
            case TimeData: {
                return new TimeHandler();
            }
            case Ready: {
                return new ReadyHandler();
            }
            case LocationData: {
            	return new LocationHandler(locspoof);
            }
            default: {
                return null;
            }
        }
    }
}
