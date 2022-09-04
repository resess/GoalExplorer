package st.cs.uni.saarland.de.reachabilityAnalysis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmlpull.v1.XmlPullParserException;
import soot.SootClass;
import soot.VoidType;
import soot.jimple.infoflow.android.axml.AXmlNode;
import soot.jimple.infoflow.android.manifest.ProcessManifest;
import soot.jimple.infoflow.android.manifest.binary.AbstractBinaryAndroidComponent;
import st.cs.uni.saarland.de.helpClasses.Helper;
import st.cs.uni.saarland.de.helpClasses.SootHelper;
import st.cs.uni.saarland.de.testApps.AppController;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Created by avdiienko on 25/11/15.
 */
public class ReachabilityAnalysis implements Runnable {

    private final Logger logger;
    private final Set<String> sourcesSinksSignatures;
    private final int rechabilityTimeoutValue;
    private final TimeUnit rechabilityTimeoutUnit;
    private final String apkPath;
    private final List<UiElement> uiElements;
    private final int maxDepthMethodLevel;
    private final boolean loadUiResults;
    private final boolean limitByPackageName;

    private List<UiElement> processedUiElement;
    private final Set<ResultsSaver> resultsSavers = new HashSet<>();

    public ReachabilityAnalysis(List<UiElement> uiElements, Set<String> sourcesSinksSignatures, TimeUnit rTimeUnit, int rTimeValue, String apkPath, int depthMethodLevel, boolean loadUiRes, boolean limitByPackageName){
        this.sourcesSinksSignatures = sourcesSinksSignatures;
        this.rechabilityTimeoutUnit = rTimeUnit;
        this.rechabilityTimeoutValue = rTimeValue;
        this.loadUiResults = loadUiRes;
        this.apkPath = apkPath;
        IntentFilterResolver.retrieveIntentFilters(apkPath);
        this.uiElements = uiElements;
        this.maxDepthMethodLevel = depthMethodLevel;
        this.limitByPackageName = limitByPackageName;
        this.logger = LoggerFactory.getLogger("Reachability Analysis");
        START_ACTIVITY_CONSTANTS.getStartActivityMethods();
        SootClass newClass = SootHelper.createSootClass("CustomIntercomponentClass");
        SootHelper.createSootMethod(newClass, "newActivity", new ArrayList<>(), VoidType.v(), false);
    }

    public void addResultsSaver(ResultsSaver resultsSaver){
        resultsSavers.add(resultsSaver);
    }

    public void run(){
        logger.info("Performing reachability analysis");
        logger.info("Extracting UI elements");
        String logText = String.format("Running reachability analysis with timeout for each callback: %s %s", this.rechabilityTimeoutValue, this.rechabilityTimeoutUnit);
        logger.info(logText);
        Helper.saveToStatisticalFile(logText);
        Map<UiElement, List<ApiInfoForForward>> results = getResults(uiElements);
        logger.info("Extracting preferences");
        PreferenceResolver.v().setIntentFilters(IntentFilterResolver.getActivitiesToIntentFilters());
        PreferenceResolver.v().run().forEach(pref -> AppController.getInstance().updateUiElement(pref.globalId, pref));
        if(!loadUiResults) {
            AppController.getInstance().mergeReachabilityAnalysisResults(results);
        }
        resultsSavers.forEach(x->x.save(results));
        logger.info("Reachability analysis has been finished");
    }

