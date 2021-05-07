package st.cs.uni.saarland.de.uiAnalysis;

import soot.Local;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.InvokeExpr;
import soot.jimple.Stmt;
import st.cs.uni.saarland.de.entities.Activity;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by avdiienko on 25/07/16.
 */
public class ActivityTitleFinder implements Runnable {
    private final SootMethod sootMethod;
    private final Set<Activity> activities;
    private final String activityName;
    private final Set<SootClass> subclassesOfActivity;
    private final Set<String> activityNames;
    private Activity currentActivity;

    public ActivityTitleFinder(SootMethod currentSootMethod, Set<Activity> activities, String activityName, List<SootClass> subclassesOfActivity) {
        this.sootMethod = currentSootMethod;
        this.activities = activities;
        this.activityNames = this.activities.stream().map(x->x.getName()).collect(Collectors.toSet());
        this.activityName = activityName;
        this.subclassesOfActivity = subclassesOfActivity.stream().collect(Collectors.toSet());
    }

    @Override
    public void run() {
        if(!activityNames.contains(activityName)){
            return;
        }
        this.currentActivity = activities.stream().filter(x->x.getName().equals(activityName)).findAny().get();

        if(!sootMethod.hasActiveBody()){
            return;
        }

        for(Unit unit: sootMethod.getActiveBody().getUnits()){
            Stmt stmt = (Stmt)unit;
            if(stmt.containsInvokeExpr()){
                InvokeExpr expr = stmt.getInvokeExpr();
                if((expr.getMethod().getSubSignature().equals("void setTitle(int)") || expr.getMethod().getSubSignature().equals("void setTitle(java.lang.CharSequence)"))
                        && subclassesOfActivity.contains(expr.getMethod().getDeclaringClass())){
                    if(!(expr.getArg(0) instanceof Local)){
                        currentActivity.setLabel(expr.getArg(0).toString());
                        return;
                    }
                }
            }
        }
    }

}
