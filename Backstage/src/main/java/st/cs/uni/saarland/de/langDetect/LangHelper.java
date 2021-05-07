package st.cs.uni.saarland.de.langDetect;

import com.cybozu.labs.langdetect.Detector;
import com.cybozu.labs.langdetect.DetectorFactory;
import com.cybozu.labs.langdetect.LangDetectException;
import com.cybozu.labs.langdetect.Language;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import st.cs.uni.saarland.de.xmlAnalysis.ResourceHandler;
import st.cs.uni.saarland.de.xmlAnalysis.StringsHandler;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;


/**
 * Created by kuznetsov on 23/02/16.
 * https://shuyo.wordpress.com/2011/11/28/language-detection-supported-17-language-profiles-for-short-messages/
 * https://github.com/shuyo/language-detection/blob/wiki/ProjectHome.md
 *
 * @misc{nakatani2010langdetect, title  = {Language Detection Library for Java},
 * author = {Shuyo Nakatani},
 * url    = {https://github.com/shuyo/language-detection},
 * year   = {2010}
 * }
 */
public class LangHelper {
    private static LangHelper langHelper;
    private static final String PROFILE_FOLDER = "backstage/res/langProfiles/";
    public static Logger logger = LoggerFactory.getLogger(LangHelper.class);
    private static final double enRatio = 0.9;
    private static final double langRatio = 0.2;

    public LangHelper() {
        try {
            DetectorFactory.loadProfile(PROFILE_FOLDER);
        } catch (LangDetectException e) {
            logger.error("langDetect failed to initialize");
        }
    }

    public static LangHelper getInstance() {
        if (langHelper == null) {
            langHelper = new LangHelper();
        }
        return langHelper;
    }

    private String getStrings(String pathToAppFolder) {
        Path path = Paths.get(pathToAppFolder, "res", "values");
        Path pathEn = Paths.get(pathToAppFolder, "res", "values-en");
        ResourceHandler stringsParser = new StringsHandler(path.toString());
        Collection<String> stringsValues = stringsParser.parseResource().values();
        String text = stringsValues.stream().collect(Collectors.joining(". "));
        int dictSize = stringsValues.size();
        if (Files.exists(pathEn)) {
            ResourceHandler stringsParserEn = new StringsHandler(pathEn.toString());
            Collection<String> stringsValuesEn = stringsParserEn.parseResource().values();
            String textEn = stringsValuesEn.stream().collect(Collectors.joining(". "));
            int dictSizeEn = stringsValuesEn.size();
            if (dictSizeEn / dictSize > enRatio) {//enough values in values-en/strings.xml
                return textEn;
            }
            if (dictSizeEn / dictSize > langRatio) {//enough values in values-en/strings.xml
                return text.concat(textEn);
            }
        }
        return text;
    }

    public String detect(String pathToAppFolder) {
        try {
            Detector detector = DetectorFactory.create();
            String text = getStrings(pathToAppFolder);
            detector.append(text);
            return detector.detect();
        } catch (LangDetectException e) {
            e.printStackTrace();
            return "NAN";
        }
    }

    public static ArrayList<Language> detectLangs(String text) throws LangDetectException {
        Detector detector = DetectorFactory.create();
        detector.append(text);
        return detector.getProbabilities();
    }

    public boolean isEnglish(String pathToAppFolder) {
        String language = detect(pathToAppFolder);
        //logger.info("LANGUAGE: "+language);
        if (!"en".equals(language)) {
            logger.warn("Non english language detected: " + language);
            return false;
        }
        return true;
    }
}
