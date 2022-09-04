package st.cs.uni.saarland.de.reachabilityAnalysis;

import soot.*;
import soot.jimple.*;
import soot.jimple.toolkits.callgraph.Edge;
import soot.tagkit.StringConstantValueTag;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.MHGDominatorsFinder;
import st.cs.uni.saarland.de.entities.FieldInfo;
import st.cs.uni.saarland.de.helpClasses.Helper;
import st.cs.uni.saarland.de.helpClasses.MyStmtSwitch;
import st.cs.uni.saarland.de.helpClasses.StmtSwitchForArrays;
import st.cs.uni.saarland.de.helpClasses.StmtSwitchForClassArrays;
import st.cs.uni.saarland.de.helpMethods.InterprocAnalysis2;
import st.cs.uni.saarland.de.helpMethods.IterateOverUnitsHelper;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by avdiienko on 22/04/16.
 */
public class StringPropagatorSwitch extends MyStmtSwitch {
    private boolean isDone = false;
    private Value registerToTrack = null;
    private String result;
    //here we add some composite structure for arrays?
    private boolean isArray = false;
    private final SootMethod currentSootMethod;
    private final Set<SootMethod> callStack;
    private final String elementId;
    private Map<String, String> potentialResults;

    private void addToResult(String toAdd){
        if(result == null || result.isEmpty()){
            result = toAdd;
        }
        else{
            result = (toAdd + "#" + result);
        }
    }

    public String getResult(){
        return result;
    }

    public StringPropagatorSwitch(Value registerToTrack, SootMethod sootMethod, Set<SootMethod> callStack, String elementId){
        super(sootMethod);
        this.registerToTrack = registerToTrack;
        this.currentSootMethod = sootMethod;
        this.callStack = callStack;
        this.elementId = elementId;
        this.potentialResults = new HashMap<>();
    }

    public boolean isDone(){
        return isDone;
    }

    public Map<String, String> getPotentialResults() {
        return potentialResults;
    }

    public void caseAssignStmt(AssignStmt stmt){
        if(stmt.getLeftOp().equals(registerToTrack)){
            if(stmt.containsInvokeExpr()){
                if(stmt.getInvokeExpr() instanceof StaticInvokeExpr) {
                    //Heuristic, check if any of the parameters is an Array
                    InvokeExpr invokeExpr = stmt.getInvokeExpr();
                    if(invokeExpr.getArgCount() == 1){
                        //Assume we're dealing with an array access
                        registerToTrack = invokeExpr.getArg(0);
                    }
                    return;
                }

                else if(stmt.getInvokeExpr().getMethod().getSignature().equals(START_ACTIVITY_CONSTANTS.GET_CLASS)){
                    registerToTrack = stmt.getInvokeExpr().getUseBoxes().get(0).getValue();
                    return;
                }
                else if(stmt.getInvokeExpr().getMethod().getSignature().equals(STRING_BUILDER_CONSTANTS.TO_STRING)){
                    //we deal with a string builder
                    registerToTrack = stmt.getInvokeExpr().getUseBoxes().get(0).getValue();
                    StringBuilderPropagator stringBuilderPropagator = new StringBuilderPropagator(currentSootMethod, stmt, registerToTrack);
                    stringBuilderPropagator.run();
                    String res = stringBuilderPropagator.getResult();
                    if(res != null){
                        addToResult(res);
                    }
                }
                processInvokeExpr(stmt.getInvokeExpr());
            }
            if(stmt.getRightOp() instanceof CastExpr){
                registerToTrack = ((CastExpr) stmt.getRightOp()).getOp();
                return;
            }
            if(stmt.getRightOp() instanceof ClassConstant){
                addToResult(((ClassConstant)stmt.getRightOp()).getValue());
                isDone = true;
                return;
            }
           if(stmt.containsFieldRef() && stmt.getRightOp() instanceof FieldRef) { //TODO check if it can resolve the value here I guess?
               SootField f = ((FieldRef) stmt.getRightOp()).getField();
               //How to track definitions of the field?
               if (f.isStatic()) {
                   StringConstantValueTag tag = (StringConstantValueTag) f.getTag(StringConstantValueTag.class.getSimpleName());
                   if (tag != null) {
                       result = tag.getStringValue();
                       isDone = true;
                       return;
                   }
               }
               Set<FieldInfo> fInfos = interprocMethods2.findInitializationsOfTheField2(f, stmt, currentSootMethod);
               if (fInfos.size() > 0) {
                   for (FieldInfo fInfo : fInfos) {
                       if (Thread.currentThread().isInterrupted()) {
                           return;
                       }
                       if (fInfo.value != null) {
                           //String arrayText = "";
                           //What to do here
                           continue;
                       } else {
                           if (fInfo.methodToStart != null && fInfo.methodToStart.method().hasActiveBody()) {
                               //Should be safe to overriding
                               StringPropagatorSwitch newStmtSwitch = new StringPropagatorSwitch(fInfo.register, fInfo.methodToStart.method(), callStack, elementId);
                               iteratorHelper.runOverToFindSpecValuesBackwards(fInfo.methodToStart.method().getActiveBody(), fInfo.unitToStart, newStmtSwitch);
                               //String newResult = newStmtSwitch.result;
                               result = newStmtSwitch.result;
                               potentialResults.putAll(newStmtSwitch.potentialResults);
                               break;
                           }
                           else{
                               logger.debug("Method {} does not have active body", fInfo.methodToStart.method());
                           }
                       }
                   }
                   //TODO deal with non static fields
               }
           }
            if(stmt.getRightOp() instanceof ArrayRef){
                ArrayRef arrayRef = (ArrayRef)stmt.getRightOp();
                processArrayRef(arrayRef, stmt);
                return;
            }

            registerToTrack = stmt.getRightOp();
        }
    }

