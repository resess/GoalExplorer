package android.goal.explorer.analysis;

import soot.Local;
import soot.PointsToAnalysis;
import soot.Scene;
import soot.Type;
import soot.Value;
import soot.jimple.spark.geom.dataMgr.Obj_full_extractor;
import soot.jimple.spark.geom.dataMgr.PtSensVisitor;
import soot.jimple.spark.geom.dataRep.IntervalContextVar;
import soot.jimple.spark.geom.geomPA.GeomPointsTo;
import soot.jimple.spark.geom.geomPA.GeomQueries;
import soot.jimple.spark.pag.AllocNode;
import soot.jimple.toolkits.callgraph.Edge;

import java.util.HashSet;
import java.util.Set;

public class TypeAnalyzer {
    private static TypeAnalyzer instance;

    public static synchronized TypeAnalyzer v() {
        if (instance == null)
            instance = new TypeAnalyzer();
        return instance;
    }

    /**
     * Gets the possible types of a value
     * @param arg The local variable
     * @return The set of possible types
     */
    public synchronized Set<Type> getPointToPossibleTypes(Value arg) {
        PointsToAnalysis PTA = Scene.v().getPointsToAnalysis();
        return PTA.reachingObjects((Local)arg).possibleTypes();
    }

    /**
     * Gets the possible types of a value (context-sensitive)
     * @param arg The local variable
     * @param x The edge in the callgraph that contains the context
     * @return The set of possible types
     */
    public synchronized Set<Type> getContextPointToPossibleTypes(Value arg, Edge x) {
        return getContextPointToPossibleTypes(arg, new Edge[]{x});
    }

    /**
     * Gets the possible types of a value (context-sensitive)
     * @param arg The local variable
     * @param x The set of k edges that maintains the kCFA context
     * @return The set of possible types
     */
    public synchronized Set<Type> getContextPointToPossibleTypes(Value arg, Edge[] x) {
        GeomPointsTo geomPTA = (GeomPointsTo) Scene.v().getPointsToAnalysis();
        GeomQueries geomQueries = new GeomQueries(geomPTA);
        Set<Type> geomContextTypes = new HashSet<>();
        PtSensVisitor<?> visitor = new Obj_full_extractor();
        if (geomQueries.kCFA(x, (Local)arg, visitor)) {
            for (Object icv_obj : visitor.outList) {
                IntervalContextVar icv = (IntervalContextVar) icv_obj;
                AllocNode obj = (AllocNode) icv.var;
                Type type = obj.getType();
                geomContextTypes.add(type);
            }
        }
        return geomContextTypes;
    }
}
