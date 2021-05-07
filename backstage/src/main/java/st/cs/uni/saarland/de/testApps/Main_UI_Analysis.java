package st.cs.uni.saarland.de.testApps;

import com.thoughtworks.xstream.XStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import soot.PackManager;
import soot.Transform;
import st.cs.uni.saarland.de.dissolveSpecXMLTags.DissolveXMLTagsMain;
import st.cs.uni.saarland.de.entities.*;
import st.cs.uni.saarland.de.helpClasses.Helper;
import st.cs.uni.saarland.de.saveData.SaveDataForTests;
import st.cs.uni.saarland.de.searchDialogs.SearchDialogMain;
import st.cs.uni.saarland.de.searchDynDecStrings.SearchDynDecMain;
import st.cs.uni.saarland.de.searchListener.SearchListener;
import st.cs.uni.saarland.de.searchMenus.SearchMenusMain;
import st.cs.uni.saarland.de.searchScreens.SearchActivityScreens;
import st.cs.uni.saarland.de.uiAnalysis.UiAnalysisPack;
import st.cs.uni.saarland.de.xmlAnalysis.XMLParserMain;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/* 
 * 
 * written by Isabelle Rommelfanger, November 2014 
 * 
 */

public class Main_UI_Analysis {

	private int errorCount;
	private int appCount;
	static final Logger logger =  LoggerFactory.getLogger(Thread.currentThread().getName());
	private final TimeUnit tTimeoutUnit;
	private final int tTimeoutValue;
	private final int numThreads;
	private final boolean isTest;
	private final boolean processMenus;
	private final int maxDepthMethodLevel;

	// arg[0]: androidSDK path
	// arg[1]: dir path of output Folder
	// arg[2]: apk file path or dir of all apkFiles to anaylse

//	public void recursiveSearchForAPKFiles(File out) {
//
//		// iter through all given apps in appDir if appsDir is a directory
//		if (appsDir.isDirectory()) {
//			File[] apkFiles = appsDir.listFiles();
//			for (File apkFile : apkFiles) {
//				this.recursiveSearchForAPKFiles(androidSDKPath, out, apkFile);
//			}
//		} else {
//			if (appsDir.getName().endsWith(".apk")) {
//				this.runAnalysisForOneApp(out);
//			} else {
//				logger.error("Potential APK File is not an apk file!:" + appsDir.getAbsolutePath());
//			}
//			;
//
//		}
//	}

	public Main_UI_Analysis(TimeUnit tTimeoutUnit, int tTimeoutValue, int numThreads, boolean processMenus, int maxDepthMethodLebel, boolean test){
		this.tTimeoutUnit = tTimeoutUnit;
		this.tTimeoutValue = tTimeoutValue;
		this.numThreads = numThreads;
		this.processMenus = processMenus;
		this.isTest = test;
		this.maxDepthMethodLevel = maxDepthMethodLebel;
	}

