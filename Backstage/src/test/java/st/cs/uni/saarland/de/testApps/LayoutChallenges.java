package st.cs.uni.saarland.de.testApps;

import com.thoughtworks.xstream.XStream;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import st.cs.uni.saarland.de.reachabilityAnalysis.ApiInfoForForward;
import st.cs.uni.saarland.de.reachabilityAnalysis.UiElement;

import java.io.File;
import java.util.*;

import static org.junit.Assert.*;

/**
 * Created by Isa on 23.12.2015.
 */
public class LayoutChallenges {

    private static Map<String, Set<Integer>> results = new HashMap<String, Set<Integer>>(); // <activity, List<LayoutIDs>>

    @BeforeClass
    public static void setUp() throws Exception {
        String [] params = {"-apk",
                "testApps/LayoutChallenges.apk",
                "-androidJar",
                "libs/android.jar",
                "-apkToolOutput",
                "testApps/LayoutChallenges",
                "-test",
                "-processMenus"
        };

        File resultFile = new File("testApps" + File.separator + "LayoutChallenges" + File.separator + "outputForTestLayout.xml");
        if(resultFile.exists()) {
            if (!resultFile.delete()) {
                throw new Exception("Can not delete file with results");
            }
        }


        TestApp.main(params);

        XStream xStream = new XStream();
        xStream.setMode(XStream.NO_REFERENCES);
        results = (HashMap<String, Set<Integer>>) xStream.fromXML(new File("testApps" + File.separator + "LayoutChallenges" + File.separator + "outputForTestLayout.xml"));
        assertNotEquals("Results are null/0", 0, results.size());


    }

    // tests if the main viewpager layout and the number of slides is matched
    @Test
    public void dynamicViewPagerTest1(){
        Set<Integer> mergedXMLLayoutFiles = results.get("com.example.LayoutChallenges.ViewPagerActivity");
        assertNotNull("You missed an activity to layout matching", mergedXMLLayoutFiles);
        assertEquals("The number of merged layouts is not correct", 1, mergedXMLLayoutFiles.size());
        // viewpager layout file
        assertTrue("You didn't find the main viewpager layout", mergedXMLLayoutFiles.contains(2130903047));
    }

    @Test
    public void dynamicViewPagerTest2(){
//        Set<Integer> mergedXMLLayoutFiles = results.get("com.example.LayoutChallenges.ViewPagerActivity");
        // ..207 viewpager element, ..041 fragement1 layout
        Collection<Integer> mergedXMLLayoutFiles = AppController.getInstance().getIncludedLayoutsFromElement(2131165207);
        assertTrue("You didn't find the one viewpager slide layout", mergedXMLLayoutFiles.contains(2130903041));
    }

    @Test
    public void dynamicViewPagerTest3(){
//        Set<Integer> mergedXMLLayoutFiles = results.get("com.example.LayoutChallenges.ViewPagerActivity");
        // ..207 viewpager element, ..042 fragement2 layout
        Collection<Integer> mergedXMLLayoutFiles = AppController.getInstance().getIncludedLayoutsFromElement(2131165207);
        assertTrue("You didn't find the one viewpager slide layout", mergedXMLLayoutFiles.contains(2130903042));
    }

    // test if the number of merged layouts is matched
    @Test
    public void mergedLayoutTest1(){
        Set<Integer> mergedXMLLayoutFiles = results.get("com.example.LayoutChallenges.MergedLayoutActivity");
        assertNotNull("You missed an activity to layout matching", mergedXMLLayoutFiles);
        assertEquals("The number of merged layouts is not correct", 2, mergedXMLLayoutFiles.size());
    }

    // fragment tag
    @Test
    public void mergedLayoutTest2(){
//        Set<Integer> mergedXMLLayoutFiles = results.get("com.example.LayoutChallenges.MergedLayoutActivity");
        // merged_layout2
        Collection<Integer> mergedXMLLayoutFiles = AppController.getInstance().getIncludedLayoutsFromXMLLayoutFileElements(2130903045);
        // fragement1
        assertTrue("You didn't find the one merged layout", mergedXMLLayoutFiles.contains(2130903041));
    }

    // dynamic added fragment
    @Test
    public void mergedLayoutTest3(){
//        Set<Integer> mergedXMLLayoutFiles = results.get("com.example.LayoutChallenges.MergedLayoutActivity");
        // fragmentContainer
        Collection<Integer> mergedXMLLayoutFiles = AppController.getInstance().getIncludedLayoutsFromElement(2131165206);
        // fragment2
        assertTrue("You didn't find the one merged layout", mergedXMLLayoutFiles.contains(2130903042));
    }

