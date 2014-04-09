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
public class MessageParser {
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

    //Convert a "MultiCast" into "MultiCastMessage.class"
    public Class<? extends Message> GetClassFromType(String msgType) {
    	//Sanity check; the switch will explode otherwise.
    	if (msgType==null) {
    		throw new LoggingRuntimeException("Message.GetClassFromType() - Can't switch on a null Message type.");
    	}
    	
    	//Return the registered type.
    	if (messageTypes.containsKey(msgType)) {
    		return messageTypes.get(msgType);
    	} else {
   			throw new LoggingRuntimeException("JsonMessageParser.GetClassFromType() - Unknown message type: " + msgType.toString());
    	}
    }
    
	public static String FilterJson(String src) {
    	final String msg = src;
        int lastBracket = -1;
        int numLeft = 0;
        for (int i=0; i<msg.length(); i++) {
        	if (msg.charAt(i)=='{') {
        		numLeft++;
        	} else if (msg.charAt(i)=='}') {
        		numLeft--;
        		if (numLeft==0) {
        			lastBracket = i;
        			break; 
        		}
        	}
        }
        if (numLeft!=0 || lastBracket==-1) { 
        	throw new LoggingRuntimeException("Bad json-formatted message string; left and right bracket counts don't add up."); 
        }
        return msg.substring(0, lastBracket+1);
	}
	
	
	/**
	 * Turns a String into an array of Messages, which is independent of encoding.
	 * @param header The 8-byte fixed-size header.
	 * @param messages The varying-length header + messages.
	 * @return The Message it corresponds to.
	 */
	//public abstract MessageBundle parse(String header, String messages);
	
	/**
	 * Serializes a message into a String format familiar to the server.
	 * @param msg The message to serialize.
	 * @return A string array of length 2 (always), with res[0] being the fixed-length header and res[1] being the data section. 
	 */
	//public abstract String[] serialize(MessageBundle messages);
}
