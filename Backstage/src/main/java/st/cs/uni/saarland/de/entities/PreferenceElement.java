package st.cs.uni.saarland.de.entities;
import st.cs.uni.saarland.de.testApps.Content;
import java.util.*;

public class PreferenceElement extends SpecialXMLTag{
    private String key, title, intentAction, intentTargetClass, intentData;
    private String assignedActivity;
    //private List<PreferenceScreen> nestedPreferences;

    public PreferenceElement(String key, String title, String kindOfUIelement, List<Integer> parent,
    String idFromXMLTag, Map<String, String> solvedText, String textVar, Set<String> drawableNames, Set<Style> styles){
        super(kindOfUIelement, parent, Integer.toString(Content.getNewUniqueID()), solvedText, textVar, drawableNames, styles);
        this.key = key;
        this.title = title;
    }

    public String toString() {
        return "PreferenceElement key: "+key+"  "+title+"  "+intentAction+"  "+intentTargetClass+" "+super.toString();
    }

    public String getKey(){
        return this.key;
    }

    public String getTitle() {
        return title;
    }

    public String getIntentAction() {
        return intentAction;
    }

    public String getIntentTargetClass() {
        return intentTargetClass;
    }

    public String getIntentData() {
        return intentData;
    }

    public String getAssignedActivity() {
        return assignedActivity;
    }

    public void setIntentAction(String intentAction) {
        this.intentAction = intentAction;
    }

    public void setIntentTargetClass(String intentTargetClass) {
        this.intentTargetClass = intentTargetClass;
    }

    public void setAssignedActivity(String assignedActivity) {
        this.assignedActivity = assignedActivity;
    }
}