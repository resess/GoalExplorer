package st.cs.uni.saarland.de.reachabilityAnalysis;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.jimple.*;
import soot.jimple.internal.JimpleLocal;
import st.cs.uni.saarland.de.helpClasses.Helper;

import java.util.Set;

public class ForwardReachabilityAnalysisForClicksWithFullObjectsSwitch extends ForwardReachabilityAnalysisForClicksSwitch {
	private final Logger logger;

	public ForwardReachabilityAnalysisForClicksWithFullObjectsSwitch(String elementId, SootMethod sootMethod, Set<CallSite> callSites){
		super(elementId, sootMethod, callSites, "@parameter0", "");
		this.logger = LoggerFactory.getLogger(this.getClass().getSimpleName());
	}

	public ForwardReachabilityAnalysisForClicksWithFullObjectsSwitch(String elementId, SootMethod sootMethod, Set<CallSite> callSites, String getIdSignature){
		super(elementId, sootMethod, callSites, "@parameter0", getIdSignature);
		this.logger = LoggerFactory.getLogger(this.getClass().getSimpleName());
	}

	public ForwardReachabilityAnalysisForClicksWithFullObjectsSwitch(String elementId, String buttonRegister, SootMethod sootMethod, Set<CallSite> callSites){
		super(elementId, sootMethod, callSites, buttonRegister, "");
		this.logger = LoggerFactory.getLogger(this.getClass().getSimpleName());
	}

	public ForwardReachabilityAnalysisForClicksWithFullObjectsSwitch(String elementId, String buttonRegister, SootMethod sootMethod, Set<CallSite> callSites, String getIdSignature){
		super(elementId, sootMethod, callSites, buttonRegister, getIdSignature);
		this.logger = LoggerFactory.getLogger(this.getClass().getSimpleName());
	}


	@Override
	public void caseLookupSwitchStmt(final LookupSwitchStmt stmt){

	}

