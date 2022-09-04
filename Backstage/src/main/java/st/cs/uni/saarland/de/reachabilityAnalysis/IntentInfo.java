package st.cs.uni.saarland.de.reachabilityAnalysis;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.apache.commons.lang3.StringUtils;
import st.cs.uni.saarland.de.helpClasses.Helper;

import java.util.*;

/**
 * Created by avdiienko on 22/04/16.
 */
@XStreamAlias("apiiintentnformation")
public class IntentInfo extends ApiInfoForForward {

    public IntentInfo(){
        this.extras = new HashSet<>();
    }

    public String getClassName() {
        if(className == null && actionTargets.size() > 0) {
            className = actionTargets.get(0);
        }
        return className;
    }

    public void setClassName(String className) {
        if(StringUtils.isBlank(this.className) || !this.className.startsWith("L"))
            this.className = className;
    }

    public String getClassNameReg() { return classNameReg; }

    public void setClassNameReg(String classNameReg) {
        this.classNameReg = classNameReg;
    }

    private String className;
    private String classNameReg = "";

    private Map<String, String> contextSensitiveClassNames;
    private List<String> actionTargets = new ArrayList<>();

    public Map<String, String> getContextSensitiveClassNames() {
        return contextSensitiveClassNames;
    }

    public void setContextSensitiveClassNames(Map<String, String> contextSensitiveClassNames) {
        this.contextSensitiveClassNames = contextSensitiveClassNames;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    private String data;

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
        actionTargets.addAll(IntentFilterResolver.getTargetComponents(action));
    }

    private String action;

    public Set<String> getExtras() {
        return extras;
    }

    public void addExtra(String extra) {
        this.extras.add(extra);
    }

    private Set<String> extras;

    @Override
    public String toString(){
        if (api != null) {
            return String.format("API: %s; DepthMethodLevel: %s; DepthComponentLevel: %s",
                    Helper.getSignatureOfSootMethod(api), depthMethodLevel, depthComponentLevel);
        } else {
            return String.format("API: %s; DepthMethodLevel: %s; DepthComponentLevel: %s",
                    "Null", depthMethodLevel, depthComponentLevel);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        IntentInfo that = (IntentInfo) o;
        return Objects.equals(className, that.className) && Objects.equals(data, that.data) && Objects.equals(action, that.action);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), className, data, action);
    }
}
