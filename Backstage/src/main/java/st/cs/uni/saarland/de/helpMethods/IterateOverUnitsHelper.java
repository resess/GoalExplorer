package st.cs.uni.saarland.de.helpMethods;

import soot.*;
import soot.jimple.*;
import soot.toolkits.graph.DominatorsFinder;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.MHGDominatorsFinder;
import st.cs.uni.saarland.de.dissolveSpecXMLTags.TabViewInfo;
import st.cs.uni.saarland.de.helpClasses.Helper;
import st.cs.uni.saarland.de.helpClasses.Info;
import st.cs.uni.saarland.de.helpClasses.MyStmtSwitch;
import st.cs.uni.saarland.de.searchScreens.LayoutInfo;
import st.cs.uni.saarland.de.searchScreens.StmtSwitchForLayoutInflater;

import java.util.*;

public class IterateOverUnitsHelper {

	private Set<Unit> switchesThatWereProcessed = new HashSet<>();

	 public static IterateOverUnitsHelper newInstance(){
		 return new IterateOverUnitsHelper();
	 }

	 
	 public void runUnitsOverMethodBackwards(Body methodBody, MyStmtSwitch stmtSwitch) {
		 if(!Helper.processMethod(methodBody.getUnits().size())){
			 return;
		 }

         if (Thread.currentThread().isInterrupted()) {
             return;
         }

         final PatchingChain<Unit> units = methodBody.getUnits();

         if (methodBody.getMethod().getName().equals("<clinit>")) {
             return;
         }

		 //TODO: Tricky place #2
         // if we come from an interproc call, we should not delete the prev.fields or callStack
         Set<SootField> prevFields = new HashSet<>(stmtSwitch.getPreviousFields());
         Set<SootMethod> callStack = null;
		 if(stmtSwitch instanceof StmtSwitchForLayoutInflater){
			 StmtSwitchForLayoutInflater stmtSwicthForLayouts = (StmtSwitchForLayoutInflater) stmtSwitch;
			 callStack = stmtSwicthForLayouts.getCallStack();
			 Map<Integer, LayoutInfo> resultLayouts = stmtSwicthForLayouts.getResultedLayouts();
			 Set<TabViewInfo> resultedTabs = stmtSwicthForLayouts.getResultedTabs();
			 stmtSwicthForLayouts.init();
			 stmtSwicthForLayouts.putAllToResultedLayouts(resultLayouts);
			 stmtSwicthForLayouts.addAllToResultedTabs(resultedTabs);
		 }
		 else {
			 stmtSwitch.init();
		 }

         if (prevFields.size() > 0) {
             stmtSwitch.setPreviousFields(prevFields);
         }
         if ((callStack != null) && (stmtSwitch instanceof StmtSwitchForLayoutInflater)) {
             ((StmtSwitchForLayoutInflater) stmtSwitch).setCallStack(callStack);
         }

//		stmtSwitch.setBody(body);
         Unit lastUnit = null;

         if (Helper.lastUnitOfMethod.containsKey(methodBody.getMethod())) {
             lastUnit = Helper.lastUnitOfMethod.get(methodBody.getMethod());
         } else {
             for (Unit u : units) {
                 if ((u instanceof ThrowStmt) || (u instanceof ReturnStmt) || (u instanceof ReturnVoidStmt)) {
                     lastUnit = u;
                     break;
                 }
             }
			 if(lastUnit != null) {
				 Helper.lastUnitOfMethod.put(methodBody.getMethod(), lastUnit);
			 }
         }

         runOverAllUnitsBackwards(units, lastUnit, stmtSwitch, methodBody);
     }

		
	public MyStmtSwitch runOverToFindSpecValuesBackwards(Body body, Unit lastUnit, MyStmtSwitch stmtSwitch) {
		if(!Helper.processMethod(body.getUnits().size())){
			return stmtSwitch;
		}

		if (Thread.currentThread().isInterrupted()) {
			return stmtSwitch;
		}
		
		// if inside the iteration of the first run of the method, a findReturnValue or findReachableMethod call happens,
			// the dominatorsFinder must be null, so that it will be created new
		Unit bufferUnit = lastUnit;
				
		Set<Info> resultInfosBeforeBlockStarts = new LinkedHashSet<>();
		Set<SootField> prevFieldsBeforBlockStarts = new HashSet<SootField>();
		Unit firstBoxAfterBlock = null;
		
//		MHGDominatorsFinder<Unit> dominatorsFinder = new MHGDominatorsFinder<Unit>(new ExceptionalUnitGraph(body));
//		cacheImmediateDomminator(body);
		PatchingChain<Unit> units = body.getUnits();
		
		// this order, because first unit will be field = $r2, and that we know
		try{
			bufferUnit = Helper.getPredecessorOf(units, bufferUnit);
		}catch(NoSuchElementException e){
			bufferUnit = null;
		}
		
		while(bufferUnit != null && !Thread.currentThread().isInterrupted()){
			
			runSwitch(bufferUnit, stmtSwitch, firstBoxAfterBlock, units, resultInfosBeforeBlockStarts, prevFieldsBeforBlockStarts, body);
			
			if (!stmtSwitch.run()){
				break;
			}
			
			try{
				bufferUnit = Helper.getPredecessorOf(units, bufferUnit);
			}catch(NoSuchElementException e){
				bufferUnit = null;
			}
			
		}
		// free again the dominatorsFinder field
		return stmtSwitch;
	}

