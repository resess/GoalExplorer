package st.cs.uni.saarland.de.anomalyValidation;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import soot.PackManager;
import soot.Scene;
import soot.SootMethod;
import st.cs.uni.saarland.de.helpClasses.CallGraphAlgorithms;
import st.cs.uni.saarland.de.helpClasses.Helper;
import st.cs.uni.saarland.de.reachabilityAnalysis.ApiInfoForForward;
import st.cs.uni.saarland.de.reachabilityAnalysis.CallbackToApiMapper;
import st.cs.uni.saarland.de.reachabilityAnalysis.RAHelper;
import st.cs.uni.saarland.de.testApps.TestApp;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Created by avdiienko on 08/03/16.
 */
public class ApiTracer {
    public static void main(String[] args){
        Logger logger =  LoggerFactory.getLogger("ApiTracer");
        ApiTracerSettings settings = new ApiTracerSettings();
        JCommander jc = new JCommander(settings);

        try {
            jc.parse(args);
        }
        catch (ParameterException e){
            System.err.println(e.getMessage());
            jc.usage();
            System.exit(1);
        }

        Helper.initializeManifestInfo(settings.apkPath);

        TestApp.initializeSootForUiAnalysis(settings.apkPath, settings.androidJar, false, false);
        SootMethod callback = Scene.v().getMethod(settings.callback);
        List<String> classes = new ArrayList<>();
        classes.add(callback.getDeclaringClass().getName());
        //superClasses
        classes.addAll(Scene.v().getActiveHierarchy().getSuperclassesOf(callback.getDeclaringClass()).
                stream().filter(x -> !Helper.isClassInSystemPackage(x.getName())).map(x -> x.getName()).collect(Collectors.toList()));
        //interfaces
        classes.addAll(callback.getDeclaringClass().getInterfaces().stream().filter(x -> !Helper.isClassInSystemPackage(x.getName()))
                .map(x -> x.getName()).collect(Collectors.toList()));

        TestApp.initializeSoot(settings.apkPath, settings.androidJar, classes, CallGraphAlgorithms.CHA);
        callback = Scene.v().getMethod(settings.callback);
        Scene.v().setEntryPoints(Collections.singletonList(callback));
        // Run the soot-based operations
        PackManager.v().getPack("wjpp").apply();
        PackManager.v().getPack("cg").apply();
        PackManager.v().getPack("wjtp").apply();
        List<ApiInfoForForward> apisFound = new ArrayList<>();
        CallbackToApiMapper mapper = new CallbackToApiMapper(callback, settings.elementId, 1, 1, settings.depth, true, apisFound, true);
        mapper.setSourcesAndSinks(Collections.singletonList(settings.api).stream().collect(Collectors.toSet()));
        try {
            RAHelper.getImplementersOfExecutorServiceClass();
            mapper.call();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Map<SootMethod, List<SootMethod>> callStack = mapper.getCallStack();
        List<SootMethod> calls = callStack.get(Scene.v().getMethod(settings.api));

        if(calls == null){
            System.out.println("API was not found!");
            return;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Callback %s with id %s -> %s\n", settings.callback, settings.elementId, settings.api));
        AtomicInteger apisCount = new AtomicInteger(0);
        calls.forEach(call-> sb.append(String.format("%s: %s\n", apisCount.incrementAndGet(),call.getActiveBody())));
        System.out.println(sb.toString());
    }
}
