package st.cs.uni.saarland.de.testApps;

import android.content.DialogInterface;
import com.thoughtworks.xstream.XStream;
import org.junit.BeforeClass;
import org.junit.Test;
import st.cs.uni.saarland.de.reachabilityAnalysis.ApiInfoForForward;
import st.cs.uni.saarland.de.reachabilityAnalysis.IntentInfo;
import st.cs.uni.saarland.de.reachabilityAnalysis.UiElement;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

/**
 * Created by avdiienko on 01/12/15.
 */
public class ButtonXMLChallengesTest {

    private static Map<UiElement, List<ApiInfoForForward>> results;
    private static Map<String, Set<Integer>> uiResults;

    @BeforeClass
    public static void setUp() throws Exception {
        String [] params = {"-apk",
                "testApps/app-debug.apk",
                "-androidJar",
                "libs/android.jar",
                "-apkToolOutput",
                "testApps/app-debug",
                "-rAnalysis",
                "-test",
                "-cgAlgo",
                "RTA",
                "-rLifecycle",
                "-images",
                "-processMenus"};

        File oldResults = new File("results"+File.separator+"app-debug.apk_forward_apiResults_1.xml");
        if(oldResults.exists()) {
            if (!oldResults.delete()) {
                throw new Exception("Can not delete file with results");
            }
        }

        File resultFile = new File("testApps" + File.separator + "app-debug" + File.separator + "outputForTestLayout.xml");
        if(resultFile.exists()) {
            if (!resultFile.delete()) {
                throw new Exception("Can not delete file with results");
            }
        }


        TestApp.main(params);

        XStream xStream = new XStream();
        xStream.processAnnotations(UiElement.class);
        xStream.processAnnotations(ApiInfoForForward.class);
        xStream.processAnnotations(IntentInfo.class);
        xStream.setMode(XStream.NO_REFERENCES);
        results = (Map<UiElement, List<ApiInfoForForward>>) xStream.fromXML(new File("results" + File.separator + "app-debug.apk_forward_apiResults_1.xml"));
        assertNotNull("Results are null", results);


        XStream xStreamUI = new XStream();
        xStreamUI.setMode(XStream.NO_REFERENCES);
        uiResults = (HashMap<String, Set<Integer>>) xStreamUI.fromXML(new File("testApps" + File.separator + "app-debug" + File.separator + "outputForTestLayout.xml"));
        assertNotEquals("Results are empty", 0, uiResults.size());
    }

    @Test
    public void checkAbstractGetLayoutId(){
        final String MEMBER_CLASS="com.example.avdiienko.button_xml.MemberClass";
        final String ANONYMOUS_CLASS="com.example.avdiienko.button_xml.AnonymousOnClickHandler";
        assertTrue(uiResults.containsKey(MEMBER_CLASS));
        assertTrue(uiResults.containsKey(ANONYMOUS_CLASS));
        Set<Integer> memberClassIds = uiResults.get(MEMBER_CLASS);
        assertEquals("Should be menu and one listener", 2, memberClassIds.size());
        final int memberClassMenu=2131558406;
        assertTrue("Menu for Member Class is missing", memberClassIds.contains(memberClassMenu));
        final int memberClassLayout=2130968608;
        assertTrue("Layout for Member Class is missing", memberClassIds.contains(memberClassLayout));

        Set<Integer> anonymousClassIds = uiResults.get(ANONYMOUS_CLASS);
        assertEquals("Should be menu and one listener", 2, anonymousClassIds.size());
        final int anonymousClassMenu=2131558401;
        assertTrue("Menu for Anonymous Class is missing", anonymousClassIds.contains(anonymousClassMenu));
        final int anonymousClassLayout=2130968602;
        assertTrue("Layout for Anonymous Class is missing", anonymousClassIds.contains(anonymousClassLayout));
    }

    @Test
    public void testCommonChecks(){
        assertEquals("Should be exactly 44 elements", 44, results.keySet().size());
    }

