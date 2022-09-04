package st.cs.uni.saarland.de.searchListener;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import soot.Scene;
import soot.SootMethod;
import st.cs.uni.saarland.de.entities.Listener;
import st.cs.uni.saarland.de.helpClasses.Helper;
import st.cs.uni.saarland.de.helpClasses.Info;
import st.cs.uni.saarland.de.helpMethods.CheckIfMethodsExisting;
import st.cs.uni.saarland.de.helpMethods.InterprocAnalysis;
import st.cs.uni.saarland.de.testApps.AppController;

import java.util.*;
import java.util.stream.Collectors;

/* 
 * 
 * written by Isabelle Rommelfanger, November 2014 
 * 
 */

public class SearchListener {


	private final Logger logger =  LoggerFactory.getLogger(Thread.currentThread().getName());
	private AppController appController = AppController.getInstance();
	private CheckIfMethodsExisting checker = CheckIfMethodsExisting.getInstance();
	
	
	public void runSearchListener(Set<ListenerInfo> qs){
		logger.info("<SearchListener>");
		// search for listeners for every element
		// first search for set*Listener methods in the apps code

		this.processListenerInfos(qs);

		// get activity/class for XML defined listener methods
		logger.info("<Search Classes of XML-onCLick methods>");
		this.searchListenerClassesFromXMLDefinedListener();

		logger.info("<\\Search Classes of XML-onCLick methods>");

		logger.info("<\\SearchOnClick>");
	}

	// extract the listeners from the produced results and add them to the datastructure
	private void processListenerInfos(Set<ListenerInfo> listenerInfos){
		if (listenerInfos != null){
			int countDynamicalFoundElements = 0;
			logger.debug("Found "+listenerInfos.size() + " listeners.");
			final int size = listenerInfos.size();
			int counter = 0;
			for (ListenerInfo listenerInfo : listenerInfos){
				logger.debug("Processing "+ ++counter + " out of "+size);
				logger.debug(listenerInfo.toString());
				
					if (checker.checkIfValueIsID(listenerInfo.getSearchedEID())){
						
						if (listenerInfo.getSearchedEID().equals("-strange Dyn Dec Element-")){
							countDynamicalFoundElements++;
							continue;
						}
						
						if (listenerInfo.isAdapter()){
							// TODO analyse not only the "getItem" method in PagerAdapter
							
							for (String listClass: listenerInfo.getListenerClasses()){
								if (StringUtils.isBlank(listClass))
									continue;
								// get all main Views/XMLLayoutFiles, which are returned from the getItem method in the adapter
								Set<Integer> mainFragLayouts = getMainLayoutOfFragmentsInAdapter(listClass);
								if (mainFragLayouts == null || mainFragLayouts.size() == 0)
									continue;

								//appController.addFragmentClassToItsViewIDs(listClass, mainFragLayouts);

								
								// get the uiElement (probably a ViewPager) corresponding to the Adapter
								// add the layout/fragment to the element
								int elementID = Integer.parseInt(listenerInfo.getSearchedEID());
								appController.extendAppsUIElementWithSpecialTag(elementID);
								appController.addXMLLayoutFileToSpecialXMLTag(elementID, mainFragLayouts);

//								for (XMLLayoutFile xmlF : app.getAllXMLLayoutFiles()){
//									if (xmlF.containsAppsUIElementWithoutIncludings(listenerInfo.getSearchedEID())){
//										try{
//											SpecialXMLTag viewPager = xmlF.extendAppsUIElementWithSpecialTag(listenerInfo.getSearchedEID());
//											viewPager.addXmlFiles(mainFragLayouts);
//											break;
//										}catch(IllegalArgumentException e ){
//											logger.error(app.getName() + ": Did not find UIElement with that ID: " + listenerInfo.getSearchedEID() + "; ListenerInfo: " + listenerInfo);
//										}
//									}
//								}
							}
						}else{	
							for (String callbackMethod : listenerInfo.getListenerMethods()){
								if (StringUtils.isBlank(callbackMethod))
									continue;
								for (String listClass: listenerInfo.getListenerClasses()){
									if (StringUtils.isBlank(listClass))
										continue;
									//TODO fix the issue of overriding listeners?
									Listener l = new Listener(listenerInfo.getWhichAction(), false, callbackMethod, listenerInfo.getDecaringSootClass());
									l.setListenerClass(listClass);
									try {
										appController.addListener(Integer.parseInt(listenerInfo.getSearchedEID()), l);
									}catch(NullPointerException e){
										// no error msg: id of element where the listener is attached was not found
									}
								}
							}
						}
					}
				
			}
			Helper.saveToStatisticalFile("Dynamical declared elements, found: " + countDynamicalFoundElements);
		}
	}

