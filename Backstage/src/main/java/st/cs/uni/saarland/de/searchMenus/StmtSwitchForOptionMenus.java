package st.cs.uni.saarland.de.searchMenus;

import soot.*;
import soot.jimple.*;
import st.cs.uni.saarland.de.entities.FieldInfo;
import st.cs.uni.saarland.de.helpClasses.Helper;
import st.cs.uni.saarland.de.helpClasses.Info;
import st.cs.uni.saarland.de.helpClasses.InterProcInfo;
import st.cs.uni.saarland.de.helpClasses.MyStmtSwitch;
import st.cs.uni.saarland.de.reachabilityAnalysis.AnalyseIntentSwitch;
import st.cs.uni.saarland.de.reachabilityAnalysis.IntentInfo;
import st.cs.uni.saarland.de.testApps.Content;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class StmtSwitchForOptionMenus extends MyStmtSwitch {
	private HashSet<MenuItemInfo> menuItemInfos = new HashSet<>();
	private List<AnalyseIntentSwitch> intentSwitches = new ArrayList<>();
	private Value caller;

//	private MenuInfo mInfo;
	// layoutId here the if of the view object!
	
//	$r0 := @this: com.example.Testapp.MainActivity;
//    $r1 := @parameter0: android.view.Menu;
//    $r2 = virtualinvoke $r0.<com.example.Testapp.MainActivity: android.view.MenuInflater getMenuInflater()>();
//    virtualinvoke $r2.<android.view.MenuInflater: void inflate(int,android.view.Menu)>(2131099649, $r1);
//	return $r2


// 	$r0 := @this: com.example.Testapp.MainActivity;
//    $r1 := @parameter0: android.view.Menu;
//	virtualinvoke $r1.<android.view.Menu: void add(int,int, int, java.lang.CharSequence)>(groupId, 2131099649, order, title);
	
	
	public StmtSwitchForOptionMenus(SootMethod currentSootMethod, Map<String, String> dynStrings){
		super(currentSootMethod, dynStrings);
	}

	public StmtSwitchForOptionMenus(MenuInfo newInfo, SootMethod currentSootMethod) {
		super(newInfo, currentSootMethod);
	}

	public StmtSwitchForOptionMenus(MenuInfo newInfo, SootMethod currentSootMethod, Map<String, String> dynStrings){
		super(newInfo, currentSootMethod);
		this.dynStrings = dynStrings;
	}
	
	public StmtSwitchForOptionMenus(SootMethod currentSootMethod) {
		super(currentSootMethod);
	}


	public Set<MenuItemInfo> getMenuItemInfos() {
		return menuItemInfos;
	}

	private void processIntents(Stmt stmt){
		for(AnalyseIntentSwitch intentSwitch: intentSwitches){
			if(!intentSwitch.isDone())
				stmt.apply(intentSwitch);
		}
	}

	public void caseIdentityStmt(final IdentityStmt stmt){
		if(Thread.currentThread().isInterrupted()){
			return;
		}
		processIntents(stmt);

//		List<Info> resultInfos = getResultInfos();
		Set<Info> toAddInfos = new LinkedHashSet<>();
		for (Info i : getResultInfos()){
			if(Thread.currentThread().isInterrupted()){
				return;
			}
			MenuInfo mInfo = (MenuInfo) i;

			//Check for all menu items inside the menu
			
			String leftReg = helpMethods.getLeftRegOfIdentityStmt(stmt),
					rightReg = stmt.getRightOp().toString();
			String rightRegType = helpMethods.getRightClassTypeOfIdentityStmt(stmt);
			if (leftReg.equals(mInfo.getActivityReg())){

				mInfo.setActivityReg("");
				if(stmt.getRightOp() instanceof ParameterRef){
					String actName = helpMethods.getRightClassTypeOfIdentityStmt(stmt);
					if (checkMethods.checkIfValueIsVariable(actName)){
						int paramIndex = ((ParameterRef)stmt.getRightOp()).getIndex();
						List<Info> resList = interprocMethods2.findInReachableMethods2(paramIndex,  getCurrentSootMethod(), new ArrayList<SootMethod>());
						if (resList.size() > 0){
							mInfo.setActivityClassName(((InterProcInfo)resList.get(0)).getClassOfSearchedReg());
							if (resList.size() > 1){
								for (int j = 1; j < resList.size() ; j++){
									InterProcInfo workingInfo = (InterProcInfo) resList.get(j);
									MenuInfo newInfo = (MenuInfo) mInfo.clone();
									toAddInfos.add(newInfo);
									newInfo.setActivityClassName(workingInfo.getClassOfSearchedReg());
								}
							}
						}
					}else{
						mInfo.setActivityClassName(actName);
					}
				}else if(stmt.getRightOp() instanceof ThisRef){
					mInfo.setActivityClassName(rightRegType);
				}	
			}
			else if (leftReg.equals(mInfo.getLayoutIDReg())){
				mInfo.setLayoutIDReg("");
				if(stmt.getRightOp() instanceof ParameterRef){
					int paramIndex = ((ParameterRef)stmt.getRightOp()).getIndex();
					List<Info> resList = interprocMethods2.findInReachableMethods2(paramIndex,  getCurrentSootMethod(), new ArrayList<SootMethod>());
					if (resList.size() > 0){
						mInfo.setLayoutID(((InterProcInfo)resList.get(0)).getValueOfSearchedReg());
						if (resList.size() > 1){
							for (int j = 1; j < resList.size() ; j++){
								InterProcInfo workingInfo = (InterProcInfo) resList.get(j);
								MenuInfo newInfo = (MenuInfo) mInfo.clone();
								toAddInfos.add(newInfo);
								newInfo.setLayoutID(workingInfo.getValueOfSearchedReg());
							}
						}
					}
				}
			}
			else {
				//Set<MenuI>

				Set<String> keysToRemove = new HashSet<>();
				Map<String, MenuItemInfo> valuesToAdd = new HashMap<>();
				if(leftReg.equals(mInfo.getSearchedEReg())){
					//We passed the menu as a parameter
					mInfo.setSearchedEReg(rightReg.split(":")[0]);
				}
				for(MenuItemInfo mItem: mInfo.getMenuItemInfos()){ //actually should be for the current menu only
					if(mItem.getIntentInfo() != null && leftReg.equals(mItem.getIntentInfo().getClassNameReg())) {
						mItem.getIntentInfo().setClassNameReg(rightReg.split(":")[0]);
					}
					else if(leftReg.equals(mItem.getIdReg())){
						mItem.setIdReg(rightReg.split(":")[0]);
						//Update the menu map //Exception?
						//mInfo = (MenuInfo)mInfo.clone();
						//if(mInfo.getDynMenus().containsKey(lef))
						keysToRemove.add(leftReg);
						valuesToAdd.put(mItem.getIdReg(), mItem);
					}
					else if(leftReg.equals(mItem.getIntentInfoReg())){
						mItem.setIntentInfoReg(rightReg.split(":")[0]);
					}
					else if(leftReg.equals(mItem.getParentMenu())){
						mItem.setParentMenu(rightReg.split(":")[0]);
						//if(mInfo.getSearchedEReg().equals(leftReg))
							//mInfo.setSearchedEReg(m);
						//TODO update menu searchedReg as well? Now, done when returned from the function assuming every menu item is for every menu
					}
					else if(leftReg.equals(mItem.getSearchedEReg())){
						mItem.setSearchedEReg(rightReg.split(":")[0]);
					}
					if(mItem.getTextReg().contains(leftReg)) {
						mItem.getTextReg().remove(leftReg);
						mItem.addTextReg(rightReg.split(":")[0]);
					}
				}
				//Here remove what we want?
				keysToRemove.forEach(key -> mInfo.removeDynMenuItem(key));
				mInfo.getDynMenus().putAll(valuesToAdd);

			}
		}
		addAllToResultInfo(toAddInfos);
	}
	


	public void caseInvokeStmt(final InvokeStmt stmt) {
		//logger.debug("Found invoke stmt while checking menus: {}",stmt);
		if(Thread.currentThread().isInterrupted()){
			return;
		}
		processIntents(stmt);

		InvokeExpr invokeExpr = stmt.getInvokeExpr();
		String methodSignature = helpMethods.getSignatureOfInvokeExpr(invokeExpr);
		
		if(methodSignature.equals("<android.view.MenuInflater: void inflate(int,android.view.Menu)>")){
			// searchedE = MenuContainer
			String layoutID = helpMethods.getParameterOfInvokeStmt(invokeExpr, 0);
			String layoutIDReg = "";
			if (!checkMethods.checkIfValueIsID(layoutID)){
				layoutIDReg = layoutID;
				layoutID = "";
			}
			MenuInfo mInfo = new MenuInfo(helpMethods.getParameterOfInvokeStmt(invokeExpr, 1), layoutID, helpMethods.getCallerOfInvokeStmt(invokeExpr), getCurrentSootMethod().getDeclaringClass().getName());
			mInfo.setLayoutIDReg(layoutIDReg);
			addToResultInfo(mInfo);

		}

		else if(methodSignature.startsWith("<android.view.Menu: android.view.MenuItem add(")
				|| methodSignature.startsWith("<android.view.ContextMenu: android.view.MenuItem add(")){
			handleDynamicMenuItems(invokeExpr,"");
		}
		//TODO deal with getItem()
		else if(methodSignature.startsWith("<android.view.MenuItem: android.view.MenuItem setIntent(android.content.Intent)")){
			//add intent info the list of intents
			//need be mapped to a given menu item index
			handleMenuItemSetIntent(invokeExpr);
		}
		else {
			String declaringClass = invokeExpr.getMethod().getDeclaringClass().getName();
			String type = helpMethods.getCallerTypeOfInvokeStmt(invokeExpr);
			if(Helper.isClassInAppNameSpace(declaringClass) && invokeExpr.getMethod().hasActiveBody()) {
				handleInterProcMenuCreation(invokeExpr);
			}
		}

	}

	private void handleInterProcMenuCreation(InvokeExpr invokeExpr) {
		//We can parse the method in any case (maybe a bit of filtering tho, like it returns a menu item or it uses menu
		//lol this is called first
		Unit lastUnitOfMethod = invokeExpr.getMethod().getActiveBody().getUnits().getLast();
		//String returnValue = ((ReturnStmt) lastUnitOfMethod).getOp().toString();

		//MenuInfo menuInfo = new MenuInfo();
		StmtSwitchForOptionMenus newStmtSwitch = new StmtSwitchForOptionMenus(invokeExpr.getMethod(), dynStrings);
		//newStmtSwitch.addAllToResultInfo(getResultInfos());

		iteratorHelper.runUnitsOverMethodBackwards(invokeExpr.getMethod().getActiveBody(), newStmtSwitch);
		//runOverToFindSpecValuesBackwards(invokeExpr.getMethod().getActiveBody(), lastUnitOfMethod, newStmtSwitch);
		//Do we need to iteratat

		Set<Info> menuInfos = newStmtSwitch.getResultInfos();
		menuItemInfos.addAll(newStmtSwitch.menuItemInfos);
		for(Info info : menuInfos){
			//We assume we always have a menu, as we're building a new menu item interprocedurally
			//TODO deal with the case where we only update menu items and do not need a new menu, i.e setStmt(menuItem, class)
			//There we'll have to
			MenuInfo mInfo = ((MenuInfo) info);
			boolean needReg = true;
			if (needReg) {
				Set<String> keysToRemove = new HashSet<>();
				Map<String, MenuItemInfo> valuesToAdd = new HashMap<>();
				for (int i = 0; i < invokeExpr.getArgCount(); i++) {
					String param = "@parameter" + i;
					//Iterator<Map.Entry<String, MenuItemInfo>> menuItemInfoIterator = mInfo.getDynMenuItems().entrySet().iterator();
					if(param.equals(mInfo.getSearchedEReg())) {
						//need to check for a menu with the same in current?
						mInfo.setSearchedEReg(param); //should it be infoke expr at i or something?
					}

					for(MenuItemInfo newInfo : mInfo.getMenuItemInfos()){
					//MenuInfo mInfo = (MenuInfo)info;
					//Here check if there's anything to fill in I guess

						//MenuItemInfo newInfo = null; //TODO iterate I guess
							if (newInfo.getIntentInfoReg().equals(param)) {
								newInfo.setIntentInfoReg(helpMethods.getParameterOfInvokeStmt(invokeExpr, i));
							} if (newInfo.getIntentInfo() != null && newInfo.getIntentInfo().getClassNameReg().equals(param)) {
								if (invokeExpr.getArg(i) instanceof ClassConstant) {
									newInfo.getIntentInfo().setClassName(((ClassConstant) invokeExpr.getArg(i)).getValue());
								}
							} if(newInfo.getTextReg().contains(param)){
								String text = helpMethods.getParameterOfInvokeStmt(invokeExpr, i);
								if(checkMethods.checkIfValueIsID(text)){
									text = Content.getInstance().getStringValueFromStringId(text);
									//empty the current text I guess
									newInfo.setText(text);
									//newInfo.addText(text);
								} else if(!checkMethods.checkIfValueIsVariable(text))//TODO, we passed a string directly?
									newInfo.setText(text.replace("\"", ""));
							}
							if (newInfo.getIdReg().equals(param)) {
								//if the id is a variable, set if right away otherwise ...
								String oldReg = newInfo.getIdReg();
								String menuItemReg = helpMethods.getParameterOfInvokeStmt(invokeExpr, i);
								if (checkMethods.checkIfValueIsInteger(menuItemReg)) {
									int menuItemId = Integer.parseInt(menuItemReg);
									newInfo.setId(menuItemId);
								} else newInfo.setIdReg(menuItemReg);
								//Exception?
								keysToRemove.add(oldReg);
								valuesToAdd.put(menuItemReg, newInfo);
							} if (newInfo.getParentMenu().equals(param)) {
							//Maybe that's hiw U should identify the menu I care about? lol dunno
							String newReg = helpMethods.getParameterOfInvokeStmt(invokeExpr, i);
							newInfo.setParentMenu(newReg);
							mInfo.setSearchedEReg(newReg);
							//We make a menu with that info if doesn't exist
							// Then we add the menu item I guess
						} if (newInfo.getSearchedEReg().equals(param)) {
							newInfo.setSearchedEReg(helpMethods.getParameterOfInvokeStmt(invokeExpr, i));
						}
					}

					//Need to make a menu out of these?

				}
				keysToRemove.forEach(key -> mInfo.removeDynMenuItem(key));
				mInfo.getDynMenus().putAll(valuesToAdd);
			}
			//check if we already have a matching menu
			MenuInfo sameMenu = null;
			for(Info i: getResultInfos()){
				MenuInfo mI = (MenuInfo) i;
				if (mI.getSearchedEReg().equals(mInfo.getSearchedEReg())) { //same menu as argument
					//Merge
					mI.mergeWith(mInfo);
					//mI.getDynMenuItems().putAll(mInfo.getDynMenuItems());
					sameMenu = mI;
					break;

				}
			}
			if(sameMenu == null)
				addToResultInfo(mInfo);
		}
	}

	private void handleMenuItemSetIntent(InvokeExpr invokeExpr) {
		logger.warn("Found a menuItem.setIntent. Not handled yet");
		logger.debug("Found a menuItem.setIntent. Not handled yet");
		MenuItemInfo currentMenuItemInfo = null;
		String reg = helpMethods.getCallerOfInvokeStmt(invokeExpr);
		for(MenuItemInfo mInfo: getMenuItemInfos()){
			if(mInfo.getSearchedEReg().equals(reg)){
				currentMenuItemInfo = mInfo;
				break;
			}
		}
		if(currentMenuItemInfo == null){
			currentMenuItemInfo = new MenuItemInfo(reg);
			menuItemInfos.add(currentMenuItemInfo);
		}
		//Need to resolve the intent basically
		Value intentReg = invokeExpr.getArg(0);
		IntentInfo intentInfo = new IntentInfo();
		AnalyseIntentSwitch intentSwitch = new AnalyseIntentSwitch(intentReg, getCurrentSootMethod(), intentInfo);
		currentMenuItemInfo.setIntentInfo(intentInfo);
		//currentMenuItemInfo.setIntentSwitch(intentSwitch);
		intentSwitches.add(intentSwitch);
		logger.info("Intent added to list for analysis");
	}

	private void handleDynamicMenuItems(InvokeExpr invokeExpr, String itemReg){
		String methodSignature = helpMethods.getSignatureOfInvokeExpr(invokeExpr);

		//We need
		//List of items -> map from reg to id

		//Get the current menu
		MenuInfo currentMenuInfo = null;
		String reg = helpMethods.getCallerOfInvokeStmt(invokeExpr);
		//TODO submenus?
		for(Info infoM: getResultInfos()){
			MenuInfo menuInfo = (MenuInfo)infoM;
			if(menuInfo.getSearchedEReg().equals(reg)){
				//Logger.debug("Updating stored menu info {}", menuInfo)
				currentMenuInfo = menuInfo;
				break;
			}
		}
		//I guess the submenu is added as its own menu?
		String callerType = helpMethods.getCallerTypeOfInvokeStmt(invokeExpr);
		if(callerType.equals("android.view.SubMenu")) {
			//Need to embed the menu within a menu item I guess?
		}
		if(currentMenuInfo == null){
			currentMenuInfo = new MenuInfo(reg, getCurrentSootMethod().getDeclaringClass().getName());
			addToResultInfo(currentMenuInfo);
		}

		int numArgs = invokeExpr.getArgs().size();
		//String title = "[PLACEHOLDER]", itemId = "";
		String menuItemReg = "";
		Integer menuItemId = null;
		//title += invokeExpr.getArg(numArgs - 1);
		if(numArgs > 1){
			menuItemReg = helpMethods.getParameterOfInvokeStmt(invokeExpr, 1);
			if (checkMethods.checkIfValueIsInteger(menuItemReg)){
				menuItemId = Integer.parseInt(menuItemReg);
			}
		}

		//Get the current menu item
		MenuItemInfo currentMenuItemInfo = null;
		if(!menuItemInfos.isEmpty()){ //first time
			for (MenuItemInfo mInfo : menuItemInfos) {
				if (mInfo.getSearchedEReg().equals(itemReg) && (menuItemReg.equals(mInfo.getIdReg()) || (mInfo.getId() == null && mInfo.getIdReg().isEmpty()))) {
					currentMenuItemInfo = mInfo;
					break;
				}
			}
			//empty the info list
			menuItemInfos.clear();
		}
		else {
			for (MenuItemInfo mInfo : currentMenuInfo.dynMenuItems.values()) {
				if (mInfo.getSearchedEReg().equals(itemReg) && (menuItemReg.equals(mInfo.getIdReg()) || (mInfo.getId() == null && mInfo.getIdReg().isEmpty()))) {
					currentMenuItemInfo = mInfo;
					break;
				}
			}
		}
		if(currentMenuItemInfo == null){
			currentMenuItemInfo = new MenuItemInfo(itemReg);
			//actually no need to add it? since all accesses have already been processed prior
			//menuItemInfos.add(currentMenuItemInfo); //or we could add later
		}

		String text = "";
		String textReg = helpMethods.getParameterOfInvokeStmt(invokeExpr, numArgs - 1);
		if(checkMethods.checkIfValueIsID(textReg)){
			text = Content.getInstance().getStringValueFromStringId(textReg);
		}
		else if(dynStrings != null){
			if(dynStrings.containsKey(menuItemReg))
				text = dynStrings.get(menuItemReg);
		}
		//TODO properly deal with dyn strings for interprocedural calls
		//logger.debug("Adding dyn menu item {} {} {}", menuItemReg, menuItemId, text);
		currentMenuItemInfo.setIdReg(menuItemReg);
		currentMenuItemInfo.setId(menuItemId);
		currentMenuItemInfo.setText(text);
		currentMenuItemInfo.setParentMenu(reg);
		currentMenuInfo.addDynMenuItem(menuItemReg, currentMenuItemInfo);
		if(dynStrings == null){
			//issue
			logger.error("Set of dynamic strings is null at {}", getCurrentSootMethod());
			currentMenuItemInfo.addTextReg(textReg);
			if(!checkMethods.checkIfValueIsVariable(textReg)){
				if(checkMethods.checkIfValueIsID(textReg)){
					textReg = Content.getInstance().getStringValueFromStringId(textReg);
				}
				else textReg = textReg.replace("\"", "");
				currentMenuItemInfo.setText(textReg);
			}
		}

		//Ignore text resolution for now
		//currentMenuInfo.addDynMenuItemTextReg(titleReg, "") //we could at least put the field name/tag
		//when do we reset it ? //should only add after a return I guess
	}

	
	public void caseAssignStmt(final AssignStmt stmt){
		if(Thread.currentThread().isInterrupted()){
			return;
		}

		processIntents(stmt);

		//System.out.println(stmt);
		Set<Info> toAddInfos = new LinkedHashSet<>();
		Set<Info> toRemoveInfos = new LinkedHashSet<>();

		if (stmt.containsInvokeExpr()){
			InvokeExpr invokeExpr = stmt.getInvokeExpr();
			String declaringClass = invokeExpr.getMethod().getDeclaringClass().getName();
			String signature = helpMethods.getSignatureOfInvokeExpr(invokeExpr);
			if (helpMethods.getSignatureOfInvokeExpr(invokeExpr).startsWith("<android.view.Menu: android.view.MenuItem add(")
					|| helpMethods.getSignatureOfInvokeExpr(invokeExpr).startsWith("<android.view.ContextMenu: android.view.MenuItem add("))
				handleDynamicMenuItems(invokeExpr, helpMethods.getLeftRegOfAssignStmt(stmt));
			else if (helpMethods.getSignatureOfInvokeExpr(invokeExpr).startsWith("<android.view.MenuItem: android.view.MenuItem setIntent(android.content.Intent)")){
				//add intent info the list of intents
				//need be mapped to a given menu item index
				handleMenuItemSetIntent(invokeExpr);
			}
			else if(Helper.isClassInAppNameSpace(declaringClass) && invokeExpr.getMethod().hasActiveBody()){ //Could be a menu item declaration I guess
				boolean returnsMenuItem = signature.contains(": android.view.MenuItem ");
				if(returnsMenuItem)  //return type is MenuItem
					//Here we only deal with the case where we return a menu item
					//Should I check if it returns something in the menu package?
 						handleInterProcMenuCreation(invokeExpr);
			}
		}

		for (Info i : getResultInfos()){
			if(Thread.currentThread().isInterrupted()){
				return;
			}
			MenuInfo mInfo = (MenuInfo) i;
			
			String leftReg = helpMethods.getLeftRegOfAssignStmt(stmt);
			
			// searchedE = MenuContainer
			if (leftReg.equals(mInfo.getSearchedEReg())){
				//TODO ?
			}
			if (leftReg.equals(mInfo.getInflaterReg())){
				if (stmt.containsInvokeExpr() && 
						(helpMethods.getSignatureOfInvokeExpr(stmt.getInvokeExpr()).equals("<android.app.Activity: android.view.MenuInflater getMenuInflater()>") ||
						helpMethods.getSignatureOfInvokeExpr(stmt.getInvokeExpr()).equals("<android.support.v7.app.AppCompatActivity: android.view.MenuInflater getMenuInflater()>"))){
					mInfo.setInflaterReg("");
					String activityName = helpMethods.getCallerTypeOfInvokeStmt(stmt.getInvokeExpr());
					if (checkMethods.checkIfValueIsVariable(activityName)){
						mInfo.setActivityReg(helpMethods.getCallerOfInvokeStmt(stmt.getInvokeExpr()));
					}else{
						mInfo.setActivityReg("");
						mInfo.setActivityClassName(helpMethods.getCallerTypeOfInvokeStmt(stmt.getInvokeExpr()));
					}
				}else if( stmt.containsInvokeExpr()){
					mInfo.setInflaterReg("");
					if (!Helper.isClassInSystemPackage(helpMethods.getCallerTypeOfInvokeStmt(stmt.getInvokeExpr()))) {
						String posActName = stmt.getInvokeExpr().getMethod().getReturnType().toString();
						if (!Helper.isClassInSystemPackage(posActName)) {
							mInfo.setActivityClassName(posActName);
						}
					}
				}
			}
			if (leftReg.equals(mInfo.getLayoutIDReg())){
			
				if (stmt.containsInvokeExpr()){
					mInfo.setLayoutIDReg("");
					InvokeExpr invokeExpr = stmt.getInvokeExpr();
					 if (!Helper.isClassInSystemPackage(helpMethods.getCallerTypeOfInvokeStmt(invokeExpr))){
						 List<InterProcInfo> resList = interprocMethods2.findReturnValueInMethod2(stmt);
						 if (resList.size() > 0){
							 mInfo.setLayoutID(resList.get(0).getValueOfSearchedReg());
							 if (resList.size() > 1){
								for (int j = 1; j < resList.size() ; j++){
									InterProcInfo workingInfo = (InterProcInfo) resList.get(j);
									MenuInfo newInfo = (MenuInfo) mInfo.clone();
									toAddInfos.add(newInfo);
									newInfo.setLayoutID(workingInfo.getValueOfSearchedReg());
								}
							}
						}
					}
				}else{
					if (stmt.getRightOp() instanceof FieldRef){
						SootField f = ((FieldRef)stmt.getRightOp()).getField();
						if(previousFields.contains(f)){
							if(!previousFieldsForCurrentStmtSwitch.contains(f)){
								continue;
							}
						}else{
							previousFields.add(f);
							previousFieldsForCurrentStmtSwitch.add(f);
						}
						Set<FieldInfo> fInfos = interprocMethods2.findInitializationsOfTheField2(f, stmt, getCurrentSootMethod());
						if(fInfos.size() > 0){
							toRemoveInfos.add(mInfo);
							for (FieldInfo fInfo : fInfos){
								if(Thread.currentThread().isInterrupted()){
									return;
								}
								if(fInfo.value != null){
									MenuInfo newInfo = (MenuInfo) mInfo.clone();
									newInfo.setLayoutID(fInfo.value);
									newInfo.setLayoutIDReg("");
									toAddInfos.add(newInfo);
									continue;
								}else{
									if(fInfo.methodToStart != null && fInfo.methodToStart.method().hasActiveBody()){
										Unit workingUnit = fInfo.unitToStart;
										MenuInfo newInfo = new MenuInfo("", "", "", getCurrentSootMethod().getDeclaringClass().getName());
										newInfo.setLayoutIDReg(fInfo.register.getName());
										StmtSwitchForOptionMenus newStmtSwitch = new StmtSwitchForOptionMenus(newInfo, getCurrentSootMethod(), dynStrings);
										previousFields.forEach(x->newStmtSwitch.addPreviousField(x));
										iteratorHelper.runOverToFindSpecValuesBackwards(fInfo.methodToStart.method().getActiveBody(), workingUnit, newStmtSwitch);
										Set<Info> initValues = newStmtSwitch.getResultInfos();

										if(initValues.size() > 0) {
											List<Info> listInfo = initValues.stream().collect(Collectors.toList());
											if(listInfo.indexOf(newInfo) == -1){
												continue;
											}
											Info initInfo = listInfo.get(listInfo.indexOf(newInfo));
											MenuInfo newInfo2 = (MenuInfo) mInfo.clone();
											newInfo2.setLayoutID(((MenuInfo)initInfo).getLayoutID());
											newInfo2.setLayoutIDReg("");
											toAddInfos.add(newInfo2);
										}
									}
								}
							}
						}else{
							mInfo.setLayoutIDReg("");
							Helper.saveToStatisticalFile("Error ListenerSwitch: Doesn't find layoutIdReg in initializationOfField: " + stmt);
						}
					}else{
						mInfo.setLayoutIDReg("");
						String rightReg = helpMethods.getRightRegOfAssignStmt(stmt);
						if (checkMethods.checkIfValueIsID(rightReg)){
							mInfo.setLayoutID(rightReg);
						}else
							mInfo.setLayoutIDReg(rightReg);
					}
				}
			}
			if (leftReg.equals(mInfo.getActivityReg())){
				
				if (stmt.containsInvokeExpr()){
					mInfo.setActivityReg("");
					InvokeExpr invokeExpr = stmt.getInvokeExpr();
					 if (!Helper.isClassInSystemPackage(helpMethods.getCallerTypeOfInvokeStmt(invokeExpr))){
						 List<InterProcInfo> resList = interprocMethods2.findReturnValueInMethod2(stmt);
						 if (resList.size() > 0){
							 mInfo.setActivityClassName(resList.get(0).getClassOfSearchedReg());
							if (resList.size() > 1){
								for (int j = 1; j < resList.size(); j++){
									InterProcInfo workingInfo = (InterProcInfo) resList.get(j);
									MenuInfo newInfo = (MenuInfo) mInfo.clone();
									toAddInfos.add(newInfo);
									newInfo.setActivityClassName(workingInfo.getClassOfSearchedReg());
								}
							}
						}
					}
				}else{
					if(stmt.getRightOp() instanceof FieldRef){
						SootField f = ((FieldRef)stmt.getRightOp()).getField();
						if(previousFields.contains(f)){
							if(!previousFieldsForCurrentStmtSwitch.contains(f)){
								continue;
							}
						}else{
							previousFields.add(f);
							previousFieldsForCurrentStmtSwitch.add(f);
						}
						Set<FieldInfo> fInfos = interprocMethods2.findInitializationsOfTheField2(f, stmt, getCurrentSootMethod());
						if(fInfos.size() > 0){
							toRemoveInfos.add(mInfo);
							for (FieldInfo fInfo : fInfos){
								if(Thread.currentThread().isInterrupted()){
									return;
								}
								if(fInfo.methodToStart != null && fInfo.methodToStart.method().hasActiveBody()){
									Unit workingUnit = fInfo.unitToStart;
									MenuInfo newInfo = new MenuInfo("", "", "", getCurrentSootMethod().getDeclaringClass().getName());
									newInfo.setActivityReg(fInfo.register.getName());
									StmtSwitchForOptionMenus newStmtSwitch = new StmtSwitchForOptionMenus(newInfo, getCurrentSootMethod(), dynStrings);
									previousFields.forEach(x->newStmtSwitch.addPreviousField(x));
									iteratorHelper.runOverToFindSpecValuesBackwards(fInfo.methodToStart.method().getActiveBody(), workingUnit, newStmtSwitch);
									Set<Info> initValues = newStmtSwitch.getResultInfos();

									if(initValues.size() > 0) {
										List<Info> listInfo = initValues.stream().collect(Collectors.toList());
										if(listInfo.indexOf(newInfo) == -1){
											continue;
										}
										Info initInfo = listInfo.get(listInfo.indexOf(newInfo));
										MenuInfo newInfo2 = (MenuInfo) mInfo.clone();
										newInfo2.setActivityClassName(((MenuInfo)initInfo).getActivityClassName());
										newInfo2.setActivityReg("");
										toAddInfos.add(newInfo2);
									}
								}
							}
						}
					}else{
						mInfo.setActivityReg("");
						String rightReg = helpMethods.getRightRegOfAssignStmt(stmt);
						if (stmt.getRightOp() instanceof NewExpr){
							mInfo.setActivityClassName(helpMethods.getTypeOfRightRegOfAssignStmt(stmt));
						}else{
							mInfo.setActivityReg(rightReg);
						}
					}
				}
			}
			else if(mInfo.getDynMenuItemsReg().contains(leftReg)){
				//r4 = smth()
				//menu.add(0,r4,0,r5)
				if (stmt.containsInvokeExpr()){
					String text = mInfo.getDynMenuItemText(leftReg);
					mInfo.removeDynMenuItem(leftReg);
					InvokeExpr invokeExpr = stmt.getInvokeExpr();
					if (!Helper.isClassInSystemPackage(helpMethods.getCallerTypeOfInvokeStmt(invokeExpr))){
						List<InterProcInfo> resList = interprocMethods2.findReturnValueInMethod2(stmt);
						if (resList.size() > 0){
							String id = resList.get(0).getValueOfSearchedReg();
							mInfo.addDynMenuItem(id, Integer.parseInt(id), text);
							if (resList.size() > 1){
								for (int j = 1; j < resList.size() ; j++){
									InterProcInfo workingInfo = (InterProcInfo) resList.get(j);
									MenuInfo newInfo = (MenuInfo) mInfo.clone();
									toAddInfos.add(newInfo);
									String value = workingInfo.getValueOfSearchedReg();
									newInfo.addDynMenuItem(value, Integer.parseInt(value), text);
								}
							}
						}
						else{
							//Couldn't resolve the value
							//Check if enum
							if(invokeExpr instanceof InstanceInvokeExpr){
								Value caller = ((InstanceInvokeExpr)invokeExpr).getBase();
								//if(Modifier.isEnum(caller));
							}

						}
					}

				}else{
					if (stmt.getRightOp() instanceof FieldRef){
						SootField f =((FieldRef)stmt.getRightOp()).getField();
						if(previousFields.contains(f)){
							if(!previousFieldsForCurrentStmtSwitch.contains(f)){
								continue;
							}
						}
						else{
							Set<FieldInfo> fInfos = interprocMethods2.findInitializationsOfTheField2(f, stmt, getCurrentSootMethod());
							if(fInfos.size() > 0){
								toRemoveInfos.add(mInfo);
								for (FieldInfo fInfo : fInfos){
									if(Thread.currentThread().isInterrupted()){
										return;
									}
									if(fInfo.value != null){
										MenuInfo newInfo = (MenuInfo) mInfo.clone();
										String text = mInfo.getDynMenuItemText(leftReg);
										newInfo.removeDynMenuItem(leftReg);
										newInfo.addDynMenuItem(fInfo.value, Integer.parseInt(fInfo.value), text);
										toAddInfos.add(newInfo);
										continue;
									}
									else{
										if(fInfo.methodToStart != null && fInfo.methodToStart.method().hasActiveBody()){
											Unit workingUnit = fInfo.unitToStart;
											MenuInfo newInfo = new MenuInfo("", getCurrentSootMethod().getDeclaringClass().getName());
											String text = newInfo.getDynMenuItemText(leftReg);
											newInfo.removeDynMenuItem(leftReg);
											newInfo.addDynMenuItem(fInfo.register.getName(), null, text);
											StmtSwitchForOptionMenus newStmtSwitch = new StmtSwitchForOptionMenus(newInfo, getCurrentSootMethod(), dynStrings);
											previousFields.forEach( x -> newStmtSwitch.addPreviousField(x));
											iteratorHelper.runOverToFindSpecValuesBackwards(fInfo.methodToStart.method().getActiveBody(), workingUnit, newStmtSwitch);
											Set<Info> initValues = newStmtSwitch.getResultInfos();

											if(initValues.size() > 0) {
												List<Info> listInfo = initValues.stream().collect(Collectors.toList());
												if(listInfo.indexOf(newInfo) == -1){
													continue;
												}
												Info initInfo = listInfo.get(listInfo.indexOf(newInfo));
												MenuInfo newInfo2 = (MenuInfo) mInfo.clone();
												MenuItemInfo newItem = ((MenuInfo)initInfo).getDynMenuItem(fInfo.register.getName());
												Integer newId = newItem.getId();
												//text = newItem.getValue();
												newInfo2.removeDynMenuItem(fInfo.register.getName());
												if(newId != null)
													newInfo2.addDynMenuItem(newId.toString(), newItem);
												toAddInfos.add(newInfo2);
											}
												
										}
									}
								}
							}
						}
					}
					else{
						String rightReg = helpMethods.getRightRegOfAssignStmt(stmt);
						Integer menuItemId = null;
						if(checkMethods.checkIfValueIsInteger(rightReg)){
							menuItemId = Integer.parseInt(rightReg);
						}
						String text = mInfo.getDynMenuItemText(leftReg);
						mInfo.removeDynMenuItem(leftReg);
						mInfo.addDynMenuItem(rightReg, menuItemId, text);
					}
				}
			}
			//TODO case where we return a menuItem
			//$r4 = getMenuItem()
			//NVm only way to create a menu item is to use menu.add(), so necessarily menu is accessed before menuItem
			
		}		
		if (toRemoveInfos.size() > 0){
			removeAllFromResultInfos(toRemoveInfos);
		}
		addAllToResultInfo(toAddInfos);
	}
	
	
	@Override
	public void defaultCase(Object o){
		//logger.debug("Current statement: default case {}", o);
	}

	public boolean run(){
		boolean allValuesFound = super.run();
		return allValuesFound;
	}


	/*private void processIntents(Stmt stmt) {
		for(AnalyseIntentSwitch intent : intents) {
			if(!intent.isDone()) {
				stmt.apply(intent);
			}
		}
	}*/
	
//	@Override
//	public boolean run(){
//		if (mInfo != null)
//			return mInfo.allValuesFound();
//		return false;
//	}
//	
//	public void init(){
//		super.init();
//		mInfo = null;
//		shouldBreak = false;
//		resultInfo = null;
//	}

	
}
