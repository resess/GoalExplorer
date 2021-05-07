package st.cs.uni.saarland.de.helpClasses;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soot.Body;

public class MyBodyTransformerForSpecMethods extends MyBodyTransformer {

	private String searchedMethod;
	private Logger logger;

	public MyBodyTransformerForSpecMethods(Class<? extends MyStmtSwitch> requiredClass, String searchedMethod, TimeUnit tTimeoutUnit, int tTimeoutValue) {
		super(requiredClass, tTimeoutUnit, tTimeoutValue);
		this.searchedMethod = searchedMethod;
		this.logger = LoggerFactory.getLogger(this.getClass());
	}

	@Override
	protected void internalTransform(Body body, String phase, Map options) {
		try {
			if (body.getMethod().getName().equals(searchedMethod)) {
				super.internalTransform(body, phase, options);
			}
		} catch (Exception e) {
			e.printStackTrace();
			Helper.saveToStatisticalFile(Helper.exceptionStacktraceToString(e));
		}
	}
}
