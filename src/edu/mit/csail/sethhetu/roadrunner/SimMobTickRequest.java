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
import android.os.Handler;
import edu.mit.csail.jasongao.roadrunner.Globals;
import edu.mit.csail.sethhetu.roadrunner.SimMobilityBroker.ServerTickDoneRunnable;

//Helper class for managing Sim Mobility time ticks (request from the server)
public class SimMobTickRequest extends AsyncTask<BufferedReader, Void, Boolean> {
	private Handler handler;
	private ServerTickDoneRunnable onComplete;
	
	public SimMobTickRequest(Handler handler, ServerTickDoneRunnable onComplete) {
		this.handler =  handler;
		this.onComplete = onComplete;
	}
	
	protected Boolean doInBackground(BufferedReader... reader) {
		if (reader.length!=1) { throw new RuntimeException("Reader must be 1 line."); }
		if (this.handler==null || this.onComplete==null) { throw new RuntimeException("Handler/Complete are null."); }
				
		//NOTE: This *shouldn't* get ahead of itself, since the server won't send any new
		//      data until the Android device responds. We could modify this if required to 
		//      only wait for data once a response has been set, but that's probably not necessary.
		boolean active = true;
		while (active) {
			try {
				String line = reader[0].readLine();
				this.onComplete.setLine(line);
			} catch (IOException ex) {
				return false;
			}
			
			//Post back.
			handler.post(onComplete);
		}
		
		return true;
	}

	protected void onProgressUpdate(Void unused) {}
	
	protected void onPostExecute(Boolean ok) {
		if (!ok) { throw new RuntimeException("Readline failed."); }
	}
}
