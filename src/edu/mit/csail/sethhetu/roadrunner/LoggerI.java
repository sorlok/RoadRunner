//Copyright (c) 2013 Singapore-MIT Alliance for Research and Technology
//Licensed under the terms of the MIT License, as described in the file:
//   license.txt   (http://opensource.org/licenses/MIT)

package edu.mit.csail.sethhetu.roadrunner;

/**
 * Helper: encapsulate logging into a class.
 *  
 * @author Seth N. Hetu
 */
public interface LoggerI  {
	/**
	 * Log to the system logger AND the screen.
	 * @param message The message to log.
	 */
	public void log(String message);
	
	/**
	 * Log to the system logger but NOT the screen.
	 * @param message The message to log.
	 */
	public void log_nodisplay(String message);
}
