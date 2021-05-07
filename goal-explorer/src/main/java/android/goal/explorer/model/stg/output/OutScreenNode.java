package android.goal.explorer.model.stg.output;

import android.goal.explorer.model.component.Fragment;
import android.goal.explorer.model.stg.node.ScreenNode;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import st.cs.uni.saarland.de.entities.Dialog;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@XStreamAlias("ScreenNode")
public class OutScreenNode extends OutAbstractNode {

    private Set<String> fragments;
    private String menu;
    private Set<String> dialogs;
    boolean target = false;

    public OutScreenNode(ScreenNode screenNode) {
        super(screenNode.getName());
        Set<String> fragmentStrings = new HashSet<>();
        Set<String> dialogStrings = new HashSet<>();
        for (Fragment fragment : screenNode.getFragments()) {
            fragmentStrings.add(fragment.getName());
        }
        for (Dialog dialog : screenNode.getDialogs()) {
            dialogStrings.add(dialog.getName());
        }
        if (screenNode.getMenu() == null) {
            menu = null;
        } else {
            menu = screenNode.getMenu().getName();
        }
        dialogs = dialogStrings;
        fragments = fragmentStrings;
    }

    @Override
    public String toString(){
        return getName() + " fragments: " + fragments + " menu: " + menu + " dialogs" + dialogs;
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

        OutScreenNode other = (OutScreenNode) obj;

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
