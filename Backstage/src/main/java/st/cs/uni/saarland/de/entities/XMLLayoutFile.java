package st.cs.uni.saarland.de.entities;

import java.util.*;

import org.apache.commons.lang3.StringUtils;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

import com.google.gson.annotations.SerializedName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import st.cs.uni.saarland.de.helpClasses.Helper;
import st.cs.uni.saarland.de.testApps.Content;

public class XMLLayoutFile {

	// name of the xml layout file as extracted from the file name of the xml file,
	// and also the name variable which represents the id of this file
	protected String name = "";
	protected int id = 0; // id of this XMLLayoutFile, extracted from the public.xml file with the name; given in decimal
	@SerializedName("ui-elements")
	protected Set<Integer> elementIDs = new HashSet<Integer>(); // set of all element ids that are located inside this layout file
	// set of layoutIDs that are statical included into this layout (via include- or fragment tag)
	protected Set<Integer> staticIncludedLayoutIDs = new HashSet<Integer>();
	// set of layoutIDs that are dynamical included into this layout
	protected Set<Integer> dynamicIncludedLayoutIDs = new HashSet<Integer>();
	protected int rootElementID; // id of the root element of this layout (file)
	// indicates if this xml layout is included in some other layout, only set by static includes(include, fragment)
	protected boolean isIncludedSomeWhere = false;
	private final Logger logger =  LoggerFactory.getLogger(Thread.currentThread().getName());


	// called by constructor
	protected void setId(String pid) {
		// the id can never be overwritten
		if (this.id == 0){
			String[] tmp = pid.split("0x");
			if (tmp.length > 1) {
				int i = Integer.parseInt(tmp[1], 16);
				this.id = i;
			} else {
				this.id = Integer.parseInt(pid);
			}
		}else
			logger.error("someone tried to replace the id of an XMLLayoutFile in " + name);
	}

	// called by SpecialXMLTag constructor
	protected void setId(int id) {
		// the id can never be overwritten
		if (this.id == 0)
			this.id = id;
		else
			logger.error("someone tried to replace the id of an XMLLayoutFile in " + name);
	}

	// return true if the element with that id, is located inside this layout file (included layouts are not processed)
	public boolean containsAppsUIElementWithoutIncludings(String id) {
		if (elementIDs.contains(Integer.parseInt(id)))
			return true;
		else
			return false;
	}

	// return true if the element with that id, is located inside this layout or an included one
	public boolean containsAppsUIElementWithInclude(int id, Map<Integer, XMLLayoutFile> layoutFiles) {
		if (elementIDs.contains(id))
			return true;
		else{
			// check if it is contained in an static included layout
			for (int layoutID : staticIncludedLayoutIDs){
				if (layoutFiles.get(layoutID).containsAppsUIElementWithInclude(id, layoutFiles)){
					return true;
				}
			}
		}
		return false;
	}

	// return true if the element with that id, is located inside this layout or an included one
	public boolean containsAppsUIElementWithIncludeAndDynamicalInclude(int id, Map<Integer, XMLLayoutFile> layoutFiles) {
		if (elementIDs.contains(id))
			return true;
		else{
			// check if it is contained in an static included layout
			for (int layoutID : staticIncludedLayoutIDs){
				if (layoutFiles.get(layoutID).containsAppsUIElementWithIncludeAndDynamicalInclude(id, layoutFiles)){
					return true;
				}
			}
			// check if it is contained in an static included layout
			for (int layoutID : dynamicIncludedLayoutIDs){
				if (layoutFiles.get(layoutID).containsAppsUIElementWithIncludeAndDynamicalInclude(id, layoutFiles)){
					return true;
				}
			}
		}
		return false;
	}

	// get all Listener of this layout including the one attached to every child of this layout
	public Collection<Listener> getListenersOfElementsAndLayout(Map<Integer, AppsUIElement> uiElements) {
		List<Listener> list = new ArrayList<Listener>();
		for (int eID : elementIDs) {
			list.addAll(uiElements.get(eID).getListernersFromElement());
		}
		return list;
	}
	public boolean hasListenersOfElementsAndLayout(Map<Integer, XMLLayoutFile> xmlLayoutFiles, Map<Integer, AppsUIElement> uiElements){
		for (int eID : elementIDs){
			boolean res = uiElements.get(eID).hasListenerWithIncludedLayouts(xmlLayoutFiles, uiElements);
			if (res)
				return res;
		}
		return false;
	}

