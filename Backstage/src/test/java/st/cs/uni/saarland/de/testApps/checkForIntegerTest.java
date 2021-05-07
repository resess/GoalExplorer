package st.cs.uni.saarland.de.testApps;

import org.junit.Test;
import st.cs.uni.saarland.de.helpMethods.CheckIfMethodsExisting;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertNotNull;

/**
 * Created by Isa on 15.02.2016.
 */
public class checkForIntegerTest {

    @Test
    public void check1(){
        String id = "2130968636";
        String className = "com.google.android.gms.ads.internal.formats.b";
        boolean t1 = CheckIfMethodsExisting.getInstance().checkIfValueIsID(id);
        boolean t2 = CheckIfMethodsExisting.getInstance().checkIfValueIsID(className);
        System.out.println(t1);
        System.out.println(t2);
        assertTrue(t1);
        assertFalse(t2);

    }
}
