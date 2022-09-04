package st.cs.uni.saarland.de.dissolveSpecXMLTags;

import st.cs.uni.saarland.de.helpClasses.Info;
import st.cs.uni.saarland.de.helpMethods.StmtSwitchForAdapter;

import java.util.ArrayList;
import java.util.List;

//data_src= ....
//adapter = new Adapter(data_src)
//list_view = ...
//list_view.setAdapter(adapter)
//setAdapterList(...., adapter)

// Need, the set of text if available
// The id of the listview
// the listener I guess

//Will be mapped to LisTView (map position to text),
//Fake ui elements for the content id+position?
//When parsing on listitem click if switch on position if position matches with id, then listviewitem (listview id, position, text)


//For ListActivity the id must be R.id.list (which is set to be a constant: 16908298)
//androidIds.get("list")
public class ListViewInfo extends Info {
    private String eID;
    private String eIDReg;

    private String declaringSootClass = "";
    private String activityClassName = "";
    private String activityReg = "";
    protected String adapterType;
    private String adapterReg = "";
    private String listViewClassName;
    private StmtSwitchForAdapter adapterSwitch;
    private List<String> adapterData;
    private String listenerReg = ""; //onListItemClick
    //private String 
    //Some map to represent ui elements (id, text, class)
    //Maybe a AdapterData object or smth
    public ListViewInfo(String reg, String declaringSootClass, String adapterType ) {
        super(reg);
        this.adapterType = adapterType;
        this.declaringSootClass =declaringSootClass;
        this.activityClassName = declaringSootClass;
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
        ListViewInfo other = (ListViewInfo)obj;
        if(eID == null){
            if(other.eID != null)
                return false;
        }
        else if(!other.eID.equals(eID))
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

        ListViewInfo listViewInfo = new ListViewInfo(searchedEReg, declaringSootClass,adapterType);
        listViewInfo.declaringSootClass = declaringSootClass;
        listViewInfo.activityClassName = activityClassName;
        listViewInfo.adapterType = adapterType;
        listViewInfo.adapterData = new ArrayList<>(adapterData);
        listViewInfo.adapterSwitch = adapterSwitch;
        listViewInfo.eID = eID;
        listViewInfo.eIDReg = eIDReg;
        return listViewInfo;

    }

    @Override
	public String toString(){
		return eID + " " + declaringSootClass +" adapter: "+ adapterType + "  "+adapterSwitch+"  " + adapterData.toString();
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


    @Override
    public boolean allValuesFound() {
        return false;
    }

    public StmtSwitchForAdapter getAdapterSwitch(){
        return this.adapterSwitch;
    }
    public void setAdapterSwitch(StmtSwitchForAdapter adapterSwitch) {
        this.adapterSwitch = adapterSwitch;
    }

    public String getActivityClassName() {
        return activityClassName;
    }

    public String getDeclaringSootClass() {
        return declaringSootClass;
    }

}
