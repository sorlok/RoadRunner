//Copyright (c) 2013 Singapore-MIT Alliance for Research and Technology
//Licensed under the terms of the MIT License, as described in the file:
//   license.txt   (http://opensource.org/licenses/MIT)

package edu.mit.smart.sm4and;

import edu.mit.smart.sm4and.message.Message.Type;


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
    AbstractMessageHandler create(Type msgType);
}
