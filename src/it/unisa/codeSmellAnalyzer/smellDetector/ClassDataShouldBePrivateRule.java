package it.unisa.codeSmellAnalyzer.smellDetector;

import it.unisa.codeSmellAnalyzer.beans.ClassBean;
import it.unisa.codeSmellAnalyzer.metrics.CKMetrics;

public class ClassDataShouldBePrivateRule {

	public boolean isClassDataShouldBePrivate(ClassBean pClass) {
		
		if(CKMetrics.getNOPA(pClass) > 10)
			return true;
		
		return false;
	}
}