    @Test
    public void mergedLayoutTest4(){
        Set<Integer> mergedXMLLayoutFiles = results.get("com.example.LayoutChallenges.MergedLayoutActivity");
        // merged_layout2
        assertTrue("You didn't find the one merged layout", mergedXMLLayoutFiles.contains(2130903045));
    }

    // include tag check
    @Test
    public void mergedLayoutTest5(){
//        Set<Integer> mergedXMLLayoutFiles = results.get("com.example.LayoutChallenges.MergedLayoutActivity");
        // merged_layout1
        Collection<Integer> mergedXMLLayoutFiles = AppController.getInstance().getIncludedLayoutsFromXMLLayoutFileElements(2130903044);
        // merged_layout3_merge_tag
        assertTrue("You didn't find the one merged layout", mergedXMLLayoutFiles.contains(2130903046));
    }

    @Test
    public void mergedLayoutTest6(){
        Set<Integer> mergedXMLLayoutFiles = results.get("com.example.LayoutChallenges.MergedLayoutActivity");
        // merged_layout1
        assertTrue("You didn't find the one merged layout", mergedXMLLayoutFiles.contains(2130903044));
    }

    // ---
    // not yet functioning: Dialog Fragment is missed, 2-2016
    // merged layouts: optionsMenu, NavDropDown menu, Dialog 1-3, main layout, context menu, popup menu
    // context and popup menu are not listed in the merged layouts list
    @Test
    @Ignore
    public void mainActivityTest1(){
        Set<Integer> mergedXMLLayoutFiles = results.get("com.example.LayoutChallenges.MyActivity");
        assertNotNull("You missed an activity to layout matching", mergedXMLLayoutFiles);
        assertEquals("The number of merged layouts is not correct", 6, mergedXMLLayoutFiles.size());
    }

    // ContextMenu Test on 2131165200
    @Test
    public void mainActivityTest2(){
//        Set<Integer> mergedXMLLayoutFiles = results.get("com.example.LayoutChallenges.MyActivity");
        Collection<Integer> mergedXMLLayoutFiles = AppController.getInstance().getIncludedLayoutsFromElement(2131165200);
        assertTrue("You didn't find the context menu", mergedXMLLayoutFiles.contains(2131099648));
    }

    // Main Layout Test
    @Test
    public void mainActivityTest3(){
        Set<Integer> mergedXMLLayoutFiles = results.get("com.example.LayoutChallenges.MyActivity");
        assertTrue("You didn't find the main layout", mergedXMLLayoutFiles.contains(2130903043));
    }

    // PopupMenu Test on 2131165199
    @Test
    public void mainActivityTest4(){
//        Set<Integer> mergedXMLLayoutFiles = results.get("com.example.LayoutChallenges.MyActivity");
        Collection<Integer> mergedXMLLayoutFiles = AppController.getInstance().getIncludedLayoutsFromElement(2131165199);
        assertTrue("You didn't find the popup menu", mergedXMLLayoutFiles.contains(2131099650));
    }

    // Optionmenu Test
    @Test
    public void mainActivityTest5(){
        Set<Integer> mergedXMLLayoutFiles = results.get("com.example.LayoutChallenges.MyActivity");
        assertTrue("You didn't find the option menu", mergedXMLLayoutFiles.contains(2131099649));
    }

    // Dialog1-3, navDropDown Menu Test
//    @Test
//    public void mainActivityTest6(){
//        Set<Integer> mergedXMLLayoutFiles = results.get("com.example.LayoutChallenges.MyActivity");
//        //assertTrue("You didn't find the option menu", mergedXMLLayoutFiles.contains("2131099649"));
//        //TODO  navDropDown, dialogs tests....
//    }

    // ---
    @Test
    public void tabViewActivityTest1(){
        Set<Integer> mergedXMLLayoutFiles = results.get("com.example.LayoutChallenges.TabViewActivity");
        assertNotNull("You missed an activity to layout matching", mergedXMLLayoutFiles);
        assertEquals("The number of merged layouts is not correct", 2, mergedXMLLayoutFiles.size());
    }

    @Test
    public void tabViewActivityTest2(){
        Set<Integer> mergedXMLLayoutFiles = results.get("com.example.LayoutChallenges.TabViewActivity");
        // fragment1
        assertTrue("You didn't find the one tab view", mergedXMLLayoutFiles.contains(2130903041));
    }

    @Test
    public void tabViewActivityTest3(){
        Set<Integer> mergedXMLLayoutFiles = results.get("com.example.LayoutChallenges.TabViewActivity");
        // fragment2
        assertTrue("You didn't find the one tab view", mergedXMLLayoutFiles.contains(2130903042));
    }

