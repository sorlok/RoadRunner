//Copyright (c) 2013 Singapore-MIT Alliance for Research and Technology
//Licensed under the terms of the MIT License, as described in the file:
//   license.txt   (http://opensource.org/licenses/MIT)

package edu.mit.smart.sm4and.android;

import java.io.BufferedReader;
import java.io.BufferedWriter;

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
			logger.log("Attempting to connect to MINA server on " + Globals.SM_HOST + ":" + Globals.SM_PORT);
			if (!Globals.SM_RERUN_FULL_TRACE) {
				mnConnect[0].connect(parser, Globals.SM_HOST, Globals.SM_PORT);
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
}
