package st.cs.uni.saarland.de.testApps;

import com.beust.jcommander.IDefaultProvider;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import com.thoughtworks.xstream.XStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import soot.PackManager;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.options.Options;
import st.cs.uni.saarland.de.entities.Application;
import st.cs.uni.saarland.de.entities.AppsUIElement;
import st.cs.uni.saarland.de.helpClasses.CallGraphAlgorithms;
import st.cs.uni.saarland.de.helpClasses.Helper;
import st.cs.uni.saarland.de.langDetect.LangHelper;
import st.cs.uni.saarland.de.reachabilityAnalysis.*;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by avdiienko on 20/11/15.
 */
public class TestApp {


    private static long beforeRun;
    private static Logger logger = LoggerFactory.getLogger("Backstage");
    private static final IDefaultProvider DEFAULT_PROVIDER = optionName -> ("-rTimeoutUnit".equals(optionName) ||
            "-tTimeoutUnit".equals(optionName)) ? "MINUTES" : null;

    public static void main(String[] args) {
        long startApp = System.nanoTime();

        Helper.trackUsedMemory();

        Settings settings = new Settings();
        JCommander jc = new JCommander(settings);
        jc.setDefaultProvider(DEFAULT_PROVIDER);

        try {
            jc.parse(args);
            Helper.setApkName(settings.apkPath);
            Helper.setLogsDir(settings.logsDir);
            Helper.setResultsDir(settings.resultsDir);
            Helper.setLOC(settings.maxLocInMethod);
            RAHelper.numThreads = settings.numThreads;
            Helper.initializeManifestInfo(settings.apkPath);
        } catch (ParameterException e) {
            System.err.println(e.getMessage());
            jc.usage();
            System.exit(1);
        }
        if(!settings.noLang) {
            if (!settings.loadUiResults) {
                LangHelper langHelper = LangHelper.getInstance();
                if (!langHelper.isEnglish(settings.apkToolOutputPath)) {
                    Helper.saveToStatisticalFile("Non english language detected. Aborting");
                    return;
                }
            }
        }

        initialize();

        AppController appResults;
        if (settings.loadUiResults) {
            final String fileName = String.format("%s/appSerialized.txt", settings.apkToolOutputPath);
            File uiFile = new File(fileName);
            if (!uiFile.exists()) {
                logger.error(String.format("File %s does not exist. Performing Ui Analysis", fileName));
                beforeRun = System.nanoTime();
                initializeSootForUiAnalysis(settings.apkPath, settings.androidJar, settings.saveJimple, false);
                appResults = performUiAnalysis(settings);
                if(appResults == null){
                    return;
                }
                Helper.saveToStatisticalFile("UI Analysis has run for " + (System.nanoTime() - beforeRun) / 1E9 + " seconds");
            } else {
                logger.info("Loading UI results from the previous run");
                initializeSootForUiAnalysis(settings.apkPath, settings.androidJar, settings.saveJimple, true);
                appResults = loadUiResults(uiFile, settings);
                PackManager.v().runPacks();
            }
            PackManager.v().writeOutput();
        } else {
            beforeRun = System.nanoTime();
            initializeSootForUiAnalysis(settings.apkPath, settings.androidJar, settings.saveJimple, false);
            appResults = performUiAnalysis(settings);
            if(appResults == null){
                return;
            }
            Helper.saveToStatisticalFile("UI Analysis has run for " + (System.nanoTime() - beforeRun) / 1E9 + " seconds");
            PackManager.v().writeOutput();
        }

        if (settings.performReachabilityAnalysis) {
            List<UiElement> uiElementObjectsForReachabilityAnalysis = appResults.getUIElementObjectsForReachabilityAnalysis(true);
            List<UiElement> distinctUiElements = uiElementObjectsForReachabilityAnalysis.stream().distinct().collect(Collectors.toList());
            runReachabilityAnalysis(distinctUiElements, appResults.getActivityNames(), settings);
        }
        Application app = AppController.getInstance().getApp();
        saveStatistics();
        Helper.saveToStatisticalFile("App Analysis has run for " + (System.nanoTime() - startApp) / 1E9 + " seconds");
    }

