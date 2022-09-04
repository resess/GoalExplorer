package st.cs.uni.saarland.de.searchTabs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.jimple.*;
import st.cs.uni.saarland.de.helpClasses.Helper;
import st.cs.uni.saarland.de.helpClasses.InterProcInfo;
import st.cs.uni.saarland.de.helpClasses.MyStmtSwitchForResultLists;
import st.cs.uni.saarland.de.helpMethods.IterateOverUnitsHelper;
import st.cs.uni.saarland.de.reachabilityAnalysis.AnalyseIntentSwitch;
import st.cs.uni.saarland.de.reachabilityAnalysis.IntentInfo;
import st.cs.uni.saarland.de.testApps.Content;

import java.util.*;

public class StmtSwitchForTab extends MyStmtSwitchForResultLists {

	private String hostReg = "";
	private String activityClassName = "";
	private List<AnalyseIntentSwitch> intents = new ArrayList<>();
	private InvokeExpr tabCreationInvokeExpr;
	private boolean isTabHost;
	private final Logger logger =  LoggerFactory.getLogger(this.getClass());

	public StmtSwitchForTab(TabInfo newInfo, SootMethod currentSootMethod) {
		super(currentSootMethod);
		addToResultedTabs(newInfo);
	}

	public StmtSwitchForTab(SootMethod currentSootMethod) {
		super(currentSootMethod);
	}

	public void setHostReg(String hostReg){
		this.hostReg = hostReg;
	}

	public void setTabCreationInvokeExpr(InvokeExpr invokeExpr){
		this.tabCreationInvokeExpr = invokeExpr;
	}

	@Override
	public void caseIdentityStmt(IdentityStmt stmt) {
		if(Thread.currentThread().isInterrupted()){
			return;
		}
		processIntents(stmt);
		//r1 = @parameter0
		String leftReg = helpMethods.getLeftRegOfIdentityStmt(stmt), rightReg = stmt.getRightOp().toString();
		for(TabInfo info: getResultedTabs()){
			if(leftReg.equals(info.getContentReg())) {
				info.setContentReg(rightReg.split(":")[0]);
			}
			else if(info.getContent() != null && leftReg.equals(info.getContent().getClassNameReg())){
				info.getContent().setClassNameReg(rightReg.split(":")[0]);
			}
			else if(leftReg.equals(info.getIndicatorTextReg())){
				info.setIndicatorTextReg(rightReg.split(":")[0]);
			}
			else if(leftReg.equals(info.getIndicatorTextResIDReg())) {
				info.setIndicatorTextResIDReg((rightReg.split(":")[0]));
			}
		}
	}

