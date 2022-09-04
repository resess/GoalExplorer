package st.cs.uni.saarland.de.entities;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import st.cs.uni.saarland.de.testApps.Content;

// XMLIncludeTag is a special AppsUIElement and a special SpecialXMLTag
// this object is only created if a include tag was found
public class XMLNavigation extends SpecialXMLTag {

    private int includedMenuId = 0;
    private String includedMenuLayout;

	
	public XMLNavigation(String kindOfUIelement, List<Integer> parent, String idFromXMLTag, Map<String, String> solvedText, String textVar, Listener listener, Set<String> drawableNames, Set<Style> styles,
			String includedMenuLayout) {
        super(kindOfUIelement, parent, idFromXMLTag, solvedText, textVar, drawableNames, styles);

        if (listener != null)
            listeners.add(listener);

        this.includedMenuLayout = (includedMenuLayout != null)?includedMenuLayout.replaceAll("@\\+?menu/", ""): "";

        if (!StringUtils.isBlank(includedMenuLayout)) {
            // analysis if idFromXMLTag is real id, id variable or Android default id and sets it accordingly
            processMenuIDFromXMLTag(includedMenuLayout);
        } else {
            this.includedMenuId = Content.getNewUniqueID();
        }
		
	}

    private void processMenuIDFromXMLTag(String idVar) {
        if (includedMenuId == 0) {
            includedMenuId = Content.getInstance().getIdFromName(includedMenuLayout, "menu");
            if (includedMenuId == 0){
                String[] tmp = idVar.split("0x");
                if (tmp.length > 1) {
                    int i = Integer.parseInt(tmp[1], 16);
                    this.includedMenuId = i;
                } else {
                    this.includedMenuId = Integer.parseInt(idVar);
                }
            }
        } else {
            logger.error("id of AppsUIElement was tried to be replaced");
        }
    }

	public int getIncludedMenuId(){
		return includedMenuId;
	}

    public String getIncludedMenuLayoutFileName(){
        return includedMenuLayout;
    }

    public Menu getIncludedMenu(Map<Integer, XMLLayoutFile> xmlLayoutFiles){
        return (Menu)xmlLayoutFiles.get(includedMenuId);
    }

    public String getIncludedMenuName(Map<Integer, XMLLayoutFile> xmlLayoutFiles){
        return xmlLayoutFiles.get(includedMenuId).getName();
    }

    @Override
    public String toString(){
        String str = super.toString();
		str = str + "---Menu: " + includedMenuId+"  "+includedMenuLayout;
		return str;

    }
	
}
