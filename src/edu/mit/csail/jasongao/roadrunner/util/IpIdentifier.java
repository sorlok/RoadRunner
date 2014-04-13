package edu.mit.csail.jasongao.roadrunner.util;

import java.net.Inet4Address;
import java.util.Random;

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
	public static final byte[] GenerateIdFromInet(Inet4Address addr)  {
		//If we have the address, just return it.
		if (addr!=null) {
			return addr.getAddress();
		}
		
		//Otherwise, just fake it.
		byte[] bytes = new byte[4];
		Rand.nextBytes(bytes);
		return bytes;
	}
	
	/**
	 * Helper method: convert a byte to an "unsigned" int.
	 */
	private static final int ToU(byte b) {
		return ((int)b)&0xFF;
	}
}
