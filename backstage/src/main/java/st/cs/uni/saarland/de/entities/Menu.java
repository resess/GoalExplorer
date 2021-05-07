package st.cs.uni.saarland.de.entities;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import st.cs.uni.saarland.de.helpClasses.Helper;
import st.cs.uni.saarland.de.testApps.Content;

import java.util.*;

// Menu is a special XMLLayoutFile which either represents a menu that was found in code or
// was saved under the menu folder in the app's res folder
public class Menu extends XMLLayoutFile {

	// TODO think about this, why it is saved here but not in XMLLayoutFile
	// list of activities this Menu is assigned, eg this is an options menu from activity1
	List<String> assignedActivity = new ArrayList<String>();
	String kindOfMenu; // states which Menu this is: option, contextual, nav.-drop-down, popup or TabView.
	// assigned listeners to the layout (not to the elements of the layout): eg a menu listener for an options menu
	Set<Listener> listeners = new HashSet<Listener>();
	private final Logger logger =  LoggerFactory.getLogger(Thread.currentThread().getName());

	// constructor for creating a new completely free Menu object
	public Menu (int specialID){
		super();
		this.setId(specialID);
	}

	public Menu(){
		super();
	}

	// constructor for replacing an XMLayoutFile to an Menu object
	public Menu(XMLLayoutFile xmlF){
		super();
		this.setId(xmlF.getId());
		this.setName(xmlF.getName());
		elementIDs = xmlF.getUIElementIDs();
		this.isIncludedSomeWhere = xmlF.isIncludedSomeWhere;
		this.rootElementID = xmlF.getRootElementID();
		this.staticIncludedLayoutIDs = xmlF.getStaticIncludedLayoutIDs();
		this.dynamicIncludedLayoutIDs = xmlF.getDynamicIncludedLayoutIDs();
	}

	// returns a list of listeners that are connected to the whole layout (see Menus)
	@Override
	public Collection<Listener> getLayoutListeners(){
		return listeners;
	}
	@Override
	public boolean hasLayoutListeners(){
		return listeners.size() > 0? true:false;
	}

	// get all Listener of this layout including the one attached to every child of this layout
	@Override
	public Collection<Listener> getListenersOfElementsAndLayout(Map<Integer, AppsUIElement> uiElements) {
		Collection<Listener> list = super.getListenersOfElementsAndLayout(uiElements);
		list.addAll(listeners);
		return list;
	}
	@Override
	public boolean hasListenersOfElementsAndLayout(Map<Integer, XMLLayoutFile> xmlLayoutFiles, Map<Integer, AppsUIElement> uiElements){
		if (listeners.size() > 0)
			return true;
		else
			return super.hasListenersOfElementsAndLayout(xmlLayoutFiles, uiElements);
	}

	@Override
	public void setName(String pname) {
		if (StringUtils.isBlank(this.name)){
			this.name = pname;
			this.id = Content.getInstance().getIdFromName(pname, "menu");
			// if id==0 then id was not found
			if (id == 0){
				// this is for the layouts that were extended by a Menu object but were former a normal layout
				this.id = Content.getInstance().getIdFromName(pname, "layout");
				if (id == 0){
					Helper.saveToStatisticalFile("Menu:setName: couldn't find id of menu/layout: name: " + pname);
				}
			}
		}else{
			logger.error("someone tried to replace the name/id of the XMLLayoutFile: " + name);
		}
	}

	public List<String> getAssignedActivity() {
		return assignedActivity;
	}
	public void addAssignedActivity(String assignedActivity) {
		this.assignedActivity.add(assignedActivity);
	}
	public String getKindOfMenu() {
		return kindOfMenu;
	}
	public void setKindOfMenu(String kindOfMenu) {
		this.kindOfMenu = kindOfMenu;
	}

	public void addListener(Listener listener) {
		this.listeners.add(listener);
	}

	@Override
	public String toString(){
		String res = kindOfMenu + "Menu:" + super.toString();
		for (Listener l : listeners){
			res = res + " " + l;
		}
		res = res + System.getProperty("line.separator");
		return res;
	}

	public void addListeners(Collection<Listener> plistener) {
		for (Listener l : plistener){
			// FIXME listener list size is not correct
			if (l == null)
				continue;
			if(l.isCompleted())
				listeners.add(l);
		}
//		listeners.addAll(plistener);
	}

}
