package android.goal.explorer.cmdline;

import soot.jimple.infoflow.InfoflowConfiguration;
import soot.jimple.infoflow.android.InfoflowAndroidConfiguration;

public class GlobalConfig {


    // FlowDroid configuration
    private InfoflowAndroidConfiguration flowdroidConfig;

    // Target API level
    private Integer targetApi;

    // The max timeout when analyzing each component
    private Integer timeout;

    // The number of threads used in parallel analysis
    private Integer numThread;

    // The point-to analysis (callgraph algorithm)
    public enum PointToType{
        CONTEXT, DEFAULT
    }
    private PointToType pointToType = PointToType.DEFAULT;

    public GlobalConfig() {
        setFlowdroidConfig(new InfoflowAndroidConfiguration());
        targetApi = -1;
        timeout = 120;
        numThread = 16;
    }

    /**
     * Gets FlowDroid configuration
     * @return FlowDroid configuration
     */
    public InfoflowAndroidConfiguration getFlowdroidConfig() {
        return flowdroidConfig;
    }

    /**
     * Sets FlowDroid configuration
     * @param flowdroidConfig FlowDroid configuration object ({@link InfoflowAndroidConfiguration})
     */
    public void setFlowdroidConfig(InfoflowAndroidConfiguration flowdroidConfig) {
        this.flowdroidConfig = flowdroidConfig;
    }

    /**
     * Gets the target api level
     * @return The target api level
     */
    public Integer getTargetApi() {
        return targetApi;
    }

    /**
     * Sets the target api level
     * @param targetApi The target api level
     */
    public void setTargetApi(Integer targetApi) {
        this.targetApi = targetApi;
    }

    /**
     * Gets the max timeout analyzing each component
     * @return The max timeout to analyze each component
     */
    public Integer getTimeout() {
        return timeout;
    }

    /**
     * Sets the max timeout analyzing each component
     * @param maxTimeout The max timeout analyzing each component
     */
    public void setTimeout(Integer maxTimeout) {
        this.timeout = maxTimeout;
    }

    /**
     * Gets the number of threads used in parallel analysis
     * @return The number of threads used in parallel analysis
     */
    public Integer getNumThread() {
        return numThread;
    }

    /**
     * Sets the number of threads used in parallel analysis
     * @param numThread The number of threads used in parallel analysis
     */
    public void setNumThread(Integer numThread) {
        this.numThread = numThread;
    }

    /**
     * Sets the point-to analysis type:
     *      context-sensitive or -insensitive (default)
     * @param pointToType The point-to analysis type
     */
    public void setPointToType(PointToType pointToType) {
        this.pointToType = pointToType;
        switch(pointToType) {
            case CONTEXT:
                flowdroidConfig.setCallgraphAlgorithm(InfoflowConfiguration.CallgraphAlgorithm.GEOM);
            case DEFAULT:
                flowdroidConfig.setCallgraphAlgorithm(InfoflowConfiguration.CallgraphAlgorithm.AutomaticSelection);
        }
    }

    /**
     * Gets the type of point-to analysis
     * @return The type of point-to analysis
     */
    public PointToType getPointToType() {
        return this.pointToType;
    }
}