    private static AppController loadUiResults(File uiFile, Settings settings) {
        XStream xstreamI = new XStream();
        xstreamI.alias("AppsUIElement", AppsUIElement.class);
        xstreamI.setMode(XStream.ID_REFERENCES);
        Content.getInstance(new File(settings.apkToolOutputPath).getAbsolutePath());
        return new AppController((Application) xstreamI.fromXML(uiFile));
    }

    public static AppController performUiAnalysis(Settings settings) {
        boolean resultToken = new Main_UI_Analysis(settings.tTimeoutUnit, settings.tTimeoutValue, settings.numThreads,
                settings.processMenus, settings.maxDepthMethodLevel ,settings.isTest).runAnalysisForOneApp(new File(settings.apkToolOutputPath), settings.process_images);
        if(!resultToken){
            return null;
        }
        return AppController.getInstance();
    }

    private static void saveStatistics() {
        Helper.timeoutedPhasesInUIAnalysis.keySet().forEach(phase -> {
            String msg = String.format("Timeouted jobs for phase %s: %s", phase, Helper.timeoutedPhasesInUIAnalysis.get(phase).get());
            logger.info(msg);
            Helper.saveToStatisticalFile(msg);
        });
        long memoryConsumptionOnStart = Helper.getUsedMemory();
        Helper.trackUsedMemory();
        long memoryConsumptionInTheEnd = Helper.getUsedMemory();
        long memoryConsumption = Math.max(memoryConsumptionOnStart, memoryConsumptionInTheEnd);
        Helper.saveToStatisticalFile(String.format("Max memory consumption: %s megabytes", memoryConsumption / 1E6));

    }

    private static void initialize() {
        Helper.loadNotAnalyzedLibs();
        Helper.loadBundlesAndParsable();
        Helper.deleteLogFileIfExist();
    }

    private static String appendClasspath(String appPath, String libPath) {
        String s = (appPath != null && !appPath.isEmpty()) ? appPath : "";

        if (libPath != null && !libPath.isEmpty()) {
            if (!s.isEmpty())
                s += File.pathSeparator;
            s += libPath;
        }
        return s;
    }

    public static void initializeSootForUiAnalysis(String apkPath, String androidJar, boolean saveJimple, boolean loadUiResults) {
        Options.v().set_src_prec(Options.src_prec_apk);
        if (saveJimple) {
            Options.v().set_output_format(Options.output_format_jimple);
        } else {
            Options.v().set_output_format(Options.output_format_none);
        }
        Options.v().set_exclude(Helper.excludedPrefixes);
        Options.v().set_force_android_jar(androidJar);
        Options.v().set_android_jars(androidJar);
        Options.v().set_allow_phantom_refs(true);
        Options.v().set_process_dir(Collections.singletonList(apkPath));
        Options.v().set_soot_classpath(androidJar);
        Options.v().set_process_multiple_dex(true);
        if(!loadUiResults) {
            Options.v().set_whole_program(true);
            Options.v().setPhaseOption("cg", "all-reachable:true");
        }
        /*Options.v().set_whole_program(true);
        Options.v().setPhaseOption("cg", "trim-clinit:false");*/

        Scene.v().loadNecessaryClasses();
    }

