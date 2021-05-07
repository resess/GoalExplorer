package android.goal.explorer.analysis.value.analysis;

import android.goal.explorer.analysis.value.AnalysisParameters;
import android.goal.explorer.analysis.value.Constants;
import soot.Local;
import soot.Scene;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.jimple.DefinitionStmt;
import soot.jimple.IntConstant;
import soot.jimple.InvokeExpr;
import soot.jimple.LongConstant;
import soot.jimple.ParameterRef;
import soot.jimple.ReturnStmt;
import soot.jimple.Stmt;
import soot.jimple.toolkits.callgraph.Edge;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * An argument value analysis for integer types.
 */
public class IntValueAnalysis extends BackwardValueAnalysis {
    private static final int TOP_VALUE = Constants.ANY_INT;

    @Override
    public Set<Object> computeInlineArgumentValues(String[] inlineValues) {
        Set<Object> result = new HashSet<>(inlineValues.length);

        for (String intString : inlineValues) {
            result.add(Integer.parseInt(intString));
        }

        return result;
    }

    @Override
    public Set<Object> computeVariableValues(Value value, Stmt callSite) {
        return computeVariableValues(value, callSite, null);
    }

    /**
     * Returns the possible values for an integer variable.
     *
     * @param value The variable whose value we are looking for.
     * @param start The statement where the variable is used.
     * @return The set of possible values for the variable.
     */
    @Override
    public Set<Object> computeVariableValues(Value value, Stmt start, Set<List<Edge>> edges) {
        if (value instanceof IntConstant) {
            return Collections.singleton((Object) ((IntConstant) value).value);
        } else if (value instanceof LongConstant) {
            return Collections.singleton((Object) ((LongConstant) value).value);
        } else if (value instanceof Local) {
            return findIntAssignmentsForLocal(start, (Local) value, new HashSet<Stmt>(), edges);
        } else {
            return Collections.singleton((Object) TOP_VALUE);
        }
    }

    /**
     * Return all possible values for an integer local variable.
     *
     * @param start The statement where the analysis should start.
     * @param local The local variable whose values we are looking for.
     * @param visitedStmts The set of visited statement.
     * @return The set of possible values for the local variable.
     */
    private Set<Object> findIntAssignmentsForLocal(Stmt start, Local local, Set<Stmt> visitedStmts,
                                                   Set<List<Edge>> contextEdges) {
        List<DefinitionStmt> assignStmts =
                findAssignmentsForLocal(start, local, true, new HashSet<>());
        Set<Object> result = new HashSet<>(assignStmts.size());

        for (DefinitionStmt assignStmt : assignStmts) {
            Value rhsValue = assignStmt.getRightOp();
            if (rhsValue instanceof IntConstant) {
                result.add(((IntConstant) rhsValue).value);
            } else if (rhsValue instanceof LongConstant) {
                result.add(((LongConstant) rhsValue).value);
            } else if (rhsValue instanceof ParameterRef) {
                ParameterRef parameterRef = (ParameterRef) rhsValue;
                Iterator<Edge> edges =
                        Scene.v().getCallGraph()
                                .edgesInto(AnalysisParameters.v().getIcfg().getMethodOf(assignStmt));
                while (edges.hasNext()) {
                    Edge edge = edges.next();
                    InvokeExpr invokeExpr = edge.srcStmt().getInvokeExpr();
                    Value argValue = invokeExpr.getArg(parameterRef.getIndex());
                    if (argValue instanceof IntConstant) {
                        result.add(((IntConstant) argValue).value);
                    } else if (argValue instanceof LongConstant) {
                        result.add(((LongConstant) argValue).value);
                    } else if (argValue instanceof Local) {
                        Set<Object> newResults =
                                findIntAssignmentsForLocal(edge.srcStmt(), (Local) argValue, visitedStmts, contextEdges);
                        result.addAll(newResults);
                    } else {
                        result.add(TOP_VALUE);
                    }
                }
            } else if (rhsValue instanceof InvokeExpr) {
                SootMethod sm = ((InvokeExpr) rhsValue).getMethod();
                for (List<Edge> edgeList : contextEdges) {
                    Edge edge = edgeList.iterator().next();
                    // Check for method overridden
                    if (edge.src().getDeclaringClass().declaresMethod(sm.getSubSignature()))
                        sm = edge.src().getDeclaringClass().getMethod(sm.getSubSignature());

                    Collection<Unit> returnSites = AnalysisParameters.v().getIcfg().getEndPointsOf(sm);
                    for (Unit returnSite : returnSites) {
                        if (returnSite instanceof ReturnStmt) {
                            Value value = ((ReturnStmt) returnSite).getOp();
                            return computeVariableValues(value, (Stmt)returnSite);
                        }
                    }
                }
            } else {
                return Collections.singleton((Object) TOP_VALUE);
            }
        }

        return result;
    }

    @Override
    public Object getTopValue() {
        return TOP_VALUE;
    }

}
