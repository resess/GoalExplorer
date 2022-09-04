package st.cs.uni.saarland.de.searchTabs;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import st.cs.uni.saarland.de.helpClasses.Info;
import st.cs.uni.saarland.de.reachabilityAnalysis.IntentInfo;

public class TabInfo extends Info {

	private IntentInfo content;
	private String contentReg = "";
	private String parentActivityClassName = "";
	private String indicatorText = "";
	private String indicatorTextReg = "";
	private String indicatorTextResID = "";
	private String indicatorTextResIDReg = "";
	private final Logger logger =  LoggerFactory.getLogger(Thread.currentThread().getName());
	
	// text is text of the TabTitle
	// searchedUiReg = TabReg
	public TabInfo(String registerTab) {
		super(registerTab);
	}

	public IntentInfo getContent() {
		return content;
	}

	public void setContent(IntentInfo content) {
		this.content = content;
	}

	public String getContentReg() {
		return contentReg;
	}

	public void setContentReg(String contentReg) {
		this.contentReg = contentReg;
	}

	public String getContentActivityName() {
		if(content.getClassName() == null || content.getClassName().equals("")) {
			logger.error("Tab missing content");
			return "";
		}
		return content.getClassName();
	}

	public String getParentActivityName() {
		if(parentActivityClassName == null || parentActivityClassName.equals("")) {
			logger.error("Tab missing parent class");
			return "";
		} else {
			return parentActivityClassName;
		}
	}

	public void setParentActivityClassName(String parentActivityClassName) {
		this.parentActivityClassName = parentActivityClassName;
	}

	public String getIndicatorText() {
		return indicatorText;
	}

	public void setIndicatorText(String indicatorText) {
		this.indicatorText = indicatorText;
	}

	public String getIndicatorTextReg() {
		return indicatorTextReg;
	}

	public void setIndicatorTextReg(String indicatorTextReg) {
		this.indicatorTextReg = indicatorTextReg;
	}

	public String getIndicatorTextResID() {
		return indicatorTextResID;
	}

	public void setIndicatorTextResID(String indicatorTextResID) {
		this.indicatorTextResID = indicatorTextResID;
	}

	public String getIndicatorTextResIDReg() {
		return indicatorTextResIDReg;
	}

	public void setIndicatorTextResIDReg(String indicatorTextResIDReg) {
		this.indicatorTextResIDReg = indicatorTextResIDReg;
	}

	@Override
	public Info clone() {
		TabInfo newInfo = new TabInfo(searchedEReg);
		newInfo.setParentActivityClassName(parentActivityClassName);
		newInfo.setText(text);
		newInfo.setTextReg(textReg);
		newInfo.setContent(content);
		newInfo.setContentReg(contentReg);
		newInfo.setIndicatorTextResIDReg(indicatorTextResIDReg);
		return newInfo;
	}

	@Override
	public boolean allValuesFound() {
		return (!parentActivityClassName.equals("") && textReg.equals("") && searchedEReg.equals("") && content != null && !content.getClassName().equals(""));
	}

	@Override
	public String toString() {
		return "TabInfo{" +
				"searchedEReg='" + searchedEReg + '\'' +
				", content=" + content +
				", parentActivityClassName='" + parentActivityClassName + '\'' +
				", indicatorText='" + indicatorText + '\'' +
				", indicatorTextReg='" + indicatorTextReg + '\'' +
				", indicatorTextResID='" + indicatorTextResID + '\'' +
				'}';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		TabInfo tabInfo = (TabInfo) o;
		if (content != null) {
			if (tabInfo.content != null)
				return Objects.equals(content.getClassName(), tabInfo.content.getClassName()) && Objects.equals(parentActivityClassName, tabInfo.parentActivityClassName);
			else
				return false;
		} else {
			if(tabInfo.content != null)
				return false;
			else
				return Objects.equals(parentActivityClassName, tabInfo.parentActivityClassName);
		}
	}

	@Override
	public int hashCode() {
		if (content != null)
			return Objects.hash(content.getClassName(), parentActivityClassName);
		else
			return Objects.hash(parentActivityClassName);
	}
}
