package st.cs.uni.saarland.de.uiAnalysis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import soot.*;
import soot.jimple.toolkits.callgraph.Edge;
import st.cs.uni.saarland.de.dissolveSpecXMLTags.FragmentDynInfo;
import st.cs.uni.saarland.de.dissolveSpecXMLTags.TabViewInfo;
import st.cs.uni.saarland.de.entities.Application;
import st.cs.uni.saarland.de.helpClasses.Helper;
import st.cs.uni.saarland.de.reachabilityAnalysis.CONSTANT_SIGNATURES;
import st.cs.uni.saarland.de.searchDialogs.DialogInfo;
import st.cs.uni.saarland.de.searchDynDecStrings.DynDecStringInfo;
import st.cs.uni.saarland.de.searchListener.ListenerInfo;
import st.cs.uni.saarland.de.searchMenus.DropDownNavMenuInfo;
import st.cs.uni.saarland.de.searchMenus.MenuInfo;
import st.cs.uni.saarland.de.searchMenus.PopupMenuInfo;
import st.cs.uni.saarland.de.searchScreens.LayoutInfo;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * Created by avdiienko on 11/05/16.
 */
public class UiAnalysisPack extends SceneTransformer {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final TimeUnit tTimeoutUnit;
    private final int tTimeoutValue;
    private final int numThreads;
    private final List<SootClass> subclassesOfAsyncTask;
    private final List<SootClass> subclassesOfApp;
    private final List<SootClass> implementersOfWidget;

    public Set<DialogInfo> getDialogResults() {
        return dialogResults;
    }

    private final Set<DialogInfo> dialogResults = Collections.synchronizedSet(new HashSet<>());

    public Map<Integer, LayoutInfo> getLayouts() {
        return layouts;
    }

    private final Map<Integer, LayoutInfo> layouts = Collections.synchronizedMap(new HashMap<>());

    public Set<DynDecStringInfo> getStrings() {
        return strings;
    }

    private final Set<DynDecStringInfo> strings = Collections.synchronizedSet(new HashSet<>());

    public Set<ListenerInfo> getListeners() {
        return listeners;
    }

    private final Set<ListenerInfo> listeners = Collections.synchronizedSet(new HashSet<>());

    public Set<FragmentDynInfo> getFragments() {
        return fragments;
    }

    private final Set<FragmentDynInfo> fragments = Collections.synchronizedSet(new HashSet<>());

    public Set<MenuInfo> getOptionMenus() {
        return optionMenus;
    }

    private final Set<MenuInfo> optionMenus = Collections.synchronizedSet(new HashSet<>());

    public Set<MenuInfo> getContextMenus() {
        return contextMenus;
    }

    private final Set<MenuInfo> contextMenus = Collections.synchronizedSet(new HashSet<>());

    public Set<MenuInfo> getContextOnCreateMenus() {
        return contextOnCreateMenus;
    }

    private final Set<MenuInfo> contextOnCreateMenus = Collections.synchronizedSet(new HashSet<>());

    public Set<PopupMenuInfo> getPopupMenus() {
        return popupMenus;
    }

    private final Set<PopupMenuInfo> popupMenus = Collections.synchronizedSet(new HashSet<>());

    public Set<DropDownNavMenuInfo> getNavigationMenus() {
        return navigationMenus;
    }

    private final Set<DropDownNavMenuInfo> navigationMenus = Collections.synchronizedSet(new HashSet<>());

    public Set<TabViewInfo> getTabViews() {
        return tabViews;
    }

    private final Set<TabViewInfo> tabViews = Collections.synchronizedSet(new HashSet<>());

    private final boolean processMenus;

    private final Set<SootClass> classesOfAndroidWidgets;

    private final int maxLevel;

    private final Application app;

    private final List<SootClass> subclassesOfActivity;

