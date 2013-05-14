//Copyright (c) 2013 Singapore-MIT Alliance for Research and Technology
//Licensed under the terms of the MIT License, as described in the file:
//   license.txt   (http://opensource.org/licenses/MIT)

package edu.mit.smart.sm4and.json.message;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import edu.mit.smart.sm4and.MessageParser;
import edu.mit.smart.sm4and.handler.WhoAreYouHandler.WhoAreYouMessage;
import edu.mit.smart.sm4and.handler.LocationHandler.LocationMessage;
import edu.mit.smart.sm4and.handler.TimeHandler.TimeMessage;
import edu.mit.smart.sm4and.handler.ReadyHandler.ReadyMessage;
import edu.mit.smart.sm4and.message.Message;

/**
 * A specific handler factory for dealing with Json-formatted messages.
 * 
 * @author Pedro Gandola
 * @author Vahid
 */
public class JsonMessageParser implements MessageParser {
	private static String FilterJson(String src) {
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
        if (lastBracket!=0) { 
        	throw new RuntimeException("Bad json-formatted message string; left and right bracket counts don't add up."); 
        }
        return msg.substring(0, lastBracket+1);
	}
	
    @Override
    public Message parse(String src) {
    	//For some reason, a few Apache Mina implementations include lots of garbage between
    	// the trailing right-brace and the newline. This is obnoxious for a number of reasons,
    	// but primarily in that it breaks Gson parsing. We solve this by manually scanning
    	// for the closing brace and cutting everything after that. This *might* disable
    	// muultiple messages on the same stream, but for now that's not a problem.
    	src = FilterJson(src);
        System.out.println("in JsonHandlerFactory.create-> "+ src);

        //Parse the message into one of our Message subclasses.
        Gson gson = new Gson();
        Message rawObject = gson.fromJson(src, Message.class);
        switch (rawObject.getMessageType()) {
            case WhoAreYou: {
                return new WhoAreYouMessage();
            }
                
            case TimeData: {
                JsonParser parser = new JsonParser();
                JsonObject jo = (JsonObject) parser.parse(src);
                JsonObject jo1 = jo.getAsJsonObject("TimeData");
                return gson.fromJson(jo1.toString(), TimeMessage.class);
            }
                
            case Ready:
                return new ReadyMessage();
                
            case LocationData:{
                JsonParser parser = new JsonParser();
                JsonObject jo = (JsonObject) parser.parse(src);
                JsonObject jo1 = jo.getAsJsonObject("LocationData");
                return gson.fromJson(jo1.toString(), LocationMessage.class);
            }
                
            default: {
                return null;
            }
        }
    }
}
