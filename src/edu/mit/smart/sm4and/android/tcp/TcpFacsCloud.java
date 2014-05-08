//Copyright (c) 2013 Singapore-MIT Alliance for Research and Technology
//Licensed under the terms of the MIT License, as described in the file:
//   license.txt   (http://opensource.org/licenses/MIT)

package edu.mit.smart.sm4and.android.tcp;

import java.io.IOException;
import java.util.LinkedList;

import edu.mit.smart.sm4and.SimMobilityBroker;
import edu.mit.smart.sm4and.json.ByteArraySerialization;
import edu.mit.smart.sm4and.message.DefaultMessageTypes.OpaqueSendMessage;

/**
 * Bears a similar interface to a normal Java socket (with writers/readers enclosed, though).
 * Routes all messages through Sim Mobility as packets. 
 * Use the Broker to retrieve this easily.
 */
public class TcpFacsCloud extends TcpFacsimile {
	//Incoming data, pushed by the server.
	protected LinkedList<String> incoming = new LinkedList<String>();
	
	public TcpFacsCloud(SimMobilityBroker broker, String host, int port, int timeout) {
		super(broker, host, port, timeout);
	}
	
	public void connect() throws IOException {
	}
	
	public void disconnect() {
	}
		
	//Called by outsiders to add an item to the read thread.
	public synchronized void addIncomingLine(String line) {
		incoming.add(line);
		notifyAll();
	}
	
	public synchronized String readLine() throws IOException {
		while (incoming.isEmpty()) {
			try {
				this.wait();
			} catch (InterruptedException ex) {}
		}
		
		return incoming.remove(0);
	}

	public void writeLine(String line) throws IOException {
		if (!line.endsWith("\n")) {
			line = line + "\n";
		}
		
		//Encode, send it.
		OpaqueSendMessage msg = new OpaqueSendMessage();
		msg.from_id = broker.getUniqueId();
		msg.format = OpaqueSendMessage.Format;
		msg.tech = OpaqueSendMessage.TechLte;
		msg.to_ids = new String[]{host+":"+port};
		msg.broadcast = false;
		msg.data = ByteArraySerialization.Serialize(line.getBytes());
		broker.forwardMessageToServer(msg);
	}
}
