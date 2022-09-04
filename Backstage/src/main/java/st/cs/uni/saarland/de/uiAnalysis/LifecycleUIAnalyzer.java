package st.cs.uni.saarland.de.uiAnalysis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.jimple.toolkits.callgraph.*;
import st.cs.uni.saarland.de.dissolveSpecXMLTags.AdapterViewInfo;
import st.cs.uni.saarland.de.dissolveSpecXMLTags.FragmentDynInfo;
import st.cs.uni.saarland.de.dissolveSpecXMLTags.TabViewInfo;
import st.cs.uni.saarland.de.entities.Activity;
import st.cs.uni.saarland.de.entities.Application;
import st.cs.uni.saarland.de.helpClasses.Helper;
import st.cs.uni.saarland.de.reachabilityAnalysis.CONSTANT_SIGNATURES;
import st.cs.uni.saarland.de.searchDialogs.DialogInfo;
import st.cs.uni.saarland.de.searchDynDecStrings.DynDecStringInfo;
import st.cs.uni.saarland.de.searchListener.ListenerInfo;
import st.cs.uni.saarland.de.searchMenus.DropDownNavMenuInfo;
import st.cs.uni.saarland.de.searchMenus.MenuInfo;
import st.cs.uni.saarland.de.searchMenus.PopupMenuInfo;
import st.cs.uni.saarland.de.searchPreferences.PreferenceInfo;
import st.cs.uni.saarland.de.searchScreens.LayoutInfo;
import st.cs.uni.saarland.de.searchTabs.TabInfo;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class LifecycleUIAnalyzer {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    protected final TimeUnit tTimeoutUnit;
    protected final int tTimeoutValue;
    protected final int numThreads;
    protected static Set<SootClass> subclassesOfAsyncTask = new HashSet<>();
    protected static List<SootClass> subclassesOfApp;
    protected static Set<SootClass> implementersOfWidget;

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


    public Set<PreferenceInfo> getPreferences() {
        return preferences;
    }

    private final Set<PreferenceInfo> preferences = Collections.synchronizedSet(new HashSet<>());


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

    public Set<TabInfo> getTabs() {
        return tabs;
    }

    private final Set<TabInfo> tabs = Collections.synchronizedSet(new HashSet<>());

    private final Set<TabViewInfo> tabViews = Collections.synchronizedSet(new HashSet<>());

    public Set<AdapterViewInfo> getAdapterViews() { return adapterViews;
    }

    private final Set<AdapterViewInfo> adapterViews = Collections.synchronizedSet(new HashSet<>());

    private final boolean processMenus;

    private static Set<SootClass> classesOfAndroidWidgets;

    private final int maxLevel;

    protected final Application app;

    private final List<SootClass> subclassesOfActivity;

    public LifecycleUIAnalyzer(TimeUnit tTimeoutUnit, int tTimeoutValue, int numThreads, boolean processMenus, int maxDepthMethodLevel, Application app) {
        this.tTimeoutUnit = tTimeoutUnit;
        this.tTimeoutValue = tTimeoutValue;
        this.numThreads = numThreads;
        this.processMenus = processMenus;
        this.classesOfAndroidWidgets = Scene.v().getClasses().stream().filter(x -> x.getName().startsWith("android.") && !x.getName().startsWith("android.view.") && x.resolvingLevel() != 0 && x.isInterface() && !x.getName().endsWith("Listener")).collect(Collectors.toSet());
        this.maxLevel = maxDepthMethodLevel;
        this.app = app;
       /* if (!Scene.v().getSootClass("android.os.AsyncTask").isPhantom() && Scene.v().getSootClassUnsafe("android.os.AsyncTask", false) != null && Scene.v().getSootClassUnsafe("android.os.AsyncTask", false).resolvingLevel() != 0) {
            this.subclassesOfAsyncTask = Scene.v().getActiveHierarchy().getSubclassesOf(Scene.v().getSootClass("android.os.AsyncTask"));
        } else {
            this.subclassesOfAsyncTask = new ArrayList<>();
        }*/
        if (/*!Scene.v().getSootClass("android.app.Application").isPhantom() &&*/ Scene.v().getSootClass("android.app.Application").resolvingLevel() != 0) {
            this.subclassesOfApp = Scene.v().getActiveHierarchy().getSubclassesOf(Scene.v().getSootClass("android.app.Application"));
        } else {
            this.subclassesOfApp = new ArrayList<>();
        }
        this.implementersOfWidget = new HashSet<>();
        for (SootClass aWidgetClass : classesOfAndroidWidgets) {
            //why does this throw a null pointer exception
            List<SootClass> implementers = Scene.v().getActiveHierarchy().getImplementersOf(aWidgetClass);
            if(implementers != null)
                this.implementersOfWidget.addAll(implementers);
        }
        this.subclassesOfActivity = Scene.v().getActiveHierarchy().getSubclassesOf(Scene.v().getSootClass("android.app.Activity"));


    }


    public void run() {

       Set<SootClass> activities = app.getActivities().stream().filter(x -> !Scene.v().getSootClass(x.getName()).isPhantom())
                .filter(x -> Helper.isClassInAppNameSpace(x.getName()))
                .map(x -> Scene.v().getSootClass(x.getName()))
                .collect(Collectors.toSet());

        Map<String, Set<SootMethod>> entryPointsOfUiAnalysis = new HashMap<>();
        logger.info("The activities before filtering {}", app.getActivities().stream().map(Activity::getName).filter(x -> !Scene.v().getSootClass(x).isPhantom()).collect(Collectors.toSet()));
        logger.info("The activities after filtering {}", activities);
        logger.info("The subclasses of activities {}", this.subclassesOfActivity);
        logger.info("The subclasses of app {}", this.subclassesOfApp);
        activities.addAll(this.subclassesOfApp);
        activities.addAll(this.subclassesOfActivity);
        logger.info("The activities after addition {} {}", activities.size(), activities);
        activities = activities.stream().filter(x-> Helper.isClassInAppNameSpace(x.getName())).collect(Collectors.toSet());
        logger.info("The activities after filtering {} {}", activities.size(), activities);

        //take all onCreate of each activity.. subclass? and iterate over all rechable methods
        for (SootClass sc : activities) {
            logger.info("Exploring lifecycle methods in activity " + sc.getName());
            for (String activityMethodSignature : CONSTANT_SIGNATURES.activityMethods) { //TODO, only parse the lifecycle once but do the mapping to ui elements later on?
                SootMethod activityMethod = null;
                if (sc.getMethods().stream().anyMatch(x -> x.getSubSignature().equals(activityMethodSignature))) {
                    activityMethod = sc.getMethod(activityMethodSignature);
                } else { //TODO comment this out, then when building the ui elements, need to look at subclasses that don't implement the method I guess
                    /**
                     * Here, we still need to parse callbacks from superclasses which would not be parsed already (i.e in the manifest)
                     * Or maybe it's fine since we take anything tha's a subclass of Activity?
                     */
                    List<SootClass> superClasses = Scene.v().getActiveHierarchy().getSuperclassesOf(sc);
                    for (SootClass directSuperClass : superClasses) {
                        if (directSuperClass.getMethods().stream().anyMatch(x -> x.getSubSignature().equals(activityMethodSignature))) {
                            activityMethod = directSuperClass.getMethod(activityMethodSignature);
                            break;
                        }
                    }
                }
                //TODO retrieve method here?
                //For activities inside non parse package
                if (activityMethod == null)
                    continue;
                if(!activityMethod.hasActiveBody()){
                    try {
                        activityMethod.retrieveActiveBody();
                        logger.warn("Retrieved active body not parsed by FD: {}", activityMethod);
                    }
                    catch (Exception e){
                       continue;
                    }
                }
                if(!activityMethod.hasActiveBody() || activityMethod.getDeclaringClass().getName().startsWith("android.")) {
                    continue;
                }
                if (!entryPointsOfUiAnalysis.containsKey(sc.getName())) {
                    entryPointsOfUiAnalysis.put(sc.getName(), new HashSet<>());
                }
                entryPointsOfUiAnalysis.get(sc.getName()).add(activityMethod);
            }
        }
        for (SootClass sc : Helper.getFragmentsLifecycleClasses()) {
            if (!Helper.isClassInAppNameSpace(sc.getName())) {
                continue;
            }

            if (!entryPointsOfUiAnalysis.containsKey(sc.getName())) {
                entryPointsOfUiAnalysis.put(sc.getName(), new HashSet<>());
            }
            logger.info("Exploring lifecycle methods in fragment " + sc.getName());
            entryPointsOfUiAnalysis.get(sc.getName()).addAll(sc.getMethods().stream().filter(x -> Arrays.asList(CONSTANT_SIGNATURES.fragmentMethods).contains(x.getSubSignature())).collect(Collectors.toList()));
        }

        final String entryPointsStat = String.format("Found %s entrypoint classes and %s methods in overall", entryPointsOfUiAnalysis.keySet().size(), entryPointsOfUiAnalysis.values().stream().mapToInt(Set::size).sum());
        logger.info(entryPointsStat);
        Helper.saveToStatisticalFile(entryPointsStat);

        ExecutorService classExecutor = Executors.newFixedThreadPool(numThreads);
        Set<Future<Void>> classTasks = new HashSet<>();

        for (final String scn : entryPointsOfUiAnalysis.keySet()) {
                List<SootClass> superClasses = Scene.v().getActiveHierarchy().getSuperclassesOfIncluding(Scene.v().getSootClass(scn));
                classTasks.add(classExecutor.submit(() -> {
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
                logger.warn("Timeout for class");
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
            logger.info("Executor did not terminate correctly");
            logger.error("Executor did not terminate correctly");
        } finally {
            while (!classExecutor.isTerminated()) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Helper.saveToStatisticalFile(Helper.exceptionStacktraceToString(e));
                    e.printStackTrace();
                }
                logger.info("Waiting for class finish");
            }
        }

    }

    protected void submitUiTask(Map<String, Set<SootMethod>> entryPointsOfUiAnalysis, String sootClassName, List<SootClass> superClasses) {
        Logger uiLogger = LoggerFactory.getLogger("UiClassTask:" + sootClassName);
        uiLogger.info("Start processing of entrypoints");
        boolean interrupt = false;

        long startTime = System.nanoTime();
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
                    int minIndex = methods.stream().map(x -> superClasses.indexOf(x.getDeclaringClass())).min(Comparator.naturalOrder()).get();
                    SootClass minClass = superClasses.get(minIndex);
                    filteredReachableMethods.add(minClass.getMethod(subSignature));
                }
            }
            final String finalReachableMethods = String.format("Found %s reachable methods from %s", filteredReachableMethods.size(), activityMethod);
            uiLogger.info(finalReachableMethods);
            uiLogger.warn(finalReachableMethods);
            Helper.saveToStatisticalFile(finalReachableMethods);

            for (SootMethod methodToExplore : filteredReachableMethods) {
                if (Thread.currentThread().isInterrupted()) {
                    uiLogger.error("Thread should be interrupted");
                    interrupt = true;
                    //break;
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
           /* if(interrupt)
                break;*/
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
                uiLogger.info("Waiting for ui tasks to finish");
            }
        }
        long endTime = System.nanoTime();
        uiLogger.info("Finished processing of entrypoints");
        String toWrite = sootClassName +": Finished processing of entrypoints after "+ ((endTime - startTime) /1000000000)+" seconds.";
        Helper.saveToStatisticalFile(toWrite);
        //uiLogger.warn("Finished processing of entrypoints");
    }

    protected Callable<Void> getTask(String sootClassName, SootMethod methodToExplore) {
        return () -> {
            Logger localLogger = LoggerFactory.getLogger(String.format("%s: %s:%s", sootClassName,methodToExplore.getDeclaringClass().getName().replace(".", "_"), methodToExplore.getName()));

            localLogger.info("Processing entrypoint");

            long startTime = System.nanoTime();
            DialogsFinder dialogsFinder = new DialogsFinder(methodToExplore);
            dialogsFinder.run();
            Set<DialogInfo> dialogs = dialogsFinder.getDialogs();
            dialogs.forEach(x -> x.setActivity(sootClassName));
            dialogResults.addAll(dialogs);
            long endTime = System.nanoTime();
            localLogger.info("Dialogs analysis took {} seconds", (endTime - startTime)/1000000000);

            LayoutsFinder layoutsFinder = new LayoutsFinder(methodToExplore, sootClassName);
            layoutsFinder.run();
            Map<Integer, LayoutInfo> layouts = layoutsFinder.getLayouts();
            layouts.keySet().forEach(i -> layouts.get(i).setActivityNameOfView(sootClassName));
            this.layouts.putAll(layouts);
            startTime = endTime;
            endTime = System.nanoTime();
            localLogger.info("Layout analysis took {} seconds", (endTime - startTime)/1000000000);


            PreferencesFinder preferencesFinder = new PreferencesFinder(methodToExplore);
            preferencesFinder.run();
            Set<PreferenceInfo> preferences = preferencesFinder.getPreferences();
            preferences.forEach(i -> i.setActivityName(sootClassName));
            this.preferences.addAll(preferences);
            if (preferences != null && !preferences.isEmpty())
                localLogger.debug("Preferences for {} {}", sootClassName, preferences);
            startTime = endTime;
            endTime = System.nanoTime();
            localLogger.info("Preferences analysis took {} seconds", (endTime - startTime)/1000000000);

            StringsFinder stringsFinder = new StringsFinder(methodToExplore);
            stringsFinder.run();
            Set<DynDecStringInfo> strings = stringsFinder.getStrings();
            strings.forEach(x -> x.setDeclaringSootClass(sootClassName));
            this.strings.addAll(strings);
            startTime = endTime;
            endTime = System.nanoTime();
            localLogger.info("String analysis took {} seconds", (endTime - startTime)/1000000000);

            ListenersFinder listenersFinder = new ListenersFinder(methodToExplore);
            listenersFinder.run();
            Set<ListenerInfo> listeners = listenersFinder.getListeners();
            listeners.forEach(x -> x.setDecaringSootClass(sootClassName));
            this.listeners.addAll(listeners);
            startTime = endTime;
            endTime = System.nanoTime();
            localLogger.info("Listener analysis took {} seconds", (endTime - startTime)/1000000000);

            FragmentsFinder fragmentsFinder = new FragmentsFinder(methodToExplore);
            fragmentsFinder.run();
            Set<FragmentDynInfo> fragments = fragmentsFinder.getFragments();
            this.fragments.addAll(fragments);
            startTime = endTime;
            endTime = System.nanoTime();
            localLogger.info("Fragment analysis took {} seconds", (endTime - startTime)/1000000000);

            if (this.processMenus) {
                Map<String, String> dynStrings = null;
                if(!strings.isEmpty()){
                    //logger.debug("The dynamic strings extracted for method {}"+strings, methodToExplore);
                    dynStrings = strings.stream().filter(info -> !info.getUiEIDReg().isEmpty()).collect(Collectors.toMap(DynDecStringInfo::getUiEIDReg, DynDecStringInfo::getText, (text1, text2) -> {
                        localLogger.warn("Duplicate dynamic string entries found :  {} {}", text1, text2);
                        return text1;
                    }));
                    //logger.debug("Before processing menus, processed strings in method {} {}", methodToExplore, dynStrings);
                }
                OptionsMenusFinder optionsMenusFinder = new OptionsMenusFinder(methodToExplore, dynStrings);
                optionsMenusFinder.run();
                Set<MenuInfo> optionMenus = optionsMenusFinder.getOptionMenus();
                optionMenus.forEach(x -> {
                    String storedActivity = x.getActivityClassName();
                    if(!storedActivity.isEmpty() && !sootClassName.equals(storedActivity)){
                        localLogger.warn("Stored activity {} differs from entrypoint {}", storedActivity, sootClassName);
                        //logger.error("Stored activity {} differs from entrypoint {}", storedActivity, sootClassName);
                    }
                    x.setActivityClassName(sootClassName); //or declaring soot class?
                });
                if(optionMenus != null && !optionMenus.isEmpty())
                    localLogger.debug("Option menus for method {} {}", methodToExplore.getName(), optionMenus);
                this.optionMenus.addAll(optionMenus);
                //iterate through the menuinfos and setActivity to sootClassName if empty or if sootclassname inherits

                startTime = endTime;
                endTime = System.nanoTime();
                localLogger.info("Options menu analysis took {} seconds", (endTime - startTime)/1000000000);


                //TODO: provide dyn strings to all of these

                ContextMenusFinder contextMenusFinder = new ContextMenusFinder(methodToExplore, dynStrings);
                //logger.debug("Context menus BEFORE {}", contextMenusFinder.getContextMenus());
                contextMenusFinder.run();
                Set<MenuInfo> contextMenus = contextMenusFinder.getContextMenus();
                contextMenus.forEach(x -> {
                    String storedActivity = x.getActivityClassName();
                    if(!storedActivity.isEmpty() && !sootClassName.equals(storedActivity)){
                        localLogger.warn("Stored activity {} differs from entrypoint {}", storedActivity, sootClassName);
                        //logger.error("Stored activity {} differs from entrypoint {}", storedActivity, sootClassName);
                    }
                    x.setActivityClassName(sootClassName);
                });
                if(contextMenus != null && !contextMenus.isEmpty())
                    localLogger.debug("Context menus for method {} {}", methodToExplore.getName(),contextMenus);
                this.contextMenus.addAll(contextMenus);

                //Elements to which context menu is attached
                ContextOnCreateMenusFinder contextOnCreateMenusFinder = new ContextOnCreateMenusFinder(methodToExplore, dynStrings);
                contextOnCreateMenusFinder.run();
                Set<MenuInfo> contextOnCreateMenus = contextOnCreateMenusFinder.getContextOnCreateMenus();
                contextOnCreateMenus.forEach(x -> x.setActivityClassName(sootClassName));
                this.contextOnCreateMenus.addAll(contextOnCreateMenus);
                startTime = endTime;
                endTime = System.nanoTime();
                localLogger.info("Context menu analysis took {} seconds", (endTime - startTime)/1000000000);

                PopupMenusFinder popupMenusFinder = new PopupMenusFinder(methodToExplore);
                popupMenusFinder.run();
                Set<PopupMenuInfo> popupMenus = popupMenusFinder.getPopupMenus();
                this.popupMenus.addAll(popupMenus);
                startTime = endTime;
                endTime = System.nanoTime();
                localLogger.info("Popup menu analysis took {} seconds", (endTime - startTime)/1000000000);

                NavigationDropDownMenusFinder navigationDropDownMenusFinder = new NavigationDropDownMenusFinder(methodToExplore);
                navigationDropDownMenusFinder.run();
                Set<DropDownNavMenuInfo> dropDownMenus = navigationDropDownMenusFinder.getDropDownMenus();
                navigationMenus.addAll(dropDownMenus);
                startTime = endTime;
                endTime = System.nanoTime();
                localLogger.info("Navigation menu analysis took {} seconds", (endTime - startTime)/1000000000);
            }
            else 
                localLogger.warn("Process menus disabled for method {}", methodToExplore);

            TabViewsFinder tabViewsFinder = new TabViewsFinder(methodToExplore);
            tabViewsFinder.run();
            Set<TabViewInfo> tabViews = tabViewsFinder.getTabViews();
            this.tabViews.addAll(tabViews);
            startTime = endTime;
                endTime = System.nanoTime();
                localLogger.info("Tab view analysis took {} seconds", (endTime - startTime)/1000000000);

            TabsFinder tabsFinder = new TabsFinder(methodToExplore);
            tabsFinder.run();
            Set<TabInfo> tabs = tabsFinder.getTabs();
            this.tabs.addAll(tabs);
            startTime = endTime;
            endTime = System.nanoTime();
            localLogger.info("Tab analysis took {} seconds", (endTime - startTime)/1000000000);

            //Set<DynDecStringInfo> adapterInfo = strings.stream().filter(info -> info.getArraySwitch() != null).collect(Collectors.toSet()); //TODO extend with other adapters eventually

            Map<String, String> adapterViewsIDs = null;
            if(!listeners.isEmpty()) {
                adapterViewsIDs = listeners.stream().filter(info -> !info.getLastRegister().isEmpty()).collect(Collectors.toMap(ListenerInfo::getLastRegister, ListenerInfo::getSearchedEID, (eid1, eid2) -> {
                    localLogger.warn("Duplicate entries for adapter view listener id {} {}", eid1, eid2);
                    return eid1;
                }));
                //logger.debug("Register ui elements and ids for this method{} {}", methodToExplore, adapterViewsIDs);
            }
            else{
                //logger.debug("No ui elements listener found for this method {}", methodToExplore);
                adapterViewsIDs = null;
            }
            AdapterViewsFinder adapterViewsFinder = new AdapterViewsFinder(methodToExplore, adapterViewsIDs);
            adapterViewsFinder.run();
            Set<AdapterViewInfo> adapterViews = adapterViewsFinder.getAdapterViews();
            //listViews.forEach(listViewInfo -> listViewInfo.set);

            if(!adapterViews.isEmpty()){
                localLogger.debug("Adapter views  for method {} {}", methodToExplore, adapterViews);
                       }
            this.adapterViews.addAll(adapterViews);
            startTime = endTime;
            endTime = System.nanoTime();
            localLogger.info("Adapter view analysis took {} seconds", (endTime - startTime)/1000000000);

            ActivityTitleFinder titleFinder = new ActivityTitleFinder(methodToExplore, app.getActivities(), sootClassName, subclassesOfActivity);
            titleFinder.run();
            startTime = endTime;
            endTime = System.nanoTime();
            localLogger.info("Activity title analysis took {} seconds", (endTime - startTime)/1000000000);

            //TODO Add AdapterViewFinder I guess?

            localLogger.info("Finished entrypoint");
            return null;
        };
    }


    private void findAllReachableMethodsUpTo(SootMethod m, Set<SootMethod> reachableMethods, int depthLevel, Set<SootMethod> callStack) {
        Logger localLogger = LoggerFactory.getLogger(String.format("%s:%s, %s", m.getDeclaringClass().getName().replace(".", "_"), m.getName(),callStack.size()));
        if (/*Thread.currentThread().isInterrupted() ||*/ reachableMethods.contains(m) || callStack.contains(m) || callStack.size() > depthLevel || !Helper.isClassInAppNameSpace(m.getDeclaringClass().getName())) {
            return;
        }
        long startTime = System.nanoTime();
        reachableMethods.add(m);

        Set<SootMethod> localCallStack = new HashSet<>();
        callStack.add(m);
        localCallStack.addAll(callStack);
        int count = 0;

        if(!m.hasActiveBody()){
            try {
                m.retrieveActiveBody();
                localLogger.debug("Retrieved active body");
            }
            catch (Exception e){
                localLogger.warn("Skipping method, no active body");
                return;
            }
        }

        //Maybe interrupt here?
        if(callStack.size() < depthLevel){
            localLogger.debug("Computing edges ...");
            Iterator targets = new Targets(Scene.v().getCallGraph().edgesOutOf(m));

            while (targets.hasNext()) {

                SootMethod targetMethod = (SootMethod) targets.next();
                localLogger.debug("Found method {}", targetMethod);

                if (callStack.contains(targetMethod) || !targetMethod.hasActiveBody() || !Helper.isClassInAppNameSpace(targetMethod.getDeclaringClass().getName())) {
                    continue;
                }
                count ++;
                findAllReachableMethodsUpTo(targetMethod, reachableMethods, depthLevel, callStack);

                SootClass cl = targetMethod.getDeclaringClass();

                callStack.clear();
                callStack.addAll(localCallStack);
                if (subclassesOfAsyncTask.contains(cl) || (cl.hasSuperclass() && cl.getSuperclass().getName().equals(Helper.ASYNCTASKCLASS))) {
                    //add onPostExectute etc
                    subclassesOfAsyncTask.add(cl);
                    Set<String> asyncMethods = Helper.getAsyncTasksOnMethods();
                    asyncMethods.stream().filter(methodSubSig -> cl.getMethodUnsafe(methodSubSig) != null).
                            forEach(methodSubSig -> findAllReachableMethodsUpTo(cl.getMethod(methodSubSig), reachableMethods, depthLevel, callStack));

            }

                callStack.clear();
                callStack.addAll(localCallStack);

                if (cl.getName().equals("android.webkit.WebViewClient")) {
                    Scene.v().getSootClass("android.webkit.WebViewClient").getMethods().stream()
                            .map(x -> x.getSubSignature())
                            .filter(subSignature -> cl.getMethodUnsafe(subSignature) != null)
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
        long endTime = System.nanoTime();
        localLogger.info("Found and processed all {} edges for {} s", count, (endTime - startTime)/1000000000);

    }
}
