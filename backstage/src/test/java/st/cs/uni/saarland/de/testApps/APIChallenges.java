package st.cs.uni.saarland.de.testApps;

import com.thoughtworks.xstream.XStream;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import st.cs.uni.saarland.de.reachabilityAnalysis.ApiInfoForForward;
import st.cs.uni.saarland.de.reachabilityAnalysis.IntentInfo;
import st.cs.uni.saarland.de.reachabilityAnalysis.UiElement;

import java.io.File;
import java.util.*;

import static org.junit.Assert.*;

/**
 * Created by Isa on 23.12.2015.
 */
public class APIChallenges {

    private static Map<UiElement, List<ApiInfoForForward>> results;
    String sendAPISignature = "<android.telephony.SmsManager: void sendTextMessage(java.lang.String,java.lang.String,java.lang.String,android.app.PendingIntent,android.app.PendingIntent)>";
    String internetAPISignature = "<android.net.ConnectivityManager: android.net.NetworkInfo getActiveNetworkInfo()>";
    String internetIntentSignature = "";

    @BeforeClass
    public static void setUp() throws Exception {
        String [] params = {"-apk",
                "testApps/APIChallenges.apk",
                "-androidJar",
                "libs/android.jar",
                "-apkToolOutput",
                "testApps/APIChallenges",
                "-saveJimple",
                "-rAnalysis",
                "-processMenus",
                "-test"};

        File oldResults = new File("results"+File.separator+"APIChallenges.apk_forward_apiResults_1.xml");
        if(oldResults.exists()) {
            if (!oldResults.delete()) {
                throw new Exception("Can not delete file with results");
            }
        }


        TestApp.main(params);

        XStream xStream = new XStream();
        xStream.processAnnotations(UiElement.class);
        xStream.processAnnotations(ApiInfoForForward.class);
        xStream.processAnnotations(IntentInfo.class);
        xStream.setMode(XStream.NO_REFERENCES);
        results = (Map<UiElement, List<ApiInfoForForward>>) xStream.fromXML(new File("results" + File.separator + "APIChallenges.apk_forward_apiResults_1.xml"));
        assertNotNull("Results are null", results);
    }

    @Test
    @Ignore
    public void hierarchyTest(){
        Optional<UiElement> callbackCandidate =  results.keySet().stream().filter(x->x.signature.equals("<com.example.APIChallenges.BaseClass1: void onClick(android.view.View)>")).findFirst();
        assertTrue("Callback is absent", callbackCandidate.isPresent());

        UiElement callback = callbackCandidate.get();
        List<ApiInfoForForward> apis = results.get(callback);
        assertNotNull("At least one API is missing", apis);
        assertFalse("At least one API is missing", apis.isEmpty());

        assertEquals("ElementId should be 2131034112", "2131034112", callback.elementId);

        List<String> apisToCompare = new ArrayList<String>(){{add(sendAPISignature);}};

        Set<String> elementAPIs = AppController.getInstance().getAPIsOfElement(2131034112);
        Arrays.asList(apisToCompare).forEach(x->assertTrue("API is not in the list", elementAPIs.contains(x)));
    }

    @Test
    public void multipleMethodsTest(){
        Optional<UiElement> callbackCandidate =  results.keySet().stream().filter(x->x.signature.equals("<com.example.APIChallenges.MyActivity$1: void onClick(android.view.View)>")).findFirst();
        assertTrue("Callback is absent", callbackCandidate.isPresent());

        UiElement callback = callbackCandidate.get();
        List<ApiInfoForForward> apis = results.get(callback);
        assertNotNull("At least one API is missing", apis);
        assertFalse("At least one API is missing", apis.isEmpty());

        assertEquals("ElementId should be 2131034115", "2131034115", callback.elementId);

        String[] apisToCompare = {sendAPISignature};

        Set<String> elementAPIs = AppController.getInstance().getAPIsOfElement(2131034115);
        Arrays.asList(apisToCompare).forEach(x->assertTrue("API is not in the list", elementAPIs.contains(x)));
    }

