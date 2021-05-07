package st.cs.uni.saarland.de.searchListener;

import soot.SootField;
import soot.SootMethod;
import soot.Unit;
import soot.VoidType;
import soot.jimple.*;
import st.cs.uni.saarland.de.entities.FieldInfo;
import st.cs.uni.saarland.de.helpClasses.Helper;
import st.cs.uni.saarland.de.helpClasses.Info;
import st.cs.uni.saarland.de.helpClasses.MyStmtSwitch;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

// at the moment for getItem method in PagerAdapter class
public class StmtSwitchForPagerAdapter extends MyStmtSwitch {
	
// if 2 return stmt are in one method, maybe because of a if stmt, then Soot resolves it and writes two return stmts
//	label3:
//        r3 = new com.example.Testapp.Fragment2ViewPager;
//        specialinvoke r3.<com.example.Testapp.Fragment2ViewPager: void <init>()>();
//        return r3;
	
//	private PagerAdapterInfo pInfo = null;

	
//	public void caseIdentityStmt(IdentityStmt stmt){
//		//System.out.println("identity: " + stmt);
//	}
//	
//	public void caseInvokeStmt(InvokeStmt stmt) {
//		//System.out.println("invoke: " + stmt);
//	}
	
	
	
	public StmtSwitchForPagerAdapter(PagerAdapterInfo newInfo, SootMethod currentSootMethod) {
		super(newInfo, currentSootMethod);
	}
	
	public StmtSwitchForPagerAdapter(SootMethod currentSootMethod) {
		super(currentSootMethod);
	}

	public void caseAssignStmt(final AssignStmt stmt){
		if(Thread.currentThread().isInterrupted()){
			return;
		}
		//Set<Info> resultInfos = getResultInfos();
		Set<Info> toAddInfos = new LinkedHashSet<>();
		Set<Info> toRemoveInfos = new LinkedHashSet<>();
		for (Info i : getResultInfos()){
			if(Thread.currentThread().isInterrupted()){
				return;
			}
			PagerAdapterInfo pInfo = (PagerAdapterInfo) i;
			
	//		 r3 = new com.example.Testapp.Fragment2ViewPager;
			String leftReg = helpMethods.getLeftRegOfAssignStmt(stmt);
			if (leftReg.equals(pInfo.getSearchedEReg())){
				if(stmt.getRightOp() instanceof ClassConstant){
					pInfo.setSearchedEReg("");
					pInfo.setFragmentClass(((ClassConstant)stmt.getRightOp()).getValue().replace("/", "."));
					shouldBreak = true;
				}
				else if (stmt.getRightOp() instanceof NewExpr){
					pInfo.setSearchedEReg("");
					pInfo.setFragmentClass(helpMethods.getTypeOfRightRegOfAssignStmt(stmt));
					shouldBreak = true;
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
						toRemoveInfos.add(pInfo);
						for (FieldInfo fInfo : fInfos){
							if(Thread.currentThread().isInterrupted()){
								return;
							}
							if(fInfo.methodToStart != null && fInfo.methodToStart != null && fInfo.methodToStart.method().hasActiveBody()){
								Unit workingUnit = fInfo.unitToStart;
								PagerAdapterInfo newInfo = new PagerAdapterInfo("");
								newInfo.setSearchedEReg(fInfo.register.getName());
								StmtSwitchForPagerAdapter newStmtSwitch = new StmtSwitchForPagerAdapter(newInfo, getCurrentSootMethod());
								previousFields.forEach(x->newStmtSwitch.addPreviousField(x));
								iteratorHelper.runOverToFindSpecValuesBackwards(fInfo.methodToStart.method().getActiveBody(), workingUnit, newStmtSwitch);
								Set<Info> initValues = newStmtSwitch.getResultInfos();

								if(initValues.size() > 0) {
									List<Info> listInfo = initValues.stream().collect(Collectors.toList());
									if(listInfo.indexOf(newInfo) == -1){
										continue;
									}
									Info initInfo = listInfo.get(listInfo.indexOf(newInfo));
									PagerAdapterInfo newInfo2 = (PagerAdapterInfo) pInfo.clone();
									newInfo2.setSearchedEReg("");
									newInfo2.setFragmentClass(((PagerAdapterInfo)initInfo).getFragmentClass());
									toAddInfos.add(newInfo2);
								}
							}
						}
					}else{
						pInfo.setSearchedEReg("");
						Helper.saveToStatisticalFile("Error PagerSwitch: Doesn't find searchedReg in initializationOfField");
					}
				}else if (stmt.containsInvokeExpr()){
					//search for fragment class

					pInfo.setSearchedEReg("");
					InvokeExpr invokeExpr = stmt.getInvokeExpr();
					if(invokeExpr.getMethod().getSignature().equals("<android.support.v4.app.Fragment: android.support.v4.app.Fragment instantiate(android.content.Context,java.lang.String,android.os.Bundle)>")){
						pInfo.setSearchedEReg(invokeExpr.getArg(1).toString());
					}
					else if(invokeExpr.getMethod().getSignature().equals("<java.lang.Class: java.lang.String getName()>")){
						pInfo.setSearchedEReg(invokeExpr.getUseBoxes().get(invokeExpr.getUseBoxes().size() - 1).getValue().toString());
					}
					else{
						//unknown method. just stop
						shouldBreak = true;
					}

				}else{
					pInfo.setSearchedEReg(helpMethods.getRightRegOfAssignStmt(stmt));
				}
			}
		}		
		
		
		if (toRemoveInfos.size() > 0){
			removeAllFromResultInfos(toRemoveInfos);
		}
		
		addAllToResultInfo(toAddInfos);
	}
	
	public void caseReturnStmt(final ReturnStmt stmt) {
		if(Thread.currentThread().isInterrupted()){
			return;
		}
		String returnReg = helpMethods.getReturnRegOfReturnStmt(stmt);
		if (!(stmt.getOp() instanceof NullConstant || stmt.getOp() instanceof VoidType)){
			PagerAdapterInfo pInfo = new PagerAdapterInfo(returnReg);
			addToResultInfo(pInfo);
			if(!stmt.getOp().getType().toString().equals("android.support.v4.app.Fragment")){
				pInfo.setSearchedEReg("");
				pInfo.setFragmentClass(stmt.getOp().getType().toString());
				shouldBreak = true;
			}
		}else{
			shouldBreak = true;
		}
	}

}
