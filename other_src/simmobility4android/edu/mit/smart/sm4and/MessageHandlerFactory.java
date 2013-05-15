//Copyright (c) 2013 Singapore-MIT Alliance for Research and Technology
//Licensed under the terms of the MIT License, as described in the file:
//   license.txt   (http://opensource.org/licenses/MIT)

package edu.mit.smart.sm4and;

import edu.mit.smart.sm4and.message.Message;


/**
 * Used for creating a variety of Handlers to respond to a variety of messages.
 * 
 * @author Pedro Gandola
 * @author Vahid
 */
public interface MessageHandlerFactory {
    AbstractMessageHandler create(Connector connector, Message message, int ID);
}