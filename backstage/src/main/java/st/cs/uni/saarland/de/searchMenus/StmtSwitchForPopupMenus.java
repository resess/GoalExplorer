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
import st.cs.uni.saarland.de.testApps.Content;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class StmtSwitchForPopupMenus extends MyStmtSwitch {

//	private PopupMenuInfo mInfo;
	private String tmpInitMethodAct = "";
	private String tmpInitMethodShowingItemReg = "";
	private String callerInitMethod = "";
	
//	 $r0 := @this: com.example.Testapp.MainActivity2;
//	$r1 := @parameter0: android.view.View;
//	r5 = new android.widget.PopupMenu;
//	r6 = (android.content.Context) $r0;
//	specialinvoke r5.<android.widget.PopupMenu: void <init>(android.content.Context,android.view.View)>(r6, $r1);
//	r7 = (android.widget.PopupMenu$OnMenuItemClickListener) $r0;
//	virtualinvoke r5.<android.widget.PopupMenu: void setOnMenuItemClickListener(android.widget.PopupMenu$OnMenuItemClickListener)>(r7);
//	$r2 = virtualinvoke r5.<android.widget.PopupMenu: android.view.MenuInflater getMenuInflater()>();
//	$r4 = virtualinvoke r5.<android.widget.PopupMenu: android.view.Menu getMenu()>();
//	virtualinvoke $r2.<android.view.MenuInflater: void inflate(int,android.view.Menu)>(2131099651, $r4);
//	virtualinvoke r5.<android.widget.PopupMenu: void show()>();
	
	
//	<android.widget.PopupMenu: void <init>(android.content.Context,android.view.View)>
//	<android.widget.PopupMenu: void setOnMenuItemClickListener(android.widget.PopupMenu$OnMenuItemClickListener)>
//	<android.widget.PopupMenu: android.view.MenuInflater getMenuInflater()>
//	<android.widget.PopupMenu: android.view.Menu getMenu()>
//	<android.view.MenuInflater: void inflate(int,android.view.Menu)>
//	<android.widget.PopupMenu: void show()>
	
	// show is not included because this PopupMenu could get a show command from anywhere
	
	public StmtSwitchForPopupMenus(PopupMenuInfo newInfo, SootMethod currentSootMethod) {
		super(newInfo, currentSootMethod);
	}


	public StmtSwitchForPopupMenus(SootMethod currentSootMethod) {
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
			PopupMenuInfo mInfo = (PopupMenuInfo) i;
			
			if (stmt.getRightOp() instanceof ParameterRef){
				String leftReg = helpMethods.getLeftRegOfIdentityStmt(stmt);
				if (leftReg.equals(mInfo.getActivityReg())){
					mInfo.setActivityReg("");
					String actName = helpMethods.getRightClassTypeOfIdentityStmt(stmt);
					if (checkMethods.checkIfValueIsVariable(actName)){
						int paramIndex = ((ParameterRef)stmt.getRightOp()).getIndex();
						List<Info> resList = interprocMethods2.findInReachableMethods2(paramIndex,  getCurrentSootMethod(), new ArrayList<SootMethod>());
						if (resList.size() > 0){
							mInfo.setActivityClassName(((InterProcInfo)resList.get(0)).getClassOfSearchedReg());
							if (resList.size() > 1){
								for (int j = 1; j < resList.size() ; j++){
									InterProcInfo workingInfo = (InterProcInfo) resList.get(j);
									PopupMenuInfo newInfo = (PopupMenuInfo) mInfo.clone();
									toAddInfos.add(newInfo);
									newInfo.setActivityClassName(workingInfo.getClassOfSearchedReg());
								}
							}
						}
					}else{
						mInfo.setActivityClassName(actName);
					}
				}
				if ((leftReg.equals(mInfo.getShowingItemReg()) && (mInfo.getShowingItemID().equals("")))){
					mInfo.setShowingItemReg("");
					int paramIndex = ((ParameterRef)stmt.getRightOp()).getIndex();
					List<Info> resList = interprocMethods2.findInReachableMethods2(paramIndex,  getCurrentSootMethod(), new ArrayList<SootMethod>());
					if (resList.size() > 0){
						mInfo.setShowingItemID(((InterProcInfo)resList.get(0)).getValueOfSearchedReg());
						if (resList.size() > 1){
							for (int j = 1; j < resList.size() ; j++){
								InterProcInfo workingInfo = (InterProcInfo) resList.get(j);
								PopupMenuInfo newInfo = (PopupMenuInfo) mInfo.clone();
								toAddInfos.add(newInfo);
								newInfo.setShowingItemID(workingInfo.getValueOfSearchedReg());
							}
						}
					}
				}
				if (leftReg.equals(tmpInitMethodAct)){
					tmpInitMethodAct = "";
					String actName = helpMethods.getRightClassTypeOfIdentityStmt(stmt);
					if (checkMethods.checkIfValueIsVariable(actName)){
						int paramIndex = ((ParameterRef)stmt.getRightOp()).getIndex();
						List<Info> resList = interprocMethods2.findInReachableMethods2(paramIndex,  getCurrentSootMethod(), new ArrayList<SootMethod>());
						if (resList.size() > 0){
							tmpInitMethodAct = ((InterProcInfo)resList.get(0)).getClassOfSearchedReg();
							if (resList.size() > 1){
								for (int j = 1; j < resList.size() ; j++){
									InterProcInfo workingInfo = (InterProcInfo) resList.get(j);
									if (!workingInfo.getClassOfSearchedReg().equals("")){
										tmpInitMethodAct = tmpInitMethodAct + "#" + workingInfo.getClassOfSearchedReg();
										if (tmpInitMethodAct.startsWith("#"))
											tmpInitMethodAct = tmpInitMethodAct.replaceFirst("#", "");
									}
								}
							}
						}
					}else{
						tmpInitMethodAct = actName;
					}
				}
				
				Listener listener = mInfo.getListenerWRegs().get(leftReg);
				if (listener != null){
					mInfo.getListenerWRegs().remove(leftReg);
					String actName = helpMethods.getRightClassTypeOfIdentityStmt(stmt);
					if (checkMethods.checkIfValueIsVariable(actName) || Helper.isClassInSystemPackage(actName)){
						int paramIndex = ((ParameterRef)stmt.getRightOp()).getIndex();
						List<Info> resList = interprocMethods2.findInReachableMethods2(paramIndex,  getCurrentSootMethod(), new ArrayList<SootMethod>());
						if (resList.size() > 0){
							listener.setListenerClass(((InterProcInfo)resList.get(0)).getClassOfSearchedReg());
							if (resList.size() > 1){
								for (int j = 1; j < resList.size() ; j++){
									InterProcInfo workingInfo = (InterProcInfo) resList.get(j);
									Listener l = new Listener("onMenuItemClick" , false, "boolean onMenuItemClick(android.view.MenuItem)", getCurrentSootMethod().getDeclaringClass().getName());
									l.setListenerClass(workingInfo.getClassOfSearchedReg());
									mInfo.addListenerWRegs(String.valueOf(Content.getInstance().getNewUniqueID()), l);
								}
							}
						}
					}else{
						listener.setListenerClass(actName);
					}
					mInfo.addListenerWRegs(Integer.toString(Content.getInstance().getNewUniqueID()), listener);
				}
			}else if (stmt.getRightOp() instanceof ThisRef){
				String leftReg = helpMethods.getLeftRegOfIdentityStmt(stmt);
				String rightRegType = helpMethods.getRightClassTypeOfIdentityStmt(stmt);
				if (leftReg.equals(mInfo.getActivityReg())){
					
					mInfo.setActivityReg("");
					mInfo.setActivityClassName(rightRegType);
				}
//				if ((leftReg.equals(mInfo.getShowingItemReg()) && (mInfo.getShowingItemID().equals("")))){
//					if (stmt.toString().contains("@parameter")){
//						mInfo.setShowingItemReg("Item is passed in parameters");
//					}
//				}
				if (leftReg.equals(tmpInitMethodAct)){
					tmpInitMethodAct = rightRegType;
				}
				
				Listener listener = mInfo.getListenerWRegs().get(leftReg);
				if (listener != null){
					mInfo.getListenerWRegs().remove(leftReg);
					listener.setListenerClass(helpMethods.getRightClassTypeOfIdentityStmt(stmt));
					mInfo.addListenerWRegs(Integer.toString(Content.getInstance().getNewUniqueID()), listener);
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

//			virtualinvoke $r2.<android.view.MenuInflater: void inflate(int,android.view.Menu)>(2131099651, $r4);
			if (methodSignature.equals("<android.view.MenuInflater: void inflate(int,android.view.Menu)>")){
				String layoutId = helpMethods.getParameterOfInvokeStmt(invokeExpr, 0);
				String layoutIdReg = "";
				if (!checkMethods.checkIfValueIsID(layoutId)){					
					layoutIdReg = layoutId;
					layoutId = "";
				}
				PopupMenuInfo mInfo = new PopupMenuInfo(layoutId, helpMethods.getCallerOfInvokeStmt(invokeExpr), getCurrentSootMethod().getDeclaringClass().getName());
				mInfo.setLayoutIDReg(layoutIdReg);
				addToResultInfo(mInfo);
			}
			Set<Info> resultInfos = getResultInfos();
			for (Info i : resultInfos){
				if(Thread.currentThread().isInterrupted()){
					return;
				}
				PopupMenuInfo mInfo = (PopupMenuInfo) i;
				
	//			specialinvoke r5.<android.widget.PopupMenu: void <init>(android.content.Context,android.view.View)>(r6, $r1);
				if (methodSignature.equals("<android.widget.PopupMenu: void <init>(android.content.Context,android.view.View)>")){
					if (mInfo.getSearchedEReg().equals(helpMethods.getCallerOfInvokeStmt(invokeExpr))){
						String actName = helpMethods.getCallerOfInvokeStmt(invokeExpr);
						if (checkMethods.checkIfValueIsVariable(actName)){
							mInfo.setActivityReg(actName);
						}else{
							mInfo.setActivityClassName(actName);
						}
						mInfo.setShowingItemReg(helpMethods.getParameterOfInvokeStmt(invokeExpr, 1));
					}else{
						callerInitMethod = helpMethods.getCallerOfInvokeStmt(invokeExpr);
						tmpInitMethodAct = helpMethods.getParameterOfInvokeStmt(invokeExpr, 0);
						tmpInitMethodShowingItemReg = helpMethods.getParameterOfInvokeStmt(invokeExpr, 1);
					}
				}else
	//				virtualinvoke r5.<android.widget.PopupMenu: void setOnMenuItemClickListener(android.widget.PopupMenu$OnMenuItemClickListener)>(r7);
				if (methodSignature.equals("<android.widget.PopupMenu: void setOnMenuItemClickListener(android.widget.PopupMenu$OnMenuItemClickListener)>")){
					if (helpMethods.getCallerOfInvokeStmt(invokeExpr).equals(mInfo.getSearchedEReg())){
						Listener l = new Listener("onMenuItemClick" , false, "boolean onMenuItemClick(android.view.MenuItem)", getCurrentSootMethod().getDeclaringClass().getName());
						String listClassName = helpMethods.getParameterTypeOfInvokeStmt(invokeExpr, 0);
						if (checkMethods.checkIfValueIsVariable(listClassName)){
							String listenerReg = helpMethods.getParameterOfInvokeStmt(invokeExpr, 0);
							mInfo.addListenerWRegs(listenerReg, l);
						}else{
							l.setListenerClass(listClassName);
							mInfo.addListenerWRegs(Integer.toString(Content.getInstance().getNewUniqueID()), l);
						}
						
					}
					
				}else if (methodSignature.equals("<android.widget.PopupMenu: void setOnDismissListener(android.widget.PopupMenu$OnDismissListener)>")){
					if (helpMethods.getCallerOfInvokeStmt(invokeExpr).equals(mInfo.getShowingItemReg())){
						Listener l = new Listener("onDismiss" , false, "boolean onDismiss(android.widget.PopupMenu)", getCurrentSootMethod().getDeclaringClass().getName());
						String listenerReg = helpMethods.getParameterOfInvokeStmt(invokeExpr, 0);
						mInfo.addListenerWRegs(listenerReg, l);
					}		
				}
		}
	}
	
	public void caseAssignStmt(final AssignStmt stmt){
		if(Thread.currentThread().isInterrupted()){
			return;
		}
		Set<Info> toAddInfos = new LinkedHashSet<>();
		Set<Info> toRemoveInfos = new LinkedHashSet<>();
		for (Info i : getResultInfos()){
			if(Thread.currentThread().isInterrupted()){
				return;
			}
			PopupMenuInfo mInfo = (PopupMenuInfo) i;
			
			String leftReg = helpMethods.getLeftRegOfAssignStmt(stmt);
			
			if (stmt.containsInvokeExpr()){
				InvokeExpr invokeExpr = stmt.getInvokeExpr();
				String methodSignature = helpMethods.getSignatureOfInvokeExpr(invokeExpr);

//				$r2 = virtualinvoke r5.<android.widget.PopupMenu: android.view.MenuInflater getMenuInflater()>();
				if (methodSignature.equals("<android.widget.PopupMenu: android.view.MenuInflater getMenuInflater()>")){
					if (leftReg.equals(mInfo.getInflaterReg())){
						mInfo.setInflaterReg("");
						mInfo.setSearchedEReg(helpMethods.getCallerOfInvokeStmt(invokeExpr));
					}
				}else
//					$r1 = virtualinvoke $r0.<com.example.SendSms.SendSmsFromAdress: android.view.View findViewById(int)>(2131034115)
					if (methodSignature.equals("<android.app.Activity: android.view.View findViewById(int)>")){
						if (leftReg.equals(mInfo.getShowingItemReg())){
							mInfo.setShowingItemReg("");
							mInfo.setShowingItemID(helpMethods.getParameterOfInvokeStmt(invokeExpr, 0));
						}else if (leftReg.equals(tmpInitMethodShowingItemReg)){
							tmpInitMethodShowingItemReg = helpMethods.getParameterOfInvokeStmt(invokeExpr, 0);
						}
				}else
					// check interproc calls
						if (leftReg.equals(mInfo.getLayoutIDReg())){
							mInfo.setLayoutIDReg("");
							if (!Helper.isClassInSystemPackage(helpMethods.getCallerTypeOfInvokeStmt(invokeExpr))) {
								List<InterProcInfo> resList = interprocMethods2.findReturnValueInMethod2(stmt);
								if (resList.size() > 0) {
									mInfo.setLayoutID(((InterProcInfo) resList.get(0)).getValueOfSearchedReg());
									for (int j = 1; j < resList.size(); j++) {
										InterProcInfo workingInfo = (InterProcInfo) resList.get(j);
										PopupMenuInfo newInfo = (PopupMenuInfo) mInfo.clone();
										toAddInfos.add(newInfo);
										newInfo.setLayoutID(workingInfo.getValueOfSearchedReg());
									}
								}
							}
														
						}else if (leftReg.equals(mInfo.getShowingItemReg())){
							mInfo.setShowingItemReg("");
							if (!Helper.isClassInSystemPackage(helpMethods.getCallerTypeOfInvokeStmt(invokeExpr))) {
								List<InterProcInfo> resList = interprocMethods2.findReturnValueInMethod2(stmt);
								if (resList.size() > 0) {
									mInfo.setShowingItemID(((InterProcInfo) resList.get(0)).getValueOfSearchedReg());
									if (resList.size() > 1) {
										for (int j = 1; j < resList.size(); j++) {
											InterProcInfo workingInfo = (InterProcInfo) resList.get(j);
											PopupMenuInfo newInfo = (PopupMenuInfo) mInfo.clone();
											toAddInfos.add(newInfo);
											newInfo.setShowingItemID(workingInfo.getValueOfSearchedReg());
										}
									}
								}
							}
						}						
						else if (mInfo.getListenerWRegs().containsKey(leftReg)){
							Listener listener = mInfo.getListenerWRegs().get(leftReg);
							mInfo.getListenerWRegs().remove(leftReg);
							if (!Helper.isClassInSystemPackage(helpMethods.getCallerTypeOfInvokeStmt(invokeExpr))) {
								List<InterProcInfo> resList = interprocMethods2.findReturnValueInMethod2(stmt);
								if (resList.size() > 0) {
									listener.setListenerClass(((InterProcInfo) resList.get(0)).getClassOfSearchedReg());
									if (resList.size() > 1) {
										for (int j = 1; j < resList.size(); j++) {
											InterProcInfo workingInfo = (InterProcInfo) resList.get(j);
											Listener newListener = new Listener("onMenuItemClick", false, "boolean onMenuItemClick(android.view.MenuItem)", getCurrentSootMethod().getDeclaringClass().getName());
											newListener.setListenerClass(workingInfo.getClassOfSearchedReg());
											mInfo.addListenerWRegs(String.valueOf(Content.getInstance().getNewUniqueID()), newListener);
										}
									}
								}
								// TODO check hashFun as key
								mInfo.getListenerWRegs().put(String.valueOf(listener.hashCode()), listener);
							}
						}
				
			}else{
//				r7 = (android.widget.PopupMenu$OnMenuItemClickListener) $r0;
				if (leftReg.equals(mInfo.getShowingItemReg())){						
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
							for (FieldInfo fInfo: fInfos){
								if(Thread.currentThread().isInterrupted()){
									return;
								}
								if(fInfo.value != null){
									PopupMenuInfo newInfo = (PopupMenuInfo) mInfo.clone();
									newInfo.setShowingItemID(fInfo.value);
									newInfo.setShowingItemReg("");
									toAddInfos.add(newInfo);
									continue;
								}else{
									if(fInfo.methodToStart != null && fInfo.methodToStart.method().hasActiveBody()){
										Unit workingUnit = fInfo.unitToStart;
										PopupMenuInfo newInfo = new PopupMenuInfo("", "", getCurrentSootMethod().getDeclaringClass().getName());
										newInfo.setShowingItemReg(fInfo.register.getName());
										StmtSwitchForPopupMenus newStmtSwitch = new StmtSwitchForPopupMenus(newInfo, getCurrentSootMethod());
										previousFields.forEach(x->newStmtSwitch.addPreviousField(x));
										iteratorHelper.runOverToFindSpecValuesBackwards(fInfo.methodToStart.method().getActiveBody(), workingUnit, newStmtSwitch);
										Set<Info> initValues = newStmtSwitch.getResultInfos();

										if(initValues.size() > 0) {
											List<Info> listInfo = initValues.stream().collect(Collectors.toList());
											if(listInfo.indexOf(newInfo) == -1){
												continue;
											}
											Info initInfo = listInfo.get(listInfo.indexOf(newInfo));
											PopupMenuInfo newInfo2 = (PopupMenuInfo) mInfo.clone();
											newInfo2.setShowingItemID(((PopupMenuInfo)initInfo).getShowingItemID());
											newInfo2.setShowingItemReg("");
											toAddInfos.add(newInfo2);
										}
									}
								}
							}
						}else{
							mInfo.setShowingItemReg("");
							Helper.saveToStatisticalFile("Error PopupMenuSwitch: Doesn't find showingItemIdReg in initializationOfField: " + stmt);
						}
					}else{
						mInfo.setShowingItemReg(helpMethods.getRightRegOfAssignStmt(stmt));
					}
				}
				if (leftReg.equals(tmpInitMethodAct)){
					tmpInitMethodAct = "";
					if (stmt.getRightOp() instanceof NewExpr){
						tmpInitMethodAct = helpMethods.getRightRegOfAssignStmt(stmt);
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
							for (FieldInfo fInfo : fInfos){
								if(Thread.currentThread().isInterrupted()){
									return;
								}
								if(fInfo.methodToStart != null && fInfo.methodToStart.method().hasActiveBody()){
									Unit workingUnit = fInfo.unitToStart;
									PopupMenuInfo newInfo = new PopupMenuInfo("", "", getCurrentSootMethod().getDeclaringClass().getName());
									StmtSwitchForPopupMenus newStmtSwitch = new StmtSwitchForPopupMenus(newInfo, getCurrentSootMethod());
									newStmtSwitch.tmpInitMethodAct = fInfo.register.getName();
									previousFields.forEach(x->newStmtSwitch.addPreviousField(x));
									iteratorHelper.runOverToFindSpecValuesBackwards(fInfo.methodToStart.method().getActiveBody(), workingUnit, newStmtSwitch);
									tmpInitMethodAct = newStmtSwitch.tmpInitMethodAct;									
								}
							}
						}else{
							Helper.saveToStatisticalFile("Error PopupMenuSwitch: Doesn't find tmpintiMethodAct in initializationOfField: " + stmt);
						}
					}else{
						tmpInitMethodAct = helpMethods.getRightRegOfAssignStmt(stmt);
					}
				}
				if (leftReg.equals(tmpInitMethodShowingItemReg)){
					tmpInitMethodShowingItemReg = "";
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
								if(fInfo.methodToStart != null && fInfo.methodToStart.method().hasActiveBody()){
									Unit workingUnit = fInfo.unitToStart;
									PopupMenuInfo newInfo = new PopupMenuInfo("", "", getCurrentSootMethod().getDeclaringClass().getName());
									StmtSwitchForPopupMenus newStmtSwitch = new StmtSwitchForPopupMenus(newInfo, getCurrentSootMethod());
									newStmtSwitch.tmpInitMethodShowingItemReg = fInfo.register.getName();
									previousFields.forEach(x->newStmtSwitch.addPreviousField(x));
									iteratorHelper.runOverToFindSpecValuesBackwards(fInfo.methodToStart.method().getActiveBody(), workingUnit, newStmtSwitch);
									tmpInitMethodShowingItemReg = newStmtSwitch.tmpInitMethodShowingItemReg;									
								}
							}
						}else{
							Helper.saveToStatisticalFile("Error PopupMenuSwitch: Doesn't find tmpInitMethodShowingItemReg in initializationOfField: " + stmt);
						}
					}else{
						tmpInitMethodShowingItemReg = helpMethods.getRightRegOfAssignStmt(stmt);
					}
				}
				if (leftReg.equals(mInfo.getLayoutIDReg())){
					
					String rightReg = helpMethods.getRightRegOfAssignStmt(stmt);
					
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
							for (FieldInfo fInfo: fInfos){
								if(Thread.currentThread().isInterrupted()){
									return;
								}
								if(fInfo.value != null){
									PopupMenuInfo newInfo = (PopupMenuInfo) mInfo.clone();
									newInfo.setLayoutID(fInfo.value);
									newInfo.setLayoutIDReg("");
									toAddInfos.add(newInfo);
									continue;
								}else{
									if(fInfo.methodToStart != null && fInfo.methodToStart.method().hasActiveBody()){
										Unit workingUnit = fInfo.unitToStart;
										PopupMenuInfo newInfo = new PopupMenuInfo("", "", getCurrentSootMethod().getDeclaringClass().getName());
										newInfo.setLayoutIDReg(fInfo.register.getName());
										StmtSwitchForPopupMenus newStmtSwitch = new StmtSwitchForPopupMenus(newInfo, getCurrentSootMethod());
										previousFields.forEach(x->newStmtSwitch.addPreviousField(x));
										iteratorHelper.runOverToFindSpecValuesBackwards(fInfo.methodToStart.method().getActiveBody(), workingUnit, newStmtSwitch);
										Set<Info> initValues = newStmtSwitch.getResultInfos();

										if(initValues.size() > 0) {
											List<Info> listInfo = initValues.stream().collect(Collectors.toList());
											if(listInfo.indexOf(newInfo) == -1){
												continue;
											}
											Info initInfo = listInfo.get(listInfo.indexOf(newInfo));
											PopupMenuInfo newInfo2 = (PopupMenuInfo) mInfo.clone();
											newInfo2.setLayoutID(((PopupMenuInfo)initInfo).getLayoutID());
											newInfo2.setLayoutIDReg("");
											toAddInfos.add(newInfo2);
										}
									}
								}
							}
						}else{
							mInfo.setLayoutIDReg("");
							Helper.saveToStatisticalFile("Error PopupMenuSwitch: Doesn't find layoutIdReg in initializationOfField: " + stmt);
						}
					}else if (stmt.getRightOp() instanceof IntConstant){
						mInfo.setLayoutIDReg("");
						mInfo.setLayoutID(rightReg);
					}else{
						mInfo.setLayoutIDReg(rightReg);
					}
				}
				if (leftReg.equals(mInfo.getSearchedEReg())){
					
					String rightReg = helpMethods.getRightRegOfAssignStmt(stmt);
					if (rightReg.equals(callerInitMethod)){
						mInfo.setSearchedEReg("");
						if (tmpInitMethodAct.contains("#")){
							String[] actNames = tmpInitMethodAct.split("#");
							if (actNames.length > 1){
								processInitTmpMethodAct(mInfo, tmpInitMethodAct);
								for (int j = 1; j < actNames.length ; j++){
									PopupMenuInfo newInfo = (PopupMenuInfo)mInfo.clone();
									toAddInfos.add(newInfo);
									processInitTmpMethodAct(newInfo, actNames[j]);
								}
							}else{
								tmpInitMethodAct.replace("#", "");
								processInitTmpMethodAct(mInfo, tmpInitMethodAct);
							}
							
						}else{
							processInitTmpMethodAct(mInfo, tmpInitMethodAct);
						}
						tmpInitMethodShowingItemReg = "";
						tmpInitMethodAct = "";
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
							toRemoveInfos.add(mInfo);
							for (FieldInfo fInfo: fInfos){
								if(Thread.currentThread().isInterrupted()){
									return;
								}
								if(fInfo.value != null){
									PopupMenuInfo newInfo = (PopupMenuInfo) mInfo.clone();
									newInfo.setLayoutID(fInfo.value);
									newInfo.setSearchedEReg("");
									toAddInfos.add(newInfo);
									continue;
								}else{
									if(fInfo.methodToStart != null && fInfo.methodToStart.method().hasActiveBody()){
										Unit workingUnit = fInfo.unitToStart;
										PopupMenuInfo newInfo = new PopupMenuInfo("", "", getCurrentSootMethod().getDeclaringClass().getName());
										newInfo.setSearchedEReg(fInfo.register.getName());
										StmtSwitchForPopupMenus newStmtSwitch = new StmtSwitchForPopupMenus(newInfo, getCurrentSootMethod());
										previousFields.forEach(x->newStmtSwitch.addPreviousField(x));
										iteratorHelper.runOverToFindSpecValuesBackwards(fInfo.methodToStart.method().getActiveBody(), workingUnit, newStmtSwitch);
										Set<Info> initValues = newStmtSwitch.getResultInfos();

										if(initValues.size() > 0) {
											List<Info> listInfo = initValues.stream().collect(Collectors.toList());
											if(listInfo.indexOf(newInfo) == -1){
												continue;
											}
											Info initInfo = listInfo.get(listInfo.indexOf(newInfo));
											PopupMenuInfo newInfo2 = (PopupMenuInfo) mInfo.clone();
											newInfo2.setLayoutID(((PopupMenuInfo)initInfo).getLayoutID());
											newInfo2.setSearchedEReg("");
											toAddInfos.add(newInfo2);
										}
									}
								}
							}
						}else{
							mInfo.setSearchedEReg("");
							Helper.saveToStatisticalFile("Error PopupMenuSwitch: Doesn't find searchedReg in initializationOfField: " + stmt);
						}
					}else{
						mInfo.setSearchedEReg(helpMethods.getRightRegOfAssignStmt(stmt));
					}
				}
				
				if (leftReg.equals(mInfo.getActivityReg())){
					
					if (stmt.getRightOp() instanceof NewExpr){
						mInfo.setActivityReg("");
						mInfo.setActivityClassName(helpMethods.getTypeOfRightRegOfAssignStmt(stmt));
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
									PopupMenuInfo newInfo = new PopupMenuInfo("", "", getCurrentSootMethod().getDeclaringClass().getName());
									newInfo.setActivityReg(fInfo.register.getName());
									StmtSwitchForPopupMenus newStmtSwitch = new StmtSwitchForPopupMenus(newInfo, getCurrentSootMethod());
									previousFields.forEach(x->newStmtSwitch.addPreviousField(x));
									iteratorHelper.runOverToFindSpecValuesBackwards(fInfo.methodToStart.method().getActiveBody(), workingUnit, newStmtSwitch);
									Set<Info> initValues = newStmtSwitch.getResultInfos();

									if(initValues.size() > 0) {
										List<Info> listInfo = initValues.stream().collect(Collectors.toList());
										if(listInfo.indexOf(newInfo) == -1){
											continue;
										}
										Info initInfo = listInfo.get(listInfo.indexOf(newInfo));
										PopupMenuInfo newInfo2 = (PopupMenuInfo) mInfo.clone();
										newInfo2.setActivityClassName(((PopupMenuInfo)initInfo).getActivityClassName());
										newInfo2.setActivityReg("");
										toAddInfos.add(newInfo2);
									}
								}
							}
						}else{
							mInfo.setActivityReg("");
							Helper.saveToStatisticalFile("Error PopupMenuSwitch: Doesn't find activityReg in initializationOfField: " + stmt);
						}
					}else{
						mInfo.setActivityReg(helpMethods.getRightRegOfAssignStmt(stmt));
					}
					
				}
				
				String rightReg = helpMethods.getRightRegOfAssignStmt(stmt);
				
