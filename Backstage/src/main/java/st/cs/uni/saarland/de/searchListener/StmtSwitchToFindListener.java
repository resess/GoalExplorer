package st.cs.uni.saarland.de.searchListener;

import soot.*;
import soot.jimple.*;
import st.cs.uni.saarland.de.entities.FieldInfo;
import st.cs.uni.saarland.de.helpClasses.*;
import st.cs.uni.saarland.de.helpMethods.InterprocAnalysis2;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class StmtSwitchToFindListener extends MyStmtSwitch {
	
//	private ListenerInfo listenerInfo;
	private static Map<String, Set<String>> possibleListenerMap; 
	
	public StmtSwitchToFindListener(SootMethod currentSootMethod) {
		super(currentSootMethod);
		if (possibleListenerMap == null)
			possibleListenerMap = loadSetListenerMethodsWithOnMethods();
	}
	
	public StmtSwitchToFindListener(ListenerInfo newInfo, SootMethod currentSootMethod) {
		super(newInfo, currentSootMethod);
		if (possibleListenerMap == null)
			possibleListenerMap = loadSetListenerMethodsWithOnMethods();
	}

	public void caseIdentityStmt(final IdentityStmt stmt){
		if(Thread.currentThread().isInterrupted()){
			return;
		}
		// invokExpr should be first stmt, if not run should break
//		List<Info> resultInfos = getResultInfos();
		Set<Info> toAddInfos = new LinkedHashSet<>();
		for (Info i : getResultInfos()){
			if(Thread.currentThread().isInterrupted()){
				return;
			}
			ListenerInfo listenerInfo = (ListenerInfo) i;
			
			String leftReg = helpMethods.getLeftRegOfIdentityStmt(stmt);			
			
			//When register is a parameter of method...			
			//virtualinvoke $r1.<android.widget.Button: void setOnClickListener(android.view.View$OnClickListener)>($r2);
			//looking for $r1 as a parameter in a method
			
			if ((listenerInfo.getSearchedEReg().equals(leftReg)|| listenerInfo.getSearchedEIDReg().equals(leftReg)) && (stmt.getRightOp() instanceof ParameterRef)){
				listenerInfo.setSearchedEReg("");
				int paramIndex = ((ParameterRef)stmt.getRightOp()).getIndex();
				List<Info> resList = interprocMethods2.findInReachableMethods2(paramIndex,  getCurrentSootMethod(), new ArrayList<SootMethod>());
				if (resList.size() > 0){
					listenerInfo.setSearchedEID(((InterProcInfo)resList.get(0)).getValueOfSearchedReg());
					if (resList.size() > 1){
						for (int j = 1; j < resList.size() ; j++){
							InterProcInfo workingInfo = (InterProcInfo) resList.get(j);
							ListenerInfo newInfo = (ListenerInfo) listenerInfo.clone();
							toAddInfos.add(newInfo);
							newInfo.setSearchedEID(workingInfo.getValueOfSearchedReg());
						}
					}
				}	
			}else if (listenerInfo.getListenerReg().equals(leftReg) && stmt.getRightOp() instanceof ParameterRef){
				listenerInfo.setListenerReg("");
				int paramIndex = ((ParameterRef)stmt.getRightOp()).getIndex();
				List<Info> resList = interprocMethods2.findInReachableMethods2(paramIndex,  getCurrentSootMethod(), new ArrayList<SootMethod>());
				if (resList.size() > 0){
					listenerInfo.addListenerClass(((InterProcInfo)resList.get(0)).getClassOfSearchedReg());
					if (resList.size() > 1){
						for (int j = 1; j < resList.size() ; j++){
							InterProcInfo workingInfo = (InterProcInfo) resList.get(j);
							ListenerInfo newInfo = (ListenerInfo) listenerInfo.clone();
							toAddInfos.add(newInfo);
							newInfo.addListenerClass(workingInfo.getClassOfSearchedReg());
						}
					}
				}
			}
			else if (listenerInfo.getListenerReg().equals(leftReg) && stmt.getRightOp() instanceof ThisRef){
				ThisRef ref = ((ThisRef)stmt.getRightOp());
				listenerInfo.addListenerClass(ref.getType().toString());
				listenerInfo.setListenerReg("");	
				
			}else if (listenerInfo.getSearchedEReg().equals(leftReg) && stmt.getRightOp() instanceof ThisRef){
//				ThisRef ref = ((ThisRef)stmt.getRightOp());
				listenerInfo.setSearchedEID("-strange Dyn Dec Element-");
				listenerInfo.setSearchedEReg("");		
			}
//			shouldBreakRun();		
		}
		addAllToResultInfo(toAddInfos);
	}
	
	
	public void caseInvokeStmt(final InvokeStmt stmt) {
		if(Thread.currentThread().isInterrupted()){
			return;
		}

		InvokeExpr invokeExpr = stmt.getInvokeExpr();
		String methodName = helpMethods.getMethodNameOfInvokeStmt(invokeExpr);

		// only do if a set..Listener method was found
		if (possibleListenerMap.containsKey(methodName)){
			Set<String> callBackMethod = possibleListenerMap.get(methodName);
						
			// in BodyTransformer the listenerClass in a set..listener stmt is always View$OnClickListener
			String uiElementReg = helpMethods.getCallerOfInvokeStmt(invokeExpr);

			// get the listener method without set..Listener
			String actionPerformed = methodName.replace("Listener", "").replace("set", "").replace("add", "");
			
			// in Bodytransformer listener class is always, eg View$OnClickListener
			// sort out the null/nulltype listener classes , and the android libary functions
			
			if (invokeExpr.getArgCount() > 0 && !(invokeExpr.getArg(0) instanceof NullConstant)){
				ListenerInfo listenerInfo = new ListenerInfo(callBackMethod, uiElementReg, "",  actionPerformed, getCurrentSootMethod().getDeclaringClass().getName());
				addToResultInfo(listenerInfo);
//				String listClass = invokeExpr.getArg(0).getType().toString();
//					if (){
					listenerInfo.setListenerReg(helpMethods.getParameterOfInvokeStmt(invokeExpr, 0));
//					}else{
//						listenerInfo.setListenerClass(listClass);
//					}
				if (methodName.equals("setAdapter")){
					String classOfAdapter = helpMethods.getParameterTypeOfInvokeStmt(invokeExpr, 0);
					if (!(classOfAdapter.equals("android.widget.ArrayAdapter") || classOfAdapter.equals("android.widget.ListAdapter"))){
						listenerInfo.setIsAdapter();						
					}else{
						// don't process ArrayAdapter or ListAdapter here (-> DynDecStrings)
						listenerInfo = null;
						shouldBreak = true;
					}
				}
			}
		}
	}
	

	public void caseAssignStmt(final AssignStmt stmt){
		processAssignStmt(stmt);
	}
	
	private void processAssignStmt(final AssignStmt stmt){
		if(Thread.currentThread().isInterrupted()){
			return;
		}
		// if quadripel is null, invokeExpr was not the first, but must be the first, so break
		//Set<Info> resultInfos = getResultInfos();
		Set<Info> toAddInfos = new LinkedHashSet<>();
		Set<Info> toRemoveInfos = new LinkedHashSet<>();
		for (Info i : getResultInfos()){
			if(Thread.currentThread().isInterrupted()){
				return;
			}
			ListenerInfo listenerInfo = (ListenerInfo) i;
					// case: $r1 = virtualinvoke $r0.<com.example.SendSms.SendSmsFromAdress: android.view.View findViewById(int)>(2131034115)
					// or case: $r3 = interfaceinvoke $r1.<android.view.Menu: android.view.MenuItem findItem(int)>(2131099672);
					if (stmt.containsInvokeExpr()){
						InvokeExpr invokeExpr = stmt.getInvokeExpr();
						String methodName = helpMethods.getMethodNameOfInvokeStmt(invokeExpr);
						String leftReg = helpMethods.getLeftRegOfAssignStmt(stmt);

						if (("findViewById".equals(methodName)) && (listenerInfo.getSearchedEReg().equals(leftReg))){
							listenerInfo.setSearchedEReg("");
							String id = helpMethods.getParameterOfInvokeStmt(invokeExpr, 0);

							if (invokeExpr.getArg(0) instanceof IntConstant){
								listenerInfo.setSearchedEID(id);
							}else{
								listenerInfo.setSearchedEIDReg(id);
							}
						}else if (("findItem".equals(methodName)) && (listenerInfo.getSearchedEReg().equals(leftReg))){
							listenerInfo.setSearchedEReg("");
							String id = helpMethods.getParameterOfInvokeStmt(invokeExpr, 0);

							if (invokeExpr.getArg(0) instanceof IntConstant){
								listenerInfo.setSearchedEID(id);
							}else{
								listenerInfo.setSearchedEIDReg(id);
							}
						}else{
								if (listenerInfo.getSearchedEReg().equals(leftReg)){
									listenerInfo.setSearchedEReg("");

									//search for the View, not ID!!!
									//search for the findViewById in the method
									//we also need to find the value
									List<Integer> ids = InterprocAnalysis2.getInstance().findElementIdFromForTheView(stmt, getCurrentSootMethod());
									if(!ids.isEmpty()){
										listenerInfo.setSearchedEID(ids.get(0).toString());
										if (ids.size() > 1){
											for (int j = 1; j < ids.size(); j++) {
												ListenerInfo newInfo = (ListenerInfo) listenerInfo.clone();
												toAddInfos.add(newInfo);
												newInfo.setSearchedEID(ids.get(j).toString());
											}
										}
										continue;
									}


								}else if (listenerInfo.getListenerReg().equals(leftReg)){
									listenerInfo.setListenerReg("");
									if (!Helper.isClassInSystemPackage(helpMethods.getCallerTypeOfInvokeStmt(invokeExpr))) {
										List<InterProcInfo> resList = interprocMethods2.findReturnValueInMethod2(stmt);
										if (resList.size() > 0) {
											listenerInfo.addListenerClass(((InterProcInfo) resList.get(0)).getClassOfSearchedReg());
											if (resList.size() > 1) {
												for (int j = 1; j < resList.size(); j++) {
													InterProcInfo workingInfo = (InterProcInfo) resList.get(j);
													ListenerInfo newInfo = (ListenerInfo) listenerInfo.clone();
													toAddInfos.add(newInfo);
													newInfo.addListenerClass(workingInfo.getClassOfSearchedReg());
												}
											}
										}
									}
								}else if (listenerInfo.getSearchedEIDReg().equals(leftReg)){
									listenerInfo.setSearchedEIDReg("");
									if (!Helper.isClassInSystemPackage(helpMethods.getCallerTypeOfInvokeStmt(invokeExpr))) {
										List<InterProcInfo> resList = interprocMethods2.findReturnValueInMethod2(stmt);
										if (resList.size() > 0) {
											listenerInfo.setSearchedEID(((InterProcInfo) resList.get(0)).getValueOfSearchedReg());
											if (resList.size() > 1) {
												for (int j = 1; j < resList.size(); j++) {
													InterProcInfo workingInfo = (InterProcInfo) resList.get(j);
													ListenerInfo newInfo = (ListenerInfo) listenerInfo.clone();
													toAddInfos.add(newInfo);
													newInfo.setSearchedEID(workingInfo.getValueOfSearchedReg());
												}
											}
										}
									}
								}
						}
					// case 2: $r2 = (android.widget.Button) $r1
					// case 3: $r2 = (android.view.View$OnClickListener) $r1
					}else {
						String leftReg = helpMethods.getLeftRegOfAssignStmt(stmt);
						String rightReg = helpMethods.getRightRegOfAssignStmt(stmt);

						// case 2: $r2 = (android.widget.Button) $r1
						// case 3: $r2 = $r0.<... android.view.Button myButton>
						if (listenerInfo.getSearchedEReg().equals(leftReg)){
							listenerInfo.setSearchedEReg("");
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
									toRemoveInfos.add(listenerInfo);
									for (FieldInfo fInfo : fInfos){
										if(Thread.currentThread().isInterrupted()){
											return;
										}
										if(fInfo.value != null){
											ListenerInfo newInfo = (ListenerInfo) listenerInfo.clone();
											newInfo.setSearchedEReg("");
											newInfo.setSearchedEID(fInfo.value);
											toAddInfos.add(newInfo);
											continue;
										}else{
											if(fInfo.methodToStart != null && fInfo.methodToStart.method().hasActiveBody()){
												Unit workingUnit = fInfo.unitToStart;
												ListenerInfo newInfo = new ListenerInfo(null, "", "", "", getCurrentSootMethod().getDeclaringClass().getName());
												newInfo.setSearchedEReg(fInfo.register.getName());
												newInfo.addListenerClass(listenerInfo.getListenerClasses());
												listenerInfo.getListenerMethods().forEach(newInfo::addListenerMethod);
												StmtSwitchToFindListener newStmtSwitch = new StmtSwitchToFindListener(newInfo, getCurrentSootMethod());
												previousFields.forEach(x->newStmtSwitch.addPreviousField(x));
												iteratorHelper.runOverToFindSpecValuesBackwards(fInfo.methodToStart.method().getActiveBody(), workingUnit, newStmtSwitch);
												Set<Info> initValues = newStmtSwitch.getResultInfos();

												if(initValues.size() > 0) {
													List<Info> listInfo = initValues.stream().collect(Collectors.toList());
													if(listInfo.indexOf(newInfo) == -1){
														continue;
													}
													Info initInfo = listInfo.get(listInfo.indexOf(newInfo));
													ListenerInfo newInfo2 = (ListenerInfo) listenerInfo.clone();
													newInfo2.setSearchedEReg("");
													newInfo2.setSearchedEID(((ListenerInfo)initInfo).getSearchedEID());
													toAddInfos.add(newInfo2);
												}
											}
										}
									}
								}else{
									listenerInfo.setSearchedEReg("");
									Helper.saveToStatisticalFile("Error ListenerSwitch: Doesn't find SearchedReg in initializationOfField: " + stmt);
								}
							}else{
								listenerInfo.setSearchedEReg(rightReg);
							}
						}else
							if (listenerInfo.getSearchedEIDReg().equals(leftReg)){
								listenerInfo.setSearchedEIDReg("");
								if (stmt.getRightOp() instanceof IntConstant){
									listenerInfo.setSearchedEIDReg("");
									listenerInfo.setSearchedEID(helpMethods.getRightRegOfAssignStmt(stmt));
								}
								else if(stmt.getRightOp() instanceof FieldRef){
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
										toRemoveInfos.add(listenerInfo);
										for (FieldInfo fInfo : fInfos){
											if(Thread.currentThread().isInterrupted()){
												return;
											}
											if(fInfo.value != null){
												ListenerInfo newInfo = (ListenerInfo) listenerInfo.clone();
												newInfo.setSearchedEIDReg("");
												newInfo.setSearchedEID(fInfo.value);
												toAddInfos.add(newInfo);
												continue;
											}
											if(fInfo.methodToStart.method().hasActiveBody()){
												Unit workingUnit = fInfo.unitToStart;
												ListenerInfo newInfo = new ListenerInfo(null, "", "", "", getCurrentSootMethod().getDeclaringClass().getName());
												newInfo.setSearchedEIDReg(fInfo.register.getName());
												StmtSwitchToFindListener newStmtSwitch = new StmtSwitchToFindListener(newInfo, getCurrentSootMethod());
												previousFields.forEach(x->newStmtSwitch.addPreviousField(x));
												iteratorHelper.runOverToFindSpecValuesBackwards(fInfo.methodToStart.method().getActiveBody(), workingUnit, newStmtSwitch);
												Set<Info> initValues = newStmtSwitch.getResultInfos();

												if(initValues.size() > 0) {
													List<Info> listInfo = initValues.stream().collect(Collectors.toList());
													if(listInfo.indexOf(newInfo) == -1){
														continue;
													}
													Info initInfo = listInfo.get(listInfo.indexOf(newInfo));
													ListenerInfo newInfo2 = (ListenerInfo) listenerInfo.clone();
													newInfo2.setSearchedEIDReg("");
													newInfo2.setSearchedEID(((ListenerInfo)initInfo).getSearchedEID());
													toAddInfos.add(newInfo2);
												}
											}
										}
									}else{
										listenerInfo.setSearchedEIDReg("");
										Helper.saveToStatisticalFile("Error ListenerSwitch: Doesn't find SearchedEIDReg in initializationOfField: " + stmt);
									}
								}
								else if(stmt.getRightOp() instanceof ArrayRef){
									//$i0 = $r2[$i2];

									//maybe there is a loop:
                                    //$r2 = newarray (int)[1];
                                    //$r2[0] = 2131492950;
                                    //for(int id : ids){getViewById(id).setOnClickListenener()...}

                                    //Heuristic:
                                    ArrayRef arrayRef = (ArrayRef)stmt.getRightOp();
                                    Value index = arrayRef.getIndex();
                                    Value array = arrayRef.getBase();
                                    if(index instanceof IntConstant){ //we are lucky
                                        //take a needed value from an array
                                        //iterate backward and find array[index] = something
                                        Unit workingUnit = stmt;
                                        InterProcInfo newInfo = new InterProcInfo(arrayRef.toString());
                                        StmtSwitchForInterProcCalls newStmtSwitch = new StmtSwitchForInterProcCalls(new ArrayList<>(), newInfo, getCurrentSootMethod());
                                        previousFields.forEach(x->newStmtSwitch.addPreviousField(x));
                                        iteratorHelper.runOverToFindSpecValuesBackwards(getCurrentSootMethod().getActiveBody(), workingUnit, newStmtSwitch);
                                        Set<Info> initValues = newStmtSwitch.getResultInfos();

										for(Info initInfo : initValues) {
                                            ListenerInfo newInfo2 = (ListenerInfo) listenerInfo.clone();
                                            newInfo2.setSearchedEIDReg("");
                                            newInfo2.setSearchedEID(((InterProcInfo)initInfo).getValueOfSearchedReg());
                                            toAddInfos.add(newInfo2);
                                        }
                                    }
                                    else{
                                        //some register is on the right side
                                        //the hell # of cases can be there
                                        //let's concentrate on the case when the index is an index within some loop
                                        //1. find init value of the index

										Unit workingUnit = stmt;
                                        InterProcInfo newInfo = new InterProcInfo(index.toString());
                                        StmtSwitchForInterProcCalls newStmtSwitch = new StmtSwitchForInterProcCalls(new ArrayList<>(), newInfo, getCurrentSootMethod());
                                        previousFields.forEach(x->newStmtSwitch.addPreviousField(x));
                                        iteratorHelper.runOverToFindSpecValuesBackwards(getCurrentSootMethod().getActiveBody(), workingUnit, newStmtSwitch);
                                        Set<Info> initValues = newStmtSwitch.getResultInfos();

                                        int indexInitialValue = -1;
										for(Info initInfo : initValues) {
                                            if(!((InterProcInfo)initInfo).getValueOfSearchedReg().isEmpty() && Helper.isIntegerParseInt(((InterProcInfo)initInfo).getValueOfSearchedReg())){
                                                indexInitialValue = Integer.parseInt(((InterProcInfo)initInfo).getValueOfSearchedReg());
                                                break;
                                            }
                                        }

                                        //2. find whether some increment/decrement happens
                                        boolean isIncrementDecrementHappens = false;
                                        boolean isBoundaryCheckHappens = false;
                                        final PatchingChain<Unit> units = getCurrentSootMethod().getActiveBody().getUnits();
                                        Unit tmpUnit = Helper.getSuccessorOf(units, stmt);
                                        while(tmpUnit != stmt){

                                            Stmt currentStmt = (Stmt)tmpUnit;
                                            if(currentStmt instanceof AssignStmt){
                                                AssignStmt currentAssignStmt = (AssignStmt)currentStmt;
                                                if(currentAssignStmt.getLeftOp().equals(index)){
                                                    if(currentAssignStmt.getRightOp().toString().contains(index.toString())){
                                                        isIncrementDecrementHappens = true;
														break;
                                                    }
                                                }
                                            }
                                            tmpUnit = Helper.getSuccessorOf(units, tmpUnit);
                                            if(tmpUnit == null){
                                                isIncrementDecrementHappens = false;
                                                break;
                                            }
                                        }


										//3. find the place when someone checks that index is in array's bound

                                        //MHGDominatorsFinder<Unit> dominatorsFinder = new MHGDominatorsFinder<Unit>(new ExceptionalUnitGraph(getCurrentSootMethod().getActiveBody()));
										/*if(isIncrementDecrementHappens){

                                            //find lengthof array
                                            Value boundaryReg = null;
                                            Unit u = dominatorsFinder.getImmediateDominator(stmt);
                                            while(u != null){
                                                if((Stmt)u instanceof AssignStmt){
                                                    if(((AssignStmt)u).getRightOp().toString().equals(String.format("lengthof %s", array))){
                                                        boundaryReg = ((AssignStmt)u).getLeftOp();
                                                        break;
                                                    }
                                                }
                                                u = dominatorsFinder.getImmediateDominator(u);
                                            }
                                            if(boundaryReg != null) {

                                                //start from the unit with increment and go backward using Dominators
                                                Unit wUnit = dominatorsFinder.getImmediateDominator(tmpUnit);
                                                while (wUnit != null && !isBoundaryCheckHappens) {
                                                    if(wUnit instanceof IfStmt) {
                                                        IfStmt ifStmt = (IfStmt) wUnit;
                                                        Value condition = ifStmt.getCondition();
                                                        if (condition instanceof ConditionExpr) {
                                                            ConditionExpr geExpr = (ConditionExpr) condition;
                                                            ;
                                                            if (geExpr.getOp1().equals(index) && geExpr.getOp2().equals(boundaryReg)) {
                                                                isBoundaryCheckHappens = true;
                                                            } else if (geExpr.getOp2().equals(index) && geExpr.getOp1().equals(boundaryReg)) {
                                                                isBoundaryCheckHappens = true;
                                                            }
                                                        }
                                                    }
                                                    wUnit = dominatorsFinder.getImmediateDominator(wUnit);
                                                }
                                            }
                                        }*/

                                        if(isIncrementDecrementHappens /*&& isBoundaryCheckHappens*/ && indexInitialValue != -1){
                                            //find the size of array
                                            Unit workingUnitForArray = stmt;
                                            InterProcInfo newInfoForArray = new InterProcInfo(array.toString());
                                            StmtSwitchForInterProcCalls newStmtSwitchForArray = new StmtSwitchForInterProcCalls(new ArrayList<>(), newInfoForArray, getCurrentSootMethod());
                                            previousFields.forEach(x->newStmtSwitchForArray.addPreviousField(x));
                                            iteratorHelper.runOverToFindSpecValuesBackwards(getCurrentSootMethod().getActiveBody(), workingUnitForArray, newStmtSwitchForArray);
                                            Set<Info> resultsForArray = newStmtSwitchForArray.getResultInfos();
                                            int arraySize = -1;
											for(Info initInfo : resultsForArray) {
                                                String arrayInit = null;
                                                if(((InterProcInfo)initInfo).getValueOfSearchedReg().startsWith("newarray (int)[")){
                                                    arrayInit = ((InterProcInfo)initInfo).getValueOfSearchedReg();
                                                }
                                                if(arrayInit != null)
                                                    arraySize = Integer.parseInt(arrayInit.split("\\[")[1].split("\\]")[0]);
                                                break;
                                            }

                                            //find all assignments like: $r2[0] = 2131492950;
                                            for(int number = indexInitialValue; number < arraySize; number++){
                                                StmtSwitchForArrays stmtSwitchFoArrays = new StmtSwitchForArrays(number, array, getCurrentSootMethod());
                                                iteratorHelper.runOverToFindSpecValuesBackwards(getCurrentSootMethod().getActiveBody(), stmt, stmtSwitchFoArrays);
                                                List<Integer> resultsForArrayPattern = stmtSwitchFoArrays.getArrayResults();

												for(Integer arrayRes : resultsForArrayPattern) {
                                                    ListenerInfo newInfo2 = (ListenerInfo) listenerInfo.clone();
                                                    newInfo2.setSearchedEIDReg("");
                                                    newInfo2.setSearchedEID(Integer.toString(arrayRes));
                                                    toAddInfos.add(newInfo2);
                                                }
                                            }
                                        }

                                    }
								}
								else{
									listenerInfo.setSearchedEIDReg(helpMethods.getRightRegOfAssignStmt(stmt));
								}
						}else
							// case 3: $r2 = (android.view.View$OnClickListener) $r1
							if (listenerInfo.getListenerReg().equals(leftReg)){

								if(stmt.getRightOp() instanceof CastExpr){
									listenerInfo.setListenerReg(rightReg);

								}else if (stmt.getRightOp() instanceof NewExpr){
									listenerInfo.addListenerClass(helpMethods.getTypeOfRightRegOfAssignStmt(stmt));
									listenerInfo.setListenerReg("");
								}
								else if(stmt.getRightOp() instanceof FieldRef){
									SootField f = ((FieldRef)stmt.getRightOp()).getField();
									if(previousFields.contains(f)){
										if(!previousFieldsForCurrentStmtSwitch.contains(f)){
											continue;
										}
									}else{
										previousFields.add(f);
										previousFieldsForCurrentStmtSwitch.add(f);
									}
									Set<FieldInfo> res = interprocMethods2.findInitializationsOfTheField2(f, stmt, getCurrentSootMethod());
									if(res.size() > 0){
										toRemoveInfos.add(listenerInfo);
										for (FieldInfo resInfo: res){
											ListenerInfo newInfo = (ListenerInfo) listenerInfo.clone();
											newInfo.addListenerClass(resInfo.className);
											newInfo.setListenerReg("");
											toAddInfos.add(newInfo);
										}
									}else{
										listenerInfo.setListenerReg("");
										Helper.saveToStatisticalFile("Error ListenerSwitch: Doesn't find ListenerReg in initializationOfField: " + stmt);
									}
								}
						}
					}
				}


