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
	/** A multicast message. Contains an opaque block of Base64-encoded data. */
	public static class MulticastMessage extends Message { 
		public MulticastMessage(String uniqueId, String mcData) { 
			super(Type.MULTICAST, uniqueId);
			this.MULTICAST_DATA = mcData;
		}
		public String MULTICAST_DATA;
		
		//This constructor is only used by GSON
		@SuppressWarnings("unused")
		private MulticastMessage() { this("0", ""); }
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
	
	/** A unicast message. Contains an opaque block of Base64-encoded data. */
	public static class UnicastMessage extends Message { 
		public UnicastMessage(String uniqueId, String ucData) {
			super(Type.UNICAST, uniqueId);
			this.UNICAST_DATA = ucData;
		}
		public String UNICAST_DATA;
		
		//This constructor is only used by GSON
		@SuppressWarnings("unused")
		private UnicastMessage() { this("0", ""); }
	}
	
	/** A message from the server requesting that the client identify itself. */
	public static class WhoAreYouMessage extends Message {
		public WhoAreYouMessage(String uniqueId) {
			super(Type.WHOAREYOU, uniqueId); 
		}
		
		//This constructor is only used by GSON
		@SuppressWarnings("unused")
		private WhoAreYouMessage() { this("0"); }
	}
	
	/** A response to the server identifying oneself. */
	public static class WhoAmIResponse extends Message {
		public WhoAmIResponse(String uniqueId, String[] requiredServices) {
			super(Type.WHOAMI, uniqueId);
			this.REQUIRED_SERVICES = requiredServices;
			
			//These are duplicated, but necessary for now.
			this.ID = this.SENDER;
			this.TYPE = this.SENDER_TYPE;
		}
	    public String ID;
	    public String TYPE;
	    public String[] REQUIRED_SERVICES;
	    
		//This constructor is only used by GSON
		@SuppressWarnings("unused")
		private WhoAmIResponse() { this("0", null); }
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
