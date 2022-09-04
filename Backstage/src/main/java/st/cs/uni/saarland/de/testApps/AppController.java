package st.cs.uni.saarland.de.testApps;

import android.content.DialogInterface;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import st.cs.uni.saarland.de.dissolveSpecXMLTags.AdapterViewInfo;
import st.cs.uni.saarland.de.dissolveSpecXMLTags.ListViewInfo;
import st.cs.uni.saarland.de.entities.*;
import st.cs.uni.saarland.de.helpClasses.Helper;
import st.cs.uni.saarland.de.reachabilityAnalysis.ApiInfoForForward;
import st.cs.uni.saarland.de.reachabilityAnalysis.UiElement;
import st.cs.uni.saarland.de.reachabilityAnalysis.PreferenceResolver;
import st.cs.uni.saarland.de.saveData.Label;
import st.cs.uni.saarland.de.searchListener.XMLDefinedListenerIterator;
import st.cs.uni.saarland.de.searchMenus.MenuInfo;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Isa on 05.02.2016.
 */
public class AppController {

    private static final Logger logger = LoggerFactory.getLogger(AppController.class);
    private static Application app;
    private static AppController appController;

    public AppController(Application app) {//FIXME: remove ASAP
        this.app = app;
        if (this.app != null)
            logger.warn("Application object object was tried to be replaced");
    }

    public static AppController getInstance() {
        if (null == appController) {
            logger.error("AppController was not created before getInstance() call");
            throw new IllegalArgumentException("AppController was not created before getInstance call");
        } else if (null == app) {
            logger.error("app was not initialised in AppController");
            throw new IllegalArgumentException("app was not initialised in AppController");
        }
        return appController;
    }

    public static AppController getInstance(Application app) {
        if (null == appController) {
            appController = new AppController(app);
        } else {
            logger.error("AppController object was tried to be replaced");
        }
        return appController;
    }

    public void addDataToAdapterViewObject(int adapterViewId,  Collection<Listener> listeners, String assignedActivity){
        AdapterView adapterView = null;
        try {
            Set<AppsUIElement> allPotentialAdapterViews = app.getAllUIElementsForId(adapterViewId);
            if(allPotentialAdapterViews.size() == 1)
                adapterView = (AdapterView)allPotentialAdapterViews.stream().findFirst().get();
            else adapterView = (AdapterView) allPotentialAdapterViews.stream().filter(elem -> elem instanceof AdapterView && assignedActivity.equals(((AdapterView)elem).getAssignedActivity())).findFirst().orElse(null);
            if(adapterView.getItemIDs() == null || adapterView.getItemIDs().isEmpty()){
                for (Listener l : listeners) {
                    //logger.debug("Adding listener {} for element {}", l, adapterView);
                    adapterView.addListener(l);
                }
            }
            else for (int eId: adapterView.getItemIDs()) {
                AppsUIElement uiE = null;
                try {
                    uiE = app.getUiElement(eId);
                    for (Listener l : listeners) {
                        //logger.debug("Adding listener {} for element {}", l, uiE);
                        uiE.addListener(l);
                    }
                }
                catch(NullPointerException e) {
                    logger.error("AppController:addDataToListViewObject: AppsUIElement not found with id: " + eId);
                    Helper.saveToStatisticalFile("AppController:addDataToListViewObject: AppsUIElement not found with id: " + eId);

                }
            }
        }
        catch(NullPointerException e){
            logger.error("AppController:addDataToListViewObject: AppsUIElement not found with id: "+adapterViewId);
        }
    }

    public void addDataToMenuObject(int menuID, String kindOfMenu, Collection<Listener> layoutListener) {
        Menu menu = null;
        try {
            menu = (Menu) app.getXmlLayoutFile(menuID);
            menu.setKindOfMenu(kindOfMenu);
            menu.addListeners(layoutListener);
            // also attached the layout listeners to every element of the menu
            for (int eID : menu.getUIElementIDs()) {
                AppsUIElement uiE = null;
                try {
                    uiE = app.getUiElement(eID);
                } catch (NullPointerException e) {
                    logger.error("AppController:addDataToMenuObject: AppsUIElement not found with id: " + eID);
                    Helper.saveToStatisticalFile("AppController:addDataToMenuObject: AppsUIElement not found with id: " + eID);
                }
                for (Listener l : layoutListener) {
                    uiE.addListener(l);
                }
            }
            app.updateMenu(menuID, menu);
        } catch (NullPointerException e) {
            logger.error("AppController:addDataToMenuObject: XMLLayoutFile not found with id: " + menuID);
            Helper.saveToStatisticalFile("AppController:addDataToMenuObject: XMLLayoutFile not found with id: " + menuID);
        }


    }