    @Test
    public void multipleAPIsInOneMethodTest(){
        Optional<UiElement> callbackCandidate =  results.keySet().stream().filter(x->x.signature.equals("<com.example.APIChallenges.MyActivity$2: void onClick(android.view.View)>")).findFirst();
        assertTrue("Callback is absent", callbackCandidate.isPresent());

        UiElement callback = callbackCandidate.get();
        List<ApiInfoForForward> apis = results.get(callback);
        assertNotNull("At least one API is missing", apis);
        assertFalse("At least one API is missing", apis.isEmpty());

        assertEquals("ElementId should be 2131034116", "2131034116", callback.elementId);

        String[] apisToCompare = {sendAPISignature, internetAPISignature};

        Set<String> elementAPIs = AppController.getInstance().getAPIsOfElement(2131034116);
        Arrays.asList(apisToCompare).forEach(x->assertTrue("API is not in the list", elementAPIs.contains(x)));
    }

    @Test
    public void multipleAPIsInMultipleMethodsTest(){
        Optional<UiElement> callbackCandidate =  results.keySet().stream().filter(x->x.signature.equals("<com.example.APIChallenges.MyActivity$3: void onClick(android.view.View)>")).findFirst();
        assertTrue("Callback is absent", callbackCandidate.isPresent());

        UiElement callback = callbackCandidate.get();
        List<ApiInfoForForward> apis = results.get(callback);
        assertNotNull("At least one API is missing", apis);
        assertFalse("At least one API is missing", apis.isEmpty());

        assertEquals("ElementId should be 2131034117", "2131034117", callback.elementId);

        String[] apisToCompare = {sendAPISignature, internetAPISignature};

        Set<String> elementAPIs = AppController.getInstance().getAPIsOfElement(2131034117);
        Arrays.asList(apisToCompare).forEach(x->assertTrue("API is not in the list", elementAPIs.contains(x)));
    }

    @Test
    public void newActivityTest(){
        Optional<UiElement> callbackCandidate =  results.keySet().stream().filter(x->x.signature.equals("<com.example.APIChallenges.MyActivity$4: void onClick(android.view.View)>")).findFirst();
        assertTrue("Callback is absent", callbackCandidate.isPresent());

        UiElement callback = callbackCandidate.get();
        List<ApiInfoForForward> apis = results.get(callback);
        assertNotNull("At least one API is missing", apis);
        assertFalse("At least one API is missing", apis.isEmpty());

        assertEquals("ElementId should be 2131034118", "2131034118", callback.elementId);

        String[] apisToCompare = {sendAPISignature};

        Set<String> elementAPIs = AppController.getInstance().getAPIsOfElement(2131034118);
        Arrays.asList(apisToCompare).forEach(x->assertTrue("API is not in the list", elementAPIs.contains(x)));
    }

    @Test
    public void multipleActivitiesTest(){
        Optional<UiElement> callbackCandidate =  results.keySet().stream().filter(x->x.signature.equals("<com.example.APIChallenges.MyActivity$5: void onClick(android.view.View)>")).findFirst();
        assertTrue("Callback is absent", callbackCandidate.isPresent());

        UiElement callback = callbackCandidate.get();
        List<ApiInfoForForward> apis = results.get(callback);
        assertNotNull("At least one API is missing", apis);
        assertFalse("At least one API is missing", apis.isEmpty());

        assertEquals("ElementId should be 2131034119", "2131034119", callback.elementId);

        String[] apisToCompare = {sendAPISignature, internetAPISignature};

        Set<String> elementAPIs = AppController.getInstance().getAPIsOfElement(2131034119);
        Arrays.asList(apisToCompare).forEach(x->assertTrue("API is not in the list", elementAPIs.contains(x)));
    }

