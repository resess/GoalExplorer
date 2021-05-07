package st.cs.uni.saarland.de.dissolveSpecXMLTags;

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

public class StmtSwitchForFragments extends MyStmtSwitch {

	// code that is searched for:
	// $r4 = virtualinvoke $r0.<com.example.Testapp.MainActivity3: android.app.FragmentManager getFragmentManager()>();
	// $r5 = virtualinvoke $r4.<android.app.FragmentManager: android.app.FragmentTransaction beginTransaction()>();
	// r17 = new com.example.Testapp.AddingFragment;
	// virtualinvoke $r5.<android.app.FragmentTransaction: android.app.FragmentTransaction add(int,android.app.Fragment)>(2131165223, r17);
	// $r5 = virtualinvoke $r5.<android.app.FragmentTransaction: android.app.FragmentTransaction replace(int,android.app.Fragment)>(2131165185, $r2);
	// virtualinvoke $r5.<android.app.FragmentTransaction: int commit()>();

	// constructor for interprocedural analysis
	private StmtSwitchForFragments(FragmentDynInfo newInfo, SootMethod currentSootMethod) {
		super(newInfo, currentSootMethod);
	}
	
	public StmtSwitchForFragments(SootMethod currentSootMethod) {
		super(currentSootMethod);
	}

	public void caseIdentityStmt(IdentityStmt stmt){
		String leftReg = helpMethods.getLeftRegOfIdentityStmt(stmt);
		
		if (stmt.getRightOp() instanceof ParameterRef){
			Set<Info> toAddInfos = new LinkedHashSet<>();
			for (Info resInfo: getResultInfos()){
				FragmentDynInfo info = (FragmentDynInfo) resInfo;
				
				if (leftReg.equals(info.getSearchedEReg())){
					info.setSearchedEReg("");
					int paramIndex = ((ParameterRef)stmt.getRightOp()).getIndex();
					List<Info> resList = interprocMethods2.findInReachableMethods2(paramIndex,  getCurrentSootMethod(), new ArrayList<SootMethod>());
					if (resList.size() > 0){
						info.setFragmentClassName(((InterProcInfo)resList.get(0)).getClassOfSearchedReg());
						if (resList.size() > 1){
							for (int j = 1; j < resList.size() ; j++){
								InterProcInfo workingInfo = (InterProcInfo) resList.get(j);
								FragmentDynInfo newInfo = (FragmentDynInfo) info.clone();
								toAddInfos.add(newInfo);
								newInfo.setFragmentClassName(workingInfo.getClassOfSearchedReg());
							}
						}
					}
				}else if (leftReg.equals(info.getUiElementWhereFragIsAddedIDReg())){
					info.setUiElementWhereFragIsAddedIDReg("");
					int paramIndex = ((ParameterRef)stmt.getRightOp()).getIndex();
					List<Info> resList = interprocMethods2.findInReachableMethods2(paramIndex,  getCurrentSootMethod(), new ArrayList<SootMethod>());
					if (resList.size() > 0){
						info.setUiElementWhereFragIsAddedID(((InterProcInfo)resList.get(0)).getValueOfSearchedReg());
						if (resList.size() > 1){
							for (int j = 1; j < resList.size() ; j++){
								InterProcInfo workingInfo = (InterProcInfo) resList.get(j);
								FragmentDynInfo newInfo = (FragmentDynInfo) info.clone();
								toAddInfos.add(newInfo);
								newInfo.setUiElementWhereFragIsAddedID(workingInfo.getValueOfSearchedReg());
							}
						}
					}	
				}
				else if (leftReg.equals(info.getSearchedEReg())){
					info.setSearchedEReg("");
					int paramIndex = ((ParameterRef)stmt.getRightOp()).getIndex();
					List<Info> resList = interprocMethods2.findInReachableMethods2(paramIndex,  getCurrentSootMethod(), new ArrayList<SootMethod>());
					if (resList.size() > 0){
						String fragClassName = ((InterProcInfo)resList.get(0)).getClassOfSearchedReg().replace("class ", "").replace("\"", "");
						info.setFragmentClassName(fragClassName);
						if (resList.size() > 1){
							for (int j = 1; j < resList.size() ; j++){
								InterProcInfo workingInfo = (InterProcInfo) resList.get(j);
								FragmentDynInfo newInfo = (FragmentDynInfo) info.clone();
								toAddInfos.add(newInfo);
								String fragClassName2 = workingInfo.getClassOfSearchedReg().replace("class ", "").replace("\"", "");
								newInfo.setFragmentClassName(fragClassName2);
							}
						}
					}
				}
			}
			addAllToResultInfo(toAddInfos);
		}
	}
	
