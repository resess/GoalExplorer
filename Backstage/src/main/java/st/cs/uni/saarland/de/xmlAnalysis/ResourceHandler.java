package st.cs.uni.saarland.de.xmlAnalysis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

// TODO rename to sth with string
public abstract class ResourceHandler extends DefaultHandler {

//    public static final String file = "";
    // stores mappings between the string variable name and the string value
    private Map<String, String> mapping = new HashMap<String, String>();
    private String resourceFile; // folder to res from the app
    private final Logger logger = LoggerFactory.getLogger(Thread.currentThread().getName());
    //"/res/values/"

    // FIXME: add parsing of <string_array> tag, Resources.getStringArray()
    // FIXME: add parsing of <plurals>
    // FIXME: according to specification mapping can be in any arbitrary file in 'res/values/' folder
    // FIXME: currently no support for styling like this <string name="welcome">Welcome to <b>Android</b>!</string>
    // FIXME: remove formatting symbols like this %1$s %2$d and &lt;b>

    // TODO change see other parsers
    public ResourceHandler(String path, String file) {
        resourceFile = path + File.separator + file;
    }

    protected void addItem(String name, String value) {
        mapping.put(name, value);
    }

    // TODO change see other parsers
    // returns a mapping between the string variables and their value
    public Map<String, String> parseResource() {
        Path file = Paths.get(resourceFile);
        if (!Files.exists(file)) {
            logger.error("file not found for parsing: " + resourceFile);
            return new HashMap<>();
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

}
