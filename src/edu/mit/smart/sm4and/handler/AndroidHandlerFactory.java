//Copyright (c) 2013 Singapore-MIT Alliance for Research and Technology
//Licensed under the terms of the MIT License, as described in the file:
//   license.txt   (http://opensource.org/licenses/MIT)

package edu.mit.smart.sm4and.handler;

import java.util.Hashtable;

import edu.mit.smart.sm4and.AbstractMessageHandler;
import edu.mit.smart.sm4and.MessageHandlerFactory;
import edu.mit.smart.sm4and.message.Message;

import edu.mit.csail.jasongao.roadrunner.RoadRunnerService.LocationSpoofer;
import edu.mit.csail.sethhetu.roadrunner.LoggingRuntimeException;
import edu.mit.csail.sethhetu.roadrunner.SimMobilityBroker.MultiCastReceiver;
import edu.mit.csail.sethhetu.roadrunner.SimMobilityBroker.TimeAdvancer;

/**
 * Provides Android-specific handlers for our various messages.
 */
public class AndroidHandlerFactory implements MessageHandlerFactory {
	//Additional handlers.
	private Hashtable<String, AbstractMessageHandler> defaultHandlers;
	private Hashtable<String, AbstractMessageHandler> customHandlers;
	
	public AndroidHandlerFactory(String clientId, LocationSpoofer locSpoof, TimeAdvancer timeTicker, MultiCastReceiver mcProcess) {
		//Set up our list of default handlers.
		defaultHandlers.put(Message.Type.WHOAREYOU, new WhoAreYouHandler(clientId));
		defaultHandlers.put(Message.Type.TIME_DATA, new TimeHandler(timeTicker));
		defaultHandlers.put(Message.Type.READY, new ReadyHandler());
		defaultHandlers.put(Message.Type.LOCATION_DATA, new LocationHandler(locSpoof));
		defaultHandlers.put(Message.Type.MULTICAST, new MulticastHandler(mcProcess));
		defaultHandlers.put(Message.Type.UNICAST, new UnicastHandler());
		defaultHandlers.put(Message.Type.READY_TO_RECEIVE, new ReadyToReceiveHandler(clientId));
	}
	
	/**
	 * See: MessageHandlerFactory::addCustomHandler().
	 */
	public void addCustomHandler(String msgType, AbstractMessageHandler handler) {
		if (!handler.hasBroker()) {
			throw new LoggingRuntimeException("Can't set a custom handler with a null Broker.");
		}
		
		customHandlers.put(msgType, handler);
	}
	
	@Override
	public AbstractMessageHandler create(String msgType) {
		//Retrieve the default and custom handlers for this type.
		AbstractMessageHandler defHand = defaultHandlers.get(msgType);
		AbstractMessageHandler custHand = customHandlers.get(msgType);
		
		//At least one must be non-null. 
		if (defHand==null && custHand==null) {
			throw new LoggingRuntimeException("Unknown message type: " + msgType.toString() + "  >>NOT HANDLED.");
		}
		
		//Else, one or both can be null. Let the BifurcatedMessageHandler decide.
		return BifurcatedHandler.CreateHandler(defHand, custHand);
	}
}
