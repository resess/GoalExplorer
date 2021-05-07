package st.cs.uni.saarland.de.testApps;

import org.junit.Test;
import st.cs.uni.saarland.de.saveData.MSRApiRepresentation;
import st.cs.uni.saarland.de.saveData.MSRResultsRepresentation;

import static org.junit.Assert.assertEquals;

/**
 * Created by avdiienko on 23/12/15.
 */
public class MSRResultsRepresentationTest {
    @Test
    public void compareMSRResultsApiRepresenation(){
        MSRApiRepresentation obj1 = new MSRApiRepresentation();
        obj1.nonUiCallbacks = 10;
        obj1.uiCallbacks = 70;

        MSRApiRepresentation obj2 = new MSRApiRepresentation();
        obj2.nonUiCallbacks = 10;
        obj2.uiCallbacks = 70;

        assertEquals(obj1, obj2);
    }

    @Test
    public void compareMSRResultsRepresenation(){
        MSRResultsRepresentation r1 = new MSRResultsRepresentation();
        MSRResultsRepresentation r2 = new MSRResultsRepresentation();
        MSRApiRepresentation obj1 = new MSRApiRepresentation();
        obj1.nonUiCallbacks = 10;
        obj1.uiCallbacks = 70;

        MSRApiRepresentation obj2 = new MSRApiRepresentation();
        obj2.nonUiCallbacks = 10;
        obj2.uiCallbacks = 70;

        r1.benignApps.nonUiCallbacks = obj1.nonUiCallbacks;
        r2.benignApps.nonUiCallbacks = obj2.nonUiCallbacks;

        r1.benignApps.uiCallbacks = obj1.uiCallbacks;
        r2.benignApps.uiCallbacks = obj2.uiCallbacks;

        r1.malciousApps.nonUiCallbacks = obj2.nonUiCallbacks;
        r2.malciousApps.nonUiCallbacks = obj2.nonUiCallbacks;

        r1.malciousApps.uiCallbacks = obj2.uiCallbacks;
        r2.malciousApps.uiCallbacks = obj2.uiCallbacks;

        assertEquals(r1, r2);
    }
}
