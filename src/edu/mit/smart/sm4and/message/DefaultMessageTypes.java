//Copyright (c) 2013 Singapore-MIT Alliance for Research and Technology
//Licensed under the terms of the MIT License, as described in the file:
//   license.txt   (http://opensource.org/licenses/MIT)

package edu.mit.smart.sm4and.message;


/**
 * Contains all DefaultMessage types supported by the system. (More can be added through user customization.)
 * These are all very simple data-type classes, and should expose only member variables and a constructor.
 * Private no-args constructors are provided for Gson
 */
public class DefaultMessageTypes {
	/** An OpaqueSend message. Contains an opaque block of Base64-encoded data. */
	public static class OpaqueSendMessage extends Message { 
		public OpaqueSendMessage(String uniqueId, String data) { 
			super(Type.OPAQUE_SEND, uniqueId);
			this.data = data;
		}
		
		public String fromId;
		public String toId;
		public String data;
		public boolean broadcast;
		
		//This constructor is only used by GSON
		@SuppressWarnings("unused")
		private OpaqueSendMessage() { this("0", ""); }
	}
	
	/** An OpaqueReceive message. Contains an opaque block of Base64-encoded data. */
	public static class OpaqueReceiveMessage extends Message { 
		public OpaqueReceiveMessage(String uniqueId, String data) {
			super(Type.OPAQUE_RECEIVE, uniqueId);
			this.data = data;
		}
		
		public String fromId;
		public String toId;
		public String data;
		
		//This constructor is only used by GSON
		@SuppressWarnings("unused")
		private OpaqueReceiveMessage() { this("0", ""); }
	}

	/** A message from the server indicating that the client may proceed. */
	public static class ReadyMessage extends Message {
		public ReadyMessage(String uniqueId) {
			super(Type.READY, uniqueId); 
		}
		
		//This constructor is only used by GSON
		@SuppressWarnings("unused")
		private ReadyMessage() { this("0"); }
	}

	/** A message from the server indicating that the server is now ready for this time tick. */
	public static class ReadyToReceiveMessage extends Message {
		public ReadyToReceiveMessage(String uniqueId) {
			super(Type.READY_TO_RECEIVE, uniqueId); 
		}
		
		//This constructor is only used by GSON
		@SuppressWarnings("unused")
		private ReadyToReceiveMessage() { this("0"); }
	}
	
	/** Client tells the server to continue. */
	public static class ClientDoneResponse extends Message {
		public ClientDoneResponse(String uniqueId) {
			super(Type.CLIENT_MESSAGES_DONE, uniqueId); 
		}
		
		//This constructor is only used by GSON
		@SuppressWarnings("unused")
		private ClientDoneResponse() { this("0"); }
	}
	
	/** A message from the server indicating that the current time has advanced. */
	public static class TimeMessage extends Message {		
		public TimeMessage(String uniqueId, int tick, int elapsed_ms) {
			super(Type.TIME_DATA, uniqueId);
			this.tick = tick;
			this.elapsed_ms = elapsed_ms;
		}
		public int tick;    
		public int elapsed_ms;
		
		//This constructor is only used by GSON
		@SuppressWarnings("unused")
		private TimeMessage() { this("0", 0, 0); }
	}

	
	/** A message from the server requesting that the client identify itself. */
	public static class WhoAreYouMessage extends Message {
		public WhoAreYouMessage(String uniqueId) {
			super(Type.WHOAREYOU, uniqueId); 
		}
		
		public String token;
		
		//This constructor is only used by GSON
		@SuppressWarnings("unused")
		private WhoAreYouMessage() { this("0"); }
	}
	
	/** A response to the server identifying oneself. */
	public static class WhoAmIResponse extends Message {
		public WhoAmIResponse(String uniqueId, String[] requiredServices, String token) {
			super(Type.WHOAMI, uniqueId);
			this.REQUIRED_SERVICES = requiredServices;
			this.token = token;
			this.type = "android";
			
			//These are duplicated, but necessary for now.
			this.ID = this.SENDER;
		}
	    public String ID;
	    public String type;
	    public String[] REQUIRED_SERVICES;
	    public String token;
	    
		//This constructor is only used by GSON
		@SuppressWarnings("unused")
		private WhoAmIResponse() { this("0", null, ""); }
	}
	
	/** A location update message. Uses projected (x,y) coordinates, not Latitude/Longitude. */
	public static class LocationMessage extends Message { 
		public LocationMessage(String uniqueId, double lat, double lng) {
			super(Type.LOCATION_DATA, uniqueId);
			this.lat = lat;
			this.lng = lng;
		}
		public double lat;
		public double lng;
	    
		//This constructor is only used by GSON
		@SuppressWarnings("unused")
		private LocationMessage() { this("0", 0, 0); }
	}
}
