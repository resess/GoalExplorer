package st.cs.uni.saarland.de.searchDynDecStrings;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soot.SootMethod;
import st.cs.uni.saarland.de.helpClasses.Info;
import st.cs.uni.saarland.de.helpMethods.StmtSwitchForArrayAdapter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DynDecStringInfo extends Info{
	protected String uiEID = "";
	protected String uiEIDReg = "";
//	protected String adapterReg;
	protected final Logger logger =  LoggerFactory.getLogger(Thread.currentThread().getName());
	private StmtSwitchForArrayAdapter arraySwitch;
	private StmtSwitchForStringBuilder stringBuilderSwitch;
	private boolean processedStmtInStringBuilder;
	// TODO extend StringBuilders to more than 1 at a time
	private List<String> notJoinedText;
	private Set<String> searchedPlaceHolders;
	private String declaredSootClass;
	
	public DynDecStringInfo(String uiElementReg, SootMethod currentSootMethod){
		super(uiElementReg);
		stringBuilderSwitch = new StmtSwitchForStringBuilder(currentSootMethod);
		notJoinedText = new ArrayList<String>();
		declaredSootClass = currentSootMethod.getDeclaringClass().getName();
	}

	public boolean isProcessedStmtInStringBuilder() {
		return processedStmtInStringBuilder;
	}

	public void setProcessedStmtInStringBuilder(boolean processedStmtInStringBuilder) {
		this.processedStmtInStringBuilder = processedStmtInStringBuilder;
	}

	public void setOldStringBuilderSet(List<String>  textSet, Set<String> placeHolders){
		notJoinedText = textSet;
		searchedPlaceHolders = placeHolders;
	}

	public Set<String> getSearchedPlaceHolders() {
		return searchedPlaceHolders;
	}

	public void addSearchedPlaceHolders(String searchedPlaceHolder) {
		if (!StringUtils.isBlank(searchedPlaceHolder))
			this.searchedPlaceHolders.add(searchedPlaceHolder);
	}

	public void removeSearchedPlaceHolders(String searchedPlaceHolder) {
		this.searchedPlaceHolders.remove(searchedPlaceHolder);
	}

	public String joinNotJoinedText() {
		if (notJoinedText.size() > 0) {
			String res = String.join("#", notJoinedText);
			notJoinedText = new ArrayList<String>();
			searchedPlaceHolders = new HashSet<String>();
			return res;
		}else
			return "";
	}

	// returns if the place holder was replaced
	public boolean replacePlaceHolder(String placeHolder, String text) {
		boolean res = false;
		for (int i = 0; i < notJoinedText.size(); i++){
			if (notJoinedText.get(i).equals(placeHolder)){
				// don't break the loop: it could be that the same variable is added more than one time
				notJoinedText.remove(i);
				notJoinedText.add(i, text);
				res = true;
			}
		}
		return res;
	}

	//	public boolean ready(){
//		if ((!text.equals(""))  && !uiEID.equals(""))
////		if (!uiEID.equals(""))
//			return true;
//		else
//			return false;
//	}
	
//	public void setIsStringArray(){
//		isStringArray = true;
//	}
//	
//	public boolean isStringArray(){
//		return this.isStringArray;
//	}
//	
//	public void setIdOfString(String id){
//		idOfString = id;
//	}
	
//	public String getAdapterReg() {
//		return adapterReg;
//	}
//
//	public void setAdapterReg(String reg) {
//		this.adapterReg = reg;
//	}

	public String getUiEID() {
		return uiEID;
	}

	public void setUiEID(String uiEID) {
		this.uiEID = uiEID;
	}
	
	public String getUiEIDReg() {
		return uiEIDReg;
	}

	public void setUiEIDReg(String uiEID) {
		this.uiEIDReg = uiEID;
	}

	public StmtSwitchForArrayAdapter getArraySwitch() {
		return arraySwitch;
	}

	public void setArraySwitch(StmtSwitchForArrayAdapter arraySwitch) {
		this.arraySwitch = arraySwitch;
	}

	public StmtSwitchForStringBuilder getStringBuilderSwitch() {
		return stringBuilderSwitch;
	}

	public void setStringBuilderSwitch(
			StmtSwitchForStringBuilder stringBuilderSwitch) {
		this.stringBuilderSwitch = stringBuilderSwitch;
	}

	public void setDeclaringSootClass(String sootClass){
		this.declaredSootClass = sootClass;
	}

	public String getDeclaredSootClass(){
		return this.declaredSootClass;
	}

//	@Override
//	public String toString(){
//		String res = super.toString();
//		return res + " uiEID: " + uiEID   ;
//	}

	
	
//	@Override
//	public boolean shouldRunOnInitMethod() {
//		if ((!uiEIDReg.equals("")) && (!searchedEReg.equals("")))
//			return true;
//		else
//			return false;
//	}

//	@Override
//	public boolean equals(Object o){
//		if (o instanceof DynDecStringInfo){
//			DynDecStringInfo oi = (DynDecStringInfo) o;
//			if (oi.getSearchedEReg().equals(searchedEReg) && oi.getTextFromElement().equals(text) && oi.getTextReg().equals(textReg) && oi.getUiEID().equals(uiEID) && oi.getUiEIDReg().equals(uiEIDReg)){
//				return true;
//			}
//		}
//		return false;
//	}
	
	@Override
	public Info clone() {
		DynDecStringInfo newInfo = new DynDecStringInfo(searchedEReg, stringBuilderSwitch.getCurrentSootMethod());
		newInfo.setText(text);
		newInfo.setTextReg(textReg);
		newInfo.setUiEID(uiEID);
		newInfo.setUiEIDReg(uiEIDReg);
		newInfo.setDeclaringSootClass(declaredSootClass);
		if (arraySwitch != null)
			newInfo.setArraySwitch(arraySwitch);
		newInfo.setStringBuilderSwitch((StmtSwitchForStringBuilder)(stringBuilderSwitch.clone()));
		return newInfo;
	}

	@Override
	public String toString() {
		return "DynDecStringInfo [uiEID=" + uiEID + ", uiEIDReg=" + uiEIDReg
				+ ", arraySwitch=" + arraySwitch
				+ ", stringBuilderSwitch=" + stringBuilderSwitch
				+ ", searchedEReg=" + searchedEReg + ", text=" + text
				+ ", textReg=" + textReg
				+ ", declaringSootClass=" + declaredSootClass +"];";
	}

	@Override
	public boolean allValuesFound() {
		boolean allValuesFoundFromArraySwitch = true;
		if (arraySwitch != null)
			arraySwitch.allValuesFound();
		return (textReg.equals("") && uiEIDReg.equals("") && uiEIDReg.equals("") && searchedEReg.equals("") && allValuesFoundFromArraySwitch && stringBuilderSwitch.allValuesFound());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ ((arraySwitch == null) ? 0 : arraySwitch.hashCode());
		result = prime
				* result
				+ ((stringBuilderSwitch == null) ? 0 : stringBuilderSwitch
						.hashCode());
		result = prime * result + ((uiEID == null) ? 0 : uiEID.hashCode());
		result = prime * result
				+ ((uiEIDReg == null) ? 0 : uiEIDReg.hashCode());
		result = prime * result + declaredSootClass.hashCode();
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
		DynDecStringInfo other = (DynDecStringInfo) obj;
		if (arraySwitch == null) {
			if (other.arraySwitch != null)
				return false;
		} else if (!arraySwitch.equals(other.arraySwitch))
			return false;
		if (stringBuilderSwitch == null) {
			if (other.stringBuilderSwitch != null)
				return false;
		} else if (!stringBuilderSwitch.equals(other.stringBuilderSwitch))
			return false;
		if (uiEID == null) {
			if (other.uiEID != null)
				return false;
		} else if (!uiEID.equals(other.uiEID))
			return false;
		if (uiEIDReg == null) {
			if (other.uiEIDReg != null)
				return false;
		} else if (!uiEIDReg.equals(other.uiEIDReg))
			return false;
		if(!declaredSootClass.equals(other.declaredSootClass)){
			return false;
		}
		return true;
	}
	
}
