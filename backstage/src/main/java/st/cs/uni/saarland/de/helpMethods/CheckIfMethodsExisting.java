package st.cs.uni.saarland.de.helpMethods;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import soot.Scene;
import soot.SootMethod;
import st.cs.uni.saarland.de.helpClasses.Helper;
import st.cs.uni.saarland.de.helpClasses.InterProcInfo;
import st.cs.uni.saarland.de.searchScreens.LayoutInfo;
import st.cs.uni.saarland.de.searchScreens.StmtSwitchForReturnedLayouts;
import st.cs.uni.saarland.de.testApps.AppController;
import st.cs.uni.saarland.de.testApps.Content;

import java.io.File;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.*;

public class CheckIfMethodsExisting {

	private static final CheckIfMethodsExisting thisClassObject = new CheckIfMethodsExisting();
	private final Logger logger =  LoggerFactory.getLogger(Thread.currentThread().getName());
	
	 public static CheckIfMethodsExisting getInstance(){
		 return thisClassObject;
	 }
	
	 public InterProcInfo getInterProcCall(String methodSignature){
//			StmtSwitchForInterProcCalls stmtswitch = (StmtSwitchForInterProcCalls ) InterprocAnalysis.newInstance().runStmtSwitchOnSpecificMethodBackward(new StmtSwitchForInterProcCalls(), methodSignature);
//			 return stmtswitch.getResultInfo();
		 return null;
		}
	
	public String getResolvedText(String text){
		String res = "";
		
		if (!StringUtils.isBlank(text)) {
			// parse the strings.xml file
			// 1st with the default values:
			String allTextValues[] = text.split("#");
			Set<String> tmpText = new HashSet<String>();

			// TODO change like in layoutParser with regex
			for (String singleTextVar : allTextValues) {
				if ((!StringUtils.isEmpty(singleTextVar))) {
					if (checkIfValueIsID(singleTextVar)){
						String posText = Content.getInstance().getArrayValueFromArrayID(singleTextVar);
						if (StringUtils.isEmpty(posText)){
							posText = Content.getInstance().getStringValueFromStringId(singleTextVar);
						}
						tmpText.add(posText);
					}else{
						if(singleTextVar.startsWith("@string")){
							String candidate = singleTextVar.replace("@string/", "");
							tmpText.add(Content.getInstance().getStringValueFromStringName(candidate));
						}
						else{
							tmpText.add(singleTextVar);
						}
					}
					
				}
			}
			res = String.join("#", tmpText);
		}
		return res;
	}

	// returns if that string is a class name and especially not a register from analysis and not empty or blank
	public boolean isNameOrText(String name){
		if (StringUtils.isBlank(name) || name.contains("$"))
			return false;
		else
			return true;
	}
	
	public boolean isMethodExisting(String className, String methodSubSignature){
		
		return Scene.v().containsMethod("<" + className +": " + methodSubSignature +">");
	}

	public boolean checkIfValueIsID(String value){
		return StringUtils.isBlank(value)? false : value.matches("[0123456789]+");
	}
	
	public boolean checkIfValueIsVariable(String value){
		if (!checkIfValueIsString(value) && (!checkIfValueIsID(value))){
			return true;
		}else
			return false;
	}

	// note: for return class "fragment1", this should return true that is why it is with contains(\"") and not with startsWith/endsWith
	public boolean checkIfValueIsString(String value){
		if ((!value.contains("$")) && value.contains("\"")/*(value.startsWith("\"")) && (value.endsWith("\""))*/)
			return true;
		else
			return false;		
	}

	public Collection<Integer> getFragmentViews(String fragmentClassName) {
		AppController appController = AppController.getInstance();

		if (StringUtils.isBlank(fragmentClassName))
			return new HashSet<Integer>();

		Collection<Integer> fragViewIds = appController.getViewsOfFragmentClass(fragmentClassName);
		if (fragViewIds != null){
			return fragViewIds;
		}else{
			// the fragment was not analysed before, so do it now
			// create the method signature which is searched for
			String signature = "<" + fragmentClassName + ": android.view.View onCreateView(android.view.LayoutInflater,android.view.ViewGroup,android.os.Bundle)>";
			SootMethod m;
			// check if this method is existing
			try {
				m = Scene.v().getMethod(signature);
			} catch (RuntimeException e) {
				// method is not existing so fragment has no view
				return null;
			}

			// create new switch for this exercise
			StmtSwitchForReturnedLayouts retLaySwitch = new StmtSwitchForReturnedLayouts(null);
			// start the analysis
			//a quite heavy place as the experienece says..

			ExecutorService executorService = Executors.newSingleThreadExecutor();
			Map<Integer, LayoutInfo> layInfos = new HashMap<>();
			Future<Void> task = executorService.submit(new Callable<Void>() {
				@Override
				public Void call() throws Exception {
					layInfos.putAll(InterprocAnalysis.getInstance().runFragmentGetViewSwitchOverUnits(retLaySwitch, m));
					return null;
				}
			});

			try {
				task.get(10, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				logger.error("getFragmentViews: interrupted for "+fragmentClassName);
				task.cancel(true);
			} catch (ExecutionException e) {
				logger.error("getFragmentViews: executor exception for "+fragmentClassName);
				Helper.saveToStatisticalFile(Helper.exceptionStacktraceToString(e));
				task.cancel(true);
			} catch (TimeoutException e) {
				logger.error("getFragmentViews: timeout for "+fragmentClassName);
				task.cancel(true);
			}

			executorService.shutdownNow();

			try {
				executorService.awaitTermination(30, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				logger.error("Executor did not terminate correctly");
			} finally {
				while (!executorService.isTerminated()) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						Helper.saveToStatisticalFile(Helper.exceptionStacktraceToString(e));
						e.printStackTrace();
					}
					logger.info("Waiting for finish");
				}
			}

			// process the results of analysis
			Set<Integer> layoutIDs = new HashSet<Integer>();
			for (Entry<Integer, LayoutInfo> entry : layInfos.entrySet()) {
				LayoutInfo layout = entry.getValue();
				if (layout.allValuesFound() && layout.isFragment()) {
					if (layout.hasLayoutID()) {
						int viewID = Integer.parseInt(layout.getLayoutID());
						layoutIDs.add(viewID);
					}
				}
			}
			appController.addFragmentClassToItsViewIDs(fragmentClassName, layoutIDs);
			return layoutIDs;
		}
	}


