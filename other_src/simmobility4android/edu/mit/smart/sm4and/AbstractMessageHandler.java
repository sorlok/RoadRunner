//Copyright (c) 2013 Singapore-MIT Alliance for Research and Technology
//Licensed under the terms of the MIT License, as described in the file:
//   license.txt   (http://opensource.org/licenses/MIT)

package edu.mit.smart.sm4and;

import edu.mit.smart.sm4and.message.Message;

/**
 * Interface for all message handler implementations.
 * 
 * Note that the message can only be set in the constructor (which is protected), 
 * and then only retrieved with the "getMessage()" function. Thus, we avoid the need
 * for a generic <T extends Message> which really only serves to simplify "getMessage()".
 * Now, you can just have the subclass cast the result of "getMessage()".
 *
 * @author Pedro Gandola
 */
public abstract class AbstractMessageHandler {
    protected AbstractMessageHandler() {
    }

    /**
     * Subclasses should handle Message here, relying on the Connector for response
     *  functionality.
     */
    public abstract void handle(Message message, Connector connector);
}
