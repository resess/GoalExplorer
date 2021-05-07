package st.cs.uni.saarland.de.saveData;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import com.thoughtworks.xstream.XStream;
import st.cs.uni.saarland.de.reachabilityAnalysis.ApiInfoForForward;
import st.cs.uni.saarland.de.reachabilityAnalysis.UiElement;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by avdiienko on 23/12/15.
 */
public class ResultsComparator {
    private static org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(ResultsComparator.class);

    public static void main(String[] args){
        ResultsComparatorSettings settings = new ResultsComparatorSettings();
        JCommander jc = new JCommander(settings);
        try {
            jc.parse(args);
        }
        catch (ParameterException e){
            System.err.println(e.getMessage());
            jc.usage();
            System.exit(1);
        }

        Map<String, MSRApiRepresentation> resultsOfFirstItem =  processXMLFile(new File(settings.firstItem));
        Map<String, MSRApiRepresentation> resultsOfSecondItem =  processXMLFile(new File(settings.secondItem));

        if(resultsOfFirstItem.equals(resultsOfSecondItem)){
            logger.info("Files are equal");
        }
        else{
            logger.info("Files are not equal");
        }

    }

    private static Map<String, MSRApiRepresentation> processXMLFile(File xmlFile){
        Map<String, MSRApiRepresentation> results = new HashMap<>();

        MSRAppInfo appInfo = new MSRAppInfo();
        appInfo.apkName = xmlFile.getName().replace("_forward_apiResults.xml", "");
        appInfo.malicious = true;
        HashMap<String, MSRApiRepresentation> apisInsideApp = new HashMap<>();


        XStream xStream = new XStream();
        xStream.processAnnotations(UiElement.class);
        xStream.processAnnotations(ApiInfoForForward.class);
        xStream.setMode(XStream.NO_REFERENCES);
        Map<UiElement, List<ApiInfoForForward>> resultsOfApp = (Map<UiElement, List<ApiInfoForForward>>) xStream.fromXML(xmlFile);

        resultsOfApp.keySet().forEach(uiElem-> {
            final boolean isUICallback = uiElem.elementId != null && !(uiElem.elementId.equals("android.app.Service") || uiElem.elementId.equals("android.content.BroadcastReceiver"));

            resultsOfApp.get(uiElem).forEach(apiElem -> {
                MSRApiRepresentation apiForApk = new MSRApiRepresentation();
                if(isUICallback){
                    apiForApk.uiCallbacks++;
                }
                else{
                    apiForApk.nonUiCallbacks++;
                }
                if(!results.containsKey(apiElem.signature)) {
                    results.put(apiElem.signature, apiForApk);
                }
                else{
                   MSRApiRepresentation fromObj = results.get(apiElem.signature);
                    fromObj.nonUiCallbacks+=apiForApk.nonUiCallbacks;
                    fromObj.uiCallbacks+=apiForApk.uiCallbacks;
                }
            });

        });
        return results;
    }
}
