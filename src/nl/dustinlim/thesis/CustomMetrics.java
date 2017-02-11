package nl.dustinlim.thesis;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;

import it.unisa.codeSmellAnalyzer.beans.ClassBean;
import it.unisa.codeSmellAnalyzer.beans.MethodBean;
import it.unisa.codeSmellAnalyzer.metrics.SizeMetrics;

public class CustomMetrics {
	
	public static boolean classInheritsFromAndroidClass(ClassBean classBean) {
		boolean result = false;
		
		for (String className : getTypeHierarchy(classBean)) {
			if (className.startsWith("android.")) {
				result = true;
			}
		}
		
		return result;		
	}
	
	public static List<String> getTypeHierarchy(ClassBean classBean) {
		List<String> superclasses = new ArrayList<>();
		ITypeBinding typeBind = classBean.getAstNode().resolveBinding();

		while (typeBind != null) {
			String className = typeBind.getQualifiedName();
			superclasses.add(className);
			typeBind = typeBind.getSuperclass();
		}				
		
		return superclasses;
	}

	// Calculated on: class
	// Returns: integer in range [0, inf>
	public static int locSumOfInnerClasses(ClassBean classBean) {
		int locSum = 0;
		
		for (ClassBean innerClass : classBean.getInnerClasses()) {
			locSum += innerClass.getLOC();
		}
		
		return locSum;
	}
	
	// Calculated on: class
	// Returns: integer in range [0, inf>
	public static int numberOfMethodOverrideAnnotations(ClassBean classBean) {
		int annotationsCount = 0;
		
		for (MethodBean methodBean : classBean.getMethods()) {
			MethodDeclaration methodDeclaration = methodBean.getAstNode();

			for (Object modifier : methodDeclaration.modifiers()) {
				if (modifier instanceof MarkerAnnotation) {
					MarkerAnnotation markerAnnotation = (MarkerAnnotation) modifier;
					if (markerAnnotation.getTypeName().toString().equals("Override")) {
						annotationsCount++;
					}
				}
			}			
		}
		
		return annotationsCount;
	}
	
	
	// Calculated on: method
	// Returns: integer in range [0, inf>
	public static int locOfLargestBlock(MethodBean methodBean) {
		int largestLOC = 0;
		List<Integer> locList = new ArrayList<>();
		
		MethodDeclaration methodDeclaration = methodBean.getAstNode();		
		methodDeclaration.accept(new ASTVisitor() {
			boolean topLevelBlock = true;
			
			@Override
			public boolean visit(Block block) {
				// The top level is of the method itself, we want one level deeper.
				if (topLevelBlock) {
					topLevelBlock = false;
					return true;
				}
				
				locList.add(SizeMetrics.getLOC(block.toString()));
				
				return false; //lower-level blocks cannot have larger LOC
			}
		});

		// FIXME: is locList valid at this point, ASTVisitor works on another thread?
		//System.out.println(locList.toString());
		
		for (Integer loc : locList) {
			if (loc > largestLOC) {
				largestLOC = loc;
			}
		}		

		return largestLOC;
	}
	
	// Calculated on: invocation chain
	// Returns: integer in range [0, inf>
	public static int numberOfOwnedClassesTraversed(List<String> traversedClasses) {		
		Set<String> filteredClasses = new HashSet<>();
		
		for (String traversedClass : traversedClasses) {
			if (!traversedClass.startsWith("android.") && !traversedClass.startsWith("java.")) {
				filteredClasses.add(traversedClass);
			}
		}
		
		return filteredClasses.size();
	}
	
	public static int numberOfStaticInvocations(MethodInvocation rootInvocation) {
		return -1;
	}
}
