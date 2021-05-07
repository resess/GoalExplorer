package st.cs.uni.saarland.de.helpClasses;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import soot.Body;
import soot.BodyTransformer;
import soot.SootClass;
import soot.SootMethod;
import st.cs.uni.saarland.de.dissolveSpecXMLTags.TabViewInfo;
import st.cs.uni.saarland.de.helpMethods.IterateOverUnitsHelper;
import st.cs.uni.saarland.de.searchScreens.LayoutInfo;
import st.cs.uni.saarland.de.searchScreens.StmtSwitchForLayoutInflater;

import java.util.*;
import java.util.concurrent.*;

public class MyBodyTransformerBackwards extends BodyTransformer{
	protected final Logger logger;

	private final TimeUnit tTimeoutUnit;
	private final int tTimeoutValue;
	
	protected Set<TabViewInfo> resultTabs = Collections.synchronizedSet(new LinkedHashSet<>());
	protected Map<Integer, LayoutInfo> resultLayouts = new ConcurrentHashMap<>();

	private final Class<? extends MyStmtSwitchForResultLists> switchClass;

	public Set<TabViewInfo> getResultTabInfos(){
		return Collections.synchronizedSet(new LinkedHashSet<>(resultTabs));
	}
	
	public Map<Integer, LayoutInfo> getResultLayoutInfos(){
		return new ConcurrentHashMap<>(resultLayouts);
	}
	
	public MyBodyTransformerBackwards(Class<? extends MyStmtSwitchForResultLists> requiredClass, TimeUnit tTimeoutUnit, int tTimeoutValue) {
		super();
		this.logger = LoggerFactory.getLogger(this.getClass());
		this.tTimeoutUnit = tTimeoutUnit;
		this.tTimeoutValue = tTimeoutValue;
		switchClass =  requiredClass;
	}
	
	
	@Override
	protected void internalTransform(Body body, String phase, Map options) {
		if(!Helper.processMethod(body.getUnits().size()) || !body.getMethod().getDeclaringClass().getName().startsWith(Helper.getPackageName())){
			return;
		}

        if (body.getMethod().getDeclaringClass().getName().equalsIgnoreCase("dummyMainClass")) {
            return;
        }

		try {
            final MyStmtSwitchForResultLists stmtSwitch = this.switchClass.getConstructor(SootMethod.class).newInstance((SootMethod) null);

            stmtSwitch.init();
            stmtSwitch.setCurrentSootMethod(body.getMethod());

			ThreadPoolExecutor executor = new ThreadPoolExecutor(1, 1,
					0L, TimeUnit.MILLISECONDS,
					new LinkedBlockingQueue<>());

			Future<Void> fTask = executor.submit(
					(Callable<Void>) () -> {
						try {
							IterateOverUnitsHelper.newInstance().runUnitsOverMethodBackwards(body, stmtSwitch);
							return null;
						} catch (Exception e) {
							Helper.saveToStatisticalFile(Helper.exceptionStacktraceToString(e));
							e.printStackTrace();
							return null;
						}
					}
			);

			try {
				fTask.get(this.tTimeoutValue, this.tTimeoutUnit);
			} catch (java.util.concurrent.TimeoutException e) {
				fTask.cancel(true);
				logger.error("Timeout for transformation occurred: " + phase + " method: "+ body.getMethod().getSignature() );
			} catch (Exception e) {
				e.printStackTrace();
			}

			executor.shutdownNow();
			try {
				executor.awaitTermination(30, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				logger.error("Executor did not terminate correctly: " + phase+ " method: "+ body.getMethod().getSignature());
			}
			finally {
				while (!executor.isTerminated()) {
					executor.purge();
					Thread.sleep(3000);
					logger.error("Cannot terminate in phase: "+ phase+ " in method: "+body.getMethod().getSignature()+
							" with LOC: "+body.getUnits().size());
				}
			}


			Set<TabViewInfo> resT = stmtSwitch.getResultedTabs();
			Map<Integer, LayoutInfo> resL = stmtSwitch.getResultLayoutInfos();

			if (resT != null)
				resultTabs.addAll(resT);
			if (resL != null)
				resultLayouts.putAll(resL);

		} catch (Exception e) {
			e.printStackTrace();
			Helper.saveToStatisticalFile(Helper.exceptionStacktraceToString(e));
		}
	}

}
