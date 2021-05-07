package st.cs.uni.saarland.de.uiAnalysis;

import soot.SootMethod;
import st.cs.uni.saarland.de.helpMethods.CastInfoHelper;
import st.cs.uni.saarland.de.searchMenus.DropDownNavMenuInfo;
import st.cs.uni.saarland.de.searchMenus.StmtSwitchForNavDropDownMenus;

import java.util.Set;

/**
 * Created by avdiienko on 12/05/16.
 */
public class NavigationDropDownMenusFinder implements Runnable {
    public Set<DropDownNavMenuInfo> getDropDownMenus() {
        return dropDownMenus;
    }

    private Set<DropDownNavMenuInfo> dropDownMenus;
    private final BaseForwardWalker forwardWalker;

    public NavigationDropDownMenusFinder(SootMethod currentSootMethod){
        forwardWalker = new BaseForwardWalker(StmtSwitchForNavDropDownMenus.class, currentSootMethod);
    }

    @Override
    public void run() {
        forwardWalker.run();
        dropDownMenus = CastInfoHelper.getInstance().getResultsInDropDownNavMenuInfo(forwardWalker.getResults());
    }
}
