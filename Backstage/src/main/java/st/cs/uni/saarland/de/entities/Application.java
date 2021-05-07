package st.cs.uni.saarland.de.entities;

import com.thoughtworks.xstream.annotations.XStreamOmitField;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import st.cs.uni.saarland.de.helpClasses.Helper;
import st.cs.uni.saarland.de.reachabilityAnalysis.UiElement;

import java.util.*;

// class where the data of the whole currently analysed app and the results of the analysis is stored
public class Application {

    private String name; // name of the application
    private Map<Integer, AppsUIElement> uiElementsOfApp;
    private Map<Integer, UiElement> uiElementsWithListener;
    // set of all XML files inside the res folder of the unpacked app, <counter of layout, XMLayoutFile object>
    private Map<Integer, XMLLayoutFile> xmlLayoutFiles;
    // list of all dialog this app has
    private Map<Integer, Dialog> dialogs;

    // list of all menus
    private Map<Integer, Menu> menus;

    public Set<String> getPermissions() {
        return permissions;
    }

    public void setPermissions(Set<String> permissions) {
        this.permissions = permissions;
    }

    private Set<String> permissions;



    public Set<Activity> getActivities() {
        return activities;
    }

    public Activity getActivityByName(String name) {
        for (Activity activity : activities) {
            if (name.equalsIgnoreCase(activity.getName())) {
                return activity;
            }
        }
        return null;
    }

    public void addActivity(Activity activity){
        this.activities.add(activity);
    }

    // list of all activity names declared inside the AndroidManifest file
    private Set<Activity> activities;

    // save the ids of include- and fragment tags, to iterate over them to resolve the included layout
    // done for performance issues (otherwise all ui element would need to be iterated over)
    private Set<Integer> includeTagIDs = new HashSet<>();
    private Set<Integer> fragmentTagIDs = new HashSet<>();
    @Deprecated
    private List<Screen> screens; // list of all screens eg all screens that a user would see
    // TODO these maps could maybe be removed -> problem. how to save hierarchy
    // map of matchings between an activity and the displayed XMLLayoutFiles
    @XStreamOmitField
    private final Map<String, Set<Integer>> activityToXMLLayoutFiles; // Map<ActivityClassName, Set<LayoutIDs>>
    // map of matchings between a special Java class to an layout file -> only used for testing/debugging/...
    private final Map<Integer, Set<Integer>> mergedLayoutFileIDs; // Map<ActivityClassName, Set<LayoutIDs>>
    private final Map<String, Set<Integer>> fragmentClassToLayout; // Map<fragmentClass name, counter of views>
    // TOOD move to activities? -> probl. how to save hierarchy
    private List<String> intentFilters; // list of all intent-filters the app claims
    // list of all dialogs that where found inside the app's code
    // TODO also moved to corresponding activity? -> problem. how to save hierarchy

    private final Logger logger = LoggerFactory.getLogger(Thread.currentThread().getName());


    // init this object
    public Application(String name) {
        this.name = name;
        xmlLayoutFiles = new HashMap<>();
        activities = new HashSet<>();
        permissions = new HashSet<>();
        screens = new ArrayList<>();
        intentFilters = new ArrayList<>();
        dialogs = new HashMap<>();
        activityToXMLLayoutFiles = new HashMap<>();
        mergedLayoutFileIDs = new HashMap<>();
        fragmentClassToLayout = new HashMap<>();
        uiElementsOfApp = new HashMap<>();
        uiElementsWithListener = new HashMap<>();

        menus = new HashMap<>();
    }


    public AppsUIElement getUiElement(int elementID) {
        return uiElementsOfApp.get(elementID);
    }

    public void addUiElementsOfApp(AppsUIElement uiElement) {
        this.uiElementsOfApp.put(uiElement.getId(), uiElement);
    }

    // TODO: remove superclasses check. should be directly there a normal class
    public void addXMLLayoutFileToActivity(String activityClassName, int layoutID) {
        if(activityClassName == null || activityClassName.length() == 0){
            return;
        }
        if (!activityClassName.equals("")) {
            if (activityToXMLLayoutFiles.containsKey(activityClassName)) {
                Set<Integer> layoutIDs = activityToXMLLayoutFiles.get(activityClassName);
                layoutIDs.add(layoutID);
            } else {
                Set<Integer> newSet = new HashSet<>();
                newSet.add(layoutID);
                activityToXMLLayoutFiles.put(activityClassName, newSet);
            }
        }
    }

