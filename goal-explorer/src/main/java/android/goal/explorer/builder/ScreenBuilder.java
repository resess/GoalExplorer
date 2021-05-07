package android.goal.explorer.builder;

import android.goal.explorer.analysis.CallbackReachableMethods;
import android.goal.explorer.analysis.TypeAnalyzer;
import android.goal.explorer.analysis.value.identifiers.Argument;
import android.goal.explorer.analysis.value.managers.ArgumentValueManager;
import android.goal.explorer.cmdline.GlobalConfig;
import android.goal.explorer.data.android.AndroidClass;
import android.goal.explorer.model.App;
import android.goal.explorer.model.component.Activity;
import android.goal.explorer.model.component.Fragment;
import android.goal.explorer.model.stg.STG;
import android.goal.explorer.model.stg.node.ScreenNode;
import android.goal.explorer.topology.TopologyExtractor;
import org.pmw.tinylog.Logger;
import soot.Local;
import soot.MethodOrMethodContext;
import soot.RefType;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Type;
import soot.Unit;
import soot.Value;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.infoflow.android.callbacks.CallbackDefinition;
import soot.jimple.infoflow.android.callbacks.filters.ICallbackFilter;
import soot.jimple.infoflow.util.SystemClassHandler;
import soot.jimple.toolkits.callgraph.Edge;
import soot.util.MultiMap;
import soot.util.queue.QueueReader;
import st.cs.uni.saarland.de.entities.Application;
import st.cs.uni.saarland.de.entities.XMLLayoutFile;
import st.cs.uni.saarland.de.helpClasses.Helper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static android.goal.explorer.analysis.AnalysisUtils.extractIntArgumentFrom;
import static android.goal.explorer.utils.InvokeExprHelper.invokesInflate;
import static android.goal.explorer.utils.InvokeExprHelper.invokesSetContentView;

public class ScreenBuilder {

    private static final String TAG = "ScreenBuilder";

    private App app;
    private Application backstageApp;
    private STG stg;

    private MultiMap<SootClass, CallbackDefinition> callbacksMap;
    private GlobalConfig config;

    private final List<ICallbackFilter> callbackFilters = new ArrayList<>();

    /**
     * Default constructor
     * @param app The application model
     */
    public ScreenBuilder(App app, STG stg, Application backstageApp, GlobalConfig config) {
        this.app = app;
        this.stg = stg;
        this.callbacksMap = app.getCallbackMethods();
        this.backstageApp = backstageApp;
        this.config = config;
    }

    /**
     * Construct the screens for each activity
     */
    public void constructScreens() {
        // Collects the initial screens with set of fragments from lifecycle methods
        collectInitialScreens();

        // Initialize the filters
        for (ICallbackFilter filter : callbackFilters)
            filter.reset();

        // Analyze screens with changes in callback methods
        analyzeCallbacksForFragmentChanges();

        // analyze entity (menu/drawer/dialog)
//        for (ScreenNode screenNode : stg.getAllScreens()) {
//            screenNode.get
//        }
    }

    /**
     * Collects the initial screen from lifecycle methods
     */
    private void collectInitialScreens() {
        // For each activity, we analyze its hosted fragments
        for (Activity activity : app.getActivities()) {
            // Collect initial screen for this activity
            collectInitialScreenForActivity(activity);
        }
    }

    /**
     * Collects initial screens for the given activity
     * @param activity the activity to collect initial screens
     */
    private void collectInitialScreenForActivity(Activity activity) {
        // Executor for multi-threading analysis
//        ExecutorService initialScreenExecutor = Executors.newSingleThreadExecutor();

        // Initial fragment classes (before activity is running)
        Set<SootClass> initialFragmentClasses = new HashSet<>();
        Set<Fragment> staticFragments = new HashSet<>();

        // Gets the fragment from layout
        Map<Integer, XMLLayoutFile> layoutMap = activity.getLayouts();
        for (XMLLayoutFile layout : layoutMap.values()) {
            for (Integer includeId : layout.getStaticIncludedLayoutIDs()) {
                staticFragments.addAll(app.getFragmentsByResId(includeId));
            }
        }

        // Analyze reachable methods from lifecycle methods pre-run
        for (MethodOrMethodContext lifecycleMethod : activity.getLifecycleMethodsPreRun()) {
            CallbackReachableMethods rm = new CallbackReachableMethods(config.getFlowdroidConfig(),
                    activity.getMainClass(), lifecycleMethod);
            rm.update();

            // iterate through the reachable methods
            QueueReader<MethodOrMethodContext> reachableMethods = rm.listener();
            while (reachableMethods.hasNext()) {
                // Get the next reachable method
                SootMethod method = reachableMethods.next().method();
                // Do not analyze system classes
                if (SystemClassHandler.isClassInSystemPackage(method.getDeclaringClass().getName()))
                    continue;
                if (!method.isConcrete())
                    continue;

                // Edges for context-sensitive point-to analysis
                Set<List<Edge>> edges = rm.getContextEdges(method);

                // Analyze layout: assigns resource id of the activity, and connects it with the layout file
                if (lifecycleMethod.method().getName().contains("onCreate")) {
                    analyzeLayout(method, activity, edges);
                }

                // Find fragments
                initialFragmentClasses.addAll(analyzeFragmentTransaction(method, edges));

                // add fragments to the screen
                createNewScreenWithFragments(activity, initialFragmentClasses, staticFragments);
            }
        }
    }

