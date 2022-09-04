package st.cs.uni.saarland.de.dissolveSpecXMLTags;

import soot.Scene;
import soot.SootMethod;
import soot.Type;
import soot.Value;
import soot.jimple.AssignStmt;
import soot.jimple.IdentityStmt;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import st.cs.uni.saarland.de.entities.AdapterView;
import st.cs.uni.saarland.de.entities.ListView;
import st.cs.uni.saarland.de.helpClasses.AndroidRIdValues;
import st.cs.uni.saarland.de.helpClasses.Info;
import st.cs.uni.saarland.de.helpClasses.MyStmtSwitch;
import st.cs.uni.saarland.de.helpMethods.StmtSwitchForAdapter;
import st.cs.uni.saarland.de.helpMethods.StmtSwitchForArrayAdapter;
import st.cs.uni.saarland.de.helpMethods.StmtSwitchForCustomAdapter;

import java.util.Map;
import java.util.regex.Pattern;

public class StmtSwitchForAdapterView extends MyStmtSwitch {
    public StmtSwitchForAdapterView(SootMethod currentSootMethod) {
        super(currentSootMethod);
    }

    public StmtSwitchForAdapterView(AdapterViewInfo adapterViewInfo, SootMethod currentSootMethod){
        super(adapterViewInfo, currentSootMethod);
    }

    public StmtSwitchForAdapterView(SootMethod currentSootMethod, Map<String, String> dynStrings, Map<String, String> elementIds){
        super(currentSootMethod, dynStrings, elementIds);
    }

    public void caseIdentityStmt(IdentityStmt stmt){
        if(Thread.currentThread().isInterrupted())
            return;
        for (Info info: getResultInfos()) {
            if (Thread.currentThread().isInterrupted())
                return;
            AdapterViewInfo aInfo = (AdapterViewInfo) info;
            if(aInfo.getAdapterSwitch() != null){
                Info newInfo = aInfo.getAdapterSwitch().caseIdentityStmt(stmt, aInfo);
            }
        }
    }

    public void caseAssignStmt(AssignStmt stmt){
        if(Thread.currentThread().isInterrupted())
            return;
        for (Info info: getResultInfos()) {
            if (Thread.currentThread().isInterrupted())
                return;
            AdapterViewInfo aInfo = (AdapterViewInfo) info;
            String leftReg = helpMethods.getLeftRegOfAssignStmt(stmt);
            if (aInfo.getSearchedEReg().equals(leftReg) && elementIds != null){
                logger.debug("Mapping adapter view {} to id {}", leftReg, elementIds.get(leftReg));
                aInfo.setEID(elementIds.get(leftReg));
            }
            if (aInfo.getAdapterSwitch() != null) {
                Info newInfo = aInfo.getAdapterSwitch().caseAssignStmt(stmt, aInfo);
            }
        }
    }

