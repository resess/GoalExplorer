package st.cs.uni.saarland.de.searchScreens;

import org.apache.commons.lang3.StringUtils;
import soot.SootField;
import soot.SootMethod;
import soot.jimple.*;
import st.cs.uni.saarland.de.entities.FieldInfo;
import st.cs.uni.saarland.de.helpClasses.Helper;
import st.cs.uni.saarland.de.helpClasses.InterProcInfo;
import st.cs.uni.saarland.de.helpClasses.MyStmtSwitchForResultLists;
import st.cs.uni.saarland.de.testApps.Content;

import java.util.*;
import java.util.Map.Entry;

public class StmtSwitchForLayoutInflater extends MyStmtSwitchForResultLists {

//	private Map<String, LayoutInfo> layouts = new HashMap<String, LayoutInfo>(); // Map<MainLayoutReg (searchedUiElement), LayoutInfo>
	protected Set<SootMethod> callStack = new HashSet<>();

	// this constructor is for analysing methods which know the main Layout (Reg), e.g. "View getView"
	public StmtSwitchForLayoutInflater(String mainLayoutReg, int id, boolean searchOnlyId, SootMethod m){
		super(m);
		LayoutInfo lay = null;
		if (searchOnlyId){
			lay = new LayoutInfo("", id);
			lay.setLayoutIDReg(mainLayoutReg);
		}else{
			lay = new LayoutInfo(mainLayoutReg, id);
		}
		putToResultedLayouts(lay.getID(), lay);
	}
	
	public StmtSwitchForLayoutInflater(Set<SootMethod> callStack2, LayoutInfo info, SootMethod pmethod){
		super(pmethod);
//		LayoutInfo lay = null;
//		if (searchOnlyId){
//			lay = new LayoutInfo("", id);
//			lay.setLayoutIDReg(mainLayoutReg);
//		}else{
//			lay = new LayoutInfo(mainLayoutReg, id);
//		}
//		layoutInfos.put(lay.getID(), lay);
		putToResultedLayouts(info.getID(), info);
//		layouts.put(mainLayoutReg, lay);
		callStack = callStack2;
	}
	
