package st.cs.uni.saarland.de.reachabilityAnalysis;

import soot.SootClass;
import soot.SootMethod;
import soot.Unit;

public class CallSite {
	public SootMethod method;
	public SootClass classOfInvokeExpr;
	public Unit unit;
	public SootMethod caller;
	
	@Override
	public String toString(){
		return method.toString();
	}
}
