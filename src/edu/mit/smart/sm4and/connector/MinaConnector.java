//Copyright (c) 2013 Singapore-MIT Alliance for Research and Technology
//Licensed under the terms of the MIT License, as described in the file:
//   license.txt   (http://opensource.org/licenses/MIT)

package edu.mit.smart.sm4and.connector;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;

import edu.mit.smart.sm4and.SimMobilityBroker;
import edu.mit.smart.sm4and.handler.AbstractMessageHandler;
import edu.mit.smart.sm4and.handler.MessageHandlerFactory;
import edu.mit.smart.sm4and.message.Message;
import edu.mit.smart.sm4and.message.MessageParser;
import edu.mit.smart.sm4and.message.MessageParser.MessageBundle;

import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.service.IoConnector;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.textline.TextLineCodecFactory;
import org.apache.mina.transport.socket.SocketSessionConfig;
import org.apache.mina.transport.socket.nio.NioSocketConnector;

import edu.mit.csail.jasongao.roadrunner.Globals;
import edu.mit.csail.jasongao.roadrunner.util.LoggingRuntimeException;

/**
 * A connector which targets Apache Mina.
 * 
 * @author Pedro Gandola
 * @author Vahid
 */
public class MinaConnector extends Connector {
	private volatile boolean connected;
	
	private SimMobilityBroker broker;
	
    private IoConnector connector;
    private IoSession session;
    private MessageHandlerFactory handlerFactory;
    
    private final int BUFFER_SIZE = 20480;
    
    private final java.util.logging.Logger LOG = java.util.logging.Logger.getLogger(getClass().getCanonicalName());
    
    public void setSession(IoSession sess) {
    	session = sess;
    }

    /**
     * Create a new connector based on Apache Mina
     * @param locspoof   A handler for spoofing location-based updates. Used to set software lat/lng.
     * @param logger     A handler for logging.
     */
    public MinaConnector(SimMobilityBroker broker, MessageHandlerFactory handlerFactory) {
        this.broker = broker;
        this.handlerFactory = handlerFactory;
    }

    
    /**
     * Connect to a specific host and port using the MINA connector.
     * This function will also start processing messages. 
     */
    @Override
    public void connect(MessageParser parser, String host, int port) throws IOException {
    	if (connected) { return; }
        connected = true;
    	
    	//Reset the session and connector if they are in use.
        if (session!=null && session.isConnected()) {
            session.close(true);
            session = null;
        }
        if (connector!=null) {
            connector.dispose();
            connector = null;
        }
        
        //Initialize the connector object, set its handler.
        connector = new NioSocketConnector();
        connector.getSessionConfig().setUseReadOperation(true);
        ((SocketSessionConfig)connector.getSessionConfig()).setTcpNoDelay(false); //Turn off Nagle's algorithm.
        connector.setHandler(new MinaIoHandler(this, LOG));
        
        //Create a UTF-8 decoder, make sure that all buffers are the right size.
        //NOTE: it seems that TextLineCodecFactory's's length is all that matters. ~Seth
        //TextLineCodecFactory utf8Filter = new TextLineCodecFactory(Charset.forName("UTF-8"));
        //utf8Filter.setDecoderMaxLineLength(BUFFER_SIZE);
        connector.getSessionConfig().setMinReadBufferSize(BUFFER_SIZE);
        
        //Add this codec to the filter chain.
        //connector.getFilterChain().addLast("codec", new ProtocolCodecFilter(utf8Filter));
        connector.getFilterChain().addLast("protocol", new ProtocolCodecFilter(new MinaProtocols(parser)));
        
        //Connect to the server and wait forever until it makes contact.
        ConnectFuture future = connector.connect(new InetSocketAddress(host, port));
        future.awaitUninterruptibly();
        
        //Check if an actual connection was made, or if some error occurred.
        if (!future.isConnected()) {
        	throw new IOException("Connection could not be established: " + future.getException().toString());
        }
        
        //Set some properties of the session.
        session = future.getSession();
        session.getConfig().setUseReadOperation(true);
    }

    @Override
    public void disconnect() {
    	if (!connected) { return; }

        if (session != null) {
            session.close(true).awaitUninterruptibly();
            session = null;
        }
        if (connector != null) {
        	connector.dispose();
        	connector = null;
        }
        connected = false;
    }
    

    @Override
    public void sendAll(MessageBundle bundle) {
        if (connected && (bundle!=null) && (session!=null) && session.isConnected()) {
        	if (Globals.SM_VERBOSE_TRACE) {
        		//TODO: Trace later (inside MinaProtocol?)
        		System.out.println("Outgoing data: ***" + "***"); 
        	}
        	session.write(bundle);

        } else {
        	StringBuilder sb = new StringBuilder("Can't send data to server:");
        	sb.append("  connected=").append(connected);
        	sb.append("  data=").append(bundle!=null ? "<non-null>" : "<NULL>");
        	sb.append("  session=").append(session!=null ? session.isConnected() : "<NULL>");
        	System.out.println(sb.toString());
        }
    }
    
    public static String escape_invalid_json(String src) {
    	StringBuilder res = new StringBuilder();
		for (int i=0; i<src.length(); i++) {
			char c = src.charAt(i);
			if (c=='&') { res.append("&&"); }
			else if (c=='\\') { res.append("&1"); }
			else if (c=='"') { res.append("&2"); }
			else { res.append(c); }
		}
		return res.toString();
    }
    
    
    @Override
    public void handleBundle(MessageBundle bundle) {    	
    	//Remote log:
    	if (Globals.SM_LOG_TRACE_ALL_MESSAGES) {
    		//TODO: Perhaps trace later? It's unclear how to do this with Bundle v1 format.
    		broker.ReflectToServer("RECIEVE: " + escape_invalid_json("(todo)"));
    	}
    	
    	//Double-check
		if (!bundle.destId.equals("0") && !broker.getUniqueId().equals(bundle.destId)) {
			if (!Globals.SM_RERUN_FULL_TRACE) {
				throw new LoggingRuntimeException("Agent destination ID mismatch; expected: " + broker.getUniqueId() + "; was: " + bundle.destId);
			}
		}
    	
		//Just pass off each message to "handle()"
    	for (Message message : bundle.messages) {    		
    		//Get an appropriate response handler.
    		AbstractMessageHandler handler = handlerFactory.create(message.getMessageType());
    		
    		//Ask the Broker to post this message on the main thread.
    		broker.handleMessage(handler, message);
    	}
    }
}