	// this constructor is for analysing methods which do not know the main Layout (Reg), e.g. "void onCreate" with setContentView(..)
	public StmtSwitchForLayoutInflater(SootMethod currentSootMethod){
		super(currentSootMethod);
//		layouts = new HashMap<String, LayoutInfo>();
	}
		
		
	// TODO search for Activities that are given as parameter (eg: ReachableMethodFromStaticClass2.apk)
	public void caseIdentityStmt(IdentityStmt stmt){
		if(Thread.currentThread().isInterrupted()){
			return;
		}
		String param = stmt.getLeftOp().toString();
		Map<Integer, LayoutInfo> toAddLayouts = new HashMap<>();
		int toRemoveLayout = -1;
		for (Entry<Integer, LayoutInfo> entry : getResultedLayouts().entrySet()){
			if(Thread.currentThread().isInterrupted()){
				return;
			}
			LayoutInfo info = entry.getValue();
			
			if((info.getLayoutIDReg().equals(param) && stmt.getRightOp() instanceof ParameterRef) ||
					(info.getLayoutReg().equals(param) && stmt.getRightOp() instanceof ParameterRef)){
				info.setLayoutIDReg("");
				info.setLayoutReg("");
				info.setCompletlyProcessed();
				toRemoveLayout = info.getID();

				List<SootMethod> localCallStack = new ArrayList<>(callStack);
				
				if ((callStack != null) && (getCurrentSootMethod() != null)){
					if(callStack.contains(getCurrentSootMethod())){
						return;
					}
					// FIXME ask if this stmt should be better after this if clause?
					callStack.add(getCurrentSootMethod());
				}else{
					callStack = new HashSet<>();
				}
				
				ParameterRef pRef = (ParameterRef)stmt.getRightOp();
				int argumentIndex = pRef.getIndex();
				Map<Integer, LayoutInfo> res = interprocMethods2.findInReachableMethodsForLayouts(argumentIndex,
						getCurrentSootMethod(), new HashSet<>(callStack));
				if (res.size() > 0){
					List<Integer> addedLayIds = new ArrayList<>();
					for (Entry<Integer, LayoutInfo> foundLay: res.entrySet()){
						LayoutInfo lay = foundLay.getValue();
						// only if isFragment is set, this layout is the layout we searched for (in comparison to eg addedLayouts)
						if (lay.isFragment()){
							String layoutID = lay.getLayoutID();
							if (!StringUtils.isBlank(layoutID)){
								LayoutInfo newLayout = info.cloneWithSpecId(info.getID());
								newLayout.setLayoutID(layoutID);
								toAddLayouts.put(newLayout.getID(), newLayout);
								addedLayIds.add(info.getID());
								
								for (Integer addedLayID: lay.getAddedLayouts()){
									LayoutInfo addedLay = res.get(addedLayID);
									if(addedLay == null){
										continue;
									}
									toAddLayouts.put(addedLay.getID(), addedLay);
									newLayout.addLayouts(addedLay.getID());
									addedLay.addRootLayout(newLayout.getID());
								}
							}
						}
					}
					for (Integer rootLayoutID: info.getRootLayouts()){
						LayoutInfo rootLayout = getResultedLayouts().get(rootLayoutID);
						if (rootLayout == null){
							Helper.saveToStatisticalFile("ERROR LayoutInflater: rootLayout null: " + rootLayoutID + "; stmt: " + stmt);
							break;
						}
						List<Integer> addedLayOfRootLay = rootLayout.getAddedLayouts();
						int indexOfLayout = -1;
						for (int i = 0; i < addedLayOfRootLay.size() ; i++){
							if (addedLayOfRootLay.get(i) == info.getID()){
								indexOfLayout = i;
								break;
							}
						}
						if (indexOfLayout != -1)
							addedLayOfRootLay.remove(indexOfLayout);
						else
							Helper.saveToStatisticalFile("Error: LayoutInflaterSwitch: addLayOfRootLay with this ID not found");
						addedLayOfRootLay.addAll(addedLayIds);									
					}
				}
				callStack.clear();
				callStack.addAll(localCallStack);
			}
			else if ((stmt.getRightOp() instanceof ThisRef) && (info.getLayoutReg().equals(param))){
				info.setLayoutReg("");
				info.setDynDecElement();
			}
		}
		if (toRemoveLayout != -1)
			removeFromResultedLayouts(toRemoveLayout);
		putAllToResultedLayouts(toAddLayouts);
	}
	
	
	public void caseAssignStmt(AssignStmt stmt){
		if(Thread.currentThread().isInterrupted()){
			return;
		}
//		 //System.out.println("assign: " +stmt);
		 
		
		// NOT Processed!:
//		 View 	inflate(XmlPullParser parser, ViewGroup root)
//		 Inflate a new view hierarchy from the specified xml node.
//		 View 	inflate(XmlPullParser parser, ViewGroup root, boolean attachToRoot)
//		 Inflate a new view hierarchy from the specified XML node.
		
		// processed:
//		 View 	inflate(int resource, ViewGroup root)
//		 Inflate a new view hierarchy from the specified xml resource.
//		 View 	inflate(int resource, ViewGroup root, boolean attachToRoot)
//		 Inflate a new view hierarchy from the specified xml resource. 
		
		
		// check if inside the assign stmt is an invoke, e.g.:
		 	// $r2 = virtualinvoke $r7.<android.view.LayoutInflater: android.view.View inflate(int,android.view.ViewGroup)>(2130903041, null);
			// $r4 = virtualinvoke $r1.<android.view.LayoutInflater: android.view.View inflate(int,android.view.ViewGroup,boolean)>(2130903045, $r2, 1);
		// note: layoutID is matched if it is an int in the procedure, not if it is declared outside!
//		for outside dec. of arrays
		// $r8 = $r0.<com.example.Testapp.MainActivity: int[] layMainId>;
//        $i1 = $r8[0];
		 
		String leftReg = helpMethods.getLeftRegOfAssignStmt(stmt);	
		// check if any stored Layout is affected by this stmt
//		if (layouts.containsKey(leftReg)){
		Map<Integer, LayoutInfo> toAddLayouts = new HashMap<>();
		List<Integer> toRemoveLayoutIDs = new ArrayList<>();
		for (final Entry<Integer, LayoutInfo> tmp : getResultedLayouts().entrySet()){
			if(Thread.currentThread().isInterrupted()){
				return;
			}
			LayoutInfo info = tmp.getValue();
			
			if (stmt.containsInvokeExpr()){
//				if (!info.getLayoutReg().equals(leftReg))
//					continue;
//				
//				info.setLayoutReg("");
				
				InvokeExpr invokeExpr = stmt.getInvokeExpr();			
				if (info.getLayoutReg().equals(leftReg)){
					
					if ("android.view.View inflate(int,android.view.ViewGroup)".equals(invokeExpr.getMethod().getSubSignature())){
						info.setLayoutReg("");
						// the ViewGroup doesn't matter since it only provides the LayoutParameter
						processSimpleInflaterStmt(invokeExpr, info);
						
					}else if ("android.view.View inflate(int,android.view.ViewGroup,boolean)".equals(invokeExpr.getMethod().getSubSignature())){
						info.setLayoutReg("");
						processSimpleInflaterStmt(invokeExpr, info);
											
						// check if the root View is included in the layout:
						// TODO test this method (explicit the boolean values)
						String attachedRoot = helpMethods.getParameterOfInvokeStmt(invokeExpr, 2);
						if ("1".equals(attachedRoot)){
							String rootView = helpMethods.getParameterOfInvokeStmt(invokeExpr, 1);
							// check if the layout is known
							LayoutInfo rootLayout = null;
//							for (LayoutInfo secLayout: layoutInfos){
							for (Entry<Integer, LayoutInfo> secEntry: getResultedLayouts().entrySet()){
								LayoutInfo secLayout = secEntry.getValue();
								if (secLayout.getSearchedEReg().equals(rootView)){
									rootLayout = secLayout;
									break;
								}
							}
							
							if (rootLayout == null){
								try{
									assert false: rootView.equals("");
								
									rootLayout = new LayoutInfo(rootView, Content.getNewUniqueID());
									toAddLayouts.put(rootLayout.getID(), rootLayout);
								}catch (AssertionError e){
									logger.error(Arrays.toString(e.getStackTrace()));
									continue;
								}
							}
							// in practice layout is inflated into the rootLayout and not other way around(like here)
							// but for practical purposes this is ignored (-> setContentView variable,..)
							if (rootLayout != null){
								info.addLayouts(rootLayout.getID());
								rootLayout.addRootLayout(info.getID());
							}
						}
					
					}else if ("android.view.View inflate(android.content.Context,int,android.view.ViewGroup)".equals(invokeExpr.getMethod().getSubSignature())){
						info.setLayoutReg("");
						String layoutID = helpMethods.getParameterOfInvokeStmt(invokeExpr, 1);
						
						// check if the layoutID is a number or a variable with the number
						if(invokeExpr.getArg(1) instanceof IntConstant){
							info.setLayoutID(layoutID);
							// the initial declaration of this layout was found and the ID is not a variable, so the layout needs no more processing
							info.setCompletlyProcessed();

						}else{
							info.setLayoutIDReg(layoutID);
						}
					}
				}
					

//						 for (Entry<String, LayoutInfo> entry : layouts.entrySet()){
//							 LayoutInfo lay = entry.getValue();
						 if (info.getLayoutReg().equals(leftReg)){
							 info.setLayoutReg("");
							 info.setCompletlyProcessed();
							 if (!Helper.isClassInSystemPackage(helpMethods.getCallerTypeOfInvokeStmt(invokeExpr))) {
								 toRemoveLayoutIDs.add(info.getID());
								 if (!this.callStack.contains(invokeExpr.getMethod())) {
									 Set<SootMethod> localCallStack = new HashSet<SootMethod>(callStack);
									 callStack.add(invokeExpr.getMethod());
									 Map<Integer, LayoutInfo> res = interprocMethods2.findReturnValueInMethodForLayouts(stmt, callStack);
									 callStack = localCallStack;
									 if (res.size() > 0) {
										 List<Integer> addedLayIds = new ArrayList<Integer>();
										 for (Entry<Integer, LayoutInfo> foundLay : res.entrySet()) {
											 LayoutInfo lay = foundLay.getValue();
											 // only if isFragment is set, this layout is the layout we searched for (in comparison to eg addedLayouts)
											 if (lay.isFragment()) {
												 String layoutID = lay.getLayoutID();
												 if (!StringUtils.isBlank(layoutID)) {
													 LayoutInfo newLayout = info.cloneWithSpecId(info.getID());
													 newLayout.setLayoutID(layoutID);
													 toAddLayouts.put(newLayout.getID(), newLayout);
													 addedLayIds.add(lay.getID());

													 for (Integer addedLayID : lay.getAddedLayouts()) {
														 LayoutInfo addedLay = res.get(addedLayID);
														 toAddLayouts.put(addedLay.getID(), addedLay);
														 newLayout.addLayouts(addedLay.getID());
														 addedLay.addRootLayout(newLayout.getID());
													 }
												 }
											 }
										 }
										 for (Integer rootLayoutID : info.getRootLayouts()) {
											 LayoutInfo rootLayout = getResultedLayouts().get(rootLayoutID);
											 if (rootLayout == null) {
												 Helper.saveToStatisticalFile("ERROR LayoutInflater: rootLayout null: " + rootLayoutID + "; stmt: " + stmt);
												 break;
											 }
											 List<Integer> addedLayOfRootLay = rootLayout.getAddedLayouts();
											 int indexOfLayout = -1;
											 for (int i = 0; i < addedLayOfRootLay.size(); i++) {
												 if (addedLayOfRootLay.get(i) == info.getID()) {
													 indexOfLayout = i;
													 break;
												 }
											 }
											 if (indexOfLayout != -1)
												 addedLayOfRootLay.remove(indexOfLayout);
											 else
												 Helper.saveToStatisticalFile("Error: LayoutInflaterSwitch: addLayOfRootLay with this ID not found");
											 addedLayOfRootLay.addAll(addedLayIds);
										 }
									 }
								 }
							 }
						 }else{
							 if (info.getLayoutIDReg().equals(leftReg)){
								 info.setLayoutIDReg("");
								 info.setCompletlyProcessed();
								 if (!Helper.isClassInSystemPackage(helpMethods.getCallerTypeOfInvokeStmt(invokeExpr))) {
									 if (!this.callStack.contains(invokeExpr.getMethod())) {
										 Set<SootMethod> localCallStack = new HashSet<SootMethod>(callStack);
										 callStack.add(invokeExpr.getMethod());
										 List<InterProcInfo> res = interprocMethods2.findReturnValueInMethod2(stmt, getCallerSootClass());
										 callStack = localCallStack;
										 if (res.size() > 0) {
											 if (!((InterProcInfo) res.get(0)).getValueOfSearchedReg().equals(""))
												 info.setLayoutID(((InterProcInfo) res.get(0)).getValueOfSearchedReg());

											 if (res.size() > 1) {
												 for (int i = 1; i < res.size(); i++) {
													 InterProcInfo possibleLayouts = (InterProcInfo) res.get(i);
													 if (!possibleLayouts.getValueOfSearchedReg().equals("")) {
														 LayoutInfo newLayout = info.clone();
														 newLayout.setLayoutID(possibleLayouts.getValueOfSearchedReg());
														 toAddLayouts.put(newLayout.getID(), newLayout);
													 }
												 }
											 }
										 }
									 }
								 }
							 }
						 }	

			}else{
				if (stmt.getRightOp() instanceof NewExpr){
					
					if (info.getLayoutReg().equals(leftReg)){
						
						// layout is now completely processed, because we found the initialization
						info.setCompletlyProcessed();
						info.setLayoutReg("");
						
						info.setLayoutID(stmt.getRightOp().getType().toString());
					}
					
				}else if (stmt.getRightOp() instanceof FieldRef){
						if (info.getLayoutIDReg().equals(leftReg)){
							info.setLayoutIDReg("");
							info.setCompletlyProcessed();
							processFieldInAssignStmt(stmt, info, toRemoveLayoutIDs, toAddLayouts, true);
						}
						if (info.getLayoutReg().equals(leftReg)){
							info.setLayoutReg("");
							info.setCompletlyProcessed();
							processFieldInAssignStmt(stmt, info, toRemoveLayoutIDs, toAddLayouts, false);
							
						}
				}else{
					// check also the reg, if it is an array $r8[0]
//					String layoutIfArray = "";
					// not int[]
//					if ((info.getLayoutIDReg().contains("["))){
//						int lastIndexOfBracket = info.getLayoutIDReg().lastIndexOf("[")+1;
//						layoutIfArray = info.getLayoutIDReg().substring(0, lastIndexOfBracket-1);
//					}
//					
//					// layoutIDReg is $r8[0], layoutIfArray is $r8
//					if (leftReg.equals(info.getLayoutIDReg()) || leftReg.equals(layoutIfArray)){
//						String rightReg = helpMethods.getRightRegOfAssignStmt(stmt);
//						
//						// check if the rightReg is a number or a variable with the number of the Layout
//						if (stmt.getRightOp() instanceof IntConstant){
//							info.setLayoutID(rightReg);
//							info.setLayoutIDReg("");
//						}else{
//							if(layoutIfArray.equals("")){
//								info.setLayoutIDReg(rightReg);
//							}else{
//								// check [] 
//								int start = info.getLayoutIDReg().lastIndexOf("[")+1;
//								int end = info.getLayoutIDReg().length()-1;
//								String arrayPosition = info.getLayoutIDReg().substring(start, end);
//								String arrayId = rightReg+"["+arrayPosition + "]";
//								info.setLayoutIDReg(arrayId);
//							}
//						}	
//					}
//					
					if (info.getLayoutReg().equals(leftReg)){
						info.setLayoutReg(helpMethods.getRightRegOfAssignStmt(stmt));
					}
					if (info.getLayoutIDReg().equals(leftReg)){
						info.setLayoutIDReg(helpMethods.getRightRegOfAssignStmt(stmt));
					}
				}
			}
		}
		
		for (Integer idsToRemove : toRemoveLayoutIDs){
			removeFromResultedLayouts(idsToRemove);
		}
		putAllToResultedLayouts(toAddLayouts);
	}

