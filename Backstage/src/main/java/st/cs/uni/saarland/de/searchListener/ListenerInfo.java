package st.cs.uni.saarland.de.searchListener;

import st.cs.uni.saarland.de.helpClasses.Info;

import javax.naming.NameNotFoundException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/* 
 * 
 * written by Isabelle Rommelfanger, November 2014 
 * 
 */

public class ListenerInfo extends Info {
	
	private String searchedEID = "";
	private String searchedEIDReg = "";
	private Set<String> listenerMethods;
	private Set<String> listenerClasses = new HashSet<String>();
//	private Set<Listener> listeners = new HashSet<Listener>();
	private String listenerReg = "";
	private int index;
	private String whichAction = "";
	private boolean isAdapter;
	private boolean stop = false;
	private String decaringSootClass = "";

	public ListenerInfo(Set<String> callBackMethod, String buttonRegister, String listenerRegister,String whichActionPerformed, String decaringSootClass){
		super(buttonRegister);
		listenerMethods = new HashSet<String>();
		if(callBackMethod != null){
			listenerMethods.addAll(callBackMethod);
		}
		listenerReg = listenerRegister;
//		index = pindex;
		whichAction = whichActionPerformed;
		isAdapter = false;
		this.decaringSootClass = decaringSootClass;
	}
	
	@Deprecated
	public ListenerInfo(String register, int pindex, String idOfElement){
		super(register);
		this.searchedEID = idOfElement;
		
		searchedEReg = register;
		index = pindex;
	}
	
//	@Override
//	public boolean shouldRunOnInitMethod(){		
//		if ((!listenerReg.equals("")) && (!searchedEIDReg.equals(""))){
//			return true;
//		}else
//			return false;
//	}
	
//	@Deprecated
//	public boolean ready(){
//		return (stop || (listenerMethods.size() > 0) && (!listenerClass.equals("")) && (!searchedEID.equals("")) && (!whichAction.equals("")));
//	}
	
//	public Set<Listener> getListenersOfElementsAndLayout() {
//		return listeners;
//	}
//
//	public void addListeners(Set<Listener> listeners) {
//		this.listeners.addAll(listeners);
//	}
//	
//	public void addListeners(Listener listener) {
//		this.listeners.add(listener);
//	}

//	@Deprecated
//	public boolean allValueWereFoundOrStopAnalysis(){
//		return  (stop || (listenerMethods.size() > 0) && (!listenerClass.equals("")) && (!searchedEID.equals("")) && (!whichAction.equals("")) );
//	}

	public String getWhichAction() {
		return whichAction;
	}

	public void setWhichActionPerformed(String action){
		this.whichAction = action;
	}
	

	public void setSearchedEID(String id){
		this.searchedEID = id;
	}
	
//	public boolean removeReg(String register){
//		if (this.reg.contains(register))
//			return reg.remove(register);
//		return false;
//	}

	public Set<String> getListenerMethods() {
		return listenerMethods;
	}

	public String getDecaringSootClass(){
		return decaringSootClass;
	}

	public void setDecaringSootClass(String sootClass){
		this.decaringSootClass = sootClass;
	}

	public void addListenerMethod(String pmethod) {
		listenerMethods.add(pmethod);
	}

	public Set<String> getListenerClasses() {
		return listenerClasses;
	}
	
	public void setListenerReg(String reg) {
		this.listenerReg = reg;
	}

	public String getListenerReg() {
		return listenerReg;
	}

	public void addListenerClass(String pListenerClass) {
		this.listenerClasses.add(pListenerClass);
	}
	
	public void addListenerClass(Set<String> pListenerClass) {
		this.listenerClasses.addAll(pListenerClass);
	}
	
	public String getSearchedEID(){
		return searchedEID;
	}
	
	@Override
	public String toString(){
		String res ="";
		for(String s : listenerMethods){
			res = res + " " + s; 
		}
		String listC  ="";
		for (String listClass: listenerClasses){
			listC = listC + listClass;
		}
		
		String a =  ("ButId: " + searchedEID + " onClickClass " + listC + " onClickMethod " + res + " ButtonReg: " + searchedEReg + " ListenerReg: "
				+ listenerReg + "DeclaringSootClass: "+decaringSootClass);
		
		return a;
	}

