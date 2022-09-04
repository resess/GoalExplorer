package st.cs.uni.saarland.de.searchDialogs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import st.cs.uni.saarland.de.entities.Listener;
import st.cs.uni.saarland.de.helpClasses.Info;

public class DialogInfo extends Info{

	// TODO change pos, neg, neu to list, and iterate over them in analysis

	protected final Logger logger = LoggerFactory.getLogger(this.getClass());
	//Here store the method where it was found
	private String title = "";
	private String message = "";
	private String titleReg = "";
	private String messageReg = "";
	private Set<Listener> posListener = new HashSet<Listener>();
	private String posListenerReg = "";
	private String posText = "";
	private String posTextReg = "";
	private Set<Listener> negativeListener = new HashSet<Listener>();
	private String negListenerReg = "";
	private String negText = "";
	private String negTextReg = "";
	private Set<Listener> neutralListener = new HashSet<Listener>();
	private String neutralListenerReg = "";
	private Set<Listener> itemListener = new HashSet<Listener>(); // for MultipleItemListener or SingleItemListener
	private String itemListenerReg = ""; 
	private String itemTextsArrayID = "";
	private String itemTextsArrayIDReg = "";
	private String neutralText = "";
	private String neutralTextReg = "";
	private String activity = "";
	private String activityReg = "";
	private String methodSignature = "";
	private boolean finished;
	
//	.setItems(R.array.colors_array, new DialogInterface.OnClickListener() {
//	setAdapter() with ListAdapter, only for Lists
//	.setMultiChoiceItems(R.array.toppings, null, new DialogInterface.OnMultiChoiceClickListener() {
	
	
//	 If you want a custom dialog, you can instead display an Activity as a dialog instead of using the Dialog APIs. 
//	 Simply create an activity and set its theme to Theme.Holo.Dialog in the <activity> manifest element:
//		 <activity android:theme="@android:style/Theme.Holo.Dialog" >

//	DialogFragment

	public String toString(){
		return "[DialogInfo "+ title+"   +msg: "+message+"  pos: "+posText+"   neg: "+negText+"  neutral: "+neutralText+"  activity: "+activity+" ]";
	}
	
	
	public DialogInfo(String register) {
		super(register);
	}

	public DialogInfo(String register, String methodSignature){
		this(register);
		this.methodSignature = methodSignature;
	}

	public boolean isFinished(){
		return finished;
	}

	public void setFinished(){
		finished = true;
	}
	
	public String getTitle() {
		return title;
	}

	public void addTitle(String title) {
		this.title = this.title + "#" + title;
		if (this.title.startsWith("#"))
			this.title.replaceFirst("#", "");
	}

	public String getMessage() {
		return message;
	}

	public void addMessage(String message) {
		if(!StringUtils.isEmpty(message)){
			this.message = this.message + "#" + message;
			if (this.message.startsWith("#"))
				this.message.replaceFirst("#", "");
		}
	}

	public String getActivity() {
		return activity;
	}

	public void setActivity(String activity) {
		this.activity = activity;
	}

	public String getMethodSignature(){
		return this.methodSignature;
	}

	public void setMethodSignature(String methodSig){
		this.methodSignature = methodSig;
	}

	public String getPosText() {
		return posText;
	}

	public void addPosText(String posText) {
		if(!StringUtils.isEmpty(posText)){
			this.posText = this.posText + "#" + posText;
			if (this.posText.startsWith("#"))
				this.posText.replaceFirst("#", "");
		}
	}

	public String getNegText() {
		return negText;
	}

	public void addNegText(String negText) {
		if(!StringUtils.isEmpty(negText)){
			this.negText = this.negText + "#" + negText;
			if (this.negText.startsWith("#"))
				this.negText.replaceFirst("#", "");
		}
	}

	public String getNeutralText() {
		return neutralText;
	}

