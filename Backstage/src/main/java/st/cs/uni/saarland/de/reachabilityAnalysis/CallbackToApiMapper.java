package st.cs.uni.saarland.de.reachabilityAnalysis;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import soot.*;
import soot.jimple.*;
import soot.jimple.toolkits.callgraph.Edge;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.MHGDominatorsFinder;
import st.cs.uni.saarland.de.helpClasses.Helper;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.stream.IntStream;

/**
 * Class that is responsible for finding all APIs that are reachable from a given callback maethod
 */

public class CallbackToApiMapper implements Callable<Void> {
	
	private final SootMethod callbackToStart;
	private final List<ApiInfoForForward> apisFound;
	private String declaringSootClass;
	private SootClass newSootActivityClass;
	private final Logger logger;
	private final int currentId;
	private final int overall;
	private final List<String> specialMethods;
	private final String runnable;
	private final List<String> runnableMethods;
	private final List<String> osHanldeMethods;
	private final List<String> asyncTaskMethods;
	private final String elementId;
	private Set<String> sourcesAndSinksSignatures;
    private final Set<SootMethod> visitedMethods;
	private final int maxDepthMethodLevel;
	private final boolean trackCallStack;
	private final boolean limitByPackageName;
	private final Map<SootMethod, List<SootMethod>> callStack;

	// Activity
	private static final String ACTIVITYCLASS = "android.app.Activity";
	private static final String APPCOMPATACTIVITYCLASS_V4 = "android.support.v4.app.AppCompatActivity";
	private static final String APPCOMPATACTIVITYCLASS_V7 = "android.support.v7.app.AppCompatActivity";

    public CallbackToApiMapper(SootMethod callback, String elementId, int currentId, int overall, int depthMethodLevel, boolean limitByPackageName, List<ApiInfoForForward> apisFound, boolean trackStack){

        this.callbackToStart = callback;
		this.apisFound = apisFound;
		this.logger = LoggerFactory.getLogger(this.getClass().getSimpleName());
		this.currentId = currentId;
		this.overall = overall;
		this.elementId = elementId;
		this.maxDepthMethodLevel = depthMethodLevel;
		this.limitByPackageName = limitByPackageName;
		this.trackCallStack = trackStack;
		this.callStack = new HashMap<>();
		this.specialMethods = new ArrayList<>();
		specialMethods.add("android.os.AsyncTask execute(");
		//specialMethods.add("android.os.AsyncTask execute(java.lang.Runnable)");
		//TODO there's one more with special parameters
		specialMethods.add("android.os.AsyncTask executeOnExecutor(java.util.concurrent.Executor,java.lang.Object[])");
		runnable = "void <init>";
		runnableMethods = new ArrayList<>();
		runnableMethods.add("run");
		
		osHanldeMethods = new ArrayList<>();
		osHanldeMethods.add("handleMessage");
		
		asyncTaskMethods = new ArrayList<>();
		asyncTaskMethods.add("doInBackground");
		asyncTaskMethods.add("onPostExecute");

        this.visitedMethods = new HashSet<>();
		this.sourcesAndSinksSignatures = new HashSet<>();
		newSootActivityClass = null;
	}

	public void setDeclaringSootClass(String declaringSootClass) {
		this.declaringSootClass = declaringSootClass;
	}

	public void setSourcesAndSinks(Set<String> sourcesAndSinks) {
		this.sourcesAndSinksSignatures.addAll(sourcesAndSinks);
	}

	public Map<SootMethod, List<SootMethod>> getCallStack(){
		return callStack;
	}

	@Override
	public Void call() throws Exception {
		logger.info(String.format("Analyzing Callback %s out of %s", currentId, overall));
		logger.info(String.format("Reachability analysis for %s started", Helper.getSignatureOfSootMethod(callbackToStart)));
		
		CallSite cs = new CallSite();
		cs.method = callbackToStart;
		findAllReachableApis(cs, new ArrayList<>());
		logger.info(String.format("Reachability analysis for %s finished", Helper.getSignatureOfSootMethod(callbackToStart)));
		return null;
	}

