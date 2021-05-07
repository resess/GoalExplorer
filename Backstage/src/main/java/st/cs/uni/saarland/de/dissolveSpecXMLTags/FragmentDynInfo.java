package st.cs.uni.saarland.de.dissolveSpecXMLTags;

import st.cs.uni.saarland.de.helpClasses.Info;

public class FragmentDynInfo extends Info {

	private String fragmentTransactionReg = "";
	private String fragmentManagerReg = "";
	private String uiElementWhereFragIsAddedID = "";
	private String uiElementWhereFragIsAddedIDReg = "";
	private String fragmentClassName = "";
	// searchedUIReg = FragmentReg
	
	// searchedUIReg = FragmentReg
	public FragmentDynInfo(String fragmentTransactionReg) {
		super("");
		this.fragmentTransactionReg = fragmentTransactionReg;
	}

	public String getFragmentManagerReg() {
		return fragmentManagerReg;
	}

	public void setFragmentManagerReg(String fragmentManagerReg) {
		this.fragmentManagerReg = fragmentManagerReg;
	}

	public String getUiElementWhereFragIsAddedID() {
		return uiElementWhereFragIsAddedID;
	}

	public void setUiElementWhereFragIsAddedID(String uiElementWhereFragIsAddedID) {
		this.uiElementWhereFragIsAddedID = uiElementWhereFragIsAddedID;
	}

	public String getFragmentClassName() {
		return fragmentClassName;
	}

	public void setFragmentClassName(String fragmentClassName) {
		this.fragmentClassName = fragmentClassName;
	}

	public String getFragmentTransactionReg() {
		return fragmentTransactionReg;
	}

	public void setFragmentTransactionReg(String fragmentTransactionReg) {
		this.fragmentTransactionReg = fragmentTransactionReg;
	}

	public String getUiElementWhereFragIsAddedIDReg() {
		return uiElementWhereFragIsAddedIDReg;
	}

	public void setUiElementWhereFragIsAddedIDReg(
			String uiElementWhereFragIsAddedIDReg) {
		this.uiElementWhereFragIsAddedIDReg = uiElementWhereFragIsAddedIDReg;
	}

	@Override
	public String toString(){
		return "FragClass: " + fragmentClassName + " ;UiIDWhereItsAdded: " + uiElementWhereFragIsAddedID;
	}

//	@Override
//	public boolean shouldRunOnInitMethod() {
//		if ((!uiElementWhereFragIsAddedIDReg.equals("")) || (!searchedEReg.equals("")))
//			return true;
//		else
//			return false;
//	}

	@Override
	public Info clone() {
		FragmentDynInfo newInfo = new FragmentDynInfo(fragmentTransactionReg);
		newInfo.setFragmentClassName(fragmentClassName);
		newInfo.setFragmentManagerReg(fragmentManagerReg);
		newInfo.setSearchedEReg(searchedEReg);
		newInfo.setText(text);
		newInfo.setTextReg(textReg);
		newInfo.setUiElementWhereFragIsAddedID(uiElementWhereFragIsAddedID);
		newInfo.setUiElementWhereFragIsAddedIDReg(uiElementWhereFragIsAddedIDReg);
		return newInfo;
	}

	@Override
	public boolean allValuesFound() {
		return (fragmentTransactionReg.equals("") && !fragmentClassName.equals("") && fragmentManagerReg.equals("") && !uiElementWhereFragIsAddedID.equals("") && uiElementWhereFragIsAddedIDReg.equals(""));
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime
				* result
				+ ((fragmentClassName == null) ? 0 : fragmentClassName
						.hashCode());
		result = prime
				* result
				+ ((fragmentManagerReg == null) ? 0 : fragmentManagerReg
						.hashCode());
		result = prime
				* result
				+ ((fragmentTransactionReg == null) ? 0
						: fragmentTransactionReg.hashCode());
		result = prime
				* result
				+ ((uiElementWhereFragIsAddedID == null) ? 0
						: uiElementWhereFragIsAddedID.hashCode());
		result = prime
				* result
				+ ((uiElementWhereFragIsAddedIDReg == null) ? 0
						: uiElementWhereFragIsAddedIDReg.hashCode());
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
		FragmentDynInfo other = (FragmentDynInfo) obj;
		if (fragmentClassName == null) {
			if (other.fragmentClassName != null)
				return false;
		} else if (!fragmentClassName.equals(other.fragmentClassName))
			return false;
		if (fragmentManagerReg == null) {
			if (other.fragmentManagerReg != null)
				return false;
		} else if (!fragmentManagerReg.equals(other.fragmentManagerReg))
			return false;
		if (fragmentTransactionReg == null) {
			if (other.fragmentTransactionReg != null)
				return false;
		} else if (!fragmentTransactionReg.equals(other.fragmentTransactionReg))
			return false;
		if (uiElementWhereFragIsAddedID == null) {
			if (other.uiElementWhereFragIsAddedID != null)
				return false;
		} else if (!uiElementWhereFragIsAddedID
				.equals(other.uiElementWhereFragIsAddedID))
			return false;
		if (uiElementWhereFragIsAddedIDReg == null) {
			if (other.uiElementWhereFragIsAddedIDReg != null)
				return false;
		} else if (!uiElementWhereFragIsAddedIDReg
				.equals(other.uiElementWhereFragIsAddedIDReg))
			return false;
		return true;
	}


}
