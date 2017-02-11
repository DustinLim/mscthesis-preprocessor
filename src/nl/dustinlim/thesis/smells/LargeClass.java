package nl.dustinlim.thesis.smells;

import java.io.IOException;
import java.util.List;

import it.unisa.codeSmellAnalyzer.beans.ClassBean;
import it.unisa.codeSmellAnalyzer.metrics.SizeMetrics;
import it.unisa.codeSmellAnalyzer.parser.ParseUtil;
import nl.dustinlim.thesis.DetectionPerformance;
import nl.dustinlim.thesis.ThesisTreeMap;
import nl.dustinlim.thesis.CustomMetrics;

public class LargeClass extends Smell {
	private String projectID;
	private ClassBean classBean;
		
	public LargeClass() throws IOException {
		appendLineToLog(logOutputWeka, ",", new String[] {
				"loc", "noa", "nom", 
				"totalLOCOfInnerClasses", 
				"numberOfAnnotatedMethodOverrides",
				"classInheritsFromAndroidClass",
				"decor", "jdeodorant", "jspirit", "oracle_smelly"
		});

		appendLineToLog(logOutputPreprocessor, ";", new String[] {
				"UID", "App name", "Terminal CMD", "Subject", "LOC", "NOA", "NOM"
		});
	}
	
	@Override
	protected void addParsedConstructToWekaFile(String parsedUid) {
		ThesisTreeMap detectors = DetectionPerformance.largeClassDetectors;
		
		appendLineToLog(logOutputWeka, ",", new String[] {
				Integer.toString(SizeMetrics.getLOC(classBean)),
				Integer.toString(SizeMetrics.getNOA(classBean)),
				Integer.toString(SizeMetrics.getNOM(classBean)),
				Integer.toString(CustomMetrics.locSumOfInnerClasses(classBean)),
				Integer.toString(CustomMetrics.numberOfMethodOverrideAnnotations(classBean)),
				Boolean.toString(CustomMetrics.classInheritsFromAndroidClass(classBean)),
				Boolean.toString(ParseUtil.listContainsUID(detectors.get(DetectionPerformance.DETECTOR_DECOR), parsedUid)),
				Boolean.toString(ParseUtil.listContainsUID(detectors.get(DetectionPerformance.DETECTOR_JDEODORANT), parsedUid)),
				Boolean.toString(ParseUtil.listContainsUID(detectors.get(DetectionPerformance.DETECTOR_JSPIRIT), parsedUid)),
				Boolean.toString(ParseUtil.listContainsUID(DetectionPerformance.largeClassOracle, parsedUid))
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

		int classLOC = SizeMetrics.getLOC(classBean);
		consoleOutput.append(classLOC + " ");

		if (classLOC > 438) {
			smellDetected = true;
			smellCount++;
			String[] output = new String[] {
					getUID(),
					projectID,
					"open '" + classBean.getParentFile().getAbsolutePath() + "'",
					classBean.getName(),
					Integer.toString(classLOC),
					Integer.toString(SizeMetrics.getNOA(classBean)),
					Integer.toString(SizeMetrics.getNOM(classBean))
			};
			logOutputPreprocessor.append(String.join(";", output) + System.lineSeparator());
		}				

		return smellDetected;
	}

	@Override
	public String getUID() {
		return String.format("LC-%s", classBean.getUID());
	}

}