    @Test
    public void testActivityClassImplementsListenerInterface() {
        Optional<UiElement> callbackCandidate =  results.keySet().stream().filter(x->x.signature.equals("<com.example.avdiienko.button_xml.ActivityClassImplementsListenerInterface: void onClick(android.view.View)>")).findFirst();
        assertTrue("ActivityClassImplementsListenerInterface callback is absent", callbackCandidate.isPresent());

        UiElement callback = callbackCandidate.get();
        List<ApiInfoForForward> apis = results.get(callback);
        assertNotNull("List of APIs for ActivityClassImplementsListenerInterface is null", apis);
        assertFalse("List of APIs for ActivityClassImplementsListenerInterface is empty", apis.isEmpty());

        assertEquals("ElementId should be 2131492944", "2131492944", callback.elementId);

        assertEquals("Text is wrong", "Test Button", callback.text.get("default_value"));
        assertEquals("Kind of element is wrong", "Button", callback.kindOfElement);

        assertFalse("List of APIs is empty", apis.isEmpty());

        String[] apisToCompare = {"<android.telephony.SmsManager: void sendTextMessage(java.lang.String,java.lang.String,java.lang.String,android.app.PendingIntent,android.app.PendingIntent)>",
                "<android.widget.TextView: java.lang.CharSequence getText()>"};

        Set<String> apisSignatures = apis.stream().map(x->x.signature).collect(Collectors.toSet());
        Arrays.asList(apisToCompare).forEach(x->assertTrue("API is not in the list", apisSignatures.contains(x)));
    }

    @Test
    public void testAnonymousOnClickHandler(){
        Optional<UiElement> callbackCandidate =  results.keySet().stream().filter(x->x.signature.equals("<com.example.avdiienko.button_xml.AnonymousOnClickHandler$1: void onClick(android.view.View)>")).findFirst();
        assertTrue("AnonymousOnClickHandler callback is absent", callbackCandidate.isPresent());

        UiElement callback = callbackCandidate.get();
        List<ApiInfoForForward> apis = results.get(callback);
        assertNotNull("List of APIs for AnonymousOnClickHandler is null", apis);
        assertFalse("List of APIs for AnonymousOnClickHandler is empty", apis.isEmpty());

        assertEquals("ElementId should be 2131492946", "2131492946", callback.elementId);

        assertEquals("Text is wrong", "Anonymous onClick", callback.text.get("default_value"));
        assertEquals("Kind of element is wrong", "Button", callback.kindOfElement);

        assertFalse("List of APIs is empty", apis.isEmpty());

        String[] apisToCompare = {"<android.telephony.SmsManager: void sendTextMessage(java.lang.String,java.lang.String,java.lang.String,android.app.PendingIntent,android.app.PendingIntent)>"};

        Set<String> apisSignatures = apis.stream().map(x->x.signature).collect(Collectors.toSet());
        Arrays.asList(apisToCompare).forEach(x->assertTrue("API is not in the list", apisSignatures.contains(x)));
    }

    @Test
    public void testButtonWithListenerInMethod(){
        Optional<UiElement> callbackCandidate =  results.keySet().stream().filter(x->x.signature.equals("<com.example.avdiienko.button_xml.ButtonWithListenerInMethod$1: void onClick(android.view.View)>")).findFirst();
        assertTrue("ButtonWithListenerInMethod callback is absent", callbackCandidate.isPresent());

        UiElement callback = callbackCandidate.get();
        List<ApiInfoForForward> apis = results.get(callback);
        assertNotNull("List of APIs for ButtonWithListenerInMethod is null", apis);
        assertFalse("List of APIs for ButtonWithListenerInMethod is empty", apis.isEmpty());

        assertEquals("ElementId should be 2131492948", "2131492948", callback.elementId);

        assertEquals("Text is wrong", "New Button", callback.text.get("default_value"));
        assertEquals("Kind of element is wrong", "Button", callback.kindOfElement);

        assertFalse("List of APIs is empty", apis.isEmpty());

        String[] apisToCompare = {"<android.telephony.TelephonyManager: java.lang.String getDeviceId()>", "<android.widget.TextView: java.lang.CharSequence getText()>"};

        Set<String> apisSignatures = apis.stream().map(x->x.signature).collect(Collectors.toSet());
        Arrays.asList(apisToCompare).forEach(x->assertTrue("API is not in the list", apisSignatures.contains(x)));
    }

