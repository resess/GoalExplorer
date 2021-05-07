import android.goal.explorer.model.component.Activity
import android.goal.explorer.model.stg.node.ScreenNode
import soot.Scene
import soot.SootMethod
import java.util.*
import kotlin.collections.HashSet

class TargetMarker {
    companion object {
        /**
         * Marks the screen nodes that contain recursive calls to any of the soot methods
         */
        fun markScreenNodesIfRecursivelyCallsSootMethod(nodes: Set<ScreenNode>, methods: List<SootMethod>): Map<ScreenNode, Boolean> {
            return markScreenNodesIfRecursivelyCallsMethod(nodes, methods) { method, methodsList ->
                methodsList.contains(method)
            }
        }

        /**
         * Marks the screen nodes that contain recursive calls to any method names
         */
        fun markScreenNodesIfRecursivelyCallsMethodName(nodes: Set<ScreenNode>, methods: List<String>): Map<ScreenNode, Boolean> {
            return markScreenNodesIfRecursivelyCallsMethod(nodes, methods) { method, methodsList ->
                methodsList.contains(method.name)
            }
        }

        /**
         * Marks nodes based on the evaluation criteria
         * @param nodes the screen nodes of the STG to mark
         * @param eval evaluation function that returns true if a node should be marked
         * @return a mapping of the screen nodes to whether they are "marked"
         */
        fun markScreenNodes(nodes: Set<ScreenNode>,
                            eval: (ScreenNode) -> Boolean): Map<ScreenNode, Boolean> {

            return nodes.associateWith { eval(it) }
        }

        /**
         * Marks nodes based on evaluation criteria
         * @param nodes the screen nodes of the STG to mark
         * @param criteria list of criteria to be evaluate the SootMethods on
         * @param eval evaluation function that returns true if a node should be marked by on criteria
         * @return a mapping of the screen nodes to whether they are "marked"
         */
        private fun <T: Any> markScreenNodesByCriteria(nodes: Set<ScreenNode>,
                                               criteria: List<T>,
                                               eval: (ScreenNode, List<T>) -> Boolean): Map<ScreenNode, Boolean> {

            return markScreenNodes(nodes) { eval(it, criteria) }
        }

        /**
         * Marks nodes based on whether a screen node contains methods that can recursively call the SootMethods
         * @param nodes the screen nodes of the STG to mark
         * @param methods list of methods to check for
         * @param match matching function to determine if [methods] contains the method
         * @return a mapping of the screen nodes to whether they are "marked"
         */
        private fun <T: Any> markScreenNodesIfRecursivelyCallsMethod(nodes: Set<ScreenNode>,
                                                             methods: List<T>,
                                                             match: (SootMethod, List<T>) -> Boolean): Map<ScreenNode, Boolean> {

            return markScreenNodesByCriteria(nodes, methods) { node, criteria ->
                val activityClass = (node.component as Activity).mainClass
                activityClass.methods.any { method ->
                    recursiveCheck(method) { match(it, criteria) }
                }
            }
        }

        /**
         * Checks if the root method or any methods called recursively by the root method satisfied the criteria
         */
        private fun recursiveCheck(rootMethod: SootMethod, func: (SootMethod) -> Boolean): Boolean {
            val callGraph = Scene.v().callGraph

            // BFS to check if the method is reachable from the rootMethod
            val checked = HashSet<SootMethod>()
            val toCheck = LinkedList<SootMethod>()
            toCheck.add(rootMethod)

            while (toCheck.isNotEmpty()) {
                val cur = toCheck.poll()
                checked.add(cur)

                if (func(cur)) {
                    return true
                }

                for (edge in callGraph.edgesOutOf(cur)) {
                    if (!checked.contains(edge.tgt())) {
                        toCheck.add(edge.tgt())
                    }
                }
            }

            return false
        }
    }

}
