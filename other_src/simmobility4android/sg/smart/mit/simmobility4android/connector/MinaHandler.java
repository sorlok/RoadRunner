//Copyright (c) 2013 Singapore-MIT Alliance for Research and Technology
//Licensed under the terms of the MIT License, as described in the file:
//   license.txt   (http://opensource.org/licenses/MIT)

package sg.smart.mit.simmobility4android.connector;

import org.apache.mina.core.service.*;
import org.apache.mina.core.session.*;

import java.util.logging.Logger;

/**
 * A very simple handler that traces all callbacks and does the following:
 *   * Calls parent.setSession() when the session is created or opened.
 *   * Calls parent.handleMessage() when a message has been received.
 *
 * @author Pedro Gandola
 * @author Vahid
 */
public class MinaHandler implements IoHandler {
	private MinaConnector parent;
	private Logger logger;
    
    /**
     * Create a new handler for Apache Mina IO.
     * @param parent The parent MinaHandler.
     * @param logger Logging handle.
     */
    public MinaHandler(MinaConnector parent, Logger logger) {
        this.parent = parent;
        this.logger = logger;
    }
    
    @Override
    public void sessionCreated(IoSession is) throws Exception {
    	logger.info("client "+ parent.getClientID() + " Session Created");
    	parent.setSession(is);
    }

    @Override
    public void sessionOpened(IoSession is) throws Exception {
    	logger.info("client "+ parent.getClientID() + " Session Opened");
    	parent.setSession(is);
    }

    @Override
    public void sessionClosed(IoSession is) throws Exception {
    	logger.info("client "+ parent.getClientID() + " Session Closed");
    }

    @Override
    public void sessionIdle(IoSession is, IdleStatus is1) throws Exception {
    	logger.info("client "+ parent.getClientID() + " Session Idle");
    }

    @Override
    public void exceptionCaught(IoSession is, Throwable thrwbl) throws Exception {
    	logger.info("client "+ parent.getClientID() + " Exception" + thrwbl.toString());
    }

    @Override
    public void messageReceived(IoSession is, Object o) throws Exception {
    	logger.info("client "+ parent.getClientID() + " received[" + o.toString() + "]");
    	parent.handleMessage(o);
    }

    @Override
    public void messageSent(IoSession is, Object o) throws Exception {
    	logger.info(String.format("client %d Message: %s was sent.", parent.getClientID(), o));
    }
}