    @Test
    public void testInterfaceType(){
        assertEquals("Should be exactly one element with such listener" , 1, results.keySet().stream().filter(x->x.signature.equals("<com.example.avdiienko.button_xml.InterfaceType$1: void onClick(android.view.View)>")).count());
        Optional<UiElement> callbackCandidate =  results.keySet().stream().filter(x->x.signature.equals("<com.example.avdiienko.button_xml.InterfaceType$1: void onClick(android.view.View)>")).findFirst();
        assertTrue("InterfaceType callback is absent", callbackCandidate.isPresent());

        UiElement callback = callbackCandidate.get();
        List<ApiInfoForForward> apis = results.get(callback);
        assertNotNull("List of APIs for InterfaceType is null", apis);
        assertFalse("List of APIs for InterfaceType is empty", apis.isEmpty());

        assertEquals("ElementId should be 2131492953", "2131492953", callback.elementId);

        assertEquals("Text is wrong", "New Button", callback.text.get("com.example.avdiienko.button_xml.InterfaceType"));
        assertEquals("Kind of element is wrong", "Button", callback.kindOfElement);

        assertFalse("List of APIs is empty", apis.isEmpty());

        String[] apisToCompare = {"<android.telephony.TelephonyManager: java.lang.String getDeviceId()>", "<android.widget.TextView: java.lang.CharSequence getText()>"};

        Set<String> apisSignatures = apis.stream().map(x->x.signature).collect(Collectors.toSet());
        Arrays.asList(apisToCompare).forEach(x->assertTrue("API is not in the list", apisSignatures.contains(x)));
    }

    @Test
    public void testMainStaticButton(){
        Optional<UiElement> callbackCandidate =  results.keySet().stream().filter(x->x.signature.equals("<com.example.avdiienko.button_xml.MainActivity: void staticButtonClick(android.view.View)>")).findFirst();
        assertTrue("MainStaticButton callback is absent", callbackCandidate.isPresent());

        UiElement callback = callbackCandidate.get();
        List<ApiInfoForForward> apis = results.get(callback);
        assertNotNull("List of APIs for MainStaticButton is null", apis);
        assertFalse("List of APIs for MainStaticButton is empty", apis.isEmpty());
        assertEquals("ElementId should be 2131492955", "2131492955", callback.elementId);

        assertEquals("Text is wrong", "Static Button", callback.text.get("default_value"));
        assertEquals("Kind of element is wrong", "ImageButton", callback.kindOfElement);

        assertFalse("List of APIs is empty", apis.isEmpty());

        String[] apisToCompare = {"<android.telephony.TelephonyManager: java.lang.String getDeviceId()>", "<android.util.Log: int v(java.lang.String,java.lang.String)>", "<android.util.Log: int w(java.lang.String,java.lang.String)>"};

        Set<String> apisSignatures = apis.stream().map(x->x.signature).collect(Collectors.toSet());
        Arrays.asList(apisToCompare).forEach(x->assertTrue("API is not in the list", apisSignatures.contains(x)));
    }

    @Test
    public void testMainGoAnonymous(){
        Optional<UiElement> callbackCandidate =  results.keySet().stream().filter(x->x.signature.equals("<com.example.avdiienko.button_xml.MainActivity: void goAnonymous(android.view.View)>")).findFirst();
        assertTrue("MainGoAnonymous callback is absent", callbackCandidate.isPresent());

        UiElement callback = callbackCandidate.get();
        List<ApiInfoForForward> apis = results.get(callback);
        assertNotNull("List of APIs for MainGoAnonymous is null", apis);
        assertFalse("List of APIs for MainGoAnonymous is empty", apis.isEmpty());

        assertEquals("ElementId should be 2131492956", "2131492956", callback.elementId);

        assertEquals("Text is wrong", "goAnaonymousActivity", callback.text.get("default_value"));
        assertEquals("Kind of element is wrong", "Button", callback.kindOfElement);

        assertFalse("List of APIs is empty", apis.isEmpty());

        String[] apisToCompare = {"<android.util.Log: int v(java.lang.String,java.lang.String)>"};

        Set<String> apisSignatures = apis.stream().map(x->x.signature).collect(Collectors.toSet());
        Arrays.asList(apisToCompare).forEach(x->assertTrue("API is not in the list", apisSignatures.contains(x)));
    }

