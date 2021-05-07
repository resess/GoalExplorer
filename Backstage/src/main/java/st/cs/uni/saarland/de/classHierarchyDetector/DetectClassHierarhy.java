package st.cs.uni.saarland.de.classHierarchyDetector;

import com.thoughtworks.xstream.XStream;
import soot.PackManager;
import soot.Scene;
import soot.SootClass;
import soot.options.Options;
import st.cs.uni.saarland.de.helpClasses.Helper;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Created by avdiienko on 23/04/16.
 */
public class DetectClassHierarhy {
    public static void main(String[] args) throws Exception {
        //TestApp.initializeSootForUiAnalysis("testApps/app-debug.apk", "/Users/avdiienko/Documents/adt-bundle/sdk/platforms/android-22/android.jar", false);
        List<String> jars = new ArrayList<>();
        jars.add("tmp/android.jar");
        jars.add("tmp/commons-io-2.5.jar");
        jars.add("tmp/commons-net-3.4.jar");
        jars.add("tmp/commons-codec-1.10.jar");
        jars.add("tmp/android-support-v4.jar");
        jars.add("tmp/google-play-services.jar");
        Options.v().set_process_dir(jars);
        Options.v().set_allow_phantom_refs(true);


        Scene.v().loadNecessaryClasses();

        PackManager.v().runPacks();

        Set<String> classes = loadApiClasses();
        Map<SootClass, List<SootClass>> classToSuperclasses = new HashMap<>();
        ClassTree tree = new ClassTree();

        final AtomicInteger interfaces = new AtomicInteger(0);
        final AtomicInteger notfound = new AtomicInteger(0);
        for (String cl : classes) {
            if (!cl.contains("android")) {
                continue;
            }
            if (Scene.v().getSootClassUnsafe(cl) != null) {
                SootClass sootClass = Scene.v().getSootClass(cl);
                if (classToSuperclasses.containsKey(sootClass)) {
                    continue;
                }
                if (sootClass.isAbstract()) {
                    classToSuperclasses.put(sootClass, Collections.singletonList(Scene.v().getSootClass("java.lang.Object")));
                    continue;
                }
                if (sootClass.isInterface()) {
                    System.out.println(cl + " is interface");
                    interfaces.incrementAndGet();
                } else {
                    List<SootClass> superClasses = Scene.v().getActiveHierarchy().getSuperclassesOfIncluding(sootClass);
                    if (superClasses.size() == 1) {
                        System.out.println(cl + " has no superclasses");
                        notfound.incrementAndGet();
                        continue;
                    }
                    int counter = superClasses.size() - 1;
                    for (int i = counter; i >= 0; i--) {
                        String curNode = superClasses.get(i).getName();
                        if (tree.getNodeByName(curNode) == null) {
                            ClassNode newNode = new ClassNode(curNode);
                            newNode.setRootDepth(superClasses.size() - i - 1);
                            newNode.setSignificance(0);
                            tree.addNode(newNode);
                            if (i != counter) {
                                String prev = superClasses.get(i + 1).getName();
                                ClassNode parent = tree.getNodeByName(prev);
                                try {
                                    newNode.setParent(parent);
                                    parent.addChild(newNode);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    throw e;
                                }
                            }
                        }
                    }
                    //classToSuperclasses.put(sootClass, superClasses);
                }
                continue;
            } else {
                System.out.println(cl + " is not found");
                notfound.incrementAndGet();
                continue;
            }
        }
        ClassNode root = tree.getRoot();
        XStream xStream = new XStream();
        xStream.processAnnotations(ClassNode.class);
        xStream.processAnnotations(ClassTree.class);
        //xStream.setMode(XStream.NO_REFERENCES);

        try (BufferedWriter bw = new BufferedWriter(
                new OutputStreamWriter(
                        new FileOutputStream("res/hierarchy.xml"), StandardCharsets.UTF_8))) {
            bw.append(xStream.toXML(root));
        } catch (IOException exception) {
            Helper.saveToStatisticalFile(exception.getMessage());
        }

        System.out.println("Classes: " + classes.size());
        System.out.println("Interfaces: " + interfaces.get());
        System.out.println("Not found in Scene: " + notfound.get());
        System.out.println("Classes that are ok: " + classToSuperclasses.keySet().size());

    }

    public static Set<String> loadApiClasses() throws IOException {

        List<String> strings = Files.readAllLines(Paths.get("res/api_class_all_list.txt"));//"res/sapi_class_all_list.txt"));
        Set<String> classes = strings.stream().map(x -> x.trim()).distinct().collect(Collectors.toSet());
        return classes;
    }
}
