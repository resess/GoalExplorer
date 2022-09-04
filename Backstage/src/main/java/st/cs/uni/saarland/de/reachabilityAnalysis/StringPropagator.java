package st.cs.uni.saarland.de.reachabilityAnalysis;

import android.util.ArrayMap;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.jimple.StringConstant;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.MHGDominatorsFinder;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * Created by avdiienko on 22/04/16.
 */
public class StringPropagator implements Runnable {
    protected final SootMethod currentSootMethod;
    protected final Unit unitOfStartMethod;
    protected final MHGDominatorsFinder<Unit> dominatorsFinder;
    protected final Value registerToTrack;
    private String result = null;
    private String elementId;

    private Map<String, String> potentialResults;

    public String getResult(){
        return result;
    }

    public Map<String, String> getPotentialResults() {return potentialResults;}

    public StringPropagator(SootMethod currentSootMethod, Unit unitOfStartMethod, Value registerToTrack, String elementId){
        this.currentSootMethod = currentSootMethod;
        this.unitOfStartMethod = unitOfStartMethod;
        this.registerToTrack = registerToTrack;
        this.dominatorsFinder = new MHGDominatorsFinder<>(new ExceptionalUnitGraph(currentSootMethod.getActiveBody()));
        this.elementId = elementId;
        this.potentialResults = new HashMap<>();
    }

    @Override
    public void run() {
        if(registerToTrack instanceof StringConstant){
            result = ((StringConstant)registerToTrack).value;
            return;
        }
        if(unitOfStartMethod == null){
            return;
        }
        Unit workingUnit = dominatorsFinder.getImmediateDominator(unitOfStartMethod);
        StringPropagatorSwitch stringPropagatorSwitch = new StringPropagatorSwitch(registerToTrack, currentSootMethod, new HashSet<>(), elementId);
        while (workingUnit != null && !stringPropagatorSwitch.isDone()){
            workingUnit.apply(stringPropagatorSwitch);
            workingUnit = dominatorsFinder.getImmediateDominator(workingUnit);
        }
        result = stringPropagatorSwitch.getResult();
        potentialResults.putAll(stringPropagatorSwitch.getPotentialResults());
    }
}
