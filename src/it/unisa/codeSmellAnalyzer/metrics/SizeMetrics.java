package it.unisa.codeSmellAnalyzer.metrics;

import java.util.Collection;

import it.unisa.codeSmellAnalyzer.beans.ClassBean;
import it.unisa.codeSmellAnalyzer.beans.InstanceVariableBean;
import it.unisa.codeSmellAnalyzer.beans.MethodBean;

public class SizeMetrics {

	/**
	 * The number of lines of code.
	 */
	public static int getLOC(ClassBean cb){
		return getLOC(cb.getTextContent());
	}
	public static int getLOC(MethodBean mb){
		return getLOC(mb.getTextContent());
	}
	public static int getLOC(String text){
		return text.split("\r\n|\r|\n").length;
	}

	/**
	 * Returns the number of methods.
	 */
	public static int getNOM(ClassBean cb){
		return cb.getMethods().size();
	}

	/**
	 * Returns the number of attributes.
	 */
	public static int getNOA(ClassBean cb){
		return cb.getInstanceVariables().size();
	}

	/**
	 * Returns the number of public attributes.
	 */
	public static int getNOPA(ClassBean cb) {
		int publicVariable=0;
	
		Collection<InstanceVariableBean> variables = cb.getInstanceVariables();
	
		for(InstanceVariableBean variable: variables) {
			if(variable.getVisibility().equals("public"))
				publicVariable++;
		}
	
		return publicVariable;
	}

	/**
	 * Returns the number of private attributes.
	 */
	public static int getNOPrivateA(ClassBean cb) {
		int privateVariable=0;
	
		Collection<InstanceVariableBean> variables = cb.getInstanceVariables();
	
		for(InstanceVariableBean variable: variables) {
			if(variable.getVisibility().equals("private"))
				privateVariable++;
		}
	
		return privateVariable;
	}

}
