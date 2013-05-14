//Copyright (c) 2013 Singapore-MIT Alliance for Research and Technology
//Licensed under the terms of the MIT License, as described in the file:
//   license.txt   (http://opensource.org/licenses/MIT)

package edu.mit.smart.sm4and.listener;

import edu.mit.smart.sm4and.Connector;
import edu.mit.smart.sm4and.Handler;
import edu.mit.smart.sm4and.HandlerFactory;

/**
 * Class that listens for messages and Handles them.
 * 
 * NOTE: I will probably remove this; it's a little bit too much indirection for me. ~Seth
 * 
 * @author Pedro Gandola
 */
public class MessageListener {
	private Connector conn;
	private HandlerFactory handlerFactory;
	private int clientId;
	
	/**
	 * NOTE: I put all of MessageListener's functionality into the interface (and turned it into a class).
	 *       We can abstract this back to an interface later if needed, but actually the interface performs
	 *       generic enough tasks that we might keep it as a class.
	 * @param handlerFactory
	 * @param conn
	 */
	public MessageListener(HandlerFactory handlerFactory, int clientId) {
		this.handlerFactory = handlerFactory;
		this.clientId = clientId;
	}
	
	public void setParent(Connector conn) {
		this.conn = conn;
	}

    public void onMessage(String message) {
        Handler handler = handlerFactory.create(conn, message, clientId);
        handler.handle();
    }
}