    // store two layouts that are merged together
    public void addMergedLayoutIDs(int layoutID, int addedLayoutID) {

        if (mergedLayoutFileIDs.containsKey(layoutID)) {
            Set<Integer> layoutIDs = mergedLayoutFileIDs.get(layoutID);
            layoutIDs.add(addedLayoutID);
        } else {
            Set<Integer> newSet = new HashSet<>();
            newSet.add(addedLayoutID);
            mergedLayoutFileIDs.put(layoutID, newSet);
        }

        // set the child and parent of the root elements of the layouts accordingly
        XMLLayoutFile xmlFroot = xmlLayoutFiles.get(layoutID);
        XMLLayoutFile xmlFaddedLay = xmlLayoutFiles.get(addedLayoutID);
        AppsUIElement rootElementOfRootLay = uiElementsOfApp.get(xmlFroot.getRootElementID());
        AppsUIElement rootElementOfAddedLay = uiElementsOfApp.get(xmlFaddedLay.getRootElementID());
        rootElementOfAddedLay.addParentDyn(rootElementOfRootLay.getId());
        rootElementOfRootLay.addChildIDDyn(rootElementOfAddedLay.getId());
    }

    // store the views of a fragment onCreateView method
    public void addFragmentClassToViewIDs(String fragmentName, Set<Integer> layout) {
        if (!StringUtils.isBlank(fragmentName)) {
            if (fragmentClassToLayout.containsKey(fragmentName)) {
                Set<Integer> layoutIDs = fragmentClassToLayout.get(fragmentName);
                layoutIDs.addAll(layout);
            } else {
                fragmentClassToLayout.put(fragmentName, layout);
            }
        }
    }

    public Collection<XMLLayoutFile> getAllXMLLayoutFiles() {
        return xmlLayoutFiles.values();
    }

    public Map<Integer, XMLLayoutFile> getXMLLayoutFilesMap() {
        return xmlLayoutFiles;
    }

    public Collection<Dialog> getDialogsOfApp() {
        return dialogs.values();
    }

    // returns a list of all AppsUIElement of the app
    // TODO return iterator? needed?
    // Corrupted
    @Deprecated
    public Collection<AppsUIElement> getAllUIElementsOfApp() {
        return uiElementsOfApp.values();
    }

    public Map<Integer, AppsUIElement> getUIElementsMap() {
        return uiElementsOfApp;
    }

//	public void addPossibleListenerClasses(String possibleListenerClass){
//			possibleXMLListenerClasses.add(possibleListenerClass);
//	}

    public Map<Integer, XMLLayoutFile> getXmlLayoutFiles() {
        return xmlLayoutFiles;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder("appName: " + name + System.getProperty("line.separator"));
        for (XMLLayoutFile sc : xmlLayoutFiles.values()) {
            str.append(sc.toString()).append(System.getProperty("line.separator"));
        }
        return str.toString();
    }

    // returns a string with all information about all AppsUiElements of the app
    @Deprecated
    public String getStringForsavingAllUIElementAttributes() {
        String res = "";
        for (AppsUIElement sc : uiElementsOfApp.values()) {
            res = res + sc.attributesForSavingToString(name);
        }
        return res;
    }

    // TODO check and check sense of method (merge the static merged layouts with the dyn. merged?)
    // returns a map of activity names to the xml layout file ids which form the screen of this activity
    public Map<String, Set<Integer>> getLayoutClasses() {
        Map<String, Set<Integer>> classToIds = getActivityToXMLLayoutFiles();
        Map<String, Set<Integer>> layoutClasses = new HashMap<>();

//		// iterate over all found activities where at least one layout file was connected too
//		// TODO bad loop? rewrite with entry set?
//		// TODO this loop only compute the same map as getActvityToXMLLayoutFiles(), only that the layout ids
//			// are stored as integer
//		for(String className: classToIds.keySet()){
//			// iterate over all activity names
//			for(String activityId : classToIds.get(className)){
//				if(!Helper.isIntegerParseInt(activityId))
//					continue;
//
//				Set<Integer> layoutIDs = layoutClasses.get(className);
//				if (layoutIDs == null) {
//					layoutIDs = new HashSet<Integer>();
//					layoutClasses.put(className, layoutIDs);
//				}
//				// add the layout to the activity map
//				layoutIDs.add(Integer.parseInt(activityId));
//			}
//		}
        return layoutClasses;
    }


