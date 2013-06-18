//Copyright (c) 2013 Singapore-MIT Alliance for Research and Technology
//Licensed under the terms of the MIT License, as described in the file:
//   license.txt   (http://opensource.org/licenses/MIT)

package edu.mit.smart.sm4and;

/**
 * Any connector implementation can handle server connections. 
 * A Connector can handle connecting/disconnecting, as well as sending 
 * data to the server and receiving data from the server.
 * 
 * Currently we support Apache MINA.
 * 
 * @author Pedro Gandola
 * @author Vahid
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
     * @param data The message you want to send.
     */
    void send(String data);
    
    /**
     * Handle a message sent from the server.
     * @param data The message that was received.
     */
    void handleMessage(String data);
}
