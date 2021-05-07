package st.cs.uni.saarland.de.uiAnalysis;

import soot.SootMethod;
import st.cs.uni.saarland.de.helpMethods.CastInfoHelper;
import st.cs.uni.saarland.de.searchMenus.PopupMenuInfo;
import st.cs.uni.saarland.de.searchMenus.StmtSwitchForPopupMenus;

import java.util.Set;

/**
 * Created by avdiienko on 11/05/16.
 */
public class PopupMenusFinder implements Runnable {
    private Set<PopupMenuInfo> popupMenus;
    private final BaseForwardWalker forwardWalker;

    public PopupMenusFinder(SootMethod currentSootMethod){
        forwardWalker = new BaseForwardWalker(StmtSwitchForPopupMenus.class, currentSootMethod);
    }

    @Override
    public void run() {
        forwardWalker.run();
        popupMenus = CastInfoHelper.getInstance().getResultsInPopupMenuInfo(forwardWalker.getResults());
    }

    public Set<PopupMenuInfo> getPopupMenus(){
        return popupMenus;
    }
}
