//Copyright (c) 2013 Singapore-MIT Alliance for Research and Technology
//Licensed under the terms of the MIT License, as described in the file:
//   license.txt   (http://opensource.org/licenses/MIT)

package edu.mit.smart.sm4and.handler;

import edu.mit.csail.jasongao.roadrunner.RoadRunnerService.PathSetter;
import edu.mit.csail.sethhetu.roadrunner.SimpleRegion;
import edu.mit.csail.sethhetu.roadrunner.SimMobilityBroker.RegionSetter;
import edu.mit.smart.sm4and.Connector;
import edu.mit.smart.sm4and.AbstractMessageHandler;
import edu.mit.smart.sm4and.MessageParser;
import edu.mit.smart.sm4and.message.Message;


/**
 * Handles a "Send me Regions" request from the client to the server and back.
 * @author Seth N. Hetu
 */
public class SendRegionHandler extends AbstractMessageHandler {
	/** A message from the client requesting that the server send it the list of Regions. */
	/*public static class SendRegionRequest extends Message {
		public SendRegionRequest() { this.MESSAGE_TYPE = Type.SEND_REGIONS; }
	}*/
	
	/** A response from the server with an attached list of Regions. */
	public static class SendRegionResponse extends Message {
		public SendRegionResponse() { this.MESSAGE_TYPE = Type.REGIONS_AND_PATH_DATA; }
	    public SimpleRegion[] all_regions;
	    public String[] region_path;
	}
	
	//Simple remote-Log message. (TODO: put into its own file).
	public static class RemoteLogMessage extends Message {
		public RemoteLogMessage() { this.MESSAGE_TYPE = Type.REMOTE_LOG; }
	    public String log_message;
	}
	
	private RegionSetter regionSetter;
	private PathSetter pathSetter;
	private String clientID;
	    
    public SendRegionHandler(RegionSetter regionSetter, PathSetter pathSetter, String clientID) {
        System.out.println("creating SendRegionHandler");
        this.regionSetter = regionSetter;
        this.pathSetter = pathSetter;
        this.clientID = clientID;
    }

    @Override
    public void handle(Message message, Connector connector, MessageParser parser) {
        System.out.println("SendRegionHandler is handling");
        
        SendRegionResponse regionMsg = (SendRegionResponse)message;
        
        //Parts of this may be null
        logAndReflectToServer(connector, regionMsg);
        
        //Respond
        if (regionMsg.all_regions!=null) {
        	regionSetter.setRegions(regionMsg.all_regions);
        }
        if (regionMsg.region_path!=null) {
        	pathSetter.setPath(regionMsg.region_path);
        }
    }
    
    //Log locally (and remotely) that the Region/Path set was received.
    private void logAndReflectToServer(Connector connector, SendRegionResponse regionMsg) {
        if (regionMsg.all_regions!=null) {
        	String msg = "Client received Region set from server [" + regionMsg.all_regions.length + "]";
        	System.out.println(msg);
        	reflectToServer(connector, msg);
        }
        if (regionMsg.region_path!=null) {
        	String msg = "Client received a new Path from server [" + regionMsg.region_path.length + "]";
        	System.out.println(msg);
        	reflectToServer(connector, msg);
        }
    }
    
    private void reflectToServer(Connector connector, String msg) {
        //Prepare a response.
    	RemoteLogMessage obj = new RemoteLogMessage();
        obj.log_message = msg;
        obj.SENDER_TYPE = "ANDROID_EMULATOR";
        obj.SENDER = clientID;
        
        //Append it.
        connector.addMessage(obj);
    }
}
