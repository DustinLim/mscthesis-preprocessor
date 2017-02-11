package it.unisa.codeSmellAnalyzer.smellDetector;

import it.unisa.codeSmellAnalyzer.beans.ClassBean;
import it.unisa.codeSmellAnalyzer.metrics.CKMetrics;

public class ComplexClassRule {

	public boolean isComplexClass(ClassBean pClass) {

		if(CKMetrics.getWMC(pClass) > 200)
				return true;

		return false;
	}
}