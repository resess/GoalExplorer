package st.cs.uni.saarland.de.xmlAnalysis;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import st.cs.uni.saarland.de.entities.*;
import st.cs.uni.saarland.de.helpClasses.Helper;
import st.cs.uni.saarland.de.helpMethods.CheckIfMethodsExisting;
import st.cs.uni.saarland.de.testApps.Content;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

class LayoutHandler extends DefaultHandler {
    private Application app;
    Stack<AppsUIElement> stack = new Stack<AppsUIElement>(); // stack to remember the parent of an element (ui element/tag)
    XMLLayoutFile xmlLayFile = new XMLLayoutFile(); // XMLLayoutFile which represents the currently analysed xml file
    CheckIfMethodsExisting checkHelper = CheckIfMethodsExisting.getInstance(); // helper class for ...
    Content content = Content.getInstance(); // data storage class for id mappings
    private boolean firstElement = true; // shows if the processed ui element tag is the root element or not
    private boolean processImages; // parameter of this program
    private final Logger logger = LoggerFactory.getLogger(Thread.currentThread().getName());


    public LayoutHandler(boolean processImages) {
        this.processImages = processImages;
    }

    // Triggered when the start of tag is found
    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        // check for the special root element menu
        //logger.debug("Parsing the element {} {} {} {}", uri, localName, qName, attributes);
        if (qName.equals("menu")) {
            // if menu is detected then this XMLLayoutFile is not only an XML file but also a Menu
            // change the XMLLayoutFile object to a Menu object
            //logger.debug("The current xmlLayFile {}", xmlLayFile);
            if(xmlLayFile instanceof Menu){
                //logger.debug("Is empty? {} {}", xmlLayFile.isEmpty(), ((XMLLayoutFile)xmlLayFile).isEmpty());
                //TODO: A submenu
                //we need to somehow keep the mapping from the previous parsed item to the submenu
                //do the same for preference screen?
            }
            else xmlLayFile = new Menu();
            //maybe here if xmlLayFile is already set and it's a menu, then it's included inside of it
            // check for the special root element merge
        } else if (qName.equals("merge")) {
            // remember that this layout can only be included somewhere
            xmlLayFile.setIncludedSomewhere();
        }
        else if(qName.equals("PreferenceScreen")){
            //logger.debug("The current xmlLayFile {}", xmlLayFile);
            if(xmlLayFile instanceof PreferenceScreen) {
                //logger.debug("Found inner preference screen");
                
            }
            else xmlLayFile = new PreferenceScreen();
            //only if it's the outermost one, so xmllayout is not set yet?
            //in anycase, get 
           
                    //intentAction = attributes.getValue("intent");

        }
        // an UI element is found
        // -------------------------------------------------------------------------------------------------------
        // get the listener of this element
        String listenerMethodName = attributes.getValue("android:onClick");
        Listener listener = null;
        if (listenerMethodName != null) {
            listener = new Listener("onClick", true, listenerMethodName, "default_value");
            logger.debug("Parsed a listener from xml file {}", listenerMethodName);
        }

        // -------------------------------------------------------------------------------------------------------
        // extract the text and drawables of the element

