package st.cs.uni.saarland.de.reachabilityAnalysis;

import com.google.common.collect.Lists;
import com.thoughtworks.xstream.XStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import st.cs.uni.saarland.de.helpClasses.Helper;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Created by avdiienko on 09/05/16.
 */
public class XmlResultsSaver implements ResultsSaver{
    Logger logger = LoggerFactory.getLogger("XMLResultsSaver");

    @Override
    public void save(Map<UiElement, List<ApiInfoForForward>> results) {
        logger.info("Saving results to xml...");
        if(results.size() == 0){
            logger.info("Nothing to save");
            return;
        }
        RAHelper.creatResultsDirIfNotExsist();

        results.keySet().forEach(x->{
            String line = String.format("%s -> %s APIs", x.toString(), results.get(x).size());
            logger.info(line);
            Helper.saveToStatisticalFile(line);
        });

        final int chunkSize = 100;
        final List<List<UiElement>> partionedKeys = Lists.partition(results.keySet().stream().collect(Collectors.toList()), chunkSize);
        AtomicInteger counter = new AtomicInteger(0);
        Helper.saveToStatisticalFile(String.format("Found %s partitions", partionedKeys.size()));
        logger.info(String.format("Found %s partitions", partionedKeys.size()));

        List<Future<Void>> tasks = new ArrayList<>();

        ThreadPoolExecutor mainExecutor = new ThreadPoolExecutor(RAHelper.numThreads, RAHelper.numThreads,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>());



        for(List<UiElement> keysInChunk: partionedKeys) {
            tasks.add(mainExecutor.submit(
                    (Callable<Void>) () -> {
                        Map<UiElement, List<ApiInfoForForward>> toSave = results.entrySet().stream().filter(x -> keysInChunk.contains(x.getKey())).collect(Collectors.toMap(p -> p.getKey(), p -> p.getValue()));

                        XStream xStream = new XStream();
                        xStream.processAnnotations(UiElement.class);
                        xStream.processAnnotations(ApiInfoForForward.class);
                        xStream.setMode(XStream.NO_REFERENCES);

                        try (BufferedWriter bw = new BufferedWriter(
                                new OutputStreamWriter(
                                        new FileOutputStream(String.format(Helper.getResultsDir() + File.separator + "%s_forward_apiResults_%s.xml", Helper.getApkName(), counter.incrementAndGet())), StandardCharsets.UTF_8))) {
                            bw.append(xStream.toXML(toSave));
                        } catch (IOException exception) {
                            Helper.saveToStatisticalFile(exception.getMessage());
                            logger.error(exception.getMessage());
                        }

                        return null;
                    }));
        }

        for(Future<Void> task : tasks){
            try {
                task.get();
            } catch (InterruptedException e) {
                Helper.saveToStatisticalFile(Helper.exceptionStacktraceToString(e));
                e.printStackTrace();
            } catch (ExecutionException e) {
                Helper.saveToStatisticalFile(Helper.exceptionStacktraceToString(e));
                e.printStackTrace();
            }
        }

        mainExecutor.shutdown();

        try {
            mainExecutor.awaitTermination(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            logger.error("Executor did not terminate correctly");
        }
    }
}
