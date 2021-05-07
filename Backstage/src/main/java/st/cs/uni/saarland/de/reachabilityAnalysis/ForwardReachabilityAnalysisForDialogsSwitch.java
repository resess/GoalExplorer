package st.cs.uni.saarland.de.reachabilityAnalysis;

import java.util.*;
import java.util.stream.Collectors;


import soot.Scene;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.jimple.AbstractStmtSwitch;
import soot.jimple.AssignStmt;
import soot.jimple.EqExpr;
import soot.jimple.IfStmt;
import soot.jimple.IntConstant;
import soot.jimple.InvokeStmt;
import soot.jimple.LookupSwitchStmt;
import soot.jimple.NeExpr;
import soot.jimple.Stmt;
import soot.jimple.internal.JimpleLocal;
import soot.jimple.toolkits.callgraph.Edge;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.MHGPostDominatorsFinder;
import st.cs.uni.saarland.de.helpClasses.Helper;

public class ForwardReachabilityAnalysisForDialogsSwitch extends AbstractStmtSwitch{
	
	private final String registerOfButtonId;
	private final String elementId;
	private final SootMethod currentSootMethod;
	private final Set<CallSite> callSites;
	private boolean isReady = false;
	private final MHGPostDominatorsFinder<Unit> postDominatorFinder;
	private final List<Unit> visitedTargets;
    private final List<String> uris;
	
	public ForwardReachabilityAnalysisForDialogsSwitch(String elementId, SootMethod sootMethod, Set<CallSite> callSites){
		this.elementId = elementId;
		this.currentSootMethod = sootMethod;
		this.callSites = callSites;
		this.postDominatorFinder = new MHGPostDominatorsFinder<>(new ExceptionalUnitGraph(currentSootMethod.getActiveBody()));
		this.registerOfButtonId = currentSootMethod.getActiveBody().getParameterLocal(1).toString();
		this.visitedTargets = new ArrayList<>();
        this.uris = new ArrayList<>();
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
			processInvokeExpr(stmt);
		}
	}

	private void processInvokeExpr(Stmt stmt) {
		CallbackToApiMapper.processUnit(currentSootMethod, callSites, stmt);

		if (RAHelper.getStaticMethodSignatures().contains(Helper.getSignatureOfSootMethod(stmt.getInvokeExpr().getMethod()))) {
			String uri = RAHelper.analyzeInvokeExpressionToFindUris(currentSootMethod.getActiveBody(), stmt, stmt.getInvokeExpr().getMethod(), stmt.getInvokeExpr().getArg(0), false);
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
				ForwardReachabilityAnalysisForDialogsSwitch newSwitch = new ForwardReachabilityAnalysisForDialogsSwitch(elementId, currentSootMethod, callSites);
				newSwitch.setVisitedTargets(this.visitedTargets);
				while(target != null){
					target.apply(newSwitch);
					if(newSwitch.isReady()){
						break;
					}
					target = postDominatorFinder.getImmediateDominator(target);						
				}
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
				ForwardReachabilityAnalysisForDialogsSwitch newSwitch = new ForwardReachabilityAnalysisForDialogsSwitch(elementId, currentSootMethod, callSites);
				newSwitch.setVisitedTargets(this.visitedTargets);
				while(target != null){
					target.apply(newSwitch);
					if(newSwitch.isReady()){
						break;
					}
					target = postDominatorFinder.getImmediateDominator(target);						
				}
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
				ForwardReachabilityAnalysisForDialogsSwitch newSwitch = new ForwardReachabilityAnalysisForDialogsSwitch(elementId, currentSootMethod, callSites);
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
				ForwardReachabilityAnalysisForDialogsSwitch newSwitch = new ForwardReachabilityAnalysisForDialogsSwitch(elementId, currentSootMethod, callSites);
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

	private void analyzeBothBranches(IfStmt stmt) {

		ForwardReachabilityAnalysisForDialogsSwitch newSwitch = new ForwardReachabilityAnalysisForDialogsSwitch(elementId, currentSootMethod, callSites);
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
					ForwardReachabilityAnalysisForDialogsSwitch newSwitch = new ForwardReachabilityAnalysisForDialogsSwitch(elementId, currentSootMethod, callSites);
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
				ForwardReachabilityAnalysisForDialogsSwitch newSwitch = new ForwardReachabilityAnalysisForDialogsSwitch(elementId, currentSootMethod, callSites);
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
				ForwardReachabilityAnalysisForDialogsSwitch newSwitch = new ForwardReachabilityAnalysisForDialogsSwitch(elementId, currentSootMethod, callSites);
				while(branchUnit != null){
					branchUnit.apply(newSwitch);
					branchUnit = postDominatorFinder.getImmediateDominator(branchUnit);					
				}
			}
		}
		isReady = true;
	}
	
}