	public void addNeutralText(String neutralText) {
		if(!StringUtils.isEmpty(neutralText)){
			this.neutralText = this.neutralText + "#" + neutralText;
			if (this.neutralText.startsWith("#"))
				this.neutralText.replaceFirst("#", "");
		}
	}

	public String getPosListenerReg() {
		return posListenerReg;
	}

	public void setPosListenerReg(String posListenerReg) {
		this.posListenerReg = posListenerReg;
	}

	public String getNegListenerReg() {
		return negListenerReg;
	}

	public void setNegListenerReg(String negListenerReg) {
		this.negListenerReg = negListenerReg;
	}

	public String getNeutralListenerReg() {
		return neutralListenerReg;
	}

	public void setNeutralListenerReg(String neutralListenerReg) {
		this.neutralListenerReg = neutralListenerReg;
	}

	public String getActivityReg() {
		return activityReg;
	}

	public void setActivityReg(String activityReg) {
		this.activityReg = activityReg;
	}

	public String getItemListenerReg() {
		return itemListenerReg;
	}

	public void setItemListenerReg(String itemListenerReg) {
		this.itemListenerReg = itemListenerReg;
	}

	public String getItemTextsArrayID() {
		return itemTextsArrayID;
	}

	public void addItemTextsArrayID(String itemTextsArrayID) {
		this.itemTextsArrayID = this.itemTextsArrayID + "#" + itemTextsArrayID;
		if (this.itemTextsArrayID.startsWith("#"))
			this.itemTextsArrayID.replaceFirst("#", "");
	}

	public String getTitleReg() {
		return titleReg;
	}

	public void setTitleReg(String titleReg) {
		this.titleReg = titleReg;
	}

	public String getMessageReg() {
		return messageReg;
	}

	public void setMessageReg(String messageReg) {
		this.messageReg = messageReg;
	}

	public String getPosTextReg() {
		return posTextReg;
	}

	public void setPosTextReg(String posTextReg) {
		this.posTextReg = posTextReg;
	}

	public String getNegTextReg() {
		return negTextReg;
	}

	public void setNegTextReg(String negTextReg) {
		this.negTextReg = negTextReg;
	}

	public String getNeutralTextReg() {
		return neutralTextReg;
	}

	public void setNeutralTextReg(String neutralTextReg) {
		this.neutralTextReg = neutralTextReg;
	}

	public String getItemTextsArrayIDReg() {
		return itemTextsArrayIDReg;
	}

	public void setItemTextsArrayIDReg(String itemTextsArrayIDReg) {
		this.itemTextsArrayIDReg = itemTextsArrayIDReg;
	}

//	@Override
//	public boolean shouldRunOnInitMethod() {
//		if ((!posListenerReg.equals("")) || (!negListenerReg.equals("")) || (!neutralListenerReg.equals("")) || (!messageReg.equals("")) || (!searchedEReg.equals("")) || (!itemTextsArrayIDReg.equals("")) ||
//				(!negTextReg.equals("")) || (!posTextReg.equals(""))|| (!neutralTextReg.equals(""))) 
//			return true;
//		else 
//			return false;
//	}

	@Override
	public Info clone() {
		DialogInfo newInfo = new DialogInfo(searchedEReg);
		newInfo.setActivity(activity);
		newInfo.setActivityReg(activityReg);
		newInfo.addItemListener(itemListener);
		newInfo.setItemListenerReg(itemListenerReg);
		newInfo.addItemTextsArrayID(itemTextsArrayID);
		newInfo.setItemTextsArrayIDReg(itemTextsArrayIDReg);
		newInfo.addMessage(message);
		newInfo.setMessageReg(messageReg);
		newInfo.addNegativeListener(negativeListener);
		newInfo.setNegListenerReg(negListenerReg);
		newInfo.addNegText(negText);
		newInfo.setNegTextReg(negTextReg);
		newInfo.addNeutralListener(neutralListener);
		newInfo.setNeutralListenerReg(neutralListenerReg);
		newInfo.addNeutralText(neutralText);
		newInfo.setNeutralTextReg(neutralTextReg);
		newInfo.addPosListener(posListener);
		newInfo.setPosListenerReg(posListenerReg);
		newInfo.addPosText(posText);
		newInfo.setPosTextReg(posTextReg);
		newInfo.setText(text);
		newInfo.setTextReg(textReg);
		newInfo.addTitle(title);
		newInfo.setTitleReg(titleReg);
		newInfo.setMethodSignature(methodSignature);
		return newInfo;
	}