	private void runOverAllUnitsBackwards(PatchingChain<Unit> units, Unit lastUnit, MyStmtSwitch stmtSwitch, Body body) {
		if(Thread.currentThread().isInterrupted()){
			return;
		}
		if(!Helper.processMethod(units.size())){
			return;
		}
		Unit bufferUnit = lastUnit;
		Set<Info> resultInfosBeforeBlockStarts = new LinkedHashSet<>();
		Set<SootField> prevFieldsBeforeBlockStarts = new HashSet<SootField>();
		Unit firstBoxAfterBlock = null;
				
		while(bufferUnit != null && !Thread.currentThread().isInterrupted()){	
			
			runSwitch(bufferUnit, stmtSwitch, firstBoxAfterBlock, units, resultInfosBeforeBlockStarts, prevFieldsBeforeBlockStarts, body);
			
			try{
				bufferUnit = Helper.getPredecessorOf(units, bufferUnit);
			}catch(NoSuchElementException e){
				bufferUnit = null;
			}
		}
	}

	
	private void runElsePart(Unit target, Unit firstBoxAfterBlock, final PatchingChain<Unit> units, Set<Info> resultInfosBeforeBlockStarts, MyStmtSwitch stmtSwitch, Set<SootField> previousFieldsBeforeBlockStarts, Body body){
		if(!Helper.processMethod(units.size())){
			return;
		}
		if (Thread.currentThread().isInterrupted()) {
			return;
		}
		Unit iterUnit = target;
		
		LinkedList<Unit> iterUnits = new LinkedList<Unit>();
		
		while (iterUnit != null && !Thread.currentThread().isInterrupted()){
			if (switchesThatWereProcessed.contains(iterUnit) || iterUnits.contains(iterUnit)){
				break;
			}
			if ((iterUnit instanceof ThrowStmt) || (iterUnit instanceof ReturnStmt)|| (iterUnit instanceof ReturnVoidStmt) || iterUnit.equals(firstBoxAfterBlock)){
				iterUnits.addFirst(iterUnit);
				break;
			}else{
				if (iterUnit instanceof GotoStmt){
					GotoStmt gotoStmt = (GotoStmt)iterUnit;
					if ((firstBoxAfterBlock != null ) && gotoStmt.getTarget().equals(firstBoxAfterBlock))
						break;
					else{
						iterUnit = ((GotoStmt)iterUnit).getTarget();
					}
				}else{
					// add always the first so that always the last unit is the first
					iterUnits.addFirst(iterUnit);
					
					iterUnit = Helper.getSuccessorOf(units, iterUnit);
					
				}
			}
		}
			
		//TODO: Tricky Place
		Set<Info> savedResults = stmtSwitch.getResultInfos();

		Set<SootField> prevFields = new HashSet<>(stmtSwitch.getPreviousFields());
		if(stmtSwitch instanceof StmtSwitchForLayoutInflater){
			StmtSwitchForLayoutInflater stmtSwicthForLayouts = (StmtSwitchForLayoutInflater) stmtSwitch;
			Map<Integer, LayoutInfo> resultLayouts = stmtSwicthForLayouts.getResultedLayouts();
			Set<TabViewInfo> resultedTabs = stmtSwicthForLayouts.getResultedTabs();
			stmtSwicthForLayouts.init();
			stmtSwicthForLayouts.putAllToResultedLayouts(resultLayouts);
			stmtSwicthForLayouts.addAllToResultedTabs(resultedTabs);

		}
		else {
			stmtSwitch.init();
		}
		stmtSwitch.addResultInfos(resultInfosBeforeBlockStarts);
		// if the previousFieldsBeforeBlockStarts.size == 0, then it could be that no firstUnitAfterBlock was found
			// that means that we are directly inside an if or else part (eg: interprocCalls)
			// in that case we should not delete the previous fields
		if (firstBoxAfterBlock == null){
			stmtSwitch.setPreviousFields(prevFields);
		}else{
			stmtSwitch.setPreviousFields(previousFieldsBeforeBlockStarts);
		}
		
		runUnitsOverSwitchWithForLoop(units, iterUnits, stmtSwitch, body);
		
		stmtSwitch.addResultInfos(savedResults);	
	}
	
