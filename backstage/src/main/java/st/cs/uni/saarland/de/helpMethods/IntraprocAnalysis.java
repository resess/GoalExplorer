package st.cs.uni.saarland.de.helpMethods;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soot.G;
import soot.Scene;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.options.Options;

public class IntraprocAnalysis {
	private static final IntraprocAnalysis intraprocAnalysis = new IntraprocAnalysis();
	private final Logger logger =  LoggerFactory.getLogger(Thread.currentThread().getName());
	
	 public static IntraprocAnalysis getInstance(){
		 return intraprocAnalysis;
	 }
	

	// returns a map of <classname, methodset>. the methodset describes methods which have a setContentView call
	public Map<String, Set<String>> searchFunctionInCode(String androidSDK, String pathToAppOutFolder, String apkFilePath, String searchedFuncName) throws IOException{
		boolean jimpleFilesCreated = checkIfJimpleFilesWereCreated(pathToAppOutFolder);
		if (!jimpleFilesCreated)
			this.transformInJimple(androidSDK, pathToAppOutFolder, apkFilePath);
		
		File appsSootOutputFile = new File(pathToAppOutFolder + File.separator + "sootOutput");
		File appsJimpleFiles[] = appsSootOutputFile.listFiles();

		// classes are the key, methods are inside the set
		Map<String, Set<String>> methodsWithClasses = new HashMap<String, Set<String>>();
		
		for (File jfile: appsJimpleFiles){
			if (!jfile.exists())
				continue;
			String className = jfile.getName().replace(".jimple", "");
			FileReader freader = new FileReader(jfile);
			BufferedReader breader = new BufferedReader(freader);
			try {
				String line = breader.readLine();
				String functionDec = "";	
				// for className class
				Set<String> methodsWithFuncInside = new HashSet<String>();
				while(line != null){
					if (line.contains(searchedFuncName + "(")){
						// only save this function if this call isn't inside a private function
						if (!functionDec.equals(""))
							methodsWithFuncInside.add(functionDec);
					}
					if (( (line.contains("public ")) && (!line.contains(";")) )
						|| ((line.contains("protected ")) && (!line.contains(";")) ) ){
						// check if ( is inside the line
						String split[] = line.split("\\(");
           				if (split.length > 1){
           					functionDec = this.createFunctionSignatureFromLine(line);
           				}
					}
					else if (((line.contains("private ")) && (!line.contains(";")) )){
						// private function could not be searched by Soot	
						functionDec = "";
	               	}
//					if (( (line.contains("private ")) && (!line.contains(";")) )
//							|| ((line.contains("public ")) && (!line.contains(";")) )
//							|| ((line.contains("protected ")) && (!line.contains(";")) ) ){
//						// check if ( is inside the line
//						String split[] = line.split("\\(");
//	      				if (split.length > 1){
//	      					functionDec = this.createFunctionSignatureFromLine(line);
//	      				}
//					}
//					
					line = breader.readLine();
				}
				if (methodsWithFuncInside.size() > 0)
					methodsWithClasses.put(className, methodsWithFuncInside);
			}finally{
				freader.close();
				breader.close();
			}
			
			
			
		}
		return methodsWithClasses;
		
	}
	
