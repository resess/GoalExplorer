package st.cs.uni.saarland.de.helpClasses;

import soot.SootField;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.*;
import st.cs.uni.saarland.de.entities.FieldInfo;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class StmtSwitchForInterProcCalls extends MyStmtSwitch {
	
//	private List<InterProcInfo> info;
	private List<SootMethod> callStack;
	
	public StmtSwitchForInterProcCalls(List<SootMethod> callStackList,  SootMethod currentSootMethod){
		super(currentSootMethod);
		callStack = callStackList;
	}
	
	public StmtSwitchForInterProcCalls(List<SootMethod> callStackList, InterProcInfo pInfo, SootMethod currentSootMethod){
		super(currentSootMethod);
		callStack = callStackList;
//		caller = methodCallerName;
		if (pInfo != null)
			addToResultInfo(pInfo);
		else
			logger.error("StmtSwitchForInterProcCalls.constructor: pInfo is null");
	}
	
	public void caseReturnStmt(ReturnStmt stmt){
		if(Thread.currentThread().isInterrupted()){
			return;
		}
		String retReg = helpMethods.getReturnRegOfReturnStmt(stmt);
		InterProcInfo info = new InterProcInfo(retReg);
		
		if (stmt.getOp() instanceof IntConstant || stmt.getOp() instanceof StringConstant){
			//whenever you know the value of return we have to clean register immediately...
			info.setSearchedEReg("");
			info.setValueOfSearchedReg(retReg);
		}
		if (checkMethods.checkIfValueIsString(retReg)){
			//whenever you know the value of return we have to clean register immediately...
			info.setSearchedEReg("");
			info.setValueOfSearchedReg(retReg.replace("\"", ""));
		}
		addToResultInfo(info);
		
	}
	
	public void caseIdentityStmt(IdentityStmt stmt){
		if(Thread.currentThread().isInterrupted()){
			return;
		}
		if (getResultInfos().size() > 0){
			Set<Info> toAddInfos = new LinkedHashSet<>();
			for (Info resInfo: getResultInfos()){
				if(Thread.currentThread().isInterrupted()){
					return;
				}
				InterProcInfo info = (InterProcInfo) resInfo;
			
				String leftReg = helpMethods.getLeftRegOfIdentityStmt(stmt);
				if (leftReg.equals(info.getSearchedEReg())){
					info.setSearchedEReg("");
					if(stmt.getRightOp() instanceof ParameterRef){
						
						List<SootMethod> localCallStack = new ArrayList<SootMethod>();
						localCallStack.addAll(callStack);
						
						if ((callStack != null) && (getCurrentSootMethod() != null)){
							if(callStack.contains(getCurrentSootMethod())){
								return;
							}
							// FIXME ask if this stmt should be better after this if clause?
							callStack.add(getCurrentSootMethod());
						}else{
							callStack = new ArrayList<SootMethod>();
						}
						
						int paramIndex = ((ParameterRef)stmt.getRightOp()).getIndex();
						
						List<Info> res = interprocMethods2.findInReachableMethods2(paramIndex, getCurrentSootMethod(), callStack);
						if (res.size() > 0){
							info.setClassOfSearchedReg(((InterProcInfo)res.get(0)).getClassOfSearchedReg());
							info.setValueOfSearchedReg(((InterProcInfo)res.get(0)).getValueOfSearchedReg());
							shouldBreak = true;
							for (int i = 1; i < res.size(); i++){
								InterProcInfo newInfo = (InterProcInfo) info.clone();
								newInfo.setClassOfSearchedReg(((InterProcInfo)res.get(i)).getClassOfSearchedReg());
								newInfo.setValueOfSearchedReg(((InterProcInfo)res.get(i)).getValueOfSearchedReg());
								toAddInfos.add(newInfo);
							}
						}
						callStack.clear();
						callStack.addAll(localCallStack);
										
					}else if(stmt.getRightOp() instanceof ThisRef){
						info.setSearchedEReg("");
						String type = ((ThisRef)stmt.getRightOp()).getType().toString();
						info.setClassOfSearchedReg(type);
					}
				}
			}
			addAllToResultInfo(toAddInfos);
		}
	}
	
	public void caseAssignStmt(AssignStmt stmt) {
		if(Thread.currentThread().isInterrupted()){
			return;
		}
		if (getResultInfos().size() > 0){
			Set<Info> toAddInfos = new LinkedHashSet<>();
			Set<Info> toRemoveInfos = new LinkedHashSet<>();
			for (Info resInfo: getResultInfos()){
				if(Thread.currentThread().isInterrupted()){
					return;
				}
				InterProcInfo info = (InterProcInfo) resInfo;
				
				String leftReg = helpMethods.getLeftRegOfAssignStmt(stmt);
				if (leftReg.equals(info.getSearchedEReg())){
					
					if (stmt.containsInvokeExpr()){
						info.setSearchedEReg("");
						InvokeExpr invokeExpr = stmt.getInvokeExpr();
//						String methodSignature = helpMethods.getSignatureOfInvokeExpr(invokeExpr);
						String methodName = helpMethods.getMethodNameOfInvokeStmt(invokeExpr);
						
						if (("findViewById".equals(methodName) || Helper.getSignatureOfSootMethod(invokeExpr.getMethod()).startsWith("<android.content.res.Resources: java.lang.String get")) && invokeExpr.getArgCount() > 0){
							 info.setSearchedEReg("");
							 String param = helpMethods.getParameterOfInvokeStmt(invokeExpr,0);
							 // check if the param is really an id -> int
							if (invokeExpr.getArg(0) instanceof IntConstant){
								 // get the id of the layout
								 info.setValueOfSearchedReg(param);
							// if ID is an variable, and not an intConstant(251426)
								 //TODO why IntType is not functioning?
	//							 if (invokeExpr.getArg(0) instanceof IntType)
							 }else{
								 info.setSearchedEReg(param);
							 }
						 }else
							if (!Helper.isClassInSystemPackage(helpMethods.getCallerTypeOfInvokeStmt(invokeExpr))){
								if(!this.callStack.contains(invokeExpr.getMethod())){
									callStack.add(invokeExpr.getMethod());
									List<InterProcInfo> res = interprocMethods2.findReturnValueInMethod2(stmt, callStack);
									if (res.size() > 0){
										info.setClassOfSearchedReg(((InterProcInfo)res.get(0)).getClassOfSearchedReg());
										info.setValueOfSearchedReg(((InterProcInfo)res.get(0)).getValueOfSearchedReg());
										for (int i = 1; i < res.size(); i++){
											InterProcInfo newInfo = (InterProcInfo) info.clone();
											newInfo.setClassOfSearchedReg(((InterProcInfo)res.get(i)).getClassOfSearchedReg());
											newInfo.setValueOfSearchedReg(((InterProcInfo)res.get(i)).getValueOfSearchedReg());
											toAddInfos.add(newInfo);
										}
									}
								}
							}
						
					}else{
						String rightReg = helpMethods.getRightRegOfAssignStmt(stmt);
						if (stmt.getRightOp() instanceof NewExpr){
							info.setSearchedEReg("");
							info.setClassOfSearchedReg(helpMethods.getTypeOfRightRegOfAssignStmt(stmt));
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
							if (fInfos.size() > 0){
								toRemoveInfos.add(info);
							}else{
								info.setSearchedEReg("");
								Helper.saveToStatisticalFile("Error InterProcSwitch: Doesn't find searchedReg in initializationOfField: " + stmt);
							}
							for(FieldInfo fInfo : fInfos){
								if(fInfo.value != null){
									InterProcInfo newInfo = (InterProcInfo) info.clone();
									newInfo.setSearchedEReg("");
									toAddInfos.add(newInfo);
									newInfo.setValueOfSearchedReg(fInfo.value);
//									//Be careful with return!!!
//									resultInfos.removeAll(toRemoveInfos);
//									resultInfos.addAll(toAddInfos);
//									return;
									continue;
								}else{
									if(fInfo.methodToStart != null && fInfo.methodToStart.method().hasActiveBody()) {
										Unit workingUnit = fInfo.unitToStart;
										InterProcInfo newInfo = new InterProcInfo(fInfo.register.getName());
										StmtSwitchForInterProcCalls stmtSwitch = new StmtSwitchForInterProcCalls(callStack, newInfo, getCurrentSootMethod());
										previousFields.forEach(x -> stmtSwitch.addPreviousField(x));
										iteratorHelper.runOverToFindSpecValuesBackwards(fInfo.methodToStart.method().getActiveBody(), workingUnit, stmtSwitch);
										Set<Info> foundInitInfos = stmtSwitch.getResultInfos();
										if(foundInitInfos.size() > 0) {
											List<Info> listInfo = foundInitInfos.stream().collect(Collectors.toList());
											if(listInfo.indexOf(newInfo) == -1){
												continue;
											}
											Info tmp = listInfo.get(listInfo.indexOf(newInfo));
											tmp.setSearchedEReg("");
											toAddInfos.add(tmp);
										}

									}
								}
								
							}
						}else if (stmt.getRightOp() instanceof IntConstant){
							info.setSearchedEReg("");
							info.setValueOfSearchedReg(rightReg);
								// TODO check
	//					}else if (stmt.getRightOp() instanceof StaticFieldRef){
	//							SootField field = ((StaticFieldRef)stmt.getRightOp()).getField();
	//							String res = interprocMethods2.analyzeStaticField(field);
	//							if (res != null){
	//								info.setValueOfSearchedReg(res);
	//								info.setClassOfSearchedReg(field.getDeclaringClass().getName());
	//								shouldBreak = true;
	//							}
						}else if(stmt.getRightOp() instanceof CastExpr){
							info.setSearchedEReg("");
							String newReg = ((CastExpr) stmt.getRightOp()).getOp().toString();
							info.setSearchedEReg(newReg);
						}else{
							info.setSearchedEReg("");
							info.setValueOfSearchedReg(rightReg);
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
	

}
