package st.cs.uni.saarland.de.helpMethods;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import soot.*;
import soot.jimple.*;
import soot.jimple.internal.JimpleLocal;
import soot.jimple.toolkits.callgraph.Edge;
import soot.tagkit.IntegerConstantValueTag;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.MHGDominatorsFinder;
import st.cs.uni.saarland.de.entities.FieldInfo;
import st.cs.uni.saarland.de.helpClasses.*;
import st.cs.uni.saarland.de.searchScreens.LayoutInfo;
import st.cs.uni.saarland.de.searchScreens.StmtSwitchForLayoutInflater;
import st.cs.uni.saarland.de.searchScreens.StmtSwitchForReturnedLayouts;
import st.cs.uni.saarland.de.testApps.Content;

import java.util.*;
import java.util.stream.Collectors;

public class InterprocAnalysis2 {


	private static final InterprocAnalysis2 interprocAnalysis = new InterprocAnalysis2();
	private final Logger logger = LoggerFactory.getLogger(Thread.currentThread().getName());
	private StmtSwitch helpMethods = StmtSwitch.getInstance();

	public static InterprocAnalysis2 getInstance() {
		return interprocAnalysis;
	}

	public List<InterProcInfo> findReturnValueInMethod2(Stmt stmt){
		return findReturnValueInMethod2(stmt, (String)null);
	}

	public List<InterProcInfo> findReturnValueInMethod2(Stmt stmt, String callerSootClass) {
		List<InterProcInfo> results = new ArrayList<InterProcInfo>();
		if (!stmt.containsInvokeExpr()) {
			return results;
		}
		InvokeExpr expr = stmt.getInvokeExpr();
		//use putString, putBoolean and so on..
		if (Helper.bundlesAndParsable.containsKey(Helper.getSignatureOfSootMethod(expr.getMethod()))) {
			String searchedSignature = Helper.bundlesAndParsable.get(Helper.getSignatureOfSootMethod(expr.getMethod()));
			if ((expr.getArg(0) instanceof StringConstant)) {
				String argValue = ((StringConstant) expr.getArg(0)).value;
				SootMethod targetMethod = Scene.v().getMethod(searchedSignature);
				List<Info> resList = findInReachableMethods2(1, targetMethod, new ArrayList<>(), argValue, 0);
				resList.forEach(x -> results.add((InterProcInfo) x));
			} else {
				results.addAll(findReturnValueInMethod2(stmt, new ArrayList<>(), callerSootClass));
			}
		} else {
			results.addAll(findReturnValueInMethod2(stmt, new ArrayList<>(), callerSootClass));
		}
		return results;
	}

	public List<InterProcInfo> findReturnValueInMethod2(Stmt stmt, List<SootMethod> callStack){
		return findReturnValueInMethod2(stmt, callStack, null);
	}

	public List<InterProcInfo> findReturnValueInMethod2(Stmt stmt, List<SootMethod> callStack, String callerSootClass) {
		List<InterProcInfo> results = new ArrayList<>();
		Iterator<Edge> iteratorOverEdges = Scene.v().getCallGraph().edgesOutOf(stmt);
		while (iteratorOverEdges.hasNext()) {
			Edge edge = iteratorOverEdges.next();
			SootMethod m = edge.tgt();
			if(callerSootClass != null) {
				if (!Scene.v().getActiveHierarchy().getSuperclassesOfIncluding(Scene.v().getSootClass(callerSootClass)).contains(m.getDeclaringClass())) {
					continue;
				}
				if (!Scene.v().getSootClass(callerSootClass).equals(m.getDeclaringClass()) && Scene.v().getSootClass(callerSootClass).getMethodUnsafe(m.getSubSignature()) != null){
					continue;
				}
			}
			if (m.hasActiveBody()) {
				StmtSwitchForInterProcCalls stmtSwitch = new StmtSwitchForInterProcCalls(new ArrayList<>(callStack), m);
				Body body = m.getActiveBody();

				IterateOverUnitsHelper iterHelper = IterateOverUnitsHelper.newInstance();
				iterHelper.runUnitsOverMethodBackwards(body, stmtSwitch);


				Set<Info> infoList = stmtSwitch.getResultInfos();
				if (infoList != null && infoList.size() > 0) {
					//Let's save the values
					for (Info info : infoList) {
						if (info instanceof InterProcInfo) {
							results.add((InterProcInfo) info);
						}
					}
				}
			}
		}
		return results;
	}

