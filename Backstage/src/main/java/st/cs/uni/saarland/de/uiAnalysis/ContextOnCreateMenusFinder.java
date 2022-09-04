package st.cs.uni.saarland.de.uiAnalysis;

import soot.SootMethod;
import st.cs.uni.saarland.de.helpMethods.CastInfoHelper;
import st.cs.uni.saarland.de.searchMenus.MenuInfo;
import st.cs.uni.saarland.de.searchMenus.StmtSwitchForContextMenus;

import java.util.Set;
import java.util.Map;
/**
 * Created by avdiienko on 11/05/16.
 */
public class ContextOnCreateMenusFinder implements Runnable {
    private Set<MenuInfo> contextOnCreateMenus;
    private final SpecMethodsForwardWalker specMethodsForwardWalker;

    public ContextOnCreateMenusFinder(SootMethod currentSootMethod){
        specMethodsForwardWalker = new SpecMethodsForwardWalker(StmtSwitchForContextMenus.class, currentSootMethod, "onCreate");
    }

    public ContextOnCreateMenusFinder(SootMethod currentSootMethod, Map<String, String> dynStrings){
        specMethodsForwardWalker = new SpecMethodsForwardWalker(StmtSwitchForContextMenus.class, currentSootMethod, "onCreate", dynStrings);
    }

    @Override
    public void run() {
        specMethodsForwardWalker.run();
        contextOnCreateMenus = CastInfoHelper.getInstance().getResultsInMenuInfo(specMethodsForwardWalker.getResults());
    }

    public Set<MenuInfo> getContextOnCreateMenus(){
        return contextOnCreateMenus;
    }
}
