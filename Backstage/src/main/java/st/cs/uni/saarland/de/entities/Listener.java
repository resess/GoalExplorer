package st.cs.uni.saarland.de.entities;

/* 
 * 
 * written by Isabelle Rommelfanger, November 2014 
 * 
 */


import java.util.HashSet;
import java.util.Set;

public class Listener {
	String declaringClass;
	String listenerMethod;
	String listenerClass;
	String actionWaitingFor; // eg onClick, onHover, onKey,...
	boolean xmlDefined;
	Set<String> calledAPISignatures = new HashSet<String>();
	
	public Listener(String whichAction, boolean xmlDefined, String methodName, String declaringClass){
		actionWaitingFor = whichAction;
		this.xmlDefined = xmlDefined;
		this.listenerMethod = methodName;
		this.declaringClass = declaringClass;
	}

	public String getDeclaringClass(){
		return declaringClass;
	}
	
	public boolean isXMLDefined(){
		return xmlDefined;
	}
		
	public String getListenerMethod() {
		return listenerMethod;
	}

	public boolean hasAPICalls(){
		return calledAPISignatures.size()>0 ? true: false;
	}
	
	public void setListenerMethod(String methodSignature){
		if (xmlDefined)
			this.listenerMethod = methodSignature;
	}
	public String getListenerClass() {
		return listenerClass;
	}
	public void setListenerClass(String onClickClass) {
		this.listenerClass = onClickClass;
	}
	
	public String getActionWaitingFor() {
		return actionWaitingFor;
	}

	public void setActionWaitingFor(String actionWaitingFor) {
		this.actionWaitingFor = actionWaitingFor;
	}
	
	public boolean isListenerClass(){
		if (listenerClass != null)
			return true;
		else
			return false;
	}
	
	public boolean isListenerMethod(){
		if (listenerMethod != null)
			return true;
		else
			return false;
	}
	
	public boolean isCompleted(){
		return isListenerClass() && isListenerMethod();
	}
	
	@Override
	public boolean equals(Object o){
		if (o instanceof Listener){
			if (((Listener) o).getListenerClass().trim().equals(this.listenerClass.trim()) && 
					(((Listener) o).getListenerMethod().trim().equals(this.listenerMethod.trim())))
				return true;
			else 
				return false;
		}else
			return false;
	}

	@Override
	public int hashCode(){
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((this.listenerClass == null) ? 0 : this.listenerClass.hashCode());
		result = prime * result + ((this.listenerMethod == null) ? 0 : this.listenerMethod.hashCode());
		return result;
	}
	
	@Override
	public String toString(){
		String res =  " ; listenerClass: " + listenerClass + "; listenerMethod " + listenerMethod;
		return res;
	}

	@Deprecated
	// returns string with listener class and method
	public String attributesForSavingToString(){
		String res =  listenerClass + "---" + listenerMethod;
		return res;
	}
	
	public String getSignature(){
		return "<" + listenerClass + ": " + listenerMethod + ">";
	}

	public Set<String> getCalledAPISignatures() {
		return calledAPISignatures;
	}

	public void addCalledAPISignatures(String calledAPISignature) {
		this.calledAPISignatures.add(calledAPISignature);
	}
}
