package st.cs.uni.saarland.de.uiAnalysis;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import st.cs.uni.saarland.de.entities.Application;
import st.cs.uni.saarland.de.entities.AppsUIElement;
import st.cs.uni.saarland.de.entities.Listener;
import st.cs.uni.saarland.de.helpClasses.Helper;
import st.cs.uni.saarland.de.searchDialogs.DialogInfo;

import java.util.*;
import java.util.concurrent.*;

public class ExtraUIAnalyzer extends LifecycleUIAnalyzer{
    private final Set<DialogInfo> dialogResults = Collections.synchronizedSet(new HashSet<>());

    public Set<DialogInfo> getDialogResults() {
        return this.dialogResults;
    }

    public ExtraUIAnalyzer(TimeUnit tTimeoutUnit, int tTimeoutValue, int numThreads,
                           int maxDepthMethodLevel, Application app) {
        super(tTimeoutUnit, tTimeoutValue, numThreads, false, maxDepthMethodLevel, app);
        //TODO Auto-generated constructor stub
    }

    public void run(){
        Map<String, Set<SootMethod>> entryPointsOfUiAnalysis = collectCallbackMethods();
        final String entryPointsStat = String.format("Found %s callback classes and %s methods overall", entryPointsOfUiAnalysis.keySet().size(), entryPointsOfUiAnalysis.entrySet().stream().mapToInt(x -> x.getValue().size()).sum());
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



    protected Callable<Void> getTask(String sootClassName, SootMethod methodToExplore) {
        return () -> {
            Logger localLogger = LoggerFactory.getLogger(String.format("%s:%s", methodToExplore.getDeclaringClass().getName().replace(".", "_"), methodToExplore.getName()));
            localLogger.info("Processing callback");

            DialogsFinder dialogsFinder = new DialogsFinder(methodToExplore);
            dialogsFinder.run();
            Set<DialogInfo> dialogs = dialogsFinder.getDialogs();
            dialogs.forEach(x -> x.setActivity(sootClassName));
            dialogResults.addAll(dialogs);
            localLogger.debug("Found additional dialogs {}", dialogResults);

            localLogger.info("Finished callback");
            return null;
        };
    }

    private Map<String, Set<SootMethod>> collectCallbackMethods(){
        Map<String, Set<SootMethod>> entryPointsOfUiAnalysis = new HashMap<>();


        for(AppsUIElement uiElement: app.getAllUIElements()){
            if (!uiElement.hasElementListener())
                continue;
            // don't analyse this tags, they have only layout listener assigned which other tags also have
            if (uiElement.getKindOfUiElement().equals("merge") || uiElement.getKindOfUiElement().equals("menu") || uiElement.getKindOfUiElement().equals("include")) {
                continue;
            }

            if (uiElement.hasElementListener()) {
                Collection<Listener> listeners = uiElement.getListernersFromElement();
                listeners.stream().filter(listener -> listener != null && !StringUtils.isBlank(listener.getListenerClass()))
                        .forEach(listener -> {
                            String listenerClassString = listener.getListenerClass();
                            SootClass listenerClass = Scene.v().getSootClass(listenerClassString);
                            SootMethod listenerMethod = listenerClass.getMethodUnsafe(listener.getListenerMethod());
                            String declaringClass = listener.getDeclaringClass();
                            if(listenerMethod != null && !StringUtils.isBlank(declaringClass)){
                                if(!entryPointsOfUiAnalysis.containsKey(declaringClass))
                                    entryPointsOfUiAnalysis.put(declaringClass, new HashSet<>());
                                entryPointsOfUiAnalysis.get(declaringClass).add(listenerMethod);
                            }
                        });

            }
        }
        return entryPointsOfUiAnalysis;
    }
}
