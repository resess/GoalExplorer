package st.cs.uni.saarland.de.entities;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import st.cs.uni.saarland.de.helpClasses.AndroidRIdValues;
import st.cs.uni.saarland.de.helpClasses.Helper;
import st.cs.uni.saarland.de.testApps.Content;

import java.util.*;


public class AppsUIElement {

    protected List<Integer> parentIDs = new ArrayList<>();
    protected List<Integer> childIDs = new ArrayList<>();
    protected List<Integer> parentIDsDyn = new ArrayList<>();
    protected List<Integer> childIDsDyn = new ArrayList<>();
    protected String idVar = "";// element id variable name (not set for every element)
    // element id given in decimal value, if 7 digits-> self created id, 8d-> android default id, 10d-> normal Android id
    protected int id = 0;
    protected String textVar = ""; // element text variables joined with "#"
    protected Map<String, String> text = new HashMap<>(); // element texts joined with "#"
    protected String kindOfUiElement = ""; // saves which tag type was found: eg button, textView, View, include
    protected List<Listener> listeners = new ArrayList<>(); // list of listeners attached to this element
    private Set<String> drawableNames = new HashSet<>(); // list of attached drawables
    private Set<Style> styles = new HashSet<>(); // list of attached styles (not parent styles)
    protected final Logger logger = LoggerFactory.getLogger(Thread.currentThread().getName());

    // constructor
    public AppsUIElement(String kindOfUIelement, List<Integer> parentIDs, String idVariable, Map<String, String> solvedText, String textVariable, Listener listener, Set<String> pdrawableNames, Set<Style> styles) {
        this.kindOfUiElement = kindOfUIelement;
        if (pdrawableNames != null)
            this.drawableNames.addAll(pdrawableNames);

        this.parentIDs.addAll(parentIDs);

        if (!StringUtils.isBlank(idVariable)) {
            // analysis if idFromXMLTag is real id, id variable or Android default id and sets it accordingly
            this.processIDFromXMLTag(idVariable);
        } else {
            this.id = Content.getNewUniqueID();
        }

        // process the text variable
        if (solvedText != null) {
            this.text.putAll(solvedText);
        }
        if (!StringUtils.isBlank(textVariable))
            this.textVar = textVariable;

        if (listener != null)
            listeners.add(listener);

        if (styles != null)
            this.styles.addAll(styles);
    }


    // TODO comment and point out the functionality of this method....
    // only images could be added that are existing(without type) in drawableNames
    public void addTypeOfImageElements(Set<String> addImagesWithTypeSet) {
        // sets to remember which elements has to be remove and added in the drawableNames set
        Set<String> toRemove = new HashSet<>();
        Set<String> toAdd = new HashSet<>();

        for (String drawable : drawableNames) {
            for (String newImageWithType : addImagesWithTypeSet) {
                // replace drawable with newImageWithType
                if (newImageWithType.contains(drawable)) {
                    if (!newImageWithType.contains("\\")) {
                        toAdd.add(newImageWithType);
                        toRemove.add(drawable);
                    } else {
                        logger.error("newImageWithType is given only with path!!!" + newImageWithType);
                        System.out.println("newImageWithType is given only with path!!!" + newImageWithType);
                    }
                }
            }
        }
        drawableNames.addAll(toAdd);
        drawableNames.removeAll(toRemove);

    }

    // returns a string of the text of this element if no listener is attached to this element and it is not a button
    // this method is not overwritten by SpecXMLTag so no text from included layouts would be returned
    public Map<String, String> getTextIfInactiveListener() {
        if (hasElementListener()) {
            return new HashMap<>();
        } else {
            if (!kindOfUiElement.toLowerCase().contains("button")) {
                return getTextFromElement();
            } else
                return new HashMap<>();
        }
    }

    // returns a string of the text of this element if no api is attached to this element
    // this method is not overwritten by SpecXMLTag so no text from included layouts would be returned
    public Map<String, String> getTextIfInactiveAPI() {
        if (isConntectedToAnAPI()) {
            return new HashMap<>();
        } else {
            return getTextFromElement();
        }
    }

    // called by constructor
    protected void setID(int pidBut) {
        // the counter can never be overwritten
        if (id == 0) {
            id = pidBut;
        }
    }

