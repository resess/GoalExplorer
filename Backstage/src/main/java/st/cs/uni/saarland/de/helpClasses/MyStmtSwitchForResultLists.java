package st.cs.uni.saarland.de.helpClasses;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import soot.SootMethod;
import st.cs.uni.saarland.de.searchTabs.TabInfo;
import st.cs.uni.saarland.de.dissolveSpecXMLTags.TabViewInfo;
import st.cs.uni.saarland.de.searchScreens.LayoutInfo;

public class MyStmtSwitchForResultLists extends MyStmtSwitch {

	private Set<TabViewInfo> resultTabsViews = Collections.synchronizedSet(new LinkedHashSet<>());
	private Set<TabInfo> resultTabs = Collections.synchronizedSet(new LinkedHashSet<>());
	private Map<Integer, LayoutInfo> resultLayouts = new ConcurrentHashMap<>();
	private final Logger loggerHere =  LoggerFactory.getLogger(Thread.currentThread().getName());

	public void putAllToResultedLayouts(Map<Integer, LayoutInfo> toAdd){
		resultLayouts.putAll(toAdd);
	}

	public void putToResultedLayouts(Integer key, LayoutInfo value){
		resultLayouts.put(key, value);
	}

	public void removeFromResultedLayouts(Integer key){
		resultLayouts.remove(key);
	}

	public Map<Integer, LayoutInfo> getResultedLayouts(){
		return resultLayouts;
	}

	public void addAllToResultedTabsViews(Set<TabViewInfo> toAdd){
		resultTabsViews.addAll(toAdd);
	}

	public void addToResultedTabsViews(TabViewInfo toAdd){
		resultTabsViews.add(toAdd);
	}

	public Set<TabViewInfo> getResultedTabsViews(){

		return resultTabsViews;
	}

	public void addAllToResultedTabs(Set<TabInfo> toAdd){
		resultTabs.addAll(toAdd);
	}

	public void addToResultedTabs(TabInfo toAdd){
		resultTabs.add(toAdd);
	}

	public Set<TabInfo> getResultedTabs(){
		return resultTabs;
	}

	public void removeAllFromResultedTabs(Set<TabViewInfo> toRemove){
		resultTabsViews.removeAll(toRemove);
	}



	public MyStmtSwitchForResultLists(SootMethod currentSootMethod){
		super(currentSootMethod);
	}
	
	
	@Override
	public void init() {
		super.init();
		resultTabsViews = Collections.synchronizedSet(new LinkedHashSet<>());
		resultLayouts = new HashMap<>();
		shouldBreak = false;
	}
	
	public Map<Integer, LayoutInfo> getResultLayoutInfos(){
		return resultLayouts;
	}
	
	@Override
	public boolean run(){
		if (resultLayouts.size() > 0){
			return !resultLayouts.entrySet().stream().allMatch(x->x.getValue().allValuesFound());
		}else if (resultTabsViews.size() > 0){
			return !resultLayouts.entrySet().stream().allMatch(x->x.getValue().allValuesFound());
		}else if(resultTabs.size() > 0){
			boolean allValuesFound = !resultTabs.stream().allMatch(x->x.allValuesFound());
			return allValuesFound;
		}else {
			return false;
		}
	}
	
}
