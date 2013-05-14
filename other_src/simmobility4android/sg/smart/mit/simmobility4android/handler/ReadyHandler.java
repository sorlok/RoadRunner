//Copyright (c) 2013 Singapore-MIT Alliance for Research and Technology
//Licensed under the terms of the MIT License, as described in the file:
//   license.txt   (http://opensource.org/licenses/MIT)

package sg.smart.mit.simmobility4android.handler;

import sg.smart.mit.simmobility4android.connector.Connector;
import sg.smart.mit.simmobility4android.message.ReadyMessage;

/**
 * @author Vahid
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
