//Copyright (c) 2013 Singapore-MIT Alliance for Research and Technology
//Licensed under the terms of the MIT License, as described in the file:
//   license.txt   (http://opensource.org/licenses/MIT)

package edu.mit.smart.sm4and;

import edu.mit.smart.sm4and.message.Message;


/**
 * Interface for parsing Messages. Turns a String into a Message, which is independent 
 *   of encoding.
 * 
 * @author Pedro Gandola
 * @author Vahid
 */
public interface MessageParser {
	Message parse(String src);
	String serialize(Message msg);
}
