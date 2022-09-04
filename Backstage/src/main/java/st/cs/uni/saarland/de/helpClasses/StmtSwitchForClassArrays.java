package st.cs.uni.saarland.de.helpClasses;

import soot.*;
import soot.jimple.*;
import st.cs.uni.saarland.de.entities.FieldInfo;
import st.cs.uni.saarland.de.helpMethods.InterprocAnalysis2;
import st.cs.uni.saarland.de.helpMethods.StmtSwitchForArrayAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

public class StmtSwitchForClassArrays extends MyStmtSwitch{
    //here we will just build the array I guess, independently from the index
    //Or maybe it should be something like array resolver
    //Then, we do something similar to ArrayAdapter
    private int arrayIndex;
    private Value arrayReg;
    private List<String> arrayResults;
    private boolean searchInReturn = false;
    private Class<ClassConstant> classInstance;

    public StmtSwitchForClassArrays(int arrIndex, Value arrRegister, SootMethod currentSootMethod){
        super(currentSootMethod);
        this.arrayIndex = arrIndex;
        this.arrayReg = arrRegister;
        this.arrayResults = new CopyOnWriteArrayList<>();
        this.classInstance = ClassConstant.class;
    }

    public List<String> getArrayResults() {
        return arrayResults;
    }

    public void setSearchInReturn(boolean value){
        searchInReturn = value;
    }

    public void caseIdentityStmt(IdentityStmt identityStmt){
        if(Thread.currentThread().isInterrupted()){
            return;
        }
        if(identityStmt.getRightOp() instanceof ParameterRef && identityStmt.getLeftOp().equals(arrayReg)){
            ParameterRef parameterRef = (ParameterRef)identityStmt.getRightOp();
            List<String> results = InterprocAnalysis2.getInstance().findClassArrayInitInReachableMethods(arrayIndex, parameterRef.getIndex(), getCurrentSootMethod(), new ArrayList<>(), searchInReturn);
            arrayResults.addAll(results);
        }
    }

    public void caseAssignStmt(AssignStmt assignStmt){
        if(Thread.currentThread().isInterrupted()){
            return;
        }
        if(!assignStmt.containsInvokeExpr()) {
            if (assignStmt.getLeftOp().equals(arrayReg)){
                if (classInstance.isInstance(assignStmt.getRightOp()))
                    arrayResults.add(((ClassConstant)assignStmt.getRightOp()).getValue());
                else {
                    //need to check if it's a field I guess?
                    if (assignStmt.getRightOp() instanceof FieldRef){
                        SootField f = ((FieldRef)assignStmt.getRightOp()).getField();
                        if(previousFields.contains(f)){
                            if(!previousFieldsForCurrentStmtSwitch.contains(f)){
                                return;
                            }
                        }else{
                            previousFields.add(f);
                            previousFieldsForCurrentStmtSwitch.add(f);
                        }
                        Set<FieldInfo> fInfos = interprocMethods2.findInitializationsOfTheField2(f, assignStmt, getCurrentSootMethod());
                        if(fInfos.size() > 0){
                            for (FieldInfo fInfo : fInfos){
                                if(Thread.currentThread().isInterrupted()){
                                    return;
                                }
                                if(fInfo.value != null){
                                    //String arrayText = "";
                                    //What to do here
                                    continue;
                                }else{
                                    if(fInfo.methodToStart != null && fInfo.methodToStart.method().hasActiveBody()){
                                        //Should be safe to overriding
                                        StmtSwitchForClassArrays newStmtSwitch = new StmtSwitchForClassArrays(arrayIndex, arrayReg, fInfo.methodToStart.method());
                                        newStmtSwitch.arrayReg = fInfo.register;
                                        newStmtSwitch.arrayResults = this.arrayResults;
                                        previousFields.forEach(x->newStmtSwitch.addPreviousField(x));
                                        iteratorHelper.runOverToFindSpecValuesBackwards(fInfo.methodToStart.method().getActiveBody(), fInfo.unitToStart, newStmtSwitch);
                                        List<String> resultsForArrayPattern = newStmtSwitch.getArrayResults();
                                    }
                                }
                            }
                        }
                    }
                    arrayReg = assignStmt.getRightOp();
                }
            }
            if (assignStmt.getLeftOp().toString().equals(String.format("%s[%s]", arrayReg, arrayIndex))) {
                if (classInstance.isInstance(assignStmt.getRightOp()))
                    arrayResults.add(((ClassConstant)assignStmt.getRightOp()).getValue());
                else {
                    arrayReg = assignStmt.getRightOp();
                }
            }
        }
        else if(assignStmt.getLeftOp().toString().equals(String.format("%s[%s]", arrayReg, arrayIndex)) || assignStmt.getLeftOp().equals(arrayReg)){
            searchInReturn = true;
            //find in return value
            arrayResults.addAll(InterprocAnalysis2.getInstance().findReturnValueInMethodForClassArrays(assignStmt, arrayReg, arrayIndex));
            searchInReturn = false;
        }
    }

    public void caseInvokeStmt(InvokeStmt stmt){
        if(Thread.currentThread().isInterrupted()){
            return;
        }
        InvokeExpr invokeExpr = stmt.getInvokeExpr();
        String callerReg = helpMethods.getCallerOfInvokeStmt(invokeExpr);
        String methodSignature = helpMethods.getSignatureOfInvokeExpr(invokeExpr);

        if(callerReg.equals(arrayReg.toString())){
            //Here we can check if there's any parameter of type ClassConstant I guess
            if(methodSignature.contains("void <init>")){
                for(Value arg: invokeExpr.getArgs()){
                    if(classInstance.isInstance(arg))
                        arrayResults.add(((ClassConstant)arg).getValue());
                }
            }
        }
        return;
    }

    public void caseReturnStmt(ReturnStmt stmt){
        if(Thread.currentThread().isInterrupted()){
            return;
        }
        if (classInstance.isInstance(stmt.getOp()))
            arrayResults.add(((ClassConstant)stmt.getOp()).getValue());
        else{
            if(stmt.getOp().getType().getClass().equals(ArrayType.class) && searchInReturn){
                arrayReg = stmt.getOp();
            }
        }
    }

    public boolean run(){
        return arrayResults.isEmpty();
    }

}