    /**
     * Execute the task on a separate thread with timeout specified in configuration file
     * @param task The task to execute
     * @param method The method for which the task executes on
     */
    private void runTask(Future<Void> task, SootMethod method) {
        try {
            task.get(config.getTimeout(), TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Logger.info("[{}] Interrupted an entrypoint: {} from a parent thread", TAG, method.getName());
            task.cancel(true);
        } catch (TimeoutException e) {
            Logger.info("[{}] Timeout for entrypoint {}", TAG, method.getName());
            task.cancel(true);
        } catch (Exception e) {
            Logger.error(Helper.exceptionStacktraceToString(e));
            Helper.saveToStatisticalFile(Helper.exceptionStacktraceToString(e));
            task.cancel(true);
        }
    }

//    /**
//     * Create task for analyzing the layout
//     * @param methodToExplore The method to explore
//     * @param callbackMethod The callback which contains the method
//     * @param activity The activity that contains the method
//     * @return null
//     */
//    private Callable<Void> getTaskForLayout(SootMethod methodToExplore, SootMethod callbackMethod, Activity activity) {
//        return () -> {
//            Logger.debug("Analyzing layout for {} from {} in activity: {}", methodToExplore, callbackMethod, activity);
//            LayoutsFinder layoutsFinder = new LayoutsFinder(callbackMethod, activity.getMainClass().getName());
//            layoutsFinder.run();
//            Map<Integer, LayoutInfo> layouts = layoutsFinder.getLayouts();
//            layouts.keySet().forEach(i -> layouts.get(i).setActivityNameOfView(activity.getMainClass().getName()));
//            activity.setLayouts(layouts);
//
//            return null;
//        };
//    }

//    /**
//     * Analyzing the initial fragments
//     * @param methodToExplore The method to explore
//     * @param callbackMethod The callback method
//     * @param activity The activity that contains the method
//     * @return null
//     */
//    private Callable<Void> getTaskForAnalyzingInitialFragment(SootMethod methodToExplore, SootMethod callbackMethod, Activity activity) {
//        return () -> {
//            // Initial fragment classes (before activity is running)
//            Set<SootClass> initialFragmentClasses = new HashSet<>();
//            //TODO: remove test variable
//            Set<SootClass> initialFragmentClassesBackstage = new HashSet<>();
//
//            // Analyze reachable methods from lifecycle methods pre-run
//            for (MethodOrMethodContext lifecycleMethod : activity.getLifecycleMethodsPreRun()) {
//                CallbackReachableMethods rm = new CallbackReachableMethods(config.getFlowdroidConfig(),
//                        activity.getMainClass(), lifecycleMethod);
//                rm.update();
//
//                // TODO: remove debug logging
//                if (activity.getName().contains("ViewCommentActivity"))
//                    Logger.debug("here");
//                if (activity.getName().contains("ViewPostActivity"))
//                    Logger.debug("here");
//
//                // iterate through the reachable methods
//                QueueReader<MethodOrMethodContext> reachableMethods = rm.listener();
//                while (reachableMethods.hasNext()) {
//                    // Get the next reachable method
//                    SootMethod method = reachableMethods.next().method();
//                    // Do not analyze system classes
//                    if (SystemClassHandler.isClassInSystemPackage(method.getDeclaringClass().getName()))
//                        continue;
//                    if (!method.isConcrete())
//                        continue;
//
//                    // Edges for context-sensitive point-to analysis
//                    Set<List<Edge>> edges = rm.getContextEdges(method);
//
//                    // Find fragments
//                    initialFragmentClasses.addAll(analyzeFragmentTransaction(method, edges));
//                    FragmentsFinder fragmentsFinder = new FragmentsFinder(method);
//                    fragmentsFinder.run();
//                    Set<FragmentDynInfo> fragments = fragmentsFinder.getFragments();
//                    for (FragmentDynInfo fragmentDynInfo : fragments) {
//                        String className = fragmentDynInfo.getFragmentClassName();
//                        if (className!=null && !className.isEmpty())
//                            initialFragmentClassesBackstage.add(Scene.v().getSootClassUnsafe(fragmentDynInfo.getFragmentClassName()));
//                        else
//                            Logger.debug("here");
//                    }
//                    assert initialFragmentClasses.equals(initialFragmentClassesBackstage);
//
//
//                    // TODO: remove debug logging
//                    if (!initialFragmentClasses.isEmpty() || !initialFragmentClassesBackstage.isEmpty()) {
//                        Logger.debug("Found fragments: {} in activity: {}", initialFragmentClasses, activity.getName());
//                        Logger.debug("BACKSTAGE - Found fragments: {} in activity: {}",
//                                initialFragmentClassesBackstage, activity.getName());
//                    }
//
//            initialFragmentClasses.addAll(analyzeFragmentTransaction(methodToExplore, edges, activity));
//
//            // TODO: compare the two methods
//            FragmentsFinder fragmentsFinder = new FragmentsFinder(method);
//            fragmentsFinder.run();
//            Set<FragmentDynInfo> fragments = fragmentsFinder.getFragments();
//            for (FragmentDynInfo fragmentDynInfo : fragments) {
//                String className = fragmentDynInfo.getFragmentClassName();
//                if (className!=null && !className.isEmpty())
//                    fragmentClassesPreRunTest.add(Scene.v().getSootClassUnsafe(fragmentDynInfo.getFragmentClassName()));
//                else
//                    Logger.debug("here");
//            }
//            assert fragmentClassesPreRun.equals(fragmentClassesPreRunTest);
//        }
//    }
//
//
//    private Callable<Void> getTask(Activity activity, SootMethod callbackMethod,
//                                   SootMethod methodToExplore, Set<List<Edge>> edges) {
//        return () -> {
//            Logger.debug("Processing: {} reachable from {}", methodToExplore, callbackMethod);
//
//            // Analyze layout: assigns resource id of the activity, and connects it with the layout file
//            if (callbackMethod.method().getName().contains("onCreate")) {
//                analyzeLayout(methodToExplore, activity, edges);
//                LayoutsFinder layoutsFinder = new LayoutsFinder(methodToExplore, activity.getMainClass().getName());
//                layoutsFinder.run();
//                Map<Integer, LayoutInfo> layouts = layoutsFinder.getLayouts();
//                layouts.keySet().forEach(i -> layouts.get(i).setActivityNameOfView(activity.getMainClass().getName()));
//                activity.setLayouts(layouts);
//            }
//
//            // Analyze fragment transaction
//
////            fragmentClassesPreRun.addAll(analyzeFragmentTransaction(method, edges, activity));
////
////            // TODO: compare the two methods
////            FragmentsFinder fragmentsFinder = new FragmentsFinder(method);
////            fragmentsFinder.run();
////            Set<FragmentDynInfo> fragments = fragmentsFinder.getFragments();
////            for (FragmentDynInfo fragmentDynInfo : fragments) {
////                String className = fragmentDynInfo.getFragmentClassName();
////                if (className!=null && !className.isEmpty())
////                    fragmentClassesPreRunTest.add(Scene.v().getSootClassUnsafe(fragmentDynInfo.getFragmentClassName()));
////                else
////                    Logger.debug("here");
////            }
////            assert fragmentClassesPreRun.equals(fragmentClassesPreRunTest);
//
//            DialogsFinder dialogsFinder = new DialogsFinder(methodToExplore);
//            dialogsFinder.run();
//            Set<DialogInfo> dialogs = dialogsFinder.getDialogs();
//            dialogs.forEach(x -> x.setActivity(sootClassName));
//            dialogResults.addAll(dialogs);
//
//            StringsFinder stringsFinder = new StringsFinder(methodToExplore);
//            stringsFinder.run();
//            Set<DynDecStringInfo> strings = stringsFinder.getStrings();
//            strings.forEach(x -> x.setDeclaringSootClass(sootClassName));
//            this.strings.addAll(strings);
//
//            ListenersFinder listenersFinder = new ListenersFinder(methodToExplore);
//            listenersFinder.run();
//            Set<ListenerInfo> listeners = listenersFinder.getListeners();
//            listeners.forEach(x -> x.setDecaringSootClass(sootClassName));
//            this.listeners.addAll(listeners);
//
//            OptionsMenusFinder optionsMenusFinder = new OptionsMenusFinder(methodToExplore);
//            optionsMenusFinder.run();
//            Set<MenuInfo> optionMenus = optionsMenusFinder.getOptionMenus();
//            this.optionMenus.addAll(optionMenus);
//
//            ContextMenusFinder contextMenusFinder = new ContextMenusFinder(methodToExplore);
//            contextMenusFinder.run();
//            Set<MenuInfo> contextMenus = contextMenusFinder.getContextMenus();
//            this.contextMenus.addAll(contextMenus);
//
//            ContextOnCreateMenusFinder contextOnCreateMenusFinder = new ContextOnCreateMenusFinder(methodToExplore);
//            contextOnCreateMenusFinder.run();
//            Set<MenuInfo> contextOnCreateMenus = contextOnCreateMenusFinder.getContextOnCreateMenus();
//            this.contextOnCreateMenus.addAll(contextOnCreateMenus);
//
//            PopupMenusFinder popupMenusFinder = new PopupMenusFinder(methodToExplore);
//            popupMenusFinder.run();
//            Set<PopupMenuInfo> popupMenus = popupMenusFinder.getPopupMenus();
//            this.popupMenus.addAll(popupMenus);
//
//            NavigationDropDownMenusFinder navigationDropDownMenusFinder = new NavigationDropDownMenusFinder(methodToExplore);
//            navigationDropDownMenusFinder.run();
//            Set<DropDownNavMenuInfo> dropDownMenus = navigationDropDownMenusFinder.getDropDownMenus();
//            this.navigationMenus.addAll(dropDownMenus);
//
//            TabViewsFinder tabViewsFinder = new TabViewsFinder(methodToExplore);
//            tabViewsFinder.run();
//            Set<TabViewInfo> tabViews = tabViewsFinder.getTabViews();
//            this.tabViews.addAll(tabViews);
//
//            ActivityTitleFinder titleFinder = new ActivityTitleFinder(methodToExplore, app.getActivities(), sootClassName, subclassesOfActivity);
//            titleFinder.run();
//
//            localLogger.info("Finished entrypoint");
//            return null;
//        };
//    }

    /**
     * Checks whether the method assigns a resource id to the activity
     * @param method The method to check for resource id
     * @param activity The activity
     */
    private void analyzeLayout(SootMethod method, Activity activity, Set<List<Edge>> edges) {
        for (Unit u : method.retrieveActiveBody().getUnits()) {
            Stmt stmt = (Stmt) u;
            if (stmt.containsInvokeExpr()) {
                InvokeExpr inv = stmt.getInvokeExpr();
                // if it invokes setContentView or inflate
                if (invokesSetContentView(inv) || invokesInflate(inv)) {
                    Argument arg = extractIntArgumentFrom(inv);
                    Set<Object> values = ArgumentValueManager.v().getArgumentValues(arg, u, edges);
                    if (values.size()==1) {
                        Object value = values.iterator().next();
                        if (value instanceof Integer)
                            activity.setResourceId((int)value);
                    }
                }
            }
        }
    }

    /**
     * Analyze the methods for fragment transaction
     * @param method The reachable method from the lifecycle method
     * @param edges The edges for context sensitive point-to analysis
     */
    private Set<SootClass> analyzeFragmentTransaction(SootMethod method, Set<List<Edge>> edges) {
        if (AndroidClass.v().scFragment == null || AndroidClass.v().scFragmentTransaction == null) {
            if (AndroidClass.v().scSupportFragment == null || AndroidClass.v().scSupportFragmentTransaction == null) {
                Logger.warn("[{}] Soot classes have not been initialized!", TAG);
                return Collections.emptySet();
            }
        }

        Set<SootClass> fragmentClasses = new HashSet<>();

        // first check if there is a Fragment manager, a fragment transaction
        // and a call to the add method which adds the fragment to the transaction
        boolean isFragmentManager = false;
        boolean isFragmentTransaction = false;
        boolean isAddTransaction = false;
        boolean isReplaceTransaction = false;
        boolean isRemoveTransaction = false;

        // Check if the parameter is Fragment or Fragment Manager
        for (Type type : method.getParameterTypes()){
            if (Scene.v().getOrMakeFastHierarchy().canStoreType(type, AndroidClass.v().scFragmentManager.getType()) ||
                    Scene.v().getOrMakeFastHierarchy().canStoreType(type, AndroidClass.v().scSupportFragmentManager.getType())) {
                isFragmentManager = true;
            }
        }

        if (!method.hasActiveBody() || !method.isConcrete())
            return Collections.emptySet();

        for (Unit u : method.getActiveBody().getUnits()) {
            Stmt stmt = (Stmt) u;
            if (stmt.containsInvokeExpr()) {
                final String methodName = stmt.getInvokeExpr().getMethod().getName();
                switch (methodName) {
                    case "getFragmentManager":
                    case "getSupportFragmentManager":
                        isFragmentManager = true;
                        break;
                    case "beginTransaction":
                        isFragmentTransaction = true;
                        break;
                    case "add":
                    case "attach":
                    case "show":
                        isAddTransaction = true;
                        break;
                    case "replace":
                        isReplaceTransaction = true;
                        break;
                    case "remove":
                    case "detach":
                    case "hide":
                        isRemoveTransaction = true;
                        break;
                }
            }
        }

        // now get the fragment class from the second argument of the add method
        // from the transaction
        if (isFragmentManager && isFragmentTransaction && (isAddTransaction || isReplaceTransaction
                || isRemoveTransaction)) {
            for (Unit u : method.getActiveBody().getUnits()) {
                Stmt stmt = (Stmt) u;
                if (stmt.containsInvokeExpr()) {
                    InvokeExpr invExpr = stmt.getInvokeExpr();
                    if (invExpr instanceof InstanceInvokeExpr) {
                        InstanceInvokeExpr iinvExpr = (InstanceInvokeExpr) invExpr;

                        // Make sure that we referring to the correct class and method
                        isFragmentTransaction = AndroidClass.v().scFragmentTransaction != null && Scene.v().getFastHierarchy()
                                .canStoreType(iinvExpr.getBase().getType(), AndroidClass.v().scFragmentTransaction.getType());
                        isFragmentTransaction |= AndroidClass.v().scSupportFragmentTransaction != null && Scene.v().getFastHierarchy()
                                .canStoreType(iinvExpr.getBase().getType(), AndroidClass.v().scSupportFragmentTransaction.getType());
                        isAddTransaction = stmt.getInvokeExpr().getMethod().getName().equals("add")
                                || stmt.getInvokeExpr().getMethod().getName().equals("attach")
                                || stmt.getInvokeExpr().getMethod().getName().equals("show");
                        isReplaceTransaction = stmt.getInvokeExpr().getMethod().getName().equals("replace");
                        isRemoveTransaction = stmt.getInvokeExpr().getMethod().getName().equals("remove")
                                || stmt.getInvokeExpr().getMethod().getName().equals("detach")
                                || stmt.getInvokeExpr().getMethod().getName().equals("hide");

                        // add fragment
                        if (isFragmentTransaction && (isAddTransaction || isReplaceTransaction || isRemoveTransaction)) {
                            // We take all fragments passed to the method
                            for (int i = 0; i < stmt.getInvokeExpr().getArgCount(); i++) {
                                Value br = stmt.getInvokeExpr().getArg(i);

                                // Is this a fragment?
                                if (br.getType() instanceof RefType) {
                                    RefType rt = (RefType) br.getType();

                                    boolean isParamFragment = AndroidClass.v().scFragment != null
                                            && Scene.v().getFastHierarchy().canStoreType(rt, AndroidClass.v().scFragment.getType());
                                    isParamFragment |= AndroidClass.v().scSupportFragment != null && Scene.v().getFastHierarchy()
                                            .canStoreType(rt, AndroidClass.v().scSupportFragment.getType());
                                    if (isParamFragment) {
                                        if (br instanceof Local) {

                                            // Reveal the possible types
                                            Set<Type> possibleTypes = new HashSet<>();

                                            if (edges!=null && !edges.isEmpty() &&
                                                    config.getPointToType()== GlobalConfig.PointToType.CONTEXT) {
                                                for (List<Edge> edgeList : edges) {
                                                    Edge[] edgeContext = new Edge[edgeList.size()];
                                                    edgeContext = edgeList.toArray(edgeContext);

                                                    // Reveal the possible types of this fragment
                                                    possibleTypes = TypeAnalyzer.v().
                                                            getContextPointToPossibleTypes(br, edgeContext);
                                                }
                                            } else {
                                                possibleTypes = TypeAnalyzer.v().getPointToPossibleTypes(br);
                                            }

                                            Type possibleType = null;
                                            if (possibleTypes.size() < 1) {
                                                Logger.warn("[{}] Unable to retrieve point-to info for: {}", TAG, stmt);
                                                return Collections.emptySet();
                                            } else if (possibleTypes.size() == 1) {
                                                possibleType = possibleTypes.iterator().next();
                                            } else {
                                                Iterator<Type> typeIterator = possibleTypes.iterator();
                                                Type lastType = typeIterator.next();
                                                while (typeIterator.hasNext()) {
                                                    possibleType = typeIterator.next();
                                                    if (Scene.v().getFastHierarchy().canStoreType(lastType, possibleType)) {
                                                        possibleType = lastType;
                                                    } else if (!Scene.v().getFastHierarchy().canStoreType(possibleType, lastType)) {
                                                        Logger.warn("[{}] Multiple possible fragment types detected in: {}", TAG, stmt);
                                                    }
                                                    lastType = possibleType;
                                                }
                                            }
                                            if (possibleType instanceof RefType) {
                                                if (isAddTransaction) {
                                                    fragmentClasses.add(((RefType) possibleType).getSootClass());
                                                } else if (isRemoveTransaction) {
                                                    fragmentClasses.remove(((RefType) possibleType).getSootClass());
                                                } else if (isReplaceTransaction) {
                                                    fragmentClasses.add(((RefType) possibleType).getSootClass());
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return fragmentClasses;
    }

    /**
     * Creates a new screen with the set of fragments
     * @param activity The activity
     * @param fragmentClassesPreRun The fragment classes collected
     * @param staticFragments The fragments declared in XML layout file
     */
    private void createNewScreenWithFragments(Activity activity, Set<SootClass> fragmentClassesPreRun,
                                              Set<Fragment> staticFragments) {
        // Process the fragments
        Set<Fragment> fragments = new HashSet<>(staticFragments);
        for (SootClass sc : fragmentClassesPreRun) {
            Fragment fragment = app.getFragmentByClass(sc);
            if (fragment != null) {
                fragments.add(fragment);
            } else {
                Set<Integer> resIds = backstageApp.getFragmentClassToLayout().get(sc.toString());
                app.createFragment(sc, resIds);
                fragment = app.getFragmentByClass(sc);
                TopologyExtractor.collectExtendedClasses(fragment);
                TopologyExtractor.collectLifecycleMethods(fragment);
                TopologyExtractor.collectCallbackMethods(fragment, app.getCallbacksInSootClass(sc));
            }
        }
        stg.addScreen(new ScreenNode(activity, fragments));
    }

    /**
     * Analyze the changes made to the set of fragments in callback methods
     */
    private void analyzeCallbacksForFragmentChanges() {
        for (ScreenNode screenNode : stg.getAllScreens()) {
            // Gets the activity and fragments
            Activity activity = (Activity) screenNode.getComponent();
            Set<Fragment> fragments = screenNode.getFragments();
            Set<CallbackDefinition> callbacks = activity.getCallbacks();
            if (!fragments.isEmpty()) {
                for (Fragment fragment : fragments) {
                    callbacks.addAll(fragment.getCallbacks());
                }
            }

            // Analyze reachable methods from callback methods
            for (CallbackDefinition callback : callbacks) {

                SootMethod targetMethod = callback.getTargetMethod();

                // Check the filters
                if (!filterAccepts(activity.getMainClass(), targetMethod))
                    continue;
                if (!filterAccepts(activity.getMainClass(), targetMethod.getDeclaringClass()))
                    continue;

                Iterator<Edge> edgesInto = Scene.v().getCallGraph().edgesInto(targetMethod);

                Set<SootClass> fragmentClasses = new HashSet<>();
                CallbackReachableMethods rm = new CallbackReachableMethods(config.getFlowdroidConfig(),
                        activity.getMainClass(), targetMethod);
                rm.update();

                // iterate through the reachable methods
                QueueReader<MethodOrMethodContext> reachableMethods = rm.listener();
                while (reachableMethods.hasNext()) {
                    // Get the next reachable method
                    SootMethod method = reachableMethods.next().method();
                    // Do not analyze system classes
                    if (SystemClassHandler.isClassInSystemPackage(method.getDeclaringClass().getName()))
                        continue;
                    if (!method.isConcrete())
                        continue;

                    // Edges
                    Set<List<Edge>> edges = rm.getContextEdges(method);

                    // Analyze fragment transaction
                    fragmentClasses.addAll(analyzeFragmentTransaction(method, edges));
                }

                // Create screen nodes with the collected fragments
                createNewScreenIfFragmentChanges(screenNode, fragmentClasses, callback);
            }
        }
    }

    /**
     * Creates a new screen if we have found changes in fragments
     * @param screenNode The screen node
     * @param fragmentClasses The fragment classes collected
     */
    private void createNewScreenIfFragmentChanges(ScreenNode screenNode, Set<SootClass> fragmentClasses,
                                                  CallbackDefinition callback) {
        Activity activity = (Activity) screenNode.getComponent();
        Set<Fragment> fragments = screenNode.getFragments();

        if (fragmentClasses!=null && !fragmentClasses.isEmpty()) {
            for (SootClass sc : fragmentClasses) {
                Fragment fragment = app.getFragmentByClass(sc);
                if (fragment == null)
                    fragments.add(new Fragment(sc));
            }
            Logger.debug("[{}] Creating new screen with fragments: {} in callback: {}", TAG, fragmentClasses, callback);
            ScreenNode newScreenNode = new ScreenNode(activity, fragments);
            stg.addScreen(newScreenNode);
            if (callback.getCallbackType().equals(CallbackDefinition.CallbackType.Widget)) {
                // TODO: get the tag
                stg.addTransitionEdge(screenNode, newScreenNode);
            } else
                stg.addTransitionEdge(screenNode, newScreenNode);
        }
    }


    /**
     * Adds a new filter that checks every callback before it is associated with the
     * respective host component
     *
     * @param filter
     *            The filter to add
     */
    public void addCallbackFilter(ICallbackFilter filter) {
        this.callbackFilters.add(filter);
    }

    /**
     * Checks whether all filters accept the association between the callback class
     * and its parent component
     *
     * @param lifecycleElement
     *            The hosting component's class
     * @param targetClass
     *            The class implementing the callbacks
     * @return True if all filters accept the given component-callback mapping,
     *         otherwise false
     */
    private boolean filterAccepts(SootClass lifecycleElement, SootClass targetClass) {
        for (ICallbackFilter filter : callbackFilters)
            if (!filter.accepts(lifecycleElement, targetClass))
                return false;
        return true;
    }

    /**
     * Checks whether all filters accept the association between the callback method
     * and its parent component
     *
     * @param lifecycleElement
     *            The hosting component's class
     * @param targetMethod
     *            The method implementing the callback
     * @return True if all filters accept the given component-callback mapping,
     *         otherwise false
     */
    private boolean filterAccepts(SootClass lifecycleElement, SootMethod targetMethod) {
        for (ICallbackFilter filter : callbackFilters)
            if (!filter.accepts(lifecycleElement, targetMethod))
                return false;
        return true;
    }

//    /**
//     * Analyze the screen for menu/drawer registration
//     * @param screen The screen node
//     */
//    private void analyzeMenuDrawerMethods(Screen screen) {
//        Activity activity = screen.getContainerActivity();
//        Set<Fragment> fragments = screen.getFragments();
//
//        analyzeMenuDrawerInComp(activity, screen);
//        for (Fragment fragment : fragments){
//            analyzeMenuDrawerInComp(fragment, screen);
//        }
//    }
//
//    /**
//     * Analyze the activity for menu/drawer registration
//     * @param component The activity or fragment
//     */
//    private void analyzeMenuDrawerInComp(AbstractComponent component, Screen screen) {
//        Set<MethodOrMethodContext> menuRegs;
//        Set<MethodOrMethodContext> menuCallbacks;
//
//        if (component instanceof Activity) {
//            menuRegs = ((Activity) component).getMenuOnCreateMethods();
//            menuCallbacks = ((Activity) component).getMenuCallbackMethods();
//        } else if (component instanceof Fragment) {
//            menuRegs = ((Fragment) component).getMenuRegistrationMethods();
//            menuCallbacks = ((Fragment) component).getMenuCallbackMethods();
//        } else throw new IllegalArgumentException("Analyzing menu/drawer for non-activity/fragment class!");
//
//        if (menuRegs != null && !menuRegs.isEmpty()) {
//            if (menuCallbacks != null && !menuCallbacks.isEmpty()) {
//                analyzeMenuDrawerRegistration(component.getMainClass(), menuRegs, screen, menuCallbacks,
//                        TopologyExtractor.AnalysisType.BOTH);
//            } else
//                analyzeMenuDrawerRegistration(component.getMainClass(), menuRegs, screen, Collections.emptySet(),
//                        TopologyExtractor.AnalysisType.MENU);
//        } else if (menuCallbacks != null && !menuCallbacks.isEmpty()) {
//            analyzeMenuDrawerRegistration(component.getMainClass(), Collections.emptySet(), screen, menuCallbacks,
//                    TopologyExtractor.AnalysisType.DRAWER);
//        } else {
//            Logger.debug("[{}] No menu methods found for: {} in screen {}", component.getName(), screen.getName());
//        }
//    }
//
//    /**
//     * Analyze drawer menu registration
//     * @param mainClass The main class of the component
//     * @param regMethods The menu registration method found in the component
//     * @param screen The screen node
//     * @param callbackMethods The menu callbacks method found in the component
//     * @param analysisType The analysis type (menu, drawer, both)
//     */
//    private void analyzeMenuDrawerRegistration(SootClass mainClass, Set<MethodOrMethodContext> regMethods, Screen screen,
//                                               Set<MethodOrMethodContext> callbackMethods, TopologyExtractor.AnalysisType analysisType) {
//
//        if (analysisType == TopologyExtractor.AnalysisType.BOTH || analysisType == TopologyExtractor.AnalysisType.MENU) {
//            analyzeMenuMethods(mainClass, regMethods, screen, callbackMethods, analysisType);
//            if (analysisType == TopologyExtractor.AnalysisType.BOTH) {
//                analyzeDrawerMethods(mainClass, screen, callbackMethods, analysisType);
//            }
//        } else {
//            analyzeDrawerMethods(mainClass, screen, callbackMethods, analysisType);
//        }
//    }
//
//    /**
//     * Analyze the menu methods
//     * @param mainClass
//     * @param regMethods
//     * @param screen
//     * @param callbackMethods
//     * @param analysisType
//     */
//    private void analyzeMenuMethods(SootClass mainClass, Set<MethodOrMethodContext> regMethods, Screen screen,
//                                    Set<MethodOrMethodContext> callbackMethods, TopologyExtractor.AnalysisType analysisType){
//        for (MethodOrMethodContext menuReg : regMethods) {
//            ComponentReachableMethods rm = new ComponentReachableMethods(mainClass,
//                    Collections.singletonList(menuReg));
//            rm.update();
//            QueueReader<MethodOrMethodContext> reachableMethods = rm.listener();
//            while (reachableMethods.hasNext()) {
//                SootMethod method = reachableMethods.next().method();
//
//                Body b = method.retrieveActiveBody();
//
//                for (Unit u : b.getUnits()) {
//                    Stmt stmt = (Stmt) u;
//                    if (stmt.containsInvokeExpr()) {
//                        InvokeExpr inv = stmt.getInvokeExpr();
//                        if (invokesGetItemId(inv)) {
//                            Map<Integer, List<PDGNode>> conditionalMapping = PDGUtils.findConditionalMapping(u,
//                                    new HashMutablePDG((UnitGraph)
//                                            AnalysisParameters.v().getIcfg().getOrCreateUnitGraph(b)));
//
//                            for (Integer resId : conditionalMapping.keySet()) {
//                                analyzeCallbackConditionalFlows(resId, conditionalMapping,
//                                        mainClass, screen, callbackMethods, true);
//                            }
//                        }
//                    }
//                }
//            }
//        }
//    }
//
//    /**
//     * Analyze the
//     * @param mainClass
//     * @param screen
//     * @param callbackMethods
//     * @param analysisType
//     */
//    private void analyzeDrawerMethods(SootClass mainClass, Screen screen,
//                                      Set<MethodOrMethodContext> callbackMethods, TopologyExtractor.AnalysisType analysisType){
//        for (MethodOrMethodContext menuCallbacks : callbackMethods) {
//            ComponentReachableMethods rm = new ComponentReachableMethods(mainClass,
//                    Collections.singletonList(menuCallbacks));
//            rm.update();
//            QueueReader<MethodOrMethodContext> reachableMethods = rm.listener();
//            while (reachableMethods.hasNext()) {
//                SootMethod method = reachableMethods.next().method();
//
//                Body b = method.retrieveActiveBody();
//
//                for (Unit u : b.getUnits()) {
//                    Stmt stmt = (Stmt) u;
//                    if (stmt.containsInvokeExpr()) {
//                        InvokeExpr inv = stmt.getInvokeExpr();
//                        if (invokesGetItemId(inv)) {
//                            Map<Integer, List<PDGNode>> conditionalMapping = PDGUtils.findConditionalMapping(u,
//                                    new HashMutablePDG((UnitGraph)
//                                            AnalysisParameters.v().getIcfg().getOrCreateUnitGraph(b)));
//
//                            for (Integer resId : conditionalMapping.keySet()) {
//                                analyzeCallbackConditionalFlows(resId, conditionalMapping,
//                                        mainClass, screen, callbackMethods, false);
//                            }
//                        }
//                    }
//                }
//            }
//        }
//    }
//
//    /**
//     * Analyze the conditional flow from getItemId
//     * This is to reveal which button leads to what consequence, such as menu open, drawer open
//     * @param resId The resource id of the button
//     * @param conditionalMapping The conditional mapping from each resource id to the create the menu or drawer
//     */
//    private void analyzeCallbackConditionalFlows(Integer resId, Map<Integer, List<PDGNode>> conditionalMapping,
//                                                 SootClass mainClass, Screen screen,
//                                                 Set<MethodOrMethodContext> menuCallbacks, boolean analyzeMenu) {
//        List<PDGNode> dependents = conditionalMapping.get(resId);
//
//        ChunkedQueue<PDGNode> pdgNodes = new ChunkedQueue<>();
//        QueueReader unprocessedNodes = pdgNodes.reader();
//
//        dependents.forEach(pdgNodes::add);
//
//        while (unprocessedNodes.hasNext()) {
//            PDGNode nextNode = (PDGNode) unprocessedNodes.next();
//            Iterator<Unit> unitIterator = PDGUtils.unitIteratorOfPDGNode(nextNode);
//            if (!analyzeMenu) {
//                if (findDrawerOpenFromUnits(unitIterator)) {
//                    Drawer drawer = new Drawer(resId);
//                    Screen newScreen = new Screen(screen);
//                    newScreen.setDrawer(drawer);
//                    SSTG.v().addScreen(newScreen);
//                }
//            } else {
//                if (findMenuInflateFromUnits(unitIterator) != null) {
//                    Unit menuInflateUnit = findMenuInflateFromUnits(unitIterator);
//                    createNewScreenWithMenuFromUnits(menuInflateUnit, resId, mainClass, menuCallbacks, screen);
//                }
//            }
//
//            // Inter-proc analysis
//            // analyze reachable methods for each unit
//            Set<Unit> unitsForFurtherAnalysis = new HashSet<>();
//            unitIterator.forEachRemaining(unitsForFurtherAnalysis::add);
//            UnitsReachableMethods unitRM = new UnitsReachableMethods(mainClass,
//                    unitsForFurtherAnalysis);
//            unitRM.update();
//            QueueReader<MethodOrMethodContext> reachables = unitRM.listener();
//            while (reachables.hasNext()) {
//                UnitGraph unitGraph = (UnitGraph) AnalysisParameters.v().getIcfg().
//                        getOrCreateUnitGraph(reachables.next().method());
//                ProgramDependenceGraph newPdg = new HashMutablePDG(unitGraph);
//                newPdg.iterator().forEachRemaining(pdgNodes::add);
//            }
//        }
//    }
//
//
//    private void createNewScreenWithMenuFromUnits(Unit u, Integer resId, SootClass mainClass,
//                                                  Set<MethodOrMethodContext> callbacks, Screen screen) {
//        InvokeExpr inv = ((Stmt)u).getInvokeExpr();
//        Argument arg = extractIntArgumentFrom(inv);
//        Set<Object> values = ArgumentValueManager.v().getArgumentValues(arg, u, null);
//        if (values!=null && !values.isEmpty()) {
//            Object value = values.iterator().next();
//            if (value instanceof Integer) {
//                Menu menu = App.v().getLayoutManager().getMenu((int) value);
//                menu.setButton(ResourceValueProvider.v().getStringById(resId));
//                menu.addCallbackMethods(callbacks);
//                menu.setParentClass(mainClass);
//                Screen newScreen = new Screen(screen);
//                newScreen.setMenu(menu);
//                SSTG.v().addScreen(newScreen);
//            }
//        }
//    }
//
//
//    /**
//     * Finds drawer from units
//     * @param unitIterator The unit iterator
//     * @return True if the drawer can be found, false otherwise
//     */
//    private boolean findDrawerOpenFromUnits(Iterator<Unit> unitIterator) {
//        while (unitIterator.hasNext()) {
//            Unit unit = unitIterator.next();
//            if (unit instanceof Stmt) {
//                if (((Stmt) unit).containsInvokeExpr()) {
//                    InvokeExpr invMethod = ((Stmt) unit).getInvokeExpr();
//                    if (invokesDrawerOpen(invMethod)) {
//                        return true;
//                    }
//                }
//            }
//        }
//        return false;
//    }
//
//    /**
//     * Finds menu from units iterator
//     * @param unitIterator The unit iterator
//     * @return True if the menu can be found, false otherwise
//     */
//    private Unit findMenuInflateFromUnits(Iterator<Unit> unitIterator) {
//        while (unitIterator.hasNext()) {
//            Unit unit = unitIterator.next();
//            if (unit instanceof Stmt) {
//                if (((Stmt) unit).containsInvokeExpr()) {
//                    InvokeExpr invMethod = ((Stmt) unit).getInvokeExpr();
//                    if (invokesMenuInflate(invMethod)) {
//                        return unit;
//                    }
//                }
//            }
//        }
//        return null;
//    }
//
//    /**
//     * Finds method registration within given method
//     * @param sm The method to look up
//     * @return true if the given method registers a menu
//     */
//    private boolean findMenuRegistration(SootMethod sm, SootClass mainClass, Screen screen,
//                                         Set<MethodOrMethodContext> callbacks) {
//        for (Unit u : sm.retrieveActiveBody().getUnits()) {
//            Stmt stmt = (Stmt) u;
//            if (stmt.containsInvokeExpr()) {
//                InvokeExpr inv = stmt.getInvokeExpr();
//                // if it invokes setContentView or inflate
//                if (invokesMenuInflate(inv)) {
//                    Argument arg = extractIntArgumentFrom(inv);
//                    Set<Object> values = ArgumentValueManager.v().getArgumentValues(arg, u, null);
//                    if (values!=null && !values.isEmpty()) {
//                        Object value = values.iterator().next();
//                        if (value instanceof Integer) {
//                            Menu menu = App.v().getLayoutManager().getMenu((int) value);
//                            menu.addCallbackMethods(callbacks);
//                            menu.setParentClass(mainClass);
//                            Screen newScreen = new Screen(screen);
//                            newScreen.setMenu(menu);
//                            SSTG.v().addScreen(newScreen);
//                            return true;
//                        }
//                    }
//                }
//            }
//        }
//        return false;
//    }
//
//
//    /**
//     * Finds all system callback methods
//     * @param fragment The fragment class
//     */
//    private void collectMenuMethods(Fragment fragment) {
//        // Get the list of menu methods
//
//
//        // The list of menu methods (in order of create and callback)
//        menuCreateMethods.iterator().forEachRemaining(x -> {
//            MethodOrMethodContext method = findAndAddMethod(x, fragment);
//            if (method!=null)
//                fragment.addMenuRegistrationMethod(method);
//        });
//
//        menuCallbackMethods.iterator().forEachRemaining(x -> {
//            MethodOrMethodContext method = findAndAddMethod(x, fragment);
//            if (method!=null)
//                fragment.addMenuCallbackMethod(method);
//        });
//    }

    //    /**
//     * Analyze the menu drawer registration
//     * @param screen The screen node
//     * @return true if the screen declares a menu
//     */
//    private boolean analyzeMenuMethods(Screen screen) {
//        Activity containerActivity = screen.getContainerActivity();
//        Set<Fragment> fragments = screen.getFragments();
//
//        // Collects all menu registration methods and menu callback methods
//        if (containerActivity.getMenuOnCreateMethods() != null &&
//                !containerActivity.getMenuOnCreateMethods().isEmpty()) {
//            if (containerActivity.getMenuCallbackMethods() != null &&
//                    !containerActivity.getMenuCallbackMethods().isEmpty()) {
//                return analyzeMenuDrawerRegistration(containerActivity.getMainClass(), containerActivity.getMenuOnCreateMethods()
//                        , screen, containerActivity.getMenuCallbackMethods());
//            } else {
//                Logger.warn("[{}] Menu registration found without callbacks to handle click events: {}",
//                        TAG, containerActivity.getName());
//            }
//        }
//
//        for (Fragment fragment : fragments) {
//            if (fragment.getMenuOnCreateMethods() != null &&
//                    !fragment.getMenuOnCreateMethods().isEmpty()) {
//                if (fragment.getMenuCallbackMethods() != null &&
//                        !fragment.getMenuCallbackMethods().isEmpty()) {
//                    return analyzeMenuDrawerRegistration(fragment.getMainClass(), fragment.getMenuOnCreateMethods(), screen,
//                            fragment.getMenuCallbackMethods());
//                } else {
//                    Logger.warn("[{}] Menu registration found without callbacks to handle click events: {}",
//                            TAG, fragment.getName());
//                }
//            }
//        }
//        return false;
//    }
}
