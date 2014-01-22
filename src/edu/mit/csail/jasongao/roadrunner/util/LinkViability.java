//Copyright (c) 2013 Singapore-MIT Alliance for Research and Technology
//Licensed under the terms of the MIT License, as described in the file:
//   license.txt   (http://opensource.org/licenses/MIT)

package edu.mit.csail.jasongao.roadrunner.util;

import android.location.Location;
import edu.mit.csail.jasongao.roadrunner.AdhocPacket;

/**
 * Various tests for whether a link is viable.
 */
public class LinkViability  {
	/**
	 * Determine whether two vehicle's location fixes indicate that a DSRC UDP
	 * link can be sustained over the next Globals.LINK_LIFETIME_THRESHOLD secs
	 * 
	 * @param v1
	 *            Location of vehicle 1
	 * @param v2
	 *            Location of vehicle 2
	 * @return true is the link is viable, false if not
	 */
	public static boolean IsViableDSRCComplex(Location v1, AdhocPacket other, LoggerI logger) {
		Location v2 = other.getLocation();

		if (v1 == null || v2 == null) {
			logger.log_nodisplay("Link viable debug: no GPS fix.");
			return true;
		}

		float distance = v1.distanceTo(v2);

		// Too far away (> 250m)
		if (distance > 250) {
			logger.log_nodisplay(String.format("Link not viable: %.1f meters apart. (>250)", distance));
			return false;
		}

		// Quite close together (< 150 m)
		if (v1.distanceTo(v2) < 150) {
			logger.log_nodisplay(String.format("Link viable: %.1f meters apart. (<100m)", distance));
			return true;
		}

		// Both stationary?
		if (v1.hasSpeed() && v1.getSpeed() < 2 && v2.hasSpeed()
				&& v2.getSpeed() < 2) {
			logger.log_nodisplay(String.format("Link viable: %.1f meters apart. (low speed)", distance));
			return true;
		}

		// One stationary and other moving towards it?
		if (v1.hasSpeed()
				&& v1.getSpeed() < 2
				&& v2.hasBearing()
				&& ((Math.abs(v2.bearingTo(v1) - v2.getBearing()) < 45) || (Math
						.abs(v2.bearingTo(v1) - v2.getBearing()) > 360 - 45))) {
			logger.log_nodisplay(String.format("Link viable: %.1f meters apart. (other approaching)",distance));
			return true;
		}
		if (v2.hasSpeed()
				&& v2.getSpeed() < 2
				&& v1.hasBearing()
				&& ((Math.abs(v1.bearingTo(v2) - v1.getBearing()) < 45) || (Math
						.abs(v1.bearingTo(v2) - v1.getBearing()) > 360 - 45))) {
			logger.log_nodisplay(String.format("Link viable: %.1f meters apart. (approaching other)",distance));
			return true;
		}

		// Both moving towards each other
		if (v1.distanceTo(v2) < 200
				&& v1.hasBearing()
				&& v2.hasBearing()
				&& (Math.abs(v1.bearingTo(v2) - v1.getBearing()) < 15 || Math
						.abs(v1.bearingTo(v2) - v1.getBearing()) > 360 - 15)
				&& (Math.abs(v2.bearingTo(v1) - v2.getBearing()) < 15 || Math
						.abs(v2.bearingTo(v1) - v2.getBearing()) > 360 - 15)) {
			logger.log_nodisplay(String.format("Link viable: %.1f meters apart. (mutual approach)",distance));
			return true;
		}

		// Moving together?
		if (v1.distanceTo(v2) < 200
				&& v1.hasBearing()
				&& v2.hasBearing()
				&& v1.hasSpeed()
				&& v2.hasSpeed()
				&& (Math.abs(v1.getBearing() - v2.getBearing()) < 15 || Math
						.abs(v1.getBearing() - v2.getBearing()) > 360 - 15)
				&& Math.abs(v1.getSpeed() - v2.getSpeed()) < 5) {
			logger.log_nodisplay(String.format("Link viable: %.1f meters apart. (moving together)",distance));
			return true;
		}

		// log_nodisplay(String.format(
		// "Link viable: %.1f meters apart. (moving apart)", distance));
		logger.log_nodisplay(String.format("Link not viable: %.1f meters apart. (moving apart)", distance));
		return false;
	}

