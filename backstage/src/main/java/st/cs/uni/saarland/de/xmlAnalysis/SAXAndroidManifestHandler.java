package st.cs.uni.saarland.de.xmlAnalysis;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import st.cs.uni.saarland.de.entities.Activity;
import st.cs.uni.saarland.de.helpClasses.Helper;

import java.util.*;


public class SAXAndroidManifestHandler extends DefaultHandler {

    public Set<Activity> getActivities() {
        return activities;
    }

    private final Set<Activity> activities = new HashSet<>();

    private Set<String> appsPermission = new HashSet<>();
    ;
    private String packagePath = ""; // path of the packages to the activities inside the app's code: eg com.example.src
    // TODO save the intentFilters corresponding to their activities
    private List<String> intentFilters = new ArrayList<String>();
    private boolean intentFilterTag = false; // variable to remember if the analysis is currently inside an intent-filter tag
    // TODO sometimes more than one activity is first activity set
    private List<String> firstActvityName = new ArrayList<String>(); // save the first activity that is called
    private String tmpLastActivityName; // to save the last activity, used to set the first activity
    // don't use firstActvityName for that->sometimes 1st act. is not declared
    // don't use activityNames.get(activityNames.size()) because sth launcher is declared but no activtiy
    private final Logger logger = LoggerFactory.getLogger(Thread.currentThread().getName());

    // Triggered if an start tag is detected
    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {

        switch (qName) {
            // get the package path from the manifest tag
            // eg: <manifest android:versionCode="1" android:versionName="1.0" package="com.example.Testapp"
            case "manifest":
                packagePath = attributes.getValue("package");
                break;

            // extract the permissions of the app
            // <uses-permission android:name="android.permission.SEND_SMS" />
            case "uses-permission":
                try {
                    appsPermission.add(attributes.getValue("android:name"));
                } catch (NullPointerException e) {
                    logger.debug(e.getMessage());
                    Helper.saveToStatisticalFile(e.getMessage());
                }
                break;

            // extract all declared activities
            // <activity android:label="@string/app_name" android:name="SendSmsFromAdress">
            // <activity android:name=".MainActivity2" />
            case "activity":
                String actName = attributes.getValue("android:name");
                if (!StringUtils.isBlank(actName)) {
                    Activity activity = new Activity();
                    // check if the package is given inside the name or not
                    if (actName.startsWith(".")) {
                        actName = packagePath + actName;
                    } else if (!actName.contains(".")) {
                        actName = packagePath + "." + actName;
                    }
                    // save the activity name with the packagePath
                    activity.setName(actName);
                    // save temporally the activity name (used if the launcher intent filter is called)
                    tmpLastActivityName = actName;


                    String actLabel = attributes.getValue("android:label");
                    if(!StringUtils.isBlank(actLabel)){
                        activity.setLabel(actLabel);
                    }
                    activities.add(activity);

                } else {
                    logger.debug("Did not find name to activity tag in Android Manifest");
                    Helper.saveToStatisticalFile("Did not find name to activity tag in Android Manifest");
                }
                break;
            // check for intent-filters eg:
            //<intent-filter>
            //	<action android:name="android.intent.action.SEARCH" />
            //</intent-filter>
            case "intent-filter":
                // remember that an intent-filter tag has started to later extract the value of it
                intentFilterTag = true;
                break;
            // action tag inside an intent-filter tag describes the intent-filters
            case "action":
                if (intentFilterTag) {
                    String intentName = attributes.getValue("android:name");
                    if (!intentFilters.contains(intentName))
                        intentFilters.add(intentName);
                }
                break;
            // check with which activity the app starts
            // eg <category android:name="android.intent.category.LAUNCHER" />
            case "category":
                if (intentFilterTag) {
                    // the last added activity is the last activity tag and therefore this intent filter belongs
                    // to this activity -> this activity will be started at first
                    if (!StringUtils.isBlank(tmpLastActivityName))
                        firstActvityName.add(tmpLastActivityName);
                }
                break;
            default:
                break;
        }


    }

    // Triggered if an end tag is detected
    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        // remember that the intent-filter tag is closed
        if (qName.equals("intent-filters"))
            intentFilterTag = false;
    }

    @Override
    public void characters(char[] ch, int start, int length)
            throws SAXException {
    }

    // getter to provide the results of the analysis

    public Set<String> getAppsPermission() {
        return appsPermission;
    }

    public List<String> getIntentFilters() {
        return intentFilters;
    }
}
