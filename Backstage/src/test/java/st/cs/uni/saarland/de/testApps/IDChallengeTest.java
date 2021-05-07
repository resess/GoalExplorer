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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by Isa on 20.12.2015.
 */
public class IDChallengeTest {

    private static Map<String, List<UiElement>> results = new HashMap<String, List<UiElement>>(); // <counter, List<UiELement>>

    @BeforeClass
    public static void setUp() throws Exception {
        String [] params = {"-apk",
                "testApps/IDChallenges.apk",
                "-androidJar",
                "libs/android.jar",
                "-apkToolOutput",
                "testApps/IDChallenges",
                "-test",
                "-processMenus"
        };

        File resultFile = new File("testApps" + File.separator + "IDChallenges" + File.separator + "outputForTests.xml");
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
        uiElements = (List<UiElement>) xStream.fromXML(new File("testApps" + File.separator + "IDChallenges" + File.separator + "outputForTests.xml"));
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
    @Ignore
    public void externFieldIDTest(){
        List<UiElement> uiEList = results.get("2131034112");
        assertNotNull("You missed at least one UiElement", uiEList);
        assertEquals("You overapproximate!", 1, uiEList.size());
        assertEquals("ID is saved in a global field in an extra class", "<com.example.ChallengeApps.MyActivity$5: void onClick(android.view.View)>", uiEList.get(0).signature);
    }

    @Test
    public void externStaticFieldIDTest(){
        List<UiElement> uiEList = results.get("2131034113");
        assertNotNull("You missed at least one UiElement", uiEList);
        assertEquals("You overapproximate!", 1, uiEList.size());
        assertEquals("ID is saved statically in a global field inside an extra class", "<com.example.ChallengeApps.MyActivity$4: void onClick(android.view.View)>", uiEList.get(0).signature);
    }

    @Test
    public void fieldIDTest(){
        List<UiElement> uiEList = results.get("2131034114");
        assertNotNull("You missed at least one UiElement", uiEList);
        assertEquals("You overapproximate!", 1, uiEList.size());
        assertEquals("ID is saved in a global field", "<com.example.ChallengeApps.MyActivity$9: void onClick(android.view.View)>", uiEList.get(0).signature);
    }

    @Test
    public void staticFieldIDTest(){
        List<UiElement> uiEList = results.get("2131034115");
        assertNotNull("You missed at least one UiElement", uiEList);
        assertEquals("You overapproximate!", 1, uiEList.size());
        assertEquals("ID is saved statically in a global field", "<com.example.ChallengeApps.MyActivity$10: void onClick(android.view.View)>", uiEList.get(0).signature);
    }

    @Test
    public void passedIDTest(){
        List<UiElement> uiEList = results.get("2131034116");
        assertNotNull("You missed at least one UiElement", uiEList);
        assertEquals("You overapproximate!", 1, uiEList.size());
        assertEquals("ID is passed in a method", "<com.example.ChallengeApps.MyActivity$21: void onClick(android.view.View)>", uiEList.get(0).signature);
    }

    @Test
    public void hierarchy1(){
        List<UiElement> uiEList = results.get("2131034127");
        assertNotNull("You missed at least one UiElement", uiEList);
        assertEquals("You overapproximate!", 1, uiEList.size());
        assertEquals("baseclass is created and used to access the public ID field in the superclass", "<com.example.ChallengeApps.MyActivity$16: void onClick(android.view.View)>", uiEList.get(0).signature);
    }

    @Test
    public void hierarchy2(){
        List<UiElement> uiEList = results.get("2131034117");
        assertNotNull("You missed at least one UiElement", uiEList);
        assertEquals("You overapproximate!", 1, uiEList.size());
        assertEquals("baseclass is created and there a getter is called which calls the getter from superclass", "<com.example.ChallengeApps.MyActivity$13: void onClick(android.view.View)>", uiEList.get(0).signature);
    }

    @Test
    public void hierarchy3(){
        List<UiElement> uiEList = results.get("2131034129");
        assertNotNull("You missed at least one UiElement", uiEList);
        assertEquals("You overapproximate!", 1, uiEList.size());
        assertEquals("baseclass is created and used to call a superclass getter", "<com.example.ChallengeApps.MyActivity$17: void onClick(android.view.View)>", uiEList.get(0).signature);
    }

    @Test
    public void hierarchy4(){
        List<UiElement> uiEList = results.get("2131034131");
        assertNotNull("You missed at least one UiElement", uiEList);
        assertEquals("You overapproximate!", 1, uiEList.size());
        assertEquals("baseclass is created and used to call a superclass getter", "<com.example.ChallengeApps.MyActivity$18: void onClick(android.view.View)>", uiEList.get(0).signature);
    }

    @Test
    @Ignore
    public void innerClassFieldTest(){
        List<UiElement> uiEList = results.get("2131034118");
        assertNotNull("You missed at least one UiElement", uiEList);
        assertEquals("You overapproximate!", 1, uiEList.size());
        assertEquals("Button is is saved as field in an inner class from the main activity", "<com.example.ChallengeApps.MyActivity$6: void onClick(android.view.View)>", uiEList.get(0).signature);
    }

    @Test
    public void arrayTest(){
        List<UiElement> uiEList = results.get("2131034119");
        assertNotNull("You missed at least one UiElement", uiEList);
        assertEquals("You overapproximate!", 1, uiEList.size());
        assertEquals("the ID is saved in an array", "<com.example.ChallengeApps.MyActivity$11: void onClick(android.view.View)>", uiEList.get(0).signature);
    }

    @Test
    @Ignore
    public void innerClassFieldIDTest(){
        List<UiElement> uiEList = results.get("2131034120");
        assertNotNull("You missed at least one UiElement", uiEList);
        assertEquals("You overapproximate!", 1, uiEList.size());
        assertEquals("ID is is saved as field in an inner class from the main activity", "<com.example.ChallengeApps.MyActivity$7: void onClick(android.view.View)>", uiEList.get(0).signature);
    }

    @Test
    public void fieldButtonTest(){
        List<UiElement> uiEList = results.get("2131034122");
        assertNotNull("You missed at least one UiElement", uiEList);
        assertEquals("You overapproximate!", 1, uiEList.size());
        assertEquals("button is saved in a field before the listener gets attached", "<com.example.ChallengeApps.MyActivity$8: void onClick(android.view.View)>", uiEList.get(0).signature);
    }

    @Test
    public void passedButtonTest(){
        List<UiElement> uiEList = results.get("2131034124");
        assertNotNull("You missed at least one UiElement", uiEList);
        assertEquals("You overapproximate!", 1, uiEList.size());
        assertEquals("button is passed in a method", "<com.example.ChallengeApps.MyActivity$20: void onClick(android.view.View)>", uiEList.get(0).signature);
    }

    // not functioning yet 2-2016
    @Test
    @Ignore
    public void loopTest(){
        List<UiElement> uiEList = results.get("2131034123");
        assertNotNull("You missed at least one UiElement", uiEList);
        assertEquals("You overapproximate!", 1, uiEList.size());
        assertEquals("ID is assigned in a loop", "<com.example.ChallengeApps.MyActivity$14: void onClick(android.view.View)>", uiEList.get(0).signature);
    }

    // not functioning yet 2-2016
    @Test
    @Ignore
    public void listTest(){
        List<UiElement> uiEList = results.get("2131034121");
        assertNotNull("You missed at least one UiElement", uiEList);
        assertEquals("You overapproximate!", 1, uiEList.size());
        assertEquals("ID is saved in a list", "<com.example.ChallengeApps.MyActivity$12: void onClick(android.view.View)>", uiEList.get(0).signature);
    }

    @Test
    @Ignore
    public void useGetterForIDTest(){
        List<UiElement> uiEList = results.get("2131034125");
        assertNotNull("You missed at least one UiElement", uiEList);
        assertEquals("You overapproximate!", 1, uiEList.size());
        assertEquals("ID is passed through a getter", "<com.example.ChallengeApps.MyActivity$15: void onClick(android.view.View)>", uiEList.get(0).signature);
    }

    @Test
    public void externStaticFieldTest(){
        List<UiElement> uiEList = results.get("2131034126");
        assertNotNull("You missed at least one UiElement", uiEList);
        assertEquals("You overapproximate!", 1, uiEList.size());
        assertEquals("ID is saved in an extern class in a static field", "<com.example.ChallengeApps.MyActivity$1: void onClick(android.view.View)>", uiEList.get(0).signature);
    }

    @Test
    @Ignore
    public void passedButtonFromOtherClassTest1(){
        List<UiElement> uiEList = results.get("2131034128");
        assertNotNull("You missed at least one UiElement", uiEList);
        assertEquals("You overapproximate!", 1, uiEList.size());
        assertEquals("button is searched/found in another class and then passed(via getter) to the activity", "<com.example.ChallengeApps.MyActivity$3: void onClick(android.view.View)>", uiEList.get(0).signature);
    }

    @Test
    @Ignore
    public void passedButtonFromOtherClassTest2(){
        List<UiElement> uiEList = results.get("2131034130");
        assertNotNull("You missed at least one UiElement", uiEList);
        assertEquals("You overapproximate!", 1, uiEList.size());
        assertEquals("button is searched/found in another class and there saved in a public field", "<com.example.ChallengeApps.MyActivity$2: void onClick(android.view.View)>", uiEList.get(0).signature);
    }

    @Test
    public void androidStandardIDTest(){
        List<UiElement> uiEList = results.get("16908308");
        assertNotNull("You missed at least one UiElement", uiEList);
        assertEquals("You overapproximate!", 1, uiEList.size());
        assertEquals("button is searched/found in another class and there saved in a public field", "<com.example.ChallengeApps.MyActivity$19: void onClick(android.view.View)>", uiEList.get(0).signature);
    }















































}