    // returns a Menu object with the same data than the XMLLayoutFile with the xmlLayoutFileID
    // and replaces the XMLLayoutFile object with the Menu object in the list of all XMLLayoutFiles in this class
    public void expandXMLLayoutFileWithMenu(int xmlLayoutFileID) throws IllegalArgumentException {
        Menu menu = null;
        XMLLayoutFile deleteFile = null;
        // search the XMLLayoutFile with the id , xmlLayoutFileID
        XMLLayoutFile xmlF = xmlLayoutFiles.get(xmlLayoutFileID);
        if (!(xmlF instanceof Menu)) {
            // create new Menu object which contains all informations of the XMLLayoutFile xmlF
            menu = new Menu(xmlF);
            // remember the XMLLayouFile
            deleteFile = xmlF;

            if ((menu == null) || (deleteFile == null))
                throw new IllegalArgumentException("no XMLLayoutFile with the given ID found: " + xmlLayoutFileID);
            // replace the XMLLayoutFile object with the Menu object
            xmlLayoutFiles.put(menu.getId(), menu);
        } else {
            // if the given XMLLayoutFile is a Menu just return it
            menu = (Menu) xmlF;
        }
        this.menus.put(menu.getId(), menu);
    }

    /**
     * Updates the menu with the given menu id and menu
     * @param menuId the resource ID of the menu
     * @param menu the menu to update
     */
    public void updateMenu(Integer menuId, Menu menu) {
        this.menus.replace(menuId, menu);
    }

    /**
     * Adds the menu with the given menu id and menu
     * @param menuId the resource ID of the menu
     * @param menu the menu to update
     */
    public void addMenu(Integer menuId, Menu menu) {
        this.menus.put(menuId, menu);
    }

    /**
     * Gets the menu map
     * @return map resource ID to menu object
     */
    public Map<Integer, Menu> getMenus() {
        return menus;
    }

    // replaces the AppsUIElement with a SpecialXMLTag(including all its data) and returns the new SpecialXMLTag
    public SpecialXMLTag extendAppsUIElementWithSpecialTag(int elementID) {

        SpecialXMLTag spec = null;
        AppsUIElement oldElement = null;

        // search the element
        AppsUIElement uiE = getUiElement(elementID);
        // check if it is a SpecialXMLTag or not
        if (!(uiE instanceof SpecialXMLTag)) {
            oldElement = uiE;
            // create a new SpecialXMLTag object with the data of uiE
            spec = new SpecialXMLTag(uiE);
        } else {
            // the given element is a SpecialXMLTag, so just return it
            return (SpecialXMLTag) uiE;
        }

        if ((spec == null) || oldElement == null)
            throw new IllegalArgumentException("no AppsUIElement with this Id found: " + elementID);

        // replace the AppsUIElement with the SpecialTag inside the elements list of this object
        uiElementsOfApp.put(spec.getId(), spec);
        // return the object with id elementID as SpecialXMLTag object
        return spec;
    }

    // returns a string with the inactive text of this element. depth marks how many hierarchy levels should be searched
    public Map<String, String> getInactiveContextText(int elementId, int depth) {
        // call getSurroundingElementIDs(id, depth)
        // and then iterate over this list and get the text
        List<Integer> children = uiElementsOfApp.get(elementId).getHierarchyChildren(depth, xmlLayoutFiles, uiElementsOfApp);

        Map<String,String> res = new HashMap<>();
        for (int uiEID : children) {
            AppsUIElement uiE = uiElementsOfApp.get(uiEID);
            Map<String, String> childrenText = uiE.getTextIfInactiveListener();
            childrenText.keySet().forEach(k->{
                if(!res.containsKey(k)){
                    res.put(k, childrenText.get(k));
                }
                else{
                    String currentValue = res.get(k);
                    res.replace(k, currentValue + "#" + childrenText.get(k));
                }
            });
        }
        return res;
    }


    // returns a list of elements ids which surrounds the element with the id id.
    // depth describes how much hierarchy levels should be searched arround the id element
    private List<Integer> getSurroundingElementIDs(int id, int depth) {
        throw new NotImplementedException("not impl yet");
    }

    public String getName() {
        return Helper.getApkName().replace(".apk", "");
    }
    public String getBaseName() {
        return name;
    }

    @Deprecated
    public void addScreens(Screen screen) {
        this.screens.add(screen);
    }

