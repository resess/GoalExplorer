package st.cs.uni.saarland.de.reachabilityAnalysis;

import soot.Scene;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.jimple.*;
import soot.jimple.internal.JimpleLocal;
import soot.jimple.toolkits.callgraph.Edge;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.MHGPostDominatorsFinder;
import st.cs.uni.saarland.de.helpClasses.Helper;

import java.util.*;
import java.util.stream.Collectors;

public class ForwardReachabilityAnalysisForClicksSwitch extends AbstractStmtSwitch {
	
	protected final String buttonClickGetId;
	protected String registerOfButtonId;
	protected final String elementId;
	protected final SootMethod currentSootMethod;
	protected final Set<CallSite> callSites;
	protected boolean isReady = false;
	protected final MHGPostDominatorsFinder<Unit> postDominatorFinder;
	protected final List<Unit> visitedTargets;
	protected final List<String> uris;

	public ForwardReachabilityAnalysisForClicksSwitch(String elementId, SootMethod sootMethod, Set<CallSite> callSites, String butonRegister, String getIdSignature){
		this.elementId = elementId;
		this.currentSootMethod = sootMethod;
		this.callSites = callSites;
		this.postDominatorFinder = new MHGPostDominatorsFinder<>(new ExceptionalUnitGraph(currentSootMethod.getActiveBody()));
		this.registerOfButtonId = butonRegister;
		this.visitedTargets = new ArrayList<>();
		this.uris = new ArrayList<>();
		this.buttonClickGetId = getIdSignature;
	}

	public List<String> getUris(){
		return uris;
	}
	
	public void setVisitedTargets(List<Unit> targets){
		this.visitedTargets.addAll(targets);
	}
	
	public boolean isReady(){
		return isReady;
	}
	
	public void caseInvokeStmt(final InvokeStmt stmt){
		processInvokeExpr(stmt);
	}
	
	public void caseAssignStmt(final AssignStmt stmt){
		if(stmt.containsInvokeExpr()){
			InvokeExpr expr = stmt.getInvokeExpr();
			if(Helper.getSignatureOfSootMethod(expr.getMethod()).equals(buttonClickGetId)){
				registerOfButtonId = stmt.getLeftOp().toString();
			}
			else{
				processInvokeExpr(stmt);
			}
		}
		else{
			if(stmt.getLeftOp().toString().equals(registerOfButtonId)){
				registerOfButtonId="";
			}
		}
	}
	
	protected void processInvokeExpr(Stmt stmt) {
		CallbackToApiMapper.processUnit(currentSootMethod, callSites, stmt);

		if (RAHelper.getStaticMethodSignatures().contains(Helper.getSignatureOfSootMethod(stmt.getInvokeExpr().getMethod()))) {
			String uri = RAHelper.analyzeInvokeExpressionToFindUris(currentSootMethod.getActiveBody(), stmt, stmt.getInvokeExpr().getMethod(), stmt.getInvokeExpr().getArg(0), true);
			if (uri != null) {
				uris.add(uri);
			} else {
				uris.add(Helper.getSignatureOfSootMethod(stmt.getInvokeExpr().getMethod()));
			}
		}
	}
	
