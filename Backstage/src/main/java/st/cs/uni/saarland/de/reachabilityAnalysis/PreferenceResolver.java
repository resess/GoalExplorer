package st.cs.uni.saarland.de.reachabilityAnalysis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.*;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;

import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Type;

import st.cs.uni.saarland.de.entities.AppsUIElement;

import st.cs.uni.saarland.de.entities.PreferenceElement;
import st.cs.uni.saarland.de.reachabilityAnalysis.UiElement;

public class PreferenceResolver {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    private Map<String, List<Map<String, List<String>>>> activitiesToIntentFilters = new HashMap<>();
    private List<AppsUIElement> prefElements;
    private List<UiElement> preferences;
    private static PreferenceResolver instance;
    
    public static PreferenceResolver v(){
        if(instance == null)
            instance = new PreferenceResolver();
        return instance;
    }

    public PreferenceResolver(){
        this.prefElements = new ArrayList<>();
        this.preferences = new ArrayList<>();
    }

    public PreferenceResolver(Map<String, List<Map<String, List<String>>>> activitiesToIntentFilters, List<AppsUIElement> preferences){
        this.activitiesToIntentFilters = activitiesToIntentFilters;
        this.prefElements = preferences;
        this.preferences = new ArrayList<>();
    }

    public void setIntentFilters(Map<String, List<Map<String, List<String>>>> activitiesToIntentFilters){
        //logger.debug("The intent filters to check {}", activitiesToIntentFilters);
        this.activitiesToIntentFilters = activitiesToIntentFilters;
    }

    public List<UiElement> run(){
        return prefElements.stream().map(pref -> resolvePreferenceElement(pref)).collect(Collectors.toList());
    }

    //map a preference screen to a destination I guess?

    public List<UiElement> getResults() {
        return preferences;
    }

    public void storePreference(AppsUIElement element){
        //logger.debug("Adding preference element {}", element);
        this.prefElements.add(element);
    }

    public UiElement resolvePreferenceElement(AppsUIElement element){
        PreferenceElement prefElement = (PreferenceElement)element;
        UiElement reachabilityElement = new UiElement();
        reachabilityElement.globalId = prefElement.getId();
        reachabilityElement.elementId = Integer.toString(prefElement.getId());
         reachabilityElement.kindOfElement = prefElement.getKindOfUiElement().toLowerCase();
        reachabilityElement.declaringSootClass = prefElement.getAssignedActivity();
        reachabilityElement.handlerMethod = new SootMethod("onPreferenceClick",new ArrayList<>(), Scene.v().getTypeUnsafe("boolean"));
        if(reachabilityElement.declaringSootClass != null)
            reachabilityElement.handlerMethod.setDeclaringClass(Scene.v().getSootClass(reachabilityElement.declaringSootClass));
        //Scene.v().getSootMethod("boolean onPreferenceChange(Preference preference, Object newValue)")
        reachabilityElement.signature = "<"+reachabilityElement.declaringSootClass+": boolean onPreferenceClick()>";
       
        //declaring sootclass should come from the parent PreferenceScreen?
        reachabilityElement.text.put("default_value",prefElement.getTitle());
        //Resolve the target class
        List<String> targetClasses = resolveTargetOfElement(prefElement);
        logger.debug("All target classes found for current element {} {}", reachabilityElement.elementId, targetClasses);
        if(!targetClasses.isEmpty())
            reachabilityElement.targetSootClass = Scene.v().getSootClass(targetClasses.get(0));
        preferences.add(reachabilityElement);
        return reachabilityElement;


    }

    public List<String> resolveTargetOfElement(PreferenceElement prefElement){
        if(!StringUtils.isBlank(prefElement.getIntentTargetClass()))
            return Collections.singletonList(prefElement.getIntentTargetClass());
        String intentAction = prefElement.getIntentAction();
        if(StringUtils.isBlank(intentAction))
            return new ArrayList<String>();
        //logger.debug("Intent action of interest {}", intentAction);
        return activitiesToIntentFilters.entrySet()
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
    
}
