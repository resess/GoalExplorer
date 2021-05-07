package st.cs.uni.saarland.de.searchListener;

import st.cs.uni.saarland.de.entities.AppsUIElement;
import st.cs.uni.saarland.de.entities.Listener;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by Isa on 06.02.2016.
 */
public class XMLDefinedListenerIterator implements Iterator {

    private Map<Integer, AppsUIElement> elements;
    private Iterator<Map.Entry<Integer, AppsUIElement>> entries;

    private List<Listener> listenerList;
    private int index = 0;

    public XMLDefinedListenerIterator(Map<Integer, AppsUIElement> uiElements){
        elements = uiElements;
        entries = elements.entrySet().iterator();
    }

    @Override
    public boolean hasNext(){
        while (entries.hasNext() || listenerList != null) {
            if (listenerList != null){
                for (int i = index; i < listenerList.size(); i++){
                    Listener l = listenerList.get(i);
                    if (l.isXMLDefined()) {
                        index = i;
                        return true;
                    }
                }
                // null the listenerList and the index because it is completly processed
                listenerList = null;
                index = 0;
            }
            // last uiE was completly processed : get a new one
            if (!entries.hasNext())
                return false;
            Map.Entry<Integer, AppsUIElement> entry = entries.next();
            listenerList = entry.getValue().getListernersFromElement();
            for (int i = index; i < listenerList.size(); i++){
                Listener l = listenerList.get(i);
                //FIXME listenerList.size() == 2 although only 1 element is attached....
                if (l == null)
                    continue;
                if (l.isXMLDefined()){
                    index = i;
                    return true;
                }
            }
            // null the listenerList and the index because it is completly processed
            listenerList = null;
            index = 0;
        }
        return false;
    }

    @Override
    public Listener next(){
        // check if there are more element that needs to be analysed
        // if listenerList is not null, then there was a former uiELement that was not completly processed
        while (entries.hasNext() || listenerList != null) {
            if (listenerList != null){
                for (int i = index; i < listenerList.size(); i++){
                    Listener l = listenerList.get(i);
                    if (l.isXMLDefined()) {
                        index = i+1;
                        return l;
                    }
                }
                // null the listenerList and the index because it is completly processed
                listenerList = null;
                index = 0;
            }
            // last uiE was completly processed : get a new one
            Map.Entry<Integer, AppsUIElement> entry = entries.next();
            listenerList = entry.getValue().getListernersFromElement();
            for (int i = index; i < listenerList.size(); i++){
                Listener l = listenerList.get(i);
                if (l.isXMLDefined()) {
                    index = i+1;
                    return l;
                }
            }
            // null the listenerList and the index because it is completly processed
            listenerList = null;
            index = 0;
        }
        return null;
    }

}
