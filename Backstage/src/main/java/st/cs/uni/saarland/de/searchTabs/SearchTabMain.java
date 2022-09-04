package st.cs.uni.saarland.de.searchTabs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import st.cs.uni.saarland.de.entities.Tab;
import st.cs.uni.saarland.de.testApps.AppController;

import java.util.Set;

public class SearchTabMain {

    private final Logger logger =  LoggerFactory.getLogger(Thread.currentThread().getName());

    public void getTabsInApp(Set<TabInfo> tabInfos){
        processTabResults(tabInfos);
    }

    public void processTabResults(Set<TabInfo> tabs){
        for(TabInfo tab : tabs) {
            if(tab == null)
                continue;

            if(tab.getContent() == null) {
                logger.error("Tab missing content");
                continue;
            }

            String clnName = tab.getContentActivityName();
            if(clnName.contains("#")) {
                logger.warn("Multiple destinations for intent found, defaulting to first {}", clnName);
                logger.error("Multiple destinations for intent found, defaulting to first {}", clnName);
                clnName = clnName.split("#")[0];
            }
            clnName = clnName.replace("/" ,".");
            if (clnName.startsWith("L")) {
                clnName = clnName.substring(1);
            }
            if (clnName.endsWith(";")) {
                clnName = clnName.substring(0, clnName.length()-1);
            }
            logger.info("Tab added: Parent: " + tab.getParentActivityName() + " Content: " + clnName  + " Indicator: " + tab.getIndicatorText());
            if(tab.getContentActivityName().equals("") || tab.getParentActivityName().equals("") || tab.getIndicatorText().equals(""))
                continue;

            Tab toAdd = new Tab(tab.getParentActivityName(), clnName, tab.getIndicatorText(), tab.getIndicatorTextResID());
            AppController appController = AppController.getInstance();
            appController.addTab(toAdd);
        }
    }

}
