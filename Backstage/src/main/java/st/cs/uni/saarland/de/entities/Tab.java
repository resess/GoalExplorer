package st.cs.uni.saarland.de.entities;

import java.io.Serializable;
import java.util.Objects;

public class Tab implements Serializable {

    private String parentActivityName;
    private String contentActivityName;
    private String indicatorText;
    private String indicatorTextResId;

    public Tab(String parentActivityName, String contentActivityName, String indicatorText, String indicatorTextResId) {
        this.parentActivityName = parentActivityName;
        this.contentActivityName = contentActivityName;
        this.indicatorText = indicatorText;
        this.indicatorTextResId  = indicatorTextResId;
    }

    public String getParentActivityName() {
        return parentActivityName;
    }

    public void setParentActivityName(String parentActivityName) {
        this.parentActivityName = parentActivityName;
    }

    public String getContentActivityName() {
        return contentActivityName;
    }

    public void setContentActivityName(String contentActivityName) {
        this.contentActivityName = contentActivityName;
    }

    public String getIndicatorText() {
        return indicatorText;
    }

    public void setIndicatorText(String indicatorText) {
        this.indicatorText = indicatorText;
    }

    public String getIndicatorTextResId() {
        return indicatorTextResId;
    }

    public void setIndicatorTextResId(String indicatorTextResId) {
        this.indicatorTextResId = indicatorTextResId;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null || !(obj instanceof Tab))
            return false;

        Tab other = (Tab) obj;

        return this.getParentActivityName().equals(other.getParentActivityName()) &&
                this.getContentActivityName().equals(other.getContentActivityName()) &&
                this.getIndicatorText().equals(other.getIndicatorText()) &&
                this.getIndicatorTextResId().equals(other.getIndicatorTextResId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(parentActivityName, contentActivityName, indicatorText, indicatorTextResId);
    }
}
