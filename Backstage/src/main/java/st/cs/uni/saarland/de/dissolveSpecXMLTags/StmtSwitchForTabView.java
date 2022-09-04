package st.cs.uni.saarland.de.dissolveSpecXMLTags;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import soot.SootField;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.*;
import st.cs.uni.saarland.de.entities.FieldInfo;
import st.cs.uni.saarland.de.entities.Listener;
import st.cs.uni.saarland.de.helpClasses.Helper;
import st.cs.uni.saarland.de.helpClasses.Info;
import st.cs.uni.saarland.de.helpClasses.InterProcInfo;
import st.cs.uni.saarland.de.helpClasses.MyStmtSwitchForResultLists;

import java.util.*;
import java.util.stream.Collectors;

public class StmtSwitchForTabView extends MyStmtSwitchForResultLists {

	// this switch only support one ActionBar from one Activity in one method
	private String actionBarReg ="";
	private boolean isActionBar;
	private boolean isNavigationMode2;
	private List<String> activityClassName = new ArrayList<String>();
	private String activityClassNameReg ="";
	private final Logger logger =  LoggerFactory.getLogger(Thread.currentThread().getName());

	
//    $r2 = virtualinvoke $r0.<com.example.Testapp.TabViewAct: android.app.ActionBar getActionBar()>();
//   ( virtualinvoke $r2.<android.app.ActionBar: void setNavigationMode(int)>(2); )
	
//    $r3 = virtualinvoke $r2.<android.app.ActionBar: android.app.ActionBar$Tab newTab()>();
//    $r5 = specialinvoke $r0.<com.example.Testapp.TabViewAct: java.lang.String getTextOfTab()>();
//    $r3 = virtualinvoke $r3.<android.app.ActionBar$Tab: android.app.ActionBar$Tab setText(java.lang.CharSequence)>($r5);
//    $r4 = new com.example.Testapp.TabListener;
//    specialinvoke $r4.<com.example.Testapp.TabListener: void <init>(android.app.Activity,java.lang.String,java.lang.Class)>($r0, "Tab22", class "com/example/Testapp/Fragment2TabView");
//    $r3 = virtualinvoke $r3.<android.app.ActionBar$Tab: android.app.ActionBar$Tab setTabListener(android.app.ActionBar$TabListener)>($r4);
//    virtualinvoke $r2.<android.app.ActionBar: void addTab(android.app.ActionBar$Tab)>($r3);

	public StmtSwitchForTabView(TabViewInfo newInfo, SootMethod currentSootMethod) {
		super(currentSootMethod);
		addToResultedTabsViews(newInfo);
	}

	public StmtSwitchForTabView(SootMethod currentSootMethod) {
		super(currentSootMethod);
	}

