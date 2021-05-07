package st.cs.uni.saarland.de.searchMenus;

import java.util.HashMap;
import java.util.Map;

import st.cs.uni.saarland.de.entities.Listener;
import st.cs.uni.saarland.de.helpClasses.Info;

public class PopupMenuInfo extends MenuInfo {

	// searchedElementReg is PopupMenu register
	
	private Map<String, Listener> listenerWRegs = new HashMap<String, Listener>();
	private String showingItemReg;
	private String showingItemID = "";
	private String layoutIDReg = "";
	
	public PopupMenuInfo(String layoutID, String inflaterReg, String declaringSootClass) {
		super("", layoutID, inflaterReg, declaringSootClass);
	}
	
//	constructor if "show()" is included in analysis
//	public PopupMenuInfo(String itemReg) {
//		super("", "", "");
//		this.showingItemReg = itemReg;
//	}

	public boolean isPopupMenu(){
		if ((activityReg != null) && (inflaterReg.equals(""))){
			return true;
		}else
			return false;
	}
	
	public Map<String, Listener> getListenerWRegs() {
		return listenerWRegs;
	}

	public void addListenerWRegs(String register, Listener list) {
		this.listenerWRegs.put(register, list);
	}

	public String getShowingItemReg() {
		return showingItemReg;
	}

	public void setShowingItemReg(String showingItemReg) {
		this.showingItemReg = showingItemReg;
	}

	public String getShowingItemID() {
		return showingItemID;
	}

	public void setShowingItemID(String showingItemID) {
		this.showingItemID = showingItemID;
	}
	
	public String getLayoutIDReg() {
		return layoutIDReg;
	}

	public void setLayoutIDReg(String layoutIDReg) {
		this.layoutIDReg = layoutIDReg;
	}

	@Override
	public String toString(){
		String res =  layoutID + " pme:" + searchedEReg + " inf:" + inflaterReg +" actReg:" + activityReg +" actName:" + activityClassName + " showItemReg:" + showingItemReg + " showItemID:" + showingItemID + " ";
		String a = "";
		for (Map.Entry<String, Listener> entry : listenerWRegs.entrySet()){
			a = a+ "; " + entry.getKey() + " " + entry.getValue().toString();
		}
		return res + a;
	}
	
	//only for using in clone!
	private void setListener( Map<String, Listener> listenerWRegs){
		this.listenerWRegs = listenerWRegs;
	}
	
//	@Override
//	public boolean shouldRunOnInitMethod() {
//		boolean first = false;
//		boolean second = false;
//		if (!layoutIDReg.equals(""))
//			first = false;
//		for (Entry<String, Listener> entry : listenerWRegs.entrySet()){
//			// if the reg of the listener was found, the reg string is assigned to 
//				// hash(Listener) which should have more than 4 chars
//			if (!(entry.getKey().length() > 4)){
//				second = true;
//			}
//		}
//		if (first || second)
//			return true;
//		else 
//			return false;
//	}

	@Override
	public Info clone() {
		PopupMenuInfo newInfo = new PopupMenuInfo(layoutID, inflaterReg, getDeclaringSootClass());
		newInfo.setShowingItemID(showingItemID);
		newInfo.setShowingItemReg(showingItemReg);
		newInfo.setLayoutIDReg(layoutIDReg);
		newInfo.setListener(listenerWRegs);
		return newInfo;
	}

	@Override
	public boolean allValuesFound() {
		return (super.allValuesFound() && !showingItemID.equals("") && showingItemReg.equals("") && layoutIDReg.equals("") && listenerWRegs.entrySet().stream().allMatch(x->x.getValue().isCompleted()));
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ ((layoutIDReg == null) ? 0 : layoutIDReg.hashCode());
		result = prime * result
				+ ((listenerWRegs == null) ? 0 : listenerWRegs.hashCode());
		result = prime * result
				+ ((showingItemID == null) ? 0 : showingItemID.hashCode());
		result = prime * result
				+ ((showingItemReg == null) ? 0 : showingItemReg.hashCode());
		result = prime * result + getDeclaringSootClass().hashCode();
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
		PopupMenuInfo other = (PopupMenuInfo) obj;
		if (layoutIDReg == null) {
			if (other.layoutIDReg != null)
				return false;
		} else if (!layoutIDReg.equals(other.layoutIDReg))
			return false;
		if (listenerWRegs == null) {
			if (other.listenerWRegs != null)
				return false;
		} else if (!listenerWRegs.equals(other.listenerWRegs))
			return false;
		if (showingItemID == null) {
			if (other.showingItemID != null)
				return false;
		} else if (!showingItemID.equals(other.showingItemID))
			return false;
		if (showingItemReg == null) {
			if (other.showingItemReg != null)
				return false;
		} else if (!showingItemReg.equals(other.showingItemReg))
			return false;
		if(!getDeclaringSootClass().equals(other.getDeclaringSootClass())){
			return false;
		}
		return true;
	}



}
