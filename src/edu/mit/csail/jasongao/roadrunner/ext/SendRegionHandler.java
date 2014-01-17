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
public class SendRegionHandler  {
	/** A message from the client asking the server to re-route it. */
	public static class RerouteRequest extends Message {
		public RerouteRequest() { this.MESSAGE_TYPE = Type.REROUTE_REQUEST; }
		public String blacklist_region;
	}
	
	/** A response from the server with an attached list of Regions. */
	public static class SendRegionResponse extends Message {
		public SendRegionResponse() { this.MESSAGE_TYPE = Type.REGIONS_AND_PATH_DATA; }
	    public SimpleRegion[] all_regions;
	    public String[] region_path;
	}
	
	/** Simple remote-Log message. (TODO: put into its own file). */
	public static class RemoteLogMessage extends Message {
		public RemoteLogMessage() { this.MESSAGE_TYPE = Type.REMOTE_LOG; }
	    public String log_message;
	}    

}