	public void caseIdentityStmt(IdentityStmt stmt){
		if(Thread.currentThread().isInterrupted()){
			return;
		}
		String leftReg = helpMethods.getLeftRegOfIdentityStmt(stmt);
		if (activityClassNameReg.equals(leftReg) && (stmt.getRightOp() instanceof ParameterRef)){
			activityClassNameReg = "";
			int paramIndex = ((ParameterRef)stmt.getRightOp()).getIndex();
			List<Info> res = interprocMethods2.findInReachableMethods2(paramIndex, getCurrentSootMethod(), new ArrayList<SootMethod>());
			if (res.size() > 0){
				if (!((InterProcInfo)res.get(0)).getValueOfSearchedReg().equals(""))
					activityClassName.add(((InterProcInfo)res.get(0)).getClassOfSearchedReg());
					
				if (res.size() > 1){
					for (int i = 1; i < res.size(); i++){
						InterProcInfo possibleLayouts = (InterProcInfo) res.get(i);
						if (!possibleLayouts.getValueOfSearchedReg().equals("")){
							activityClassName.add(possibleLayouts.getClassOfSearchedReg());
						}
					}
				}
			}
		}else if (activityClassNameReg.equals(leftReg) && stmt.getRightOp() instanceof ThisRef){
			activityClassNameReg = "";
			activityClassName.add(helpMethods.getRightClassTypeOfIdentityStmt(stmt));
		}
		
		Set<TabViewInfo> toAddInfos = new LinkedHashSet<>();
		for (TabViewInfo info : getResultedTabsViews()){
			if(Thread.currentThread().isInterrupted()){
				return;
			}
			if (stmt.getRightOp() instanceof ParameterRef){
				
				if (info.getTextReg().contains(leftReg)){
					info.removeTextReg(leftReg);
					int paramIndex = ((ParameterRef)stmt.getRightOp()).getIndex();
					List<Info> resList = interprocMethods2.findInReachableMethods2(paramIndex,  getCurrentSootMethod(), new ArrayList<SootMethod>());
					if (resList.size() > 0){
						info.addText(((InterProcInfo)resList.get(0)).getValueOfSearchedReg());
						if (resList.size() > 1){
							for (int j = 1; j < resList.size() ; j++){
								InterProcInfo workingInfo = (InterProcInfo) resList.get(j);
								info.addText(workingInfo.getValueOfSearchedReg());
							}
						}
					}	
				}else if (info.getFragmentClassReg().equals(leftReg)){
					info.setFragmentClassReg("");
					int paramIndex = ((ParameterRef)stmt.getRightOp()).getIndex();
					List<Info> resList = interprocMethods2.findInReachableMethods2(paramIndex,  getCurrentSootMethod(), new ArrayList<SootMethod>());
					if (resList.size() > 0){
						String fragClassName = ((InterProcInfo)resList.get(0)).getValueOfSearchedReg().replace("class ", "").replace("\"", "");
						info.setFragmentClassName(fragClassName);
						if (resList.size() > 1){
							for (int j = 1; j < resList.size() ; j++){
								InterProcInfo workingInfo = (InterProcInfo) resList.get(j);
								TabViewInfo newInfo = (TabViewInfo) info.clone();
								toAddInfos.add(newInfo);
								fragClassName = workingInfo.getValueOfSearchedReg().replace("class ", "").replace("\"", "");
								newInfo.setFragmentClassName(fragClassName);
							}
						}
					}
				}else if (info.getListenerReg().equals(leftReg)){
					info.setListenerReg("");
					int paramIndex = ((ParameterRef)stmt.getRightOp()).getIndex();
					List<Info> resList = interprocMethods2.findInReachableMethods2(paramIndex,  getCurrentSootMethod(), new ArrayList<SootMethod>());
					if (resList.size() > 0){
						Set<Listener> newList = getTabListener(((InterProcInfo)resList.get(0)).getClassOfSearchedReg(), getCurrentSootMethod().getDeclaringClass().getName());
						info.addListener(newList);
						if (resList.size() > 1){
							for (int j = 1; j < resList.size() ; j++){
								Set<Listener> newList2 = getTabListener(((InterProcInfo)resList.get(j)).getClassOfSearchedReg(), getCurrentSootMethod().getDeclaringClass().getName());
								info.addListener(newList2);
							}
						}
					}
				}
				
			}
		}
		addAllToResultedTabsViews(toAddInfos);
	}
	
