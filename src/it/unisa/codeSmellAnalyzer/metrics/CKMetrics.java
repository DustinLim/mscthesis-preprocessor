package it.unisa.codeSmellAnalyzer.metrics;

import java.util.Collection;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import it.unisa.codeSmellAnalyzer.beans.*;

public class CKMetrics {


	/**
	 * Calculate Weighted Methods per Class.
	 * This is defined as the sum of all class method complexities.
	 * (CK metric #1) 
	 */
	public static int getWMC(ClassBean cb) {

		int WMC = 0;

		Vector<MethodBean> methods = (Vector<MethodBean>) cb.getMethods();
		for(MethodBean m: methods){
			WMC+=getMcCabeCycloComplexity(m);
		}

		return WMC;

	}

	/**
	 * Calculate Depth of Inheritance Tree.
	 * This is defined as the distance between the given class and the root
	 * of its inheritance tree.
	 * (CK metric #2) 
	 */
	public static int getDIT(ClassBean cb, Vector<ClassBean> System, int inizialization){

		int DIT = inizialization;

		if(DIT == 3) 
			return DIT;
		else {
			if(cb.getSuperclass() != null) {
				DIT++;
				for(ClassBean cb2: System){
					if(cb2.getName().equals(cb.getSuperclass())){
						getDIT(cb2, System, DIT);
					}
				}
			} else {
				return DIT;
			}
		}
		return DIT;

	}

	/**
	 * Calculate Number of Children.
	 * This is defined as the number of immediate children of the given class.
	 * (CK metric #3) 
	 */
	public static int getNOC(ClassBean cb, Vector<ClassBean> System){

		int NOC = 0;


		for(ClassBean c: System){
			if(c.getSuperclass() != null && c.getSuperclass().equals(cb.getName())){
				NOC++;
			}
		}

		return NOC;

	}

	/**
	 * Calculate Response For a Class.
	 * Defined as the number of methods and constructors that can be invoked
	 * as a result of messaging the given class. 
	 * (CK metric #5)
	 */
	public static int getRFC(ClassBean cb){

		int RFC = 0;

		Vector<MethodBean> methods = (Vector<MethodBean>) cb.getMethods();
		for(MethodBean m: methods){
			RFC+=m.getInvocations().size();
		}

		return RFC;

	}

	/**
	 * Calculate Coupling between object classes.
	 * Defined as the number of other classes to which it is coupled.
	 * (CK metric #4)
	 */
	public static int getCBO(ClassBean cb){

		Vector<String> imports = (Vector<String>) cb.getImports();

		return imports.size();

	}

	/**
	 * Calculate Lack of Cohesion in Methods.
	 * Defined as the number of class method pairs that do not share any
	 * used instance variables, minus the number of method pairs that do.
	 * This value does not drop further below zero. 
	 * (CK metric #6)
	 */
	public static int getLCOM(ClassBean cb){

		int share = 0;
		int notShare = 0;

		Vector<MethodBean> methods = (Vector<MethodBean>) cb.getMethods();
		for(int i=0; i<methods.size(); i++){
			for (int j=i+1; j<methods.size(); j++){
				if(shareAnInstanceVariable(methods.elementAt(i), methods.elementAt(j))){
					share++;
				} else {
					notShare++;
				}
			}
		}

		if(share > notShare){
			return 0;
		} else {
			return (notShare-share);
		}
	}

	//Number of operations added by a subclass
	//Unused method.
	public static int getNOA(ClassBean cb, Vector<ClassBean> System){

		int NOA = 0;

		for(ClassBean c: System){
			if(c.getName().equals(cb.getSuperclass())){
				Vector<MethodBean> subClassMethods = (Vector<MethodBean>) cb.getMethods();
				Vector<MethodBean> superClassMethods = (Vector<MethodBean>) c.getMethods();
				for(MethodBean m: subClassMethods){
					if(!superClassMethods.contains(m)){
						NOA++;
					}
				}
				break;
			}
		}

		return NOA;
	}


	//Number of operations overridden by a subclass
	//Unused method.
	public static int getNOO(ClassBean cb, Vector<ClassBean> System){

		int NOO = 0;

		if(cb.getSuperclass() != null){

			for(ClassBean c: System){
				if(c.getName().equals(cb.getSuperclass())){
					Vector<MethodBean> subClassMethods = (Vector<MethodBean>) cb.getMethods();
					Vector<MethodBean> superClassMethods = (Vector<MethodBean>) c.getMethods();
					for(MethodBean m: subClassMethods){
						if(superClassMethods.contains(m)){
							NOO++;
						}
					}
					break;
				}
			}
		}

		return NOO;
	}

	public static double computeMediumIntraConnectivity(PackageBean pPackage) {
		double packAllLinks = Math.pow(pPackage.getClasses().size(), 2);
		double packIntraConnectivity=0.0;

		for(ClassBean eClass: pPackage.getClasses()) {
			for(ClassBean current: pPackage.getClasses()) {
				if(eClass!=current) {
					if(existsDependence(eClass, current)) {
						packIntraConnectivity++;
					}
				}
			}
		}

		return packIntraConnectivity/packAllLinks;
	}

