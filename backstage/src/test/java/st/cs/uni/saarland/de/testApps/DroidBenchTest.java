package st.cs.uni.saarland.de.testApps;

import com.thoughtworks.xstream.XStream;
import org.junit.Before;
import org.junit.Test;
import soot.G;
import soot.Scene;
import st.cs.uni.saarland.de.helpClasses.Helper;
import st.cs.uni.saarland.de.reachabilityAnalysis.ApiInfoForForward;
import st.cs.uni.saarland.de.reachabilityAnalysis.IntentInfo;
import st.cs.uni.saarland.de.reachabilityAnalysis.RAHelper;
import st.cs.uni.saarland.de.reachabilityAnalysis.UiElement;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

/**
 * Created by avdiienko on 10/05/16.
 */
public class DroidBenchTest {

    private Map<UiElement, List<ApiInfoForForward>> loadResults(String apk){
        Map<UiElement, List<ApiInfoForForward>> results;
        XStream xStream = new XStream();
        xStream.processAnnotations(UiElement.class);
        xStream.processAnnotations(ApiInfoForForward.class);
        xStream.processAnnotations(IntentInfo.class);
        xStream.setMode(XStream.NO_REFERENCES);
        results = (Map<UiElement, List<ApiInfoForForward>>) xStream.fromXML(new File("results" + File.separator + apk+ "_forward_apiResults_1.xml"));
        return results;
    }

    @Before
    public void clean(){
        G.reset();
    }

    @Test
    public void asyncTask1Test(){
        final String apkName = "AsyncTask1.apk";
        final String apkPath="testApps/Threading/"+apkName;
        final String androidJar="libs/android.jar";

        runAndValidateResults(apkName, apkPath, androidJar);
    }

    @Test
    public void executor1Test(){
        final String apkName = "Executor1.apk";
        final String apkPath="testApps/Threading/"+apkName;
        final String androidJar="libs/android.jar";

        runAndValidateResults(apkName, apkPath, androidJar);
    }

    @Test
    public void javaThread1Test(){
        final String apkName = "JavaThread1.apk";
        final String apkPath="testApps/Threading/"+apkName;
        final String androidJar="libs/android.jar";

        runAndValidateResults(apkName, apkPath, androidJar);
    }

    @Test
    public void javaThread2Test(){
        final String apkName = "JavaThread2.apk";
        final String apkPath="testApps/Threading/"+apkName;
        final String androidJar="libs/android.jar";

        runAndValidateResults(apkName, apkPath, androidJar);
    }

    @Test
    public void activityCommunication3Test(){
        final String apkName = "ActivityCommunication3.apk";
        final String apkPath="testApps/IntercomponentCommunication/"+apkName;
        final String androidJar="libs/android.jar";

        File oldResults = new File("results"+File.separator+apkName+"_forward_apiResults_1.xml");
        if(oldResults.exists()) {
            if (!oldResults.delete()) {
                fail("Can not delete file with results");
            }
        }


        TestApp.initializeSootForUiAnalysis(apkPath, androidJar, false, true);
        UiElement onCreate = new UiElement();
        onCreate.handlerMethod = Scene.v().getMethod("<edu.mit.icc_componentname_class_constant.OutFlowActivity: void onCreate(android.os.Bundle)>");
        onCreate.signature = onCreate.handlerMethod.toString();
        onCreate.kindOfElement = "LifecycleMethod";
        onCreate.elementId = "LifecycleMethod";

        Settings settings = new Settings();
        settings.apkPath = apkPath;
        settings.androidJar = androidJar;
        settings.limitByPackageName = true;
        settings.loadUiResults = true;

        Helper.setApkName(settings.apkPath);
        Helper.setLogsDir(settings.logsDir);
        Helper.setResultsDir(settings.resultsDir);
        Helper.setLOC(settings.maxLocInMethod);
        RAHelper.numThreads = settings.numThreads;
        Helper.initializeManifestInfo(settings.apkPath);

        List<String> activities = new ArrayList<>();
        activities.add("edu.mit.icc_componentname_class_constant.OutFlowActivity");
        activities.add("edu.mit.icc_componentname_class_constant.InFlowActivity");
        activities.add("edu.mit.icc_componentname_class_constantIsolateActivity");

        TestApp.runReachabilityAnalysis(Collections.singletonList(onCreate), activities, settings);

        Map<UiElement, List<ApiInfoForForward>> results = loadResults(apkName);
        assertNotNull("Results are null", results);

        assertEquals(1, results.size());

        UiElement processedOnCreate = results.keySet().stream().findFirst().get();
        assertEquals(onCreate.signature, processedOnCreate.signature);

        String[] signaturesToCheck = {"<android.telephony.TelephonyManager: java.lang.String getDeviceId()>", "<android.util.Log: int i(java.lang.String,java.lang.String)>"};
        Set<String> apisSignatures = results.get(processedOnCreate).stream().map(x->x.signature).collect(Collectors.toSet());
        Arrays.asList(signaturesToCheck).forEach(x->assertTrue("API is not in the list", apisSignatures.contains(x)));

    }


    private void runAndValidateResults(String apkName, String apkPath, String androidJar) {
        File oldResults = new File("results"+File.separator+apkName+"_forward_apiResults_1.xml");
        if(oldResults.exists()) {
            if (!oldResults.delete()) {
                fail("Can not delete file with results");
            }
        }


        TestApp.initializeSootForUiAnalysis(apkPath, androidJar, false, true);
        UiElement onCreate = new UiElement();
        onCreate.handlerMethod = Scene.v().getMethod("<de.ecspride.MainActivity: void onCreate(android.os.Bundle)>");
        onCreate.signature = onCreate.handlerMethod.toString();
        onCreate.kindOfElement = "LifecycleMethod";
        onCreate.elementId = "LifecycleMethod";

        Settings settings = new Settings();
        settings.apkPath = apkPath;
        settings.androidJar = androidJar;
        settings.limitByPackageName = true;
        settings.loadUiResults = true;

        Helper.setApkName(settings.apkPath);
        Helper.setLogsDir(settings.logsDir);
        Helper.setResultsDir(settings.resultsDir);
        Helper.setLOC(settings.maxLocInMethod);
        RAHelper.numThreads = settings.numThreads;
        Helper.initializeManifestInfo(settings.apkPath);

        TestApp.runReachabilityAnalysis(Collections.singletonList(onCreate), Collections.singletonList("de.ecspride.MainActivity"), settings);

        Map<UiElement, List<ApiInfoForForward>> results = loadResults(apkName);
        assertNotNull("Results are null", results);

        assertEquals(1, results.size());

        UiElement processedOnCreate = results.keySet().stream().findFirst().get();
        assertEquals(onCreate.signature, processedOnCreate.signature);

        String[] signaturesToCheck = {"<android.telephony.TelephonyManager: java.lang.String getDeviceId()>", "<android.util.Log: int d(java.lang.String,java.lang.String)>"};
        Set<String> apisSignatures = results.get(processedOnCreate).stream().map(x->x.signature).collect(Collectors.toSet());
        Arrays.asList(signaturesToCheck).forEach(x->assertTrue("API is not in the list", apisSignatures.contains(x)));
    }

}