    @Test
    public void testMemberClass(){
        Optional<UiElement> callbackCandidate =  results.keySet().stream().filter(x->x.signature.equals("<com.example.avdiienko.button_xml.MemberClass$HandleClick: void onClick(android.view.View)>")).findFirst();
        assertTrue("MemberClass callback is absent", callbackCandidate.isPresent());

        UiElement callback = callbackCandidate.get();
        List<ApiInfoForForward> apis = results.get(callback);
        assertNotNull("List of APIs for MemberClass is null", apis);
        assertFalse("List of APIs for MemberClass is empty", apis.isEmpty());

        assertEquals("ElementId should be 2131492959", "2131492959", callback.elementId);

        assertEquals("Text is wrong", "Member Class Button", callback.text.get("default_value"));
        assertEquals("Kind of element is wrong", "Button", callback.kindOfElement);

        assertFalse("List of APIs is empty", apis.isEmpty());

        String[] apisToCompare = {"<android.telephony.TelephonyManager: java.lang.String getDeviceId()>", "<android.widget.TextView: java.lang.CharSequence getText()>"};

        Set<String> apisSignatures = apis.stream().map(x->x.signature).collect(Collectors.toSet());
        Arrays.asList(apisToCompare).forEach(x->assertTrue("API is not in the list", apisSignatures.contains(x)));
    }

    // TODO check with Vitalii, Dialogs
    @Test
    public void testOneListenerMultipleButtonsDialogPositive(){
        Optional<UiElement> callbackCandidate =  results.keySet().stream().filter(x->x.signature.equals("<com.example.avdiienko.button_xml.OneListenerMultipleButtons$1: void onClick(android.content.DialogInterface,int)>")
                && x.elementId.equals(Integer.toString(DialogInterface.BUTTON_POSITIVE))).findFirst();
        assertTrue("OneListenerMultipleButtonsDialogPositive callback is absent", callbackCandidate.isPresent());

        UiElement callback = callbackCandidate.get();
        List<ApiInfoForForward> apis = results.get(callback);
        assertNotNull("List of APIs for OneListenerMultipleButtonsDialogPositive is null", apis);
        assertFalse("List of APIs for OneListenerMultipleButtonsDialogPositive is empty", apis.isEmpty());

        assertFalse("List of APIs is empty", apis.isEmpty());

        String[] apisToCompare = {"<android.telephony.TelephonyManager: java.lang.String getDeviceId()>"};

        Set<String> apisSignatures = apis.stream().map(x->x.signature).collect(Collectors.toSet());
        Arrays.asList(apisToCompare).forEach(x->assertTrue("API is not in the list", apisSignatures.contains(x)));
    }
    // TODO check with Vitalii, Dialogs
    @Test
    public void testOneListenerMultipleButtonsDialogNegative(){
        Optional<UiElement> callbackCandidate =  results.keySet().stream().filter(x->x.signature.equals("<com.example.avdiienko.button_xml.OneListenerMultipleButtons$1: void onClick(android.content.DialogInterface,int)>")
                && x.elementId.equals(Integer.toString(DialogInterface.BUTTON_NEGATIVE))).findFirst();
        assertTrue("OneListenerMultipleButtonsDialogPositive callback is absent", callbackCandidate.isPresent());

        UiElement callback = callbackCandidate.get();
        List<ApiInfoForForward> apis = results.get(callback);
        assertNotNull("List of APIs for OneListenerMultipleButtonsDialogPositive is null", apis);
        assertFalse("List of APIs for OneListenerMultipleButtonsDialogPositive is empty", apis.isEmpty());

        assertFalse("List of APIs is empty", apis.isEmpty());

        String[] apisToCompare = {"<android.util.Log: int i(java.lang.String,java.lang.String)>"};

        Set<String> apisSignatures = apis.stream().map(x->x.signature).collect(Collectors.toSet());
        Arrays.asList(apisToCompare).forEach(x->assertTrue("API is not in the list", apisSignatures.contains(x)));
    }
    // TODO check with Vitalii, Dialogs
    @Test
    public void testOneListenerMultipleButtonsDialogNeutral(){
        Optional<UiElement> callbackCandidate =  results.keySet().stream().filter(x->x.signature.equals("<com.example.avdiienko.button_xml.OneListenerMultipleButtons$1: void onClick(android.content.DialogInterface,int)>")
                && x.elementId.equals(Integer.toString(DialogInterface.BUTTON_NEUTRAL))).findFirst();
        assertTrue("OneListenerMultipleButtonsDialogPositive callback is absent", callbackCandidate.isPresent());

        UiElement callback = callbackCandidate.get();
        List<ApiInfoForForward> apis = results.get(callback);
        assertNotNull("List of APIs for OneListenerMultipleButtonsDialogPositive is null", apis);
        assertFalse("List of APIs for OneListenerMultipleButtonsDialogPositive is empty", apis.isEmpty());

        assertFalse("List of APIs is empty", apis.isEmpty());

        String[] apisToCompare = {"<android.util.Log: int d(java.lang.String,java.lang.String)>"};

        Set<String> apisSignatures = apis.stream().map(x->x.signature).collect(Collectors.toSet());
        Arrays.asList(apisToCompare).forEach(x->assertTrue("API is not in the list", apisSignatures.contains(x)));
    }