        // initialise the text variable
        String text = "";
        String textVar = "";
        int attributeLength = attributes.getLength();
        // set of all drawables variable names that will be found
        Set<String> drawableVarOfImages = new HashSet<String>();
        // iter over all attributes this tag has
        for (int i = 0; i < attributeLength; i++) {
            // extract name (with "android:") and value of this attribute
            String attrFullName = attributes.getQName(i);
            String value = attributes.getValue(i);

            // split the android prequel from the attribute's name
            String[] attrNames = attrFullName.split(":");
            // only android: attributes are searched, if this is not the case, continue with the next attribute
            if (attrNames.length == 1)
                continue;
            // extract the attribute name
            String attrName = attrNames[1];

            //TODO Add listview special tag I guess
            // -----------------------------------------------
            // extract images
            // check if the currently analysed attribute is an imageMarker that is searched
            if (imageMarkers.contains(attrName) && processImages) {
                // check if the drawable is an Android default image
                if (value.contains("@android:drawable/")) {
                    // save the name of this android default image (also with "android:" to mark that it is an
                    // Android default image
                    drawableVarOfImages.add(value.replace("@", ""));
                    // process a normal/custom drawable
                } else if (value.contains("drawable/")) {
                    // TODO include checker @[package:]drawable/filename sometimes(not often)...
                    String[] tmp = value.split("drawable/");
                    // check if this name is really a drawable variable-> other are not processed
                    if (tmp.length > 1) {
                        String drawableName = tmp[1];
                        // add the image to the list of found images
                        drawableVarOfImages.add(drawableName);
                    }
                } else {
                    // save all values that were found inside an image marker but do not include a drawable variable
                    // this is done because of debug purposes -> check which other imageMarker are out there
                    if (!attrName.equals("background")) {
                        // save this value and the attribute name
                        logger.debug("found imageMarker without drawable: " + attrName + "=" + value);
                        Helper.saveToStatisticalFile("found imageMarker without drawable: " + attrName + "=" + value);
                    }
                }
                // TODO discuss if this is still necessary because imageMarkers should cover all important
                // save also all image that were not found inside the imageMarkers -> done for debug purposes
            } else if (value.contains("drawable/") && (!attrFullName.equals("android:textColor"))) {
                if (value.contains("@android:drawable/")) {
                    drawableVarOfImages.add(value.replace("@", ""));
                }
//                    else {
//                        // TODO include checker @[package:]drawable/filename sometimes(not often)...
//                        String[] tmp = value.split("drawable/");
//                        if (tmp.length > 1) {
//                            String drawableName = tmp[1];
//                            drawableVarOfImages.add(drawableName);
//                            logger.debug("found drawable without imageMarker: " + attrName);
//                            Helper.saveToStatisticalFile("found drawable without imageMarker: " + attrName);
//                        }
//                    }
            } else
                // -----------------------------------------------
                // extract text

                if (textMarkers.contains(attrName)) {
                    // process android default values, string variables and concret string values ("text1")
                    if (value.contains("@android:string/"))
                        // resolve variable name to real string/value and add it to the text variable (joined by "#")
                        text = text + "#" + stringDefaultValues.get(value.replace("@android:string/", ""));
                        // process string variables and concret text
                    else {
                        String tmpText = value;
                        if (value.contains("@string/")) {
                            String var = value.replaceAll("@\\+?string/", "");
                            tmpText = content.getStringValueFromStringName(var);
                            textVar = textVar + "#" + var;
                        }
                        // add the string variable or concret text to the text variable
                        text = text + "#" + tmpText;

                    }
                    // TODO remove? debug purposes, only search for textmarker?
                } else if (!qName.equals("PreferenceScreen") && !qName.equals("item") && value.contains("string/")) {
                    String var = value.replaceAll("@\\+?string/", "");
                    textVar = textVar + "#" + var;
                    text = text + "#" + content.getStringValueFromStringName(var);
                    //logger.debug("Unknown tag with text added: " + attrFullName);
                }
        }

        // -------------------------------------------------------------------------------------------------------
        // extract style
        String styleName = attributes.getValue("style");
        Set<Style> styles = null;
        if (!StringUtils.isBlank(styleName) && !styleName.contains("@android")){
            String[] splittedStyleAttr = styleName.split("@style/");
            if (splittedStyleAttr.length == 2){
                Style style = content.getStyleFromName(splittedStyleAttr[1]);
                if (style != null){
                    styles = new HashSet<Style>();
                    styles.add(style);
                }else{
                    Helper.saveToStatisticalFile("LayoutHandler: didn't find style with name: " + styleName);
                }
            }
        }

        // remove the first "#" in the text string
        if (text.startsWith("#"))
            text = text.replaceFirst("#", "");
        if (textVar.startsWith("#"))
            textVar = textVar.replaceFirst("#", "");

        // -------------------------------------------------------------------------------------------------------
        // create the AppsUIElement object with the corresponding found data
        List<Integer> parentList = new ArrayList<Integer>();
        AppsUIElement parent = null;
        try {
            // get the parent of this element
            parent = stack.peek();
            if(parent != null)
                parentList.add(parent.getId());
        } catch (EmptyStackException e) {
        }