	// check all invoke statements if they match the methods the tool searches for,
		// e.g.: setContentView, addView
	public void caseInvokeStmt(InvokeStmt stmt) {
		if(Thread.currentThread().isInterrupted()){
			return;
		}
		InvokeExpr invokeExpr = stmt.getInvokeExpr();
		String method_name = helpMethods.getMethodNameOfInvokeStmt(invokeExpr);
//		String methodSignature = helpMethods.getSignatureOfInvokeExpr(invokeExpr);
		
//		System.out.println("invoke: " + stmt + " metSig: " + methodSignature);
		
		// case set ContentView: e.g.:
		// virtualinvoke $r0.<com.example.Testapp.MainActivity: void setContentView(android.view.View)>($r4);
//		if ("<android.app.Activity: void setContentView(android.view.View)>".equals(methodSignature)){		
		if (("setContentView".equals(method_name) || "addContentView".equals(method_name)) && invokeExpr.getArgCount() > 0){		
			String parameterReg = helpMethods.getParameterOfInvokeStmt(invokeExpr, 0);
			// TODO search for activity if it is a parameter, eg android.app.Activity, android.view.Window
			String activityName = helpMethods.getCallerTypeOfInvokeStmt(invokeExpr);
			String paramType = invokeExpr.getMethod().getParameterType(0).toString();
			String paramSpecialType = helpMethods.getParameterTypeOfInvokeStmt(invokeExpr, 0);
			//TODO serarch for parameter if it is a special type
			if (paramSpecialType.equals("android.view.View"))
				paramSpecialType = "";
			
			try{
				assert !activityName.isEmpty();
				assert !paramType.isEmpty();
				assert !parameterReg.isEmpty();
			
			// setContentView(2152358)
			if (invokeExpr.getArg(0) instanceof IntConstant){
				LayoutInfo newLayout = new LayoutInfo("", Content.getNewUniqueID());
				newLayout.setLayoutID(parameterReg);
				newLayout.setSetContentViewLayout();
				newLayout.setActivityNameOfView(activityName);
				newLayout.setCompletlyProcessed();
				putToResultedLayouts(newLayout.getID(), newLayout);
				
			// setContentView($r4)-> $r4 is Layout
			}else if ("int".equals(paramSpecialType)){
				LayoutInfo newLayout = new LayoutInfo("", Content.getNewUniqueID());
				newLayout.setLayoutIDReg(parameterReg);
				newLayout.setSetContentViewLayout();
				newLayout.setActivityNameOfView(activityName);
				putToResultedLayouts(newLayout.getID(), newLayout);
			}
			// a complete layout is the parameter
			else {
				LayoutInfo foundLayout = null;
				for (Entry<Integer, LayoutInfo> entry : getResultedLayouts().entrySet()){
					LayoutInfo info = entry.getValue();
					if (info.getLayoutReg().equals(parameterReg)){
						foundLayout = info;
						break;
					}
				}
				// check if it is a known layout
				if (foundLayout == null){
					// setContentView is called with a layout as variable
					foundLayout = new LayoutInfo(parameterReg, Content.getNewUniqueID());
					putToResultedLayouts(foundLayout.getID(), foundLayout);
				}
				if (foundLayout.getLayoutID().equals("")){
					// id is set to the type, if it is eg a dyn dec element
					foundLayout.setLayoutID(paramSpecialType);
				}
				foundLayout.setActivityNameOfView(activityName);
				foundLayout.setSetContentViewLayout();
				
			// setContentView($i0)-> $i0 is Id
			}
//			else{
//				LayoutInfo newLayout = new LayoutInfo("");
//				newLayout.setLayoutIDReg(parameterReg);
//				newLayout.setSetContentViewLayout();
//				newLayout.setActivityNameOfView(activityName);
//
//				// id is set to the type, if it is eg a dyn dec element
//				newLayout.setLayoutID(paramSpecialType);
//				layoutInfos.add(newLayout);
//			}
						
			}catch(AssertionError a ){
				a.printStackTrace();
				Helper.saveToStatisticalFile(a.getMessage());
			}
			
			
				
			
//		}else
//		// case set ContentView: e.g.:
//		// virtualinvoke $r0.<com.example.Testapp.MainActivity: void setContentView(android.view.View)>(2154555285);
//			if ("<android.app.Activity: void setContentView(int)>".equals(methodSignature)){
//				String parameterReg = helpMethods.getParameterOfInvokeStmt(invokeExpr, 0);
//				String activityName = helpMethods.getCallerTypeOfInvokeStmt(invokeExpr);
//				
//				if (invokeExpr.getArg(0) instanceof IntConstant){
//					LayoutInfo newLayout = new LayoutInfo("");
//					newLayout.setLayoutID(parameterReg);
//					newLayout.setSetContentViewLayout();
//					newLayout.setActivityNameOfView(activityName);
//					newLayout.setCompletlyProcessed();
//					layoutInfos.add(newLayout);
//				}else{
//					LayoutInfo newLayout = new LayoutInfo("");
//					newLayout.setLayoutIDReg(parameterReg);
//					newLayout.setSetContentViewLayout();
//					newLayout.setActivityNameOfView(activityName);
//					layoutInfos.add(newLayout);
//				}
				
//		}else
//			if ("<android.app.Activity: void addContentView(android.view.View,android.view.ViewGroup$LayoutParams)>".equals(methodSignature)){
//				String parameterReg = helpMethods.getParameterOfInvokeStmt(invokeExpr, 0);
//				String activityName = helpMethods.getCallerTypeOfInvokeStmt(invokeExpr);
//				
//				try{
//					assert false: activityName.equals("");
//					assert false: parameterReg.equals("");
//				
//
//				if (invokeExpr.getArg(0) instanceof IntConstant){
//					LayoutInfo newLayout = new LayoutInfo("");
//					newLayout.setLayoutID(parameterReg);
//					newLayout.setSetContentViewLayout();
//					newLayout.setActivityNameOfView(activityName);
//					newLayout.setCompletlyProcessed();
//					layoutInfos.add(newLayout);
//				}else{
//					LayoutInfo newLayout = new LayoutInfo("");
//					newLayout.setLayoutIDReg(parameterReg);
//					newLayout.setSetContentViewLayout();
//					newLayout.setActivityNameOfView(activityName);
//					layoutInfos.add(newLayout);
//				}
//				
//				
//				}catch(AssertionError a ){
//					logger.error(a.getStackTrace().toString());
//				}
//				
//				

		}else
		// case this is a call of addView: e.g.:
			// virtualinvoke $r4.<android.widget.LinearLayout: void addView(android.view.View,android.view.ViewGroup$LayoutParams)>($r9, $r5);
			// if checking methodSignature include RealtiveLayout, FrameLayout,...
			if (method_name.equals("addView")){
				String rootViewReg = helpMethods.getCallerOfInvokeStmt(invokeExpr);
				String addedViewReg = helpMethods.getParameterOfInvokeStmt(invokeExpr, 0);
				String rootViewType = helpMethods.getCallerTypeOfInvokeStmt(invokeExpr);
				String addedViewType = helpMethods.getParameterTypeOfInvokeStmt(invokeExpr, 0);

				// check if rootView is existing, if so extract it
				LayoutInfo rootViewLayout = null;
				for (Entry<Integer, LayoutInfo> entry : getResultedLayouts().entrySet()){
					LayoutInfo tmp = entry.getValue();
					if (tmp.getLayoutReg().equals(rootViewReg)){
						rootViewLayout = tmp;
						break;
					}
				}
				// if no rootView Layout is exitsting, create it
				if (null == rootViewLayout){
					rootViewLayout = new LayoutInfo(rootViewReg, Content.getNewUniqueID());
					putToResultedLayouts(rootViewLayout.getID(), rootViewLayout);
				}

				// check if addedView is existing, if so extract it
				LayoutInfo addedViewLayout = null;
				for (Entry<Integer, LayoutInfo> entry : getResultedLayouts().entrySet()){
					LayoutInfo tmp = entry.getValue();
					if (tmp.getLayoutReg().equals(addedViewReg)){
						addedViewLayout = tmp;
						break;
					}
				}
				// if no addedView Layout is exitsting, create it
				if (null == addedViewLayout){
					addedViewLayout = new LayoutInfo(addedViewReg, Content.getNewUniqueID());
					putToResultedLayouts(addedViewLayout.getID(), addedViewLayout);
				}

				// save the dependency between both layouts
				addedViewLayout.addRootLayout(rootViewLayout.getID());
				rootViewLayout.addLayouts(addedViewLayout.getID());


				// for later dyn def ui classes analysis
//				// set LayoutID to type in case these layouts are dyn dec.
//				// if they are not, the layoutID will be overwritten
//				if (!(rootViewType.contains("android.app.")
//						|| rootViewType.contains("android.view.")
//						|| rootViewType.contains("android.widget.")
//						|| rootViewType.contains("android.support.")
//						|| rootViewType.contains("android.webkit."))){
//					rootViewLayout.setLayoutID(rootViewType);
//				}
//
//				if (!(addedViewType.contains("android.app.")
//						|| addedViewType.contains("android.view.")
//						|| addedViewType.contains("android.widget.")
//						|| addedViewType.contains("android.support.")
//						|| addedViewType.contains("android.webkit."))){
//					addedViewLayout.setLayoutID(addedViewType);
//				}
		}
	}
	