    @Test
    public void testOneListenerMultipleButtonsButtonOk(){
        Optional<UiElement> callbackCandidate =  results.keySet().stream().filter(x->x.signature.equals("<com.example.avdiienko.button_xml.OneListenerMultipleButtons$2: void onClick(android.view.View)>")
                && x.elementId.equals("2131492961")).findFirst();
        assertTrue("OneListenerMultipleButtonsButtonOk callback is absent", callbackCandidate.isPresent());

        UiElement callback = callbackCandidate.get();
        List<ApiInfoForForward> apis = results.get(callback);
        assertNotNull("List of APIs for OneListenerMultipleButtonsDialogPositive is null", apis);
        assertFalse("List of APIs for OneListenerMultipleButtonsDialogPositive is empty", apis.isEmpty());

        assertFalse("List of APIs is empty", apis.isEmpty());

        String[] apisToCompare = {"<android.telephony.TelephonyManager: java.lang.String getDeviceId()>"};

        Set<String> apisSignatures = apis.stream().map(x->x.signature).collect(Collectors.toSet());
        Arrays.asList(apisToCompare).forEach(x->assertTrue("API is not in the list", apisSignatures.contains(x)));
        assertEquals("Text is wrong", "Ok", callback.text.get("default_value"));
        assertEquals("Kind of element is wrong", "Button", callback.kindOfElement);
    }

    @Test
    public void testOneListenerMultipleButtonsButtonCancel(){
        Optional<UiElement> callbackCandidate =  results.keySet().stream().filter(x->x.signature.equals("<com.example.avdiienko.button_xml.OneListenerMultipleButtons$2: void onClick(android.view.View)>")
                && x.elementId.equals("2131492962")).findFirst();
        assertTrue("OneListenerMultipleButtonsButtonOk callback is absent", callbackCandidate.isPresent());

        UiElement callback = callbackCandidate.get();
        List<ApiInfoForForward> apis = results.get(callback);
        assertNotNull("List of APIs for OneListenerMultipleButtonsDialogPositive is null", apis);
        assertFalse("List of APIs for OneListenerMultipleButtonsDialogPositive is empty", apis.isEmpty());

        assertFalse("List of APIs is empty", apis.isEmpty());

        String[] apisToCompare = {"<android.util.Log: int i(java.lang.String,java.lang.String)>", "<android.util.Log: int v(java.lang.String,java.lang.String)>"};

        Set<String> apisSignatures = apis.stream().map(x->x.signature).collect(Collectors.toSet());
        Arrays.asList(apisToCompare).forEach(x->assertTrue("API is not in the list", apisSignatures.contains(x)));
        assertEquals("Text is wrong", "Cancel", callback.text.get("default_value"));
        assertEquals("Kind of element is wrong", "Button", callback.kindOfElement);
    }

