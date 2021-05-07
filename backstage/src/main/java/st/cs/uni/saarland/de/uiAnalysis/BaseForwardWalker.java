package st.cs.uni.saarland.de.uiAnalysis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import soot.Body;
import soot.SootMethod;
import st.cs.uni.saarland.de.helpClasses.Helper;
import st.cs.uni.saarland.de.helpClasses.Info;
import st.cs.uni.saarland.de.helpClasses.MyStmtSwitch;
import st.cs.uni.saarland.de.helpMethods.IterateOverUnitsHelper;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by avdiienko on 11/05/16.
 */
public class BaseForwardWalker implements Runnable {

    protected Set<Info> resultList;
    private final Class<? extends MyStmtSwitch> switchClass;
    protected final SootMethod currentMethod;

    public BaseForwardWalker(Class<? extends MyStmtSwitch> requiredClass, SootMethod currentMethod) {
        resultList= Collections.synchronizedSet(new LinkedHashSet<>());
        switchClass = requiredClass;
        this.currentMethod = currentMethod;
    }

    public Set<Info> getResults(){
        return resultList;
    }

    @Override
    public void run() {
        if(!currentMethod.hasActiveBody()){
            return;
        }
        final Body body = currentMethod.getActiveBody();
        if (!Helper.processMethod(body.getUnits().size()) || !body.getMethod().getDeclaringClass().getName().startsWith(Helper.getPackageName())) {
            return;
        }

        try {

            final MyStmtSwitch stmtSwitch = this.switchClass.getConstructor(SootMethod.class).newInstance((SootMethod) null);
            stmtSwitch.init();
            stmtSwitch.setCurrentSootMethod(body.getMethod());

            IterateOverUnitsHelper.newInstance().runUnitsOverMethodBackwards(body, stmtSwitch);

            Set<Info> res = stmtSwitch.getResultInfos();
            resultList.addAll(res);

        } catch (Exception e) {
            e.printStackTrace();
            Helper.saveToStatisticalFile(Helper.exceptionStacktraceToString(e));
        }
    }
}
