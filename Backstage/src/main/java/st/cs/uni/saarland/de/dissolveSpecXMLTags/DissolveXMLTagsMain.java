package st.cs.uni.saarland.de.dissolveSpecXMLTags;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import st.cs.uni.saarland.de.entities.AppsUIElement;
import st.cs.uni.saarland.de.entities.Listener;
import st.cs.uni.saarland.de.helpMethods.CheckIfMethodsExisting;
import st.cs.uni.saarland.de.testApps.AppController;
import st.cs.uni.saarland.de.testApps.Content;
import st.cs.uni.saarland.de.helpClasses.Helper;

import java.util.*;

public class DissolveXMLTagsMain {
	private final Logger logger =  LoggerFactory.getLogger(Thread.currentThread().getName());
	private AppController appController = AppController.getInstance();
	private CheckIfMethodsExisting checker = CheckIfMethodsExisting.getInstance();
	
	
	// TODO RENAMING!
	public void dissolveXMLTags(Set<FragmentDynInfo> fragments, Set<TabViewInfo> tabViews, Set<AdapterViewInfo> adapterViews){
		// TODO move to include tag resolving
		// resolve the static fragment tag found in xml files
		this.dissolveFragmentTags();
		// process that data of the Transformer run from dynamical fragments
		processDynAddedOrReplacedFragments(fragments);
		// process that data of the Transformer run from tabviews
		processTabViews(tabViews);

		processAdapterViews(adapterViews);
	}

	// process the results of the tab view analysis
	private void processTabViews(Set<TabViewInfo> tabViewInfos) {

		// each tabViewInfo objects represents one found start of tab view analysis
		for (TabViewInfo tabInfo: tabViewInfos){
			// check if the fragment's class name was found
			if (!tabInfo.getFragmentClassName().equals("")){
				String fragmentName = tabInfo.getFragmentClassName();

				// get the views this fragment class has
				Collection<Integer> fragViews = CheckIfMethodsExisting.getInstance().getFragmentViews(fragmentName);
				if (fragViews == null)
					continue;
				// iterate over these views of the fragmentName class
				for (int viewId : fragViews){
					// extend each fragment view XMLLayoutFile object to a menu object to save that it is a TabView
					if (viewId == 0)
						continue;
					appController.extendXMLLayoutFileToMenu(viewId);

					// save that this tabview is attached to the activty act
					for (String act : tabInfo.getActivityClassName()){
						if (!checker.isNameOrText(act))
							continue;
						appController.addXMLLayoutFileToActivity(act, viewId);
					}
					// remember that the fragment class is used in the tabView(Menu)
					// TODO why we save that?
					// AppController.getInstance().addIncludedLayoutInClass(fragmentName, m.getId());

					// check if the tabView has a title text
					if (checker.isNameOrText(tabInfo.getText())){
						String text = tabInfo.getText();
						// create a new element to save this title text
						AppsUIElement textView = new AppsUIElement("-AddedText-", new ArrayList<Integer>(), String.valueOf(Content.getInstance().getNewUniqueID()), new HashMap<>(), "",null, null, null);
						// FIXME text now with #
						// TODO text will directly be replaced to the concret value
						if (CheckIfMethodsExisting.getInstance().checkIfValueIsID(text)){
							text = Content.getInstance().getStringValueFromStringId(text);
						}
						// add the text to the new element
						textView.addText(text, "default_value");
						// add the textView to the app, the menu, set the menu to "TabView"
						appController.addDataToTabViewMenuObject(viewId, "TabView", tabInfo.getListener(), textView);
					}else {
						appController.addDataToMenuObject(viewId, "TabView", tabInfo.getListener());
					}
				}
			}
		}
	}

