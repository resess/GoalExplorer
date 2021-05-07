package st.cs.uni.saarland.de.testApps;

import com.thoughtworks.xstream.XStream;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import st.cs.uni.saarland.de.reachabilityAnalysis.ApiInfoForForward;
import st.cs.uni.saarland.de.reachabilityAnalysis.UiElement;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.*;

/**
 * Created by Isa on 19.12.2015.
 */
public class ListenerChallengeTests {

    private static Map<String, List<UiElement>> results = new HashMap<String, List<UiElement>>(); // <counter, List<UiELement>>

    @BeforeClass
    public static void setUp() throws Exception {
        String [] params = {"-apk",
                "testApps/ListenerChallenges.apk",
                "-androidJar",
                "libs/android.jar",
                "-apkToolOutput",
                "testApps/ListenerChallenges",
                "-test",
                "-noLang",
                "-processMenus"
        };

        File resultFile = new File("testApps" + File.separator + "ListenerChallenges" + File.separator + "outputForTests.xml");
        if(resultFile.exists()) {
            if (!resultFile.delete()) {
                throw new Exception("Can not delete file with results");
            }
        }


        TestApp.main(params);

        XStream xStream = new XStream();
        xStream.processAnnotations(UiElement.class);
        xStream.processAnnotations(ApiInfoForForward.class);
        xStream.setMode(XStream.NO_REFERENCES);
        List<UiElement> uiElements = new ArrayList<UiElement>();
        uiElements = (List<UiElement>) xStream.fromXML(new File("testApps" + File.separator + "ListenerChallenges" + File.separator + "outputForTests.xml"));
        assertNotEquals("Results are null/0", 0, uiElements.size());

        // create map of ids to UiElements
        for (UiElement uiE : uiElements){
            List<UiElement> uiEList = results.get(uiE.elementId);
            // check if ID is already inserted
            if (uiEList != null){
                // insert the new uiE to the ID key
                uiEList.add(uiE);
            }else{
                results.put(uiE.elementId, new ArrayList(){{add(uiE);}});
            }

        }
    }

    @Test
    public void simpleListenerTest(){
        // onCreateButton
        List<UiElement> uiEList = results.get("2131099654");
        assertNotNull("You missed a listener", uiEList);
        assertEquals("You overapproximate!", 1, uiEList.size());
        assertEquals("simple dynamic assignment of a listener in onCreate (buttonID:2131034118)", "<com.example.UIEventHandlersChallenge.MyActivity$2: void onClick(android.view.View)>", uiEList.get(0).signature);
    }

    @Test
    public void simpleXMLListenerDeclarationTest(){
        List<UiElement> uiEList =  results.get("2131099655");
        assertNotNull("You missed a listener", uiEList);
        assertEquals("You overapproximate!", 1, uiEList.size());
        assertEquals("assignment of a listener in the XML layout file (buttonID:2131099655)", "<com.example.UIEventHandlersChallenge.MyActivity: void xmlOnClick1(android.view.View)>", uiEList.get(0).signature);
    }

    @Test
    public void externalListenerTest1(){
        // external List Button1
        List<UiElement> uiEList = results.get("2131099653");
        assertNotNull("You missed a listener", uiEList);
        assertEquals("You overapproximate!", 1, uiEList.size());
        assertEquals("external listener class implementation was not processed (buttonID:2131099653)", "<com.example.UIEventHandlersChallenge.MyListener: void onClick(android.view.View)>", uiEList.get(0).signature);
    }

    @Test
    public void externalListenerTest2(){
        // externalListButton2
        List<UiElement> uiEList = results.get("2131099648");
        assertNotNull("You missed a listener", uiEList);
        assertEquals("You overapproximate!", 1, uiEList.size());
        assertEquals("external listener class implementation was not processed (buttonID:2131099648)", "<com.example.UIEventHandlersChallenge.MyListener: void onClick(android.view.View)>", uiEList.get(0).signature);
    }

    @Test
    public void externalListenerTest3() {
        // multipleListenerButton
        List<UiElement> uiEList = results.get("2131099665");
        assertNotNull("You missed a listener", uiEList);
        assertEquals("You overapproximate!", 1, uiEList.size());
        assertEquals("Assigned listener in a for loop via arrays", "<com.example.UIEventHandlersChallenge.CompositeOnClickListener: void onClick(android.view.View)>",uiEList.get(0).signature);
    }

    @Test
    public void innerListenerClassTest1(){
        // sameListenerButton1
        List<UiElement> uiEList = results.get("2131099649");
        assertNotNull("You missed a listener", uiEList);
        assertEquals("You overapproximate!", 1, uiEList.size());
        assertEquals("inner listener class implementation was not processed (buttonID:2131099649)", "<com.example.UIEventHandlersChallenge.MyActivity$MyInnerListener: void onClick(android.view.View)>",uiEList.get(0).signature);
    }

