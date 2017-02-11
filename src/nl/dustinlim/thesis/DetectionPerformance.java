package nl.dustinlim.thesis;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;

import it.unisa.codeSmellAnalyzer.parser.ParseUtil;
import nl.dustinlim.thesis.smells.Smell;

public class DetectionPerformance {
	public static final String DETECTOR_JDEODORANT = "JDeodorant";
	public static final String DETECTOR_JSPIRIT = "JSpIRIT";
	public static final String DETECTOR_TACO = "TACO";
	public static final String DETECTOR_DECOR = "DECOR";

	private static List<String> featureEnvyParsed;
	private static List<String> largeClassParsed;
	private static List<String> longMethodParsed;	
	private static List<String> messageChainsParsed;	
	private static List<String> spaghettiCodeParsed;	
	
	public static List<String> featureEnvyOracle;
	public static List<String> largeClassOracle;
	public static List<String> longMethodOracle;	
	public static List<String> messageChainsOracle;	
	public static List<String> spaghettiCodeOracle;	

	public static ThesisTreeMap featureEnvyDetectors;
	public static ThesisTreeMap largeClassDetectors;
	public static ThesisTreeMap longMethodDetectors;
	public static ThesisTreeMap messageChainsDetectors;
	public static ThesisTreeMap spaghettiCodeDetectors;

