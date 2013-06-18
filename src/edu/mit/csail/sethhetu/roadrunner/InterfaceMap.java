//Copyright (c) 2013 Singapore-MIT Alliance for Research and Technology
//Licensed under the terms of the MIT License, as described in the file:
//   license.txt   (http://opensource.org/licenses/MIT)

package edu.mit.csail.sethhetu.roadrunner;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Hashtable;

/**
 * Maps all (network) interfaces. E.g., "eth0"=>"192.168.0.5"
 *  
 * @author Seth N. Hetu
 */
public class InterfaceMap  {
	///Retrieve the singleton.
	public static InterfaceMap GetInstance() {
		if (instance==null) {
			instance = new InterfaceMap();
		}
		return instance;
	}
	
	/**
	 * Retrieves the first address from a set of interface names. Returns null if none match.
	 */
	public Inet4Address getAddress(String[] intNames) {
		Inet4Address res = null;
		for (String name : intNames) {
			res = mappings.get(name);
			if (res != null) {
				break;
			}
		}
		return res;
	}
	
	public Inet4Address getAddress(String intName) {
		return getAddress(new String[]{intName});
	}
	
	private void buildMappings() {
		//Reset. Try to retrieve the interface enumeration; failure here is fine.
		mappings.clear();
		Enumeration<NetworkInterface> nifs = null;
		try {
			nifs = NetworkInterface.getNetworkInterfaces();
		} catch (SocketException ex) {}
		
		//Loop through it.
		while ((nifs!=null) && nifs.hasMoreElements()) {
			NetworkInterface nif = nifs.nextElement();
			for (Enumeration<InetAddress> enumIpAddr = nif.getInetAddresses(); enumIpAddr.hasMoreElements();) {
				InetAddress inetAddress = enumIpAddr.nextElement();
				if (!inetAddress.isLoopbackAddress() && (inetAddress instanceof Inet4Address)) {
					mappings.put(nif.getName(), (Inet4Address)inetAddress);
					break;
				}
			}
		}
	}
	
	//Storage is relatively simple.
	private Hashtable<String, Inet4Address> mappings;
	
	//It's a singleton.
	private static InterfaceMap instance;
	private InterfaceMap() {
		mappings = new Hashtable<String, Inet4Address>();
		buildMappings();
	}
}
