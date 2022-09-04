package st.cs.uni.saarland.de.searchDialogs;


import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import st.cs.uni.saarland.de.entities.Application;
import st.cs.uni.saarland.de.entities.Dialog;
import st.cs.uni.saarland.de.helpMethods.CheckIfMethodsExisting;
import st.cs.uni.saarland.de.testApps.AppController;
import st.cs.uni.saarland.de.testApps.Content;

public class SearchDialogMain {
	protected final Logger logger = LoggerFactory.getLogger(this.getClass());

	public void getDialogsOfApp(Set<DialogInfo> dialogs){
		processDialogResults(dialogs);
	}
	
	public void processDialogResults(Set<DialogInfo> dialogs){
		for (DialogInfo dia: dialogs){
			//logger.debug("Processing dialog info {}", dia);
			if (dia.isFinished()){
				CheckIfMethodsExisting checkHelper = CheckIfMethodsExisting.getInstance();

				String posText = checkHelper.getResolvedText(dia.getPosText());
				
				String negText = checkHelper.getResolvedText(dia.getNegText());
				if(!StringUtils.isBlank(negText))
					negText = negText.toUpperCase(Locale.ROOT);
				//logger.debug("The negative text {} and resolved text {}", dia.getNegText(), negText);
				
				String neuText = checkHelper.getResolvedText(dia.getNeutralText());

				
				String msg = checkHelper.getResolvedText(dia.getMessage());
				
				String title = checkHelper.getResolvedText(dia.getTitle());			
				
				
//				Dialog:String pid, String titleText, String classWhereItIsShown, String posText, String negText, String message
				Dialog diaElement = new Dialog(Content.getInstance().getNewUniqueID(), title, dia.getActivity(), posText, negText, msg);
				
				if (dia.getPosListener() != null && dia.getPosListener().size() > 0){
					diaElement.setPosListener(dia.getPosListener());
				}
				
				if (dia.getNegativeListener() != null){
					diaElement.setNegativeListener(dia.getNegativeListener());
				}
				
				if (dia.getNeutralListener() != null)
					diaElement.setNeutralListener(dia.getNeutralListener());
				
				if (!neuText.equals("")) {
					neuText = neuText.toUpperCase(Locale.ROOT);
					diaElement.setNeutralText(neuText);
				}
				
				if (dia.getItemListener() != null)
					diaElement.setItemListener(dia.getItemListener());
				
				diaElement.setItemTexts(checkHelper.getResolvedText(dia.getItemTextsArrayID()));
				diaElement.setDialogCreationMethodSignature(dia.getMethodSignature());
				//diaElement.set(dia.getMethodSignature());

				AppController appController = AppController.getInstance();

				CheckIfMethodsExisting checker = CheckIfMethodsExisting.getInstance();
				if (checker.isNameOrText(dia.getActivity())){
					appController.addXMLLayoutFileToActivity(dia.getActivity(), diaElement.getId());
					appController.addDialog(diaElement);
				}
			}
		}
	}
	
}