	// TODO check repair
	private Set<Integer> getMainLayoutOfFragmentsInAdapter(String listenerClass) {
		Set<Integer> res = new HashSet<Integer>();
		//TODO deal with androidx as well
	// get Fragment class names that are returned from the getItem method in the adapter
		String fragmentOnCreateViewSig = "<" + listenerClass + ": " + "android.support.v4.app.Fragment getItem(int)>";
		logger.debug("Started Page Adapter");
		List<Info> infos = InterprocAnalysis.getInstance().runStmtSwitchOnSpecificMethodForward(new StmtSwitchForPagerAdapter(null), fragmentOnCreateViewSig);
		logger.debug("Finished Page Adapter");
		// transform the returned Info object into PagerAdapterInfos
		Set<PagerAdapterInfo> pInfos = infos.stream().map(i -> (PagerAdapterInfo) i).collect(Collectors.toSet());

		// get for every found Fragment class the main corresponding View (XMLLayoutFiles)
		for (PagerAdapterInfo pi : pInfos){
			String fragmentClass = pi.getFragmentClass();
			if (StringUtils.isBlank(fragmentClass))
				continue;
			// ( first Layout is the main layout (where every other view is inflated in ))
			logger.debug("Started getFragmentView");
			Collection<Integer> fragLayoutIds = CheckIfMethodsExisting.getInstance().getFragmentViews(fragmentClass);
			logger.debug("Finished getFragmentView");
			if (fragLayoutIds != null)
				res.addAll(fragLayoutIds);
		}
		return res;		
	}


	// iterates over all xml defined listener and solves/finds the connected activity
	// TODO for later implementations: search only these Java classes that were connected to the XMLLayoutFile where the uiE is located
	public void searchListenerClassesFromXMLDefinedListener(){

		List<String> possibleClasses = appController.getActivityNames();
		XMLDefinedListenerIterator iterXMLListeners = appController.getIteratorOfXMLDefinedListeners();

		// iterate over all listener objects that are xml defined and connected to an ui element
		while (iterXMLListeners.hasNext()){
			// the xml defined listener which is next analysed
			Listener listener = iterXMLListeners.next();
			boolean found = false;
			// iter through all possible classes -> all activities
			for (int i = 0; i < possibleClasses.size(); i++) {

				String methodSignature = "";
				// create the methodSignature of the listener
				methodSignature = String.format("<%s: void %s(android.view.View)>", possibleClasses.get(i), listener.getListenerMethod());

				// check if this method exists
				if (Scene.v().containsMethod(methodSignature)){
					// set the listener class and method accordingly
					listener.setListenerClass(possibleClasses.get(i));
					listener.setListenerMethod(String.format("void %s(android.view.View)", listener.getListenerMethod()));
					found = true; // remember that this listener was found
					break;
				}
			}
			// if it was not found, probably the method has a different return type then void
			if (!found){
				for (int i = 0; i < possibleClasses.size(); i++) {
					// check if inside this activity is a class with the name of the listener method
					if (Scene.v().getSootClass(possibleClasses.get(i)).declaresMethodByName(listener.getListenerMethod())){
						// set the listener class to the activity
						listener.setListenerClass(possibleClasses.get(i));
						try{
							// try to get the method with the listener method's name
							SootMethod m = Scene.v().getSootClass(possibleClasses.get(i)).getMethodByName(listener.getListenerMethod());
							// set the signature of the method to the listener method
							listener.setListenerClass(m.getDeclaringClass().toString());
							listener.setListenerMethod(m.getSubSignature());
						}catch( Exception e){
							// if the method is for example ambiguous then an exception will be thrown
							// iter through all soot method of the activity class
							for (SootMethod m : Scene.v().getSootClass(possibleClasses.get(i)).getMethods()){
								// catch the first method that has the same name than the listener method's name
								if (m.getName().equals(listener.getListenerMethod())){
									// set the listener method to the signature of the found method with the same name
									listener.setListenerClass(m.getDeclaringClass().toString());
									listener.setListenerMethod(m.getSubSignature());
									
								}
							}
						}
						break;
					}
				}
			}
		}

//		for (Listener list : uiE.getListernersFromElement()) {
//
//			// look if the class was already found and the tool can break out of the search
//			boolean found = false;
//			// this is only for the XML defined listeners
//			if (list.isXMLDefined()) {
//				List<String> possibleClasses = appController.getActivityNames();
//
//				// iter through all possibleClasses where the listener method
//				// could be located
//				for (int i = 0; i < possibleClasses.size(); i++) {
//
//					String methodSignature = "";
//
//					methodSignature = String.format("<%s: void %s(android.view.View)>", possibleClasses.get(i), list.getListenerMethod());
//
//					if (Scene.v().containsMethod(methodSignature)){
//						list.setListenerClass(possibleClasses.get(i));
//						list.setListenerMethod(String.format("void %s(android.view.View)", list.getListenerMethod()));
//						found = true;
//						break;
//					}
//				}
//				// if it was not found, probably the method has a different return type then void
//				if (!found){
//					for (int i = 0; i < possibleClasses.size(); i++) {
//						if (Scene.v().getSootClass(possibleClasses.get(i)).declaresMethodByName(list.getListenerMethod())){
//							list.setListenerClass(possibleClasses.get(i));
//							try{
//								SootMethod m = Scene.v().getSootClass(possibleClasses.get(i)).getMethodByName(list.getListenerMethod());
//								list.setListenerMethod(m.getSubSignature());
//							}catch( Exception e){
//								for (SootMethod m : Scene.v().getSootClass(possibleClasses.get(i)).getMethods()){
//									if (m.getName().equals(list.getListenerMethod())){
//										list.setListenerMethod(m.getSubSignature());
//										break;
//									}
//								}
//							}
//							break;
//						}
//					}
//				}
//			}
//		}
	}
}
