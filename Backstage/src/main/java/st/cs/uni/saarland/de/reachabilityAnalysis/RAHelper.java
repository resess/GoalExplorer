package st.cs.uni.saarland.de.reachabilityAnalysis;

import org.apache.commons.lang3.StringUtils;
import soot.*;
import soot.jimple.*;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.MHGDominatorsFinder;
import st.cs.uni.saarland.de.entities.FieldInfo;
import st.cs.uni.saarland.de.helpClasses.Helper;
import st.cs.uni.saarland.de.helpClasses.InterProcInfo;
import st.cs.uni.saarland.de.helpMethods.InterprocAnalysis2;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by avdiienko on 25/11/15.
 */
public class RAHelper {

    public final static Map<SootMethod, Set<CallSite>> callSitesForMethods = new ConcurrentHashMap<>();
    public static int numThreads = Runtime.getRuntime().availableProcessors();
    private static List<String> staticMethodSignatures;

    private static SootClass executorServiceClass;

    public static SootClass getExecutorServiceClass(){
        Integer tmpVar = 0;
        if(executorServiceClass == null){
            synchronized (tmpVar){
                executorServiceClass = Scene.v().getSootClass("java.util.concurrent.ExecutorService");
                return executorServiceClass;
            }
        }
        return executorServiceClass;
    }

    public static List<String> getStaticMethodSignatures(){
        if(staticMethodSignatures == null){
            initStaticUriSignatures();
        }
        return staticMethodSignatures;
    }
    private static List<SootClass> implementersOfExecutorServiceClass;

    public static List<SootClass> getImplementersOfExecutorServiceClass() {
        if(/*RAHelper.getExecutorServiceClass().isPhantom() ||*/ !RAHelper.getExecutorServiceClass().isInterface()){
            return new ArrayList<>();
        }
        if (implementersOfExecutorServiceClass == null) {
            initializeImplementersOfExecutorServiceClass();
        }
        return implementersOfExecutorServiceClass;
    }

    private static void initializeImplementersOfExecutorServiceClass(){
        implementersOfExecutorServiceClass = Scene.v().getActiveHierarchy().getImplementersOf(RAHelper.getExecutorServiceClass());
    }



    public static void initStaticUriSignatures(){

        staticMethodSignatures = new ArrayList<>();
        staticMethodSignatures.add(CONTENT_RESOLVER_CONSTANTS.QUERY);
        staticMethodSignatures.add(CONTENT_RESOLVER_CONSTANTS.INSERT);
        staticMethodSignatures.add(CONTENT_RESOLVER_CONSTANTS.BULKINSERT);
        staticMethodSignatures.add(CONTENT_RESOLVER_CONSTANTS.UPDATE);
        staticMethodSignatures.add(CONTENT_RESOLVER_CONSTANTS.DELETE);
    }

    public static String getValueOfTheVariable(Value l, Unit currentUnit, SootMethod currentSootMethod){
        MHGDominatorsFinder<Unit> domFinder = new MHGDominatorsFinder<Unit>(new ExceptionalUnitGraph(currentSootMethod.getActiveBody()));
        currentUnit = domFinder.getImmediateDominator(currentUnit);
        while(currentUnit != null){
            if((Stmt)currentUnit instanceof AssignStmt){
                AssignStmt stmt = (AssignStmt)currentUnit;
                if(stmt.getLeftOp().equals(l) && stmt.containsFieldRef()){
                    Set<FieldInfo> fieldResults = InterprocAnalysis2.getInstance().findInitializationsOfTheField2(stmt.getFieldRef().getField(), stmt, currentSootMethod);
                    for(FieldInfo fInfo : fieldResults){
                        if(fInfo.value != null){
                            return fInfo.value;
                        }
                    }
                }
                if(stmt.getLeftOp().equals(l) && stmt.containsInvokeExpr()){
                    List<InterProcInfo> infos = InterprocAnalysis2.getInstance().findReturnValueInMethod2(stmt);
                    for(InterProcInfo info : infos){
                        if(!StringUtils.isEmpty(info.getValueOfSearchedReg())){
                            return info.getValueOfSearchedReg();
                        }
                    }
                }
            }
            currentUnit = domFinder.getImmediateDominator(currentUnit);
        }
        return null;
    }

