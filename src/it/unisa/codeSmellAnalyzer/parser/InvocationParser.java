package it.unisa.codeSmellAnalyzer.parser;

import java.util.List;

import org.eclipse.jdt.core.dom.MethodInvocation;

import it.unisa.codeSmellAnalyzer.beans.InvocationBean;

public class InvocationParser {
	
	public static InvocationBean parse(List<Object> pInvocation) {
		InvocationBean invocationBean = new InvocationBean();
		invocationBean.setName((String)pInvocation.get(0));
		invocationBean.setChainedMethodCall((Boolean)pInvocation.get(1));
		invocationBean.setBelongingClassName((String)pInvocation.get(2));
		invocationBean.setLineNumber((Integer)pInvocation.get(3));
		invocationBean.setNode((MethodInvocation)pInvocation.get(4));
		return invocationBean;
	}

}
