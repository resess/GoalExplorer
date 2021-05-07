package st.cs.uni.saarland.de.uiAnalysis;

import soot.SootMethod;
import st.cs.uni.saarland.de.helpMethods.CastInfoHelper;
import st.cs.uni.saarland.de.searchMenus.MenuInfo;
import st.cs.uni.saarland.de.searchMenus.StmtSwitchForOptionMenus;

import java.util.Set;

/**
 * Created by avdiienko on 11/05/16.
 */
public class OptionsMenusFinder implements Runnable {
    private Set<MenuInfo> optionMenus;
    private final SpecMethodsForwardWalker specMethodsForwardWalker;

    public OptionsMenusFinder(SootMethod currentSootMethod){
        specMethodsForwardWalker = new SpecMethodsForwardWalker(StmtSwitchForOptionMenus.class, currentSootMethod, "onCreateOptionsMenu");
    }

    @Override
    public void run() {
        specMethodsForwardWalker.run();
        optionMenus = CastInfoHelper.getInstance().getResultsInMenuInfo(specMethodsForwardWalker.getResults());
    }

    public Set<MenuInfo> getOptionMenus(){
        return optionMenus;
    }
}