    public void addDataToTabViewMenuObject(int menuID, String kindOfMenu, Collection<Listener> layoutListener, AppsUIElement textView) {
        if ("TabView".equals(kindOfMenu)) {
            this.addDataToMenuObject(menuID, kindOfMenu, layoutListener);
            try {
                Menu tabViewMenu = (Menu) app.getXmlLayoutFile(menuID);
                // check if the textView is a new object
                if (app.getUiElement(textView.getId()) == null) {
                    tabViewMenu.addElementIDs(textView.getId());
                    app.addUiElementsOfApp(textView);
                }
            } catch (NullPointerException e) {
                logger.error("AppController:addDataToTabViewMenuObject: XMLLayoutFile or AppsUIElement not found with id: xmlF: " + menuID + "; uiEID: " + textView.getId());
                Helper.saveToStatisticalFile("AppController:addDataToTabViewMenuObject: XMLLayoutFile not found with id: " + menuID);
            }
        }
    }

    public void addDialog(Dialog dialog) {
        if (null != dialog) {
            app.addDialog(dialog);
            app.addXMLLayoutFileToActivity(dialog.getShowingClass(), dialog.getId());
        } else {
            logger.error("Null Dialog was tryied to be added in AppController");
            Helper.saveToStatisticalFile("Null Dialog was tryied to be added in AppController");
        }
    }

    public void addTab(Tab tab) {
        if (tab != null) {
            app.addTab(tab);
        } else {
            logger.error("Null Tab was tryied to be added in AppController");
            Helper.saveToStatisticalFile("Null Tab was tryied to be added in AppController");
        }
    }

    public void addFragmentClassToItsViewIDs(String fragmentName, Set<Integer> viewIDs) {
        app.addFragmentClassToViewIDs(fragmentName, viewIDs);
    }

    // adds a listener to the specified element with the id elementID
    public void addListener(int elementID, Listener listener) {
        try {
            //filter by listener declaring class
            AppsUIElement element = app.getUiElement(elementID);
            //TODO listeners are overriden since same listener class, need to have multiple element somehow ?
            //maybe should make a copy of the element with the declaring class?
            /*if(StringUtils.isBlank(element.getDeclaringClass())){
                //element.
                element.setDeclaringClass(listener.getDeclaringClass());
            }*/
            //TODO check for multiple ui elements with same ids besides (adapterview)
            if(element instanceof AdapterView){
                Collection<Listener> listeners = new HashSet<Listener>();
                listeners.add(listener);
                if(!StringUtils.isBlank(listener.getDeclaringClass()))
                    appController.addDataToAdapterViewObject(elementID, listeners, listener.getDeclaringClass());//((AdapterView) element).getAssignedActivity());
                else //Needed ?
                    appController.addDataToAdapterViewObject(elementID, listeners, ((AdapterView) element).getAssignedActivity());
            }
            else
                element.addListener(listener);

        } catch (NullPointerException e) {
            logger.error("Couldn't add listener, AppsUIElement not found with id: " + elementID);
            Helper.saveToStatisticalFile("AppsUIElement not found with id: " + elementID);
        }
    }

    // add the menu with its element to the app
    public void addNewNavigationDropDownMenu(Menu menu, AppsUIElement uiElement) {
        // check if it is really a NEW menu/XMLLayoutFIle object -> check for id and check if kindOfMenu is Nav.DropDown
        if (null != menu && menu.getKindOfMenu().equals("NavDropDown")) {
            try {
                if (null == app.getXmlLayoutFile(menu.getId())) {
                    // check if the uiE is really the element of the menu and that the menu don't have any more
                    if (menu.getUIElementIDs().contains(uiElement.getId()) && menu.getUIElementIDs().size() == 1) {
                        app.addUiElementsOfApp(uiElement);
                        app.addXMLLayoutFile(menu);
                    }
                }
            } catch (NullPointerException e) {
                logger.error("Menu not found with id: " + menu.getId());
                Helper.saveToStatisticalFile("Menu not found with id: " + menu.getId());
            }
        } else {
            logger.error("tried to add other menu than Navigation Drop Down! (not allowed!) or menu was null");
            Helper.saveToStatisticalFile("tried to add other menu than Navigation Drop Down! (not allowed!) or menu was null");
        }
    }

