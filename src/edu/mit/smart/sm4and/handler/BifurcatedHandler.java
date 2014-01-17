//Copyright (c) 2013 Singapore-MIT Alliance for Research and Technology
//Licensed under the terms of the MIT License, as described in the file:
//   license.txt   (http://opensource.org/licenses/MIT)

package edu.mit.smart.sm4and.handler;

import edu.mit.smart.sm4and.AbstractMessageHandler;
import edu.mit.smart.sm4and.Connector;
import edu.mit.smart.sm4and.MessageParser;
import edu.mit.smart.sm4and.message.Message;

/**
 * A special handler that contains 2 different handlers. Its "handle()" method will 
 * call each child handler individually.
 */
public class BifurcatedHandler extends AbstractMessageHandler {
	private AbstractMessageHandler first;
	private AbstractMessageHandler second;
	
    private BifurcatedHandler(AbstractMessageHandler first, AbstractMessageHandler second) {
        this.first = first;
        this.second = second;
    }

    @Override
    public void handle(Message message, Connector connector, MessageParser parser) {
    	first.handle(message, connector, parser);
    	second.handle(message, connector, parser);
    }
    
    /**
     * Creates a (potentially) bifurcated message handler from two sub-handlers.
     * @param first The first handler.
     * @param second The second handler.
     * @return A BifurcatedMessageHandler representing first and second. If only one is non-null, then that handler itself is returned. 
     *         If both are null, returns null.
     */
    public static AbstractMessageHandler CreateHandler(AbstractMessageHandler first, AbstractMessageHandler second) {
    	if (first!=null && second!=null) {
    		return new BifurcatedHandler(first, second);
    	}
    	
    	if (first!=null) {
    		return first;
    	} else if (second != null) {
    		return second;
    	} 
    	
    	return null;
    }
}
