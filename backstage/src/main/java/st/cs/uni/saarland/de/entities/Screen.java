package st.cs.uni.saarland.de.entities;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

@Deprecated
public class Screen {

	//TODO add here hierarchy of the screen
	//TODO add sisconnectededToActicity
	private String activityName;
	private List<XMLLayoutFile> xmlFiles = new ArrayList<XMLLayoutFile>();
	private List<Dialog> dialogs = new ArrayList<Dialog>();
	private final int id; // unique id assigned to all screens from this program
//	private final Logger logger =  LoggerFactory.getLogger(Thread.currentThread().getName());
	
	public Screen(int id, String activityNameOfView) {
		this.id = id;
		activityName = activityNameOfView;
	}
	
	public Screen(List<XMLLayoutFile> layoutFiles, int id, String activityNameOfView){
		xmlFiles = layoutFiles;
		this.id = id;
		activityName = activityNameOfView;
	}
	
	public void addXmlFiles(XMLLayoutFile layoutFile) {
		this.xmlFiles.add(layoutFile);
	}


	// TODO rewrite
	public List<AppsUIElement> getUIElementsOfScreen(){
//		List<AppsUIElement> ret = new ArrayList<AppsUIElement>();
//		for (XMLLayoutFile f: xmlFiles){
//			ret.addAll(f.getUIElements());
//		}
////		ret.addAll(dialogs);
		return null;
	}
	
	public int getID(){
		return id;
	}


	public String getXMLLayoutFilesIDs(){
		String res = "";
		for (XMLLayoutFile f : xmlFiles){
			res = res + f.getId() + "; " ;
		}
		return res;
	}
	
	@Override
	public String toString(){
		String ret = "Screen: " + id + System.getProperty("line.separator");
		for (XMLLayoutFile xmlF : xmlFiles){
			ret = ret + xmlF.toString() + System.getProperty("line.separator");
		}
		for(Dialog dia: dialogs){
			ret = ret + dia.toString()+ System.getProperty("line.separator");
		}
		return ret;
	}

	@Deprecated
	// returns a string with all fields of this object
	public String attributesForSavingToString(String appName){
//		String ret = "";
//		for (XMLLayoutFile xmlF : xmlFiles){
//			ret = ret + System.getProperty("line.separator") + xmlF.attributesForSavingToString(appName + "---" + this.id);
//		}
//		for(Dialog dia: dialogs){
//			ret = System.getProperty("line.separator") + dia.attributesForSavingToString(ret);
//		}
		return null;
	}

	public String getActivityName() {
		return activityName;
	}

	public List<Dialog> getDialogs() {
		return dialogs;
	}

	public void addDialogs(Dialog dialog) {
		this.dialogs.add(dialog);
	}

	// returns a list of all listeners attached to this screen(including all elements, layouts)
	public Set<Listener> getListenersFromScreen(){
		Set<Listener> list = new HashSet<Listener>();
		for (AppsUIElement uiE: this.getUIElementsOfScreen()){
			for (Listener l: uiE.getListernersFromElement()){
				list.add(l);
			}
		}
		return list;
	}
}
