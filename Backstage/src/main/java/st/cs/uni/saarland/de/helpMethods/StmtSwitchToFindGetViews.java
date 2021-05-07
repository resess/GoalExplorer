package st.cs.uni.saarland.de.helpMethods;

import soot.Scene;
import soot.SootMethod;
import soot.Value;
import soot.jimple.*;
import soot.jimple.toolkits.callgraph.Edge;
import st.cs.uni.saarland.de.helpClasses.Helper;
import st.cs.uni.saarland.de.helpClasses.InterProcInfo;

import java.util.Iterator;
import java.util.List;

/**
 * Created by avdiienko on 15/05/16.
 */
public class StmtSwitchToFindGetViews extends AbstractStmtSwitch {
    private final SootMethod currentMethod;
    private final SootMethod caller;
    private Value retReg;
    private boolean isViewFound = false;

    @Override
    public Integer getResult() {
        return result;
    }

    private int result;
    private Value elementIdReg;

    public boolean isReady() {
        return isReady;
    }

    private boolean isReady = false;

    public StmtSwitchToFindGetViews(Value returnValue, SootMethod curentMetod,  SootMethod caller){
        this.currentMethod = curentMetod;
        this.retReg = returnValue;
        this.caller = caller;
    }

    public void caseAssignStmt(AssignStmt stmt){
        if(stmt.containsInvokeExpr()){
            if(!isViewFound && stmt.getLeftOp().equals(retReg)){
                InvokeExpr expr = stmt.getInvokeExpr();
                if(expr.getMethod().getSubSignature().equals("android.view.View findViewById(int)")){
                    isViewFound = true;
                    if(expr.getArg(0) instanceof IntConstant){
                        result = ((IntConstant)expr.getArg(0)).value;
                        isReady = true;
                        return;
                    }
                    else{
                        elementIdReg = expr.getArg(0);
                        return;
                    }
                }

                if(isViewFound && elementIdReg != null){
                    if(elementIdReg.equals(stmt.getLeftOp())) {
                        List<InterProcInfo> resList = InterprocAnalysis2.getInstance().findReturnValueInMethod2(stmt);
                        if(!resList.isEmpty()){
                            if(!resList.get(0).getValueOfSearchedReg().isEmpty() && Helper.isIntegerParseInt(resList.get(0).getValueOfSearchedReg())){
                                result = Integer.parseInt(resList.get(0).getValueOfSearchedReg());
                            }
                        }
                        isReady = true;
                        return;
                    }
                }
            }
        }
    }

    public void caseIdentityStmt(IdentityStmt stmt){
        if(isViewFound && elementIdReg != null && stmt.getRightOp() instanceof ParameterRef && stmt.getLeftOp().equals(elementIdReg)){
            ParameterRef param = (ParameterRef)stmt.getRightOp();
            final int index = param.getIndex();
            Iterator<Edge> edgeIterator = Scene.v().getCallGraph().edgesInto(currentMethod);
            while (edgeIterator.hasNext()){
                Edge e = edgeIterator.next();
                if(e.src().equals(caller)){
                    Value arg = e.srcStmt().getInvokeExpr().getArg(index);
                    if(arg instanceof IntConstant){
                        result = ((IntConstant)arg).value;
                        isReady = true;
                        return;
                    }
                }
            }


        }
    }
}
