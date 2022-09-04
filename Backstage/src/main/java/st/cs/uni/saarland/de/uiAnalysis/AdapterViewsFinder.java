package st.cs.uni.saarland.de.uiAnalysis;

import soot.SootMethod;
import st.cs.uni.saarland.de.dissolveSpecXMLTags.AdapterViewInfo;
import st.cs.uni.saarland.de.dissolveSpecXMLTags.StmtSwitchForAdapterView;
import st.cs.uni.saarland.de.helpMethods.CastInfoHelper;
import st.cs.uni.saarland.de.searchDynDecStrings.DynDecStringInfo;

import java.util.Map;
import java.util.Set;

public class AdapterViewsFinder implements Runnable{
    private Set<AdapterViewInfo> adapterViews;
    private final BaseForwardWalker forwardWalker;

    public AdapterViewsFinder(SootMethod currentSootMethod, Set<DynDecStringInfo> adapterInfo){
        forwardWalker = new BaseForwardWalker(StmtSwitchForAdapterView.class, currentSootMethod, adapterInfo);
    }

    public AdapterViewsFinder(SootMethod currentSootMethod,  Map<String, String> elementsIDs){
        forwardWalker = new BaseForwardWalker(StmtSwitchForAdapterView.class,  currentSootMethod, null, elementsIDs);
    }
    @Override
    public void run() {
        forwardWalker.run();
        adapterViews = CastInfoHelper.getInstance().getResultsInAdapterViewInfos(forwardWalker.getResults());
    }

    public Set<AdapterViewInfo> getAdapterViews()  { return adapterViews;}
}