    // check if idVar is Android default id (if it is resolves) or if it is a id variable or the concret value
    private void processIDFromXMLTag(String idVar) {
        if (id == 0) {
            if (idVar.contains("android:id/")) {
                String idVariable = idVar.split("android:id/")[1];
                this.id = AndroidRIdValues.getAndroidID(idVariable);
                this.idVar = idVariable;
                // process the id variables
            } else if (idVar.contains("id/")) {
                String nameOfId = idVar.replaceAll("@\\+?id/", "");
                id = Content.getInstance().getIdFromName(nameOfId, "id");
                if (id == 0)
                    return;
                // process hard coded ids
            } else {
                String[] tmp = idVar.split("0x");
                if (tmp.length > 1) {
                    int i = Integer.parseInt(tmp[1], 16);
                    this.id = i;
                } else {
                    this.id = Integer.parseInt(idVar);
                }
            }
        } else {
            logger.error("id of AppsUIElement was tried to be replaced");
        }
    }

    @Override
    public String toString() {
        String parentName = "";
        if (parentIDs != null) {
            for (int parentID : parentIDs) {
                if (parentID != 0)
                    parentName = parentName + parentID + ";";
            }
        }
        String pathes = "";
        for (String path : drawableNames) {
            pathes = pathes + "#" + path;
        }
        String res = "object_of: " + kindOfUiElement + "; parent: " + parentName + "; but_idButVar: " + idVar + "; but_idBut: " + id +
                "; but_textVar: " + textVar + "; but_Text: " + text + "; drawableName: " + pathes;
        for (Listener l : listeners) {
            res = res + l.toString();

        }
        return res;
    }

    @Deprecated
    // special output of all attributes of this class written in one string
    public String attributesForSavingToString(String appAndScreenName) {
        String res = appAndScreenName + ";" + kindOfUiElement + ";" + idVar + ";" + id + ";" + textVar + ";" + text;
        // TODO get lsiteners again
//		for (Listener l : listeners){
//			res = res + ";" + l.attributesForSavingToString();
//		}
        return res + System.lineSeparator();
    }

    public boolean isText() {
        StringBuilder fString = new StringBuilder();
        text.values().forEach(fString::append);
        if (StringUtils.isBlank(fString.toString()))
            return true;
        else
            return false;
    }

    public List<Listener> getListernersFromElement() {
        if (styles.size() <= 0)
            return this.listeners;
        else {
            boolean hasXMLListener = listeners.stream().anyMatch(l -> l.isXMLDefined());
            // if the uiE has an xml defined listener, this overwrites all style listeners
            if (!hasXMLListener) {
                List<Listener> allListeners = new ArrayList<>();
                styles.stream().filter(s -> s.hasListener()).forEach(x -> allListeners.add(x.getOnClickMethod()));
                allListeners.addAll(listeners);
                return allListeners;
            } else {
                return this.listeners;
            }
        }
    }

    public List<Listener> getListenersFromElementAndIncludedLayouts(Map<Integer, XMLLayoutFile> xmlLayoutFiles, Map<Integer, AppsUIElement> uiElements) {
        return getListernersFromElement();
    }

    public void addListener(Listener l) {
        if (l != null && !this.listeners.contains(l))
            this.listeners.add(l);
        else
            Helper.saveToStatisticalFile("AppsUIElement: tried to assign null listener object in " + id);
    }

    // should only be used by transformation from an AppsUIElement to SpecialXMLTag
    protected void setListener(List<Listener> l) {
        this.listeners = l;
    }

    public boolean hasElementListener() {
        List<Listener> allListeners = getListernersFromElement();
        if (allListeners.size() > 0) {
            for (Listener l : allListeners) {
                if (l.isListenerClass() && l.isListenerMethod())
                    return true;
            }
        }
        return false;
    }

    public boolean hasListenerWithIncludedLayouts(Map<Integer, XMLLayoutFile> xmlLayoutFiles, Map<Integer, AppsUIElement> uiElements) {
        return hasElementListener();
    }

    public String getKindOfUiElement() {
        return kindOfUiElement;
    }


    public List<Integer> getChildIDs() {
        return childIDs;
    }

    public List<Integer> getChildIDsWithDyn() {
        return new ArrayList<Integer>() {{
            addAll(childIDs);
            addAll(childIDsDyn);
        }};
    }

    public void addChildID(int childID) {
        this.childIDs.add(childID);
    }

    public void addChildIDDyn(int childID) {
        this.childIDsDyn.add(childID);
    }

    public void addParentDyn(int parentID) {
        this.parentIDsDyn.add(parentID);
    }

    public void addParent(int parent) {
        this.parentIDs.add(parent);
    }

    protected List<Integer> getParentIDsDyn() {
        return parentIDsDyn;
    }

    protected List<Integer> getChildIDsDyn() {
        return childIDsDyn;
    }

    public List<Integer> getParents() {
        return parentIDs;
    }

    public List<Integer> getParentsWithDyn() {
        return new ArrayList<Integer>() {{
            addAll(parentIDs);
            addAll(parentIDsDyn);
        }};
    }

