package android.goal.explorer.model.component;

import android.goal.explorer.data.android.constants.MethodConstants;
import android.goal.explorer.model.entity.IntentFilter;
import android.goal.explorer.model.entity.Listener;
import android.goal.explorer.model.widget.AbstractWidget;
import android.goal.explorer.utils.AxmlUtils;
import soot.MethodOrMethodContext;
import soot.SootClass;
import soot.jimple.infoflow.android.axml.AXmlNode;
import st.cs.uni.saarland.de.entities.Dialog;
import st.cs.uni.saarland.de.entities.Menu;
import st.cs.uni.saarland.de.entities.XMLLayoutFile;
import st.cs.uni.saarland.de.reachabilityAnalysis.UiElement;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static android.goal.explorer.utils.SootUtils.findAndAddMethod;

public class Activity extends AbstractComponent {

    private Set<MethodOrMethodContext> menuOnCreateMethods;
    private Set<MethodOrMethodContext> menuCallbackMethods;

    private Set<IntentFilter> intentFilters;
    private Set<Listener> listeners;
    private Set<Fragment> fragments;
    private Set<AbstractWidget> widgets;

    private Set<XMLLayoutFile> layoutFiles;

    private Integer resourceId;
    private Integer mainXmlLayoutResId;
    private Set<Integer> addedXmlLayoutResId;
    private String alias = null;

    private String parentCompString;
    private AbstractComponent parentComp;
    private Set<String> childCompStrings;
    private Set<AbstractComponent> childComps;

    // For BACKSTAGE
    private Map<Integer, XMLLayoutFile> layouts;
    private Map<Integer, UiElement> uiElementsMap;
    private Menu menu;
    private Set<Dialog> dialogs;

    public Activity(AXmlNode node, SootClass sc, String packageName) {
        super(node, sc, packageName);

        this.intentFilters = createIntentFilters(AxmlUtils.processIntentFilter(node, IntentFilter.Type.Action),
                AxmlUtils.processIntentFilter(node, IntentFilter.Type.Category));

        parentCompString = AxmlUtils.processNodeParent(node, packageName);
        resourceId = -1;

        menuOnCreateMethods = new HashSet<>();
        menuCallbackMethods = new HashSet<>();

        listeners = new HashSet<>();
        fragments = new HashSet<>();
        widgets = new HashSet<>();
        childCompStrings = new HashSet<>();
        childComps = new HashSet<>();

        // For BACKSTAGE
        layouts = new HashMap<>();
        uiElementsMap = new HashMap<>();
        layoutFiles = new HashSet<>();
        dialogs = new HashSet<>();
    }

    /* ========================================
                Getters and setters
       ========================================*/

    /**
     * Gets the resource id of this activity
     * @return The resource id of the activity
     */
    public Integer getResourceId(){ return resourceId; }

    /**
     * Sets the resource id of this activity
     * @param resourceId The resource id of the activity
     */
    public void setResourceId(Integer resourceId){ this.resourceId = resourceId; }

    /**
     * Gets the intent filters of this activity
     * @return The intent filters
     */
    public Set<IntentFilter> getIntentFilters() {
        return intentFilters;
    }

    /**
     * Adds a new intent filter to this activity
     * @param intentFilter The intent filter to be added
     */
    public void addIntentFilter(IntentFilter intentFilter) {
        this.intentFilters.add(intentFilter);
    }

    /**
     * Adds new intent filter to this activity
     * @param intentFilters The intent filters to be added
     */
    public void addIntentFilters(Set<IntentFilter> intentFilters) {
        this.intentFilters.addAll(intentFilters);
    }

    /**
     * Gets the menu methods of this activity
     * @return The menu methods
     */
    public Set<MethodOrMethodContext> getMenuOnCreateMethods() {
        return menuOnCreateMethods;
    }

    /**
     * Adds the menu methods to this activity
     * @param menuMethods The menu methods to be added
     */
    public void addMenuOnCreateMethods(Set<MethodOrMethodContext> menuMethods) {
        this.menuOnCreateMethods.addAll(menuMethods);
    }

    /**
     * Adds a menu method to this activity
     * @param menuMethod The menu method to be added
     */
    public void addMenuOnCreateMethod(MethodOrMethodContext menuMethod) {
        this.menuOnCreateMethods.add(menuMethod);
    }