        AppsUIElement uiE = null;
        // if this element is an include element/tag, it is special handeled
        if (qName.equals("include")) {
            // layout could be : 1)layout="@layout/merged_layout3_merge_tag", 2) layout="@android:layout/select_dialog_singlechoice"
            String layoutName = attributes.getValue("layout");
            if(layoutName == null){
                logger.error("No layout found for include attribute {}",attributes);
                layoutName = "";
            }
            layoutName = layoutName.replace("@layout/", "");
            if (layoutName.contains("@android:layout")) {
                layoutName = ""; // just ignore it....
            }
            uiE = new XMLIncludeTag(qName, parentList, layoutName);
            app.addIncludeTagIDs(uiE.getId());
            // drawerlayout and viewPager tag are also special handeled
        } else if (qName.equals("android.support.v4.view.ViewPager") || qName.equals("android.support.v4.widget.DrawerLayout") || qName.equals("androidx.drawerlayout.widget.DrawerLayout")) {
            Map<String, String> textMap = new HashMap<>();
            textMap.put("default_value", text);
            if(qName.contains("DrawerLayout")){
                //need to check for nav graph
                //parse app:navgraph and then map
            }
            uiE = new SpecialXMLTag(qName, parentList, attributes.getValue("android:id"), textMap, textVar, drawableVarOfImages, styles);
            // fragment tags are also handeled special
        } 
         else if (qName.equals("fragment") || qName.equals("androidx.fragment.app.FragmentContainerView")) {
            // the class which is included/inflated here, gets extracted and saved
            // the class name could be saved in the class attribute or in the android:name attribute
            String className = attributes.getValue("class");
            if (className == null)
                className = attributes.getValue("android:name");
            Map<String, String> textMap = new HashMap<>();
            textMap.put("default_value", text);
            uiE = new XMLFragment(qName, parentList, attributes.getValue("android:id"), textMap, textVar, className);
            app.addFragmentTagIDs(uiE.getId());
           
        }
        else if(qName.equals("PreferenceScreen")){
            //logger.debug("The current xmlLayFile {}", xmlLayFile);
            if(xmlLayFile instanceof PreferenceScreen) {
                //PreferenceScreen prefScreen = (PreferenceScreen)xmlLayFile;
                String key = attributes.getValue("android:key"),
                title = attributes.getValue("android:title");
                Map<String, String> textMap = new HashMap<>();
                textMap.put("default_value", text);
                uiE = new PreferenceElement(key, text, qName, parentList, "", textMap, textVar, drawableVarOfImages, styles);
                //prefScreen.addPreferenceElementId(uiE.getId());
            }
                
        }
        else if(qName.equals("intent")){
            //need to add it to the previous I guess
            AppsUIElement prevElement = stack.peek();
            //logger.debug("The previous element {} {}",prevElement, uiE);
            //xmlLayFile.setIntentAction()
            if(prevElement instanceof PreferenceElement){
                PreferenceElement prefElement = (PreferenceElement)prevElement;
                prefElement.setIntentAction(attributes.getValue("android:action"));
                prefElement.setIntentTargetClass(attributes.getValue("android:targetClass"));
            }
        }
        else if (qName.equals("android.support.design.widget.NavigationView") || qName.equals("com.google.android.material.navigation.NavigationView")) {
            String menuLayout = attributes.getValue("app:menu");
            //logger.debug("Found a navigation view with menu {}", menuLayout);
            Map<String, String> textMap = new HashMap<>();
            textMap.put("default_value", text);
            uiE = new XMLNavigation(qName, parentList, attributes.getValue("android:id"), textMap, textVar, listener, drawableVarOfImages, styles, menuLayout);

        }
        else if (qName.contains("ListView")) {
            Map<String, String> textMap = new HashMap<>();
            textMap.put("default_value", text);
            uiE = new ListView(qName, parentList, attributes.getValue("android:id"), textMap, textVar, drawableVarOfImages, styles);
        }
        else if (qName.contains("Spinner")) {
            Map<String, String> textMap = new HashMap<>();
            textMap.put("default_value", text);
            uiE = new Spinner(qName, parentList, attributes.getValue("android:id"), textMap, textVar, drawableVarOfImages, styles);

        }
        else {  // case normal ui element was found like Button, TextView, ...
            Map<String, String> textMap = new HashMap<>();
            textMap.put("default_value", text);
            //TODO maybe add the layout file as an Id?
            uiE = new AppsUIElement(qName, parentList, attributes.getValue("android:id"), textMap, textVar, listener, drawableVarOfImages, styles);
            /*if(qName.equals("item"))
                logger.debug("Adding the listener object {} for item {} and parentList {} and xml {}", listener, uiE.getId(), parentList, xmlLayFile.getName());
                */
            
        }

