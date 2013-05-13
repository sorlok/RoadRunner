/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sg.smart.mit.simmobility4android.listener;

import sg.smart.mit.simmobility4android.connector.Connector;
import sg.smart.mit.simmobility4android.connector.MinaConnector;
import sg.smart.mit.simmobility4android.handler.Handler;
import sg.smart.mit.simmobility4android.handler.HandlerFactory;
import sg.smart.mit.simmobility4android.message.Message;

/**
 *
 * @author gandola
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
	public MessageListener(HandlerFactory handlerFactory, Connector mnc, int clientId) {
		this.handlerFactory = handlerFactory;
		this.mnc = mnc; 
		this.clientId = clientId;
	}

    public void onMessage(Object message) {
        Handler handler = handlerFactory.create(mnc, message, clientId);
        handler.handle();
    }
}
