package st.cs.uni.saarland.de.saveData;

import com.beust.jcommander.Parameter;

/**
 * Created by avdiienko on 30/11/15.
 */
public class DataMergerHelperSettings {
    public static final String PATH_TO_RESULTS_XML="-apiResults";
    public static final String PATH_TO_SERIALIZED_OBJECT="-serializedObject";
    public static final String OUTPUT_FOLDER="-outputFolder";
    public static final String WHOLE_LAYOUT="-wholeLayout";

    @Parameter(names = PATH_TO_RESULTS_XML, description = "Path to <*.apk_forward_apiResults.xml> file", required = true)
    public String xmlPath;

    @Parameter(names = PATH_TO_SERIALIZED_OBJECT, description = "Path to appSerialised.txt", required = true)
    public String serializedObjectPath;

    @Parameter(names = OUTPUT_FOLDER, description = "Path to an output folder", required = true)
    public String outputFolderPath;

    @Parameter(names = WHOLE_LAYOUT, description = "Use text from the whole layout", required = false)
    public Boolean wholeLayout = false;
}
