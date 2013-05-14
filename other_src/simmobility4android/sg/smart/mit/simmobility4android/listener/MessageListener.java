//Copyright (c) 2013 Singapore-MIT Alliance for Research and Technology
//Licensed under the terms of the MIT License, as described in the file:
//   license.txt   (http://opensource.org/licenses/MIT)

package sg.smart.mit.simmobility4android.listener;

import sg.smart.mit.simmobility4android.connector.Connector;
import sg.smart.mit.simmobility4android.connector.MinaConnector;
import sg.smart.mit.simmobility4android.handler.Handler;
import sg.smart.mit.simmobility4android.handler.HandlerFactory;

/**
 * @author Pedro Gandola
 */
public class MessageListener {
	private Connector mnc;
	private HandlerFactory handlerFactory;
	private int clientId;
	
	/**
	 * NOTE: I put all of MessageListener's functionality into the interface (and turned it into a class).
	 *       We can abstract this back to an interface later if needed, but actually the interface performs
	 *       generic enough tasks that we might keep it as a class.
	 * @param handlerFactory
	 * @param mnc
	 */
	public MessageListener(HandlerFactory handlerFactory, int clientId) {
		this.handlerFactory = handlerFactory;
		this.clientId = clientId;
	}
	
	public void setParent(Connector mnc) {
		this.mnc = mnc;
	}

    public void onMessage(Object message) {
        Handler handler = handlerFactory.create(mnc, message, clientId);
        handler.handle();
    }
}
