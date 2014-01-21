//Copyright (c) 2013 Singapore-MIT Alliance for Research and Technology
//Licensed under the terms of the MIT License, as described in the file:
//   license.txt   (http://opensource.org/licenses/MIT)

package edu.mit.smart.sm4and.handler;

import edu.mit.smart.sm4and.connector.Connector;
import edu.mit.smart.sm4and.message.DefaultMessageTypes.UnicastMessage;
import edu.mit.smart.sm4and.message.Message;
import edu.mit.smart.sm4and.message.MessageParser;

/**
 * Handle a unicast message.
 * @author Vahid
 */
public class UnicastHandler extends AbstractMessageHandler {	    
    @Override
    public void handle(Message message, Connector connector, MessageParser parser) {
        //TODO: Unicast messages are specific to-agent messages. Currently, they are not implemented,
        //      but will be needed for key exchanges.
    	UnicastMessage ucMsg = (UnicastMessage)message;
        System.out.println("Unicast message received of length: " + ucMsg.UNICAST_DATA.length());
    }
    
}
