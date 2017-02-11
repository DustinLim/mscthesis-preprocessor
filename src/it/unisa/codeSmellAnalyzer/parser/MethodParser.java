package it.unisa.codeSmellAnalyzer.parser;

import it.unisa.codeSmellAnalyzer.beans.InstanceVariableBean;
import it.unisa.codeSmellAnalyzer.beans.InvocationBean;
import it.unisa.codeSmellAnalyzer.beans.MethodBean;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;

public class MethodParser extends ASTVisitor {
	private MethodBean methodBean;
	private List<IVariableBinding> fields;
	
	public MethodBean parse(MethodDeclaration pMethodNode, Collection<InstanceVariableBean> pClassInstanceVariableBeans, CompilationUnit astRoot) {
		// Instantiate the bean
		methodBean = new MethodBean(pMethodNode);
		
		// Set the name
		methodBean.setName(pMethodNode.getName().toString());
		
		methodBean.setParameters(pMethodNode.parameters());
		
		methodBean.setReturnType(pMethodNode.getReturnType2());

		methodBean.setLineNumber(ParseUtil.parseLineNumber(astRoot, pMethodNode));
		
		// Set the textual content
		methodBean.setTextContent(pMethodNode.toString());
		
		// Get all variables
		fields = new ArrayList<>();
		pMethodNode.accept(this);
		methodBean.setReferencedFields(fields);
		
		// Get the names in the method
		Collection<String> names = new HashSet<String>();
		pMethodNode.accept(new NameVisitor(names));
		
		// Verify the correspondence between names and instance variables 
		Collection<InstanceVariableBean> usedInstanceVariableBeans = getUsedInstanceVariable(names, pClassInstanceVariableBeans);
		
		// Set the used instance variables
		methodBean.setUsedInstanceVariables(usedInstanceVariableBeans);
		
		// Get the invocation names
		Collection<List<Object>> invocations = new HashSet<List<Object>>();
		pMethodNode.accept(new InvocationVisitor(invocations, astRoot));
				
		// Get the invocation beans from the invocation names
		Collection<InvocationBean> invocationBeans = new Vector<InvocationBean>();
		for (List<Object> invocation : invocations){
			invocationBeans.add(InvocationParser.parse(invocation));
		}
		
		// Set the invocations
		methodBean.setInvocations(invocationBeans);
				
		// Return the bean
		return methodBean;
		
	}
	
	public boolean visit(MethodInvocation methodInvocation) {
		// We need to avoid double counting dependencies,
		// when field access is wrapped within a method invocation..
		// variable.fieldName.methodName(anyArguments)
		for (Object obj : methodInvocation.arguments()) {
			((Expression) obj).accept(this);
		}
		return false;
	}
	
	public boolean visit(SimpleName simpleName) { 		
		IBinding binding = simpleName.resolveBinding();
		if (binding instanceof IVariableBinding) {
			IVariableBinding variableBinding = (IVariableBinding) binding;
			if (variableBinding.isField()) {				
					fields.add(variableBinding);					
			}
		}
		return true;
	}

	private boolean nodeHasParentOfType(ASTNode node, Class<?> type) {
		ASTNode finger = node.getParent();
		while (finger != null) {
			if (finger.getClass().equals(type)) {
				return true;
			}			
			finger = finger.getParent();
		}
		return false;
	}	
	
	private static Collection<InstanceVariableBean> getUsedInstanceVariable(Collection<String> pNames, Collection<InstanceVariableBean> pClassInstanceVariableBeans) {
		
		// Instantiate the collection to return
		Collection<InstanceVariableBean> usedInstanceVariableBeans = new Vector<InstanceVariableBean>();
		
		// Iterate over the instance variables defined in the class
		for (InstanceVariableBean classInstanceVariableBean : pClassInstanceVariableBeans)
			
			// If there is a correspondence, add to the returned collection
			if (pNames.remove(classInstanceVariableBean.getName()))
				usedInstanceVariableBeans.add(classInstanceVariableBean);
		
		// Return the collection
		return usedInstanceVariableBeans;
		
	}
	
}