	//public List<InterProcInfo> findReturnValueOfMethod(

	public List<String> findReturnValueInMethodForClassArrays(Stmt stmt, Value arrayReg, int index) {
		List<String> results = new ArrayList<>();
		Iterator<Edge> iteratorOverEdges = Scene.v().getCallGraph().edgesOutOf(stmt);
		while (iteratorOverEdges.hasNext()) {
			Edge edge = iteratorOverEdges.next();
			SootMethod m = edge.tgt();
			if (m.hasActiveBody()) {
				StmtSwitchForClassArrays stmtSwitch = new StmtSwitchForClassArrays(index, arrayReg, m);
				stmtSwitch.setSearchInReturn(true);
				Body body = m.getActiveBody();
				IterateOverUnitsHelper iterHelper = IterateOverUnitsHelper.newInstance();
				iterHelper.runUnitsOverMethodBackwards(body, stmtSwitch);

				results.addAll(stmtSwitch.getArrayResults());
			}
		}
		return results;
	}

	public List<Integer> findReturnValueInMethodForArrays(Stmt stmt, Value arrayReg, int index) {
		List<Integer> results = new ArrayList<>();
		Iterator<Edge> iteratorOverEdges = Scene.v().getCallGraph().edgesOutOf(stmt);
		while (iteratorOverEdges.hasNext()) {
			Edge edge = iteratorOverEdges.next();
			SootMethod m = edge.tgt();
			if (m.hasActiveBody()) {
				StmtSwitchForArrays stmtSwitch = new StmtSwitchForArrays(index, arrayReg, m);
				stmtSwitch.setSearchInReturn(true);
				Body body = m.getActiveBody();
				IterateOverUnitsHelper iterHelper = IterateOverUnitsHelper.newInstance();
				iterHelper.runUnitsOverMethodBackwards(body, stmtSwitch);

				results.addAll(stmtSwitch.getArrayResults());
			}
		}
		return results;
	}

	public Map<Integer, LayoutInfo> findReturnValueInMethodForLayouts(Stmt stmt, Set<SootMethod> callStack) {
		Map<Integer, LayoutInfo> results = new HashMap<Integer, LayoutInfo>();
		Iterator<Edge> iteratorOverEdges = Scene.v().getCallGraph().edgesOutOf(stmt);
		while (iteratorOverEdges.hasNext()) {
			Edge edge = iteratorOverEdges.next();
			SootMethod m = edge.tgt();
			if (m.hasActiveBody()) {
				StmtSwitchForReturnedLayouts stmtSwitch = new StmtSwitchForReturnedLayouts(new HashSet<SootMethod>(callStack), m);
				Body body = m.getActiveBody();
				IterateOverUnitsHelper iterHelper = IterateOverUnitsHelper.newInstance();
				iterHelper.runUnitsOverMethodBackwards(body, stmtSwitch);

				Map<Integer, LayoutInfo> infoList = stmtSwitch.getResultLayoutInfos();
				if (infoList != null && infoList.size() > 0) {
					results.putAll(infoList);
				}
			}
		}
		return results;
	}

