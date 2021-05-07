package st.cs.uni.saarland.de.helpMethods;

import st.cs.uni.saarland.de.dissolveSpecXMLTags.FragmentDynInfo;
import st.cs.uni.saarland.de.dissolveSpecXMLTags.FragmentTagInfo;
import st.cs.uni.saarland.de.dissolveSpecXMLTags.TabViewInfo;
import st.cs.uni.saarland.de.helpClasses.Info;
import st.cs.uni.saarland.de.searchDialogs.DialogInfo;
import st.cs.uni.saarland.de.searchDynDecStrings.DynDecStringInfo;
import st.cs.uni.saarland.de.searchListener.ListenerInfo;
import st.cs.uni.saarland.de.searchListener.PagerAdapterInfo;
import st.cs.uni.saarland.de.searchMenus.DropDownNavMenuInfo;
import st.cs.uni.saarland.de.searchMenus.MenuInfo;
import st.cs.uni.saarland.de.searchMenus.PopupMenuInfo;
import st.cs.uni.saarland.de.searchScreens.LayoutInfo;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class CastInfoHelper {
	
	private static final CastInfoHelper castHelper= new CastInfoHelper();
	
	public static CastInfoHelper getInstance(){
		return castHelper;
	}
	
	public Set<DialogInfo> getResultsInDialogInfo(Set<Info> resultList){
		return resultList.stream().map(i -> (DialogInfo) i).collect(Collectors.toCollection(LinkedHashSet::new));
	} 

	public Set<ListenerInfo> getResultsInListenerInfo(Set<Info> resultList){
		return resultList.stream().map(i -> (ListenerInfo) i).collect(Collectors.toCollection(LinkedHashSet::new));
	} 
	
	public Set<FragmentTagInfo> getResultsInFragmentTagInfo(Set<Info> resultList){
		return resultList.stream().map(i -> (FragmentTagInfo) i).collect(Collectors.toCollection(LinkedHashSet::new));
	} 
	
	public Set<DynDecStringInfo> getResultsInDynDecStringInfo(Set<Info> resultList){
		return resultList.stream().map(i -> (DynDecStringInfo) i).collect(Collectors.toCollection(LinkedHashSet::new));
	} 
	
	public Set<MenuInfo> getResultsInMenuInfo(Set<Info> resultList){
		return resultList.stream().map(i -> (MenuInfo) i).collect(Collectors.toCollection(LinkedHashSet::new));
	} 
	
	public Set<PopupMenuInfo> getResultsInPopupMenuInfo(Set<Info> resultList){
		return resultList.stream().map(i -> (PopupMenuInfo) i).collect(Collectors.toCollection(LinkedHashSet::new));
	} 
	
	public Set<DropDownNavMenuInfo> getResultsInDropDownNavMenuInfo(Set<Info> resultList) {
		return resultList.stream().map(i -> (DropDownNavMenuInfo) i).collect(Collectors.toCollection(LinkedHashSet::new));
	}
	
	public Set<PagerAdapterInfo> getResultsInPagerAdapterInfos(Set<Info> resultList) {
		return resultList.stream().map(i -> (PagerAdapterInfo) i).collect(Collectors.toCollection(LinkedHashSet::new));
	}

	public Set<LayoutInfo> getResultsInLayoutInfos(Set<Info> resultList) {
		return resultList.stream().map(i -> (LayoutInfo) i).collect(Collectors.toCollection(LinkedHashSet::new));
	}
	
	public Set<FragmentDynInfo> getResultsInFragmentDynInfos(Set<Info> resultList) {
		return resultList.stream().map(i -> (FragmentDynInfo) i).collect(Collectors.toCollection(LinkedHashSet::new));
	}
	
	public Set<TabViewInfo> getResultsInTabViewInfos(Set<Info> resultList) {
		return resultList.stream().map(i -> (TabViewInfo) i).collect(Collectors.toCollection(LinkedHashSet::new));
	}

}
