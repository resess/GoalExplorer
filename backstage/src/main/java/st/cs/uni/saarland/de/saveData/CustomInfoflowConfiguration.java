package st.cs.uni.saarland.de.saveData;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kuznetsov on 19/01/16.
 */
public class CustomInfoflowConfiguration {
    public static String sourceCategories = "ALL";
    public static String sinkCategories = "ALL";
    public static List<String> excludeSourceCategories = new ArrayList<String>();
    public static List<String> excludeSinkCategories = new ArrayList<String>();
    public static String androidSources;
    public static String androidSinks;
    public static List<String> sourceSignatures = new ArrayList<String>();
    public static List<String> sinkSignatures = new ArrayList<String>();
}