	public void caseAssignStmt(AssignStmt stmt){
		if(Thread.currentThread().isInterrupted()){
			return;
		}
		String leftReg = helpMethods.getLeftRegOfAssignStmt(stmt);
		String rightReg = helpMethods.getRightRegOfAssignStmt(stmt);
		
		
		if (stmt.containsInvokeExpr()){
			InvokeExpr invokeExpr = stmt.getInvokeExpr();
			String methodSignature = helpMethods.getSignatureOfInvokeExpr(invokeExpr);
			String caller = helpMethods.getCallerOfInvokeStmt(invokeExpr);
			
			if ("<android.app.ActionBar$Tab: android.app.ActionBar$Tab setTabListener(android.app.ActionBar$TabListener)>".equals(methodSignature)){
				String listenerReg = helpMethods.getParameterOfInvokeStmt(invokeExpr, 0);
				for (TabViewInfo i : getResultedTabsViews()){
					if (i.getSearchedEReg().equals(caller)){
						i.setListenerReg(listenerReg);
					}
				}
				// ab
			}else if ("<android.app.ActionBar$Tab: android.app.ActionBar$Tab setText(java.lang.CharSequence)>".equals(methodSignature) ||
					"<android.app.ActionBar$Tab: android.app.ActionBar$Tab setText(int)>".equals(methodSignature)){
				TabViewInfo tabInfo = null;
				for (TabViewInfo i : getResultedTabsViews()){
					if (i.getSearchedEReg().equals(caller)){
						tabInfo = i;
						break;
					}
				}
				if (tabInfo != null){
					String text = helpMethods.getParameterOfInvokeStmt(invokeExpr, 0);
					if (checkMethods.checkIfValueIsVariable(text)){
						tabInfo.addTextReg(text);
					}else{
						tabInfo.addText(text);
					}
				}
			}else if ("<android.app.ActionBar: android.app.ActionBar$Tab newTab()>".equals(methodSignature)){
				if (actionBarReg.equals(caller)){
					for (TabViewInfo i : getResultedTabsViews()){
						if (i.getSearchedEReg().equals(leftReg)){
							i.setSearchedEReg("");
						}
					}
				}else{
					logger.warn("ActionBar was not the one that was found earlier: found AB: " + caller + ", earlier found AB: " + actionBarReg);
				}
			}else if (methodSignature.contains(": android.app.ActionBar getActionBar()>")){
				if (actionBarReg.equals(leftReg)){
					isActionBar = true;
					actionBarReg = "";
					activityClassName.add(helpMethods.getCallerTypeOfInvokeStmt(invokeExpr));
					checkLineUp();
					shouldBreak = true;
				}
			}
			else{
				String callerType = helpMethods.getCallerTypeOfInvokeStmt(invokeExpr);


					if (leftReg.equals(activityClassNameReg)){
						activityClassNameReg = "";
						if (!Helper.isClassInSystemPackage(callerType)) {
							List<InterProcInfo> res = interprocMethods2.findReturnValueInMethod2(stmt);
							if (res.size() > 0) {
								if (!((InterProcInfo) res.get(0)).getValueOfSearchedReg().equals(""))
									activityClassName.add(((InterProcInfo) res.get(0)).getClassOfSearchedReg());
								if (res.size() > 1) {
									for (int i = 1; i < res.size(); i++) {
										InterProcInfo possibleLayouts = (InterProcInfo) res.get(i);
										if (!possibleLayouts.getValueOfSearchedReg().equals("")) {
											activityClassName.add(possibleLayouts.getClassOfSearchedReg());
										}
									}
								}
							}
						}
					}else{
						Set<TabViewInfo> toAddInfos = new LinkedHashSet<>();
						for (TabViewInfo info: getResultedTabsViews()){
							// TODO will never come back with results, unless this stmtSwitch is used in a chaned version
//							if (info.getSearchedEReg().equals(leftReg)){
//								info.setSearchedEReg("");
//								InterProcInfo res = checkMethods.getInterProcCall(methodSignature);
//								if ((res != null) && (!res.getClassOfSearchedReg().equals(""))){
//									activityClassName = res.getClassOfSearchedReg();
//								}
//							}else 
								if (info.getFragmentClassReg().equals(leftReg)){
								info.setFragmentClassReg("");
								if (!Helper.isClassInSystemPackage(callerType)) {
									List<InterProcInfo> resList = interprocMethods2.findReturnValueInMethod2(stmt);
									if (resList.size() > 0) {
										String fragClassName = ((InterProcInfo) resList.get(0)).getValueOfSearchedReg().replace("class ", "").replace("\"", "");
										info.setFragmentClassName(fragClassName);
										if (resList.size() > 1) {
											for (int j = 1; j < resList.size(); j++) {
												InterProcInfo workingInfo = (InterProcInfo) resList.get(j);
												TabViewInfo newInfo = (TabViewInfo) info.clone();
												toAddInfos.add(newInfo);
												fragClassName = workingInfo.getValueOfSearchedReg().replace("class ", "").replace("\"", "");
												newInfo.setFragmentClassName(fragClassName);
											}
										}
									}
								}
							}else if (info.getListenerReg().equals(leftReg)){
								info.setListenerReg("");
								if (!Helper.isClassInSystemPackage(callerType)) {
									List<InterProcInfo> resList = interprocMethods2.findReturnValueInMethod2(stmt);
									if (resList.size() > 0) {
										Set<Listener> newList = getTabListener(((InterProcInfo) resList.get(0)).getClassOfSearchedReg(), getCurrentSootMethod().getDeclaringClass().getName());
										info.addListener(newList);
										if (resList.size() > 1) {
											for (int j = 1; j < resList.size(); j++) {
												Set<Listener> newList2 = getTabListener(((InterProcInfo) resList.get(j)).getClassOfSearchedReg(), getCurrentSootMethod().getDeclaringClass().getName());
												info.addListener(newList2);
											}
										}
									}
								}
							}else if (info.getTextReg().contains(leftReg)){
								info.removeTextReg(leftReg);
								if (!Helper.isClassInSystemPackage(callerType)) {
									List<InterProcInfo> resList = interprocMethods2.findReturnValueInMethod2(stmt);
									if (resList.size() > 0) {
										info.addText(((InterProcInfo) resList.get(0)).getValueOfSearchedReg());
										if (resList.size() > 1) {
											for (int j = 1; j < resList.size(); j++) {
												InterProcInfo workingInfo = (InterProcInfo) resList.get(j);
												info.addText(workingInfo.getValueOfSearchedReg());
											}
										}
									}
								}
							}
						}
						addAllToResultedTabsViews(toAddInfos);
					}
			}
		}else{
			Set<TabViewInfo> toAddedInfos = new LinkedHashSet<>();
			Set<TabViewInfo> toRemoveInfos = new LinkedHashSet<>();
			// TODO check if variable are here possible
			for (TabViewInfo infos : getResultedTabsViews()){
				if(Thread.currentThread().isInterrupted()){
					return;
				}
				if (infos.getFragmentClassReg().equals(leftReg)){
					infos.setFragmentClassReg("");
					if(stmt.getRightOp() instanceof FieldRef){
						SootField f = ((FieldRef)stmt.getRightOp()).getField();
						if(previousFields.contains(f)){
							if(!previousFieldsForCurrentStmtSwitch.contains(f)){
								continue;
							}
						}else{
							previousFields.add(f);
							previousFieldsForCurrentStmtSwitch.add(f);
						}
						Set<FieldInfo> fInfos = interprocMethods2.findInitializationsOfTheField2(f, stmt, getCurrentSootMethod());
						if(fInfos.size() > 0){
							toRemoveInfos.add(infos);
							for (FieldInfo fInfo : fInfos){
								if(Thread.currentThread().isInterrupted()){
									return;
								}
								if(fInfo.methodToStart != null && fInfo.methodToStart.method().hasActiveBody()){
									Unit workingUnit = fInfo.unitToStart;
									TabViewInfo newInfo = new TabViewInfo("");
									newInfo.setFragmentClassReg(fInfo.register.getName());
									StmtSwitchForTabView newStmtSwitch = new StmtSwitchForTabView(newInfo, getCurrentSootMethod());
									previousFields.forEach(x->newStmtSwitch.addPreviousField(x));
									iteratorHelper.runOverToFindSpecValuesBackwards(fInfo.methodToStart.method().getActiveBody(), workingUnit, newStmtSwitch);
									Set<Info> initValues = newStmtSwitch.getResultInfos();

									if(initValues.size() > 0) {
										List<Info> listInfo = initValues.stream().collect(Collectors.toList());
										if(listInfo.indexOf(newInfo) == -1){
											continue;
										}
										Info initInfo = listInfo.get(listInfo.indexOf(newInfo));
										TabViewInfo newInfo2 = (TabViewInfo) infos.clone();
										newInfo2.setFragmentClassName(((TabViewInfo)initInfo).getFragmentClassName());
										newInfo2.setFragmentClassReg("");
										toAddedInfos.add(newInfo2);
									}
								}
							}
						}else{
							infos.setFragmentClassReg("");
							Helper.saveToStatisticalFile("Error TabViewSwitch: Doesn't find FragmentClassReg in initializationOfField: " + stmt);
						}
					}else{
						infos.setFragmentClassReg("");
						if (rightReg.contains("class ")){
							infos.setFragmentClassName(rightReg.replace("class ", "").replace("\"", ""));
						}else{
							infos.setFragmentClassReg(rightReg);
						}
					}
				}else if (infos.getListenerReg().equals(leftReg)){
					infos.setListenerReg("");
					if (stmt.getRightOp() instanceof NewExpr){
						infos.setListenerReg("");
						String listenerClass = helpMethods.getTypeOfRightRegOfAssignStmt(stmt);
						Set<Listener> listener = this.getTabListener(listenerClass, getCurrentSootMethod().getDeclaringClass().getName());
						infos.addListener(listener);
					}else if (stmt.getRightOp() instanceof FieldRef){
						SootField f = ((FieldRef)stmt.getRightOp()).getField();
						if(previousFields.contains(f)){
							if(!previousFieldsForCurrentStmtSwitch.contains(f)){
								continue;
							}
						}else{
							previousFields.add(f);
							previousFieldsForCurrentStmtSwitch.add(f);
						}
						Set<FieldInfo> fInfos = interprocMethods2.findInitializationsOfTheField2(f, stmt, getCurrentSootMethod());
						if(fInfos.size() > 0){
							toRemoveInfos.add(infos);
							for (FieldInfo fInfo : fInfos){
								if(Thread.currentThread().isInterrupted()){
									return;
								}
								if(fInfo.methodToStart != null && fInfo.methodToStart.method().hasActiveBody()){
									Unit workingUnit = fInfo.unitToStart;
									TabViewInfo newInfo = new TabViewInfo("");
									newInfo.setListenerReg(fInfo.register.getName());
									StmtSwitchForTabView newStmtSwitch = new StmtSwitchForTabView(newInfo, getCurrentSootMethod());
									previousFields.forEach(x->newStmtSwitch.addPreviousField(x));
									iteratorHelper.runOverToFindSpecValuesBackwards(fInfo.methodToStart.method().getActiveBody(), workingUnit, newStmtSwitch);
									Set<Info> initValues = newStmtSwitch.getResultInfos();

									if(initValues.size() > 0) {
										List<Info> listInfo = initValues.stream().collect(Collectors.toList());
										if(listInfo.indexOf(newInfo) == -1){
											continue;
										}
										Info initInfo = listInfo.get(listInfo.indexOf(newInfo));
										TabViewInfo newInfo2 = (TabViewInfo) infos.clone();
										newInfo2.setListenerReg("");
										newInfo2.addListener(((TabViewInfo)initInfo).getListener());
										toAddedInfos.add(newInfo2);
									}
								}
							}
						}else{
							infos.setListenerReg("");
							Helper.saveToStatisticalFile("Error TabViewSwitch: Doesn't find ListenerReg in initializationOfField: " + stmt);
						}
					}else{
						infos.setListenerReg(rightReg);
					}
				}else if (infos.getSearchedEReg().equals(leftReg)){
					// TODO think about it: do not search in FieldRef because it would not find it (searchedEReg -> Tab)
					infos.setSearchedEReg(rightReg);
					
				}else if (infos.getTextReg().contains(leftReg)){
					infos.removeTextReg(leftReg);
					if(stmt.getRightOp() instanceof FieldRef){						
						FieldRef ref = (FieldRef)stmt.getRightOp();
						SootField f = ref.getField();
						if(previousFields.contains(f)){
							if(!previousFieldsForCurrentStmtSwitch.contains(f)){
								continue;
							}
						}else{
							previousFields.add(f);
							previousFieldsForCurrentStmtSwitch.add(f);
						}
						Set<FieldInfo> fInfos = interprocMethods2.findInitializationsOfTheField2(f, stmt, getCurrentSootMethod());
						for(FieldInfo fInfo : fInfos){
							if(Thread.currentThread().isInterrupted()){
								return;
							}
							if(fInfo.value != null){
								infos.addText(fInfo.value);
							}else{
								if(fInfo.methodToStart != null && fInfo.methodToStart.method().hasActiveBody()){
									Unit workingUnit = fInfo.unitToStart;
									TabViewInfo newInfo = new TabViewInfo("");
									newInfo.addTextReg(fInfo.register.getName());
									// TODO here not better to use StmtSwitchForStrings?
									StmtSwitchForTabView newStmtSwitch  = new StmtSwitchForTabView(newInfo, getCurrentSootMethod());
									previousFields.forEach(x->newStmtSwitch.addPreviousField(x));
									iteratorHelper.runOverToFindSpecValuesBackwards(fInfo.methodToStart.method().getActiveBody(), workingUnit, newStmtSwitch);
									Set<TabViewInfo> initInfos = newStmtSwitch.getResultedTabsViews();
									if (initInfos.size() > 0) {
										List<Info> listInfo = initInfos.stream().collect(Collectors.toList());
										if(listInfo.indexOf(newInfo) == -1){
											continue;
										}
										infos.addText(listInfo.get(listInfo.indexOf(newInfo)).getText());
									}
								}
							}
						}
					}else{
						if (checkMethods.checkIfValueIsString(rightReg) || checkMethods.checkIfValueIsID(rightReg)){
							infos.addText(rightReg);
						}else{
							infos.addTextReg(rightReg);
						}
					}
				}
			}			
			removeAllFromResultedTabs(toRemoveInfos);
			addAllToResultedTabsViews(toAddedInfos);
		}
	}
	
	
	public void caseInvokeStmt(InvokeStmt stmt) {
		if(Thread.currentThread().isInterrupted()){
			return;
		}
		InvokeExpr invokeExpr = stmt.getInvokeExpr();
		String methodSignature = helpMethods.getSignatureOfInvokeExpr(invokeExpr);
//		methodSignature is : <com.example.Testapp.TabListener: void <init>(android.app.Activity,java.lang.String,java.lang.Class)>
		if (methodSignature.contains(": void <init>(android.app.Activity,java.lang.String,java.lang.Class)>")){
			String callerReg = helpMethods.getCallerOfInvokeStmt(invokeExpr);
			for (TabViewInfo info : getResultedTabsViews()){
				if(Thread.currentThread().isInterrupted()){
					return;
				}
				if (info.getListenerReg().equals(callerReg)){
					info.setListenerReg("");
					String posFragmentName = helpMethods.getParameterOfInvokeStmt(invokeExpr, 2);
					if (posFragmentName.contains("class ")){
						info.setFragmentClassName(posFragmentName.replace("class ", "").replace("\"", ""));
					}else{
						info.setFragmentClassReg(posFragmentName);
					}
				}
			}
		}else
			if ("<android.app.ActionBar: void setNavigationMode(int)>".equals(methodSignature)){
				String paramter = helpMethods.getParameterOfInvokeStmt(invokeExpr, 0);
				String posActionBarReg = helpMethods.getCallerOfInvokeStmt(invokeExpr);
				if ("2".equals(paramter)){
					isNavigationMode2 = true;
					if (posActionBarReg.equals(actionBarReg)){
						// everything is fine
					}else if (actionBarReg.equals("")){
						actionBarReg = posActionBarReg;
					}else{
						logger.warn("ActionBar was not the one that was found earlier: found AB: " + posActionBarReg + ", earlier found AB: " + actionBarReg);
					}
				}
		}else 
			if ("<android.app.ActionBar: void addTab(android.app.ActionBar$Tab)>".equals(methodSignature)){
				String caller = helpMethods.getCallerOfInvokeStmt(invokeExpr);
				if (actionBarReg.equals(""))
					actionBarReg = caller;
				else if (!actionBarReg.equals(caller))
					logger.warn("ActionBar was not the one that was found earlier: found AB: " + caller + ", earlier found AB: " + actionBarReg);
				String tabReg = helpMethods.getParameterOfInvokeStmt(invokeExpr, 0);
				addToResultedTabsViews(new TabViewInfo(tabReg));
			}
	}
	
