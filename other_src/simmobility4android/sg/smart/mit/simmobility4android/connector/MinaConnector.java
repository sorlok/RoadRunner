//Copyright (c) 2013 Singapore-MIT Alliance for Research and Technology
//Licensed under the terms of the MIT License, as described in the file:
//   license.txt   (http://opensource.org/licenses/MIT)

package sg.smart.mit.simmobility4android.connector;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.service.IoConnector;
import org.apache.mina.core.service.IoHandler;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.textline.TextLineCodecFactory;
import org.apache.mina.transport.socket.nio.NioSocketConnector;

import edu.mit.csail.jasongao.roadrunner.RoadRunnerService.LocationSpoofer;
import edu.mit.csail.sethhetu.roadrunner.LoggerI;
import sg.smart.mit.simmobility4android.handler.HandlerFactory;
import sg.smart.mit.simmobility4android.handler.JsonHandlerFactory;
import sg.smart.mit.simmobility4android.listener.MessageListener;

/**
 *
 * @author Pedro Gandola
 * @author Vahid
 */
public class MinaConnector implements Connector {
    private IoConnector connector;
    private volatile boolean connected;
    private IoSession session;
    private IoHandler ioHandler;
    private MessageListener messageListener;
    private HandlerFactory handlerFactory;
    private int clientID;
    private final int BUFFER_SIZE = 2048;
    private final java.util.logging.Logger LOG = java.util.logging.Logger.getLogger(getClass().getCanonicalName());
    private LoggerI logger;
    
    public int getClientID() { return clientID; }
    
    public void setSession(IoSession sess) {
    	session = sess;
    }

    /**
     * Create a new connector based on Apache Mina
     * @param clientID_  The unique ID of this client. Used for communication.
     * @param locspoof   A handler for spoofing location-based updates. Used to set software lat/lng.
     * @param logger     A handler for logging.
     */
    public MinaConnector(int clientID_, LocationSpoofer locspoof, LoggerI logger) {
        this.clientID = clientID_;
        this.logger = logger;
        this.handlerFactory = new JsonHandlerFactory(locspoof);
        this.ioHandler = new MinaHandler(this, LOG);
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
        
        ConnectFuture future = connector.connect(new InetSocketAddress(host, port));
        connected = true; //todo: is this a good place to set the flag?
        future.awaitUninterruptibly();
        if (future.isConnected()) {
            session = future.getSession();
            session.getConfig().setUseReadOperation(true);
            session.getCloseFuture().awaitUninterruptibly();
        } else {
            logger.log("Connection could not be established:");
            logger.log(future.getException().toString());
        }
    }

    @Override
    public void disconnect() {
        if (connected) {
            if (session != null) {
                session.close(true);
                session.getCloseFuture().awaitUninterruptibly();
                session = null;
                connector.dispose();
            }
            connected = false;
        }
    }

    @Override
    public void send(Object data) {
        System.out.println("outgoing data : " + data.toString());
        
        if(!connected) {
            System.out.println("client "+ clientID + " we are NOT connected");
        }
        
        if(data == null) {
          System.out.println("client "+ clientID + " data is null");  
        }
        
        if(session == null) {
            System.out.println("client "+ clientID + " session is null");
        }
        
        if(!session.isConnected()){
            System.out.println("client "+ clientID + " session is not Connected");
        }
        
        if (connected && data != null && session != null && session.isConnected()) {
            String str = String.format("%8h%s", data.toString().length(), data.toString());
            session.write(str);
        }
    }

    @Override
    public void listen(MessageListener listener) {
        messageListener = listener;
    }
    
    public MessageListener getMessageListener() {
    	return messageListener;
    }
    
    public void handleMessage(Object o) {
        /*Handler handler = */handlerFactory.create(this, o, this.getClientID());
        //LOG.info("A handler was created,  ... handling");
        //handler.handle();
        getMessageListener().onMessage(o);
    }
    
}