    public void addText(int elementID, String declaringSootClass, String text) {
        AppsUIElement uiE = app.getUiElement(elementID);
        if (null != uiE)
            uiE.addText(text, declaringSootClass);
        else {
            logger.error("AppController:addText: elementID not found for adding text {} in class {} : elementID: " + elementID, text, declaringSootClass);
            Helper.saveToStatisticalFile("AppController:addText: elementID not found for adding text: elementID: " + elementID);
        }
    }

    public void addTwoMergedLayouts(int rootLayoutID, int includedLayoutID) {
        app.addMergedLayoutIDs(rootLayoutID, includedLayoutID);
    }

    public void addXMLLayoutFileToActivity(String activityClassName, int layoutID) {
        app.addXMLLayoutFileToActivity(activityClassName, layoutID);
    }

    public void addPreferenceToActivity(String activityClassName, String layoutID) {
        app.addXMLPreferenceToActivity(activityClassName, layoutID);
    }

    public void addXMLLayoutFileToSpecialXMLTag(int specXMLTagID, Collection<Integer> addedXMLLayoutFileIDs) {
        if (addedXMLLayoutFileIDs == null && addedXMLLayoutFileIDs.size() == 0)
            Helper.saveToStatisticalFile("AppController:addXMLLayoutFileToSpecialXMLTag was called with params null or zero");
        try {
            for (int addedLay : addedXMLLayoutFileIDs) {
                addXMLLayoutFileToSpecialXMLTag(specXMLTagID, addedLay);
            }
        } catch (NullPointerException e) {
            throw e;
            //logger.error("AppController:addXMLLayoutFileToSpecXMLTag: SpecXMLTag not found with id: " + specXMLTagID);
            //Helper.saveToStatisticalFile("SpecXMLTag not found with id: " + specXMLTagID);
        }
    }

    public void addXMLLayoutFileToSpecialXMLTag(int specXMLTagID, int addedXMLLayoutFileID) {
        AppsUIElement element = app.getUIElementByType(specXMLTagID, "");
        try {
            SpecialXMLTag specTag = (SpecialXMLTag)element;
                    specTag.addXmlFile(addedXMLLayoutFileID);

            // add parents and children accordingly
            XMLLayoutFile xmlFadded = app.getXmlLayoutFile(addedXMLLayoutFileID);
            if(xmlFadded.getRootElementID() == 0)
                xmlFadded.setRootElementID(specXMLTagID);
            else {
                AppsUIElement rootOfAddedLayout = app.getUiElement(xmlFadded.getRootElementID());
                specTag.addChildIDDyn(
                        rootOfAddedLayout.getId());
                rootOfAddedLayout.addParentDyn(specXMLTagID);
            }
        } catch (NullPointerException | ClassCastException e ) { //catch castexception?
            logger.error("AppController:addXMLLayoutFileToSpecialXMLTag: SpecXMLTag or XMLLayoutFile not found with id: {} {}",specXMLTagID, element);
            Helper.saveToStatisticalFile("SpecXMLTag not found with id: " + specXMLTagID);
        }
    }

    public void extendAppsUIElementWithSpecialTag(int elementID) {
        // look at the old implementation in XMLLayouFile class
        try {
            app.extendAppsUIElementWithSpecialTag(elementID);
        } catch (NullPointerException e) {
            logger.error("AppsUIElement not found with id: " + elementID);
            Helper.saveToStatisticalFile("AppsUIElement not found with id: " + elementID);
        }
    }

    // returns a Menu object with the same data than the XMLLayoutFile with the xmlLayoutFileID
    // and replaces the XMLLayoutFile object with the Menu object in the list of all XMLLayoutFiles in this class
    public void extendXMLLayoutFileToMenu(int xmlLayoutFileID) {
        app.expandXMLLayoutFileWithMenu(xmlLayoutFileID);
    }

