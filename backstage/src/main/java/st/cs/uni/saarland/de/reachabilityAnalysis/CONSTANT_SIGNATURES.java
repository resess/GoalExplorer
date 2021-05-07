package st.cs.uni.saarland.de.reachabilityAnalysis;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by avdiienko on 28/01/16.
 */
public class CONSTANT_SIGNATURES {
    public static final String dialogOnClick = "void onClick(android.content.DialogInterface,int)>";
    public static final String buttonGetId="<android.view.View: int getId()>";
    public static final String optionMenuGetId="<android.view.MenuItem: int getItemId()>";
    public static final String buttonGetIdFullUnit="virtualinvoke $r1.<android.view.View: int getId()>()";
    public static final String executorServiceSubmitRunnable="java.util.concurrent.Future submit(java.lang.Runnable)";

    public static final String FRAGMENT_ON_CREATE_VIEW="android.view.View onCreateView(android.view.LayoutInflater,android.view.ViewGroup,android.os.Bundle)";
    public static final String FRAGMENT_ON_VIEW_CREATED="void onViewCreated(android.view.View,android.os.Bundle)";
    public static final String FRAGMENT_ON_ATTACH="void onAttach(android.os.Bundle)";

    public static final String ACTIVITY_ONCREATE = "void onCreate(android.os.Bundle)";
    public static final String ACTIVITY_ONSTART = "void onStart()";
    public static final String ACTIVITY_ONRESTOREINSTANCESTATE = "void onRestoreInstanceState(android.os.Bundle)";
    public static final String ACTIVITY_ONPOSTCREATE = "void onPostCreate(android.os.Bundle)";
    public static final String ACTIVITY_ONRESUME = "void onResume()";
    public static final String ACTIVITY_ONPOSTRESUME = "void onPostResume()";
    public static final String ACTIVITY_ONCREATEDESCRIPTION = "java.lang.CharSequence onCreateDescription()";
    public static final String ACTIVITY_ONSAVEINSTANCESTATE = "void onSaveInstanceState(android.os.Bundle)";
    public static final String ACTIVITY_ONPAUSE = "void onPause()";
    public static final String ACTIVITY_ONSTOP = "void onStop()";
    public static final String ACTIVITY_ONRESTART = "void onRestart()";
    public static final String ACTIVITY_ONDESTROY = "void onDestroy()";
    public static final String ACITVITY_ONCREATE_OPTIONS_MENU="boolean onCreateOptionsMenu(android.view.Menu)";
    public static final String ACTIVITY_ONCREATE_CONTEXT_MENU="void onCreateContextMenu(android.view.ContextMenu,android.view.View,android.view.ContextMenu$ContextMenuInfo)";

    public static final String[] fragmentMethods = {ACTIVITY_ONCREATE,
            ACTIVITY_ONSTART,
            ACTIVITY_ONCREATEDESCRIPTION,
            ACTIVITY_ONPOSTCREATE,
            ACITVITY_ONCREATE_OPTIONS_MENU,
            ACTIVITY_ONCREATE_CONTEXT_MENU,
            FRAGMENT_ON_CREATE_VIEW,
            FRAGMENT_ON_VIEW_CREATED,
            FRAGMENT_ON_ATTACH
    };

    public static final String[] activityMethods = {ACTIVITY_ONCREATE,
            //ACTIVITY_ONDESTROY,
            //ACTIVITY_ONPAUSE,
            //ACTIVITY_ONRESTART,
            //ACTIVITY_ONRESUME,
            ACTIVITY_ONSTART,
            //ACTIVITY_ONSTOP,
            //ACTIVITY_ONSAVEINSTANCESTATE,
            //ACTIVITY_ONRESTOREINSTANCESTATE,
            ACTIVITY_ONCREATEDESCRIPTION,
            ACTIVITY_ONPOSTCREATE,
            //ACTIVITY_ONPOSTRESUME,
            ACITVITY_ONCREATE_OPTIONS_MENU,
            ACTIVITY_ONCREATE_CONTEXT_MENU
    };


    public static final List<String> buttonOnClickSignatures = new ArrayList<String>(){{
        add("void onClick(android.view.View)>");
        add("void onLongClick(android.view.View)>");
        add("boolean onTouch(android.view.View,android.view.MotionEvent)>");
    }};


    public static final List<String> optionMenuOnClicks = new ArrayList<String>(){{
        add("boolean onOptionsItemSelected(android.view.MenuItem)>");
        add("boolean onMenuItemClick(android.view.MenuItem)>");
        add("boolean onActionItemClicked(android.view.ActionMode,android.view.MenuItem)>");
    }};
}
