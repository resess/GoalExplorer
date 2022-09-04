package st.cs.uni.saarland.de.searchMenus;

import soot.SootField;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.*;
import st.cs.uni.saarland.de.entities.FieldInfo;
import st.cs.uni.saarland.de.entities.Listener;
import st.cs.uni.saarland.de.helpClasses.Helper;
import st.cs.uni.saarland.de.helpClasses.Info;
import st.cs.uni.saarland.de.helpClasses.InterProcInfo;
import st.cs.uni.saarland.de.helpClasses.MyStmtSwitch;
import st.cs.uni.saarland.de.helpMethods.StmtSwitchForArrayAdapter;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class StmtSwitchForNavDropDownMenus extends MyStmtSwitch {
	
	
//	private DropDownNavMenuInfo mInfo;

	
	// comment: setNavigationMode(NAVIGATION_MODE_LIST) is not checked, because it could also be done after setListNavCallbacks
	// FIXME: small bug: actionBar could also be created in an interproc call -> this is not tracked (not included in InterProcSwitch)
	
//	  ( $r2 = virtualinvoke $r0.<com.example.Testapp.MainActivity2: android.app.ActionBar getActionBar()>(); )
//    ( virtualinvoke $r2.<android.app.ActionBar: void setNavigationMode(int)>(1); )
//     r17 = (android.content.Context) $r0;
//     $r3 = staticinvoke <android.widget.ArrayAdapter: android.widget.ArrayAdapter createFromResource(android.content.Context,int,int)>(r17, 2130968576, 17367043);
//     r15 = new com.example.Testapp.MainActivity2$5;
//    ( specialinvoke r15.<com.example.Testapp.MainActivity2$5: void <init>(com.example.Testapp.MainActivity2)>($r0); )
//     r18 = (android.app.ActionBar$OnNavigationListener) r15;
//     virtualinvoke $r2.<android.app.ActionBar: void setListNavigationCallbacks(android.widget.SpinnerAdapter,android.app.ActionBar$OnNavigationListener)>($r3, r18);
	
	
	public StmtSwitchForNavDropDownMenus(DropDownNavMenuInfo newInfo, SootMethod currentSootMethod) {
		super(newInfo, currentSootMethod);
	}
	
	public StmtSwitchForNavDropDownMenus(SootMethod currentSootMethod) {
		super(currentSootMethod);
	}

	public void caseIdentityStmt(final IdentityStmt stmt){
		if(Thread.currentThread().isInterrupted()){
			return;
		}
//		List<Info> resultInfos = getResultInfos();
		Set<Info> toAddInfos = new LinkedHashSet<>();
		for (Info i : getResultInfos()){
			if(Thread.currentThread().isInterrupted()){
				return;
			}
			DropDownNavMenuInfo mInfo = (DropDownNavMenuInfo) i;
			
			String leftReg = helpMethods.getLeftRegOfIdentityStmt(stmt);
			String rightRegType = helpMethods.getRightClassTypeOfIdentityStmt(stmt);
			if (leftReg.equals(mInfo.getActivityReg())){
				mInfo.setActivityReg("");
				if(stmt.getRightOp() instanceof ParameterRef){
					int paramIndex = ((ParameterRef)stmt.getRightOp()).getIndex();
					List<Info> resList = interprocMethods2.findInReachableMethods2(paramIndex,  getCurrentSootMethod(), new ArrayList<>());
					if (resList.size() > 0){
						mInfo.setActivityName(((InterProcInfo)resList.get(0)).getClassOfSearchedReg());
						if (resList.size() > 1){
							for (int j = 1; j < resList.size() ; j++){
								InterProcInfo workingInfo = (InterProcInfo) resList.get(j);
								DropDownNavMenuInfo newInfo = (DropDownNavMenuInfo) mInfo.clone();
								toAddInfos.add(newInfo);
								newInfo.setActivityName(workingInfo.getClassOfSearchedReg());
							}
						}
					}
				}else if(stmt.getRightOp() instanceof ThisRef){
					mInfo.setActivityName(rightRegType);
				}	
			}
			if (leftReg.equals(mInfo.getListenerReg())){//TODO: Update
				mInfo.setListenerReg("");
				if(stmt.getRightOp() instanceof ParameterRef){
					int paramIndex = ((ParameterRef)stmt.getRightOp()).getIndex();
					List<Info> resList = interprocMethods2.findInReachableMethods2(paramIndex,  getCurrentSootMethod(), new ArrayList<>());
					if (resList.size() > 0){
						Listener list = new Listener("onClick", false, "boolean onNavigationItemSelected(int,long)", getCurrentSootMethod().getDeclaringClass().getName());
						list.setListenerClass(((InterProcInfo)resList.get(0)).getClassOfSearchedReg());
						mInfo.addListener(list);
						if (resList.size() > 1){
							for (int j = 1; j < resList.size(); j++){
								Listener list2 = new Listener("onClick", false, "boolean onNavigationItemSelected(int,long)", getCurrentSootMethod().getDeclaringClass().getName());
								list2.setListenerClass(((InterProcInfo)resList.get(j)).getClassOfSearchedReg());
								mInfo.addListener(list2);
							}
						}						
					}
				}else if(stmt.getRightOp() instanceof ThisRef){
					Listener list = new Listener("onClick", false, "boolean onNavigationItemSelected(int,long)", getCurrentSootMethod().getDeclaringClass().getName());
					list.setListenerClass(rightRegType);
					mInfo.addListener(list);
				}	
			}
		}
		addAllToResultInfo(toAddInfos);
	}
	
	public void caseInvokeStmt(final InvokeStmt stmt) {
		if(Thread.currentThread().isInterrupted()){
			return;
		}
		InvokeExpr invokeExpr = stmt.getInvokeExpr();
		String methodSignature = helpMethods.getSignatureOfInvokeExpr(invokeExpr);

//		     virtualinvoke $r2.<android.app.ActionBar: void setListNavigationCallbacks(android.widget.SpinnerAdapter,android.app.ActionBar$OnNavigationListener)>($r3, r18);
			if (methodSignature.equals("<android.app.ActionBar: void setListNavigationCallbacks(android.widget.SpinnerAdapter,android.app.ActionBar$OnNavigationListener)>")){
				DropDownNavMenuInfo mInfo = new DropDownNavMenuInfo(helpMethods.getCallerOfInvokeStmt(invokeExpr), helpMethods.getParameterOfInvokeStmt(invokeExpr, 1), helpMethods.getParameterOfInvokeStmt(invokeExpr, 0), getCurrentSootMethod());
				mInfo.setArraySwitch(new StmtSwitchForArrayAdapter(helpMethods.getParameterOfInvokeStmt(invokeExpr, 0), getCurrentSootMethod()));
				addToResultInfo(mInfo);
			}
			
			Set<Info> resultInfos = getResultInfos();
			for (Info i : resultInfos){
				DropDownNavMenuInfo mInfo = (DropDownNavMenuInfo) i;
				
				if (mInfo.getArraySwitch() != null)
					mInfo = (DropDownNavMenuInfo) mInfo.getArraySwitch().caseInvokeStmt(stmt, mInfo);
			}
			
			// not checked, see comment on top
////		     virtualinvoke $r2.<android.app.ActionBar: void setNavigationMode(int)>(1);
//			if (methodSignature.equals("<android.app.ActionBar: void setNavigationMode(int)>") && helpMethods.getParameterOfInvokeStmt(invokeExpr, 0).equals("1")){
//				mInfo.setNavigationMode();
//			}
		
	}
	
	public void caseAssignStmt(final AssignStmt stmt){
//		List<Info> resultInfos = getResultInfos();
		Set<Info> toAddInfos = new LinkedHashSet<>();
		Set<Info> toRemoveInfos = new LinkedHashSet<>();
		for (Info i : getResultInfos()){
			if(Thread.currentThread().isInterrupted()){
				return;
			}

			DropDownNavMenuInfo mInfo = (DropDownNavMenuInfo) i;
			
			if (mInfo.getArraySwitch() != null)
				mInfo = (DropDownNavMenuInfo) (mInfo.getArraySwitch().caseAssignStmt(stmt, mInfo));
			
			String leftReg = helpMethods.getLeftRegOfAssignStmt(stmt);
			
			if (stmt.containsInvokeExpr()){
				InvokeExpr invokeExpr = stmt.getInvokeExpr();
				String methodSignature = helpMethods.getSignatureOfInvokeExpr(invokeExpr);

//				   $r2 = virtualinvoke $r0.<com.example.Testapp.MainActivity2: android.app.ActionBar getActionBar()>();
					if (methodSignature.equals("<android.app.Activity: android.app.ActionBar getActionBar()>")
							&& leftReg.equals(mInfo.getActionBarReg())){
						mInfo.setActionBarReg("");
						mInfo.setActionBarThere();
						String actName = helpMethods.getCallerOfInvokeStmt(invokeExpr);
						if (checkMethods.checkIfValueIsVariable(actName)){
							mInfo.setActivityReg(actName);
						}else{
							mInfo.setActivityName(actName);
						}
					}
					else{
						if (mInfo.getListenerReg().equals(leftReg)){
							mInfo.setListenerReg("");
							if (!Helper.isClassInSystemPackage(helpMethods.getCallerTypeOfInvokeStmt(invokeExpr))) {
								List<InterProcInfo> resList = interprocMethods2.findReturnValueInMethod2(stmt);
								if (resList.size() > 0) {
									Listener list = new Listener("onClick", false, "boolean onNavigationItemSelected(int,long)", getCurrentSootMethod().getDeclaringClass().getName());
									list.setListenerClass(resList.get(0).getClassOfSearchedReg());
									mInfo.addListener(list);
									if (resList.size() > 1) {
										for (int j = 1; j < resList.size(); j++) {
											InterProcInfo workingInfo = (InterProcInfo) resList.get(j);
											Listener newListener = new Listener("onClick", false, "boolean onNavigationItemSelected(int,long)", getCurrentSootMethod().getDeclaringClass().getName());
											newListener.setListenerClass(workingInfo.getClassOfSearchedReg());
											mInfo.addListener(newListener);
										}
									}
								}
							}
						}
						if (mInfo.getActivityReg().equals(leftReg)){
							mInfo.setActivityReg("");
							if (!Helper.isClassInSystemPackage(helpMethods.getCallerTypeOfInvokeStmt(invokeExpr))) {
								List<InterProcInfo> resList = interprocMethods2.findReturnValueInMethod2(stmt);
								if (resList.size() > 0) {
									mInfo.setActivityName(((InterProcInfo) resList.get(0)).getClassOfSearchedReg());
									if (resList.size() > 1) {
										for (int j = 1; j < resList.size(); j++) {
											InterProcInfo workingInfo = (InterProcInfo) resList.get(j);
											DropDownNavMenuInfo newInfo = (DropDownNavMenuInfo) mInfo.clone();
											toAddInfos.add(newInfo);
											newInfo.setActivityName(workingInfo.getClassOfSearchedReg());
										}
									}
								}
							}
						}
					}
			}else{
				
//				r7 = (android.widget.PopupMenu$OnMenuItemClickListener) $r0;
				if (leftReg.equals(mInfo.getActivityReg())){
					
					if (stmt.getRightOp() instanceof NewExpr){
						mInfo.setActivityReg("");
						mInfo.setActivityName(helpMethods.getTypeOfRightRegOfAssignStmt(stmt));
					}else if(stmt.getRightOp() instanceof FieldRef){
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
							toRemoveInfos.add(mInfo);
							for (FieldInfo fInfo : fInfos){
								if(Thread.currentThread().isInterrupted()){
									return;
								}
								if(fInfo.methodToStart != null && fInfo.methodToStart.method().hasActiveBody()){
									Unit workingUnit = fInfo.unitToStart;
									DropDownNavMenuInfo newInfo = new DropDownNavMenuInfo("", "", "", getCurrentSootMethod());
									newInfo.setActivityReg(fInfo.register.getName());
									StmtSwitchForNavDropDownMenus newStmtSwitch = new StmtSwitchForNavDropDownMenus(newInfo, getCurrentSootMethod());
									previousFields.forEach(x->newStmtSwitch.addPreviousField(x));
									iteratorHelper.runOverToFindSpecValuesBackwards(fInfo.methodToStart.method().getActiveBody(), workingUnit, newStmtSwitch);
									Set<Info> initValues = newStmtSwitch.getResultInfos();

									if(initValues.size() > 0) {
										List<Info> listInfo = initValues.stream().collect(Collectors.toList());
										if(listInfo.indexOf(newInfo) == -1){
											continue;
										}
										Info initInfo = listInfo.get(listInfo.indexOf(newInfo));
										DropDownNavMenuInfo newInfo2 = (DropDownNavMenuInfo) mInfo.clone();
										newInfo2.setActivityName(((DropDownNavMenuInfo)initInfo).getActivityName());
										newInfo2.setActivityReg("");
										toAddInfos.add(newInfo2);
									}
								}
							}
						}else{
							mInfo.setActivityReg("");
							Helper.saveToStatisticalFile("Error NavDropDownSwitch: Doesn't find activityReg in initializationOfField: " + stmt);
						}
					}else{
						mInfo.setActivityReg(helpMethods.getRightRegOfAssignStmt(stmt));
					}
				}
				if (leftReg.equals(mInfo.getListenerReg())){
					if (stmt.getRightOp() instanceof NewExpr){
						mInfo.setListenerReg("");
						Listener list = new Listener("onClick", false, "boolean onNavigationItemSelected(int,long)", getCurrentSootMethod().getDeclaringClass().getName());
						list.setListenerClass(helpMethods.getTypeOfRightRegOfAssignStmt(stmt));
						mInfo.addListener(list);
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
							for (FieldInfo fInfo : fInfos){
								if(Thread.currentThread().isInterrupted()){
									return;
								}
								if(fInfo.methodToStart != null && fInfo.methodToStart.method().hasActiveBody()){
									Unit workingUnit = fInfo.unitToStart;
									DropDownNavMenuInfo newInfo =  new DropDownNavMenuInfo("", "", "", getCurrentSootMethod());
									newInfo.setListenerReg(fInfo.register.getName());
									StmtSwitchForNavDropDownMenus newStmtSwitch = new StmtSwitchForNavDropDownMenus(newInfo, getCurrentSootMethod());
									previousFields.forEach(x->newStmtSwitch.addPreviousField(x));
									iteratorHelper.runOverToFindSpecValuesBackwards(fInfo.methodToStart.method().getActiveBody(), workingUnit, newStmtSwitch);
									Set<Info> initValues = newStmtSwitch.getResultInfos();

									if(initValues.size() > 0) {
										List<Info> listInfo = initValues.stream().collect(Collectors.toList());
										if(listInfo.indexOf(newInfo) == -1){
											continue;
										}
										Info initInfo = listInfo.get(listInfo.indexOf(newInfo));
										mInfo.addListener(((DropDownNavMenuInfo)initInfo).getListener());
									}
								}
							}
						}else{
							mInfo.setListenerReg("");
							Helper.saveToStatisticalFile("Error NavDropDownSwitch: Doesn't find listenerReg in initializationOfField: " + stmt);
						}
						
					}else{
						mInfo.setListenerReg(helpMethods.getRightRegOfAssignStmt(stmt));
					}
				}
			}
			
		}
		
		if (toRemoveInfos.size() > 0){
			removeAllFromResultInfos(toRemoveInfos);
		}
		addAllToResultInfo(toAddInfos);
	}

}
