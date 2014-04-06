//Copyright (c) 2013 Singapore-MIT Alliance for Research and Technology
//Licensed under the terms of the MIT License, as described in the file:
//   license.txt   (http://opensource.org/licenses/MIT)

package edu.mit.smart.sm4and.message;

import java.util.ArrayList;
import java.util.Hashtable;

import edu.mit.csail.jasongao.roadrunner.util.LoggingRuntimeException;


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
public abstract class MessageParser {
	//Represents an entire message set, plus header information.
	public static class MessageBundle {
		public String sendId = "";
		public String destId = "";
		public ArrayList<Message> messages = new ArrayList<Message>();
	}
	
	//Types of Messages we expect to parse.
	protected Hashtable<String, Class<? extends Message>> messageTypes = new Hashtable<String, Class<? extends Message>>();

	/**
	 * Adds a message type to the list of lookups. Throws an exception if that type already has a class associated with it.
	 * @param msgType The message type, as a unique string.
	 * @param msgClass The class representing this Message Type.
	 */
	public void addMessagetype(String msgType, Class<? extends Message> msgClass) {
		if (messageTypes.containsKey(msgType)) {
			throw new LoggingRuntimeException("addMessageType() trying to add a class which already exists: " + msgType);
		}
		messageTypes.put(msgType, msgClass);
	}

	
	/**
	 * Turns a String into an array of Messages, which is independent of encoding.
	 * @param src The input String.
	 * @return The Message it corresponds to.
	 */
	public abstract MessageBundle parse(String src);
	
	/**
	 * Serializes a message into a String format familiar to the server.
	 * @param msg The message to serialize.
	 * @return A string representation of that message.
	 */
	public abstract String serialize(MessageBundle messages);
}