	public List<String> findClassArrayInitInReachableMethods(int arrayIndex, int argumentIndex, SootMethod method, List<SootMethod> callStack, boolean searchInReturn) {
		List<String> res = new ArrayList<>();
		callStack.add(method);

		List<SootMethod> localCallStack = new ArrayList<SootMethod>();
		localCallStack.addAll(callStack);

		final Iterator<Edge> iteratorOverCallers = Scene.v().getCallGraph().edgesInto(method);
		while (iteratorOverCallers.hasNext()) {

			callStack.clear();
			callStack.addAll(localCallStack);

			final Edge e = iteratorOverCallers.next();
			Stmt stmt = e.srcStmt();
			MethodOrMethodContext caller = e.getSrc();

			// get the value of the argument of the stmt which called the analysed method/stmt
			Value arrayReg = stmt.getInvokeExpr().getArg(argumentIndex);
			StmtSwitchForClassArrays switchForArrays = new StmtSwitchForClassArrays(arrayIndex, arrayReg, method);
			switchForArrays.setSearchInReturn(searchInReturn);
			IterateOverUnitsHelper.newInstance().runUnitsOverMethodBackwards(caller.method().getActiveBody(), switchForArrays);
			res.addAll(switchForArrays.getArrayResults());

		}
		return res;
	}

	public List<Integer> findArrayInitInReachableMethods(int arrayIndex, int argumentIndex, SootMethod method, List<SootMethod> callStack, boolean searchInReturn) {
		List<Integer> res = new ArrayList<>();
		callStack.add(method);

		List<SootMethod> localCallStack = new ArrayList<SootMethod>();
		localCallStack.addAll(callStack);

		final Iterator<Edge> iteratorOverCallers = Scene.v().getCallGraph().edgesInto(method);
		while (iteratorOverCallers.hasNext()) {

			callStack.clear();
			callStack.addAll(localCallStack);

			final Edge e = iteratorOverCallers.next();
			Stmt stmt = e.srcStmt();
			MethodOrMethodContext caller = e.getSrc();

			if(caller != null && caller.method() != null && caller.method().getDeclaringClass().getName().equals("dummyMainClass"))
				continue;

			// get the value of the argument of the stmt which called the analysed method/stmt
			Value arrayReg = stmt.getInvokeExpr().getArg(argumentIndex);
			StmtSwitchForArrays switchForArrays = new StmtSwitchForArrays(arrayIndex, arrayReg, method);
			switchForArrays.setSearchInReturn(searchInReturn);
			IterateOverUnitsHelper.newInstance().runUnitsOverMethodBackwards(caller.method().getActiveBody(), switchForArrays);
			res.addAll(switchForArrays.getArrayResults());

		}
		return res;
	}


		public List<Info> findInReachableMethods2(int argumentIndex, SootMethod method, List<SootMethod> callStack) {
		return findInReachableMethods2(argumentIndex, method, callStack, null, -1);
	}

