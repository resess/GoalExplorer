package st.cs.uni.saarland.de.dissolveSpecXMLTags;

import st.cs.uni.saarland.de.helpClasses.Info;
import st.cs.uni.saarland.de.helpMethods.StmtSwitchForAdapter;

import java.util.ArrayList;
import java.util.List;

public class AdapterViewInfo extends Info {
    private String eID;
    private String eIDReg;

    private String declaringSootClass = "";
    private String activityClassName = "";
    private String activityReg = "";
    protected String adapterType;
    protected String adapterViewType;
    private String adapterReg = "";
    private String listViewClassName;
    private StmtSwitchForAdapter adapterSwitch;
    private List<String> adapterData;
    private String listenerReg = ""; //onListItemClick

    public AdapterViewInfo(String reg, String declaringSootClass, String adapterType, String adapterViewType) {
        super(reg);
        this.declaringSootClass = declaringSootClass;
        this.adapterType = adapterType;
        this.adapterViewType = adapterViewType;
        this.adapterData = new ArrayList<>();
    }

    public int hashCode(){
        final int prime = 31;
        int result = super.hashCode(); //TODO update this hashcode thingie
        result = prime * result + ((eID == null) ? 0: eID.hashCode());

        //result = prime * result + (adapterData.hashCode());
        result = prime * result + ((adapterType == null)? 0: adapterType.hashCode());
        result = prime * result + declaringSootClass.hashCode();
        return result;
    }

    public boolean equals(Object obj){
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        AdapterViewInfo other = (AdapterViewInfo)obj;
        if(eID == null){
            if(other.eID != null)
                return false;
        }
        else if(!other.eID.equals(eID))
            return false;
        if(adapterViewType != other.adapterViewType)
            return false;
        /*if(adapterSwitch == null)
            if(other.adapterSwitch != null)
                return false;
        else if(!other.adapterSwitch.equals(adapterSwitch))
            return false;*/
        if(!declaringSootClass.equals(other.declaringSootClass))
            return false;
        return true;

    }

    @Override
    public Info clone() {
        AdapterViewInfo adapterViewInfo = new  AdapterViewInfo(searchedEReg, declaringSootClass,adapterType, adapterViewType);
        adapterViewInfo.declaringSootClass = declaringSootClass;
        adapterViewInfo.activityClassName = activityClassName;
        adapterViewInfo.adapterType = adapterType;
        adapterViewInfo.adapterData = new ArrayList<>(adapterData);
        adapterViewInfo.adapterViewType = adapterViewType;
        adapterViewInfo.adapterSwitch = adapterSwitch;
        adapterViewInfo.eID = eID;
        adapterViewInfo.eIDReg = eIDReg;
        return adapterViewInfo;

    }

    @Override
    public String toString(){
        return eID + " " + adapterViewType+" "+ declaringSootClass +" adapter: "+ adapterType + "  "+adapterSwitch+"  " + adapterData.toString();
    }

    @Override
    public void addText(String text){
        super.addText(text);
        this.adapterData.add(0,text);
    }

    @Override
    public List<String> getTextAsList(){
        return adapterData;
    }

    public String getEIDReg(){
        return eIDReg;
    }

    public String getEID(){
        return eID;
    }

    public void setEID(String layoutID){
        this.eID = layoutID;
    }

    public void setEIDReg(String layoutIDReg){
        this.eIDReg = layoutIDReg;
    }


    public StmtSwitchForAdapter getAdapterSwitch(){
        return this.adapterSwitch;
    }
    public void setAdapterSwitch(StmtSwitchForAdapter adapterSwitch) {
        this.adapterSwitch = adapterSwitch;
    }


    public String getAdapterViewType() {
        return adapterViewType;
    }

    public void setAdapterViewType(String adapterViewType) {
        this.adapterViewType = adapterViewType;
    }

    public String getActivityClassName() {
        return activityClassName;
    }

    public String getDeclaringSootClass() {
        return declaringSootClass;
    }

    @Override
    public boolean allValuesFound() {
        return false;
    }
}