	@Override
	public boolean allValuesFound() {
		return (posListenerReg.equals("") && (negListenerReg.equals("")) && (neutralListenerReg.equals("")) && (messageReg.equals("")) && (searchedEReg.equals("")) && (itemTextsArrayIDReg.equals("")) &&
				(negTextReg.equals("")) && (posTextReg.equals(""))&& (neutralTextReg.equals("")));
	}

	public void addPosListener(Set<Listener> posListener) {
		this.posListener.addAll(posListener);
	}

	public void addNegativeListener(Set<Listener> negativeListener) {
		this.negativeListener.addAll(negativeListener);
	}

	public void addNeutralListener(Set<Listener> neutralListener) {
		this.neutralListener.addAll(neutralListener);
	}

	public void addItemListener(Set<Listener> itemListener) {
		this.itemListener.addAll(itemListener);
	}
	
	public void addPosListener(Listener posListener) {
		this.posListener.add(posListener);
	}

	public void addNegativeListener(Listener negativeListener) {
		this.negativeListener.add(negativeListener);
	}

	public void addNeutralListener(Listener neutralListener) {
		this.neutralListener.add(neutralListener);
	}

	public void addItemListener(Listener itemListener) {
		this.itemListener.add(itemListener);
	}

	public Set<Listener> getPosListener() {
		return posListener;
	}

	public Set<Listener> getNegativeListener() {
		return negativeListener;
	}

	public Set<Listener> getNeutralListener() {
		return neutralListener;
	}