    public void extendPhantomXMLFileForDynMenu(int xmlLayoutFileID, MenuInfo info){
        app.addPhantomXMLLayoutForDynMenu(xmlLayoutFileID, info);
    }

    public void extendAdapterViewWithDynItems(int id, AdapterViewInfo adapterViewInfo) {
        app.extendAdapterViewWithDynItems(id, adapterViewInfo);
    }

    // used for tests
    public Set<String> getAPIsOfElement(int elementID) {
        AppsUIElement uiE = app.getUiElement(elementID);
        Set<String> apis = new HashSet<String>();
        uiE.getListernersFromElement().forEach(x -> apis.addAll(x.getCalledAPISignatures()));
        return apis;
    }

    public List<String> getActivityNames() {
        return app.getActivities().stream().map(x->x.getName()).collect(Collectors.toList());
    }

    public Map<String, Set<Integer>> getActivityToXMLLayoutFiles() {
        return app.getActivityToXMLLayoutFiles();
    }

    // throws exception of fragTagID is not an XMLFragment
    public String getFragmentClassFromTag(int fragTagID) {
        try {
            XMLFragment fragE = (XMLFragment) app.getUIElementByType(fragTagID, "fragment");
            return fragE.getClassName();
        } catch (NullPointerException e) {
            logger.error("XMLFragment not found with id: " + fragTagID);
            Helper.saveToStatisticalFile("XMLFragment not found with id: " + fragTagID);
        }
        return null;
    }

    public Iterator getFragmentTagIDIterator() {
        return app.getFragmentTagIDs().iterator();
    }

    public Iterator getIncludeTagIDIterator() {
        return app.getIncludeTagIDs().iterator();
    }

    // used for tests
    public Collection<Integer> getIncludedLayoutsFromElement(int elementID) {
        if (app.containsUiElement(elementID))
            return app.getUiElement(elementID).getIncludedLayouts(app.getXMLLayoutFilesMap(), app.getUIElementsMap(), new HashSet<Integer>());
        else {
            logger.error("getIncludedLayoutsFromElement: Did not find element with elementID: " + elementID);
            Helper.saveToStatisticalFile("getIncludedLayoutsFromElement: Did not find element with elementID: " + elementID);
        }
        return null;
    }

    // used for tests
    public Collection<Integer> getIncludedLayoutsFromXMLLayoutFileElements(int xmlFID) {
        XMLLayoutFile xmlF = app.getXmlLayoutFile(xmlFID);
        return xmlF.getLayoutIDsWithIncludeAndDynamicalInclude(app.getXMLLayoutFilesMap(), app.getUIElementsMap(), new HashSet<Integer>());
    }

    public XMLDefinedListenerIterator getIteratorOfXMLDefinedListeners() {
        return new XMLDefinedListenerIterator(app.getUIElementsMap());
    }

    // returns a set of ids of elements that are the neighbours of the element ID element of depth level that is given
    // parentDepth describes how far parent should get considered:
    // <0: all parents; 0: no parents; x: depth (level up) (how much parents)
    // if an element has more than one parent, the layouts get splitted -> Set<Set<Integer>>
    // childrenDepth describes how far children should get considered:
    // <0: all children; 0: no children; x: depth (how much level of childrens)
    // childrenOfParents describes how far children of parents should be considered.
    // this number cannot be higher than the parentDepth. it counts from the analysed element up.
    // <0: all children of parents where a parent was considered; 0: no children of parents at all;
    // x: the number of level up, the children of parents should be considered
    public List<Set<Integer>> getNeighbours(int elementID, int parentLevel, int childrenOfParenLevel, int childrenLevel) {
        AppsUIElement uiE = app.getUiElement(elementID);
        List<Set<Integer>> neighbours = uiE.getNeighbours(parentLevel, childrenLevel, childrenOfParenLevel, app.getUIElementsMap());
        return neighbours;
    }

