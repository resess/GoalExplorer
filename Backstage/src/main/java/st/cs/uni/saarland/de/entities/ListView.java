package st.cs.uni.saarland.de.entities;

import java.util.*;

public class ListView extends AdapterView{

    
    public ListView(String kindOfUIelement, List<Integer> parent,
                    String idFromXMLTag, Map<String, String> solvedText, String textVar, Set<String> drawableNames, Set<Style> styles) {
        
        super(kindOfUIelement, parent, idFromXMLTag, solvedText, textVar, drawableNames, styles);
    }
}
