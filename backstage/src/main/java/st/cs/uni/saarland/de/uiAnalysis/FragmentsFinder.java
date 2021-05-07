package st.cs.uni.saarland.de.uiAnalysis;

import soot.SootMethod;
import st.cs.uni.saarland.de.dissolveSpecXMLTags.FragmentDynInfo;
import st.cs.uni.saarland.de.dissolveSpecXMLTags.StmtSwitchForFragments;
import st.cs.uni.saarland.de.helpMethods.CastInfoHelper;

import java.util.Set;

/**
 * Created by avdiienko on 11/05/16.
 */
public class FragmentsFinder implements Runnable {
    private Set<FragmentDynInfo> fragments;
    private final BaseForwardWalker forwardWalker;

    public FragmentsFinder(SootMethod currentSootMethod){
        forwardWalker = new BaseForwardWalker(StmtSwitchForFragments.class, currentSootMethod);
    }

    @Override
    public void run() {
        forwardWalker.run();
        fragments = CastInfoHelper.getInstance().getResultsInFragmentDynInfos(forwardWalker.getResults());
    }

    public Set<FragmentDynInfo> getFragments(){
        return fragments;
    }
}
