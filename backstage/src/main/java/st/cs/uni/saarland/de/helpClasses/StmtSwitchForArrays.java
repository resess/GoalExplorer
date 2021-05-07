package st.cs.uni.saarland.de.helpClasses;

import soot.ArrayType;
import soot.SootMethod;
import soot.Value;
import soot.jimple.*;
import st.cs.uni.saarland.de.helpMethods.InterprocAnalysis2;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by avdiienko on 09/12/15.
 */
public class StmtSwitchForArrays extends MyStmtSwitch {
    private int arrayIndex;
    private Value arrayReg;
    private List<Integer> arrayResults;
    private boolean searchInReturn = false;

    public void setSearchInReturn(boolean value){
        searchInReturn = value;
    }

    public List<Integer> getArrayResults(){
        return  arrayResults;
    }

    public StmtSwitchForArrays(int arrIndex, Value arrRegister, SootMethod currentSootMethod){
        super(currentSootMethod);
        this.arrayIndex = arrIndex;
        this.arrayReg = arrRegister;
        this.arrayResults = new CopyOnWriteArrayList<>();
    }

    public void caseIdentityStmt(IdentityStmt identityStmt){
        if(Thread.currentThread().isInterrupted()){
            return;
        }
        if(identityStmt.getRightOp() instanceof ParameterRef && identityStmt.getLeftOp().equals(arrayReg)){
            ParameterRef parameterRef = (ParameterRef)identityStmt.getRightOp();
            List<Integer> results = InterprocAnalysis2.getInstance().findArrayInitInReachableMethods(arrayIndex, parameterRef.getIndex(), getCurrentSootMethod(), new ArrayList<>(), searchInReturn);
            arrayResults.addAll(results);
        }
    }

    public void caseAssignStmt(AssignStmt assignStmt){
        if(Thread.currentThread().isInterrupted()){
            return;
        }
        if(!assignStmt.containsInvokeExpr()) {
            if (assignStmt.getLeftOp().toString().equals(String.format("%s[%s]", arrayReg, arrayIndex))) {
                if (assignStmt.getRightOp() instanceof IntConstant) {
                    arrayResults.add(((IntConstant) assignStmt.getRightOp()).value);
                } else {
                    arrayReg = assignStmt.getRightOp();
                }
            }
        }
        else if(assignStmt.getLeftOp().toString().equals(String.format("%s[%s]", arrayReg, arrayIndex)) || assignStmt.getLeftOp().equals(arrayReg)){
            searchInReturn = true;
            //find in return value
            arrayResults.addAll(InterprocAnalysis2.getInstance().findReturnValueInMethodForArrays(assignStmt, arrayReg, arrayIndex));
            searchInReturn = false;
        }
    }

    public void caseReturnStmt(ReturnStmt stmt){
        if(Thread.currentThread().isInterrupted()){
            return;
        }
        if (stmt.getOp() instanceof IntConstant){
            arrayResults.add(((IntConstant)stmt.getOp()).value);
        }
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
