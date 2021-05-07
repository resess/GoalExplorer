package st.cs.uni.saarland.de.classHierarchyDetector;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by avdiienko on 25/04/16.
 */
@XStreamAlias("ClassNode")
public class ClassNode implements Comparable {
    private String name;
    private List<ClassNode> children = new ArrayList<>();
    private ClassNode parent;
    private int rootDepth = 0;
    private double significance = 0;
    private double freq = 0;


    public ClassNode(String signature) {
        this.name = signature;
    }

    public void addChild(ClassNode child) {
        this.children.add(child);
    }

    @Override
    public int compareTo(Object obj) {
        if (!(obj instanceof ClassNode))
            return 1;
        ClassNode n = (ClassNode) obj;
        return Double.compare(this.significance, n.getSignificance());
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ClassNode)) {
            return false;
        }
        return name.equals(((ClassNode) obj).getName());
    }

    public int getChildrenSize() {
        return this.children.size();
    }

    public double getFreq() {
        return freq;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ClassNode getParent() {
        return parent;
    }

    public void setFreq(double freq) {
        this.freq = freq;
    }

    public void setParent(ClassNode par) throws Exception {
        if (parent == null) {
            parent = par;
        } else {
            throw new Exception("Parent is already present");
        }
    }

    public int getRootDepth() {
        return rootDepth;
    }

    public List<ClassNode> getChildren() {
        return children;
    }

    public void setRootDepth(int rootDepth) {
        this.rootDepth = rootDepth;
    }

    public double getSignificance() {
        return significance;
    }

    public void setSignificance(double significance) {
        this.significance = significance;
    }

    public boolean isLeaf() {
        return children.isEmpty();
    }

    public Set<ClassNode> getLeafs() {
        return children.stream().filter(cn -> cn.isLeaf()).collect(Collectors.toSet());
    }

    public boolean canMerge() {
        return children.stream().map(cn -> cn.isLeaf()).reduce(true, Boolean::logicalAnd);
    }

    public Set<ClassNode> getFreeLeafs() {
        Set<ClassNode> leafs = getLeafs();
        if (leafs.size() != getChildren().size()) return leafs;
        return null;
    }

}
