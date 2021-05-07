package st.cs.uni.saarland.de.saveData;

import com.google.common.collect.Lists;
import com.opencsv.CSVWriter;
import com.thoughtworks.xstream.XStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import st.cs.uni.saarland.de.entities.*;
import st.cs.uni.saarland.de.xmlAnalysis.ResourceHandler;
import st.cs.uni.saarland.de.xmlAnalysis.StringsHandler;
import st.cs.uni.saarland.de.xmlAnalysis.StyleHandler;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by kuznetsov on 24/02/16. partially refactored
 */
public class UIAnalysis {
    private static Logger logger = LoggerFactory.getLogger(UIAnalysis.class);
    private static AtomicLong uuid = new AtomicLong();

    public static void main(String args[]) throws IOException {
        UIAnalysis uiAnalysis = new UIAnalysis();
        if (args.length == 0) {
            System.out.println("1 - serialized, 2 - output file prefix, 3 - folder with styles");
            return;
        }
        Path dir = Paths.get(args[0]).toRealPath();
        Path outputFile = Paths.get(args[1]);
        Path resDir = Paths.get(args[2]);
        uiAnalysis.processFiles(dir, outputFile, resDir);
    }

    public static String getID() {
        return String.valueOf(uuid.getAndIncrement());
    }

