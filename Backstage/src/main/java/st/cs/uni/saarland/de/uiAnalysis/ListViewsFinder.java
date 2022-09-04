package st.cs.uni.saarland.de.uiAnalysis;

import soot.SootMethod;

import st.cs.uni.saarland.de.dissolveSpecXMLTags.ListViewInfo;
import st.cs.uni.saarland.de.dissolveSpecXMLTags.StmtSwitchForListView;
import st.cs.uni.saarland.de.helpMethods.CastInfoHelper;
import st.cs.uni.saarland.de.searchDynDecStrings.DynDecStringInfo;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ListViewsFinder implements Runnable{
    private Set<ListViewInfo> listViews;
    private final BaseForwardWalker forwardWalker;

    public ListViewsFinder(SootMethod currentSootMethod, Set<DynDecStringInfo> adapterInfo){
        forwardWalker = new BaseForwardWalker(StmtSwitchForListView.class, currentSootMethod, adapterInfo);
    }

    public ListViewsFinder(SootMethod currentSootMethod,  Map<String, String> elementsIDs){
        forwardWalker = new BaseForwardWalker(StmtSwitchForListView.class,  currentSootMethod, null, elementsIDs);
    }
    @Override
    public void run() {
        forwardWalker.run();
        listViews = CastInfoHelper.getInstance().getResultsInListViewInfos(forwardWalker.getResults());
    }

    public Set<ListViewInfo> getListViews()  { return listViews;}
}
