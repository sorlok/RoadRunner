//Copyright (c) 2013 Singapore-MIT Alliance for Research and Technology
//Licensed under the terms of the MIT License, as described in the file:
//   license.txt   (http://opensource.org/licenses/MIT)

package edu.mit.smart.sm4and.mina;

import edu.mit.smart.sm4and.Connector;
import edu.mit.smart.sm4and.AbstractMessageHandler;
import edu.mit.smart.sm4and.MessageHandlerFactory;
import edu.mit.smart.sm4and.MessageParser;
import edu.mit.smart.sm4and.message.Message;

/**
 * Class that listens for messages and Handles them.
 * 
 * NOTE: I will probably remove this; it's a little bit too much indirection for me. ~Seth
 * 
 * @author Pedro Gandola
 */
public class MessageListener {
	private Connector connector;
	private MessageParser parser;
	private MessageHandlerFactory handlerFactory;
	private int clientId;
	
	/**
	 * NOTE: I put all of MessageListener's functionality into the interface (and turned it into a class).
	 *       We can abstract this back to an interface later if needed, but actually the interface performs
	 *       generic enough tasks that we might keep it as a class.
	 * @param handlerFactory
	 * @param conn
	 */
	public MessageListener(MessageParser parser, MessageHandlerFactory handlerFactory, int clientId) {
		this.parser = parser;
		this.handlerFactory = handlerFactory;
		this.clientId = clientId;
	}
	
	public void setParent(Connector connector) {
		this.connector = connector;
	}

    public void onMessage(Message message) {
        AbstractMessageHandler handler = handlerFactory.create(connector, message, clientId);
        handler.handle(message, connector, this.parser);
    }
}
