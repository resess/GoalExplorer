package st.cs.uni.saarland.de.searchDynDecStrings;

import soot.Scene;
import soot.SootField;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.*;
import st.cs.uni.saarland.de.entities.FieldInfo;
import st.cs.uni.saarland.de.helpClasses.Helper;
import st.cs.uni.saarland.de.helpClasses.Info;
import st.cs.uni.saarland.de.helpClasses.InterProcInfo;
import st.cs.uni.saarland.de.helpClasses.MyStmtSwitch;
import st.cs.uni.saarland.de.helpMethods.InterprocAnalysis2;
import st.cs.uni.saarland.de.helpMethods.StmtSwitchForArrayAdapter;
import st.cs.uni.saarland.de.testApps.Content;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StmtSwitchForStrings extends MyStmtSwitch {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
//	private DynDecStringInfo searchedString;
	
	
	public StmtSwitchForStrings(SootMethod currentSootMethod) {
		super(currentSootMethod);
	}
	
	
	// case 2: arrays:
//	 listView = (ListView) findViewById(R.id.list);
//	 ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
//   android.R.layout.simple_list_item_1, android.R.id.text1, values);
//   listView.setAdapter(adapter);
	// searchedReg == searched element
	
	public StmtSwitchForStrings(DynDecStringInfo newInfo, SootMethod currentSootMethod) {
		super(newInfo, currentSootMethod);
	}
	
	public void caseIdentityStmt(IdentityStmt stmt){
		if(Thread.currentThread().isInterrupted()){
			return;
		}
		String leftReg = helpMethods.getLeftRegOfIdentityStmt(stmt);
		
		if (stmt.getRightOp() instanceof ParameterRef){
			Set<Info> toAddInfos = new LinkedHashSet<>();
			for (Info i: getResultInfos()){
				if(Thread.currentThread().isInterrupted()){
					return;
				}
				DynDecStringInfo searchedString = (DynDecStringInfo) i;
				
				if (leftReg.equals(searchedString.getSearchedEReg()) || leftReg.equals(searchedString.getUiEIDReg())){
					searchedString.setSearchedEReg("");
					searchedString.setUiEIDReg("");
					int paramIndex = ((ParameterRef)stmt.getRightOp()).getIndex();
					List<Info> resList = interprocMethods2.findInReachableMethods2(paramIndex, getCurrentSootMethod(), new ArrayList<SootMethod>());
					if (resList.size() > 0){
						searchedString.setUiEID(((InterProcInfo)resList.get(0)).getValueOfSearchedReg());
						if (resList.size() > 1){
							for (int j = 1; j < resList.size() ; j++){
								InterProcInfo workingInfo = (InterProcInfo) resList.get(j);
								DynDecStringInfo newInfo = (DynDecStringInfo) searchedString.clone();
								newInfo.setUiEID(workingInfo.getValueOfSearchedReg());
								newInfo.setInterProcIndex(paramIndex);
								toAddInfos.add(newInfo);
							}
						}
					}	
				}
				
				if (searchedString.getTextReg().contains(leftReg)){
					searchedString.removeTextReg(leftReg);
					int paramIndex = ((ParameterRef)stmt.getRightOp()).getIndex();
					List<Info> resList = interprocMethods2.findInReachableMethods2(paramIndex,  getCurrentSootMethod(), new ArrayList<SootMethod>());
					if (resList.size() > 0){
						searchedString.addText(((InterProcInfo)resList.get(0)).getValueOfSearchedReg());
						if (resList.size() > 1){
							for (int j = 1; j < resList.size() ; j++){
								InterProcInfo workingInfo = (InterProcInfo) resList.get(j);
								searchedString.addText(workingInfo.getValueOfSearchedReg());
							}
						}
					}	
				}				
			}
			addAllToResultInfo(toAddInfos);
		}
		
	}


	public void caseAssignStmt(AssignStmt stmt){
		if(Thread.currentThread().isInterrupted()){
			return;
		}
//		System.out.println(stmt);
//		List<Info> resultInfos = getResultInfos();
		Set<Info> toAddInfos = new LinkedHashSet<>();
		Set<Info> toRemoveInfos = new LinkedHashSet<>();

		if (stmt.containsInvokeExpr()) {
			InvokeExpr invokeExpr = stmt.getInvokeExpr();
			String declaringClass = invokeExpr.getMethod().getDeclaringClass().getName();
			String signature = helpMethods.getSignatureOfInvokeExpr(invokeExpr);

			if (signature.startsWith("<android.view.Menu: android.view.MenuItem add(")
				|| signature.startsWith("<android.view.ContextMenu: android.view.MenuItem add(")) {
				handleDynStringForDynMenuItems(invokeExpr);
			}
			/*else if(Helper.isClassInAppNameSpace(declaringClass) && invokeExpr.getMethod().hasActiveBody()) { //Could be a menu item declaration I guess
				boolean returnsMenuItem = signature.contains(": android.view.MenuItem ");
				if (returnsMenuItem){ //interprocedural

					Unit lastUnitOfMethod = invokeExpr.getMethod().getActiveBody().getUnits().getLast();
					String returnedValue = helpMethods.getReturnRegOfReturnStmt(lastUnitOfMethod);
					DynDecStringInfo newInfo = new DynDecStringInfo()
					StmtSwitchForStrings newStmtSwitch = new StmtSwitchForStrings(invokeExpr.getMethod());
					iteratorHelper.runUnitsOverMethodBackwards(invokeExpr.getMethod().getActiveBody(), newStmtSwitch);
					addAllToResultInfo(newStmtSwitch.getResultInfos());
				}
			}*/
		}
		
		for (Info i: getResultInfos()){
			if(Thread.currentThread().isInterrupted()){
				return;
			}
			DynDecStringInfo searchedString = (DynDecStringInfo) i;
		
			// call the arraySwitch on the assignStmt, if it is used
			if (searchedString.getArraySwitch() != null)
				searchedString = (DynDecStringInfo) searchedString.getArraySwitch().caseAssignStmt(stmt, searchedString);

			searchedString.setProcessedStmtInStringBuilder(false);
			searchedString = (DynDecStringInfo) searchedString.getStringBuilderSwitch().caseAssignStmt(stmt, searchedString);
			// if the stringBuilder processed this statement, nobody else has to do it
			if (searchedString.isProcessedStmtInStringBuilder())
				continue;

			String rightReg = helpMethods.getRightRegOfAssignStmt(stmt);
			String leftReg = stmt.getLeftOpBox().getValue().toString();
			
		// check if inside the assign stmt is an invoke, e.g.:
		 	// $r2 = virtualinvoke $r0.<com.example.Testapp.MainActivity: android.view.View findViewById(int)>(2131034120);
			if (stmt.containsInvokeExpr()){
				 InvokeExpr invokeExpr = stmt.getInvokeExpr();
				 String methodSignature = helpMethods.getSignatureOfInvokeExpr(invokeExpr);
				 String methodName = helpMethods.getMethodNameOfInvokeStmt(invokeExpr);
				 
				 if ("findViewById".equals(methodName)){
					
					 // get left side of assign stmt
					 if (leftReg.equals(searchedString.getSearchedEReg())){
						 if (!(invokeExpr.getArg(0) instanceof NullConstant)){
							 // get the id of the layout
							 String param = helpMethods.getParameterOfInvokeStmt(invokeExpr,0);
							 searchedString.setSearchedEReg("");
							 if (invokeExpr.getArg(0) instanceof IntConstant){
								 searchedString.setUiEID(param);
							 }else{
								 searchedString.setUiEIDReg(param);
							 }
						 }else{
							 searchedString.setUiEID("-No integer given: NullType");
						 }
					 }
				 }else
					 if (methodSignature.equals("<android.content.res.Resources: java.lang.CharSequence getTextFromElement(int)>") ||
					 	methodSignature.equals("<android.content.res.Resources: java.lang.CharSequence getText(int)>") ||
								methodSignature.equals("<android.content.res.Resources: java.lang.String getTextFromElement(int,CharSequence)>") ||
								methodSignature.equals("<android.content.res.Resources: java.lang.String getString(int)>") ||
							 	invokeExpr.getMethod().getSubSignature().startsWith("java.lang.String getString(int") ||
								methodSignature.equals("<android.content.res.Resources: java.lang.String[] getStringArray(int)>")){ //need to add getText()
									 if (searchedString.getTextReg().contains(leftReg)){
											searchedString.removeTextReg(leftReg);
											String text = helpMethods.getParameterOfInvokeStmt(invokeExpr, 0);
											if(checkMethods.checkIfValueIsVariable(text)){
												//searchedString.setSearchedEReg(text);
												searchedString.addTextReg(text);
											}
											else{
												if(checkMethods.checkIfValueIsID(text)){
													text = Content.getInstance().getStringValueFromStringId(text);
												}
												searchedString.addText(text);
											}
										}
									 else if (searchedString.getSearchedPlaceHolders() != null && searchedString.getSearchedPlaceHolders().contains(leftReg)){
										 searchedString.removeSearchedPlaceHolders(leftReg);
										 String tmp = helpMethods.getParameterOfInvokeStmt(invokeExpr, 0);
										 if (checkMethods.checkIfValueIsID(tmp)){
											 tmp = Content.getInstance().getStringValueFromStringId(tmp);
										 }else if (checkMethods.checkIfValueIsString(tmp)){
											tmp = tmp.replace("\"", "");
										 }
										 searchedString.replacePlaceHolder(leftReg, tmp);
									 }
						}
				 else{			
						 if (leftReg.equals(searchedString.getUiEIDReg())){
							 searchedString.setUiEIDReg(""); //textId

							 List<InterProcInfo> resList = interprocMethods2.findReturnValueInMethod2(stmt);
							 if (resList.size() > 0){
									searchedString.setUiEID(((InterProcInfo)resList.get(0)).getValueOfSearchedReg());
									if (resList.size() > 1){
										for (int j = 1; j < resList.size() ; j++){
											InterProcInfo workingInfo = (InterProcInfo) resList.get(j);
											DynDecStringInfo newInfo = (DynDecStringInfo) searchedString.clone();
											newInfo.setUiEID(workingInfo.getValueOfSearchedReg());
											toAddInfos.add(newInfo);
										}
									}
								}	
						 }

						 if(leftReg.equals(searchedString.getSearchedEReg())){
							 searchedString.setSearchedEReg(""); // View

							 //search for the View, not ID!!!
							 //search for the findViewById in the method
							 //we also need to find the value
							 List<Integer> ids = InterprocAnalysis2.getInstance().findElementIdFromForTheView(stmt, getCurrentSootMethod());
							 if(!ids.isEmpty()){
								 searchedString.setUiEID(ids.get(0).toString());
								 if (ids.size() > 1){
									 for (int j = 1; j < ids.size() ; j++){
										 DynDecStringInfo newInfo = (DynDecStringInfo) searchedString.clone();
										 newInfo.setUiEID(ids.get(j).toString());
										 newInfo.setSearchedEReg("");
										 toAddInfos.add(newInfo);
									 }
								 }
								 continue;
							 }

						 }
						
						 if (searchedString.getTextReg().contains(leftReg)) {
							 searchedString.removeTextReg(leftReg);
							 if (invokeExpr.getMethod().getSignature().startsWith("<android.text.Html: android.text.Spanned fromHtml(java.lang.String")) {
								 searchedString.addTextReg(invokeExpr.getArg(0).toString());
							 } else {
								 List<InterProcInfo> resList = interprocMethods2.findReturnValueInMethod2(stmt);
								 if (resList.size() > 0) {
									 searchedString.addText(((InterProcInfo) resList.get(0)).getValueOfSearchedReg());
									 if (resList.size() > 1) {
										 for (int j = 1; j < resList.size(); j++) {
											 InterProcInfo workingInfo = (InterProcInfo) resList.get(j);
											 searchedString.addText(workingInfo.getValueOfSearchedReg());
										 }
									 }
								 }
							 }
						 }
					 //}
				 }
			 }
			 // check if an existing register that is searched for, changes the register
			 else{
				 if (leftReg.equals(searchedString.getSearchedEReg())){
					 
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
								toRemoveInfos.add(searchedString);
								for (FieldInfo fInfo : fInfos){
									if(Thread.currentThread().isInterrupted()){
										return;
									}
									if(fInfo.value != null){
										DynDecStringInfo newInfo = (DynDecStringInfo) searchedString.clone();
										newInfo.setUiEID(fInfo.value);
										newInfo.setSearchedEReg("");
										toAddInfos.add(newInfo);
										continue;
									}else{
										if(fInfo.methodToStart != null && fInfo.methodToStart.method().hasActiveBody()) {
											Unit workingUnit = fInfo.unitToStart;
											DynDecStringInfo newInfo = new DynDecStringInfo("", getCurrentSootMethod());
											newInfo.setSearchedEReg(fInfo.register.getName());
											StmtSwitchForStrings newStmtSwitch = new StmtSwitchForStrings(newInfo, getCurrentSootMethod());
											previousFields.forEach(x -> newStmtSwitch.addPreviousField(x));
											iteratorHelper.runOverToFindSpecValuesBackwards(fInfo.methodToStart.method().getActiveBody(), workingUnit, newStmtSwitch);
											Set<Info> initValues = newStmtSwitch.getResultInfos();
											if(initValues.size() > 0) {
												List<Info> listInfo = initValues.stream().collect(Collectors.toList());
												if(listInfo.indexOf(newInfo) == -1){
													continue;
												}
												Info initInfo = listInfo.get(listInfo.indexOf(newInfo));
												DynDecStringInfo newInfo2 = (DynDecStringInfo) searchedString.clone();
												newInfo2.setUiEID(((DynDecStringInfo) initInfo).getUiEID());
												newInfo2.setSearchedEReg("");
												toAddInfos.add(newInfo2);
											}

										}
									}
								}
							}else{
								searchedString.setSearchedEReg("");
								Helper.saveToStatisticalFile("Error StringSwitch: Doesn't find searchedReg in initializationOfField: " + stmt);
							}
					 }else{
						 searchedString.setSearchedEReg("");
						 searchedString.setSearchedEReg(rightReg); 
					 }
				 }
				 
				 if (leftReg.equals(searchedString.getUiEIDReg())){
					 
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
							toRemoveInfos.add(searchedString);
							for (FieldInfo fInfo : fInfos){
								if(Thread.currentThread().isInterrupted()){
									return;
								}
								if(fInfo.value != null){
									DynDecStringInfo newInfo = (DynDecStringInfo) searchedString.clone();
									newInfo.setUiEID(fInfo.value);
									newInfo.setUiEIDReg("");
									toAddInfos.add(newInfo);
									continue;
								}else{
									if(fInfo.methodToStart != null && fInfo.methodToStart.method().hasActiveBody()){
										Unit workingUnit = fInfo.unitToStart;
										DynDecStringInfo newInfo = new DynDecStringInfo("", getCurrentSootMethod());
										newInfo.setUiEIDReg(fInfo.register.getName());
										StmtSwitchForStrings newStmtSwitch = new StmtSwitchForStrings(newInfo, getCurrentSootMethod());
										previousFields.forEach(x->newStmtSwitch.addPreviousField(x));
										iteratorHelper.runOverToFindSpecValuesBackwards(fInfo.methodToStart.method().getActiveBody(), workingUnit, newStmtSwitch);
										Set<Info> initValues = newStmtSwitch.getResultInfos();

										if(initValues.size() > 0) {
											List<Info> listInfo = initValues.stream().collect(Collectors.toList());
											if(listInfo.indexOf(newInfo) == -1){
												continue;
											}
											Info initInfo = listInfo.get(listInfo.indexOf(newInfo));
											DynDecStringInfo newInfo2 = (DynDecStringInfo) searchedString.clone();
											newInfo2.setUiEID(((DynDecStringInfo)initInfo).getUiEID());
											newInfo2.setUiEIDReg("");
											toAddInfos.add(newInfo2);
										}
									}
								}
							}
						}else{
							searchedString.setUiEIDReg("");
							Helper.saveToStatisticalFile("Error StringSwitch: Doesn't find uiEIDReg in initializationOfField: " + stmt);
						}
					 }else{
						 searchedString.setUiEIDReg("");
						 searchedString.setUiEIDReg(rightReg);
					 }
				 }
				 
				 if (searchedString.getTextReg().contains(leftReg)){
					 searchedString.removeTextReg(leftReg);
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
								for (FieldInfo fInfo : fInfos){
									if(Thread.currentThread().isInterrupted()){
										return;
									}
									if(fInfo.value != null){
										searchedString.addText(fInfo.value);
										continue;
									}else{
										if(fInfo.methodToStart != null && fInfo.methodToStart.method().hasActiveBody()){
											Unit workingUnit = fInfo.unitToStart;
											DynDecStringInfo newInfo = new DynDecStringInfo("", getCurrentSootMethod());
											newInfo.addTextReg(fInfo.register.getName());
											StmtSwitchForStrings newStmtSwitch = new StmtSwitchForStrings(newInfo, getCurrentSootMethod());
											previousFields.forEach(x->newStmtSwitch.addPreviousField(x));
											iteratorHelper.runOverToFindSpecValuesBackwards(fInfo.methodToStart.method().getActiveBody(), workingUnit, newStmtSwitch);
											Set<Info> initValues = newStmtSwitch.getResultInfos();

											if(initValues.size() > 0) {
												List<Info> listInfo = initValues.stream().collect(Collectors.toList());
												if(listInfo.indexOf(newInfo) == -1){
													continue;
												}
												Info initInfo = listInfo.get(listInfo.indexOf(newInfo));
//												DynDecStringInfo newInfo2 = (DynDecStringInfo) searchedString.clone();
												searchedString.addText(((DynDecStringInfo)initInfo).getText());
//												toAddInfos.add(newInfo2);
											}
										}
									}
								}
							}else{
								Helper.saveToStatisticalFile("Error StringSwitch: Doesn't find textReg in initializationOfField: " + stmt);
							}
					 }else{
						 if (checkMethods.checkIfValueIsVariable(rightReg)){
							 searchedString.addTextReg(rightReg);
						 }else{
							 searchedString.addText(rightReg);
						 }
					 }
				 }

				if (searchedString.getSearchedPlaceHolders() != null && searchedString.getSearchedPlaceHolders().contains(leftReg)){
					searchedString.removeSearchedPlaceHolders(leftReg);
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
							for (FieldInfo fInfo : fInfos){
								if(Thread.currentThread().isInterrupted()){
									return;
								}
								if(fInfo.value != null){
									searchedString.replacePlaceHolder(leftReg, fInfo.value);
									continue;
								}else{
									if(fInfo.methodToStart != null && fInfo.methodToStart.method().hasActiveBody()){
										Unit workingUnit = fInfo.unitToStart;
										DynDecStringInfo newInfo = new DynDecStringInfo("", getCurrentSootMethod());
										newInfo.addTextReg(fInfo.register.getName());
										StmtSwitchForStrings newStmtSwitch = new StmtSwitchForStrings(newInfo, getCurrentSootMethod());
										previousFields.forEach(x->newStmtSwitch.addPreviousField(x));
										iteratorHelper.runOverToFindSpecValuesBackwards(fInfo.methodToStart.method().getActiveBody(), workingUnit, newStmtSwitch);
										Set<Info> initValues = newStmtSwitch.getResultInfos();

										if(initValues.size() > 0) {
											List<Info> listInfo = initValues.stream().collect(Collectors.toList());
											if(listInfo.indexOf(newInfo) == -1){
												continue;
											}
											Info initInfo = listInfo.get(listInfo.indexOf(newInfo));
											searchedString.replacePlaceHolder(leftReg,((DynDecStringInfo)initInfo).getText());
										}
									}
								}
							}
						}else{
							Helper.saveToStatisticalFile("Error StringSwitch: Doesn't find textReg in initializationOfField: " + stmt);
						}
					}else{
						if (checkMethods.checkIfValueIsVariable(rightReg)){
							searchedString.addSearchedPlaceHolders(rightReg);
							searchedString.replacePlaceHolder(leftReg, rightReg);
						}else{
							searchedString.replacePlaceHolder(leftReg, rightReg.replace("\"", ""));
						}
					}
					if (searchedString.getSearchedPlaceHolders().size() == 0){
						searchedString.addText(searchedString.joinNotJoinedText());
					}
				}
						
			}
		}
		if (toRemoveInfos.size() > 0){
			removeAllFromResultInfos(toRemoveInfos);
		}
		addAllToResultInfo(toAddInfos);
	}
	
	public void caseInvokeStmt(InvokeStmt stmt) {
		if(Thread.currentThread().isInterrupted()){
			return;
		}
//		System.out.println(stmt);
		InvokeExpr invokeExpr = stmt.getInvokeExpr();
		String methodSignature = helpMethods.getSignatureOfInvokeExpr(invokeExpr);
		String method_name = helpMethods.getMethodNameOfInvokeStmt(invokeExpr);
		
		// call this method only if it is the first call and
			// and call this method once
//		List<Info> resultInfos = getResultInfos();
		for (Info i: getResultInfos()){
			if(Thread.currentThread().isInterrupted()){
				return;
			}
			DynDecStringInfo searchedString = (DynDecStringInfo) i;
			
			if (searchedString.getArraySwitch() != null)
				searchedString = (DynDecStringInfo) searchedString.getArraySwitch().caseInvokeStmt(stmt, searchedString);

			searchedString.setProcessedStmtInStringBuilder(false);
			searchedString = (DynDecStringInfo) searchedString.getStringBuilderSwitch().caseInvokeStmt(stmt, searchedString);
			// if the stringBuilder processed this statement, nobody else has to do it
			if (searchedString.isProcessedStmtInStringBuilder())
				continue;

		}
			// case set setText: e.g.:
				// virtualinvoke $r10.<android.widget.Button: void setText(int)>(2130968577);
				// or
				// virtualinvoke $r10.<android.widget.Button: void setText(java.lang.CharSequence)>("nextIntentInternet");
			if (method_name.equals("setText") && invokeExpr.getArgCount() > 0){	
				String typeOfParam = helpMethods.getParameterTypeOfInvokeStmt(invokeExpr,0);
				String param = helpMethods.getParameterOfInvokeStmt(invokeExpr,0);
				// if typeOfParam is int, then the text is set via the string id: e.g.:
					// virtualinvoke $r10.<android.widget.Button: void setText(int)>(2130968577);
				if (typeOfParam.equals("int")){
					
					if (invokeExpr.getArg(0) instanceof IntConstant){
						DynDecStringInfo searchedString = new DynDecStringInfo(helpMethods.getCallerOfInvokeStmt(invokeExpr), getCurrentSootMethod());
						String text =param;
						searchedString.addText(text);
						addToResultInfo(searchedString);
					}else{
						DynDecStringInfo searchedString = new DynDecStringInfo(helpMethods.getCallerOfInvokeStmt(invokeExpr), getCurrentSootMethod());
						searchedString.addTextReg(param);
						addToResultInfo(searchedString);
					}
				
				// otherwise the text is set directly as String e.g.:
				// virtualinvoke $r10.<android.widget.Button: void setText(java.lang.CharSequence)>("nextIntentInternet");
				}else if (typeOfParam.equals("java.lang.CharSequence") || typeOfParam.equals("java.lang.String") || Scene.v().getSootClass(typeOfParam).implementsInterface("java.lang.CharSequence")){
					DynDecStringInfo searchedString = new DynDecStringInfo(helpMethods.getCallerOfInvokeStmt(invokeExpr), getCurrentSootMethod());
					// TODO add other check if its variable
					// TODO include arrays
					if (checkMethods.checkIfValueIsVariable(param)){
						searchedString.addTextReg(param);
					}else{
						searchedString.addText(param.replace("\"", ""));
					}
					addToResultInfo(searchedString);
				}	
			}else if (method_name.equals("setAdapter") && (invokeExpr.getArgCount() > 0)){
//			     virtualinvoke $r5.<android.widget.ListView: void setAdapter(android.widget.ListAdapter)>($r2);
				String typeOfParam = helpMethods.getParameterTypeOfInvokeStmt(invokeExpr,0);
				
				if (typeOfParam.equals("android.widget.ArrayAdapter") || typeOfParam.equals("android.widget.ListAdapter")){
					DynDecStringInfo searchedString = new DynDecStringInfo(helpMethods.getCallerOfInvokeStmt(invokeExpr), getCurrentSootMethod());
					StmtSwitchForArrayAdapter arraySwitch = new StmtSwitchForArrayAdapter(helpMethods.getParameterOfInvokeStmt(invokeExpr, 0), getCurrentSootMethod());
					searchedString.setArraySwitch(arraySwitch);
					addToResultInfo(searchedString);
				}
			}
			else if(methodSignature.startsWith("<android.view.Menu: android.view.MenuItem add(")
					|| methodSignature.startsWith("<android.view.ContextMenu: android.view.MenuItem add(")){
				handleDynStringForDynMenuItems(invokeExpr);
			}
//		}
	}

	public void handleDynStringForDynMenuItems(InvokeExpr invokeExpr){
		String methodSignature = helpMethods.getSignatureOfInvokeExpr(invokeExpr);
		String method_name = helpMethods.getMethodNameOfInvokeStmt(invokeExpr);

		//virtualinvoke $r5.<android.view.Menu: android.view.MenuItem add(int, int, int, String)>(0, 0, 0, $r5)
		int numArgs = invokeExpr.getArgCount();
		String typeOfParam = helpMethods.getParameterTypeOfInvokeStmt(invokeExpr, numArgs -1);
		String param = helpMethods.getParameterOfInvokeStmt(invokeExpr, numArgs - 1);
		//if typeOfParam is int, then the text is set via the string id: e.g add(int, int, int, int)<0,0,0,213655675>
		//Need to get param-pos2 if available
		
			/*if(menuItemId instanceof IntConstant){
				searchedString.setUiEIDReg(uiEParam);
				searchedString.setUiEID(uiEParam);
			}
			else{
				searchedString.setUiEIDReg(uiEParam);
				searchedString.setUiEID("");
			}*/
		DynDecStringInfo searchedString = null;
		if (typeOfParam.equals("int")){
			if (invokeExpr.getArg(numArgs - 1) instanceof IntConstant){
				searchedString = new DynDecStringInfo(helpMethods.getCallerOfInvokeStmt(invokeExpr), getCurrentSootMethod());
				String text= param;
				if(checkMethods.checkIfValueIsID(text)){
					text = Content.getInstance().getStringValueFromStringId(text);
				}
				searchedString.addText(text);
				//addToResultInfo(searchedString);
			}
			else{
				 searchedString = new DynDecStringInfo(helpMethods.getCallerOfInvokeStmt(invokeExpr), getCurrentSootMethod());
					searchedString.addTextReg(param);
				//addToResultInfo(searchedString);
			}
		}
		else if (typeOfParam.equals("java.lang.CharSequence") || typeOfParam.equals("java.lang.String")){
			searchedString = new DynDecStringInfo(helpMethods.getCallerOfInvokeStmt(invokeExpr), getCurrentSootMethod());

			if (checkMethods.checkIfValueIsVariable(param)){
				searchedString.addTextReg(param); //should we group them for the menu ?
			}else{ //is string
				searchedString.addText(param.replace("\"", ""));
			}
			//addToResultInfo(searchedString);
		}
		if(searchedString != null){
			if (numArgs > 1){//extract menu item element registration
				//Value menuItemId = invokeExpr.getArg(1);
				String uiEParam = helpMethods.getParameterOfInvokeStmt(invokeExpr, 1);
				searchedString.setUiEIDReg(uiEParam);
				searchedString.setUiEID("");
				searchedString.setUiEIDRegMutable(false);
				logger.debug("Adding the string for dyn menu with uiEidReg {} .{}", uiEParam, searchedString);
			}
			addToResultInfo(searchedString);
		}

	}

	
	
	
//	private void checkIfStringIsFinished(){
//		if (searchedString.ready())
//			shouldBreak = true;
//	}

}
