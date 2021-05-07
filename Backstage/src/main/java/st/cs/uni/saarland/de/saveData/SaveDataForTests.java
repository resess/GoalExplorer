package st.cs.uni.saarland.de.saveData;

import com.thoughtworks.xstream.XStream;
import st.cs.uni.saarland.de.entities.*;
import st.cs.uni.saarland.de.helpClasses.Helper;
import st.cs.uni.saarland.de.reachabilityAnalysis.ApiInfoForForward;
import st.cs.uni.saarland.de.reachabilityAnalysis.UiElement;
import st.cs.uni.saarland.de.testApps.AppController;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;

/**
 * Created by Isa on 19.12.2015.
 */
public class SaveDataForTests {

    private Application app;

    private static SaveDataForTests me = new SaveDataForTests();

    public static SaveDataForTests getInstance(){
        return me;
    }

    public void saveIDWithListenerAndText(Application app){
        this.app = app;
        List<UiElement> uiElementsWithListeners = extractListeners(app.getAllXMLLayoutFiles());
        saveResults(uiElementsWithListeners);

    }

    public void saveActivityToLayout(Application app) {
        this.app = app;
        XStream xStream = new XStream();
        //xStream.processAnnotations(UiElement.class);
        //xStream.processAnnotations(ApiInfoForForward.class);
        xStream.setMode(XStream.NO_REFERENCES);

        try (BufferedWriter bw = new BufferedWriter(
                new OutputStreamWriter(
                        new FileOutputStream(String.format("testApps" + File.separator + Helper.getApkName().replace(".apk", "") + File.separator + "outputForTestLayout.xml")), StandardCharsets.UTF_8))) {
            bw.append(xStream.toXML(app.getActivityToXMLLayoutFiles()));
            bw.append(xStream.toXML(app.getMergedLayoutFileIDs()));
            bw.append(xStream.toXML(app.getFragmentClassToLayout()));
        } catch (IOException exception) {
            Helper.saveToStatisticalFile(exception.getMessage());
        }

/*
        String list = "";

        for (Map.Entry<String, Set<String>> entry : app.getActivityToXMLLayoutFiles().entrySet()){
            String res = "";
            for (String id : entry.getValue())
                res = res + id + "; ";
            list = list + entry.getKey() + ": " +  res + "\n";
        }


        File f = createFile(appOutputDir, "activitiesToLayoutIds");

        FileOutputStream writerStream = new FileOutputStream(f, true);
        try {
            byte[] b = list.getBytes();
            writerStream.write(b);

        } finally {
            writerStream.close();
        }*/

    }

    private void saveResults(List<UiElement> results){

        XStream xStream = new XStream();
        xStream.processAnnotations(UiElement.class);
        xStream.processAnnotations(ApiInfoForForward.class);
        xStream.setMode(XStream.NO_REFERENCES);

       /* try {
            BufferedWriter bw = new BufferedWriter(
                    new OutputStreamWriter(
                           new FileOutputStream(String.format("results" + File.separator + "%s_forward_apiResults.xml", Helper.getApkName())), StandardCharsets.UTF_8));
                            // new FileOutputStream("testApps" + File.separator + Helper.getApkName().replace(".apk", "") + File.separator + "outputForTests.xml")));

            bw.append(xStream.toXML(results));

        }catch (IOException exception) {
            Helper.saveToStatisticalFile(exception.getMessage());
            exception.printStackTrace();
        }*/
        try (BufferedWriter bw = new BufferedWriter(
                new OutputStreamWriter(
                        new FileOutputStream(String.format("testApps" + File.separator + Helper.getApkName().replace(".apk", "") + File.separator + "outputForTests.xml")), StandardCharsets.UTF_8))) {
            bw.append(xStream.toXML(results));
        } catch (IOException exception) {
            Helper.saveToStatisticalFile(exception.getMessage());
        }
    }

