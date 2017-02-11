package it.unisa.codeSmellAnalyzer.beans;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.MethodInvocation;

public class InvocationBean {
	private MethodInvocation node;
	private String uid;
	private String name;
	private boolean isChainedMethodCall;
	private String belongingClassName;
	private int lineNumber;

	public MethodInvocation getNode() {
		return node;
	}

	public void setNode(MethodInvocation node) {
		this.node = node;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUID() {
		return uid;
	}

	public void setUID(String uid) {
		this.uid = uid;
	}
	
	public boolean isChainedMethodCall() {
		return isChainedMethodCall;
	}

	public void setChainedMethodCall(boolean isChainedMethodCall) {
		this.isChainedMethodCall = isChainedMethodCall;
	}

	public String getBelongingClassName() {
		return belongingClassName;
	}

	public void setBelongingClassName(String belongingClassName) {
		this.belongingClassName = belongingClassName;
	}

	public int getLineNumber() {
		return lineNumber;
	}

	public void setLineNumber(int lineNumber) {
		this.lineNumber = lineNumber;
	}
	
	public String toString() {
		return node.toString();
	}
	
	public String toShortString() {		
		String shortString = "";
		ASTNode pointer = node;
		
		while (pointer instanceof MethodInvocation) {
			node = (MethodInvocation) pointer;
			shortString = "." + node.getName() + "( )" + shortString;
			pointer = node.getExpression();
		}
		
		return shortString;
	}

	public boolean isRootInvocation() {
		return !(node.getParent() instanceof MethodInvocation);
	}

}
