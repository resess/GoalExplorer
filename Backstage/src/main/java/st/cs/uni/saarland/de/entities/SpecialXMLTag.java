package st.cs.uni.saarland.de.entities;

import java.util.*;

import org.apache.commons.lang3.StringUtils;
import soot.Main;
import st.cs.uni.saarland.de.helpClasses.Helper;


// special AppsUIElement: possible tags that will be a SpecialXMLTag: include, fragment, DrawerLayout, ViewPager
public class SpecialXMLTag extends AppsUIElement {

	// other layouts could be added to this tag: these layouts are saved here
	protected List<Integer> xmlFileIDs = new ArrayList<Integer>();

	public SpecialXMLTag(String kindOfUIelement, List<Integer> parent,
			String idFromXMLTag, Map<String, String> solvedText, String textVar, Set<String> drawableNames, Set<Style> styles) {
		super(kindOfUIelement, parent, idFromXMLTag, solvedText, textVar, null, drawableNames, styles);
		
	}
	
	public SpecialXMLTag(AppsUIElement uiE){
		super(uiE.getKindOfUiElement(), uiE.getParents(), String.valueOf(uiE.getID()), uiE.getTextFromElement(), uiE.getTextVar(), null, uiE.getDrawableNames(), uiE.getStyles());
//		setID(uiE.getID());
		setListener(uiE.getListernersFromElement());
		this.childIDs = uiE.getChildIDs();
		this.parentIDsDyn = uiE.getParentIDsDyn();
		this.childIDsDyn = uiE.getChildIDsDyn();
	}

	// TODO change to unique listeners: only: return listeners;
	@Override
	public List<Listener> getListenersFromElementAndIncludedLayouts(Map<Integer, XMLLayoutFile> xmlLayoutFiles, Map<Integer, AppsUIElement> uiElements){
		List<Listener> list = super.getListernersFromElement();
		for (int xmlFID : xmlFileIDs){
			list.addAll(xmlLayoutFiles.get(xmlFID).getListenersOfElementsAndLayout(uiElements));
		}
		return list;
	}

	@Override
	public boolean hasListenerWithIncludedLayouts(Map<Integer, XMLLayoutFile> xmlLayoutFiles, Map<Integer, AppsUIElement> uiElements){
		boolean res = super.hasListenerWithIncludedLayouts(xmlLayoutFiles, uiElements);
		// if this element has element listener, the xmlFiles don't have to be checked
		if (res)
			return res;

		for (int xmlFID: xmlFileIDs){
			XMLLayoutFile xmlF = xmlLayoutFiles.get(xmlFID);
			res = xmlF.hasListenersOfElementsAndLayout(xmlLayoutFiles, uiElements);
			if (res)
				return res;
		}
		return false;
	}

	@Override
	public Map<String, String> getTextFromElementAndIncludedLayouts(Map<Integer, XMLLayoutFile> xmlFiles, Map<Integer, AppsUIElement> uiElements){
		Map<String, String> res = new HashMap<>();
		Map<String, String> textFromElement = super.getTextFromElement();
		res.putAll(textFromElement);

		for (int xmlFID : xmlFileIDs){
			Map<String, String> textInInc = xmlFiles.get(xmlFID).getTextFromLayoutWithoutIncluded(uiElements);
			textInInc.keySet().forEach(k->{
				if(!res.containsKey(k)){
					res.put(k, textInInc.get(k));
				}
				else{
					String currentValue = res.get(k);
					res.replace(k, currentValue + "#" + textInInc.get(k));
				}
			});
		}
		return res;
	}

