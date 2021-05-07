package android.goal.explorer;

import android.goal.explorer.cmdline.GlobalConfig;
import android.goal.explorer.model.stg.output.OutSTG;
import com.thoughtworks.xstream.XStream;
import org.pmw.tinylog.Logger;
import st.cs.uni.saarland.de.helpClasses.Helper;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class SaveData {

    private OutSTG stg;
    private GlobalConfig config;

    public SaveData(OutSTG stg, GlobalConfig config) {
        this.stg = stg;
        this.config = config;
    }

    public void saveSTG(){
        XStream xStream = new XStream();
        xStream.setMode(XStream.NO_REFERENCES);
        xStream.autodetectAnnotations(true);
        String outputDir = config.getFlowdroidConfig().getAnalysisFileConfig().getOutputFile();
        String outputFile = outputDir + File.separator + Helper.getApkName()
                .replace(".apk", "") + "_stg.xml";
        Logger.info("Writing output to " + outputFile);
        if (new File(outputFile).exists()) {
            new File(outputFile).delete();
        }
        String xmlString = xStream.toXML(stg);
        try {
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile)));
            writer.append(xmlString);
            writer.close();
        } catch (IOException ex) {
            Logger.error("[ERROR] Failed to save STG to {}", outputDir);
        }
    }
}
