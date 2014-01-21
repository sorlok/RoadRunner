//Copyright (c) 2013 Singapore-MIT Alliance for Research and Technology
//Licensed under the terms of the MIT License, as described in the file:
//   license.txt   (http://opensource.org/licenses/MIT)

package edu.mit.csail.jasongao.roadrunner.ext;

import edu.mit.csail.sethhetu.roadrunner.SimpleRegion;
import edu.mit.smart.sm4and.message.Message;

/**
 * Handles a "Send me Regions" request from the client to the server and back.
 * @author Seth N. Hetu
 */
public class RegionMessages  {
	/** A message from the client asking the server to re-route it. */
	public static class RerouteRequest extends Message {
		public static final String MessageType = "REROUTE_REQUEST";
		
		public RerouteRequest(String uniqueId, String blacklistRegion) {
			super(MessageType, uniqueId);
			this.blacklist_region = blacklistRegion;
		}
		
		public String blacklist_region;
		
		//This constructor is only used by Gson.
		@SuppressWarnings("unused")
		private RerouteRequest() { this("0", ""); }
	}
	
	/** A response from the server with an attached list of Regions. */
	public static class SendRegionResponse extends Message {
		public static final String MessageType = "REGIONS_AND_PATH_DATA";
		
		public SendRegionResponse(String uniqueId, SimpleRegion[] all_regions, String[] region_path) { 
			super(MessageType, uniqueId);
			this.all_regions = all_regions;
			this.region_path = region_path;
		}
	    public SimpleRegion[] all_regions;
	    public String[] region_path;
	    
		//This constructor is only used by Gson.
		@SuppressWarnings("unused")
		private SendRegionResponse() { this("0", null, null); }
	} 

}