	public List<Info> findInReachableMethods2(int argumentIndex, SootMethod method, List<SootMethod> callStack, String valueToCompare, int indexToCompare) {
		List<Info> res = new ArrayList<Info>();
		callStack.add(method);

		List<SootMethod> localCallStack = new ArrayList<SootMethod>();
		localCallStack.addAll(callStack);

		final Iterator<Edge> iteratorOverCallers = Scene.v().getCallGraph().edgesInto(method);
		while (iteratorOverCallers.hasNext() && !Thread.currentThread().isInterrupted()) {

			callStack.clear();
			callStack.addAll(localCallStack);

			InterProcInfo info = new InterProcInfo("");
			final Edge e = iteratorOverCallers.next();
			Stmt stmt = e.srcStmt();
			MethodOrMethodContext caller = e.src();

			if(caller != null && caller.method() != null && caller.method().getDeclaringClass().getName().equals("dummyMainClass"))
				continue;

			if (argumentIndex > stmt.getInvokeExpr().getArgCount() - 1) {
				continue;
			}

			if (valueToCompare != null && indexToCompare != -1 && indexToCompare <= stmt.getInvokeExpr().getArgCount()) {
				if (!(stmt.getInvokeExpr().getArg(indexToCompare) instanceof StringConstant)) {
					continue;
				} else {
					String argValue = ((StringConstant) stmt.getInvokeExpr().getArg(indexToCompare)).value;
					if (!argValue.equals(valueToCompare)) {
						continue;
					}
				}
			}

			// get the value of the argument of the stmt which called the analysed method/stmt
			Value argument = stmt.getInvokeExpr().getArg(argumentIndex);
			if (argument instanceof IntConstant) {
				info.setValueOfSearchedReg(argument.toString());
				res.add(info);
				continue;
			} else if (argument instanceof StringConstant) {
				String value = ((StringConstant) argument).value;
				info.setValueOfSearchedReg(value);
				res.add(info);
				continue;
			} else {
				info.setSearchedEReg(argument.toString());

				final Body body = caller.method().getActiveBody();
				Unit workingUnit = e.srcUnit();

				StmtSwitchForInterProcCalls stmtSwitch = new StmtSwitchForInterProcCalls(callStack, info, caller.method());
				IterateOverUnitsHelper.newInstance().runOverToFindSpecValuesBackwards(body, workingUnit, stmtSwitch);
				//try to find where the value has been initialized
				Set<Info> newInfos = stmtSwitch.getResultInfos();
				if (newInfos.size() > 0) {
					List<Info> listInfo = newInfos.stream().collect(Collectors.toList());
					if(listInfo.indexOf(info) == -1){
						continue;
					}
					Info initInfo = listInfo.get(listInfo.indexOf(info));
					res.add(initInfo);
				}
			}

		}
		return res;
	}


	public Map<Integer, LayoutInfo> findInReachableMethodsForLayouts(int argumentIndex, SootMethod method, Set<SootMethod> callStack) {
		Map<Integer, LayoutInfo> res = new HashMap<Integer, LayoutInfo>();
		if (Thread.currentThread().isInterrupted()) {
			return res;
		}
		callStack.add(method);
		List<SootMethod> localCallStack = new ArrayList<SootMethod>();
		localCallStack.addAll(callStack);

		final Iterator<Edge> edges = Scene.v().getCallGraph().edgesInto(method);
		while (edges.hasNext() && !Thread.currentThread().isInterrupted()) {

			callStack.clear();
			callStack.addAll(localCallStack);

			LayoutInfo info = new LayoutInfo("", Content.getInstance().getNewUniqueID());
			// set isFragment, because with this way, the tool could differenciate between added layouts and the rootLayout that we are searching
			info.setFragment();
			final Edge e = edges.next();
			Stmt stmt = e.srcStmt();
			MethodOrMethodContext caller = e.src();

			if(caller != null && caller.method() != null && caller.method().getDeclaringClass().getName().equals("dummyMainClass"))
				continue;

			if (argumentIndex > stmt.getInvokeExpr().getArgCount() - 1) {
				continue;
			}

			// get the value of the argument of the stmt which called the analysed method/stmt
			Value argument = stmt.getInvokeExpr().getArg(argumentIndex);
			if ((argument instanceof IntConstant)) {
				info.setLayoutID(argument.toString());
				res.put(info.getID(), info);
				continue;
			} else {
				info.setSearchedEReg(argument.toString());

				final Body body = caller.method().getActiveBody();
				Unit workingUnit = e.srcUnit();

				StmtSwitchForLayoutInflater stmtSwitch = new StmtSwitchForLayoutInflater(callStack, info, caller.method());
				//try to find where the value has been initialized
				IterateOverUnitsHelper.newInstance().runOverToFindSpecValuesBackwards(body, workingUnit, stmtSwitch);
				Map<Integer, LayoutInfo> newInfos = stmtSwitch.getResultLayoutInfos();
				newInfos.keySet().forEach(id->{
					LayoutInfo value = newInfos.get(id);
					if(value.equals(info)){
						res.put(id, value);
					}
				});
			}

		}
		return res;
	}