	@Override
	public void caseIfStmt(final IfStmt stmt){
		//logger.debug("Comparing {} {} for {}", elementId, stmt, currentSootMethod);
		boolean isBranchTaken = false;
		Value condition = stmt.getCondition();
		if(condition instanceof NeExpr){
			NeExpr neExpr = (NeExpr)condition;
			//logger.debug("Op1 {} {} and op2 {} {}", neExpr.getOp1(), neExpr.getOp1().getType().toString(),  neExpr.getOp2(), neExpr.getOp2().getType().toString());
		
			if(neExpr.getOp1().getType().toString().equals("android.view.View") && neExpr.getOp1().toString().equals(registerOfButtonId)){
				isBranchTaken = true;
				Unit target;
				String valueToCompare = null;
				if(neExpr.getOp2().getType().toString().equals("android.widget.Button") || neExpr.getOp2().getType().toString().equals("android.view.View") ){
					//search in fields
					valueToCompare = RAHelper.getValueOfTheVariableForTheButtonsClick((JimpleLocal)neExpr.getOp2(), stmt, currentSootMethod);
				}
				//logger.debug("Comparing value {} with {} for {} {}", elementId, valueToCompare, stmt, currentSootMethod);
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
				ForwardReachabilityAnalysisForClicksSwitch newSwitch = new ForwardReachabilityAnalysisForClicksWithFullObjectsSwitch(elementId, registerOfButtonId, currentSootMethod, callSites, buttonClickGetId);
				//ForwardReachabilityAnalysisForClicksSwitch newSwitch = new ForwardReachabilityAnalysisForClicksSwitch(elementId, currentSootMethod, callSites, registerOfButtonId, this.buttonClickGetId);
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
			else if(neExpr.getOp2().getType().toString().equals("android.view.View") && neExpr.getOp2().toString().equals(registerOfButtonId)){
				isBranchTaken = true;
				Unit target;
				String valueToCompare = null;
				if(neExpr.getOp1().getType().toString().equals("android.widget.Button") || neExpr.getOp1().getType().toString().equals("android.view.View") ){
					//search in fields
					valueToCompare = RAHelper.getValueOfTheVariableForTheButtonsClick((JimpleLocal)neExpr.getOp1(), stmt, currentSootMethod);
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
				ForwardReachabilityAnalysisForClicksSwitch newSwitch = new ForwardReachabilityAnalysisForClicksWithFullObjectsSwitch(elementId, registerOfButtonId, currentSootMethod, callSites, buttonClickGetId);
				//ForwardReachabilityAnalysisForClicksSwitch newSwitch = new ForwardReachabilityAnalysisForClicksSwitch(elementId, currentSootMethod, callSites, registerOfButtonId, this.buttonClickGetId);
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
			//logger.debug("Op1 {} {} and op2 {} {}", eqExpr.getOp1(), eqExpr.getOp1().getType().toString(),  eqExpr.getOp2(), eqExpr.getOp2().getType().toString());
			if(eqExpr.getOp1().getType().toString().equals("android.view.View") && eqExpr.getOp1().toString().equals(registerOfButtonId)){
				isBranchTaken = true;
				Unit target;
				String valueToCompare = null;
				if(eqExpr.getOp2().getType().toString().equals("android.widget.Button") || eqExpr.getOp2().getType().toString().equals("android.view.View") ){
					//search in fields
					valueToCompare = RAHelper.getValueOfTheVariableForTheButtonsClick((JimpleLocal)eqExpr.getOp2(), stmt, currentSootMethod);
				}
				//logger.debug("Comparing value {} with {} for {} {}", elementId, valueToCompare, stmt, currentSootMethod);
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
				ForwardReachabilityAnalysisForClicksSwitch newSwitch = new ForwardReachabilityAnalysisForClicksWithFullObjectsSwitch(elementId, registerOfButtonId, currentSootMethod, callSites, buttonClickGetId);
				//(elementId, currentSootMethod, callSites, registerOfButtonId, this.buttonClickGetId);
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
			else if(eqExpr.getOp2().getType().toString().equals("android.view.View") && eqExpr.getOp2().toString().equals(registerOfButtonId)){
				isBranchTaken = true;
				Unit target;
				String valueToCompare = null;
				if(eqExpr.getOp1().getType().toString().equals("android.widget.Button") || eqExpr.getOp1().getType().toString().equals("android.view.View") ){
					//search in fields
					valueToCompare = RAHelper.getValueOfTheVariableForTheButtonsClick((JimpleLocal)eqExpr.getOp1(), stmt, currentSootMethod);
				}
				if(valueToCompare == null){
					return;
				}
				if(StringUtils.isEmpty(valueToCompare)){
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
				ForwardReachabilityAnalysisForClicksSwitch newSwitch = new ForwardReachabilityAnalysisForClicksWithFullObjectsSwitch(elementId, registerOfButtonId, currentSootMethod, callSites, buttonClickGetId);
				//ForwardReachabilityAnalysisForClicksSwitch newSwitch = new ForwardReachabilityAnalysisForClicksSwitch(elementId, currentSootMethod, callSites, registerOfButtonId, this.buttonClickGetId);
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
		else if(!isBranchTaken){
			//use both branches
			analyzeBothBranches(stmt);
		}
		isReady = true;
	}

	protected void analyzeBothBranches(IfStmt stmt) {

		ForwardReachabilityAnalysisForClicksWithFullObjectsSwitch newSwitch = new ForwardReachabilityAnalysisForClicksWithFullObjectsSwitch(elementId, registerOfButtonId, currentSootMethod, callSites, buttonClickGetId);
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

	@Override
	protected void processInvokeExpr(Stmt stmt) {
		//TODO, here need to deal with full object case as well, add a boolean false here and true in child class
		//TODO here we assume that getId was already called and we pass an item, when technically we could still pass the full object and invoke getId afterwards
		CallbackToApiMapper.processUnit(currentSootMethod, callSites, stmt, registerOfButtonId, buttonClickGetId, true);


		if (RAHelper.getStaticMethodSignatures().contains(Helper.getSignatureOfSootMethod(stmt.getInvokeExpr().getMethod()))) {
			String uri = RAHelper.analyzeInvokeExpressionToFindUris(currentSootMethod.getActiveBody(), stmt, stmt.getInvokeExpr().getMethod(), stmt.getInvokeExpr().getArg(0), true);
			if (uri != null) {
				uris.add(uri);
			} else {
				uris.add(Helper.getSignatureOfSootMethod(stmt.getInvokeExpr().getMethod()));
			}
		}


	}
}
