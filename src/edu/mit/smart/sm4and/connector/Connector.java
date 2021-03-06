//Copyright (c) 2013 Singapore-MIT Alliance for Research and Technology
//Licensed under the terms of the MIT License, as described in the file:
//   license.txt   (http://opensource.org/licenses/MIT)

package edu.mit.smart.sm4and.connector;

import java.io.IOException;
import java.util.ArrayList;

import edu.mit.smart.sm4and.message.Message;
import edu.mit.smart.sm4and.message.MessageParser;
import edu.mit.smart.sm4and.message.MessageParser.MessageBundle;

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
public abstract class Connector {
    /**
     * Connects to server.
     * @param host of the server.
     * @param port of the server.
     * 
     * Throws an exception if the connection could not be made.
     */
    public abstract void connect(MessageParser parser, String host, int port) throws IOException;
    
    /**
     * Disconnects from the server.
     * @param host
     * @param port
     */
    public abstract void disconnect();
    
    /**
     * Add a message to the queue (send later)
     * Synchronized: our TCP threads can send messages in parallel with our MessageHandlers (invoked by the MINA thread).
     */
    public synchronized void addMessage(Message msg) { messages.add(msg); }
    
    /**
     * Retrieve and clear all messages.
     * Synchronized: our TCP threads can send messages in parallel with our MessageHandlers (invoked by the MINA thread).
     */
    public synchronized ArrayList<Message> getAndClearMessages() {
    	ArrayList<Message> res = messages;
    	messages = new ArrayList<Message>();
    	return res;
    }
    
    /**
     * Sends the given object.
     * @param data The message you want to send.
     */
    public abstract void sendAll(MessageBundle bundle);
    
    /**
     * Handle a bundle of messages sent from the server.
     * @param data The full bundle string (fixed+varying headers, +messages)
     */
    public abstract void handleBundle(MessageBundle bundle);
    
    private ArrayList<Message> messages = new ArrayList<Message>();
}
