package st.cs.uni.saarland.de.searchPreferences;

import st.cs.uni.saarland.de.helpClasses.Info;

public class PreferenceInfo extends Info{
    private String activityName = "";
    private String layoutID = "";
    private String layoutIDReg = "";
    private String declaringSootClassName = "";
    //activity name, xml id, ?

    public PreferenceInfo(String reg){
        super(reg);
    }

    public String toString(){
        return "Preferences: activity: "+activityName+" layoutId: "+layoutID;
    }

    public String getLayoutID(){
        return layoutID;
    }

    public String getLayoutIDReg() {
        return layoutIDReg;
    }

    public String getActivityName(){
        return activityName;
    }

    public String getDeclaringSootClassName(){
        return declaringSootClassName;
    }

    public void setLayoutID(String layoutID) {
        this.layoutID = layoutID;
    }

    public void setLayoutIDReg(String layoutIDReg) {
        this.layoutIDReg = layoutIDReg;
    }

    public void setActivityName(String activityName) {
        this.activityName = activityName;
    }

    public void setDeclaringSootClass(String declaringSootClassName){
        this.declaringSootClassName = declaringSootClassName;
    }

    public boolean hasLayoutID(){
        return !layoutID.isEmpty();
    }

    public boolean hasActivityName() {
        return !activityName.isEmpty();
    }

    @Override
    public boolean allValuesFound() {
        return false;
    }

    public PreferenceInfo clone(){
        PreferenceInfo newInfo = new PreferenceInfo(searchedEReg);
        this.setLayoutID(layoutID);
        this.setLayoutIDReg(layoutIDReg);
        this.setActivityName(activityName);
        this.setDeclaringSootClass(declaringSootClassName);
        return newInfo;
    }

    /*public boolean equals(Object obj) {
        if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
        PreferenceInfo other = (PreferenceInfo)obj;
        return other.layoutID 
    }*/
}
