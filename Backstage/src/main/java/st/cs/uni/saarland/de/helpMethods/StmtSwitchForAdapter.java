package st.cs.uni.saarland.de.helpMethods;

import soot.SootMethod;
import soot.jimple.AssignStmt;
import soot.jimple.IdentityStmt;
import soot.jimple.InvokeStmt;
import st.cs.uni.saarland.de.helpClasses.Info;
import st.cs.uni.saarland.de.helpClasses.MyStmtSwitch;

public abstract class StmtSwitchForAdapter extends MyStmtSwitch {

    public StmtSwitchForAdapter(SootMethod currentSootMethod) {
        super(currentSootMethod);
    }

    @Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		/*if (!super.equals(obj))
			return false;*/
		if (getClass() != obj.getClass())
			return false;
		return true;
	}
   
    public abstract Info caseIdentityStmt(final IdentityStmt stmt, Info info);

    public abstract Info caseInvokeStmt(final InvokeStmt stmt, Info info);

    public abstract Info caseAssignStmt(final AssignStmt stmt, Info info);
}