    /*
     *   this method is used with legacy data without test extracted from style files
     */
    public void processFiles(Path dir, Path outputFile, Path resDir) throws IOException {
        int nThreads = 1;//Runtime.getRuntime().availableProcessors();
        List<Path> fileList;
        try (Stream<Path> stream = Files.walk(dir, 3)) {
            fileList = stream.filter(path -> path.endsWith("appSerialized.txt") && Files.exists(path))
                    .map(path -> path.resolveSibling("")).collect(Collectors.toList());
        }
        ExecutorService pool = Executors.newFixedThreadPool(nThreads);
        int partitionSize = Math.max(fileList.size(), nThreads) / nThreads;
        List<List<Path>> partitions = Lists.partition(fileList, partitionSize);

        ArrayList<Future<?>> futures = new ArrayList<>(nThreads);
        futures.addAll(partitions.stream().map(partition -> pool.submit(() -> {
            try (CSVWriter csvWriter = new CSVWriter(new BufferedWriter(
                    (new FileWriter(new File(outputFile.toString() + "_" + Thread.currentThread().getId() + ".txt")))),
                    ';')) {
                ArrayList<String> header = Label.getRowHeader();
                csvWriter.writeNext(header.toArray(new String[header.size()]));
                partition.forEach(fName -> {
                    try {
                        List<List<String>> e = extractText(fName, resDir.resolve(fName.getFileName()));
                        e.forEach(x -> {
                            csvWriter.writeNext(x.toArray(new String[x.size()]));
                        });
                    } catch (Exception e) {
                        logger.error("error " + e.getMessage() + " while processing " + fName, e);
                        e.printStackTrace();
                    }
                });
            } catch (IOException e) {
                logger.error("We done fucked up!", e);
            }
        })).collect(Collectors.toList()));

        futures.forEach(future -> {
            try {
                future.get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        });
        pool.shutdown();
        try {
            pool.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    public List<List<String>> extractText(Path apkToolOutputPath, Path pathToRes) throws IOException {
        Path path = apkToolOutputPath.resolve("appSerialized.txt");
        final String fileName = path.toRealPath().toString();
        logger.info(fileName);
        File uiFile = new File(fileName);
        if (!uiFile.exists()) {
            return new ArrayList<>();
        }
        XStream xStream = new XStream();
        xStream.alias("AppsUIElement", AppsUIElement.class);
        xStream.setMode(XStream.ID_REFERENCES);
        Application app = (Application) xStream.fromXML(uiFile);
        Map<String, String> stringValues = getStringValues(pathToRes);
        String pathToStyle = pathToRes.resolve("res").resolve("values").toString();
        StyleHandler styleParser = new StyleHandler(pathToStyle, stringValues);
        Map<String, Style> styleMap = styleParser.parseResource();
        if (null == styleMap)
            styleMap = new HashMap<>();
        try {
            List<List<String>> entries = processAppObject(app, styleMap);
            return entries;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("error while processing " + apkToolOutputPath, e);
            return new ArrayList<>();
        }
    }

    public List<List<String>> processAppObject(Application app, Map<String, Style> styleMap) {
        List<List<String>> results = new ArrayList<>();
        Set<Integer> unlinkedLayouts = new HashSet<>();
        Map<Integer, XMLLayoutFile> xmlLayoutFilesMap = app.getXMLLayoutFilesMap();
        unlinkedLayouts.addAll(xmlLayoutFilesMap.keySet());
        logger.info("LAYOUTS in total: # " + unlinkedLayouts.size());
        //process dialogs
        app.getDialogsOfApp().forEach(dialog -> {
            extractLabelsFromDialog(app, results, dialog.getId());
            unlinkedLayouts.remove(dialog.getId());
        });
        //FIXME: this is to support legacy code with bug in Activity.equals method
        Map<String, Activity> activityNames = app.getActivities().stream()
                .collect(Collectors.toMap(Activity::getName, x -> x, (a, s) -> {
                    a.getXmlLayouts().addAll(s.getXmlLayouts());
                    return a;
                }));
        // this should be used for normal case
        // Map<String, Activity> activityNames = app.getActivities().stream()
        //      .collect(Collectors.toMap(Activity::getName, x -> x));
        //get all fragments and add them to the activity
        for (Activity activity : app.getActivities()) {
            Set<Integer> layouts = activity.getXmlLayouts();
            while (!layouts.isEmpty()) { //resolve fragments including nested ones
                Set<Integer> fragments = extractFragments(layouts, app);
                fragments.removeAll(activity.getXmlLayouts());
                activity.getXmlLayouts().addAll(fragments);
                layouts = fragments;
            }
        }
        //remove all xmlLayouts bound to any activity
        app.getActivities().stream().map(Activity::getXmlLayouts).forEach(unlinkedLayouts::removeAll);
        logger.info("Activities are done. Remaining layouts: # " + unlinkedLayouts.size());
        //bind layouts to an activity based on listeners
        //match activity name with declaring class name
        for (Iterator<Integer> iterator = unlinkedLayouts.iterator(); iterator.hasNext(); ) {
            Integer layoutId = iterator.next();
            XMLLayoutFile layout = app.getXmlLayoutFile(layoutId);
            Set<String> declaringClasses = layout.getUIElementIDs().stream().map(app::getUiElement)
                    .map(AppsUIElement::getListernersFromElement).flatMap(Collection::stream)
                    .map(Listener::getDeclaringClass).filter(dc -> activityNames.keySet().contains(dc))
                    .collect(Collectors.toSet());
            if (!declaringClasses.isEmpty()) {
                logger.warn(String.format("%1$d Parent(s) for orphaned XMLLayout %2$d found in listeners",
                        declaringClasses.size(), layoutId));
                declaringClasses.forEach(d -> activityNames.get(d).getXmlLayouts().add(layoutId));
                iterator.remove();
            }
        }
        logger.info("Declaring class name layouts removed. Remaining layouts: # " + unlinkedLayouts.size());
        //bind layouts to an activity based on listeners
        //match activity name with the name of class for which the certain text is defined (if no, we have only default_value in a class:text map)
        for (Iterator<Integer> iterator = unlinkedLayouts.iterator(); iterator.hasNext(); ) {
            Integer layoutId = iterator.next();
            XMLLayoutFile layout = app.getXmlLayoutFile(layoutId);
            Set<String> declaringClasses = layout.getUIElementIDs().stream().map(app::getUiElement)
                    .map(AppsUIElement::getTextFromElement).map(Map::keySet).flatMap(Collection::stream)
                    .filter(dc -> activityNames.keySet().contains(dc)).collect(Collectors.toSet());
            if (!declaringClasses.isEmpty()) {
                logger.warn(String.format("%1$d Parent(s) for orphaned XMLLayout %2$d found in text",
                        declaringClasses.size(), layoutId));
                declaringClasses.forEach(d -> activityNames.get(d).getXmlLayouts().add(layoutId));
                iterator.remove();
            }
        }
        logger.info("Declaring class text layouts removed. Remaining layouts: # " + unlinkedLayouts.size());

        for (Activity activity : app.getActivities()) {
            String activityClass = activity.getName();
            String activityLabel = activity.getLabel();
            //            logger.info(activityClass);
            List<Label> labels = new ArrayList<>();
            for (int layID : activity.getXmlLayouts()) {
                //List<Label> labels = new ArrayList<>();//restrict context to layouts
                if (app.containsDialog(layID) && unlinkedLayouts.contains(layID)) {
                    unlinkedLayouts.remove(layID);
                    extractLabelsFromDialog(app, results,
                            layID); //no global context for dialogs //should not reach this line normally
                    logger.error("Dialog outside <dialog> list found: " + layID);
                    continue;
                }
                if (app.containsXMLLayoutFile(layID)) {
                    List<Label> layoutLabels = extractLabelsFromLayout(layID, activityClass, app, styleMap);
                    labels.addAll(layoutLabels);
                    //restrict context to layouts
                    //Set<String> context = makeContext(labels);
                    //context.add(Label.sanitise(activityLabel));
                    //labels.stream().filter(x -> x.hasCallback)
                    //      .forEach(label -> results.add(makeCsvRow(context, label)));
                }
                else {
                    logger.error(String.format("XMLLayoutFile with id: %s, of activity: %s - %s, not found", layID,
                            activityClass, activityLabel));
                }
            }
            Set<String> context = makeContext(labels);
            context.add(Label.sanitise(activityLabel));
            labels.stream().filter(x -> x.hasCallback).forEach(label -> results.add(makeCsvRow(context, label)));
            //            logger.info(String.format("%s - %d", activityClass, labels.size()));
        }

        //orphan layouts
        logger.info("ORPHAN LAYOUTS # " + unlinkedLayouts.size());
        Map<Integer, Set<Integer>> unlinkedContainers = new HashMap<>();
        for (Integer lid : unlinkedLayouts) {
            Set<Integer> layouts = extractFragment(lid, app);
            unlinkedContainers.put(lid, layouts);
            while (!layouts.isEmpty()) { //resolve fragments including nested ones
                Set<Integer> fragments = extractFragments(layouts, app);
                fragments.removeAll(unlinkedContainers.get(lid));
                unlinkedContainers.get(lid).addAll(fragments);
                layouts = fragments;
            }
        }
        Set<Integer> nonRootFragments = unlinkedContainers.values().stream().flatMap(Set::stream)
                .collect(Collectors.toSet());
        logger.info("ORPHAN LAYOUTS before filtering " + unlinkedContainers.keySet().size());
        unlinkedContainers.keySet().removeAll(nonRootFragments);
        logger.info("***ORPHAN LAYOUTS after filtering " + unlinkedContainers.keySet().size());
        unlinkedContainers.keySet().stream().filter(lid -> !app.getXmlLayoutFile(lid).getName().startsWith("abc_"))
                .forEach(layID -> {
                    XMLLayoutFile rootLayout = app.getXmlLayoutFile(layID);
                    List<Label> labels = extractLabelsFromLayout(layID, rootLayout.getName(), app, styleMap);
                    List<Label> fragmentLabels = unlinkedContainers.get(layID).stream()
                            .filter(lid -> !app.containsDialog(lid))
                            .map(lid -> extractLabelsFromLayout(lid, rootLayout.getName(), app, styleMap))
                            .flatMap(List::stream).collect(Collectors.toList());
                    labels.addAll(fragmentLabels);
                    Set<String> context = makeContext(labels);
                    labels.stream().filter(x -> x.hasCallback)
                            .forEach(label -> results.add(makeCsvRow(context, label)));
                });
        return results;
    }

    private Set<Integer> extractFragments(Collection<Integer> layoutIds, Application app) {
        return layoutIds.stream().map(x -> UIAnalysis.extractFragment(x, app)).flatMap(Set::stream)
                .collect(Collectors.toSet());
    }

    private static Set<Integer> extractFragment(Integer layoutId, Application app) {
        if (!app.containsXMLLayoutFile(layoutId)) {
            logger.error(String.format("Layout with id %s not found!", layoutId));
            return new HashSet<>(0);
        }

        return app.getXmlLayoutFile(layoutId).getUIElementIDs().stream().map(app::getUiElement)
                .filter(e -> e instanceof SpecialXMLTag).map(e -> (SpecialXMLTag) e).map(SpecialXMLTag::getXmlFileIds)
                .flatMap(Collection::stream).collect(Collectors.toSet());
    }

    /*
     * labels come from one container
     */
    private Set<String> makeContext(List<Label> labels) {
        Set<String> res = labels.stream().filter(Label::isAccepted).filter(x -> !x.isButton())
                .filter(x -> !x.hasCallback).map(x -> x.textValue.replace("#", " ")).collect(Collectors.toSet());
        return res;

    }

    private List<Label> extractLabelsFromLayout(int layoutID, String activityClass, Application app,
                                                Map<String, Style> styleMap) {
        List<Label> labels = new ArrayList<>();
        if (!app.containsXMLLayoutFile(layoutID)) {
            logger.error("Declared XMLLayout not found!");
            return labels;
        }
        XMLLayoutFile xmlLayoutFile = app.getXmlLayoutFile(layoutID);
        if (xmlLayoutFile == null) {
            logger.error("Null XMLLayout, though it should be there, id: " + layoutID);
            return labels;
        }
        String layoutName = xmlLayoutFile.getName().toLowerCase().replace("_", "");

        Set<Integer> elementIds = xmlLayoutFile.getUIElementIDs();
        for (int eID : elementIds) {
            AppsUIElement uiE = app.getUiElement(eID);
            if (uiE == null) {
                logger.warn("Element with id: " + eID + " wasn't found");
                continue;
            }
            if (uiE instanceof SpecialXMLTag) {
                continue;
            }
            String elementType = uiE.getKindOfUiElement() != null ? uiE.getKindOfUiElement() : "";
            String uiId = String.valueOf(uiE.getId());
            String varName = uiE.getTextVar();
            Map<String, String> textMap = uiE.getTextFromElement();
            List<String> textValues = textMap.values().stream().map(String::trim).filter(x -> !x.isEmpty())
                    .collect(Collectors.toList());
            //use parent styles only if no text and we have a styleMap attached (this is legacy mode)
            if (textValues.isEmpty() && !styleMap.isEmpty()) {
                textMap = getTextFromStyles(uiE.getStyles(), styleMap);
            }
            //get text specifically for this activity
            String callbackClass;
            if (textMap.containsKey(activityClass)) {
                callbackClass = activityClass;
            }
            else {
                //try to find fragment class, using heuristic:
                //TODO: we may still miss text values if a callback is defined in a parent activity
                callbackClass = textMap.keySet().stream().filter(x -> {
                    if (x.toLowerCase().endsWith("." + layoutName) || x.toLowerCase()
                            .endsWith("." + layoutName + "fragment"))
                        return true;
                    else if (layoutName.contains("fragment"))
                        return x.toLowerCase()
                                .endsWith(String.format(".%sfragment", layoutName.replace("fragment", "")));
                    return false;
                }).findFirst().orElse(Label.default_label);

                if (!Label.default_label.equals(callbackClass))
                    logger.info(
                            String.format("+++++ Activity %s;  class %s found in layout %s for element %s with text %s",
                                    activityClass, callbackClass, layoutName, uiId, textMap.get(callbackClass)));
            }
            String textForActivity = textMap.get(callbackClass);

            List<Listener> listeners = uiE.getListernersFromElement();
            boolean hasCallback = listeners.size() > 0;
            ;
            if (!Label.default_label.equals(callbackClass)) {
                hasCallback = listeners.size() > 0;
                //listeners.stream().filter(x -> x.getDeclaringClass().equals(callbackClass)).count() > 0;

            }

            boolean isOrphan = activityClass.equals(xmlLayoutFile.getName());

            //for orphan layouts
            if (isOrphan && textMap.keySet().stream().filter(x -> !Label.default_label.equals(x))
                    .count() > 0 && Label.default_label.equals(callbackClass))
                logger.info(String.format("***** Unknown declared Classes: %s; in orphaned layout: %s, activity %s",
                        textMap.keySet().stream().collect(Collectors.joining(", ")), xmlLayoutFile.getName(),
                        activityClass));


            //we don't split buttons now
            if (!textForActivity.isEmpty()) {//remove empty labels
                Optional<String> iconOpt = uiE.getDrawableNames().stream().filter(x -> x.endsWith(".png")).findFirst();
                String icon = iconOpt.isPresent() ? iconOpt.get() : Label.NO_ICON;
                Label label = new Label(app.getBaseName(), elementType, textForActivity, callbackClass, activityClass,
                        uiId, varName, hasCallback, textMap.get(Label.default_label), isOrphan, icon);
                if (label.isAccepted())
                    labels.add(label);

            }
            //split buttons:
            //                if (textForActivity.contains("#")) {
            //                    List<String> multiLabel = Arrays.asList(textForActivity.split("#")).stream().distinct().collect(Collectors.toList());
            //                    if (multiLabel.size() == 1 && elementType.toLowerCase().contains("button")) {
            //                        labels.add(new Label(elementType, multiLabel., classForActivity, uiID));
            //                    } else {
            //                        context.add(textForActivity);
            //                        logger.warn("Multilabel:" + textForActivity + " - skiping");
            //                    }
            //end split
        }

        return labels;
    }


    private Map<String, String> getTextFromStyles(Set<Style> styles, Map<String, Style> styleMap) {
        Map<String, String> textMap = new HashMap<>();
        String styleText = styles.stream().map(s -> s.getText().trim()).filter(s -> !s.isEmpty()).findFirst()
                .orElse("");
        if (styleText.isEmpty() && styleMap != null) {
            styleText = styles.stream().map(s -> getTextFromStyle(styleMap, s, 0)).filter(t -> !t.isEmpty()).findFirst()
                    .orElse("");
            //we have it here but not in getText of AppsUIElement because we missed styles hierarchy //TODO refactor
            //even if there are many styles attached take the first non-empty text only
        }
        if (!styleText.isEmpty())
            textMap.put(Label.default_label, styleText);
        return textMap;
    }


    private void extractLabelsFromDialog(Application app, List<List<String>> results, int layID) {
        Set<Label> labels = new HashSet<>();
        if (app.containsDialog(layID)) {
            Dialog dialog = app.getDialog(layID);
            String id = String.valueOf(dialog.getId());
            Set<String> context = new HashSet<>();
            context.add(Label.sanitise(dialog.getMessage()));
            context.add(Label.sanitise(dialog.getTitleText()));
            Listener posListener = dialog.getPosListener().stream().findFirst().orElse(null);
            String pp = dialog.getPosListener().stream().findFirst().map(Listener::getDeclaringClass)
                    .orElse("default_value");
            String posClass;
            if (posListener != null)
                posClass = posListener.getDeclaringClass();
            else
                posClass = "default_value";
            Listener negListener = dialog.getNegativeListener().stream().findFirst().orElse(null);
            String negClass;
            if (negListener != null)
                negClass = negListener.getDeclaringClass();
            else
                negClass = "default_value";
            Listener neutrListener = dialog.getNeutralListener().stream().findFirst().orElse(null);
            String neutrClass;
            if (neutrListener != null)
                neutrClass = neutrListener.getDeclaringClass();
            else
                neutrClass = "default_value";
            labels.add(Label.DialogLabel(app.getBaseName(), dialog.getPosText(), "-1" + id, posClass));
            labels.add(Label.DialogLabel(app.getBaseName(), dialog.getNegText(), "-2" + id, negClass));
            labels.add(Label.DialogLabel(app.getBaseName(), dialog.getNeutralText(), "-3" + id, neutrClass));
            labels.stream().filter(Label::isAccepted).forEach(l -> results.add(makeCsvRow(context, l)));
        }
    }

    private String getTextFromStyle(Map<String, Style> styleMap, Style style, int depth) {
        if (depth > 10)
            return "";
        if (null == style.getText() || style.getText().isEmpty()) {
            if (style.hasParent()) {
                String parentId = style.getParent();
                Style parent = styleMap.get(parentId);
                if (parent != null) {
                    return getTextFromStyle(styleMap, parent, depth + 1);
                }
            }
        }
        return style.getText();
    }

    private ArrayList<String> makeCsvRow(final Set<String> context, final Label label) {
        return label.getRow(context);
    }

    private Map<String, String> getStringValues(Path path) {
        ResourceHandler stringsParser = new StringsHandler(path.resolve("res").resolve("values").toString());
        Map<String, String> stringsValues = stringsParser.parseResource();
        File valuesEnFile = new File(path.resolve("res").resolve("values-en").toString());
        if (valuesEnFile.exists()) {
            ResourceHandler stringsParserEn = new StringsHandler(path.resolve("res").resolve("values-en").toString());
            Map<String, String> stringsValuesEn = stringsParserEn.parseResource();
            if (stringsValuesEn != null) {
                stringsValues.putAll(stringsValuesEn);
            }
        }
        return stringsValues;
    }

}
