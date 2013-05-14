//Copyright (c) 2013 Singapore-MIT Alliance for Research and Technology
//Licensed under the terms of the MIT License, as described in the file:
//   license.txt   (http://opensource.org/licenses/MIT)

package edu.mit.smart.sm4and.handler;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import edu.mit.csail.jasongao.roadrunner.RoadRunnerService.LocationSpoofer;
import edu.mit.smart.sm4and.connector.Connector;
import edu.mit.smart.sm4and.message.*;

/**
 * @author Pedro Gandola
 * @author Vahid
 */
public class JsonHandlerFactory implements HandlerFactory {
	public LocationSpoofer locspoof;
	
	public JsonHandlerFactory(LocationSpoofer locspoof) {
		this.locspoof = locspoof;
	}

    @Override
    public Handler create(Connector connector, String message, int clientID) {
    	//For some reason, a few Apache Mina implementations include lots of garbage between
    	// the trailing right-brace and the newline. This is obnoxious for a number of reasons,
    	// but primarily in that it breaks Gson parsing. We solve this by manually scanning
    	// for the closing brace and cutting everything after that. This *might* disable
    	// muultiple messages on the same stream, but for now that's not a problem.
    	final String msg = message;
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
        message = msg.substring(0, lastBracket+1);
        System.out.println("in JsonHandlerFactory.create-> "+ message);

        //Parse and respond to the message.
        Gson gson = new Gson();
        Message result = gson.fromJson(message, Message.class);
        switch (result.getMessageType()) {
            
            case WhoAreYou: {
                return new WhoAreYouHandler(null, connector, clientID);
            }
                
            case TimeData: {
                JsonParser parser = new JsonParser();
                JsonObject jo = (JsonObject) parser.parse(message);
                JsonObject jo1 = jo.getAsJsonObject("TimeData");
                TimeMessage res = gson.fromJson(jo1.toString(), TimeMessage.class);
                return new TimeHandler(res, connector);
            }
                
            case Ready:
                return new ReadyHandler(null, connector);
                
            case LocationData:{
                JsonParser parser = new JsonParser();
                JsonObject jo = (JsonObject) parser.parse(message);
                JsonObject jo1 = jo.getAsJsonObject("LocationData");
                LocationMessage res = gson.fromJson(jo1.toString(), LocationMessage.class);
                return new LocationHandler(locspoof, res, connector);
            }
                
                
            default:
                return null;
        }
    }
}
