//Copyright (c) 2013 Singapore-MIT Alliance for Research and Technology
//Licensed under the terms of the MIT License, as described in the file:
//   license.txt   (http://opensource.org/licenses/MIT)

package edu.mit.smart.sm4and.json;

import java.util.ArrayList;
import java.util.Arrays;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import edu.mit.csail.jasongao.roadrunner.Globals;
import edu.mit.csail.jasongao.roadrunner.util.LoggingRuntimeException;
import edu.mit.smart.sm4and.message.Message;
import edu.mit.smart.sm4and.message.MessageParser;

/**
 * A specific handler factory for dealing with Json-formatted messages.
 * 
 * @author Pedro Gandola
 * @author Vahid
 */
public class JsonMessageParser extends MessageParser {	
    //Convert a "MultiCast" into "MultiCastMessage.class"
    private Class<? extends Message> GetClassFromType(String msgType) {
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
	
    @Override
    public MessageBundle parse(String header, String data) {
    	//For some reason, a few Apache Mina implementations include lots of garbage between
    	// the trailing right-brace and the newline. This is obnoxious for a number of reasons,
    	// but primarily in that it breaks Gson parsing. We solve this by manually scanning
    	// for the closing brace and cutting everything after that. This *might* disable
    	// muultiple messages on the same stream, but for now that's not a problem.
    	//NOTE: This isn't necessary for v1, where each message is an exact, known size.
    	if (!Globals.SM_NEW_BUNDLE_FORMAT) {
    		data = FilterJson(data);
    	}
    	if (Globals.SM_VERBOSE_TRACE) {
    		System.out.println("in JsonMessageParser.create-> "+ data);
    	}
    	
    	//Parsing differs for v1 and v0
    	return Globals.SM_NEW_BUNDLE_FORMAT ? parse_v1(header, data) : parse_v0(header, data);
    }
    
    ///Useful for some debugging operations
    private static String print_ints(char[] data) {
    	StringBuilder sb = new StringBuilder();
    	sb.append("[");
    	for (char c : data) {
    		sb.append(((int)c));
    		sb.append(" , ");
    	}
    	sb.append("]");
    	return sb.toString();
    }
    
    private MessageBundle parse_v1(String header, String data) {
    	//For debugging v1 headers:
    	/*System.out.println("Header: " + header);
    	System.out.println("   int: " + print_ints(header.toCharArray()));
    	System.out.println("Data: " + data);
    	System.out.println(" int: " + print_ints(data.toCharArray()));*/
    	
    	//Check the static header.
    	int bundleVers = ((int)header.charAt(0)&0xFF);
    	int numMsgs = ((int)header.charAt(1)&0xFF);
    	int sendIdLen = ((int)header.charAt(2)&0xFF);
    	int destIdLen = ((int)header.charAt(3)&0xFF);
    	if (bundleVers!=1) { throw new RuntimeException("Invalid bundle version number."); }
    	
    	//Extract message lengths. Start tracking the current offset.
    	int i = 0;
    	ArrayList<Integer> msgLengths = new ArrayList<Integer>();
    	for (int n=0; n<numMsgs; n++) {
    		msgLengths.add((((int)data.charAt(i)&0xFF)<<16) | (((int)data.charAt(i+1)&0xFF)<<8) | ((int)data.charAt(i+2)&0xFF));
    		i += 3;
    	}
    	
    	//Extract send/dest IDs.
    	MessageBundle res = new MessageBundle();
    	res.sendId = data.substring(i, i+sendIdLen);
    	i += sendIdLen;
    	res.destId = data.substring(i, i+destIdLen);
    	i += destIdLen;
    	
    	//Iterate through each message and add an appropriate Message type to the result list.
    	Gson gson = new Gson();
    	for (int len : msgLengths) {
    		int firstChar = ((int)data.charAt(i))&0xFF;
    		if (firstChar == 0xBB) {
    			throw new RuntimeException("Binary message format not yet supported.");
    		} else if (firstChar == '{') {
            	//First, parse it as a generic "message" object.
    			String msg = data.substring(i, i+len); //TODO: There might be a way to avoid substring, maybe using a string reader.
System.out.println("DESERIALIZING: #" + msg + "#");
            	Message rawObject = gson.fromJson(msg, Message.class);

            	//Depending on the type, re-parse it as a sub-class.
            	Class<? extends Message> msgClass = GetClassFromType(rawObject.getMessageType());
            	Message specificObject = null;
            	try {
            		specificObject = gson.fromJson(msg, msgClass); //This line is failing for the new message type.
            	} catch (JsonSyntaxException ex) {
            		ex.printStackTrace();
            		throw new LoggingRuntimeException(ex);
            	}
            	res.messages.add(specificObject);
    		} else {
    			throw new RuntimeException("Could not determine message format.");
    		}
    		
    		//Increment
    		i += len;
    	}
    	
    	return res;
    }
    
    
    
    private MessageBundle parse_v0(String header, String data) {
        //Parse the message into a generic Json object.
        JsonParser parser = new JsonParser();
        JsonObject root = (JsonObject)parser.parse(data);
                
        //Ensure we have a few mandatory properties.
        JsonObject head = root.getAsJsonObject("header");
        JsonArray messages = root.getAsJsonArray("messages");
        if (head==null) { throw new LoggingRuntimeException("Packet arrived with no header."); }
        if (messages==null) { throw new LoggingRuntimeException("Packet arrived with no messages."); }
                
    	//Set sender/dest properties.
    	if (!(head.has("send_client") && head.has("dest_client"))) { throw new LoggingRuntimeException("Header missing fields."); }
    	MessageBundle res = new MessageBundle();
    	res.sendId = head.get("send_client").getAsString();
    	res.destId = head.get("dest_client").getAsString();

    	//Iterate through each DATA element and add an appropriate Message type to the result list.
    	Gson gson = new Gson();
        for (JsonElement msg : messages) {
        	//First, parse it as a generic "message" object.
        	Message rawObject = gson.fromJson(msg, Message.class);

        	//Depending on the type, re-parse it as a sub-class.
        	Class<? extends Message> msgClass = GetClassFromType(rawObject.getMessageType());
        	Message specificObject = null;
        	try {
        		specificObject = gson.fromJson(msg, msgClass); //This line is failing for the new message type.
        	} catch (JsonSyntaxException ex) {
        		ex.printStackTrace();
        		throw new LoggingRuntimeException(ex);
        	}
        	res.messages.add(specificObject);
        }
        
        //Done
        return res;
    }
    
    @Override
    public String[] serialize(MessageBundle bundle) {
    	String[] res = new String[]{"",""};
    	
    	//Create the data section first.
    	if (Globals.SM_NEW_BUNDLE_FORMAT) {
    		StringBuilder vary = new StringBuilder();
    		StringBuilder msgs = new StringBuilder();
    		byte[] msgLen = new byte[3];
    		
        	Gson gson = new Gson();
        	int offset = 0;
        	for (Message msg : bundle.messages) {
        		String msgStr = gson.toJsonTree(msg).toString();
        		msgLen[0] = (byte)((msgStr.length()>>16)&0xFF);
        		msgLen[1] = (byte)((msgStr.length()>>8)&0xFF);
        		msgLen[2] = (byte)(msgStr.length()&0xFF);
        		vary.append(new String(msgLen));
        		msgs.append(msgStr);
        	}
        	vary.append(bundle.sendId);
        	vary.append(bundle.destId);
        	res[1] = vary.toString() + msgs.toString();
    	} else {
        	JsonObject packet = new JsonObject();
        	serializeHeader(packet, bundle.sendId, bundle.destId);
        	serializeData(packet, bundle.messages);
        	res[1] = packet.toString();
    	}
    	
    	//Now, create an appropriately-formatted header.
    	if (Globals.SM_NEW_BUNDLE_FORMAT) {
    		byte[] byteRep = new byte[8];
    		byteRep[0] = 1;
    		byteRep[1] = (byte)bundle.messages.size();
    		byteRep[2] = (byte)bundle.sendId.length();
    		byteRep[3] = (byte)bundle.destId.length();
    		byteRep[4] = (byte)((res[1].length()>>24)&0xFF);
    		byteRep[5] = (byte)((res[1].length()>>16)&0xFF);
    		byteRep[6] = (byte)((res[1].length()>>8)&0xFF);
    		byteRep[7] = (byte)(res[1].length()&0xFF);
    		res[0] = new String(byteRep);
    	} else {
    		res[0] = String.format("%8h", res[1].length()+1);
    	}
    	
    	return res;
    }
    
    private void serializeHeader(JsonObject res, String sendId, String destId) {
        JsonObject header = new JsonObject();
        header.addProperty("send_client", sendId);
        header.addProperty("dest_client", destId);
        res.add("header", header);
    }
    
    private void serializeData(JsonObject res, ArrayList<Message> messages) {
    	Gson gson = new Gson();
    	JsonArray data = new JsonArray();
    	for (Message msg : messages) {
    		data.add(gson.toJsonTree(msg));
    	}
        res.add("messages", data);
    }
    
    
    
    
    
}
