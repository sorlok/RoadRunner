/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sg.smart.mit.simmobility4android.listener;

import sg.smart.mit.simmobility4android.connector.MinaConnector;
import sg.smart.mit.simmobility4android.handler.Handler;
import sg.smart.mit.simmobility4android.handler.HandlerFactory;
import sg.smart.mit.simmobility4android.message.Message;

/**
 *
 * @author gandola
 */
public class MessageListener {
	private MinaConnector mnc;
	private HandlerFactory handlerFactory;
	
	public MessageListener(HandlerFactory handlerFactory, MinaConnector mnc) {
		this.handlerFactory = handlerFactory;
		this.mnc = mnc; 
	}

    public void onMessage(Object message) {
        Handler handler = handlerFactory.create(mnc, message, mnc.getClientID());
        handler.handle();
    }
}
