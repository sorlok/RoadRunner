//Copyright (c) 2013 Singapore-MIT Alliance for Research and Technology
//Licensed under the terms of the MIT License, as described in the file:
//   license.txt   (http://opensource.org/licenses/MIT)

package edu.mit.smart.sm4and;


import java.util.Hashtable;

import edu.mit.smart.sm4and.AbstractMessageHandler;
import edu.mit.smart.sm4and.MessageHandlerFactory;
import edu.mit.smart.sm4and.handler.BifurcatedHandler;
import edu.mit.csail.sethhetu.roadrunner.LoggingRuntimeException;
import edu.mit.csail.sethhetu.roadrunner.SimMobilityBroker;


/**
 * Used for creating a variety of Handlers to respond to a variety of messages.
 * 
 * @author Pedro Gandola
 * @author Vahid
 * @author Seth N. Hetu
 */
public class MessageHandlerFactory  {
	//The default handlers are defined by Sim Mobility.
	private Hashtable<String, AbstractMessageHandler> defaultHandlers = new Hashtable<String, AbstractMessageHandler>();
	
	//Any custom handlers have to be supported in Sim Mobility, but are defined by the user in the Android app.
	private Hashtable<String, AbstractMessageHandler> customHandlers = new Hashtable<String, AbstractMessageHandler>();
	

	/**
	 * Adds a default message handler. This method should only be called internally; to set up your own
	 *   handlers you should call addCustomHandler().
	 * @param msgType The message type, as a unique string.
	 * @param handler The handler to call when this message type is encountered.
	 * @param broker The Broker requesting this handler to be added.
	 */
	public void addDefaultHandler(String msgType, AbstractMessageHandler handler, SimMobilityBroker broker) {
		handler.setBroker(broker);
		defaultHandlers.put(msgType, handler);
	}
	
	
	/**
	 * Adds a custom handler for a given message type. If a "base" type (like WHOAREYOU) is given 
	 *   a custom handler, it will be combined with the default handler and executed after it.
	 * @param msgType The message type, as a unique string.
	 * @param handler The handler to call when this message type is encountered.
	 */
	public void addCustomHandler(String msgType, AbstractMessageHandler handler) {
		if (!handler.hasBroker()) {
			throw new LoggingRuntimeException("Can't set a custom handler with a null Broker.");
		}
		
		customHandlers.put(msgType, handler);
	}
	
	/**
	 * Create an AbstractMessageHandler for dealing with a given message.
	 * @param connector The connector (used for responding to the message).
	 * @param message The message in question.
	 * @param ID The ID of the current agent.
	 */
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
