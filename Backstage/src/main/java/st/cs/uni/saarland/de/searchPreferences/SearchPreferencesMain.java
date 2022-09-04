package st.cs.uni.saarland.de.searchPreferences;

import st.cs.uni.saarland.de.testApps.AppController;
import java.util.Set;


public class SearchPreferencesMain {
    AppController appController = AppController.getInstance();

    //Eventually, we'll have individual preference elements (like a list view item) with onPreferenceChange


    public void processPreferences(Set<PreferenceInfo> preferenceInfos) {

        for(PreferenceInfo prefInfo: preferenceInfos) {
            //Need to: propagate the activity name to the childrens of the thing
            if(prefInfo.hasLayoutID() && prefInfo.hasActivityName())
                appController.addPreferenceToActivity(prefInfo.getActivityName(), prefInfo.getLayoutID());
        }
    }
    
}
