package st.cs.uni.saarland.de.xmlAnalysis;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;
import st.cs.uni.saarland.de.entities.*;
import st.cs.uni.saarland.de.helpClasses.Helper;
import st.cs.uni.saarland.de.testApps.Content;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.stream.Stream;

public class XMLParserMain {

    Application app; // object structure where data is saved
    // path to the folder where the app's output of the apk tool is stored; folder name should be the app's name
    String pathToAppOutFolder;
    private static final Logger logger = LoggerFactory.getLogger(Thread.currentThread().getName());

    private static XMLParserMain xmlParser;
    private boolean processImages;

    public static XMLParserMain getInstance(String path) {
        if (null == xmlParser) {
            xmlParser = new XMLParserMain(path);
        }
        return xmlParser;
    }

    public static XMLParserMain getInstance() {
        if (null == xmlParser) {
            logger.error("XMLParserMain not initialized");
            Helper.saveToStatisticalFile("XMLParserMain not initialized");
        }
        return xmlParser;
    }

    public XMLParserMain(String pathToAppOutFolder) {
        this.pathToAppOutFolder = pathToAppOutFolder;
    }

    // returns an instance of Application -> here it gets initialized with all data from the XMLLayoutFiles
    public Application xmlParserMain(boolean processImages) {
        Application app = null;
        this.processImages = processImages;
        try {
            // call to the XML analysis
            app = this.runXMLAnalysis();
        } catch (IOException | ParserConfigurationException | SAXException e) {
            logger.error("XMLParser: " + e);
            Helper.saveToStatisticalFile(Helper.exceptionStacktraceToString(e));
        }
        return app;
    }

    // returns the initialized Application object
    public Application runXMLAnalysis() throws SAXException, IOException, ParserConfigurationException {

        // appOutFolder is the folder where the unpacked app files are stored
        Path appOutFolder = Paths.get(pathToAppOutFolder);
        this.app = new Application(appOutFolder.getFileName().toString());

        //------------------------------------------------------------------------------------------------------------
        // analyze all value/resource files

        // process the strings.xml file
        Map<String, String> stringValues = processStringValues();
        Content.getInstance().setStringNameToValue(stringValues);
        Map<String, String> arrayValues = processArrayValues();
        Content.getInstance().setArrayNameToArrayValue(arrayValues);
        StyleHandler styleParser = new StyleHandler(pathToAppOutFolder + File.separator + "res" + File.separator + "values", stringValues);
        Map<String, Style> styleMap = styleParser.parseResource();
        Content.getInstance().setStyleNameToStyle(styleMap);

        // process the public.xml file (where the variable name to id matchings are stored)
        PublicXmlHandler publicHelper = new PublicXmlHandler(pathToAppOutFolder + File.separator + "res" + File.separator + "values");
        Content.getInstance().setNameToIds(publicHelper.parseResource());
        //setIdsOfXMLLayoutFilesAndAppsUiElements(); done now inside the handlers

        //------------------------------------------------------------------------------------------------------------
        // analyse all layout XML files

        // check if this is the directory of one app (appOutFolder)
        if (Files.isDirectory(appOutFolder)) {
            // all files of app get searched/analyzed (by the xmlparser)
            try {
                searchXMLFilesInDir(Paths.get(appOutFolder.toString(), "res", "layout"));
                searchXMLFilesInDir(Paths.get(appOutFolder.toString(), "res", "menu"));
            } catch (Exception e) {
                logger.error(appOutFolder.getFileName().toString() + ": " + e);
                Helper.saveToStatisticalFile(Helper.exceptionStacktraceToString(e));
            }
        } else {
            logger.error(appOutFolder.getFileName().toString() + ": OutputDir not a directory: " + appOutFolder.toString());
            Helper.saveToStatisticalFile(appOutFolder.getFileName().toString() + ": OutputDir not a directory: " + appOutFolder.toString());
        }

        // search and save permissions of this app
        this.runAndroidManifestParser(Paths.get(appOutFolder.toString(), "AndroidManifest.xml").toString());

        //------------------------------------------------------------------------------------------------------------
        // process the drawable folder and catch all images and icons
        if (processImages) {
            ImageCatcher imageCatcher = new ImageCatcher();
            app = imageCatcher.runImageCatcher(app);
        }

        //------------------------------------------------------------------------------------------------------------
        // resolve include tags
        resolveIncludeTags();

        return app;
    }


    // recursive search of all files inside dir
    // it is called with dir = appoutFolder/res/<X> (<X> -> layout/menu)
    //FIXME
    private void searchXMLFilesInDir(Path dir) throws IOException, SAXException, ParserConfigurationException {
        if (!Files.exists(dir)) {
            Helper.saveToStatisticalFile(dir.toString() + " folder does not exists!");
            return;
        }
        try (Stream<Path> stream = Files.walk(dir, 999)) {
            stream.map(Path::toString)// TODO use parallel()
                    .filter(path -> path.endsWith("xml") && !path.equals("AndroidManifest.xml"))
                    .forEach(fName -> {
                        try {
                            app = runLayoutParser(fName, app);
                        } catch (Exception e) {
                            logger.error("error " + e.getMessage() + " while processing " + fName);
                            e.printStackTrace();
                        }
                    });
        }
    }

