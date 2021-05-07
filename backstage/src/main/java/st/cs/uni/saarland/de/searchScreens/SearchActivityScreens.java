package st.cs.uni.saarland.de.searchScreens;

import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import st.cs.uni.saarland.de.entities.Application;
import st.cs.uni.saarland.de.testApps.AppController;
import st.cs.uni.saarland.de.testApps.Content;
// TODO check
public class SearchActivityScreens {

	AppController appController = AppController.getInstance();
	
	public void runSearchActivityScreens(Map<Integer, LayoutInfo> layoutInfos){
		for (Entry<Integer, LayoutInfo> entry: layoutInfos.entrySet()){
			LayoutInfo lay = entry.getValue();
			
			
			if (lay.isSetContentViewLayout()){
				if (lay.hasLayoutID() && lay.hasActivityNameOfView()){
					appController.addXMLLayoutFileToActivity(lay.getActivityNameOfView(), Integer.parseInt(lay.getLayoutID()));
				}

				for (Integer addedLayoutId : lay.getAddedLayouts()){
					addAllLayoutsForActivity(lay.getActivityNameOfView(), layoutInfos.get(addedLayoutId), layoutInfos);
					// merged layouts get added to the datastructure if the layout is processed (in else case then)
				}
			}else{
				// currently the dyn dec elements/layouts are not processed
				if (!lay.isDynDecElement()){
					// in lay.getLayoutID the class name of the dyn dec element is saved
					if (lay.hasLayoutID()){
						for (Integer addedLayoutId : lay.getAddedLayouts()){
							addAllLayoutsForClass(lay.getLayoutID(), layoutInfos.get(addedLayoutId),layoutInfos);
						}
					}
				}
			}
		}
		
		//resolveDynamicDeclaredClasses();
	}

	private void addAllLayoutsForActivity(String activityName, LayoutInfo layout, Map<Integer, LayoutInfo> layoutList){
		if (layout != null){
			if (layout.hasLayoutID() && !StringUtils.isBlank(activityName))
				appController.addXMLLayoutFileToActivity(activityName, Integer.parseInt(layout.getLayoutID()));
			
			for (Integer addedLayoutId : layout.getAddedLayouts()){
				if (!StringUtils.isBlank(activityName))
					addAllLayoutsForActivity(activityName, layoutList.get(addedLayoutId), layoutList);
			}
		}
	}

	// TODO rename, we don't save class to layout anymore except of activties
	private void addAllLayoutsForClass(String className, LayoutInfo layout, Map<Integer, LayoutInfo> layoutList){
		// layout ID could be empty if it is never found
		if (layout != null && layout.hasLayoutID() && !StringUtils.isBlank(className)){
			int idClassName = Integer.parseInt(className);
			int layID = Integer.parseInt(layout.getLayoutID());
			appController.addTwoMergedLayouts(idClassName,layID );
			
			for (Integer addedLayoutId : layout.getAddedLayouts()){
				addAllLayoutsForClass(className, layoutList.get(addedLayoutId), layoutList);
			}
		}
	}

//	private void resolveDynamicDeclaredClasses() {
//
//		for (Entry<String, Set<String>> entry: app.getActivityToXMLLayoutFiles().entrySet()){
//			Set<String> ids = entry.getValue();
//			Set<String> toAdd = new HashSet<String>();
//			Set<String> toRemove = new HashSet<String>();
//			for (String posID: ids){
//				if ((posID.length() > 0 ) && (!Character.isDigit(posID.charAt(0)))){
//					Set<String> classesIDs = app.getClassToLayoutFileOrClass().get(posID);
//					if ((classesIDs != null) && (classesIDs.size() > 0 )){
//						toRemove.add(posID);
//						toAdd.addAll(classesIDs);
//					}
//				}
//			}
//			ids.removeAll(toRemove);
//			ids.addAll(toAdd);
//		}
//	}

}
