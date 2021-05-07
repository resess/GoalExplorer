package android.goal.explorer.data.android.constants;

public class ClassConstants {

    // Application
    public static final String APPLICATIONCLASS = "android.app.Application";
    public static final String CONTEXTCLASS = "android.content.Context";
    public static final String SUPPORTV7APPCLASS = "android.support.v7.app";
    public static final String SUPPORTV4APPCLASS = "android.support.v4.app";

    // Activity
    public static final String ACTIVITYCLASS = "android.app.Activity";
    public static final String APPCOMPATACTIVITYCLASS_V4 = "android.support.v4.app.AppCompatActivity";
    public static final String APPCOMPATACTIVITYCLASS_V7 = "android.support.v7.app.AppCompatActivity";

    public static final String ACTIVITYLIFECYCLECALLBACKSINTERFACE = "android.app.Application$ActivityLifecycleCallbacks";

    // Fragment
    public static final String FRAGMENTCLASS = "android.app.Fragment";
    public static final String SUPPORTFRAGMENTCLASS = "android.support.v4.app.Fragment";
    public static final String FRAGMENTTRANSACTIONCLASS = "android.app.FragmentTransaction";
    public static final String FRAGMENTMANAGERCLASS = "android.app.FragmentManager";
    public static final String FRAGMENTSUPPORTTRANSACTIONCLASS = "android.support.v4.app.FragmentTransaction";
    public static final String FRAGMENTSUPPORTMANAGERCLASS = "android.support.v4.app.FragmentManager";

    // Service
    public static final String SERVICECLASS = "android.app.Service";
    public static final String SERVICECONNECTIONINTERFACE = "android.content.ServiceConnection";
    public static final String SIG_CAR_CREATE = "<android.car.Car: android.car.Car createCar(android.content.Context,android.content.ServiceConnection)>";

    // GCM
    public static final String GCMBASEINTENTSERVICECLASS = "com.google.android.gcm.GCMBaseIntentService";
    public static final String GCMLISTENERSERVICECLASS = "com.google.android.gms.gcm.GcmListenerService";

    // Broadcast Receiver
    public static final String BROADCASTRECEIVERCLASS = "android.content.BroadcastReceiver";

    // Content Provider
    public static final String CONTENTPROVIDERCLASS = "android.content.ContentProvider";

    // Callbacks
    public static final String COMPONENTCALLBACKSINTERFACE = "android.content.ComponentCallbacks";
    public static final String COMPONENTCALLBACKS2INTERFACE = "android.content.ComponentCallbacks2";

    // Intent
    public static final String INTENTCLASS = "android.content.Intent";

    public static final String PREFERENCEACTIVITY = "android.preference.PreferenceActivity";

    /**
     * Gets whether the given class if one of Android's default lifecycle classes
     * (android.app.Activity etc.)
     *
     * @param className The name of the class to check
     * @return True if the given class is one of Android's default lifecycle
     * classes, otherwise false
     */
    public static boolean isLifecycleClass(String className) {
        return className.equals(ACTIVITYCLASS) || className.equals(SERVICECLASS) || className.equals(FRAGMENTCLASS)
                || className.equals(BROADCASTRECEIVERCLASS) || className.equals(CONTENTPROVIDERCLASS)
                || className.equals(APPLICATIONCLASS) || className.equals(APPCOMPATACTIVITYCLASS_V4)
                || className.equals(APPCOMPATACTIVITYCLASS_V7);
    }


}
