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
public class TextChallengeTest {

    private static Map<String, List<UiElement>> results = new HashMap<String, List<UiElement>>(); // <counter, List<UiELement>>

    @BeforeClass
    public static void setUp() throws Exception {
        String [] params = {"-apk",
                "testApps/TextChallenges.apk",
                "-androidJar",
                "libs/android.jar",
                "-apkToolOutput",
                "testApps/TextChallenges",
                "-test",
                "-processMenus"
        };

        File resultFile = new File("testApps" + File.separator + "TextChallenges" + File.separator + "outputForTests.xml");
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
        uiElements = (List<UiElement>) xStream.fromXML(new File("testApps" + File.separator + "TextChallenges" + File.separator + "outputForTests.xml"));
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
    public void stringInXMLTest(){
        List<UiElement> uiEList = results.get("2131034112");
        assertNotNull("You missed at least one UiElement", uiEList);
        assertEquals("You have not the correct number of UiElements for this ID", 1, uiEList.size());
        assertEquals("pure string given in the XML layout file", "Test 1", uiEList.get(0).text.get("default_value"));
    }

    @Test
    public void stringIDInXMLTest(){
        List<UiElement> uiEList = results.get("2131034113");
        assertNotNull("You missed at least one UiElement", uiEList);
        assertEquals("You have not the correct number of UiElements for this ID", 1, uiEList.size());
        assertEquals("pure string given in the XML layout file", "Test 2", uiEList.get(0).text.get("default_value"));
    }

    @Test
    public void simpleDynTextTest(){
        List<UiElement> uiEList = results.get("2131034121");
        assertNotNull("You missed at least one UiElement", uiEList);
        assertEquals("You have not the correct number of UiElements for this ID", 1, uiEList.size());
        assertEquals("simple StringBuilder", "Test 10", uiEList.get(0).text.get("com.example.TextChallenges.MyActivity"));
    }

    @Test
    @Ignore
    public void globalFieldTest(){
        List<UiElement> uiEList = results.get("2131034115");
        assertNotNull("You missed at least one UiElement", uiEList);
        assertEquals("You have not the correct number of UiElements for this ID", 1, uiEList.size());
        assertEquals("text is inside a global field", "Test 4", uiEList.get(0).text.get("com.example.TextChallenges.MyActivity"));
    }

    @Test
    public void globalFieldIDTest(){
        List<UiElement> uiEList = results.get("2131034116");
        assertNotNull("You missed at least one UiElement", uiEList);
        assertEquals("You have not the correct number of UiElements for this ID", 1, uiEList.size());
        assertEquals("text ID is inside a global field", "Test 5", uiEList.get(0).text.get("com.example.TextChallenges.MyActivity"));
    }

    @Test
    @Ignore
    public void globalFieldLaterWrittenTest(){
        List<UiElement> uiEList = results.get("2131034114");
        assertNotNull("You missed at least one UiElement", uiEList);
        assertEquals("You have not the correct number of UiElements for this ID", 1, uiEList.size());
        assertEquals("text is declared in a global field that is at the start of the app empty", "Test 3", uiEList.get(0).text.get("com.example.TextChallenges.MyActivity"));
    }

    @Test
    public void staticTextIDTest(){
        List<UiElement> uiEList = results.get("2131034117");
        assertNotNull("You missed at least one UiElement", uiEList);
        assertEquals("You have not the correct number of UiElements for this ID", 1, uiEList.size());
        assertEquals("text ID is inside a global field", "Test 6", uiEList.get(0).text.get("com.example.TextChallenges.MyActivity"));
    }

    @Test
    public void simpleStringBuilderTest(){
        List<UiElement> uiEList = results.get("2131034119");
        assertNotNull("You missed at least one UiElement", uiEList);
        assertEquals("You have not the correct number of UiElements for this ID", 1, uiEList.size());
        assertEquals("simple StringBuilder", "Test 8", uiEList.get(0).text.get("com.example.TextChallenges.MyActivity"));
    }

    // not yet functioning 2-2016
    @Test
    @Ignore
    public void nestedStringBuilderTest(){
        List<UiElement> uiEList = results.get("2131034118");
        assertNotNull("You missed at least one UiElement", uiEList);
        assertEquals("You have not the correct number of UiElements for this ID", 1, uiEList.size());
        assertEquals("one StringBuilder requires the output of the other StringBuilder", "Test 7", uiEList.get(0).text.get("com.example.TextChallenges.MyActivity"));
    }

    @Test
    public void stringDynAttachedTest(){
        List<UiElement> uiEList = results.get("2131034120");
        assertNotNull("You missed at least one UiElement", uiEList);
        assertEquals("You have not the correct number of UiElements for this ID", 1, uiEList.size());
        assertEquals("text attached to TextView in java code; dyn. text overwrites the XML ones", "Test 9", uiEList.get(0).text.get("com.example.TextChallenges.MyActivity"));
    }

    @Test
    @Ignore
    public void concatStringTest(){
        List<UiElement> uiEList = results.get("2131034122");
        assertNotNull("You missed at least one UiElement", uiEList);
        assertEquals("You have not the correct number of UiElements for this ID", 1, uiEList.size());
        assertEquals("concatination of strings", "Test 11", uiEList.get(0).text.get("com.example.TextChallenges.MyActivity"));
    }

    @Test
    public void androidStdIDXMLTest(){
        List<UiElement> uiEList = results.get("2131034125");
        assertNotNull("You missed at least one UiElement", uiEList);
        assertEquals("You have not the correct number of UiElements for this ID", 1, uiEList.size());
        assertEquals("resolve android standard value string IDs in an XML layout file", "Cancel", uiEList.get(0).text.get("default_value"));
    }

    @Test
    @Ignore
    public void androidStdIDTest(){
        List<UiElement> uiEList = results.get("2131034126");
        assertNotNull("You missed at least one UiElement", uiEList);
        assertEquals("You have not the correct number of UiElements for this ID", 1, uiEList.size());
        assertEquals("resolve android standard value string IDs in java code", "OK", uiEList.get(0).text.get("default_value"));
    }

    @Test
    @Ignore
    public void arrayTextTest(){
        List<UiElement> uiEList = results.get("2131034123");
        assertNotNull("You missed at least one UiElement", uiEList);
        assertEquals("You have not the correct number of UiElements for this ID", 1, uiEList.size());
        assertEquals("the text for the TextView is saved in an array", "Test 12", uiEList.get(0).text.get("com.example.TextChallenges.MyActivity"));
    }

    // not yet functioning 2-2016
    @Test
    @Ignore
    public void listTextTest(){
        List<UiElement> uiEList = results.get("2131034124");
        assertNotNull("You missed at least one UiElement", uiEList);
        assertEquals("You have not the correct number of UiElements for this ID", 1, uiEList.size());
        assertEquals("the text for the TextView is saved in an list, plus the index of the list element is given by a parameter", "Test 13", uiEList.get(0).text.get("default_value"));
    }



































}
