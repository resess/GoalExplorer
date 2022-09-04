package st.cs.uni.saarland.de.searchMenus;

import soot.SootField;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.*;
import st.cs.uni.saarland.de.entities.FieldInfo;
import st.cs.uni.saarland.de.helpClasses.*;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.stream.Collectors;

public class StmtSwitchForContextMenus extends MyStmtSwitch {

	public StmtSwitchForContextMenus(MenuInfo newInfo, SootMethod currentSootMethod) {
		super(newInfo, currentSootMethod);
	}
	
	public StmtSwitchForContextMenus(SootMethod currentSootMethod) {
		super(currentSootMethod);
	}

	public StmtSwitchForContextMenus(SootMethod currentSootMethod, Map<String, String> dynStrings) {
		super(currentSootMethod, dynStrings);
	}


//	private MenuInfo menuInfo;
	
//	onCreate:
//	 $r0 := @this: com.example.Testapp.ListViewAct;
//	 $r3 = virtualinvoke $r0.<com.example.Testapp.ListViewAct: android.view.View findViewById(int)>(2131165194);
//   $r5 = (android.widget.ListView) $r3;
//	 virtualinvoke $r0.<com.example.Testapp.ListViewAct: void registerForContextMenu(android.view.View)>($r5);
	
	
	public void caseIdentityStmt(final IdentityStmt stmt){
		if(Thread.currentThread().isInterrupted()){
			return;
		}
		Set<Info> resultInfos = getResultInfos();
		Set<Info> toAddInfos = new LinkedHashSet<>();
		for (Info i : resultInfos){
			if(Thread.currentThread().isInterrupted()){
				return;
			}
			MenuInfo menuInfo = (MenuInfo) i;
			
			String leftReg = helpMethods.getLeftRegOfIdentityStmt(stmt);
			String rightRegType = helpMethods.getRightClassTypeOfIdentityStmt(stmt);
			if (leftReg.equals(menuInfo.getActivityReg())){
				menuInfo.setActivityReg("");
				if(stmt.getRightOp() instanceof ParameterRef){
					int paramIndex = ((ParameterRef)stmt.getRightOp()).getIndex();
					List<Info> resList = interprocMethods2.findInReachableMethods2(paramIndex,  getCurrentSootMethod(), new ArrayList<SootMethod>());
					if (resList.size() > 0){
						menuInfo.setActivityClassName(((InterProcInfo)resList.get(0)).getClassOfSearchedReg());
						if (resList.size() > 1){
							for (int j = 1; j < resList.size() ; j++){
								if(Thread.currentThread().isInterrupted()){
									return;
								}
								InterProcInfo workingInfo = (InterProcInfo) resList.get(j);
								MenuInfo newInfo = (MenuInfo) menuInfo.clone();
								toAddInfos.add(newInfo);
								newInfo.setActivityClassName(workingInfo.getClassOfSearchedReg());
							}
						}
					}
				}else if(stmt.getRightOp() instanceof ThisRef){
					menuInfo.setActivityReg("");
					menuInfo.setActivityClassName(rightRegType);
				}				
			}
			if (leftReg.equals(menuInfo.getLayoutIDReg())){
				menuInfo.setLayoutIDReg("");
				if(stmt.getRightOp() instanceof ParameterRef){
					int paramIndex = ((ParameterRef)stmt.getRightOp()).getIndex();
					// TODO ask why methodSig is null
					List<Info> resList = interprocMethods2.findInReachableMethods2(paramIndex,  getCurrentSootMethod(), new ArrayList<SootMethod>());
					if (resList.size() > 0){
						menuInfo.setLayoutID(((InterProcInfo)resList.get(0)).getValueOfSearchedReg());
						if (resList.size() > 1){
							for (int j = 1; j < resList.size() ; j++){
								if(Thread.currentThread().isInterrupted()){
									return;
								}
								InterProcInfo workingInfo = (InterProcInfo) resList.get(j);
								MenuInfo newInfo = (MenuInfo) menuInfo.clone();
								toAddInfos.add(newInfo);
								newInfo.setLayoutID(workingInfo.getValueOfSearchedReg());
							}
						}
					}
				}else if(stmt.getRightOp() instanceof ThisRef){
					menuInfo.setLayoutID(rightRegType);
				}
			}
		}
		resultInfos.addAll(toAddInfos);
	}
	
