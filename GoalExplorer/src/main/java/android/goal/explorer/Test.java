package android.goal.explorer;

import android.goal.explorer.cmdline.GlobalConfig;
import soot.jimple.infoflow.android.InfoflowAndroidConfiguration;
import soot.jimple.infoflow.android.SetupApplication;
import org.pmw.tinylog.Logger;

import java.util.Set;

public class Test {
    public GlobalConfig config;


    public Test(GlobalConfig config){
        this.config = config;
        //this.setupApplication = new SetupApplication(config.getFlowdroidConfig());

    }

    public void run(){
        SetupApplication setupApplication;
        InfoflowAndroidConfiguration fConfig = config.getFlowdroidConfig();
        fConfig.setWriteOutputFiles(false);
        setupApplication = new SetupApplication(config.getFlowdroidConfig());
        setupApplication.constructCallgraph();
        Logger.debug("Done with callgraph construction");
    }

}