	private Set<CallSite> getCallsInMenuOnClick(SootMethod optionMenuOnItemSelected, int depthMethodLevel){
		//logger.debug("Checking calls in menu on click for {} {} {}",optionMenuOnItemSelected, elementId, depthMethodLevel);
		Set<CallSite> callSites = new HashSet<>();
		ForwardReachabilityAnalysisForClicksSwitch reachabilitySwitch = null;
		if(optionMenuOnItemSelected.getActiveBody().toString().contains(CONSTANT_SIGNATURES.optionMenuGetId)) {
			reachabilitySwitch = new ForwardReachabilityAnalysisForClicksSwitch(elementId, optionMenuOnItemSelected, callSites, "", CONSTANT_SIGNATURES.optionMenuGetId);
		}
		else{
			reachabilitySwitch = new ForwardReachabilityAnalysisForClicksWithFullObjectsSwitch(elementId, optionMenuOnItemSelected, callSites, CONSTANT_SIGNATURES.optionMenuGetId );
		}
		for (final Unit u : optionMenuOnItemSelected.getActiveBody().getUnits()){
			u.apply(reachabilitySwitch);
			if(reachabilitySwitch.isReady()){
				break;
			}
		}
		addUriToResults(reachabilitySwitch.getUris(), depthMethodLevel);
		return callSites;
	}

	private Set<CallSite> getCallsInListViewOnClick(SootMethod listViewItemSelected, int depthMethodLevel){
		//logger.debug("Checking calls in list view on click for {} {}", listViewItemSelected, depthMethodLevel);
		Set<CallSite> callSites = new HashSet<>();
		//Either we switch over the pos of the item or we switch over the listview itself
		ForwardReachabilityAnalysisForClicksSwitch reachabilitySwitch = new ForwardReachabilityAnalysisForClicksSwitch(elementId, listViewItemSelected, callSites, "@parameter2", CONSTANT_SIGNATURES.adapterViewGetId); //todo replace with param2?
		for (final Unit u : listViewItemSelected.getActiveBody().getUnits()){
			u.apply(reachabilitySwitch);
			if(reachabilitySwitch.isReady()){
				break;
			}
		}
		addUriToResults(reachabilitySwitch.getUris(), depthMethodLevel);
		return callSites;
	}

	private Set<CallSite> getCallsInAuxMethod(SootMethod auxMethod, int depthMethodLevel, String registerToSwitchOver, boolean isRegisterFullObject, String getIdMethod){
		Set<CallSite> callSites = new HashSet<>();
		ForwardReachabilityAnalysisForClicksSwitch reachabilitySwitch = null;
		if(!isRegisterFullObject) {
			reachabilitySwitch = new ForwardReachabilityAnalysisForClicksSwitch(elementId, auxMethod, callSites, registerToSwitchOver, "");
		}
		else{
			//If it's a full object, we can either switch over it or get the id?
			//String getIdMethod = 
			//TODO deal with objects switch as well
			if(!StringUtils.isBlank(getIdMethod) && auxMethod.getActiveBody().toString().contains(getIdMethod)){
				reachabilitySwitch = new ForwardReachabilityAnalysisForClicksSwitch(elementId, auxMethod, callSites, "", getIdMethod);
			}
			//TODO (more interprocedural calls)
			else reachabilitySwitch = new ForwardReachabilityAnalysisForClicksWithFullObjectsSwitch(elementId, registerToSwitchOver, auxMethod, callSites, getIdMethod);
		} 
		for (final Unit u : auxMethod.getActiveBody().getUnits()){
			//logger.debug("Current analyzed unit in aux method for {} {} in {}", elementId, u, auxMethod);
			u.apply(reachabilitySwitch);
			if(reachabilitySwitch.isReady()){
				break;
			}
		}
        addUriToResults(reachabilitySwitch.getUris(), depthMethodLevel);
		return callSites;
	}
	
	private Set<CallSite> getCallsInButtonOnClick(SootMethod buttonClick, int depthMethodLevel){
		Set<CallSite> callSites = new HashSet<>();
		ForwardReachabilityAnalysisForClicksSwitch reachabilitySwitch = null;
		if(buttonClick.getActiveBody().toString().contains(CONSTANT_SIGNATURES.buttonGetIdFullUnit)) {
			reachabilitySwitch = new ForwardReachabilityAnalysisForClicksSwitch(elementId, buttonClick, callSites, "", CONSTANT_SIGNATURES.buttonGetId);
		}
		else{
			reachabilitySwitch = new ForwardReachabilityAnalysisForClicksWithFullObjectsSwitch(elementId, buttonClick, callSites, CONSTANT_SIGNATURES.buttonGetId);
		}
		for (final Unit u : buttonClick.getActiveBody().getUnits()){
			u.apply(reachabilitySwitch);
			if(reachabilitySwitch.isReady()){
				break;
			}
		}
        addUriToResults(reachabilitySwitch.getUris(), depthMethodLevel);
		return callSites;
	}


	
	private Set<CallSite> getCallsInButtonOnClickDialogs(SootMethod buttonClick, int currentDepthMethodLevel){
		Set<CallSite> callSites = new HashSet<>();
		ForwardReachabilityAnalysisForDialogsSwitch reachabilitySwitch = new ForwardReachabilityAnalysisForDialogsSwitch(elementId, buttonClick, callSites);
		for (final Unit u : buttonClick.getActiveBody().getUnits()){
			u.apply(reachabilitySwitch);
			if(reachabilitySwitch.isReady()){
				break;
			}
		}
        addUriToResults(reachabilitySwitch.getUris(), currentDepthMethodLevel);
		return callSites;
	}

