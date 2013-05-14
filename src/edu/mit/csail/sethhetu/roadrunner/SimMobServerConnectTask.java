//Copyright (c) 2013 Singapore-MIT Alliance for Research and Technology
//Licensed under the terms of the MIT License, as described in the file:
//   license.txt   (http://opensource.org/licenses/MIT)

package edu.mit.csail.sethhetu.roadrunner;

import java.io.BufferedReader;
import java.io.BufferedWriter;

import sg.smart.mit.simmobility4android.connector.Connector;
import sg.smart.mit.simmobility4android.connector.MinaConnector;
import sg.smart.mit.simmobility4android.handler.HandlerFactory;
import sg.smart.mit.simmobility4android.listener.MessageListener;

import android.os.AsyncTask;
import edu.mit.csail.jasongao.roadrunner.Globals;

/**
 * Helper class for connecting to the Sim Mobility server.
 * 
 * @author Seth N. Hetu
 */
public class SimMobServerConnectTask extends AsyncTask<Connector, Void, Boolean> {
	//Callback for task completion
	public interface PostExecuteAction {
		public void onPostExecute(Exception thrownException, BufferedReader reader, BufferedWriter writer);
	}
	
    private HandlerFactory handlerFactory;
	private BufferedReader reader;
	private BufferedWriter writer;
	private Exception errorEx;
	private PostExecuteAction onComplete;

	
	public SimMobServerConnectTask(PostExecuteAction onComplete, HandlerFactory handlerFactory) {
		this.onComplete = onComplete;
		this.handlerFactory = handlerFactory;
	}
	
	protected void onPreExecute() {
		super.onPreExecute();
	}
	
	protected Boolean doInBackground(Connector... mnConnect) {
		if (mnConnect.length!=1) { throw new RuntimeException("Only one Connector allowed."); }
		try {
	        System.out.println("client " + ((MinaConnector)mnConnect[0]).getClientID() + " connecting...");
	        mnConnect[0].connect(Globals.SM_HOST, Globals.SM_PORT);
			
			/*mnConnect[0].connect(new InetSocketAddress(Globals.SM_HOST, Globals.SM_PORT), Globals.SM_TIMEOUT);

			//Retrieve underlying input/output streams.
			InputStream in = smSocket[0].getInputStream();
			OutputStream out = smSocket[0].getOutputStream();

			//Wrap these with Buffered readers/writers.
			this.reader = new BufferedReader(new InputStreamReader(in));
			this.writer = new BufferedWriter(new OutputStreamWriter(out));
			
			//Now we read one line, and see if the Sim Mobility server will accept us.
			String firstResponse = reader.readLine();
			if (!firstResponse.equals("OK")) {
				throw new IOException("Sim Mobility Server refused to accept us; received \"" + firstResponse + "\"");
			}*/
		} catch (Exception ex) {
			this.errorEx = ex;
		}
		
		return this.errorEx!=null;
	}

	protected void onProgressUpdate(Void unused) {}
	
	protected void onPostExecute(Boolean success) {
		if (onComplete!=null) {
			onComplete.onPostExecute(errorEx, reader, writer);
		}
	}
}
