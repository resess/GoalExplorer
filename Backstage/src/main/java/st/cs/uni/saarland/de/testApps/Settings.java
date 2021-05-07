package st.cs.uni.saarland.de.testApps;

import com.beust.jcommander.Parameter;
import st.cs.uni.saarland.de.helpClasses.CallGraphAlgorithms;

import java.util.concurrent.TimeUnit;

/**
 * Created by avdiienko on 24/11/15.
 */
public class Settings {
    private static final String APK = "-apk";
    private static final String ANDROID_JAR = "-androidJar";
    private static final String APK_TOOL_OUTPUT = "-apkToolOutput";
    private static final String R_TIMEOUT_VALUE="-rTimeoutValue";
    private static final String R_TIMEOUT_UNIT="-rTimeoutUnit";
    private static final String R_ANALYSIS="-rAnalysis";
    private static final String T_TIMEOUT_UNIT ="-uiTimeoutUnit";
    private static final String T_TIMEOUT_VALUE ="-uiTimeoutValue";
    private static final String LOAD_UI_RESULTS="-loadUiResults";
    private static final String SAVE_JIMPLE="-saveJimple";
    private static final String TEST="-test";
    private static final String LOGS_DIR="-logsDir";
    private static final String RESULTS_DIR="-resultsDir";
    private static final String NUM_THREADS="-numThreads";
    private static final String MAX_LOC_IN_METHOD="-maxLocInMethod";
    private static final String MAX_DEPTH_METHOD_LEVEL="-maxDepthMethodLevel";
    private static final String UI_MENUS="-processMenus";
    private static final String PROCESS_IMAGES="-images";
    private static final String NO_LANG="-noLang";
    private static final String CG_ALGO="-cgAlgo";
    private static final String USE_LIFECYCLE="-rLifecycle";
    private static final String LIMIT_BY_PACKAGE_NAME="-rLimitByPackageName";

    @Parameter(names = APK, description = "Path to the APK file", required = true)
    public String apkPath;

    @Parameter(names = ANDROID_JAR, description = "Path to the android.jar file", required = true)
    public String androidJar;

    @Parameter(names = APK_TOOL_OUTPUT, description = "Path to the output of an apktool", required = true)
    public String apkToolOutputPath;

    @Parameter(names=R_TIMEOUT_VALUE, description = "Timeout value for each callback in the reachability analysis", required = false)
    public int rTimeoutValue = 30;

    @Parameter(names=R_TIMEOUT_UNIT, description = "Timeout unit for each callback in the reachability analysis", required = false)
    public TimeUnit rTimeoutUnit= TimeUnit.SECONDS;

    @Parameter(names=R_ANALYSIS, description = "Perform reachability analysis", required = false)
    public Boolean performReachabilityAnalysis = false;

    @Parameter(names= T_TIMEOUT_UNIT, description = "Timeout unit for each transformer per entrypoint in the UI analysis", required = false)
    public TimeUnit tTimeoutUnit = TimeUnit.MINUTES;

    @Parameter(names= T_TIMEOUT_VALUE, description = "Timeout value for each transformer per entrypoint in the UI analysis", required = false)
    public int tTimeoutValue = 1;

    @Parameter(names = LOAD_UI_RESULTS, description = "Skip UI phase. Load results from the previous run")
    public boolean loadUiResults = false;

    @Parameter(names = SAVE_JIMPLE, description = "Save jimple files to the sootOutput folder")
    public boolean saveJimple = false;

    @Parameter(names = TEST, description = "Indicates whether we run analysis for JUnit tests")
    public boolean isTest=false;

    @Parameter(names = LOGS_DIR, description = "Directory for logs")
    public String logsDir = "logs";

    @Parameter(names = RESULTS_DIR, description = "Directory for results")
    public String resultsDir = "results";

    @Parameter(names = NUM_THREADS, description = "Number of threads that will be used in an analysis")
    public int numThreads = Runtime.getRuntime().availableProcessors();

    @Parameter(names = MAX_LOC_IN_METHOD, description = "Skip methods with LOC > <value>")
    public int maxLocInMethod = Integer.MAX_VALUE;

    @Parameter(names = MAX_DEPTH_METHOD_LEVEL, description = "Maximum depth method level until " +
            "that an Analysis will search for Ui Elements and APIs")
    public int maxDepthMethodLevel = 15;

    @Parameter(names = PROCESS_IMAGES, description = "extract the images and icons of the application")
    public boolean process_images = false;

    @Parameter(names = NO_LANG, description = "Do not check the lang of the app")
    public Boolean noLang = false;

    @Parameter(names = CG_ALGO, description = "The algorithm for building a Call-Graph")
    public CallGraphAlgorithms cgAlgo = CallGraphAlgorithms.RTA;

    @Parameter(names = USE_LIFECYCLE, description = "Add Lifecycle methods to the Reachability Analysis")
    public Boolean RA_LIFECYCLE = false;

    @Parameter(names = LIMIT_BY_PACKAGE_NAME, description = "Limit reachability analysis to walk through methods only inside the same package name as an app has")
    public Boolean limitByPackageName = false;

    @Parameter(names = UI_MENUS, description = "Process Menus in the UI phase")
    public boolean processMenus = true;
}
