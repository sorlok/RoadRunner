package edu.mit.csail.sethhetu.roadrunner;

import android.util.Base64;

/**
 * Helper class for converting byte[] to String, with escape sequences.
 * @author Seth N. Hetu
 */
public class ByteArraySerialization {
	
	/**
	 * Convert a byte array to a String, escaping the semicolons.
	 * @param bytes The byte array to serialize.
	 * @return A Base 64-encoded string, with troublesome characters escaped for json-encoding.
	 */
	public static String Serialize(byte[] bytes) {
		//First, just convert using Base64
		String raw = Base64.encodeToString(bytes, Base64.NO_WRAP);
		
		//Now escape as follows:
		//  . becomes ".."
		//  ; becomes ".1"
		//  : becomes ".2"
		//  \n becomes ".3"
		//(some of these are leftover from UTF-8, and won't occur in Base64)
		StringBuilder res = new StringBuilder();
		for (int i=0; i<raw.length(); i++) {
			char c = raw.charAt(i);
			if (c=='.') { res.append(".."); }
			else if (c==';') { res.append(".1"); }
			else if (c==':') { res.append(".2"); }
			else if (c=='\n') { res.append(".3"); }
			else { res.append(c); }
		}
		return res.toString();
	}
	
	/**
	 * Convert a String to a byte array, un-escaping the semicolons.
	 * @param str The String to deserialize. 
	 * @return The byte[] array that was fed into Serialize() to generate this String.
	 */
	public static byte[] Deserialize(String str) {
		//First, remove our escape sequences.
		StringBuilder unescaped = new StringBuilder();
		for (int i=0; i<str.length(); i++) {
			char c = str.charAt(i);
			if (c=='.') {
				char next = str.charAt(i+1);
				if (next=='.') { unescaped.append("."); }
				else if (next=='1') { unescaped.append(";"); }
				else if (next=='2') { unescaped.append(":"); }
				else if (next=='3') { unescaped.append("\n"); }
				else { throw new LoggingRuntimeException("Bad escape sequence."); }
			} else {
				unescaped.append(c);
			}
		}
		
		//Next, just convert using Base64
		return Base64.decode(unescaped.toString(), Base64.NO_WRAP);
	}
	
}
