package st.cs.uni.saarland.de.uiAnalysis;

import soot.SootMethod;
import st.cs.uni.saarland.de.helpMethods.CastInfoHelper;
import st.cs.uni.saarland.de.searchDynDecStrings.DynDecStringInfo;
import st.cs.uni.saarland.de.searchDynDecStrings.StmtSwitchForStrings;

import java.util.Set;

/**
 * Created by avdiienko on 11/05/16.
 */
public class StringsFinder implements Runnable {
    private Set<DynDecStringInfo> strings;
    private final BaseForwardWalker forwardWalker;

    public StringsFinder(SootMethod currentSootMethod){
        forwardWalker = new BaseForwardWalker(StmtSwitchForStrings.class, currentSootMethod);
    }

    @Override
    public void run() {
        forwardWalker.run();
        strings = CastInfoHelper.getInstance().getResultsInDynDecStringInfo(forwardWalker.getResults());
    }

    public Set<DynDecStringInfo> getStrings(){
        return strings;
    }
}
