package st.cs.uni.saarland.de.searchDialogs;

import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import st.cs.uni.saarland.de.entities.Application;
import st.cs.uni.saarland.de.entities.Dialog;
import st.cs.uni.saarland.de.helpMethods.CheckIfMethodsExisting;
import st.cs.uni.saarland.de.testApps.AppController;
import st.cs.uni.saarland.de.testApps.Content;

public class SearchDialogMain {

	public void getDialogsOfApp(Set<DialogInfo> dialogs){
		processDialogResults(dialogs);
	}
	
	public void processDialogResults(Set<DialogInfo> dialogs){
		for (DialogInfo dia: dialogs){
			//System.out.println(dia.toString());
			if (dia.isFinished()){
				CheckIfMethodsExisting checkHelper = CheckIfMethodsExisting.getInstance();
				
				String posText = checkHelper.getResolvedText(dia.getPosText());
				
				String negText = checkHelper.getResolvedText(dia.getNegText());
				
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
				
				if (!neuText.equals(""))
					diaElement.setNeutralText(neuText);
				
				if (dia.getItemListener() != null)
					diaElement.setItemListener(dia.getItemListener());
				
				diaElement.setItemTexts(checkHelper.getResolvedText(dia.getItemTextsArrayID()));

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
