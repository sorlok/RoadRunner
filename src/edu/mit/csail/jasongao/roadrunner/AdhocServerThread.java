package edu.mit.csail.jasongao.roadrunner;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import android.os.Handler;
import android.util.Log;

public class AdhocServerThread extends Thread {
	private static final String TAG = "AdhocServerThread";

	@SuppressWarnings("unused")
	private Handler parentHandler;
	
	private RoadRunnerService rrs;

	private ServerSocket mySocket;
	private boolean socketOK = false;
	int count = 0;

	/** AdhocServerThread constructor */
	public AdhocServerThread(Handler p, RoadRunnerService rrs_) {
		this.parentHandler = p;
		this.rrs = rrs_;

		/** Create a socket and set it up for receiving broadcasts. */
		try {
			mySocket = new ServerSocket(Globals.CLOUD_PORT);
			socketOK = true;
		} catch (Exception e) {
			Log.i(TAG, "Cannot open socket: " + e.getMessage());
			return;
		}
	}

	/** If not socketOK, then receive loop thread will stop */
	public synchronized boolean socketIsOK() {
		return socketOK;
	}

	/** Close the socket before exiting the application */
	public synchronized void close() {
		if (mySocket != null && !mySocket.isClosed())
			try {
				mySocket.close();
				socketOK = false;
			} catch (IOException e) {
				e.printStackTrace();
			}
	}

	/** Thread's run loop */
	@Override
	public void run() {
		ServerSocket s = mySocket;

		try {
			while (socketOK) {
				Socket connection = s.accept();
				Runnable runnable = new AdhocServerConnection(connection,
						++count, rrs);
				Thread thread = new Thread(runnable);
				thread.start();
			}
		} catch (IOException e) {
			Log.e(TAG, "Exception on Socket.accept() " + e.getMessage());
			socketOK = false;
		}

		rrs.log("AdhocServerThread exiting.");
	} // end run()
}