    @Test
    public void testOneListenerMultipleButtonsButtonDoNothing(){
        Optional<UiElement> callbackCandidate =  results.keySet().stream().filter(x->x.signature.equals("<com.example.avdiienko.button_xml.OneListenerMultipleButtons$2: void onClick(android.view.View)>")
                && x.elementId.equals("2131492963")).findFirst();
        assertTrue("OneListenerMultipleButtonsButtonOk callback is absent", callbackCandidate.isPresent());

        UiElement callback = callbackCandidate.get();
        List<ApiInfoForForward> apis = results.get(callback);
        assertNotNull("List of APIs for OneListenerMultipleButtonsDialogPositive is null", apis);
        assertFalse("List of APIs for OneListenerMultipleButtonsDialogPositive is empty", apis.isEmpty());

        assertFalse("List of APIs is empty", apis.isEmpty());

        String[] apisToCompare = {"<android.util.Log: int d(java.lang.String,java.lang.String)>"};

        Set<String> apisSignatures = apis.stream().map(x->x.signature).collect(Collectors.toSet());
        Arrays.asList(apisToCompare).forEach(x->assertTrue("API is not in the list", apisSignatures.contains(x)));
        assertEquals("Text is wrong", "Do nothing", callback.text.get("default_value"));
        assertEquals("Kind of element is wrong", "Button", callback.kindOfElement);
    }

    @Test
    public void testIdInArrayIntraProceduralMeEither(){
        Optional<UiElement> callbackCandidate =  results.keySet().stream().filter(x->x.signature.equals("<com.example.avdiienko.button_xml.IdInArray$1: void onClick(android.view.View)>")
                && x.elementId.equals("2131492951")).findFirst();
        assertTrue("IdInArrayIntraProceduralMeEither callback is absent", callbackCandidate.isPresent());

        UiElement callback = callbackCandidate.get();
        List<ApiInfoForForward> apis = results.get(callback);
        assertNotNull("List of APIs for IdInArrayIntraProceduralMeEither is null", apis);
        assertFalse("List of APIs for IdInArrayIntraProceduralMeEither is empty", apis.isEmpty());

        assertFalse("List of APIs is empty", apis.isEmpty());

        String[] apisToCompare = {"<android.util.Log: int v(java.lang.String,java.lang.String)>"};

        Set<String> apisSignatures = apis.stream().map(x->x.signature).collect(Collectors.toSet());
        Arrays.asList(apisToCompare).forEach(x->assertTrue("API is not in the list", apisSignatures.contains(x)));
        assertEquals("Text is wrong", "Me either", callback.text.get("default_value"));
        assertEquals("Kind of element is wrong", "Button", callback.kindOfElement);
    }

    @Test
    public void testIdInArrayInterProceduralMeEither(){
        Optional<UiElement> callbackCandidate =  results.keySet().stream().filter(x->x.signature.equals("<com.example.avdiienko.button_xml.IdInArray$2: void onClick(android.view.View)>")
                && x.elementId.equals("2131492951")).findFirst();
        assertTrue("IdInArrayInterProceduralMeEither callback is absent", callbackCandidate.isPresent());

        UiElement callback = callbackCandidate.get();
        List<ApiInfoForForward> apis = results.get(callback);
        assertNotNull("List of APIs for IdInArrayInterProceduralMeEither is null", apis);
        assertFalse("List of APIs for IdInArrayInterProceduralMeEither is empty", apis.isEmpty());

        assertFalse("List of APIs is empty", apis.isEmpty());

        String[] apisToCompare = {"<android.util.Log: int d(java.lang.String,java.lang.String)>"};

        Set<String> apisSignatures = apis.stream().map(x->x.signature).collect(Collectors.toSet());
        Arrays.asList(apisToCompare).forEach(x->assertTrue("API is not in the list", apisSignatures.contains(x)));
        assertEquals("Text is wrong", "Me either", callback.text.get("default_value"));
        assertEquals("Kind of element is wrong", "Button", callback.kindOfElement);
    }