    /**
     * Gets the menu methods of this activity
     * @return The menu methods
     */
    public Set<MethodOrMethodContext> getMenuCallbackMethods() {
        return menuCallbackMethods;
    }

    /**
     * Adds the menu methods to this activity
     * @param menuMethods The menu methods to be added
     */
    public void addMenuCallbackMethods(Set<MethodOrMethodContext> menuMethods) {
        this.menuCallbackMethods.addAll(menuMethods);
    }

    /**
     * Adds a menu method to this activity
     * @param menuMethod The menu method to be added
     */
    public void addMenuCallbackMethod(MethodOrMethodContext menuMethod) {
        this.menuCallbackMethods.add(menuMethod);
    }

    /**
     * Gets the resource id of the parse XML layout file of this activity
     * @return The resource id of the parse XML layout file of this activity
     */
    public Integer getMainXmlLayoutResId() {
        return mainXmlLayoutResId;
    }

    /**
     * Sets the resource id of the parse XML layout file of this activity
     * @param mainXmlLayoutResId The resource id of the parse XML layout file of this activity
     */
    public void setMainXmlLayoutResId(Integer mainXmlLayoutResId) {
        this.mainXmlLayoutResId = mainXmlLayoutResId;
    }

    /**
     * Gets the resource ids of the added XML layout file of this activity
     * @return The resource ids of the added XML layout file of this activity
     */
    public Set<Integer> getAddedXmlLayoutResId() {
        return addedXmlLayoutResId;
    }

    /**
     * Sets the resource id of the added XML layout file of this activity
     * @param addedXmlLayoutResId The resource id of the added XML layout file of this activity
     */
    public void setAddedXmlLayoutResId(Set<Integer> addedXmlLayoutResId) {
        this.addedXmlLayoutResId = addedXmlLayoutResId;
    }

    /**
     * Gets the alias of this activity
     * @return The alias of this activity
     */
    public String getAlias() {
        return alias;
    }

    /**
     * Sets the alias of this activity
     * @param target The alias of this activity
     */
    public void setAlias(String target) {
        alias = target;
    }

    /**
     * Gets all listeners of this activity
     * @return All listeners in this activity
     */
    public Set<Listener> getListeners() {
        return listeners;
    }

    /**
     * Adds new listeners to this activity
     * @param listeners The listeners to be added
     */
    public void addListeners(Set<Listener> listeners) {
        if (this.listeners == null)
            this.listeners = new HashSet<>();
        this.listeners.addAll(listeners);
    }

    /**
     * Adds a new listener to this activity
     * @param listener The listener to be added
     */
    public void addListener(Listener listener) {
        if (this.listeners == null)
            this.listeners = new HashSet<>();
        this.listeners.add(listener);
    }

    /**
     * Gets all fragments implemented in this activity
     * @return All fragments implemented in this activity
     */
    public Set<Fragment> getFragments() {
        return fragments;
    }

    /**
     * Adds a new fragment to this activity
     * @param fragment The new fragment to be added
     */
    public void addFragment(Fragment fragment) {
        if (this.fragments == null)
            this.fragments = new HashSet<>();
        this.fragments.add(fragment);
    }

    /**
     * Gets all widgets implemented in this activity
     * @return All widgets implemented in this activity
     */
    public Set<AbstractWidget> getWidgets() {
        return widgets;
    }

    /**
     * Gets the parent component name in String
     * @return The parent component name in String
     */
    public String getParentCompString() {
        return this.parentCompString;
    }

    /**
     * Sets the parent component name in String
     * @param parent The parent component name in String
     */
    public void setParentCompString(String parent) {
        this.parentCompString = parent;
    }

    /**
     * Adds a new widget to this activity
     * @param widget The new widget to be added to this activity
     */
    public void addWidget(AbstractWidget widget) {
        this.widgets.add(widget);
    }

    /**
     * Gets the parent component from manifest
     * @return The parent component from manifest
     */
    public AbstractComponent getParentComp() {
        return parentComp;
    }

    /**
     * Sets the parent component from manifest
     * @param parentComp The parent component from manifest
     */
    public void setParentComp(AbstractComponent parentComp) {
        this.parentComp = parentComp;
    }

    /**
     * Gets the child component (string) from manifest
     * @return The child component (string) from manifest
     */
    public Set<String> getChildCompStrings() {
        return childCompStrings;
    }

