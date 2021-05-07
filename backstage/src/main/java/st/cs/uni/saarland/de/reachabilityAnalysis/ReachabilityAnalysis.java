package st.cs.uni.saarland.de.reachabilityAnalysis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmlpull.v1.XmlPullParserException;
import soot.SootClass;
import soot.VoidType;
import soot.jimple.infoflow.android.axml.AXmlNode;
import soot.jimple.infoflow.android.manifest.Manifest;
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
    private Map<String, List<Map<String, List<String>>>> servicesToIntentFilters = new HashMap<>();
    private Map<String, List<Map<String, List<String>>>> activitiesToIntentFilters = new HashMap<>();
    private Map<String, List<Map<String, List<String>>>> receiversToIntentFilters = new HashMap<>();
    private List<UiElement> processedUiElement;
    private final Set<ResultsSaver> resultsSavers = new HashSet<>();

    public ReachabilityAnalysis(List<UiElement> uiElements, Set<String> sourcesSinksSignatures, TimeUnit rTimeUnit, int rTimeValue, String apkPath, int depthMethodLevel, boolean loadUiRes, boolean limitByPackageName){
        this.sourcesSinksSignatures = sourcesSinksSignatures;
        this.rechabilityTimeoutUnit = rTimeUnit;
        this.rechabilityTimeoutValue = rTimeValue;
        this.loadUiResults = loadUiRes;
        this.apkPath = apkPath;
        retrieveIntentFilters();
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
        if(!loadUiResults) {
            AppController.getInstance().mergeReachabilityAnalysisResults(results);
        }
        resultsSavers.forEach(x->x.save(results));
        logger.info("Reachability analysis has been finished");
    }

    private void retrieveIntentFilters(){
        try {
            Manifest processMan = new Manifest(apkPath);
            String packageName = processMan.getPackageName();
            List<AXmlNode> activities = processMan.getActivities();
            List<AXmlNode> services = processMan.getServices();
            List<AXmlNode> receivers = processMan.getReceivers();


            servicesToIntentFilters.putAll(getIntentFilters(packageName, services));
            activitiesToIntentFilters.putAll(getIntentFilters(packageName, activities));
            receiversToIntentFilters.putAll(getIntentFilters(packageName, receivers));
        } catch (IOException | XmlPullParserException e) {
            e.printStackTrace();
            Helper.saveToStatisticalFile(Helper.exceptionStacktraceToString(e));
        }
    }

    private Map<String, List<Map<String, List<String>>>> getIntentFilters(String packageName, List<AXmlNode> baseNode) {
        Map<String, List<Map<String, List<String>>>> baseNodeToIntentFilters = new HashMap<>();
        for(AXmlNode service: baseNode){
            String name = service.getAttribute("name").getValue().toString();
            if(name.startsWith(".")){
                name = String.format("%s%s", packageName, name);
            }
            List<AXmlNode> nodes = service.getChildrenWithTag("intent-filter");
            if(nodes.size() == 0){
                continue;
            }
            baseNodeToIntentFilters.put(name, new ArrayList<>());
            for(AXmlNode inFilterNode : nodes) {
                Map<String, List<String>> local = new HashMap<>();
                for (AXmlNode subNode : inFilterNode.getChildren()) {
                    String tagName = subNode.getTag().toString();
                    switch (tagName) {
                        case "action":
                        case "category": {
                            if(subNode.hasAttribute("name")) {
                                String attrValue = subNode.getAttribute("name").getValue().toString();
                                if (attrValue.startsWith(".")) {
                                    attrValue = String.format("%s%s", packageName, attrValue);
                                }
                                if (!local.containsKey(tagName)) {
                                    local.put(tagName, new ArrayList());
                                }
                                local.get(tagName).add(attrValue);
                            }
                            break;
                        }
                        case "data": {
                            if(subNode.hasAttribute("mimeType")) {
                                String attrValue = subNode.getAttribute("mimeType").getValue().toString();
                                if (attrValue.startsWith(".")) {
                                    attrValue = String.format("%s%s", packageName, attrValue);
                                }
                                if (!local.containsKey(tagName)) {
                                    local.put(tagName, new ArrayList());
                                }
                                local.get(tagName).add(attrValue);
                            }
                            break;
                        }
                    }
                }
                baseNodeToIntentFilters.get(name).add(local);
            }
        }
        return baseNodeToIntentFilters;
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
        localUiElements.forEach(callbackToAnalyze -> {
            tasks.add(mainExecutor.submit(() -> {
                ExecutorService executor = Executors.newSingleThreadExecutor();

                final List<ApiInfoForForward> apis = new CopyOnWriteArrayList<>();
                CallbackToApiMapper forwardFounder = new CallbackToApiMapper(
                        callbackToAnalyze.handlerMethod, callbackToAnalyze.elementId, counter.incrementAndGet(),
                        overallCallbacks, this.maxDepthMethodLevel, this.limitByPackageName, apis, false);
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
