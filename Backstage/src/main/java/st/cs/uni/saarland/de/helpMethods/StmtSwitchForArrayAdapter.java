package st.cs.uni.saarland.de.helpMethods;

import org.apache.commons.lang3.StringUtils;
import soot.*;
import soot.jimple.*;
import soot.jimple.toolkits.callgraph.Edge;
import st.cs.uni.saarland.de.entities.FieldInfo;
import st.cs.uni.saarland.de.helpClasses.Helper;
import st.cs.uni.saarland.de.helpClasses.Info;
import st.cs.uni.saarland.de.helpClasses.InterProcInfo;
import st.cs.uni.saarland.de.helpClasses.MyStmtSwitch;
import st.cs.uni.saarland.de.testApps.Content;

import java.util.*;

public class StmtSwitchForArrayAdapter extends StmtSwitchForAdapter{

	protected String stringArrayReg = "";
	protected String listReg = "";
	protected String idOfStringOrText = "";
	protected String associatedLayoutReg = "";
	protected String associatedLayoutID = "";
//	protected String text = "";
	protected String adapterReg = "";
	protected boolean isStringArray;
	protected Set<String> dataToResolve = new LinkedHashSet<>();;
	protected Map<String, Integer> dataToResolveMap = new HashMap<>();
//	protected StmtSwitch helpMethods = StmtSwitch.newInstance();
//	protected InterprocAnalysis interprocMethods= InterprocAnalysis.newInstance();
//	protected CheckIfMethodsExisting checkMethods= CheckIfMethodsExisting.newInstance();
//	protected InterprocAnalysis2 interprocMethods2 = InterprocAnalysis2.newInstance();
//	protected Set<SootField> previousFields = new HashSet<SootField>();

	
	// TODO what if the parameter of ArrayAdapter is not a String Array....
	public StmtSwitchForArrayAdapter (String adapterRegister, SootMethod currentSootMethod){
		super(currentSootMethod);
		this.adapterReg = adapterRegister;
		isStringArray = false;
	}

	public StmtSwitchForArrayAdapter(SootMethod currentSootMethod){
		super(currentSootMethod);
	}
	
	public void caseAssignStmt(final AssignStmt stmt){
		if(Thread.currentThread().isInterrupted()){
			return;
		}
		caseAssignStmt(stmt, null);
	}
	
	public void caseInvokeStmt(final InvokeStmt stmt){
		if(Thread.currentThread().isInterrupted()){
			return;
		}
		caseInvokeStmt(stmt, null);
	}
	public void caseIdentityStmt(final IdentityStmt stmt){
		if(Thread.currentThread().isInterrupted()){
			return;
		}
		caseIdentityStmt(stmt, null);
	}
	
	public Info caseIdentityStmt(final IdentityStmt stmt, Info info){
		if(Thread.currentThread().isInterrupted()){
			return null;
		}
		String leftReg = helpMethods.getLeftRegOfIdentityStmt(stmt);
		
		if (leftReg.equals(stringArrayReg)){
			stringArrayReg = "";
			if(stmt.getRightOp() instanceof ParameterRef) {
				int paramIndex = ((ParameterRef) stmt.getRightOp()).getIndex();
				List<Info> resList = interprocMethods2.findInReachableMethods2(paramIndex, getCurrentSootMethod(), new ArrayList<SootMethod>());
				if (resList.size() > 0) {
					addIdOfStringOrText(((InterProcInfo) resList.get(0)).getValueOfSearchedReg());
					if (resList.size() > 1) {
						for (int j = 1; j < resList.size(); j++) {
							InterProcInfo workingInfo = (InterProcInfo) resList.get(j);
							addIdOfStringOrText(workingInfo.getValueOfSearchedReg());
						}
					}
					info = refreshInfo(info);
				}
			}
			
		}
		return info;
	}
	
