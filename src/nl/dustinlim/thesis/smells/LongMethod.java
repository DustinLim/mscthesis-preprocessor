package nl.dustinlim.thesis.smells;

import java.io.IOException;
import java.util.List;

import it.unisa.codeSmellAnalyzer.beans.ClassBean;
import it.unisa.codeSmellAnalyzer.beans.MethodBean;
import it.unisa.codeSmellAnalyzer.metrics.SizeMetrics;
import it.unisa.codeSmellAnalyzer.parser.ParseUtil;
import nl.dustinlim.thesis.DetectionPerformance;
import nl.dustinlim.thesis.ThesisTreeMap;
import nl.dustinlim.thesis.CustomMetrics;

public class LongMethod extends Smell {
	private String projectID;
	private ClassBean classBean;
	private MethodBean methodBean;

	public LongMethod() throws IOException {
		appendLineToLog(logOutputWeka, ",", new String[] {
				"locOfLargestCodeBlock",
				"classInheritsFromAndroidClass",
				"decor", "jdeodorant", "taco", "oracle_smelly"
		});
		
		appendLineToLog(logOutputPreprocessor, ";", new String[] {
				"UID", "App name", "Terminal CMD", "Subject", "LOC"
		});
	}
	
	@Override
	protected void addParsedConstructToWekaFile(String parsedUid) {
		ThesisTreeMap detectors = DetectionPerformance.longMethodDetectors;
		
		appendLineToLog(logOutputWeka, ",", new String[] {
				Integer.toString(CustomMetrics.locOfLargestBlock(methodBean)),
				Boolean.toString(CustomMetrics.classInheritsFromAndroidClass(classBean)),
				Boolean.toString(ParseUtil.listContainsUID(detectors.get(DetectionPerformance.DETECTOR_DECOR), parsedUid)),
				Boolean.toString(ParseUtil.listContainsUID(detectors.get(DetectionPerformance.DETECTOR_JDEODORANT), parsedUid)),
				Boolean.toString(ParseUtil.listContainsUID(detectors.get(DetectionPerformance.DETECTOR_TACO), parsedUid)),
				Boolean.toString(ParseUtil.listContainsUID(DetectionPerformance.longMethodOracle, parsedUid))
		});		
	}

	public void setup(String projectID) {
		this.projectID = projectID;
		smellCount = 0;
	}

	@Override
	public void setInput(List<Object> parameters) {
		this.classBean = (ClassBean) parameters.get(0);
		this.methodBean = (MethodBean) parameters.get(1);
	}

	@Override
	public boolean passesFilter() {
		boolean smellDetected = false;
		
		int methodLOC = SizeMetrics.getLOC(methodBean);
		consoleOutput.append(methodLOC + " ");

		if (methodLOC > 80) {
			smellDetected = true;
			smellCount++;

			String[] output = new String[] {
					getUID(),
					projectID,
					generateTerminalCommand(classBean, methodBean),
					classBean.getName() + "." + methodBean.getName(),
					Integer.toString(methodLOC)
			};
			logOutputPreprocessor.append(String.join(";", output) + System.lineSeparator());
		}		
		
		return smellDetected;
	}

	@Override
	public String getUID() {
		return String.format("LM-%s", methodBean.getUID());
	}

}