    public static void initializeSoot(String appPath, String libPath, Collection<String> classes, CallGraphAlgorithms cgAlgo) {
        // reset Soot:
        logger.info("Resetting Soot...");
        soot.G.reset();

        Options.v().set_exclude(Helper.excludedPrefixes);
        Options.v().set_no_bodies_for_excluded(true);
        Options.v().set_allow_phantom_refs(true);
        Options.v().set_process_multiple_dex(true);

        if (logger.isDebugEnabled())
            Options.v().set_output_format(Options.output_format_jimple);
        else
            Options.v().set_output_format(Options.output_format_none);


        Options.v().set_soot_classpath(appendClasspath(appPath, libPath));

        Options.v().set_whole_program(true);

        Options.v().set_src_prec(Options.src_prec_apk);
        soot.options.Options.v().set_force_android_jar(libPath);

        switch (cgAlgo){
            case RTA:{
                Options.v().setPhaseOption("cg.spark", "on");
                Options.v().setPhaseOption("cg.spark", "on-fly-cg:false");
                Options.v().setPhaseOption("cg.spark", "rta:true");
                break;
            }
            case VTA:{
                Options.v().setPhaseOption("cg.spark", "on");
                Options.v().setPhaseOption("cg.spark", "vta:true");
            }
            case SPARK:{
                Options.v().setPhaseOption("cg.spark", "on");
            }
        }
        // load all entryPoint classes with their bodies
        for (String className : classes)
            Scene.v().addBasicClass(className, SootClass.BODIES);
        Scene.v().loadNecessaryClasses();
        logger.info("Basic class loading done.");

        boolean hasClasses = false;
        for (String className : classes) {
            SootClass c = Scene.v().forceResolve(className, SootClass.BODIES);
            if (c != null) {
                c.setApplicationClass();
                if (!c.isPhantomClass() && !c.isPhantom()) {
                    hasClasses = true;
                }
            }
        }
        if (!hasClasses) {
            logger.error("Only phantom classes loaded, skipping analysis...");
            Helper.saveToStatisticalFile("Only phantom classes loaded, skipping analysis...");
            System.exit(1);
        }
    }