    public static String getValueOfTheVariableForTheButtonsClick(Value regToFind, Unit currentUnit, SootMethod currentSootMethod){
        MHGDominatorsFinder<Unit> domFinder = new MHGDominatorsFinder<Unit>(new ExceptionalUnitGraph(currentSootMethod.getActiveBody()));
        currentUnit = domFinder.getImmediateDominator(currentUnit);
        while(currentUnit != null){
            if(currentUnit instanceof AssignStmt){
                AssignStmt stmt = (AssignStmt)currentUnit;
                if(stmt.getLeftOp().equals(regToFind)){
                    if(stmt.containsFieldRef()) {
                        Set<FieldInfo> fieldResults = InterprocAnalysis2.getInstance().findInitializationsOfTheFieldForButtonClicks(stmt.getFieldRef().getField(), stmt, currentSootMethod.getDeclaringClass());
                        for (FieldInfo fInfo : fieldResults) {
                            if (fInfo.register != null) {
                                return getValueOfTheVariableForTheButtonsClick(fInfo.register, fInfo.unitToStart, fInfo.methodToStart.method());
                            }
                        }
                    }
                    else if(stmt.getRightOp() instanceof CastExpr){
                        CastExpr castExpr = (CastExpr)stmt.getRightOp();
                        regToFind = castExpr.getOp();
                    }
                    else if(stmt.getLeftOp().equals(regToFind) && stmt.containsInvokeExpr()){
                        InvokeExpr invExpr = stmt.getInvokeExpr();
                        if(invExpr.getMethod().getSubSignature().equals("android.view.View findViewById(int)")){
                            if(invExpr.getArg(0) instanceof IntConstant){
                                return String.valueOf(invExpr.getArg(0));
                            }
                            else{
                                return getValueOfTheVariable(invExpr.getArg(0), currentUnit, currentSootMethod);
                            }
                        }
                    }
                }

            }
            currentUnit = domFinder.getImmediateDominator(currentUnit);
        }
        return null;
    }

    public static void creatResultsDirIfNotExsist() {
        File theDir = new File(Helper.getResultsDir());
        if (!theDir.exists()) {
            boolean result = theDir.mkdir();
            if (result) {
                System.out.println(String.format("Results will be saved to '%s' directory", Helper.getResultsDir()));
            }
        }
    }

    public static boolean isClassInSystemPackage(String className) {
        return !className.startsWith(Helper.getPackageName()) && className.startsWith("android.") || className.startsWith("java.") || className.startsWith("sun.");
    }
    public static String analyzeInvokeExpressionToFindUris(final Body b, final Unit u, SootMethod method, Value uriRegister, boolean isContentResolver) {

        if (!isContentResolver || getStaticMethodSignatures().contains(method.getSignature())) {

            if (!(uriRegister instanceof Local) && !(uriRegister.getType().equals("android.net.Uri")))
                return null;

            Local localRegister = (Local) uriRegister;

            MHGDominatorsFinder<Unit> dominatorsFinder = new MHGDominatorsFinder<Unit>(new ExceptionalUnitGraph(b));
            UriFinderSwitch uriSwitch = new UriFinderSwitch(localRegister, b.getMethod());

            Unit currentUnit = u;
            while (dominatorsFinder.getImmediateDominator(currentUnit) != null && !uriSwitch.isStop()) {
                currentUnit = dominatorsFinder.getImmediateDominator(currentUnit);
                currentUnit.apply(uriSwitch);
            }
            String uri = uriSwitch.getUri();
            if(uri != null){
                if(!isContentResolver){
                    return uri;
                }
                return Helper.getSignatureOfSootMethod(method).replace("(android.net.Uri,", String.format("(%s,", uri.replace("<","").replace(">","")));
            }
        }
        return null;
    }

    public static IntentInfo analyzeStartActivityAndGetIntentInfo(final Body b, final Unit u, SootMethod method, List<Value> parameterValues){
        return null;
    }
}
