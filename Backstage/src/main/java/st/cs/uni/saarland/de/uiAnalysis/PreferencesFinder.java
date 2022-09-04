package st.cs.uni.saarland.de.uiAnalysis;

import soot.SootMethod;
import st.cs.uni.saarland.de.helpMethods.CastInfoHelper;
import st.cs.uni.saarland.de.searchPreferences.PreferenceInfo;
import st.cs.uni.saarland.de.searchPreferences.StmtSwitchForPreferences;

import java.util.*;

/**
 * Created by Faridah on 07/02/2022
 */
public class PreferencesFinder implements Runnable {
    private final BaseForwardWalker forwardWalker;
    private Set<PreferenceInfo> preferences;

    public PreferencesFinder(SootMethod currentSootMethod){
        forwardWalker = new BaseForwardWalker(StmtSwitchForPreferences.class, currentSootMethod);
    }

    @Override
    public void run() {
        forwardWalker.run();
        preferences = CastInfoHelper.getInstance().getResultsInPreferenceInfo(forwardWalker.getResults());
    }

    public Set<PreferenceInfo> getPreferences(){
        return preferences;
    }
}