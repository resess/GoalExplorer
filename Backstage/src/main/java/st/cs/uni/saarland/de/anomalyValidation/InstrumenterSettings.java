package st.cs.uni.saarland.de.anomalyValidation;

import com.beust.jcommander.Parameter;

/**
 * Created by avdiienko on 05/03/16.
 */
public class InstrumenterSettings {
    private static final String APKPATH="-apkPath";
    private static final String ANDROIDJAR="-androidJar";
    private static final String CALLBACK_SIGNATURE="-callback";
    private static final String API_SIGNATURE="-api";


    @Parameter(names = APKPATH, description = "Path to an apk file", required = true)
    public String apkPath;

    @Parameter(names = ANDROIDJAR, description = "Path to an androidJar file", required = true)
    public String androidJar;

    @Parameter(names = API_SIGNATURE, description = "API Signature", required = true)
    public String api;
}
