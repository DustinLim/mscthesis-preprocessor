package nl.dustinlim.thesis;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import it.unisa.codeSmellAnalyzer.beans.ClassBean;
import it.unisa.codeSmellAnalyzer.beans.InvocationBean;
import it.unisa.codeSmellAnalyzer.beans.MethodBean;
import it.unisa.codeSmellAnalyzer.beans.PackageBean;
import it.unisa.codeSmellAnalyzer.computation.FileUtility;
import it.unisa.codeSmellAnalyzer.computation.FolderToJavaProjectConverter;
import nl.dustinlim.thesis.smells.FeatureEnvy;
import nl.dustinlim.thesis.smells.LargeClass;
import nl.dustinlim.thesis.smells.LongMethod;
import nl.dustinlim.thesis.smells.MessageChains;
import nl.dustinlim.thesis.smells.SpaghettiCode;

public class PreprocessorApp {
	private static final boolean RUN_PREPROCESSOR_STAGE = true;
	private static final boolean RUN_PERFORMANCE_MEASUREMENT_STAGE = true;
	
	private static List<File> directories = new ArrayList<>();
	private static List<String> identifiers = new ArrayList<>();
	
	private static int classesInProject = 0;
	private static int classesInAllProjects = 0;
	private static int methodsInProject = 0;
	private static int methodsInAllProjects = 0;
	private static int rootInvocationsInProject = 0;
	private static int rootInvocationsInAllProjects = 0;
		
	private static FeatureEnvy feDetector;
	private static LargeClass lcDetector;
	private static LongMethod lmDetector;
	private static SpaghettiCode scDetector;
	private static MessageChains mcDetector;
	