    public UiAnalysisPack(TimeUnit tTimeoutUnit, int tTimeoutValue, int numThreads, boolean processMenus, int maxDepthMethodLevel, Application app) {
        this.tTimeoutUnit = tTimeoutUnit;
        this.tTimeoutValue = tTimeoutValue;
        this.numThreads = numThreads;
        this.processMenus = processMenus;
        this.classesOfAndroidWidgets = Scene.v().getClasses().stream().filter(x -> x.getName().startsWith("android.") && !x.getName().startsWith("android.view.") && x.isInterface() && !x.getName().endsWith("Listener")).collect(Collectors.toSet());
        this.maxLevel = maxDepthMethodLevel;
        if (!Scene.v().getSootClass("android.os.AsyncTask").isPhantom() && Scene.v().getSootClass("android.os.AsyncTask").resolvingLevel() != 0) {
            this.subclassesOfAsyncTask = Scene.v().getActiveHierarchy().getSubclassesOf(Scene.v().getSootClass("android.os.AsyncTask"));
        } else {
            this.subclassesOfAsyncTask = new ArrayList<>();
        }
        if (!Scene.v().getSootClass("android.app.Application").isPhantom() && Scene.v().getSootClass("android.app.Application").resolvingLevel() != 0) {
            this.subclassesOfApp = Scene.v().getActiveHierarchy().getSubclassesOf(Scene.v().getSootClass("android.app.Application"));
        } else {
            this.subclassesOfApp = new ArrayList<>();
        }
        this.implementersOfWidget = new ArrayList<>();
        for (SootClass aWidgetClass : classesOfAndroidWidgets) {
            this.implementersOfWidget.addAll(Scene.v().getActiveHierarchy().getImplementersOf(aWidgetClass));
        }
        this.app = app;
        this.subclassesOfActivity = Scene.v().getActiveHierarchy().getSubclassesOf(Scene.v().getSootClass("android.app.Activity"));
    }