    @Test
    public void innerListenerClassTest2(){
        // sameListenerButton2
        List<UiElement> uiEList =  results.get("2131099650");
        assertNotNull("You missed a listener", uiEList);
        assertEquals("You overapproximate!", 1, uiEList.size());
        assertEquals("Couldn't find correct listener for 2131034114, external listener", "<com.example.UIEventHandlersChallenge.MyActivity$MyInnerListener: void onClick(android.view.View)>",uiEList.get(0).signature);
    }

    @Test
    public void sameListenerTest(){
        UiElement uiE1 = results.get("2131099649").get(0);
        UiElement uiE2 = results.get("2131099650").get(0);
        assertEquals("This should be the same listener method", uiE1.signature, uiE2.signature);
    }

    @Test
    public void listenerVariableTest(){
        // listVarButton
        List<UiElement> uiEList = results.get("2131099651");
        assertNotNull("You missed a listener", uiEList);
        assertEquals("You overapproximate!", 1, uiEList.size());
        assertEquals("Test for a listener class variable (buttonID:2131099651)", "<com.example.UIEventHandlersChallenge.MyActivity$4: void onClick(android.view.View)>",uiEList.get(0).signature);
    }

    @Test
    public void declarationSplittedInMultipleMethodTest(){
        List<UiElement> uiEList = results.get("2131099652");
        assertTrue("splitted declaration in multiple methods(buttonID:2131099652)", uiEList.stream().anyMatch(x->"<com.example.UIEventHandlersChallenge.MyActivity$3: void onClick(android.view.View)>".equals(x.signature)));
    }

    @Test
    @Ignore
    public void declarationSplittedInMultipleMethodOverapproximationTest(){
        List<UiElement> uiEList = results.get("2131099652");
        assertEquals("You overapproximate! (buttonID:2131099652)", 1, uiEList.size());
    }

    @Test
    public void textViewAnonymousListenerTest(){
        // anonyListTextView
        List<UiElement> uiEList = results.get("2131099656");
        assertNotNull("You missed a listener", uiEList);
        assertEquals("You overapproximate!", 1, uiEList.size());
        assertEquals("anonymous listener implementation for a TextView(TextViewID:2131099656)", "<com.example.UIEventHandlersChallenge.MyActivity$1: void onClick(android.view.View)>",uiEList.get(0).signature);
    }

    @Test
    public void onTouchListenerTest(){
        // myTouchPlusClickButton
        List<UiElement> uiEList =  results.get("2131099658");
        assertTrue("onTouch listener(buttonID:2131099658)", uiEList.stream().anyMatch(x->"<com.example.UIEventHandlersChallenge.MyOnTouchListener: boolean onTouch(android.view.View,android.view.MotionEvent)>".equals(x.signature)));
    }

    @Test
    public void onTouchClickListenerTest2(){
        // myTouchPlusClickButton
        List<UiElement> uiEList =  results.get("2131099658");
        assertTrue("onTouch listener(buttonID:2131099658)", uiEList.stream().anyMatch(x->"<com.example.UIEventHandlersChallenge.MyActivity$5: void onClick(android.view.View)>".equals(x.signature)));
    }

    @Test
    public void onTouchClickListenerNumberTest2() {
        List<UiElement> uiEList = results.get("2131099658");
        assertNotNull("You missed at least one listener", uiEList);
        assertEquals("The number of listeners is not correct", 2, uiEList.size());
    }

    @Test
    @Ignore
    public void whileLoopWithListTest1() {
        // whileLoopButton1
        List<UiElement> uiEList = results.get("2131099657");
        assertNotNull("You missed a listener", uiEList);
        assertEquals("You overapproximate!", 1, uiEList.size());
        assertEquals("Assigned listener in a while loop via lists", "<com.example.UIEventHandlersChallenge.MySecondListener: void onClick(android.view.View)>",uiEList.get(0).signature);
    }

    @Test
    @Ignore
    public void whileLoopWithListTest2() {
        // whileLoopButton2
        List<UiElement> uiEList = results.get("2131099659");
        assertNotNull("You missed a listener", uiEList);
        assertEquals("You overapproximate!", 1, uiEList.size());
        assertEquals("Assigned listener in a while loop via lists", "<com.example.UIEventHandlersChallenge.MyThirdListener: void onClick(android.view.View)>",uiEList.get(0).signature);
    }

    @Test
    public void longClickListenerTest() {
        // actImplListLongClickButton
        List<UiElement> uiEList = results.get("2131099660");
        assertNotNull("You missed a listener", uiEList);
        assertEquals("You overapproximate!", 1, uiEList.size());
        assertEquals("used longClick listener", "<com.example.UIEventHandlersChallenge.MyActivity: boolean onLongClick(android.view.View)>",uiEList.get(0).signature);
    }

