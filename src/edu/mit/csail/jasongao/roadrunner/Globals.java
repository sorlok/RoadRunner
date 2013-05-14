package edu.mit.csail.jasongao.roadrunner;

public class Globals {
	/** Enable communication via Sim Mobility, including time-step synchronization. */
	public static final boolean SIM_MOBILITY = true;
	public static final String SM_HOST = "192.168.1.109"; //Note that 127.0.0.1 won't work; Android assigns this to the phone.
	public static final int SM_PORT = 6745;
	public static final int SM_TIMEOUT = 0;  //0=infinite
	
	/** We offer single-letter tokens, sampled from this range. 
	 *  NOTE: Tokens are *not* consistent at the moment; vehicles will offer them randomly. */
	public static final String SM_TOKEN_RANGE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
	
	/** The minimum (inclusive) and maximum (exclusive) number of tokens we send each "announce" */
	public static final int SM_NUM_TOKENS_LOWER = 0;
	public static final int SM_NUM_TOKENS_UPPER = 5;
	
	/** With what (flat) percentage is a link considered viable? */
	public static final double SM_VIABILITY_PERCENT = 0.7;
	
	/** With what (uniform) percentage is a vehicle in a free zone? */
	public static final double SM_FREE_REGION_PERCENT = 0.5;
	
	/** Adhoc wireless communication parameters */
	public static final long ADHOC_ANNOUNCE_PERIOD = 2000 * 1;
	static final boolean ADHOC_UDP_ONLY = true; // false to use TCP for transfer
	static final int ADHOC_MAX_PACKET_SIZE = 1024; // bytes
	final static public String ADHOC_SEND_REMOTE_ADDRESS = "192.168.42.255";
	
	/** Now set in MainActivity.onCreate() */
	static String ADHOC_IFACE_NAME = "eth0"; // default to adhoc wifi
	static int ADHOC_RECV_PORT = 4200; // set send and recv port same for adhoc WiFi
	static int ADHOC_SEND_PORT = 4200;
	static int CLOUD_PORT = 50000; // 50001 for wifi, 50000 for dsrc
	
	/** Cloud parameters */
	static final String CLOUD_HOST = "128.30.87.68";
	static final int CLOUD_SOCKET_TIMEOUT = 3000;
	static final byte[] CLOUD_PUBLIC_KEY = null; // TODO
	static final byte[] MY_PRIVATE_KEY = null; // TODO

	/** Request timeouts */
	static int REQUEST_PENALTY_VALID_PERIOD = 600000; // 10 min
	static int REQUEST_PENALTY_CHECK_PERIOD = 60000; // 1 min
	static int REQUEST_DEADLINE_CHECK_PERIOD = 500;
	static int REQUEST_DIRECT_PUT_DEADLINE_FROM_NOW = 10 * 60 * 1000; // TODO
	static int REQUEST_DIRECT_GET_DEADLINE_FROM_NOW = 10 * 60 * 1000; // TODO
	static int REQUEST_RELAY_GET_DEADLINE_FROM_NOW = 3000; // deprecated

	/** Relaying cloud accesses through other devices with hot links */
	static boolean RELAY_ENABLED = false; // doesn't work well currently
	static final int LAST_DATA_ACTIVITY_THRESHOLD = 8000;
	
	/** Navigation */
	static boolean NAV_SPEECH = true;
	static boolean NAV_REQUESTS = true;
	static boolean SUPER_DENSE_REQUESTS = false; // for super-dense test

	/** Experiment automation */
	static long EXPT_START_DELAY = 1 * 15 * 1000;
	static long RESET_SERVER_DELAY = 1 * 15 * 1000;
	static long EXPT_LENGTH = 10 * 60 * 1000; // TODO now set in MainActivity
	static final boolean EXPT_DEBUG = true; // short 1 minute expts for test
	static long START_TIME = 1354161600L * 1000L; // GMT-4
}
