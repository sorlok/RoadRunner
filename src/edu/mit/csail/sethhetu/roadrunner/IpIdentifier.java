package edu.mit.csail.sethhetu.roadrunner;

import java.net.Inet4Address;
import java.util.Random;

import edu.mit.csail.sethhetu.roadrunner.SimMobilityBroker;


/**
 * Allows a unique identifier to be constructed from an IPv4 address.
 * @author Seth N. Hetu
 */
public class IpIdentifier {
	private static final Random Rand = new Random();
	
	/**
	 * Converts an ip4 address (xxx.yyy.zzz.aaa) into an identifier of the form (xxxyyyzzzaaa).
	 * @param addr The Inet4Address to use as input. If null, a random address will be generated.
	 * @return The identifier.  
	 */
	public static final long GenerateIdFromInet(Inet4Address addr)  {
		byte[] bytes = null;
		
		//Get/fake the address as an array of bytes.
		if (addr!=null) {
			bytes = addr.getAddress();
		} else {
			bytes = new byte[4];
			Rand.nextBytes(bytes);
		}
		
		//Try to make something semi-recognizable:
		return  ToU(bytes[0])*1000000000
			  + ToU(bytes[1])*1000000
			  + ToU(bytes[2])*1000
			  + ToU(bytes[3]);
	}
	
	/**
	 * Helper method: convert a byte to an "unsigned" int.
	 */
	private static final long ToU(byte b) {
		return ((int)b)&0xFF;
	}
}
