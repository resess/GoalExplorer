package st.cs.uni.saarland.de.reachabilityAnalysis;

import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.jimple.*;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.MHGDominatorsFinder;
import soot.toolkits.graph.MHGPostDominatorsFinder;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by avdiienko on 22/04/16.
 */
public class AnalyseIntentSwitch extends AbstractStmtSwitch {
    private Value registerOfIntent;
    private final SootMethod curentSootMethod;
    private boolean isDone = false;
    private String elementId;

    public IntentInfo getIntentInfo() {
        return intentInfo;
    }

    private final IntentInfo intentInfo;
    private final Logger logger;

    public boolean isDone(){
        return isDone;
    }

    public AnalyseIntentSwitch(Value register, SootMethod currentSootMethod, IntentInfo intentInfo){
        this.registerOfIntent = register;
        this.curentSootMethod = currentSootMethod;
        this.intentInfo = intentInfo;
        this.logger = LoggerFactory.getLogger(this.getClass().getSimpleName());
    }
    public AnalyseIntentSwitch(Value register, SootMethod currentSootMethod, IntentInfo intentInfo, String elementId){
        this(register, currentSootMethod, intentInfo);
        this.elementId = elementId;
    }

    public void caseAssignStmt(AssignStmt stmt){
        //logger.debug("Checking assign stmt {} in method {}", stmt, curentSootMethod);
        if(stmt.containsInvokeExpr()){
            processInvokeExpr(stmt);
            return;
        }

        if(stmt.getRightOp().equals(registerOfIntent)){
            //TODO huh?
            //just reassignment
            //Is this needed? We already know what we're tracking
            registerOfIntent = stmt.getRightOp();
            return;
        }
        if(stmt.getRightOp() instanceof CastExpr){
            //just cast
            CastExpr expr = ((CastExpr) stmt.getRightOp());
            //TODO nothing? since there's no subtype for Intent
            //registerOfIntent = expr.getOp();
            return;
        }

    }

    public void caseInvokeStmt(InvokeStmt stmt){
        //logger.debug("Checking invoke stmt {} in method {}", stmt, curentSootMethod);
        processInvokeExpr(stmt);
    }