	private void checkLineUp() {
		if (isNavigationMode2 && isActionBar && (!activityClassName.equals(""))){
			for (TabViewInfo info : getResultedTabsViews()){
				info.addActivityClassName(activityClassName);
			}
		}else{
			logger.warn("ActionBar, NavigationMode2 not set or activtiyClassNotFound");
		}
	}
	
	@Override
	public void init() {
		super.init();
		shouldBreak = false;
	}
	
//
//	public List<TabViewInfo> getResultInfos(){
//		return tabInfos;
//	}
	
	private Set<Listener> getTabListener(String listenerClass, String declaringSootClass){
		Set<Listener> onCallMethod = new HashSet<Listener>();	
		Listener list1 = new Listener("tabReselected", false, "void onTabReselected(android.app.ActionBar.Tab,android.app.FragmentTransaction)", declaringSootClass);
		list1.setListenerClass(listenerClass);
		// TODO maybe check if methods are existing
		Listener list2 = new Listener("tabSelected", false, "void onTabSelected(android.app.ActionBar.Tab,android.app.FragmentTransaction)", declaringSootClass);
		list2.setListenerClass(listenerClass);
		Listener list3 = new Listener("tabUnselected", false, "void onTabUnselected(android.app.ActionBar.Tab,android.app.FragmentTransaction)", declaringSootClass);
		list3.setListenerClass(listenerClass);
		onCallMethod.add(list1);
		onCallMethod.add(list2);
		onCallMethod.add(list3);	
		return onCallMethod;
	}

	@Override
	public void defaultCase(Object o){
		shouldBreak = false;
	}
}
