package st.cs.uni.saarland.de.reachabilityAnalysis;

import st.cs.uni.saarland.de.helpClasses.Helper;

import java.io.*;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by avdiienko on 22/04/16.
 */
public class START_ACTIVITY_CONSTANTS {
    private static Set<String> startActivityMethods = new HashSet<>();
    public static final String INTENT_CLASS = "android.content.Intent";
    public static final String SET_DATA_METHOD="<android.content.Intent: android.content.Intent setData(android.net.Uri)>";
    public static final String SET_ACTION="<android.content.Intent: android.content.Intent setAction(java.lang.String)>";
    public static final String SET_CLASS_NAME="<android.content.Intent: android.content.Intent setClassName(android.content.Context,java.lang.String)>";
    public static final String SET_CLASS="<android.content.Intent: android.content.Intent setClass(android.content.Context,java.lang.Class)>";
    public static final String PUT_EXTRA="<android.content.Intent: android.content.Intent putExtra(java.lang.String,java.lang.String[])>";
    public static final String PUT_EXTRA2="<android.content.Intent: android.content.Intent putExtra(java.lang.String,java.lang.String)>";
    public static final String INIT_DEFAULT="<android.content.Intent: void <init>()>";
    public static final String INIT_WITH_ACTION="<android.content.Intent: void <init>(java.lang.String)>";
    public static final String INIT_WITH_ACTION_URI="<android.content.Intent: void <init>(java.lang.String,android.net.Uri)>";
    public static final String INIT_WITH_CONTEXT_CLASS="<android.content.Intent: void <init>(android.content.Context,java.lang.Class)>";
    public static final String INIT_WITH_ACTION_URI_CONTEXT_CLASS="<android.content.Intent: void <init>(java.lang.String,android.net.Uri,android.content.Context,java.lang.Class)>";
    public static final String GET_CLASS="<java.lang.Class: java.lang.String getName()>";
    public static final String PUT_EXTRAS_BUNDLE="<android.content.Intent: android.content.Intent putExtras(android.os.Bundle)>";
    public static final String PUT_STRING_TO_BUNDLE="<android.os.Bundle: void putString(java.lang.String,java.lang.String)>";
    public static final String BUNDLE_INIT="<android.os.Bundle: void <init>()>";
    public static final String SET_COMPONENT="<android.content.Intent: android.content.Intent setComponent(android.content.ComponentName)>";
    public static final String COMPONENT_INIT="<android.content.ComponentName: void <init>(java.lang.String,java.lang.String)>";
    public static final String COMPONENT_INIT_CLASS="<android.content.ComponentName: void <init>(java.lang.String,java.lang.Class)>";
    public static final String COMPONENT_INIT_CONTEXT_CLASS="<android.content.ComponentName: void <init>(android.content.Context,java.lang.Class)>";


    public static final String ANDROID_APP_SERVICE = "android.app.Service";


    private static Set<String> constructors = new HashSet<>();

    public static Set<String> getIntentConstructors(){
        if(constructors.isEmpty()){
            constructors.add(INIT_DEFAULT);
            constructors.add(INIT_WITH_ACTION);
            constructors.add(INIT_WITH_ACTION_URI);
            constructors.add(INIT_WITH_ACTION_URI_CONTEXT_CLASS);
            constructors.add(INIT_WITH_CONTEXT_CLASS);
        }
        return constructors;
    }

    public static Set<String> getStartActivityMethods(){
        if(startActivityMethods.isEmpty()){
            loadStartActivitySignatures();
        }
        return  startActivityMethods;
    }

    private static void loadStartActivitySignatures() {
        String parentPath = new File(System.getProperty("user.dir")).getAbsolutePath();
        String filePath = parentPath + File.separator + "backstage" + File.separator +
                "res" + File.separator + "startActivitySignatures.txt";
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            for (String line; (line = br.readLine()) != null; ) {
                if (line.trim().length() > 0) {
                    if(line.trim().startsWith("#"))
                        continue;
                    startActivityMethods.add(line.split(":")[1].trim().replace(">",""));
                }
            }
        } catch (IOException e) {
            Helper.saveToStatisticalFile(Helper.exceptionStacktraceToString(e));
            e.printStackTrace();
        }
    }
}
