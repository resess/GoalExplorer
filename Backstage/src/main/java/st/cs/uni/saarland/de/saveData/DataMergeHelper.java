package st.cs.uni.saarland.de.saveData;

import android.content.DialogInterface;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import com.thoughtworks.xstream.XStream;
import org.apache.commons.lang3.StringUtils;
import st.cs.uni.saarland.de.entities.*;
import st.cs.uni.saarland.de.reachabilityAnalysis.ApiInfoForForward;
import st.cs.uni.saarland.de.reachabilityAnalysis.ReachabilityAnalysis;
import st.cs.uni.saarland.de.reachabilityAnalysis.UiElement;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class DataMergeHelper {
	private boolean useLayouts;
	private Application app;
	private Map<UiElement, List<ApiInfoForForward>> apiData;

	public DataMergeHelper(File apiFile, File uiFile) {
		XStream xstreamI = new XStream();
		xstreamI.alias("AppsUIElement", AppsUIElement.class);
		xstreamI.setMode(XStream.ID_REFERENCES);
		this.app = (Application) xstreamI.fromXML(uiFile);
		XStream xstreamV = new XStream();
		xstreamV.processAnnotations(UiElement.class);
		xstreamV.processAnnotations(ApiInfoForForward.class);
		xstreamV.setMode(XStream.NO_REFERENCES);
		this.apiData = (Map<UiElement, List<ApiInfoForForward>>) xstreamV.fromXML(apiFile);
	}
	
	private boolean isIdInDialogsId(String id){
		return id.equals(Integer.toString(DialogInterface.BUTTON_POSITIVE)) || id.equals(Integer.toString(DialogInterface.BUTTON_NEGATIVE)) || id.equals(Integer.toString(DialogInterface.BUTTON_NEUTRAL));
	}

	public void process(File apiFile, String resultsFolder) throws IOException {
		//FIXME
			/*List<String> results = new ArrayList<String>();
			String appName = apiFile.getName().replace(".apk_forward_apiResults.xml", "");
			Path output = Paths.get(resultsFolder, appName + "_apiToUI.txt");
			Files.deleteIfExists(output);
			for (UiElement ui : apiData.keySet()) {
				Set<String> layouts = ui.layoutIds;
				final String elId = ui.elementId;
				final int eId = Integer.parseInt(ui.elementId);
				String id="";
				String text = "";
				String[] splittedSignature = ui.signature.replace("<", "").replace(">", "").split(":");
				if(StringUtils.isBlank(elId) || (!layouts.isEmpty() && isIdInDialogsId(elId))){
					//search only for alert dialogs
					List<String> textFromDia = new ArrayList<>();
					for(String layoutId : layouts){
						if(StringUtils.isEmpty(layoutId) || !Character.isDigit(layoutId.charAt(0)))
							continue;
						try{
							XMLLayoutFile xmlLayFile = app.getXmlLayoutFile(Integer.parseInt(layoutId));
							if(xmlLayFile instanceof Dialog){
								Dialog dia = (Dialog)xmlLayFile;
								Listener listener = new Listener("", false, splittedSignature[1].trim());
								listener.setListenerClass(splittedSignature[0].trim());
								// TODO CHECK! really elID? inside the getInacitveT... method this elID is only check for -1 -> -3
								String candidate = dia.getInactiveTextBasedOnActiveListener(listener, StringUtils.isBlank(elId) ? 0 : Integer.parseInt(elId));
								if(!StringUtils.isEmpty(candidate)){
									textFromDia.add(candidate);
								}
								id = "dialog_"+UUID.randomUUID();
							}
							else if(useLayouts){
								// TODO changed to sth which is not what there should be -> xmlF.getInactiveText(..)
								String layoutInactive = xmlLayFile.getTextFromLayoutWithoutIncluded(app.getUIElementsMap());
								if(!StringUtils.isEmpty(layoutInactive)){
									textFromDia.add(layoutInactive);
								}
								id = layoutId;
							}
						}
						catch(Exception e){
							continue;
						}
					}
					text = StringUtils.join(textFromDia, "#").replaceAll("(\\r|\\n)", " ");
				}
				else{
					id=elId;
					if(ReachabilityAnalysis.ANDROID_APP_SERVICE.equals(id) || ReachabilityAnalysis.ANDROID_CONTENT_BROADCAST_RECEIVER.equals(id)){
						continue;
					}
					List<String> tList = new ArrayList<String>();
					tList.add(app.getUiElement(eId).getTextFromElement().replaceAll("(\\r|\\n)", " "));
					
					for(XMLLayoutFile xmlFile : app.getAllXMLLayoutFiles()){
						if(xmlFile.containsAppsUIElementWithInclude(eId, app.getXMLLayoutFilesMap())){
							// TODO changed to sth which is not what there should be -> xmlF.getInactiveText(..)
							tList.add(xmlFile.getTextFromLayoutWithoutIncluded(app.getUIElementsMap()).replaceAll("(\\r|\\n)", " "));
						}
					}
					tList.remove("");
					text = StringUtils.join(tList, "#").replaceAll("(\\r|\\n)", " ");
				}
				if(StringUtils.isEmpty(text)){
					continue;
				}
				List<ApiInfoForForward> apiList = apiData.get(ui);
				for (ApiInfoForForward apiInfo : apiList) {
					String signature = apiInfo.signature;
					// get additional info
					List<String> info = apiInfo.additionalInfo;
					for (String param : info) {
						if (param.contains("ACTION") || param.contains("content://")) {
							signature = param.replace("ACTION: ", "");// FIXME hardcoded
							break;
						}
					}
					String txtToWrite = text.replace("_", " ");
					if(txtToWrite.replace("#", "").trim().length() > 0){
						results.add(appName+"_"+id + ";\"" + signature + "\";\"" + txtToWrite + "\"");
					}
				}
			}

			if (results.isEmpty())
				System.out.println("WARNING no text found for " + appName);
			else
				Files.write(output, results);*/
	}

	public static void main(String[] args) throws IOException {
		DataMergerHelperSettings dmSettings = new DataMergerHelperSettings();
		JCommander jc = new JCommander(dmSettings);
		try {
			jc.parse(args);
		}
		catch (ParameterException e){
			System.err.println(e.getMessage());
			jc.usage();
			System.exit(1);
		}
		File apiFile = new File(dmSettings.xmlPath);
		File uiFile = new File(dmSettings.serializedObjectPath);
		if (!apiFile.exists())
			throw new FileNotFoundException("invalid arguments provided; file not found; " + apiFile.toString());
		if (!uiFile.exists())
			throw new FileNotFoundException("invalid arguments provided; file not found; " + uiFile.toString());
		DataMergeHelper dm = new DataMergeHelper(apiFile, uiFile);
		if (dmSettings.wholeLayout) {
			dm.useLayouts = true;
			System.out.println("Collecting inactive text from layouts");
		}
		String appName = apiFile.getName().replace(".apk_forward_apiResults.xml", "");
		System.out.println("Processing " + appName);
		dm.process(apiFile, dmSettings.outputFolderPath);
		System.out.println("Finished " + appName);
	}
}
