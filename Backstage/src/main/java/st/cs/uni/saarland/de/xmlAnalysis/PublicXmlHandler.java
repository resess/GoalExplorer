package st.cs.uni.saarland.de.xmlAnalysis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

// analysis of the public.xml file
public class PublicXmlHandler extends DefaultHandler {

    // saves mappings of id variable name and type to the id value
    // eg <id:myButton, 2145457512> or <string:myText, 2145654512>
    private Map<String, Integer> mapping = new HashMap<>();
    private String valuesFolder; // folder res from the unpacked app
    private final Logger logger = LoggerFactory.getLogger(Thread.currentThread().getName());
    private static final String file = "public.xml"; // relative path to the public.xml file

    public PublicXmlHandler(String path) {
        valuesFolder = path + File.separator + file;
    }

    // Triggered if a start tag is detected
    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        // every id tag is a public tag with specific attributes
        if ("public".equals(qName)) {
            // get the variable name of the id
            String name = attributes.getValue("name");
            // get the id value connected to the extracted name of the variable
            Integer id = Integer.parseInt((attributes.getValue("id")).replace("0x", ""), 16);
            // get the type of the id, eg default id, string id, layout id, drawable id ...
            String type = attributes.getValue("type");
            // store the found id mapping
            mapping.put(type + ":" + name, id);
        }
    }

    // Triggered if an end tag is detected
    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
    }

    // Triggered if characters between two tags are detected
    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
    }

    // returns a mapping between the string variables and their value
    public Map<String, Integer> parseResource() {
        Path file = Paths.get(valuesFolder);
        if (!Files.exists(file)){
            logger.error("file of public xml handler does not exists");
            return mapping;
        }
        SAXParserFactory factory = SAXParserFactory.newInstance();
        try {
            SAXParser parser = factory.newSAXParser();
            parser.parse(valuesFolder, this);
        } catch (Exception e) {
            logger.error("ParserConfig error");
        }
        // return the results of the analysis
        return mapping;
    }
}