    @Test
    @Ignore
    public void useInternetIntentTest(){
        Optional<UiElement> callbackCandidate =  results.keySet().stream().filter(x->x.signature.equals("<com.example.APIChallenges.MyActivity$6: void onClick(android.view.View)>")).findFirst();
        assertTrue("Callback is absent", callbackCandidate.isPresent());

        UiElement callback = callbackCandidate.get();
        List<ApiInfoForForward> apis = results.get(callback);
        assertNotNull("At least one API is missing", apis);
        assertFalse("At least one API is missing", apis.isEmpty());

        assertEquals("ElementId should be 2131034120", "2131034120", callback.elementId);

        String[] apisToCompare = {internetIntentSignature};

        Set<String> elementAPIs = AppController.getInstance().getAPIsOfElement(2131034120);
        Arrays.asList(apisToCompare).forEach(x->assertTrue("API is not in the list", elementAPIs.contains(x)));
    }

    @Test
    @Ignore
    public void onDestroyTest(){
        Optional<UiElement> callbackCandidate =  results.keySet().stream().filter(x->x.signature.equals("<com.example.APIChallenges.MyActivity$7: void onClick(android.view.View)>")).findFirst();
        assertTrue("Callback is absent", callbackCandidate.isPresent());

        UiElement callback = callbackCandidate.get();
        List<ApiInfoForForward> apis = results.get(callback);
        assertNotNull("At least one API is missing", apis);
        assertFalse("At least one API is missing", apis.isEmpty());

        assertEquals("ElementId should be 2131034121", "2131034121", callback.elementId);

        String[] apisToCompare = {sendAPISignature};

        Set<String> elementAPIs = AppController.getInstance().getAPIsOfElement(2131034121);

        Arrays.asList(apisToCompare).forEach(x->assertTrue("API is not in the list", elementAPIs.contains(x)));
    }

    @Test
    public void simpleAPICallTest(){
        Optional<UiElement> callbackCandidate =  results.keySet().stream().filter(x->x.signature.equals("<com.example.APIChallenges.SuperClass1: void onClick(android.view.View)>")).findFirst();
        assertTrue("Callback is absent", callbackCandidate.isPresent());

        UiElement callback = callbackCandidate.get();
        List<ApiInfoForForward> apis = results.get(callback);
        assertNotNull("At least one API is missing", apis);
        assertFalse("At least one API is missing", apis.isEmpty());

        assertEquals("ElementId should be 2131034122", "2131034122", callback.elementId);

        String[] apisToCompare = {sendAPISignature};

        Set<String> elementAPIs = AppController.getInstance().getAPIsOfElement(2131034122);

        Arrays.asList(apisToCompare).forEach(x->assertTrue("API is not in the list", elementAPIs.contains(x)));
    }

    @Test
    public void compositeListenerTest(){
        Optional<UiElement> callbackCandidate =  results.keySet().stream().filter(x->x.signature.equals("<com.example.APIChallenges.CompositeListener: void onClick(android.view.View)>")).findFirst();
        assertTrue("Callback is absent", callbackCandidate.isPresent());

        UiElement callback = callbackCandidate.get();
        List<ApiInfoForForward> apis = results.get(callback);
        assertNotNull("At least one API is missing", apis);
        assertFalse("At least one API is missing", apis.isEmpty());

        assertEquals("ElementId should be 2131034123", "2131034123", callback.elementId);

        String[] apisToCompare = {sendAPISignature, internetAPISignature};

        Set<String> elementAPIs = AppController.getInstance().getAPIsOfElement(2131034123);
        Arrays.asList(apisToCompare).forEach(x->assertTrue("API is not in the list", elementAPIs.contains(x)));
    }

    @Test
    public void construtorTest(){
        Optional<UiElement> callbackCandidate =  results.keySet().stream().filter(x->x.signature.equals("<com.example.APIChallenges.MyActivity$10: void onClick(android.view.View)>")).findFirst();
        assertTrue("Callback is absent", callbackCandidate.isPresent());

        UiElement callback = callbackCandidate.get();
        List<ApiInfoForForward> apis = results.get(callback);
        assertNotNull("At least one API is missing", apis);
        assertFalse("At least one API is missing", apis.isEmpty());

        assertEquals("ElementId should be 2131034124", "2131034124", callback.elementId);

        String[] apisToCompare = {sendAPISignature};

        Set<String> elementAPIs = AppController.getInstance().getAPIsOfElement(2131034124);
        Arrays.asList(apisToCompare).forEach(x->assertTrue("API is not in the list", elementAPIs.contains(x)));
    }

