package st.cs.uni.saarland.de.xmlAnalysis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.util.HashMap;
import java.util.Map;

// TODO rename to sth with string
public class StringsHandler extends ResourceHandler {

	// stores mappings between the string variable name and the string value
	private Map<String, String> mapping = new HashMap<String, String>();
	private String name; // saves the currently analysed variable name
	private String value; // saves the currently analysed string value
	public static final String file = "strings.xml";

	// FIXME: add parsing of <string_array> tag, Resources.getStringArray()
	// FIXME: add parsing of <plurals>
	// FIXME: according to specification mapping can be in any arbitrary file in 'res/values/' folder
	// FIXME: currently no support for styling like this <string name="welcome">Welcome to <b>Android</b>!</string>
	// FIXME: remove formatting symbols like this %1$s %2$d and &lt;b>

	// Triggered if a start tag is detected
	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		// search for string tags
		if ("string".equals(qName)) {
			// stores the name of the string variable
			name = attributes.getValue("name");
		}
	}

	// Triggered if an end tag is detected
	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		// find the end tag of the string tag
		if ("string".equals(qName)) {
			// save the variable with its value
			addItem(name, value);
		}
	}

	// Triggered if characters between two tags are detected
	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		// saves the value of the string
		value = String.copyValueOf(ch, start, length).trim();
	}

	public StringsHandler(String path) {
		super(path, file);
	}
}
