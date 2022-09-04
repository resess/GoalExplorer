package st.cs.uni.saarland.de.helpMethods;

import soot.SootMethod;
import soot.jimple.AssignStmt;
import soot.jimple.IdentityStmt;
import soot.jimple.InvokeStmt;
import st.cs.uni.saarland.de.helpClasses.Info;

public class StmtSwitchForCursorAdapter extends StmtSwitchForAdapter{
    public StmtSwitchForCursorAdapter(SootMethod currentSootMethod) {
        super(currentSootMethod);
    }

    @Override
    public Info caseIdentityStmt(IdentityStmt stmt, Info info) {
        return null;
    }

    @Override
    public Info caseInvokeStmt(InvokeStmt stmt, Info info) {
        return null;
    }

    @Override
    public Info caseAssignStmt(AssignStmt stmt, Info info) {
        return null;
    }
}