	@Override
	// returns all children from this element that are inside the depth parameter
	public List<Integer> getHierarchyChildren(int depth, Map<Integer, XMLLayoutFile> xmlFiles, Map<Integer, AppsUIElement> uiElements){
		List<Integer> children = super.getHierarchyChildren(depth, xmlFiles, uiElements);
		if (xmlFileIDs.size() > 0){
			for (int xmlFID : xmlFileIDs){
				// get the XMLLayoutFile which is included in this ui element
				XMLLayoutFile xmlF = xmlFiles.get(xmlFID);
				// get the root element of this included layout (so the child of this element)
				AppsUIElement rootElementOfxmlF = uiElements.get(xmlF.getRootElementID());
				// add the root element as first child of this subtree
				children.add(rootElementOfxmlF.getId());
				// call getHierarchyChildren to get all children from this element in range of depth-1
				// -> get all children of this element inside depth
				children.addAll(rootElementOfxmlF.getHierarchyChildren(depth-1, xmlFiles, uiElements));
			}
			return children;
		}else
		// no xmlFile is included so return the "normal" childs of this element
			return children;
	}

	@Override
	public Collection<Integer> getIncludedLayouts(Map<Integer, XMLLayoutFile> xmlLayoutFiles, Map<Integer, AppsUIElement> uiElements, Collection<Integer> processedUIElements) {
		//Set<Integer> set =  new HashSet<Integer>();
		for (int xmlFID : xmlFileIDs){
			XMLLayoutFile xmlF = null;
			try{
				xmlF = xmlLayoutFiles.get(xmlFID);
			}catch(NullPointerException e){
				Helper.saveToStatisticalFile("SpecialXMLTag:getIncludedLayouts: Couldn't find xmlF with id: " + xmlFID);
			}
			if (xmlF != null)
				processedUIElements.addAll(xmlF.getLayoutIDsWithIncludeAndDynamicalInclude(xmlLayoutFiles, uiElements, processedUIElements));
		}
		return processedUIElements;
	}

	//TODO FIX
	// returns the text that is not connected to a listener via an element and if this element is not a button
//	public String getTextIfInactive(String id) {
//		Set<String> res = new HashSet<String>();
//		if (!hasElementListeners()){
//			if (!kindOfUiElement.toLowerCase().contains("button")){
//				res.add(getTextFromElement());
//			}
//		}
//		for (XMLLayoutFile xmlF : xmlFiles){
//			res.add(xmlF.getInactiveContextText(id));
//		}
//		return String.join("#", res);
//	}


	@Override
	public String toString(){
		String str = super.toString();
		str = str + "---SubLayout: ";
		
		for (int f: xmlFileIDs){
			String a = String.valueOf(f);
			str = str + a + "---";
		}
		
		return str;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		//what to do check the id here?
		if (!super.equals(o)) return false;
		SpecialXMLTag that = (SpecialXMLTag) o;
		return Objects.equals(xmlFileIDs, that.xmlFileIDs);
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), xmlFileIDs);
	}

	@Override
	@Deprecated
	public String attributesForSavingToString(String appAndScreenName){		
		StringBuilder str = new StringBuilder();
		str.append(super.attributesForSavingToString(appAndScreenName));
		
		str.append("---SubLayout: ");
		for (int f: xmlFileIDs){
			str.append(String.valueOf(f));
			str.append(";");
		}	
		
		return str.toString();
	}

	public void addXmlFile(int mainFragLayoutID) {
		xmlFileIDs.add(mainFragLayoutID);
	}
	public List<Integer> getXmlFileIds() {
		return xmlFileIDs;
	}
	@Deprecated
	// with this method the children and parents are not updated
	public void addXmlFiles(Collection<Integer> xmlFileIDs) {
		if (null != xmlFileIDs)
			this.xmlFileIDs.addAll(xmlFileIDs);
		else {
			logger.debug("addXMLFiles in SpecXMLTag, but xmlFileIDs were null");
			Helper.saveToStatisticalFile("addXMLFiles in SpecXMLTag, but xmlFileIDs were null");
		}
	}

	public boolean containsElement(int id){
		return xmlFileIDs.contains(id);
	}

	public boolean hasSubLayout(){
		return xmlFileIDs.size() > 0;
	}



}