    private void processInvokeExpr(Stmt stmt) {
        InvokeExpr invokeExpr = stmt.getInvokeExpr();
        if(invokeExpr instanceof StaticInvokeExpr)
            return;
        if(invokeExpr instanceof InstanceInvokeExpr) {
            InstanceInvokeExpr iinvokeExpr = (InstanceInvokeExpr)invokeExpr;
            Value regsiterThatInvokes = iinvokeExpr.getBase();
            //invokeExpr.getUseBoxes().get(0).getValue();
            //logger.debug("Found intent register {} {} at {} for {}", regsiterThatInvokes, registerOfIntent, stmt, curentSootMethod.getSubSignature());
            if (regsiterThatInvokes.equals(registerOfIntent) && regsiterThatInvokes.getType().toString().equals(START_ACTIVITY_CONSTANTS.INTENT_CLASS)) {
                //check if we have method that sets something
                // logger.debug("Checking intent instantiation (matching registers) for method {} \nand stmt {}", curentSootMethod, stmt);
                if (START_ACTIVITY_CONSTANTS.getIntentConstructors().contains(invokeExpr.getMethod().getSignature())) {
                    //   logger.debug("Checking intent instantiation for method {} \nand stmt {}", curentSootMethod, stmt);
                    switch (invokeExpr.getMethod().getSignature()) {
                        case START_ACTIVITY_CONSTANTS.INIT_DEFAULT: {
                            break;
                        }
                        case START_ACTIVITY_CONSTANTS.INIT_WITH_ACTION: {
                            processAction(stmt, invokeExpr, 0);
                            break;
                        }
                        case START_ACTIVITY_CONSTANTS.INIT_WITH_ACTION_URI: {
                            processAction(stmt, invokeExpr, 0);
                            processUri(stmt, invokeExpr, 1);
                            break;
                        }
                        case START_ACTIVITY_CONSTANTS.INIT_WITH_ACTION_URI_CONTEXT_CLASS: {
                            processAction(stmt, invokeExpr, 0);
                            processUri(stmt, invokeExpr, 1);
                            if (invokeExpr.getArg(2) instanceof ClassConstant) {
                                intentInfo.setClassName(((ClassConstant) invokeExpr.getArg(2)).getValue());
                            } else {
                                processClass(stmt, invokeExpr, 2);
                            }
                            break;
                        }
                        case START_ACTIVITY_CONSTANTS.INIT_WITH_CONTEXT_CLASS: {
                            if (invokeExpr.getArg(1) instanceof ClassConstant) {
                                intentInfo.setClassName(((ClassConstant) invokeExpr.getArg(1)).getValue());
                            } else {
                                processClass(stmt, invokeExpr, 1);
                            }
                            break;
                        }
                    }
                    //nothing to do. we rached a starting point of intents here
                    isDone = true;
                } else {
                    //just assignments
                    switch (invokeExpr.getMethod().getSignature()) {
                        case START_ACTIVITY_CONSTANTS.PUT_EXTRA:
                        case START_ACTIVITY_CONSTANTS.PUT_EXTRA2: {
                            StringPropagator stringPropagator = new StringPropagator(curentSootMethod, stmt, invokeExpr.getArg(0), elementId);
                            stringPropagator.run();
                            String extra = stringPropagator.getResult();
                            if (extra != null) {
                                intentInfo.addExtra(extra);
                            }
                            break;
                        }
                        case START_ACTIVITY_CONSTANTS.PUT_EXTRAS_BUNDLE: {
                            BundlesPropagator bundlesPropagator = new BundlesPropagator(curentSootMethod, stmt, invokeExpr.getArg(0));
                            bundlesPropagator.run();
                            Set<String> extras = bundlesPropagator.getResults();
                            extras.forEach(intentInfo::addExtra);
                            break;
                        }
                        case START_ACTIVITY_CONSTANTS.SET_ACTION: {
                            processAction(stmt, invokeExpr, 0);
                            break;
                        }
                        case START_ACTIVITY_CONSTANTS.SET_CLASS_NAME: {
                            processClass(stmt, invokeExpr, 1);
                            break;
                        }
                        case START_ACTIVITY_CONSTANTS.SET_CLASS: {
                            if (invokeExpr.getArg(1) instanceof ClassConstant) {
                                intentInfo.setClassName(((ClassConstant) invokeExpr.getArg(1)).getValue());
                            } else {
                                processClass(stmt, invokeExpr, 1);
                            }
                            break;
                        }
                        case START_ACTIVITY_CONSTANTS.SET_DATA_METHOD: {
                            processUri(stmt, invokeExpr, 0);
                            break;
                        }
                        case START_ACTIVITY_CONSTANTS.SET_COMPONENT: {
                            //analyze setComponent and find a target class
                            //logger.debug("Using setComponent {} {}", stmt, curentSootMethod);
                            Value componentReg = invokeExpr.getArg(0);

                            MHGDominatorsFinder<Unit> dominatorsFinder = new MHGDominatorsFinder<>(new ExceptionalUnitGraph(curentSootMethod.getActiveBody()));
                            Unit curUnit = dominatorsFinder.getImmediateDominator(stmt);
                            AnalyzeComponentSwitch compSwitch = new AnalyzeComponentSwitch(componentReg, curentSootMethod, elementId);
                            while (curUnit != null) {
                                //logger.debug("Previous unit {} {}", curUnit, curentSootMethod);
                                curUnit.apply(compSwitch);
                                if (compSwitch.getResult() != null) {
                                    //logger.debug("Found component name unit {} {} {}", compSwitch.getResult(), curUnit, curentSootMethod);
                                    intentInfo.setClassName(compSwitch.getResult());
                                    break;
                                }
                                curUnit = dominatorsFinder.getImmediateDominator(curUnit);
                            }

                            break;
                        }
                    }
                }

            }
        }
    }

    public Value getRegisterOfIntent() {
        return registerOfIntent;
    }

    private void processClass(Stmt stmt, InvokeExpr invokeExpr, int paramNumber) {
        StringPropagator stringPropagator = new StringPropagator(curentSootMethod, stmt, invokeExpr.getArg(paramNumber), elementId);
        stringPropagator.run();
        String className = stringPropagator.getResult();
        //here we check if we could return an array
        //then we set the result to be a field of intentInfo
        if(className != null){
            intentInfo.setClassName(className);
            intentInfo.setContextSensitiveClassNames(stringPropagator.getPotentialResults());
        }
        else intentInfo.setClassNameReg(invokeExpr.getArg(1).toString());
    }

    private void processUri(Stmt stmt, InvokeExpr invokeExpr, int paramNumber) {
        String uri = RAHelper.analyzeInvokeExpressionToFindUris(curentSootMethod.getActiveBody(), stmt, invokeExpr.getMethod(), invokeExpr.getArg(paramNumber), false);
        if(uri != null){
            intentInfo.setData(uri);
        }
    }

    private void processAction(Stmt stmt, InvokeExpr invokeExpr, int paramNumber) {
        StringPropagator stringPropagator = new StringPropagator(curentSootMethod, stmt, invokeExpr.getArg(paramNumber), elementId);
        stringPropagator.run();
        String action = stringPropagator.getResult();
        if(action != null){
            intentInfo.setAction(action);
        }
    }
}