    /**
     * Adds a child component (string) from manifest to this activity
     * @param childCompString The child component (string) from manifest to be added
     */
    public void addChildCompString(String childCompString) {
        this.childCompStrings.add(childCompString);
    }

    /**
     * Gets the child component from manifest
     * @return The child component from manifest
     */
    public Set<AbstractComponent> getChildComps() {
        return childComps;
    }

    /**
     * Adds a child component from manifest
     * @param childComp The child component from manifest to be added
     */
    public void addChildComp(AbstractComponent childComp) {
        this.childComps.add(childComp);
    }

    /*============================
    for BACKSTAGE
     */
    /**
     * Gets the layouts associated with the activity
     * @return The layouts
     */
    public Map<Integer, XMLLayoutFile> getLayouts() {
        return layouts;
    }

    /**
     * Sets the layouts associated with the activity
     */
    public void setLayouts(Map<Integer, XMLLayoutFile> layouts) {
        this.layouts.putAll(layouts);
    }

    public void addLayout(Integer resId, XMLLayoutFile layout) {
        layouts.put(resId, layout);
    }

    public void getLayout(Integer resId) {
        layouts.get(resId);
    }


    public Menu getMenu() {
        return menu;
    }

    public void setMenu(Menu menu) {
        this.menu = menu;
    }

    public Set<Dialog> getDialogs() {
        return dialogs;
    }

    public void setDialogs(Set<Dialog> dialogs) {
        this.dialogs = dialogs;
    }

    public void addDialog(Dialog dialog) {
        this.dialogs.add(dialog);
    }

    /**
     * Gets the lifecycle methods before the activity is running
     * @return The lifecycle methods before the activity is running
     */
    public LinkedList<MethodOrMethodContext> getLifecycleMethodsPreRun() {
        LinkedList<MethodOrMethodContext> lifecycleMethodsPreRun = new LinkedList<>();

        List<String> lifecycleMethods = MethodConstants.Activity.getlifecycleMethodsPreRun();

        // Collect the list of lifecycle methods pre-run (in order)
        for (String lifecycleMethod : lifecycleMethods) {
            MethodOrMethodContext method = findAndAddMethod(lifecycleMethod, this);
            if (method != null) lifecycleMethodsPreRun.add(method);
        }
        return lifecycleMethodsPreRun;
    }

    /**
     * Gets the lifecycle methods when the activity is paused
     * @return The lifecycle methods when the activity is paused
     */
    public LinkedList<MethodOrMethodContext> getLifecycleMethodsOnPause() {
        LinkedList<MethodOrMethodContext> lifecycleMethodsOnPause = new LinkedList<>();

        List<String> lifecycleMethods = MethodConstants.Activity.getlifecycleMethodsOnPause();

        // Collect the list of lifecycle methods pre-run (in order)
        lifecycleMethods.iterator().forEachRemaining(x -> {
            MethodOrMethodContext method = findAndAddMethod(x, this);
            if (method!=null)
                lifecycleMethodsOnPause.add(method);
        });

        return lifecycleMethodsOnPause;
    }

    /**
     * Gets the lifecycle methods when the activity is stopped
     * @return The lifecycle methods when the activity is stopped
     */
    public LinkedList<MethodOrMethodContext> getLifecycleMethodsOnStop() {
        LinkedList<MethodOrMethodContext> lifecycleMethodsOnStop = new LinkedList<>();

        List<String> lifecycleMethods = MethodConstants.Activity.getlifecycleMethodsOnStop();

        // Collect the list of lifecycle methods pre-run (in order)
        lifecycleMethods.iterator().forEachRemaining(x -> {
            MethodOrMethodContext method = findAndAddMethod(x, this);
            if (method!=null)
                lifecycleMethodsOnStop.add(method);
        });

        return lifecycleMethodsOnStop;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((resourceId == null) ? 0 : resourceId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || this.getClass() != obj.getClass())
            return false;
        if (!super.equals(obj))
            return false;

        Activity other = (Activity) obj;

        if (!resourceId.equals(other.resourceId))
            return false;
        return getName().equals(other.getName());
    }

    public Collection<UiElement> getUiElements() {
        return uiElementsMap.values();
    }

    public UiElement getUiElement(Integer id) {
        return uiElementsMap.getOrDefault(id, null);
    }

    public void addUiElement(Integer id, UiElement uiElement) {
        uiElementsMap.put(id, uiElement);
    }
}
