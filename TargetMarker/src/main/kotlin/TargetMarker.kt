import android.goal.explorer.analysis.CallbackWidgetProvider
import android.goal.explorer.model.stg.output.OutAbstractNode
import android.goal.explorer.model.stg.output.OutSTG
import android.goal.explorer.model.stg.output.OutScreenNode
import android.goal.explorer.model.stg.output.OutTransitionEdge
import org.slf4j.LoggerFactory
import soot.Scene
import soot.SootMethod
import soot.jimple.infoflow.android.callbacks.AndroidCallbackDefinition
import soot.jimple.infoflow.util.SystemClassHandler
import soot.util.HashMultiMap
import soot.util.MultiMap
import st.cs.uni.saarland.de.reachabilityAnalysis.ApiInfoForForward
import st.cs.uni.saarland.de.reachabilityAnalysis.CallbackToApiMapper
import st.cs.uni.saarland.de.reachabilityAnalysis.UiElement

import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import kotlin.collections.ArrayList
import kotlin.collections.HashSet

class TargetMarker {
    companion object {
        private val logger = LoggerFactory.getLogger(TargetMarker::class.java)
        private const val maxMethodDepth = 30
        private var numFoundTargets = 0


        /**
         * Mark screen nodes containing containing recursive calls to any of the soot methods
         */

        fun markScreenNodesIfRecursivelyCallsMethod(methods: Set<SootMethod>): Map<OutAbstractNode, Pair<Boolean, String>> {
            return markScreenNodesIfRecursivelyCallsMethod(ModelLoader.stg, methods, HashMultiMap()) { method, methodsList ->
                methodsList.contains(method)
            }
        }

        /**
         * Mark screen nodes containing containing recursive calls to any of the soot methods
         */

        fun markScreenNodesIfRecursivelyCallsMethodName(methods: Set<String>): Map<OutAbstractNode, Pair<Boolean, String>> {
            return markScreenNodesIfRecursivelyCallsMethod(ModelLoader.stg, methods, HashMultiMap()) { method, methodsList ->
                methodsList.contains(method.name)
            }
        }

        /**
         * Marks nodes based on whether a screen node contains methods that can recursively invoke the SootMethods
         * @param  stg
         * @param methods
         * @param memo
         * @param match matching function to determine if [methods] contains the method of interest
         * @return a mapping of the screen nodes to whether they are marked and a string describing the action to perform to trigger the method
         */
        private fun <T : Any> markScreenNodesIfRecursivelyCallsMethod(stg: OutSTG,
                                                                      methods: Set<T>,
                                                                      memo: MultiMap<String, AndroidCallbackDefinition>,
                                                                      match: (SootMethod, Set<T>) -> Boolean): Map<OutAbstractNode, Pair<Boolean, String>> {
            logger.debug("Searching for targets in stg from $methods")
            val nodes = stg.abstractNodes
            val marks = nodes.associateWith { Pair(false, "") }.toMutableMap()
            for (node in nodes) {
                logger.debug("Checking node $node")
                if (node is OutScreenNode) {
                    val activity = (node.name)
                    //TODO deal with tabs?
                    //TODO update to deal with SERVICES and BROADCAST RECEIVERS
                    //Parse callbacks of the current activity
                    //Here, we want to check also the menu callbacks if defined (need to map it back to the "real" method
                    ModelLoader.getLifecycleMethodsForActivity(activity).forEach { lifecycle ->
                        logger.debug("Checking lifecycle $lifecycle")
                        if (recursiveCheck(lifecycle.method()) { match(it, methods) }) {
                            logger.debug("Found method of interest, marking node $node")
                            numFoundTargets += 1
                            marks[node] = Pair(true, "")
                        }
                    }

                    //TODO: add some consistent filter so that only feasible callbacks are parsed for each node
                    ModelLoader.getCallbacksForActivity(activity) //TODO check in list of menu callbacks instead
                        .filter { callback ->
                            callback.targetMethod.hasActiveBody() && //why not?
                                    //can we have that actually? //ah right cause it's all the callbacks not just the STG ones
                                    (!callback.targetMethod.name.equals("onOptionsItemSelected") && !callback.targetMethod.name.equals(
                                        "onContextItemSelected"
                                    ) && node.contextMenu == null && node.menu == null && node.drawerMenu == null)
                        }.forEach { callback ->
                            logger.debug("Checking callback $callback")
                            //Get all nodes reachable through callback
                            if (memo.get(activity).contains(callback)) {
                                //callback invokes method of interest
                                logger.debug("Loading from memoized $callback ...")
                                val uiTrigger = getTriggerOfCallbackPrecise(callback, activity) { match(it, methods) }
                                if (uiTrigger != null) {
                                    logger.debug("Found ui trigger $uiTrigger for $callback")
                                    if (!foundEdgeForTarget(
                                            stg.getEdgesWithSrcNode(node),
                                            callback,
                                            uiTrigger,
                                            marks
                                        )
                                    ) {
                                        numFoundTargets += 1
                                        marks[node] = Pair(true, formatTrigger(callback, uiTrigger))
                                    }
                                }
                            } else if (recursiveCheck(callback.targetMethod) { match(it, methods) }) {
                                //logger.debug("Found method of interest in $callback")
                                memo.put(activity, callback)
                                /*if(ModelLoader.getMenuCallbacksForActivity(activity).contains(callback.targetMethod)) {
                            //control flow sensitive analysis needed
                            val act:Activity = ModelLoader.loadedAppModel.getActivityByName(activity)
                            if(act.hasMenu()){
                                val itemCallbacks = act.visibleMenu.menuItemsCallbacks
                                itemCallbacks.forEach {if (recursiveCheck(it.value)) {
                                    //need to make sure the name is properly resolved?
                                    //actually might not work since callgraph based
                                } }
                            }
                        }*/
                                val uiTrigger = getTriggerOfCallbackPrecise(callback, activity) { match(it, methods) }
                                if (uiTrigger != null) {
                                    logger.debug("Found ui trigger $uiTrigger")
                                    if (!foundEdgeForTarget(
                                            stg.getEdgesWithSrcNode(node),
                                            callback,
                                            uiTrigger,
                                            marks
                                        )
                                    ) {
                                        numFoundTargets += 1
                                        marks[node] = Pair(true, formatTrigger(callback, uiTrigger))
                                        logger.debug("Adding mark in stg ${marks[node]} for $node")
                                    }
                                }
                            }
                        }

                }
            }
            logger.debug("Done with target marking, collected {} target statements", numFoundTargets)
            return marks
        }

        /**
         * Converts pair callback, ui id to output
         */
        private fun formatTrigger(callback: AndroidCallbackDefinition, uiTrigger: UiElement): String =
            callback.parentMethod.subSignature + "; " + uiTrigger.globalId+"; "+ uiTrigger.text["default_value"]


        /**
         * Checks if STG contains edge with desired action
         * @param edges the set of edges of the STG
         * @param callback to look for on edge
         * @param uiTrigger the id of the ui trigger
         * @param marks the target screens
         */
        private fun foundEdgeForTarget(edges: Set<OutTransitionEdge>,
                                       callback: AndroidCallbackDefinition,
                                       uiTrigger: UiElement,
                                       marks: MutableMap<OutAbstractNode, Pair<Boolean, String>>): Boolean =
            edges.any { edge ->
                logger.debug("Checking edge $edge")
                if (isTriggerOfEdge(edge, callback, uiTrigger.globalId)) {
                    logger.debug("Marking target node ${edge.tgtNode}")
                    numFoundTargets += 1
                    marks.put(edge.tgtNode as OutScreenNode, Pair(true, formatTrigger(callback, uiTrigger)))
                    true
                } else false
            }


        /**
         * Checks if transition is started by given ui element and callback
         */
        private fun isTriggerOfEdge(edge: OutTransitionEdge, callback: AndroidCallbackDefinition, uiTrigger: Int): Boolean {
            val tag = edge.edgeTag
            val resId = tag.resId
            val handlerMethod = tag.handlerMethod
            val targetMethod = callback.targetMethod
            val parentMethod = callback.parentMethod
            logger.debug("Checking edge $handlerMethod with $targetMethod $parentMethod")
            return (parentMethod.equals(handlerMethod) || targetMethod.equals(handlerMethod)) && resId == uiTrigger
        }

        /**
         * Get ui element associated with a callback
         */
        private fun getTriggerOfCallback(callback: AndroidCallbackDefinition): UiElement? =
            //should do something for menus?
            CallbackWidgetProvider.v().findWidget(callback.parentMethod)


        private fun getTriggerOfCallbackPrecise(callback: AndroidCallbackDefinition, activity: String, match: (SootMethod) -> Boolean): UiElement? {
            val potentialUiElements = ModelLoader.getUiElementsForActivity(activity).filter { callback.targetMethod.equals(it.handlerMethod) }
                if (potentialUiElements.isEmpty())
                    return CallbackWidgetProvider.v().findWidget(callback.parentMethod)
                if (potentialUiElements.size == 1)
                    return potentialUiElements[0]
                val counter = AtomicInteger(0)
                return potentialUiElements.find {
                    val elementId = if (it.hasIdInCode()) it.idInCode.toString() else it.elementId
                    val apisFound = ArrayList<ApiInfoForForward>()
                    val forwardFounder = CallbackToApiMapper(it.handlerMethod, elementId, counter.getAndIncrement(),potentialUiElements.size,
                        maxMethodDepth, true,apisFound,false )

                    val methods = setOf<String>("<java.net.URL: java.net.URLConnection openConnection()>")
                    forwardFounder.setSourcesAndSinks(methods)
                    forwardFounder.call()
                    logger.debug("All apis found {}", apisFound)
                    apisFound.any {api -> match(api.api) }
                }
        }

        //here if we don't get the result we can try backstage's reachability analysis? nah, we should already have the results


        /*private fun getTriggerOfCallbackExtended(callback: AndroidCallbackDefinition, activity: Activity, match: (SootMethod) -> Boolean) {
            val uiElement = CallbackWidgetProvider.v().findWidget(callback.parentMethod)
            if(uiElement == null){
                val potentialUiElements = activity.uiElements.filter { it.handlerMethod.equals(callback.targetMethod)  }
                if (potentialUiElements.size == 1)
                    return potentialUiElements.get(0)
                else {
                    //perform reachability analysis
                    val counter = AtomicInteger(0)
                    potentialUiElements.any {
                        val elementId = if (it.hasIdInCode()) it.idInCode.toString() else it.elementId
                        val apisFound = ArrayList<ApiInfoForForward>()
                        val forwardFounder = CallbackToApiMapper(it.handlerMethod, elementId, counter.getAndIncrement(),potentialUiElements.size,
                            maxMethodDepth, true,apisFound,false )
                        forwardFounder.setSourcesAndSinks(HashSet<String>())
                        forwardFounder.call()

                        //check if any of the returned apis match?
                        //return apisFound.contains()
                    }
                }
            }
            //return uiElement
        }*/


        /**
         * Checks if the root method or any methods called recursively by the root method satisfied the criteria
         */
        private fun recursiveCheck(rootMethod: SootMethod, func: (SootMethod) -> Boolean): Boolean {
            try {
                val callGraph = Scene.v().callGraph

                // BFS to check if the method is reachable from the rootMethod
                val visited = HashSet<SootMethod>()
                val toCheck = LinkedList<SootMethod>()

                visited.add(rootMethod)
                toCheck.add(rootMethod)

                while (toCheck.isNotEmpty()) {
                    val cur = toCheck.poll()

                    //checked.add(cur)
                    //logger.debug("Checking method ${cur.getSignature()}")
                    if (func(cur)) {
                        return true
                    }

                    //TODO check if we can reuse Backstage's inAppNameSpace
                    for (edge in callGraph.edgesOutOf(cur)) {
                        val mCtxt = edge.tgt
                        if(mCtxt.method() != null && !visited.contains(mCtxt.method()) && mCtxt.method().hasActiveBody() && SystemClassHandler.v().isClassInSystemPackage(mCtxt.method().declaringClass.name)){
                            //&& Helper.isClassInAppNameSpace(mCtxt.method().declaringClass.name)) {
                            //if (!checked.contains(edge.tgt()) && edge.tgt().hasActiveBody() && SystemClassHandler.isClassInSystemPackage(edge.tgt().declaringClass.name);
                            visited.add(mCtxt.method())
                            toCheck.add(mCtxt.method())
                        }
                    }
                    //logger.debug("Computed edges for ${cur.getSignature()}")
                }

                return false
            } catch (e: Exception) {
                return false
            }

        }



    }
}