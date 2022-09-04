package st.cs.uni.saarland.de.reachabilityAnalysis;

import org.xmlpull.v1.XmlPullParserException;
import soot.jimple.infoflow.android.axml.AXmlNode;
import soot.jimple.infoflow.android.manifest.ProcessManifest;
import soot.jimple.infoflow.android.manifest.binary.AbstractBinaryAndroidComponent;
import st.cs.uni.saarland.de.helpClasses.Helper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class IntentFilterResolver {
    private static Map<String, List<Map<String, List<String>>>> servicesToIntentFilters = new HashMap<>();
    private static Map<String, List<Map<String, List<String>>>> activitiesToIntentFilters = new HashMap<>();
    private static Map<String, List<Map<String, List<String>>>> receiversToIntentFilters = new HashMap<>();

    /*public IntentFilterResolver(Map<String, List<Map<String, List<String>>>> activitiesToIntentFilters, Map<String, List<Map<String, List<String>>>> servicesToIntentFilters,Map<String, List<Map<String, List<String>>>> receiversToIntentFilters ){
        this.activitiesToIntentFilters = activitiesToIntentFilters;
        this.servicesToIntentFilters = servicesToIntentFilters;
        this.receiversToIntentFilters = receiversToIntentFilters;
    }*/

    public static Map<String, List<Map<String, List<String>>>> getActivitiesToIntentFilters() {
        return activitiesToIntentFilters;
    }

    public static Map<String, List<Map<String, List<String>>>> getReceiversToIntentFilters() {
        return receiversToIntentFilters;
    }

    public static Map<String, List<Map<String, List<String>>>> getServicesToIntentFilters() {
        return servicesToIntentFilters;
    }

    public void setActivitiesToIntentFilters(Map<String, List<Map<String, List<String>>>> activitiesToIntentFilters){
        //logger.debug("The intent filters to check {}", activitiesToIntentFilters);
        activitiesToIntentFilters = activitiesToIntentFilters;
    }

    public static List<String> getTargetComponents(String intentAction){
        List<String> targets = getTargetComponents(activitiesToIntentFilters, intentAction);
        targets.addAll(getTargetComponents(servicesToIntentFilters, intentAction));
        targets.addAll(getTargetComponents(receiversToIntentFilters, intentAction));
        return targets;
    }

    public static List<String> getTargetComponents(Map<String, List<Map<String, List<String>>>> componentToFilters, String intentAction){
        return componentToFilters.entrySet()
                .stream()
                .filter(e -> {
                    //logger.debug("The current val "+e);
                    return e.getValue().stream().anyMatch(intentMap -> {
                        //logger.debug("The intent map "+intentMap);
                        return intentMap.containsKey("action") && intentMap.get("action").contains(intentAction);
                    });
                })
                .map(e -> e.getKey())
                .collect(Collectors.toList());
    }


    public static void retrieveIntentFilters(String apkPath){
        try {
            ProcessManifest processMan = new ProcessManifest(apkPath);
            String packageName = processMan.getPackageName();
            List<AXmlNode> activities = processMan.getAllActivities();
            List<AXmlNode> services = processMan.getServices().asList().stream().map(AbstractBinaryAndroidComponent::getAXmlNode).collect(Collectors.toList());
            List<AXmlNode> receivers = processMan.getBroadcastReceivers().asList().stream().map(AbstractBinaryAndroidComponent::getAXmlNode).collect(Collectors.toList());


            servicesToIntentFilters.putAll(getIntentFilters(packageName, services));
            activitiesToIntentFilters.putAll(getIntentFilters(packageName, activities));
            receiversToIntentFilters.putAll(getIntentFilters(packageName, receivers));
        } catch (IOException | XmlPullParserException e) {
            e.printStackTrace();
            Helper.saveToStatisticalFile(Helper.exceptionStacktraceToString(e));
        }
    }

    private static Map<String, List<Map<String, List<String>>>> getIntentFilters(String packageName, List<AXmlNode> baseNode) {
        Map<String, List<Map<String, List<String>>>> baseNodeToIntentFilters = new HashMap<>();
        for(AXmlNode service: baseNode){
            String name = service.getAttribute("name").getValue().toString();
            if(name.startsWith(".")){
                name = String.format("%s%s", packageName, name);
            }
            else if(!name.contains(".")){
                name = String.format("%s.%s", packageName, name);
            }
            List<AXmlNode> nodes = service.getChildrenWithTag("intent-filter");
            if(nodes.size() == 0){
                continue;
            }
            baseNodeToIntentFilters.put(name, new ArrayList<>());
            for(AXmlNode inFilterNode : nodes) {
                Map<String, List<String>> local = new HashMap<>();
                for (AXmlNode subNode : inFilterNode.getChildren()) {
                    String tagName = subNode.getTag().toString();
                    switch (tagName) {
                        case "action":
                        case "category": {
                            if(subNode.hasAttribute("name")) {
                                String attrValue = subNode.getAttribute("name").getValue().toString();
                                if (attrValue.startsWith(".")) {
                                    attrValue = String.format("%s%s", packageName, attrValue);
                                }
                                if (!local.containsKey(tagName)) {
                                    local.put(tagName, new ArrayList());
                                }
                                local.get(tagName).add(attrValue);
                            }
                            break;
                        }
                        case "data": {
                            if(subNode.hasAttribute("mimeType")) {
                                String attrValue = subNode.getAttribute("mimeType").getValue().toString();
                                if (attrValue.startsWith(".")) {
                                    attrValue = String.format("%s%s", packageName, attrValue);
                                }
                                if (!local.containsKey(tagName)) {
                                    local.put(tagName, new ArrayList());
                                }
                                local.get(tagName).add(attrValue);
                            }
                            break;
                        }
                    }
                }
                baseNodeToIntentFilters.get(name).add(local);
            }
        }
        return baseNodeToIntentFilters;
    }
}
