package st.cs.uni.saarland.de.reachabilityAnalysis;

import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.jimple.Stmt;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.MHGDominatorsFinder;
import soot.toolkits.graph.MHGPostDominatorsFinder;

/**
 * Created by avdiienko on 22/04/16.
 */
public class AnalyzeIntents implements Runnable {
    private final IntentInfo intentInfo;
    private final SootMethod currentSootMethod;
    private final SootMethod startMethod;
    private final Unit unitOfStartMethod;
    private final MHGDominatorsFinder<Unit> dominatorFinder;
    private final String elementId;
    private Value registerOfIntent;


    public AnalyzeIntents(SootMethod startMethod, Unit unitOfInvoke, SootMethod methodOfInvoke, String elementId){
        intentInfo = new IntentInfo();
        this.startMethod = startMethod;
        this.unitOfStartMethod = unitOfInvoke;
        this.currentSootMethod = methodOfInvoke;
        this.dominatorFinder = new MHGDominatorsFinder<>(new ExceptionalUnitGraph(currentSootMethod.getActiveBody()));
        this.elementId = elementId;
    }

    public IntentInfo getIntentInfo(){
        return intentInfo;
    }

    @Override
    public void run() {
        if(unitOfStartMethod == null){
            return;
        }
        Unit workingUnit = dominatorFinder.getImmediateDominator(unitOfStartMethod);
        for(Value arg : ((Stmt)unitOfStartMethod).getInvokeExpr().getArgs()){
            if(arg.getType().toString().equals(START_ACTIVITY_CONSTANTS.INTENT_CLASS)){
                registerOfIntent = arg;
                break;
            }
        }
        AnalyseIntentSwitch intentsSwitch = new AnalyseIntentSwitch(registerOfIntent, currentSootMethod, intentInfo, elementId);
        while(workingUnit != null && !intentsSwitch.isDone()){
            workingUnit.apply(intentsSwitch);
            workingUnit = dominatorFinder.getImmediateDominator(workingUnit);
        }
    }
}
