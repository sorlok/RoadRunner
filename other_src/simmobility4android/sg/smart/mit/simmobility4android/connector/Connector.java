package sg.smart.mit.simmobility4android.connector;

import sg.smart.mit.simmobility4android.listener.MessageListener;

/**
 * Interface for connector implementation.
 */
public interface Connector {
    /**
     * Connects to server. 
     * @param host of the server.
     * @param port of the server.
     */
    void connect(String host, int port);
    
    /**
     * Disconnects from the server.
     * @param host
     * @param port
     */
    void disconnect();
    
    /**
     * Sends the given object.
     * @param data
     */
    void send(Object data);
    
    void listen(MessageListener listener);
}