	public void caseIfStmt(final IfStmt stmt){
		boolean isBranchTaken = false;
		Value condition = stmt.getCondition();
		if(condition instanceof NeExpr){
			NeExpr neExpr = (NeExpr)condition;
			if(neExpr.getOp1().getType().toString().equals("int") && neExpr.getOp1().toString().equals(registerOfButtonId)){
                isBranchTaken = true;
				Unit target;
				String valueToCompare;
				if(neExpr.getOp2() instanceof IntConstant){
					valueToCompare = Integer.toString(((IntConstant)neExpr.getOp2()).value);
				}
				else{
					//search in fields
					valueToCompare = RAHelper.getValueOfTheVariable((JimpleLocal)neExpr.getOp2(), stmt, currentSootMethod);
				}
				if(valueToCompare == null){
					return;
				}
				if(!valueToCompare.equals(elementId)){
					target = stmt.getTarget();
					if(visitedTargets.contains(target)){
						return;
					}
					visitedTargets.add(target);
				}
				else{
					//go to the immediate post dominator of this if stmt
					target = Helper.getSuccessorOf(currentSootMethod.getActiveBody().getUnits(), stmt);
					if(visitedTargets.contains(target)){
						return;
					}
					visitedTargets.add(target);
				}
				ForwardReachabilityAnalysisForClicksSwitch newSwitch = new ForwardReachabilityAnalysisForClicksSwitch(elementId, currentSootMethod, callSites, registerOfButtonId, this.buttonClickGetId);
				newSwitch.setVisitedTargets(this.visitedTargets);
				while(target != null){
					target.apply(newSwitch);
					if(newSwitch.isReady()){
						break;
					}
					target = postDominatorFinder.getImmediateDominator(target);						
				}
				//isReady = true;
			}
			else if(neExpr.getOp2().getType().toString().equals("int") && neExpr.getOp2().toString().equals(registerOfButtonId)){
                isBranchTaken = true;
				Unit target;
				String valueToCompare;
				if(neExpr.getOp1() instanceof IntConstant){
					valueToCompare = Integer.toString(((IntConstant)neExpr.getOp1()).value);
				}
				else{
					//search in fields
					valueToCompare = RAHelper.getValueOfTheVariable((JimpleLocal)neExpr.getOp1(), stmt, currentSootMethod);
				}
				if(valueToCompare == null){
					return;
				}
				if(!valueToCompare.equals(elementId)){
					target = stmt.getTarget();
					if(visitedTargets.contains(target)){
						return;
					}
					visitedTargets.add(target);
				}
				else{
					//go to the immediate post dominator of this if stmt
					target = Helper.getSuccessorOf(currentSootMethod.getActiveBody().getUnits(), stmt);
					if(visitedTargets.contains(target)){
						return;
					}
					visitedTargets.add(target);
				}
				ForwardReachabilityAnalysisForClicksSwitch newSwitch = new ForwardReachabilityAnalysisForClicksSwitch(elementId, currentSootMethod, callSites, registerOfButtonId, this.buttonClickGetId);
				newSwitch.setVisitedTargets(this.visitedTargets);
				while(target != null){
					target.apply(newSwitch);
					if(newSwitch.isReady()){
						break;
					}
					target = postDominatorFinder.getImmediateDominator(target);						
				}
				//isReady = true;
			}
			else{
				//use both branches
				analyzeBothBranches(stmt);
			}
		}
		else if(condition instanceof EqExpr){
			EqExpr eqExpr = (EqExpr)condition;
			if(eqExpr.getOp1().getType().toString().equals("int") && eqExpr.getOp1().toString().equals(registerOfButtonId)){
                isBranchTaken = true;
				Unit target;
				String valueToCompare;
				if(eqExpr.getOp2() instanceof IntConstant){
					valueToCompare = Integer.toString(((IntConstant)eqExpr.getOp2()).value);
				}
				else{
					//search in fields
					valueToCompare = RAHelper.getValueOfTheVariable((JimpleLocal)eqExpr.getOp2(), stmt, currentSootMethod);
				}
				if(valueToCompare == null){
					return;
				}
				if(valueToCompare.equals(elementId)){
					target = stmt.getTarget();
					if(visitedTargets.contains(target)){
						return;
					}
					visitedTargets.add(target);
				}
				else{
					//go to the immediate post dominator of this if stmt
					target = Helper.getSuccessorOf(currentSootMethod.getActiveBody().getUnits(), stmt);
					if(visitedTargets.contains(target)){
						return;
					}
					visitedTargets.add(target);
				}
				ForwardReachabilityAnalysisForClicksSwitch newSwitch = new ForwardReachabilityAnalysisForClicksSwitch(elementId, currentSootMethod, callSites, registerOfButtonId, this.buttonClickGetId);
				newSwitch.setVisitedTargets(this.visitedTargets);
				while(target != null){
					target.apply(newSwitch);
					if(newSwitch.isReady()){
						break;
					}
					target = postDominatorFinder.getImmediateDominator(target);						
				}
				//isReady = true;
			}
			else if(eqExpr.getOp2().getType().toString().equals("int") && eqExpr.getOp2().toString().equals(registerOfButtonId)){
                isBranchTaken = true;
				Unit target;
				String valueToCompare;
				if(eqExpr.getOp1() instanceof IntConstant){
					valueToCompare = Integer.toString(((IntConstant)eqExpr.getOp1()).value);
				}
				else{
					//search in fields
					valueToCompare = RAHelper.getValueOfTheVariable((JimpleLocal)eqExpr.getOp1(), stmt, currentSootMethod);
				}
				if(valueToCompare == null){
					return;
				}
				if(valueToCompare.equals(elementId)){
					target = stmt.getTarget();
					if(visitedTargets.contains(target)){
						return;
					}
					visitedTargets.add(target);
				}
				else{
					//go to the immediate post dominator of this if stmt
					target = Helper.getSuccessorOf(currentSootMethod.getActiveBody().getUnits(), stmt);
					if(visitedTargets.contains(target)){
						return;
					}
					visitedTargets.add(target);
				}
				ForwardReachabilityAnalysisForClicksSwitch newSwitch = new ForwardReachabilityAnalysisForClicksSwitch(elementId, currentSootMethod, callSites, registerOfButtonId, this.buttonClickGetId);
				newSwitch.setVisitedTargets(this.visitedTargets);
				while(target != null){
					target.apply(newSwitch);
					if(newSwitch.isReady()){
						break;
					}
					target = postDominatorFinder.getImmediateDominator(target);						
				}
				//isReady = true;
			}
			else{
				//use both branches
				analyzeBothBranches(stmt);
			}
		}
		if(!isBranchTaken){
			//use both branches
			analyzeBothBranches(stmt);
		}
		isReady = true;
	}
	protected void analyzeBothBranches(IfStmt stmt) {

		ForwardReachabilityAnalysisForClicksSwitch newSwitch = new ForwardReachabilityAnalysisForClicksSwitch(elementId, currentSootMethod, callSites, registerOfButtonId, this.buttonClickGetId);
		Unit target = stmt.getTarget();
		if(!visitedTargets.contains(target)){
			visitedTargets.add(target);
			newSwitch.setVisitedTargets(this.visitedTargets);
			while(target != null){
				target.apply(newSwitch);
				if(newSwitch.isReady()){
					break;
				}
				target = postDominatorFinder.getImmediateDominator(target);						
			}
		}
		
		target = Helper.getSuccessorOf(currentSootMethod.getActiveBody().getUnits(), stmt);
		if(!visitedTargets.contains(target)){
			visitedTargets.add(target);
			newSwitch.setVisitedTargets(this.visitedTargets);
			while(target != null){
				target.apply(newSwitch);
				if(newSwitch.isReady()){
					break;
				}
				target = postDominatorFinder.getImmediateDominator(target);						
			}
		}
	}
	
