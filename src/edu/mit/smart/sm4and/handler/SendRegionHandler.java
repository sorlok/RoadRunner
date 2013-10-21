//Copyright (c) 2013 Singapore-MIT Alliance for Research and Technology
//Licensed under the terms of the MIT License, as described in the file:
//   license.txt   (http://opensource.org/licenses/MIT)

package edu.mit.smart.sm4and.handler;

import edu.mit.csail.jasongao.roadrunner.Region;
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
	public static class SendRegionRequest extends Message {
		public SendRegionRequest() { this.MESSAGE_TYPE = Type.SEND_REGIONS; }
	}
	
	/** A response from the server with an attached list of Regions. */
	public static class SendRegionResponse extends Message {
		public SendRegionResponse() { this.MESSAGE_TYPE = Type.REGIONS_SENT; }
	    public Region[] REGIONS;
	}
	
	private RegionSetter regionSetter;
	    
    public SendRegionHandler(RegionSetter regionSetter) {
        System.out.println("creating SendRegionHandler");
        this.regionSetter = regionSetter;
    }

    @Override
    public void handle(Message message, Connector connector, MessageParser parser) {
        System.out.println("SendRegionHandler is handling");
        
        SendRegionResponse regionMsg= (SendRegionResponse)message;
        System.out.println("Regions sent:  " + regionMsg.REGIONS.length);
        
        //Respond
        regionSetter.setRegions(regionMsg.REGIONS);
    }
}