    public boolean hasImageElement() {
        if (drawableNames.size() > 0)
            return true;
        else
            return false;
    }

    // evaluation method to get all merged layouts but not merged menus
    public List<Integer> getIncludedChilds(){
        return getChildIDsWithDyn();
    }

    public int getId() {
        return id;
    }

    public Set<String> getDrawableNames() {
        return drawableNames;
    }

    public void addDrawableName(String nameOfDrawable) {
        if (!nameOfDrawable.contains("\\"))
            // nameOfDrawable without any PATH!!!
            this.drawableNames.add(nameOfDrawable);
        else {
            logger.error("nameOfDrawable is given only with path!!!" + nameOfDrawable);
            System.out.println("nameOfDrawable is given only with path!!!" + nameOfDrawable);
        }
    }

    public String getUIIDVar() {
        return idVar;
    }

    public boolean isUIIDVar() {
        if ((idVar != null) && (!idVar.equals("")))
            return true;
        else
            return false;
    }

    public int getID() {
        return id;
    }


    public String getTextVar() {
        return textVar;
    }

    @Deprecated
    public boolean isTextVar() {
        if ((textVar != null) && (!textVar.equals("")))
            return true;
        else
            return false;
    }



    public Set<String> getCalledAPIs(){
        Set<String> apis = new HashSet<String>();
        for (Listener l : listeners){
            for (String api: l.getCalledAPISignatures()){
                apis.add(api);
            }
        }
        return apis;
    }


    public Map<String, String> getTextFromElement() {// FIXME:
        if (styles == null) {
            return text;
        }
        if (!text.containsKey("default_value") || text.get("default_value").trim().isEmpty()) {
            //take only first style
            String styleText = styles.stream()
                    .map(s -> s.getText())
                    .filter(t -> !t.isEmpty())
                    .findFirst().orElse("");
            //collect(Collectors.joining("#"));
            text.put("default_value", styleText);
//            if (text.containsKey("default_value")) {
//                String value;
//                if (text.get("default_value").isEmpty() || allText.isEmpty())
//                    value = text.get("default_value").concat(allText);
//                else
//                    value = (text.get("default_value") + "#" + allText);
//
//                text.replace("default_value", value);
//            } else {
//                text.put("default_value", allText);
//            }
        }
        return text;

    }

    public Map<String, String> getTextFromElementAndIncludedLayouts(Map<Integer, XMLLayoutFile> xmlFiles, Map<Integer, AppsUIElement> uiElements) {
        return getTextFromElement();
    }

    public void addText(String ptext, String declaringSootClass) {
        ptext = ptext.trim();
        if (!StringUtils.isBlank(ptext) && !ptext.equals("#")) {
            if (text.containsKey(declaringSootClass)) {
                String value = text.get(declaringSootClass);
                value = value + "#" + ptext;
                if (value.startsWith("#")) {
                    value = value.replaceFirst("#", "");
                }
                text.replace(declaringSootClass, value);
            } else {
                text.put(declaringSootClass, ptext);
            }
        }
    }

    // returns all children from this element that are inside the depth parameter
    public List<Integer> getHierarchyChildren(int depth, Map<Integer, XMLLayoutFile> xmlFiles, Map<Integer, AppsUIElement> uiElements) {
        return getChildIDsWithDyn();
    }