	public static Set<String> getIds(List<ListenerInfo> quad){
		if ((quad != null) && (quad.size() > 0)){
			Set<String> ret = new HashSet<String>();
			for (ListenerInfo q: quad){
				ret.add(q.getSearchedEID());
			}
		return ret;
		}else
			throw new NullPointerException();
	}
	
	public static ListenerInfo getEntryById (List<ListenerInfo> quad, String id) throws NameNotFoundException{
		for (ListenerInfo q : quad){
			if (q.getSearchedEID() == (id)){
				return q;
			}
		}
		throw new NameNotFoundException();
	}
	
	// returns ALL Quadripel with this reg
	public static List<ListenerInfo> getEntryByReg (List<ListenerInfo> quad, String reg){
		ArrayList<ListenerInfo> res = new ArrayList<ListenerInfo>();
		for (ListenerInfo q : quad){
			if ((q.getSearchedEReg() != null) && (q.getSearchedEReg().contains(reg))){
				res.add(q);
			}
		}
		return res;
	}

	public int getIndex() {
		return index;
	}


	public boolean isAdapter() {
		return isAdapter;
	}


	public void setIsAdapter() {
		this.isAdapter = true;
	}


	public String getSearchedEIDReg() {
		return searchedEIDReg;
	}


	public void setSearchedEIDReg(String searchedEIDReg) {
		this.searchedEIDReg = searchedEIDReg;
	}


	public void setStopSignal() {
		stop = true;		
	}

//	Set<String> callBackMethod, String buttonRegister, String listenerRegister,String whichActionPerformed
	@Override
	public Info clone() {
		ListenerInfo newInfo = new ListenerInfo(listenerMethods, searchedEReg, listenerReg, whichAction, decaringSootClass);
		if (isAdapter)
			newInfo.setIsAdapter();
		newInfo.addListenerClass(listenerClasses);
		newInfo.setSearchedEID(searchedEID);
		newInfo.setSearchedEIDReg(searchedEIDReg);
		if (stop)
			newInfo.setStopSignal();
		return newInfo;
	}


	@Override
	public boolean allValuesFound() {
		return (listenerMethods.size() > 0)  && (!searchedEID.equals("")) && (!whichAction.equals("") && listenerReg.equals("") && searchedEIDReg.equals(""));
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + (isAdapter ? 1231 : 1237);
		result = prime * result
				+ ((listenerClasses == null) ? 0 : listenerClasses.hashCode());
		result = prime * result
				+ ((listenerMethods == null) ? 0 : listenerMethods.hashCode());
		result = prime * result
				+ ((listenerReg == null) ? 0 : listenerReg.hashCode());
		result = prime * result
				+ ((searchedEID == null) ? 0 : searchedEID.hashCode());
		result = prime * result
				+ ((searchedEIDReg == null) ? 0 : searchedEIDReg.hashCode());
		result = prime * result + (stop ? 1231 : 1237);
		result = prime * result
				+ ((whichAction == null) ? 0 : whichAction.hashCode());
		result = prime * result + decaringSootClass.hashCode();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		ListenerInfo other = (ListenerInfo) obj;
		if (isAdapter != other.isAdapter)
			return false;
		if (listenerClasses == null) {
			if (other.listenerClasses != null)
				return false;
		} else if (!listenerClasses.equals(other.listenerClasses))
			return false;
		if (listenerMethods == null) {
			if (other.listenerMethods != null)
				return false;
		} else if (!listenerMethods.equals(other.listenerMethods))
			return false;
		if (listenerReg == null) {
			if (other.listenerReg != null)
				return false;
		} else if (!listenerReg.equals(other.listenerReg))
			return false;
		if (searchedEID == null) {
			if (other.searchedEID != null)
				return false;
		} else if (!searchedEID.equals(other.searchedEID))
			return false;
		if (searchedEIDReg == null) {
			if (other.searchedEIDReg != null)
				return false;
		} else if (!searchedEIDReg.equals(other.searchedEIDReg))
			return false;
		if (stop != other.stop)
			return false;
		if (whichAction == null) {
			if (other.whichAction != null)
				return false;
		} else if (!whichAction.equals(other.whichAction))
			return false;
		if(!decaringSootClass.equals(other.decaringSootClass)){
			return false;
		}
		return true;
	}
	
}
