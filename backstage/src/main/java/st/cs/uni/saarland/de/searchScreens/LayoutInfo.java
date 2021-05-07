package st.cs.uni.saarland.de.searchScreens;

import org.apache.commons.lang3.StringUtils;
import st.cs.uni.saarland.de.helpClasses.Info;
import st.cs.uni.saarland.de.helpMethods.CheckIfMethodsExisting;
import st.cs.uni.saarland.de.testApps.Content;

import java.util.ArrayList;
import java.util.List;

/* 
 * 
 * written by Isabelle Rommelfanger, November 2014 
 * 
 */

public class LayoutInfo extends Info{
	
//	private String searchedEReg; // main LayoutReg that is created (later called: setContentView(layoutMainReg))
	private String layoutID = ""; // apps id of the layout
	private String layoutIDReg = ""; // reg where the ID of the layout is stored if it is not directly set
	private List<Integer> addedLayouts; // added layouts to this layout
	private List<Integer> rootLayouts; // added layouts to this layout
	private boolean setContentViewLayout;
	private boolean isFragment;
	private String activityNameOfView = "";
	private boolean completlyProcessed;
	private boolean dynDecElement;
//	private final Logger logger =  LoggerFactory.getLogger(Thread.currentThread().getName());
	private final int id;
	
//	@Deprecated
//	public LayoutInfo(int id, boolean pfinished){
//		super("");
//		index = id;
//		setContentViewLayout = pfinished;
//		addedLayouts = new ArrayList<LayoutInfo>();
//		searchedEReg = "";
//		dynDecElement = false;
//	}
	
	public LayoutInfo(String mainLayoutReg, int pid){
		super(mainLayoutReg);
		addedLayouts = new ArrayList<Integer>();
		rootLayouts = new ArrayList<Integer>();
		completlyProcessed = false;
		id = pid;
	}

	// clones this object into new object/LayoutInfo
	public LayoutInfo clone(){
		LayoutInfo lay = new LayoutInfo(searchedEReg, Content.getNewUniqueID());
		if (setContentViewLayout)
			lay.setSetContentViewLayout();
		if (isFragment){
			lay.setFragment();
		}
		lay.setActivityNameOfView(activityNameOfView);
		lay.setLayoutID(layoutID);
		lay.setLayoutIDReg(layoutIDReg);
		if (dynDecElement)
			lay.setDynDecElement();
		return lay;		
	}
	
	public LayoutInfo cloneWithSpecId(int pid){
		LayoutInfo lay = new LayoutInfo(searchedEReg, pid);
		if (setContentViewLayout)
			lay.setSetContentViewLayout();
		lay.setActivityNameOfView(activityNameOfView);
		lay.setLayoutID(layoutID);
		lay.setLayoutIDReg(layoutIDReg);
		if (isFragment)
			lay.setFragment();
		if (completlyProcessed)
			lay.setCompletlyProcessed();
		if (dynDecElement)
			lay.setDynDecElement();
		lay.addedLayouts = addedLayouts;
		lay.rootLayouts = rootLayouts;
		return lay;		
	}
	
	@Override
	public String toString(){
		String roots = "";
		for (int r : rootLayouts){
			roots = roots + ";" + r;
		}
		String res ="Id:" + id +  "LayoutReg: " + searchedEReg + "; roots: " + roots + " ;layoutIDReg: " + layoutIDReg + "; layoutId: " + layoutID + " setContentView: " + setContentViewLayout + " ;dynDecElement: " + dynDecElement + " activityOfView " + activityNameOfView + " addedLayouts: ";
		for (int l: addedLayouts){
			res = res + l + ", ";
//			if (l.hasLayoutID())
//				res = res + l.getLayoutID() + ", ";
//			else
//				res = res + l.getSearchedEReg();
		}
		return res;
	}

	
	public boolean isFragment() {
		return isFragment;
	}

	public void setFragment() {
		this.isFragment = true;
	}

	public boolean hasActivityNameOfView(){
		return !activityNameOfView.equals("");
	}
	
	public void setDynDecElement(){
		dynDecElement = true;
	}
	
	public boolean isDynDecElement(){
		return dynDecElement;
	}
	
	public String getActivityNameOfView() {
		return activityNameOfView;
	}

