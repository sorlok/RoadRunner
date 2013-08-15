package edu.mit.csail.jasongao.roadrunner;

import java.util.ArrayList;
import java.util.List;

import edu.mit.csail.sethhetu.roadrunner.LoggerI;

import android.location.Location;


/**
 * A static-only class which contains the testing regions from RoadRunnerService.
 */
public abstract class TestRegions  {
	//No construction
	private TestRegions() {}

	/** Test regions in Stata courtyard */
	public static List<Region> MakeStataRegions() {
		List<Region> rs = new ArrayList<Region>();
		Region r;

		// Region 1
		r = new Region("Stata-1");
		r.addVertex(42.36218276352746,-71.08994364738464);
		r.addVertex(42.36207970542849,-71.08879566192627);
		r.addVertex(42.36181809564884,-71.08882784843445);
		r.addVertex(42.36184980598321,-71.08983635902405);
		rs.add(r);

		// Region 2
		r = new Region("Stata-2");
		r.addVertex(42.36184980598321, -71.08983635902405);
		r.addVertex(42.36181809564884, -71.08882784843445);
		r.addVertex(42.361556484779946, -71.08887076377869);
		r.addVertex(42.36158819524629, -71.08986854553223);
		rs.add(r);

		// Region 3
		r = new Region("Stata-3");
		r.addVertex(42.36158819524629, -71.08986854553223);
		r.addVertex(42.361556484779946, -71.08887076377869);
		r.addVertex(42.36131865577206, -71.08895659446716);
		r.addVertex(42.361366221645646, -71.08989000320435);
		rs.add(r);

		return rs;
	}
	
	public static void TestStataRegions(List<Region> regionSet, LoggerI logger) {
		logger.log("Testing regions and getRegion logic...");
		Location l;
		l = new Location("");
		l.setLatitude(42.36196871959442);
		l.setLongitude(-71.0893964767456);
		logger.log(String.format("Test point 1 is in region %s", RoadRunnerService.GetRegion(regionSet, l)));

		l = new Location("");
		l.setLatitude(42.361659543737126);
		l.setLongitude(-71.0893964767456);
		logger.log(String.format("Test point 2 is in region %s", RoadRunnerService.GetRegion(regionSet, l)));

		l = new Location("");
		l.setLatitude(42.36140585984613);
		l.setLongitude(-71.0893964767456);
		logger.log(String.format("Test point 3 is in region %s", RoadRunnerService.GetRegion(regionSet, l)));
	}
	
	
	/** Regions for our Sim Mobility network */
	public static List<Region> MakeSimMobilityRegions() {
		//TODO: Actually add regions.
		return new ArrayList<Region>();
	}
	

	/** Test regions in Stata courtyard */
	public static List<Region> MakeExperimentARegions() {
		List<Region> rs = new ArrayList<Region>();
		Region r;

		// Region 1
		r = new Region("Stata-1");
		r.addVertex(42.36199654330529, -71.09211625725555);
		r.addVertex(42.362892351545966, -71.09046401650238);
		r.addVertex(42.362234369747256, -71.08898343712616);
		r.addVertex(42.36113243298882, -71.09136523873138);
		rs.add(r);

		return rs;
	}
	
