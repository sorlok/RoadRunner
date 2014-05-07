//Copyright (c) 2013 Singapore-MIT Alliance for Research and Technology
//Licensed under the terms of the MIT License, as described in the file:
//   license.txt   (http://opensource.org/licenses/MIT)

package edu.mit.smart.sm4and.android.tcp;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.LinkedList;

import edu.mit.smart.sm4and.SimMobilityBroker;
import edu.mit.smart.sm4and.json.ByteArraySerialization;
import edu.mit.smart.sm4and.message.DefaultMessageTypes.OpaqueSendMessage;

/**
 * Subclass of TcpFacsimile that sends all data through a normal Java socket.
 */
public class TcpFacsSocket extends TcpFacsimile {
	protected Socket socket;
	protected BufferedReader reader;
	protected Writer writer;
	
	public TcpFacsSocket(SimMobilityBroker broker, String host, int port, int timeout) {
		super(broker, host, port, timeout);
		socket = new Socket();
	}
	
	public void connect() throws IOException {
		socket.connect(new InetSocketAddress(host, port), timeout);

		reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		writer = new OutputStreamWriter(socket.getOutputStream());
	}
	
	public void disconnect() {
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

	public synchronized String readLine() throws IOException {
		return reader.readLine();
	}
	
	public void writeLine(String line) throws IOException {
		if (!line.endsWith("\n")) {
			line = line + "\n";
		}
		
		writer.write(line);
		writer.flush();
	}
}
