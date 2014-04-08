//Copyright (c) 2013 Singapore-MIT Alliance for Research and Technology
//Licensed under the terms of the MIT License, as described in the file:
//   license.txt   (http://opensource.org/licenses/MIT)

package edu.mit.csail.jasongao.roadrunner.ext;

import edu.mit.smart.sm4and.message.Message;

/**
 * Handles a "Send me Regions" request from the client to the server and back.
 * @author Seth N. Hetu
 */
public class RegionMessages  {
	/** A simpler version of a Region, used for serialization. */
	public static class SimpleRegion {
		public static class SimpleLocation {
			public double lng;
			public double lat;
		}
		
		public String id;
		public SimpleLocation[] vertices;
	}
	
	
	/** A message from the client asking the server to re-route it. */
	public static class RerouteRequest extends Message {
		public static final String MessageType = "reroute_request";
		
		public RerouteRequest() {
			super(MessageType);
		}
		
		public String blacklist_region;
	}
	
	/** A response from the server with an attached list of Regions. */
	public static class SendRegionResponse extends Message {
		public static final String MessageType = "regions_and_path";
		
		public SendRegionResponse() { 
			super(MessageType);
		}
	    public SimpleRegion[] regions;
	    public String[] path;
	} 

}
