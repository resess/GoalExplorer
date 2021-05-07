package android.goal.explorer.model.entity;

import java.util.ArrayList;
import java.util.List;

public class Drawer extends AbstractEntity {

    private String button;
    private List<Integer> items;

    public Drawer(Integer resId) {
        super(resId, Type.DRAWER);
        items = new ArrayList<>();
    }

    public Drawer(Integer resId, List<Integer> items) {
        super(resId, Type.DRAWER);
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

        Drawer other = (Drawer) obj;

        if (items == null) {
            if (other.items != null)
                return false;
        } else if (!items.equals(other.items))
            return false;

        if (button == null) {
            return other.button == null;
        } else return button.equals(other.button);
    }

    @Override
    public String toString(){
        return "Drawer - button: " + button + " items: " + items;
    }

    @Override
    public Drawer clone() {
        Drawer drawer = new Drawer(getResId(), getItems());
        if (button != null) drawer.setButton(button);
        if (getItems() != null) drawer.addItems(items);
        if (getParentClass() != null) drawer.setParentClass(getParentClass());
        if (getCallbackMethods() != null) drawer.addCallbackMethods(getCallbackMethods());
        return drawer;
    }
}
