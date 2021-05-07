package st.cs.uni.saarland.de.searchDynDecStrings;

import org.apache.commons.lang3.StringUtils;
import soot.SootMethod;
import soot.jimple.*;
import st.cs.uni.saarland.de.helpClasses.Helper;
import st.cs.uni.saarland.de.helpClasses.Info;
import st.cs.uni.saarland.de.helpClasses.InterProcInfo;
import st.cs.uni.saarland.de.helpClasses.MyStmtSwitch;
import st.cs.uni.saarland.de.testApps.Content;

import java.util.*;

public class StmtSwitchForStringBuilder extends MyStmtSwitch {
	
	private String stringBuilderReg ="";
	private List<String> textSet = new ArrayList<String>();
	private String text = "";
	private Set<String> textVars = new HashSet<String>();
	private Info info;

	public StmtSwitchForStringBuilder(SootMethod currentSootMethod){
		super(currentSootMethod);
	}
	
	
	// not possible: analyse at the same time more then one StringBuilder, second would not be analysed
	// not possible: if a stringBuilder appends a string-variable which is declared by a stringbuilder (stringbuilder.toString()) 
	// 		-> in this case the variable is append to the info.textVars, so that we get the value in info.text but not correct appended (text1_text2#text3 instead of text1_text2_text3)
//	  	$r5 = new java.lang.StringBuilder;
//     ( specialinvoke $r5.<java.lang.StringBuilder: void <init>()>(); )
//      $r5 = virtualinvoke $r5.<java.lang.StringBuilder: java.lang.StringBuilder append(java.lang.String)>("case2:");
//      $r5 = virtualinvoke $r5.<java.lang.StringBuilder: java.lang.StringBuilder append(int)>(17039376);
//      $r6 = virtualinvoke $r5.<java.lang.StringBuilder: java.lang.String toString()>();
	
	public Info caseAssignStmt(AssignStmt stmt, Info pinfo ){
		this.info = pinfo;
		caseAssignStmt(stmt);
		return info;
	}
	
	
	public void caseAssignStmt(AssignStmt stmt){
		if(Thread.currentThread().isInterrupted()){
			return;
		}
		String leftReg = helpMethods.getLeftRegOfAssignStmt(stmt);
		if (stmt.containsInvokeExpr()){
			InvokeExpr invokeExpr = stmt.getInvokeExpr();
			String methodSignature = helpMethods.getSignatureOfInvokeExpr(invokeExpr);

			if (caseInvokeStmt(invokeExpr)){
				if (info.getTextReg().contains(leftReg)){
					info.removeTextReg(leftReg);
					info.addTextReg(stringBuilderReg);
				}

			}else if ("<java.lang.StringBuilder: java.lang.String toString()>".equals(methodSignature)){
				((DynDecStringInfo) info).setProcessedStmtInStringBuilder(true);
				if (StringUtils.isBlank(stringBuilderReg)){
					stringBuilderReg = helpMethods.getCallerOfInvokeStmt(invokeExpr);
				}
				if (info.getTextReg().contains(leftReg)){
					info.removeTextReg(leftReg);
					info.addTextReg(stringBuilderReg);
				}
				if (textVars.contains(leftReg)){
					textVars.remove(leftReg);
					textVars.add(stringBuilderReg);
				}
			}else if (textVars.equals(leftReg)){
				((DynDecStringInfo) info).setProcessedStmtInStringBuilder(true);
				textVars.remove(leftReg);
				if (!Helper.isClassInSystemPackage(helpMethods.getCallerTypeOfInvokeStmt(invokeExpr))){

					List<InterProcInfo> resList = interprocMethods2.findReturnValueInMethod2(stmt);
					if (resList.size() > 0){
						String tmp = (((InterProcInfo)resList.get(0)).getValueOfSearchedReg());
						if (checkMethods.checkIfValueIsID(tmp)){
							tmp = Content.getInstance().getStringValueFromStringId(tmp);
						}

						// find all place holder that matches leftReg in the textSet and replace them by tmp
						StmtSwitchForStringBuilder.replaceAllPlaceHolders(leftReg, tmp, textSet);

						if (resList.size() > 1){
							for (int j = 1; j < resList.size() ; j++){
								InterProcInfo workingInfo = (InterProcInfo) resList.get(j);
								String tmp2 = (workingInfo.getValueOfSearchedReg());
								if (checkMethods.checkIfValueIsID(tmp2)){
									tmp = Content.getInstance().getStringValueFromStringId(tmp2);
									textSet.add(0,tmp2);
								}else{
									textSet.add(0,tmp2);
								}
							}
						}
					}
				}else if ((stmt.getRightOp() instanceof IntConstant)){
//					info.addText(helpMethods.getRightRegOfAssignStmt(stmt));
					String tmp = helpMethods.getRightRegOfAssignStmt(stmt);
					textSet.add(0,tmp);
				}else{
					String tmp = helpMethods.getRightRegOfAssignStmt(stmt);
					if (checkMethods.checkIfValueIsVariable(tmp)){
						textVars.add(tmp);
					}else{
//						info.addText(tmp);
						textSet.add(0,tmp);
					}
				}
			}

		}else{
			if (leftReg.equals(stringBuilderReg)){
				((DynDecStringInfo) info).setProcessedStmtInStringBuilder(true);
				if (stmt.getRightOp() instanceof NewExpr){
					stringBuilderReg = "";
					if (info.getTextReg().contains(leftReg)) {
						if (textVars.size() > 0) {
							DynDecStringInfo dynInfo = (DynDecStringInfo) info;
							dynInfo.setOldStringBuilderSet(textSet, textVars);
						} else {
							text = String.join("", textSet);
							info.addText(text);
						}
					}
					text = "";
					textSet = new ArrayList<String>();
					textVars = new HashSet<String>();
				}else{
					stringBuilderReg = helpMethods.getRightRegOfAssignStmt(stmt);
				}
			}else if (textVars.contains(leftReg)){
				((DynDecStringInfo) info).setProcessedStmtInStringBuilder(true);
				textVars.remove(leftReg);
				String tmp = helpMethods.getRightRegOfAssignStmt(stmt);
				if (checkMethods.checkIfValueIsID(tmp)){
					tmp = Content.getInstance().getStringValueFromStringId(tmp);
					// find all place holder that matches leftReg in the textSet and replace them by tmp
					StmtSwitchForStringBuilder.replaceAllPlaceHolders(leftReg, tmp, textSet);
				}else if (checkMethods.checkIfValueIsString(tmp)){
					// find all place holder that matches leftReg in the textSet and replace them by tmp
					StmtSwitchForStringBuilder.replaceAllPlaceHolders(leftReg, tmp.replace("\"", ""), textSet);
				}else{
					// find all place holder that matches leftReg in the textSet and replace them by the new place holder
					StmtSwitchForStringBuilder.replaceAllPlaceHolders(leftReg, tmp, textSet);
				}
			}
		}
		
	}

