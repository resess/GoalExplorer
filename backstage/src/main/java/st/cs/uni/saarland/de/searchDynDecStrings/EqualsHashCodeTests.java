package st.cs.uni.saarland.de.searchDynDecStrings;

import static org.junit.Assert.assertEquals;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import st.cs.uni.saarland.de.helpClasses.Info;


public class EqualsHashCodeTests {

	@Test
	public void testDynDecString(){
		DynDecStringInfo info1 = new DynDecStringInfo("$r4", null);
		info1.addText("bla1");
		
		DynDecStringInfo info2 = new DynDecStringInfo("$r4", null);
		info2.addText("bla1");
		
		assertEquals(info1,info2);
		
		DynDecStringInfo info3 = new DynDecStringInfo("$r4", null);
		info3.addText("13213213");
		
		DynDecStringInfo info4 = new DynDecStringInfo("$r4", null);
		info4.addText("13213213");
		
		assertEquals(info3,info4);
		
		Set<Info> infoSet = new HashSet<Info>();
		
		infoSet.add(info1);
		infoSet.add(info2);
		infoSet.add(info3);
		infoSet.add(info4);
		
		assertEquals(2, infoSet.size());
	}
}
