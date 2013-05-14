//Copyright (c) 2013 Singapore-MIT Alliance for Research and Technology
//Licensed under the terms of the MIT License, as described in the file:
//   license.txt   (http://opensource.org/licenses/MIT)

package edu.mit.smart.sm4and.handler;

import edu.mit.smart.sm4and.Connector;
import edu.mit.smart.sm4and.AbstractMessageHandler;
import edu.mit.smart.sm4and.MessageHandlerFactory;
import edu.mit.smart.sm4and.handler.LocationHandler.LocationMessage;
import edu.mit.smart.sm4and.handler.ReadyHandler.ReadyMessage;
import edu.mit.smart.sm4and.handler.TimeHandler.TimeMessage;
import edu.mit.smart.sm4and.handler.WhoAreYouHandler.WhoAreYouMessage;
import edu.mit.smart.sm4and.message.Message;

import edu.mit.csail.jasongao.roadrunner.RoadRunnerService.LocationSpoofer;

/**
 * Returns to messages by simply returning their default Handlers.
 * 
 * @author Pedro Gandola
 * @author Vahid
 */
public class SimpleHandlerFactory implements MessageHandlerFactory {
	public LocationSpoofer locspoof;
	
	public SimpleHandlerFactory(LocationSpoofer locspoof) {
		this.locspoof = locspoof;
	}

    @Override
    public AbstractMessageHandler create(Connector connector, Message message, int clientID) {
    	//Respond to the given message.
    	switch (message.getMessageType()) {
            case WhoAreYou: {
                return new WhoAreYouHandler((WhoAreYouMessage)message, connector, clientID);
            }
            case TimeData: {
                return new TimeHandler((TimeMessage)message, connector);
            }
            case Ready: {
                return new ReadyHandler((ReadyMessage)message, connector);
            }
            case LocationData: {
            	return new LocationHandler(locspoof, (LocationMessage)message, connector);
            }
            default: {
                return null;
            }
        }
    }
}
