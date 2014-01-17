package edu.mit.csail.sethhetu.roadrunner.impl;

import android.os.Handler;
import edu.mit.csail.jasongao.roadrunner.RoadRunnerService.AdHocAnnouncer;
import edu.mit.csail.jasongao.roadrunner.RoadRunnerService.LocationSpoofer;
import edu.mit.csail.jasongao.roadrunner.RoadRunnerService.RegionChecker;
import edu.mit.csail.sethhetu.roadrunner.LoggerI;
import edu.mit.smart.sm4and.Connector;


/**
 * Contains the protected implementation of RoadRunnerService. 
 * You will never have to interact with this class directly.
 * 
 * @author Seth N. Hetu
 */
public class SimMobilityBrokerImpl {
	//Have we started yet?
	protected boolean activated = false;
	
	//We need to maintain an open connection to the Sim Mobility server, since we are in a 
	//  tight time loop.
	protected Connector conn;
	
	//For communicating back to the RoadRunner service.
	protected Handler myHandler;
	protected LoggerI logger;
	protected AdHocAnnouncer adhoc;
	protected LocationSpoofer locspoof;
	protected RegionChecker regcheck;

}