	public static void main(String[] args) {		
		System.out.println("[Preprocessor started..]");

		// Measure execution time
		long startTime = System.nanoTime();
		
		try {
			feDetector = new FeatureEnvy();
			lcDetector = new LargeClass();
			lmDetector = new LongMethod();
			scDetector = new SpaghettiCode();
			mcDetector = new MessageChains();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		if (RUN_PREPROCESSOR_STAGE) {
			addSourceProject("AmazeFileManager", "/Users/dustinlim/Downloads/Apps/AmazeFileManager/src/main/java");
			addSourceProject("Cool Reader", "/Users/dustinlim/Downloads/Apps/crengine-crengine/android/src/");
			addSourceProject("Focal", "/Users/dustinlim/Downloads/Apps/Focal/src/");
			addSourceProject("SMS Backup Plus", "/Users/dustinlim/Downloads/Apps/sms-backup-plus/src/main/java");		
			addSourceProject("Solitaire", "/Users/dustinlim/Downloads/Apps/Solitaire/trunk/src/");
			addSourceProject("WordPress", "/Users/dustinlim/Downloads/Apps/WordPress-Android/WordPress/src/main/java");			
		}

		// Detect candidates for the various code smells
		// Results are written to the logs.
		for (File project: directories) {
			if (!project.isDirectory()) {
				continue; //jump to the next loop
			}
			
			String projectID = identifiers.get(directories.indexOf(project));
			
			try {
				Vector<PackageBean> packages = FolderToJavaProjectConverter.convert(project.getAbsolutePath(), projectID);
								
				feDetector.setup(projectID);
				lcDetector.setup(projectID);
				lmDetector.setup(projectID);
				scDetector.setup(projectID);
				mcDetector.setup(projectID);

				for (PackageBean packageBean: packages) {					
					for (ClassBean classBean: packageBean.getClasses()) {
						preprocessClass(classBean, projectID);						
					}
				}

				//lcDetector.writeConsoleOutput(); //class LOC
				//lmDetector.writeConsoleOutput(); //method LOC
				//mcDetector.writeConsoleOutput(); //chain length
				printAppStatistics(packages);
			} 
			catch (Exception e) {
				e.printStackTrace();
			}	
		}

		if (RUN_PREPROCESSOR_STAGE) {
			writeLogs();
		}

		System.out.println("### Total classes parsed: " + classesInAllProjects);
		System.out.println("### Total methods parsed: " + methodsInAllProjects);
		System.out.println("### Total root invocations parsed: " + rootInvocationsInAllProjects);
		
		if (RUN_PERFORMANCE_MEASUREMENT_STAGE) {			
			System.out.println();
			DetectionPerformance.printPerformanceData();
		}
		DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
		System.out.println("[Preprocessor ready] @ " + dateFormat.format(new Date()));
		long elapsedTime = System.nanoTime() - startTime;
		System.out.println("Elapsed time: " + (elapsedTime / 1E9) + " s");		
	}

	protected static void preprocessClass(ClassBean classBean, String projectID) {
		classesInProject++;
		
		lcDetector.filterParsedConstruct(Arrays.asList(classBean));
		scDetector.filterParsedConstruct(Arrays.asList(classBean));

		for (ClassBean innerClass : classBean.getInnerClasses()) {							
			preprocessClass(innerClass, projectID);
		}

		for (MethodBean methodBean: classBean.getMethods()) {														
			methodsInProject++;							
			lmDetector.filterParsedConstruct(Arrays.asList(classBean, methodBean));
			feDetector.filterParsedConstruct(Arrays.asList(classBean, methodBean));

			for (InvocationBean invocationBean : methodBean.getRootInvocations()) {
				rootInvocationsInProject++;
				
				// (WordPress, Message Chain) combo is excluded
				//if (!projectID.equals("WordPress")) {
					mcDetector.filterParsedConstruct(Arrays.asList(classBean, methodBean, invocationBean));
				//}
			}
		}
	}

	public static void addSourceProject(String id, String path) {
		identifiers.add(id);		
		directories.add(new File(path));
	}

	public static void writeLogs() {
		FileUtility.writeFile(String.join(System.lineSeparator(), feDetector.parsedIdentifiers), "input/FE-parsed.txt");
		FileUtility.writeFile(String.join(System.lineSeparator(), lcDetector.parsedIdentifiers), "input/LC-parsed.txt");
		FileUtility.writeFile(String.join(System.lineSeparator(), lmDetector.parsedIdentifiers), "input/LM-parsed.txt");
		FileUtility.writeFile(String.join(System.lineSeparator(), mcDetector.parsedIdentifiers), "input/MC-parsed.txt");
		FileUtility.writeFile(String.join(System.lineSeparator(), scDetector.parsedIdentifiers), "input/SC-parsed.txt");
		
		feDetector.writeLogOutput("output/1-FeatureEnvy-prepped.csv", "output/1-FeatureEnvy-weka.csv");
		lcDetector.writeLogOutput("output/2-LargeClass-prepped.csv", "output/2-LargeClass-weka.csv");
		lmDetector.writeLogOutput("output/3-LongMethod-prepped.csv", "output/3-LongMethod-weka.csv");
		mcDetector.writeLogOutput("output/4-MessageChains-prepped.csv", "output/4-MessageChains-weka.csv");
		scDetector.writeLogOutput("output/5-SpaghettiCode-prepped.csv", "output/5-SpaghettiCode-weka.csv");
	}
	
	public static void printAppStatistics(Vector<PackageBean> packages) {
		System.out.println("# Classes : " + classesInProject);
		System.out.println("# Methods : " + methodsInProject);
		System.out.println("# Invocations : " + rootInvocationsInProject);
		System.out.println("## Feature Envy: " + feDetector.getSmellCount());	
		System.out.println("## Large Class: " + lcDetector.getSmellCount());	
		System.out.println("## Long Method: " + lmDetector.getSmellCount());	
		System.out.println("## Message Chain: " + mcDetector.getSmellCount());	
		System.out.println("## Spaghetti Code: " + scDetector.getSmellCount());	
		System.out.println("");
		
		classesInAllProjects += classesInProject;
		classesInProject = 0;
		methodsInAllProjects += methodsInProject;
		methodsInProject = 0;
		rootInvocationsInAllProjects += rootInvocationsInProject;
		rootInvocationsInProject = 0;
	}
}
