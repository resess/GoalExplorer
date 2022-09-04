package st.cs.uni.saarland.de.testApps;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import st.cs.uni.saarland.de.entities.Style;
import st.cs.uni.saarland.de.helpClasses.Helper;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;


public class Content {
	
	private static final Logger logger =  LoggerFactory.getLogger(Thread.currentThread().getName());
	
	private static Content content;

	private static AtomicInteger idCounter = new AtomicInteger(100000);
	
	private Map<String, Integer> nameToIds; // Map<type:name, id>
	private Map<Integer, String> idToName; // Map<id, name>
	private Map<String, String> stringNameToValue; // Map<StringID, StringValue>
	private Map<String, String> arrayNameToArrayValue; 
	private Map<String, String> stringDefaultIdToValue;
	private Map<String, Style> styleNameToStyle; // Map<style name, style>

	
	String appOutputDir;

	
	
	public Content(String appOutputDir){
		this.appOutputDir = appOutputDir;
		initStringDefaultIdToValueMap();
		Content.content=this;
	}

	public static Content getInstance(String appOutputDir){
		if (null==content){
			content = new Content(appOutputDir);
		}
		return content;
	}
	public static Content getInstance(){
		if (null==content){
			logger.error("Content not initialized");
		}
		return content;
	}

	// first ID starts at 10.000.001 -> 8 digits; Android IDs are always 6 digits
	public static int getNewUniqueID(){
		// first increment, then the new value is returned
		return idCounter.getAndIncrement();
	}

	public static void updateIDCounter(int newId){
		idCounter.set(newId);
	}

	public String getAppOutputDir() {
		return appOutputDir;
	}
	
	public String getStringValueFromStringId(String id){
		String res = "";
		int idInt = Integer.parseInt(id);
		String strName = idToName.get(idInt);
		if (!StringUtils.isBlank(strName)) {
			if (stringNameToValue.containsKey(strName)) 
				res = stringNameToValue.get(strName);

		}
		if(StringUtils.isBlank(res)){
			res = stringDefaultIdToValue.get(id);
		}
		if(!StringUtils.isBlank(res))
			return res;
		else{
			Helper.saveToStatisticalFile("Did not find string name from string id: id: " + id);
			return "";
		}
	}

	// <type:name, id>
	public void setNameToIds(Map<String, Integer> nameToIds) {
		this.nameToIds = nameToIds;
		
		idToName = new HashMap<Integer, String>();
		for (Entry<String, Integer> entry : nameToIds.entrySet()){
			idToName.put(entry.getValue(), entry.getKey().split(":")[1]);
		}
	}

	// returns 0 if the name with the type was not found
	public int getIdFromName(String name, String type) {
		String tmp = type+":"+name;
		try {
			int res = nameToIds.get(tmp);
			return res;
		}catch(NullPointerException e){
			return 0;
		}
	}

	public String getStringValueFromStringName(String stringName) {
		String res = stringNameToValue.get(stringName);
		if (StringUtils.isBlank(res) || res.equals("null"))
			return "";
		else
			return res;
	}

	public void setStringNameToValue(Map<String, String> stringNameToValue) {
		this.stringNameToValue = stringNameToValue;
	}
	
	public String getArrayValueFromArrayID(String arrayID){
		int arrayIDAsInt = Integer.parseInt(arrayID);
		String arrayName = idToName.get(arrayIDAsInt);
		try {
			String res = arrayNameToArrayValue.get(arrayName);
			return res;
		}catch(NullPointerException e){
			return null;
		}
	}
	
	public void setArrayNameToArrayValue(Map<String, String> arrayNameToArrayValue) {
		if (this.arrayNameToArrayValue == null)
			this.arrayNameToArrayValue = arrayNameToArrayValue;
		else
			Helper.saveToStatisticalFile("Content: ArrayNameToValue map was tried to be replaced");
	}

	public void setStyleNameToStyle(Map<String, Style> styleNameToStyle) {
		if (this.styleNameToStyle == null)
			this.styleNameToStyle = styleNameToStyle;
		else
			Helper.saveToStatisticalFile("Content: Style map was tried to be replaced");
	}

	public Style getStyleFromName(String name){
		try {
			return styleNameToStyle.get(name);
		}catch(NullPointerException e){
			return null;
		}

	}

	private void initStringDefaultIdToValueMap(){
		stringDefaultIdToValue = new HashMap<>();
		stringDefaultIdToValue.put("17039376","OK");
		stringDefaultIdToValue.put("17039381","This video isn't valid for streaming to this device.");
		stringDefaultIdToValue.put("17039377","Can't play this video.");
		stringDefaultIdToValue.put("17039378","Video problem");
		stringDefaultIdToValue.put("17039360","Cancel");
		stringDefaultIdToValue.put("17039361","Copy");
		stringDefaultIdToValue.put("17039362","Copy URL");
		stringDefaultIdToValue.put("17039363","Cut");
		stringDefaultIdToValue.put("17039365","MSISDN1");
		stringDefaultIdToValue.put("17039364","Voicemail");
		stringDefaultIdToValue.put("17039380","Attention");
		stringDefaultIdToValue.put("17039366","(No phone number)");
		stringDefaultIdToValue.put("17039367","Couldn't open the page because the URL is invalid.");
		stringDefaultIdToValue.put("17039368","The protocol isn't supported.");
		stringDefaultIdToValue.put("17039369","Cancel");
		stringDefaultIdToValue.put("17039370","OK");
		stringDefaultIdToValue.put("17039371","Paste");
		stringDefaultIdToValue.put("17039372","Search");
		stringDefaultIdToValue.put("17039373","Select all");
		stringDefaultIdToValue.put("17039382","Select text");
		stringDefaultIdToValue.put("17039383","999+");
		stringDefaultIdToValue.put("17039374","(Unknown)");
		stringDefaultIdToValue.put("17039375","<Untitled>");
		stringDefaultIdToValue.put("17039379","OK");
	}
}
