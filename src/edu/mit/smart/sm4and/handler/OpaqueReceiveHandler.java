//Copyright (c) 2013 Singapore-MIT Alliance for Research and Technology
//Licensed under the terms of the MIT License, as described in the file:
//   license.txt   (http://opensource.org/licenses/MIT)

package edu.mit.smart.sm4and.handler;

import edu.mit.csail.jasongao.roadrunner.util.LoggingRuntimeException;
import edu.mit.smart.sm4and.android.AndroidSimMobilityBroker.OpaqueMsgReceiver;
import edu.mit.smart.sm4and.connector.Connector;
import edu.mit.smart.sm4and.message.DefaultMessageTypes.OpaqueReceiveMessage;
import edu.mit.smart.sm4and.message.DefaultMessageTypes.OpaqueSendMessage;
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
    	if (!recvMsg.format.equals(OpaqueSendMessage.Format)) {
    		throw new LoggingRuntimeException("Received message in unexpected format: " + recvMsg.format);
    	}
    	
    	boolean isCloud = false;
    	if (recvMsg.tech.equals(OpaqueSendMessage.TechLte)) {
    		isCloud = true;
    	} else if (!recvMsg.tech.equals(OpaqueSendMessage.TechDsrc)) {
    		throw new LoggingRuntimeException("Unknown message \"tech\" on incoming opaque message.");
    	}
    	
        mcProcess.receive(recvMsg.from_id, recvMsg.to_id, isCloud, recvMsg.data);
    }
    
}