	private void processSimpleInflaterStmt(InvokeExpr invokeExpr, LayoutInfo layout){
		if(Thread.currentThread().isInterrupted()){
			return;
		}

		// layoutID is always the 0th  parameter
		String layoutID = helpMethods.getParameterOfInvokeStmt(invokeExpr, 0);
		
		// check if the layoutID is a number or a variable with the number
		if(checkMethods.checkIfValueIsID(layoutID)){
			layout.setLayoutID(layoutID);
			// the initial declaration of this layout was found and the ID is not a variable, so the layout needs no more processing
			layout.setCompletlyProcessed();

		}else{
			layout.setLayoutIDReg(layoutID);
		}
	}
	
	public Map<Integer, LayoutInfo> getResults(){
		return getResultedLayouts();
	}
	
	private void processFieldInAssignStmt(AssignStmt stmt, LayoutInfo info, List<Integer> toRemoveLayoutIDs, Map<Integer, LayoutInfo> toAddLayouts, boolean searchLayoutID){
		if(Thread.currentThread().isInterrupted()){
			return;
		}
		SootField f = ((FieldRef)stmt.getRightOp()).getField();
		if(previousFields.contains(f)){
			if(!previousFieldsForCurrentStmtSwitch.contains(f)){
				return;
			}
		}else{
			previousFields.add(f);
			previousFieldsForCurrentStmtSwitch.add(f);
		}
		Set<FieldInfo> fInfos = interprocMethods2.findInitializationsOfTheField2(f, stmt, getCurrentSootMethod());
		if (fInfos.size() > 0){
			toRemoveLayoutIDs.add(info.getID());
		}else{
			Helper.saveToStatisticalFile("Error LayoutSwitch: Doesn't find searchedREg or searchedIDReg in initializationOfField: " + stmt);
		}
		for(FieldInfo fInfo : fInfos){
			if(Thread.currentThread().isInterrupted()){
				return;
			}
											
			if(fInfo.value != null){
				LayoutInfo newLayoutInfo = info.clone();
				newLayoutInfo.setLayoutID(fInfo.value);
				toAddLayouts.put(newLayoutInfo.getID(), newLayoutInfo);
			}else{
				if(fInfo.methodToStart != null && fInfo.methodToStart.method().hasActiveBody()){
					StmtSwitchForLayoutInflater newLayoutSwitch = new StmtSwitchForLayoutInflater(fInfo.register.getName(), info.getID(), searchLayoutID, fInfo.methodToStart.method());
					previousFields.forEach(newLayoutSwitch::addPreviousField);
					Set<SootMethod> localCallStack = new HashSet<>(callStack);
					newLayoutSwitch.setCallStack(callStack);
					iteratorHelper.runOverToFindSpecValuesBackwards(fInfo.methodToStart.method().getActiveBody(), fInfo.unitToStart, newLayoutSwitch);
					callStack = localCallStack;
					Map<Integer, LayoutInfo> initLayouts = newLayoutSwitch.getResults();
					
					if (initLayouts.entrySet().size() > 0){
						LayoutInfo newLayout = info.clone();
						toAddLayouts.put(newLayout.getID(), newLayout);
						LayoutInfo mainLayout = initLayouts.get(info.getID());
						
						// process the searched layout (main layout):
						if (mainLayout != null){
							newLayout.setLayoutID(mainLayout.getLayoutID());
							if (mainLayout.isFragment())
								newLayout.setFragment();
														
							// now process the added layouts to the main layouts: (could only be if more then one layout is given)
							if (initLayouts.size() > 1){
								if (mainLayout.getAddedLayouts().size() > 0){
									List<Integer> addedLayouts = mainLayout.getAddedLayouts();
									for (Integer addedLayID : addedLayouts){
										LayoutInfo addedLay = initLayouts.get(addedLayID);
										if (addedLay != null){
											toAddLayouts.put(addedLay.getID(), addedLay);
											newLayout.addLayouts(addedLay.getID());
											addedLay.addRootLayout(newLayout.getID());
										}else{
											Helper.saveToStatisticalFile("ERROR LayoutInflater 714: addedLayID was not found in layoutInfos, "+ addedLayID);
										}
									}
								}
							}
						}else{
							logger.error("LayoutInflater, processFieldInAssign: mainLayout was null");
						}
					}
				}
			}
		}
	}
	
	@Override
	public boolean run(){
		return !shouldBreak && !getResultedLayouts().entrySet().stream()
				.allMatch(x -> x.getValue().isCompletlyProcessed());
	}
	

	public Set<SootMethod> getCallStack() {
		return callStack;
	}
	
	public void setCallStack(Set<SootMethod> lastCallStack){
		if (callStack != null){
			callStack.addAll(lastCallStack);
		}else{
			callStack = lastCallStack;
		}
	
	}
}
