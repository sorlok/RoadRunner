//Copyright (c) 2013 Singapore-MIT Alliance for Research and Technology
//Licensed under the terms of the MIT License, as described in the file:
//   license.txt   (http://opensource.org/licenses/MIT)

package edu.mit.smart.sm4and.handler;

import edu.mit.smart.sm4and.AbstractMessageHandler;
import edu.mit.smart.sm4and.MessageHandlerFactory;
import edu.mit.smart.sm4and.message.Message.Type;

import edu.mit.csail.jasongao.roadrunner.RoadRunnerService.LocationSpoofer;
import edu.mit.csail.jasongao.roadrunner.RoadRunnerService.PathSetter;
import edu.mit.csail.sethhetu.roadrunner.LoggingRuntimeException;
import edu.mit.csail.sethhetu.roadrunner.SimMobilityBroker.MultiCastReceiver;
import edu.mit.csail.sethhetu.roadrunner.SimMobilityBroker.RegionSetter;
import edu.mit.csail.sethhetu.roadrunner.SimMobilityBroker.TimeAdvancer;

/**
 * Provides Android-specific handlers for our various messages.
 */
public class AndroidHandlerFactory implements MessageHandlerFactory {
	private String clientId;
	private LocationSpoofer locSpoof;
	private TimeAdvancer timeTicker;
	private MultiCastReceiver mcProcess;
	private RegionSetter regSet;
	private PathSetter pathSet;
	
	public AndroidHandlerFactory(String clientId, LocationSpoofer locSpoof, TimeAdvancer timeTicker, MultiCastReceiver mcProcess, RegionSetter regSet, PathSetter pathSet) {
		this.clientId = clientId;
		this.locSpoof = locSpoof;
		this.timeTicker = timeTicker;
		this.mcProcess = mcProcess;
		this.regSet = regSet;
		this.pathSet = pathSet;
	}
	
	@Override
	public AbstractMessageHandler create(Type msgType) {
    	//Respond to the given message.
    	switch (msgType) {
            case WHOAREYOU:
                return new WhoAreYouHandler(clientId);
            case TIME_DATA: 
                return new TimeHandler(timeTicker);
            case READY: 
                return new ReadyHandler();
            case LOCATION_DATA: 
            	return new LocationHandler(locSpoof);
            case MULTICAST: 
            	return new MulticastHandler(mcProcess);
            case UNICAST:
            	return new UnicastHandler();
            case READY_TO_RECEIVE:
            	return new ReadyToReceiveHandler(clientId);
            case REGIONS_AND_PATH_DATA:
            	return new SendRegionHandler(regSet, pathSet, clientId);
            default:
            	throw new LoggingRuntimeException("Unknown message type: " + msgType.toString() + "  >>NOT HANDLED.");
        }
	}
}
