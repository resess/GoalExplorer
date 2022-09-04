package st.cs.uni.saarland.de.uiAnalysis;

import soot.SootMethod;
import st.cs.uni.saarland.de.helpMethods.CastInfoHelper;
import st.cs.uni.saarland.de.searchMenus.MenuInfo;
import st.cs.uni.saarland.de.searchMenus.StmtSwitchForOptionMenus;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.Map;

/**
 * Created by avdiienko on 11/05/16.
 */
public class OptionsMenusFinder implements Runnable {
    private Set<MenuInfo> optionMenus;
    private static final Set<String> searchedMethods = new HashSet<String>(Arrays.asList("onCreateOptionsMenu", "onPrepareOptionsMenu"));
    private final SpecMethodsForwardWalker specMethodsForwardWalker;

    public OptionsMenusFinder(SootMethod currentSootMethod){
        specMethodsForwardWalker = new SpecMethodsForwardWalker(StmtSwitchForOptionMenus.class, currentSootMethod, searchedMethods);
    }

    public OptionsMenusFinder(SootMethod currentSootMethod, Map<String, String> dynStrings){
        specMethodsForwardWalker = new SpecMethodsForwardWalker(StmtSwitchForOptionMenus.class, currentSootMethod, searchedMethods, dynStrings);
    }

    @Override
    public void run() {
        specMethodsForwardWalker.run();
        optionMenus = CastInfoHelper.getInstance().getResultsInMenuInfo(specMethodsForwardWalker.getResults());
        //specMethodsForwardWalker2.run();
        //optionMenus.addAll(CastInfoHelper.getInstance().getResultsInMenuInfo(specMethodsForwardWalker2.getResults()));
    }

    public Set<MenuInfo> getOptionMenus(){
        return optionMenus;
    }
}
