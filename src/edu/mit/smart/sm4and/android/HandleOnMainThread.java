//Copyright (c) 2013 Singapore-MIT Alliance for Research and Technology
//Licensed under the terms of the MIT License, as described in the file:
//   license.txt   (http://opensource.org/licenses/MIT)

package edu.mit.smart.sm4and.android;

import edu.mit.smart.sm4and.AbstractMessageHandler;
import edu.mit.smart.sm4and.Connector;
import edu.mit.smart.sm4and.MessageParser;
import edu.mit.smart.sm4and.message.Message;

/**
 * Wraps an AbstractMessageHandler, calling its "handle" statement in a run block. 
 * Push these to the Android message queue to handle them one at a time.
 */
public class HandleOnMainThread implements Runnable {
	private AbstractMessageHandler handler;
	private Message message;
	private Connector connector;
	private MessageParser parser;
	
	public HandleOnMainThread(AbstractMessageHandler handler, Message message, Connector connector, MessageParser parser) {
		this.handler = handler;
		this.message = message;
		this.connector = connector;
		this.parser = parser;
	}
	
	@Override
	public void run() {
		handler.handle(message, connector, parser);
	}
}
