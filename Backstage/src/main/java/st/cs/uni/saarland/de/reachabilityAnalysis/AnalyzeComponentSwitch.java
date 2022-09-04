package st.cs.uni.saarland.de.reachabilityAnalysis;

import soot.SootMethod;
import soot.Value;
import soot.jimple.*;

/**
 * Created by avdiienko on 10/05/16.
 */
public class AnalyzeComponentSwitch extends AbstractStmtSwitch {
    private final Value registerOfComponent;
    private final SootMethod currentMethod;
    private String elementId;
    private String result;

    public String getResult(){
        return result;
    }

    public AnalyzeComponentSwitch(Value reg, SootMethod currentSootMethod, String elementId){
        registerOfComponent = reg;
        currentMethod = currentSootMethod;
        this.elementId = elementId;
    }

    public void caseInvokeStmt(InvokeStmt stmt) {
        InvokeExpr expr = stmt.getInvokeExpr();
        if(expr.getUseBoxes().get(0).getValue().equals(registerOfComponent)) {
            if (expr.getMethod().getSignature().equals(START_ACTIVITY_CONSTANTS.COMPONENT_INIT)) {
                result = processClass(stmt, expr, 1);
            } else if (expr.getMethod().getSignature().equals(START_ACTIVITY_CONSTANTS.COMPONENT_INIT_CLASS) || expr.getMethod().getSignature().equals(START_ACTIVITY_CONSTANTS.COMPONENT_INIT_CONTEXT_CLASS)) {
                if (expr.getArg(1) instanceof ClassConstant) {
                    result = ((ClassConstant) expr.getArg(1)).getValue();
                }
            }
        }
    }

    private String processClass(Stmt stmt, InvokeExpr invokeExpr, int paramNumber) {
        StringPropagator stringPropagator = new StringPropagator(currentMethod, stmt, invokeExpr.getArg(paramNumber), elementId);
        stringPropagator.run();
        String className = stringPropagator.getResult();
        if(className != null){
            return className;
        }
        return null;
    }
}
