/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sg.smart.mit.simmobility4android.handler;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import sg.smart.mit.simmobility4android.connector.Connector;
import sg.smart.mit.simmobility4android.message.*;

/**
 *
 * @author gandola, vahid
 */
public class JsonHandlerFactory implements HandlerFactory {

    @Override
    public Handler create(Connector connector, Object message) {
        System.out.println("in JsonHandlerFactory.create-> "+ message.toString());
        Gson gson = new Gson();
        Message result = gson.fromJson(message.toString(), Message.class);
        switch (result.getMessageType()) {
            
            case WhoAreYou: {
                return new WhoAreYouHandler(null, connector);
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
                return new LocationHandler(res, connector);
            }
                
                
            default:
                return null;
        }
    }
}
