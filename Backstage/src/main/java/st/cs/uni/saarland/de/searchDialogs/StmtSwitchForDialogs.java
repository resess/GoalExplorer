package st.cs.uni.saarland.de.searchDialogs;

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
import st.cs.uni.saarland.de.helpClasses.MyStmtSwitch;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class StmtSwitchForDialogs extends MyStmtSwitch {
	protected final Logger logger = LoggerFactory.getLogger(this.getClass());

	//TODO by default, all text is capitalized, unless the setting is set to false (need to check lol)

//	private DialogInfo diaInfo;
	private String subSignatureOnClick = "void onClick(android.content.DialogInterface,int)";
	
	public StmtSwitchForDialogs(SootMethod currentSootMethod) {
		super(currentSootMethod);
	}
	
	
//    r6 = new android.app.AlertDialog$Builder;
//    $r1 = r6;
//    r10 = (android.content.Context) $r0;
//    specialinvoke r6.<android.app.AlertDialog$Builder: void <init>(android.content.Context)>(r10);
//    virtualinvoke $r1.<android.app.AlertDialog$Builder: android.app.AlertDialog$Builder setTitle(int)>(2131034122);
//    virtualinvoke $r1.<android.app.AlertDialog$Builder: android.app.AlertDialog$Builder setMessage(java.lang.CharSequence)>("Sending SMS");
//    r7 = new com.example.Testapp.MainActivity2$6;
//    specialinvoke r7.<com.example.Testapp.MainActivity2$6: void <init>(com.example.Testapp.MainActivity2)>($r0);
//    virtualinvoke $r1.<android.app.AlertDialog$Builder: android.app.AlertDialog$Builder setItems(int,android.content.DialogInterface$OnClickListener)>(2130968576, r7);
//    r8 = new com.example.Testapp.MainActivity2$7;
//    specialinvoke r8.<com.example.Testapp.MainActivity2$7: void <init>(com.example.Testapp.MainActivity2)>($r0);
//    virtualinvoke $r1.<android.app.AlertDialog$Builder: android.app.AlertDialog$Builder setNeutralButton(java.lang.CharSequence,android.content.DialogInterface$OnClickListener)>("Yes", r8);
//    $r2 = virtualinvoke $r1.<android.app.AlertDialog$Builder: android.app.AlertDialog create()>();
//    virtualinvoke $r2.<android.app.AlertDialog: void show()>();
    
	
	public StmtSwitchForDialogs(DialogInfo newInfo, SootMethod currentSootMethod) {
		super(newInfo, currentSootMethod);
	}
	


	public void caseIdentityStmt(final IdentityStmt stmt){
		if(Thread.currentThread().isInterrupted()){
			return;
		}
		Set<Info> toAddInfos = new LinkedHashSet<>();
		for (Info i: getResultInfos()){
			if(Thread.currentThread().isInterrupted()){
				return;
			}
			DialogInfo diaInfo = (DialogInfo) i;
			
			String leftReg = helpMethods.getLeftRegOfIdentityStmt(stmt);
			String rightRegType = helpMethods.getRightClassTypeOfIdentityStmt(stmt);
			if (stmt.getRightOp() instanceof ParameterRef){
				int paramIndex = ((ParameterRef)stmt.getRightOp()).getIndex();
				if (diaInfo.getActivityReg().equals(leftReg)){
					diaInfo.setActivityReg("");
					List<Info> resList = interprocMethods2.findInReachableMethods2(paramIndex,  getCurrentSootMethod(), new ArrayList<SootMethod>());
					if (resList.size() > 0){
						diaInfo.setActivity(((InterProcInfo)resList.get(0)).getClassOfSearchedReg());
						if (resList.size() > 1){
							for (int j = 1; j < resList.size() ; j++){
								InterProcInfo workingInfo = (InterProcInfo) resList.get(j);
								DialogInfo newInfo = (DialogInfo) diaInfo.clone();
								toAddInfos.add(newInfo);
								newInfo.setActivity(workingInfo.getClassOfSearchedReg());
							}
						}
					}				
				}
				if (diaInfo.getNegListenerReg().equals(leftReg)){
					diaInfo.setNegListenerReg("");
					List<Info> resList = interprocMethods2.findInReachableMethods2(paramIndex,  getCurrentSootMethod(), new ArrayList<SootMethod>());
					if (resList.size() > 0){
						diaInfo.addNegativeListener(getListener(((InterProcInfo)resList.get(0)).getClassOfSearchedReg()));
						if (resList.size() > 1){
							for (int j = 1; j < resList.size() ; j++){
								InterProcInfo workingInfo = (InterProcInfo) resList.get(j);
								DialogInfo newInfo = (DialogInfo) diaInfo.clone();
								toAddInfos.add(newInfo);
								newInfo.addNegativeListener(getListener(workingInfo.getClassOfSearchedReg()));
							}
						}
					}	
				}
				if (diaInfo.getPosListenerReg().equals(leftReg)){
					diaInfo.setPosListenerReg("");
					List<Info> resList = interprocMethods2.findInReachableMethods2(paramIndex,  getCurrentSootMethod(), new ArrayList<SootMethod>());
					if (resList.size() > 0){
						diaInfo.addPosListener(getListener(((InterProcInfo)resList.get(0)).getClassOfSearchedReg()));
						if (resList.size() > 1){
							for (int j = 1; j < resList.size() ; j++){
								InterProcInfo workingInfo = (InterProcInfo) resList.get(j);
								DialogInfo newInfo = (DialogInfo) diaInfo.clone();
								toAddInfos.add(newInfo);
								newInfo.addPosListener(getListener(workingInfo.getClassOfSearchedReg()));
							}
						}
					}	
				}
				if (diaInfo.getNeutralListenerReg().equals(leftReg)){
					diaInfo.setNeutralListenerReg("");
					List<Info> resList = interprocMethods2.findInReachableMethods2(paramIndex,  getCurrentSootMethod(), new ArrayList<SootMethod>());
					if (resList.size() > 0){
						diaInfo.addNeutralListener(getListener(((InterProcInfo)resList.get(0)).getClassOfSearchedReg()));
						if (resList.size() > 1){
							for (int j = 1; j < resList.size() ; j++){
								InterProcInfo workingInfo = (InterProcInfo) resList.get(j);
								DialogInfo newInfo = (DialogInfo) diaInfo.clone();
								toAddInfos.add(newInfo);
								newInfo.addNeutralListener(getListener(workingInfo.getClassOfSearchedReg()));
							}
						}
					}
				}
				if (diaInfo.getItemListenerReg().equals(leftReg)){
					diaInfo.setItemListenerReg("");
					List<Info> resList = interprocMethods2.findInReachableMethods2(paramIndex,  getCurrentSootMethod(), new ArrayList<SootMethod>());
					if (resList.size() > 0){
						diaInfo.addItemListener(getListener((((InterProcInfo)resList.get(0)).getClassOfSearchedReg())));
						if (resList.size() > 1){
							for (int j = 1; j < resList.size() ; j++){
								InterProcInfo workingInfo = (InterProcInfo) resList.get(j);
								DialogInfo newInfo = (DialogInfo) diaInfo.clone();
								toAddInfos.add(newInfo);
								newInfo.addItemListener(getListener((workingInfo.getClassOfSearchedReg())));
							}
						}
					}
				}
			}else if (stmt.getRightOp() instanceof ThisRef){

				if (diaInfo.getActivityReg().equals(leftReg)){
					diaInfo.setActivity(rightRegType);
					diaInfo.setActivityReg("");
				}
				if (diaInfo.getNegListenerReg().equals(leftReg)){
					diaInfo.setNegListenerReg("");
					diaInfo.addNegativeListener(getListener(rightRegType));
				}
				if (diaInfo.getPosListenerReg().equals(leftReg)){
					diaInfo.setPosListenerReg("");
					diaInfo.addPosListener(getListener(rightRegType));
				}
				if (diaInfo.getNeutralListenerReg().equals(leftReg)){
					diaInfo.setNeutralListenerReg("");
					diaInfo.addNeutralListener(getListener(rightRegType));
				}
				if (diaInfo.getItemListenerReg().equals(leftReg)){
					diaInfo.setItemListenerReg("");
					diaInfo.addItemListener(getListener(rightRegType));
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
		analyzeInvokeExpr(invokeExpr);
	}


	private void analyzeInvokeExpr(final InvokeExpr invokeExpr) {
		String methodSignature = helpMethods.getSignatureOfInvokeExpr(invokeExpr);
		
		
		// case 1:  virtualinvoke $r1.<android.app.AlertDialog: void show()>();
			// !!! not any more included, because with the tool start the DialogAnalysis with "create" call
//		if (methodSignature.equals("<android.app.AlertDialog: void show()>") && (diaInfo == null)){
//			diaInfo = new DialogInfo(helpMethods.getCallerOfInvokeStmt(invokeExpr));
//		}else if (diaInfo == null){
//				shouldBreak = true;
//		}else

		//case 1: $r2 = virtualinvoke $r1.<android.app.AlertDialog$Builder: android.app.AlertDialog show()>();
		if (getResultInfos().size() == 0){
			if(methodSignature.equals("<android.app.AlertDialog$Builder: android.app.AlertDialog show()>")){ //we shouldn't have both show and create
				DialogInfo diaInfo = new DialogInfo(helpMethods.getCallerOfInvokeStmt(invokeExpr));
				diaInfo.setMethodSignature(getCurrentSootMethod().getSignature());
				addToResultInfo(diaInfo);
			}
		}
		else{
			for (Info i: getResultInfos()){
				if(Thread.currentThread().isInterrupted()){
					return;
				}
				DialogInfo diaInfo = (DialogInfo) i;

				// case 2a: virtualinvoke $r2.<android.app.AlertDialog$Builder: android.app.AlertDialog$Builder setNegativeButton(java.lang.CharSequence,android.content.DialogInterface$OnClickListener)>("No", $r5);
					if ((methodSignature.contains("<android.app.AlertDialog$Builder: android.app.AlertDialog$Builder setNegativeButton")
							|| methodSignature.contains("<android.app.AlertDialogBuilder: void setButton(java.lang.CharSequence,android.content.DialogInterface$OnClickListener")) //TODO check 2nd argument
						&& (diaInfo != null)){
						if (diaInfo.getSearchedEReg().equals(helpMethods.getCallerOfInvokeStmt(invokeExpr))){
							//logger.debug("Setting negative button for diaInfo {} at {}", diaInfo, invokeExpr);
							String param = helpMethods.getParameterOfInvokeStmt(invokeExpr, 0);
							//TODO remove null case
							if (!((invokeExpr.getArg(0) instanceof IntConstant) || checkMethods.checkIfValueIsString(param)))
								diaInfo.setNegTextReg(helpMethods.getParameterOfInvokeStmt(invokeExpr, 0).replace("\"", ""));
							else
								diaInfo.addNegText(helpMethods.getParameterOfInvokeStmt(invokeExpr, 0).replace("\"", ""));

							//logger.debug("Setting negative text to {}", diaInfo.getNegText());
							diaInfo.setNegListenerReg(helpMethods.getParameterOfInvokeStmt(invokeExpr, 1));
		//					Listener l = new Listener("onClick", false, "void onClick(android.view.View)");
		//					l.setListenerClass(helpMethods.getParameterTypeOfInvokeStmt(invokeExpr,1));
		//					diaInfo.addNegativeListener(l);
						}
				}else
					// case 2b: virtualinvoke $r2.<android.app.AlertDialog$Builder: android.app.AlertDialog$Builder setPositiveButton(java.lang.CharSequence,android.content.DialogInterface$OnClickListener)>("Yes", $r4);
					if ((methodSignature.contains("<android.app.AlertDialog$Builder: android.app.AlertDialog$Builder setPositiveButton")
							|| methodSignature.contains("<android.app.AlertDialogBuilder: void setButton2(java.lang.CharSequence,android.content.DialogInterface$OnClickListener")) //TODO check 2nd argument
							&& (diaInfo != null)){
						if (diaInfo.getSearchedEReg().equals(helpMethods.getCallerOfInvokeStmt(invokeExpr))){
							//logger.debug("Setting positive button for diaInfo at {}", diaInfo, invokeExpr);
							String param = helpMethods.getParameterOfInvokeStmt(invokeExpr, 0);
							if (!((invokeExpr.getArg(0) instanceof IntConstant) || checkMethods.checkIfValueIsString(param)))
								diaInfo.setPosTextReg(helpMethods.getParameterOfInvokeStmt(invokeExpr, 0).replace("\"", ""));
							else
								diaInfo.addPosText(helpMethods.getParameterOfInvokeStmt(invokeExpr, 0).replace("\"", ""));
							//logger.debug("Setting positive text to {}", diaInfo.getPosText());
							diaInfo.setPosListenerReg(helpMethods.getParameterOfInvokeStmt(invokeExpr, 1));
						}
				}else
					// case 2c: virtualinvoke $r2.<android.app.AlertDialog$Builder: android.app.AlertDialog$Builder setPositiveButton(int,android.content.DialogInterface$OnClickListener)>(25145565.., $r4);
					if ((methodSignature.contains("<android.app.AlertDialog$Builder: android.app.AlertDialog$Builder setNeutralButton")
							|| methodSignature.contains("<android.app.AlertDialogBuilder: void setButton3(java.lang.CharSequence,android.content.DialogInterface$OnClickListener"))
							&& (diaInfo != null)){
						if (diaInfo.getSearchedEReg().equals(helpMethods.getCallerOfInvokeStmt(invokeExpr))){
							String param = helpMethods.getParameterOfInvokeStmt(invokeExpr, 0);
							if (!((invokeExpr.getArg(0) instanceof IntConstant) || checkMethods.checkIfValueIsString(param)))
								diaInfo.setNeutralTextReg(helpMethods.getParameterOfInvokeStmt(invokeExpr, 0).replace("\"", ""));
							else
								diaInfo.addNeutralText(helpMethods.getParameterOfInvokeStmt(invokeExpr, 0).replace("\"", ""));
							diaInfo.setNeutralListenerReg(helpMethods.getParameterOfInvokeStmt(invokeExpr, 1));
		//					Listener l = new Listener("onClick", false, "void onClick(android.view.View)");
		//					l.setListenerClass(helpMethods.getParameterTypeOfInvokeStmt(invokeExpr,1));
		//					diaInfo.addNeutralListener(l);
						}
				}else
					if(methodSignature.contains("<android.app.AlertDialogBuilder: void setButton(int, java.lang.CharSequence")){
					//need to switch on which button
						logger.warn("Switch on dialog button type not handled yet ... {}", methodSignature);
						//logger.debug("Switch on dialog button type not handled yet ... {}", methodSignature);
					}
					else
					// case 3: virtualinvoke $r2.<android.app.AlertDialog$Builder: android.app.AlertDialog$Builder setMessage(java.lang.CharSequence)>("Sending SMS");
					if (methodSignature.contains("<android.app.AlertDialog$Builder: android.app.AlertDialog$Builder setMessage") 
							&& (diaInfo != null)){
						if (diaInfo.getSearchedEReg().equals(helpMethods.getCallerOfInvokeStmt(invokeExpr))){
							String param = helpMethods.getParameterOfInvokeStmt(invokeExpr, 0);
							if (!((invokeExpr.getArg(0) instanceof IntConstant) || checkMethods.checkIfValueIsString(param)))
								diaInfo.setMessageReg(helpMethods.getParameterOfInvokeStmt(invokeExpr, 0).replace("\"", ""));
							else
								diaInfo.addMessage(helpMethods.getParameterOfInvokeStmt(invokeExpr, 0).replace("\"", ""));
						}
				}else
					// case 4: virtualinvoke $r2.<android.app.AlertDialog$Builder: android.app.AlertDialog$Builder setTitle(java.lang.CharSequence)>("Your Title");
					if (methodSignature.contains("<android.app.AlertDialog$Builder: android.app.AlertDialog$Builder setTitle") 
							&& (diaInfo != null)){
						if (diaInfo.getSearchedEReg().equals(helpMethods.getCallerOfInvokeStmt(invokeExpr))){
							String param = helpMethods.getParameterOfInvokeStmt(invokeExpr, 0);
							if (!((invokeExpr.getArg(0) instanceof IntConstant) || checkMethods.checkIfValueIsString(param)))
								diaInfo.setTitleReg(helpMethods.getParameterOfInvokeStmt(invokeExpr, 0).replace("\"", ""));
							else
								diaInfo.addTitle(helpMethods.getParameterOfInvokeStmt(invokeExpr, 0).replace("\"", ""));
						}
				}else
					// case 5: specialinvoke $r2.<android.app.AlertDialog$Builder: void <init>(android.content.Context)>($r0);
					if (methodSignature.equals("<android.app.AlertDialog$Builder: void <init>(android.content.Context)>") 
							&& (diaInfo != null)){
						if (diaInfo.getSearchedEReg().equals(helpMethods.getCallerOfInvokeStmt(invokeExpr))){
							diaInfo.setActivityReg(helpMethods.getParameterOfInvokeStmt(invokeExpr, 0));
						}
				}else
		//			// case 6: virtualinvoke r7.<android.app.AlertDialog$Builder: android.app.AlertDialog$Builder setMultiChoiceItems(int,boolean[],android.content.DialogInterface$OnMultiChoiceClickListener)>(2130968576, null, r12);
					if (methodSignature.equals("<android.app.AlertDialog$Builder: android.app.AlertDialog$Builder setMultiChoiceItems(int,boolean[],android.content.DialogInterface$OnMultiChoiceClickListener)>") 
							&& (diaInfo != null)){
						if (diaInfo.getSearchedEReg().equals(helpMethods.getCallerOfInvokeStmt(invokeExpr))){
							String idOfArray = helpMethods.getParameterOfInvokeStmt(invokeExpr, 0);
							String param = helpMethods.getParameterOfInvokeStmt(invokeExpr, 0);
							if (!((invokeExpr.getArg(0) instanceof IntConstant) || checkMethods.checkIfValueIsString(param)))
								diaInfo.setItemTextsArrayIDReg(idOfArray);
							else
								diaInfo.addItemTextsArrayID(idOfArray);
							diaInfo.setItemListenerReg(helpMethods.getParameterOfInvokeStmt(invokeExpr, 2));
						}
				} else
	//				<android.app.AlertDialog$Builder: android.app.AlertDialog$Builder setItems(int,android.content.DialogInterface$OnClickListener)>(
					if (methodSignature.equals("<android.app.AlertDialog$Builder: android.app.AlertDialog$Builder setItems(int,android.content.DialogInterface$OnClickListener)>") 
							&& (diaInfo != null)){
						if (diaInfo.getSearchedEReg().equals(helpMethods.getCallerOfInvokeStmt(invokeExpr))){
							String idOfArray = helpMethods.getParameterOfInvokeStmt(invokeExpr, 0);
							if (!((invokeExpr.getArg(0) instanceof IntConstant) || checkMethods.checkIfValueIsString(idOfArray)))
								diaInfo.setItemTextsArrayIDReg(idOfArray);
							else
								diaInfo.addItemTextsArrayID(idOfArray);
							diaInfo.setItemListenerReg(helpMethods.getParameterOfInvokeStmt(invokeExpr, 1));
						}
				} else
					// TODO missing variable for stringIDs for items
		//			// case 6: virtualinvoke r7.<android.app.AlertDialog$Builder: android.app.AlertDialog$Builder setSingleChoiceItems(int,boolean[],android.content.DialogInterface$OnMultiChoiceClickListener)>(2130968576, null, r12);
					if (methodSignature.equals("<android.app.AlertDialog$Builder: android.app.AlertDialog$Builder setSingleChoiceItems(int,int,android.content.DialogInterface$OnClickListener)>") 
							&& (diaInfo != null)){
						if (diaInfo.getSearchedEReg().equals(helpMethods.getCallerOfInvokeStmt(invokeExpr))){
							String idOfArray = helpMethods.getParameterOfInvokeStmt(invokeExpr, 0);
							if (!((invokeExpr.getArg(0) instanceof IntConstant) || checkMethods.checkIfValueIsString(idOfArray)))
								diaInfo.setItemTextsArrayIDReg(idOfArray);
							else
								diaInfo.addItemTextsArrayID(idOfArray);
							diaInfo.setItemListenerReg(helpMethods.getParameterOfInvokeStmt(invokeExpr, 2));
						}
				} 
			}
		}
	}
	
	public void caseAssignStmt(final AssignStmt stmt){
		if(Thread.currentThread().isInterrupted()){
			return;
		}
		String leftReg = helpMethods.getLeftRegOfAssignStmt(stmt);
		
		//case 1:  $r1 = virtualinvoke $r2.<android.app.AlertDialog$Builder: android.app.AlertDialog create()>();
		if (stmt.containsInvokeExpr()){
			InvokeExpr invokeExpr = stmt.getInvokeExpr();
			String methodSignature = helpMethods.getSignatureOfInvokeExpr(invokeExpr);
			if (getResultInfos().size() == 0){
				if (methodSignature.equals("<android.app.AlertDialog$Builder: android.app.AlertDialog create()>")){
					DialogInfo diaInfo = new DialogInfo(helpMethods.getCallerOfInvokeStmt(invokeExpr));
					diaInfo.setMethodSignature(getCurrentSootMethod().getSignature());
					/*logger.debug("Found a dialog builder create instance {}", stmt);
					logger.debug("Adding info {} to set ", diaInfo);*/
					addToResultInfo(diaInfo);
				}
			}else{
				Set<Info> toAddInfos = new LinkedHashSet<>();
				for (Info i: getResultInfos()){
					if(Thread.currentThread().isInterrupted()){
						return;
					}
					DialogInfo diaInfo = (DialogInfo) i;
					// check if a method is called to get the object of a variable
					 //if (!SystemClassHandler.isClassInSystemPackage(callerClass) || callerClass.equalsIgnoreCase("android.os.Bundle")){//android.os.Bundle || android.os.Parcel
						if (diaInfo.getPosListenerReg().equals(leftReg)){
							diaInfo.setPosListenerReg("");
							List<InterProcInfo> resList = interprocMethods2.findReturnValueInMethod2(stmt);
							if (resList.size() > 0){
								diaInfo.addPosListener(getListener(((InterProcInfo)resList.get(0)).getClassOfSearchedReg()));
								if (resList.size() > 1){
									for (int j = 1; j < resList.size() ; j++){
										InterProcInfo workingInfo = (InterProcInfo) resList.get(j);
										DialogInfo newInfo = (DialogInfo) diaInfo.clone();
										toAddInfos.add(newInfo);
										newInfo.addPosListener(getListener(workingInfo.getClassOfSearchedReg()));
									}
								}
							}	
						}
						if (diaInfo.getNegListenerReg().equals(leftReg)){
							diaInfo.setNegListenerReg("");
							List<InterProcInfo> resList = interprocMethods2.findReturnValueInMethod2(stmt);
							if (resList.size() > 0){
								diaInfo.addNegativeListener(getListener(((InterProcInfo)resList.get(0)).getClassOfSearchedReg()));
								if (resList.size() > 1){
									for (int j = 1; j < resList.size() ; j++){
										InterProcInfo workingInfo = (InterProcInfo) resList.get(j);
										DialogInfo newInfo = (DialogInfo) diaInfo.clone();
										toAddInfos.add(newInfo);
										newInfo.addNegativeListener(getListener(workingInfo.getClassOfSearchedReg()));
									}
								}
							}	
						}
						if (diaInfo.getNeutralListenerReg().equals(leftReg)){
							diaInfo.setNeutralListenerReg("");
							List<InterProcInfo> resList = interprocMethods2.findReturnValueInMethod2(stmt);
							if (resList.size() > 0){
								diaInfo.addNeutralListener(getListener(((InterProcInfo)resList.get(0)).getClassOfSearchedReg()));
								if (resList.size() > 1){
									for (int j = 1; j < resList.size() ; j++){
										InterProcInfo workingInfo = (InterProcInfo) resList.get(j);
										DialogInfo newInfo = (DialogInfo) diaInfo.clone();
										toAddInfos.add(newInfo);
										newInfo.addNeutralListener(getListener(workingInfo.getClassOfSearchedReg()));
									}
								}
							}	
						}
						if (diaInfo.getActivityReg().equals(leftReg)){
							diaInfo.setActivityReg("");
							if(!invokeExpr.getMethod().hasActiveBody() && invokeExpr.getMethod().getName().equals("getActivity")){
								String callerOfInvokeStmt = helpMethods.getCallerTypeOfInvokeStmt(invokeExpr);
								diaInfo.setActivity(callerOfInvokeStmt);
							}
							else{
								List<InterProcInfo> resList = interprocMethods2.findReturnValueInMethod2(stmt);
								if (resList.size() > 0){
									diaInfo.setActivity(((InterProcInfo)resList.get(0)).getClassOfSearchedReg());
									if (resList.size() > 1){
										for (int j = 1; j < resList.size() ; j++){
											InterProcInfo workingInfo = (InterProcInfo) resList.get(j);
											DialogInfo newInfo = (DialogInfo) diaInfo.clone();
											toAddInfos.add(newInfo);
											newInfo.setActivity(workingInfo.getClassOfSearchedReg());
										}
									}
								}
							}
						}
						if (diaInfo.getItemListenerReg().equals(leftReg)){
							diaInfo.setItemListenerReg("");
							List<InterProcInfo> resList = interprocMethods2.findReturnValueInMethod2(stmt);
							if (resList.size() > 0){
								diaInfo.addItemListener(getListener((((InterProcInfo)resList.get(0)).getClassOfSearchedReg())));
								if (resList.size() > 1){
									for (int j = 1; j < resList.size() ; j++){
										InterProcInfo workingInfo = (InterProcInfo) resList.get(j);
										DialogInfo newInfo = (DialogInfo) diaInfo.clone();
										toAddInfos.add(newInfo);
										newInfo.addItemListener(getListener((workingInfo.getClassOfSearchedReg())));
									}
								}
							}	
						}
						if (diaInfo.getPosTextReg().equals(leftReg)){
							diaInfo.setPosTextReg("");
							List<InterProcInfo> resList = interprocMethods2.findReturnValueInMethod2(stmt);
							if (resList.size() > 0){
								diaInfo.addPosText((((InterProcInfo)resList.get(0)).getValueOfSearchedReg()));
								if (resList.size() > 1){
									for (int j = 1; j < resList.size() ; j++){
										InterProcInfo workingInfo = (InterProcInfo) resList.get(j);
										diaInfo.addPosText((workingInfo.getValueOfSearchedReg()));
									}
								}
							}
						}
						if (diaInfo.getNegTextReg().equals(leftReg)){
							diaInfo.setNegTextReg("");
							List<InterProcInfo> resList = interprocMethods2.findReturnValueInMethod2(stmt);
							if (resList.size() > 0){
								diaInfo.addNegText((((InterProcInfo)resList.get(0)).getValueOfSearchedReg()));
								if (resList.size() > 1){
									for (int j = 1; j < resList.size() ; j++){
										InterProcInfo workingInfo = (InterProcInfo) resList.get(j);
										diaInfo.addNegText((workingInfo.getValueOfSearchedReg()));
									}
								}
							}
						}
						if (diaInfo.getNeutralTextReg().equals(leftReg)){
							diaInfo.setNeutralTextReg("");
							List<InterProcInfo> resList = interprocMethods2.findReturnValueInMethod2(stmt);
							if (resList.size() > 0){
								diaInfo.addNeutralText((((InterProcInfo)resList.get(0)).getValueOfSearchedReg()));
								if (resList.size() > 1){
									for (int j = 1; j < resList.size() ; j++){
										InterProcInfo workingInfo = (InterProcInfo) resList.get(j);
										diaInfo.addNeutralText((workingInfo.getValueOfSearchedReg()));
									}
								}
							}
						}
						if (diaInfo.getTitleReg().equals(leftReg)){
							diaInfo.setTitleReg("");
							List<InterProcInfo> resList = interprocMethods2.findReturnValueInMethod2(stmt);
							if (resList.size() > 0){
								diaInfo.addTitle((((InterProcInfo)resList.get(0)).getValueOfSearchedReg()));
								if (resList.size() > 1){
									for (int j = 1; j < resList.size() ; j++){
										InterProcInfo workingInfo = (InterProcInfo) resList.get(j);
										diaInfo.addTitle((workingInfo.getValueOfSearchedReg()));
									}
								}
							}
						}
						if (diaInfo.getMessageReg().equals(leftReg)){
							diaInfo.setMessageReg("");
							List<InterProcInfo> resList = interprocMethods2.findReturnValueInMethod2(stmt);
							if (resList.size() > 0){
								diaInfo.addMessage((((InterProcInfo)resList.get(0)).getValueOfSearchedReg()));
								if (resList.size() > 1){
									for (int j = 1; j < resList.size() ; j++){
										InterProcInfo workingInfo = (InterProcInfo) resList.get(j);
										diaInfo.addMessage((workingInfo.getValueOfSearchedReg()));
									}
								}
							}
						}
						if (diaInfo.getItemTextsArrayIDReg().equals(leftReg)){
							diaInfo.setItemTextsArrayIDReg("");
							List<InterProcInfo> resList = interprocMethods2.findReturnValueInMethod2(stmt);
							if (resList.size() > 0){
								diaInfo.addItemTextsArrayID((((InterProcInfo)resList.get(0)).getValueOfSearchedReg()));
								if (resList.size() > 1){
									for (int j = 1; j < resList.size() ; j++){
										InterProcInfo workingInfo = (InterProcInfo) resList.get(j);
										diaInfo.addItemTextsArrayID((workingInfo.getValueOfSearchedReg()));
									}
								}
							}
						}
						analyzeInvokeExpr(invokeExpr);
					//}
				}
				addAllToResultInfo(toAddInfos);
			}
		}else{
			Set<Info> toAddInfos = new LinkedHashSet<>();
			Set<Info> toRemoveInfos = new LinkedHashSet<>();
			for (Info i: getResultInfos()){
				if(Thread.currentThread().isInterrupted()){
					return;
				}
				DialogInfo diaInfo = (DialogInfo) i;
				
				// case 2 : $r2 = new android.app.AlertDialog$Builder;
				if ((diaInfo != null) && leftReg.equals(diaInfo.getSearchedEReg()) && helpMethods.getCompleteRightSideOfAssignStmt(stmt).equals("new android.app.AlertDialog$Builder") ){
					diaInfo.setSearchedEReg("");
					diaInfo.setFinished();
				} else
				if (diaInfo.getPosListenerReg().equals(leftReg)){
					diaInfo.setPosListenerReg("");
					if (stmt.getRightOp() instanceof NewExpr){
						Listener l = new Listener("onClick", false, subSignatureOnClick, getCurrentSootMethod().getDeclaringClass().getName());
						l.setListenerClass(helpMethods.getTypeOfRightRegOfAssignStmt(stmt));
						diaInfo.addPosListener(l);
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
									DialogInfo newInfo = new DialogInfo("");
									
									newInfo.setPosListenerReg(fInfo.register.getName());
									StmtSwitchForDialogs newStmtSwitch = new StmtSwitchForDialogs(newInfo, getCurrentSootMethod());
									previousFields.forEach(x->newStmtSwitch.addPreviousField(x));
									iteratorHelper.runOverToFindSpecValuesBackwards(fInfo.methodToStart.method().getActiveBody(), workingUnit, newStmtSwitch);
									Set<Info> initValues = newStmtSwitch.getResultInfos();

									if(initValues.size() > 0) {
										List<Info> listInfo = initValues.stream().collect(Collectors.toList());
										if(listInfo.indexOf(newInfo) == -1){
											continue;
										}
										Info initInfo = listInfo.get(listInfo.indexOf(newInfo));
										diaInfo.addPosListener(((DialogInfo) initInfo).getPosListener());
									}

								}
							}
						}else{
							Helper.saveToStatisticalFile("Error DialogSwitch: Doesn't find posListenerReg in initializationOfField: " + stmt);
						}
					}else{
						diaInfo.setPosListenerReg(helpMethods.getRightRegOfAssignStmt(stmt));
					}
				
				}
				if (diaInfo.getNegListenerReg().equals(leftReg)){
					diaInfo.setNegListenerReg("");
					if (stmt.getRightOp() instanceof NewExpr){
						Listener l = new Listener("onClick", false, subSignatureOnClick, getCurrentSootMethod().getDeclaringClass().getName());
						l.setListenerClass(helpMethods.getTypeOfRightRegOfAssignStmt(stmt));
						diaInfo.addNegativeListener(l);
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
									DialogInfo newInfo = new DialogInfo("");
									newInfo.setNegListenerReg(fInfo.register.getName());
									StmtSwitchForDialogs newStmtSwitch = new StmtSwitchForDialogs(newInfo, getCurrentSootMethod());
									previousFields.forEach(x->newStmtSwitch.addPreviousField(x));
									iteratorHelper.runOverToFindSpecValuesBackwards(fInfo.methodToStart.method().getActiveBody(), workingUnit, newStmtSwitch);
									Set<Info> initValues = newStmtSwitch.getResultInfos();

									if(initValues.size() > 0) {
										List<Info> listInfo = initValues.stream().collect(Collectors.toList());
										if(listInfo.indexOf(newInfo) == -1){
											continue;
										}
										Info initInfo = listInfo.get(listInfo.indexOf(newInfo));
										diaInfo.addNegativeListener(((DialogInfo)initInfo).getNegativeListener());
									}
								}
							}
						}else{
							Helper.saveToStatisticalFile("Error DialogSwitch: Doesn't find negativeListenerReg in initializationOfField: " + stmt);
						}
					}else{
						diaInfo.setNegListenerReg(helpMethods.getRightRegOfAssignStmt(stmt));
					}
				} if (diaInfo.getNeutralListenerReg().equals(leftReg)){
					diaInfo.setNeutralListenerReg("");
					if (stmt.getRightOp() instanceof NewExpr){
						Listener l = new Listener("onClick", false, subSignatureOnClick, getCurrentSootMethod().getDeclaringClass().getName());
						l.setListenerClass(helpMethods.getTypeOfRightRegOfAssignStmt(stmt));
						diaInfo.addNeutralListener(l);
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
									DialogInfo newInfo = new DialogInfo("");
									newInfo.setNeutralListenerReg(fInfo.register.getName());
									StmtSwitchForDialogs newStmtSwitch = new StmtSwitchForDialogs(newInfo, getCurrentSootMethod());
									previousFields.forEach(x->newStmtSwitch.addPreviousField(x));
									iteratorHelper.runOverToFindSpecValuesBackwards(fInfo.methodToStart.method().getActiveBody(), workingUnit, newStmtSwitch);
									Set<Info> initValues = newStmtSwitch.getResultInfos();

									if(initValues.size() > 0) {
										List<Info> listInfo = initValues.stream().collect(Collectors.toList());
										if(listInfo.indexOf(newInfo) == -1){
											continue;
										}
										Info initInfo = listInfo.get(listInfo.indexOf(newInfo));
										diaInfo.addNeutralListener(((DialogInfo)initInfo).getNeutralListener());
									}
								}
							}
						}else{
							Helper.saveToStatisticalFile("Error DialogSwitch: Doesn't find neutralListenerReg in initializationOfField: " + stmt);
						}
					}else{
						diaInfo.setNeutralListenerReg(helpMethods.getRightRegOfAssignStmt(stmt));
					}
				
				} if (diaInfo.getActivityReg().equals(leftReg)){
					
					if (stmt.getRightOp() instanceof NewExpr){
						diaInfo.setActivityReg("");
						diaInfo.setActivity(helpMethods.getTypeOfRightRegOfAssignStmt(stmt));
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
							toRemoveInfos.add(diaInfo);
							for (FieldInfo fInfo : fInfos){
								if(Thread.currentThread().isInterrupted()){
									return;
								}
								if(fInfo.methodToStart != null && fInfo.methodToStart.method().hasActiveBody()){
									
									Unit workingUnit = fInfo.unitToStart;
									DialogInfo newInfo = new DialogInfo("");
									newInfo.setActivityReg(fInfo.register.getName());
									StmtSwitchForDialogs newStmtSwitch = new StmtSwitchForDialogs(newInfo, getCurrentSootMethod());
									previousFields.forEach(x->newStmtSwitch.addPreviousField(x));
									iteratorHelper.runOverToFindSpecValuesBackwards(fInfo.methodToStart.method().getActiveBody(), workingUnit, newStmtSwitch);
									Set<Info> initValues = newStmtSwitch.getResultInfos();

									if(initValues.size() > 0) {
										List<Info> listInfo = initValues.stream().collect(Collectors.toList());
										if(listInfo.indexOf(newInfo) == -1){
											continue;
										}
										Info initInfo = listInfo.get(listInfo.indexOf(newInfo));
										DialogInfo newInfo2 = (DialogInfo) diaInfo.clone();
										newInfo2.setActivity(((DialogInfo)initInfo).getActivity());
										newInfo2.setActivityReg("");
										toAddInfos.add(newInfo2);
									}
								}
							}
						}else{
							diaInfo.setActivityReg("");
							Helper.saveToStatisticalFile("Error DialogSwitch: Doesn't find ActivityReg in initializationOfField: " + stmt);
						}
					}else{
						diaInfo.setActivityReg("");
						diaInfo.setActivityReg(helpMethods.getRightRegOfAssignStmt(stmt));
					}
				} if (diaInfo.getItemListenerReg().equals(leftReg)){
					diaInfo.setItemListenerReg("");
					if (stmt.getRightOp() instanceof NewExpr){
						Listener l = new Listener("onClick", false, subSignatureOnClick, getCurrentSootMethod().getDeclaringClass().getName());
						l.setListenerClass(helpMethods.getTypeOfRightRegOfAssignStmt(stmt));
						diaInfo.addItemListener(l);
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
									DialogInfo newInfo = new DialogInfo("");
									newInfo.setItemListenerReg(fInfo.register.getName());
									StmtSwitchForDialogs newStmtSwitch = new StmtSwitchForDialogs(newInfo, getCurrentSootMethod());
									previousFields.forEach(x->newStmtSwitch.addPreviousField(x));
									iteratorHelper.runOverToFindSpecValuesBackwards(fInfo.methodToStart.method().getActiveBody(), workingUnit, newStmtSwitch);
									Set<Info> initValues = newStmtSwitch.getResultInfos();

									if(initValues.size() > 0) {
										List<Info> listInfo = initValues.stream().collect(Collectors.toList());
										if(listInfo.indexOf(newInfo) == -1){
											continue;
										}
										Info initInfo = listInfo.get(listInfo.indexOf(newInfo));
										diaInfo.addItemListener(((DialogInfo)initInfo).getItemListener());
									}
								}
							}
						}else{
							Helper.saveToStatisticalFile("Error DialogSwitch: Doesn't find itemListenerReg in initializationOfField: " + stmt);
						}
					}else{
						diaInfo.setItemListenerReg(helpMethods.getRightRegOfAssignStmt(stmt));
					}
				}
				if (leftReg.equals(diaInfo.getPosTextReg())){
					diaInfo.setPosTextReg("");
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
							for (FieldInfo fInfo : fInfos){
								if(Thread.currentThread().isInterrupted()){
									return;
								}
								if(fInfo.value != null){
									diaInfo.addPosText(fInfo.value);
									diaInfo.setPosTextReg("");
									continue;
								}else{
									if(fInfo.methodToStart != null && fInfo.methodToStart.method().hasActiveBody()){
										
										Unit workingUnit = fInfo.unitToStart;
										DialogInfo newInfo = new DialogInfo("");
										newInfo.setPosTextReg(fInfo.register.getName());
										StmtSwitchForDialogs newStmtSwitch = new StmtSwitchForDialogs(newInfo, getCurrentSootMethod());
										previousFields.forEach(x->newStmtSwitch.addPreviousField(x));
										iteratorHelper.runOverToFindSpecValuesBackwards(fInfo.methodToStart.method().getActiveBody(), workingUnit, newStmtSwitch);
										Set<Info> initValues = newStmtSwitch.getResultInfos();

										if(initValues.size() > 0) {
											List<Info> listInfo = initValues.stream().collect(Collectors.toList());
											if(listInfo.indexOf(newInfo) == -1){
												continue;
											}
											Info initInfo = listInfo.get(listInfo.indexOf(newInfo));
											diaInfo.addPosText(((DialogInfo)initInfo).getPosText());
											diaInfo.setPosTextReg("");
										}
									}
								}
							}
						}else{
							Helper.saveToStatisticalFile("Error DialogSwitch: Doesn't find posTextReg in initializationOfField: " + stmt);
						}
					}else{
						diaInfo.setPosTextReg(helpMethods.getRightRegOfAssignStmt(stmt));
					}
				}
				if (leftReg.equals(diaInfo.getNegTextReg())){
					diaInfo.setNegTextReg("");
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
							for (FieldInfo fInfo : fInfos){
								if(Thread.currentThread().isInterrupted()){
									return;
								}
								if(fInfo.value != null){
									diaInfo.addNegText(fInfo.value);
									continue;
								}else{
									if(fInfo.methodToStart != null && fInfo.methodToStart.method().hasActiveBody()){
										
										Unit workingUnit = fInfo.unitToStart;
										DialogInfo newInfo = new DialogInfo("");
										newInfo.setNegTextReg(fInfo.register.getName());
										StmtSwitchForDialogs newStmtSwitch = new StmtSwitchForDialogs(newInfo, getCurrentSootMethod());
										previousFields.forEach(x->newStmtSwitch.addPreviousField(x));
										iteratorHelper.runOverToFindSpecValuesBackwards(fInfo.methodToStart.method().getActiveBody(), workingUnit, newStmtSwitch);
										Set<Info> initValues = newStmtSwitch.getResultInfos();

										if(initValues.size() > 0) {
											List<Info> listInfo = initValues.stream().collect(Collectors.toList());
											if(listInfo.indexOf(newInfo) == -1){
												continue;
											}
											Info initInfo = listInfo.get(listInfo.indexOf(newInfo));
											diaInfo.addNegText(((DialogInfo)initInfo).getNegText());
										}
									}
								}
							}
						}else{
							Helper.saveToStatisticalFile("Error DialogSwitch: Doesn't find negTextReg in initializationOfField: " + stmt);
						}
					}else{
						diaInfo.setNegTextReg(helpMethods.getRightRegOfAssignStmt(stmt));
					}
				}
				if (leftReg.equals(diaInfo.getNeutralTextReg())){
					diaInfo.setNeutralTextReg("");
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
							for (FieldInfo fInfo : fInfos){
								if(Thread.currentThread().isInterrupted()){
									return;
								}
								if(fInfo.value != null){
									diaInfo.addNeutralText(fInfo.value);
									diaInfo.setNeutralTextReg("");
									continue;
								}else{
									if(fInfo.methodToStart != null && fInfo.methodToStart.method().hasActiveBody()){
										
										Unit workingUnit = fInfo.unitToStart;
										DialogInfo newInfo = new DialogInfo("");
										newInfo.setNeutralTextReg(fInfo.register.getName());
										StmtSwitchForDialogs newStmtSwitch = new StmtSwitchForDialogs(newInfo, getCurrentSootMethod());
										previousFields.forEach(x->newStmtSwitch.addPreviousField(x));
										iteratorHelper.runOverToFindSpecValuesBackwards(fInfo.methodToStart.method().getActiveBody(), workingUnit, newStmtSwitch);
										Set<Info> initValues = newStmtSwitch.getResultInfos();

										if(initValues.size() > 0) {
											List<Info> listInfo = initValues.stream().collect(Collectors.toList());
											if(listInfo.indexOf(newInfo) == -1){
												continue;
											}
											Info initInfo = listInfo.get(listInfo.indexOf(newInfo));
											diaInfo.addNeutralText(((DialogInfo)initInfo).getNeutralText());
											diaInfo.setNeutralTextReg("");
										}
									}
								}
							}
						}else{
							Helper.saveToStatisticalFile("Error DialogSwitch: Doesn't find neutralTextReg in initializationOfField: " + stmt);
						}
					}else{
						diaInfo.setNeutralTextReg(helpMethods.getRightRegOfAssignStmt(stmt));
					}
				}
				if (leftReg.equals(diaInfo.getTitleReg())){
					diaInfo.setTitleReg("");
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
							for (FieldInfo fInfo : fInfos){
								if(Thread.currentThread().isInterrupted()){
									return;
								}
								if(fInfo.value != null){
									diaInfo.addTitle(fInfo.value);
									diaInfo.setTitleReg("");
									continue;
								}else{
									if(fInfo.methodToStart != null && fInfo.methodToStart.method().hasActiveBody()){
										
										Unit workingUnit = fInfo.unitToStart;
										DialogInfo newInfo = new DialogInfo("");
										newInfo.setTitleReg(fInfo.register.getName());
										StmtSwitchForDialogs newStmtSwitch = new StmtSwitchForDialogs(newInfo, getCurrentSootMethod());
										previousFields.forEach(x->newStmtSwitch.addPreviousField(x));
										iteratorHelper.runOverToFindSpecValuesBackwards(fInfo.methodToStart.method().getActiveBody(), workingUnit, newStmtSwitch);
										Set<Info> initValues = newStmtSwitch.getResultInfos();

										if(initValues.size() > 0) {
											List<Info> listInfo = initValues.stream().collect(Collectors.toList());
											if(listInfo.indexOf(newInfo) == -1){
												continue;
											}
											Info initInfo = listInfo.get(listInfo.indexOf(newInfo));
											diaInfo.addTitle(((DialogInfo)initInfo).getTitle());
											
										}
									}
								}
							}
						}else{
							Helper.saveToStatisticalFile("Error DialogSwitch: Doesn't find titleTextReg in initializationOfField: " + stmt);
						}
					}else{
						diaInfo.setTitleReg(helpMethods.getRightRegOfAssignStmt(stmt));
					}
				}
				if (leftReg.equals(diaInfo.getMessageReg())){
					diaInfo.setMessageReg("");
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
							for (FieldInfo fInfo : fInfos){
								if(Thread.currentThread().isInterrupted()){
									return;
								}
								if(fInfo.value != null){
									diaInfo.addMessage(fInfo.value);
									diaInfo.setMessageReg("");
									continue;
								}else{
									if(fInfo.methodToStart != null && fInfo.methodToStart.method().hasActiveBody()){
										
										Unit workingUnit = fInfo.unitToStart;
										DialogInfo newInfo = new DialogInfo("");
										newInfo.setMessageReg(fInfo.register.getName());
										StmtSwitchForDialogs newStmtSwitch = new StmtSwitchForDialogs(newInfo, getCurrentSootMethod());
										previousFields.forEach(x->newStmtSwitch.addPreviousField(x));
										iteratorHelper.runOverToFindSpecValuesBackwards(fInfo.methodToStart.method().getActiveBody(), workingUnit, newStmtSwitch);
										Set<Info> initValues = newStmtSwitch.getResultInfos();

										if(initValues.size() > 0) {
											List<Info> listInfo = initValues.stream().collect(Collectors.toList());
											if(listInfo.indexOf(newInfo) == -1){
												continue;
											}
											Info initInfo = listInfo.get(listInfo.indexOf(newInfo));
											diaInfo.addMessage(((DialogInfo)initInfo).getMessage());
											diaInfo.setMessageReg("");
										}
									}
								}
							}
						}else{
							Helper.saveToStatisticalFile("Error DialogSwitch: Doesn't find messageTextReg in initializationOfField: " + stmt);
						}
					}else{
						diaInfo.setMessageReg(helpMethods.getRightRegOfAssignStmt(stmt));
					}
				}
				if (leftReg.equals(diaInfo.getItemTextsArrayIDReg())){
					diaInfo.setItemTextsArrayIDReg("");
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
							for (FieldInfo fInfo : fInfos){
								if(Thread.currentThread().isInterrupted()){
									return;
								}
								if(fInfo.value != null){
									diaInfo.addItemTextsArrayID(fInfo.value);
									continue;
								}else{
									if(fInfo.methodToStart != null && fInfo.methodToStart.method().hasActiveBody()){
										
										Unit workingUnit = fInfo.unitToStart;
										DialogInfo newInfo = new DialogInfo("");
										newInfo.setItemTextsArrayIDReg(fInfo.register.getName());
										StmtSwitchForDialogs newStmtSwitch = new StmtSwitchForDialogs(newInfo, getCurrentSootMethod());
										previousFields.forEach(x->newStmtSwitch.addPreviousField(x));
										iteratorHelper.runOverToFindSpecValuesBackwards(fInfo.methodToStart.method().getActiveBody(), workingUnit, newStmtSwitch);
										Set<Info> initValues = newStmtSwitch.getResultInfos();

										if(initValues.size() > 0) {
											List<Info> listInfo = initValues.stream().collect(Collectors.toList());
											if(listInfo.indexOf(newInfo) == -1){
												continue;
											}
											Info initInfo = listInfo.get(listInfo.indexOf(newInfo));
											diaInfo.addItemTextsArrayID(((DialogInfo)initInfo).getItemTextsArrayID());
										}
									}
								}
							}
						}else{
							Helper.saveToStatisticalFile("Error DialogSwitch: Doesn't find ItemTextsArrayIDReg in initializationOfField: " + stmt);
						}
					}else{
						diaInfo.setItemTextsArrayIDReg(helpMethods.getRightRegOfAssignStmt(stmt));
					}
				}
			}
			
			
			if (toRemoveInfos.size() > 0){
				removeAllFromResultInfos(toRemoveInfos);
			}
			addAllToResultInfo(toAddInfos);
		}
	}
	
	private Listener getListener(String listenerClass){
		Listener l = new Listener("onClick", false, subSignatureOnClick, getCurrentSootMethod().getDeclaringClass().getName());
		l.setListenerClass(listenerClass);
		return l;
	}
	
//	@Override
//	public boolean run(){
//		if (diaInfo != null)
//			return diaInfo.allValuesFound();
//		else
//			return false;
//	}
//	
//	@Override
//	public void init(){
//		super.init();
//		diaInfo = null;
//		resultInfo = null;
//		shouldBreak = false;
//	}

}