	public Info caseInvokeStmt(final InvokeStmt stmt, Info info){
		if(Thread.currentThread().isInterrupted()){
			return null;
		}
		
		InvokeExpr invokeExpr = stmt.getInvokeExpr();
		String methodSignature = helpMethods.getSignatureOfInvokeExpr(invokeExpr);
		String callerReg = helpMethods.getCallerOfInvokeStmt(invokeExpr);
		/*if(methodSignature.contains("Adapter"))
			logger.debug("Found adapter construction: {}", stmt);*/
		if (adapterReg.equals(helpMethods.getCallerOfInvokeStmt(invokeExpr)) && methodSignature.contains("void <init>")){
	
	//	    specialinvoke $r2.<android.widget.ArrayAdapter: void <init>(android.content.Context,int,int,java.lang.Object[])>($r0, 17367043, 16908308, $r4);
			if (methodSignature.equals("<android.widget.ArrayAdapter: void <init>(android.content.Context,int,int,java.lang.Object[])>") ||
					methodSignature.equals("<android.widget.ListAdapter: void <init>(android.content.Context,int,int,java.lang.Object[])>")){
				
					stringArrayReg = helpMethods.getParameterOfInvokeStmt(invokeExpr, 3);
						associatedLayoutReg = helpMethods.getParameterOfInvokeStmt(invokeExpr,2 );
						if(invokeExpr.getArg(2) instanceof IntConstant){
							associatedLayoutID = stringArrayReg;
							info.setAssociatedLayoutIDForText(associatedLayoutID);
						}
						else{
							//TODO handle reg
							//Logger
						}
						isStringArray = true;
				}else
					
		//		specialinvoke $r8.<android.widget.ArrayAdapter: void <init>(android.content.Context,int,java.lang.Object[])>($r0, 2130903043, $r2);
				if (methodSignature.equals("<android.widget.ArrayAdapter: void <init>(android.content.Context,int,java.lang.Object[])>") ||
						methodSignature.equals("<android.widget.ListAdapter: void <init>(android.content.Context,int,java.lang.Object[])>")){
						stringArrayReg = helpMethods.getParameterOfInvokeStmt(invokeExpr, 2);
						isStringArray = true;
						associatedLayoutReg = helpMethods.getParameterOfInvokeStmt(invokeExpr,1 );
						if(invokeExpr.getArg(2) instanceof IntConstant){
							associatedLayoutID = stringArrayReg;
							info.setAssociatedLayoutIDForText(associatedLayoutID);
						}
						else{
							//TODO handle reg
							//Logger
						}
				}

				//	    specialinvoke $r2.<android.widget.ArrayAdapter: void <init>(android.content.Context,int,int,java.lang.Object[])>($r0, 17367043, 16908308, $r4);
			if (methodSignature.equals("<android.widget.ArrayAdapter: void <init>(android.content.Context,int,int,java.util.List)>") ||
					methodSignature.equals("<android.widget.ListAdapter: void <init>(android.content.Context,int,int,java.util.List)>")){

				listReg = helpMethods.getParameterOfInvokeStmt(invokeExpr, 3);
				associatedLayoutReg = helpMethods.getParameterOfInvokeStmt(invokeExpr,2 );
				if(invokeExpr.getArg(2) instanceof IntConstant){
					associatedLayoutID = stringArrayReg;
					info.setAssociatedLayoutIDForText(associatedLayoutID);
				}
				else{
					//TODO handle reg
					//Logger
				}
				//isStringArray = true;
			}else
				//		specialinvoke $r8.<android.widget.ArrayAdapter: void <init>(android.content.Context,int,java.lang.Object[])>($r0, 2130903043, $r2);

				if (methodSignature.equals("<android.widget.ArrayAdapter: void <init>(android.content.Context,int,java.util.List)>") ||
						methodSignature.equals("<android.widget.ListAdapter: void <init>(android.content.Context,int,java.util.List)>")){
					listReg = helpMethods.getParameterOfInvokeStmt(invokeExpr, 2);
					//isStringArray = true;
					associatedLayoutReg = helpMethods.getParameterOfInvokeStmt(invokeExpr,1 );
					if(invokeExpr.getArg(2) instanceof IntConstant){
						associatedLayoutID = stringArrayReg;
						info.setAssociatedLayoutIDForText(associatedLayoutID);
					}
					else{
						//TODO handle reg
						//Logger
					}
				}

				//TODO: for cursor adaptor, the array is actually the columns to be fetched from the database and the mapping to the ui elements? maybe we can use the ui elements instead?
				//<android.widget.SimpleCursorAdapter: void <init>(android.content.Context,int,android.database.Cursor,java.lang.String[],int[])>
				else if (methodSignature.equals("<android.widget.SimpleCursorAdapter: void <init>(android.content.Context,int,android.database.Cursor,java.lang.String[],int[])>") ||
				methodSignature.equals("<android.widget.SimpleCursorAdapter: void <init>(android.content.Context,int,android.database.Cursor,java.lang.String[],int[],int)>")){
					//logger.debug("Looking for adapter reg {}", adapterReg);
						//stringArrayReg = helpMethods.getParameterOfInvokeStmt(invokeExpr, 3);
						//todo deal with array of int
						//associatedLayoutReg = helpMethods.getParameterOfInvokeStmt(invokeExpr,2 );
						/*if(invokeExpr.getArg(2) instanceof IntConstant){
							associatedLayoutID = stringArrayReg;
							info.setAssociatedLayoutIDForText(associatedLayoutID);
						}
						else{
							//TODO handle reg
							//Logger
						}*/
						//logger.debug("Found the declaration of the adapter {}", stringArrayReg);
						
						//isStringArray = true;
				}
				else if(methodSignature.contains("$")){ //TODO double check this, might just be about inner classes
					if(methodSignature.contains("android.database.Cursor")){
						//Anonymous class extending SimpleCursorAdapter
						//stringArrayReg = helpMethods.getParameterOfInvokeStmt(invokeExpr, 4);
						//logger.debug("Found the declaration of the adapter {}", stringArrayReg);
						
						//isStringArray = true;
					}
					else{
						//Anonymous class extending Array adapter
						stringArrayReg = helpMethods.getParameterOfInvokeStmt(invokeExpr, invokeExpr.getArgCount() -1);
						//logger.debug("Found the declaration of the adapter {}", stringArrayReg);
						
						//isStringArray = true;
					}
				}
			}
		//specialinvoke $r3.<org.custom.Class: void <init>(param1, param2, ...)(..,..,...)>
		else if(dataToResolve.contains(callerReg)){
			if(methodSignature.contains("void <init>") ){ //constructor for a new class
				//logger.debug("Found a custom instantiation for list view item {}",invokeExpr);
				if(!Helper.isClassInSystemPackage(helpMethods.getCallerTypeOfInvokeStmt(invokeExpr))){
					//Need to get the toString method of that class
					String extractedText = getStringRepresentationOfObject(stmt, invokeExpr);
					if(!StringUtils.isBlank(extractedText)){
						dataToResolve.remove(callerReg);
						if(checkMethods.checkIfValueIsVariable(extractedText)){
							dataToResolve.add(extractedText);
						}
						else {
							//TODO replace quotes
							addIdOfStringOrText(extractedText.replace("\"",""));
							info = refreshInfo(info);
						}
					}
				}
				else{
					//logger.debug("Instanting a system class, to look into ...");
				}

			}

		}
		//		info = refreshInfo(info);
				return info;
		}

