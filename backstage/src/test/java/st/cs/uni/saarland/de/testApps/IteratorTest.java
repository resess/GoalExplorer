package st.cs.uni.saarland.de.testApps;

import org.junit.Test;
import st.cs.uni.saarland.de.entities.AppsUIElement;
import st.cs.uni.saarland.de.entities.Listener;
import st.cs.uni.saarland.de.searchListener.XMLDefinedListenerIterator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static junit.framework.TestCase.assertEquals;

/**
 * Created by Isa on 06.02.2016.
 */
public class IteratorTest{

    Map<Integer, AppsUIElement> elements = new HashMap<Integer, AppsUIElement>();

    @Test
    public void test1(){
        Listener listener = new Listener("onClick", true, "void onClick(View view)", "default_value");
        AppsUIElement ui = new AppsUIElement("button", new ArrayList<>(), "", new HashMap<>(), "", listener , null, null);
        elements.put(ui.getId(), ui);

        Listener listener2 = new Listener("onKey", true, "void onClick(View view)", "default_value");
        AppsUIElement ui2 = new AppsUIElement("button", new ArrayList<>(), "", new HashMap<>(), "", listener2 , null, null);
        elements.put(ui2.getId(), ui2);

        XMLDefinedListenerIterator iter = new XMLDefinedListenerIterator(elements);

        while(iter.hasNext()){
            Listener l = iter.next();
            l.setListenerClass("bla1");
        }

        for (AppsUIElement uiE : elements.values()){
           assertEquals(uiE.getListernersFromElement().get(0).getListenerClass(),"bla1");
        }
    }
}
