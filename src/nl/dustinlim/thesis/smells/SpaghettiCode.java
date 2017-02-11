package nl.dustinlim.thesis.smells;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import it.unisa.codeSmellAnalyzer.beans.ClassBean;
import it.unisa.codeSmellAnalyzer.beans.InstanceVariableBean;
import it.unisa.codeSmellAnalyzer.beans.MethodBean;
import it.unisa.codeSmellAnalyzer.metrics.SizeMetrics;
import it.unisa.codeSmellAnalyzer.parser.ParseUtil;
import nl.dustinlim.thesis.DetectionPerformance;
import nl.dustinlim.thesis.ThesisTreeMap;

public class SpaghettiCode extends Smell {
	private String projectID;
	private ClassBean classBean;
	
	public SpaghettiCode() throws IOException {		
		appendLineToLog(logOutputWeka, ",", new String[] {
				"decor", "oracle_smelly"
		});
		
		appendLineToLog(logOutputPreprocessor, ";", new String[] {
				"UID", "App name", "Subject", "Long Methods", "Shared Ivars", "Terminal CMD"
		});
	}
	
	@Override
	protected void addParsedConstructToWekaFile(String parsedUid) {
		ThesisTreeMap detectors = DetectionPerformance.spaghettiCodeDetectors;
		
		appendLineToLog(logOutputWeka, ",", new String[] {
				Boolean.toString(ParseUtil.listContainsUID(detectors.get(DetectionPerformance.DETECTOR_DECOR), parsedUid)),
				Boolean.toString(ParseUtil.listContainsUID(DetectionPerformance.spaghettiCodeOracle, parsedUid))
		});				
	}

	public void setup(String projectID) {
		this.projectID = projectID;
		smellCount = 0;
	}

	@Override
	public void setInput(List<Object> parameters) {
		this.classBean = (ClassBean) parameters.get(0);
	}

	@Override
	public boolean passesFilter() {
		boolean smellDetected = false;
		
		List<MethodBean> longMethods = new ArrayList<>();
		for (MethodBean methodBean : classBean.getMethods()) {
			int loc = SizeMetrics.getLOC(methodBean);
			if (loc > 80) {
				longMethods.add(methodBean);
			}
		}
		
		if (longMethods.size() >= 2) {
			smellDetected = true;
			smellCount++;
			
			String[] output = new String[] {
					getUID(),
					projectID,
					classBean.getName(),
					methodsToString(longMethods),
					sharedInstanceVariables(longMethods).toString(),
					"xed '" + classBean.getParentFile().getAbsolutePath() + "'"
			};
			logOutputPreprocessor.append(String.join(";", output) + System.lineSeparator());
			
			for (MethodBean methodBean : longMethods) {
				//foo(methodBean);
				//System.out.println();
			}
			//System.out.println("#########");
		}
								
		return smellDetected;
	}

	private String methodsToString(List<MethodBean> methods) {
		List<String> strings = new ArrayList<>();		
		for (MethodBean methodBean : methods) {
			strings.add(classBean.getName() + "." + methodBean.getName() + "()@LN" + methodBean.getLineNumber());
		}
		return strings.toString();
	}

	private void foo(MethodBean methodBean) {
		for (InstanceVariableBean ivarBean : methodBean.getUsedInstanceVariables()) {
			System.out.println(ivarBean.getName());
		}
	}

	private List<InstanceVariableBean> sharedInstanceVariables(List<MethodBean> methods) {
		List<InstanceVariableBean> sharedIvars = new ArrayList<>();
		
		List<InstanceVariableBean> combinedIvars = new ArrayList<>();
		for (MethodBean methodBean : methods) {
			combinedIvars.addAll(methodBean.getUsedInstanceVariables());
		}

		// Derive a set with unique elements
		Set<InstanceVariableBean> uniqueIvars = new HashSet<>(combinedIvars);
		for (InstanceVariableBean ivar : uniqueIvars) {
			if (Collections.frequency(combinedIvars, ivar) > 1) {
				sharedIvars.add(ivar);
			}
		}
		
		Collections.sort(sharedIvars);
		return sharedIvars;
	}

	@Override
	public String getUID() {
		return String.format("SC-%s", classBean.getUID());
	}

}
