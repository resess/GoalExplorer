package st.cs.uni.saarland.de.searchMenus;

import java.util.*;

import st.cs.uni.saarland.de.helpClasses.Info;

import st.cs.uni.saarland.de.reachabilityAnalysis.IntentInfo;
import st.cs.uni.saarland.de.testApps.Content;


public class MenuInfo extends Info {

	private String declaringSootClass = "";
	protected String layoutID ="";
	protected String layoutIDReg ="";
	protected String inflaterReg ="";
	protected String activityReg ="";
	protected String activityClassName ="";
	//keep a map of the intents? I guess
	//then map that to a target sootclass for a ui element?

	
	//ADDED
	protected boolean isXMLDeclared;
	//protected Map<String, AbstractMap.SimpleEntry<Integer, String>> dynMenuItems = new LinkedHashMap<>();
	protected Map<String, MenuItemInfo> dynMenuItems = new LinkedHashMap<>();
	//protected Map<String, String> dynMenuItemsTextReg = new HashMap<>();
	
	public MenuInfo(String reg, String layoutID, String inflaterReg, String declaringSootClass) {
		super(reg);
		this.layoutID = layoutID;
		this.inflaterReg = inflaterReg;
		this.declaringSootClass = declaringSootClass;
		this.isXMLDeclared = true;
	}

	public MenuInfo(String reg, String declaringSootClass){
		super(reg);
		this.layoutID = Integer.toString(Content.getNewUniqueID()); //generate unique layout id
		this.declaringSootClass = declaringSootClass;
		//assume activity name matches declaring sootClass
		this.activityClassName = declaringSootClass;
		this.isXMLDeclared = false;
	}

	public MenuInfo(){
		super("");
	}

	public String getDeclaringSootClass(){
		return this.declaringSootClass;
	}
	
	public String getLayoutID() {
		return layoutID;
	}

	public String getInflaterReg() {
		return inflaterReg;
	}

	public String getActivityReg() {
		return activityReg;
	}

	public void setActivityReg(String activityReg) {
		this.activityReg = activityReg;
	}

	public String getActivityClassName() {
		return activityClassName;
	}

	public void setActivityClassName(String activityClassName) {
		this.activityClassName = activityClassName;
	}

	public void setInflaterReg(String inflaterReg) {
		this.inflaterReg = inflaterReg;
	}

	//ADDED
	public boolean isXMLDeclared(){
		return this.isXMLDeclared;
	}

	//ADDED
	public void setIsXMLDeclared(boolean isXMLDeclared){
		this.isXMLDeclared = isXMLDeclared;
	}

	//ADDED
	public Map<String, MenuItemInfo> getDynMenus(){
		return this.dynMenuItems;
	}


	//ADDED
	public Map<String, MenuItemInfo> getDynMenuItems(){
		return this.dynMenuItems;
	}

	public Collection<MenuItemInfo> getMenuItemInfos(){
		return this.dynMenuItems.values();
	}
	
	//ADDED
	public Set<String> getDynMenuItemsReg(){
		return this.dynMenuItems.keySet();
	}

	//ADDED
	public MenuItemInfo getDynMenuItem(String reg){
		return this.dynMenuItems.get(reg);
	}

	//ADDED
	public Integer getDynMenuItemId(String reg){
		return getDynMenuItem(reg).getId();
	}

	//ADDED
	public String getDynMenuItemText(String reg){
		return getDynMenuItem(reg).getText();
	}

	public void addDynMenuItem(String menuItemReg, MenuItemInfo mInfo){
		/*if(menuItemReg.isEmpty()){
			int id = Content.getNewUniqueID();
			this.dynMenuItems.put(Integer.toString(id),mInfo);
		}
		else */
		this.dynMenuItems.put(menuItemReg, mInfo);
	}

	//ADDED
	public void addDynMenuItem(String menuItemReg, AbstractMap.SimpleEntry<Integer, String> menuItem){
		//if(menuItemReg.isEmpty()){
		addDynMenuItem(menuItemReg, menuItem.getKey(), menuItem.getValue());
		/*}
		else this.dynMenuItems.put(menuItemReg, menuItem);*/
	}