	/**
	 * Determine whether two vehicle's location fixes indicate that a DSRC UDP
	 * link is viable for a token transfer / other communications
	 * 
	 * @param v1
	 *            Location of vehicle 1
	 * @param v2
	 *            Location of vehicle 2
	 * @return true is the link is viable, false if not
	 */
	public static boolean IsViableDSRC(Location v1, Location v2, LoggerI logger) {
		if (v1 == null || v2 == null) {
			logger.log_nodisplay("Link not viable: GPS disabled");
			return false;
		}

		float distance = v1.distanceTo(v2);
		long threshold = 250;
		boolean viable = (distance < threshold);

		logger.log_nodisplay(String.format(
			"Link %s: %.1f meters apart (threshold %d). v1=%s v2=%s",
			viable ? "viable" : "not viable", distance, threshold, v1, v2)
		);

		return viable;
	}

	/**
	 * Determine whether two vehicle's location fixes indicate that a WiFi TCP
	 * link can be sustained over the next Globals.LINK_LIFETIME_THRESHOLD secs
	 * 
	 * NOTE: We currently don't set speed information in Sim Mobility, so we can't
	 *       use this method. Currently, the DSRC method is the one actually used, so
	 *       it's not really an issue; just note that we might have to add speed
	 *       to Location information in the future. 
	 * 
	 * @param v1
	 *            Location of vehicle 1
	 * @param v2
	 *            Location of vehicle 2
	 * @return true is the link is viable, false if not
	 */
	public static boolean IsViableWiFi(Location v1, AdhocPacket other, LoggerI logger) {
		Location v2 = other.getLocation();

		if (v1 == null || v2 == null) {
			logger.log_nodisplay("Link viable debug: no GPS fix.");
			return true;
		}

		float distance = v1.distanceTo(v2);

		// Too far away (> 70m)
		if (distance > 70) {
			logger.log_nodisplay(String.format("Link not viable: %.1f meters apart. (>70)", distance));
			return false;
		}

		// Quite close together (< 20 m)
		if (v1.distanceTo(v2) < 20) {
			logger.log_nodisplay(String.format("Link viable: %.1f meters apart. (<20m)", distance));
			return true;
		}

		// Both stationary?
		if (v1.hasSpeed() && v1.getSpeed() < 2 && v2.hasSpeed() && v2.getSpeed() < 2) {
			logger.log_nodisplay(String.format("Link viable: %.1f meters apart. (low speed)", distance));
			return true;
		}

		// One stationary and other moving towards it?
		if (v1.hasSpeed()
				&& v1.getSpeed() < 2
				&& v2.hasBearing()
				&& ((Math.abs(v2.bearingTo(v1) - v2.getBearing()) < 45) || (Math
						.abs(v2.bearingTo(v1) - v2.getBearing()) > 360 - 45))) {
			logger.log_nodisplay(String.format("Link viable: %.1f meters apart. (other approaching)",distance));
			return true;
		}
		if (v2.hasSpeed()
				&& v2.getSpeed() < 2
				&& v1.hasBearing()
				&& ((Math.abs(v1.bearingTo(v2) - v1.getBearing()) < 45) || (Math
						.abs(v1.bearingTo(v2) - v1.getBearing()) > 360 - 45))) {
			logger.log_nodisplay(String.format("Link viable: %.1f meters apart. (approaching other)",distance));
			return true;
		}

		// Both moving towards each other
		if (v1.distanceTo(v2) < 35
				&& v1.hasBearing()
				&& v2.hasBearing()
				&& (Math.abs(v1.bearingTo(v2) - v1.getBearing()) < 15 || Math
						.abs(v1.bearingTo(v2) - v1.getBearing()) > 360 - 15)
				&& (Math.abs(v2.bearingTo(v1) - v2.getBearing()) < 15 || Math
						.abs(v2.bearingTo(v1) - v2.getBearing()) > 360 - 15)) {
			logger.log_nodisplay(String.format("Link viable: %.1f meters apart. (mutual approach)",distance));
			return true;
		}

		// Moving together?
		if (v1.distanceTo(v2) < 35
				&& v1.hasBearing()
				&& v2.hasBearing()
				&& v1.hasSpeed()
				&& v2.hasSpeed()
				&& (Math.abs(v1.getBearing() - v2.getBearing()) < 15 || Math
						.abs(v1.getBearing() - v2.getBearing()) > 360 - 15)
				&& Math.abs(v1.getSpeed() - v2.getSpeed()) < 5) {
			logger.log_nodisplay(String.format("Link viable: %.1f meters apart. (moving together)",distance));
			return true;
		}

		// log_nodisplay(String.format(
		// "Link viable: %.1f meters apart. (moving apart)", distance));
		logger.log_nodisplay(String.format("Link not viable: %.1f meters apart. (moving apart)", distance));
		return false;
	}
}
