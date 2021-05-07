package st.cs.uni.saarland.de.helpClasses;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

public abstract class Info {
	
	protected String searchedEReg = "";
	protected Set<String> text = new HashSet<String>();
	protected Set<String> textReg = new HashSet<String>();
	protected boolean shouldRunOnInitMethod;
	
	public Info(String reg) {
		searchedEReg = reg;
		shouldRunOnInitMethod = false;
	}

	public String getSearchedEReg() {
		return searchedEReg;
	}

	public void setSearchedEReg(String searchedEReg) {
		this.searchedEReg = searchedEReg;
	}

	public String getText() {
		return String.join("#", text);
	}
	
	public void addText(String ptext) {
		if (!StringUtils.isBlank(ptext))
			text.add(ptext);
//		// TODO why text could null
//		if (StringUtils.isBlank(ptext))
//			return;
//		text = StringUtils.isBlank(text) ? ptext : String.join("#", text, ptext);
	}
		
	public String toString() {
		return "SearchedEReg: " + searchedEReg  + "; text: " + text;
	}
	
	public Set<String> getTextReg() {
		return textReg;
	}

	public void addTextReg(String textReg) {
		this.textReg.add(textReg);
	}
	
	public void removeTextReg(String ptextReg){
		textReg.remove(ptextReg);
	}
	
	public void setTextReg(Set<String> textReg) {
		this.textReg = textReg;
	}
	
	public void setText(Set<String> texts){
		text.addAll(texts);
	}

	public abstract Info clone();

	public abstract boolean allValuesFound();
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Info other = (Info) obj;
		if (searchedEReg == null) {
			if (other.searchedEReg != null)
				return false;
		} else if (!searchedEReg.equals(other.searchedEReg))
			return false;
		if (text == null) {
			if (other.text != null)
				return false;
		} else if (!text.equals(other.text))
			return false;
		if (textReg == null) {
			if (other.textReg != null)
				return false;
		} else if (!textReg.equals(other.textReg))
			return false;
		return true;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((searchedEReg == null) ? 0 : searchedEReg.hashCode());
		result = prime * result + ((text == null) ? 0 : text.hashCode());
		result = prime * result + ((textReg == null) ? 0 : textReg.hashCode());
		return result;
	}
}
