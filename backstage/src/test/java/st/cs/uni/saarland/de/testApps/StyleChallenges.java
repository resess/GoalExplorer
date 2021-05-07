package st.cs.uni.saarland.de.testApps;

import com.thoughtworks.xstream.XStream;
import org.junit.Assert;
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
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by Isa on 02.03.2016.
 */
public class StyleChallenges {

    private static Map<String, List<UiElement>> results = new HashMap<String, List<UiElement>>(); // <counter, List<UiELement>>
    @BeforeClass
    public static void setUp() throws Exception {
        String [] params = {"-apk",
                "testApps/StyleChallenges.apk",
                "-androidJar",
                "libs/android.jar",
                "-apkToolOutput",
                "testApps/StyleChallenges",
                "-test",
                "-processMenus"
        };

        File resultFile = new File("testApps" + File.separator + "StyleChallenges" + File.separator + "outputForTests.xml");
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
        uiElements = (List<UiElement>) xStream.fromXML(new File("testApps" + File.separator + "StyleChallenges" + File.separator + "outputForTests.xml"));
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

    // string variable test
    @Test
    public void simpleTextTest(){
        List<UiElement> uiEList = results.get("2131099648");
        assertNotNull("You missed at least one UiElement", uiEList);
        Assert.assertEquals("pure string given in the XML layout file", "simple style with strVar", uiEList.get(0).text.get("default_value"));
    }
    @Test
    public void simpleListenerTest(){
        List<UiElement> uiEList = results.get("2131099648");
        assertNotNull("You missed at least one UiElement", uiEList);
        assertEquals("simple onClick in style file", "<com.example.StyleChallenges.MainActivity: void xmlOnClick(android.view.View)>", uiEList.get(0).signature);
    }


    // plain text
    @Test
    public void simpleTextTest2(){
        List<UiElement> uiEList = results.get("2131099649");
        assertNotNull("You missed at least one UiElement", uiEList);
        Assert.assertEquals("pure string given in the XML layout file", "simple Button plain", uiEList.get(0).text.get("default_value"));
    }

    @Test
    public void superClassText(){
        List<UiElement> uiEList = results.get("2131099650");
        assertNotNull("You missed at least one UiElement", uiEList);
        Assert.assertEquals("pure string given in the XML layout file", "superStyle", uiEList.get(0).text.get("default_value"));
    }

    @Test
    @Ignore
    public void childClassText(){
        List<UiElement> uiEList = results.get("2131099651");
        assertNotNull("You missed at least one UiElement", uiEList);
        Assert.assertEquals("pure string given in the XML layout file", "superStyle", uiEList.get(0).text.get("default_value"));
    }

    @Test
    @Ignore
    public void textonClickButton(){
        List<UiElement> uiEList = results.get("2131099652");
        assertNotNull("You missed at least one UiElement", uiEList);
        Assert.assertTrue("pure string given in the XML layout file", uiEList.get(0).text.get("default_value").contains("simple style with strVar"));
        Assert.assertTrue("pure string given in the XML layout file", uiEList.get(0).text.get("default_value").contains("TextInButtonTag"));
    }

    @Test
    public void onClickButton(){
        List<UiElement> uiEList = results.get("2131099652");
        assertNotNull("You missed at least one UiElement", uiEList);
        assertEquals("simple onClick in style file", "<com.example.StyleChallenges.MainActivity: void xmlOnClick2(android.view.View)>", uiEList.get(0).signature);
    }

    /*
    <styles id="12">
          <st.cs.uni.saarland.de.entities.Style id="13">
            <name>MySimpleButtonStyle</name>
            <id>0</id>
            <text id="14">
              <string>simple style with strVar</string>
            </text>
            <onClickMethod id="15">
              <listenerMethod>void xmlOnClick(android.view.View)</listenerMethod>
              <listenerClass>com.example.StyleChallenges.MainActivity</listenerClass>
              <actionWaitingFor>onClick</actionWaitingFor>
              <xmlDefined>true</xmlDefined>
              <calledAPISignatures id="16"/>
            </onClickMethod>
            <parent></parent>
          </st.cs.uni.saarland.de.entities.Style>
        </styles>
    */

}
