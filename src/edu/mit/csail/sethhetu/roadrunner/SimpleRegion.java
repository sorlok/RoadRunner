package edu.mit.csail.sethhetu.roadrunner;


/**
 * A simpler version of a Region, used for serialization.
 * @author sethhetu
 */
public class SimpleRegion {
	public static class SimpleLocation {
		public double longitude;
		public double latitude;
	}
	
	public String id;
	public SimpleLocation[] vertices;
}
