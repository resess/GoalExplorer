package st.cs.uni.saarland.de.saveData;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.google.common.collect.Lists;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.thoughtworks.xstream.XStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import st.cs.uni.saarland.de.reachabilityAnalysis.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Created by avdiienko on 15/12/15.
 */

class ApiResultsProcessorSettings {
    private final static String INPUT_DIR = "-i";
    private final static String OUTPUT_DIR = "-o";

    @Parameter(names = INPUT_DIR, description = "Path to the root dir containing _forward_apiResults.xml")
    public String inputDir;

    @Parameter(names = OUTPUT_DIR, description = "Path to the output dir")
    public String outputDir;

}

public class ApiResultsProcessor {
    private static Logger logger = LoggerFactory.getLogger(ApiResultsProcessor.class);
    private static Map<String, String> susi = new HashMap<>();
    private static Map<String, String> susiUri = new HashMap<>();
    private static Map<String, String> susiContent = new HashMap<>();
    private static Set<String> droidSafeApis = new HashSet<>();
    private static List<String> lifecycleMethods = new ArrayList<>();
    private static List<String> specialTriggers = new ArrayList<String>() {{
        add("android.app.Activity");
        add("android.content.BroadcastReceiver");
        add("android.app.Service");
    }};

    public ApiResultsProcessor() throws IOException {
        readAPIMapping("res");
    }

    protected void countMultipleCallBacks(Path xmlFile, List<String[]> result) {
        logger.info(String.format("Processing %s", xmlFile.getFileName()));
        String apkName = xmlFile.getFileName().toString().split("_forward_apiResults")[0];
        XStream xStream = new XStream();
        xStream.processAnnotations(UiElement.class);
        xStream.processAnnotations(ApiInfoForForward.class);
        xStream.setMode(XStream.NO_REFERENCES);
        Map<UiElement, List<ApiInfoForForward>> resultsOfApp = (Map<UiElement, List<ApiInfoForForward>>) xStream
                .fromXML(xmlFile.toFile());
        for (UiElement uiElem : resultsOfApp.keySet()) {
            String uiSignature = uiElem.signature;//parseSignature(uiElem.signature);
            String subSignature = parseSignatureMultipleCallbacks(uiSignature);
            String trigger;
            String uiIDtmp = String.valueOf(uiElem.globalId);
            String elementId = uiElem.elementId;

            if (elementId.startsWith("-")) {
                uiIDtmp = elementId + uiIDtmp;//for Dialogs
                uiElem.kindOfElement = "buttonDialog";
            }
            final String uiID = uiIDtmp;
            trigger = truncateSpecialTriggers(uiElem.kindOfElement);
            if (trigger == null || !trigger.toLowerCase().contains("button"))
                continue;
            if (resultsOfApp.get(uiElem) == null || resultsOfApp.get(uiElem).isEmpty())
                continue;
            String[] res = {apkName, uiID, subSignature};
            result.add(res);
        }
    }

    public static void main(String[] args) throws IOException {
        ApiResultsProcessorSettings settings = new ApiResultsProcessorSettings();
        JCommander jc = new JCommander(settings);
        try {
            jc.parse(args);
        } catch (ParameterException e) {
            jc.usage();
            System.err.println(e.getMessage());
            System.exit(1);
        }

        Path inputDir = Paths.get(settings.inputDir);
        Path outputDir;
        if (settings.outputDir == null || settings.outputDir.isEmpty())
            outputDir = inputDir;
        else
            outputDir = Paths.get(settings.outputDir);
        ApiResultsProcessor apiResultsProcessor = new ApiResultsProcessor();
        apiResultsProcessor.processFiles(inputDir, outputDir);

    }