    // ---
//    @Test
//    public void viewPagerActivity_XMLTest1(){
//        Set<Integer> mergedXMLLayoutFiles = results.get("com.example.LayoutChallenges.ViewPagerActivity_XML");
//        assertNotNull("You missed an activity to layout matching", mergedXMLLayoutFiles);
//        assertEquals("The number of merged layouts is not correct", 3, mergedXMLLayoutFiles.size());
//        assertTrue("You didn't find the main xml viewpager layout", mergedXMLLayoutFiles.contains(2130903048));
//    }
//
//    @Test
//    public void viewPagerActivity_XMLTest2(){
//        Set<Integer> mergedXMLLayoutFiles = results.get("com.example.LayoutChallenges.ViewPagerActivity_XML");
//        assertTrue("You didn't find the one viewpager slide", mergedXMLLayoutFiles.contains(2130903041));
//    }
//
//    @Test
//    public void viewPagerActivity_XMLTest3(){
//        Set<Integer> mergedXMLLayoutFiles = results.get("com.example.LayoutChallenges.ViewPagerActivity_XML");
//        assertTrue("You didn't find the one viewpager slide", mergedXMLLayoutFiles.contains(2130903042));
//    }

    // ---
    @Test
    public void DrawerActivityTest1(){
        Set<Integer> mergedXMLLayoutFiles = results.get("com.example.LayoutChallenges.DrawerActivity");
        assertNotNull("You missed an activity to layout matching", mergedXMLLayoutFiles);
        assertEquals("The number of merged layouts is not correct", 1, mergedXMLLayoutFiles.size());
        // drawerlayout layout
        assertTrue("You didn't find the main drawer layout", mergedXMLLayoutFiles.contains(2130903040));
    }

    // ---
    @Test
    public void DrawerActivityListViewTest2(){
        XStream xStream = new XStream();
        xStream.processAnnotations(UiElement.class);
        xStream.processAnnotations(ApiInfoForForward.class);
        xStream.setMode(XStream.NO_REFERENCES);
        List<UiElement> uiElements = new ArrayList<UiElement>();
        uiElements = (List<UiElement>) xStream.fromXML(new File("testApps" + File.separator + "LayoutChallenges" + File.separator + "outputForTests.xml"));
        boolean foundElement = false;
        for (UiElement uiE : uiElements){
            if (uiE.elementId.equals("2131165187")){
                foundElement = true;
                assertEquals("You didn't find the text of a listview inside the drawer layout", "Second choice#First choice", uiE.text.get("com.example.LayoutChallenges.DrawerActivity"));
                break;
            }
        }
        assertTrue("you missed a ListView", foundElement);
    }


    // test for getNeighbours method
    @Test
    public void getNeighboursTest1(){
        List<Set<Integer>> neighbours = AppController.getInstance().getNeighbours(2131165191, 1, 1, 1);
        assertEquals("number of layouts is not correct", 1, neighbours.size());
        // Linearlayout, xmlViewpager button
        assertEquals("number of found elements inside the layout is not correct", 3, neighbours.get(0).size());
        // xmlviewpager button
        assertTrue("", neighbours.get(0).contains(2131165192));
        // the element itself
        assertTrue("", neighbours.get(0).contains(2131165191));
    }

//    @Test
//    public void getNeighboursTest2(){
//        List<Set<Integer>> neighbours = AppController.getInstance().getNeighbours(2131165191, 2, 2, 2);
//        assertEquals("number of layouts is not correct", 1, neighbours.size());
//        // Linearlayout, xmlViewpager button
//        //assertEquals("number of found elements inside the layout is not correct", 14, neighbours.get(0).size());
//        // xmlviewpager button
//        assertTrue("", neighbours.get(0).contains(2131165192));
//        // the element itself
//        assertTrue("", neighbours.get(0).contains(2131165191));
//        assertTrue("", neighbours.get(0).contains(2131165190));
//        assertTrue("", neighbours.get(0).contains(2131165199));
//        assertTrue("", neighbours.get(0).contains(2131165198));
//        assertTrue("", neighbours.get(0).contains(2131165197));
//        assertTrue("", neighbours.get(0).contains(2131165196));
//        assertTrue("", neighbours.get(0).contains(2131165195));
//        assertTrue("", neighbours.get(0).contains(2131165194));
//        assertTrue("", neighbours.get(0).contains(2131165193));
//        assertTrue("", neighbours.get(0).contains(2131165192));
//        assertTrue("", neighbours.get(0).contains(2131165201)); // ListView
//        assertTrue("", neighbours.get(0).contains(2131165200));// getContextMenu
//    int a = 9;
    // 2131165215 popupMenuItem1
    // 2131165212-14 optionMenu items
//    }



}
