package st.cs.uni.saarland.de.anomalyValidation;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import soot.PackManager;
import soot.Transform;
import soot.options.Options;
import st.cs.uni.saarland.de.testApps.TestApp;

/**
 * Created by avdiienko on 04/04/16.
 */
public class WebViewTextExtractor {

    public static void main(String[] args){
        InstrumenterSettings settings = new InstrumenterSettings();
        JCommander jc = new JCommander(settings);

        try {
            jc.parse(args);
        }
        catch (ParameterException e){
            System.err.println(e.getMessage());
            jc.usage();
            System.exit(1);
        }
        TestApp.initializeSootForUiAnalysis(settings.apkPath, settings.androidJar, false, false);
        Options.v().set_output_format(Options.output_format_none);
        String[] splitted = settings.apkPath.split("\\/");
        String apkName = splitted[splitted.length-1];
        PackManager.v().getPack("jtp").add(new Transform("jtp.myInstrumenter", new WebViewTransformer(apkName)));
        PackManager.v().runPacks();
    }
}
