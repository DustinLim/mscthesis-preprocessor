package it.unisa.codeSmellAnalyzer.parser;

import java.io.File;
import java.util.List;

import org.eclipse.jdt.core.dom.*;

/**
 * This class contains the methods necessary for the parser;
 *
 * @author Fabio Palomba;
 * @author Dustin Lim
 */

public class CodeParser {
	private final String PATH_TO_ANDROID_SDK = 
			"/Users/dustinlim/Library/Android/sdk-eclipse/platforms/android-25/android.jar";
	
	/**
	 * This method allows to create a parser for the AST;
	 * 
	 * @param 
	 * 			a String representation of a Class;
	 * @return
	 * 			a CompilationUnit to work on;
	 */
	public CompilationUnit createParserWithBindings(File pFile, String pClass, List<String> sourcepathEntries) {
		ASTParser parser = ASTParser.newParser(AST.JLS8);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setSource(pClass.toCharArray()); // set source
		parser.setResolveBindings(true); // we need bindings later on
		parser.setBindingsRecovery(true);		
		parser.setEnvironment(
				new String[] { PATH_TO_ANDROID_SDK }, 
				sourcepathEntries.toArray(new String[0]), 
				null, 
				true);
		parser.setUnitName(pFile.getName());		
		return (CompilationUnit) parser.createAST(null);
	}
}