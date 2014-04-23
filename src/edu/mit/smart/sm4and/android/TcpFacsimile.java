//Copyright (c) 2013 Singapore-MIT Alliance for Research and Technology
//Licensed under the terms of the MIT License, as described in the file:
//   license.txt   (http://opensource.org/licenses/MIT)

package edu.mit.smart.sm4and.android;

import edu.mit.csail.jasongao.roadrunner.util.LoggingRuntimeException;
import edu.mit.smart.sm4and.SimMobilityBroker;
import edu.mit.smart.sm4and.connector.Connector;
import edu.mit.smart.sm4and.handler.AbstractMessageHandler;
import edu.mit.smart.sm4and.json.ByteArraySerialization;
import edu.mit.smart.sm4and.message.DefaultMessageTypes.OpaqueSendMessage;
import edu.mit.smart.sm4and.message.Message;
import edu.mit.smart.sm4and.message.MessageParser;

/**
 * Bears a similar interface to a normal Java socket (with writers/readers enclosed, though).
 * Routes all messages through Sim Mobility as packets. 
 * Use the Broker to retrieve this easily.
 */
public class TcpFacsimile {
	private SimMobilityBroker broker;
	private String host;
	private int port;
	
	/*pakage-private*/ TcpFacsimile(SimMobilityBroker broker, String host, int port) {
		this.broker = broker;
		this.host = host;
		this.port = port;
	}
	
	public String getHost() {
		return host;
	}
	
	public int getPort() {
		return port;
	}
	
	public String readLine() {
		if (true) {
			throw new LoggingRuntimeException("TCP connection stuff needs to be posted (or somehow thread-safe), since it's run on another thread.");
		}
		
		//TODO
		return "";
	}
	
	public void writeLine(String line) {
		if (!line.endsWith("\n")) {
			line = line + "\n";
		}
		
		//Encode, send it.
		OpaqueSendMessage msg = new OpaqueSendMessage();
		msg.from_id = broker.getUniqueId();
		msg.to_ids = new String[]{host+":"+port};
		msg.broadcast = false;
		msg.data = ByteArraySerialization.Serialize(line.getBytes());
		broker.forwardMessageToServer(msg);
	}
}
