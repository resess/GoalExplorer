package st.cs.uni.saarland.de.searchDynDecStrings;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import st.cs.uni.saarland.de.entities.Application;
import st.cs.uni.saarland.de.entities.AppsUIElement;
import st.cs.uni.saarland.de.helpMethods.CheckIfMethodsExisting;
import st.cs.uni.saarland.de.helpMethods.IntraprocAnalysis;
import st.cs.uni.saarland.de.testApps.AppController;
import st.cs.uni.saarland.de.testApps.Content;

public class SearchDynDecMain {
	private final Logger logger =  LoggerFactory.getLogger(Thread.currentThread().getName());

	// get the help methods for an intraprocedural analysis defined in the helpMethods package
	IntraprocAnalysis helpMethods = IntraprocAnalysis.getInstance();
	AppController appController = AppController.getInstance();
	
	public void searchDynDeclaredStrings(Set<DynDecStringInfo> strings){
		logger.info("<RunSoot to find dynamical declared strings>");
		processResults(strings);
		logger.info("</RunSoot to find dynamical declared strings>");
	}
	
	private void processResults(Set<DynDecStringInfo> strings){
		logger.debug("Processing dynamic string results");
		
		// the xmlParser is needed for reparsing the strings.xml and the public.xml file
			// this way the tool finds out the text of the string id
		
		for (DynDecStringInfo str: strings){
			//logger.debug("Dyn string {}", str);
			if (CheckIfMethodsExisting.getInstance().checkIfValueIsID(str.getUiEID())){

				CheckIfMethodsExisting check = CheckIfMethodsExisting.getInstance();
				Content content = Content.getInstance();
				// check if an old string Builder build was not completed
				if (str.getSearchedPlaceHolders() != null){
					// if it is not completed, take what is there:
					for (String pH : str.getSearchedPlaceHolders()){
						// remove the place holders
						str.replacePlaceHolder(pH, "");
					}
					String resStrBuilder = str.joinNotJoinedText();
					str.addText(resStrBuilder);
				}

				// process the text
				String[] idAndTexts = str.getText().split("#");
				String text = "";
				for (String idOrText: idAndTexts){
					if (check.checkIfValueIsID(idOrText)){
						String arrayValue = content.getArrayValueFromArrayID(idOrText);
						if (StringUtils.isBlank(arrayValue)){
							arrayValue = content.getStringValueFromStringId(idOrText);
						}
						if (!StringUtils.isBlank(arrayValue)){
							text = text + "#" + arrayValue;
						}
					}else{
						text = text + "#" + idOrText;
					}
				}
				
				if (text.startsWith("#"))
					text = text.replaceFirst("#", "");

				appController.addText(Integer.parseInt(str.getUiEID()), str.getDeclaredSootClass(), text);
				
			}
		}
	}

}
