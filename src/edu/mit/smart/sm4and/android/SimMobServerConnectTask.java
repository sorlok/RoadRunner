//Copyright (c) 2013 Singapore-MIT Alliance for Research and Technology
//Licensed under the terms of the MIT License, as described in the file:
//   license.txt   (http://opensource.org/licenses/MIT)

package edu.mit.smart.sm4and.android;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.UnknownHostException;

import android.os.AsyncTask;

import edu.mit.csail.jasongao.roadrunner.Globals;
import edu.mit.csail.jasongao.roadrunner.util.LoggerI;
import edu.mit.csail.jasongao.roadrunner.util.LoggingRuntimeException;
import edu.mit.smart.sm4and.connector.Connector;
import edu.mit.smart.sm4and.handler.MessageHandlerFactory;
import edu.mit.smart.sm4and.message.MessageParser;

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
	
    //private MessageHandlerFactory handlerFactory;
	private BufferedReader reader;
	private BufferedWriter writer;
	private Exception errorEx;
	private PostExecuteAction onComplete;
	private LoggerI logger;
	MessageParser parser;

	
	public SimMobServerConnectTask(PostExecuteAction onComplete, MessageHandlerFactory handlerFactory, MessageParser parser,  LoggerI logger) {
		this.onComplete = onComplete;
		this.logger = logger;
		this.parser = parser;
	}
	
	protected void onPreExecute() {
	}
	
	protected Boolean doInBackground(Connector... mnConnect) {
		if (mnConnect.length!=1) { throw new LoggingRuntimeException("Only one Connector allowed."); }
		try {
			if (!Globals.SM_RERUN_FULL_TRACE) {
				//Try to detect "AUTO" hosts.
				String host = Globals.SM_HOST.equals("AUTO") ? AutoDetectRelay() : Globals.SM_HOST;
				
				//Did we find anything?
				if (host.isEmpty()) {
					throw new LoggingRuntimeException("Could not auto-detect host (or empty host in Globals).");
				}
				
				//Connect
				logger.log("Attempting to connect to MINA server on " + host + ":" + Globals.SM_PORT);
				mnConnect[0].connect(parser, host, Globals.SM_PORT);
			}
		} catch (Exception ex) {
			logger.log(ex.toString());
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
	
	protected final String AutoDetectRelay() {
		String res = "";
		try {
			Process process = new ProcessBuilder()
				.command("getprop", "dhcp.eth0.gateway")
				.redirectErrorStream(true)
				.start();
			
			//Need to catch its output.
			try {
				@SuppressWarnings("unused")
				OutputStream out = process.getOutputStream();
				
				InputStream in = process.getInputStream();
				BufferedReader br = new BufferedReader(new InputStreamReader(in));
				
				//The first line contains our result.
				String line;
				while ((line = br.readLine()) != null) {
					if (res.isEmpty()) {
						res = line.trim();
					}
				}
			} catch (Exception ex) {
				logger.log("Can't start address auto-detection Process: EXCEPTION(" + ex.getClass().getName() + "): " + ex.toString());
			} finally {
				process.destroy();
			}			
		} catch (Exception ex) {
			logger.log("Can't auto-detect relay address: EXCEPTION(" + ex.getClass().getName() + "): " + ex.toString());
		}
		return res;
	}
}
