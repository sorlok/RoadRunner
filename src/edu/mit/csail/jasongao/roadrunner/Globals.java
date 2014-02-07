package edu.mit.csail.jasongao.roadrunner;

public class Globals {
	/** Enable communication via Sim Mobility, including time-step synchronization. */
	public static final boolean SIM_MOBILITY = true;
	
	private static final int HDirect = 6745;
	private static final int HRelay = 6799;
	
	//public static final String SM_HOST = "10.0.0.1"; //Note that 127.0.0.1 won't work; Android assigns this to the phone.
	public static final String SM_HOST = "128.30.87.128"; //Desktop
	
	//public static final String SM_HOST = Hermes11;
	public static final int SM_PORT = HDirect;
	
	
	public static final int SM_TIMEOUT = 0;  //0=infinite
	public static final boolean SM_AUTORUN = true; //Set to "true" to auto-run the app (good for MegaDroid)
	public static final boolean SM_VERBOSE_TRACE = false; //Set to "true" to get a trace of all messages sent and received.
	public static final boolean SM_ALLOW_RANDOM_REGIONS = true; //As a fallback mode, a "null" set of Region just leads to randomization.
	public static final boolean SM_REAL_REGIONS = true; //If "true", we will ask Sim Mobility for the actual Regions. Set to false if you just want to spoof Regions.
	
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
	
	/** 
	 * When do we perform checks to see if we should re-route before entering a Region?
	 * The _BASE value is the minimum, and a random value between 0 and _ADDITIVE is added
	 * when the actual wait time is calculated.
	 * We stagger these to avoid slowing down every X  time ticks, which would throw off our measurements.
	 * Currently set to 1.0 to check every 2.0 (1.9) seconds.
	 */
	public static final int SM_REROUTE_CHECK_BASE = 1000;
	public static final int SM_REROUTE_CHECK_ADDITIVE = 1000;
	
	/** At what distance (or less) do we re-route if we are unable to find a token. */
	public static final double SM_REROUTE_DISTANCE = 250; //meters
	
	/** 
	 * What percentage of our required tokens are initially (randomly) filled?
	 * Each index represents the odds of getting a token for Region lists of that size. 
	 * So, if the list is [0.5, 0.2, 0.1], then you have a 50% chance of getting a token if you 
	 * only need 1, a 20% chance of getting a token if you only need 2, and a 10% chance for 
	 * any other (3+).
	 * Note that these tokens are only handed out the FIRST time you get a token set; if you re-route, 
	 *   you don't get any free tokens.
	 */
	//"roughly 50%" chance of getting at least 1 token. After path size 10, starts increasing >65%
	public static final double[] SM_INITIAL_TOKEN_PROBABILITIES = {0.5, 0.25, 0.2, 0.2, 0.1}; 
	
	/** What string constitutes a "free" region. */
	public static final String FREE_REGION_TAG = "FREE";
	
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
	
	///Which interfaces we use to identify this phone (in order)
	///We list ADHOC_IFACE_NAME first, even though it may repeat.
	public static final String[] SM_IDENTIFYING_INTERFACES = {ADHOC_IFACE_NAME, "eth0", "wlan0"};
	
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