    private void processFiles(Path inputDir, Path outputDir) throws IOException {
        int nThreads = 1;//Runtime.getRuntime().availableProcessors();
        ExecutorService pool = Executors.newFixedThreadPool(nThreads);
        List<Path> filesList = Files.walk(inputDir)
                .filter(p -> p.getFileName().toString().contains("_forward_apiResults") && p.getFileName().toString()
                        .endsWith(".xml")).collect(Collectors.toList());
        int partitionSize = Math.max(filesList.size(), nThreads) / nThreads;
        List<List<Path>> partitions = Lists.partition(filesList, partitionSize);
        ArrayList<Future<?>> futures = new ArrayList<>();
        for (List<Path> partition : partitions) {
            futures.add(pool.submit(() -> {
                try (CSVWriter csvWriter = new CSVWriter(new BufferedWriter(new FileWriter(
                        new File(outputDir.toString() + "/api_" + Thread.currentThread().getId() + ".txt"))), ';')) {
                    List<String[]> lines = new ArrayList<>();
                    ArrayList<String> header = getRowHeader();
                    csvWriter.writeNext(header.toArray(new String[header.size()]));
                    partition.forEach(p -> {
                        deserializeAndSave(p, lines);
                        ////countMultipleCallBacks(p, lines);
                        csvWriter.writeAll(lines);
                        csvWriter.flushQuietly();
                        lines.clear();
                    });

                } catch (IOException e) {
                    logger.error("Unexpected error occurred.");
                }
            }));
        }
        for (Future<?> future : futures) {
            try {
                future.get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
        pool.shutdown();
        try {
            pool.awaitTermination(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        while (!pool.isTerminated()) {
            try {
                Thread.sleep(3000);
                logger.warn("Waiting for term");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        logger.info(outputDir.toString());
    }

    private String parseSignature(String signature) {
        Matcher m = Pattern.compile(".*\\s([\\w<>]+)\\(.*\\)").matcher(signature);
        if (m.find()) {
            return m.group(1);
        }
        System.err.println("Signature is illegal " + signature);
        return "";
    }

    private String parseSignatureMultipleCallbacks(String signature) {
        Matcher m = Pattern.compile(".*\\s([\\w<>]+\\(.*\\))").matcher(signature);
        if (m.find()) {
            return m.group(1);
        }
        System.err.println("Signature is illegal " + signature);
        return "";
    }

    private void readAPIMapping(String path) throws IOException {
        CSVReader reader = new CSVReader(new FileReader(path + "/susi.txt"), ';');
        List<String[]> lines = reader.readAll();
        susi = lines.stream().collect(Collectors.toMap(x -> x[0], x -> x[1], (val1, val2) -> resolveNoCat(val1, val2)));
        CSVReader readerUri = new CSVReader(new FileReader(path + "/susi_uri.txt"), ';');
        List<String[]> linesUri = readerUri.readAll();
        susiUri = linesUri.stream()
                .collect(Collectors.toMap(x -> x[0], x -> x[1], (val1, val2) -> resolveNoCat(val1, val2)));
        CSVReader readerContent = new CSVReader(new FileReader(path + "/susi_content.txt"), ';');
        List<String[]> linesContent = readerContent.readAll();
        susiContent = linesContent.stream()
                .collect(Collectors.toMap(x -> x[0], x -> x[1], (val1, val2) -> resolveNoCat(val1, val2)));

        CSVReader droidSafeContent = new CSVReader(new FileReader(path + "/SourcesAndSinksDroidSafe.txt"), ';');
        List<String[]> linesDroidSafe = droidSafeContent.readAll();

        linesDroidSafe.forEach(line -> {
            if (line.length > 0) {
                droidSafeApis.add(line[0].split("->")[0].trim());
            }
        });
        lifecycleMethods = Files.lines(Paths.get(path + "/lifecycle.txt")).collect(Collectors.toList());
        /*CSVReader intentConent = new CSVReader(new FileReader(path + "/intents.txt"), ';');
        List<String[]> linesIntentContent = intentConent.readAll();
        intents = linesIntentContent.stream()
                .collect(Collectors.toMap(x -> x[0], x -> x[1], (val1, val2) -> resolveNoCat(val1, val2)));*/
    }

    private String resolveNoCat(String val1, String val2) {
        if (val1.contains("NO_CATEGORY"))
            return val2;
        else if (val2.contains("NO_CATEGORY"))
            return val1;
        return val2;
    }

    @SuppressWarnings("unchecked")
    public void deserializeAndSave(Path xmlFile, List<String[]> result) {
        logger.info(String.format("Processing %s", xmlFile.getFileName()));
        String apkName = xmlFile.getFileName().toString().split("_forward_apiResults")[0];
        XStream xStream = new XStream();
        xStream.processAnnotations(UiElement.class);
        xStream.processAnnotations(ApiInfoForForward.class);
        xStream.processAnnotations(IntentInfo.class);
        xStream.setMode(XStream.NO_REFERENCES);
        Map<UiElement, List<ApiInfoForForward>> resultsOfApp = (Map<UiElement, List<ApiInfoForForward>>) xStream
                .fromXML(xmlFile.toFile());
        resultsOfApp.keySet().forEach(uiElem -> {
            processUIElement(result, apkName, resultsOfApp, uiElem);
        });
    }

    private void processUIElement(List<String[]> result, String apkName,
                                  Map<UiElement, List<ApiInfoForForward>> resultsOfApp, UiElement uiElem) {
        final List<ApiInfoForForward> results = resultsOfApp.get(uiElem); //retrieve results before you change uiElem object
        String elementId = uiElem.elementId;
        final String declaringSootClass = ""
                .equals(uiElem.declaringSootClass) ? Label.default_label : uiElem.declaringSootClass;
        String globalId = String.valueOf(uiElem.globalId);
        if (elementId.startsWith("-")) {//elementId < 0
            globalId = elementId + globalId;//for Dialogs
            uiElem.kindOfElement = "buttonDialog"; //here you change uiElem object!
        }
        if ("0".equals(globalId)) {
            globalId = elementId;
        }
        final String uiId = globalId;
        String uiElement = uiElem.kindOfElement;
        String event = uiElem.signature;

        if (results == null || results.isEmpty()) {
            String[] row = makeNoAPIRow(apkName, uiId, uiElement, event, declaringSootClass);
            result.add(row);
            return;
        }
        results.forEach(apiElem -> {
            ApiSignatureResult api = parseApiSignature(apiElem);
            if (api == null)
                return;
            String[] row = makeRow(apkName, uiId, uiElement, event, api.signature, api.sType, api.category,
                    String.valueOf(apiElem.depthMethodLevel), declaringSootClass);
            result.add(row);
        });
    }

    private ApiSignatureResult parseApiSignature(ApiInfoForForward apiElem) {
        String category = susi.get(apiElem.signature);
        if (category != null) {
            if(category.equals("NO_CATEGORY")){
                String className = apiElem.signature.replace("<","").replace(">","").split(":")[0];
                Pattern r = Pattern.compile("(.+)\\.[^\\.]+$");
                Matcher m = r.matcher(className);
                if (m.find()) {
                    category = m.group(1);
                }
            }
            return new ApiSignatureResult(category, apiElem.signature, "NORMAL");
        }
        if (!apiElem.signature.contains(":")) {
            return null;
        }
        String methodName = apiElem.signature.split(":")[1].trim().replace(">", "");
        if (START_ACTIVITY_CONSTANTS.getStartActivityMethods().contains(methodName)) {
            return extractIntentSignature((IntentInfo) apiElem);
        }
        if (CONTENT_RESOLVER_CONSTANTS.getPrefixes().contains(apiElem.signature.split("\\(")[0])) {
            return extractResolverSignature(apiElem.signature);
        }
        return null;
    }

    protected ApiSignatureResult extractResolverSignature(String signature) {
        String category;
        if (signature.contains("content:")) {
            Matcher m = Pattern.compile(".*(content://.*?),.*").matcher(signature);
            m.find();
            category = susiContent.get(m.group(1));
            if (category == null)
                signature = "CONTENT_RESOLVER_CUSTOM_CONTENT";
            else
                signature = category;
        }
        else {
            Matcher m = Pattern.compile(".*(,|\\()(.*?: android\\.net\\.Uri.*?),.*").matcher(signature);
            if (m.find()) {
                String crUri = m.group(2);
                category = susiUri.get(crUri);
                if (!crUri.startsWith("android.") && !crUri.contains(".android."))
                    category = "CONTENT_RESOLVER_CUSTOM_URI";
                else
                    signature = crUri;
            }
            else
                category = "CONTENT_RESOLVER";
        }
        if (category == null || category.isEmpty())
            category = "NAN";
        return new ApiSignatureResult(category, signature, "RESOLVER");
    }

    protected ApiSignatureResult extractIntentSignature(IntentInfo intentInfo) {
        String category;
        String className = intentInfo.getClassName();
        String uri = intentInfo.getData();
        String action = intentInfo.getAction();
        if (className != null && !className.isEmpty())
            category = "EXPLICIT_INTENT";
        else if (action != null || uri != null) {
            category = "IMPLICIT_INTENT";
            if (action == null || !action.contains("android"))
                action = "CUSTOM_ACTION";
            if (uri != null && !uri.isEmpty()) {
                uri = uri.contains(":") ? uri.split(":")[0] + ":" : "CUSTOM_URI";
            }
        }
        else {
            category = "UNCLASSIFIED_INTENT";
        }
        List<String> tmp = new ArrayList<>();
        tmp.add("INTENT");
        if (uri != null)
            tmp.add(uri);
        if (action != null)
            tmp.add(action);
        String signature = String.join(".", tmp);
        return new ApiSignatureResult(category, signature, "INTENT");
    }

    private String[] makeRow(String apkName, String uiID, String trigger, String event, String signature,
                             String apiType, String category, String depth, String declaringClass) {
        List<String> row = new ArrayList<>();
        row.add(apkName);
        row.add(uiID);
        row.add(trigger);
        row.add(event);
        row.add(signature.replaceAll("[\n\r]",""));
        row.add(droidSafeApis.contains(signature) ? "SENSITIVE" : "NON_SENSITIVE");
        row.add(apiType);
        row.add(category);
        row.add(depth);
        row.add(declaringClass);
        return row.toArray(new String[row.size()]);
    }

    private ArrayList<String> getRowHeader() {
        return new ArrayList<String>() {
            {
                add("apk");
                add("uid");
                add("type");
                add("callback");
                add("api");
                add("sensitive");
                add("apitype");
                add("category");
                add("depth");
                add("declaringClass");
            }
        };
    }

    private String[] makeNoAPIRow(String apkName, String uiId, String trigger, String event,
                                  String declaringSootClass) {
        return makeRow(apkName, uiId, trigger, event, "NOAPI", "SENSITIVE", "NO_API", "0", declaringSootClass);
    }

    private String truncateSpecialTriggers(String trigger) { //get last part after dot abc.def.xyz -> xyz
        if (specialTriggers.contains(trigger)) {
            Matcher m = Pattern.compile(".*\\.([^\\.]*)$").matcher(trigger);
            if (m.find())
                return m.group(1);
        }
        return trigger;
    }

    protected class ApiSignatureResult {
        protected String category;
        protected String signature;
        protected String sType;

        public ApiSignatureResult(String category, String signature, String sType) {
            this.category = category;
            this.signature = signature;
            this.sType = sType;
        }
    }
}