//		boolean resbool = resultInfos.removeAll(toRemoveInfos);
//		Set<ListenerInfo> bla = new HashSet<ListenerInfo>();
//		for (Info i : resultInfos){
//			bla.add((ListenerInfo) i);
//		}
//		
//		bla.removeAll(toRemoveInfos);

//		boolean resbool1 = resultInfos.remove(i);
//		
		if (toRemoveInfos.size() > 0){
			removeAllFromResultInfos(toRemoveInfos);
		}
		addAllToResultInfo(toAddInfos);
	}
	
	
	//TODO get a correct "listenerList.txt" with the on.. methods and their set..Listener methods
	// return Map<setListenerMethod, callBackMethod(on..)>
	private Map<String, Set<String>> loadSetListenerMethodsWithOnMethods(){
		Map<String, Set<String>> listenerMap = new HashMap<String, Set<String>>();
		
		String curpath = System.getProperty("user.dir");

		File fparent = new File (curpath);
		File listenerListFile = new File(fparent.getAbsolutePath() + File.separator + "backstage" + File.separator + "res" + File.separator + "listenerListWithOnMethods.txt");
		

		FileReader fReader = null;
		BufferedReader bufReader = null;
		try {
			fReader = new FileReader(listenerListFile);
			bufReader = new BufferedReader(fReader);
		
			String line = bufReader.readLine();
        
	        while(line != null){
				if (line.startsWith("?")){
					line = bufReader.readLine();
					continue;
				}
				String splitString[] = line.split("-");
				String setListenerMethod = splitString[0].trim();
				
				Set<String> onMethodsSet = new HashSet<>();
				if (splitString.length > 1){
					String callbackMethods = splitString[1];
					String[] onMethods = callbackMethods.split(";");
					for (String s: onMethods){
						onMethodsSet.add(s.trim());
					}
				}
	        	listenerMap.put(setListenerMethod, onMethodsSet);
	        	line = bufReader.readLine();
	        }
	        
		}catch (IOException e) {
			e.printStackTrace();
			Helper.saveToStatisticalFile(Helper.exceptionStacktraceToString(e));
		}finally{
			try {
				if(fReader != null){
					fReader.close();
				}
				if(bufReader != null){
					bufReader.close();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Helper.saveToStatisticalFile(Helper.exceptionStacktraceToString(e));
			}
		}
		
		return listenerMap;
	}
	
	
//	@Override
//	public boolean run(){
//		if (listenerInfo != null)
//			return listenerInfo.allValuesFound();
//		else
//			return false;
//	}
//
//	@Override
//	public void init() {
//		super.init();
//		listenerInfo = null;
//		resultInfo = null;
//		shouldBreak = false;
//		
//	}	
	

	@Override
	public void defaultCase(Object o){
	}
}