    @Override
    protected void internalTransform(String phaseName, Map<String, String> options) {
        Set<SootClass> activities = app.getActivities().stream().map(x->x.getName()).filter(x -> !Scene.v().getSootClass(x).isPhantom()).
                map(x -> Scene.v().getSootClass(x)).filter(x -> x.getName().startsWith(Helper.getPackageName())).collect(Collectors.toSet());
        Map<String, Set<SootMethod>> entryPointsOfUiAnalysis = new HashMap<>();

        activities.addAll(this.subclassesOfApp);

        //take all onCreate of each activity.. subclass? and iterate over all rechable methods
        for (SootClass sc : activities) {
            logger.info("Exploring lifecycle methods in activity " + sc.getName());
            for (String activityMethodSignature : CONSTANT_SIGNATURES.activityMethods) {
                SootMethod activityMethod = null;
                if (sc.getMethods().stream().filter(x -> x.getSubSignature().equals(activityMethodSignature)).findAny().isPresent()) {
                    activityMethod = sc.getMethod(activityMethodSignature);
                } else {
                    List<SootClass> superClasses = Scene.v().getActiveHierarchy().getSuperclassesOf(sc);
                    for (SootClass directSuperClass : superClasses) {
                        if (directSuperClass.getMethods().stream().filter(x -> x.getSubSignature().equals(activityMethodSignature)).findAny().isPresent()) {
                            activityMethod = directSuperClass.getMethod(activityMethodSignature);
                            break;
                        }
                    }
                }
                if (activityMethod == null || !activityMethod.hasActiveBody() || activityMethod.getDeclaringClass().getName().startsWith("android.")) {
                    continue;
                }
                if (!entryPointsOfUiAnalysis.containsKey(sc.getName())) {
                    entryPointsOfUiAnalysis.put(sc.getName(), new HashSet<>());
                }
                entryPointsOfUiAnalysis.get(sc.getName()).add(activityMethod);
            }
        }
        for (SootClass sc : Helper.getFragmentsLifecycleClasses()) {
            if (!sc.getName().startsWith(Helper.getPackageName())) {
                continue;
            }

            if (!entryPointsOfUiAnalysis.containsKey(sc.getName())) {
                entryPointsOfUiAnalysis.put(sc.getName(), new HashSet<>());
            }
            logger.info("Exploring lifecycle methods in fragment " + sc.getName());
            entryPointsOfUiAnalysis.get(sc.getName()).addAll(sc.getMethods().stream().filter(x -> Arrays.asList(CONSTANT_SIGNATURES.fragmentMethods).contains(x.getSubSignature())).collect(Collectors.toList()));
        }

        final String entryPointsStat = String.format("Found %s entrypoint classes and %s methods in overall", entryPointsOfUiAnalysis.keySet().size(), entryPointsOfUiAnalysis.entrySet().stream().mapToInt(x -> x.getValue().size()).sum());
        logger.info(entryPointsStat);
        Helper.saveToStatisticalFile(entryPointsStat);

        ExecutorService classExecutor = Executors.newFixedThreadPool(numThreads);
        Set<Future<Void>> classTasks = new HashSet<>();

        for (final String scn : entryPointsOfUiAnalysis.keySet()) {
            List<SootClass> superClasses = Scene.v().getActiveHierarchy().getSuperclassesOfIncluding(Scene.v().getSootClass(scn));
            classTasks.add(classExecutor.submit((Callable<Void>) () -> {
                submitUiTask(entryPointsOfUiAnalysis, scn, superClasses);
                return null;
            }));
        }


        for (Future<Void> classTask : classTasks) {
            try {
                classTask.get(10, TimeUnit.MINUTES);
            } catch (InterruptedException e) {
                logger.info("Interrupted class from a parent thread");
                classTask.cancel(true);
            } catch (TimeoutException e) {
                logger.info("Timeout for class");
                classTask.cancel(true);
            } catch (Exception e) {
                logger.error(Helper.exceptionStacktraceToString(e));
                Helper.saveToStatisticalFile(Helper.exceptionStacktraceToString(e));
                classTask.cancel(true);
            }
        }

        classExecutor.shutdown();

        try {
            classExecutor.awaitTermination(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            logger.error("Executor did not terminate correctly");
        } finally {
            while (!classExecutor.isTerminated()) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Helper.saveToStatisticalFile(Helper.exceptionStacktraceToString(e));
                    e.printStackTrace();
                }
                logger.info("Waiting for finish");
            }
        }

    }

    private void submitUiTask(Map<String, Set<SootMethod>> entryPointsOfUiAnalysis, String sootClassName, List<SootClass> superClasses) {
        Logger uiLogger = LoggerFactory.getLogger("UiClassTask:" + sootClassName);
        uiLogger.info("Start processing of entrypoints");

        ExecutorService uiExecutor = Executors.newSingleThreadExecutor();

        for (SootMethod activityMethod : entryPointsOfUiAnalysis.get(sootClassName)) {
            final Set<SootMethod> reachableMethods = new HashSet<>();
            findAllReachableMethodsUpTo(activityMethod, reachableMethods, maxLevel, new HashSet<>());

            Set<SootMethod> filteredReachableMethods = new HashSet<>();
            Map<String, Set<SootMethod>> grouped = reachableMethods.stream().collect(Collectors.groupingBy(x -> x.getSubSignature(), Collectors.toSet()));
            for (String subSignature : grouped.keySet()) {
                Set<SootMethod> methods = grouped.get(subSignature);
                if (methods.size() == 1 || Arrays.asList(CONSTANT_SIGNATURES.activityMethods).contains(subSignature) || Arrays.asList(CONSTANT_SIGNATURES.fragmentMethods).contains(subSignature)) {
                    filteredReachableMethods.addAll(methods);
                } else {
                    if (methods.stream().filter(x -> !superClasses.contains(x.getDeclaringClass())).findAny().isPresent()) {
                        filteredReachableMethods.addAll(methods);
                        continue;
                    }
                    int minIndex = methods.stream().map(x -> superClasses.indexOf(x.getDeclaringClass())).min((o1, o2) -> o1.compareTo(o2)).get();
                    SootClass minClass = superClasses.get(minIndex);
                    filteredReachableMethods.add(minClass.getMethod(subSignature));
                }
            }
            final String finalReachableMethods = String.format("Found %s reachable methods from %s", filteredReachableMethods.size(), activityMethod);
            uiLogger.info(finalReachableMethods);
            Helper.saveToStatisticalFile(finalReachableMethods);

            for (SootMethod methodToExplore : filteredReachableMethods) {
                if (Thread.currentThread().isInterrupted()) {
                    return;
                }
                Future<Void> task = uiExecutor.submit(
                        getTask(sootClassName, methodToExplore));
                try {
                    task.get(tTimeoutValue, tTimeoutUnit);
                } catch (InterruptedException e) {
                    uiLogger.info("Interrupted an entrypoint " + methodToExplore + " from a parent thread");
                    task.cancel(true);
                } catch (TimeoutException e) {
                    uiLogger.info("Timeout for entrypoint " + methodToExplore);
                    task.cancel(true);
                } catch (Exception e) {
                    uiLogger.error(Helper.exceptionStacktraceToString(e));
                    Helper.saveToStatisticalFile(Helper.exceptionStacktraceToString(e));
                    task.cancel(true);
                }
            }
        }

        uiExecutor.shutdown();

        try {
            uiExecutor.awaitTermination(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            uiLogger.error("Executor did not terminate correctly");
        } finally {
            while (!uiExecutor.isTerminated()) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Helper.saveToStatisticalFile(Helper.exceptionStacktraceToString(e));
                    e.printStackTrace();
                }
                uiLogger.info("Waiting for finish");
            }
        }
        uiLogger.info("Finished processing of entrypoints");
    }

    private Callable<Void> getTask(String sootClassName, SootMethod methodToExplore) {
        return () -> {
            Logger localLogger = LoggerFactory.getLogger(String.format("%s:%s", methodToExplore.getDeclaringClass().getName().replace(".", "_"), methodToExplore.getName()));

            localLogger.info("Processing entrypoint");

            DialogsFinder dialogsFinder = new DialogsFinder(methodToExplore);
            dialogsFinder.run();
            Set<DialogInfo> dialogs = dialogsFinder.getDialogs();
            dialogs.forEach(x -> x.setActivity(sootClassName));
            dialogResults.addAll(dialogs);

            LayoutsFinder layoutsFinder = new LayoutsFinder(methodToExplore, sootClassName);
            layoutsFinder.run();
            Map<Integer, LayoutInfo> layouts = layoutsFinder.getLayouts();
            layouts.keySet().forEach(i -> layouts.get(i).setActivityNameOfView(sootClassName));
            this.layouts.putAll(layouts);

            StringsFinder stringsFinder = new StringsFinder(methodToExplore);
            stringsFinder.run();
            Set<DynDecStringInfo> strings = stringsFinder.getStrings();
            strings.forEach(x -> x.setDeclaringSootClass(sootClassName));
            this.strings.addAll(strings);

            ListenersFinder listenersFinder = new ListenersFinder(methodToExplore);
            listenersFinder.run();
            Set<ListenerInfo> listeners = listenersFinder.getListeners();
            listeners.forEach(x -> x.setDecaringSootClass(sootClassName));
            this.listeners.addAll(listeners);

            FragmentsFinder fragmentsFinder = new FragmentsFinder(methodToExplore);
            fragmentsFinder.run();
            Set<FragmentDynInfo> fragments = fragmentsFinder.getFragments();
            this.fragments.addAll(fragments);

            if (this.processMenus) {

                OptionsMenusFinder optionsMenusFinder = new OptionsMenusFinder(methodToExplore);
                optionsMenusFinder.run();
                Set<MenuInfo> optionMenus = optionsMenusFinder.getOptionMenus();
                this.optionMenus.addAll(optionMenus);

                ContextMenusFinder contextMenusFinder = new ContextMenusFinder(methodToExplore);
                contextMenusFinder.run();
                Set<MenuInfo> contextMenus = contextMenusFinder.getContextMenus();
                this.contextMenus.addAll(contextMenus);

                ContextOnCreateMenusFinder contextOnCreateMenusFinder = new ContextOnCreateMenusFinder(methodToExplore);
                contextOnCreateMenusFinder.run();
                Set<MenuInfo> contextOnCreateMenus = contextOnCreateMenusFinder.getContextOnCreateMenus();
                this.contextOnCreateMenus.addAll(contextOnCreateMenus);

                PopupMenusFinder popupMenusFinder = new PopupMenusFinder(methodToExplore);
                popupMenusFinder.run();
                Set<PopupMenuInfo> popupMenus = popupMenusFinder.getPopupMenus();
                this.popupMenus.addAll(popupMenus);

                NavigationDropDownMenusFinder navigationDropDownMenusFinder = new NavigationDropDownMenusFinder(methodToExplore);
                navigationDropDownMenusFinder.run();
                Set<DropDownNavMenuInfo> dropDownMenus = navigationDropDownMenusFinder.getDropDownMenus();
                navigationMenus.addAll(dropDownMenus);
            }

            TabViewsFinder tabViewsFinder = new TabViewsFinder(methodToExplore);
            tabViewsFinder.run();
            Set<TabViewInfo> tabViews = tabViewsFinder.getTabViews();
            this.tabViews.addAll(tabViews);

            ActivityTitleFinder titleFinder = new ActivityTitleFinder(methodToExplore, app.getActivities(), sootClassName, subclassesOfActivity);
            titleFinder.run();


            localLogger.info("Finished entrypoint");
            return null;
        };
    }

    private void findAllReachableMethodsUpTo(SootMethod m, Set<SootMethod> reachableMethods, int depthLevel, Set<SootMethod> callStack) {

        if (reachableMethods.contains(m) || callStack.contains(m) || callStack.size() > depthLevel || !m.getDeclaringClass().getName().startsWith(Helper.getPackageName())) {
            return;
        }
        reachableMethods.add(m);

        Set<SootMethod> localCallStack = new HashSet<>();
        callStack.add(m);
        localCallStack.addAll(callStack);

        final Iterator<Edge> edges = Scene.v().getCallGraph().edgesOutOf(m);
        while (edges.hasNext()) {
            Edge currentEdge = edges.next();
            if (currentEdge.getTgt().method() == null) {
                continue;
            }
            SootMethod targetMethod = currentEdge.getTgt().method();
            if (callStack.contains(targetMethod) || !targetMethod.hasActiveBody() || !targetMethod.getDeclaringClass().getName().startsWith(Helper.getPackageName())) {
                continue;
            }
            findAllReachableMethodsUpTo(targetMethod, reachableMethods, depthLevel, callStack);

            SootClass cl = currentEdge.getTgt().method().getDeclaringClass();

            callStack.clear();
            callStack.addAll(localCallStack);
            if (subclassesOfAsyncTask.contains(cl)) {
                //add onPostExectute etc
                Set<String> asyncMethods = Helper.getAsyncTasksOnMethods();
                asyncMethods.stream().filter(methodSubSig -> cl.getMethodUnsafe(methodSubSig) != null).
                        forEach(methodSubSig -> findAllReachableMethodsUpTo(cl.getMethod(methodSubSig), reachableMethods, depthLevel, callStack));

            }

            callStack.clear();
            callStack.addAll(localCallStack);

            if (cl.getName().equals("android.webkit.WebViewClient")) {
                Scene.v().getSootClass("android.webkit.WebViewClient").getMethods().stream()
                        .filter(mInSuper -> cl.getMethodUnsafe(mInSuper.getSubSignature()) != null)
                        .map(x -> x.getSubSignature())
                        .forEach(methodSubSig ->
                                findAllReachableMethodsUpTo(cl.getMethod(methodSubSig), reachableMethods, depthLevel, callStack));

            }

            callStack.clear();
            callStack.addAll(localCallStack);

            Set<SootMethod> methodsToProcessInWidgets = new HashSet<>();

            for (SootClass aWidgetClass : classesOfAndroidWidgets) {
                implementersOfWidget.stream().filter(x -> x.equals(cl)).forEach(implInRMethods -> aWidgetClass.getMethods().stream().filter(x -> implInRMethods.getMethodUnsafe(x.getSubSignature()) != null).
                        forEach(y -> methodsToProcessInWidgets.add(implInRMethods.getMethodUnsafe(y.getSubSignature()))));
            }
            methodsToProcessInWidgets.forEach(x -> findAllReachableMethodsUpTo(x, reachableMethods, depthLevel, callStack));

            callStack.clear();
            callStack.addAll(localCallStack);
        }
    }
}
