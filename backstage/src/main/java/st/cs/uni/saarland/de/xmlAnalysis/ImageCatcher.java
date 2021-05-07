package st.cs.uni.saarland.de.xmlAnalysis;

import com.google.common.io.Files;
import st.cs.uni.saarland.de.entities.Application;
import st.cs.uni.saarland.de.entities.AppsUIElement;
import st.cs.uni.saarland.de.helpClasses.Helper;
import st.cs.uni.saarland.de.testApps.Content;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;

// TODO rename inconsistent
// TODO rewrite completly...
public class ImageCatcher {

	// Map<Filename, FileType>, eg: <bild1, .png> or <bild1, .9.png>
	private static List<Map<String, String>> drawables = new ArrayList<Map<String, String>>();
	// list of the paths of each found drawable
	private static List<String> drawableMapPaths = new ArrayList<String>();
	// set of paths of drawable folders for different screen sizes and resolutions (currently not analysed)
	private static Set<String> drawablePathNotAnalysed = new HashSet<String>();
	private File resFolder; // res folder of the app

	private static final Logger logger = LoggerFactory.getLogger(Thread.currentThread().getName());
	
	public Application runImageCatcher(Application app){
		resFolder = new File(Content.getInstance().getAppOutputDir()+ File.separator +"res");
		// folder where later on all found images get stored
		File imageFolder = new File(Content.getInstance().getAppOutputDir()+ File.separator +"images");

		// create the folder where all images that are found are later stored
		if (!imageFolder.exists()){
			boolean created = imageFolder.mkdirs();
			if (!created){
				throw new IllegalArgumentException("Couldn't create ImageFolder at: " + imageFolder.getAbsolutePath());
			}
		}

		// check if the resource directory of the app is existing
		// TODO check unneccesaary? isn't it check somewhere else-> XMLParserMain-init? if not should be
		if (!(resFolder.exists() && resFolder.isDirectory())){
			throw new IllegalArgumentException("ImageCatcher didn't get the resFolder: " + resFolder.getAbsolutePath());
		}

		// list of folders/files in the resource folder
		File[] filesInResFolder = resFolder.listFiles();
		for (File f: filesInResFolder){
			// analyse all drawable folders (there are many-> different folders for different screen sizes/resolutions..)
			if (f.isDirectory() && f.getName().contains("drawable")){
				// TODO remember anywhere: ... other files would have duplicated image names
				// ( two images for two diff. screen sizes, at the moment: problem which one to pick...)
				// disadvantage: sometime images are missing in the default folder
				// only the drawable folder is analysed
				if (!f.getName().equals("drawable")){
					// remember the folders that are not analysed
					drawablePathNotAnalysed.add(f.getAbsolutePath());
				}
			}
		}

		// analyse files....
		getDrawableMap(resFolder + File.separator + "drawable");
		
		
		for (AppsUIElement uiE: app.getAllUIElementsOfApp()){
			if (uiE.hasImageElement()){
				List<File> nestedImages = new ArrayList<File>();
				Set<String> toAddImagesWithType = new HashSet<String>();
				
				for (String imageName : uiE.getDrawableNames()){
					File imageFile = searchImage(imageName);
					if (imageFile != null){
						toAddImagesWithType.add(imageFile.getName());
						copyImage(imageFile, imageFolder);
						// check if image is xml or a normal image file
						if (".xml".equals( imageFile.getName().substring(imageFile.getName().lastIndexOf(".")) )){
							nestedImages.addAll(getNestedImagesFromXMLFile(imageFile));
						}
					}else{
						logger.error("ImageFile not found in ImageCatcher, imageName: " + imageName);
					}
				}
				uiE.addTypeOfImageElements(toAddImagesWithType);
				if (nestedImages != null){
					for (File image: nestedImages){
						copyImage(image, imageFolder);
						uiE.addDrawableName(image.getName());
					}
				}
			}
		}
		return app;
	}
	
