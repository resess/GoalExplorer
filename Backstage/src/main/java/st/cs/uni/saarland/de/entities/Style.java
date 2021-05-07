package st.cs.uni.saarland.de.entities;

import org.apache.commons.lang3.StringUtils;
import st.cs.uni.saarland.de.helpClasses.Helper;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Isa on 02.03.2016.
 */
public class Style {
    private String name = ""; // name of style as defined in styles.xml
    private int id; // Android id (at the moment not used or set)
    private Set<String> text = new HashSet<String>(); // set of text found in this style (not parent styles)
    private Listener onClickMethod; // name of xml defined onClick method
    private String parent;

    public Style(String pName, String parent) {
        this.name = pName;
        this.parent = parent;
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean hasText() { //FIXME check usages! doesn't check parents
        return !text.isEmpty();
    }

    public boolean hasListener() { //FIXME check usages! doesn't check parents
        return onClickMethod != null;
    }

    /*
     * take the first non-empty line
     */
    public String getText() { //FIXME doesn't check parents
        //StringUtils.join(text, '#');
        return text.stream().map(String::trim).filter(x -> !x.isEmpty()).findFirst().orElse("");
    }

    public Set<String> getTextSet() {
        return text;
    }

    public void addText(String ptext) {
        if (!StringUtils.isBlank(ptext))
            this.text.add(ptext);
    }

    // returns the listener of that style or if there is none, the listener of the next parent style that has one
    public Listener getOnClickMethod() { //FIXME check usages! doesn't check parents
        return onClickMethod;
    }

    public void setOnClickMethod(Listener onClickMethod) {
        if (this.onClickMethod == null)
            this.onClickMethod = onClickMethod;
        else
            Helper.saveToStatisticalFile("Style: tried to overwrite onClick method inside a style");
    }

    public String getParent() {
        return parent;
    }

    public boolean hasParent() {
        return StringUtils.isBlank(parent) ? false : true;
    }
}