		//TODO properly parse class and usages
	private String getStringRepresentationOfObject(Stmt stmt, InvokeExpr expr){
		//Get the class type
		//Look at the toString method of the object if exists
		String callerType = helpMethods.getCallerTypeOfInvokeStmt(expr);
		String subSignature = "java.lang.String toString()";
		String stringRep = "";
		try{
			for(int i = 0; i< expr.getArgCount(); i++){
				String param = helpMethods.getParameterOfInvokeStmt(expr, i);
				String paramType = helpMethods.getParameterTypeOfInvokeStmt(expr, i);
				if(paramType.equals("java.lang.String")) {
					//Assume it's what we want
					if(!stringRep.isEmpty()){
						logger.warn("Already found a string representation {} for object, need proper analysis. Trying to replace with ",stringRep, param);
					}
					else stringRep += param;
				}
			}
			//TODO
			//SootClass callerClass = Scene.v().getSootClass(callerType);
			//SootMethod toStringMethod = callerClass.getMethod(subSignature);
			//Iterator<Edge> edgesOutOf = Scene.v().getCallGraph().edgesOutOf(stmt);
			//Analyze the toString method
			//Collect the field used in the toString method
			//Then go inside the constructor, check which parameter is assigned to field(s) of interest
			//Then look for the value of the field of interest

		}
		catch (Exception e){
			logger.error("Could not resolve class {} or method {}", callerType, subSignature);
			logger.error(e.toString());
		}
		finally {
			return stringRep;
		}
	}
	
