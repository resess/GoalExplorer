package st.cs.uni.saarland.de.uiAnalysis;

import soot.SootMethod;
import st.cs.uni.saarland.de.dissolveSpecXMLTags.StmtSwitchForTabView;
import st.cs.uni.saarland.de.dissolveSpecXMLTags.TabViewInfo;

import java.util.Set;

/**
 * Created by avdiienko on 12/05/16.
 */
public class TabViewsFinder implements Runnable {
    public Set<TabViewInfo> getTabViews() {
        return tabViews;
    }

    private Set<TabViewInfo> tabViews;
    private final BaseBackwardsWalker backwardsWalker;

    public TabViewsFinder(SootMethod currentSootMethod){
        backwardsWalker = new BaseBackwardsWalker(StmtSwitchForTabView.class, currentSootMethod);
    }
    @Override
    public void run() {
        backwardsWalker.run();
        tabViews = backwardsWalker.getResultTabInfos();
    }
}
