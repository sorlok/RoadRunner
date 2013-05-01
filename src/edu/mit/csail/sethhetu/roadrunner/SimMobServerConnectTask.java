package edu.mit.csail.sethhetu.roadrunner;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;

import android.os.AsyncTask;
import edu.mit.csail.jasongao.roadrunner.Globals;

//Helper class for connecting to the Sim Mobility server.
public class SimMobServerConnectTask extends AsyncTask<Socket, Void, Boolean> {
	//Callback for task completion
	public interface PostExecuteAction {
		public void onPostExecute(Exception thrownException, BufferedReader reader, BufferedWriter writer);
	}
	
	private BufferedReader reader;
	private BufferedWriter writer;
	private boolean error;
	private Exception errorEx;
	private PostExecuteAction onComplete;

	
	public SimMobServerConnectTask(PostExecuteAction onComplete) {
		this.onComplete = onComplete;
	}
	
	boolean isError() { return error; }
	
	protected void onPreExecute() {
		super.onPreExecute();
		this.error = false;
	}
	
	protected Boolean doInBackground(Socket... smSocket) {
		if (smSocket.length!=1) { throw new RuntimeException("Only one socket allowed."); }
		try {
			smSocket[0].connect(new InetSocketAddress(Globals.SM_HOST, Globals.SM_PORT), Globals.SM_TIMEOUT);

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
			}
		} catch (IOException ex) {
			this.error = true;
			this.errorEx = ex;
		}
		
		return !this.error;
	}

	protected void onProgressUpdate(Void unused) {}
	
	protected void onPostExecute(Boolean success) {
		if (onComplete!=null) {
			onComplete.onPostExecute(errorEx, reader, writer);
		}
	}
}