    public static void runReachabilityAnalysis(List<UiElement> uiElements, List<String> activityNames, Settings settings) {

        Helper.clearCache();

        final Iterator<UiElement> uiElementIterator = uiElements.iterator();
        while (uiElementIterator.hasNext()) {
            UiElement uiElem = uiElementIterator.next();
            if (!uiElem.handlerMethod.getDeclaringClass().getName().startsWith(Helper.getPackageName()) ||
                    uiElem.handlerMethod.getDeclaringClass().isPhantom()) {
                uiElementIterator.remove();
            }
        }

        beforeRun = System.nanoTime();

        //add classes from callbacks and activities to the application classes

        Set<String> classes = new HashSet<>();
        //UiCallbacks
        for (UiElement uiEl : uiElements) {

            classes.add(uiEl.handlerMethod.getDeclaringClass().getName());
            //superClasses
            classes.addAll(Scene.v().getActiveHierarchy().getSuperclassesOf(uiEl.handlerMethod.getDeclaringClass()).
                    stream().filter(x ->
                    x.getName().startsWith(Helper.getPackageName())).map(x -> x.getName()).collect(Collectors.toList()));
            //interfaces
            classes.addAll(uiEl.handlerMethod.getDeclaringClass().getInterfaces().stream().filter(x ->
                    x.getName().startsWith(Helper.getPackageName()))
                    .map(x -> x.getName()).collect(Collectors.toList()));
        }

        List<String> validActivityNames = new ArrayList<>();


        //Activities
        for (String activityName : activityNames) {
            SootClass sc = Scene.v().getSootClassUnsafe(activityName);
            if(!sc.getName().startsWith(Helper.getPackageName()))
                continue;

            if (sc != null && !sc.isPhantom()) {
                classes.add(activityName);
                validActivityNames.add(activityName);
                //superClasses
                classes.addAll(Scene.v().getActiveHierarchy().getSuperclassesOf(sc).
                        stream().filter(x ->
                        x.getName().startsWith(Helper.getPackageName())).map(x -> x.getName()).collect(Collectors.toList()));
                //interfaces
                classes.addAll(sc.getInterfaces().stream().filter(x -> x.getName().startsWith(Helper.getPackageName()))
                        .map(x -> x.getName()).collect(Collectors.toList()));
            }
        }
        //Services
        Set<String> serviceMethodsToAdd = new HashSet<>();
        if(!Scene.v().getSootClass(START_ACTIVITY_CONSTANTS.ANDROID_APP_SERVICE).isPhantom() && Scene.v().getSootClass(START_ACTIVITY_CONSTANTS.ANDROID_APP_SERVICE).resolvingLevel() != 0) {
            for (SootClass serviceExtender : Scene.v().getActiveHierarchy().getDirectSubclassesOf(Scene.v().getSootClass(START_ACTIVITY_CONSTANTS.ANDROID_APP_SERVICE))) {
                if (!serviceExtender.getPackageName().startsWith(Helper.getPackageName())) {
                    continue;
                }

                //add to classes
                classes.add(serviceExtender.getName());

                //add to entry points
                serviceExtender.getMethods().stream().forEach(serviceMethod -> {
                    if (new HashSet<>(Arrays.asList(LifecycleConstants.serviceMethods)).contains(serviceMethod.getSubSignature())) {
                        serviceMethodsToAdd.add(serviceMethod.getSignature());
                    }
                });
            }
        }
        Set<String> runnableMethodsToAdd = new HashSet<>();
        //TODO: runnable
        if(!Scene.v().getSootClass("java.lang.Runnable").isPhantom() && Scene.v().getSootClass("java.lang.Runnable").resolvingLevel() != 0) {
            Set<SootClass> runnableImplementers = Scene.v().getActiveHierarchy().getImplementersOf(Scene.v().
                    getSootClass("java.lang.Runnable")).stream().filter(x ->
                    x.getName().startsWith(Helper.getPackageName())).collect(Collectors.toSet());

            for (SootClass runnable : runnableImplementers) {
                classes.add(runnable.getName());

                runnableMethodsToAdd.addAll(runnable.getMethods().stream().filter(x->x.getName().equals("run")).map(x -> x.getSignature()).collect(Collectors.toList()));
            }
        }

        Set<String> asyncMethodsToAdd = new HashSet<>();
        //TODO: Async Tasks
        if(!Scene.v().getSootClass("android.os.AsyncTask").isPhantom() && Scene.v().getSootClass("android.os.AsyncTask").resolvingLevel() != 0) {
            Set<SootClass> asyncTaskExtenders = Scene.v().getActiveHierarchy().getSubclassesOf(Scene.v().
                    getSootClass("android.os.AsyncTask")).stream().filter(x ->
                    x.getName().startsWith(Helper.getPackageName())).collect(Collectors.toSet());

            for (SootClass async : asyncTaskExtenders) {
                classes.add(async.getName());

                asyncMethodsToAdd.addAll(async.getMethods().stream().filter(x->x.getName().equals("doInBackground")).map(x -> x.getSignature()).collect(Collectors.toList()));
            }
        }


        initializeSoot(settings.apkPath, settings.androidJar, classes, settings.cgAlgo);
        //refresh SootMethods
        uiElements.forEach(uiEl -> uiEl.handlerMethod = Scene.v().getMethod(uiEl.handlerMethod.getSignature()));

        // create callback methods for entrypoints
        Set<SootMethod> callbackMethods = new HashSet<>();

        //add service methods
        serviceMethodsToAdd.stream().filter(x->Scene.v().containsMethod(x)).forEach(serviceMethod -> callbackMethods.add(Scene.v().getMethod(serviceMethod)));

        //add async methods
        asyncMethodsToAdd.stream().filter(x->Scene.v().containsMethod(x)).forEach(asyncMethod -> callbackMethods.add(Scene.v().getMethod(asyncMethod)));

        //add runnable methods
        runnableMethodsToAdd.stream().filter(x->Scene.v().containsMethod(x)).forEach(runnableMethod -> callbackMethods.add(Scene.v().getMethod(runnableMethod)));

        //resolve interfaces and superclasses of callbacks for RA
        List<UiElement> uiElementsToAdd = new ArrayList<>();
        callbackMethods.addAll(uiElements.stream().map(uiEl -> uiEl.handlerMethod).collect(Collectors.toList()));

        uiElements.addAll(uiElementsToAdd);


        //add from lifecycle methods
        for (String acClass : validActivityNames) {
            SootClass sc = Scene.v().getSootClass(acClass);
            //search the same lifecycle signature in superclasses
            //it always produce the direct superclass first
            List<SootClass> superClasses = Scene.v().getActiveHierarchy().getSuperclassesOf(sc).
                    stream().filter(x -> !Helper.isClassInSystemPackage(x.getName())).collect(Collectors.toList());

            for (String lifecycleMethod : LifecycleConstants.activityMethods) {
                SootMethod sm = sc.getMethodUnsafe(lifecycleMethod);
                addToUiElements(uiElements, callbackMethods, acClass, sm, settings.RA_LIFECYCLE);
                if (sm == null) {
                    //it doesn't exist here but can be in superclasses
                    for (SootClass x : superClasses) {
                        SootMethod smInSuperClass = x.getMethodUnsafe(lifecycleMethod);
                        addToUiElements(uiElements, callbackMethods, acClass, smInSuperClass, settings.RA_LIFECYCLE);
                        if (smInSuperClass != null) {
                            break;
                        }
                    }
                }
            }
        }


        Scene.v().setEntryPoints(new ArrayList<>(callbackMethods));

        logger.info("Entry points calculation is done");
        Helper.saveToStatisticalFile("Entry point creation has run for " + (System.nanoTime() - beforeRun) / 1E9 + " seconds");

        beforeRun = System.nanoTime();
        try {
            // Run the soot-based operations
            PackManager.v().getPack("wjpp").apply();
            PackManager.v().getPack("cg").apply();
            PackManager.v().getPack("wjtp").apply();
        }
        catch (Exception e){
            e.printStackTrace();
            Helper.saveToStatisticalFile(Helper.exceptionStacktraceToString(e));
            return;
        }

        Helper.saveToStatisticalFile("CallGraph has built for " + (System.nanoTime() - beforeRun) / 1E9 + " seconds");

        String cgSizeInfo = String.format("CallGraph has %s edges", Scene.v().getCallGraph().size());
        logger.info(cgSizeInfo);
        Helper.saveToStatisticalFile(cgSizeInfo);

        beforeRun = System.nanoTime();

        Set<String> sourcesAndSinksSignatures = Helper.loadSourcesAndSinks();
        logger.info(String.format("Loaded %s sources and sinks", sourcesAndSinksSignatures.size()));

        ReachabilityAnalysis rAnalysis = new ReachabilityAnalysis(uiElements.stream().distinct().collect(Collectors.toList()), sourcesAndSinksSignatures,
                settings.rTimeoutUnit, settings.rTimeoutValue, settings.apkPath, settings.maxDepthMethodLevel, settings.loadUiResults, settings.limitByPackageName);
        rAnalysis.addResultsSaver(new XmlResultsSaver());
        rAnalysis.run();
        Helper.saveToStatisticalFile("Reachability Analysis has run for " + (System.nanoTime() - beforeRun) / 1E9 + " seconds");
    }

    private static void addToUiElements(List<UiElement> uiElements, Set<SootMethod> callbackMethods, String acClass, SootMethod sm, boolean callBackMethodsIncluded) {
        if (sm != null) {
            callbackMethods.add(sm);
            if(callBackMethodsIncluded) {
                UiElement uiElement = new UiElement();
                uiElement.handlerMethod = sm;
                uiElement.signature = Helper.getSignatureOfSootMethod(sm);
                uiElement.kindOfElement = "ActivityLifecycleMethod";
                uiElement.elementId = acClass;
                uiElements.add(uiElement);
            }
        }
    }

}
