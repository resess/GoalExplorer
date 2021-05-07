package st.cs.uni.saarland.de.xmlAnalysis;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class SAX_XMLImageHandler extends DefaultHandler {

	// set of path of drawables that are found in pathToImageFile file
	private Set<String> drawablesFound = new HashSet<String>();
	private String pathToImageFile;
	private final Logger log =  LoggerFactory.getLogger(Thread.currentThread().getName());

	// Triggered when the start of tag is found.
	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {

		// get the drawable attribute
		String drawableFullName = attributes.getValue("android:drawable");
		// check if there was a attribute called android:drawable
		if (drawableFullName != null){
			// check if there is a drawable variable
			// TODO process android default drawables
			if (drawableFullName.contains("drawable/")) {
				// split the drawable keyword
				String[] tmp = drawableFullName.split("drawable/");
				// add the value to the drawableFound list
				// tmp.length-1 is called for /drawable/drawable/pic
				drawablesFound.add(tmp[tmp.length-1]);
			}
		}


		/*
		for (int i = 0; i < attributes.getLength(); i++) {
			String attrFullName = attributes.getQName(i);
			String value = attributes.getValue(i);


			if (attrFullName.equals("android:drawable")){
				 if (value.contains("drawable/")) {
					String[] tmp = value.split("drawable/");
					if (tmp.length > 1){
						String drawableName = tmp[1];
						drawablesFound.add(drawableName);	
					}
				 }
			}
		}*/
		
	}
	
	@Override
	public void endElement(String uri, String localName, String qName)throws SAXException {
	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {			
	}

	public SAX_XMLImageHandler(String pathToXMLImageFile){
		this.pathToImageFile = pathToXMLImageFile;
	}

	public Set<String> parseResource() {
		SAXParserFactory factory = SAXParserFactory.newInstance();
		try {
			SAXParser parser = factory.newSAXParser();
			parser.parse(pathToImageFile, this);
		} catch (ParserConfigurationException e) {
			log.error("ParserConfig error");
		} catch (SAXException e) {
			log.error("SAXException : image xml not well formed");
		} catch (IOException e) {
			log.error("IO error");
		}
		return drawablesFound;
	}

}
