package android.goal.explorer.model.stg.output;

import android.goal.explorer.model.stg.STG;
import android.goal.explorer.model.stg.edge.TransitionEdge;
import android.goal.explorer.model.stg.node.BroadcastReceiverNode;
import android.goal.explorer.model.stg.node.ScreenNode;
import android.goal.explorer.model.stg.node.ServiceNode;
import com.thoughtworks.xstream.annotations.XStreamAlias;

import java.util.*;

@XStreamAlias("ScreenTransitionGraph")
public class OutSTG {
    public Set<OutTransitionEdge> transitionEdges;
    public Set<OutScreenNode> screenNodeSet;
    public Set<OutServiceNode> serviceNodeSet;
    public Set<OutBroadcastReceiverNode> broadcastReceiverNodeSet;

    public OutSTG(STG stg, Map<ScreenNode, Boolean> targetMarking) {
        transitionEdges = new HashSet<>();
        screenNodeSet = new HashSet<>();
        serviceNodeSet = new HashSet<>();
        broadcastReceiverNodeSet = new HashSet<>();

        for (TransitionEdge edge : stg.getTransitionEdges()) {
            if (edge.getSrcNode() == null || edge.getTgtNode() == null) {
                continue;
            }
            transitionEdges.add(new OutTransitionEdge(edge));
        }
        for (ScreenNode node : stg.getAllScreens()) {
            OutScreenNode n = (OutScreenNode) ConvertToOutput.convertNode(node);
            if (targetMarking.get(node)) {
                n.target = true;
            }
            screenNodeSet.add(n);
        }
        for (ServiceNode node : stg.getAllServices()) {
            serviceNodeSet.add((OutServiceNode) ConvertToOutput.convertNode(node));
        }
        for (BroadcastReceiverNode node : stg.getAllBroadcastReceivers()) {
            broadcastReceiverNodeSet.add((OutBroadcastReceiverNode) ConvertToOutput.convertNode(node));
        }
    }
}