	// run over the found units (not over predecessors because they could differ (-> goto stmt)
	private void runUnitsOverSwitchWithForLoop(final PatchingChain<Unit> units, List<Unit> iterUnits, MyStmtSwitch stmtSwitch, Body body){
		if(!Helper.processMethod(units.size())){
			return;
		}
		if (Thread.currentThread().isInterrupted()) {
			return;
		}
		Set<Info> resultInfosBeforeBlockStarts = new LinkedHashSet<>();
		Set<SootField> previousFieldsBeforeBlockStarts = new HashSet<SootField>();
		Unit firstBoxAfterBlock = null;
		// the units are from bottom to top unit in the list
		for (final Unit bufferUnit : iterUnits){
			if(Thread.currentThread().isInterrupted()){
				return;
			}
			runSwitch(bufferUnit, stmtSwitch, firstBoxAfterBlock, units, resultInfosBeforeBlockStarts, previousFieldsBeforeBlockStarts, body);
		}
	}
	
	private void runSwitch (Unit bufferUnit, MyStmtSwitch stmtSwitch, Unit firstBoxAfterBlock, PatchingChain<Unit> units, Set<Info> resultInfosBeforeBlockStarts, Set<SootField> previousFieldsBeforeBlock, Body body){
		if(Thread.currentThread().isInterrupted()){
			return;
		}
		if(!Helper.processMethod(units.size())){
			return;
		}
		bufferUnit.apply(stmtSwitch);
		
		// search the start of the block
		if (!switchesThatWereProcessed.contains(bufferUnit) && !Thread.currentThread().isInterrupted() /*&& stmtSwitch.run()*/
				&& ((bufferUnit instanceof IfStmt) || (bufferUnit instanceof TableSwitchStmt) || (bufferUnit instanceof LookupSwitchStmt))){


			if (bufferUnit instanceof IfStmt){
				// check: if __ goto label1(bufferUnit.getTargetbox().getUnit()); label1: (SuccOf(buffUnit))...
				// TODO this if condition should never be false, because this is checked in the find1stBoxPart
				if (!(((IfStmt)bufferUnit).getTargetBox().getUnit().equals(Helper.getSuccessorOf(units, bufferUnit)))){
					// run the else part of the IfStmt
					switchesThatWereProcessed.add(bufferUnit);
					runElsePart(((IfStmt)bufferUnit).getTargetBox().getUnit(), firstBoxAfterBlock, units, resultInfosBeforeBlockStarts, stmtSwitch,previousFieldsBeforeBlock, body);
					switchesThatWereProcessed.remove(bufferUnit);
				}
			}else if (bufferUnit instanceof TableSwitchStmt){
				switchesThatWereProcessed.add(bufferUnit);
				List<Unit> targets = ((TableSwitchStmt)bufferUnit).getTargets();
				for (Unit u : targets){
					if(Thread.currentThread().isInterrupted()){
						return;
					}
					runElsePart(u, firstBoxAfterBlock, units, resultInfosBeforeBlockStarts, stmtSwitch,previousFieldsBeforeBlock,  body);
				}
				switchesThatWereProcessed.remove(bufferUnit);
			}else if (bufferUnit instanceof LookupSwitchStmt){
				switchesThatWereProcessed.add(bufferUnit);
				List<Unit> targets = ((LookupSwitchStmt)bufferUnit).getTargets();
				for (Unit u : targets){
					if(Thread.currentThread().isInterrupted()){
						return;
					}
					runElsePart(u, firstBoxAfterBlock, units, resultInfosBeforeBlockStarts, stmtSwitch, previousFieldsBeforeBlock, body);
				}
				switchesThatWereProcessed.remove(bufferUnit);
			}
		}
		Unit predecessorUnit = null;
		try{
			predecessorUnit = Helper.getPredecessorOf(units, bufferUnit);
		}catch(NoSuchElementException e){
			predecessorUnit = null;
		}

		// get the immediate domminator
        SootMethod m = body.getMethod();
        Unit immediateDominator = getImmediateDominatorFromCache(bufferUnit, m);
		// check if inside the map was a dominator
		if(immediateDominator == null){
            DominatorsFinder<Unit> dominatorsFinder = new MHGDominatorsFinder<>(new ExceptionalUnitGraph(body));
            try {
                // get immediate domminator
                immediateDominator = dominatorsFinder.getImmediateDominator(bufferUnit);
            }
            catch (NullPointerException e){
                //weird case.... happens.. don't know why...
                immediateDominator = predecessorUnit;
                Helper.saveToStatisticalFile(String.format("Can not find immediate postdominator for unit %s in method %s", bufferUnit, Helper.getSignatureOfSootMethod(m)));
            }
			// save domminator in map
            Map<Unit, Unit> toSave = new HashMap<>();
            toSave.put(bufferUnit, immediateDominator);

            if(!Helper.immediateDominators.containsKey(m)){
                Helper.immediateDominators.put(m, toSave);
            }
            else {
                Helper.immediateDominators.get(m).put(bufferUnit, immediateDominator);
            }
		}
		Unit immediateDomUnit = immediateDominator;
		
		// if immediateDomminator != predecessor unit, there starts the units of an if stmt/switch
			// !startBlock will stop copying the list again and again inside the block
		if (immediateDomUnit != null && predecessorUnit != null && (!immediateDomUnit.equals(predecessorUnit))){
			if (immediateDomUnit instanceof IfStmt){
				IfStmt ifstmt = (IfStmt)immediateDomUnit;
				Unit target = ifstmt.getTarget();
				// check: if __ goto label1(target); label1: (buffUnit)...
				if (!bufferUnit.equals(target)){
					firstBoxAfterBlock = bufferUnit;
					for(Info p : stmtSwitch.getResultInfos()) {
						resultInfosBeforeBlockStarts.add(p.clone());
					}
					if(stmtSwitch.getPreviousFields() != null)
						previousFieldsBeforeBlock.addAll(stmtSwitch.getPreviousFields());
				}
			}else if (immediateDomUnit instanceof TableSwitchStmt){
				TableSwitchStmt tableSwitchStmt = (TableSwitchStmt) immediateDomUnit;
				List<Unit> targets = tableSwitchStmt.getTargets();
				for (Unit target : targets){
					if (!bufferUnit.equals(target)){
						firstBoxAfterBlock = bufferUnit;
						for(Info p : stmtSwitch.getResultInfos()) {
							resultInfosBeforeBlockStarts.add(p.clone());
						}
						if(stmtSwitch.getPreviousFields() != null)
							previousFieldsBeforeBlock.addAll(stmtSwitch.getPreviousFields());
					}
//					break;
				}
			}else if (immediateDomUnit instanceof LookupSwitchStmt){
				LookupSwitchStmt lookupSwitchStmt = (LookupSwitchStmt) immediateDomUnit;
				List<Unit> targets = lookupSwitchStmt.getTargets();
				for (Unit target : targets){
					if (!bufferUnit.equals(target)){
						firstBoxAfterBlock = bufferUnit;
						for(Info p : stmtSwitch.getResultInfos()) {
							resultInfosBeforeBlockStarts.add(p.clone());
						}
						if(stmtSwitch.getPreviousFields() != null)
							previousFieldsBeforeBlock.addAll(stmtSwitch.getPreviousFields());
					}
//					break;
				}
			}
		}
	}

    private Unit getImmediateDominatorFromCache(Unit bufferUnit, SootMethod m) {
		if(!Helper.immediateDominators.containsKey(m)){
			return  null;
		}
        return Helper.immediateDominators.get(m).get(bufferUnit);
    }
}