	public void processAdapterViews(Set<AdapterViewInfo> adapterViewInfos) {
		//for each adapter view element
		//TODO double check activity class name vs declaring soot class, maybe should be set in uianalysis?
		for (AdapterViewInfo adapterViewInfo: adapterViewInfos){
			if(Helper.isIntegerParseInt(adapterViewInfo.getEID())){
				int adapterViewId = Integer.parseInt(adapterViewInfo.getEID());
				appController.extendAdapterViewWithDynItems(adapterViewId, adapterViewInfo);
				//Not needed I guess
				boolean dynListener = false;
				Listener listener = null;
				if(adapterViewInfo.getAdapterViewType().equals("spinner")){
					//TODO deal with onNothingSelected
					//Shouldn't we search in the adapter class instead?
					dynListener = CheckIfMethodsExisting.getInstance().isMethodExisting(adapterViewInfo.getDeclaringSootClass(),
							"void onItemSelected(android.widget.AdapterView,android.view.View,int,long)");
					if (dynListener) {
						//TODO switch to activityClassName
						listener = new Listener("onClick", false, "void onItemSelected(android.widget.AdapterView,android.view.View,int,long)", adapterViewInfo.getDeclaringSootClass());
						listener.setListenerClass(adapterViewInfo.getDeclaringSootClass());
					}
				}
				else { //Nope should be in the adapter view class
					//No need to do this here, dealt by listener class
					//TODO only keep the onListItemClick case
					dynListener = CheckIfMethodsExisting.getInstance().isMethodExisting(adapterViewInfo.getDeclaringSootClass(),
							"void onItemClick(android.widget.AdapterView,android.view.View,int,long)");
					if (dynListener) {
						//logger.debug("Adding listener for object onItemClick");
						listener = new Listener("onClick", false, "void onItemClick(android.widget.AdapterView,android.view.View,int,long)", adapterViewInfo.getDeclaringSootClass());
						listener.setListenerClass(adapterViewInfo.getDeclaringSootClass());
					} else {
						dynListener = CheckIfMethodsExisting.getInstance().isMethodExisting(adapterViewInfo.getDeclaringSootClass(),
								"void onListItemClick(android.widget.ListView,android.view.View,int,long)");
						if (dynListener) {
							//logger.debug("Adding listener for object onListItemClick");
							listener = new Listener("onClick", false, "void onListItemClick(android.widget.ListView,android.view.View,int,long)", adapterViewInfo.getDeclaringSootClass());
							listener.setListenerClass(adapterViewInfo.getDeclaringSootClass());
						}
					}
				}
				//also onListItemClick
				if(listener != null){
					Collection<Listener> listeners = new HashSet<Listener>();
					listeners.add(listener);
					appController.addDataToAdapterViewObject(adapterViewId, listeners, adapterViewInfo.getDeclaringSootClass());
				}
			}
			else{
				logger.error("No AdapterView found with this id {}", adapterViewInfo.getEID());
			}
				

		}
		//create a AppsUIElement for every item in the listview (in text I guess) with:
			//either the assigned layout class type for ArrayAdapter
			// Or whatever was obtained from parsing getView
		//add onListItemClick as a listener for each of them
		//add it to all the children elements as well
	}

	// processes all found fragment tags
	public void dissolveFragmentTags(){
		Iterator iter = appController.getFragmentTagIDIterator();
		while(iter.hasNext()){
			int fragTagID = (int) iter.next();
			if (fragTagID == 0)
				continue;
			String fragClassName = appController.getFragmentClassFromTag(fragTagID);
			if (!checker.isNameOrText(fragClassName))
				continue;
			// analyse this fragment and resolve the fragment class to its layouts
			Collection<Integer> fragViews = CheckIfMethodsExisting.getInstance().getFragmentViews(fragClassName);
			if (fragViews != null && fragViews.size() > 0)
				// set the views to the fragmentTag
				appController.addXMLLayoutFileToSpecialXMLTag(fragTagID, fragViews);
		}
	}

