package edu.mit.csail.sethhetu.roadrunner;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.InetSocketAddress;
import java.net.Socket;

import edu.mit.csail.jasongao.roadrunner.Globals;

/**
 * The SimMobilityBroker is used by RoadRunner to communicate with Sim Mobility. 
 * The basic structure of this communication is as follows:
 *     1) Android entity establishes the connection (similar to how it connects to a Cloud).
 *     2) Sim Mobility signals that the connection was successful. 
 *     3) Sim Mobility signals that time tick 1 is complete.
 *        A) Any number of additional messages may be bundled with this update.
 *     4) Android entity confirms that time tick 1 is complete.
 *        A) Any number of additional messages may be bundled with this update.
 *     5) Steps 3 and 4 continue for time ticks 2, 3, 4, etc.
 * This behavior can be enabled by setting Globals.SIM_MOBILITY to "true".
 */
public class SimMobilityBroker  {
	//We need to maintain an open connection to the Sim Mobility server, since we are in a 
	//  tight time loop.
	private Socket smSocket;
	
	//Open streams for communicating with the server.
	private BufferedReader reader;
	private BufferedWriter writer;
	
	
	/**
	 * Create the broker entity and connect to the server.
	 */
	public SimMobilityBroker() {
		smSocket = new Socket();
		try {
			smSocket.connect(new InetSocketAddress(Globals.SM_HOST, Globals.SM_PORT), Globals.SM_TIMEOUT);

			//Retrieve underlying input/output streams.
			InputStream in = smSocket.getInputStream();
			OutputStream out = smSocket.getOutputStream();

			//Wrap these with Buffered readers/writers.
			this.reader = new BufferedReader(new InputStreamReader(in));
			this.writer = new BufferedWriter(new OutputStreamWriter(out));
			
			//Now we read one line, and see if the Sim Mobility server will accept us.
			String firstResponse = reader.readLine();
			if (firstResponse!="OK") {
				throw new IOException("Sim Mobility Server refused to accept us.");
			}
		} catch (IOException ex) {
			//Close our streams.
			this.closeStreams();
			
			//TODO: Shut down the app? Or...
			throw new RuntimeException(ex);
		}
	}
	
	private void closeStreams() {
		try {
			if (this.reader!=null) { this.reader.close(); }
		} catch (IOException e2) {}
		try {
			if (this.writer!=null) { this.writer.close(); }
		} catch (IOException e2) {}
	}
	
	
	
	
	
	
	
	
	
	

}
