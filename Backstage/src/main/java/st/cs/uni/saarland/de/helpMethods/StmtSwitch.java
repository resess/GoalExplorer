package st.cs.uni.saarland.de.helpMethods;

import java.util.List;

import soot.Value;
import soot.ValueBox;
import soot.jimple.*;
import st.cs.uni.saarland.de.helpClasses.Helper;

public class StmtSwitch {

	private static final StmtSwitch stmtSwitch = new StmtSwitch();
	
	public static StmtSwitch getInstance(){
		 return stmtSwitch;
	 }

	// case: r8 = (android.view.View$OnClickListener) r6, result would be "android.view.View$OnClickListener"
	public String getTypeOfRightRegOfAssignStmt(final AssignStmt stmt){
		String res = stmt.getRightOpBox().getValue().getType().toString();
		return res;
	}
	
	public String getRightRegOfAssignStmt(final AssignStmt stmt){
		List<ValueBox> v = stmt.getRightOp().getUseBoxes();
		String exactRightReg = stmt.getRightOpBox().getValue().toString();
		// if the reg contains an array, the tool needs the exact string to determine the arrayposition
		if (exactRightReg.contains("["))
			return exactRightReg;
		String rightReg;
		if (v.size() > 0){
			rightReg = v.get(0).getValue().toString();
			ValueBox rightField = stmt.getRightOpBox();
			if (rightField.getValue().toString().contains("<")){
				rightReg = rightField.getValue().toString();
			}
		}else{
			rightReg = stmt.getRightOpBox().getValue().toString();
		}
		
		return rightReg;		
	}
	
	public String getLeftRegOfAssignStmt(final AssignStmt stmt){
		return stmt.getLeftOpBox().getValue().toString();
	}
	
	public String getLeftRegOfIdentityStmt(final IdentityStmt astmt){
		return astmt.getLeftOpBox().getValue().toString();
	}
	
	public String getRightClassTypeOfIdentityStmt(final IdentityStmt stmt){
		String type = stmt.getRightOp().getType().toString();
		return type;
	}
	
	public String getParameterTypeOfInvokeStmt(final InvokeExpr invokeExpr, int nrOfArgument){
//		List<ValueBox> box = invokeExpr.getUseBoxes();						
//		return box.get(nrOfArgument).getValue().getType().toString();
		return invokeExpr.getArg(nrOfArgument).getType().toString();
	}
	
	public String getParameterOfInvokeStmt(final InvokeExpr invokeExpr, int nrOfArgument){
		return invokeExpr.getArg(nrOfArgument).toString();
	}
	
	public String getMethodNameOfInvokeStmt(final InvokeExpr invokeExpr){
		return invokeExpr.getMethod().getName();
	}
	
	public String getDeclaringClassName(InvokeExpr invokeExpr){
		return invokeExpr.getMethod().getDeclaringClass().getName().toString();
	}

	public Value getCallerValueOfInvokeStmt(final InvokeExpr invokeExpr){
		if(!(invokeExpr instanceof InstanceInvokeExpr)){
			List<ValueBox> useBoxes = invokeExpr.getUseBoxes();
			if(useBoxes.size() > 0)
				return useBoxes.get(0).getValue();
			return null;
		}
		InstanceInvokeExpr iExpr = (InstanceInvokeExpr)invokeExpr;
		return iExpr.getBase();
	}
	
	public String getCallerOfInvokeStmt(final InvokeExpr invokeExpr){ //TODO check static invoke
		Value value = getCallerValueOfInvokeStmt(invokeExpr);
		return (value == null)?"": value.toString();
	}


	
	public String getCallerTypeOfInvokeStmt(InvokeExpr invokeExpr){
		Value value = getCallerValueOfInvokeStmt(invokeExpr);
		return (value == null)?"": value.getType().toString();
	}
	
	public String getSignatureOfInvokeExpr(InvokeExpr invokeExpr){
		return Helper.getSignatureOfSootMethod(invokeExpr.getMethod());
	}

	public String getCompleteRightSideOfAssignStmt(AssignStmt stmt){
		return stmt.getRightOp().toString();
	}
	
	public String getReturnRegOfReturnStmt(ReturnStmt stmt){
		return stmt.getOp().toString();
	}


}
