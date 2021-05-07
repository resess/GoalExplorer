package st.cs.uni.saarland.de.reachabilityAnalysis;

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

/**
 * Class that is responsible for finding all APIs that are reachable from a given callback maethod
 */

public class CallbackToApiMapper implements Callable<Void> {
	
	private final SootMethod callbackToStart;
	private final List<ApiInfoForForward> apisFound;
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
    private final List<SootMethod> visitedMethods;
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
		//specialMethods.add("android.os.AsyncTask execute(java.lang.Object[])");
		specialMethods.add("android.os.AsyncTask executeOnExecutor(java.util.concurrent.Executor,java.lang.Object[])");
		runnable = "void <init>";
		runnableMethods = new ArrayList<>();
		runnableMethods.add("run");
		
		osHanldeMethods = new ArrayList<>();
		osHanldeMethods.add("handleMessage");
		
		asyncTaskMethods = new ArrayList<>();
		asyncTaskMethods.add("doInBackground");

        this.visitedMethods = new ArrayList<>();
		this.sourcesAndSinksSignatures = new HashSet<>();
		newSootActivityClass = null;
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
		Set<CallSite> callSites = new HashSet<>();
		ForwardReachabilityAnalysisForClicksSwitch reachabilitySwitch = new ForwardReachabilityAnalysisForClicksSwitch(elementId, optionMenuOnItemSelected, callSites, "", CONSTANT_SIGNATURES.optionMenuGetId);
		for (final Unit u : optionMenuOnItemSelected.getActiveBody().getUnits()){
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
			reachabilitySwitch = new ForwardReachabilityAnalysisForClicksWithFullObjectsSwitch(elementId, buttonClick, callSites);
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
	
	private Set<CallSite> getCallsInMethod(SootMethod method, int depthMethodLevel) {
		Set<CallSite> callSites = Collections.synchronizedSet(new HashSet<>());

		if (!method.hasActiveBody()) {
			return callSites;
		}

		if (CONSTANT_SIGNATURES.buttonOnClickSignatures.stream().filter(x -> Helper.getSignatureOfSootMethod(method).endsWith(x)).findAny().isPresent()) {
			return getCallsInButtonOnClick(method, depthMethodLevel);
		}
		if (Helper.getSignatureOfSootMethod(method).endsWith(CONSTANT_SIGNATURES.dialogOnClick)) {
			return getCallsInButtonOnClickDialogs(method, depthMethodLevel);
		}

		if (CONSTANT_SIGNATURES.optionMenuOnClicks.stream().filter(x -> Helper.getSignatureOfSootMethod(method).endsWith(x)).findAny().isPresent()) {
			return getCallsInMenuOnClick(method, depthMethodLevel);
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
                            Value regOfThreadLocal = invokeExpr.getUseBoxes().get(invokeExpr.getUseBoxes().size() - 1).getValue();
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
		
		if(this.specialMethods.stream().anyMatch(x->cSite.method.getSubSignature().equals(x)) && cSite.classOfInvokeExpr != null){
			//AsyncTask etc..
            cSite.classOfInvokeExpr.getMethods().stream().filter(m -> asyncTaskMethods.stream()
                    .anyMatch(x -> m.getName().equals(x)) && m.hasActiveBody())
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
						.filter(m -> runnableMethods.stream().anyMatch(x -> m.getName().equals(x)) && m.hasActiveBody())
						.forEach(m -> callSites.addAll(getCallsInMethod(m, currentMethodDepthLevel+1)));
				processNormallCalls = false;
			}
		}
		if(processNormallCalls){
			if(limitByPackageName && cSite.method.getDeclaringClass() != null && !cSite.method.getDeclaringClass().getName().startsWith(Helper.getPackageName())){
				return;
			}
			callSites.addAll(getCallsInMethod(cSite.method, currentMethodDepthLevel));
		}
		for(CallSite targetCall : callSites){
			if(visitedMethods.contains(targetCall.method)){
				continue;
			}
			
			if(sourcesAndSinksSignatures.contains(Helper.getSignatureOfSootMethod(targetCall.method))){
				ApiInfoForForward info = null;
				//Check for the startActivity
				if(START_ACTIVITY_CONSTANTS.getStartActivityMethods().contains(targetCall.method.getSubSignature())){
					//we should collect more data about the startActivity
					AnalyzeIntents intentAnalyzer = new AnalyzeIntents(targetCall.method, targetCall.unit, targetCall.caller);
					intentAnalyzer.run();
					info = intentAnalyzer.getIntentInfo();
					String clnName = ((IntentInfo)info).getClassName();
					if(clnName != null){
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
									classOfTarget = Scene.v().getActiveHierarchy().getSuperclassesOf(classOfTarget).get(0);
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
									classOfTarget = Scene.v().getActiveHierarchy().getSuperclassesOf(classOfTarget).get(0);
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
									classOfTarget = Scene.v().getActiveHierarchy().getSuperclassesOf(classOfTarget).get(0);
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
									classOfTarget = Scene.v().getActiveHierarchy().getSuperclassesOf(classOfTarget).get(0);
								}
							} else {
								//add connection to the OnCreate
								while (!classOfTarget.getName().equals("java.lang.Object")) {
									if (classOfTarget.getMethodUnsafe(CONSTANT_SIGNATURES.ACTIVITY_ONCREATE) != null) {
										SootMethod onCreate = classOfTarget.getMethod(CONSTANT_SIGNATURES.ACTIVITY_ONCREATE);
										if (onCreate.getSubSignature().equals(CONSTANT_SIGNATURES.ACTIVITY_ONCREATE)) {
											SootMethod caller = targetCall.method;
											targetCall.method = onCreate;
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
				info.callerMethod = cSite.method;
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