    // same as in Reachability analysis (except of parameter
    private List<UiElement> extractListeners(Collection<XMLLayoutFile> xmlLayoutFiles) {
        // FIXME save data for test
        return AppController.getInstance().getUIElementObjectsForReachabilityAnalysis(false);

//        List<UiElement> uiElements = new ArrayList<>();
//
//        for (final XMLLayoutFile xmlFile : xmlLayoutFiles){
//
//            if(xmlFile instanceof Dialog){
//                //check for dialog. a very special case
//                Dialog dia = (Dialog)xmlFile;
//                for(Listener l : dia.getPosListener()){
//                    addListenerOfDialogToTheList(uiElements, xmlFile, l, DialogInterface.BUTTON_POSITIVE);
//                }
//                for(Listener l : dia.getNegativeListener()){
//                    addListenerOfDialogToTheList(uiElements, xmlFile, l, DialogInterface.BUTTON_NEGATIVE);
//                }
//                for(Listener l : dia.getNeutralListener()){
//                    addListenerOfDialogToTheList(uiElements, xmlFile, l, DialogInterface.BUTTON_NEUTRAL);
//                }
//                continue;
//            }
//
//            for (final int uiElementID : xmlFile.getUIElementIDs()) {
//                AppsUIElement uiElement = app.getUiElement(uiElementID);
//                if (uiElement.hasElementListener()) {
//                    final List<Listener> listeners = new ArrayList<Listener>();
//                    listeners.addAll(uiElement.getListernersFromElement());
//                    for (final Listener l : listeners) {
//                        if (l.getListenerClass() != null && l.getListenerClass().trim().length() > 0 && l.getListenerMethod() != null && l.getListenerMethod().trim().length() > 0) {
//                            SootClass c = Scene.v().getSootClass(l.getListenerClass());
//                            SootMethod candidateMethod = c.getMethodUnsafe(l.getListenerMethod());
//                            String candidateName = null;
//                            if (candidateMethod == null) {//should not be a case, but still can be useful for self-fixing
//                                if (l.getListenerMethod().split("\\(")[0].split(" ").length < 2) {
//                                    candidateName = l.getListenerMethod().split("\\(")[0].split(" ")[0].trim();
//                                } else {
//                                    candidateName = l.getListenerMethod().split("\\(")[0].split(" ")[1].trim();
//                                }
//                                for (SootMethod internalMethod : c.getMethods()) {
//                                    if (internalMethod.getName().equals(candidateName)) {
//                                        UiElement uiEl = new UiElement();
//                                        uiEl.kindOfElement = uiElement.getKindOfUiElement();
//                                        uiEl.handlerMethod = internalMethod;
//                                        uiEl.signature = Helper.getSignatureOfSootMethod(internalMethod);
//                                        uiEl.elementId = String.valueOf(uiElement.getID());
//                                        for (int parentID : uiElement.getParents()) {
////                                            if (parent != null && parent.getID() != null && parent.getUIID().trim().length() > 0) {
//                                                uiEl.parents.add(String.valueOf(parentID));
////                                            }
//                                        }
//                                        if (uiElement.isText()) {
//                                            uiEl.text = uiElement.getTextFromElement();
//                                        }
//                                        uiElements.add(uiEl);
//                                    }
//                                }
//                            } else {
//                                UiElement uiEl = new UiElement();
//                                uiEl.kindOfElement = uiElement.getKindOfUiElement();
//                                uiEl.handlerMethod = candidateMethod;
//                                uiEl.signature = Helper.getSignatureOfSootMethod(candidateMethod);
//                                uiEl.elementId = String.valueOf(uiElement.getID());
//                                for (int parentID : uiElement.getParents()) {
////                                    if (parent != null && parent.getUIID() != null && parent.getUIID().trim().length() > 0) {
//                                        uiEl.parents.add(String.valueOf(parentID));
////                                    }
//                                }
//                                if (uiElement.isText()) {
//                                    uiEl.text = uiElement.getTextFromElement();
//                                }
//                                uiElements.add(uiEl);
//                            }
//                        }
//                    }
//
//                    // get the text for testing purposes (also if there is no listener attached)
//                }else if (uiElement.isText()){
//                    UiElement uiEl = new UiElement();
//                    uiEl.elementId = String.valueOf(uiElement.getID());
//                    uiEl.kindOfElement = uiElement.getKindOfUiElement();
//                    uiEl.text = uiElement.getTextFromElement();
//                    uiElements.add(uiEl);
//                }
//            }
//
//            for (final Listener l : xmlFile.getLayoutListeners()){
//                // place here your code
//                if (l.getListenerClass() != null && l.getListenerClass().trim().length() > 0 && l.getListenerMethod() != null && l.getListenerMethod().trim().length() > 0) {
//                    SootClass c = Scene.v().getSootClass(l.getListenerClass());
//                    SootMethod candidateMethod = c.getMethodUnsafe(l.getListenerMethod());
//                    String candidateName = null;
//                    if (candidateMethod == null) {//should not be a case, but still can be useful for self-fixing
//                        if (l.getListenerMethod().split("\\(")[0].split(" ").length < 2) {
//                            candidateName = l.getListenerMethod().split("\\(")[0].split(" ")[0].trim();
//                        } else {
//                            candidateName = l.getListenerMethod().split("\\(")[0].split(" ")[1].trim();
//                        }
//                        for (SootMethod internalMethod : c.getMethods()) {
//                            if (internalMethod.getName().equals(candidateName)){
//                                UiElement uiEl = new UiElement();
//                                uiEl.handlerMethod = internalMethod;
//                                uiEl.signature = Helper.getSignatureOfSootMethod(internalMethod);
//                                // every xmlFile has an id
//                                if(/*xmlFile.hasId() && */!uiEl.layoutIds.contains(xmlFile.getId())){
//                                    uiEl.layoutIds.add(String.valueOf(xmlFile.getId()));
//                                }
//                                uiElements.add(uiEl);
//                            }
//                        }
//                    }else {
//                        UiElement uiEl = new UiElement();
//                        uiEl.handlerMethod = candidateMethod;
//                        uiEl.signature = Helper.getSignatureOfSootMethod(candidateMethod);
//                        // every xmlFile has an id
//                        if(/*xmlFile.hasId() &&*/ !uiEl.layoutIds.contains(xmlFile.getId())){
//                            uiEl.layoutIds.add(String.valueOf(xmlFile.getId()));
//                        }
//                        uiElements.add(uiEl);
//                    }
//                }
//            }
//        }
//        return uiElements;
    }

