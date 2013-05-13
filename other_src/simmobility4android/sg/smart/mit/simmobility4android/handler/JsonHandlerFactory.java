/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sg.smart.mit.simmobility4android.handler;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import edu.mit.csail.jasongao.roadrunner.RoadRunnerService.LocationSpoofer;
import sg.smart.mit.simmobility4android.connector.Connector;
import sg.smart.mit.simmobility4android.message.*;

/**
 *
 * @author gandola, vahid
 */
public class JsonHandlerFactory implements HandlerFactory {
	public LocationSpoofer locspoof;
	
	public JsonHandlerFactory(LocationSpoofer locspoof) {
		this.locspoof = locspoof;
	}

    @Override
    public Handler create(Connector connector, Object message, int clientID) {
        String msg = (String)message;
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
        if (lastBracket==-1) { throw new RuntimeException("BAD"); }
        msg = msg.substring(0, lastBracket+1);
        message = msg;
        
    	System.out.println("Message type: " + message.getClass().getCanonicalName());
        System.out.println("in JsonHandlerFactory.create->  **"+ message.toString() + "**");
        System.out.println("TEST: " + ((String)message).length());
        
        Gson gson = new Gson();
        Message result = gson.fromJson(message.toString(), Message.class);
        switch (result.getMessageType()) {
            
            case WhoAreYou: {
                return new WhoAreYouHandler(null, connector, clientID);
            }
                
            case TimeData: {
                JsonParser parser = new JsonParser();
                JsonObject jo = (JsonObject) parser.parse(message.toString());
                JsonObject jo1 = jo.getAsJsonObject("TimeData");
                TimeMessage res = gson.fromJson(jo1.toString(), TimeMessage.class);
                return new TimeHandler(res, connector);
            }
                
            case Ready:
                return new ReadyHandler(null, connector);
                
            case LocationData:{
                JsonParser parser = new JsonParser();
                JsonObject jo = (JsonObject) parser.parse(message.toString());
                JsonObject jo1 = jo.getAsJsonObject("LocationData");
                LocationMessage res = gson.fromJson(jo1.toString(), LocationMessage.class);
                return new LocationHandler(locspoof, res, connector);
            }
                
                
            default:
                return null;
        }
    }
}
