package st.cs.uni.saarland.de.helpClasses;

public class InterProcInfo extends Info {

	private String classOfSearchedReg = "";
	private String valueOfSearchedReg = "";
	private boolean stop;
	
	// searchedUIE is the return register of the start stmt
	public InterProcInfo(String reg) {
		super(reg);
		stop = false;
	}

	public String getClassOfSearchedReg() {
		return classOfSearchedReg;
	}

	public void setClassOfSearchedReg(String classOfSearchedReg) {
		this.classOfSearchedReg = classOfSearchedReg;
	}

	public String getValueOfSearchedReg() {
		return valueOfSearchedReg;
	}

	public void setValueOfSearchedReg(String valueOfSearchedReg) {
		this.valueOfSearchedReg = valueOfSearchedReg;
	}

	public boolean finishedInfo(){
		return ((!classOfSearchedReg.equals("")) || (!valueOfSearchedReg.equals("")));
	}

//	@Override
//	public boolean shouldRunOnInitMethod() {
//		// TODO Auto-generated method stub
//		return false;
//	}

	@Override
	public Info clone() {
		InterProcInfo newInfo = new InterProcInfo(searchedEReg);
		newInfo.setClassOfSearchedReg(classOfSearchedReg);
		newInfo.stop = stop;
		newInfo.setText(text);
		newInfo.setTextReg(textReg);
		newInfo.setValueOfSearchedReg(valueOfSearchedReg);
		return newInfo;
	}

	@Override
	public boolean allValuesFound() {
		return searchedEReg.equals("");
	}
}