	public static void TestExperimentARegions(List<Region> regionSet, LoggerI logger) {
		logger.log("Testing regions and getRegion logic...");
		
		Location l;
		l = new Location("");
		l.setLatitude(42.361921154176926);
		l.setLongitude(-71.09134912490845);
		logger.log(String.format("Test point Stata-1 is in region %s",
				RoadRunnerService.GetRegion(regionSet, l)));
	}
	
	
	/** Regions for Randomized road network. */
	/** Generated: 2013-08-15 15:39 */
	public static List<Region> MakeRandomNetworkRegions(LoggerI logger) {
	  List<Region> rs = new ArrayList<Region>();
	  Region r;
	  
	  r = new Region("Region_1");
	  r.addVertex(0.005564, 0.016406);
	  r.addVertex(0.006955, 0.014266);
	  r.addVertex(0.005564, 0.012126);
	  r.addVertex(0.004173, 0.014266);
	  rs.add(r);
	  
	  r = new Region("Region_2");
	  r.addVertex(0.001855, 0.010700);
	  r.addVertex(0.003246, 0.008560);
	  r.addVertex(0.001855, 0.006420);
	  r.addVertex(0.000464, 0.008560);
	  rs.add(r);
	  
	  r = new Region("Region_3");
	  r.addVertex(0.020401, 0.027819);
	  r.addVertex(0.021792, 0.025679);
	  r.addVertex(0.020401, 0.023539);
	  r.addVertex(0.019010, 0.025679);
	  rs.add(r);
	  
	  r = new Region("Region_4");
	  r.addVertex(0.009273, 0.039232);
	  r.addVertex(0.010664, 0.037092);
	  r.addVertex(0.009273, 0.034952);
	  r.addVertex(0.007882, 0.037092);
	  rs.add(r);
	  
	  r = new Region("Region_5");
	  r.addVertex(0.027820, 0.022113);
	  r.addVertex(0.029211, 0.019973);
	  r.addVertex(0.027820, 0.017833);
	  r.addVertex(0.026429, 0.019973);
	  rs.add(r);
	  
	  r = new Region("Region_6");
	  r.addVertex(0.012983, 0.022113);
	  r.addVertex(0.014374, 0.019973);
	  r.addVertex(0.012983, 0.017833);
	  r.addVertex(0.011592, 0.019973);
	  rs.add(r);
	  
	  r = new Region("Region_7");
	  r.addVertex(0.024111, 0.039232);
	  r.addVertex(0.025502, 0.037092);
	  r.addVertex(0.024111, 0.034952);
	  r.addVertex(0.022720, 0.037092);
	  rs.add(r);
	  
	  r = new Region("Region_8");
	  r.addVertex(0.020401, 0.039232);
	  r.addVertex(0.021792, 0.037092);
	  r.addVertex(0.020401, 0.034952);
	  r.addVertex(0.019010, 0.037092);
	  rs.add(r);
	  
	  r = new Region("Region_9");
	  r.addVertex(0.027820, 0.056352);
	  r.addVertex(0.029211, 0.054212);
	  r.addVertex(0.027820, 0.052072);
	  r.addVertex(0.026429, 0.054212);
	  rs.add(r);
	  
	  r = new Region("Region_10");
	  r.addVertex(0.024111, 0.010700);
	  r.addVertex(0.025502, 0.008560);
	  r.addVertex(0.024111, 0.006420);
	  r.addVertex(0.022720, 0.008560);
	  rs.add(r);
	  
	  r = new Region("Region_11");
	  r.addVertex(0.024111, 0.004993);
	  r.addVertex(0.025502, 0.002853);
	  r.addVertex(0.024111, 0.000713);
	  r.addVertex(0.022720, 0.002853);
	  rs.add(r);
	  
	  r = new Region("Region_12");
	  r.addVertex(0.031529, 0.016406);
	  r.addVertex(0.032920, 0.014266);
	  r.addVertex(0.031529, 0.012126);
	  r.addVertex(0.030138, 0.014266);
	  rs.add(r);
	  
	  r = new Region("Region_13");
	  r.addVertex(0.005564, 0.033526);
	  r.addVertex(0.006955, 0.031386);
	  r.addVertex(0.005564, 0.029246);
	  r.addVertex(0.004173, 0.031386);
	  rs.add(r);
	  
	  r = new Region("Region_14");
	  r.addVertex(0.031529, 0.050645);
	  r.addVertex(0.032920, 0.048505);
	  r.addVertex(0.031529, 0.046365);
	  r.addVertex(0.030138, 0.048505);
	  rs.add(r);
	  
	  r = new Region("Region_15");
	  r.addVertex(0.005564, 0.039232);
	  r.addVertex(0.006955, 0.037092);
	  r.addVertex(0.005564, 0.034952);
	  r.addVertex(0.004173, 0.037092);
	  rs.add(r);
	  
	  r = new Region("Region_16");
	  r.addVertex(0.016692, 0.022113);
	  r.addVertex(0.018083, 0.019973);
	  r.addVertex(0.016692, 0.017833);
	  r.addVertex(0.015301, 0.019973);
	  rs.add(r);
	  
	  r = new Region("Region_17");
	  r.addVertex(0.005564, 0.004993);
	  r.addVertex(0.006955, 0.002853);
	  r.addVertex(0.005564, 0.000713);
	  r.addVertex(0.004173, 0.002853);
	  rs.add(r);
	  
	  r = new Region("Region_18");
	  r.addVertex(0.035239, 0.044939);
	  r.addVertex(0.036630, 0.042799);
	  r.addVertex(0.035239, 0.040659);
	  r.addVertex(0.033848, 0.042799);
	  rs.add(r);
	  
	  r = new Region("Region_19");
	  r.addVertex(0.020401, 0.016406);
	  r.addVertex(0.021792, 0.014266);
	  r.addVertex(0.020401, 0.012126);
	  r.addVertex(0.019010, 0.014266);
	  rs.add(r);
	  
	  r = new Region("Region_20");
	  r.addVertex(0.031529, 0.027819);
	  r.addVertex(0.032920, 0.025679);
	  r.addVertex(0.031529, 0.023539);
	  r.addVertex(0.030138, 0.025679);
	  rs.add(r);
	  
	  return rs;
	}
	

