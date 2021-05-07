package st.cs.uni.saarland.de.helpClasses;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import soot.SootMethod;
import st.cs.uni.saarland.de.dissolveSpecXMLTags.TabViewInfo;
import st.cs.uni.saarland.de.searchScreens.LayoutInfo;

public class MyStmtSwitchForResultLists extends MyStmtSwitch {

	private Set<TabViewInfo> resultTabs = Collections.synchronizedSet(new LinkedHashSet<>());
	private Map<Integer, LayoutInfo> resultLayouts = new ConcurrentHashMap<>();

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

	public void addAllToResultedTabs(Set<TabViewInfo> toAdd){
		resultTabs.addAll(toAdd);
	}

	public void addToResultedTabs(TabViewInfo toAdd){
		resultTabs.add(toAdd);
	}

	public Set<TabViewInfo> getResultedTabs(){
		return resultTabs;
	}

	public void removeAllFromResultedTabs(Set<TabViewInfo> toRemove){
		resultTabs.removeAll(toRemove);
	}



	public MyStmtSwitchForResultLists(SootMethod currentSootMethod){
		super(currentSootMethod);
	}
	
	
	@Override
	public void init() {
		super.init();
		resultTabs = Collections.synchronizedSet(new LinkedHashSet<>());
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
		}else if (resultTabs.size() > 0){
			return !resultLayouts.entrySet().stream().allMatch(x->x.getValue().allValuesFound());
		}else{
			return false;
		}
	}
	
}
