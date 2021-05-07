package st.cs.uni.saarland.de.anomalyValidation;

import com.beust.jcommander.Parameter;

/**
 * Created by avdiienko on 08/03/16.
 */
public class ApiTracerSettings {
    private static final String APKPATH="-apkPath";
    private static final String ANDROIDJAR="-androidJar";
    private static final String CALLBACK_SIGNATURE="-callback";
    private static final String API_SIGNATURE="-api";
    private static final String DEPTH="-depth";
    private static final String ELEMENTID="-elementId";


    @Parameter(names = APKPATH, description = "Path to an apk file", required = true)
    public String apkPath;

    @Parameter(names = ANDROIDJAR, description = "Path to an androidJar file", required = true)
    public String androidJar;

    @Parameter(names = API_SIGNATURE, description = "API Signature", required = true)
    public String api;

    @Parameter(names = CALLBACK_SIGNATURE, description = "Callback from which we start", required = true)
    public  String callback;

    @Parameter(names = DEPTH, description = "Depth of Api", required = true)
    public int depth;

    @Parameter(names = ELEMENTID, description = "ElementId", required = true)
    public String elementId;


}
