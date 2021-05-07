package android.goal.explorer.builder;

import android.goal.explorer.model.component.Activity;
import android.goal.explorer.model.stg.STG;
import android.goal.explorer.model.stg.edge.EdgeTag;
import android.goal.explorer.model.stg.node.AbstractNode;
import android.goal.explorer.model.stg.node.ScreenNode;
import org.pmw.tinylog.Logger;
import st.cs.uni.saarland.de.reachabilityAnalysis.UiElement;

import java.util.Set;

public class TransitionBuilder {
    public STG stg;

    private static final String TAG = "TransitionBuilder";

    /**
     * Default constructor
     * @param stg The stg model
     */
    public TransitionBuilder(STG stg) {
        this.stg = stg;
    }

    public void collectTransitions() {
        for (ScreenNode screen : stg.getAllScreens()) {
            Activity mainActivity = (Activity) screen.getComponent();
            // iterate all UI elements to find triggers to transitions
            for (UiElement uiElement : mainActivity.getUiElements()) {
                if (uiElement.targetSootClass != null) {
                    // find all target screen nodes
                    Set<AbstractNode> targetNodes = stg.getNodesByName(uiElement.targetSootClass.getName());
                    if (targetNodes.isEmpty()) {
                        Logger.warn("[{}] Cannot find the screen node matches component name {}", TAG,
                                uiElement.targetSootClass.getName());
                        continue;
                    }

                    // if we can find the tag for this transition
                    EdgeTag tag = new EdgeTag(uiElement.kindOfElement, uiElement.handlerMethod.getSubSignature(),
                            uiElement.globalId);
                    targetNodes.forEach(targetNode -> stg.addTransitionEdge(screen, targetNode, tag));
                }
            }
        }
    }
}
