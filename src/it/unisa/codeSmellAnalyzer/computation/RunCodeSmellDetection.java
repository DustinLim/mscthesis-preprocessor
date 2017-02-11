package it.unisa.codeSmellAnalyzer.computation;

import java.io.File;
import java.util.Vector;

import org.eclipse.core.runtime.CoreException;

import it.unisa.codeSmellAnalyzer.beans.ClassBean;
import it.unisa.codeSmellAnalyzer.beans.MethodBean;
import it.unisa.codeSmellAnalyzer.beans.PackageBean;
import it.unisa.codeSmellAnalyzer.smellDetector.*;

public class RunCodeSmellDetection {
	public static void main(String args[]) {

		// Path to the directory containing all the projects under analysis 
		String pathToDirectory = "/Users/fabiopalomba/Desktop/experiment/";

		File experimentDirectory = new File(pathToDirectory);

		// Declaring Class-level code smell objects.
		ClassDataShouldBePrivateRule cdsbp = new ClassDataShouldBePrivateRule();
		ComplexClassRule complexClass = new ComplexClassRule();
		FunctionalDecompositionRule functionalDecomposition = new FunctionalDecompositionRule();
		GodClassRule godClass = new GodClassRule();
		SpaghettiCodeRule spaghettiCode = new SpaghettiCodeRule(); 
		LongMethodRule longMethod = new LongMethodRule();

		// The following rules are quite low for detecting smelly code components.
		// MisplacedClassRule misplacedClass = new MisplacedClassRule();
		// FeatureEnvyRule featureEnvy = new FeatureEnvyRule();

		for(File project: experimentDirectory.listFiles()) {
			try {
				// Method to convert a directory into a set of java packages.
				Vector<PackageBean> packages = FolderToJavaProjectConverter.convert(project.getAbsolutePath());

				for(PackageBean packageBean: packages) {
					for(ClassBean classBean: packageBean.getClasses()) {

						// How to call methods for computing code smell detection.
						// The currently implemented detector is DECOR (http://www.irisa.fr/triskell/publis/2009/Moha09d.pdf)

						boolean isClassDataShouldBePrivate = cdsbp.isClassDataShouldBePrivate(classBean);
						boolean isComplexClass = complexClass.isComplexClass(classBean);
						boolean isFunctionalDecomposition = functionalDecomposition.isFunctionalDecomposition(classBean);
						boolean isGodClass = godClass.isGodClass(classBean);
						boolean isSpaghettiCode = spaghettiCode.isSpaghettiCode(classBean);

						System.out.println("Class: " + classBean.getBelongingPackage() 
							+ "." + classBean.getName() + "\n"
							+ "		ClassDataShouldBePrivate: " + isClassDataShouldBePrivate + "\n"
							+ "		ComplexClass: " + isComplexClass + "\n"
							+ "		FunctionalDecomposition: " + isFunctionalDecomposition + "\n"
							+ "		GodClass: " + isGodClass + "\n"
							+ "		SpaghettiCode: " + isSpaghettiCode);
						
						for(MethodBean methodBean: classBean.getMethods()) {
							boolean isLongMethod = longMethod.isLongMethod(methodBean);
							
							System.out.println("		Method: " + methodBean.getName() + "\n"
									+ "			LongMethod: " + isLongMethod);
						}
					}
				}
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
	}
}
