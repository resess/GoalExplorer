package st.cs.uni.saarland.de.uiAnalysis;

import soot.SootMethod;
import st.cs.uni.saarland.de.helpClasses.MyStmtSwitch;

/**
 * Created by avdiienko on 11/05/16.
 */
public class SpecMethodsForwardWalker extends BaseForwardWalker implements Runnable {
    private final String searchedMethod;

    public SpecMethodsForwardWalker(Class<? extends MyStmtSwitch> requiredClass,
                                    SootMethod currentMethod, String searchedMethod) {
        super(requiredClass, currentMethod);
        this.searchedMethod = searchedMethod;
    }

    @Override
    public void run() {
        if (!currentMethod.hasActiveBody() && !currentMethod.getName().equals(searchedMethod)) {
            return;
        }
        super.run();
    }
}
