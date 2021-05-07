package st.cs.uni.saarland.de.searchMenus;

import soot.SootClass;
import st.cs.uni.saarland.de.helpClasses.Info;

public class MenuInfo extends Info {

	private String declaringSootClass = "";
	protected String layoutID ="";
	protected String layoutIDReg ="";
	protected String inflaterReg ="";
	protected String activityReg ="";
	protected String activityClassName ="";
	
	public MenuInfo(String reg, String layoutID, String inflaterReg, String declaringSootClass) {
		super(reg);
		this.layoutID = layoutID;
		this.inflaterReg = inflaterReg;
		this.declaringSootClass = declaringSootClass;
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
 
	@Override
	public String toString(){
		return layoutID + " " + inflaterReg +" " + activityReg +" " + activityClassName;
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

	@Override
	public Info clone() {
		MenuInfo newInfo = new MenuInfo(searchedEReg, layoutID, inflaterReg, declaringSootClass);
		newInfo.setActivityClassName(activityClassName);
		newInfo.setActivityReg(activityReg);
		newInfo.setLayoutIDReg(layoutIDReg);
		newInfo.setText(text);
		newInfo.setTextReg(textReg);
		return newInfo;
	}

	@Override
	public boolean allValuesFound() {
		return (!activityClassName.equals("") && activityReg.equals("") && layoutIDReg.equals("") && !layoutID.equals(""));
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
		result = prime * result
				+ ((layoutID == null) ? 0 : layoutID.hashCode());
		result = prime * result
				+ ((layoutIDReg == null) ? 0 : layoutIDReg.hashCode());
		result = prime * result + declaringSootClass.hashCode();
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
		if(!declaringSootClass.equals(other.declaringSootClass)){
			return false;
		}
		return true;
	}
	
	
}