	public void caseInvokeStmt(InvokeStmt stmt){
		InvokeExpr invokeExpr = stmt.getInvokeExpr();
		String methodSignature = helpMethods.getSignatureOfInvokeExpr(invokeExpr);
		
		
			Set<Info> resultInfos = getResultInfos();
			if(resultInfos.size() == 0){
				if (("<android.app.FragmentTransaction: int commit()>".equals(methodSignature))
						|| ("<android.support.v4.app.FragmentTransaction: int commit()>".equals(methodSignature))){
					String caller = helpMethods.getCallerOfInvokeStmt(invokeExpr);
					FragmentDynInfo info = new FragmentDynInfo(caller);
					resultInfos.add(info);
				}else
					shouldBreak = true;
				
			}else{
				// check for other methods that could also be called in assign with invoke expr:
				for (Info resInfo: resultInfos){
					FragmentDynInfo info = (FragmentDynInfo) resInfo;
					analyseMainMethodThatCouldBeInvokeOrAssign(stmt.getInvokeExpr(), info);
				}
			}
	}
	
	public void caseAssignStmt(AssignStmt stmt){

			Set<Info> toAddInfos = new LinkedHashSet<>();
			Set<Info> toRemoveInfos = new LinkedHashSet<>();
			for (Info resInfo: getResultInfos()){
				FragmentDynInfo info = (FragmentDynInfo) resInfo;
				
				String leftReg = helpMethods.getLeftRegOfAssignStmt(stmt);
				
				if (stmt.containsInvokeExpr()){
					InvokeExpr invokeExpr = stmt.getInvokeExpr();
					boolean foundThereSth = analyseMainMethodThatCouldBeInvokeOrAssign(invokeExpr, info);
					if (foundThereSth)
						continue;

					String methodSubSignature = invokeExpr.getMethod().getSubSignature();
					if (("android.app.FragmentTransaction beginTransaction()".equals(methodSubSignature) && leftReg.equals(info.getFragmentTransactionReg()))
							|| ("android.support.v4.app.FragmentTransaction beginTransaction()".equals(methodSubSignature)/* && leftReg.equals(info.getFragmentTransactionReg())*/)){
						info.setFragmentTransactionReg("");
						info.setFragmentManagerReg(helpMethods.getCallerOfInvokeStmt(invokeExpr));
					}else if (("android.app.FragmentManager getFragmentManager()".equals(methodSubSignature) && leftReg.equals(info.getFragmentManagerReg()))
							|| ("android.support.v4.app.FragmentManager getSupportFragmentManager()".equals(methodSubSignature) /*&& leftReg.equals(info.getFragmentManagerReg())*/)){
						info.setFragmentManagerReg("");
					}else{
						// check if an interproc. call into the one of the searched variables is done, if so go to that method and analyse it
						// not needed for this calls
	//					 || leftReg.equals(info.getFragmentManagerReg()) || leftReg.equals(info.getFragmentTransactionReg())
						if (leftReg.equals(info.getSearchedEReg())){
							info.setSearchedEReg("");
							if (!Helper.isClassInSystemPackage(helpMethods.getCallerTypeOfInvokeStmt(invokeExpr))){
								List<InterProcInfo> resList = interprocMethods2.findReturnValueInMethod2(stmt);
								if (resList.size() > 0){
									String fragClassName = ((InterProcInfo)resList.get(0)).getClassOfSearchedReg().replace("class ", "").replace("\"", "");
									info.setFragmentClassName(fragClassName);
									if (resList.size() > 1){
										for (int j = 1; j < resList.size() ; j++){
											InterProcInfo workingInfo = (InterProcInfo) resList.get(j);
											FragmentDynInfo newInfo = (FragmentDynInfo) info.clone();
											toAddInfos.add(newInfo);
											String fragClassName2 = workingInfo.getClassOfSearchedReg().replace("class ", "").replace("\"", "");
											newInfo.setFragmentClassName(fragClassName2);
										}
									}
								}
							}
						}else if (leftReg.equals(info.getUiElementWhereFragIsAddedIDReg())){
							info.setUiElementWhereFragIsAddedIDReg("");
							if (!Helper.isClassInSystemPackage(helpMethods.getCallerTypeOfInvokeStmt(invokeExpr))){
								List<InterProcInfo> resList = interprocMethods2.findReturnValueInMethod2(stmt);
								if (resList.size() > 0){
									info.setUiElementWhereFragIsAddedID(((InterProcInfo)resList.get(0)).getValueOfSearchedReg());
									if (resList.size() > 1){
										for (int j = 1; j < resList.size() ; j++){
											InterProcInfo workingInfo = (InterProcInfo) resList.get(j);
											FragmentDynInfo newInfo = (FragmentDynInfo) info.clone();
											toAddInfos.add(newInfo);
											newInfo.setUiElementWhereFragIsAddedID(workingInfo.getValueOfSearchedReg());
										}
									}
								}
							}
						}
					
					}
					
				}else{
					if (leftReg.equals(info.getSearchedEReg())){
						info.setSearchedEReg("");
						String rightReg = helpMethods.getRightRegOfAssignStmt(stmt);
						if (stmt.getRightOp() instanceof NewExpr){

							info.setFragmentClassName(helpMethods.getTypeOfRightRegOfAssignStmt(stmt));
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
								toRemoveInfos.add(info);
								for (FieldInfo fInfo : fInfos){
									if(fInfo.value != null){
										FragmentDynInfo newInfo = (FragmentDynInfo) info.clone();
										newInfo.setSearchedEReg("");
										newInfo.setUiElementWhereFragIsAddedID(fInfo.value);
										toAddInfos.add(newInfo);
										continue;
									}else{
										if(fInfo.methodToStart != null && fInfo.methodToStart.method().hasActiveBody()){
											Unit workingUnit = fInfo.unitToStart;
											FragmentDynInfo newInfo = new FragmentDynInfo("");
											newInfo.setSearchedEReg(fInfo.register.getName());
											StmtSwitchForFragments newStmtSwitch = new StmtSwitchForFragments(newInfo, getCurrentSootMethod());
											previousFields.forEach(x->newStmtSwitch.addPreviousField(x));
											iteratorHelper.runOverToFindSpecValuesBackwards(fInfo.methodToStart.method().getActiveBody(), workingUnit, newStmtSwitch);
											Set<Info> initValues = newStmtSwitch.getResultInfos();

											if(initValues.size() > 0) {
												List<Info> listInfo = initValues.stream().collect(Collectors.toList());
												if(listInfo.indexOf(newInfo) == -1){
													continue;
												}
												Info initInfo = listInfo.get(listInfo.indexOf(newInfo));
												FragmentDynInfo newInfo2 = (FragmentDynInfo) info.clone();
												newInfo2.setUiElementWhereFragIsAddedID(((FragmentDynInfo)initInfo).getUiElementWhereFragIsAddedID());
												newInfo2.setSearchedEReg("");
												toAddInfos.add(newInfo2);
											}
										}
									}
								}
							}else{
								info.setSearchedEReg("");
								Helper.saveToStatisticalFile("Error FragmentSwitch: Doesn't find SearchedEReg in initializationOfField: " + stmt);
							}
						}else{
							info.setSearchedEReg(rightReg);
						}
					}	
					
					if (leftReg.equals(info.getUiElementWhereFragIsAddedIDReg())){
						info.setUiElementWhereFragIsAddedIDReg("");
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
								toRemoveInfos.add(info);
								for (FieldInfo fInfo : fInfos){
									if(fInfo.value != null){
										FragmentDynInfo newInfo = (FragmentDynInfo) info.clone();
										newInfo.setUiElementWhereFragIsAddedID(fInfo.value);
										newInfo.setUiElementWhereFragIsAddedIDReg("");
										toAddInfos.add(newInfo);
										continue;
									}else{
										if(fInfo.methodToStart != null && fInfo.methodToStart.method().hasActiveBody()){
											Unit workingUnit = fInfo.unitToStart;
											FragmentDynInfo newInfo = new FragmentDynInfo("");
											newInfo.setUiElementWhereFragIsAddedIDReg(fInfo.register.getName());
											StmtSwitchForFragments newStmtSwitch = new StmtSwitchForFragments(newInfo, getCurrentSootMethod());
											previousFields.forEach(x->newStmtSwitch.addPreviousField(x));
											iteratorHelper.runOverToFindSpecValuesBackwards(fInfo.methodToStart.method().getActiveBody(), workingUnit, newStmtSwitch);
											Set<Info> initValues = newStmtSwitch.getResultInfos();

											if(initValues.size() > 0) {
												List<Info> listInfo = initValues.stream().collect(Collectors.toList());
												if(listInfo.indexOf(newInfo) == -1){
													continue;
												}
												Info initInfo = listInfo.get(listInfo.indexOf(newInfo));
												FragmentDynInfo newInfo2 = (FragmentDynInfo) info.clone();
												newInfo2.setUiElementWhereFragIsAddedID(((FragmentDynInfo)initInfo).getUiElementWhereFragIsAddedID());
												newInfo2.setUiElementWhereFragIsAddedIDReg("");
												toAddInfos.add(newInfo2);
											}
										}
									}
								}
							}else{
								info.setUiElementWhereFragIsAddedIDReg("");
								Helper.saveToStatisticalFile("Error FragmentSwitch: Doesn't find UiElementWhereFragIsAddedIDReg in initializationOfField: " + stmt);
							}
						}else{
							info.setUiElementWhereFragIsAddedIDReg("");
							String rightReg = helpMethods.getRightRegOfAssignStmt(stmt);
							if (checkMethods.checkIfValueIsID(rightReg)){
								info.setUiElementWhereFragIsAddedID(rightReg);
							}else{
								info.setUiElementWhereFragIsAddedIDReg(rightReg);
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
	

	private boolean analyseMainMethodThatCouldBeInvokeOrAssign(InvokeExpr invokeExpr, FragmentDynInfo info){
		boolean foundSth = false;
		String methodSubSignature = invokeExpr.getMethod().getSubSignature();
			// TODO check if adding and replacing could be done with one Manager
			if (("android.app.FragmentTransaction replace(int,android.app.Fragment)".equals(methodSubSignature))
					|| ("android.app.FragmentTransaction replace(int,android.app.Fragment,java.lang.String)".equals(methodSubSignature))
					|| ("android.support.v4.app.FragmentTransaction replace(int,android.support.v4.app.Fragment)".equals(methodSubSignature))
					|| ("android.support.v4.app.FragmentTransaction replace(int,android.support.v4.app.Fragment,java.lang.String)".equals(methodSubSignature))){
				foundSth = true;
				String caller = helpMethods.getCallerOfInvokeStmt(invokeExpr);
				if (caller.equals(info.getFragmentTransactionReg())){
					info.setSearchedEReg(helpMethods.getParameterOfInvokeStmt(invokeExpr, 1));
					// TODO check if variable
					String id = helpMethods.getParameterOfInvokeStmt(invokeExpr, 0);
					if (checkMethods.checkIfValueIsID(id)){
						info.setUiElementWhereFragIsAddedID(id);
					}else{
						info.setUiElementWhereFragIsAddedIDReg(id);
					}
				}
			}else if ("android.app.FragmentTransaction add(int,android.app.Fragment)".equals(methodSubSignature)
					|| "android.app.FragmentTransaction add(int,android.app.Fragment,java.lang.String)".equals(methodSubSignature)
					|| "android.app.FragmentTransaction add(int,android.support.v4.app.Fragment)".equals(methodSubSignature)
					|| "android.app.FragmentTransaction add(int,android.support.v4.app.Fragment,java.lang.String)".equals(methodSubSignature) ){
				foundSth = true;
				String caller = helpMethods.getCallerOfInvokeStmt(invokeExpr);
				if (caller.equals(info.getFragmentTransactionReg())){
					info.setSearchedEReg(helpMethods.getParameterOfInvokeStmt(invokeExpr, 1));
					String id = helpMethods.getParameterOfInvokeStmt(invokeExpr, 0);
					if (checkMethods.checkIfValueIsID(id)){
						info.setUiElementWhereFragIsAddedID(id);
					}else{
						info.setUiElementWhereFragIsAddedIDReg(id);
					}

				}
			}
		return foundSth;
	}

}