        if(uiE != null){
            // add the found ui element to the XMLLayoutFile in which it is located
            xmlLayFile.addUIElement(uiE.getId());
            // add UI element to the Application map
            app.addUiElementsOfApp(uiE);

            // check if this is the root element of this XMLLayoutFile:
            if (firstElement) {
                xmlLayFile.setRootElementID(uiE.getId());
                firstElement = false;
            }

            if (parent != null)
                // add this element as a child of the parent
                parent.addChildID(uiE.getId());
        }

            // add the element to the stack so that children of this element, get this element as parent
        stack.push(uiE);


    }

    // Triggered when the end of tag is found
    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        // TODO check why include is not popped from the stack?
//        if (!qName.equals("include") && !qName.equals("merge"))
        // remove the last element again from stack if the tag is finished (there will be no more children of this element)
        if (stack.size() > 0)
            stack.pop();
    }

    @Override
    public void characters(char[] ch, int start, int length)
            throws SAXException {
    }

    public Application parseLayout(String filePath, Application app) {
        this.app = app;
        Path file = Paths.get(filePath);
        if (!Files.exists(file)) {
            Helper.saveToStatisticalFile("file path of xml file didn't match an existing file: " + filePath);
            return app;
        }
        logger.info("Parsing layout file at {}", filePath);
        xmlLayFile = new XMLLayoutFile();
        SAXParserFactory factory = SAXParserFactory.newInstance();
        try {
            SAXParser parser = factory.newSAXParser();
            parser.parse(filePath, this);
        } catch (Exception e) {
            Helper.saveToStatisticalFile("error " + e.getMessage() + " while processing " + filePath);
            logger.error("error " + e.getMessage() + " while processing " + filePath);
            e.printStackTrace();
        }
        // set the name of the currently parsed xml layout file
        xmlLayFile.setName(file.getFileName().toString().split("\\.xml")[0]);
        // add the XMLLayoutFile object to the app
        logger.info("Done parsing file at {}", filePath);
        app.addXMLLayoutFile(xmlLayFile);

        // return the results of the analysis
        return app;
    }


    // set of text markers which get searched as attribute
    @SuppressWarnings("serial")
    private Set<String> textMarkers = new HashSet<String>() {
        {
            add("text");
            add("hint");
            add("label");
            add("title");
            add("contentDescription");
            add("textOff");
            add("textOn");
        }
    };

    // set of image markers which get searched as attribute
    @SuppressWarnings("serial")
    private Set<String> imageMarkers = new HashSet<String>() {
        {
            add("drawableBottom");
            add("drawableEnd");
            add("drawableLeft");
            add("drawableRight");
            add("drawableStart");
            add("drawableTop");
            add("background");
            add("src");
            add("drawable");
        }
    };

    //FIXME: move to file
    Map<String, String> stringDefaultValues = new HashMap<String, String>() {{
        put("VideoView_error_button", "OK");
        put("VideoView_error_text_invalid_progressive_playback", "This video isn't valid for streaming to this device.");
        put("VideoView_error_text_unknown", "Can't play this video.");
        put("VideoView_error_title", "Video problem");
        put("cancel", "Cancel");
        put("copy", "Copy");
        put("copyUrl", "Copy URL");
        put("cut", "Cut");
        put("defaultMsisdnAlphaTag", "MSISDN1");
        put("defaultVoiceMailAlphaTag", "Voicemail");
        put("dialog_alert_title", "Attention");
        put("emptyPhoneNumber", "(No phone number)");
        put("httpErrorBadUrl", "Couldn't open the page because the URL is invalid.");
        put("httpErrorUnsupportedScheme", "The protocol isn't supported.");
        put("no", "Cancel");
        put("ok", "OK");
        put("paste", "Paste");
        put("search_go", "Search");
        put("selectAll", "Select all");
        put("selectTextMode", "Select text");
        put("status_bar_notification_info_overflow", "999+");
        put("unknownName", "(Unknown)");
        put("untitled", "<Untitled>");
        put("yes", "OK");
    }}; // map of default string variables to their values
}