	// args:
	public boolean runAnalysisForOneApp(File outputDir, boolean processImages) {


		String logText = String.format("Running UI analysis with timeout for entrypoint class: %s %s", this.tTimeoutValue, this.tTimeoutUnit);
		logger.info(logText);
		Helper.saveToStatisticalFile(logText);

		appCount++;

		// save start time
		long startTime = System.currentTimeMillis();
		String appname = Helper.getApkName().replace(".apk", "");
		String appOutputDir = outputDir.getAbsolutePath() + File.separator + appname;

		try {
			// start analysis
			logger.info("<Start Analysis for app nr: " + appCount + " : " + appname + ">");

			// start apkTool
			// extract xml structure of app with the help of the apkTool, for the given application
			try {
				String cmd = "java -jar " + appOutputDir + File.separator +
						"apktool_2.1.1.jar -s -f d " + Helper.getApkPath() +
						" -o " + appOutputDir;
				Process p = Runtime.getRuntime().exec(cmd);
				InputStream is = p.getInputStream();
				BufferedReader reader = new BufferedReader(new InputStreamReader(is));
				String line;
				while ((line = reader.readLine()) != null) {
					logger.debug(line);
				}
				p.waitFor();
				is.close();
				reader.close();
				p.destroy();
			} catch (IOException|InterruptedException e) {
				logger.error("Failed to run apktool: " + e.getMessage());
			}


			Content.getInstance(appOutputDir);
			Application app = null;
			try {
				// XML analysis
				// search for all UI elements and declared permissions in the XML files
				XMLParserMain xmlparser = XMLParserMain.getInstance(appOutputDir);
				app = xmlparser.xmlParserMain(processImages);
			} catch (Exception e) {
				e.printStackTrace();
				Helper.saveToStatisticalFile(Helper.exceptionStacktraceToString(e));
			}

			// initialise appController
			AppController.getInstance(app);


			if(!isTest) {
				final int countOfActivities;
				if (app != null) {
					countOfActivities = app.getActivities().size();
					final long countOfXMlLayouts = app.getAllXMLLayoutFiles().stream().filter(x -> !(x instanceof Menu) && !x.getName().startsWith("abc_")).count();
					if (countOfXMlLayouts / (double) (countOfActivities) < 0.7) {
						String message = "App has less than 70% of XML Layouts.";
						logger.error(message);
						Helper.saveToStatisticalFile(message);
						return false;
					}
				}
			}

			UiAnalysisPack uiAnalysisPack = new UiAnalysisPack(tTimeoutUnit, tTimeoutValue, numThreads, processMenus, maxDepthMethodLevel, app);
			PackManager.v().getPack("wjtp").add(new Transform("wjtp.UiAnalysis", uiAnalysisPack));
			PackManager.v().runPacks();
			logger.info("UI Analysis Pack is done");

			if (app == null) {
				logger.error("Failed to run UI analysis!");
				return false;
			}

			// TODO Needed
			try {
				logger.info("Resolving XML dependencies");
//				 ressolve XMLTag dependencies
				DissolveXMLTagsMain dissolveXMLTags = new DissolveXMLTagsMain();
				dissolveXMLTags.dissolveXMLTags(uiAnalysisPack.getFragments(), uiAnalysisPack.getTabViews());
			} catch (Exception e) {
				logger.error(app.getName() + ": " + e.getMessage());
				e.printStackTrace();
				Helper.saveToStatisticalFile(Helper.exceptionStacktraceToString(e));
			}

			try {
				// get the dynamical declared texts
				logger.info("Searching for dynamically declared text");
				SearchDynDecMain searchDynDec = new SearchDynDecMain();
				searchDynDec.searchDynDeclaredStrings(uiAnalysisPack.getStrings());
			} catch (Exception e) {
				logger.error(app.getName() + ": " + e);
				e.printStackTrace();
				Helper.saveToStatisticalFile(Helper.exceptionStacktraceToString(e));
			}


			try {
				// get all the UIElements, located at the one screen, together ~ grouping
				logger.info("Grouping UI elements");
				SearchActivityScreens screenMain = new SearchActivityScreens();
				screenMain.runSearchActivityScreens(uiAnalysisPack.getLayouts());
			} catch (Exception e) {
				logger.error(app.getName() + ": " + e);
				Helper.saveToStatisticalFile(Helper.exceptionStacktraceToString(e));
			}

			// TODO needed
			try {
				logger.info("Processing menus");
				SearchMenusMain searchMenus = new SearchMenusMain();
				searchMenus.getMenusInApp(uiAnalysisPack.getOptionMenus(), uiAnalysisPack.getContextMenus(),
						uiAnalysisPack.getContextOnCreateMenus(), uiAnalysisPack.getPopupMenus(), uiAnalysisPack.getNavigationMenus());
			} catch (Exception e) {
				logger.error(app.getName() + ": " + e);
				e.printStackTrace();
				Helper.saveToStatisticalFile(Helper.exceptionStacktraceToString(e));
			}

			// Needed for getDialogs
			try {
				logger.info("Processing dialogs");
				// search dynamical build Dialogs
				SearchDialogMain diaMain = new SearchDialogMain();
				diaMain.getDialogsOfApp(uiAnalysisPack.getDialogResults());
			} catch (Exception e) {
				logger.error(app.getName() + ": " + e.getMessage());
				e.printStackTrace();
				Helper.saveToStatisticalFile(Helper.exceptionStacktraceToString(e));
			}

			try {
				logger.info("Processing listeners");
				// search for listener
				SearchListener sl = new SearchListener();
				sl.runSearchListener(uiAnalysisPack.getListeners());
			} catch (Exception e) {
				logger.error(app.getName() + ": " + e);
				e.printStackTrace();
				Helper.saveToStatisticalFile(Helper.exceptionStacktraceToString(e));
			}

			// TODO: needed for get menus
			//Mapping activities to layouts
			for (XMLLayoutFile layoutFile : app.getAllXMLLayoutFiles()) {
				if (layoutFile instanceof Menu) {
					Menu newMenu = (Menu) layoutFile;
					app.addMenu(newMenu.getId(), newMenu);
				}
			}
			final Set<Activity> activities = app.getActivities();
			final Map<String, Set<Integer>> activityToXmlLayoutFiles = app.getActivityToXMLLayoutFiles();
			activityToXmlLayoutFiles.keySet().forEach(acName -> {
				Set<Integer> layoutIds = activityToXmlLayoutFiles.get(acName);
				Optional<Activity> optionalActivity = activities.stream().filter(x -> x.getName().equals(acName)).findFirst();
				optionalActivity.ifPresent(activity -> layoutIds.forEach(activity::addXmlLayout));
			});
			for (Activity ac : app.getActivities()) {
				if (ac.getLabel().startsWith("@string/")) {
					ac.setLabel(Content.getInstance().getStringValueFromStringName(ac.getLabel().replace("@string/", "")));
				}
				else if(!ac.getLabel().isEmpty() && Character.isDigit(ac.getLabel().charAt(0))){
					ac.setLabel(Content.getInstance().getStringValueFromStringId(ac.getLabel()));
				}
			}

			// save the data of the UIElements with their listeners
			try {
				logger.info("Saving appSerialized");
//				this.getSerializedOutput(appOutputDir, app);
//				this.saveElementsData(new File(appOutputDir + File.separator + "UIElements.txt"), app);
//				this.saveOnlyButtonData(new File(appOutputDir + File.separator + "buttons.txt"), app);
//				this.getScreensInFile(appOutputDir, app);
//				this.saveXMLLayoutFiles(appOutputDir, app);

				this.saveClassToClassOrLayoutFile(appOutputDir, app);
			} catch (IOException e1) {
				e1.printStackTrace();
				Helper.saveToStatisticalFile(Helper.exceptionStacktraceToString(e1));
			}

			// save the results for testing purposes in a simple XML file
			if (this.isTest) {
				SaveDataForTests.getInstance().saveIDWithListenerAndText(app);
				SaveDataForTests.getInstance().saveActivityToLayout(app);
			}

			// end of analysis for this app
			logger.info("</Start Analysis>");
			return true;
		} catch (Exception e) {
			logger.trace(appname + ": " + e);
			e.printStackTrace();
			Helper.saveToStatisticalFile(Helper.exceptionStacktraceToString(e));
			throw e;
		}
	}

	

