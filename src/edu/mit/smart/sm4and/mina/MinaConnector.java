//Copyright (c) 2013 Singapore-MIT Alliance for Research and Technology
//Licensed under the terms of the MIT License, as described in the file:
//   license.txt   (http://opensource.org/licenses/MIT)

package edu.mit.smart.sm4and.mina;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.ArrayList;

import edu.mit.smart.sm4and.AbstractMessageHandler;
import edu.mit.smart.sm4and.Connector;
import edu.mit.smart.sm4and.MessageHandlerFactory;
import edu.mit.smart.sm4and.MessageParser;
import edu.mit.smart.sm4and.handler.SimpleAndroidHandler;
import edu.mit.smart.sm4and.message.Message;

import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.service.IoConnector;
import org.apache.mina.core.service.IoHandler;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.textline.TextLineCodecFactory;
import org.apache.mina.transport.socket.nio.NioSocketConnector;

import android.os.Handler;

import edu.mit.csail.jasongao.roadrunner.Globals;
import edu.mit.csail.jasongao.roadrunner.RoadRunnerService.LocationSpoofer;
import edu.mit.csail.sethhetu.roadrunner.LoggerI;

/**
 * A connector which targets Apache Mina.
 * 
 * @author Pedro Gandola
 * @author Vahid
 */
public class MinaConnector extends Connector {
	private Handler myHandler;
    private IoConnector connector;
    private volatile boolean connected;
    private IoSession session;
    private IoHandler ioHandler;
    private MessageParser parser;
    private MessageHandlerFactory handlerFactory;
    private final int BUFFER_SIZE = 2048;
    private final java.util.logging.Logger LOG = java.util.logging.Logger.getLogger(getClass().getCanonicalName());
    private LoggerI logger;
    
    public void setSession(IoSession sess) {
    	session = sess;
    }

    /**
     * Create a new connector based on Apache Mina
     * @param locspoof   A handler for spoofing location-based updates. Used to set software lat/lng.
     * @param logger     A handler for logging.
     */
    public MinaConnector(Handler myHandler, MessageParser parser, MessageHandlerFactory handlerFactory, LocationSpoofer locspoof, LoggerI logger) {
    	this.myHandler = myHandler;
        this.logger = logger;
        this.parser = parser;
        this.handlerFactory = handlerFactory;
        this.ioHandler = new MinaIoHandler(this, LOG);
    }

    
    /**
     * Connect to a specific host and port using the MINA connector.
     * This function will also start processing messages. 
     */
    @Override
    public void connect(String host, int port) {
    	if (connected) {
    		System.out.println("NOTE: MinaConnector.connect() called, but already connected.");
    		return;
    	}
    	
    	//Reset the session and connector if they are in use.
        if (session!=null && session.isConnected()) {
            session.close(true);
        }
        if (connector!=null) {
            connector.dispose();
        }
        
        //Initialize the connector object.
        connector = new NioSocketConnector();
        connector.getSessionConfig().setUseReadOperation(true);
        connector.getSessionConfig().setReadBufferSize(BUFFER_SIZE);
        connector.getFilterChain().addLast("codec", new ProtocolCodecFilter(new TextLineCodecFactory(Charset.forName("UTF-8"))));
        connector.setHandler(ioHandler);
        
        //Connect to the server and wait forever until it makes contact.
        logger.log("Attempting to connect to MINA server on " + host + ":" + port);
        ConnectFuture future = connector.connect(new InetSocketAddress(host, port));
        future.awaitUninterruptibly();
        
        //Check if an actual connection was made, or if some error occurred.
        if (future.isConnected()) {
        	connected = true;
        } else {
            logger.log("Connection could not be established:");
            logger.log(future.getException().toString());
            return;
        }
        
        //Set some properties of the session.
        //NOTE: I am not 100% sure how "setUseReadOperation()" affects data sent *before* it is called.
        session = future.getSession();
        session.getConfig().setUseReadOperation(true);
        
        //NOTE: This shouldn't be needed; we don't *need* to wait for the session to close here.
        //session.getCloseFuture().awaitUninterruptibly();
    }

    @Override
    public void disconnect() {
        if (connected) {
            if (session != null) {
                session.close(true).awaitUninterruptibly();
                session = null;
            }
            if (connector != null) { //Not likely, but check anyway.
            	connector.dispose();
            	connector = null;
            }
            connected = false;
        }
    }
    

    @Override
    public void sendAll(String data) {        
        if (connected && (data!=null) && (session!=null) && session.isConnected()) {
        	String str = String.format("%8h%s", data.toString().length()+1, data.toString());
        	if (Globals.SM_VERBOSE_TRACE) {
        		System.out.println("Outgoing data: ***" + str + "***");
        	}
        	session.write(str);
        } else {
        	StringBuilder sb = new StringBuilder("Can't send data to server:");
        	sb.append("  connected=").append(connected);
        	sb.append("  data=").append(data!=null ? data : "<NULL>");
        	sb.append("  session=").append(session!=null ? session.isConnected() : "<NULL>");
        	System.out.println(sb.toString());
        }
    }
    
    @Override
    public void handleMessage(String data) {
    	//Trim the first 8 bytes.
    	//String trim = data.substring(0,8);
    	data = data.substring(8);
    	
    	//Just pass off each message to "handle()"
    	ArrayList<Message> messages = parser.parse(data);
    	for (Message message : messages) {
    		//Get an appropriate response handler.
    		AbstractMessageHandler handler = handlerFactory.create(message.getMessageType());
    		
    		//We want to process this on the main thread, as we may want to interact with the user.
    		//Thus, we post it to the message queue.
    		SimpleAndroidHandler sam = new SimpleAndroidHandler(handler, message, this, parser);
    		myHandler.post(sam);
    	}
    }
    
}
