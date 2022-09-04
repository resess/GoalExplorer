package st.cs.uni.saarland.de.helpMethods;

import soot.SootMethod;
import soot.jimple.AssignStmt;
import soot.jimple.IdentityStmt;
import soot.jimple.InvokeStmt;
import soot.jimple.InvokeExpr;
import st.cs.uni.saarland.de.helpClasses.Info;

public class StmtSwitchForCustomAdapter extends StmtSwitchForArrayAdapter{
    private String typeOfAdapter;


    public StmtSwitchForCustomAdapter(SootMethod currentSootMethod) {
        super(currentSootMethod);
    }

    public StmtSwitchForCustomAdapter (String adapterRegister, String typeOfParam, SootMethod currentSootMethod){
		super(currentSootMethod);
		this.adapterReg = adapterRegister;
        this.typeOfAdapter = typeOfParam;
		isStringArray = false;
	}

    @Override
    public Info caseIdentityStmt(IdentityStmt stmt, Info info) {
        return super.caseIdentityStmt(stmt, info);
    }

    @Override
    public Info caseInvokeStmt(final InvokeStmt stmt, Info info){
		if(Thread.currentThread().isInterrupted()){
			return null;
		}

        //NEED to deal with case where
            //adapter takes a list instead of an array
            //content is not a string (find relevant class and toString method)
		
		InvokeExpr invokeExpr = stmt.getInvokeExpr();
		String methodSignature = helpMethods.getSignatureOfInvokeExpr(invokeExpr);
		/*if(methodSignature.contains("Adapter"))
			logger.debug("Found custom adapter construction: {}", stmt);*/
        if (adapterReg.equals(helpMethods.getCallerOfInvokeStmt(invokeExpr)) && methodSignature.contains("void <init>")){
        //setListAdapter(new BetterArrayAdapter(this, Arrays.asList(ACTIVITIES), false));
    //	    specialinvoke $r2.<org.custom.CustomArrayAdapter: void <init>(android.content.Context,int,int,java.lang.Object[])>($r0, 17367043, 16908308, $r4);
            if (methodSignature.startsWith("<"+typeOfAdapter+": void <init>(")){
                //check if there's a parameter of type array
                for(int i = 0; i<invokeExpr.getArgCount(); i++){
                    String typeOfParam = helpMethods.getParameterTypeOfInvokeStmt(invokeExpr,i);
                    if(typeOfParam.contains("[]")){
                        stringArrayReg = helpMethods.getParameterOfInvokeStmt(invokeExpr, i);
                        isStringArray = true;
                        break;
                    }
                    else if(typeOfParam.equals("java.util.List")){
                        listReg = helpMethods.getParameterOfInvokeStmt(invokeExpr, i);
                        break;
                    }
                }
            }
        }
        return info;
    }

    @Override
    public Info caseAssignStmt(AssignStmt stmt, Info info) {
        return super.caseAssignStmt(stmt, info);
    }
}