    // returns a set of element ids that neighbours this element including itself:
    // if an element has more than one parent, the layouts get splitted -> Set<Set<Integer>>
    // params:
    // parentDepth describes how far parent should get considered:
    // <0: all parents; 0: no parents; x: depth (level up) (how much parents)
    // childrenDepth describes how far children should get considered:
    // <0: all children; 0: no children; x: depth (how much level of childrens)
    // childrenOfParents describes how far children of parents should be considered.
    // this number cannot be higher than the parentDepth. it counts from the analysed element up.
    // <0: all children of parents where a parent was considered; 0: no children of parents at all;
    // x: the number of level up, the children of parents should be considered
    public List<Set<Integer>> getNeighbours(int parentDepth, int childrenDepth, int childrenOfParents, Map<Integer, AppsUIElement> uiElements) {
//		if(childrenOfParents > parentDepth)
//			throw new IllegalArgumentException("childrenOfParents could not be higher than parentDepth");
        List<Set<Integer>> neighbourIDs = new ArrayList<>();
        Set<Integer> children = new HashSet<>();
        Set<Integer> allChildren = new HashSet<>();
        allChildren.addAll(childIDs);
        allChildren.addAll(childIDsDyn);
        // analyse children depth and the "childrenOfParents" if this is a parent
        if ((childrenDepth != 0) && allChildren.size() > 0) {
            // --- analyse children ---
            // add all children to the result list
//			neighbourIDs.add(new HashSet<Integer>());
            children.addAll(allChildren);
            for (int childID : allChildren) {
                AppsUIElement child = uiElements.get(childID);
                List<Set<Integer>> childrenSet = null;
                if (childrenDepth != 0) {
                    // don't analyse parents (this is the parent), only analyse children and children of children
                    childrenSet = child.getNeighbours(0, childrenDepth - 1, 0, uiElements);
                } else if (childrenOfParents != 0) {
                    // don't analyse parents (this is the parent), only analyse children and children of children
                    childrenSet = child.getNeighbours(0, childrenOfParents - 1, 0, uiElements);
                }
                // childrenSet should only have one set inside because no parents are analysed
                if (childrenSet.size() > 1)
                    throw new IllegalArgumentException("childrenSet with more than one set");
                    // add all children of the child children to the set
                else if (childrenSet.size() == 1) {
                    children.addAll(childrenSet.get(0));
                }
            }
        }
        // analyse the parent depth
        if (parentDepth != 0 && parentIDs.size() > 0) {
            // --- analyse parents ---
            Set<Integer> allParents = new HashSet<>();
            allParents.addAll(parentIDs);
            allParents.addAll(parentIDsDyn);
            // analyse each parent and create for each parent a new layout (where this layout is included)
            for (int parentID : allParents) {
                // search for the parents
                AppsUIElement parent = uiElements.get(parentID);
                List<Set<Integer>> parentLayouts = parent
                        .getNeighbours(parentDepth - 1, childrenOfParents, childrenOfParents, uiElements);
                if (parentLayouts.size() == 0)
                    parentLayouts.add(new HashSet<>());
                // add all found children to each parent layout
                for (Set<Integer> parentLayout : parentLayouts) {
                    // add the parent itself to the set
                    parentLayout.add(parentID);
                    // add the children (first call) or/and the parentsOfChildren
                    parentLayout.addAll(children);
                }
                neighbourIDs.addAll(parentLayouts);
            }
        } else {
            // no parents were found or should be found so return the childs
            neighbourIDs.add(children);
        }
        return neighbourIDs;
    }
/*

    // constructor
    // idFromXMLTag could be id variable or value
    @Deprecated
    public AppsUIElement(String kindOfUIelement, List<Integer> parentIDs, String idFromXMLTag, String textVar, Listener listener, Set<String> pdrawableNames) {
        this.kindOfUiElement = kindOfUIelement;
        if (pdrawableNames != null)
            this.drawableNames.addAll(pdrawableNames);

        this.parentIDs.addAll(parentIDs);
        if ((idFromXMLTag != null) && (!idFromXMLTag.equals(""))) {
            // analysis if idFromXMLTag is real id, id variable or Android default id and sets it accordingly
            this.processIDFromXMLTag(idFromXMLTag);
        }

        // TODO rewrite: text will always be the real value, not the textVar
        // process the text variable
        if ((textVar != null) && (!textVar.equals(""))) {
//			this.text = textVar.replaceAll("#?@\\+?string/(.*)#?", "$1");
            String[] texts = textVar.split("#");
            Set<String> textVarSet = new HashSet<String>();
            Set<String> textSet = new HashSet<String>();
            if (!StringUtils.isBlank(text))
                textSet.add(text);//FIXME should we rewrite text and textVar or append???
            for (String t : texts) {
                if (t.contains("string/"))
                    textVarSet.add(t.replaceAll("@\\+?string/", ""));
                else
                    textSet.add(t);
            }
            text = String.join("#", textSet);
            this.textVar = String.join("#", textVarSet);
        }
        if (listener != null)
            listeners.add(listener);
    }*/

    public Collection<Integer> getIncludedLayouts(Map<Integer, XMLLayoutFile> xmlLayoutFiles, Map<Integer, AppsUIElement> uiElements, Collection<Integer> processedUIElements) {
        return new HashSet<>();
    }

    public boolean isConntectedToAnAPI() {
        for (Listener l : listeners) {
            if (l.hasAPICalls()) {
                return true;
            }
        }
        return false;
    }

    public Set<Style> getStyles() {
        return styles;
    }
    //	@Override
//	public boolean equals(Object o){
//		if (o instanceof AppsUIElement){
//			if (((AppsUIElement) o).getKindOfUiElement().equals(kindOfUiElement)
//					&& ((AppsUIElement) o).getUIID().equals(uiID)){
//				return true;
//			}
//		}
//		return false;
//	}

}

