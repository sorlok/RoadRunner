//Copyright (c) 2013 Singapore-MIT Alliance for Research and Technology
//Licensed under the terms of the MIT License, as described in the file:
//   license.txt   (http://opensource.org/licenses/MIT)

package edu.mit.csail.sethhetu.roadrunner;

import java.io.BufferedWriter;
import java.io.IOException;
import android.os.AsyncTask;

/**
 * Helper class for managing Sim Mobility time ticks (response from the emulator)
 * This is a one-shot Task, unlike TickRequest.
 *  
 * @author Seth N. Hetu
 */
public class SimMobTickResponse extends AsyncTask<BufferedWriter, Void, Boolean> {	
	private String line;
	
	public SimMobTickResponse(String line) {
		this.line = line;
	}
	
	protected Boolean doInBackground(BufferedWriter... writer) {
		if (writer.length!=1) { throw new LoggingRuntimeException("Reader must be 1 line."); }
		if (this.line==null) { throw new LoggingRuntimeException("Line is null."); }
		
		//Make sure our line ends with a newline.
		if (line.isEmpty() || (line.charAt(line.length()-1)!='\n')) {
			line += "\n";
		}
				
		//NOTE: This *shouldn't* get ahead of itself, since the server won't send any new
		//      data until the Android device responds. We could modify this if required to 
		//      only wait for data once a response has been set, but that's probably not necessary.
		try {
			writer[0].write(line);
			writer[0].flush();
		} catch (IOException ex) {
			return false;
		}
		
		return true;
	}

	protected void onProgressUpdate(Void unused) {}
	
	protected void onPostExecute(Boolean ok) {
		if (!ok) { throw new LoggingRuntimeException("Writeline failed."); }
	}
}
