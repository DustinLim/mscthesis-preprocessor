package it.unisa.codeSmellAnalyzer.parser;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Vector;
import java.util.regex.Pattern;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import it.unisa.codeSmellAnalyzer.beans.*;

public class ClassParser {
	private ClassBean outerClass;
	public File parentFile;
	
	public ClassBean parse(TypeDeclaration pClassNode, String belongingPackage, List<String> imports, CompilationUnit astRoot, String projectID) {
		int numberOfGetterOrSetter=0;
		
		// Instantiate the bean
		ClassBean classBean = new ClassBean(pClassNode);

		if(pClassNode.getSuperclassType() != null)
			classBean.setSuperclass(pClassNode.getSuperclassType().toString());
		else
			classBean.setSuperclass(null);

		classBean.setParentFile(parentFile);
		
		// Set the name
		String name = pClassNode.getName().toString();
		if (outerClass != null) {
			name = outerClass.getName() + "." + name;
		}
		classBean.setName(name);
		
		classBean.setUID(String.format("%s-%s", projectID, classBean.getName()));				
		classBean.setImports(imports);
		classBean.setBelongingPackage(belongingPackage);

		// Get the instance variable nodes 
		Collection<FieldDeclaration> instanceVariableNodes = new Vector<FieldDeclaration>();
		pClassNode.accept(new InstanceVariableVisitor(instanceVariableNodes));

		classBean.setTextContent(pClassNode.toString());

		Pattern newLine = Pattern.compile("\n");
		String[] lines = newLine.split(pClassNode.toString());

		classBean.setLOC(lines.length);

		// Get the instance variable beans from the instance variable nodes
		Collection<InstanceVariableBean> instanceVariableBeans = new Vector<InstanceVariableBean>();
		for (FieldDeclaration instanceVariableNode : instanceVariableNodes)
			instanceVariableBeans.add(InstanceVariableParser.parse(instanceVariableNode));

		// Set the collection of instance variables
		classBean.setInstanceVariables(instanceVariableBeans);

		// Get the method nodes
		Collection<MethodDeclaration> methodNodes = new Vector<MethodDeclaration>();
		pClassNode.accept(new MethodVisitor(methodNodes));

		// Get the method beans from the method nodes
		Collection<MethodBean> methodBeans = new Vector<MethodBean>();
		for (MethodDeclaration methodNode : methodNodes) {
			
			if((methodNode.getName().toString().contains("get") || (methodNode.getName().toString().contains("set")))) {
				if(methodNode.parameters().size() == 0) {
					numberOfGetterOrSetter++;
				}
			}
			
			MethodBean methodBean = new MethodParser().parse(methodNode, instanceVariableBeans, astRoot);

			// Set UID's
			methodBean.setUID(String.format("%s.%s@LN%s", 
					classBean.getUID(), 
					methodBean.getName(), 
					methodBean.getLineNumber()
					));			
			for (InvocationBean invocationBean : methodBean.getInvocations()) {
				invocationBean.setUID(String.format("%s.%s@LN%s:%s", 
					classBean.getUID(), 
					methodBean.getName(),
					invocationBean.getLineNumber(),
					ParseUtil.parseColumnNumber(astRoot, invocationBean.getNode())
					));
			}
			
			methodBeans.add(methodBean);
		}

		classBean.setNumberOfGetterAndSetter(numberOfGetterOrSetter);
		
		// Iterate over the collection of methods
		for (MethodBean classMethod : methodBeans) {
			// Set attributes
			classMethod.setBelongingClassName(classBean.getQualifiedName());

			/*
			// Instantiate a collection of class-defined invocations
			Collection<MethodBean> definedInvocations = new Vector<MethodBean>();

			// Get the method invocations
			Collection<MethodBean> classMethodInvocations = classMethod.getMethodCalls();

			// Iterate over the collection of method invocations
			for (MethodBean classMethodInvocation : classMethodInvocations) {
				definedInvocations.add(classMethodInvocation);
			}

			// Set the class-defined invocations
			classMethod.setMethodCalls(definedInvocations);
			*/
		}
		


		// Set the collection of methods
		classBean.setMethods(methodBeans);
		
		// Add inner classes
		ClassVisitor classVisitor = new ClassVisitor(pClassNode);
		pClassNode.accept(classVisitor);
		for (TypeDeclaration innerClass : classVisitor.innerClasses) {
			outerClass = classBean;
			ClassBean bean = parse(innerClass, belongingPackage, imports, astRoot, projectID);
			classBean.getInnerClasses().add(bean);
		}

		return classBean;

	}

	private class ClassVisitor extends ASTVisitor {
		public Collection<TypeDeclaration> innerClasses;
		private TypeDeclaration startingNode;
		
		public ClassVisitor(TypeDeclaration startingNode) {
			innerClasses = new ArrayList<>();
			this.startingNode = startingNode;
		}
		
		public boolean visit(TypeDeclaration typeDeclaration) {	
			// Ignore starting node itself
			if (typeDeclaration.equals(startingNode)) {
				return true;
			}
						
			// If type is an inner class
			if (!typeDeclaration.isInterface()) {
				innerClasses.add(typeDeclaration);
			}

			// Don't visit possible nested inner classes 
            return false;
		}			
	}

}