	/** Regions in experiment A */
	public static List<Region> MakeExperimentARegions_old(LoggerI logger) {
		List<Region> rs = new ArrayList<Region>();
		Region r;

		// Vassar St
		r = new Region("Vassar-1");
		r.addVertex(42.36255147026933, -71.09034599930573);
		r.addVertex(42.36240877523236, -71.08975591332245);
		r.addVertex(42.36013353836458, -71.09434785515595);
		r.addVertex(42.360442721730834, -71.0948091951065);
		rs.add(r);

		// Windsor-1
		r = new Region("Windsor-1");
		r.addVertex(42.36302711805193, -71.09707297951508);
		r.addVertex(42.36297955343571, -71.09641852051544);
		r.addVertex(42.3615288153431, -71.09657945305634);
		r.addVertex(42.36186970216797, -71.09723391205597);
		rs.add(r);

		// Mass-1
		r = new Region("Mass-1");
		r.addVertex(42.362678310030105, -71.0995620694809);
		r.addVertex(42.3629954083118, -71.09918656021881);
		r.addVertex(42.36179042632724, -71.09720172554779);
		r.addVertex(42.361322696830854, -71.09736265808868);
		rs.add(r);

		// Mass-2
		r = new Region("Mass-2");
		r.addVertex(42.36114036066024, -71.09588207871246);
		r.addVertex(42.360791542163774, -71.09660091072845);
		r.addVertex(42.36106901157985, -71.0969335046463);
		r.addVertex(42.36156052582344, -71.09657945305634);
		rs.add(r);

		// Mass-3
		r = new Region("Mass-3");
		r.addVertex(42.36035551632001, -71.09489502579498);
		r.addVertex(42.3601731773427, -71.09523834854889);
		r.addVertex(42.360577493491306, -71.095978638237);
		r.addVertex(42.36077568673155, -71.0955816713028);
		rs.add(r);

		/*
		 * Albany-1-full r = new Region("Albany-1");
		 * r.addVertex(42.36087874696942, -71.09530272156525);
		 * r.addVertex(42.361227564981775, -71.0956353154831);
		 * r.addVertex(42.362678310030105, -71.092556139534);
		 * r.addVertex(42.362527687785665, -71.09185876519012); rs.add(r);
		 */

		// Albany-1
		r = new Region("Albany-1");
		r.addVertex(42.36172700558263, -71.09442295700836);
		r.addVertex(42.3614891772202, -71.09410109192658);
		r.addVertex(42.360823253016186, -71.09553875595856);
		r.addVertex(42.361084866938036, -71.09590353638458);
		rs.add(r);

		// Albany-2
		r = new Region("Albany-2");
		r.addVertex(42.362678310030105, -71.09243812233734);
		r.addVertex(42.36253561528121, -71.09191240937042);
		r.addVertex(42.36180628150339, -71.09342517525482);
		r.addVertex(42.36223436974708, -71.09344663292694);
		rs.add(r);

		// Portland-1
		r = new Region("Portland-1");
		r.addVertex(42.362757584750575, -71.09386505753326);
		r.addVertex(42.36273380234492, -71.09342517525482);
		r.addVertex(42.36217887699113, -71.09354319245148);
		r.addVertex(42.36198861574153, -71.09409036309052);
		rs.add(r);

		// Main-2
		r = new Region("Main-1");
		r.addVertex(42.36321737615673, -71.09918656021881);
		r.addVertex(42.36356618118581, -71.09917583138275);
		r.addVertex(42.36342348845344, -71.0969335046463);
		r.addVertex(42.363042972916034, -71.09699787766266);
		rs.add(r);

		// Main-2
		r = new Region("Main-2");
		r.addVertex(42.36318566651262, -71.09384359986115);
		r.addVertex(42.36278929461076, -71.09392943054962);
		r.addVertex(42.36297162599619, -71.09643997818756);
		r.addVertex(42.36336799674776, -71.09641852051544);
		rs.add(r);

		// Main-3
		r = new Region("Main-3");
		r.addVertex(42.36300333574834, -71.09216990143585);
		r.addVertex(42.36271794740286, -71.09249176651764);
		r.addVertex(42.36277343968266, -71.09333934456635);
		r.addVertex(42.363106392332284, -71.09324278504181);
		rs.add(r);

		// Main-4
		r = new Region("Main-4");
		r.addVertex(42.36289235154579, -71.09035672814178);
		r.addVertex(42.36259110772208, -71.09038891464996);
		r.addVertex(42.36264660011392, -71.09166564614105);
		r.addVertex(42.36303504548448, -71.09157981545258);
		rs.add(r);

		return rs;
	}
	
