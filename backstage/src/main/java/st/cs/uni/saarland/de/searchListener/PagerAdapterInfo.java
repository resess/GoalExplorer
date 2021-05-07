package st.cs.uni.saarland.de.searchListener;

import st.cs.uni.saarland.de.helpClasses.Info;

public class PagerAdapterInfo extends Info {

	private String fragmentClass = "";
	private String fragementClassReg = "";
	
	public PagerAdapterInfo(String returnViewReg) {
		super(returnViewReg);
	}

	public String getFragmentClass() {
		return fragmentClass;
	}

	public void setFragmentClass(String fragmentClass) {
		this.fragmentClass = fragmentClass;
	}

	public String getFragementClassReg() {
		return fragementClassReg;
	}

	public void setFragementClassReg(String fragementClassReg) {
		this.fragementClassReg = fragementClassReg;
	}

	@Override
	public Info clone() {
		PagerAdapterInfo newInfo = new PagerAdapterInfo(searchedEReg);
		newInfo.setFragementClassReg(fragementClassReg);
		newInfo.setFragmentClass(fragmentClass);
		newInfo.setText(text);
		newInfo.setTextReg(textReg);
		return newInfo;
	}

	@Override
	public boolean allValuesFound() {
		return (fragementClassReg.equals("") && !fragmentClass.equals("") && searchedEReg.equals(""));
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime
				* result
				+ ((fragementClassReg == null) ? 0 : fragementClassReg
						.hashCode());
		result = prime * result
				+ ((fragmentClass == null) ? 0 : fragmentClass.hashCode());
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
		PagerAdapterInfo other = (PagerAdapterInfo) obj;
		if (fragementClassReg == null) {
			if (other.fragementClassReg != null)
				return false;
		} else if (!fragementClassReg.equals(other.fragementClassReg))
			return false;
		if (fragmentClass == null) {
			if (other.fragmentClass != null)
				return false;
		} else if (!fragmentClass.equals(other.fragmentClass))
			return false;
		return true;
	}


//	@Override
//	public boolean shouldRunOnInitMethod() {
//		if (!fragementClassReg.equals(""))
//			return true;
//		else 
//			return false;
//	}

	
}
