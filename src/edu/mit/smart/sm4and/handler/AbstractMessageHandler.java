//Copyright (c) 2013 Singapore-MIT Alliance for Research and Technology
//Licensed under the terms of the MIT License, as described in the file:
//   license.txt   (http://opensource.org/licenses/MIT)

package edu.mit.smart.sm4and.handler;

import edu.mit.smart.sm4and.SimMobilityBroker;
import edu.mit.smart.sm4and.connector.Connector;
import edu.mit.smart.sm4and.message.Message;
import edu.mit.smart.sm4and.message.MessageParser;

/**
 * Interface for all message handler implementations. Any custom message type will need
 *  a Handler which reacts to received messages of that type.
 * The only method you should concern yourself with is handle().
 *
 * @author Pedro Gandola
 */
public abstract class AbstractMessageHandler {
    /**
     * Subclasses should handle Message here, relying on the Connector for response
     *  functionality.
     */
    public abstract void handle(Message message, Connector connector, MessageParser parser);
    
    /**
     * Set the Broker for this message handler. This function will be called by the SimMobilityBroker; 
     * there is no need to call it explicitly.
     * @param broker
     */
    public void setBroker(SimMobilityBroker broker) {
    	this.broker = broker;
    }
    
    /**
     * Test if a Broker has been set. Called internally.
     * @return
     */
    public boolean hasBroker() {
    	return this.broker != null;
    }
    
    protected SimMobilityBroker broker;
}
