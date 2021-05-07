package st.cs.uni.saarland.de.reachabilityAnalysis;

import java.util.List;
import java.util.Map;

/**
 * Created by avdiienko on 09/05/16.
 */
public interface ResultsSaver {
    void save(Map<UiElement, List<ApiInfoForForward>> results);
}