	public Set<Listener> getItemListener() {
		return itemListener;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ ((activity == null) ? 0 : activity.hashCode());
		result = prime * result
				+ ((activityReg == null) ? 0 : activityReg.hashCode());
		result = prime * result + (finished ? 1231 : 1237);
		result = prime * result
				+ ((itemListener == null) ? 0 : itemListener.hashCode());
		result = prime * result
				+ ((itemListenerReg == null) ? 0 : itemListenerReg.hashCode());
		result = prime
				* result
				+ ((itemTextsArrayID == null) ? 0 : itemTextsArrayID.hashCode());
		result = prime
				* result
				+ ((itemTextsArrayIDReg == null) ? 0 : itemTextsArrayIDReg
						.hashCode());
		result = prime * result + ((message == null) ? 0 : message.hashCode());
		result = prime * result
				+ ((messageReg == null) ? 0 : messageReg.hashCode());
		result = prime * result
				+ ((negListenerReg == null) ? 0 : negListenerReg.hashCode());
		result = prime * result + ((negText == null) ? 0 : negText.hashCode());
		result = prime * result
				+ ((negTextReg == null) ? 0 : negTextReg.hashCode());
		result = prime
				* result
				+ ((negativeListener == null) ? 0 : negativeListener.hashCode());
		result = prime * result
				+ ((neutralListener == null) ? 0 : neutralListener.hashCode());
		result = prime
				* result
				+ ((neutralListenerReg == null) ? 0 : neutralListenerReg
						.hashCode());
		result = prime * result
				+ ((neutralText == null) ? 0 : neutralText.hashCode());
		result = prime * result
				+ ((neutralTextReg == null) ? 0 : neutralTextReg.hashCode());
		result = prime * result
				+ ((posListener == null) ? 0 : posListener.hashCode());
		result = prime * result
				+ ((posListenerReg == null) ? 0 : posListenerReg.hashCode());
		result = prime * result + ((posText == null) ? 0 : posText.hashCode());
		result = prime * result
				+ ((posTextReg == null) ? 0 : posTextReg.hashCode());
		result = prime * result + ((title == null) ? 0 : title.hashCode());
		result = prime * result
				+ ((titleReg == null) ? 0 : titleReg.hashCode());
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
		DialogInfo other = (DialogInfo) obj;
		if (activity == null) {
			if (other.activity != null)
				return false;
		} else if (!activity.equals(other.activity))
			return false;
		if (activityReg == null) {
			if (other.activityReg != null)
				return false;
		} else if (!activityReg.equals(other.activityReg))
			return false;
		if (finished != other.finished)
			return false;
		if (itemListener == null) {
			if (other.itemListener != null)
				return false;
		} else if (!itemListener.equals(other.itemListener))
			return false;
		if (itemListenerReg == null) {
			if (other.itemListenerReg != null)
				return false;
		} else if (!itemListenerReg.equals(other.itemListenerReg))
			return false;
		if (itemTextsArrayID == null) {
			if (other.itemTextsArrayID != null)
				return false;
		} else if (!itemTextsArrayID.equals(other.itemTextsArrayID))
			return false;
		if (itemTextsArrayIDReg == null) {
			if (other.itemTextsArrayIDReg != null)
				return false;
		} else if (!itemTextsArrayIDReg.equals(other.itemTextsArrayIDReg))
			return false;
		if (message == null) {
			if (other.message != null)
				return false;
		} else if (!message.equals(other.message))
			return false;
		if (messageReg == null) {
			if (other.messageReg != null)
				return false;
		} else if (!messageReg.equals(other.messageReg))
			return false;
		if (negListenerReg == null) {
			if (other.negListenerReg != null)
				return false;
		} else if (!negListenerReg.equals(other.negListenerReg))
			return false;
		if (negText == null) {
			if (other.negText != null)
				return false;
		} else if (!negText.equals(other.negText))
			return false;
		if (negTextReg == null) {
			if (other.negTextReg != null)
				return false;
		} else if (!negTextReg.equals(other.negTextReg))
			return false;
		if (negativeListener == null) {
			if (other.negativeListener != null)
				return false;
		} else if (!negativeListener.equals(other.negativeListener))
			return false;
		if (neutralListener == null) {
			if (other.neutralListener != null)
				return false;
		} else if (!neutralListener.equals(other.neutralListener))
			return false;
		if (neutralListenerReg == null) {
			if (other.neutralListenerReg != null)
				return false;
		} else if (!neutralListenerReg.equals(other.neutralListenerReg))
			return false;
		if (neutralText == null) {
			if (other.neutralText != null)
				return false;
		} else if (!neutralText.equals(other.neutralText))
			return false;
		if (neutralTextReg == null) {
			if (other.neutralTextReg != null)
				return false;
		} else if (!neutralTextReg.equals(other.neutralTextReg))
			return false;
		if (posListener == null) {
			if (other.posListener != null)
				return false;
		} else if (!posListener.equals(other.posListener))
			return false;
		if (posListenerReg == null) {
			if (other.posListenerReg != null)
				return false;
		} else if (!posListenerReg.equals(other.posListenerReg))
			return false;
		if (posText == null) {
			if (other.posText != null)
				return false;
		} else if (!posText.equals(other.posText))
			return false;
		if (posTextReg == null) {
			if (other.posTextReg != null)
				return false;
		} else if (!posTextReg.equals(other.posTextReg))
			return false;
		if (title == null) {
			if (other.title != null)
				return false;
		} else if (!title.equals(other.title))
			return false;
		if (titleReg == null) {
			if (other.titleReg != null)
				return false;
		} else if (!titleReg.equals(other.titleReg))
			return false;
		return true;
	}
	

}