    @Test
    @Ignore
    public void arrayAndListsCombinedTest1() {
        // arrayListButton1
        List<UiElement> uiEList = results.get("2131099661");
        assertNotNull("You missed a listener", uiEList);
        assertEquals("You overapproximate!", 1, uiEList.size());
        assertEquals("Assigned listener via lists and arrays", "<com.example.UIEventHandlersChallenge.MySecondListener: void onClick(android.view.View)>",uiEList.get(0).signature);
    }

    @Test
    @Ignore
    public void arrayAndListsCombinedTest2() {
        // arrayListButton2
        List<UiElement> uiEList = results.get("2131099663");
        assertNotNull("You missed a listener", uiEList);
        assertEquals("You overapproximate!", 1, uiEList.size());
        assertEquals("Assigned listener via lists and arrays", "<com.example.UIEventHandlersChallenge.MyThirdListener: void onClick(android.view.View)>",uiEList.get(0).signature);
    }

    @Test
    @Ignore
    public void forLoopWithArraysTest1() {
        // forLoopButton1
        List<UiElement> uiEList = results.get("2131099662");
        assertNotNull("You missed a listener", uiEList);
        assertEquals("You overapproximate!", 1, uiEList.size());
        assertEquals("Assigned listener in a for loop via arrays", "<com.example.UIEventHandlersChallenge.MySecondListener: void onClick(android.view.View)>",uiEList.get(0).signature);
    }

    @Test
    @Ignore
    public void forLoopWithArraysTest2() {
        // forLoopButton2
        List<UiElement> uiEList = results.get("2131099664");
        assertNotNull("You missed a listener", uiEList);
        assertEquals("You overapproximate!", 1, uiEList.size());
        assertEquals("Assigned listener in a for loop via arrays", "<com.example.UIEventHandlersChallenge.MyThirdListener: void onClick(android.view.View)>",uiEList.get(0).signature);
    }

    @Test
    public void switchCaseTest1() {
        // switchButton2
        List<UiElement> uiEList = results.get("2131099668");
        assertTrue("listener attached in a switch case", uiEList.stream().anyMatch(x->"<com.example.UIEventHandlersChallenge.MySecondListener: void onClick(android.view.View)>".equals(x.signature)));
    }

    @Test
    public void switchCaseTest2() {
        // switchButton1
        List<UiElement> uiEList = results.get("2131099666");
        assertNotNull("You missed at least one listener", uiEList);
        assertTrue("listener attached in a switch case", uiEList.stream().anyMatch(x->"<com.example.UIEventHandlersChallenge.MyThirdListener: void onClick(android.view.View)>".equals(x.signature)));
    }

    @Test
    @Ignore
    public void switchCaseOverapproximationTest1() {
        List<UiElement> uiEList = results.get("2131099668");
        assertNotNull("You missed a listener", uiEList);
        assertEquals("You overapproximate in a switch case!", 1, uiEList.size());
    }

    @Test
    @Ignore
    public void switchCaseOverapproximationTest2() {
        List<UiElement> uiEList = results.get("2131099666");
        assertNotNull("You missed at least one listener", uiEList);
        assertEquals("You overapproximate in a switch case!", 1, uiEList.size());
    }

    @Test
    public void interproceduralCallsTest() {
        // setterGetterButton
        List<UiElement> uiEList = results.get("2131099667");
        assertNotNull("You missed at least one listener", uiEList);
        assertTrue("interprocedural call to get the listener and button", uiEList.stream().anyMatch(x->"<com.example.UIEventHandlersChallenge.MyActivity$5: void onClick(android.view.View)>".equals(x.signature)));
    }

    @Test
    @Ignore
    public void interproceduralCallsOverapproximationTest() {
        List<UiElement> uiEList = results.get("2131099667");
        assertNotNull("You missed at least one listener", uiEList);
        assertEquals("You overapproximate in interprocedural calls!", 1, uiEList.size());
    }

    @Test
    public void xmlListenerWithReturnTypeTest() {
        List<UiElement> uiEList = results.get("2131099669");
        assertNotNull("You missed a listener", uiEList);
        assertEquals("You overapproximate!", 1, uiEList.size());
        assertEquals("xml declared listener with a non-void return type", "<com.example.UIEventHandlersChallenge.MyActivity: boolean xmlOnClick2(android.view.View)>",uiEList.get(0).signature);
    }

    @Test
    @Ignore
    public void onMenuItemClickTest() {
        List<UiElement> uiEList = results.get("2131099672");
        assertNotNull("You missed a listener", uiEList);
        assertEquals("You overapproximate!", 1, uiEList.size());
        assertEquals("onMenuItemClick listener are not processed", "<com.example.UIEventHandlersChallenge.MyActivity$6: boolean onMenuItemClick(android.view.MenuItem)>",uiEList.get(0).signature);
    }
}







