	private void saveActivityToLayout(String appOutputDir, Application app) throws IOException {
		StringBuilder list = new StringBuilder();

		for (Entry<String, Set<Integer>> entry : app.getActivityToXMLLayoutFiles().entrySet()){
			StringBuilder res = new StringBuilder();
			for (int id : entry.getValue())
				res.append(id).append("; ");
			list.append(entry.getKey()).append(": ").append(res).append("\n");
		}


		File f = createFile(appOutputDir, "activitiesToLayoutIds");

		try (FileOutputStream writerStream = new FileOutputStream(f, true)) {
			byte[] b = list.toString().getBytes();
			writerStream.write(b);

		}

	}
	
	private void saveClassToClassOrLayoutFile(String appOutputDir, Application app) throws IOException {
		String list = "";

		for (Entry<Integer, Set<Integer>> entry : app.getMergedLayoutFileIDs().entrySet()){
			String res = "";
			for (int id : entry.getValue())
				res = res + id + "; ";
			list = list + entry.getKey() + ": " +  res + "\n";
		}


		File f = createFile(appOutputDir, "classesToClasseOrLayoutMapping");

		FileOutputStream writerStream = new FileOutputStream(f, true);
		try {
			byte[] b = list.getBytes();
			writerStream.write(b);

		} finally {
			writerStream.close();
		}

	}

