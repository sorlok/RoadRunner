package edu.mit.csail.sethhetu.roadrunner;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

import android.os.Handler;
import edu.mit.csail.jasongao.roadrunner.Globals;
import edu.mit.csail.jasongao.roadrunner.ResRequest;
import edu.mit.csail.jasongao.roadrunner.RoadRunnerService.Logger;
import edu.mit.csail.sethhetu.roadrunner.SimMobServerConnectTask.PostExecuteAction;

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
public class SimMobilityBroker  implements PostExecuteAction {
	//We need to maintain an open connection to the Sim Mobility server, since we are in a 
	//  tight time loop.
	private Socket smSocket;
	
	//Open streams for communicating with the server.
	private BufferedReader reader;
	private BufferedWriter writer;
	
	//For communicating back to the RoadRunner service.
	private Handler myHandler;
	private Logger logger;
	
	//What's the time according to Sim Mobility?
	private long currTimeMs;

	@Override
	public void onPostExecute(Exception thrownException, BufferedReader reader, BufferedWriter writer) {
		//Was there an error?
		if (thrownException!=null) {
			SimMobilityBroker.this.closeStreams();
			throw new RuntimeException(thrownException);
		}
		
		//Save objects locally.
		this.reader = reader;
		this.writer = writer;
		
		System.out.println("TEST1: " + this.reader);
		
		//Assuming everything went ok, start our simulation loop. It looks like this:
		//   1) Send out an Async task waiting for the "tick done" message.
		//   2) When that's done, process it and send "android done" for this time step.
		//As always, messages are sent before the final "done" message.
		this.currTimeMs = 0;
		new SimMobTickRequest(myHandler, new ServerTickDoneRunnable()).execute(this.reader);
	}
	
	
	/**
	 * Create the broker entity and connect to the server.
	 */
	public SimMobilityBroker(Handler myHandler, Logger logger) {
		this.myHandler = myHandler;
		this.logger = logger;
		
		smSocket = new Socket();
		SimMobServerConnectTask task = new SimMobServerConnectTask(this);
		task.execute(smSocket);
	}
	
	private void closeStreams() {
		try {
			if (this.reader!=null) { this.reader.close(); }
		} catch (IOException e2) {}
		try {
			if (this.writer!=null) { this.writer.close(); }
		} catch (IOException e2) {}
	}
	
	
	//Called when the server states that a time tick has completed.
	public class ServerTickDoneRunnable implements Runnable {
		//The line received from the server.
		private String line;
		
		public ServerTickDoneRunnable() {}
		
		public void setLine(String line) {
			this.line = line;
		}
		
		public void run() {
			if (line==null) { throw new RuntimeException("ServerTick line ignored!"); }
			
			logger.log("Line received: \"" + line + "\"");
			
			// TODO Auto-generated method stub
			
		}
	};
	
	
	
	
	
	
	

}
