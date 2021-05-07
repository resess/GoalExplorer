package st.cs.uni.saarland.de.reachabilityAnalysis;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import st.cs.uni.saarland.de.helpClasses.Helper;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by avdiienko on 22/04/16.
 */
@XStreamAlias("apiiintentnformation")
public class IntentInfo extends ApiInfoForForward {

    public IntentInfo(){
        this.extras = new HashSet<>();
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    private String className;

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
}
