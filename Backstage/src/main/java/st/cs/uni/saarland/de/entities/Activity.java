package st.cs.uni.saarland.de.entities;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by avdiienko on 19/07/16.
 */
public class Activity {
    public String getName() {
        if (name == null) {
            name = "";
        }
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    private String name;

    public String getLabel() {
        if (label == null) {
            label = "";
        }
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    private String label;

    public Set<Integer> getXmlLayouts() {
        if (xmlLayouts == null) {
            xmlLayouts = new HashSet<>();
        }
        return xmlLayouts;
    }

    public void addXmlLayout(Integer layoutId) {
        if (xmlLayouts == null) {
            xmlLayouts = new HashSet<>();
        }
        xmlLayouts.add(layoutId);
    }

    private Set<Integer> xmlLayouts;

    @Override
    public boolean equals(Object toCompare) {
        if (!(Activity.class.equals(toCompare.getClass()))) {
            return false;
        }
        Activity second = (Activity) toCompare;
        return this.name.equals(second.getName());
    }

    @Override
    public int hashCode() {
        return this.name.hashCode();
    }
}
