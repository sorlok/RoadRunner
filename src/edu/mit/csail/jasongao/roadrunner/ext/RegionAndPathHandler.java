package edu.mit.csail.jasongao.roadrunner.ext;

import java.util.ArrayList;
import java.util.Hashtable;

import edu.mit.csail.jasongao.roadrunner.Globals;
import edu.mit.csail.jasongao.roadrunner.Region;
import edu.mit.csail.jasongao.roadrunner.ResRequest;
import edu.mit.csail.jasongao.roadrunner.RoadRunnerService;
import edu.mit.csail.jasongao.roadrunner.RoadRunnerService.PathSetter;
import edu.mit.csail.jasongao.roadrunner.RoadRunnerService.RegionSetter;
import edu.mit.csail.jasongao.roadrunner.ext.RegionMessages.SendRegionResponse;
import edu.mit.csail.jasongao.roadrunner.ext.RegionMessages.SimpleRegion;
import edu.mit.csail.jasongao.roadrunner.ext.RegionMessages.SimpleRegion.SimpleLocation;
import edu.mit.csail.jasongao.roadrunner.util.LoggingRuntimeException;
import edu.mit.smart.sm4and.connector.Connector;
import edu.mit.smart.sm4and.handler.AbstractMessageHandler;
import edu.mit.smart.sm4and.message.Message;
import edu.mit.smart.sm4and.message.MessageParser;

public class RegionAndPathHandler extends AbstractMessageHandler {
	private RegionSetter regSetter;
	private PathSetter pathSetter;
	private RoadRunnerService rr;
	

	public RegionAndPathHandler(RoadRunnerService rr, RegionSetter regSetter, PathSetter pathSetter) {
		if (regSetter==null || pathSetter==null) {
			throw new LoggingRuntimeException("Region and Path settings need to be non-null.");
		}
		
		this.regSetter = regSetter;
		this.pathSetter = pathSetter;
		this.rr = rr;
	}
	
	
	@Override
	public void handle(Message message, Connector connector, MessageParser parser) {
        SendRegionResponse regionMsg = (SendRegionResponse)message;
        
        //Null == empty
        if (regionMsg.regions==null || regionMsg.regions.length==0) {
        	regionMsg.regions = null;
        }
        if (regionMsg.path==null || regionMsg.path.length==0) {
        	regionMsg.path = null;
        }

        //Parts of this may be null
        logAndReflectToServer(connector, regionMsg);
        
        //Respond
        if (regionMsg.regions!=null) {
        	regSetter.setRegions(constructRegions(regionMsg.regions));
        }
        if (regionMsg.path!=null) {
        	pathSetter.setPath(constructPath(regionMsg.path), generateGratisTokens(regionMsg.path));
        }
	}
	
	
	private Hashtable<String,Region> constructRegions(SimpleRegion[] regions) {
        //Change the current region set
		Hashtable<String,Region> res = new Hashtable<String, Region>();
        for (SimpleRegion sr : regions) {
        	Region rg = new Region(sr.id);
        	for (SimpleLocation sloc : sr.vertices) {
        		rg.addVertex(sloc.lat, sloc.lng);
        	}
        	res.put(rg.id, rg);
        }
        
        return res;
	}
	
	
	private ArrayList<ResRequest> constructPath(String[] pathIds) {
		if (!Globals.SIM_MOBILITY || !broker.isActive()) { return null; }
	
		ArrayList<ResRequest> res = new ArrayList<ResRequest>();
		for (int i=0; i<pathIds.length; i++) {
			res.add(new ResRequest(rr, broker.getUniqueId(), ResRequest.RES_GET, pathIds[i]));
		}
		
		return res;
	}
	
	private ArrayList<ResRequest> generateGratisTokens(String[] pathIds) {
		if (!Globals.SIM_MOBILITY || !broker.isActive()) { return null; }
		
		//Gratis tokens are assigned the first time the path is set. Since most Agents will only ever
		// get one path, we just generate them every time, and let the RoadRunner service ignore them  
		// at its discretion. The performance hit is negligible.
		double[] probs = Globals.SM_INITIAL_TOKEN_PROBABILITIES;
		if (probs==null || probs.length==0) { return null; }
		
		//Retrieve our probability, populate a list with each value that passes that threshold.
		ArrayList<ResRequest> res = new ArrayList<ResRequest>();
		double prob = probs[(pathIds.length-1)<probs.length ? (pathIds.length-1) : probs.length-1];
		for (int i=0; i<pathIds.length; i++) {
			if (broker.getRand().nextDouble() <= prob) {
				res.add(new ResRequest(rr, broker.getUniqueId(), ResRequest.RES_GET, pathIds[i]));
			}
		}
		
		//Inform the server (for repeatability/debugging).
		if (broker.isActive() && !res.isEmpty()) {
			StringBuffer msg = new StringBuffer();
			msg.append("Agent received gratis tokens [");
			String comma = "";
			for (ResRequest rr : res) {
				msg.append(comma).append(rr.regionId);
				comma = ",";
			}
			msg.append("]");
			broker.ReflectToServer(msg.toString());
		}
		
		return res;
	}
	
	
    //Log locally (and remotely) that the Region/Path set was received.
    private void logAndReflectToServer(Connector connector, SendRegionResponse regionMsg) {
        if (regionMsg.regions!=null) {
        	String msg = "Client received Region set from server [" + regionMsg.regions.length + "]";
        	System.out.println(msg);
        	broker.ReflectToServer(msg);
        }
        if (regionMsg.path!=null) {
        	String msg = "Client received a new Path from server [" + regionMsg.path.length + "]";
        	System.out.println(msg);
        	broker.ReflectToServer(msg);
        }
    }
}
