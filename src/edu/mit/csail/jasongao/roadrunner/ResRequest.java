package edu.mit.csail.jasongao.roadrunner;

import java.io.Serializable;
import java.util.Arrays;

/** A request to get or put a token */
public class ResRequest implements Serializable {
	private static final long serialVersionUID = 101L;

	/** Type of reservation request */
	public static final int RES_GET = 1;
	public static final int RES_PUT = 4;
	public static final int PENALTY = 5;
	public static final int DEBUG_RESET = 13;
	
	/** accessType */
	public static final int CLOUD_DIRECT = 1;
	public static final int CLOUD_RELAY = 2;
	public static final int ADHOC_DIRECT = 3;
	

	/** Always populated */
	public int type;
	public long created, completed;
	public String srcId;
	public String regionId;
	public long softDeadline;
	public long hardDeadline;
	public boolean done;
	public int accessType;

	/** Populated for RES_GET done=true */
	public long issued, expires;
	public String tokenString;
	public String signature;
	
	/*public ResRequest(String srcId_, int type_, String regionId_) {
		this(Long.parseLong(srcId_), type_, regionId_);
	}*/

	public ResRequest(RoadRunnerService service, String srcId, int type_, String regionId_) {
		this.created = service.getTime();
		if (Globals.SIM_MOBILITY) {
			this.softDeadline = this.created + Globals.SM_REQUEST_RELAY_GET_DEADLINE_FROM_NOW;
			this.hardDeadline = this.created + Globals.SM_REQUEST_DIRECT_GET_DEADLINE_FROM_NOW;
		} else {
			this.softDeadline = this.created + Globals.REQUEST_RELAY_GET_DEADLINE_FROM_NOW;
			this.hardDeadline = this.created + Globals.REQUEST_DIRECT_GET_DEADLINE_FROM_NOW;
		}
		this.srcId = srcId;
		this.type = type_;
		this.regionId = regionId_;
		this.done = false;
	}

	/** Checks signature and expiration */
	@SuppressWarnings("unused")
	public boolean tokenIsValid() {
		// TODO EXPT_DEBUG
		if (true) {
			return true;
		}

		// Check expiration
		long now = System.currentTimeMillis();
		if (now > expires) {
			return false;
		} else { // TODO Verify signature
			String tokenString = String.format("%s %d %d", regionId, issued,
					expires);
			byte[] byteArray1 = null;
			byte[] byteArray2 = null;
			return Arrays.equals(byteArray1, byteArray2);
		}
	}

	@Override
	public String toString() {
		return String.format("ResRequest[src=%s, regionId=%s, type=%d, done=%b]", srcId, regionId, type, done);
	}
}