	// process the results of the dynamic fragment analysis
	private void processDynAddedOrReplacedFragments(Set<FragmentDynInfo> list){
		logger.info(String.format("Found %s dynamic fragments", list.size()));
		// process all found info (data storage) objects
		// fragInfo is the Info object which was processed in the class where a transaction has taken place
		int fragNumber = 0;
		for (FragmentDynInfo fragInfo : list){
			logger.info(String.format("Processing fragment %s out of %s", ++fragNumber, list.size()));
			// check if the class name of the fragment was found AND
			// if the counter of that element was found where the fragment layout should be inflated
			if ((!StringUtils.isBlank(fragInfo.getFragmentClassName())) && !StringUtils.isBlank(fragInfo.getUiElementWhereFragIsAddedID())){

				//logger.info(fragInfo.getFragmentClassName());
				// get the layout of the Fragment:
				Collection<Integer> mainLayout = CheckIfMethodsExisting.getInstance().getFragmentViews(fragInfo.getFragmentClassName());
				
				String uiEIdWhereFragIsAdded = fragInfo.getUiElementWhereFragIsAddedID();
				// search the element where the fragment layout is added
				if (!CheckIfMethodsExisting.getInstance().checkIfValueIsID(uiEIdWhereFragIsAdded))
					continue;
				int elementId = Integer.parseInt(uiEIdWhereFragIsAdded);

				appController.extendAppsUIElementWithSpecialTag(elementId);
				if (mainLayout != null && mainLayout.size() > 0 )
					appController.addXMLLayoutFileToSpecialXMLTag(elementId, mainLayout);

//				for (XMLLayoutFile xmlF : app.getAllXMLLayoutFiles()){
//					if (xmlF.containsAppsUIElementWithoutIncludings(uiEIdWhereFragIsAdded)){
//						// replace this element with a SpecialXMLTag (NO XMLFragment tag: that is only for static use)
//						SpecialXMLTag spec = xmlF.extendAppsUIElementWithSpecialTag(uiEIdWhereFragIsAdded);
//						// add the layout of the fragment to the element
//						spec.addXmlFiles(mainLayout);
//						break;
//					}
//				}
			}
		}
	}

//	//  move with fragment tag analysis
//	//  duplicate methods!!! with CheckIfMethodsExisting.getInstance().getFragmentViews(fragInfo.getFragmentClassName(), app);
//	// search and processes the fragment class layout which is included in the XMLFragment object
//	private Application analyseFragmentClass(String fragmentClassName, XMLFragment fragmentTag){
//		//  cache results!!
//		StmtSwitchForReturnedLayouts retLaySwitch = new StmtSwitchForReturnedLayouts(null);
//		String signature = "<" + fragmentClassName + ": android.view.View onCreateView(android.view.LayoutInflater,android.view.ViewGroup,android.os.Bundle)>";
//		SootMethod m;
//		try{
//			m = Scene.v().getMethod(signature);
//		}
//		catch (RuntimeException e){
//			m = null;
//		}
//		Map<Integer, LayoutInfo> layInfos = InterprocAnalysis.getInstance().runFragmentGetViewSwitchOverUnits(retLaySwitch, m);
//
////		Map<Integer, LayoutInfo> layInfos = stmtSwitch.getResults();
//
//		for (Entry<Integer, LayoutInfo> entry : layInfos.entrySet()){
//			LayoutInfo layout = entry.getValue();
//			if (layout.allValuesFound()){
//				if (layout.hasLayoutID()){
//					app.addClassToLayoutFileOrClass(fragmentClassName, layout.getLayoutID());
//					fragmentTag.addXmlFiles(app.getXMLLayoutFileFromItsId(layout.getLayoutID()));
//				}
//			}
//		}
//		return app;
//	}


	// process the results of the dynamic fragment analysis
//	private Application processDynAddedOrReplacedFragments(List<FragmentDynInfo> list, Application app){
//
//		// process all found info (data storage) objects
//		// fragInfo is the Info object which was processed in the class where a transaction has taken place
//		for (FragmentDynInfo fragInfo : list){
//			//  strange check -> why there should be a null object in this list??? big bug-> logger; or delete this check
//			if (fragInfo == null)
//				continue;
//			// check if the class name of the fragment was found AND
//			// if the counter of that element was found where the fragment layout should be inflated
//			if ((fragInfo.getFragmentClassName() != null) && (fragInfo.getUiElementWhereFragIsAddedID() != null)){
//
//				// get the layout of the Fragment:
//				List<XMLLayoutFile> mainLayout = CheckIfMethodsExisting.getInstance().getFragmentViews(fragInfo.getFragmentClassName(), app);
//
//				String uiEIdWhereFragIsAdded = fragInfo.getUiElementWhereFragIsAddedID();
//				// search the element where the fragment layout is added
//				for (XMLLayoutFile xmlF : app.getAllXMLLayoutFiles()){
//					if (xmlF.containsAppsUIElementWithoutIncludings(uiEIdWhereFragIsAdded)){
//						// replace this element with a SpecialXMLTag (NO XMLFragment tag: that is only for static use)
//						SpecialXMLTag spec = xmlF.extendAppsUIElementWithSpecialTag(uiEIdWhereFragIsAdded);
//						// add the layout of the fragment to the element
//						spec.addXmlFiles(mainLayout);
//						break;
//					}
//				}
//			}
//		}
//		return app;
//	}

}