	//ADDED
	public void addDynMenuItem(String menuItemReg, Integer menuItemId, String text){
		if(menuItemReg.isEmpty()){
			int id = Content.getNewUniqueID();
			this.dynMenuItems.put(Integer.toString(id), new MenuItemInfo(searchedEReg, menuItemReg, menuItemId,text));
		}
		else {
			MenuItemInfo mInfo = dynMenuItems.get(menuItemReg);
			if(mInfo != null){
				mInfo.setId(menuItemId);
				mInfo.setText(text);
			}
			//this.dynMenuItems.put(menuItemReg, new AbstractMap.SimpleEntry(menuItemId, text));
		}
	}


	//ADDED
	public MenuItemInfo removeDynMenuItem(String menuItemReg){
		//dynMenuItems.get(menuItemReg).setIdReg("");
		return dynMenuItems.remove(menuItemReg);
	}


	public boolean hasMenuItem(String menuItemReg){
		return this.dynMenuItems.containsKey(menuItemReg);
	}

 
	@Override
	public String toString(){
		return layoutID + " " + inflaterReg +" " + activityReg +" " + activityClassName +" "+dynMenuItems+" static: "+isXMLDeclared + " activity: "+activityClassName;
	}



	public String getLayoutIDReg() {
		return layoutIDReg;
	}

	public void setLayoutIDReg(String layoutIDReg) {
		this.layoutIDReg = layoutIDReg;
	}

	public void setLayoutID(String layoutID) {
		this.layoutID = layoutID;
	}

//	@Override
//	public boolean shouldRunOnInitMethod() {
//		if ((!layoutIDReg.equals("")) || (!activityReg.equals("")))
//			return true;
//		else 
//			return false;
//	}

	public void mergeWith(MenuInfo other){
		if(other.dynMenuItems != null)
			dynMenuItems.putAll(other.dynMenuItems);
	}

	@Override
	public Info clone() {
		MenuInfo newInfo = new MenuInfo(searchedEReg, layoutID, inflaterReg, declaringSootClass);
		newInfo.setActivityClassName(activityClassName);
		newInfo.setActivityReg(activityReg);
		newInfo.setLayoutIDReg(layoutIDReg);
		newInfo.setText(text);
		newInfo.setTextReg(textReg);
		newInfo.setIsXMLDeclared(isXMLDeclared);
		this.dynMenuItems.forEach( (reg, id) -> newInfo.addDynMenuItem(reg, id));
		return newInfo;
	}

	@Override
	public boolean allValuesFound() {
		boolean allValuesFound = dynMenuItems.isEmpty() ? true : dynMenuItems.values().stream().allMatch(item -> item.allValuesFound());
		return (!activityClassName.equals("") && activityReg.equals("") && layoutIDReg.equals("") && !layoutID.equals("") && allValuesFound);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime
				* result
				+ ((activityClassName == null) ? 0 : activityClassName
						.hashCode());
		result = prime * result
				+ ((activityReg == null) ? 0 : activityReg.hashCode());
		result = prime * result
				+ ((inflaterReg == null) ? 0 : inflaterReg.hashCode());
		if(isXMLDeclared) {
			result = prime * result
					+ ((layoutID == null) ? 0 : layoutID.hashCode());
			result = prime * result
					+ ((layoutIDReg == null) ? 0 : layoutIDReg.hashCode());
		}
		else result = prime * result + dynMenuItems.hashCode();
		result = prime * result + declaringSootClass.hashCode();
		return result;
	}

	//Here need to compare the texts to figure out if same menu?
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		MenuInfo other = (MenuInfo) obj;
		if (activityClassName == null) {
			if (other.activityClassName != null)
				return false;
		} else if (!activityClassName.equals(other.activityClassName))
			return false;
		if (activityReg == null) {
			if (other.activityReg != null)
				return false;
		} else if (!activityReg.equals(other.activityReg))
			return false;
		if (inflaterReg == null) {
			if (other.inflaterReg != null)
				return false;
		} else if (!inflaterReg.equals(other.inflaterReg))
			return false;
		if(isXMLDeclared) {
			if (layoutID == null) {
				if (other.layoutID != null)
					return false;
			} else if (!layoutID.equals(other.layoutID))
				return false;
			if (layoutIDReg == null) {
				if (other.layoutIDReg != null)
					return false;
			} else if (!layoutIDReg.equals(other.layoutIDReg))
				return false;
		}
		else {
			if(!dynMenuItems.equals(other.dynMenuItems))
				return false;
		}
		if(!declaringSootClass.equals(other.declaringSootClass)){
			return false;
		}
		return true;
	}
	
	
}