	// copies specified image to the imageFolder
	public void copyImage(File image, File imageFolder){
		if (image.exists()){
			if (imageFolder.exists() && imageFolder.isDirectory()){
				try {
					File newFile = new File(imageFolder + File.separator + image.getName());
					Files.copy(image, newFile);
				} catch (IOException e) {
					logger.error(e.getMessage());
				}
			}else{
				logger.error("ImageFolder not existing or not folder:" + imageFolder.getAbsolutePath());
			}
		}else{
			logger.error("Image file not existing:" + image.getAbsolutePath());
		}
	}
	
	
	// searches the specified image first in the default drawables folder,
		// if not found there, it searches it in every drawable folder
		// the method will stop at the first occurence of an image with that name
	public File searchImage(String imageName){
		
		for (int i = 0; i < drawables.size(); i++){
			Map<String, String> drawableMap = drawables.get(i);
			if (drawableMap.containsKey(imageName)){
				String pathToImage = drawableMapPaths.get(i);
				File imageFile = new File(pathToImage + File.separator + imageName + drawableMap.get(imageName));
				if (imageFile.exists()){
					return imageFile;
				}else{
					logger.error("ImageFile was not existing: expected at: " + imageFile.getAbsolutePath());
					return null;
				}
			}
			
		}
		
		List<String> drawablesThatGetsAnalysed = new ArrayList<String>();
		File imageFile = null;
		
		for (String pathToNewDrawableFolder: drawablePathNotAnalysed){
			drawablesThatGetsAnalysed.add(pathToNewDrawableFolder);
			Map<String, String> newDrawableMap = getDrawableMap(pathToNewDrawableFolder);
			if (newDrawableMap != null && newDrawableMap.containsKey(imageName)){
				imageFile = new File(pathToNewDrawableFolder + File.separator + imageName + newDrawableMap.get(imageName));
				if (imageFile.exists()){
					break;
				}else{
					logger.error("ImageFile was not existing: expected at: " + imageFile.getAbsolutePath());
					break;
				}
			}
		}
		
		for (String path1 : drawablesThatGetsAnalysed){
			drawablePathNotAnalysed.remove(path1);
		}
		
		return imageFile;
	}
	
	
	// returns a map of all found files inside the folder at folderPath to their types
	// only call this method if folderName does not exist in drawableMapPaths
	public Map<String, String> getDrawableMap(String folderPath){
		File drawableFolder = new File(folderPath);
		// create new map for matching...
		Map<String, String> drawMap = new HashMap<String, String>();

		// check if the specified drawable folder exists
		if (drawableFolder.exists()){
			// remember the path of the map
			drawableMapPaths.add(drawableFolder.getAbsolutePath());

			File[] filesInDir = drawableFolder.listFiles();
			for (File file : filesInDir){
				// TODO rewrite maybe to regex,-> why is the point saved inside the map?
				// split the file name into name [0] and type[1](without any .)
				// (not correct: it splits at the last "." not the first)
				/*String[] nameSplitted = file.getName().split("\\.(?=[^\\.]+$)");
				if (nameSplitted.length > 1){
					drawMap.put(nameSplitted[0], nameSplitted[1]);
				}else{
					logger.debug("possible image file without any point!: " + file.getName());
					Helper.saveToStatisticalFile("possible image file without any point!: " + file.getName());
				}*/
				String nameSplitted[] = file.getName().split("\\.");
				if (nameSplitted.length > 1){
					String nameWithoutType = nameSplitted[0];
					String fileType = file.getName().substring(file.getName().indexOf("."));
					drawMap.put(nameWithoutType, fileType);
				}else{
					logger.debug("possible image file without any point!: " + file.getName());
					Helper.saveToStatisticalFile("possible image file without any point!: " + file.getName());
				}
				
			}

			// add all found drawables to the main drawable map
			// TODO why is here a new drawMap created???
			drawables.add(drawMap);
			
		}else{
			logger.error("expected drawable folder but was not found here: " + drawableFolder.getAbsolutePath());
			Helper.saveToStatisticalFile("expected drawable folder but was not found here: " + drawableFolder.getAbsolutePath());
			return null;
		}
		// return the matchings of all files to their types
		return drawMap;
	}

	// returns a list of filepaths of images that were found in the xmlImage file
	public List<File> getNestedImagesFromXMLFile(File xmlImage){
		// create a new list where the new found images are saved
		List<File> nestedImages = new ArrayList<File>();
		// initialise the parser to analyse the xml image file
		SAX_XMLImageHandler xmlImageHandler = new SAX_XMLImageHandler(xmlImage.getAbsolutePath());
		// newDrawables is a list of image files that were found in the analysed file
		Set<String> newDrawables = xmlImageHandler.parseResource();

		// check for each new found image file, if it is also an xml image file (and therefore need further analysis)
		for (String newDraw: newDrawables){

			File imageFile = searchImage(newDraw);
			if (imageFile != null){
				nestedImages.add(imageFile);
				// check if image is xml or a normal image file
				if (".xml".equals( imageFile.getName().substring(imageFile.getName().lastIndexOf(".")) )){
					List<File> newNestedImages = getNestedImagesFromXMLFile(imageFile);
					nestedImages.addAll(newNestedImages);	
				}
			}else{
				logger.error("NewDrawableFile not found in ImageCatcher/nestedImages, newDrawableFile: " + newDraw);
			}
		}
		
		return nestedImages;
	}
	
	
}
