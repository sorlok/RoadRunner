/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sg.smart.mit.simmobility4android.handler;
import com.google.gson.JsonObject;
import sg.smart.mit.simmobility4android.connector.Connector;
import sg.smart.mit.simmobility4android.message.ReadyMessage;

/**
 *
 * @author vahid
 */
public class ReadyHandler extends Handler {
    public ReadyHandler(ReadyMessage message, Connector connector) {
        super(message, connector);
    }
    
    @Override
    public void handle() {
        /*Seth, if you want your emulator set any flag to make sure 
         * it is successfully registered with the simMobility server(Broker)
         * here is your chance
         */
        System.out.println("Server Knows you. You can send and receive now.");
    }
    
}