	static {
		try {
			featureEnvyOracle = Smell.readAllLines("FE-oracle.txt");				
			featureEnvyDetectors = new ThesisTreeMap();			
			for (String line : Smell.readAllLines("FE-detectors.txt")) {
				String[] values = line.split("\\t");
				featureEnvyDetectors.append(values[0], values[1]);
			}
						
			largeClassOracle = Smell.readAllLines("LC-oracle.txt");			
			largeClassDetectors = new ThesisTreeMap();
			for (String line : Smell.readAllLines("LC-detectors.txt")) {
				String[] values = line.split("\\t");
				largeClassDetectors.append(values[0], values[1]);
			}
			
			longMethodOracle = Smell.readAllLines("LM-oracle.txt");
			longMethodDetectors = new ThesisTreeMap();
			for (String line : Smell.readAllLines("LM-detectors.txt")) {
				String[] values = line.split("\\t");
				longMethodDetectors.append(values[0], values[1]);
			}
			
			messageChainsOracle = Smell.readAllLines("MC-oracle.txt");
			messageChainsDetectors = new ThesisTreeMap();
			for (String line : Smell.readAllLines("MC-detectors.txt")) {
				String[] values = line.split("\\t");
				messageChainsDetectors.append(values[0], values[1]);
			}
			
			spaghettiCodeOracle = Smell.readAllLines("SC-oracle.txt");
			spaghettiCodeDetectors = new ThesisTreeMap();
			for (String line : Smell.readAllLines("SC-detectors.txt")) {
				String[] values = line.split("\\t");
				spaghettiCodeDetectors.append(values[0], values[1]);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void loadParsed() {		
		try {
			featureEnvyParsed = Smell.readAllLines("FE-parsed.txt");
			ParseUtil.checkConsistencyOfIdentifiers(featureEnvyOracle, featureEnvyParsed);
			ParseUtil.checkConsistencyOfIdentifiers(featureEnvyDetectors, featureEnvyParsed);
			
			largeClassParsed = Smell.readAllLines("LC-parsed.txt");				
			ParseUtil.checkConsistencyOfIdentifiers(largeClassOracle, largeClassParsed);
			ParseUtil.checkConsistencyOfIdentifiers(largeClassDetectors, largeClassParsed);

			longMethodParsed = Smell.readAllLines("LM-parsed.txt");				
			ParseUtil.checkConsistencyOfIdentifiers(longMethodOracle, longMethodParsed);
			ParseUtil.checkConsistencyOfIdentifiers(longMethodDetectors, longMethodParsed);
			
			messageChainsParsed = Smell.readAllLines("MC-parsed.txt");				
			ParseUtil.checkConsistencyOfIdentifiers(messageChainsOracle, messageChainsParsed);
			ParseUtil.checkConsistencyOfIdentifiers(messageChainsDetectors, messageChainsParsed);

			spaghettiCodeParsed = Smell.readAllLines("SC-parsed.txt");	
			ParseUtil.checkConsistencyOfIdentifiers(spaghettiCodeOracle, spaghettiCodeParsed);
			ParseUtil.checkConsistencyOfIdentifiers(spaghettiCodeDetectors, spaghettiCodeParsed);

			System.out.println();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	
	public static void printPerformanceData() {
		loadParsed();
		
		printFeatureEnvyPerformance();
		System.out.println("\\midrule");
		printLargeClassPerformance();
		System.out.println("\\midrule");
		printLongMethodPerformance();
		System.out.println("\\midrule");
		printMessageChainsPerformance();
		System.out.println("\\midrule");
		printSpaghettiCodePerformance();		

		System.out.println();
		
		printOverlapHeader("Feature Envy");
		printOverlap(
				"JDeodorant $\\cup$ JSpIRIT", 
				featureEnvyDetectors.get(DETECTOR_JDEODORANT), 
				featureEnvyDetectors.get(DETECTOR_JSPIRIT)
		);
		printOverlap(
				"JDeodorant $\\cup$ TACO", 
				featureEnvyDetectors.get(DETECTOR_JDEODORANT), 
				featureEnvyDetectors.get(DETECTOR_TACO)
		);
		printOverlap(
				"JSpIRIT $\\cup$ TACO", 
				featureEnvyDetectors.get(DETECTOR_JSPIRIT), 
				featureEnvyDetectors.get(DETECTOR_TACO)
		);
		printOverlapHeader("Large Class");
		printOverlap(
				"DECOR $\\cup$ JDeodorant", 
				largeClassDetectors.get(DETECTOR_DECOR), 
				largeClassDetectors.get(DETECTOR_JDEODORANT)
		);
		printOverlap(
				"DECOR $\\cup$ JSpIRIT", 
				largeClassDetectors.get(DETECTOR_DECOR), 
				largeClassDetectors.get(DETECTOR_JSPIRIT)
		);
		printOverlap(
				"JDeodorant $\\cup$ JSpIRIT", 
				largeClassDetectors.get(DETECTOR_JDEODORANT), 
				largeClassDetectors.get(DETECTOR_JSPIRIT)
		);
		printOverlapHeader("Long Method");
		printOverlap(
				"DECOR $\\cup$ TACO", 
				longMethodDetectors.get(DETECTOR_DECOR), 
				longMethodDetectors.get(DETECTOR_TACO)
		);		
		
		System.out.println();
	}
	
	private static void printFeatureEnvyPerformance() {
		printPerformanceHeader("Feature Envy");
		
		List<String> jdeodorant = featureEnvyDetectors.get(DETECTOR_JDEODORANT);
		List<String> jspirit = featureEnvyDetectors.get(DETECTOR_JSPIRIT);
		List<String> taco = featureEnvyDetectors.get(DETECTOR_TACO);
		
		printPerformance("JDeodorant", jdeodorant, featureEnvyOracle);
		printPerformance("JSpIRIT", jspirit, featureEnvyOracle);
		printPerformance("TACO", taco, featureEnvyOracle);		
		/*
		printPerformance(
				"JDeodorant $\\cup$ JSpIRIT", 
				ListUtils.union(jdeodorant, jspirit), 
				featureEnvyOracle
		);		
		printPerformance(
				"JDeodorant $\\cap$ JSpIRIT", 
				ListUtils.intersection(jdeodorant, jspirit), 
				featureEnvyOracle
		);
		printPerformance(
				"JDeodorant $\\cup$ TACO", 
				ListUtils.union(jdeodorant, taco), 
				featureEnvyOracle
		);		
		printPerformance(
				"JDeodorant $\\cap$ TACO", 
				ListUtils.intersection(jdeodorant, taco), 
				featureEnvyOracle
		);
		printPerformance(
				"JSpIRIT $\\cup$ TACO", 
				ListUtils.union(jspirit, taco), 
				featureEnvyOracle
		);		
		printPerformance(
				"JSpIRIT $\\cap$ TACO", 
				ListUtils.intersection(jspirit, taco), 
				featureEnvyOracle
		);
		*/
	}
	
	private static void printLargeClassPerformance() {
		printPerformanceHeader("Large Class");

		List<String> decor = largeClassDetectors.get(DETECTOR_DECOR);
		List<String> jdeodorant = largeClassDetectors.get(DETECTOR_JDEODORANT);
		List<String> jspirit = largeClassDetectors.get(DETECTOR_JSPIRIT);
		
		printPerformance("DECOR", decor, largeClassOracle);
		printPerformance("JDeodorant", jdeodorant, largeClassOracle);		
		printPerformance("JSpIRIT", jspirit, largeClassOracle);
		/*
		printPerformance(
				"DECOR $\\cup$ JDeodorant", 
				ListUtils.union(decor, jdeodorant), 
				largeClassOracle
		);		
		printPerformance(
				"DECOR $\\cap$ JDeodorant", 
				ListUtils.intersection(decor, jdeodorant), 
				largeClassOracle
		);		
		printPerformance(
				"JDeodorant $\\cup$ JSpIRIT", 
				ListUtils.union(jdeodorant, jspirit), 
				largeClassOracle
		);		
		printPerformance(
				"JDeodorant $\\cap$ JSpIRIT", 
				ListUtils.intersection(jdeodorant, jspirit), 
				largeClassOracle
		);		
		printPerformance(
				"DECOR $\\cup$ JSpIRIT", 
				ListUtils.union(decor, jspirit), 
				largeClassOracle
		);		
		printPerformance(
				"DECOR $\\cap$ JSpIRIT", 
				ListUtils.intersection(decor, jspirit), 
				largeClassOracle
		);		
		*/
	}
	
	private static void printLongMethodPerformance() {
		printPerformanceHeader("Long Method");

		List<String> decor = longMethodDetectors.get(DETECTOR_DECOR);
		List<String> jdeodorant = longMethodDetectors.get(DETECTOR_JDEODORANT);
		List<String> taco = longMethodDetectors.get(DETECTOR_TACO);

		DetectionPerformance.printPerformance("DECOR", decor, longMethodOracle);
		DetectionPerformance.printPerformance("JDeodorant", jdeodorant, longMethodOracle);		
		DetectionPerformance.printPerformance("TACO", taco, longMethodOracle);
		
		/*
		DetectionPerformance.printPerformance(
				"DECOR $\\cup$ TACO", 
				ListUtils.union(decor, taco), 
				longMethodOracle
		);				
		DetectionPerformance.printPerformance(
				"DECOR $\\cap$ TACO", 
				ListUtils.intersection(decor, taco), 
				longMethodOracle
		);				
		*/
	}
	
	private static void printMessageChainsPerformance() {
		printPerformanceHeader("Message Chains");
		List<String> decor = messageChainsDetectors.get(DETECTOR_DECOR);
		DetectionPerformance.printPerformance("DECOR", decor, messageChainsOracle);				
	}

	private static void printSpaghettiCodePerformance() {
		printPerformanceHeader("Spaghetti Code");
		List<String> decor = spaghettiCodeDetectors.get(DETECTOR_DECOR);
		DetectionPerformance.printPerformance("DECOR", decor, spaghettiCodeOracle);				
	}

	private static void printPerformanceHeader(String detectorLabel) {
		System.out.println("\\textbf{" + detectorLabel + "} & & & & & & \\\\");
	}

	private static void printOverlapHeader(String detectorLabel) {
		System.out.println("\\textbf{" + detectorLabel + "} & \\\\");
	}

	private static void printPerformance(String label, List<String> detections, List<String> oracle) {
		if (detections == null) {
			detections = new ArrayList<>();
		}
		
		Set<String> detectionSet = new HashSet<>();
		for (String detection : detections) {
			if (!detectionSet.add(detection)) {
				//System.out.println("%[WARNING] Duplicate detection uid: " + detection);
			}				
		}
		
		System.out.println(String.format("%s & %.0f & %.0f & %.0f & %.2f & %.2f & %.2f \\\\",
				label,
				truePositives(detections, oracle),
				falsePositives(detections, oracle),
				falseNegatives(detections, oracle),	
				precision(detections, oracle),
				recall(detections, oracle),
				f1score(detections, oracle)
		));		
	}

	private static double truePositives(List<String> detections, List<String> oracle) {
		int truePositives = 0;
		
		// Forces unique identifiers..
		Set<String> detectionSet = new HashSet<>(detections);
		Set<String> oracleSet = new HashSet<>(oracle);
		
		for (String uid : detectionSet) {
			if (ParseUtil.setContainsUID(oracleSet, uid)) {
				truePositives++;
			}
		}
		
		return truePositives;
	}

	private static double falsePositives(List<String> detections, List<String> oracle) {
		int falsePositives = 0;
		
		// Forces unique identifiers..
		Set<String> detectionSet = new HashSet<>(detections);
		Set<String> oracleSet = new HashSet<>(oracle);
		
		for (String uid : detectionSet) {
			if (!ParseUtil.setContainsUID(oracleSet, uid)) {
				falsePositives++;
			}
		}		
		
		return falsePositives;
	}
	
	private static double falseNegatives(List<String> detections, List<String> oracle) {
		int falseNegatives = 0;
		
		// Forces unique identifiers..
		Set<String> detectionSet = new HashSet<>(detections);
		Set<String> oracleSet = new HashSet<>(oracle);

		for (String uid : oracleSet) {
			if (!ParseUtil.setContainsUID(detectionSet, uid)) {
				falseNegatives++;
			}
		}
		
		return falseNegatives;
	}

	private static double precision(List<String> detections, List<String> oracle) {
		double TP = truePositives(detections, oracle);
		double FP = falsePositives(detections, oracle);
		return TP/(TP+FP);
	}

	private static double recall(List<String> detections, List<String> oracle) {
		double TP = truePositives(detections, oracle);
		double FN = falseNegatives(detections, oracle);
		return TP/(TP+FN);
	}

	private static double f1score(List<String> detections, List<String> oracle) {
		double precision = precision(detections, oracle);
		double recall = recall(detections, oracle);
				
		return (precision * recall) / (precision + recall);
	}

	private static void printOverlap(String identifier, List<String> detector1, List<String> detector2) {
		double intersectCount = 0;
		
		// Forces unique identifiers..
		Set<String> detector1Set = new HashSet<>(detector1);
		Set<String> detector2Set = new HashSet<>(detector2);

		for (String uid : detector1Set) {
			if (ParseUtil.setContainsUID(detector2Set, uid)) {
				intersectCount += 1.0;
			}
		}

		double overlap = intersectCount / (detector1Set.size()+detector2Set.size());
		
		System.out.println(String.format("%s & %.2f\\\\", identifier, overlap));
	}

}
