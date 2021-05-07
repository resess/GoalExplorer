package st.cs.uni.saarland.de.xmlAnalysis;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import st.cs.uni.saarland.de.entities.Listener;
import st.cs.uni.saarland.de.entities.Style;
import st.cs.uni.saarland.de.helpClasses.Helper;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by Isa on 02.03.2016.
 */
public class StyleHandler extends DefaultHandler {

    public static final String file = "styles.xml";
    // stores mappings between the string variable name and the string value
    private Map<String, Style> mapping = new HashMap<String, Style>();
    private String resourceFile; // folder to res from the app
    private final Logger logger = LoggerFactory.getLogger(Thread.currentThread().getName());
    //"/res/values/"

    private Style tmpStyle;
    private boolean extractText = false;
    private boolean extractOnClick = false;
    private Map<String, String> stringsDict;

    // TODO save in file with LayoutHandler text markers
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


    // TODO change see other parsers
    public StyleHandler(String path, Map<String, String> stringsDict) {
        resourceFile = path + File.separator + file;
        this.stringsDict = stringsDict;
    }

    // returns a mapping between the string variables and their value
    public Map<String, Style> parseResource() {
        Path file = Paths.get(resourceFile);
        if (!Files.exists(file)) {
            logger.error("file not found for parsing: " + resourceFile);
            return null;
        }
        SAXParserFactory factory = SAXParserFactory.newInstance();
        try {
            SAXParser parser = factory.newSAXParser();
            parser.parse(resourceFile, this);
        } catch (ParserConfigurationException e) {
            // TODO add saveStatFile
            logger.error("ParserConfig error");
        } catch (SAXException e) {
            logger.error("SAXException : xml not well formed: " + resourceFile);
        } catch (IOException e) {
            logger.error("IO error");
        }
        // return the results of the analysis
        return mapping;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {

        if (qName.equals("style")) {
            // extract name and potential parent of this style
            String styName = attributes.getValue("name");
            String parent = attributes.getValue("parent");
            if (!StringUtils.isBlank(parent) && !parent.contains("@android")) {
                String[] splittedparent = parent.split("@style/");
                if (splittedparent.length == 2) {
                    parent = splittedparent[1];
                }
            } else {
                parent = "";
            }
            tmpStyle = new Style(styName, parent);
        } else {
            if (qName.equals("item")) {
                // extract name and remove the "android:"
                String attributeName = attributes.getValue("name");
                if (!StringUtils.isBlank(attributeName)) {
                    String splittedAttributes[] = attributeName.split(":");
                    if (splittedAttributes.length == 2) {
                        attributeName = splittedAttributes[1];
                        // save that a textMarker or a onClick has been found
                        if (textMarkers.contains(attributeName)) {
                            extractText = true;
                        } else if ("onClick".equals(attributeName)) {
                            extractOnClick = true;
                        }
                    }
                }
            }
        }
    }

    public String getStringValueFromStringName(String stringName) {
        if (stringsDict == null) return "";
        String res = stringsDict.get(stringName);
        if (StringUtils.isBlank(res) || res.equals("null"))
            return "";
        else
            return res;
    }

    // Triggered when the end of tag is found
    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (qName.equals("style")) {
            if (tmpStyle != null) {
                mapping.put(tmpStyle.getName(), tmpStyle);
            } else {
                Helper.saveToStatisticalFile("StyleHandler: found end of style tag but don't have tmpStyle element");
            }
        }
    }

    @Override
    public void characters(char[] ch, int start, int length)
            throws SAXException {
        if (extractOnClick) {
            String tmp = String.copyValueOf(ch, start, length).trim();
            if (!StringUtils.isBlank(tmp)) {
                Listener listener = new Listener("onClick", true, String.copyValueOf(ch, start, length).trim(), "default_value");
                tmpStyle.setOnClickMethod(listener);
                extractOnClick = false;
            }
        } else if (extractText) {
            String value = String.copyValueOf(ch, start, length).trim();
            // TODO the same as in LayoutHandler : refactor
            if (value.contains("@android:string/"))
                // resolve variable name to real string/value and add it to the text variable (joined by "#")
                tmpStyle.addText(stringDefaultValues.get(value.replace("@android:string/", "")));
                // process string variables and concret text
            else {
                String tmpText = value;
                // add the string variable
                if (value.contains("@string/")) {
                    String var = value.replaceAll("@\\+?string/", "");
                    tmpStyle.addText(getStringValueFromStringName(var));
                } else {
                    // add the concrete text to the text variable
                    tmpStyle.addText(value);
                }

            }
            extractText = false;
        }
    }
}