    // returns a list of UIElement objects. these objects contain the information of listeners that are needed for
    // reachability analysis
    public List<UiElement> getUIElementObjectsForReachabilityAnalysis(boolean withListener) {
        List<UiElement> reachabilityElements = new ArrayList<UiElement>();
        // iter through all ui elements which were found in the ui analysis
        for (AppsUIElement uiE : app.getAllUIElements()) {
            //if (uiE.getKindOfUiElement().equals("listviewitem") || uiE.getKindOfUiElement().equals("spinneritem"))
                //logger.debug("Found a list/spinner view item for with listener reachability {}", uiE);
            if(uiE.getKindOfUiElement().equals("listview")){
                ListView listView = (ListView)uiE;
                if(listView.getItemIDs().size() == 0){
                    //logger.debug("Found an empty list view for reachability analysis");
                }
                else continue;
            }
            if(uiE.getKindOfUiElement().equalsIgnoreCase("spinner")){ //what about what extends Spinner
                logger.debug("Found a spinner {}", uiE);
                continue;
            }
            if (uiE.getKindOfUiElement().equalsIgnoreCase("PreferenceScreen")) {
                logger.debug("Found a preference screen FOR REACHABILITY {}",uiE);
                PreferenceResolver.v().storePreference(uiE);
            }
            // check if element has a listener attached
            // if with layout listeners change to uiE.hasListenersOfElementsAndLayout(..)
            if (withListener && !uiE.hasElementListener()){
                if(!uiE.getIntent().isEmpty())
                    //We do not add this for reachability analysis
                    createUiElementWithIntent(uiE.getId(), uiE.getTextFromElement(), uiE.getKindOfUiElement(), uiE.getDeclaringClass(), uiE.getIntent());
                //need a fake listener, and to load the class I guess
                continue;
            }

            // don't analyse this tags, they have only layout listener assigned which other tags also have
            //TODO only add a listener for the listview if elements are empty
            if (uiE.getKindOfUiElement().equals("merge") || uiE.getKindOfUiElement().equals("menu") || uiE.getKindOfUiElement().equals("include")) {
                continue;
            }
           

            Set<Integer> layoutsOfElement = new HashSet<Integer>();
            for (XMLLayoutFile xmlF : app.getAllXMLLayoutFiles()) {
                if (xmlF.containsAppsUIElementWithIncludeAndDynamicalInclude(uiE.getId(), app.getXMLLayoutFilesMap())) {
                    layoutsOfElement.addAll(xmlF.getLayoutIDsWithIncludeAndDynamicalInclude(app.getXMLLayoutFilesMap(), app.getUIElementsMap(), new HashSet<Integer>()));
                }
            }
            // create the same set as layoutsOfElement, but with the ids as string
            final String id = String.valueOf(uiE.getId());
            if (uiE.hasElementListener()) {

                Collection<Listener> listeners = uiE.getListernersFromElement();

                Collection<UiElement> newUiElements = createUiElementFromListeners(listeners, uiE.getId(), id, uiE.getTextFromElement(), uiE.getKindOfUiElement());
                reachabilityElements.addAll(newUiElements);
            } else { //Throws an exception (cause no signature), shouldn't go in this branch?
                reachabilityElements.add(createUiElementWithoutListener(uiE.getId(), uiE.getTextFromElement(), uiE.getKindOfUiElement()));
            }
        }

        //for preference screen
        //we want a uielement for each preference object with id, text, 


        // iter through all XMLLayoutFiles (Menus) to catch all layout listeners:
        for (XMLLayoutFile menu : app.getAllXMLLayoutFiles()) {
            if (menu.hasLayoutListeners()) {
                final String id = String.valueOf(menu.getId());
                Collection<UiElement> newUiElements = createUiElementFromListeners(menu.getLayoutListeners(), menu.getId(), id, new HashMap<>(), "");
                reachabilityElements.addAll(newUiElements);
            }
        }

        // iter through all dialogs to catch all layout listeners:
        for (Dialog dialog : app.getDialogsOfApp()) {
            if (!dialog.getNegativeListener().isEmpty()) {
                HashMap<String, String> text = new HashMap<>();
                text.put("default_value", dialog.getNegText());
                reachabilityElements.addAll(createUiElementFromListeners(dialog.getNegativeListener(),  Content.getNewUniqueID(), String.valueOf(DialogInterface.BUTTON_NEGATIVE), dialog.getId(), text, dialog.getKindOfElement()));
            }
            if (!dialog.getPosListener().isEmpty()) {
                HashMap<String, String> text = new HashMap<>();
                text.put("default_value", dialog.getPosText());
                reachabilityElements.addAll(createUiElementFromListeners(dialog.getPosListener(), Content.getNewUniqueID(),  String.valueOf(DialogInterface.BUTTON_POSITIVE), dialog.getId(),text, dialog.getKindOfElement()));
            }
            if (!dialog.getNeutralListener().isEmpty()) {
                HashMap<String, String> text = new HashMap<>();
                text.put("default_value", dialog.getNeutralText());
                reachabilityElements.addAll(createUiElementFromListeners(dialog.getNeutralListener(), Content.getNewUniqueID(),  String.valueOf(DialogInterface.BUTTON_NEUTRAL), dialog.getId(), text, dialog.getKindOfElement()));
            }
            if (!dialog.getItemListener().isEmpty()) {
                HashMap<String, String> text = new HashMap<>();
                text.put("default_value", dialog.getItemTexts());
                reachabilityElements.addAll(createUiElementFromListeners(dialog.getItemListener(), Content.getNewUniqueID(),  String.valueOf(dialog.getId()), dialog.getId(), text, dialog.getKindOfElement()));
            }
        }

        return reachabilityElements;
    }



