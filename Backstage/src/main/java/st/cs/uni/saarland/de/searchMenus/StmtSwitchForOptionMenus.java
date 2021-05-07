package st.cs.uni.saarland.de.searchMenus;

import soot.SootField;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.*;
import st.cs.uni.saarland.de.entities.FieldInfo;
import st.cs.uni.saarland.de.helpClasses.Helper;
import st.cs.uni.saarland.de.helpClasses.Info;
import st.cs.uni.saarland.de.helpClasses.InterProcInfo;
import st.cs.uni.saarland.de.helpClasses.MyStmtSwitch;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class StmtSwitchForOptionMenus extends MyStmtSwitch {

//	private MenuInfo mInfo;
	// layoutId here the if of the view object!
	
//	$r0 := @this: com.example.Testapp.MainActivity;
//    $r1 := @parameter0: android.view.Menu;
//    $r2 = virtualinvoke $r0.<com.example.Testapp.MainActivity: android.view.MenuInflater getMenuInflater()>();
//    virtualinvoke $r2.<android.view.MenuInflater: void inflate(int,android.view.Menu)>(2131099649, $r1);
//	return $r2
	
	
	public StmtSwitchForOptionMenus(MenuInfo newInfo, SootMethod currentSootMethod) {
		super(newInfo, currentSootMethod);
	}
	
	public StmtSwitchForOptionMenus(SootMethod currentSootMethod) {
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
			MenuInfo mInfo = (MenuInfo) i;
			
			String leftReg = helpMethods.getLeftRegOfIdentityStmt(stmt);
			String rightRegType = helpMethods.getRightClassTypeOfIdentityStmt(stmt);
			if (leftReg.equals(mInfo.getActivityReg())){
				mInfo.setActivityReg("");
				if(stmt.getRightOp() instanceof ParameterRef){
					String actName = helpMethods.getRightClassTypeOfIdentityStmt(stmt);
					if (checkMethods.checkIfValueIsVariable(actName)){
						int paramIndex = ((ParameterRef)stmt.getRightOp()).getIndex();
						List<Info> resList = interprocMethods2.findInReachableMethods2(paramIndex,  getCurrentSootMethod(), new ArrayList<SootMethod>());
						if (resList.size() > 0){
							mInfo.setActivityClassName(((InterProcInfo)resList.get(0)).getClassOfSearchedReg());
							if (resList.size() > 1){
								for (int j = 1; j < resList.size() ; j++){
									InterProcInfo workingInfo = (InterProcInfo) resList.get(j);
									MenuInfo newInfo = (MenuInfo) mInfo.clone();
									toAddInfos.add(newInfo);
									newInfo.setActivityClassName(workingInfo.getClassOfSearchedReg());
								}
							}
						}
					}else{
						mInfo.setActivityClassName(actName);
					}
				}else if(stmt.getRightOp() instanceof ThisRef){
					mInfo.setActivityClassName(rightRegType);
				}	
			}
			if (leftReg.equals(mInfo.getLayoutIDReg())){
				mInfo.setLayoutIDReg("");
				if(stmt.getRightOp() instanceof ParameterRef){
					int paramIndex = ((ParameterRef)stmt.getRightOp()).getIndex();
					List<Info> resList = interprocMethods2.findInReachableMethods2(paramIndex,  getCurrentSootMethod(), new ArrayList<SootMethod>());
					if (resList.size() > 0){
						mInfo.setLayoutID(((InterProcInfo)resList.get(0)).getValueOfSearchedReg());
						if (resList.size() > 1){
							for (int j = 1; j < resList.size() ; j++){
								InterProcInfo workingInfo = (InterProcInfo) resList.get(j);
								MenuInfo newInfo = (MenuInfo) mInfo.clone();
								toAddInfos.add(newInfo);
								newInfo.setLayoutID(workingInfo.getValueOfSearchedReg());
							}
						}
					}
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
		
		if(methodSignature.equals("<android.view.MenuInflater: void inflate(int,android.view.Menu)>")){
			// searchedE = MenuContainer
			String layoutID = helpMethods.getParameterOfInvokeStmt(invokeExpr, 0);
			String layoutIDReg = "";
			if (!checkMethods.checkIfValueIsID(layoutID)){
				layoutIDReg = layoutID;
				layoutID = "";
			}
			MenuInfo mInfo = new MenuInfo(helpMethods.getParameterOfInvokeStmt(invokeExpr, 1), layoutID, helpMethods.getCallerOfInvokeStmt(invokeExpr), getCurrentSootMethod().getDeclaringClass().getName());
			mInfo.setLayoutIDReg(layoutIDReg);
			addToResultInfo(mInfo);

		}		
	}
	
	
	
	public void caseAssignStmt(final AssignStmt stmt){
		if(Thread.currentThread().isInterrupted()){
			return;
		}
		//System.out.println(stmt);
		Set<Info> toAddInfos = new LinkedHashSet<>();
		Set<Info> toRemoveInfos = new LinkedHashSet<>();
		for (Info i : getResultInfos()){
			if(Thread.currentThread().isInterrupted()){
				return;
			}
			MenuInfo mInfo = (MenuInfo) i;
			
			String leftReg = helpMethods.getLeftRegOfAssignStmt(stmt);
			
			// searchedE = MenuContainer
			if (leftReg.equals(mInfo.getSearchedEReg())){
				//TODO ?
			}
			if (leftReg.equals(mInfo.getInflaterReg())){
				if (stmt.containsInvokeExpr() && 
						(helpMethods.getSignatureOfInvokeExpr(stmt.getInvokeExpr()).equals("<android.app.Activity: android.view.MenuInflater getMenuInflater()>") ||
						helpMethods.getSignatureOfInvokeExpr(stmt.getInvokeExpr()).equals("<android.support.v7.app.AppCompatActivity: android.view.MenuInflater getMenuInflater()>"))){
					mInfo.setInflaterReg("");
					String activityName = helpMethods.getCallerTypeOfInvokeStmt(stmt.getInvokeExpr());
					if (checkMethods.checkIfValueIsVariable(activityName)){
						mInfo.setActivityReg(helpMethods.getCallerOfInvokeStmt(stmt.getInvokeExpr()));
					}else{
						mInfo.setActivityReg("");
						mInfo.setActivityClassName(helpMethods.getCallerTypeOfInvokeStmt(stmt.getInvokeExpr()));
					}
				}else if( stmt.containsInvokeExpr()){
					mInfo.setInflaterReg("");
					if (!Helper.isClassInSystemPackage(helpMethods.getCallerTypeOfInvokeStmt(stmt.getInvokeExpr()))) {
						String posActName = stmt.getInvokeExpr().getMethod().getReturnType().toString();
						if (!Helper.isClassInSystemPackage(posActName)) {
							mInfo.setActivityClassName(posActName);
						}
					}
				}
			}
			if (leftReg.equals(mInfo.getLayoutIDReg())){
			
				if (stmt.containsInvokeExpr()){
					mInfo.setLayoutIDReg("");
					InvokeExpr invokeExpr = stmt.getInvokeExpr();
					 if (!Helper.isClassInSystemPackage(helpMethods.getCallerTypeOfInvokeStmt(invokeExpr))){
						 List<InterProcInfo> resList = interprocMethods2.findReturnValueInMethod2(stmt);
						 if (resList.size() > 0){
							 mInfo.setLayoutID(resList.get(0).getValueOfSearchedReg());
							 if (resList.size() > 1){
								for (int j = 1; j < resList.size() ; j++){
									InterProcInfo workingInfo = (InterProcInfo) resList.get(j);
									MenuInfo newInfo = (MenuInfo) mInfo.clone();
									toAddInfos.add(newInfo);
									newInfo.setLayoutID(workingInfo.getValueOfSearchedReg());
								}
							}
						}
					}
				}else{
					if (stmt.getRightOp() instanceof FieldRef){
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
								if(fInfo.value != null){
									MenuInfo newInfo = (MenuInfo) mInfo.clone();
									newInfo.setLayoutID(fInfo.value);
									newInfo.setLayoutIDReg("");
									toAddInfos.add(newInfo);
									continue;
								}else{
									if(fInfo.methodToStart != null && fInfo.methodToStart.method().hasActiveBody()){
										Unit workingUnit = fInfo.unitToStart;
										MenuInfo newInfo = new MenuInfo("", "", "", getCurrentSootMethod().getDeclaringClass().getName());
										newInfo.setLayoutIDReg(fInfo.register.getName());
										StmtSwitchForOptionMenus newStmtSwitch = new StmtSwitchForOptionMenus(newInfo, getCurrentSootMethod());
										previousFields.forEach(x->newStmtSwitch.addPreviousField(x));
										iteratorHelper.runOverToFindSpecValuesBackwards(fInfo.methodToStart.method().getActiveBody(), workingUnit, newStmtSwitch);
										Set<Info> initValues = newStmtSwitch.getResultInfos();

										if(initValues.size() > 0) {
											List<Info> listInfo = initValues.stream().collect(Collectors.toList());
											if(listInfo.indexOf(newInfo) == -1){
												continue;
											}
											Info initInfo = listInfo.get(listInfo.indexOf(newInfo));
											MenuInfo newInfo2 = (MenuInfo) mInfo.clone();
											newInfo2.setLayoutID(((MenuInfo)initInfo).getLayoutID());
											newInfo2.setLayoutIDReg("");
											toAddInfos.add(newInfo2);
										}
									}
								}
							}
						}else{
							mInfo.setLayoutIDReg("");
							Helper.saveToStatisticalFile("Error ListenerSwitch: Doesn't find layoutIdReg in initializationOfField: " + stmt);
						}
					}else{
						mInfo.setLayoutIDReg("");
						String rightReg = helpMethods.getRightRegOfAssignStmt(stmt);
						if (checkMethods.checkIfValueIsID(rightReg)){
							mInfo.setLayoutID(rightReg);
						}else
							mInfo.setLayoutIDReg(rightReg);
					}
				}
			}
			if (leftReg.equals(mInfo.getActivityReg())){
				
				if (stmt.containsInvokeExpr()){
					mInfo.setActivityReg("");
					InvokeExpr invokeExpr = stmt.getInvokeExpr();
					 if (!Helper.isClassInSystemPackage(helpMethods.getCallerTypeOfInvokeStmt(invokeExpr))){
						 List<InterProcInfo> resList = interprocMethods2.findReturnValueInMethod2(stmt);
						 if (resList.size() > 0){
							 mInfo.setActivityClassName(resList.get(0).getClassOfSearchedReg());
							if (resList.size() > 1){
								for (int j = 1; j < resList.size(); j++){
									InterProcInfo workingInfo = (InterProcInfo) resList.get(j);
									MenuInfo newInfo = (MenuInfo) mInfo.clone();
									toAddInfos.add(newInfo);
									newInfo.setActivityClassName(workingInfo.getClassOfSearchedReg());
								}
							}
						}
					}
				}else{
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
							toRemoveInfos.add(mInfo);
							for (FieldInfo fInfo : fInfos){
								if(Thread.currentThread().isInterrupted()){
									return;
								}
								if(fInfo.methodToStart != null && fInfo.methodToStart.method().hasActiveBody()){
									Unit workingUnit = fInfo.unitToStart;
									MenuInfo newInfo = new MenuInfo("", "", "", getCurrentSootMethod().getDeclaringClass().getName());
									newInfo.setActivityReg(fInfo.register.getName());
									StmtSwitchForOptionMenus newStmtSwitch = new StmtSwitchForOptionMenus(newInfo, getCurrentSootMethod());
									previousFields.forEach(x->newStmtSwitch.addPreviousField(x));
									iteratorHelper.runOverToFindSpecValuesBackwards(fInfo.methodToStart.method().getActiveBody(), workingUnit, newStmtSwitch);
									Set<Info> initValues = newStmtSwitch.getResultInfos();

									if(initValues.size() > 0) {
										List<Info> listInfo = initValues.stream().collect(Collectors.toList());
										if(listInfo.indexOf(newInfo) == -1){
											continue;
										}
										Info initInfo = listInfo.get(listInfo.indexOf(newInfo));
										MenuInfo newInfo2 = (MenuInfo) mInfo.clone();
										newInfo2.setActivityClassName(((MenuInfo)initInfo).getActivityClassName());
										newInfo2.setActivityReg("");
										toAddInfos.add(newInfo2);
									}
								}
							}
						}
					}else{
						mInfo.setActivityReg("");
						String rightReg = helpMethods.getRightRegOfAssignStmt(stmt);
						if (stmt.getRightOp() instanceof NewExpr){
							mInfo.setActivityClassName(helpMethods.getTypeOfRightRegOfAssignStmt(stmt));
						}else{
							mInfo.setActivityReg(rightReg);
						}
					}
				}
			}
			
		}		
		if (toRemoveInfos.size() > 0){
			removeAllFromResultInfos(toRemoveInfos);
		}
		addAllToResultInfo(toAddInfos);
	}
	
	@Override
	public void defaultCase(Object o){
	}
	
//	@Override
//	public boolean run(){
//		if (mInfo != null)
//			return mInfo.allValuesFound();
//		return false;
//	}
//	
//	public void init(){
//		super.init();
//		mInfo = null;
//		shouldBreak = false;
//		resultInfo = null;
//	}

	
}
