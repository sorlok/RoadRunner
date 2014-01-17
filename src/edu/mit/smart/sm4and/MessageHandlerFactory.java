//Copyright (c) 2013 Singapore-MIT Alliance for Research and Technology
//Licensed under the terms of the MIT License, as described in the file:
//   license.txt   (http://opensource.org/licenses/MIT)

package edu.mit.smart.sm4and;


/**
 * Used for creating a variety of Handlers to respond to a variety of messages.
 * 
 * @author Pedro Gandola
 * @author Vahid
 */
public interface MessageHandlerFactory {
	/**
	 * Create an AbstractMessageHandler for dealing with a given message.
	 * @param connector The connector (used for responding to the message).
	 * @param message The message in question.
	 * @param ID The ID of the current agent.
	 */
    public abstract AbstractMessageHandler create(String msgType);
    
    
	/**
	 * Adds a custom handler for a given message type. If a "base" type (like WHOAREYOU) is given 
	 *   a custom handler, it will be combined with the default handler and executed after it.
	 * @param msgType The message type, as a unique string.
	 * @param handler The handler to call when this message type is encountered.
	 */
    public abstract void addCustomHandler(String msgType, AbstractMessageHandler handler);
}
