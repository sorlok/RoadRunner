//Copyright (c) 2013 Singapore-MIT Alliance for Research and Technology
//Licensed under the terms of the MIT License, as described in the file:
//   license.txt   (http://opensource.org/licenses/MIT)

package edu.mit.csail.jasongao.roadrunner.ext;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/**
 * Contains a helper method for generating a randomized token set.
 * @author Seth N. Hetu
 */
public class TokenRandomizer  {
	/**
	 * Generate a random set of tokens (usually sampled from 'A' through 'Z').
	 */
	public static Set<String> RandomTokens(Random rand, String token_range, int lower, int upper) {
		Set<String> res = new HashSet<String>();
		
		//How many tokens?
		int numTokens = rand.nextInt(upper-lower)+lower;		
		for (int i=0; i<numTokens; i++) {
			//Which letter?
			res.add(Character.toString(token_range.charAt(rand.nextInt(token_range.length()))));
		}
		
		return res;
	}
	
	/**
	 * Spoof regions too. Returns null for no region (e.g., "FREE")
	 */
	public static String SpoofRandomRegion(Random rand, String token_range, double percentChance) {
		if (rand.nextDouble() <= percentChance) { return null; }
		return Character.toString((token_range.charAt(rand.nextInt(token_range.length()))));
	}

}
