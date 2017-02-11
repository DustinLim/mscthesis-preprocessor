package it.unisa.codeSmellAnalyzer.parser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.TypeDeclaration;

public class InvocationVisitor extends ASTVisitor {
	
	private Collection<List<Object>> invocations;
	private CompilationUnit astRoot;
	
	public InvocationVisitor(Collection<List<Object>> pInvocations, CompilationUnit astRoot) {
		invocations = pInvocations;
		this.astRoot = astRoot;
	}
	
	public boolean visit(MethodInvocation pInvocationNode) {		
		// Mark message chains, to avoid double counting
		// only the root of the chain is marked.		
		boolean isChainedMethodCall = false;
		if (pInvocationNode.getExpression() instanceof MethodInvocation
				&& !(pInvocationNode.getParent() instanceof MethodInvocation)) {			
			isChainedMethodCall = true;			
		}

		// Resolve the class on which the method is invoked
		// This fails -iff- the method declaration is outside of our scope,
		// which implies that refactoring is non-applicable here. 
		String belongingClassName = null;
		IMethodBinding binding = pInvocationNode.resolveMethodBinding();		
		if (binding != null) {
			belongingClassName = binding.getDeclaringClass().getQualifiedName();
		}
						
		List<Object> invocation = new ArrayList<Object>();
		invocation.add(pInvocationNode.getName().toString());
		invocation.add(new Boolean(isChainedMethodCall));
		invocation.add(belongingClassName);
		invocation.add(ParseUtil.parseLineNumber(astRoot, pInvocationNode));
		invocation.add(pInvocationNode);
		
		invocations.add(invocation);
		return true;
	}
	
	public boolean visit(TypeDeclaration pClassNode) {
		return false;
	}
	
	private boolean isLocalInvocation(MethodInvocation pInvocationNode) {
		
		// Get the important data of the invocation
		String invocation = pInvocationNode.toString();
		String invocationName = pInvocationNode.getName().toString();
		int index = invocation.indexOf(invocationName);
		
		// The invocation has not a qualifier
		if (index == 0)
			return true;
		
		// The invocation has this as qualifier
		else if (index >= 5 && invocation.substring(index-5, index-1).equals("this"))
			return true;
		
		// The invocation has some other qualifier
		else
			return false;
		
	}
	
}
