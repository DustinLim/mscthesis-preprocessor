package it.unisa.codeSmellAnalyzer.computation;

import java.io.File;
import java.util.Vector;

import org.eclipse.core.runtime.CoreException;

import it.unisa.codeSmellAnalyzer.beans.ClassBean;
import it.unisa.codeSmellAnalyzer.beans.PackageBean;
import it.unisa.codeSmellAnalyzer.metrics.CKMetrics;

public class RunQualityMetricsAnalysis {

	public static void main(String args[]) {
		
		// Path to the directory containing all the projects under analysis 
		String pathToDirectory = "/Users/dustinlim/Desktop/src/";
		
		File experimentDirectory = new File(pathToDirectory);
		
		for(File project: experimentDirectory.listFiles()) {
			try {
				// Method to convert a directory into a set of java packages.
				Vector<PackageBean> packages = FolderToJavaProjectConverter.convert(project.getAbsolutePath());
				
				for(PackageBean packageBean: packages) {
					for(ClassBean classBean: packageBean.getClasses()) {
						
						// How to call methods for computing quality metrics.
						System.out.println("====================================");
						System.out.println("Class Name: " + packageBean.getName() +"."+classBean.getName());
						System.out.println("	Lines of code: " + CKMetrics.getLOC(classBean));
						System.out.println("	Lack of Cohesion: " + CKMetrics.getLCOM(classBean));
						System.out.println("	Coupling Between Objects: " + CKMetrics.getCBO(classBean));
						System.out.println("	Response For a Class: " + CKMetrics.getRFC(classBean));
						System.out.println("	Weighted Methods per Class: " + CKMetrics.getWMC(classBean));
						System.out.println("====================================");
						// Other metrics are available in the CKMetrics class.
						
					}
				}	
			} catch (CoreException e) {
				e.printStackTrace();
			}	
		}
	}
}