	public Set<FieldInfo> findInitializationsOfTheField2(SootField f, AssignStmt currentStmt, SootMethod currentMethod) {
		if (Thread.currentThread().isInterrupted()) {
			return new HashSet<>();
		}
		if (Helper.resolvedFields.containsKey(f)) {
			return Helper.resolvedFields.get(f);
		}
		if (f.isStatic() && f.getType() instanceof IntType) {
			String result = analyzeStaticField(f);
			if (result != null) {
				Set<FieldInfo> fInfos = new HashSet<FieldInfo>();
				FieldInfo fInfo = new FieldInfo();
				fInfo.value = result;
				fInfos.add(fInfo);
				Helper.resolvedFields.put(f, fInfos);
				return fInfos;
			}
		}
		if (f.isStatic() && f.getDeclaringClass().getMethodByNameUnsafe("<clinit>") != null) {
			//has to be defined in the init
			List<FieldInfo> fInfos = findInFieldInMethod(f, f.getDeclaringClass().getMethodByName("<clinit>"), currentStmt);
			if (fInfos.size() > 0) {
				Set<FieldInfo> distinctFields = fInfos.stream().distinct().collect(Collectors.toSet());
				Helper.resolvedFields.put(f, distinctFields);
				return distinctFields;
			}
		}

		//worth case..

		List<FieldInfo> result = new ArrayList<>();
		//start with the current method and then proceed with others if needed
		result.addAll(findInFieldInMethod(f, currentMethod, currentStmt));
		if(!result.isEmpty()){
			return result.stream().distinct().collect(Collectors.toSet());
		}

		//otherwise search in other methods of the same class
		SootClass currentClass = currentMethod.getDeclaringClass();
		for (SootMethod m : currentClass.getMethods()) {
			result.addAll(findInFieldInMethod(f, m.method(), currentStmt));
		}
		Set<FieldInfo> distinctFields = result.stream().distinct().collect(Collectors.toSet());
		Helper.resolvedFields.put(f, distinctFields);
		return distinctFields;
	}

	public Set<FieldInfo> findInitializationsOfTheFieldForButtonClicks(SootField f, AssignStmt currentStmt, SootClass currentClass) {
		if (Thread.currentThread().isInterrupted()) {
			return new HashSet<>();
		}
		if (Helper.resolvedFields.containsKey(f)) {
			return Helper.resolvedFields.get(f);
		}

		List<FieldInfo> result = new ArrayList<>();

		for (SootMethod m : currentClass.getMethods()) {
			result.addAll(findInFieldInMethod(f, m.method(), currentStmt));
		}

		Set<FieldInfo> distinctFields = result.stream().distinct().collect(Collectors.toSet());
		Helper.resolvedFields.put(f, distinctFields);
		return distinctFields;
	}


	private List<FieldInfo> findInFieldInMethod(SootField f, SootMethod method, AssignStmt currentStmt) {
		if(!method.hasActiveBody()){
			return new ArrayList<>();
		}
		synchronized (this) {
			List<FieldInfo> fInfos = new ArrayList<FieldInfo>();
			if (Thread.currentThread().isInterrupted()) {
				return fInfos;
			}
			final PatchingChain<Unit> units = method.method().getActiveBody().getUnits();
			for (Unit u : units) {
				Stmt stmt = (Stmt) u;
				if (stmt.containsFieldRef() && stmt.getFieldRef().getField().equals(f) && stmt instanceof AssignStmt && !stmt.equals(currentStmt)) {
					AssignStmt asStmt = (AssignStmt) stmt;
					if (asStmt.getLeftOp() instanceof FieldRef) {
						SootField internalField = ((FieldRef) asStmt.getLeftOp()).getField();
						if (internalField.equals(f)) {
							if (asStmt.getRightOp() instanceof JimpleLocal) {
								FieldInfo fInfo = new FieldInfo();
								fInfo.register = (JimpleLocal) asStmt.getRightOp();
								fInfo.methodToStart = method;
								fInfo.unitToStart = u;
								fInfo.className = fInfo.register.getType().toString();
								fInfos.add(fInfo);
							} else if (asStmt.getRightOp() instanceof IntConstant) {
								FieldInfo fInfo = new FieldInfo();
								fInfo.value = Integer.toString(((IntConstant) asStmt.getRightOp()).value);
								fInfos.add(fInfo);
							} else if (!(asStmt.getRightOp() instanceof NullConstant)) {
								FieldInfo fInfo = new FieldInfo();
								fInfo.value = asStmt.getRightOp().toString();
								fInfos.add(fInfo);
							}
						}
					}
				}
			}


			return fInfos;
		}
	}


