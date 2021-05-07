package st.cs.uni.saarland.de.uiAnalysis;

import soot.SootMethod;
import st.cs.uni.saarland.de.searchScreens.LayoutInfo;
import st.cs.uni.saarland.de.searchScreens.StmtSwitchForLayoutInflater;

import java.util.Map;

/**
 * Created by avdiienko on 11/05/16.
 */
public class LayoutsFinder implements Runnable {
    private final BaseBackwardsWalker backwardsWalker;
    private Map<Integer, LayoutInfo> layouts;

    public LayoutsFinder(SootMethod currentSootMethod, String callerSootClass){
        backwardsWalker = new BaseBackwardsWalker(StmtSwitchForLayoutInflater.class, currentSootMethod, callerSootClass);
    }

    @Override
    public void run() {
        backwardsWalker.run();
        layouts = backwardsWalker.getResultLayoutInfos();
    }

    public Map<Integer, LayoutInfo> getLayouts(){
        return layouts;
    }
}
