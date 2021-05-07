package st.cs.uni.saarland.de.helpMethods;

import org.apache.commons.lang3.StringUtils;
import soot.SootField;
import soot.SootMethod;
import soot.jimple.*;
import st.cs.uni.saarland.de.entities.FieldInfo;
import st.cs.uni.saarland.de.helpClasses.Helper;
import st.cs.uni.saarland.de.helpClasses.Info;
import st.cs.uni.saarland.de.helpClasses.InterProcInfo;
import st.cs.uni.saarland.de.helpClasses.MyStmtSwitch;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class StmtSwitchForArrayAdapter extends MyStmtSwitch{

	private String stringArrayReg = "";
	protected String idOfStringOrText = "";
//	protected String text = "";
	protected String adapterReg = "";
	boolean isStringArray;
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
			int paramIndex = ((ParameterRef)stmt.getRightOp()).getIndex();
			List<Info> resList = interprocMethods2.findInReachableMethods2(paramIndex,  getCurrentSootMethod(), new ArrayList<SootMethod>());
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
		return info;
	}
	
	public Info caseInvokeStmt(final InvokeStmt stmt, Info info){
		if(Thread.currentThread().isInterrupted()){
			return null;
		}
		
		InvokeExpr invokeExpr = stmt.getInvokeExpr();
		String methodSignature = helpMethods.getSignatureOfInvokeExpr(invokeExpr);
		
//	    specialinvoke $r2.<android.widget.ArrayAdapter: void <init>(android.content.Context,int,int,java.lang.Object[])>($r0, 17367043, 16908308, $r4);
		if (methodSignature.equals("<android.widget.ArrayAdapter: void <init>(android.content.Context,int,int,java.lang.Object[])>") ||
				methodSignature.equals("<android.widget.ListAdapter: void <init>(android.content.Context,int,int,java.lang.Object[])>")){
			if (adapterReg.equals(helpMethods.getCallerOfInvokeStmt(invokeExpr))){
				stringArrayReg = helpMethods.getParameterOfInvokeStmt(invokeExpr, 3);
				isStringArray = true;
			}
		}else
			
//		specialinvoke $r8.<android.widget.ArrayAdapter: void <init>(android.content.Context,int,java.lang.Object[])>($r0, 2130903043, $r2);
		if (methodSignature.equals("<android.widget.ArrayAdapter: void <init>(android.content.Context,int,java.lang.Object[])>") ||
				methodSignature.equals("<android.widget.ListAdapter: void <init>(android.content.Context,int,java.lang.Object[])>")){
			if (adapterReg.equals(helpMethods.getCallerOfInvokeStmt(invokeExpr))){
				stringArrayReg = helpMethods.getParameterOfInvokeStmt(invokeExpr, 2);
				isStringArray = true;
			}
		}
//		info = refreshInfo(info);
		return info;
	}
	
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
			}else if (leftReg.equals(stringArrayReg)){
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
		}else{
			String rightReg = helpMethods.getRightRegOfAssignStmt(stmt);
//				r8 = new android.widget.ArrayAdapter;			
			if (leftReg.equals(adapterReg)){
				if (stmt.getRightOp() instanceof NewExpr){
					adapterReg = "";
				}
			}else
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
									StmtSwitchForArrayAdapter newStmtSwitch = new StmtSwitchForArrayAdapter("", getCurrentSootMethod());
									newStmtSwitch.stringArrayReg = fInfo.register.getName();
									previousFields.forEach(x->newStmtSwitch.addPreviousField(x));
									iteratorHelper.runOverToFindSpecValuesBackwards(fInfo.methodToStart.method().getActiveBody(), fInfo.unitToStart, newStmtSwitch);
									addIdOfStringOrText(newStmtSwitch.idOfStringOrText);
									refreshInfo(info);
								}
							}
						}
					}
				}
			}else if (leftReg.contains(stringArrayReg) && leftReg.contains("[") && leftReg.contains("]")){
				addIdOfStringOrText(rightReg.replace("\"", ""));
				info = refreshInfo(info);
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
	
	public boolean allValuesFound(){
		return !idOfStringOrText.equals("") && adapterReg.equals("");
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
		int result = super.hashCode();
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
		if (!super.equals(obj))
			return false;
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
