package st.cs.uni.saarland.de.searchPreferences;

import st.cs.uni.saarland.de.helpClasses.Helper;
import st.cs.uni.saarland.de.helpClasses.Info;
import st.cs.uni.saarland.de.helpClasses.InterProcInfo;
import st.cs.uni.saarland.de.helpClasses.MyStmtSwitch;
import st.cs.uni.saarland.de.entities.FieldInfo;
import soot.SootField;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.*;

import java.util.*;
import java.util.stream.Collectors;

public class StmtSwitchForPreferences extends MyStmtSwitch {
    //TODO: search for addPreferencesFromResource(get the layout id), then mark the activity of interest in the exisiting xml representation

    //then we want to add it like any other layout thingie
    //then we want to go through all its children and update their assigned activity

    //TODO Deal with the dynamic preferences and associate callback onPreferenceChange
    //TODO Deal with later apis
    public StmtSwitchForPreferences(SootMethod currentSootMethod) {
        super(currentSootMethod);
    }

    public void caseIdentityStmt(IdentityStmt stmt){
        if(Thread.currentThread().isInterrupted()){
			return;
		}

    }

    public void caseAssignStmt(AssignStmt stmt){
        if(Thread.currentThread().isInterrupted()){
			return;
		}
        Set<Info> toAddInfos = new HashSet<>();
        for (Info info: getResultInfos()) {
            PreferenceInfo pInfo = (PreferenceInfo)info;
            String leftReg = helpMethods.getLeftRegOfAssignStmt(stmt);

            if(pInfo.getLayoutIDReg().equals(leftReg)){
                if (stmt.containsInvokeExpr()){
					pInfo.setLayoutIDReg("");
					InvokeExpr invokeExpr = stmt.getInvokeExpr();
					 if (!Helper.isClassInSystemPackage(helpMethods.getCallerTypeOfInvokeStmt(invokeExpr))){
						 List<InterProcInfo> resList = interprocMethods2.findReturnValueInMethod2(stmt);
						 if (resList.size() > 0){
							 pInfo.setLayoutID(resList.get(0).getValueOfSearchedReg());
							 if (resList.size() > 1){
								for (int j = 1; j < resList.size() ; j++){
									InterProcInfo workingInfo = (InterProcInfo) resList.get(j);
									PreferenceInfo newInfo = (PreferenceInfo) pInfo.clone();
									toAddInfos.add(newInfo);
									newInfo.setLayoutID(workingInfo.getValueOfSearchedReg());
								}
							}
						}
				}
                
            }
            else if (stmt.getRightOp() instanceof FieldRef){

            }
            else{
                pInfo.setLayoutIDReg("");
                String rightReg = helpMethods.getRightRegOfAssignStmt(stmt);
                if (checkMethods.checkIfValueIsID(rightReg)){
                    pInfo.setLayoutID(rightReg);
                }else
                    pInfo.setLayoutIDReg(rightReg);
            }
        }

    }
    addAllToResultInfo(toAddInfos);
}

    public void caseInvokeStmt(InvokeStmt stmt){
        if(Thread.currentThread().isInterrupted()){
			return;
		}
        InvokeExpr invokeExpr = stmt.getInvokeExpr();
		String method_name = helpMethods.getMethodNameOfInvokeStmt(invokeExpr);
		String methodSignature = helpMethods.getSignatureOfInvokeExpr(invokeExpr);
        
        //Todo double check it's the right method not just something with the same signature
        if("addPreferencesFromResource".equals(method_name) && invokeExpr.getArgCount() == 1){
            String parameterReg = helpMethods.getParameterOfInvokeStmt(invokeExpr, 0);
            String activityName = helpMethods.getCallerTypeOfInvokeStmt(invokeExpr);

            PreferenceInfo preferenceInfo = new PreferenceInfo("");
            preferenceInfo.setActivityName(activityName);
            //if the param is int which it's probably is, we can set the ide?
            if(invokeExpr.getArg(0) instanceof IntConstant) {
                preferenceInfo.setLayoutID(parameterReg);
                preferenceInfo.setDeclaringSootClass(activityName);
                preferenceInfo.setActivityName(activityName);
            }
            else{
                preferenceInfo.setLayoutIDReg(parameterReg);
            }
            addToResultInfo(preferenceInfo); 
        
        }
		
    }

}