	// returns a list of listeners that are connected to the whole layout (see Menus), no included layouts!
	public Collection<Listener> getLayoutListeners() {
		return new ArrayList<Listener>();
	}
	public boolean hasLayoutListeners(){
		return false;
	}

	// included layouts are not considered
	public Collection<Listener> getElementListeners(Map<Integer, AppsUIElement> uiElements){
		Collection<Listener> listeners = new HashSet<Listener>();
		for (int eID : elementIDs){
			listeners.addAll(uiElements.get(eID).getListernersFromElement());
		}
		return listeners;
	}
	public boolean hasElementListeners(Map<Integer, AppsUIElement> uiElements){
		for (int eID : elementIDs){
			boolean res = uiElements.get(eID).hasElementListener();
			if (res)
				return res;
		}
		return false;
	}




	public Map<String, String> getTextFromLayoutWithoutIncluded(Map<Integer, AppsUIElement> uiElements) {
		Map<String, String> res = new HashMap<>();
		for (int eID : elementIDs) {
			AppsUIElement uiE = uiElements.get(eID);
			Map<String, String> textFromUiElem = uiE.getTextFromElement();
			textFromUiElem.keySet().forEach(k->{
				if(!res.containsKey(k)){
					res.put(k, textFromUiElem.get(k));
				}
				else{
					String currentValue = res.get(k);
					res.replace(k, currentValue + "#" + textFromUiElem.get(k));
				}
			});
		}
		return res;
	}

	@Override
	public String toString() {
		String ret = "XMLLayoutFile: " + name + "; " + id + System.getProperty("line.separator");
		for (int uiEid : elementIDs) {
			String id = String.valueOf(uiEid);
			if (id == null)
				id = "noIDFound";
			ret = ret + "UIElement id: " + id + System.getProperty("line.separator");
		}
		return ret;
	}


	public void setIncludedSomewhere(){
		isIncludedSomeWhere = true;
	}

	public String getName() {
		return name;
	}

	public int getId() {
		return id;
	}

	public int getRootElementID() {
		return rootElementID;
	}

	public void setRootElementID(int rootElementID) {
		this.rootElementID = rootElementID;
	}

	public void addElementIDs(int elementID) {
		this.elementIDs.add(elementID);
	}

	public Set<Integer> getStaticIncludedLayoutIDs() {
		return staticIncludedLayoutIDs;
	}

	public void addStaticIncludedLayoutIDs(int staticIncludedLayoutID) {
		this.staticIncludedLayoutIDs.add(staticIncludedLayoutID);
	}

	public void setName(String name) {
		if (StringUtils.isBlank(this.name)){
			this.name = name;
			this.id = Content.getInstance().getIdFromName(name, "layout");
			if (id== 0){
				Helper.saveToStatisticalFile("XMLLayoutFile:setName: couldn't find id of name: " + name);
			}
		}else{
			logger.error("someone tried to replace the name/id of the XMLLayoutFile: " + name);
		}
	}

	public void addUIElement(int uiElementID) {
		elementIDs.add(uiElementID);
	}

	public Set<Integer> getUIElementIDs() {
		return elementIDs;
	}

	public Collection<Integer> getLayoutIDsWithIncludeAndDynamicalInclude(Map<Integer, XMLLayoutFile> xmlLayoutFiles, Map<Integer, AppsUIElement> uiElements, Collection<Integer> processedUIElements){
		//Set<Integer> set = new HashSet<Integer>();
		processedUIElements.add(this.getId());
		for (int uiEID : elementIDs){
			if (!processedUIElements.contains(uiEID)) {
				AppsUIElement uiE = uiElements.get(uiEID);
				processedUIElements.addAll(uiE.getIncludedLayouts(xmlLayoutFiles, uiElements, processedUIElements));
			}
		}
		return processedUIElements;
	}

	protected Set<Integer> getDynamicIncludedLayoutIDs() {
		return dynamicIncludedLayoutIDs;
	}
}

// not including itself
// returns a list of ids of layouts that are included to this layout
//	public Set<String> getInSpecXMLTagConnectedXMLLayoutFileNames() {
//		throw new NotImplementedException("not maybe never implemented again");
//		Set<String> conClasses = new HashSet<String>();
//		for (AppsUIElement uiE : elements) {
//			if (uiE instanceof SpecialXMLTag) {
//				for (XMLLayoutFile xmlF : ((SpecialXMLTag) uiE).getXmlFiles()) {
//					String name = xmlF.getId();
//					conClasses.add(name);
//				}
//			}
//		}
//		return conClasses;
//	}