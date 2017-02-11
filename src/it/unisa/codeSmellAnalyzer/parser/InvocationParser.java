package it.unisa.codeSmellAnalyzer.parser;

import it.unisa.codeSmellAnalyzer.beans.MethodBean;

public class InvocationParser {
	
	public static MethodBean parse(String pInvocationName) {
		MethodBean methodBean = new MethodBean();
		methodBean.setName(pInvocationName);
		return methodBean;
	}

}
