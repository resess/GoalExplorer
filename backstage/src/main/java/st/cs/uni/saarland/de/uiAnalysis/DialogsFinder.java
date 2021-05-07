package st.cs.uni.saarland.de.uiAnalysis;

import soot.SootMethod;
import st.cs.uni.saarland.de.helpClasses.Info;
import st.cs.uni.saarland.de.helpMethods.CastInfoHelper;
import st.cs.uni.saarland.de.searchDialogs.DialogInfo;
import st.cs.uni.saarland.de.searchDialogs.StmtSwitchForDialogs;

import java.util.Set;

/**
 * Created by avdiienko on 11/05/16.
 */
public class DialogsFinder implements Runnable {
    private final BaseForwardWalker forwardWalker;
    private Set<DialogInfo> dialogs;

    public DialogsFinder(SootMethod currentSootMethod){
        forwardWalker = new BaseForwardWalker(StmtSwitchForDialogs.class, currentSootMethod);
    }

    @Override
    public void run() {
        forwardWalker.run();
        Set<Info> results = forwardWalker.getResults();
        dialogs = CastInfoHelper.getInstance().getResultsInDialogInfo(results);
    }

    public Set<DialogInfo> getDialogs(){
        return dialogs;
    }
}
