package st.cs.uni.saarland.de.anomalyValidation;

import soot.*;
import soot.jimple.*;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.MHGDominatorsFinder;

import java.io.*;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by avdiienko on 04/04/16.
 */
public class WebViewTransformer extends BodyTransformer {

    public final String webView="<android.webkit.WebView: void loadDataWithBaseURL(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String)>";
    private final String apkName;

    public WebViewTransformer(String apkName){
        this.apkName = apkName;
    }

    private static void createDirIfNotExsist(String name){
        File theDir = new File(name);
        if (!theDir.exists()) {
            theDir.mkdir();
        }
    }

    private void write(String clazz, String value){
        createDirIfNotExsist("webViewResults");
        try(PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("webViewResults/"+apkName+"_res.txt", true)))) {
            out.println(clazz + ";" + value);
            //more code
        }catch (IOException e) {
            e.printStackTrace();
        }
    }
    @Override
    protected void internalTransform(final Body b, String phaseName, @SuppressWarnings("rawtypes") Map options) {
        final PatchingChain<Unit> units = b.getUnits();
        List<SootClass> subclasses = Scene.v().getActiveHierarchy().getSubclassesOf(Scene.v().getSootClass("android.app.Activity"));
        if(!subclasses.contains(b.getMethod().getDeclaringClass())){
            return;
        }
        for(final Iterator<Unit> iter = units.snapshotIterator(); iter.hasNext();) {
            final Unit u = iter.next();

            u.apply(new AbstractStmtSwitch() {

                public void caseInvokeStmt(InvokeStmt stmt) {
                    InvokeExpr invokeExpr = stmt.getInvokeExpr();
                    if(invokeExpr.getMethod().getSignature().equals(webView)){
                        Value arg = invokeExpr.getArg(1);
                        if(arg instanceof StringConstant){
                            write(b.getMethod().getDeclaringClass().getName(), ((StringConstant)arg).value);
                        }
                        else{
                            MHGDominatorsFinder<Unit> dominatorsFinder = new MHGDominatorsFinder<>(new ExceptionalUnitGraph(b));
                            Unit curUnit = dominatorsFinder.getImmediateDominator(u);
                            boolean stop = false;
                            while (curUnit != null && !stop){
                                if(curUnit instanceof AssignStmt){
                                    AssignStmt assignStmt = ((AssignStmt)curUnit);
                                    if(assignStmt.getLeftOp().equals(arg)){
                                        if(assignStmt.getRightOp() instanceof StringConstant){
                                            stop = true;
                                            write(b.getMethod().getDeclaringClass().getName(), ((StringConstant)assignStmt.getRightOp()).value);
                                        }
                                        else if(assignStmt.containsInvokeExpr() && assignStmt.getInvokeExpr().getMethod().getSubSignature().equals("java.lang.String getString(int)")){
                                            stop = true;
                                            write(b.getMethod().getDeclaringClass().getName(), assignStmt.getInvokeExpr().getArg(0).toString());
                                        }
                                        else{
                                            stop = true;
                                            write(b.getMethod().getDeclaringClass().getName(), assignStmt.getRightOp().toString());
                                        }
                                    }
                                }
                                curUnit = dominatorsFinder.getImmediateDominator(curUnit);
                            }
                        }
                    }
                }

            });
            //it++;
        }
        b.validate();
    }
}
