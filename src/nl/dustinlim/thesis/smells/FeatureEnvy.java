package nl.dustinlim.thesis.smells;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;

import it.unisa.codeSmellAnalyzer.beans.ClassBean;
import it.unisa.codeSmellAnalyzer.beans.InvocationBean;
import it.unisa.codeSmellAnalyzer.beans.MethodBean;
import it.unisa.codeSmellAnalyzer.parser.ParseUtil;
import nl.dustinlim.thesis.CustomMetrics;
import nl.dustinlim.thesis.DetectionPerformance;
import nl.dustinlim.thesis.ThesisTreeMap;

public class FeatureEnvy extends Smell {

	final boolean COUNTING_UNIQUE_ENTITIES = false;

	private String projectID;
	private ClassBean classBean;
	private MethodBean methodBean;

	private List<String> callsInternal;
	private ThesisTreeMap callsExternal;
	private ThesisTreeMap callsIgnored;	

	private List<String> fieldsInternal;
	private ThesisTreeMap fieldsExternal;	
	private ThesisTreeMap fieldsIgnored;
		
	public FeatureEnvy() throws IOException {
		
		appendLineToLog(logOutputWeka, ",", new String[] {
				"internalClassReferences", 
				"externalClassMaxReferences", 
				"numberOfEnviedClasses",
				"parentClassInheritsFromAndroidClass",
				"jdeodorant", "jspirit", "taco", "oracle_smelly"
		});
		
		appendLineToLog(logOutputPreprocessor, ";", new String[] {
				"UID", "App", "Subject", "Envy", "Envied classes", "Internal", "External", "Ignored", "Terminal CMD"				
		});		
	}
	
	@Override
	protected void addParsedConstructToWekaFile(String parsedUid) {
		// Excluded from oracle.
		if (projectID.equals("WordPress")) {
			return;
		}
		
		ThesisTreeMap detectors = DetectionPerformance.featureEnvyDetectors;
		appendLineToLog(logOutputWeka, ",", new String[] {
				Integer.toString(internalEnvyValue()),
				Integer.toString(externalEnvyValue()),
				Integer.toString(getEnviedClasses().size()),
				Boolean.toString(CustomMetrics.classInheritsFromAndroidClass(classBean)),
				Boolean.toString(ParseUtil.listContainsUID(detectors.get(DetectionPerformance.DETECTOR_JDEODORANT), parsedUid)),
				Boolean.toString(ParseUtil.listContainsUID(detectors.get(DetectionPerformance.DETECTOR_JSPIRIT), parsedUid)),
				Boolean.toString(ParseUtil.listContainsUID(detectors.get(DetectionPerformance.DETECTOR_TACO), parsedUid)),
				Boolean.toString(ParseUtil.listContainsUID(DetectionPerformance.featureEnvyOracle, parsedUid))
		});
	}

	public void setup(String projectID) {
		this.projectID = projectID;
		smellCount = 0;
	}
	
	@Override
	public void setInput(List<Object> parameters) {
		classBean = (ClassBean) parameters.get(0);
		methodBean = (MethodBean) parameters.get(1);
		callsInternal = new ArrayList<String>();
		callsExternal = new ThesisTreeMap();
		callsIgnored = new ThesisTreeMap();
		fieldsInternal = new ArrayList<String>();
		fieldsExternal = new ThesisTreeMap();
		fieldsIgnored =  new ThesisTreeMap();
	}
	