    private Map<UiElement, List<ApiInfoForForward>> getResults(List<UiElement> uiElements) {
        List<UiElement> localUiElements = uiElements.stream().distinct().collect(Collectors.toList());

        final int callbacksFromUIAnalysis = localUiElements.size();
        String msg = String.format("Found %s callback methods from UI Analysis", callbacksFromUIAnalysis);
        logger.info(msg);
        Helper.saveToStatisticalFile(msg);

        final int overallCallbacks = localUiElements.size();

        msg = String.format("Found %s callback methods in overall", overallCallbacks);
        logger.info(msg);
        Helper.saveToStatisticalFile(msg);

        logger.info("Performing reachability analysis");
        AtomicInteger counter = new AtomicInteger(0);
        AtomicInteger timeoutedCallbacks = new AtomicInteger(0);

        final Map<UiElement, List<ApiInfoForForward>> callbackToApis = new ConcurrentHashMap<>();

        ExecutorService mainExecutor = Executors.newFixedThreadPool(RAHelper.numThreads);

        List<Future<Void>> tasks = new ArrayList<>();
        //localUiElements.stream().filter(uiElement -> uiElement.declaringSootClass.endsWith("ISBNSearch")).collect(Collectors.toList())
        localUiElements.forEach(callbackToAnalyze -> {
            tasks.add(mainExecutor.submit(() -> {
                ExecutorService executor = Executors.newSingleThreadExecutor();

                final List<ApiInfoForForward> apis = new CopyOnWriteArrayList<>();
                String elementId = callbackToAnalyze.hasIdInCode() ? Integer.toString(callbackToAnalyze.idInCode) : callbackToAnalyze.elementId;
                CallbackToApiMapper forwardFounder = new CallbackToApiMapper(
                        callbackToAnalyze.handlerMethod, elementId, counter.incrementAndGet(),
                        overallCallbacks, this.maxDepthMethodLevel, this.limitByPackageName, apis, false);
                forwardFounder.setDeclaringSootClass(callbackToAnalyze.declaringSootClass);
                forwardFounder.setSourcesAndSinks(this.sourcesSinksSignatures);

                Future<Void> futureTask =  executor.submit(forwardFounder);
                try {
                    futureTask.get(this.rechabilityTimeoutValue, this.rechabilityTimeoutUnit);
                } catch (TimeoutException e) {
                    logger.info("Timeout");
                    timeoutedCallbacks.incrementAndGet();
                    futureTask.cancel(true);
                } catch (Exception e) {
                    Helper.saveToStatisticalFile(Helper.exceptionStacktraceToString(e));
                    e.printStackTrace();
                    futureTask.cancel(true);
                }
                finally {
                    //here need to see if forwardFounder found any AlertDialog.Builder
                    //need to call dialogfinder, process the results,
                    //maybe not the best place for this
                    //then update set of appsuielements and also setofuielements
                    //then back to list, probably need to use a stack instead for concurrency
                    logger.debug("Performed forward reachability analysis for {} and result {}", callbackToAnalyze, forwardFounder.getNewSootActivityClass());
                    if (forwardFounder.getNewSootActivityClass() != null) {
                        callbackToAnalyze.targetSootClass = forwardFounder.getNewSootActivityClass();
                    }
                    callbackToApis.put(callbackToAnalyze, apis);
                    AppController.getInstance().updateUiElement(callbackToAnalyze.globalId, callbackToAnalyze);
                }
                executor.shutdownNow();
                try {
                    executor.awaitTermination(30, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    logger.error("Executor did not terminate correctly");
                }
                finally {
                    while (!executor.isTerminated()) {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            Helper.saveToStatisticalFile(Helper.exceptionStacktraceToString(e));
                            e.printStackTrace();
                        }
                        logger.info("Waiting for finish");
                    }
                }
                return null;
            }));
        });

        for(Future<Void> task : tasks){
            try {
                task.get();
            } catch (InterruptedException | ExecutionException e) {
                Helper.saveToStatisticalFile(Helper.exceptionStacktraceToString(e));
                e.printStackTrace();
            }
        }
        mainExecutor.shutdown();
        try {
            mainExecutor.awaitTermination(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            logger.error("Executor did not terminate correctly");
        }
        Helper.saveToStatisticalFile(String.format("Used timeout %s %s", this.rechabilityTimeoutValue, this.rechabilityTimeoutUnit));
        Helper.saveToStatisticalFile(String.format("Timeouted callbacks: %s", timeoutedCallbacks));
        processedUiElement = localUiElements;
        return callbackToApis;
    }


    /**
     * Gets the list of UI elements
     * @return
     */
    public List<UiElement> getUiElements() {
        return processedUiElement;
    }
}