	private Set<CallSite> getCallsInMethod(CallSite cs, int depthMethodLevel) {
		SootMethod method = cs.method;
		String registerOfButtonId = cs.registerToSwitchOver;
		boolean isRegisterFullObject = cs.isFullObject;

		if (!method.hasActiveBody()) {
			try{
				method.retrieveActiveBody();
				//logger.debug("Retrieved missing active body for {} {}", declaringSootClass, method);
			}
			catch (Exception e) {
				logger.warn("Skipping method with empty body {} {} for {}", declaringSootClass, method, elementId);
				return Collections.synchronizedSet(new HashSet<>());
			}

		}

		if(registerOfButtonId != null && !registerOfButtonId.isEmpty()){
			//Propagate switch to next method
			return getCallsInAuxMethod(method, depthMethodLevel, registerOfButtonId, isRegisterFullObject, cs.getIdMethod);
		}
		return getCallsInMethod(method, depthMethodLevel);
	}
	
	private Set<CallSite> getCallsInMethod(SootMethod method, int depthMethodLevel) {
		Set<CallSite> callSites = Collections.synchronizedSet(new HashSet<>());

		if (!method.hasActiveBody()) {
			try{
				method.retrieveActiveBody();
				//logger.debug("Retrieved missing active body for {} {}", declaringSootClass, method);
			}
			catch (Exception e) {
				logger.warn("Skipping method with empty body {} {} for {}", declaringSootClass, method, elementId);
				return callSites;
			}

		}
		if (CONSTANT_SIGNATURES.buttonOnClickSignatures.stream().filter(x -> Helper.getSignatureOfSootMethod(method).endsWith(x)).findAny().isPresent()) {
			return getCallsInButtonOnClick(method, depthMethodLevel);
		}
		if (Helper.getSignatureOfSootMethod(method).endsWith(CONSTANT_SIGNATURES.dialogOnClick)) {
			return getCallsInButtonOnClickDialogs(method, depthMethodLevel);
		}

		//Here should be the current method or supermethod ?
		//Here we can check if the callback is menu onclick as well, or store the register
		if (CONSTANT_SIGNATURES.optionMenuOnClicks.stream().filter(x -> Helper.getSignatureOfSootMethod(method).endsWith(x)).findAny().isPresent()) {
			return getCallsInMenuOnClick(method, depthMethodLevel);
		}

		if (CONSTANT_SIGNATURES.listViewOnClicks.stream().filter(x -> Helper.getSignatureOfSootMethod(method).endsWith(x)).findAny().isPresent()) {
			return getCallsInListViewOnClick(method, depthMethodLevel);
		}

		if (RAHelper.callSitesForMethods.containsKey(method)) {
			return RAHelper.callSitesForMethods.get(method);
		}
		for (Unit u : method.getActiveBody().getUnits()) {
			//URI
			if(((Stmt) u).containsInvokeExpr() && RAHelper.getStaticMethodSignatures().
					contains(((Stmt) u).getInvokeExpr().getMethod().getSignature())) {
				u.apply(new AbstractStmtSwitch() {

					public void caseAssignStmt(AssignStmt stmt) {
						boolean containsInvokeExpr = stmt.containsInvokeExpr();
						if (containsInvokeExpr) {
							InvokeExpr invokeExpr = stmt.getInvokeExpr();
							processUris(stmt, invokeExpr, method, u, depthMethodLevel);
						}
					}

					public void caseInvokeStmt(InvokeStmt stmt) {
						InvokeExpr invokeExpr = stmt.getInvokeExpr();
						processUris(stmt, invokeExpr, method, u, depthMethodLevel);
					}

				});
			}
			processUnit(method, callSites, u);
		}
		RAHelper.callSitesForMethods.put(method, callSites);
		return callSites;
	}


	