	private String returnValue;

	// returns id?
	private String analyzeStaticField(SootField f) {
		if (Thread.currentThread().isInterrupted()) {
			return null;
		}
		if (f.getType() instanceof IntType) {
			IntegerConstantValueTag t = (IntegerConstantValueTag) f.getTag(IntegerConstantValueTag.class.getSimpleName());
			if (t != null) {
				int resValue = t.getIntValue();
				return Integer.toString(resValue);
			} else {
				//if(f.getDeclaringClass().isFinal()){
				if (f.getDeclaringClass().declaresMethodByName("<clinit>")) {
					SootMethod clinit = f.getDeclaringClass().getMethodByName("<clinit>");
					if (clinit.hasActiveBody()) {
						Body b = clinit.getActiveBody();
						final PatchingChain<Unit> unitsOfClinit = b.getUnits();
						for (final Iterator<Unit> iter = unitsOfClinit.snapshotIterator(); iter.hasNext(); ) {
							if (Thread.currentThread().isInterrupted()) {
								continue;
							}
//									if(quadripel.ready()){
//										return;
//									}
							final Unit u = iter.next();
							u.apply(new AbstractStmtSwitch() {
								public void caseAssignStmt(AssignStmt stmt) {
									if (stmt.getLeftOp() instanceof FieldRef && stmt.getRightOp() instanceof IntConstant) {
										SootField fieldInTheClinit = ((FieldRef) stmt.getLeftOp()).getField();
										if (Helper.getSignatureOfField(fieldInTheClinit).equals(Helper.getSignatureOfField(f))) {
											int resValue = ((IntConstant) stmt.getRightOp()).value;
											returnValue = Integer.toString(resValue);
										}
									}
								}
							});
							if (returnValue != null)
								return returnValue;
						}
					}
				}
				//}
			}
		}
		return null;
	}

	public List<Integer> findElementIdFromForTheView(Stmt stmt, SootMethod caller){
		List<Integer> results = new ArrayList<>();
		if(!stmt.containsInvokeExpr()){
			return  results;
		}
		Iterator<Edge> edgesOutOf = Scene.v().getCallGraph().edgesOutOf(stmt);
		while (edgesOutOf.hasNext()){
			Edge e = edgesOutOf.next();

			SootMethod targetMethod = e.tgt();

			//the task is to find findViewById and idetify the elementId
			if(!targetMethod.hasActiveBody())
				continue;

			MHGDominatorsFinder<Unit> dominatorsFinder = new MHGDominatorsFinder<>(new ExceptionalUnitGraph(targetMethod.getActiveBody()));
			Set<Unit> returnStmts = targetMethod.getActiveBody().getUnits().stream().
					filter(u-> u instanceof ReturnStmt).collect(Collectors.toSet());
			for(Unit retStm : returnStmts){
				Unit tmUnit = retStm;
				Value retReg = ((ReturnStmt)retStm).getOp();
				tmUnit = dominatorsFinder.getImmediateDominator(tmUnit);
				StmtSwitchToFindGetViews switchStmt = new StmtSwitchToFindGetViews(retReg, targetMethod ,caller);

				while (tmUnit != null && !switchStmt.isReady()){
					tmUnit.apply(switchStmt);
					tmUnit = dominatorsFinder.getImmediateDominator(tmUnit);
				}
				if(switchStmt.getResult() != -1){
					results.add(switchStmt.getResult());
				}
			}
		}
		return results;
	}
}
