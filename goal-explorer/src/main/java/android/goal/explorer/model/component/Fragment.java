package android.goal.explorer.model.component;

import android.goal.explorer.model.entity.Listener;
import android.goal.explorer.model.widget.AbstractWidget;
import com.thoughtworks.xstream.annotations.XStreamOmitField;
import soot.MethodOrMethodContext;
import soot.SootClass;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class Fragment extends AbstractComponent {

    @XStreamOmitField()
    private Set<Listener> listeners;
    @XStreamOmitField()
    private Set<AbstractWidget> widgets;

    @XStreamOmitField()
    private Set<Integer> resourceIds;
    @XStreamOmitField()
    private Set<Activity> parentActivities;

    @XStreamOmitField()
    private Set<MethodOrMethodContext> menuRegistrationMethods;
    @XStreamOmitField()
    private Set<MethodOrMethodContext> menuCallbackMethods;

    public Fragment(SootClass sootClass, Activity parentActivity) {
        super(sootClass.getName(), sootClass);
        this.parentActivities = new HashSet<>(Collections.singletonList(parentActivity));
        this.menuRegistrationMethods = new HashSet<>();
        this.menuCallbackMethods = new HashSet<>();
        this.resourceIds = new HashSet<>();
    }

    public Fragment(SootClass sootClass) {
        super(sootClass.getName(), sootClass);
        this.menuRegistrationMethods = new HashSet<>();
        this.menuCallbackMethods = new HashSet<>();
        this.resourceIds = new HashSet<>();
    }

    public Fragment(SootClass sootClass, Set<Integer> resourceIds) {
        super(sootClass.getName(), sootClass);
        this.resourceIds = resourceIds;
        this.menuRegistrationMethods = new HashSet<>();
        this.menuCallbackMethods = new HashSet<>();
        this.resourceIds = new HashSet<>();
    }

    /*
    Getters and setters
     */

    public Set<Listener> getListeners() {
        return listeners;
    }

    public Set<AbstractWidget> getWidgets() {
        return widgets;
    }

    public Set<Integer> getResourceIds() {
        return resourceIds;
    }

    public Set<Activity> getParentActivities() {
        return parentActivities;
    }

    /**
     * Adds a listener to the fragment
     * @param listener The listener to be added
     */
    public void addListener(Listener listener) {
        this.listeners.add(listener);
    }

    /**
     * Adds a widget to the fragment
     * @param widget The widget to be added
     */
    public void addWidget(AbstractWidget widget) {
        this.widgets.add(widget);
    }

    /**
     * Sets the resource id of the fragment
     * @param resourceIds The resource ids of the fragment
     */
    public void setResourceIds(Set<Integer> resourceIds) {
        this.resourceIds = resourceIds;
    }

    /**
     * Adds the parent activity to the fragment. This method adds another activity
     * @param parentActivity The parent activity
     */
    public void AddParentActivity(Activity parentActivity) {
        this.parentActivities.add(parentActivity);
    }

    /**
     * Gets the menu methods of this activity
     * @return The menu methods
     */
    public Set<MethodOrMethodContext> getMenuRegistrationMethods() {
        return menuRegistrationMethods;
    }

    /**
     * Adds the menu methods to this activity
     * @param menuMethods The menu methods to be added
     */
    public void addMenuRegistrationMethods(Set<MethodOrMethodContext> menuMethods) {
        this.menuRegistrationMethods.addAll(menuMethods);
    }

    /**
     * Adds a menu method to this activity
     * @param menuMethod The menu method to be added
     */
    public void addMenuRegistrationMethod(MethodOrMethodContext menuMethod) {
        this.menuRegistrationMethods.add(menuMethod);
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

    @Override
    public String toString(){
        return getName();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((listeners == null) ? 0 : listeners.hashCode());
        result = prime * result + ((widgets == null) ? 0 : widgets.hashCode());
        result = prime * result + (resourceIds.isEmpty() ? 0 : resourceIds.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        if (!super.equals(obj))
            return false;

        Fragment other = (Fragment) obj;
        return this.getMainClass().equals(other.getMainClass());
    }
}
