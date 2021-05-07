package st.cs.uni.saarland.de.uiAnalysis;

import soot.SootMethod;
import st.cs.uni.saarland.de.helpMethods.CastInfoHelper;
import st.cs.uni.saarland.de.searchListener.ListenerInfo;
import st.cs.uni.saarland.de.searchListener.StmtSwitchToFindListener;

import java.util.Set;

/**
 * Created by avdiienko on 11/05/16.
 */
public class ListenersFinder implements Runnable {
    private Set<ListenerInfo> listeners ;
    private final BaseForwardWalker forwardWalker;

    public ListenersFinder(SootMethod currentSootMethod){
        forwardWalker = new BaseForwardWalker(StmtSwitchToFindListener.class, currentSootMethod);
    }

    @Override
    public void run() {
        forwardWalker.run();
        listeners = CastInfoHelper.getInstance().getResultsInListenerInfo(forwardWalker.getResults());
    }

    public Set<ListenerInfo> getListeners(){
        return listeners;
    }
}