	public void caseLookupSwitchStmt(final LookupSwitchStmt stmt){
		if(stmt.getKey().toString().equals(registerOfButtonId)){
			boolean isBranchTaken = false;
			//find the case when our Id is used
			for(int i = 0; i<stmt.getLookupValues().size(); i++){
				if(Integer.toString(stmt.getLookupValue(i)).equals(elementId)){
					//we are in the right case
					Unit branchUnit = stmt.getTarget(i);
					//start from this unit and go forward
					ForwardReachabilityAnalysisForClicksSwitch newSwitch = new ForwardReachabilityAnalysisForClicksSwitch(elementId, currentSootMethod, callSites, registerOfButtonId, this.buttonClickGetId);
					while(branchUnit != null){
						branchUnit.apply(newSwitch);
						branchUnit = postDominatorFinder.getImmediateDominator(branchUnit);					
					}
					//isReady = true;
					isBranchTaken = true;
					break;
				}
			}
			if(!isBranchTaken){
				Unit defaultTarget = stmt.getDefaultTarget();
				ForwardReachabilityAnalysisForClicksSwitch newSwitch = new ForwardReachabilityAnalysisForClicksSwitch(elementId, currentSootMethod, callSites, registerOfButtonId, this.buttonClickGetId);
				while(defaultTarget != null){
					defaultTarget.apply(newSwitch);
					defaultTarget = postDominatorFinder.getImmediateDominator(defaultTarget);					
				}
				//isReady = true;
			}			
		}
		else{
			//take all branches...
			List<Unit> targets = new ArrayList<>();
			targets.addAll(stmt.getTargets());
			targets.add(stmt.getDefaultTarget());
			for(Unit branchUnit : targets.stream().distinct().collect(Collectors.toList())){
				//start from this unit and go forward
				ForwardReachabilityAnalysisForClicksSwitch newSwitch = new ForwardReachabilityAnalysisForClicksSwitch(elementId, currentSootMethod, callSites, registerOfButtonId, this.buttonClickGetId);
				while(branchUnit != null){
					branchUnit.apply(newSwitch);
					branchUnit = postDominatorFinder.getImmediateDominator(branchUnit);					
				}
			}
		}
		isReady = true;
	}
}
