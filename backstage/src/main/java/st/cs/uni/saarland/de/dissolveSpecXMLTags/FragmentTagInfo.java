package st.cs.uni.saarland.de.dissolveSpecXMLTags;

import st.cs.uni.saarland.de.helpClasses.Info;

public class FragmentTagInfo extends Info{


	private String layout;
	
	public FragmentTagInfo(String returnReg) {
		super(returnReg);
//		layouts = new ArrayList<String>();
	}

	public String getLayout() {
		return layout;
	}

	public void addLayout(String layout) {
		this.layout = layout;
	}

//	@Override
//	public boolean shouldRunOnInitMethod() {
//		return false;
//	}

	@Override
	public Info clone() {
		FragmentTagInfo newInfo = new FragmentTagInfo(searchedEReg);
		newInfo.setText(text);
		newInfo.setTextReg(textReg);
		newInfo.addLayout(layout);
		return newInfo;
	}

	@Override
	public boolean allValuesFound() {
		return (searchedEReg.equals("") && !layout.equals(""));
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((layout == null) ? 0 : layout.hashCode());
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
		FragmentTagInfo other = (FragmentTagInfo) obj;
		if (layout == null) {
			if (other.layout != null)
				return false;
		} else if (!layout.equals(other.layout))
			return false;
		return true;
	}


}
