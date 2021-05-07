package st.cs.uni.saarland.de.xmlAnalysis;

import org.apache.commons.lang3.StringUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import st.cs.uni.saarland.de.testApps.Content;

import java.util.HashSet;
import java.util.Set;

// TODO rename due to inconsistent name
public class ArrayHandler extends ResourceHandler {

    // Map<ArrayName, ArrayValue>: eg: myArray-> "Day1#Day2#Day3"
    private String name; // name of array which is currently analysed
    private Set<String> value = new HashSet<String>(); // values of the currently analysed array
    private boolean readItemValue = false; // variable to mark if an "item" tag has started (true) or has ended (false)
    private static final String file = "arrays.xml";


    // Triggered if a start tag is detected
    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        // check if the tag is an array definition tag
        if ("string-array".equals(qName)) {
            // set the name of the array
            name = attributes.getValue("name");
            // check if the tag is a item tag
        } else if ("item".equals(qName)) {
            // remember that an item tag has started
            readItemValue = true;
        }
    }

    // Triggered if an end tag is detected
    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        // check if the tag is an array definition tag
        if ("string-array".equals(qName)) {
            // add the array with its values(concatinated with "#") to the map
            this.addItem(name, String.join("#", value));
            // reinit the string values set
            value = new HashSet<String>();
            // reinit the name of the array
            name = "";
        } else if ("item".equals(qName)) {
            // remember that the item tag has ended
            readItemValue = false;
        }
    }

    // Triggered everytime if characters between tags are detected
    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        // only read strings between the start and end item tag
        if (readItemValue) {
            // get the string of the item
            String text = String.copyValueOf(ch, start, length).trim();
            if (!StringUtils.isBlank(text)) {
                // check if the found text is a string variable
                if (text.startsWith("@string")) {
                    // resolve the string variable to the exact value
                    text = Content.getInstance().getStringValueFromStringName(text.replace("@string/", ""));
                }
                // add the correct text to the value set (where all strings of the array are temporary stored)
                value.add(text);
            }
        }
    }

    // constructor of ArrayHandler: path is the path to the folder where the unpacked files of the app are stored
    public ArrayHandler(String path) {
        super(path, file);
    }

}
