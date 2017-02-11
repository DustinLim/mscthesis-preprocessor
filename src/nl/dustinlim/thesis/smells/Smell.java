package nl.dustinlim.thesis.smells;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import it.unisa.codeSmellAnalyzer.beans.ClassBean;
import it.unisa.codeSmellAnalyzer.beans.MethodBean;
import it.unisa.codeSmellAnalyzer.computation.FileUtility;

public abstract class Smell {
	protected StringBuilder logOutputPreprocessor = new StringBuilder();
	protected StringBuilder logOutputWeka = new StringBuilder();
	protected int smellCount = 0;
	protected StringBuilder consoleOutput = new StringBuilder();

	public List<String> parsedIdentifiers = new ArrayList<>();
	public List<String> filteredIdentifiers = new ArrayList<>();

	protected abstract void setInput(List<Object> parameters);		
	protected abstract boolean passesFilter();
	
	public void filterParsedConstruct(List<Object> parameters) {
		setInput(parameters);
		boolean instancePassedFilter = passesFilter();
		
		addParsedConstructToWekaFile(getUID());

		if (parsedIdentifiers.contains(getUID())) {
			System.out.println("[WARNING] Ambiguous UID: " + getUID());			
		}
		parsedIdentifiers.add(getUID());

		if (instancePassedFilter) {
			filteredIdentifiers.add(getUID());
		}
	}
	
	protected abstract void addParsedConstructToWekaFile(String parsedUid);
	
	public abstract String getUID();
	
	protected void appendLineToLog(StringBuilder log, CharSequence outputDelimiter, String[] output) {
		log.append(String.join(outputDelimiter, output) + System.lineSeparator());		
	}

	public void writeLogOutput(String outputPreprocessorPath, String outputCompletePath) {
		String output = logOutputPreprocessor.toString();
		String[] lines = output.split(System.lineSeparator());
		Arrays.sort(lines, 1, lines.length, String.CASE_INSENSITIVE_ORDER); //ignore header line		
		output = String.join(System.lineSeparator(), lines);		
		FileUtility.writeFile(output.toString(), outputPreprocessorPath);

		FileUtility.writeFile(logOutputWeka.toString(), outputCompletePath);
	}	

	public int getSmellCount() {
		return smellCount;
	}
	
	public void writeConsoleOutput() {
		System.out.println("[" + consoleOutput.toString() + "];");
	}

	protected String generateTerminalCommand(ClassBean classBean, MethodBean methodBean) {
		return "xed --line " + methodBean.getLineNumber() + " '" + classBean.getParentFile().getAbsolutePath() + "'";
	}		
	
	public static List<String> readAllLines(String pathString) throws IOException {
		final String PREFIX = "input/";
		return Files.readAllLines(Paths.get(PREFIX + pathString), Charset.defaultCharset());
	}
	
}