    public void addXMLLayoutFile(XMLLayoutFile sc) {
        if (!xmlLayoutFiles.containsKey(sc.getId()))
            xmlLayoutFiles.put(sc.getId(), sc);
        else
            logger.error("xmlFile could not be added to map because id (as key) is contained in xmlFiles map");
    }

    public void setIntentFilters(List<String> intentFilters) {
        this.intentFilters = intentFilters;
    }

    public List<String> getIntentFilters() {
        return intentFilters;
    }

    public Map<Integer, Set<Integer>> getMergedLayoutFileIDs() {
        return mergedLayoutFileIDs;
    }

    public Map<String, Set<Integer>> getActivityToXMLLayoutFiles() {
        return activityToXMLLayoutFiles;
    }

    public void addDialog(Dialog dia) {
        dialogs.put(dia.getId(), dia);
    }

    public XMLLayoutFile getXmlLayoutFile(int elementID) {
        return xmlLayoutFiles.get(elementID);
    }

    // use this with attention. only AppController is allowed to call this!
    public Collection<AppsUIElement> getAllUIElements() {
        return uiElementsOfApp.values();
    }

    // use this with attention. only AppController is allowed to call this!
    public Map<String, Set<Integer>> getFragmentClassToLayout() {
        return fragmentClassToLayout;
    }

    public Set<Integer> getIncludeTagIDs() {
        return includeTagIDs;
    }

    public void addIncludeTagIDs(int includeTagID) {
        this.includeTagIDs.add(includeTagID);
    }

    public Set<Integer> getFragmentTagIDs() {
        return fragmentTagIDs;
    }

    public void addFragmentTagIDs(int fragmentTagID) {
        this.fragmentTagIDs.add(fragmentTagID);
    }

    public boolean containsUiElement(int id) {
        return uiElementsOfApp.containsKey(id);
    }

    public boolean containsXMLLayoutFile(int id) {
        return xmlLayoutFiles.containsKey(id);
    }

    public boolean containsDialog(int id) {
        return dialogs.containsKey(id);
    }

    public Dialog getDialog(int dialogID) {
        return dialogs.get(dialogID);
    }

    public Map<Integer, Dialog> getDialogs() {
        return dialogs;
    }

    public UiElement getUiElementWithListenerById(Integer resId) {
        return uiElementsWithListener.get(resId);
    }

    public Collection<UiElement> getUiElementsWithListeners() {
        return uiElementsWithListener.values();
    }

    public void addUiElementsWithListener(Integer resId, UiElement distinctUiElement) {
        this.uiElementsWithListener.put(resId, distinctUiElement);
    }
}


// TODO check: this method is never used.... simple output method of all XMLLayoutFiles plus Dialogs
//	private void writeXmlLayoutsJSON(String appOutputDir) throws IOException {
//		File f = null;//createFile(appOutputDir, "xmlLayoutFiles");
//
//		FileOutputStream writerStream = new FileOutputStream(f, true);
//		try {
//			for (XMLLayoutFile xmlF: this.getAllXMLLayoutFiles()){
//				writerStream.write(xmlF.toString().getBytes());
//				writerStream.write("\n".getBytes());
//			}
//			for (Dialog d : this.getDialogsOfApp()){
//				writerStream.write(d.toString().getBytes());
//				writerStream.write("\n".getBytes());
//			}
//		} finally {
//			writerStream.close();
//		}
//
//	}

// returns list of childs of the element with the id : parentID
//	public List<AppsUIElement> retrieveChildElementsByParentId(String parentId, List<AppsUIElement> childs){
//		for (XMLLayoutFile lf : xmlLayoutFiles){
//			for (AppsUIElement uiE : lf.getUIElements()){
//				for(AppsUIElement parent : uiE.getParents()){
//					if(parent != null && !StringUtils.isEmpty(parent.getUIID()) && parent.getUIID().equals(parentId)){
//						if(childs.contains(uiE)){
//							continue;
//						}
//						childs.add(uiE);
//						retrieveChildElementsByParentId(uiE.getUIID(), childs);
//					}
//				}
//			}
//		}
//		return childs;
//	}

// returns a list of AppsUiElement that has at least one listener
//	public List<AppsUIElement> getAllUIElementsWithListener(){
//		List<AppsUIElement> res = new ArrayList<AppsUIElement>();
//		for (AppsUIElement uiE: this.getAllUIElementsOfApp()){
//			if (uiE.hasElementListeners()){
//				res.add(uiE);
//			}
//		}
//		return res;
//	}