	public void caseAssignStmt(final AssignStmt stmt){
		Set<Info> toAddInfos = new LinkedHashSet<>();
		Set<Info> toRemoveInfos = new LinkedHashSet<>();
		for (Info i : getResultInfos()){
			if(Thread.currentThread().isInterrupted()){
				return;
			}
			MenuInfo menuInfo = (MenuInfo) i;
			
			String leftReg = helpMethods.getLeftRegOfAssignStmt(stmt);
			
			if (stmt.containsInvokeExpr()){
				//TODO deal with lsit views and layouts
				//TODO deal with fields as well?
				InvokeExpr invokeExpr = stmt.getInvokeExpr();
				String methodSignature = helpMethods.getSignatureOfInvokeExpr(stmt.getInvokeExpr());
				if (methodSignature.equals("<android.app.Activity: android.view.View findViewById(int)>")
								&& leftReg.equals(menuInfo.getSearchedEReg())){
					String layoutID = helpMethods.getParameterOfInvokeStmt(stmt.getInvokeExpr(), 0);
					String layoutIDReg = "";
					if (!checkMethods.checkIfValueIsID(layoutID)){
						layoutIDReg = layoutID;
						layoutID = "";
					}
					menuInfo.setLayoutID(layoutID);
					menuInfo.setLayoutIDReg(layoutIDReg);
					menuInfo.setSearchedEReg("");
					
				}
					if (leftReg.equals(menuInfo.getActivityReg())){
						menuInfo.setActivityReg("");
						if (!Helper.isClassInSystemPackage(helpMethods.getCallerTypeOfInvokeStmt(invokeExpr))) {
							List<InterProcInfo> resList = interprocMethods2.findReturnValueInMethod2(stmt);
							if (resList.size() > 0) {
								menuInfo.setActivityClassName(((InterProcInfo) resList.get(0)).getClassOfSearchedReg());
								if (resList.size() > 1) {
									for (int j = 1; j < resList.size(); j++) {
										if(Thread.currentThread().isInterrupted()){
											return;
										}
										InterProcInfo workingInfo = (InterProcInfo) resList.get(j);
										MenuInfo newInfo = (MenuInfo) menuInfo.clone();
										toAddInfos.add(newInfo);
										newInfo.setActivityClassName(workingInfo.getClassOfSearchedReg());
									}
								}
							}
						}
					}
					if (leftReg.equals(menuInfo.getLayoutIDReg())){
						menuInfo.setLayoutIDReg("");
						if (!Helper.isClassInSystemPackage(helpMethods.getCallerTypeOfInvokeStmt(invokeExpr))) {
							List<InterProcInfo> resList = interprocMethods2.findReturnValueInMethod2(stmt);
							if (resList.size() > 0) {
								menuInfo.setLayoutID(((InterProcInfo) resList.get(0)).getValueOfSearchedReg());
								if (resList.size() > 1) {
									for (int j = 1; j < resList.size(); j++) {
										if(Thread.currentThread().isInterrupted()){
											return;
										}
										InterProcInfo workingInfo = (InterProcInfo) resList.get(j);
										MenuInfo newInfo = (MenuInfo) menuInfo.clone();
										toAddInfos.add(newInfo);
										newInfo.setLayoutID(workingInfo.getValueOfSearchedReg());
									}
								}
							}
						}
					}
					if (leftReg.equals(menuInfo.getSearchedEReg())){
						menuInfo.setSearchedEReg("");
						if (!Helper.isClassInSystemPackage(helpMethods.getCallerTypeOfInvokeStmt(invokeExpr))) {
							List<InterProcInfo> resList = interprocMethods2.findReturnValueInMethod2(stmt);
							if (resList.size() > 0) {
								menuInfo.setLayoutID(((InterProcInfo) resList.get(0)).getValueOfSearchedReg());
								if (resList.size() > 1) {
									for (int j = 1; j < resList.size(); j++) {
										if(Thread.currentThread().isInterrupted()){
											return;
										}
										InterProcInfo workingInfo = (InterProcInfo) resList.get(j);
										MenuInfo newInfo = (MenuInfo) menuInfo.clone();
										toAddInfos.add(newInfo);
										newInfo.setLayoutID(workingInfo.getValueOfSearchedReg());
									}
								}
							}
						}
						else if(methodSignature.contains(": android.widget.ExpandableListView getExpandableListView()") || methodSignature.contains("android.widget.ListView getListView()")){
							menuInfo.setLayoutID(Integer.toString(AndroidRIdValues.getAndroidID("list")));
						}
					}
			}
			else{
				String rightReg = helpMethods.getRightRegOfAssignStmt(stmt);
				if (leftReg.equals(menuInfo.getSearchedEReg())){
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
							toRemoveInfos.add(menuInfo);
							for (FieldInfo fInfo : fInfos){
								if(Thread.currentThread().isInterrupted()){
									return;
								}
								if(fInfo.value != null){
									MenuInfo newInfo = (MenuInfo) menuInfo.clone();
									newInfo.setLayoutID(fInfo.value);
									newInfo.setSearchedEReg("");
									toAddInfos.add(newInfo);
									continue;
								}else{
									if(fInfo.methodToStart != null && fInfo.methodToStart.method().hasActiveBody()){
										Unit workingUnit = fInfo.unitToStart;
										MenuInfo newInfo = new MenuInfo("", "", "", getCurrentSootMethod().getDeclaringClass().getName());
										newInfo.setSearchedEReg(fInfo.register.getName());
										StmtSwitchForContextMenus newStmtSwitch = new StmtSwitchForContextMenus(newInfo, getCurrentSootMethod());
										previousFields.forEach(x->newStmtSwitch.addPreviousField(x));
										iteratorHelper.runOverToFindSpecValuesBackwards(fInfo.methodToStart.method().getActiveBody(), workingUnit, newStmtSwitch);
										Set<Info> initValues = newStmtSwitch.getResultInfos();

										if(initValues.size() > 0) {
											List<Info> listInfo = initValues.stream().collect(Collectors.toList());
											if(listInfo.indexOf(newInfo) == -1){
												continue;
											}
											Info initInfo = listInfo.get(listInfo.indexOf(newInfo));
											MenuInfo newInfo2 = (MenuInfo) menuInfo.clone();
											newInfo2.setLayoutID(((MenuInfo)initInfo).getLayoutID());
											newInfo2.setSearchedEReg("");
											toAddInfos.add(newInfo2);
										}
									}
								}
							}
						}else{
							menuInfo.setSearchedEReg("");
							Helper.saveToStatisticalFile("Error ContextMenuSwitch: Doesn't find SearchedEReg in initializationOfField: " + stmt);
						}
					}else{
						menuInfo.setSearchedEReg("");
						menuInfo.setSearchedEReg(rightReg);
					}
				}
				if (leftReg.equals(menuInfo.getLayoutIDReg())){
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
							toRemoveInfos.add(menuInfo);
							for (FieldInfo fInfo : fInfos){
								if(Thread.currentThread().isInterrupted()){
									return;
								}
								if(fInfo.value != null){
									MenuInfo newInfo = (MenuInfo) menuInfo.clone();
									newInfo.setLayoutID(fInfo.value);
									newInfo.setLayoutIDReg("");
									toAddInfos.add(newInfo);
									continue;
								}else{
									if(fInfo.methodToStart != null && fInfo.methodToStart.method().hasActiveBody()){
										Unit workingUnit = fInfo.unitToStart;
										MenuInfo newInfo = new MenuInfo("", "", "", getCurrentSootMethod().getDeclaringClass().getName());
										newInfo.setLayoutIDReg(fInfo.register.getName());
										StmtSwitchForContextMenus newStmtSwitch = new StmtSwitchForContextMenus(newInfo, getCurrentSootMethod());
										previousFields.forEach(x->newStmtSwitch.addPreviousField(x));
										iteratorHelper.runOverToFindSpecValuesBackwards(fInfo.methodToStart.method().getActiveBody(), workingUnit, newStmtSwitch);
										Set<Info> initValues = newStmtSwitch.getResultInfos();

										if(initValues.size() > 0) {
											List<Info> listInfo = initValues.stream().collect(Collectors.toList());
											if(listInfo.indexOf(newInfo) == -1){
												continue;
											}
											Info initInfo = listInfo.get(listInfo.indexOf(newInfo));
											MenuInfo newInfo2 = (MenuInfo) menuInfo.clone();
											newInfo2.setLayoutIDReg("");
											newInfo2.setLayoutID(((MenuInfo)initInfo).getLayoutID());
											toAddInfos.add(newInfo2);
										}
									}
								}
							}
						}
					}else{
						menuInfo.setLayoutIDReg("");
						if (checkMethods.checkIfValueIsID(rightReg))
							menuInfo.setLayoutID(rightReg);
						else
							menuInfo.setLayoutIDReg(rightReg);
					}
				}
			}
		}
		
		if (toRemoveInfos.size() > 0){
			removeAllFromResultInfos(toRemoveInfos);
		}
		addAllToResultInfo(toAddInfos);
	}
	
	public void caseInvokeStmt(final InvokeStmt stmt) {
		if(Thread.currentThread().isInterrupted()){
			return;
		}
		InvokeExpr invokeExpr = stmt.getInvokeExpr();
		String methodSignature = helpMethods.getSignatureOfInvokeExpr(invokeExpr);

			if(methodSignature.equals("<android.app.Activity: void registerForContextMenu(android.view.View)>")){
				MenuInfo menuInfo = new MenuInfo(helpMethods.getParameterOfInvokeStmt(invokeExpr, 0), "", null, getCurrentSootMethod().getDeclaringClass().getName());
				addToResultInfo(menuInfo);
				String actName = helpMethods.getCallerOfInvokeStmt(invokeExpr);
				if (checkMethods.checkIfValueIsVariable(actName)){
					menuInfo.setActivityReg(actName);
				}else{
					menuInfo.setActivityClassName(actName);
				}

		}		
	}
	
//	@Override
//	public boolean run(){
//		if (menuInfo != null)
//			return menuInfo.allValuesFound();
//		else
//			return false;
//	}
//	
//	@Override
//	public void init(){
//		super.init();
//		menuInfo = null;
//		resultInfo = null;
//		shouldBreak = false;
//	}
	
	@Override
	public void defaultCase(Object o){
	}
	
}