	public File createFile(String path, String fileName) {
		File f = null;
		if ((path != null) && (!path.equals(""))) {
			String filePath = path + File.separator + fileName + ".txt";

			f = new File(filePath);
			if (f.exists())
				f.delete();

			try {
				f.createNewFile();
			} catch (IOException e) {
				logger.error(e.getMessage());
				Helper.saveToStatisticalFile(Helper.exceptionStacktraceToString(e));
			}
		}
		return f;
	}

	public String getAppName(File apkFile) {
		// check is apkFile is really a .apk file
		if ((apkFile.isFile()) && (apkFile.getName().contains(".apk"))) {

			// create appName
			return apkFile.getName().replace(".apk", "");
		} else
			logger.error("Given appFile is not a file or not an apk file!");
		return null;
	}

	/*
	 * public static void saveUnrecognizedPermissions(String filePath){ if (Main.unrecognizedPermissions.size() > 0){
	 * File f = new File(filePath + File.separator + "unrecPermissions.txt");
	 * 
	 * if (!f.exists()) try { f.createNewFile(); } catch (IOException e1) { // TODO Auto-generated catch block
	 * e1.printStackTrace(); }
	 * 
	 * FileOutputStream writerStream = null; try{ try { writerStream = new FileOutputStream(f, true); } catch
	 * (FileNotFoundException e) { // TODO Auto-generated catch block e.printStackTrace(); } byte[] sep =
	 * "//".getBytes(); for (String s : Main.unrecognizedPermissions){ byte[] by = s.getBytes(); try {
	 * writerStream.write(by); writerStream.write(sep); } catch (IOException e) { // TODO Auto-generated catch block
	 * e.printStackTrace(); } } Main.unrecognizedPermissions = new HashSet<String>(); }finally{ try {
	 * writerStream.close(); } catch (IOException e) { // TODO Auto-generated catch block e.printStackTrace(); }
	 * 
	 * } }
	 * 
	 * }
	 */

	public static void saveTime(String appOutFolder, long time) throws IOException {

		double minutes = time / (double) 60000;

		File stat = new File(appOutFolder + File.separator + "statistics.txt");

		FileOutputStream writerStream = null;
		try {
			writerStream = new FileOutputStream(stat, true);
			byte[] lineSep = "//".getBytes();
			writerStream.write(lineSep);
			byte[] tmp = ("needed time: " + String.valueOf(minutes) + " minutes").getBytes();
			writerStream.write(tmp);
		} finally {
			writerStream.close();
		}
	}

	public void saveElementsData(File butFile, Application app) throws IOException {

		if (butFile != null) {
			String pathToElementsFile = butFile.getParentFile().getAbsolutePath();
			String fileName = butFile.getName().replace(".txt", "");
			if (butFile.exists())
				butFile.delete();
			createFile(pathToElementsFile, fileName);
			FileOutputStream writerStream = new FileOutputStream(butFile, true);
			try {
				String stringForSaving = app.getStringForsavingAllUIElementAttributes();
				byte[] b = stringForSaving.getBytes();
				writerStream.write(b);

				if (stringForSaving.length() < 1)
					writerStream.write("no UIelements found".getBytes());
			} finally {
				writerStream.close();
			}
		}
	}

