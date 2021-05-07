package st.cs.uni.saarland.de.reachabilityAnalysis;

import org.apache.commons.lang3.StringUtils;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.jimple.EqExpr;
import soot.jimple.IfStmt;
import soot.jimple.LookupSwitchStmt;
import soot.jimple.NeExpr;
import soot.jimple.internal.JimpleLocal;
import st.cs.uni.saarland.de.helpClasses.Helper;

import java.util.Set;

public class ForwardReachabilityAnalysisForClicksWithFullObjectsSwitch extends ForwardReachabilityAnalysisForClicksSwitch {

	public ForwardReachabilityAnalysisForClicksWithFullObjectsSwitch(String elementId, SootMethod sootMethod, Set<CallSite> callSites){
		super(elementId, sootMethod, callSites, "$r1", "");
	}

	@Override
	public void caseLookupSwitchStmt(final LookupSwitchStmt stmt){

	}

	@Override
	public void caseIfStmt(final IfStmt stmt){
		boolean isBranchTaken = false;
		Value condition = stmt.getCondition();
		if(condition instanceof NeExpr){
			NeExpr neExpr = (NeExpr)condition;
			if(neExpr.getOp1().getType().toString().equals("android.view.View") && neExpr.getOp1().toString().equals(registerOfButtonId)){
				isBranchTaken = true;
				Unit target;
				String valueToCompare = null;
				if(neExpr.getOp2().getType().toString().equals("android.widget.Button") || neExpr.getOp2().getType().toString().equals("android.view.View") ){
					//search in fields
					valueToCompare = RAHelper.getValueOfTheVariableForTheButtonsClick((JimpleLocal)neExpr.getOp2(), stmt, currentSootMethod);
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
				String valueToCompare = null;
				if(eqExpr.getOp2().getType().toString().equals("android.widget.Button") || eqExpr.getOp2().getType().toString().equals("android.view.View") ){
					//search in fields
					valueToCompare = RAHelper.getValueOfTheVariableForTheButtonsClick((JimpleLocal)eqExpr.getOp2(), stmt, currentSootMethod);
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
}