    public Collection<Integer> getViewsOfClass(String className) {
        return app.getMergedLayoutFileIDs().get(className);
    }

    public Collection<Integer> getViewsOfFragmentClass(String className) {
        return app.getFragmentClassToLayout().get(className);
    }

    // saves the results of the reachability analysis inside the data structure
    public void mergeReachabilityAnalysisResults(Map<UiElement, List<ApiInfoForForward>> resultsOfReachabilityAnalysis) {

        for (Map.Entry<UiElement, List<ApiInfoForForward>> entry : resultsOfReachabilityAnalysis.entrySet()) {
            int reachabilityID = entry.getKey().globalId;
            if (reachabilityID == 0) {
                // found lifecycle method
                // TODO do sth with it
                logger.error("Found reachability element with no id: kindOfElement: " + entry.getKey().kindOfElement);
                continue;
            }
            if (app.containsUiElement(reachabilityID)) {
                AppsUIElement uiE = app.getUiElement(reachabilityID);
                attachAPISignatureToListener(uiE.getId(), uiE.getListernersFromElement(), entry.getKey().signature, entry.getValue());
            } else if (app.containsXMLLayoutFile(reachabilityID)) {
                XMLLayoutFile xmlF = app.getXmlLayoutFile(reachabilityID);
                attachAPISignatureToListener(xmlF.getId(), xmlF.getLayoutListeners(), entry.getKey().signature, entry.getValue());
            } else if (app.containsDialog(reachabilityID)) {
                Dialog dialog = app.getDialog(reachabilityID);
                attachAPISignatureToListener(dialog.getId(), dialog.getLayoutListeners(), entry.getKey().signature, entry.getValue());
            } else {
                logger.error("Did not find AppsUiElement or XMLLayoutFile or Dialog from UIElement id: " + entry.getKey().globalId);
            }
        }
    }



    private void attachAPISignatureToListener(int elementID, Collection<Listener> possibleListeners, String listenerSignature, Collection<ApiInfoForForward> apiResults) {
        boolean checkIfListenerFound = false;
        // find the listener that was processed in this UIElement object
        for (Listener l : possibleListeners) {
            if (l.getSignature().equals(listenerSignature)) {
                checkIfListenerFound = true;
                for (ApiInfoForForward info : apiResults) {
                    l.addCalledAPISignatures(info.signature);
                }
                break; // listener was found, APIs attached
            }
        }
        if (!checkIfListenerFound) {
            logger.debug("Did not find listener of AppsUIElement from UIElement: " + elementID + " signature: " + listenerSignature);
            Helper.saveToStatisticalFile("Did not find listener of AppsUIElement from UIElement: " + elementID + " signature: " + listenerSignature);
        }
    }

    //FIXME: this is CRUTCH
    private Collection<UiElement> createUiElementFromListeners(Collection<Listener> listeners, int globalID, String elementID, Map<String, String> text, String kindOfElement) {
        return createUiElementFromListeners(listeners, globalID, elementID, -1, text, kindOfElement);
    }

