package st.cs.uni.saarland.de.helpMethods;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import soot.*;
import soot.toolkits.graph.MHGDominatorsFinder;
import st.cs.uni.saarland.de.helpClasses.Helper;
import st.cs.uni.saarland.de.helpClasses.Info;
import st.cs.uni.saarland.de.helpClasses.MyStmtSwitch;
import st.cs.uni.saarland.de.searchScreens.LayoutInfo;
import st.cs.uni.saarland.de.searchScreens.StmtSwitchForReturnedLayouts;

import java.util.*;

public class InterprocAnalysis {

	private static final InterprocAnalysis interprocAnalysis = new InterprocAnalysis();
	private final Logger logger =  LoggerFactory.getLogger(Thread.currentThread().getName());
	
	 public static InterprocAnalysis getInstance(){
		 return interprocAnalysis;
	 }
	 
	 //TODO resolve if clauses in backward, if "ifClause" clone dataset, run on if and then on else branch
	 public Map<Integer, LayoutInfo> runFragmentGetViewSwitchOverUnits(StmtSwitchForReturnedLayouts stmtSwitch, SootMethod method){
		 Map<Integer, LayoutInfo> results = new HashMap<Integer, LayoutInfo>();
		 if(method == null)
			 return results;
		 
		if (method.hasActiveBody()){
			
			
			stmtSwitch.init();
			stmtSwitch.setCurrentSootMethod(method);
			IterateOverUnitsHelper.newInstance().runUnitsOverMethodBackwards(method.getActiveBody(), stmtSwitch);
			Map<Integer, LayoutInfo> infoList = stmtSwitch.getResultLayoutInfos();
			if(infoList != null && infoList.size() > 0){
				results.putAll(infoList);
			}
			
			
//			//System.out.println( b.toString());
//			PatchingChain<Unit> units = b.getUnits();
//			Unit end = units.getLast();
//			
//			MHGDominatorsFinder<Unit> dominatorsFinder = new MHGDominatorsFinder<Unit>(new ExceptionalUnitGraph(b));
//			
//			//We use forward traversing of units. Whenever we found a returnStmt we should go backward and find the value that it returns
//			for(final Iterator<Unit> iter = units.snapshotIterator(); iter.hasNext();) {
//				final Unit u = iter.next();
//				u.apply(stmtSwitch);
//				if(stmtSwitch.run()){
//					//It means that we found a return
//					//let's iterate backward and find initializations of return values
//					Unit currentUnit = dominatorsFinder.getImmediateDominator(u);
//					while(currentUnit != null && stmtSwitch.run()){
//						currentUnit.apply(stmtSwitch);
//						currentUnit = dominatorsFinder.getImmediateDominator(currentUnit);
//					}
//				}
//
//				if(!stmtSwitch.run()){
//					Map<Integer, LayoutInfo> infoList = stmtSwitch.getResultLayoutInfos();
//					if(infoList != null && infoList.size() > 0){
//						results.putAll(infoList);
//					}
//					//start from the scratch and search the next return
//					stmtSwitch = new StmtSwitchForReturnedLayouts(new ArrayList<SootMethod>(), m);
//				}
//			}
		}
		
		return results;
	 }
	 
	 
	 public List<Info> runStmtSwitchOnSpecificMethodForward(MyStmtSwitch stmtSwitch, String methodSignature){
		 	List<Info> resultList = new ArrayList<Info>();

			 SootMethod m = null;
			 Body body = null;
			 try{
				 if (Scene.v().containsMethod(methodSignature)){
					 m = Scene.v().getMethod(methodSignature);
					 stmtSwitch.setCurrentSootMethod(m);
					 body = m.retrieveActiveBody();
				 }
			 }catch(Exception e){
				 e.printStackTrace();
				 logger.error("Method not found or Wrong level of resolution: " + methodSignature);
			}
			 
			if (body != null){
				
				
				
				stmtSwitch.init();
				IterateOverUnitsHelper.newInstance().runUnitsOverMethodBackwards(body, stmtSwitch);
				Set<Info> res =  stmtSwitch.getResultInfos();
				resultList.addAll(res);
				
							
				
//				//System.out.println( b.toString());
//				PatchingChain<Unit> units = body.getUnits();
//				
//				
//				String methodName = body.getMethod().getName();
////				stmtSwitch.setCaller(body.getMethod().getName());
//				Body initMethod = null;
//				// store the init function body in initMethod so that it can be again produced
//					// if a variable is found as listener
//				if(methodName.equals("<init>")){
//					initMethod = body;
//				}
//				
//				stmtSwitch.setUnits(units);
//				
//				for(final Iterator<Unit> iter = units.snapshotIterator(); iter.hasNext();) {
//					MHGDominatorsFinder<Unit> dominatorsFinder = new MHGDominatorsFinder<Unit>(new ExceptionalUnitGraph(body));
//					
//					Unit currentUnit = iter.next();
//					stmtSwitch.init();
//					// run the switch on the currentUnit (pred(currentUnit))
//					// if it is a set..Listener method, then all predecessor are viewed to get all information about the listener
//				
//					this.runSwitch(currentUnit, units, stmtSwitch, dominatorsFinder);			
//					// get the results of the switch
//					// in ...Info all information about the results are stored
//					Set<Info> res =  stmtSwitch.getResultInfos();
////					if (res != null){
////						// check if all necessary information are found
////						if (res.shouldRunOnInitMethod()){
////							// assume that the searchedReg was not found in this method because the object
////								// is a varibale of the class, so search for this variable in the init method
////							
////							if (initMethod != null){
////								// run <init> from the last to the first unit/stmt
////								PatchingChain<Unit> initMethodUnits = initMethod.getUnits();
////								Unit iterUnit = initMethodUnits.getLast();
////								while(iterUnit != null){
////									iterUnit.apply(stmtSwitch);
////									try{
////										iterUnit = units.getPredOf(iterUnit);
////										// catch this exception, it is thrown if PredOf is null
////									}catch(NoSuchElementException e){
////										iterUnit = null;
////									}
////								}
////							}
////						}
//						resultList.addAll(res);
////					}
//				}
			}
			return resultList;
	}
	 
	 
	 // run the switch over the the current unit,
		// if the current unit is a method that the tool searched
		// the pred(currentUnit) will be ran with the stmtSwitch
		// further up (analyse all predecessors, prepredecessors), up to the point where it found all needed information about the seached unit
		// or their are no predecessors left
	protected void runSwitch(Unit currentUnit, final PatchingChain<Unit> units, MyStmtSwitch stmtSwitch, MHGDominatorsFinder<Unit> dominatorsFinder){
			Unit bufferUnit = currentUnit;
//			boolean condition = units.getPredOf(bufferUnit) != null;
			boolean condition = true;
			while(condition){
				
				bufferUnit.apply(stmtSwitch);
				
				if (!stmtSwitch.run()){
					break;
				}
				
				Set<Info> q = stmtSwitch.getResultInfos();
				Unit predecessorUnit = null;
				if(q.size() == 0){
					condition = Helper.getPredecessorOf(units, bufferUnit) != null;
					predecessorUnit = Helper.getPredecessorOf(units, bufferUnit);
				}
				else{
					condition = dominatorsFinder.getImmediateDominator(bufferUnit) != null;
					predecessorUnit = dominatorsFinder.getImmediateDominator(bufferUnit);
				}
				
				bufferUnit = predecessorUnit;
			}
		
//		Unit bufferUnit = currentUnit;
//		while(bufferUnit != null){
//			bufferUnit.apply(stmtSwitch);
//			if (stmtSwitch.isShouldBreak()){
////					//System.out.println("break");
//				break;
//			}
////				//System.out.println("buff: " + bufferUnit);
//			try{
//				bufferUnit = units.getPredOf(bufferUnit);
//			}catch(NoSuchElementException e){
//				bufferUnit = null;
//			}
//		}
	}	
}
