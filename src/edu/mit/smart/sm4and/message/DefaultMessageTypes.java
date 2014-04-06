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
		public OpaqueSendMessage() { 
			super(Type.opaque_send);
		}
		
		public String from_id;
		public String[] to_ids;
		public String data;
		public boolean broadcast;
	}
	
	/** An OpaqueReceive message. Contains an opaque block of Base64-encoded data. */
	public static class OpaqueReceiveMessage extends Message { 
		public OpaqueReceiveMessage() {
			super(Type.opaque_receive);
		}
		
		public String from_id;
		public String to_id;
		public String data;
	}

	/** A message from the server indicating that the client may proceed. */
	public static class IdAckMessage extends Message {
		public IdAckMessage() {
			super(Type.id_ack); 
		}
	}

	/** A message from the server indicating that the server is now ready for this time tick. */
	public static class TickedSimMobMessage extends Message {
		public TickedSimMobMessage() {
			super(Type.ticked_simmob); 
		}
		
		public int tick;
		public int elapsed;
	}
	
	/** Client tells the server to continue. */
	public static class TickedClientResponse extends Message {
		public TickedClientResponse() {
			super(Type.ticked_client); 
		}
	}

	
	/** A message from the server requesting that the client identify itself. */
	public static class IdRequestMessage extends Message {
		public IdRequestMessage() {
			super(Type.id_request); 
		}
		
		public String token;
	}
	
	/** A response to the server identifying oneself. */
	public static class IdResponse extends Message {
		public IdResponse() {
			super(Type.id_response);
		}
	    public String id;
	    public String type;
	    public String token;
	    public String[] services;
	}
	
	/** A location update message. Uses projected (x,y) coordinates, not Latitude/Longitude. */
	public static class LocationMessage extends Message { 
		public LocationMessage() {
			super(Type.location);
		}
		public double lat;
		public double lng;
		public int x;
		public int y;
	}
}