    public void caseInvokeStmt(InvokeStmt stmt) {
        if(Thread.currentThread().isInterrupted())
            return;
        InvokeExpr invokeExpr = stmt.getInvokeExpr();
        String methodSignature = helpMethods.getSignatureOfInvokeExpr(invokeExpr),
                methodName = helpMethods.getMethodNameOfInvokeStmt(invokeExpr),
                declaringSootClass = getCurrentSootMethod().getDeclaringClass().getName();
        //logger.debug("Current invoke expr {} {}", stmt, getCurrentSootMethod());

        //		methodSignature is : android.widget.ListView: setAdapter(android.widget.ListAdapter)
        if ((methodName.equals("setAdapter") || methodName.equals("setListAdapter")) && (invokeExpr.getArgCount() > 0)) {
            StmtSwitchForAdapter stmtSwitch = null;
            AdapterViewInfo aInfo = null;

            String callerType = helpMethods.getCallerTypeOfInvokeStmt(invokeExpr);
            String callerReg = helpMethods.getCallerOfInvokeStmt(invokeExpr);
            String adapterViewType = "listview";
            Value arg = invokeExpr.getArg(0);
            String typeOfParam = helpMethods.getParameterTypeOfInvokeStmt(invokeExpr, 0);

            logger.debug("The type of the adapter for adapterview {} {}", typeOfParam, stmt );

            if(typeOfParam.equals("android.widget.ArrayAdapter") || typeOfParam.equals("android.widget.ListAdapter")) {
                stmtSwitch = new StmtSwitchForArrayAdapter(helpMethods.getParameterOfInvokeStmt(invokeExpr, 0), getCurrentSootMethod());
            }
            else if(typeOfParam.equals("android.widget.SimpleCursorAdapter") || typeOfParam.equals("android.widget.CursorAdapter")){
                //TODO rename
                stmtSwitch = new StmtSwitchForArrayAdapter(helpMethods.getParameterOfInvokeStmt(invokeExpr, 0), getCurrentSootMethod());
            }
            else {
                //Maybe it's enough to check the signature?
                Type listAdapterType = Scene.v().getSootClassUnsafe("android.widget.ListAdapter").getType();
                Type expListAdapterType = Scene.v().getSootClassUnsafe("android.widget.ExpandableListAdapter").getType();
                Type recyclerAdapterType = Scene.v().getSootClassUnsafe("android.support.v7.widget.RecyclerView.Adapter").getType();
                //Here resolve possible types and only use default if types empty
                if (Scene.v().getFastHierarchy().canStoreType(arg.getType(), listAdapterType)
                        || Scene.v().getFastHierarchy().canStoreType(arg.getType(), expListAdapterType)){
                    String regex = Pattern.quote(declaringSootClass)+"\\$[0-9].*";

                    if(typeOfParam.matches(regex)){
                        logger.debug("Anonymous class ... {}",typeOfParam );
                        stmtSwitch = new StmtSwitchForArrayAdapter(helpMethods.getParameterOfInvokeStmt(invokeExpr, 0), getCurrentSootMethod());
                    }
                    else{//setListAdapter(new BetterArrayAdapter(this, Arrays.asList(ACTIVITIES), false));
                        logger.debug("Custom adapter extending ListAdapter ... {}", typeOfParam);
                        stmtSwitch = new StmtSwitchForCustomAdapter(helpMethods.getParameterOfInvokeStmt(invokeExpr, 0), typeOfParam, getCurrentSootMethod());
                    }
                }
                else if(Scene.v().getFastHierarchy().canStoreType(arg.getType(), recyclerAdapterType)){
                    adapterViewType = "recyclerview";
                    /*if(typeOfParam.matches(regex)){
                        logger.debug("Anonymous class ... {}",typeOfParam );
                        stmtSwitch = new StmtSwitchForArrayAdapter(helpMethods.getParameterOfInvokeStmt(invokeExpr, 0), getCurrentSootMethod());
                    }
                    else{//setListAdapter(new BetterArrayAdapter(this, Arrays.asList(ACTIVITIES), false));*/
                        logger.debug("Custom adapter extending RecyclerView.Adapter ... {}", typeOfParam);
                        stmtSwitch = new StmtSwitchForCustomAdapter(helpMethods.getParameterOfInvokeStmt(invokeExpr, 0), typeOfParam, getCurrentSootMethod());
                    //}
                }
                //TODO recyclerview
                else logger.debug("Adapter type not supported {}",arg.getType());
            }


            //TODO check if it's from ListActivity
            if(stmtSwitch != null) {
                if (methodName.equals("setAdapter")) {
                    //TODO refine
                    if(methodSignature.contains("Spinner"))
                        adapterViewType = "spinner";
                    aInfo = new AdapterViewInfo(callerReg, declaringSootClass, typeOfParam, adapterViewType);
                } else {
                    aInfo = new AdapterViewInfo("", declaringSootClass, typeOfParam, "listview");
                    aInfo.setEID(Integer.toString(AndroidRIdValues.getAndroidID("list")));
                }
                aInfo.setAdapterSwitch(stmtSwitch);
                addToResultInfo(aInfo);
            }
        }

        for (Info info: getResultInfos()) {
            if (Thread.currentThread().isInterrupted())
                return;
            AdapterViewInfo aInfo = (AdapterViewInfo) info;
            if(aInfo.getAdapterSwitch() != null)
                aInfo = (AdapterViewInfo) aInfo.getAdapterSwitch().caseInvokeStmt(stmt, aInfo);
        }

    }

    @Override
    public void defaultCase(Object obj) {
        //super.defaultCase(obj);
    }

    public void updateResultInfos(Info oldInfo, Info newInfo){
        addToResultInfo(newInfo);
    }
}
