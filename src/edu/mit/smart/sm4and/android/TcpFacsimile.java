//Copyright (c) 2013 Singapore-MIT Alliance for Research and Technology
//Licensed under the terms of the MIT License, as described in the file:
//   license.txt   (http://opensource.org/licenses/MIT)

package edu.mit.smart.sm4and.android;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedList;

import android.util.Log;

import edu.mit.csail.jasongao.roadrunner.Globals;
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
	private int timeout;
	
	//Real connection (if fakeSocket is false)
	private Socket socket;
	private BufferedReader reader;
	private Writer writer;
	
	//Incoming data, pushed by the server.
	private LinkedList<String> incoming = new LinkedList<String>();
	
	/*pakage-private*/ TcpFacsimile(boolean fakeSocket, SimMobilityBroker broker, String host, int port, int timeout) {
		this.broker = broker;
		this.host = host;
		this.port = port;
		this.timeout = timeout;
		
		if (!fakeSocket) {
			socket = new Socket();
		}
	}
	
	public void connect() throws IOException {
		if (socket!=null) {
			socket.connect(new InetSocketAddress(host, port), timeout);

			reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			writer = new OutputStreamWriter(socket.getOutputStream());
		}
	}
	
	public void disconnect() {
		if (socket!=null) {
			try {
				socket.shutdownOutput();
			} catch (Exception e) {}

			try {
				socket.shutdownInput();
			} catch (Exception e) {}

			try {
				socket.close();
			} catch (Exception e) {}
		}
	}
	
	public String getHost() {
		return host;
	}
	
	public int getPort() {
		return port;
	}
	
	public boolean isFake() {
		return socket==null;
	}
	
	//Called by outsiders to add an item to the read thread.
	public synchronized void addIncomingLine(String line) {
		incoming.add(line);
		notifyAll();
	}
	
	public synchronized String readLine() throws IOException {
		if (socket!=null) {
			return reader.readLine();
		} else {
			while (incoming.isEmpty()) {
				try {
					this.wait();
				} catch (InterruptedException ex) {}
			}
			
			return incoming.remove(0);
		}
	}
	
	public void writeLine(String line) throws IOException {
		if (!line.endsWith("\n")) {
			line = line + "\n";
		}
		
		if (socket!=null) {
			writer.write(line);
			writer.flush();
		} else {
			//Encode, send it.
			OpaqueSendMessage msg = new OpaqueSendMessage();
			msg.from_id = broker.getUniqueId();
			msg.format = OpaqueSendMessage.Format;
			msg.to_ids = new String[]{host+":"+port};
			msg.broadcast = false;
			msg.data = ByteArraySerialization.Serialize(line.getBytes());
			broker.forwardMessageToServer(msg);
		}
	}
}
