package st.cs.uni.saarland.de;

import soot.PackManager;
import st.cs.uni.saarland.de.entities.Application;
import st.cs.uni.saarland.de.helpClasses.Helper;
import st.cs.uni.saarland.de.reachabilityAnalysis.UiElement;
import st.cs.uni.saarland.de.testApps.AppController;
import st.cs.uni.saarland.de.testApps.Settings;

import java.util.List;
import java.util.stream.Collectors;

import static st.cs.uni.saarland.de.testApps.TestApp.initializeSootForUiAnalysis;
import static st.cs.uni.saarland.de.testApps.TestApp.performUiAnalysis;
import static st.cs.uni.saarland.de.testApps.TestApp.runReachabilityAnalysis;

public class MockRunner {
    public static void main(String[] args) {
        long beforeRun = System.nanoTime();
        Settings settings = new Settings();
        initializeSootForUiAnalysis(settings.apkPath, settings.androidJar, settings.saveJimple, false);
        AppController appResults = performUiAnalysis(settings);
        if(appResults == null){
            return;
        }
        Helper.saveToStatisticalFile("UI Analysis has run for " + (System.nanoTime() - beforeRun) / 1E9 + " seconds");
        PackManager.v().writeOutput();

        List<UiElement> uiElementObjectsForReachabilityAnalysis =
                appResults.getUIElementObjectsForReachabilityAnalysis(true);
        List<UiElement> distinctUiElements = uiElementObjectsForReachabilityAnalysis
                .stream().distinct().collect(Collectors.toList());
        runReachabilityAnalysis(distinctUiElements, appResults.getActivityNames(), settings);
        Application application =  AppController.getInstance().getApp();

        application.getDialogs();
        application.getMenus();
        application.getUiElementsWithListeners();
        application.getActivityByName("name");
        application.getXmlLayoutFile(1);
        application.getUiElementWithListenerById(1);
        application.getFragmentClassToLayout();
    }
}
