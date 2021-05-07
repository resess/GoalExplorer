package st.cs.uni.saarland.de.entities;

import java.util.List;
import java.util.Map;

// XMLFragment is a special AppsUIElement and a special SpecialXMLTag
// this object is only created if a fragment tag was found
public class XMLFragment extends SpecialXMLTag{
	
	private String className ; // describes the fragment class that is here included into the layout
	
	public XMLFragment(String kindOfUIelement, List<Integer> parent,
					   String idFromXMLTag, Map<String, String> solvedText, String textVar, String className) {
		
		super(kindOfUIelement, parent, idFromXMLTag, solvedText, textVar, null, null);
		this.className = className;
	}
	
	public String getClassName() {
		return className;
	}
	
	@Override
	public String toString(){
		String str = this.className + " " + super.toString();
		return str;
	}

	@Override
	public String attributesForSavingToString(String appAndScreenName){
		String res = this.className + super.attributesForSavingToString(appAndScreenName);	
		return res;
	}


}
