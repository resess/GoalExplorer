package st.cs.uni.saarland.de.uiAnalysis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import soot.SootMethod;
import st.cs.uni.saarland.de.searchTabs.StmtSwitchForTab;
import st.cs.uni.saarland.de.searchTabs.TabInfo;

import java.util.Set;

/**
 * Created by avdiienko on 12/05/16.
 */
public class TabsFinder implements Runnable {

    private final Logger logger =  LoggerFactory.getLogger(Thread.currentThread().getName());

    public Set<TabInfo> getTabs() {
        return tabs;
    }

    private Set<TabInfo> tabs;
    private final BaseBackwardsWalker backwardsWalker;

    public TabsFinder(SootMethod currentSootMethod){
        backwardsWalker = new BaseBackwardsWalker(StmtSwitchForTab.class, currentSootMethod);
    }
    @Override
    public void run() {
        backwardsWalker.run();
        Set<TabInfo> tempTabs = backwardsWalker.getResultTabInfos();
        tabs = tempTabs;
    }
}
