package it.unisa.codeSmellAnalyzer.parser;

import java.util.Collection;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

public class MethodVisitor extends ASTVisitor {
	
	private Collection<MethodDeclaration> methodNodes;
	private boolean firstTime;
	
	public MethodVisitor(Collection<MethodDeclaration> pMethodNodes) {
		methodNodes = pMethodNodes;
		firstTime = true;
	}
	
	public boolean visit(MethodDeclaration pMethodNode) { 
		methodNodes.add(pMethodNode);
		
		// Nested methods do not exist in Java, but methods can contain
		// anonymous classes that define methods. However, to avoid double counting  
		// child nodes such as MethodInvocation types we should return false here.
		return false;
	}
	
	public boolean visit(TypeDeclaration pClassNode) {
		if (firstTime) {
			firstTime = false;
			return true;
		}
		return firstTime;
	}
	
}
