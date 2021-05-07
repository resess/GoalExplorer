package st.cs.uni.saarland.de.saveData;

import org.jsoup.Jsoup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by kuznetsov on 16/04/16.
 */
public class Label {
    static final String default_label = "default_value";
    public static final String NO_ICON = "NO_ICON";
    public boolean isOrphanLayout;
    public String originalText;
    public String elementType;
    public String textValue;
    public String callbackClass;
    public String uiID;
    public String variableName;
    public String container;
    public boolean hasCallback;
    private String apkName;
    private String icon;

    public Label(String apkName, String elementType, String textValue, String callbackClass, String container,
                 String uiID, String variableName, boolean hasCallback, String default_label, boolean isOrphanLayout,
                 String icon) {
        this.apkName = apkName;
        assert (textValue != null);
        this.elementType = elementType;
        if (textValue.contains("#"))
            this.textValue = reduceLabel(textValue, default_label);
        else
            this.textValue = sanitise(textValue);
        this.originalText = sanitise_special(textValue).replaceAll("[\";\\\\]", "");
        this.callbackClass = callbackClass;
        this.container = container;
        this.uiID = uiID;
        this.variableName = variableName;
        this.hasCallback = hasCallback;
        this.isOrphanLayout = isOrphanLayout;
        this.icon = icon;
    }

    public static Label DialogLabel(String apkName, String text, String id, String declaringClass) {
        return new Label(apkName, "dialogButton", text, declaringClass, "DIALOG", id, "NAN", true, Label.default_label,
                false, NO_ICON);
    }

    public ArrayList<String> getRow(Set<String> context) {
        String contextLine = context.stream().filter(x -> x.length() > 0).filter(x -> !x.equals(textValue)).distinct()
                .collect(Collectors.joining(". "));
        return new ArrayList<String>() {
            {
                add(icon);
                add(elementType);
                add(textValue);
                add(contextLine);
                add(callbackClass);
                add(hasCallback ? "CALL" : "NO_CALL");
                add(uiID);
                add(container);
                add(originalText);
                add(apkName);
                add(UIAnalysis.getID());
            }
        };
    }

    public static ArrayList<String> getRowHeader() {
        return new ArrayList<String>() {
            {
                add("icon");
                add("type");
                add("label");
                add("context");
                add("declaringClass");
                add("hascallback");
                add("uid");
                add("container");
                add("rawtext");
                add("apk");
                add("id");
            }
        };
    }

    public boolean isAccepted() {
        return !textValue.isEmpty() && !(isOrphanLayout && variableName.startsWith("abc_"));
    }

    public boolean isActive() {
        return hasCallback;
    }

    public boolean isButton() {
        return elementType != null && elementType.toLowerCase().contains("button");
    }

    public boolean isEmpty() {
        return textValue.isEmpty();
    }

    public static String sanitise(String value) {
        String text = sanitise_special(value);
        text = Jsoup.parse(text).text();
        text = text.replaceAll("[\\(\\)]+", " ");//remove parenthesis
        text = text.replaceAll("(null)", " ");//remove null
        text = text.replaceAll("\\$[rdi][^#\\s]*(#|$)?", " ");//remove $i0 strings
        text = text.replaceAll("http[s]?://[^\\s]*", " ");//remove links
        text = text.replaceAll("_", "");//remove underscores
        text = text.replaceAll("\\b[0-9]+\\b", " ");//remove standalone numbers
        text = text.replaceAll("\\\\n", " ");//remove \n
        text = text.replaceAll("[\\p{Punct}&&[^-'#]]", "");//remove punctuation
        text = text.replaceAll("\\B-\\B", " ");//remove orphan dash
        text = text.replaceAll("\\b(?<![\\w'])[a-zA-Z](?![\\w-])\\b", " ");//remove one letter but not 's and e-
        text = text.replaceAll("\\s+", " ");//remove multiple spaces
        text = text.trim();
        return text;
    }

    private static  String sanitise_special(String value) {
        String text = value.replaceAll("[^\\x20-\\x7E]", " ");//remove non acsii
        text = text.replaceAll("[\\h\\v]+", " ");//remove unicode spaces
        text = text.replaceAll("[\n\r\t]", " ");//remove line break
        text = text.trim();
        return text;
    }

    private String reduceLabel(String textForActivity, String default_label) {
        Set<String> textChunks = Arrays.stream(textForActivity.split("#")).map(Label::sanitise)
                .collect(Collectors.toSet());
        textChunks.remove(sanitise(default_label));
        textChunks.remove("");
        return textChunks.stream().collect(Collectors.joining("#"));
    }

    @Override
    public int hashCode() {
        return Objects.hash(uiID, textValue, callbackClass);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Label))
            return false;
        if (obj == this)
            return true;
        return obj.hashCode() == this.hashCode();
    }
}