	//TO DO case when array is not loaded from resources?
	//Case where it's a list?
	public Info caseAssignStmt(final AssignStmt stmt, Info info){
		if(Thread.currentThread().isInterrupted()){
			return null;
		}
		String leftReg = helpMethods.getLeftRegOfAssignStmt(stmt);
		
		if (stmt.containsInvokeExpr()){
			InvokeExpr invokeExpr = stmt.getInvokeExpr();
			String methodSignature = helpMethods.getSignatureOfInvokeExpr(invokeExpr);
			
//		    	 $r3 = staticinvoke <android.widget.ArrayAdapter: android.widget.ArrayAdapter createFromResource(android.content.Context,int,int)>(r17, 2130968576, 17367043);
			if (methodSignature.equals("<android.widget.ArrayAdapter: android.widget.ArrayAdapter createFromResource(android.content.Context,int,int)>")){
				if (leftReg.equals(adapterReg)){
					adapterReg = "";
					addIdOfStringOrText(helpMethods.getParameterOfInvokeStmt(invokeExpr, 1));
					info = refreshInfo(info);
				}
			}else
//				 $r4 = virtualinvoke $r6.<android.content.res.Resources: java.lang.String[] getStringArray(int)>(2130968576);
			if (methodSignature.equals("<android.content.res.Resources: java.lang.String[] getStringArray(int)>") ||
						methodSignature.equals("<android.content.res.Resources: java.lang.String[] getTextArray(int)>")){
				if (stringArrayReg.equals(leftReg)){
					stringArrayReg = "";
					addIdOfStringOrText(helpMethods.getParameterOfInvokeStmt(invokeExpr, 0));
					info = refreshInfo(info);
				}
			}
			else if (leftReg.equals(listReg)){
				listReg = "";
				if(methodSignature.equals("<java.util.Arrays: java.util.List asList(java.lang.Object[])>")){
					//Extract the original array
					stringArrayReg = helpMethods.getParameterOfInvokeStmt(invokeExpr,0);
					isStringArray = true;
				}
				//case of add to list?
				else if (!Helper.isClassInSystemPackage(helpMethods.getCallerTypeOfInvokeStmt(invokeExpr))){
					List<InterProcInfo> resList = interprocMethods2.findReturnValueInMethod2(stmt);
					if (resList.size() > 0){
						addIdOfStringOrText(((InterProcInfo)resList.get(0)).getValueOfSearchedReg());
						if (resList.size() > 1){
							for (int j = 1; j < resList.size() ; j++){
								InterProcInfo workingInfo = (InterProcInfo) resList.get(j);
								addIdOfStringOrText(workingInfo.getValueOfSearchedReg());
							}
						}
						info = refreshInfo(info);
					}
				}
			}
			else if (leftReg.equals(stringArrayReg)){
				stringArrayReg = "";
				if (!Helper.isClassInSystemPackage(helpMethods.getCallerTypeOfInvokeStmt(invokeExpr))){
					List<InterProcInfo> resList = interprocMethods2.findReturnValueInMethod2(stmt);
					if (resList.size() > 0){
						addIdOfStringOrText(((InterProcInfo)resList.get(0)).getValueOfSearchedReg());
						if (resList.size() > 1){
							for (int j = 1; j < resList.size() ; j++){
								InterProcInfo workingInfo = (InterProcInfo) resList.get(j);
								addIdOfStringOrText(workingInfo.getValueOfSearchedReg());
							}
						}
						info = refreshInfo(info);
					}
				}
			}
			else if(dataToResolve.contains(leftReg)){
				dataToResolve.remove(leftReg);
				if(invokeExpr.getMethod().getSubSignature().startsWith("java.lang.String getString(int")){
					//$r8 = getString(...)
					String id = helpMethods.getParameterOfInvokeStmt(invokeExpr, 0);
					if(checkMethods.checkIfValueIsID(id)) {
						String text = Content.getInstance().getStringValueFromStringId(id);
						addIdOfStringOrText(text.replace("\"", ""));
						info = refreshInfo(info);
					}
					else {
						logger.error("Case not handled {}", stmt);
					}
				}
			}
		}else{
			//logger.debug("Value to assign {} {}", stringArrayReg, stmt);
			String rightReg = helpMethods.getRightRegOfAssignStmt(stmt);
//				r8 = new android.widget.ArrayAdapter;			
			if (leftReg.equals(adapterReg)){
				if (stmt.getRightOp() instanceof NewExpr){
					adapterReg = "";
				}
			}
			else if(dataToResolve.contains(leftReg)){
				dataToResolve.remove(leftReg);
				if(checkMethods.checkIfValueIsVariable(rightReg)){
					dataToResolve.add(rightReg); //TODO order is lost here
				}
				else{
					//Assume it's a constant
					addIdOfStringOrText(rightReg.replace("\"", ""));
					info = refreshInfo(info);
				}
			}
			else if(!stringArrayReg.isEmpty()){
	//			$r3 = newarray (java.lang.String)[3];
	//	        $r3[0] = "Line1";
	//	        $r3[1] = "Line2";
				if (leftReg.equals(stringArrayReg)){
					stringArrayReg = "";
					if (rightReg.contains("newarray")){
						stringArrayReg = "";
					}else if (stmt.getRightOp() instanceof FieldRef){
						SootField f = ((FieldRef)stmt.getRightOp()).getField();
						if(previousFields.contains(f)){
							if(!previousFieldsForCurrentStmtSwitch.contains(f)){
								return null;
							}
						}else{
							previousFields.add(f);
							previousFieldsForCurrentStmtSwitch.add(f);
						}
						Set<FieldInfo> fInfos = interprocMethods2.findInitializationsOfTheField2(f, stmt, getCurrentSootMethod());
						if(fInfos.size() > 0){
							for (FieldInfo fInfo : fInfos){
								if(Thread.currentThread().isInterrupted()){
									return info;
								}
								if(fInfo.value != null){
	//								String arrayText = "";
									if (checkMethods.checkIfValueIsID(fInfo.value)){
										addIdOfStringOrText(fInfo.value);
									}else{
										addIdOfStringOrText(fInfo.value);
									}
									refreshInfo(info);
									continue;
								}else{
									if(fInfo.methodToStart != null && fInfo.methodToStart.method().hasActiveBody()){
										//Should be safe to overriding
										StmtSwitchForArrayAdapter newStmtSwitch = new StmtSwitchForArrayAdapter("", getCurrentSootMethod());
										newStmtSwitch.stringArrayReg = fInfo.register.getName();
										newStmtSwitch.dataToResolve = this.dataToResolve;
										//TODO should the current method be overwritten to the start method? (if defined somewhere else)
										previousFields.forEach(x->newStmtSwitch.addPreviousField(x));
										iteratorHelper.runOverToFindSpecValuesBackwards(fInfo.methodToStart.method().getActiveBody(), fInfo.unitToStart, newStmtSwitch);
										addIdOfStringOrText(newStmtSwitch.idOfStringOrText);
										refreshInfoFull(info);
									}
								}
							}
						}
					}

				}else if (leftReg.contains(stringArrayReg) && leftReg.contains("[") && leftReg.contains("]")){
					//Here it assume it's necessarily a know string when it might not be
					//logger.debug("Looking at {} in method ", stmt, getCurrentSootMethod());
					if(checkMethods.checkIfValueIsVariable(rightReg)){
						//dataToResolveMap.put()
						dataToResolve.add(rightReg);
					}
					else {
						addIdOfStringOrText(rightReg.replace("\"", ""));
						//logger.debug("Adding the string {} {} to {}", idOfStringOrText, stringArrayReg, info);
						info = refreshInfo(info);
					}
				}
			}
			else if(!listReg.isEmpty()) {
				if(leftReg.equals(listReg)) {
					//$r3 = newarraylist (java.lang.String)[3];
					//Field access
				if (stmt.getRightOp() instanceof FieldRef){
					SootField f = ((FieldRef)stmt.getRightOp()).getField();
					if(previousFields.contains(f)){
						if(!previousFieldsForCurrentStmtSwitch.contains(f)){
							return null;
						}
					}else{
						previousFields.add(f);
						previousFieldsForCurrentStmtSwitch.add(f);
					}
					Set<FieldInfo> fInfos = interprocMethods2.findInitializationsOfTheField2(f, stmt, getCurrentSootMethod());
					if(fInfos.size() > 0){
						for (FieldInfo fInfo : fInfos){
							if(Thread.currentThread().isInterrupted()){
								return info;
							}
							if(fInfo.value != null){
								//								String arrayText = "";
								if (checkMethods.checkIfValueIsID(fInfo.value)){
									addIdOfStringOrText(fInfo.value);
								}else{
									addIdOfStringOrText(fInfo.value);
								}
								refreshInfo(info);
								continue;
							}else{
								if(fInfo.methodToStart != null && fInfo.methodToStart.method().hasActiveBody()){
									StmtSwitchForArrayAdapter newStmtSwitch = new StmtSwitchForArrayAdapter("", getCurrentSootMethod());
									newStmtSwitch.listReg = fInfo.register.getName();
									newStmtSwitch.dataToResolve = dataToResolve;
									previousFields.forEach(x->newStmtSwitch.addPreviousField(x));
									iteratorHelper.runOverToFindSpecValuesBackwards(fInfo.methodToStart.method().getActiveBody(), fInfo.unitToStart, newStmtSwitch);
									addIdOfStringOrText(newStmtSwitch.idOfStringOrText);
									refreshInfoFull(info);
								}
							}
						}
					}
				}
				}
			}
		}
		return info;
	}
	
	
	private Info refreshInfo(Info info){
		if (info != null){
			info.addText(idOfStringOrText);
			// reset idOfStringOrText after adding the text to info, otherwise there are duplicate strings
			idOfStringOrText = "";
			return info;
		}
		return null;
	}