	public void setActivityNameOfView(String pactivityNameOfView) {
		if (!(pactivityNameOfView.contains("android.app.") 
				|| pactivityNameOfView.contains("android.view.")
				|| pactivityNameOfView.contains("android.widget.") 
				|| pactivityNameOfView.contains("android.support."))){
			this.activityNameOfView = pactivityNameOfView;
		}
	}

	public boolean isSetContentViewLayout() {
		return setContentViewLayout;
	}

	public void setSetContentViewLayout() {
		this.setContentViewLayout = true;
	}

	public String getLayoutReg() {
		return searchedEReg;
	}

	public boolean hasLayoutID(){
		if ((!StringUtils.isBlank(layoutID)) && CheckIfMethodsExisting.getInstance().checkIfValueIsID(layoutID))
			return true;
		else {
			return false;
		}
	}

	public void setLayoutReg(String layoutReg) {
		this.searchedEReg = layoutReg;
	}

	public String getLayoutID() {
		return layoutID;
	}



	public void setLayoutID(String playoutID) {
		if ((!StringUtils.isBlank(playoutID)) && (!(playoutID.contains("android.app.")
				|| playoutID.contains("android.view.")
				|| playoutID.contains("android.widget.") 
				|| playoutID.contains("android.support.")))){
			// TODO uncomment this if dyn classes are again processed
			if (CheckIfMethodsExisting.getInstance().checkIfValueIsID(playoutID)){
				this.layoutID = playoutID;
			}
		}
	}



	public List<Integer> getAddedLayouts() {
		return addedLayouts;
	}



	public void addRootLayout(int id) {
		this.rootLayouts.add(id);
	}

	public List<Integer> getRootLayouts() {
		return rootLayouts;
	}



	public void addLayouts(int id) {
		this.addedLayouts.add(id);
	}

	public String getLayoutIDReg() {
		return layoutIDReg;
	}

	public void setLayoutIDReg(String layoutIDReg) {
		this.layoutIDReg = layoutIDReg;
	}

	
	public boolean isCompletlyProcessed() {
		return completlyProcessed;
	}

	public void setCompletlyProcessed() {
		this.completlyProcessed = true;
	}

//	@Override
//	public boolean shouldRunOnInitMethod() {
//		if ((!layoutIDReg.equals("")) || (!textReg.equals("")) || (!searchedEReg.equals("")))
//			return true;
//		else
//			return false;
//	}

	public int getID() {
		return id;
	}

	@Override
	public boolean allValuesFound() {
		return (!layoutID.equals("") && layoutIDReg.equals(""));
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime
				* result
				+ ((activityNameOfView == null) ? 0 : activityNameOfView
						.hashCode());
		result = prime * result
				+ ((addedLayouts == null) ? 0 : addedLayouts.hashCode());
		result = prime * result + (completlyProcessed ? 1231 : 1237);
		result = prime * result + (dynDecElement ? 1231 : 1237);
		result = prime * result + id;
		result = prime * result + (isFragment ? 1231 : 1237);
		result = prime * result
				+ ((layoutID == null) ? 0 : layoutID.hashCode());
		result = prime * result
				+ ((layoutIDReg == null) ? 0 : layoutIDReg.hashCode());
		result = prime * result
				+ ((rootLayouts == null) ? 0 : rootLayouts.hashCode());
		result = prime * result + (setContentViewLayout ? 1231 : 1237);
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
		LayoutInfo other = (LayoutInfo) obj;
		if (activityNameOfView == null) {
			if (other.activityNameOfView != null)
				return false;
		} else if (!activityNameOfView.equals(other.activityNameOfView))
			return false;
		if (addedLayouts == null) {
			if (other.addedLayouts != null)
				return false;
		} else if (!addedLayouts.equals(other.addedLayouts))
			return false;
		if (completlyProcessed != other.completlyProcessed)
			return false;
		if (dynDecElement != other.dynDecElement)
			return false;
		if (id != other.id)
			return false;
		if (isFragment != other.isFragment)
			return false;
		if (layoutID == null) {
			if (other.layoutID != null)
				return false;
		} else if (!layoutID.equals(other.layoutID))
			return false;
		if (layoutIDReg == null) {
			if (other.layoutIDReg != null)
				return false;
		} else if (!layoutIDReg.equals(other.layoutIDReg))
			return false;
		if (rootLayouts == null) {
			if (other.rootLayouts != null)
				return false;
		} else if (!rootLayouts.equals(other.rootLayouts))
			return false;
		if (setContentViewLayout != other.setContentViewLayout)
			return false;
		return true;
	}
	
	
}