	public static void TakeExperimentARegions_old(List<Region> regionSet, LoggerI logger) {
		Location l;

		l = new Location("");
		l.setLatitude(42.36035940296916);
		l.setLongitude(-71.0944926738739);
		logger.log(String.format("Test point on Vassar is in region %s",
				RoadRunnerService.GetRegion(regionSet, l)));

		l = new Location("");
		l.setLatitude(42.36081921192526);
		l.setLongitude(-71.09338760375977);
		logger.log(String.format("Test point on Vassar is in region %s",
				RoadRunnerService.GetRegion(regionSet, l)));

		l = new Location("");
		l.setLatitude(42.36160405047349);
		l.setLongitude(-71.0919177532196);
		logger.log(String.format("Test point on Vassar is in region %s",
				RoadRunnerService.GetRegion(regionSet, l)));

		l = new Location("");
		l.setLatitude(42.3619370093201);
		l.setLongitude(-71.09123110771179);
		logger.log(String.format("Test point on Vassar is in region %s",
				RoadRunnerService.GetRegion(regionSet, l)));

		l = new Location("");
		l.setLatitude(42.36234924163794);
		l.setLongitude(-71.09039425849915);
		logger.log(String.format("Test point on Vassar is in region %s",
				RoadRunnerService.GetRegion(regionSet, l)));

		l = new Location("");
		l.setLatitude(42.3631736981596);
		l.setLongitude(-71.09626293182373);
		logger.log(String.format("Test point on Main-1 is in region %s",
				RoadRunnerService.GetRegion(regionSet, l)));

		l = new Location("");
		l.setLatitude(42.36303893196785);
		l.setLongitude(-71.09436392784119);
		logger.log(String.format("Test point on Main-1 is in region %s",
				RoadRunnerService.GetRegion(regionSet, l)));

		l = new Location("");
		l.setLatitude(42.362935875273244);
		l.setLongitude(-71.09288334846497);
		logger.log(String.format("Test point on Main-2 is in region %s",
				RoadRunnerService.GetRegion(regionSet, l)));

		l = new Location("");
		l.setLatitude(42.362785253646265);
		l.setLongitude(-71.09100580215454);
		logger.log(String.format("Test point on Main-3 is in region %s",
				RoadRunnerService.GetRegion(regionSet, l)));

		l = new Location("");
		l.setLatitude(42.362476081807);
		l.setLongitude(-71.0936987400055);
		logger.log(String.format("Test point on Portland-1 is in region %s",
				RoadRunnerService.GetRegion(regionSet, l)));

		l = new Location("");
		l.setLatitude(42.36099362133876);
		l.setLongitude(-71.09561920166016);
		logger.log(String.format("Test point on Albany-1 is in region %s",
				RoadRunnerService.GetRegion(regionSet, l)));

		l = new Location("");
		l.setLatitude(42.36154855716084);
		l.setLongitude(-71.0943853855133);
		logger.log(String.format("Test point on Albany-1 is in region %s",
				RoadRunnerService.GetRegion(regionSet, l)));

		l = new Location("");
		l.setLatitude(42.362008357414815);
		l.setLongitude(-71.093430519104);
		logger.log(String.format("Test point on Albany-2 is in region %s",
				RoadRunnerService.GetRegion(regionSet, l)));

		l = new Location("");
		l.setLatitude(42.362610849206014);
		l.setLongitude(-71.09221816062927);
		logger.log(String.format("Test point on Albany-2 is in region %s",
				RoadRunnerService.GetRegion(regionSet, l)));

		l = new Location("");
		l.setLatitude(42.3611521749309);
		l.setLongitude(-71.09653115272522);
		logger.log(String.format("Test point on Mass-1 is in region %s",
				RoadRunnerService.GetRegion(regionSet, l)));

		l = new Location("");
		l.setLatitude(42.3604862471552);
		l.setLongitude(-71.09537243843079);
		logger.log(String.format("Test point on Mass-2 is in region %s",
				RoadRunnerService.GetRegion(regionSet, l)));

		l = new Location("");
		l.setLatitude(42.36238887921827);
		l.setLongitude(-71.09683156013489);
		logger.log(String.format("Test point on Windsor-1 is in region %s",
				RoadRunnerService.GetRegion(regionSet, l)));
	}
}
