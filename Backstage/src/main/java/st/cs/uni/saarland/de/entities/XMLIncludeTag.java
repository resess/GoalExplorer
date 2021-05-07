package st.cs.uni.saarland.de.entities;

import java.util.List;

// XMLIncludeTag is a special AppsUIElement and a special SpecialXMLTag
// this object is only created if a include tag was found
public class XMLIncludeTag extends SpecialXMLTag {

	private String layoutName; // describes the XMLLayoutFile name that is here included into the layout
	
	public XMLIncludeTag(String kindOfUIelement, List<Integer> parent,
			String layoutName) {
		super(kindOfUIelement, parent,null,  null, null, null, null);
		
		this.layoutName = layoutName;
	}

	public String getLayoutName(){
		return layoutName;
	}
	
	
}