	public static void processUnit(final SootMethod method, Set<CallSite> callSites, Stmt stmt, String registerOfButtonId, String getIdMethod, boolean isFullObject){
		InvokeExpr invokeExpr = stmt.getInvokeExpr();
		SootMethod auxMethod = invokeExpr.getMethod();
		String className = auxMethod.getDeclaringClass().getName();

		if(!RAHelper.isClassInSystemPackage(className) && !registerOfButtonId.isEmpty()){
			//TODO add: if invokeExpr and one of the parameters is the id registrer, need to do the same analysis on it?
			int arg = -1;
			//logger.debug("The invoked expr {} and register {}", invokeExpr, registerOfButtonId);
			
			//String reg = registerOfButtonId.substring(0,2);
			//boolean isRef = reg.equals("$r");
			int indexOfRegister = IntStream.range(0, invokeExpr.getArgCount())
											.filter(i -> invokeExpr.getArg(i).toString().equals(registerOfButtonId))
											.findFirst()
											.orElse(-1);
			if(indexOfRegister != -1){
				String newRegister = "@parameter"+indexOfRegister;
				//We want to add a callsite here
				final Iterator<Edge> invocations = Scene.v().getCallGraph().edgesOutOf(stmt);
				if (!invocations.hasNext()) {
					//call graph didn't manage to correctly resolve the edge
					CallSite cs = new CallSite();
					cs.method = auxMethod;
					cs.classOfInvokeExpr = invokeExpr.getMethodRef().declaringClass();
					cs.unit = stmt;
					cs.caller = method;
					cs.registerToSwitchOver = newRegister;
					cs.isFullObject = isFullObject;
					cs.getIdMethod = getIdMethod;
					callSites.add(cs);

					return;
        		}
				while (invocations.hasNext()) {
					Edge invocation = invocations.next();
					if (invocation.getTgt().method() == null || invocation.srcUnit() == null) {
						continue;
					}
					CallSite cs = new CallSite();
					cs.method = invocation.getTgt().method();
					cs.unit = invocation.srcUnit();
					cs.caller = method;
					cs.registerToSwitchOver = newRegister;
					cs.isFullObject = isFullObject;
					cs.getIdMethod = getIdMethod;

					if (!((Stmt) invocation.srcUnit()).containsInvokeExpr()) {
						cs.classOfInvokeExpr = invocation.getTgt().method().getDeclaringClass();
					} else {
						cs.classOfInvokeExpr = ((Stmt) invocation.srcUnit()).getInvokeExpr().getMethodRef().declaringClass();
					}
					callSites.add(cs);
				}
				return;
			}
			//TODO mark as visited and increase the depth
		}
		CallbackToApiMapper.processUnit(method, callSites, stmt);
	}

	public static void processUnit(final SootMethod method, Set<CallSite> callSites, final Unit u) {

		final Iterator<Edge> invocations = Scene.v().getCallGraph().edgesOutOf(u);
		if (!invocations.hasNext() && ((Stmt) u).containsInvokeExpr()) {
            //call graph didn't manage to correctly resolve the edge
            CallSite cs = new CallSite();
            cs.method = ((Stmt) u).getInvokeExpr().getMethod();
            cs.classOfInvokeExpr = ((Stmt) u).getInvokeExpr().getMethodRef().declaringClass();
            cs.unit = u;
            cs.caller = method;
            callSites.add(cs);

			return;
        }
		while (invocations.hasNext()) {
            Edge invocation = invocations.next();
            if (invocation.getTgt().method() == null || invocation.srcUnit() == null) {
                continue;
            }
            CallSite cs = new CallSite();
            cs.method = invocation.getTgt().method();
            cs.unit = invocation.srcUnit();
            cs.caller = method;

            if(invocation.kind().isExecutor() || invocation.kind().isAsyncTask()){
                cs.classOfInvokeExpr = cs.method.getDeclaringClass();
            }
            else if(invocation.kind().isThread()){
                SootMethod mInSource = ((Stmt)cs.unit).getInvokeExpr().getMethod();
                if(mInSource.getDeclaringClass().getName().equals("java.lang.Thread") && mInSource.getName().equals("start")){
                    MHGDominatorsFinder<Unit> dominatorFinder = new MHGDominatorsFinder<>(new ExceptionalUnitGraph(method.getActiveBody()));
                    Unit curUnit = dominatorFinder.getImmediateDominator(cs.unit);
                    Value regOfThread = cs.unit.getUseBoxes().get(0).getValue();
                    while (curUnit != null){
                        if(((Stmt)curUnit).containsInvokeExpr() && ((Stmt)curUnit).getInvokeExpr().getMethod().getSignature().equals("<java.lang.Thread: void <init>(java.lang.Runnable)>")){
                            InvokeExpr invokeExpr =  ((Stmt)curUnit).getInvokeExpr();
                            Value regOfThreadLocal = invokeExpr.getUseBoxes().get(0).getValue();
                            if(regOfThread.equals(regOfThreadLocal)){
                                cs.classOfInvokeExpr = Scene.v().getSootClass(curUnit.getUseBoxes().get(0).getValue().getType().toString());
								if(cs.classOfInvokeExpr.getMethods().stream().filter(x->x.getSubSignature().equals(cs.method.getSubSignature())).findFirst().isPresent()){
									cs.method = cs.classOfInvokeExpr.getMethod(cs.method.getSubSignature());
								}
								else{
									for(SootClass superClass : Scene.v().getActiveHierarchy().getSuperclassesOf(cs.classOfInvokeExpr)){
										if(superClass.getMethods().stream().filter(x->x.getSubSignature().equals(cs.method.getSubSignature())).findFirst().isPresent()){
											cs.method = superClass.getMethod(cs.method.getSubSignature());
											break;
										}
									}
								}
                                break;
                            }
                        }
                        curUnit = dominatorFinder.getImmediateDominator(curUnit);
                    }
                }
            }
            else if (!((Stmt) invocation.srcUnit()).containsInvokeExpr()) {
                cs.classOfInvokeExpr = invocation.getTgt().method().getDeclaringClass();
            } else {
                cs.classOfInvokeExpr = ((Stmt) invocation.srcUnit()).getInvokeExpr().getMethodRef().declaringClass();
            }
            callSites.add(cs);
        }
	}

