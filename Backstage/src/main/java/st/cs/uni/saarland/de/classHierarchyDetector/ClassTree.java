package st.cs.uni.saarland.de.classHierarchyDetector;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by avdiienko on 25/04/16.
 */
@XStreamAlias("ClassTree")
public class ClassTree {
    private Set<ClassNode> nodes = new HashSet<>();
    public static final double K_SIGN = 1;
    public static final double K_FREQ = 10;

    public boolean addNode(ClassNode node) {
        if (!nodes.contains(node)) {
            nodes.add(node);
            return true;
        }
        return false;
    }

    public ClassNode getMergingNode() {
        Set<ClassNode> mergeNodes = nodes.stream().filter(cn -> cn.canMerge()).collect(Collectors.toSet());
        ClassNode minNode = mergeNodes.stream().collect(Collectors.minBy((a, b) -> a.compareTo(b))).orElse(null);
        return minNode;
    }

    public ClassNode getWeakLeaf() {
        Set<ClassNode> leafs = new HashSet<>();
        nodes.forEach(n ->
                leafs.addAll(n.getFreeLeafs()));
        ClassNode minNode = leafs.stream().collect(Collectors.minBy((a, b) -> Double.compare(a.getFreq(), b.getFreq()))).orElse(null);
        return minNode;
    }

    public ClassNode getNodeByName(String signature) {
        ClassNode classNodeOptional = nodes.stream().filter(x -> x.getName().equals(signature)).findFirst().orElse(null);
        return classNodeOptional;
    }

    public ClassNode getRoot() {
        return nodes.stream().filter(x -> x.getParent() == null).findFirst().get();
    }

    public void init(Map<String, Integer> frequencies) {
        nodes.stream().forEach(x -> {
            if (x.isLeaf()) x.setSignificance(1);
            x.setFreq(frequencies.getOrDefault(x.getName(), 0));
        });
    }

    public void process(int finalSize) {
        long initSize = nodes.stream().flatMap(x -> x.getLeafs().stream()).count();
        for (long i = initSize; i > finalSize; i--) {
            ClassNode mergingNode = getMergingNode();
            ClassNode weakLeaf = getWeakLeaf();
            if (weakLeaf.getSignificance() * K_SIGN <= mergingNode.getSignificance() && weakLeaf.getFreq() * K_FREQ < mergingNode.getFreq()) {
                mergeLeaf(weakLeaf);
            } else
                mergeChildren(mergingNode);
        }
        nodes.stream().forEach(x -> System.out.println(x.getName()));
    }

    public ClassNode mergeChildren(ClassNode node) {
        String newName = node.getName() + "#" + node.getChildren().stream().map(cn -> cn.getName()).collect(Collectors.joining("#"));
        double newFreq = node.getFreq() + node.getChildren().stream().collect(Collectors.summingDouble(cn -> cn.getFreq()));
        double newSign = 1 / node.getRootDepth() * node.getChildrenSize() * node.getChildren().stream().collect(Collectors.summingDouble(cn -> cn.getSignificance()));
        nodes.removeAll(node.getChildren());
        node.getChildren().clear();
        node.setName(newName);
        node.setFreq(newFreq);
        node.setSignificance(newSign);
        return node;
    }

    public void mergeLeaf(ClassNode node) {
        ClassNode parent = node.getParent();
        parent.setName(parent.getName() + "#" + node.getName());
        parent.setFreq(parent.getFreq() + node.getFreq());
        nodes.remove(node);
        parent.getChildren().remove(node);
    }
}