    @Test
    public void testIdInArrayInterProceduralFindMyHandler(){
        Optional<UiElement> callbackCandidate =  results.keySet().stream().filter(x->x.signature.equals("<com.example.avdiienko.button_xml.IdInArray$2: void onClick(android.view.View)>")
                && x.elementId.equals("2131492950")).findFirst();
        assertTrue("IdInArrayInterProceduralFindMyHandler callback is absent", callbackCandidate.isPresent());

        UiElement callback = callbackCandidate.get();
        List<ApiInfoForForward> apis = results.get(callback);
        assertNotNull("List of APIs for IdInArrayInterProceduralFindMyHandler is null", apis);
        assertFalse("List of APIs for IdInArrayInterProceduralFindMyHandler is empty", apis.isEmpty());

        assertFalse("List of APIs is empty", apis.isEmpty());

        String[] apisToCompare = {"<android.util.Log: int d(java.lang.String,java.lang.String)>"};

        Set<String> apisSignatures = apis.stream().map(x->x.signature).collect(Collectors.toSet());
        Arrays.asList(apisToCompare).forEach(x->assertTrue("API is not in the list", apisSignatures.contains(x)));
        assertEquals("Text is wrong", "Hey! Find my handler :)", callback.text.get("default_value"));
        assertEquals("Kind of element is wrong", "Button", callback.kindOfElement);
    }

    @Test
    public void testLifecycleMethodsFromSuperClasses(){
        List<UiElement> baseActivityLifecycle =  results.keySet().stream().filter(x->x.elementId.equals("com.example.avdiienko.button_xml.BaseActivity")).collect(Collectors.toList());
        assertEquals(3, baseActivityLifecycle.size());
        String[] callbacksToCompare = {"<com.example.avdiienko.button_xml.ChildActivity: void onResume()>", "<com.example.avdiienko.button_xml.ChildChildActivity: void onDestroy()>",
        "<com.example.avdiienko.button_xml.BaseActivity: void onCreate(android.os.Bundle)>"};
        baseActivityLifecycle.forEach(x->assertTrue("Callbacks is not in the list", Arrays.asList(callbacksToCompare).contains(x.signature)));
        Optional<UiElement> onResumeOptional = baseActivityLifecycle.stream().filter(x->x.signature.equals("<com.example.avdiienko.button_xml.ChildActivity: void onResume()>")).findAny();
        assertTrue(onResumeOptional.isPresent());
        UiElement onResume = onResumeOptional.get();
        List<ApiInfoForForward> apis = results.get(onResume);
        assertEquals("GetDeviceId should be here", 1, apis.stream().filter(x->x.signature.equals("<android.telephony.TelephonyManager: java.lang.String getDeviceId()>")).count());
    }

    @Test
    public void testIntercomponentAnalysisForExplicitIntents(){
        List<UiElement> goToTheChildButton = results.keySet().stream().filter(x->x.elementId.equals("2131492957")).collect(Collectors.toList());
        assertEquals(1, goToTheChildButton.size());
        UiElement button = goToTheChildButton.get(0);
        assertEquals("Signature of the element is not correct" ,"<com.example.avdiienko.button_xml.MainActivity: void goToTheChild(android.view.View)>", button.signature);
        assertEquals("Should be Log.e()", 1, results.get(button).stream().filter(x->x.signature.equals("<android.util.Log: int e(java.lang.String,java.lang.String)>")).count());
        assertTrue("No other logs should be there", results.get(button).stream().filter(x->x.signature.startsWith("<android.util.Log")).count() == 1);
        assertEquals("ICC level should be 1", 1, results.get(button).get(0).depthComponentLevel);
    }
}