package st.cs.uni.saarland.de.helpClasses;

import org.xmlpull.v1.XmlPullParserException;
import soot.*;
import soot.jimple.infoflow.android.axml.AXmlNode;
import soot.jimple.infoflow.android.manifest.ProcessManifest;
import st.cs.uni.saarland.de.entities.FieldInfo;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class Helper {

	private static String apkName;
	private static String logsDir;
	private static String resultsDir;
	private static String packageName;
	private static String apkPath;
	private static String apkToolPath = "backstage"+ File.separator + "res" + File.separator + "apktool_2.1.1.jar";
	private static int LOC;
	public static Map<String, AtomicInteger> timeoutedPhasesInUIAnalysis = new ConcurrentHashMap<>();

	private static Set<String> appNameSpaces = new HashSet<>();
	public final static Map<SootField, Set<FieldInfo>> resolvedFields = new ConcurrentHashMap<>();
	public final static Map<SootMethod, Map<Unit, Unit>> immediateDominators = new ConcurrentHashMap<>();
	public final static Map<SootMethod, Unit> lastUnitOfMethod = new ConcurrentHashMap<>();
	private final static Map<SootMethod, String> signatureOfSootMethod = new ConcurrentHashMap<>();
	private final static Map<SootField, String> signatureOfSootField = new ConcurrentHashMap<>();
	private final static Map<Unit, Unit> successorUnit = new ConcurrentHashMap<>();
	private final static Map<Unit, Unit> predecessorUnit = new ConcurrentHashMap<>();
	private final static Set<String> activitiesFromManifestFile = new HashSet<>();
	private final static Set<SootClass> fragmentsLifecycleClasses = new HashSet<>();
	private final static Set<String> asyncTasksOnMethods = new HashSet<>();
	public static final String ASYNCTASKCLASS = "android.os.AsyncTask";

	public final static Set<String> getAsyncTasksOnMethods(){
		/*if(asyncTasksOnMethods.isEmpty()  && !Scene.v().getSootClass("android.os.AsyncTask").isPhantom() && Scene.v().getSootClass("android.os.AsyncTask").resolvingLevel() != 0){
			asyncTasksOnMethods.addAll(Scene.v().getSootClass("android.os.AsyncTask").getMethods().stream().map(x->x.getSubSignature()).collect(Collectors.toSet()));
		}*/
		if(asyncTasksOnMethods.isEmpty()){
			asyncTasksOnMethods.add("doInBackground");
			asyncTasksOnMethods.add("onPostExecute");
		}
		return asyncTasksOnMethods;
	}

	public static Set<SootClass> getFragmentsLifecycleClasses(){
		if(fragmentsLifecycleClasses.isEmpty() /*&& !Scene.v().getSootClass("android.support.v4.app.Fragment").isPhantom()*/ && Scene.v().getSootClass("android.support.v4.app.Fragment").resolvingLevel() != 0){
			fragmentsLifecycleClasses.addAll(Scene.v().getActiveHierarchy().getSubclassesOf(Scene.v().getSootClass("android.support.v4.app.Fragment")));
		}
		if(/*!Scene.v().getSootClass("android.app.Fragment").isPhantom() && */Scene.v().getSootClass("android.app.Fragment").resolvingLevel() != 0){
			fragmentsLifecycleClasses.addAll(Scene.v().getActiveHierarchy().getSubclassesOf(Scene.v().getSootClass("android.app.Fragment")));
		}
		return fragmentsLifecycleClasses;
	}

	public static void clearCache(){
		resolvedFields.clear(); //should I clear this later then?
		immediateDominators.clear();
		lastUnitOfMethod.clear();
		signatureOfSootMethod.clear();
		signatureOfSootField.clear();
		successorUnit.clear();
		predecessorUnit.clear();
	}

	public static Set<String> getActivitiesFromManifestFile(){
		return activitiesFromManifestFile;
	}

	public static void initializeManifestInfo(String apkPath) {
		ProcessManifest processMan = null;
		try {
			processMan = new ProcessManifest(apkPath);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (XmlPullParserException e) {
			e.printStackTrace();
		}
		String localPackageName = processMan.getPackageName();
		String[] splittedPackageName = localPackageName.split("\\.");
		if(splittedPackageName.length == 1){
			packageName = localPackageName;
		}
		else {
			packageName = splittedPackageName[0] + "." + splittedPackageName[1];
		}
		List<AXmlNode> xml = processMan.getAllActivities();
		String realPackageName = processMan.getPackageName();
		activitiesFromManifestFile.addAll(getActivitiesFromAxml(xml, realPackageName));
	}

	private static Set<String> getActivitiesFromAxml(List<AXmlNode> xml, String realPackageName){
		Set<String> activites = new HashSet<>();
		for(AXmlNode activity: xml) {
			String name = activity.getAttribute("name").getValue().toString();
			if(name.startsWith(".")){
				name = realPackageName+name;
			}
			else if(!name.contains(".")){
				name = String.format("%s.%s", realPackageName, name);
			}
			activites.add(name);
		}
		return activites;
	}

	public static void setPackageName(String name){
		packageName = name;
	}

	public static String getPackageName(){
		return packageName;
	}

	public static void addNameSpace(String nameSpace){
		appNameSpaces.add(nameSpace);
	}

	public static Set<String> getAppNameSpaces(){
		return appNameSpaces;
	}


	public static void setLOC(int value){
		LOC = value;
	}

	public static void setLogsDir(String dir){
		logsDir = dir;
	}

	public static void setResultsDir(String dir){
		resultsDir = dir;
	}

	public static void setApkToolPath(String path) { 
		apkToolPath = path; 
	}

	public static void setApkPath(String path) { apkPath = path; }

	public static String getApkPath() {return apkPath;}

	public static String getApkToolPath() {return apkToolPath;}

	public static String getResultsDir(){
		return resultsDir;
	}

	public static Unit getPredecessorOf(PatchingChain<Unit> units,  Unit u){
		if(predecessorUnit.containsKey(u)){
			return predecessorUnit.get(u);
		}
		final Unit predecessor = units.getPredOf(u);
		if(predecessor == null){
			return null;
		}
		predecessorUnit.put(u, predecessor);
		return predecessor;
	}

	public static Unit getSuccessorOf(PatchingChain<Unit> units,  Unit u){
		if(successorUnit.containsKey(u)){
			return successorUnit.get(u);
		}
		final Unit successor = units.getSuccOf(u);
		if(successor == null){
			return null;
		}
		successorUnit.put(u, successor);
		return successor;
	}

	public static String getSignatureOfField(SootField f){
		if(signatureOfSootField.containsKey(f)){
			return signatureOfSootField.get(f);
		}
		final String signature = f.getSignature();
		signatureOfSootField.put(f, signature);
		return signature;
	}

	public static String getSignatureOfSootMethod(SootMethod m){
		if(signatureOfSootMethod.containsKey(m)){
			return signatureOfSootMethod.get(m);
		}
		final String signature = m.getSignature();
		signatureOfSootMethod.put(m, signature);
		return signature;
	}

	public static void setApkName(String apkPath){
		// get the name of the apk name
		File apkPathFile = new File(apkPath);
		apkName = apkPathFile.getName();
	}

	public static String getApkName(){
		return apkName;
	};
	public static Map<String,String> bundlesAndParsable = new HashMap<>();
	public static List<String> excludedPrefixes = new ArrayList<>();
	private static long usedMemory;

	public static long getUsedMemory(){
		return usedMemory;
	}
	
	public static String exceptionStacktraceToString(Exception e)
	{
	    ByteArrayOutputStream baos = new ByteArrayOutputStream();
	    PrintStream ps = new PrintStream(baos);
	    e.printStackTrace(ps);
	    ps.close();
	    return baos.toString();
	}

	private static void createLogDirIfNotExsist(){
		File theDir = new File(logsDir);
		if (!theDir.exists()) {
			boolean result = theDir.mkdir();
			if(result) {
				System.out.println(String.format("Logs will be saved to '%s' directory", logsDir));
			}
		}
	}

	public static void saveToStatisticalFile(String message){
		createLogDirIfNotExsist();
		//creating file object from given path
		File file = new File(logsDir+File.separator+getApkName()+"_log.txt");
		try {
			file.createNewFile();

			//FileWriter second argument is for append if its true than FileWritter will
			//write bytes at the end of File (append) rather than beginning of file
			FileWriter fileWriter = new FileWriter(file,true);

			//Use BufferedWriter instead of FileWriter for better performance
			BufferedWriter bufferFileWriter  = new BufferedWriter(fileWriter);

			fileWriter.append(message + "\r\n");

			//Don't forget to close Streams or Reader to free FileDescriptor associated with it
			bufferFileWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(0);
		}

	}

	public static void deleteLogFileIfExist() {
		File theDir = new File(logsDir);
		if (!theDir.exists())
			return;

		File logFile = new File(String.format("%s/%s_log.txt", logsDir, getApkName()));
		if (logFile.exists()) {
			logFile.delete();
		}
	}

	public static Set<String> loadSourcesAndSinks(){
		Set<String> excludedClasses = new HashSet<>();
		excludedClasses.add("java.");
		excludedClasses.add("com.google.common.");
		excludedClasses.add("sun.");
		Set<String> sourcesAndSinks = new HashSet<>();

		String parentPath = new File(System.getProperty("user.dir")).getAbsolutePath();
		File susi_api_file = new File(parentPath + File.separator + "backstage" + File.separator + "res" + File.separator + "susi_apis.txt");

		try(BufferedReader br = new BufferedReader(new FileReader(susi_api_file))) {
			for(String line; (line = br.readLine()) != null; ) {
				if(line.trim().length() > 0){
					String trimmedLine = line.trim();
					String className = trimmedLine.split(":")[0].replace("<", "");
					if(!excludedClasses.stream().filter(x->className.contains(x)).findAny().isPresent()) {
						sourcesAndSinks.add(trimmedLine);
					}
				}
			}
		} catch (FileNotFoundException e) {
			saveToStatisticalFile(exceptionStacktraceToString(e));
			e.printStackTrace();
		} catch (IOException e) {
			saveToStatisticalFile(exceptionStacktraceToString(e));
			e.printStackTrace();
		}
		return sourcesAndSinks;
	}

	public static void loadBundlesAndParsable(){
		File fparent = new File (System.getProperty("user.dir"));
		File bundlesAndParsable_file = new File(fparent.getAbsolutePath() + File.separator + "backstage" +
				File.separator + "res" + File.separator + "bundlesAndParsable.txt");
		try(BufferedReader br = new BufferedReader(new FileReader(bundlesAndParsable_file))) {
			for(String line; (line = br.readLine()) != null; ) {
				if(line.trim().length() > 0){
					String[] splittedLine = line.split(";");
					bundlesAndParsable.put(splittedLine[0], splittedLine[1]);
				}
			}
		} catch (IOException e) {
			saveToStatisticalFile(exceptionStacktraceToString(e));
			e.printStackTrace();
		}
	}

	public static void loadNotAnalyzedLibs(){
		File fparent = new File (System.getProperty("user.dir"));
		File android_lib_file = new File(fparent.getAbsolutePath() + File.separator + "backstage" +
				File.separator + "res" + File.separator + "androidAndAdLibs.txt");
		try(BufferedReader br = new BufferedReader(new FileReader(android_lib_file))) {
			for(String line; (line = br.readLine()) != null; ) {
				if(line.trim().length() > 0){
					excludedPrefixes.add(line.trim());
				}
			}
		} catch (FileNotFoundException e) {
			saveToStatisticalFile(exceptionStacktraceToString(e));
			e.printStackTrace();
		} catch (IOException e) {
			saveToStatisticalFile(exceptionStacktraceToString(e));
			e.printStackTrace();
		}
	}

	public static boolean isClassInSystemPackage(String className) { //plus libraries
		return !className.startsWith(Helper.getPackageName()) && (className.startsWith("android.")
				|| className.startsWith("java.")
				|| className.startsWith("javax.")
				|| className.startsWith("sun.")
				|| className.startsWith("org.omg.")
				|| className.startsWith("org.w3c.dom.")
				|| className.startsWith("com.google.")
				|| className.startsWith("com.android.")
				|| className.startsWith("com.facebook."));
	}

	public static boolean isClassInAppNameSpace(String className) {
		return className.startsWith(packageName) || (!Helper.isClassInSystemPackage(className) && appNameSpaces.stream().anyMatch(ns -> className.startsWith(ns)));
	}

	

	public static void trackUsedMemory(){
		Runtime runtime = Runtime.getRuntime();
		usedMemory = (runtime.totalMemory() - runtime.freeMemory());
	}

	public static boolean isIntegerParseInt(String str) {
		try {
			Integer.parseInt(str);
			return true;
		} catch (NumberFormatException nfe) {}
		return false;
	}

	public static boolean processMethod(int bodySize){
		return LOC > bodySize;
	}

}
