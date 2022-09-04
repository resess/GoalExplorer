package st.cs.uni.saarland.de.entities;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class Spinner extends AdapterView{
    public Spinner(String kindOfUIelement, List<Integer> parent, String idFromXMLTag, Map<String, String> solvedText, String textVar, Set<String> drawableNames, Set<Style> styles) {
        super(kindOfUIelement, parent, idFromXMLTag, solvedText, textVar, drawableNames, styles);
    }

}
