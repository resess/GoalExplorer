package st.cs.uni.saarland.de.entities;

import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import st.cs.uni.saarland.de.helpClasses.Helper;
import st.cs.uni.saarland.de.testApps.Content;
import org.apache.commons.lang3.StringUtils;

public class PreferenceScreen extends XMLLayoutFile{
    protected String assignedActivity;
    protected List<Integer> preferenceElementsIds;
    private final Logger logger =  LoggerFactory.getLogger(Thread.currentThread().getName());
    

    public PreferenceScreen(){
        super();
        assignedActivity = "";
        preferenceElementsIds = new ArrayList<>();
    }
    
    public void setAssignedActivity(String activityName) {
        this.assignedActivity = activityName;

    }

    public String getAssignedActivity() {
        return assignedActivity;
    }

    public void addPreferenceElementId(int id){
        preferenceElementsIds.add(id);
    }

    public List<Integer> getPreferenceElementsIds(){
        return this.preferenceElementsIds;
    }

    @Override
    public void setName(String name) {
		if (StringUtils.isBlank(this.name)){
			this.name = name;
            this.id = Content.getInstance().getIdFromName(name, "xml");
			if (id== 0){
				Helper.saveToStatisticalFile("PreferenceScreen:setName: couldn't find id of name: " + name);
                this.id = Content.getInstance().getIdFromName(name, "layout");
                if (id== 0)
                    Helper.saveToStatisticalFile("PreferenceScreen:setName: couldn't find id of name: " + name);
			}
		}else{
			logger.error("someone tried to replace the name/id of the XMLLayoutFile: " + name);
		}
	}
}
