package it.unisa.codeSmellAnalyzer.computation;

import it.unisa.codeSmellAnalyzer.beans.ClassBean;
import it.unisa.codeSmellAnalyzer.beans.PackageBean;
import it.unisa.codeSmellAnalyzer.parser.ClassParser;
import it.unisa.codeSmellAnalyzer.parser.CodeParser;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;
import java.util.Vector;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

public class FolderToJavaProjectConverter {
	
	public static Vector<PackageBean> convert(String pPath, String projectID) throws CoreException {
		File projectDirectory = new File(pPath);
		CodeParser codeParser = new CodeParser();
		Vector<PackageBean> packages = new Vector<PackageBean>();
		
		if(projectDirectory.isDirectory()) {
			System.out.println(projectDirectory.getAbsolutePath());
			List<String> sourcepathEntries = listAllDirectories(projectDirectory);
			for(File subDir: projectDirectory.listFiles()) {
				
				if(subDir.isDirectory()) {
					Vector<File> javaFiles = FolderToJavaProjectConverter.listJavaFiles(subDir);
					
					if(javaFiles.size() > 0) {
						//System.out.println("Files found:");
						for(File javaFile: javaFiles) {
							//System.out.println(javaFile.getAbsolutePath());
							CompilationUnit parsed;
							try {
								parsed = codeParser.createParserWithBindings(javaFile, FileUtility.readFile(javaFile.getAbsolutePath()), sourcepathEntries);
								
								/*
								for (IProblem problem : parsed.getProblems()) {
									//System.out.println(problem.getMessage());
								}
								*/
								
								Vector<String> imports = new Vector<String>();

								for(Object importedResource: parsed.imports())
									imports.add(importedResource.toString());
								
								// Note: No package declaration = default package
								PackageDeclaration packageDeclaration = parsed.getPackage();
								String packageQualifiedName = (packageDeclaration == null) ? 
										"(default package)" : packageDeclaration.getName().getFullyQualifiedName();
								
								
								if(! FolderToJavaProjectConverter.isAlreadyCreated(
										packageQualifiedName, packages)) {
									
									PackageBean packageBean = new PackageBean();
									packageBean.setName(packageQualifiedName);
									
									processCompilationUnit(parsed, projectID, javaFile, packageBean, imports);
									
									packages.add(packageBean);
									
								} else {
									PackageBean packageBean = FolderToJavaProjectConverter.getPackageByName(
											packageQualifiedName, packages);
									
									processCompilationUnit(parsed, projectID, javaFile, packageBean, imports);
								}
								
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}
					
				}
				
			}
			
		}
		
		for(PackageBean pb: packages) {
			String textualContent="";
			for(ClassBean cb: pb.getClasses()) {
				textualContent+=cb.getTextContent();
			}
			pb.setTextContent(textualContent);
		}
		
		return packages;
	}

	protected static void processCompilationUnit(
			CompilationUnit unit, 
			String projectID,
			File javaFile, 
			PackageBean packageBean,
			Vector<String> imports) {
		ClassParser classParser = new ClassParser();
		classParser.parentFile = javaFile;

		for (Object object: unit.types()) {
			TypeDeclaration typeDeclaration = (TypeDeclaration)object;
			ClassBean classBean = classParser.parse(typeDeclaration, packageBean.getName(), imports, unit, projectID);
			packageBean.addClass(classBean);
		}
	}

	/***
	 * Given a root directory, lists itself and all of its sub-directories. 
	 */
	public static List<String> listAllDirectories(File root) {
		Stack<File> stack = new Stack<>();
		stack.push(root);
		List<String> foundDirectories = new ArrayList<>();
		
		while (!stack.isEmpty()) {
			File file = stack.pop();
			if (file.isDirectory()) {
				foundDirectories.add(file.getAbsolutePath());
				stack.addAll(Arrays.asList(file.listFiles()));
			}
		}
		
		System.out.println("# (Sub)directories: " + foundDirectories.size());
		return foundDirectories;
	}
	
	private static Vector<File> listJavaFiles(File pDirectory) {
		Vector<File> javaFiles=new Vector<File>(); 
		File[] fList = pDirectory.listFiles();

		if(fList != null) {
			for (File file : fList) {
				if (file.isFile()) {
					if(file.getName().endsWith(".java")) {
						javaFiles.add(file);
					}
				} else if (file.isDirectory()) {
					File directory = new File(file.getAbsolutePath());
					javaFiles.addAll(listJavaFiles(directory));
				}
			}
		}
		return javaFiles;
	}
	
	private static boolean isAlreadyCreated(String pPackageName, Vector<PackageBean> pPackages) {
		for(PackageBean pb: pPackages) {
			if(pb.getName().equals(pPackageName))
				return true;
		}
		
		return false;
	}
	
	private static PackageBean getPackageByName(String pPackageName, Vector<PackageBean> pPackages) {
		for(PackageBean pb: pPackages) {
			if(pb.getName().equals(pPackageName))
				return pb;
		}
		return null;
	}
}
