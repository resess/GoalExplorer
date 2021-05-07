package st.cs.uni.saarland.de.helpClasses;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import st.cs.uni.saarland.de.helpMethods.IterateOverUnitsHelper;
import soot.Body;
import soot.BodyTransformer;
import soot.SootMethod;


public class MyBodyTransformer  extends BodyTransformer {

	protected Set<Info> resultList;
	private final Logger logger;
	private final TimeUnit tTimeoutUnit;
	private final int tTimeoutValue;
	private final Class<? extends MyStmtSwitch> switchClass;
	
	
	public MyBodyTransformer(Class<? extends MyStmtSwitch> requiredClass, TimeUnit tTimeoutUnit, int tTimeoutValue) {
		super();
		this.logger = LoggerFactory.getLogger(this.getClass());
		this.tTimeoutUnit = tTimeoutUnit;
		this.tTimeoutValue = tTimeoutValue;
		resultList= Collections.synchronizedSet(new LinkedHashSet<>());
		switchClass = requiredClass;
	}
	
	
	@Override
	protected void internalTransform(final Body body, String phase, Map options) {
		if(!Helper.processMethod(body.getUnits().size()) || !body.getMethod().getDeclaringClass().getName().startsWith(Helper.getPackageName())){
			return;
		}

		try {

			final MyStmtSwitch stmtSwitch = this.switchClass.getConstructor(SootMethod.class).newInstance((SootMethod) null);
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
				logger.error("Timeout for transformation occurred: " + phase+ " method: "+ body.getMethod().getSignature());
				if (!Helper.timeoutedPhasesInUIAnalysis.containsKey(phase)) {
					Helper.timeoutedPhasesInUIAnalysis.put(phase, new AtomicInteger(0));
				}
				Helper.timeoutedPhasesInUIAnalysis.get(phase).incrementAndGet();
			} catch (Exception e) {
				e.printStackTrace();
			}

			executor.shutdownNow();
			try {
				executor.awaitTermination(30, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				logger.error("Executor did not terminate correctly: " + phase+ " method: "+ body.getMethod().getSignature());
			} finally {
				while (!executor.isTerminated()) {
					executor.purge();
					Thread.sleep(3000);
					logger.error("Cannot terminate in phase: "+ phase+ " in method: "+body.getMethod().getSignature()+
							" with LOC: "+body.getUnits().size());
				}
			}


			Set<Info> res = stmtSwitch.getResultInfos();
			resultList.addAll(res);

		} catch (Exception e) {
			e.printStackTrace();
			Helper.saveToStatisticalFile(Helper.exceptionStacktraceToString(e));
		}
	}
	
	
	public Set<Info> getResultInfos(){
		return Collections.synchronizedSet(new LinkedHashSet<>(resultList));
	}
}
