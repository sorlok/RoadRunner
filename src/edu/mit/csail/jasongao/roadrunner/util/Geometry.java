//Copyright (c) 2013 Singapore-MIT Alliance for Research and Technology
//Licensed under the terms of the MIT License, as described in the file:
//   license.txt   (http://opensource.org/licenses/MIT)

package edu.mit.csail.jasongao.roadrunner.util;

import android.graphics.PointF;
import android.location.Location;

/**
 * Some geometric calculations, used for various things.
 */
public class Geometry  {
	///This code is taken from the OpenJDK.
	//Returns the distance between this point and the line (extended to infinity), or 0 if the point is on the line.
	///http://grepcode.com/file/repository.grepcode.com/java/root/jdk/openjdk/7-b147/java/awt/geom/Line2D.java
	public static double pt_line_dist(PointF p1, PointF p2, PointF pt) {
		//Adjust vectors relative to x1,y1
		// x2,y2 becomes relative vector from x1,y1 to end of segment
		p2.x -= p1.x;
		p2.y -= p1.y;
		// px,py becomes relative vector from x1,y1 to test point
		pt.x -= p1.x;
		pt.y -= p1.y;
		double dotprod = pt.x * p2.x + pt.y * p2.y;
		// dotprod is the length of the px,py vector
		// projected on the x1,y1=>x2,y2 vector times the
		// length of the x1,y1=>x2,y2 vector
		double projlenSq = dotprod * dotprod / (p2.x * p2.x + p2.y * p2.y);
		// Distance to line is now the length of the relative point
		// vector minus the length of its projection onto the line
		double lenSq = pt.x * pt.x + pt.y * pt.y - projlenSq;
		if (lenSq < 0) {
			lenSq = 0;
		}
		return Math.sqrt(lenSq);
	}
	
	
	///This is a hack; it just normalizes the points to each other then converts them LINEARLY to meters as if
	/// they were at the equator. It's accurate enough for the kinds of distance checks we're doing (re-routing).
	///NOTE: Since this hack is only used for distances, we do not invert latitude.
	public static void normalize_to_meters(Location l1, Location l2, Location l3, PointF p1, PointF p2, PointF p3) {
		double minLat = Math.min(Math.min(l1.getLatitude(), l2.getLatitude()), l3.getLatitude());
		double minLng = Math.min(Math.min(l1.getLongitude(), l2.getLongitude()), l3.getLongitude());
		
		//Now convert.
		linear_convert_to_meters(l1.getLatitude() - minLat, l1.getLongitude() - minLng, p1);
		linear_convert_to_meters(l2.getLatitude() - minLat, l2.getLongitude() - minLng, p2);
		linear_convert_to_meters(l3.getLatitude() - minLat, l3.getLongitude() - minLng, p3);
	}
	
	///Linearly convert a Lat/Lng pair to meters. Only works if the difference is small (hence, call normalize_to_meters() first).
	private static void linear_convert_to_meters(double lat, double lng, PointF res) {
		//Assume we are at the equator.
		res.x = (float)(lng * 111.321 * 1000);
		res.y = (float)(lat * 110.567 * 1000);
	}
}
