package st.cs.uni.saarland.de.dissolveSpecXMLTags;



import soot.SootMethod;
import soot.Scene;
import soot.Value;
import soot.Type;
import soot.jimple.AssignStmt;
import soot.jimple.IdentityStmt;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import st.cs.uni.saarland.de.helpClasses.Helper;
import st.cs.uni.saarland.de.helpClasses.Info;
import st.cs.uni.saarland.de.helpClasses.InterProcInfo;
import st.cs.uni.saarland.de.helpClasses.MyStmtSwitch;
import st.cs.uni.saarland.de.helpClasses.AndroidRIdValues;
import st.cs.uni.saarland.de.helpMethods.InterprocAnalysis2;
import st.cs.uni.saarland.de.helpMethods.StmtSwitchForArrayAdapter;
import st.cs.uni.saarland.de.helpMethods.StmtSwitchForCustomAdapter;
import st.cs.uni.saarland.de.searchDynDecStrings.DynDecStringInfo;
import st.cs.uni.saarland.de.searchListener.ListenerInfo;

import java.util.List;
import java.util.Map;


//TODO Rename to AdapterView instead of ListView


//TODO: for a spinner, first click on the button for the drop down to show up
//then onItemSelect (similar to menus basically)

public class StmtSwitchForSpinner extends MyStmtSwitch{

    public StmtSwitchForSpinner(SootMethod currentSootMethod) {
        super(currentSootMethod);
    }

    public StmtSwitchForSpinner(ListViewInfo listViewInfo, SootMethod currentSootMethod){
        super(listViewInfo, currentSootMethod);
    }

    public StmtSwitchForSpinner(SootMethod currentSootMethod, Map<String, String> dynStrings, Map<String, String> elementIds){
        super(currentSootMethod, dynStrings, elementIds);
    }


        public void caseIdentityStmt(IdentityStmt stmt){
            if(Thread.currentThread().isInterrupted()){
                return;
            }
            for (Info info: getResultInfos()) {
                if (Thread.currentThread().isInterrupted())
                    return;
                ListViewInfo lInfo = (ListViewInfo) info;
                //String leftReg = helpMethods.getLeftRegOfAssignStmt(stmt);
                if (lInfo.getAdapterSwitch() != null) {
                    Info newInfo = lInfo.getAdapterSwitch().caseIdentityStmt(stmt, lInfo);
                    //if(newInfo != null)
                        //updateResultInfos(lInfo, newInfo);
                }
            }
        }

        public void caseAssignStmt(AssignStmt stmt){
            if(Thread.currentThread().isInterrupted()){
                return;
            }
            for (Info info: getResultInfos()) {
                if (Thread.currentThread().isInterrupted())
                    return;
                ListViewInfo lInfo = (ListViewInfo) info;
                String leftReg = helpMethods.getLeftRegOfAssignStmt(stmt);
                if (lInfo.getSearchedEReg().equals(leftReg) && elementIds != null) {
                    lInfo.setEID(elementIds.get(leftReg));
                }
                if (lInfo.getAdapterSwitch() != null) {
                    Info newInfo = lInfo.getAdapterSwitch().caseAssignStmt(stmt, lInfo);
                    //if(newInfo != null)
                        //updateResultInfos(lInfo, newInfo);
                }
            }
        }