	private void processUris(Stmt stmt, InvokeExpr invokeExpr, SootMethod method, Unit u, int depthMethodLevel) {
		String uri = RAHelper.analyzeInvokeExpressionToFindUris(method.getActiveBody(), u, invokeExpr.getMethod(), invokeExpr.getArg(0), false);
		if (uri != null) {
            addUriToResults(Collections.singletonList(uri), depthMethodLevel);
        } else {
            addUriToResults(Collections.singletonList(Helper.getSignatureOfSootMethod(stmt.getInvokeExpr().getMethod())), depthMethodLevel);
        }
	}

	private void addUriToResults(List<String> uris, int depth){
		uris.forEach(x->{
			ApiInfoForForward res = new ApiInfoForForward();
			res.depthMethodLevel = depth;
			res.signature = x;
			apisFound.add(res);
		});
	}
	
	private void findAllReachableApis(CallSite cSite, List<SootMethod> callStack){

		if(Thread.currentThread().isInterrupted() || (callStack.size() > this.maxDepthMethodLevel)){
			return;
		}

		callStack.add(cSite.method);
		visitedMethods.add(cSite.method);
		final int currentMethodDepthLevel = callStack.size();

		List<SootMethod> localCallStack = new ArrayList<>(callStack);
		
		List<CallSite> callSites = new ArrayList<>();

		boolean processNormallCalls = true;
		
		if(this.specialMethods.stream().anyMatch(x->cSite.method.getSubSignature().startsWith(x)) && cSite.classOfInvokeExpr != null){
			//AsyncTask etc..
            cSite.classOfInvokeExpr.getMethods().stream().filter(m -> asyncTaskMethods.stream()
                    .anyMatch(x -> m.getName().equals(x)) /*&& m.hasActiveBody()*/)
                    .forEach(m -> callSites.addAll(getCallsInMethod(m, currentMethodDepthLevel+1)));
			processNormallCalls = false;
		}
		else if(cSite.classOfInvokeExpr != null  && (cSite.classOfInvokeExpr.equals(RAHelper.getExecutorServiceClass()) ||
				RAHelper.getImplementersOfExecutorServiceClass().contains(cSite.classOfInvokeExpr))){
			//Executor Service
			if(cSite.method.getSubSignature().equals(CONSTANT_SIGNATURES.executorServiceSubmitRunnable)){
				//submit task to executor service
				Value runnable = ((Stmt)cSite.unit).getInvokeExpr().getArg(0);
				String classOfReg = runnable.getType().toString();
				Scene.v().getSootClass(classOfReg).getMethods().stream()
						.filter(m -> runnableMethods.stream().anyMatch(x -> m.getName().equals(x)) /*&& m.hasActiveBody()*/)
						.forEach(m -> callSites.addAll(getCallsInMethod(m, currentMethodDepthLevel+1)));
				processNormallCalls = false;
			}
		}
		if(processNormallCalls){
			//TODO: change this since the activities could have a different package name compared to the app
			if(limitByPackageName && cSite.method.getDeclaringClass() != null && !Helper.isClassInAppNameSpace(cSite.method.getDeclaringClass().getName())){
				return;
			}
			callSites.addAll(getCallsInMethod(cSite, currentMethodDepthLevel));
		}
		//logger.debug("All callsites for {} {} {}", cSite, elementId, callSites);
		for(CallSite targetCall : callSites){
			if(visitedMethods.contains(targetCall.method)){
				continue;
			}
			if(sourcesAndSinksSignatures.contains(Helper.getSignatureOfSootMethod(targetCall.method)) || START_ACTIVITY_CONSTANTS.getStartActivityMethods().contains(targetCall.method.getSubSignature())){
				ApiInfoForForward info = null;
				//Check for the startActivity
				if(START_ACTIVITY_CONSTANTS.getStartActivityMethods().contains(targetCall.method.getSubSignature())){
					//we should collect more data about the startActivity
					logger.debug("Found startActivity for {} {} in {} at {}", declaringSootClass, elementId, targetCall, cSite);
					if(targetCall != null && !targetCall.caller.hasActiveBody()){
						try {
							targetCall.caller.retrieveActiveBody();
						}
						catch (Exception e){
							logger.error("Could not retrieve active body for {}", targetCall.method);
							//continue;
						}
					}
					AnalyzeIntents intentAnalyzer = new AnalyzeIntents(targetCall.method, targetCall.unit, targetCall.caller, elementId);
					intentAnalyzer.run();
					info = intentAnalyzer.getIntentInfo();
					logger.debug("Info from intent analyzer {} {} {} {}", declaringSootClass, elementId, info, ((IntentInfo)info).getClassName());
					String clnName = ((IntentInfo)info).getClassName();
					Map<String, String> contextSensitiveClassNames = ((IntentInfo)info).getContextSensitiveClassNames();
					if(clnName != null){
						if(contextSensitiveClassNames != null && contextSensitiveClassNames.size() > 1) {
							if(declaringSootClass != null && contextSensitiveClassNames.containsKey(declaringSootClass)) {
								clnName = contextSensitiveClassNames.get(declaringSootClass);
								logger.debug("{} actual destination for {}", clnName, declaringSootClass);
							}
							else if(declaringSootClass != null){
								logger.warn("Adding target sootClass for non identified source {}, {}", declaringSootClass, clnName);
							}
							//TODO deal with the case where the class is not in there?
						}
						if(clnName.contains("#")) {
							logger.warn("Multiple destinations for intent found, defaulting to first {}", clnName);
							//logger.error("Multiple destinations for intent found, defaulting to first {}", clnName);
							clnName = clnName.split("#")[0];
						}
						clnName = clnName.replace("/" ,".");
						if (clnName.startsWith("L")) {
							clnName = clnName.substring(1);
						}
						if (clnName.endsWith(";")) {
							clnName = clnName.substring(0, clnName.length()-1);
						}
						if(Scene.v().getSootClassUnsafe(clnName) != null) {
							SootClass classOfTarget = Scene.v().getSootClass(clnName);
							setNewSootActivityClass(classOfTarget);
							//logger.debug("New soot activity for ui {} {}", elementId, newSootActivityClass);
							if(targetCall.method.getSubSignature().equals(LifecycleConstants.SERVICE_BIND)){
								while (!classOfTarget.getName().equals("java.lang.Object")) {
									if (classOfTarget.getMethodByNameUnsafe("onBind") != null) {
										SootMethod onBind = classOfTarget.getMethodByName("onBind");
										if (onBind.getSubSignature().equals(LifecycleConstants.SERVICE_ONBIND)) {
											SootMethod caller = targetCall.method;
											targetCall.method = onBind;
											targetCall.unit = null;
											targetCall.classOfInvokeExpr = classOfTarget;
											targetCall.caller = caller;

											callStack.remove(callStack.size() - 1);
											SootMethod newMethod = Scene.v().getMethod("<CustomIntercomponentClass: void newActivity()>");
											callStack.add(newMethod);
											findAllReachableApis(targetCall, callStack);

											callStack.clear();
											callStack.addAll(localCallStack);

											if (Thread.currentThread().isInterrupted()) {
												return;
											}
											break;
										}
									}
									List<SootClass> superClasses = Scene.v().getActiveHierarchy().getSuperclassesOf(classOfTarget);
									if(superClasses == null || superClasses.isEmpty()){
										logger.warn("No superclass found for {}, likely issue", classOfTarget);
										break;
									}
									classOfTarget = superClasses.get(0);
								}
								classOfTarget = Scene.v().getSootClass(clnName);
								while (!classOfTarget.getName().equals("java.lang.Object")) {
									if (classOfTarget.getMethodByNameUnsafe("onRebind") != null) {
										SootMethod onRebind = classOfTarget.getMethodByName("onRebind");
										if (onRebind.getSubSignature().equals(LifecycleConstants.SERVICE_ONREBIND)) {
											SootMethod caller = targetCall.method;
											targetCall.method = onRebind;
											targetCall.unit = null;
											targetCall.classOfInvokeExpr = classOfTarget;
											targetCall.caller = caller;

											callStack.remove(callStack.size() - 1);
											SootMethod newMethod = Scene.v().getMethod("<CustomIntercomponentClass: void newActivity()>");
											callStack.add(newMethod);
											findAllReachableApis(targetCall, callStack);

											callStack.clear();
											callStack.addAll(localCallStack);

											if (Thread.currentThread().isInterrupted()) {
												return;
											}
											break;
										}
									}
									List<SootClass> superClasses = Scene.v().getActiveHierarchy().getSuperclassesOf(classOfTarget);
									if(superClasses == null || superClasses.isEmpty()){
										logger.warn("No superclass found for {}, likely issue", classOfTarget);
										break;
									}
									classOfTarget = superClasses.get(0);
								}
							} else if(targetCall.method.getSubSignature().equals(LifecycleConstants.SERVICE_START)){
								while (!classOfTarget.getName().equals("java.lang.Object")) {
									if (classOfTarget.getMethodByNameUnsafe("onStart") != null) {
										SootMethod onStart = classOfTarget.getMethodByName("onStart");
										if (onStart.getSubSignature().equals(LifecycleConstants.SERVICE_ONSTART1)) {
											SootMethod caller = targetCall.method;
											targetCall.method = onStart;
											targetCall.unit = null;
											targetCall.classOfInvokeExpr = classOfTarget;
											targetCall.caller = caller;

											callStack.remove(callStack.size() - 1);
											SootMethod newMethod = Scene.v().getMethod("<CustomIntercomponentClass: void newActivity()>");
											callStack.add(newMethod);
											findAllReachableApis(targetCall, callStack);

											callStack.clear();
											callStack.addAll(localCallStack);

											if (Thread.currentThread().isInterrupted()) {
												return;
											}
											break;
										}
									}
									//TODO if no superclass then exist?
									List<SootClass> superClasses = Scene.v().getActiveHierarchy().getSuperclassesOf(classOfTarget);
									if(superClasses == null || superClasses.isEmpty()){
										logger.warn("No superclass found for {}, likely issue", classOfTarget);
										break;
									}
									classOfTarget = superClasses.get(0);
								}
								classOfTarget = Scene.v().getSootClass(clnName);
								while (!classOfTarget.getName().equals("java.lang.Object")) {
									if (classOfTarget.getMethodByNameUnsafe("onStartCommand") != null) {
										SootMethod onStart = classOfTarget.getMethodByName("onStartCommand");
										if (onStart.getSubSignature().equals(LifecycleConstants.SERVICE_ONSTART2)) {
											SootMethod caller = targetCall.method;
											targetCall.method = onStart;
											targetCall.unit = null;
											targetCall.classOfInvokeExpr = classOfTarget;
											targetCall.caller = caller;

											callStack.remove(callStack.size() - 1);
											SootMethod newMethod = Scene.v().getMethod("<CustomIntercomponentClass: void newActivity()>");
											callStack.add(newMethod);
											findAllReachableApis(targetCall, callStack);

											callStack.clear();
											callStack.addAll(localCallStack);

											if (Thread.currentThread().isInterrupted()) {
												return;
											}
											break;
										}
									}
									List<SootClass> superClasses = Scene.v().getActiveHierarchy().getSuperclassesOf(classOfTarget);
									if(superClasses == null || superClasses.isEmpty()){
										logger.warn("No superclass found for {}, likely issue", classOfTarget);
										break;
									}
									classOfTarget = superClasses.get(0);
								}
								while (!classOfTarget.getName().equals("java.lang.Object")) { //to parse content defined in IntentService
									if (classOfTarget.getMethodByNameUnsafe("onHandleIntent") != null) {
										SootMethod onHandleIntent = classOfTarget.getMethodByName("onHandleIntent");
										if (onHandleIntent.getSubSignature().equals(LifecycleConstants.INTENT_SERVICE_ONHANDLEINTENT)) {
											SootMethod caller = targetCall.method;
											targetCall.method = onHandleIntent;
											targetCall.unit = null;
											targetCall.classOfInvokeExpr = classOfTarget;
											targetCall.caller = caller;

											callStack.remove(callStack.size() - 1);
											SootMethod newMethod = Scene.v().getMethod("<CustomIntercomponentClass: void newActivity()>");
											callStack.add(newMethod);
											findAllReachableApis(targetCall, callStack);

											callStack.clear();
											callStack.addAll(localCallStack);

											if (Thread.currentThread().isInterrupted()) {
												return;
											}
											break;
										}
									}
									List<SootClass> superClasses = Scene.v().getActiveHierarchy().getSuperclassesOf(classOfTarget);
									if(superClasses == null || superClasses.isEmpty()){
										logger.warn("No superclass found for {}, likely issue", classOfTarget);
										break;
									}
									classOfTarget = superClasses.get(0);
								}

							} else {
								//add connection to the OnCreate
								while (!classOfTarget.getName().equals("java.lang.Object")) {
									if (classOfTarget.getMethodUnsafe(CONSTANT_SIGNATURES.ACTIVITY_ONCREATE) != null) {
										//logger.debug("Found onCreate method for {} {}", elementId, classOfTarget);
										SootMethod onCreate = classOfTarget.getMethod(CONSTANT_SIGNATURES.ACTIVITY_ONCREATE);
										if (onCreate.getSubSignature().equals(CONSTANT_SIGNATURES.ACTIVITY_ONCREATE)) {
											SootMethod caller = targetCall.method;
											CallSite onCreateSite = new CallSite();
											onCreateSite.method = onCreate;
											onCreateSite.unit = null;
											onCreateSite.classOfInvokeExpr = classOfTarget;
											onCreateSite.caller = caller;

											callStack.remove(callStack.size() - 1);
											SootMethod newMethod = Scene.v().getMethod("<CustomIntercomponentClass: void newActivity()>");
											callStack.add(newMethod);
											findAllReachableApis(onCreateSite, callStack);

											callStack.clear();
											callStack.addAll(localCallStack);

											if (Thread.currentThread().isInterrupted()) {
												return;
											}
											break;
										}
									}
									if(Scene.v().getActiveHierarchy().getSuperclassesOf(classOfTarget).size() == 0){
										break;
									}
									
									classOfTarget = Scene.v().getActiveHierarchy().getSuperclassesOf(classOfTarget).get(0);
								}
							}
							continue;
						}
					}

				}
				else {
					info = new ApiInfoForForward();
				}

				info.api = targetCall.method;
				info.signature = Helper.getSignatureOfSootMethod(targetCall.method);
				info.depthMethodLevel = currentMethodDepthLevel;
				info.depthComponentLevel = (int) callStack.stream().filter(x -> x.getSignature().equals("<CustomIntercomponentClass: void newActivity()>")).count();
				info.callerMethod = cSite.method; //should this not be updated?
				info.callerSignature = info.callerMethod.getSignature();

				if(!this.apisFound.contains(info)){
					this.apisFound.add(info);
					if(trackCallStack && !this.callStack.containsKey(info.api)){
						this.callStack.put(info.api, new ArrayList<>(callStack));
					}
				}
				else{
					//maybe it contains the same API but with greater depthLevel. We prefer to have the nearest one
					final String apiSignature = info.api.getSignature();
					final int apiDepthComponentLevel = info.depthComponentLevel;

					ApiInfoForForward fromList = this.apisFound.stream().filter(x->x.signature.equals(apiSignature) && x.depthComponentLevel == apiDepthComponentLevel).min((o1, o2) -> {
						Integer depthMethod1 = o1.depthMethodLevel;
						Integer depthMethod2 = o2.depthMethodLevel;
						return depthMethod1.compareTo(depthMethod2);
					}).get();

					if(fromList.depthMethodLevel > info.depthMethodLevel) {
						this.apisFound.remove(fromList);

						if(trackCallStack && !this.callStack.containsKey(info.api)){
							this.callStack.put(info.api, new ArrayList<>(callStack));
						}

						this.apisFound.add(info);
					}
				}
				//logger.debug("All the apis found so far {} {}", elementId, apisFound);
				callStack.clear();
				callStack.addAll(localCallStack);

				if(Thread.currentThread().isInterrupted()){
					return;
				}
				continue;
			}

			/*if (RAHelper.isClassInSystemPackage(targetCall.method.getDeclaringClass().getName())) {
                continue;
            }*/
			
			findAllReachableApis(targetCall, callStack);
			callStack.clear();
			callStack.addAll(localCallStack);
			
			if(Thread.currentThread().isInterrupted()){
				return;
			}
		}
	}

	public synchronized SootClass getNewSootActivityClass() {
		return newSootActivityClass;
	}

	public synchronized void setNewSootActivityClass(SootClass newSootActivityClass) {
		this.newSootActivityClass = newSootActivityClass;
	}
}
