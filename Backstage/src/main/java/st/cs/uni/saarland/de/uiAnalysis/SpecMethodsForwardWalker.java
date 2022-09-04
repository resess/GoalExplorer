package st.cs.uni.saarland.de.uiAnalysis;

import soot.SootMethod;
import st.cs.uni.saarland.de.helpClasses.MyStmtSwitch;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by avdiienko on 11/05/16.
 */
public class SpecMethodsForwardWalker extends BaseForwardWalker implements Runnable {
    private final Set<String> searchedMethods = new HashSet<>();

    public SpecMethodsForwardWalker(Class<? extends MyStmtSwitch> requiredClass,
                                    SootMethod currentMethod, String searchedMethod) {
        super(requiredClass, currentMethod);
        this.searchedMethods.add(searchedMethod);
    }

    public SpecMethodsForwardWalker(Class<? extends MyStmtSwitch> requiredClass,
                                    SootMethod currentMethod, String searchedMethod, Map<String, String> dynStrings) {
        super(requiredClass, currentMethod, dynStrings);
        this.searchedMethods.add(searchedMethod);
    }

    public SpecMethodsForwardWalker(Class<? extends MyStmtSwitch> requiredClass,
                                    SootMethod currentMethod, Set<String> searchedMethods) {
        super(requiredClass, currentMethod);
        this.searchedMethods.addAll(searchedMethods);
    }

    public SpecMethodsForwardWalker(Class<? extends MyStmtSwitch> requiredClass,
                                    SootMethod currentMethod, Set<String> searchedMethods, Map<String, String> dynStrings) {
        super(requiredClass, currentMethod, dynStrings);
        this.searchedMethods.addAll(searchedMethods);
    }

    @Override
    public void run() {
        if (!currentMethod.hasActiveBody() || !searchedMethods.contains(currentMethod.getName())) {
            return;
        }
        super.run();
    }
}
