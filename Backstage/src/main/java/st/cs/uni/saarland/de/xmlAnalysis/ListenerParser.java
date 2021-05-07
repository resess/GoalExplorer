package st.cs.uni.saarland.de.xmlAnalysis;

import java.io.File;

import java.io.FilenameFilter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;


@Deprecated
public class ListenerParser extends DefaultHandler {
	String path;
	String name;
	private final Logger log =  LoggerFactory.getLogger(Thread.currentThread().getName());
	private Map<String, String> listeners = new HashMap<String, String>();

	public ListenerParser(String path) {
		this.path = path;
	}

	public Map<String, String> parse() {
		SAXParserFactory factory = SAXParserFactory.newInstance();
		try {
			SAXParser parser = factory.newSAXParser();
			File layoutDir = new File(path);
			File[] xmlFiles = layoutDir.listFiles(new FilenameFilter() {
				public boolean accept(File dir, String name) {
					return name.toLowerCase().endsWith(".xml");
				}
			});
			for (File xmlFile : xmlFiles) {
				this.name = xmlFile.getName().replace(".xml", "");
				parser.parse(xmlFile, this);
			}
		} catch (ParserConfigurationException e) {
			log.error("ParserConfig error");
		} catch (SAXException e) {
			log.error("SAXException : xml not well formed: " + name + " / " + e.getMessage());
		} catch (IOException e) {
			log.error("IO error");
		}
		return listeners;
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		// get listener
		String listener = attributes.getValue("android:onClick");
		String id = strip(attributes.getValue("android:id"));
		if (null != listener && null != id) {
			listeners.put(id + ":" + name, listener);
		}
	}

	private String strip(String line) {
		if (null != line) {
			String[] res = line.split("/");
			if (res.length > 1)
				return (res[1]);
		}
		return null;
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
	}

}
