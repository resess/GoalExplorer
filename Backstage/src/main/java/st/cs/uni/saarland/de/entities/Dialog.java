package st.cs.uni.saarland.de.entities;

import java.util.*;

import android.content.DialogInterface;

public class Dialog extends XMLLayoutFile {

	private String showingClass; // activity (Content) where the Dialog is shown // TODO rename to showingActivity
	private String posText = ""; // text that is shown for a positive answer
	private String negText = ""; // text that is shown for a negative answer
	private String neutralText = ""; // text that is shown for a neutral answer
	private String itemTexts; // text of items -> case it is a item Dialog
	private String message = ""; // text that is shown as message or question at the top of the Dialog
	protected String titleText = ""; // title text of the Dialog
	protected String kindOfUiElement = ""; // always is Dialog
	private String dialogCreationMethodSignature = "";
	private Set<Listener> negativeListener = new HashSet<Listener>(); // set of listeners for a negative answer
	private Set<Listener> posListener = new HashSet<Listener>(); // set of listeners for a positive answer
	private Set<Listener> neutralListener = new HashSet<Listener>(); // set of listeners for a neutral answer
	private Set<Listener> itemListener = new HashSet<Listener>(); // set of listeners for MultipleItemListener or SingleItemListener

	// constructor
	public Dialog(int pid, String titleText, String classWhereItIsShown, String posText, String negText, String message) {
		super();
		id = pid;
		this.kindOfUiElement = "Dialog";
		this.titleText = titleText;
		this.showingClass = classWhereItIsShown;
		this.posText = posText;
		this.negText = negText;
		this.message = message;
	}

	// returns a list of listeners that are connected to the whole layout (see Menus), no included layouts!
	@Override
	public Collection<Listener> getLayoutListeners(){
		List<Listener> list = new ArrayList<Listener>();
		if (negativeListener != null)
			list.addAll(negativeListener);
		if (posListener != null)
			list.addAll(posListener);
		if (neutralListener != null)
			list.addAll(neutralListener);
		if (itemListener != null)
			list.addAll(itemListener);
		return list;
	}

	public String getTitleText() {
		return titleText;
	}

	@Override
	public boolean hasLayoutListeners(){
		if (negativeListener.size() > 0)
			return true;
		if (posListener.size() > 0)
			return true;
		if (neutralListener.size() > 0)
			return true;
		if (itemListener.size() > 0)
			return true;
		return false;
	}

	public String getShowingClass() {
		return showingClass;
	}

	public String getPosText() {
		return posText;
	}

	public String getNegText() {
		return negText;
	}

	public String getMessage() {
		return message;
	}


	@Override
	// returns a string with all assigned text to this dialog, text is joint via "#"
	public Map<String, String> getTextFromLayoutWithoutIncluded(Map<Integer, AppsUIElement> uiElements) {
		Set<String> alltext = new HashSet<>();
		alltext.add(titleText);
		alltext.add(posText);
		alltext.add(negText);
		alltext.add(neutralText);
		alltext.add(message);
		alltext.add(itemTexts);
		alltext.remove("");
		Map<String, String> retValue = new HashMap<>();
		retValue.put("default_text", String.join("#", alltext));
		return retValue;
	}

	// returns a string with the text that is not connected with the activeListener(param)
	// eg if the pos text, pos listener, msg, and negText is set, and the activeListener is the pos listener
	//  then this method would return the msg and the negText
	// TODO rename elmeentID to type
	// TODO rewrite or deprecated after refacoring
	public String getInactiveTextBasedOnActiveListener(Listener activeListener, int elementId){

		// TODO is this method working correct? inactivetextBasedOnActiveListener? eg like above or is it to get
		// the inactive text of an active listener
		if(posListener != null && elementId == DialogInterface.BUTTON_POSITIVE){
			// check if any positive listener is equal to the searched active listener
			if(posListener.stream().anyMatch(x->x.equals(activeListener))){
				Set<String> alltext = new HashSet<String>();
				alltext.add(titleText);
				alltext.add(posText);
				alltext.add(message);
				alltext.remove("");
				if(alltext.size() > 0){
					return String.join("#", alltext);
				}
			}
		}
		if(negativeListener != null && elementId == DialogInterface.BUTTON_NEGATIVE){
			if(negativeListener.stream().anyMatch(x->x.equals(activeListener))){
				Set<String> alltext = new HashSet<String>();
				alltext.add(titleText);
				alltext.add(negText);
				alltext.add(message);
				alltext.remove("");
				if(alltext.size() > 0){
					return String.join("#", alltext);
				}
			}
		}
		if(neutralListener != null  && elementId == DialogInterface.BUTTON_NEUTRAL){
			if(neutralListener.stream().anyMatch(x->x.equals(activeListener))){
				Set<String> alltext = new HashSet<String>();
				alltext.add(titleText);
				alltext.add(neutralText);
				alltext.add(message);
				alltext.remove("");
				if(alltext.size() > 0){
					return String.join("#", alltext);
				}
			}
		}
		if(itemListener != null){
			if(itemListener.stream().anyMatch(x->x.equals(activeListener))){
				Set<String> alltext = new HashSet<String>();
				alltext.add(titleText);
				alltext.add(itemTexts);
				alltext.add(message);
				alltext.remove("");
				if(alltext.size() > 0){
					return String.join("#", alltext);
				}
			}
		}
		return "";
	}

	public String getNeutralText() {
		return neutralText;
	}

	public void setNeutralText(String neutralText) {
		this.neutralText = neutralText;
	}

	@Override
	public String toString() {
		// System.out.println("inside ToStirng Dialog!");
		String res = super.toString();
		// System.out.println("inside ToStirng Dialog!2");
		res = res + " ;activity: " + showingClass + " ;posText: " + posText + " ;negText: " + negText
				+ " ; neutralText;: " + neutralText + " ; itemTexts: " + itemTexts + " ;message: " + message + " ;created in: "+dialogCreationMethodSignature;
		if (negativeListener != null)
			res = res + negativeListener.toString();
		if (posListener != null)
			res = res + posListener.toString();
		if (neutralListener != null)
			res = res + neutralListener.toString();
		if (itemListener != null)
			res = res + itemListener.toString();

		return res;
	}

	public boolean isConnectedToAnAPI(){
		for (Listener l: getLayoutListeners()){
			if (l.hasAPICalls())
				return true;
		}
		return false;
	}

	public Set<Listener> getNegativeListener() {
		return negativeListener;
	}

	public void setNegativeListener(Set<Listener> negativeListener) {
		this.negativeListener = negativeListener;
	}

	public Set<Listener> getPosListener() {
		return posListener;
	}

	public void setPosListener(Set<Listener> posListener) {
		this.posListener = posListener;
	}

	public Set<Listener> getNeutralListener() {
		return neutralListener;
	}

	public void setNeutralListener(Set<Listener> neutralListener) {
		this.neutralListener = neutralListener;
	}

	public Set<Listener> getItemListener() {
		return itemListener;
	}

	public void setItemListener(Set<Listener> itemListener) {
		this.itemListener = itemListener;
	}

	public String getItemTexts() {
		return itemTexts;
	}

	public void setItemTexts(String itemTexts) {
		this.itemTexts = itemTexts;
	}

	public String getKindOfElement(){
		return kindOfUiElement;
	}

	public String getDialogCreationMethodSignature(){
		return this.dialogCreationMethodSignature;
	}

	public void setDialogCreationMethodSignature(String signature){
		this.dialogCreationMethodSignature = signature;
	}


}