	public boolean checkIfDrawableIsPng(String pathToResFolder, String drawableName){
		
		File resFolder = new File(pathToResFolder);
		File[] resFolderFiles = resFolder.listFiles();
		
		for (File resFolderFile : resFolderFiles){
			if (resFolderFile.getName().contains("drawable")){
				if (resFolderFile.isDirectory()){
					File[] drawableFiles = resFolderFile.listFiles();
					for (File drawable: drawableFiles){
						String[] splittedName = drawable.getName().split(".");
						if (splittedName.equals(drawableName)){
							if ((splittedName.length > 1) && (splittedName[1].equals("png"))){
								return true;
							}
						}
					}
				}
			}
		}
		
		
		return false;
	}
}

// if file with this id was not found, null is returned
// first element is the main layout
//	public List<XMLLayoutFile> getFragmentViews(String fragmentClass){
//		XMLLayoutFile mainLayout = null;
//		List<XMLLayoutFile> layouts = new ArrayList<XMLLayoutFile>();
////			if (app.getMergedXMLLayoutFiles().containsKey(fragInfo.getFragmentClassName())){
////				mainLayout = app.getXMLLayoutFileFromItsId(app.getMergedXMLLayoutFiles().get(fragInfo.getFragmentClassName()));
////			}else{
//			// find the View of the Fragment
//			String fragmentOnCreateViewSig = "<" + fragmentClass + ": " + "android.view.View onCreateView(android.view.LayoutInflater,android.view.ViewGroup,android.os.Bundle)>";
//			SootMethod m;
//			try{
//				m = Scene.v().getMethod(fragmentOnCreateViewSig);
//			}
//			catch (RuntimeException e){
//				m = null;
//			}
//			Map<Integer, LayoutInfo> onCreateViewInfos = InterprocAnalysis.getInstance().runFragmentGetViewSwitchOverUnits(new StmtSwitchForReturnedLayouts(null), m);
////			Map<Integer, LayoutInfo> onCreateViewInfos = stmtSwitch.getResults();
////			List<Info> stmtSwitch = InterprocAnalysis.newInstance().runStmtSwitchOnSpecificMethodForward(new StmtSwitchForLayoutInflater(), fragmentOnCreateViewSig);
////			List<LayoutInfo> onCreateViewInfos = new ArrayList<LayoutInfo>();
////			for (Info i : stmtSwitch){
////				onCreateViewInfos.add((LayoutInfo) i);
////			}
//
//			// fragView are the Views of the fragmentClass from fragInfo
//			for (Entry<Integer, LayoutInfo> entry: onCreateViewInfos.entrySet()){
//				LayoutInfo fragView = entry.getValue();
////			for (LayoutInfo fragView : onCreateViewInfos){
//				String layoutId = fragView.getLayoutID();
//				// check that this layout is the main View that is returned
//				if (fragView.isFragment()){
//					try{
//						mainLayout = app.getXMLLayoutFileFromItsId(layoutId);
//						app.addClassToLayoutFileOrClass(fragmentClass, mainLayout.getId());
//
//						// add the main layout to the map of activity-LayoutIDs
//						layouts.add(mainLayout);
//						app.addClassToLayoutFileOrClass(fragmentClass, layoutId);
//						// add the found added layouts (to the main layout) to the mapping
//						for (Integer addedLayoutID : fragView.getAddedLayouts()){
//							LayoutInfo addedLayout = onCreateViewInfos.get(addedLayoutID);
//							if (!addedLayout.getLayoutID().equals("")){
//								app.addClassToLayoutFileOrClass(fragmentClass, addedLayout.getLayoutID());
//								layouts.add(app.getXMLLayoutFileFromItsId(addedLayout.getLayoutID()));
//							}
//						}
//					}catch(IllegalArgumentException e){
//						logger.error(app.getName() + ": Didn't find XMLLayoutID: " + layoutId + "; " + e);
//					}
//				}
////				}
//		}
//		return layouts;
//	}