	@Override
	public boolean passesFilter() {
		boolean smellDetected = false;
		
		// Excluded from oracle.
		if (projectID.equals("WordPress")) {
			return smellDetected;
		}
		
		classifyFields();
		classifyCalls();

		// Sort for consistent output files
		Collections.sort(fieldsInternal);
		Collections.sort(callsInternal);		
		sortMap(fieldsExternal);
		sortMap(callsExternal);
		sortMap(fieldsIgnored);
		sortMap(callsIgnored);
		
		smellDetected = (externalEnvyValue() > internalEnvyValue());
		if (smellDetected) {
			smellCount++;
			
			Map<String, List<String>> enviedClasses = getEnviedClasses();
			sortMap(enviedClasses);			

			List<String> internals = new ArrayList<>();
			internals.addAll(fieldsInternal);
			internals.addAll(callsInternal);			
			ThesisTreeMap externals = ThesisTreeMap.mergeMaps(fieldsExternal, callsExternal);
			ThesisTreeMap ignored = ThesisTreeMap.mergeMaps(fieldsIgnored, callsIgnored);
			
			String[] output = new String[] {
					getUID(),
					projectID,
					classBean.getName() + "." + methodBean.getName(),
					internalEnvyValue() + "|" + externalEnvyValue(),
					String.format("\"%s: %s\"", enviedClasses.size(), enviedClasses.toString()),
					String.format("\"%s: %s\"", internals.size(), internals.toString()),
					String.format(
							"\"%s|%s: %s\"", 
							sizeOfMap(fieldsExternal), 
							sizeOfMap(callsExternal), 
							externals.toString()
					),
					String.format(
							"\"%s|%s: %s\"", 
							sizeOfMap(fieldsIgnored), 
							sizeOfMap(callsIgnored), 
							ignored.toString()
					),
					generateTerminalCommand(classBean, methodBean)
			};
			logOutputPreprocessor.append(String.join(";", output) + System.lineSeparator());
		}									
		
		return smellDetected;
	}

	private void classifyFields() {
		for (IVariableBinding field : methodBean.getReferencedFields()) {			
			ITypeBinding declaringClass = field.getDeclaringClass();			
			if (declaringClass != null) {
				String declaringClassName = declaringClass.getQualifiedName();
				if (declaringClassName.equals(methodBean.getBelongingClassName())) {
					fieldsInternal.add(field.getName());
				}
				else if (!ignoredExternalClass(declaringClassName)) {
					fieldsExternal.append(declaringClassName, Arrays.asList(field.getName()));
				}				
				else {
					// Type was explicitly ignored
					fieldsIgnored.append(declaringClassName, Arrays.asList(field.getName()));					
				}
			}
			else {
				// Type was unresolved
				fieldsIgnored.append("(?)", Arrays.asList(field.getName()));
			}
		}		
	}
	
	private void classifyCalls() {
		for (InvocationBean methodCall : methodBean.getInvocations()) {
			String methodID = String.format("%s(*)", methodCall.getName());

			if (methodCall.getBelongingClassName() != null) {
				if (methodCall.getBelongingClassName().equals(methodBean.getBelongingClassName())) {
					callsInternal.add(methodID);
				}
				else if (!ignoredExternalClass(methodCall.getBelongingClassName())) {
					callsExternal.append(methodCall.getBelongingClassName(), Arrays.asList(methodID));
				}
				else {
					// Type was explicitly ignored
					callsIgnored.append(methodCall.getBelongingClassName(), Arrays.asList(methodID));
				}
			}
			else {
				// Type was unresolved
				callsIgnored.append("(?)", Arrays.asList(methodID));
			}
		}		
	}
	
	private boolean ignoredExternalClass(String className) {
		// The following namespaces should not count towards external envy.
		return (className.startsWith("java.") 
				|| className.startsWith("javax.")
				|| className.startsWith("android"));
	}
	
	private int internalEnvyValue() {
		if (COUNTING_UNIQUE_ENTITIES) {
			return new HashSet<String>(fieldsInternal).size() + new HashSet<String>(callsInternal).size();
		}
		else {
			return fieldsInternal.size() + callsInternal.size();			
		}
	}
	
	private int externalEnvyValue() {
		int largestInterest = 0;
		for (Map.Entry<String, List<String>> entry : getEnviedClasses().entrySet()) {
			int interest;
			if (COUNTING_UNIQUE_ENTITIES) {
				interest = new HashSet<String>(entry.getValue()).size();
			}
			else {
				interest = entry.getValue().size();
			}
			if (interest > largestInterest) {
				largestInterest = interest; 
			}
		}		
		return largestInterest;
	}
		
	private ThesisTreeMap getEnviedClasses() {
		ThesisTreeMap externals = ThesisTreeMap.mergeMaps(fieldsExternal, callsExternal);				

		externals.entrySet().removeIf(entry -> entry.getValue().size() <= internalEnvyValue());
		
		return externals;
	}
	
	private int sizeOfMap(Map<String, List<String>> map) {
		int size = 0;
		for (List<String> value : map.values()) {
			size += value.size();
		}
		return size;
	}
	
	private void sortMap(Map<String, List<String>> map) {
		map.entrySet().forEach(entry -> Collections.sort(entry.getValue()));
	}

	@Override
	public String getUID() {
		return String.format("FE-%s", methodBean.getUID());
	}
}
