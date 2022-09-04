package st.cs.uni.saarland.de.entities;

import java.util.*;

public class AdapterView extends SpecialXMLTag{
    private String assignedActivity;
    private Set<Integer> itemIDs;



    public AdapterView(String kindOfUIelement, List<Integer> parent, String idFromXMLTag, Map<String, String> solvedText, String textVar, Set<String> drawableNames, Set<Style> styles) {
        super(kindOfUIelement, parent, idFromXMLTag, solvedText, textVar, drawableNames, styles);
        itemIDs = new HashSet<>();
    }

        public String getAssignedActivity() {
        return assignedActivity;
    }

    public void setAssignedActivity(String activity){
        this.assignedActivity = activity;
    }

    public void addItemID(Integer id){
        this.itemIDs.add(id);
    }

    public Set<Integer> getItemIDs() {
        return itemIDs;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        AdapterView that = (AdapterView) o;
        return Objects.equals(assignedActivity, that.assignedActivity) && Objects.equals(itemIDs, that.itemIDs);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), assignedActivity, itemIDs);
    }
}
