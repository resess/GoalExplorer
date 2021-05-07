package st.cs.uni.saarland.de.hierarchy;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Isa on 05.02.2016.
 */
public class Node {

        private List<Node> children = new ArrayList<Node>();
        private Node parent = null;
        private int elementID;

        public Node(int elementID, Node parent) {
            this.elementID = elementID;
            this.parent = parent;
        }

        public List<Node> getChildren() {
            return children;
        }

        public void addChild(Node childNode) {
            this.children.add(childNode);
        }

        public int getElementID() {
            return this.elementID;
        }

        public boolean isLeaf() {
            if(this.children.size() == 0)
                return true;
            else
                return false;
        }

}