//				boolean found = false;
				Listener listenerOfReg = mInfo.getListenerWRegs().get(leftReg);
				if (listenerOfReg != null){
					mInfo.getListenerWRegs().remove(leftReg);
					if (stmt.getRightOp() instanceof NewExpr){
						listenerOfReg.setListenerClass(helpMethods.getTypeOfRightRegOfAssignStmt(stmt));
						//TODO show Konstantin
						// here the hashCode of listener is set as key of the map,
							// because this entry is finished and the Reg shouldn't be searched for
							// and the hashCode is differenciate between the different listeners
						mInfo.addListenerWRegs(String.valueOf(Content.getInstance().getNewUniqueID()), listenerOfReg);
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
							for (FieldInfo fInfo : fInfos){
								if(Thread.currentThread().isInterrupted()){
									return;
								}
								if(fInfo.methodToStart != null && fInfo.methodToStart.method().hasActiveBody()){
									Unit workingUnit = fInfo.unitToStart;
									PopupMenuInfo newInfo = new PopupMenuInfo("", "", getCurrentSootMethod().getDeclaringClass().getName());
									newInfo.addListenerWRegs(fInfo.register.getName(), listenerOfReg);
									StmtSwitchForPopupMenus newStmtSwitch = new StmtSwitchForPopupMenus(newInfo, getCurrentSootMethod());
									previousFields.forEach(x->newStmtSwitch.addPreviousField(x));
									iteratorHelper.runOverToFindSpecValuesBackwards(fInfo.methodToStart.method().getActiveBody(), workingUnit, newStmtSwitch);
									Set<Info> initValues = newStmtSwitch.getResultInfos();
									if(initValues.size() > 0) {
										List<Info> listInfo = initValues.stream().collect(Collectors.toList());
										if(listInfo.indexOf(newInfo) == -1){
											continue;
										}
										Info initInfo = listInfo.get(listInfo.indexOf(newInfo));
										Listener newListener = new Listener("onMenuItemClick" , false, "boolean onMenuItemClick(android.view.MenuItem)", getCurrentSootMethod().getDeclaringClass().getName());
										newListener.setListenerClass(((PopupMenuInfo)initInfo).getActivityClassName());
										mInfo.addListenerWRegs(String.valueOf(Content.getInstance().getNewUniqueID()), newListener);
									}
								}
							}
						}
					}else{
						// create new entry and add it to the listeners
						mInfo.addListenerWRegs(rightReg, listenerOfReg);
					}
				}
			}			
		}
		
		if (toRemoveInfos.size() > 0){
			removeAllFromResultInfos(toRemoveInfos);
		}
		addAllToResultInfo(toAddInfos);
	}
	
	public void processInitTmpMethodAct(PopupMenuInfo mInfo, String tmpInitActivity){
		if (checkMethods.checkIfValueIsVariable(tmpInitActivity)){
			mInfo.setActivityReg(tmpInitActivity);
		}else{
			mInfo.setActivityClassName(tmpInitActivity);
		}
		tmpInitMethodAct = "";
		if (checkMethods.checkIfValueIsVariable(tmpInitMethodShowingItemReg)){
			mInfo.setShowingItemReg(tmpInitMethodShowingItemReg);
		}else{
			mInfo.setShowingItemID(tmpInitMethodShowingItemReg);
		}
	}
}