    // returns an object of XMLLayoutFile which represents the xml file at xmlFilePath
    // inside this XMLLayoutFile object are all elements of this file stored (as AppsUIElements)

    private Application runLayoutParser(String xmlFilePath, Application app) throws SAXException, IOException,
            ParserConfigurationException {
        LayoutHandler handler = new LayoutHandler(processImages);
        return handler.parseLayout(xmlFilePath, app);
    }

    // analyses the AndroidManifest.xml file: it searches for permissions, activity names, and intent filters
    // all data is stored in the Application object(app)
    private void runAndroidManifestParser(String androidManifestFilePath) {

        try {
            SAXParserFactory parserFactor = SAXParserFactory.newInstance();
            SAXParser parser = parserFactor.newSAXParser();
            // create a new handler for the AndroidManifest(A.M.) XML file
            SAXAndroidManifestHandler handler = new SAXAndroidManifestHandler();
            // parse the AndroidManifest file with the parse and the A.M. handler
            parser.parse(androidManifestFilePath, handler);
            // store the found data in the app object
            app.setPermissions(handler.getAppsPermission());
            handler.getActivities().forEach(app::addActivity);
            app.setIntentFilters(handler.getIntentFilters());
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }

    }

    // analysis the strings.xml file: there the matching of string variables and its text is stored
    // after analysis the matching get stored in the Content class
    private Map<String, String> processStringValues() {
        ResourceHandler stringsParser = new StringsHandler(pathToAppOutFolder + File.separator + "res" + File.separator + "values");
        Map<String, String> stringsValues = stringsParser.parseResource();
        // check for English values
        File valuesEnFile = new File(pathToAppOutFolder + File.separator + "res" + File.separator + "values-en");
        if (valuesEnFile.exists()) {
            ResourceHandler stringsParserEn = new StringsHandler(pathToAppOutFolder + File.separator + "res" + File.separator + "values-en");
            Map<String, String> stringsValuesEn = stringsParserEn.parseResource();
            if (stringsValuesEn != null) {
                stringsValues.putAll(stringsValuesEn);
            }
        }
        return stringsValues;
    }

    private Map<String, String> processArrayValues() {
        ResourceHandler arrayParser = new ArrayHandler(pathToAppOutFolder + File.separator + "res" + File.separator + "values");
        Map<String, String> arrayValues = arrayParser.parseResource();
        // check for English values
        File valuesEnFile = new File(pathToAppOutFolder + File.separator + "res" + File.separator + "values-en");
        if (valuesEnFile.exists()) {
            ResourceHandler arrayParserEn = new ArrayHandler(pathToAppOutFolder + File.separator + "res" + File.separator + "values-en");
            Map<String, String> arrayValuesEn = arrayParserEn.parseResource();
            if (arrayValuesEn != null) {
                arrayValues.putAll(arrayValuesEn);
            }
        }
        return arrayValues;
    }

    // include tags are resolved: parents and childs are added where an include tag appears
    public void resolveIncludeTags() {
        // iterate overall all AppsUIElements that where found in all XMLLayout files
        for (int eID : app.getIncludeTagIDs()) {
            XMLIncludeTag incTag = (XMLIncludeTag) app.getUiElement(eID);
            // layoutName is the name of the layout that should be added
            String layoutName = incTag.getLayoutName();
            // layoutName could be blank if it was an Android layout
            if (StringUtils.isBlank(layoutName))
                continue;
            // get the XMLLayoutFile that is included
            int layoutID = Content.getInstance().getIdFromName(layoutName, "layout");
            if (layoutID == 0) {// case name to id not found
                layoutID = Content.getInstance().getIdFromName(layoutName, "menu");
            }
            if (layoutID == 0) {
                Helper.saveToStatisticalFile("XMLParserMain: resolveIncludeTags: couldn't find XMLLayoutFile id from name: " + layoutName);
                continue;
            }
            // add the included layout to the include tag
            incTag.addXmlFile(layoutID);
            try {
                XMLLayoutFile xmlF = app.getXmlLayoutFile(layoutID);
                // set the root element from the included layout as child of the include tag
                int rootElementIDOfIncludedLayout = xmlF.getRootElementID();
                incTag.addChildID(rootElementIDOfIncludedLayout);
                // add the include tag as parent to the root element from the included layout
                AppsUIElement rootElementOfIncludedLayout = app.getUiElement(rootElementIDOfIncludedLayout);
                rootElementOfIncludedLayout.addParent(incTag.getId());
            } catch (NullPointerException e) {
                // at the moment only layout and menu folder get parsed:
                // sometimes layouts get added if they are used for another screen size, but not inside the default layout folder
                Helper.saveToStatisticalFile("XMLParserMain:resolveIncludeTags: found XMLLayoutFile that was not parsed: name:" + layoutName + "; id:" + layoutID);
            }

        }
    }

}
