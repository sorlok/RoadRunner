package edu.mit.csail.jasongao.roadrunner;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import android.location.Location;

public class Region {	
	public String id;
	
	public List<Location> vertices;
	
	public Region(String id_) {
		this.id = id_;
		vertices = new ArrayList<Location>();
	}
	
	public void addVertex(double latitude, double longitude) {
		Location v = new Location("");
		v.setLongitude(longitude);
		v.setLatitude(latitude);
		vertices.add(v);
	}
	
	/**
	 * Return the first Region in the region set that contains the given Location.
	 * @param rs The set of regions to check.
	 * @param loc The Location we are searching for.
	 * @param def The default value to return if no Regions contain the given Location.
	 * @return The first valid Region, or the default value.
	 */
	public static String Loc2Region(Hashtable<String, Region> rs, Location loc, String def) {
		for (Region r : rs.values()) {
			if (r.contains(loc)) {
				return r.id;
			}
		}
		return def;
	}

	/** Test if a Location p is inside this Region */
	public boolean contains(Location p) {

		double x = p.getLongitude();
		double y = p.getLatitude();
		int polySides = vertices.size();
		boolean oddTransitions = false;

		for (int i = 0, j = polySides - 1; i < polySides; j = i++) {
			if (   (vertices.get(i).getLatitude() < y && vertices.get(j).getLatitude() >= y)
				|| (vertices.get(j).getLatitude() < y && vertices.get(i).getLatitude() >= y)) {
				if (vertices.get(i).getLongitude() + 
						(y - vertices.get(i).getLatitude()) / (vertices.get(j).getLatitude() 
						- vertices.get(i).getLatitude())
						* (vertices.get(j).getLongitude() - vertices.get(i).getLongitude()) < x) {
					oddTransitions = !oddTransitions;
				}
			}
		}
		return oddTransitions;
	}
}
