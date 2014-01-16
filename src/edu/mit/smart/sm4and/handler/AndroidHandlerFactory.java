//Copyright (c) 2013 Singapore-MIT Alliance for Research and Technology
//Licensed under the terms of the MIT License, as described in the file:
//   license.txt   (http://opensource.org/licenses/MIT)

package edu.mit.smart.sm4and.handler;

import edu.mit.smart.sm4and.AbstractMessageHandler;
import edu.mit.smart.sm4and.MessageHandlerFactory;
import edu.mit.smart.sm4and.message.Message;
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
	public AbstractMessageHandler create(String msgType) {
    	//Respond to the given message.
		if (msgType.equals(Message.Type.WHOAREYOU)) {
			return new WhoAreYouHandler(clientId);
		} else if (msgType.equals(Message.Type.TIME_DATA)) {
			return new TimeHandler(timeTicker);
		} else if (msgType.equals(Message.Type.READY)) {
			return new ReadyHandler();
		} else if (msgType.equals(Message.Type.LOCATION_DATA)) {
			return new LocationHandler(locSpoof);
		} else if (msgType.equals(Message.Type.MULTICAST)) {
			return new MulticastHandler(mcProcess);
		} else if (msgType.equals(Message.Type.UNICAST)) {
			return new UnicastHandler();
		} else if (msgType.equals(Message.Type.READY_TO_RECEIVE)) {
			return new ReadyToReceiveHandler(clientId);
		} else if (msgType.equals(Message.Type.REGIONS_AND_PATH_DATA)) {
			return new SendRegionHandler(regSet, pathSet, clientId);
		} else {
			throw new LoggingRuntimeException("Unknown message type: " + msgType.toString() + "  >>NOT HANDLED.");
		}
	}
}
