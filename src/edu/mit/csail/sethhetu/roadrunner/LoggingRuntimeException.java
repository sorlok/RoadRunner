//Copyright (c) 2013 Singapore-MIT Alliance for Research and Technology
//Licensed under the terms of the MIT License, as described in the file:
//   license.txt   (http://opensource.org/licenses/MIT)

package edu.mit.csail.sethhetu.roadrunner;

/**
 * Subclasses RuntimeException, logging the error message upon creation.
 *  
 * @author Seth N. Hetu
 */
public class LoggingRuntimeException extends RuntimeException {
	private static final long serialVersionUID = 1L;
	
	public LoggingRuntimeException() {
		super();
		System.out.println("LoggingRuntimeException: default constructor");
	}
	public LoggingRuntimeException(String message) {
		super(message);
		System.out.println(message);
	}
	public LoggingRuntimeException(Throwable thrwb) {
		super(thrwb);
		System.out.println("LoggingRuntimeException constructed from: " + thrwb.getMessage());
	}
	public LoggingRuntimeException(String message, Throwable thrwb) {
		super(message, thrwb);
		System.out.println(message);
	}
}