    // same as in Reachability analysis
    /*private void addListenerOfDialogToTheList(List<UiElement> uiElements, final XMLLayoutFile xmlFile, Listener l, int dialogButton) {
        if (l.getListenerClass() != null && l.getListenerClass().trim().length() > 0 && l.getListenerMethod() != null && l.getListenerMethod().trim().length() > 0) {
            SootClass c = Scene.v().getSootClass(l.getListenerClass());
            SootMethod candidateMethod = c.getMethodUnsafe(l.getListenerMethod());
            String candidateName = null;
            if (candidateMethod == null) {//should not be a case, but still can be useful for self-fixing
                if (l.getListenerMethod().split("\\(")[0].split(" ").length < 2) {
                    candidateName = l.getListenerMethod().split("\\(")[0].split(" ")[0].trim();
                } else {
                    candidateName = l.getListenerMethod().split("\\(")[0].split(" ")[1].trim();
                }
                for (SootMethod internalMethod : c.getMethods()) {
                    if (internalMethod.getName().equals(candidateName)){
                        UiElement uiEl = new UiElement();
                        uiEl.elementId = Integer.toString(dialogButton);
                        uiEl.handlerMethod = internalMethod;
                        uiEl.signature = Helper.getSignatureOfSootMethod(internalMethod);
                        // every xmlFile has an id
                        if(!uiEl.layoutIds.contains(xmlFile.getId())){
                            uiEl.layoutIds.add(String.valueOf(xmlFile.getId()));
                        }
                        uiElements.add(uiEl);
                    }
                }
            }else {
                UiElement uiEl = new UiElement();
                uiEl.elementId = Integer.toString(dialogButton);
                uiEl.handlerMethod = candidateMethod;
                uiEl.signature = Helper.getSignatureOfSootMethod(candidateMethod);
                // every xmlFile has an id
                if(!uiEl.layoutIds.contains(xmlFile.getId())){
                    uiEl.layoutIds.add(String.valueOf(xmlFile.getId()));
                }
                uiElements.add(uiEl);
            }
        }
    }*/
}
