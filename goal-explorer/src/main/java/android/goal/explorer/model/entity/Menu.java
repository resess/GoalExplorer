package android.goal.explorer.model.entity;

import java.util.ArrayList;
import java.util.List;

public class Menu extends AbstractEntity {

    private String button;
    private List<Integer> items;

    private String layoutFile;

    public Menu(Integer resId) {
        super(resId, Type.MENU);
        items = new ArrayList<>();
    }

    public Menu(Integer resId, List<Integer> items) {
        super(resId, Type.MENU);
        this.items = items;
    }

    /**
     * Gets the resource id of the items
     * @return The resource ids of the items
     */
    public List<Integer> getItems() {
        return items;
    }

    /**
     * Adds an item to this entity
     * @param item the item to be added
     * @return true if successfully added
     */
    public boolean addItem(Integer item) {
        return items.add(item);
    }

    /**
     * Adds a list of items to this entity
     * @param items the list of items to be added
     * @return true if successfully added
     */
    public boolean addItems(List<Integer> items) {
        return this.items.addAll(items);
    }

    /**
     * Gets the layout file associated with this menu
     * @return The layout file
     */
    public String getLayoutFile() {
        return layoutFile;
    }

    /**
     * Sets the layout file
     * @param layoutFile The layout file
     */
    public void setLayoutFile(String layoutFile) {
        this.layoutFile = layoutFile;
    }

    /**
     * Gets the button which opens the menu
     * @return The button
     */
    public String getButton() {
        return button;
    }

    /**
     * Sets the button which opens the menu
     * @param button The button
     */
    public void setButton(String button) {
        this.button = button;
    }

    @Override
    public int hashCode(){
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((items == null) ? 0 : items.hashCode());
        result = prime * result + ((button == null) ? 0 : button.hashCode());
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

        Menu other = (Menu) obj;

        if (items == null) {
            if (other.getItems() != null)
                return false;
        } else if (!items.equals(other.getItems()))
            return false;

        if (button == null) {
            return other.getButton() == null;
        } else return button.equals(other.getButton());
    }

    @Override
    public String toString(){
        List<String> itemString = new ArrayList<>();
        items.forEach(x -> itemString.add(Integer.toString(x)));
        return "Menu - resId: " + getResId() + "; items: " + itemString;
    }

    @Override
    public Menu clone() {
        Menu menu = new Menu(getResId(), getItems());
        if (button != null) menu.setButton(button);
        if (layoutFile != null) menu.setLayoutFile(layoutFile);
        if (getParentClass() != null) menu.setParentClass(getParentClass());
        if (getCallbackMethods() != null) menu.addCallbackMethods(getCallbackMethods());
        return menu;
    }
}
