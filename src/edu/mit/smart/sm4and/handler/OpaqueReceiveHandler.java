//Copyright (c) 2013 Singapore-MIT Alliance for Research and Technology
//Licensed under the terms of the MIT License, as described in the file:
//   license.txt   (http://opensource.org/licenses/MIT)

package edu.mit.smart.sm4and.handler;

import edu.mit.smart.sm4and.android.AndroidSimMobilityBroker.OpaqueMsgReceiver;
import edu.mit.smart.sm4and.connector.Connector;
import edu.mit.smart.sm4and.message.DefaultMessageTypes.OpaqueReceiveMessage;
import edu.mit.smart.sm4and.message.Message;
import edu.mit.smart.sm4and.message.MessageParser;

/**
 * Handle a multicast message.
 * @author Vahid
 */
public class OpaqueReceiveHandler extends AbstractMessageHandler {	
	private OpaqueMsgReceiver mcProcess;
	    
    public OpaqueReceiveHandler(OpaqueMsgReceiver mcProcess) {
    	this.mcProcess = mcProcess;
    }

    @Override
    public void handle(Message message, Connector connector, MessageParser parser) {
    	//Hand back to the Broker
    	OpaqueReceiveMessage recvMsg = (OpaqueReceiveMessage)message;
        mcProcess.receive(recvMsg.from_id, recvMsg.data);
    }
    
}
