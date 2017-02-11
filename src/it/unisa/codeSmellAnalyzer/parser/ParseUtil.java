package it.unisa.codeSmellAnalyzer.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;

import nl.dustinlim.thesis.ThesisTreeMap;

public class ParseUtil {

	public static int parseLineNumber(CompilationUnit astRoot, ASTNode astNode) {
		int lineNumber = astRoot.getLineNumber(astNode.getStartPosition());			

		try {
			if (lineNumber < 0) {
				throw new Exception("Issue with CompilationUnit.getLineNumber(..)");
			}
		}
		catch (Exception e) {
			System.err.println(e.getMessage());
		}
		
		return lineNumber;
	}
	
	public static int parseColumnNumber(CompilationUnit astRoot, ASTNode astNode) {
		int columnNumber = astRoot.getColumnNumber(astNode.getStartPosition());			

		try {
			if (columnNumber < 0) {
				throw new Exception("Issue with CompilationUnit.getColumnNumber(..)");
			}
		}
		catch (Exception e) {
			System.err.println(e.getMessage());
		}
		
		return columnNumber;
	}
	
	public static boolean listContainsUID(List<String> list, String uid) {
		return matchIdentifierInList(uid, list).size() > 0;
	}
	
	public static boolean setContainsUID(Set<String> set, String uid) {
		return listContainsUID(new ArrayList<>(set), uid);
	}

	public static void checkConsistencyOfIdentifiers(List<String> identifiers, List<String> parsedIdentifiers) {		
		List<String> discarded = new ArrayList<>();;

		for (String identifier : identifiers) {
			List<String> matches = matchIdentifierInList(identifier, parsedIdentifiers);
			
			if (matches.size() == 0) {
				System.out.println("[WARNING] Missing identifier	: " + identifier);
				//discarded.add(identifier);
			}
			else if (matches.size() >= 2) {
				System.out.println("[WARNING] Ambiguous identifier	: " + identifier + " -> " + matches.toString());				
				//discarded.add(identifier);
			}
			
			matches.clear();
		}
		
		identifiers.removeAll(discarded);
	}
	
	public static void checkConsistencyOfIdentifiers(ThesisTreeMap identifiers, List<String> parsedIdentifiers) {
		for (Entry<String, List<String>> entry : identifiers.entrySet()) {
			checkConsistencyOfIdentifiers(entry.getValue(), parsedIdentifiers);
		}
	}
	
	private static List<String> matchIdentifierInList(String uid, List<String> list) {

		// UID formats are:
		// smell-app-className										for classes
		// smell-app-className.methodName@lineNumber				for methods
		// smell-app-className.methodName@lineNumber-columnNumber	for invocation chains
		
		List<String> matches = new ArrayList<>();
		
		if (list != null) {
			if (list.contains(uid)) {
				for (String listUid : list) {
					if (listUid.equals(uid)) {
						matches.add(listUid);
					}
				}			
			}
			else if (!uid.startsWith("MC")) {
				// The @-sign and line/column numbers were an uid extension, to fix ambiguity.
				// Next, try to match the original uid format. 
				String shortUid = uid.split("@")[0] + "@";
				
				for (String listUid : list) {			
					String shortListUid = listUid.split("@")[0] + "@";
					if (shortListUid.equals(shortUid)) {
						matches.add(listUid);
					}				
				}								
			}	
		}
		
		return matches;				
	}
	
}
