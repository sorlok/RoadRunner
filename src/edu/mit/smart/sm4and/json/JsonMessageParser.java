//Copyright (c) 2013 Singapore-MIT Alliance for Research and Technology
//Licensed under the terms of the MIT License, as described in the file:
//   license.txt   (http://opensource.org/licenses/MIT)

package edu.mit.smart.sm4and.json;

import java.util.ArrayList;

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
    public MessageBundle parse(String src) {
    	//For some reason, a few Apache Mina implementations include lots of garbage between
    	// the trailing right-brace and the newline. This is obnoxious for a number of reasons,
    	// but primarily in that it breaks Gson parsing. We solve this by manually scanning
    	// for the closing brace and cutting everything after that. This *might* disable
    	// muultiple messages on the same stream, but for now that's not a problem.
    	src = FilterJson(src);
    	if (Globals.SM_VERBOSE_TRACE) {
    		System.out.println("in JsonMessageParser.create-> "+ src);
    	}
    	
        //Parse the message into a generic Json object.
        JsonParser parser = new JsonParser();
        JsonObject root = (JsonObject)parser.parse(src);
                
        //Ensure we have a few mandatory properties.
        JsonObject head = root.getAsJsonObject("header");
        JsonArray data = root.getAsJsonArray("messages");
        if (head==null) { throw new LoggingRuntimeException("Packet arrived with no header."); }
        if (data==null) { throw new LoggingRuntimeException("Packet arrived with no messages."); }
                
        //Dispatch parsing.
        MessageBundle res = parseCustomMessageFormat(head, data);
        return res;
    }
    
    private MessageBundle parseCustomMessageFormat(JsonObject head, JsonArray data) {
    	//Set sender/dest properties.
    	if (!(head.has("send_client") && head.has("dest_client"))) { throw new LoggingRuntimeException("Header missing fields."); }
    	MessageBundle res = new MessageBundle();
    	res.sendId = head.get("send_client").getAsString();
    	res.destId = head.get("dest_client").getAsString();

    	//Iterate through each DATA element and add an appropriate Message type to the result list.
    	Gson gson = new Gson();
        for (JsonElement msg : data) {
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
    public String serialize(MessageBundle bundle) {
    	//Do it manually:
    	JsonObject packet = new JsonObject();
    	serializeHeader(packet, bundle.sendId, bundle.destId);
    	serializeData(packet, bundle.messages);
    	return packet.toString();
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
