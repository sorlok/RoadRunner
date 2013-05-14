/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sg.smart.mit.simmobility4android.handler;
import edu.mit.csail.jasongao.roadrunner.RoadRunnerService.LocationSpoofer;
import sg.smart.mit.simmobility4android.connector.Connector;
import sg.smart.mit.simmobility4android.message.LocationMessage;
/**
 *
 * @author vahid
 */
public class LocationHandler extends Handler {
    private LocationSpoofer locspoof;
    
    public LocationHandler(LocationSpoofer locspoof, LocationMessage message, Connector connector) {
        super(message, connector);
        this.locspoof = locspoof;
    }

    @Override
    public void handle() {
        /*
         * TODO: This currently uses X/Y coordinates. We need to reverse project back to lat/lng
         *       (but randomized networks need a fallback, as there is no projection matrix). 
         */  
        LocationMessage message = (LocationMessage)getMessage();
        System.out.println("Current location is "+ message.getX() + ":" + message.getY());
        locspoof.setLocation(message.getX(), message.getY());
    }
    
}