    private boolean processArrayRef(ArrayRef arrayRef, Stmt stmt) {
        isArray = true;
        Value index = arrayRef.getIndex();
        Value array = arrayRef.getBase();
        int arrayIndex = -1;
        boolean success = false;
        if(index instanceof IntConstant){
            //Here we need to resolve the array and then get the element at that index?
            //If we can resolve the array at least and store it that's good enough
            //Or I could pass along the id and just reuse the stmtswitchforarray thingie?
            arrayIndex = ((IntConstant)index).value;
        }
        else arrayIndex = Integer.parseInt(elementId);
        StmtSwitchForClassArrays stmtSwitchForArrays = new StmtSwitchForClassArrays(arrayIndex, array, currentSootMethod);
        iteratorHelper.runOverToFindSpecValuesBackwards(currentSootMethod.getActiveBody(), stmt, stmtSwitchForArrays);
        List<String> resultsForArrayPattern = stmtSwitchForArrays.getArrayResults();
        //TODO check if elementId matches with the array index, maybe it's unrelated for some reason
        for(String value: resultsForArrayPattern){
            result = value;
            success = true;
        }
        return success;
    }
    private void processInvokeExpr(InvokeExpr invokeExpr){
        findInReturnValue(invokeExpr.getMethod());
    }

    private void findInReturnValue(SootMethod methodToObserve){
        if(!methodToObserve.hasActiveBody()){
            try{
                methodToObserve.retrieveActiveBody();
            }
            catch (Exception e) {
                logger.warn("Skipping method with empty body {}", methodToObserve);
                isDone = true;
                return;
            }
        }
        Set<Unit> returnStmts = methodToObserve.getActiveBody().getUnits().stream().
                filter(u-> u instanceof ReturnStmt).collect(Collectors.toSet());
        MHGDominatorsFinder<Unit> dominatorsFinder = new MHGDominatorsFinder<>(new ExceptionalUnitGraph(methodToObserve.getActiveBody()));
        for(Unit retStmt : returnStmts){
            ReturnStmt returnStmt = (ReturnStmt)retStmt;
            Value register = returnStmt.getOp();
            if(register instanceof StringConstant){

                addToResult(((StringConstant)register).value);
                logger.debug("Added constant to results {}", potentialResults);
                isDone = true;
                return;
            }
            Unit currentUnit = retStmt;
            StringPropagatorSwitch internalStringPropagator = new StringPropagatorSwitch(register, currentSootMethod, callStack, elementId);
            while(currentUnit != null && internalStringPropagator.isDone()){
                currentUnit.apply(internalStringPropagator);
                currentUnit = dominatorsFinder.getImmediateDominator(currentUnit);
            }
            addToResult(internalStringPropagator.getResult());
        }
        isDone = true;
    }

    public void caseIdentityStmt(IdentityStmt stmt){
        if(stmt.getLeftOp().equals(registerToTrack) && stmt.getRightOp() instanceof ParameterRef){
            ParameterRef parameterRef = (ParameterRef)stmt.getRightOp();
            findInReachableMethods(currentSootMethod, parameterRef.getIndex());
        }
    }

    private void findInReachableMethods(SootMethod method, int argumentIndex){
        if(callStack.contains(method)){
            return;
        }
        callStack.add(method);
        final Iterator<Edge> edgesToM = Scene.v().getCallGraph().edgesInto(method);


        //TODO think about possible cases where the caller of the setter is different from the actual nextActivity?1
        //Also I'm bothered by the callback time
        while(edgesToM.hasNext()) {
            final Edge e = edgesToM.next();
            MethodOrMethodContext caller = e.getSrc();
            Stmt stmt = e.srcStmt();

            if (stmt.getInvokeExpr().getArg(argumentIndex) instanceof StringConstant) {
                String val = ((StringConstant) stmt.getInvokeExpr().getArg(argumentIndex)).value;
                addToResult(val);
                logger.debug("Added {} to optential results", val);
                potentialResults.put(caller.method().getDeclaringClass().toString(), val);
                isDone = true;
                continue;
            }
            else if(stmt.getInvokeExpr().getArg(argumentIndex) instanceof ClassConstant){
                String val = ((ClassConstant) stmt.getInvokeExpr().getArg(argumentIndex)).value;
                addToResult(val);
                logger.debug("Added {} to optential results", val);
                potentialResults.put(caller.method().getDeclaringClass().toString(), val);
                isDone = true;
                continue;
            }
            if (!(stmt.getInvokeExpr().getArg(argumentIndex) instanceof Local)) {
                continue;
            }

            Value localRegister = stmt.getInvokeExpr().getArg(argumentIndex);
            Body body = caller.method().getActiveBody();
            Unit workingUnit = e.srcUnit();

            if (!Helper.processMethod(body.getUnits().size())) {
                continue;
            }

            //try to find where the value has been initialized
            MHGDominatorsFinder<Unit> dominatorsFinder = new MHGDominatorsFinder<>(new ExceptionalUnitGraph(body));
            StringPropagatorSwitch internalStringSwitch = new StringPropagatorSwitch(localRegister, caller.method(), callStack, elementId);
            workingUnit = dominatorsFinder.getImmediateDominator(workingUnit);
            while(workingUnit != null && !internalStringSwitch.isDone()){
                workingUnit.apply(internalStringSwitch);
                workingUnit = dominatorsFinder.getImmediateDominator(workingUnit);
            }
            logger.debug("Added {} to optential results", internalStringSwitch.getResult());
            addToResult(internalStringSwitch.getResult());
            isDone = true;
        }
    }

    public boolean run(){

        return result == null || result.isEmpty();
    }
}
