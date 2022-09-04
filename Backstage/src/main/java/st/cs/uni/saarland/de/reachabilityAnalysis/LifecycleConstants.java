package st.cs.uni.saarland.de.reachabilityAnalysis;

/**
 * Created by avdiienko on 10/02/16.
 */
public class LifecycleConstants {
    private static final String ACTIVITY_ONCREATE = "void onCreate(android.os.Bundle)";
    private static final String ACTIVITY_ONSTART = "void onStart()";
    private static final String ACTIVITY_ONRESTOREINSTANCESTATE = "void onRestoreInstanceState(android.os.Bundle)";
    private static final String ACTIVITY_ONPOSTCREATE = "void onPostCreate(android.os.Bundle)";
    private static final String ACTIVITY_ONRESUME = "void onResume()";
    private static final String ACTIVITY_ONPOSTRESUME = "void onPostResume()";
    private static final String ACTIVITY_ONCREATEDESCRIPTION = "java.lang.CharSequence onCreateDescription()";
    private static final String ACTIVITY_ONSAVEINSTANCESTATE = "void onSaveInstanceState(android.os.Bundle)";
    private static final String ACTIVITY_ONPAUSE = "void onPause()";
    private static final String ACTIVITY_ONSTOP = "void onStop()";
    private static final String ACTIVITY_ONRESTART = "void onRestart()";
    private static final String ACTIVITY_ONDESTROY = "void onDestroy()";

    public static final String SERVICE_ONCREATE = "void onCreate()";
    public static final String SERVICE_ONSTART1 = "void onStart(android.content.Intent,int)";
    public static final String SERVICE_ONSTART2 = "int onStartCommand(android.content.Intent,int,int)";
    public static final String SERVICE_ONBIND = "android.os.IBinder onBind(android.content.Intent)";
    public static final String SERVICE_ONREBIND = "void onRebind(android.content.Intent)";
    public static final String SERVICE_ONUNBIND = "boolean onUnbind(android.content.Intent)";
    public static final String INTENT_SERVICE_ONHANDLEINTENT = "void onHandleIntent(android.content.Intent)";
    public static final String SERVICE_ONDESTROY = "void onDestroy()";

    public static final String SERVICE_START="android.content.ComponentName startService(android.content.Intent)";
    public static final String SERVICE_BIND="boolean bindService(android.content.Intent,android.content.ServiceConnection,int)";

    public static final String[] activityMethods = {ACTIVITY_ONCREATE,
            ACTIVITY_ONDESTROY,
            ACTIVITY_ONPAUSE,
            ACTIVITY_ONRESTART,
            ACTIVITY_ONRESUME,
            ACTIVITY_ONSTART,
            ACTIVITY_ONSTOP,
            ACTIVITY_ONSAVEINSTANCESTATE,
            ACTIVITY_ONRESTOREINSTANCESTATE,
            ACTIVITY_ONCREATEDESCRIPTION,
            ACTIVITY_ONPOSTCREATE,
            ACTIVITY_ONPOSTRESUME};
    public static final String[] serviceMethods = {SERVICE_ONBIND, SERVICE_ONCREATE, SERVICE_ONDESTROY, SERVICE_ONREBIND, SERVICE_ONSTART1, SERVICE_ONSTART2, SERVICE_ONUNBIND, INTENT_SERVICE_ONHANDLEINTENT};
}