    // global id is unique id, element id is Android id for element, for dialogs it reflects the kind of listener (positive, neg, neu, item)
    private Collection<UiElement> createUiElementFromListeners(Collection<Listener> listeners, int globalID, String elementID, int parentID, Map<String, String> text, String kindOfElement) {
        List<UiElement> reachabilityElements = new ArrayList<UiElement>();

        for (Listener listener : listeners) {
            //logger.debug("Checking listener for {} {} {}", globalID, kindOfElement, listener);
            //FIXME size of list is not correct
            if (listener == null) {
                Helper.saveToStatisticalFile("found listener null: uiE id : " + elementID);
                continue;
            }

            // find the SootMethod corresponding to the listener
            String listenerClassString = listener.getListenerClass();
            if (StringUtils.isBlank(listenerClassString)) {
                logger.error("Listener class was null or empty: uiEID: " + elementID);
                //TODO FIx
                continue;
            }
            SootClass listenerClass = Scene.v().getSootClass(listenerClassString);
            //TODO body is null for fragments method?
            SootMethod listenerMethod = listenerClass.getMethodUnsafe(listener.getListenerMethod());
            //logger.debug("Listener class {} AND method {} for {}", listenerClass, listenerMethod, kindOfElement);
            if (listenerMethod == null) {
                // FIXME exmple: layoutchallenges; it was former found by Soot....
                Helper.saveToStatisticalFile("AppController:createUIElementsFromListener: listener method was not found!!!: " + listener.getSignature());
                continue;
            }
            // create a new UiElement for the reachability analysis and assign the corresponding values from the uiE to it
            UiElement reachabilityElement = new UiElement();
            reachabilityElement.globalId = globalID;
            reachabilityElement.elementId = elementID;
            reachabilityElement.handlerMethod = listenerMethod;
            reachabilityElement.signature = listener.getSignature();
            reachabilityElement.kindOfElement = kindOfElement;
            reachabilityElement.declaringSootClass = listener.getDeclaringClass();
            if(parentID != -1)
                reachabilityElement.parentId = parentID;
            reachabilityElement.text.putAll(text);

            if(kindOfElement.contains("item")){
                AppsUIElement item = app.getUiElement(globalID);
                //logger.debug("Analyzing item {}", item);
                if(item.hasIdInCode()){
                    //logger.debug("Item has a different id in code  {}", item.getIdInCode());
                    reachabilityElement.idInCode = item.getIdInCode();
                }
                reachabilityElement.parentId = item.getParents().stream().findFirst().orElse(-1);
            }
            //here for list view, need to create new ui reachable element for each POSItion
            //with global id the list view id and idInCode the position?

            // add the reachabilityElement to the result list
            reachabilityElements.add(reachabilityElement);
        }
        return reachabilityElements;
    }



    private UiElement createUiElementWithIntent(int elementID,  Map<String, String> text, String kindOfElement, String declaringClass, String intent) {
        UiElement reachabilityElement = new UiElement();
        reachabilityElement.elementId = String.valueOf(elementID);
        reachabilityElement.globalId = elementID;
        reachabilityElement.kindOfElement = kindOfElement;
        reachabilityElement.declaringSootClass = declaringClass;
        reachabilityElement.targetSootClass = Scene.v().getSootClass(resolveClassName(intent));
        if(!kindOfElement.contains("item")){
            logger.warn("Found set intent for non menu element {}", kindOfElement);
            logger.warn("Tried to add a ui element without a listener or a signature {} {}", elementID, kindOfElement);
            return null;
        }
        AppsUIElement item = app.getUiElement(elementID);
        //logger.debug("Analyzing item with intent {}", item, intent);
        if(item.hasIdInCode()){
            //logger.debug("Item has a different id in code  {}", item.getIdInCode());
            reachabilityElement.idInCode = item.getIdInCode();
        }
        reachabilityElement.handlerMethod = new SootMethod("onItemWithIntentSelected",new ArrayList<>(), Scene.v().getTypeUnsafe("boolean"));
        //reachabilityElement.handlerMethod = new SootMethod()
        if(reachabilityElement.declaringSootClass != null) {
            reachabilityElement.handlerMethod.setDeclaringClass(Scene.v().getSootClass(reachabilityElement.declaringSootClass));
        }
        reachabilityElement.signature = "<"+declaringClass+": boolean onItemWithIntentSelected()>";
        reachabilityElement.parentId = item.getParents().stream().findFirst().orElse(-1);
        reachabilityElement.text.putAll(text);
        AppController.getInstance().updateUiElement(elementID, reachabilityElement);
        return reachabilityElement;
    }

