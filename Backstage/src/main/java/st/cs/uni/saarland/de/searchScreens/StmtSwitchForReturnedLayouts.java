package st.cs.uni.saarland.de.searchScreens;

import soot.SootMethod;
import soot.jimple.IntConstant;
import soot.jimple.NullConstant;
import soot.jimple.ReturnStmt;
import st.cs.uni.saarland.de.testApps.Content;

import java.util.HashSet;
import java.util.Set;

public class StmtSwitchForReturnedLayouts extends StmtSwitchForLayoutInflater {

	private Set<ReturnStmt> visitedReturnStmt = new HashSet<>();

	public StmtSwitchForReturnedLayouts(SootMethod currentSootMethod) {
		super(currentSootMethod);
	}

	public StmtSwitchForReturnedLayouts(Set<SootMethod> pcallStack, SootMethod m) {
		super(m);
		callStack = pcallStack;
	}

	public void caseReturnStmt(ReturnStmt stmt){
		if(visitedReturnStmt.contains(stmt)){
			return;
		}
		visitedReturnStmt.add(stmt);
		if(Thread.currentThread().isInterrupted()){
			return;
		}
		if (stmt.getOp() instanceof NullConstant){
			shouldBreak = true;
		}else{
			String retReg = helpMethods.getReturnRegOfReturnStmt(stmt);
			if ("".equals(retReg)){
				shouldBreak = true;
			}
			else if(stmt.getOp() instanceof IntConstant){
				int value = ((IntConstant) stmt.getOp()).value;
				if(value == 0){
					shouldBreak = true;
				}
				else {
					LayoutInfo lay = new LayoutInfo("", Content.getInstance().getNewUniqueID());
					lay.setFragment();
					lay.setLayoutID(Integer.toString(value));
					putToResultedLayouts(lay.getID(), lay);
					shouldBreak = true;
				}
			}
			else{
				LayoutInfo lay =  new LayoutInfo(retReg, Content.getInstance().getNewUniqueID());
				lay.setFragment();
	//			layouts.put(retReg,lay);
				putToResultedLayouts(lay.getID(), lay);
			}
		}
	}
	
}
