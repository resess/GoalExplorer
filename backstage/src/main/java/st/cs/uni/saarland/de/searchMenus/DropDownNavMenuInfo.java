package st.cs.uni.saarland.de.searchMenus;

import java.util.ArrayList;
import java.util.List;

import soot.SootMethod;
import st.cs.uni.saarland.de.entities.Listener;
import st.cs.uni.saarland.de.helpClasses.Info;
import st.cs.uni.saarland.de.helpMethods.StmtSwitchForArrayAdapter;
import st.cs.uni.saarland.de.searchDynDecStrings.DynDecStringInfo;

public class DropDownNavMenuInfo extends DynDecStringInfo {

	private String activityReg = "";
	private String activityName = "";
	private String actionBarReg = "";
	private String listenerReg = "";
//	private Map<String, Listener> listenerWRegs = new HashMap<String, Listener>();
	private List<Listener> listener;
//	private boolean setNavigationMode;
	private boolean setActionBarThere;
	private StmtSwitchForArrayAdapter arraySwitch;
//TODO include also stringBuilderSwitch?
	
	public DropDownNavMenuInfo(String actionBarReg, String listenerReg, String adapterReg, SootMethod currentSootMethod) {
//		String text, String stringID, String searchedEReg, String adapterReg){
		super("", currentSootMethod);
		this.actionBarReg = actionBarReg;
		this.listenerReg = listenerReg;
		setActionBarThere = false;
//		setNavigationMode = false;
		listener = new ArrayList<Listener>();
	}
	
//	public DropDownNavMenuInfo(String actionBarReg) {
////		String text, String stringID, String searchedEReg, String adapterReg){
//		super("");
//		this.actionBarReg = actionBarReg;
////		this.listenerReg = listenerReg;
//		setActionBarThere = false;
////		setNavigationMode = false;
//		listener = new ArrayList<Listener>();
//	}

	public String getActivityReg() {
		return activityReg;
	}

//	public Map<String, Listener> getListenerWRegs() {
//		return listenerWRegs;
//	}
//
//	public void addListenerWRegs(String register, Listener list) {
//		this.listenerWRegs.put(register, list);
//	}

	public StmtSwitchForArrayAdapter getArraySwitch() {
		return arraySwitch;
	}

	public void setArraySwitch(StmtSwitchForArrayAdapter arraySwitch) {
		this.arraySwitch = arraySwitch;
	}

	public void setActivityReg(String activityReg) {
		this.activityReg = activityReg;
	}


	public String getActivityName() {
		return activityName;
	}


	public void setActivityName(String activityName) {
		this.activityName = activityName;
	}


	public String getActionBarReg() {
		return actionBarReg;
	}


	public void setActionBarReg(String actionBarReg) {
		this.actionBarReg = actionBarReg;
	}


	public String getListenerReg() {
		return listenerReg;
	}


	public void setListenerReg(String listenerReg) {
		this.listenerReg = listenerReg;
	}


	public List<Listener> getListener() {
		return listener;
	}


	public void addListener(Listener listener) {
		this.listener.add(listener);
	}

	public void addListener(List<Listener> listener) {
		this.listener.addAll(listener);
	}
	
//	public boolean isSetNavigationMode() {
//		return setNavigationMode;
//	}
//
//
//	public void setNavigationMode() {
//		this.setNavigationMode = true;
//	}

	public void setActionBarThere() {
		this.setActionBarThere = true;
		
	}

	public boolean isSetActionBarThere() {
		return setActionBarThere;
	}

	@Override
	public String toString(){
		String res = super.toString();
		
		res = res + " actReg: " + activityReg + " ; actName: " + activityName + " ; listenerReg: " + listenerReg + " ;listener: " + listener ;
//		String a = "";
//		for (Map.Entry<String, Listener> entry : listenerWRegs.entrySet()){
//			a = a+ "; " + entry.getKey() + " " + entry.getValue().toString();
//		}
//		return res + a;
		return res;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ ((actionBarReg == null) ? 0 : actionBarReg.hashCode());
		result = prime * result
				+ ((activityName == null) ? 0 : activityName.hashCode());
		result = prime * result
				+ ((activityReg == null) ? 0 : activityReg.hashCode());
		result = prime * result
				+ ((arraySwitch == null) ? 0 : arraySwitch.hashCode());
		result = prime * result
				+ ((listener == null) ? 0 : listener.hashCode());
		result = prime * result
				+ ((listenerReg == null) ? 0 : listenerReg.hashCode());
		result = prime * result + (setActionBarThere ? 1231 : 1237);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		DropDownNavMenuInfo other = (DropDownNavMenuInfo) obj;
		if (actionBarReg == null) {
			if (other.actionBarReg != null)
				return false;
		} else if (!actionBarReg.equals(other.actionBarReg))
			return false;
		if (activityName == null) {
			if (other.activityName != null)
				return false;
		} else if (!activityName.equals(other.activityName))
			return false;
		if (activityReg == null) {
			if (other.activityReg != null)
				return false;
		} else if (!activityReg.equals(other.activityReg))
			return false;
		if (arraySwitch == null) {
			if (other.arraySwitch != null)
				return false;
		} else if (!arraySwitch.equals(other.arraySwitch))
			return false;
		if (listener == null) {
			if (other.listener != null)
				return false;
		} else if (!listener.equals(other.listener))
			return false;
		if (listenerReg == null) {
			if (other.listenerReg != null)
				return false;
		} else if (!listenerReg.equals(other.listenerReg))
			return false;
		if (setActionBarThere != other.setActionBarThere)
			return false;
		return true;
	}

	@Override
	public Info clone() {
		DropDownNavMenuInfo newInfo = new DropDownNavMenuInfo(actionBarReg, listenerReg, "", arraySwitch == null ? null : arraySwitch.getCurrentSootMethod());
		newInfo.activityName = activityName;
		newInfo.activityReg = activityReg;
		newInfo.listener = listener;
		if (isSetActionBarThere())
			newInfo.setActionBarThere();		
		newInfo.setText(text);
		newInfo.setTextReg(textReg);
		newInfo.setUiEID(uiEID);
		newInfo.setUiEIDReg(uiEIDReg);
		if (arraySwitch != null)
			newInfo.setArraySwitch(arraySwitch);
		return newInfo;
	}
	
	

}
