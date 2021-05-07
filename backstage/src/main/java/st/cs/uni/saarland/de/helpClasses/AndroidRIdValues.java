package st.cs.uni.saarland.de.helpClasses;

import java.util.HashMap;
import java.util.Map;

public final class AndroidRIdValues {

//	@Deprecated
//	private static Map<String, String> ids = new HashMap<String, String>(){{
//		put("addToDictionary","16908330");
//		put("background","16908288");
//		put("button1","16908313");
//		put("button2","16908314");
//		put("button3","16908315");
//		put("candidatesArea","16908317");
//		put("checkbox","16908289");
//		put("closeButton","16908327");
//		put("content","16908290");
//		put("copy", "16908321");
//		put("copyUrl","16908323");
//		put("custom", "16908331");
//		put("cut", "16908320");
//		put("edit","16908291");
//		put("empty","16908292");
//		put("extractArea", "16908316");
//		put("hint","16908293");
//		put("home","16908332");
//		put("icon","16908294");
//		put("icon1","16908295");
//		put("icon2","16908296");
//		put("input","16908297");
//		put("inputArea","16908318");
//		put("inputExtractEditText","16908325");
//		put("keyboardView","16908326");
//		put("list","16908298");
//		put("mask","16908334");
//		put("message","16908299");
//		put("navigationBarBackground","16908336");
//		put("paste","16908322");
//		put("primary","16908300");
//		put("progress","16908301");
//		put("secondaryProgress","16908303");
//		put("selectAll","16908319");
//		put("selectTextMode","16908333");
//		put("selectedIcon","16908302");
//		put("startSelectingText","16908328");
//		put("statusBarBackground","16908335");
//		put("stopSelectingText","16908329");
//		put("summary","16908304");
//		put("switchInputMethod","16908324");
//		put("tabcontent","16908305");
//		put("tabhost","16908306");
//		put("tabs","16908307");
//		put("text1","16908308");
//		put("text2","16908309");
//		put("title","16908310");
//		put("toggle","16908311");
//		put("widget_frame","16908312");
//
//}};

//	public static String getID(String idVariableName){
//		return ids.get(idVariableName);
//	}

	public static int getAndroidID(String idVariableName){
		if (androidIds.get(idVariableName) == null) {
			return -1;
		} else return androidIds.get(idVariableName);
	}

	private static Map<String, Integer> androidIds = new HashMap<String, Integer>(){{
		put("addToDictionary",16908330);
		put("autofill",16908355);
		put("background",16908288);
		put("button1",16908313);
		put("button2",16908314);
		put("button3",16908315);
		put("candidatesArea",16908317);
		put("checkbox",16908289);
		put("closeButton",16908327);
		put("content",16908290);
		put("copy", 16908321);
		put("copyUrl",16908323);
		put("custom", 16908331);
		put("cut",16908320);
		put("edit",16908291);
		put("empty",16908292);
		put("extractArea", 16908316);
		put("hint",16908293);
		put("home",16908332);
		put("icon",16908294);
		put("icon1",16908295);
		put("icon2",16908296);
		put("icon_frame",16908350);
		put("input",16908297);
		put("inputArea",16908318);
		put("inputExtractEditText",16908325);
		put("keyboardView",16908326);
		put("list",16908298);
		put("list_container",16908298);
		put("mask",16908334);
		put("message",16908299);
		put("navigationBarBackground",16908336);
		put("paste",16908322);
		put("pasteAsPlainText",16908337);
		put("primary",16908300);
		put("progress",16908301);
		put("redo",16908339);
		put("replaceText",16908340);
		put("secondaryProgress",16908303);
		put("selectAll",16908319);
		put("selectTextMode",16908333);
		put("selectedIcon",16908302);
		put("shareText",16908341);
		put("startSelectingText",16908328);
		put("statusBarBackground",16908335);
		put("stopSelectingText",16908329);
		put("summary",16908304);
		put("switchInputMethod",16908324);
		put("tabcontent",16908305);
		put("tabhost",16908306);
		put("tabs",16908307);
		put("text1",16908308);
		put("text2",16908309);
		put("textAssist",16908353);
		put("title",16908310);
		put("toggle",16908311);
		put("undo",16908338);
		put("widget_frame",16908312);

		}};


}