        //Deal with findViewById
        public void caseInvokeStmt(InvokeStmt stmt){
            if(Thread.currentThread().isInterrupted()){
                return;
            }
            InvokeExpr invokeExpr = stmt.getInvokeExpr();
            String methodSignature = helpMethods.getSignatureOfInvokeExpr(invokeExpr);
            String methodName = helpMethods.getMethodNameOfInvokeStmt(invokeExpr);
            String declaringSootClass = getCurrentSootMethod().getDeclaringClass().getName();
            //logger.debug("Current invoke expr {} {}", stmt, getCurrentSootMethod());
    //		methodSignature is : android.widget.ListView: setAdapter(android.widget.ListAdapter)

            //For array adapter, we use StmtSwitch, we store a list of text + id of the list view, (we'll create list_view_id + position for each element)
            //For cursor adapter, WE STORE id of the list view, and data is explicit
            //Otherwise, the text might be too difficult to figure out, instead we try to rely on positions,
            //We parse onListViewItemClick and we do a switch on the position basically (which also means we should create ui elements on the fly?)
            //or instead we use the listview for each of them, but we add the position as well or smth,
            //yeah, then a map from position to text I guess?
            //maybe we keep the parent id FOR THE LIST VIEW ELEMENT OR SMTH?
            //maybe only keep the listview
            //but then add multiple ui elements with listener for reachability analysis?
            if ((methodName.equals("setAdapter") || methodName.equals("setListAdapter")) && (invokeExpr.getArgCount() > 0)) {
                //TODO check if it's from ListActivity
                    String callerReg = helpMethods.getCallerOfInvokeStmt(invokeExpr);
                    //need to store the caller reg
                    //check if extends ListView or AdapterView type I guess
                    //get the class of the registration and check if subtype of AdapterView
                    Value arg = invokeExpr.getArg(0);
                    String typeOfParam = helpMethods.getParameterTypeOfInvokeStmt(invokeExpr, 0);
                    logger.debug("The type of the adapter for listview {} {}", typeOfParam, stmt );
                    if (typeOfParam.equals("android.widget.ArrayAdapter") || typeOfParam.equals("android.widget.ListAdapter")) {
                        ListViewInfo lInfo = null;
                        if(methodName.equals("setAdapter"))
                            lInfo = new ListViewInfo(callerReg, declaringSootClass, typeOfParam);
                        else {
                            lInfo = new ListViewInfo("", declaringSootClass, typeOfParam);
                            lInfo.setEID(Integer.toString(AndroidRIdValues.getAndroidID("list")));
                        }
                        //DynDecStringInfo searchedString = new DynDecStringInfo(helpMethods.getCallerOfInvokeStmt(invokeExpr), getCurrentSootMethod());
                        StmtSwitchForArrayAdapter arraySwitch = new StmtSwitchForArrayAdapter(helpMethods.getParameterOfInvokeStmt(invokeExpr, 0), getCurrentSootMethod());
                        lInfo.setAdapterSwitch(arraySwitch);
                        addToResultInfo(lInfo);
                        
                    }
                    else if(typeOfParam.equals("android.widget.SimpleCursorAdapter") || typeOfParam.equals("android.widget.CursorAdapter")){
                        ListViewInfo lInfo = null;
                        if(methodName.equals("setAdapter"))
                            lInfo = new ListViewInfo(callerReg, declaringSootClass, typeOfParam);
                        else {
                            lInfo = new ListViewInfo("", declaringSootClass, typeOfParam);
                            lInfo.setEID(Integer.toString(AndroidRIdValues.getAndroidID("list")));
                        }
                        //DynDecStringInfo searchedString = new DynDecStringInfo(helpMethods.getCallerOfInvokeStmt(invokeExpr), getCurrentSootMethod());
                        StmtSwitchForArrayAdapter arraySwitch = new StmtSwitchForArrayAdapter(helpMethods.getParameterOfInvokeStmt(invokeExpr, 0), getCurrentSootMethod());
                        lInfo.setAdapterSwitch(arraySwitch);
                        addToResultInfo(lInfo);

                    }  
                    else { //TODO SpinnerAdapter?
                        Type listAdapterType = Scene.v().getSootClassUnsafe("android.widget.ListAdapter").getType();
                        //here resolve possible types and only use default if types empty
                        if(Scene.v().getFastHierarchy().canStoreType(arg.getType(), listAdapterType)) {
                            if(typeOfParam.contains("$")){
                                logger.debug("Anonymous class ... {}",typeOfParam );
                                ListViewInfo lInfo = null;
                                if(methodName.equals("setAdapter"))
                                    lInfo = new ListViewInfo(callerReg, declaringSootClass, typeOfParam);
                                else {
                                    lInfo = new ListViewInfo("", declaringSootClass, typeOfParam);
                                    lInfo.setEID(Integer.toString(AndroidRIdValues.getAndroidID("list")));
                                }
                                //DynDecStringInfo searchedString = new DynDecStringInfo(helpMethods.getCallerOfInvokeStmt(invokeExpr), getCurrentSootMethod());
                                StmtSwitchForArrayAdapter arraySwitch = new StmtSwitchForArrayAdapter(helpMethods.getParameterOfInvokeStmt(invokeExpr, 0), getCurrentSootMethod());
                                lInfo.setAdapterSwitch(arraySwitch);
                                addToResultInfo(lInfo);
                            }
                            else { //setListAdapter(new BetterArrayAdapter(this, Arrays.asList(ACTIVITIES), false));
                                logger.debug("Custom adapter extending ListAdapter ... {}", typeOfParam);
                                ListViewInfo lInfo = null;
                                if(methodName.equals("setAdapter"))
                                    lInfo = new ListViewInfo(callerReg, declaringSootClass, typeOfParam);
                                else {
                                    lInfo = new ListViewInfo("", declaringSootClass, typeOfParam);
                                    lInfo.setEID(Integer.toString(AndroidRIdValues.getAndroidID("list")));
                                }
                                StmtSwitchForCustomAdapter customSwitch = new StmtSwitchForCustomAdapter(helpMethods.getParameterOfInvokeStmt(invokeExpr, 0), typeOfParam, getCurrentSootMethod());
                                lInfo.setAdapterSwitch(customSwitch);
                                addToResultInfo(lInfo);
                            }
                        } 
                        else logger.debug("Adapter type not supported");
                        //custom
                        //need to parse getView of the method
                        //case whe
                        //get the sootClass from the parameter type
                        //case where it extends ArrayAdapter
                        //parse the constructor
                        //look for super and identify the parameter which contains the text
                        //or we can assume we just grab any parameter that is an array of object or an arraylist maybe (for now)?

                            //in that case, we can probably apply the arrayswitch first
                            //then we need to parse findView, to update the view elements I guess?
                        //search for getView in that class
                        SootMethod getViewMethod;

                    }
                }
                for (Info info: getResultInfos()){
                    if(Thread.currentThread().isInterrupted())
                        return;
                    ListViewInfo lInfo = (ListViewInfo) info;
                    if(lInfo.getAdapterSwitch() != null){
                        lInfo = (ListViewInfo) lInfo.getAdapterSwitch().caseInvokeStmt(stmt, lInfo);
                    }
                }
        }

        public void defaultCase(Object o) {

        }

        public void updateResultInfos(Info oldInfo, Info newInfo){
            //removeFromResultInfo(oldInfo);
            addToResultInfo(newInfo);
            logger.debug("Updating list view results info with {} {}", newInfo, getResultInfos());
        }
}

