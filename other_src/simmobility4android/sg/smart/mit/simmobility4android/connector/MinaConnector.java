//Copyright (c) 2013 Singapore-MIT Alliance for Research and Technology
//Licensed under the terms of the MIT License, as described in the file:
//   license.txt   (http://opensource.org/licenses/MIT)

package sg.smart.mit.simmobility4android.connector;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.service.IoConnector;
import org.apache.mina.core.service.IoHandler;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.textline.TextLineCodecFactory;
import org.apache.mina.transport.socket.nio.NioSocketConnector;

import edu.mit.csail.jasongao.roadrunner.RoadRunnerService.LocationSpoofer;
import edu.mit.csail.jasongao.roadrunner.RoadRunnerService.Logger;
import sg.smart.mit.simmobility4android.handler.Handler;
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
    private MessageListener messageListener;
    private HandlerFactory handlerFactory;
    private int clientID;
    private final int BUFFER_SIZE = 2048;
    private final java.util.logging.Logger LOG = java.util.logging.Logger.getLogger(getClass().getCanonicalName());
    private Logger logger;
    
    public int getClientID() { return clientID; }

    /**
     * Create a new connector based on Apache Mina
     * @param clientID_  The unique ID of this client. Used for communication.
     * @param locspoof   A handler for spoofing location-based updates. Used to set software lat/lng.
     * @param logger     A handler for logging.
     */
    public MinaConnector(int clientID_, LocationSpoofer locspoof, Logger logger) {
        this.clientID = clientID_;
        this.logger = logger;
        this.handlerFactory = new JsonHandlerFactory(locspoof);
    }

    @Override
    public void connect(String host, int port) {
        if (!connected) {
            if (session != null && session.isConnected()) {
                session.close(true);
            }
            if (connector != null) {
                connector.dispose();
            }
            connector = new NioSocketConnector();
            connector.getSessionConfig().setUseReadOperation(true);
            connector.getSessionConfig().setReadBufferSize(BUFFER_SIZE);
            //connector.getFilterChain().addLast("logger", new LoggingFilter());
            connector.getFilterChain().addLast("codec", new ProtocolCodecFilter(new TextLineCodecFactory(Charset.forName("UTF-8"))));
            connector.setHandler(new IoHandler() {
                @Override
                public void sessionCreated(IoSession is) throws Exception {
                    session = is;
                    LOG.info("client "+ clientID + " Session Created");
                }

                @Override
                public void sessionOpened(IoSession is) throws Exception {
                    session = is;
                    LOG.info("client "+ clientID + " Session Opened");
                }

                @Override
                public void sessionClosed(IoSession is) throws Exception {
                     LOG.info("client "+ clientID + " Session Closed");
                }

                @Override
                public void sessionIdle(IoSession is, IdleStatus is1) throws Exception {
                     LOG.info("client "+ clientID + " Session Idle");
                }

                @Override
                public void exceptionCaught(IoSession is, Throwable thrwbl) throws Exception {
                    LOG.info("client "+ clientID + " Exception" + thrwbl.toString());
                }

                @Override
                public void messageReceived(IoSession is, Object o) throws Exception {
                    LOG.info("client "+ clientID + " received[" + o.toString() + "]");
//                    Handler handler = handlerFactory.create(MinaConnector.this, o, clientID);
//                    LOG.info("A handler was created,  ... handling");
//                    handler.handle();
                    MinaConnector.this.messageListener.onMessage(o);
                }

                @Override
                public void messageSent(IoSession is, Object o) throws Exception {
                    LOG.info(String.format("client %d Message: %s was sent.", clientID, o));
                }
            });
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
        
        if(connected) {
//            System.out.println("we are connected");
        } else {
            System.out.println("client "+ clientID + " we are NOT connected");
        }
        
        if(data == null) {
          System.out.println("client "+ clientID + " data is null");  
        }
        
        if(session != null) {
//            System.out.println("session not null");
        } else {
            System.out.println("client "+ clientID + " session is null");
        }
        
        if(session.isConnected()){
//            System.out.println("session.isConnected");
        } else {
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
}
