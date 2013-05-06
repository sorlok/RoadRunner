/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sg.smart.mit.simmobility4android.handler;
import com.google.gson.JsonObject;
import sg.smart.mit.simmobility4android.connector.Connector;
import sg.smart.mit.simmobility4android.message.WhoAreYouMessage;

/**
 *
 * @author gandola, vahid
 */
public class WhoAreYouHandler extends Handler<WhoAreYouMessage> {

    public WhoAreYouHandler(WhoAreYouMessage message, Connector connector) {
        super(message, connector);
        System.out.println("creating WhoAreYouHandler");
    }

    @Override
    public void handle() {
        System.out.println("WhoAreYouHandler is handling");
        JsonObject obj = new JsonObject();
        /*
         * seth please add your emulator's ID here. 
         * Note that it will be received as unsidned int 
         * on the other side
         */
        obj.addProperty("ID", 116);
        getConnector().send(obj.toString());
    }
}