	public static double computeMediumInterConnectivity(PackageBean pPackage, Collection<PackageBean> pPackages) {
		double sumInterConnectivities=0.0;	

		for(ClassBean eClass: pPackage.getClasses()) {
			for(PackageBean currentPack: pPackages) {
				double packsInterConnectivity=0.0;
				double packsAllLinks = 2 * pPackage.getClasses().size() * currentPack.getClasses().size();

				if(pPackage!=currentPack) {
					for(ClassBean currentClass: currentPack.getClasses()) {
						if(existsDependence(eClass, currentClass)) {
							packsInterConnectivity++;
						}
					}
				}
				sumInterConnectivities+=((packsInterConnectivity)/packsAllLinks);
			}
		}

		return ((1.0/(pPackages.size()*(pPackages.size()-1))) * sumInterConnectivities);
	}

	public static double computeMediumIntraConnectivity(Collection<PackageBean> pClusters) { 
		double sumIntraConnectivities=0.0;	
		for(PackageBean pack: pClusters) {
			double packAllLinks = Math.pow(pack.getClasses().size(), 2);
			double packIntraConnectivity=0.0;
			for(ClassBean eClass: pack.getClasses()) {
				for(ClassBean current: pack.getClasses()) {
					if(eClass!=current) {
						if(existsDependence(eClass, current)) {
							packIntraConnectivity++;
						}
					}
				}
			}
			sumIntraConnectivities+=packIntraConnectivity/packAllLinks;
		}

		return ((1.0/pClusters.size()) * sumIntraConnectivities);
	}

	public static double computeMediumInterConnectivity(Collection<PackageBean> pClusters) {
		double sumInterConnectivities=0.0;	

		for(PackageBean pack: pClusters) {
			for(ClassBean eClass: pack.getClasses()) {
				for(PackageBean currentPack: pClusters) {
					double packsInterConnectivity=0.0;
					double packsAllLinks = 2 * pack.getClasses().size() * currentPack.getClasses().size();
					if(pack!=currentPack) {
						for(ClassBean currentClass: currentPack.getClasses()) {
							if(existsDependence(eClass, currentClass)) {
								packsInterConnectivity++;
							}
						}
					}
					sumInterConnectivities+=((packsInterConnectivity)/packsAllLinks);
				}
			}

		}

		return ((1.0/(pClusters.size()*(pClusters.size()-1))) * sumInterConnectivities);
	}


	public static int getMcCabeCycloComplexity(MethodBean mb){

		int mcCabe = 0;
		String code = mb.getTextContent();


		String regex = "return";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(code);


		if(matcher.find()){
			mcCabe++;
		}


		regex = "if";
		pattern = Pattern.compile(regex);
		matcher = pattern.matcher(code);


		if(matcher.find()){
			mcCabe++;
		}

		regex = "else";
		pattern = Pattern.compile(regex);
		matcher = pattern.matcher(code);


		if(matcher.find()){
			mcCabe++;
		}


		regex = "case";
		pattern = Pattern.compile(regex);
		matcher = pattern.matcher(code);


		if(matcher.find()){
			mcCabe++;
		}

		regex = "for";
		pattern = Pattern.compile(regex);
		matcher = pattern.matcher(code);


		if(matcher.find()){
			mcCabe++;
		}

		regex = "while";
		pattern = Pattern.compile(regex);
		matcher = pattern.matcher(code);


		if(matcher.find()){
			mcCabe++;
		}

		regex = "break";
		pattern = Pattern.compile(regex);
		matcher = pattern.matcher(code);


		if(matcher.find()){
			mcCabe++;
		}

		regex = "&&";
		pattern = Pattern.compile(regex);
		matcher = pattern.matcher(code);


		if(matcher.find()){
			mcCabe++;
		}

		regex = "||";
		pattern = Pattern.compile(regex);
		matcher = pattern.matcher(code);

		if(matcher.find()){
			mcCabe++;
		}

		regex = "catch";
		pattern = Pattern.compile(regex);
		matcher = pattern.matcher(code);


		if(matcher.find()){
			mcCabe++;
		}

		regex = "throw";
		pattern = Pattern.compile(regex);
		matcher = pattern.matcher(code);


		if(matcher.find()){
			mcCabe++;
		}


		return mcCabe;
	}

	private static boolean existsDependence(ClassBean pClass1, ClassBean pClass2) {
		for(MethodBean methodClass1: pClass1.getMethods()) {
			for(InvocationBean call: methodClass1.getInvocations()) {

				for(MethodBean methodClass2: pClass2.getMethods()) {
					if(call.getName().equals(methodClass2.getName())) 
						return true;
				}
			}
		}

		for(MethodBean methodClass2: pClass2.getMethods()) {
			for(InvocationBean call: methodClass2.getInvocations()) {

				for(MethodBean methodClass1: pClass1.getMethods()) {
					if(call.getName().equals(methodClass1.getName())) 
						return true;
				}
			}
		}

		return false;
	}

	private static boolean shareAnInstanceVariable(MethodBean m1, MethodBean m2){

		Vector<InstanceVariableBean> m1Variables = (Vector<InstanceVariableBean>) m1.getUsedInstanceVariables();
		Vector<InstanceVariableBean> m2Variables = (Vector<InstanceVariableBean>) m2.getUsedInstanceVariables();

		for(InstanceVariableBean i: m1Variables){
			if(m2Variables.contains(i)){
				return true;
			}
		}

		return false;

	}

}