	public Info caseInvokeStmt(InvokeStmt stmt, Info pinfo){
		this.info = pinfo;
		caseInvokeStmt(stmt.getInvokeExpr());
		return info;
	}

	private boolean caseInvokeStmt(InvokeExpr invokeExpr) {
		String methodSignature = helpMethods.getSignatureOfInvokeExpr(invokeExpr);

		// TODO check that it always is the same info that you process
		// TODO $r5 = ..toString() ; setText($r5) from one info, not others and maybe from other stringbuilder. What to do?
		if (("<java.lang.StringBuilder: java.lang.StringBuilder append(int)>".equals(methodSignature)) || ("<java.lang.StringBuilder: java.lang.StringBuilder append(java.lang.String)>".equals(methodSignature))) {
			((DynDecStringInfo) info).setProcessedStmtInStringBuilder(true);
			String callerInvoke = helpMethods.getCallerOfInvokeStmt(invokeExpr);
			if (stringBuilderReg.equals(callerInvoke)) {
				String tmp = helpMethods.getParameterOfInvokeStmt(invokeExpr, 0);
				if (checkMethods.checkIfValueIsID(tmp)) {
					tmp = Content.getInstance().getStringValueFromStringId(tmp);
					textSet.add(0, tmp);
				} else if (checkMethods.checkIfValueIsVariable(tmp)) {
//						if (info.getTextReg().contains(leftReg)){
//							info.addTextReg(tmp);
//						}
					// add the reg as place holder for this string
					textSet.add(0, tmp);
					textVars.add(tmp);
				} else {
					textSet.add(0, tmp.replace("\"", ""));
				}
				// return true so that the assign stmt know that this stmt was processed
				return true;
			}
		}
		return false;
	}

	public boolean allValuesFound() {
		return stringBuilderReg.equals("");		
	}
	
	@Override
	public StmtSwitchForStringBuilder clone(){
		StmtSwitchForStringBuilder newSwitch = new StmtSwitchForStringBuilder(getCurrentSootMethod());
		newSwitch.stringBuilderReg = stringBuilderReg;
		newSwitch.text = text;
		List<String> newSetText = new ArrayList<String>();
		for (String a : textSet){
			newSetText.add(a);
		}
		newSwitch.textSet = newSetText;
		
		Set<String> newSetTextVar = new HashSet<String>();
		for (String a : textVars){
			newSetTextVar.add(a);
		}
		newSwitch.textVars = newSetTextVar;
		
		return newSwitch;
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime
				* result
				+ ((stringBuilderReg == null) ? 0 : stringBuilderReg.hashCode());
		result = prime * result + ((text == null) ? 0 : text.hashCode());
		result = prime * result + ((textSet == null) ? 0 : textSet.hashCode());
		result = prime * result
				+ ((textVars == null) ? 0 : textVars.hashCode());
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
		StmtSwitchForStringBuilder other = (StmtSwitchForStringBuilder) obj;
		if (stringBuilderReg == null) {
			if (other.stringBuilderReg != null)
				return false;
		} else if (!stringBuilderReg.equals(other.stringBuilderReg))
			return false;
		if (text == null) {
			if (other.text != null)
				return false;
		} else if (!text.equals(other.text))
			return false;
		if (textSet == null) {
			if (other.textSet != null)
				return false;
		} else if (!textSet.equals(other.textSet))
			return false;
		if (textVars == null) {
			if (other.textVars != null)
				return false;
		} else if (!textVars.equals(other.textVars))
			return false;
		return true;
	}


	@Override
	public String toString() {
		return "StmtSwitchForStringBuilder [stringBuilderReg="
				+ stringBuilderReg + ", textSet=" + textSet + ", text=" + text
				+ ", textVars=" + textVars + "]";
	}

	public static void replaceAllPlaceHolders(String placeHolder, String text, List<String> searchedList){
		Collections.replaceAll(searchedList, placeHolder, text);
	}
}