    @Test
    @Ignore
    public void startBroadcastTest(){
        Optional<UiElement> callbackCandidate =  results.keySet().stream().filter(x->x.signature.equals("<com.example.APIChallenges.MyActivity$12: void onClick(android.view.View)>")).findFirst();
        assertTrue("Callback is absent", callbackCandidate.isPresent());

        UiElement callback = callbackCandidate.get();
        List<ApiInfoForForward> apis = results.get(callback);
        assertNotNull("At least one API is missing", apis);
        assertFalse("At least one API is missing", apis.isEmpty());

        assertEquals("ElementId should be 2131034125", "2131034125", callback.elementId);

        String[] apisToCompare = {sendAPISignature};

        Set<String> elementAPIs = AppController.getInstance().getAPIsOfElement(2131034125);
        Arrays.asList(apisToCompare).forEach(x->assertTrue("API is not in the list", elementAPIs.contains(x)));
    }

    @Test
    @Ignore
    public void dynBroadCastTest(){
        Optional<UiElement> callbackCandidate =  results.keySet().stream().filter(x->x.signature.equals("<com.example.APIChallenges.MyActivity$11: void onClick(android.view.View)>")).findFirst();
        assertTrue("Callback is absent", callbackCandidate.isPresent());

        UiElement callback = callbackCandidate.get();
        List<ApiInfoForForward> apis = results.get(callback);
        assertNotNull("At least one API is missing", apis);
        assertFalse("At least one API is missing", apis.isEmpty());

        assertEquals("ElementId should be 2131034126", "2131034126", callback.elementId);

        String[] apisToCompare = {sendAPISignature};

        Set<String> elementAPIs = AppController.getInstance().getAPIsOfElement(2131034126);
        Arrays.asList(apisToCompare).forEach(x->assertTrue("API is not in the list", elementAPIs.contains(x)));
    }

    @Test
    @Ignore
    public void broadCastTriggersActivityTest(){
        Optional<UiElement> callbackCandidate =  results.keySet().stream().filter(x->x.signature.equals("<com.example.APIChallenges.MyActivity$13: void onClick(android.view.View)>")).findFirst();
        assertTrue("Callback is absent", callbackCandidate.isPresent());

        UiElement callback = callbackCandidate.get();
        List<ApiInfoForForward> apis = results.get(callback);
        assertNotNull("At least one API is missing", apis);
        assertFalse("At least one API is missing", apis.isEmpty());

        assertEquals("ElementId should be 2131034127", "2131034127", callback.elementId);

        String[] apisToCompare = {sendAPISignature};

        Set<String> elementAPIs = AppController.getInstance().getAPIsOfElement(2131034127);
        Arrays.asList(apisToCompare).forEach(x->assertTrue("API is not in the list", elementAPIs.contains(x)));
    }

    @Test
    public void serviceTest(){
        Optional<UiElement> callbackCandidate =  results.keySet().stream().filter(x->x.signature.equals("<com.example.APIChallenges.MyActivity$14: void onClick(android.view.View)>")).findFirst();
        assertTrue("Callback is absent", callbackCandidate.isPresent());

        UiElement callback = callbackCandidate.get();
        List<ApiInfoForForward> apis = results.get(callback);
        assertNotNull("At least one API is missing", apis);
        assertFalse("At least one API is missing", apis.isEmpty());

        assertEquals("ElementId should be 2131034128", "2131034128", callback.elementId);

        String[] apisToCompare = {sendAPISignature};

        Set<String> elementAPIs = AppController.getInstance().getAPIsOfElement(2131034128);
        Arrays.asList(apisToCompare).forEach(x->assertTrue("API is not in the list", elementAPIs.contains(x)));
    }
}
