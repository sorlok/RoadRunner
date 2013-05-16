//Copyright (c) 2013 Singapore-MIT Alliance for Research and Technology
//Licensed under the terms of the MIT License, as described in the file:
//   license.txt   (http://opensource.org/licenses/MIT)

package edu.mit.smart.sm4and;

import edu.mit.smart.sm4and.message.Message;


/**
 * Interface for converting between Messages and Strings.
 * 
 * NOTE: Ideally, we would separate parsing and serializing, so that you could send 
 *       a different format to the server than the server replies with. In practice, 
 *       this is not really necessary, so we just lump both functionalities together. 
 * 
 * @author Pedro Gandola
 * @author Vahid
 */
public interface MessageParser {
	/**
	 * Turns a String into a Message, which is independent of encoding.
	 * @param src The input String.
	 * @return The Message it corresponds to.
	 */
	Message parse(String src);
	
	/**
	 * Serializes a message into a String format familiar to the server.
	 * @param msg The message to serialize.
	 * @return A string representation of that message.
	 */
	String serialize(Message msg);
}