	private Info refreshInfoFull(Info info){
		if(info != null){
			String[] texts = idOfStringOrText.split("#");
			for(String text: texts) info.addText(text);
			idOfStringOrText = "";
			return info;
		}
		return null;
	}
	
	public boolean allValuesFound(){
		return !idOfStringOrText.equals("") && adapterReg.equals("") && stringArrayReg.equals(""); //TODO and dataToResolveEmpty?
	}
	
	@Override
	public boolean run(){
		return !allValuesFound();
	}
	
//	private String getStringFromID(String stringID){
//		return Content.newInstance().getStringValueFromStringId(stringID);
//		
//	}
//	
//	private String getTextFromArrayID(String arrayID){
//		return Content.newInstance().getArrayValueFromArrayID(arrayID);
//	}
	
	public void addPreviousField(SootField f){
		previousFields.add(f);
	}
	
	private void addIdOfStringOrText(String newText){
		if (!StringUtils.isBlank(newText) && !newText.contains("$rs")){
			this.idOfStringOrText = idOfStringOrText + "#" + newText;
			if (idOfStringOrText.startsWith("#")){
				idOfStringOrText = idOfStringOrText.replaceFirst("#", "");
			}
		}
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((adapterReg == null) ? 0 : adapterReg.hashCode());
		result = prime
				* result
				+ ((idOfStringOrText == null) ? 0 : idOfStringOrText.hashCode());
		result = prime * result + (isStringArray ? 1231 : 1237);
		result = prime * result
				+ ((stringArrayReg == null) ? 0 : stringArrayReg.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		/*if (!super.equals(obj))
			return false;*/
		if (getClass() != obj.getClass())
			return false;
		StmtSwitchForArrayAdapter other = (StmtSwitchForArrayAdapter) obj;
		if (adapterReg == null) {
			if (other.adapterReg != null)
				return false;
		} else if (!adapterReg.equals(other.adapterReg))
			return false;
		if (idOfStringOrText == null) {
			if (other.idOfStringOrText != null)
				return false;
		} else if (!idOfStringOrText.equals(other.idOfStringOrText))
			return false;
		if (isStringArray != other.isStringArray)
			return false;
		if (stringArrayReg == null) {
			if (other.stringArrayReg != null)
				return false;
		} else if (!stringArrayReg.equals(other.stringArrayReg))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "StmtSwitchForArrayAdapter [stringArrayReg=" + stringArrayReg
				+ ", idOfStringOrText=" + idOfStringOrText + ", adapterReg="
				+ adapterReg + ", isStringArray=" + isStringArray + "]";
	}
	
}
