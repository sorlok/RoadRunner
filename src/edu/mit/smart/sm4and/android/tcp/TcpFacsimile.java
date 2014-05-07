//Copyright (c) 2013 Singapore-MIT Alliance for Research and Technology
//Licensed under the terms of the MIT License, as described in the file:
//   license.txt   (http://opensource.org/licenses/MIT)

package edu.mit.smart.sm4and.android.tcp;

import java.io.*;

import edu.mit.smart.sm4and.SimMobilityBroker;

/**
 * Contains a similar interest to a normal Java socket (readers/writers are enclosed).
 * Subclasses either use a socket directly or send all communications via Sim Mobility messages.
 * Use the Broker to retrieve the correct subclass easily.
 * 
 * WARNING: It seems that under the current Android threading mode, clients that connect() then write() then read() will
 *          have their data processed in the right order. HOWEVER, this might also be a result of the generic nature of
 *          our current Cloud requests (e.g., they are basically stateless). Essentially, if large deployments start 
 *          freezing for no known reason (and cloud writing is not disabled) then you might want to check here. 
 */
public abstract class TcpFacsimile {
	protected SimMobilityBroker broker;
	protected String host;
	protected int port;
	protected int timeout;
	
	protected TcpFacsimile(SimMobilityBroker broker, String host, int port, int timeout) {
		this.broker = broker;
		this.host = host;
		this.port = port;
		this.timeout = timeout;
	}
	
	public String getHost() {
		return host;
	}
	
	public int getPort() {
		return port;
	}

	public abstract void connect() throws IOException;
	
	public abstract void disconnect();
	
	public abstract String readLine() throws IOException;
	
	public abstract void writeLine(String line) throws IOException;
}
