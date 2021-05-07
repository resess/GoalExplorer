package android.goal.explorer.model.stg.node;

import android.goal.explorer.model.component.Activity;
import android.goal.explorer.model.component.Fragment;
import st.cs.uni.saarland.de.entities.Dialog;
import st.cs.uni.saarland.de.entities.Menu;

import java.util.HashSet;
import java.util.Set;

public class ScreenNode extends AbstractNode {

    private Set<Fragment> fragments;
    private Menu menu;
    private Set<Dialog> dialogs;

    /**
     * Constructor of screen node with no fragments nor menu/drawer
     * @param activity The activity
     */
    public ScreenNode(Activity activity) {
        super(activity);
        fragments = new HashSet<>();
        dialogs = new HashSet<>();
    }

    /**
     * Constructor of screen node with fragments
     * @param activity The activity
     * @param fragments The set of fragments
     */
    public ScreenNode(Activity activity, Set<Fragment> fragments) {
        super(activity);
        this.fragments = fragments;
        dialogs = new HashSet<>();
    }

    /**
     * A copy constructor
     * @param toClone The screen to clone
     */
    public ScreenNode(ScreenNode toClone) {
        super(toClone.getComponent());
        if (toClone.getFragments()!=null && !toClone.getFragments().isEmpty()) {
            fragments = toClone.getFragments();
        } else {
            fragments = new HashSet<>();
        }
        menu = toClone.getMenu();
        dialogs = toClone.getDialogs();
    }

    /**
     * Clone the screen
     * @param origScreenNode The original screen
     * @return The new screen which is a copy of the original screen
     */
    public ScreenNode clone(ScreenNode origScreenNode){
        ScreenNode screenNode = new ScreenNode((Activity) origScreenNode.getComponent());
        screenNode.addFragments(origScreenNode.fragments);
        if (origScreenNode.menu != null)
            screenNode.setMenu(origScreenNode.menu);
        if (!origScreenNode.dialogs.isEmpty())
            screenNode.setMenu(origScreenNode.menu);
        return screenNode;
    }

    /**
     * Gets the fragments
     * @return The fragments
     */
    public Set<Fragment> getFragments() {
        return fragments;
    }

    /**
     * Adds a set of fragments to the activity
     * @param fragments The set of fragments to be added
     */
    public void addFragments(Set<Fragment> fragments) {
        this.fragments.addAll(fragments);
    }

    /**
     * Adds a fragment to the activity
     * @param fragment The fragment to be added
     */
    public void addFragment(Fragment fragment) {
        this.fragments.add(fragment);
    }

    /**
     * Menu of this screen node
     * @return gets the menu
     */
    public Menu getMenu() {
        return menu;
    }

    /**
     * sets the menu of this screen node
     * @param menu The menu to set
     */
    public void setMenu(Menu menu) {
        this.menu = menu;
    }

    /**
     * Gets the dialog of this screen node
     * @return the dialog in this screen node
     */
    public Set<Dialog> getDialogs() {
        return dialogs;
    }

    /**
     * Sets the dialogs of this screen node
     * @param dialogs the dialogs to set
     */
    public void setDialogs(Set<Dialog> dialogs) {
        this.dialogs = dialogs;
    }

    @Override
    public String toString(){
        return getComponent().getName() + " fragments: " + fragments + " menu: " + menu + " dialogs" + dialogs;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((fragments == null) ? 0 : fragments.hashCode());
        result = prime * result + ((menu == null) ? 0 : menu.hashCode());
        result = prime * result + ((dialogs.isEmpty()) ? 0 : dialogs.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;

        ScreenNode other = (ScreenNode) obj;

        if (fragments == null) {
            if (other.fragments != null)
                return false;
        } else if (!fragments.equals(other.fragments))
            return false;

        if (menu == null) {
            return other.menu == null;
        } else return menu.equals(other.menu);
    }
}
