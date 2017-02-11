package nl.dustinlim.thesis.smells;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;

import it.unisa.codeSmellAnalyzer.beans.ClassBean;
import it.unisa.codeSmellAnalyzer.beans.InvocationBean;
import it.unisa.codeSmellAnalyzer.beans.MethodBean;
import it.unisa.codeSmellAnalyzer.parser.ParseUtil;
import nl.dustinlim.thesis.DetectionPerformance;
import nl.dustinlim.thesis.ThesisTreeMap;
import nl.dustinlim.thesis.CustomMetrics;

public class MessageChains extends Smell {	
	private String projectID;
	private ClassBean classBean;
	private MethodBean methodBean;	
	private InvocationBean invocationBean;	
	private List<String> traversedClasses;
	
	int numberOfTraversedClasses;
	
	public MessageChains() throws IOException {		
		appendLineToLog(logOutputWeka, ",", new String[] {
				"getNumberOfTraversedClasses",
				"numberOfOwnedClassesTraversed",
				"parentClassInheritsFromAndroidClass",				
				"decor", "oracle_smelly"
		});

		appendLineToLog(logOutputPreprocessor, ";", new String[] {
				"UID", "App name", "Length", "Names", "Types", "Terminal CMD"
		});
	}
	
	@Override
	protected void addParsedConstructToWekaFile(String parsedUid) {
		
		/*
		ThesisTreeMap detectors = DetectionPerformance.messageChainsDetectors;

		appendLineToLog(logOutputWeka, ",", new String[] {
				Integer.toString(numberOfTraversedClasses),
				//Integer.toString(getNumberOfTraversedClasses(invocationBean.getNode())),
				Integer.toString(CustomMetrics.numberOfOwnedClassesTraversed(traversedClasses)),
				Boolean.toString(CustomMetrics.classInheritsFromAndroidClass(classBean)),
				Boolean.toString(ParseUtil.listContainsUID(detectors.get(DetectionPerformance.DETECTOR_DECOR), getUID())),
				Boolean.toString(ParseUtil.listContainsUID(DetectionPerformance.messageChainsOracle, getUID()))
		});		
		*/
					
	}
	
	public void setup(String projectID) {
		this.projectID = projectID;
		smellCount = 0;
	}

	@Override
	public void setInput(List<Object> parameters) {
		this.classBean = (ClassBean) parameters.get(0);
		this.methodBean = (MethodBean) parameters.get(1);
		this.invocationBean = (InvocationBean) parameters.get(2);
	}	
	
	@Override
	public boolean passesFilter() {
		boolean smellDetected = false;

		// Excluded from oracle.
		if (projectID.equals("WordPress")) {
			return smellDetected;
		}

		MethodInvocation node = invocationBean.getNode();
		boolean isTopLevelInvocation = !(node.getParent() instanceof MethodInvocation);
		boolean satisfiesMinimumTraversedClasses = (getNumberOfTraversedClasses(node) >= 2);		
		if (isTopLevelInvocation 
				&& satisfiesMinimumTraversedClasses)  {
			smellDetected = true;			
		}
		if (isTopLevelInvocation) {
			consoleOutput.append(getNumberOfTraversedClasses(node) + " ");			
		}
		
		if (smellDetected) {
			// Append log
			smellCount++;
			String[] output = new String[] {
				getUID(),
				projectID,
				Integer.toString(getChainLength(node)),
				invocationBean.toShortString(),
				traversedClasses.toString(),
				"xed --line " + invocationBean.getLineNumber() + " '" + classBean.getParentFile().getAbsolutePath() + "'"
			};
			logOutputPreprocessor.append(String.join(";", output) + System.lineSeparator());			
		}

		
		ThesisTreeMap detectors = DetectionPerformance.messageChainsDetectors;

		appendLineToLog(logOutputWeka, ",", new String[] {
				Integer.toString(numberOfTraversedClasses),
				//Integer.toString(getNumberOfTraversedClasses(invocationBean.getNode())),
				Integer.toString(CustomMetrics.numberOfOwnedClassesTraversed(traversedClasses)),
				Boolean.toString(CustomMetrics.classInheritsFromAndroidClass(classBean)),
				Boolean.toString(ParseUtil.listContainsUID(detectors.get(DetectionPerformance.DETECTOR_DECOR), getUID())),
				Boolean.toString(ParseUtil.listContainsUID(DetectionPerformance.messageChainsOracle, getUID()))
		});		

		
		
		
		return smellDetected;
	}
	
	private int getChainLength(MethodInvocation invocation) {		
		int chainLength = 1;
		MethodInvocation pointer = invocation;
		
		while (pointer.getExpression() instanceof MethodInvocation) {
			chainLength++;
			pointer = (MethodInvocation) pointer.getExpression();
		}
		
		return chainLength;
	}
	
	private int getNumberOfTraversedClasses(MethodInvocation rootInvocation) {
		numberOfTraversedClasses = getTraversedClasses(rootInvocation).size();
		return numberOfTraversedClasses;
	}

	private Set<String> getTraversedClasses(MethodInvocation rootInvocation) {
		traversedClasses = new ArrayList<>();
		ASTNode pointer = rootInvocation;

		while (pointer instanceof MethodInvocation) {
			MethodInvocation invocation = (MethodInvocation) pointer;

			IMethodBinding methodBinding = invocation.resolveMethodBinding();			
			String qualifiedTypeName = "(?)";
			if (methodBinding != null) {
				ITypeBinding typeBinding = methodBinding.getDeclaringClass();
				qualifiedTypeName = typeBinding.getQualifiedName();
			}
			traversedClasses.add(qualifiedTypeName);
			pointer = invocation.getExpression();
		}
		
		// A set prevents duplicates
		Collections.reverse(traversedClasses);
		Set<String> uniqueClasses = new HashSet<String>(traversedClasses);
		
		Set<String> countedClasses = new HashSet<>();
		for (String classname : traversedClasses) {
			if (!countedClasses.contains(classname)) {
				countedClasses.add(classname);
				if (classname.startsWith("android.")) {
					// Refactoring is not possible in the Android namespace, so stop counting.
					break;
				}
			}			
		}
		
		//return uniqueClasses.size();
		return countedClasses;
	}
	
	private static String toShortString(MethodInvocation invocation) {		
		String shortString = "";
		ASTNode pointer = invocation;
		
		while (pointer instanceof MethodInvocation) {
			invocation = (MethodInvocation) pointer;
			shortString = "." + invocation.getName() + "( )" + shortString;
			pointer = invocation.getExpression();
		}
		
		return shortString;
	}

	@Override
	public String getUID() {
		return String.format("MC-%s", invocationBean.getUID());
	}
	
}
