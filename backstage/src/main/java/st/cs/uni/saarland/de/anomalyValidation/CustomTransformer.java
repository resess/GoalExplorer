package st.cs.uni.saarland.de.anomalyValidation;

import soot.*;
import soot.jimple.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class CustomTransformer extends BodyTransformer {

    private final String apiSignature;


    public CustomTransformer(String api){
        apiSignature = api;
    }

    @Override
    protected void internalTransform(final Body b, String phaseName, @SuppressWarnings("rawtypes") Map options) {
        final PatchingChain<Unit> units = b.getUnits();
        //important to use snapshotIterator here
        for(final Iterator<Unit> iter = units.snapshotIterator(); iter.hasNext();) {
            final Unit u = iter.next();

            u.apply(new AbstractStmtSwitch() {

                public void caseAssignStmt(AssignStmt stmt){
                    boolean containsInvokeExpr = stmt.containsInvokeExpr();
                    if(containsInvokeExpr){
                        InvokeExpr invokeExpr = stmt.getInvokeExpr();
                        instrumentInvokeExpression(units, u, invokeExpr.getMethod());
                    }
                }

                public void caseInvokeStmt(InvokeStmt stmt) {
                    InvokeExpr invokeExpr = stmt.getInvokeExpr();
                    instrumentInvokeExpression(units, u, invokeExpr.getMethod());
                }

            });
            //it++;
        }
        b.validate();
    }

    private void instrumentInvokeExpression(final PatchingChain<Unit> units, final Unit u,
                                                   SootMethod method) {

            if(!method.getSignature().equals(apiSignature)) {
                return;
            }

            System.out.println("INSTRUMENTED: " + method.getSignature());

            //Initialization of list with arguments, which will be passed to my LogMethodEnter method. If we have up to 2 parameters, we can pass them explicitly, but if we have more than 2 - we need to create a list and put them there and pass this list.
            List<Value> argsToInsert = new ArrayList<>();
            argsToInsert.add(StringConstant.v("Backstage"));
            argsToInsert.add(StringConstant.v(method.getSignature()));

            //Invoke LogMethodEnter method
            units.insertAfter(Jimple.v().newInvokeStmt(
                    Jimple.v().newStaticInvokeExpr(Scene.v().getMethod("<android.util.Log: int i(java.lang.String,java.lang.String)>").makeRef(), argsToInsert )), u);
        }

    }