	public void saveOnlyButtonData(File butFile, Application app) throws IOException {

		if (butFile != null) {
			String pathToElementsFile = butFile.getParentFile().getAbsolutePath();
			String fileName = butFile.getName().replace(".txt", "");
			if (butFile.exists())
				butFile.delete();
			createFile(pathToElementsFile, fileName);
			FileOutputStream writerStream = new FileOutputStream(butFile, true);
			try {
				for (AppsUIElement bt : app.getAllUIElementsOfApp()) {
					if (bt.getKindOfUiElement().equals("Button")) {
						String stringForSaving = bt.attributesForSavingToString(app.getName());
						byte[] b = stringForSaving.getBytes();
						writerStream.write(b);

						if (stringForSaving.length() < 1)
							writerStream.write("no UIelements found".getBytes());
					}
				}
			} finally {
				writerStream.close();
			}
		}
	}

	// FIXME -> Checkbox of PopupMenu with Listeners is not included in file
//	public void getListenersInFile(String outPath, Application app) throws IOException {
//		File f = createFile(outPath, "listeners");
//
//		FileOutputStream writerStream = new FileOutputStream(f, true);
//		try {
//			for (AppsUIElement bt : app.getAllUIElementsOfApp()) {
//				if (bt instanceof SpecialXMLTag)
//					// if ("include".equals(((SpecialXMLTag) bt).getKindOfUiElement()))
//					// continue;
//					continue;
//				String stringForSaving = "";
//				for (Listener l : bt.getListernersFromElement()) {
//					stringForSaving = stringForSaving + bt.getUIID() + ";" + l.getSignature() + "\n";
//				}
//				byte[] b = stringForSaving.getBytes();
//				writerStream.write(b);
//			}
//		} finally {
//			writerStream.close();
//		}
//	}

//	public void getScreensInFile(String outPath, Application app) throws IOException {
//		File f = createFile(outPath, "screensWitListeners");
//
//		FileOutputStream writerStream = new FileOutputStream(f, true);
//		try {
//			for (Screen sc : app.getScreens()) {
//				String stringForSaving = "";
//
//				stringForSaving = sc.getActivityName() + ": " + sc.getXMLLayoutFilesIDs() + ":" + "\n";
//				byte[] b = stringForSaving.getBytes();
//				writerStream.write(b);
//				stringForSaving = "";
//				for (Listener list : sc.getListenersFromScreen()) {
//					stringForSaving = stringForSaving + list.getSignature() + "\n";
//
//					b = stringForSaving.getBytes();
//					writerStream.write(b);
//				}
//			}
//		} finally {
//			writerStream.close();
//		}
//	}
	//FIXME: currently not configured at all
	public void getSerializedOutput(String outPath, Application app) throws IOException {
		File f = createFile(outPath, "appSerialized");
		
		XStream xstream = new XStream();
		xstream.setMode(XStream.ID_REFERENCES);
		xstream.alias("AppsUIElement", AppsUIElement.class);
		String xml = xstream.toXML(app);
		
		try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f), StandardCharsets.UTF_8))) {
			bw.append(xml);
		} catch (IOException exception) {
			Helper.saveToStatisticalFile(exception.getMessage());
			logger.error(exception.getMessage());
		}
	}
	
	private void saveXMLLayoutFiles(String appOutputDir, Application app) throws IOException {
		File f = createFile(appOutputDir, "xmlLayoutFiles");

		FileOutputStream writerStream = new FileOutputStream(f, true);
		try {
			for (XMLLayoutFile xmlF: app.getAllXMLLayoutFiles()){
				writerStream.write(xmlF.toString().getBytes());
				writerStream.write("\n".getBytes());
			}
			for (Dialog d : app.getDialogsOfApp()){
				writerStream.write(d.toString().getBytes());
				writerStream.write("\n".getBytes());
			}
		} finally {
			writerStream.close();
		}
		
	}

	public void deleteFolder(File folder) {
		File[] files = folder.listFiles();
		if (files != null) {
			for (File f : files) {
				if (f.isDirectory()) {
					deleteFolder(f);
				} else
					f.delete();
			}
		}
		folder.delete();
	}

}
