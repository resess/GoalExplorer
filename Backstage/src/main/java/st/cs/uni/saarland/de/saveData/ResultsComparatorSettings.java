package st.cs.uni.saarland.de.saveData;

import com.beust.jcommander.Parameter;

/**
 * Created by avdiienko on 23/12/15.
 */
public class ResultsComparatorSettings {
    private final static String FIRST_ITEM = "-f1";
    private final static String SECOND_ITEM = "-f2";

    @Parameter(names = FIRST_ITEM, description = "First file to compare")
    public String firstItem;

    @Parameter(names = SECOND_ITEM, description = "First file to compare")
    public String secondItem;
}