	public void caseAssignStmt(AssignStmt stmt){
		if(Thread.currentThread().isInterrupted()){
			return;
		}

		processIntents(stmt);

		String leftReg = helpMethods.getLeftRegOfAssignStmt(stmt);
		String rightReg = helpMethods.getRightRegOfAssignStmt(stmt);

		//Deal wigh getString(reg1) later on

		if (stmt.containsInvokeExpr()) {
			InvokeExpr invokeExpr = stmt.getInvokeExpr();
			String methodSignature = helpMethods.getSignatureOfInvokeExpr(invokeExpr);
			String caller = helpMethods.getCallerOfInvokeStmt(invokeExpr);

			if (methodSignature.contains(": android.widget.TabHost getTabHost()>")){
				logger.info(getCurrentSootMethod().getSignature() +  " get tab host");
				if (hostReg.equals(leftReg)) {
					isTabHost = true;
					hostReg = "";
					activityClassName = helpMethods.getCallerTypeOfInvokeStmt(invokeExpr);
					checkLineUp();
					shouldBreak = true;
				}
			}else if("<android.widget.TabHost$TabSpec: android.widget.TabHost$TabSpec setIndicator(java.lang.CharSequence,android.graphics.drawable.Drawable)>".equals(methodSignature) ||
					 "<android.widget.TabHost$TabSpec: android.widget.TabHost$TabSpec setIndicator(java.lang.CharSequence)>".equals(methodSignature)) {
				// Does not support custom views
				TabInfo tabInfo = getCallerInfo(caller);
				if (tabInfo != null){
					String text = helpMethods.getParameterOfInvokeStmt(invokeExpr, 0);
					if (checkMethods.checkIfValueIsVariable(text)){
						tabInfo.setIndicatorTextReg(text);
					}else{
						//Here need to resolve text
						tabInfo.setIndicatorText(text.substring(1, text.length() - 1));
					}
				}
			}else if("<android.widget.TabHost$TabSpec: android.widget.TabHost$TabSpec setContent(android.content.Intent)>".equals(methodSignature)) {
				TabInfo tabInfo = getCallerInfo(caller);
				if (tabInfo != null){
					if(tabInfo.getContent() != null) {
						logger.warn("Content is set more than once for a tab");
					} else {

						//here we need to do something about resolving the intent? might be tough, need to look into
						Value intentReg = invokeExpr.getArg(0);
						IntentInfo content = new IntentInfo();
						AnalyseIntentSwitch intentSwitch = new AnalyseIntentSwitch(intentReg, getCurrentSootMethod(), content);
						intents.add(intentSwitch);
						tabInfo.setContent(content);
						logger.info("Intent added to list for analysis");
					}

				} else {
					logger.warn("Corresponding tab not found for " + caller + " while setting content");
					for(TabInfo tab : getResultedTabs()) {
						logger.warn("Tab info reg: " + tab.getSearchedEReg());
					}
				}
			} else { //TODO check listview more (custom list views test and spinner test and array of classes as well)
				for(TabInfo tab : getResultedTabs()) {
					if(leftReg.equals(tab.getSearchedEReg())){
						//The function returns a tab
						//We need to do the same analysis on that tab I guess
						if(!Helper.isClassInSystemPackage(helpMethods.getCallerTypeOfInvokeStmt(invokeExpr)) && invokeExpr.getMethod().hasActiveBody()) {
							Unit lastUnitOfMethod = invokeExpr.getMethod().getActiveBody().getUnits().getLast();
							if (lastUnitOfMethod instanceof ReturnStmt) {
								String returnValue = ((ReturnStmt) lastUnitOfMethod).getOp().toString();
								if (checkMethods.checkIfValueIsVariable(returnValue)) {
									TabInfo newTab = new TabInfo(returnValue);
									StmtSwitchForTab newStmtSwitch = new StmtSwitchForTab(newTab, invokeExpr.getMethod()); //probabbly need to go forward here?
									//Need to pass the calling info I guess
									newStmtSwitch.setHostReg(hostReg);
									newStmtSwitch.setTabCreationInvokeExpr(invokeExpr);

									iteratorHelper.runOverToFindSpecValuesBackwards(invokeExpr.getMethod().getActiveBody(), lastUnitOfMethod, newStmtSwitch);
									//IterateOverUnitsHelper.newInstance().runUnitsOverMethodBackwards(invokeExpr.getMethod().getActiveBody(), newStmtSwitch);
									//Maybe it's still fill if I go backwards, I just need to store special information in register
									//Like if I see a method like getString, I know  what I need to track is the parameter, so I store it in a special reg thingie
									//Then I resolved it afterwards?
									//e.g et
									//newStmtSwitch.setTabCreationStmt(tabCreationStmt);
									for (TabInfo newInfo : newStmtSwitch.getResultedTabs()) {
										//logger.debug("Found tab interprocedurally {}", newInfo);
										boolean needReg = true;
							/*if (newInfo.getContent() != null) {
								tab.setContent(newInfo.getContent());
								needReg = false;
							}
							if (!newInfo.getIndicatorText().isEmpty()) {
								tab.setIndicatorText(newInfo.getIndicatorText());
								needReg = false;
							}*/
										//Does not work cause these are only used as parameters in subsequent functions, i.e newIntent(param0, ...)
										//Maybe we should go forward for that one?
										if (needReg) { //tab should have been modified in place
											for (int i = 0; i < invokeExpr.getArgCount(); i++) {
												String param = "@parameter" + i;
												if (newInfo.getContentReg().equals(param)) {
													//issue here, we're probably passing the class as an input?
													//so is there a way look into AnalyzeIntentInfo
													tab.setContentReg(helpMethods.getParameterOfInvokeStmt(invokeExpr, i));
												} else if (newInfo.getContent() != null && newInfo.getContent().getClassNameReg().equals(param)) {
													tab.setContent(newInfo.getContent());
													if (invokeExpr.getArg(i) instanceof ClassConstant) {
														tab.getContent().setClassName(((ClassConstant) invokeExpr.getArg(i)).getValue());
													}
												} else if (newInfo.getIndicatorTextReg().equals(param))
													tab.setIndicatorTextReg(helpMethods.getParameterOfInvokeStmt(invokeExpr, i));
												else if (newInfo.getIndicatorTextResIDReg().equals(param)) {
													String id = helpMethods.getParameterOfInvokeStmt(invokeExpr, i);
													if (checkMethods.checkIfValueIsID(id)) {
														tab.setIndicatorTextResID(id);
														tab.setIndicatorText(Content.getInstance().getStringValueFromStringId(id));
														tab.setIndicatorTextResIDReg("");

													} else {
														tab.setIndicatorTextResIDReg(newInfo.getIndicatorTextResIDReg());
													}
												}
											}
											//There should also be the case where it was fully resolved already
										}
									}
								}
							}
						}
					}
					else if (leftReg.equals(tab.getIndicatorTextReg())) {
						tab.setIndicatorTextReg("");
						if(methodSignature.equals("<android.content.res.Resources: java.lang.CharSequence getTextFromElement(int)>") ||
								methodSignature.equals("<android.content.res.Resources: java.lang.CharSequence getText(int)>") ||
								methodSignature.equals("<android.content.res.Resources: java.lang.String getTextFromElement(int,CharSequence)>") ||
								methodSignature.equals("<android.content.res.Resources: java.lang.String getString(int)>") ||
								invokeExpr.getMethod().getSubSignature().startsWith("java.lang.String getString(int") ||
								methodSignature.equals("<android.content.res.Resources: java.lang.String[] getStringArray(int)>")) {

							//Here check if the register is an integer, otherwise we can have something like indicatprTextResIdReg

							String id = helpMethods.getParameterOfInvokeStmt(invokeExpr, 0);
							if(checkMethods.checkIfValueIsID(id)) {
								tab.setIndicatorTextResID(id);
								tab.setIndicatorText(Content.getInstance().getStringValueFromStringId(id));
								logger.info("Tab indicator: " + tab.getIndicatorText() + " tab indicator id: " + tab.getIndicatorTextResID());
							}
							else
								tab.setIndicatorTextResIDReg(id);

						} else {
							List<InterProcInfo> resList = interprocMethods2.findReturnValueInMethod2(stmt);
							if (resList.size() > 0) {
								tab.setIndicatorText((((InterProcInfo) resList.get(0)).getValueOfSearchedReg()));
								if (resList.size() > 1) {
									for (int j = 1; j < resList.size(); j++) {
										InterProcInfo workingInfo = (InterProcInfo) resList.get(j);
										tab.setIndicatorText(workingInfo.getValueOfSearchedReg());
									}
								}
							}
						}
					}
				}
			}
		} else {
			for(TabInfo tab : getResultedTabs()) {
				if(leftReg.equals(tab.getIndicatorTextReg())) {

					tab.setIndicatorTextReg("");
					if(stmt.getRightOp() instanceof FieldRef) {
						// TODO: Take another look at this.
//
//						// Full disclosure. I only barely understand what this code is doing.
//						SootField f = ((FieldRef)stmt.getRightOp()).getField();
//						if(previousFields.contains(f)){
//							if(!previousFieldsForCurrentStmtSwitch.contains(f)){
//								continue;
//							}
//						}else{
//							previousFields.add(f);
//							previousFieldsForCurrentStmtSwitch.add(f);
//						}
//						Set<FieldInfo> fInfos = interprocMethods2.findInitializationsOfTheField2(f, stmt, getCurrentSootMethod());
//						if(fInfos.size() > 0){
//							for (FieldInfo fInfo : fInfos){
//								if(Thread.currentThread().isInterrupted()){
//									return;
//								}
//								if(fInfo.value != null){
//									tab.setIndicatorText(fInfo.value);
//									continue;
//								}else{
//									if(fInfo.methodToStart != null && fInfo.methodToStart.method().hasActiveBody()){
//
//										Unit workingUnit = fInfo.unitToStart;
//										TabInfo newInfo = new TabInfo("");
//										newInfo.setIndicatorTextReg(fInfo.register.getName());
//										StmtSwitchForTab newStmtSwitch = new StmtSwitchForTab(newInfo, getCurrentSootMethod());
//										previousFields.forEach(x->newStmtSwitch.addPreviousField(x));
//										iteratorHelper.runOverToFindSpecValuesBackwards(fInfo.methodToStart.method().getActiveBody(), workingUnit, newStmtSwitch);
//										Set<Info> initValues = newStmtSwitch.getResultInfos();
//
//										if(initValues.size() > 0) {
//											List<Info> listInfo = initValues.stream().collect(Collectors.toList());
//											if(listInfo.indexOf(newInfo) == -1){
//												continue;
//											}
//											Info initInfo = listInfo.get(listInfo.indexOf(newInfo));
//											tab.setIndicatorText(((TabInfo)initInfo).getIndicatorText());
//											logger.info("Set indicator text1: " + tab.getIndicatorText());
//										}
//									}
//								}
//							}
//						}else{
//							Helper.saveToStatisticalFile("Error TabSwitch: Doesn't find indicatorTextReg in initializationOfField: " + stmt);
//						}

					} else {
						tab.setIndicatorTextReg(helpMethods.getRightRegOfAssignStmt(stmt));
					}
				}
			}
		}
	}
	
	
	public void caseInvokeStmt(InvokeStmt stmt) {
		if(Thread.currentThread().isInterrupted()){
			return;
		}

		processIntents(stmt);

		InvokeExpr invokeExpr = stmt.getInvokeExpr();
		String methodSignature = helpMethods.getSignatureOfInvokeExpr(invokeExpr);
		String caller = helpMethods.getCallerOfInvokeStmt(invokeExpr);

		if("<android.widget.TabHost: void addTab(android.widget.TabHost$TabSpec)>".equals(methodSignature)) {


			if(hostReg.equals("")) {
				hostReg = caller;
			} else if(!hostReg.equals(caller)) {
				logger.warn("Multiple TabHosts were found for one activity");
			}

			String tabReg = helpMethods.getParameterOfInvokeStmt(invokeExpr, 0);
			TabInfo prev = getCallerInfo(tabReg);
			if(prev != null)
				prev.setSearchedEReg("");
			logger.info(getCurrentSootMethod().getSignature() + " Add tab");
			addToResultedTabs(new TabInfo(tabReg));
		}
		else if("<android.widget.TabHost$TabSpec: android.widget.TabHost$TabSpec setIndicator(java.lang.CharSequence,android.graphics.drawable.Drawable)>".equals(methodSignature) ||
				"<android.widget.TabHost$TabSpec: android.widget.TabHost$TabSpec setIndicator(java.lang.CharSequence)>".equals(methodSignature)) {
			// Does not support custom views
			TabInfo tabInfo = getCallerInfo(caller);
			if (tabInfo != null){
				String text = helpMethods.getParameterOfInvokeStmt(invokeExpr, 0);
				if (checkMethods.checkIfValueIsVariable(text)){
					tabInfo.setIndicatorTextReg(text);
				}else{
					//Here need to resolve text
					tabInfo.setIndicatorText(text.substring(1, text.length() - 1));
				}
			}
		}else if("<android.widget.TabHost$TabSpec: android.widget.TabHost$TabSpec setContent(android.content.Intent)>".equals(methodSignature)) {
			TabInfo tabInfo = getCallerInfo(caller);
			if (tabInfo != null) {
				if (tabInfo.getContent() != null) {
					logger.warn("Content is set more than once for a tab");
				} else {

					//here we need to do something about resolving the intent? might be tough, need to look into
					Value intentReg = invokeExpr.getArg(0);
					IntentInfo content = new IntentInfo();
					AnalyseIntentSwitch intentSwitch = new AnalyseIntentSwitch(intentReg, getCurrentSootMethod(), content);
					intents.add(intentSwitch);
					tabInfo.setContent(content);
					logger.info("Intent added to list for analysis");
				}

			}
		}
	}
	
	@Override
	public void init() {
		super.init();
		shouldBreak = false;
	}

	private TabInfo getCallerInfo(String caller) {
		TabInfo tabInfo = null;
		for (TabInfo i : getResultedTabs()){
			if (i.getSearchedEReg().equals(caller)){
				tabInfo = i;
				break;
			}
		}
		return tabInfo;
	}

	private void processIntents(Stmt stmt) {
		for(AnalyseIntentSwitch intent : intents) {
			if(!intent.isDone()) {
				stmt.apply(intent);
			}
		}
	}

	private void checkLineUp() {
		if (isTabHost && !activityClassName.equals("")){
			for (TabInfo info : getResultedTabs()){
				info.setParentActivityClassName(activityClassName);
			}
		}else{
			logger.warn("TabActivity does not line up.");
		}
	}

	@Override
	public void defaultCase(Object o){
		shouldBreak = false;
	}
}