    private String resolveClassName(String intent){
        String clnName = intent;
        if(clnName.contains("#")) {
            logger.warn("Multiple destinations for intent found, defaulting to first {}", clnName);
            logger.error("Multiple destinations for intent found, defaulting to first {}", clnName);
            clnName = clnName.split("#")[0];
        }
        clnName = clnName.replace("/" ,".");
        if (clnName.startsWith("L")) {
            clnName = clnName.substring(1);
        }
        if (clnName.endsWith(";")) {
            clnName = clnName.substring(0, clnName.length()-1);
        }
        return clnName;
    }

    private UiElement createUiElementWithoutListener(int elementID, Map<String, String> text, String kindOfElement) {
        // create a new UiElement for the reachability analysis and assign the corresponding values from the uiE to it
        UiElement reachabilityElement = new UiElement();
        reachabilityElement.elementId = String.valueOf(elementID);
        reachabilityElement.globalId = elementID;
        reachabilityElement.kindOfElement = kindOfElement;
        reachabilityElement.signature = "";
        reachabilityElement.text.putAll(text);
        return reachabilityElement;
    }

    public void updateUiElement(Integer resId, UiElement uiElement) {
        app.addUiElementsWithListener(resId, uiElement);
    }

    public Application getApp() {
        return app;
    }

/*
    // save the surrounding text of all elements. the surrounding text is calculated with these parameters:
    // parentDepth describes how far parent should get considered:
    // <0: all parents; 0: no parents; x: depth (level up) (how much parents)
    // if an element has more than one parent, the layouts get splitted -> Set<Set<Integer>>
    // childrenDepth describes how far children should get considered:
    // <0: all children; 0: no children; x: depth (how much level of childrens)
    // childrenOfParents describes how far children of parents should be considered.
    // this number cannot be higher than the parentDepth. it counts from the analysed element up.
    // <0: all children of parents where a parent was considered; 0: no children of parents at all;
    // x: the number of level up, the children of parents should be considered
    public List<Map.Entry<String, Map<String, String>>> saveTextToApiMatchingFromNeighbours(int parentDepth, int childrenDepth, int childrenOfParents) {
        List<Map.Entry<String, String>> results = new ArrayList<>();
        for (AppsUIElement uiE : app.getAllUIElements()) {
            // choice for text : uiE.getTextIfInactiveListener(), uiE.getTextIfInactiveAPI()
            Map<String, String> labelOfElement = uiE.getTextFromElement();
            Set<String> calledAPIsignatures = new HashSet<String>();
            uiE.getListernersFromElement().forEach(x -> calledAPIsignatures.addAll(x.getCalledAPISignatures()));

            List<Set<Integer>> neighbours = uiE.getNeighbours(parentDepth, childrenDepth, childrenOfParents, app.getUIElementsMap());

            // one set corresponds to one layout
            for (Set<Integer> oneLayout : neighbours) {
                Map<String, String> surroundingTextOfOneLayout = new HashMap<>();
                for (int neighbourElement : oneLayout) {
                    AppsUIElement neighbour = app.getUiElement(neighbourElement);
                    // choice for text : neighbour.getTextIfInactiveListener(), neighbour.getTextIfInactiveAPI()
                    Map<String, String> textFromNeighbour = neighbour.getTextFromElement();
                    textFromNeighbour.keySet().forEach(k->{
                        if(!surroundingTextOfOneLayout.containsKey(k)){
                            surroundingTextOfOneLayout.put(k, textFromNeighbour.get(k));
                        }
                        else{
                            String currentValue = surroundingTextOfOneLayout.get(k);
                            surroundingTextOfOneLayout.replace(k, currentValue + "#" + textFromNeighbour.get(k));
                        }
                    });
                    surroundingTextOfOneLayout.putAll(neighbour.getTextFromElement());
                }

                results.add(new AbstractMap.SimpleEntry<>(labelOfElement, surroundingTextOfOneLayout));
            }
        }
        return results;
    }*/

    @Deprecated
    private String getTextFromStyle(Set<Style> styles) {
        String allText = styles.stream()
                .map(s -> s.getText().trim())
                .map(s -> s.replaceAll("\\r|\\n", ""))
                .filter(t -> !t.isEmpty())
                .collect(Collectors.joining("#"));
        return allText;
    }


}
