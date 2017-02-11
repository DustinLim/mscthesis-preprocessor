package it.unisa.codeSmellAnalyzer.beans;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Vector;

import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Type;

public class MethodBean {
	
	/**
	 * The Eclipse JDT ast node this class wraps around.
	 */
	private MethodDeclaration astNode;
	private String uid;

	private String belongingClassName;
	private String name;
	private String textContent;
	private Collection<InstanceVariableBean> usedInstanceVariables;
	private Collection<InvocationBean> invocations;
	private List<IVariableBinding> referencedFields;
	private boolean isChainedMethodCall;
	private Type returnType;
	private List<SingleVariableDeclaration> parameters;
	private int lineNumber;	
		
	public MethodBean(MethodDeclaration astNode) {
		this.astNode = astNode;
		usedInstanceVariables = new Vector<InstanceVariableBean>();
		invocations = new Vector<InvocationBean>();
	}
	
	public MethodDeclaration getAstNode() {
		return astNode;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String pName) {
		name = pName;
	}

	public String getUID() {
		return uid;
	}
	
	public void setUID(String pUid) {
		uid = pUid;
	}

	public String getTextContent() {
		return textContent;
	}
	
	public void setTextContent(String pTextContent) {
		textContent = pTextContent;
	}
	
	public Collection<InstanceVariableBean> getUsedInstanceVariables() {
		return usedInstanceVariables;
	}
	
	public void setUsedInstanceVariables(Collection<InstanceVariableBean> pUsedInstanceVariables) {
		usedInstanceVariables = pUsedInstanceVariables;
	}
	
	public void addUsedInstanceVariables(InstanceVariableBean pInstanceVariable) {
		usedInstanceVariables.add(pInstanceVariable);
	}
	
	public void removeUsedInstanceVariables(InstanceVariableBean pInstanceVariable) {
		usedInstanceVariables.remove(pInstanceVariable);
	}

	public boolean isChainedMethodCall() {
		return isChainedMethodCall;
	}
	
	public void setChainedMethodCall(boolean b) {
		isChainedMethodCall = b;
	}
	
	public Collection<InvocationBean> getInvocations() {
		return invocations;
	}

	public void setInvocations(Collection<InvocationBean> invocations) {
		this.invocations = invocations;
	}

	public Collection<InvocationBean> getRootInvocations() {
		Collection<InvocationBean> rootInvocations = new ArrayList<>();
		
		for (InvocationBean invocation : invocations) {
			if (invocation.isRootInvocation()) {
				rootInvocations.add(invocation);
			}
		}
		
		return rootInvocations;
	}
	
	public List<IVariableBinding> getReferencedFields() {
		return referencedFields;
	}

	public void setReferencedFields(List<IVariableBinding> fields) {
		this.referencedFields = fields;
	}

	public String toString() {
		
		String string =
			"(" + name + "|" +
			(textContent.length() > 10 ? textContent.replace("\n", " ").replace("\t", "").substring(0, 10).concat("...") : "") + "|";

		string += "line " + lineNumber + "|";
		
		for (InstanceVariableBean usedInstanceVariable : usedInstanceVariables)
			string += usedInstanceVariable.getName() + ",";
		string = string.substring(0, string.length() - 1);
		string += "|";
		
		for (InvocationBean methodCall : invocations)
			string += methodCall.getName() + ",";
		string = string.substring(0, string.length() - 1);
		string += ")";
		
		return string;
		
	}

	public int compareTo(Object o) {
		return this.getName().compareTo(((MethodBean)o).getName());
	}

	public Type getReturnType() {
		return returnType;
	}

	public void setReturnType(Type returnType) {
		this.returnType = returnType;
	}

	public List<SingleVariableDeclaration> getParameters() {
		return parameters;
	}

	public void setParameters(List<SingleVariableDeclaration> parameters) {
		this.parameters = parameters;
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
	
	public String getQualifiedName() {
		if (belongingClassName != null) {
			return belongingClassName + "." + name;
		}
		else {
			return null;			
		}
	}

	public boolean equals(Object arg){
		return(this.getName().equals(((MethodBean)arg).getName()));
	}
	
}