	// runs Soot on the given FlowAnalysis class
	public CallGraph getSootCallGraph(){
		
		return Scene.v().getCallGraph();
		
//		// Set up soot
////		Options.v().set_whole_program(true);
//		Options.v().set_android_jars(androidSDK);
//		Options.v().set_process_dir(Collections.singletonList(apkFilePath));
//		Options.v().set_src_prec(Options.src_prec_apk);
//		Options.v().set_allow_phantom_refs(true);
//		
//		// Set up listener classes and methods
//		SootClass c = Scene.v().loadClassAndSupport(activity);
//		c.setApplicationClass();
//		Scene.v().loadNecessaryClasses();
//			
//		// Retrieve the method and its body
//		SootMethod m = null;
//		try{
////			//System.out.println(method);
//			m = c.getMethod(method);
//		}catch(RuntimeException e){
////			e.printStackTrace();
//			logger.info("no success with getMethod; trying with getMethodByName method: ");
//			String[] tmp = method.split("\\(");
//       		String[] tmp2 = tmp[0].split(" ");
//       		String methodName = tmp2[tmp2.length-1];
//       		try{
//       			m = c.getMethodByName(methodName);
//       		}catch(RuntimeException e1){
//       			logger.debug(apkFilePath + ": " + e1);
//       			logger.warn("was not able to find method: " + apkFilePath + ": " + activity + " " + method);
//       		}
//		}
//		if (m != null){
//			Body b = m.retrieveActiveBody();
//			
//			// Build the CFG and run the analysis
//			UnitGraph g = new BriefUnitGraph(b);
//			return g;
//		}
//		else
//			return null;

	}
	
	
	public void transformInJimple(String androidSDK, String pathToAppOutFolder, String apkFilePath){
		logger.info("<transform in Jimple>");
		IntraprocAnalysis.resetSootInitialization();
		try{
	//		File dexFile = new File(pathToAppOutFolder +  File.separator + "classes.dex");
			File sootOutputFile = new File(pathToAppOutFolder + File.separator + "sootOutput");
			if (!sootOutputFile.exists())
				sootOutputFile.mkdir();
			
			String[] args = new String[]{"-android-jars", androidSDK,"-process-dir",apkFilePath, "-output-dir", sootOutputFile.getAbsolutePath()};
	
//			Options.v().set_whole_program(true);
			Options.v().set_allow_phantom_refs(true);
			Options.v().set_src_prec(Options.src_prec_apk);
			
			
			Options.v().set_output_format(Options.output_format_jimple);
	//		Options.v().parse(args);
			soot.Main.main(args);
		}catch(OutOfMemoryError e){
			logger.error(apkFilePath + ": " + e);
		}
		finally{
			IntraprocAnalysis.resetSootInitialization();
			logger.info("</transform in Jimple>");
		}
		
	}
	
	 private String createFunctionSignatureFromLine (String functionLine){
		 String funcSig = functionLine;
    		if (functionLine.contains("public"))
    			funcSig = functionLine.replace("public ", "");
    		else if (functionLine.contains("private"))
    			funcSig = functionLine.replace("private ", "");
    		else if (functionLine.contains("protected"))
    			funcSig = functionLine.replace("protected ", "");
    		
//    		if (funcSig.contains("static"))
//    			funcSig = funcSig.replace("static ", "");
//    		
//    		if (funcSig.contains("final"))
//    			funcSig = funcSig.replace("final ", "");
    		
    		while (funcSig.startsWith(" "))
    			funcSig = funcSig.replaceFirst(" ", "");
    		
    		if (funcSig.contains(",")){
    			funcSig = funcSig.replace(", ", ",");
    		}
    		
    		return funcSig;
	 }
	 
	public static void resetSootInitialization(){
			G.reset();
	}
	
	private boolean checkIfJimpleFilesWereCreated(String pathToAppOutFolder){
		File sootOutputFile = new File(pathToAppOutFolder + File.separator + "sootOutput");
		if (!sootOutputFile.exists()){
			return false;
		}
		File[] files = sootOutputFile.listFiles();
		// 4 files must be there => 1 activity class, R$layout, R$id, R
		// TODO maybe R files must only in Java 7 be there?
		if (files.length > 4)
			return true;
		else 
			return false;
	}
	
	public boolean checkIfAppCodeContainsThisMethod(String className, String methodHeader, String pathToAppOutFolder, String androidSDK, String apkFilePath) throws IOException{
		
		boolean jimpleFilesCreated = checkIfJimpleFilesWereCreated(pathToAppOutFolder);
		if (!jimpleFilesCreated)
			this.transformInJimple(androidSDK, pathToAppOutFolder, apkFilePath);
		
		File appsSootOutputFile = new File(pathToAppOutFolder + File.separator + "sootOutput");
		File appsJimpleFiles[] = appsSootOutputFile.listFiles();

		for (File jfile: appsJimpleFiles){
			
			if (jfile.getName().replace(".jimple", "").equals(className)){
				
				FileReader freader = new FileReader(jfile);
				BufferedReader breader = new BufferedReader(freader);
				try{
					String line = breader.readLine();
					while(line != null){
						if (line.contains(methodHeader)){
							return true;
						}	
						line = breader.readLine();
					}
				}finally{
					freader.close();
					breader.close();
				}
				break;
			}
			
		}
		return false;
	}
}
