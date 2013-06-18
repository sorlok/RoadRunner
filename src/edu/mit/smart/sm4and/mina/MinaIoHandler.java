//Copyright (c) 2013 Singapore-MIT Alliance for Research and Technology
//Licensed under the terms of the MIT License, as described in the file:
//   license.txt   (http://opensource.org/licenses/MIT)

package edu.mit.smart.sm4and.mina;

import org.apache.mina.core.service.*;
import org.apache.mina.core.session.*;

import java.util.logging.Logger;

/**
 * A very simple handler intended for use with a MinaConnector which traces all 
 * callbacks and does the following:
 *   * Calls parent.setSession() when the session is created or opened.
 *   * Calls parent.handleMessage() when a message has been received.
 *
 * @author Pedro Gandola
 * @author Vahid
 */
public class MinaIoHandler implements IoHandler {
	private MinaConnector parent;
	private Logger logger;
    
    /**
     * Create a new handler for Apache Mina IO.
     * @param parent The parent MinaHandler.
     * @param logger Logging handle.
     */
    public MinaIoHandler(MinaConnector parent, Logger logger) {
        this.parent = parent;
        this.logger = logger;
    }
    
    @Override
    public void sessionCreated(IoSession is) throws Exception {
    	logger.info("(MinaHandler) Session Created");
    	parent.setSession(is);
    }

    @Override
    public void sessionOpened(IoSession is) throws Exception {
    	logger.info("(MinaHandler) Session Opened");
    	parent.setSession(is);
    }

    @Override
    public void sessionClosed(IoSession is) throws Exception {
    	logger.info("(MinaHandler) Session Closed");
    }

    @Override
    public void sessionIdle(IoSession is, IdleStatus is1) throws Exception {
    	logger.info("(MinaHandler) Session Idle");
    }

    @Override
    public void exceptionCaught(IoSession is, Throwable thrwbl) throws Exception {
    	//Propagate.
    	throw new RuntimeException(thrwbl);
    }

    @Override
    public void messageReceived(IoSession is, Object o) throws Exception {
    	logger.info("(MinaHandler) received[" + o.toString() + "]");
    	parent.handleMessage(o.toString());
    }

    @Override
    public void messageSent(IoSession is, Object o) throws Exception {
    	logger.info(String.format("(MinaHandler) Message: %s was sent.", o.toString()));
    }
}
