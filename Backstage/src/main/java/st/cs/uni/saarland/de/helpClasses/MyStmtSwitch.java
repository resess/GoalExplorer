package st.cs.uni.saarland.de.helpClasses;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import soot.SootField;
import soot.SootMethod;
import soot.jimple.AbstractStmtSwitch;
import st.cs.uni.saarland.de.helpMethods.CheckIfMethodsExisting;
import st.cs.uni.saarland.de.helpMethods.InterprocAnalysis2;
import st.cs.uni.saarland.de.helpMethods.IterateOverUnitsHelper;
import st.cs.uni.saarland.de.helpMethods.StmtSwitch;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public abstract class MyStmtSwitch extends AbstractStmtSwitch{

	private Set<Info> resultInfos = Collections.synchronizedSet(new LinkedHashSet<>());
	protected Info resultInfo;
	protected boolean shouldBreak;
	protected StmtSwitch helpMethods = StmtSwitch.getInstance();
	protected CheckIfMethodsExisting checkMethods= CheckIfMethodsExisting.getInstance();
	protected InterprocAnalysis2 interprocMethods2 = InterprocAnalysis2.getInstance();
	protected IterateOverUnitsHelper iteratorHelper = IterateOverUnitsHelper.newInstance();
	protected final Logger logger;
	protected Set<SootField> previousFields = new CopyOnWriteArraySet<>();
	protected Set<SootField> previousFieldsForCurrentStmtSwitch = new CopyOnWriteArraySet<>();

	public String getCallerSootClass() {
		return callerSootClass;
	}

	public void setCallerSootClass(String callerSootClass) {
		this.callerSootClass = callerSootClass;
	}

	protected String callerSootClass;



	public SootMethod getCurrentSootMethod() {
		return currentSootMethod;
	}

	public void setCurrentSootMethod(SootMethod currentSootMethod) {
		this.currentSootMethod = currentSootMethod;
	}

	private SootMethod currentSootMethod;

	public void addToResultInfo(Info toAdd){
		resultInfos.add(toAdd);
	}

	public void addAllToResultInfo(Set<Info> toAdd){
		resultInfos.addAll(toAdd);
	}

	public void removeAllFromResultInfos(Set<Info> toRemove){
		resultInfos.removeAll(toRemove);
	}
	
	public Set<Info> getResultInfos(){
		return resultInfos;
	}
	
	public Set<SootField> getPreviousFields() {
		return previousFields;
	}

	public void setPreviousFields(Set<SootField> previousFields) {
		if(previousFields != null) {
			this.previousFields = previousFields;
		}

	}

	public void addPreviousField(SootField f){
		previousFields.add(f);
	}
	
	public MyStmtSwitch(SootMethod currentSootMethod) {
		shouldBreak = false;
		this.logger = LoggerFactory.getLogger(this.getClass());
		setCurrentSootMethod(currentSootMethod);
	}
	
	public MyStmtSwitch(Info info, SootMethod currentSootMethod) {
		shouldBreak = false;
		resultInfos.add(info);
		this.logger = LoggerFactory.getLogger(this.getClass());
		setCurrentSootMethod(currentSootMethod);
	}

	public boolean run(){
		if(Thread.currentThread().isInterrupted()){
			return false;
		}
		boolean allValuesFound;
		if (resultInfos.size() > 0)
			allValuesFound = resultInfos.stream().allMatch(x->x.allValuesFound());
		else
			return false;
		return !allValuesFound;
	}
	
	public void addResultInfos(Set<Info> resultsElsePart){
		resultInfos.addAll(resultsElsePart);
	}

	public boolean isShouldBreak() {
		return shouldBreak;
	}
	
	public void init(){
		shouldBreak = false;
		resultInfos = Collections.synchronizedSet(new LinkedHashSet<>());
		previousFields = new HashSet<>();
		previousFieldsForCurrentStmtSwitch = new HashSet<>();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((previousFields == null) ? 0 : previousFields.hashCode());
		result = prime * result
				+ ((resultInfo == null) ? 0 : resultInfo.hashCode());
		result = prime * result
				+ ((resultInfos == null) ? 0 : resultInfos.hashCode());
		result = prime * result + (shouldBreak ? 1231 : 1237);
//		result = prime * result + ((body == null) ? 0 : body.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MyStmtSwitch other = (MyStmtSwitch) obj;
		if (previousFields == null) {
			if (other.previousFields != null)
				return false;
		} else if (!previousFields.equals(other.previousFields))
			return false;
		if (resultInfo == null) {
			if (other.resultInfo != null)
				return false;
		} else if (!resultInfo.equals(other.resultInfo))
			return false;
		if (resultInfos == null) {
			if (other.resultInfos != null)
				return false;
		} else if (!resultInfos.equals(other.resultInfos))
			return false;
		if (shouldBreak != other.shouldBreak)
			return false;
		return true;
	}

}
