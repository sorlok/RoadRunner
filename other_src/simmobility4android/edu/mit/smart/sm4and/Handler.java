//Copyright (c) 2013 Singapore-MIT Alliance for Research and Technology
//Licensed under the terms of the MIT License, as described in the file:
//   license.txt   (http://opensource.org/licenses/MIT)

package edu.mit.smart.sm4and;

import edu.mit.smart.sm4and.message.Message;

/**
 * Interface for all handler implementations.
 * 
 * Note that the message can only be set in the constructor (which is protected), 
 * and then only retrieved with the "getMessage()" function. Thus, we avoid the need
 * for a generic <T extends Message> which really only serves to simplify "getMessage()".
 * Now, you can just have the subclass cast the result of "getMessage()".
 *
 * @author Pedro Gandola
 */
public abstract class Handler {
    private final Message message;
    private final Connector connector;

    protected Handler(Message message, Connector connector) {
        this.message = message;
        this.connector = connector;
    }

    /**
     * Subclasses should handle Message here, relying on the Connector for response
     *  functionality.
     */
    public abstract void handle();

    protected Connector getConnector() {
        return connector;
    }

    public Message getMessage() {
        return message;
    }
}
