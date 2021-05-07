package st.cs.uni.saarland.de.saveData;

import com.opencsv.CSVWriter;
import de.linguatools.disco.CorruptConfigFileException;
import de.linguatools.disco.DISCO;
import de.linguatools.disco.TextSimilarity;
import de.linguatools.disco.WrongWordspaceTypeException;
import edu.smu.tspell.wordnet.Synset;
import edu.smu.tspell.wordnet.SynsetType;
import edu.smu.tspell.wordnet.VerbSynset;
import edu.smu.tspell.wordnet.WordNetDatabase;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.IntStream;

public class UseDISCO {

    public static final String DISCO_DIR = "/Users/kuznetsov/LAB/workspace/disco/enwiki-20130403-sim-lemma-mwl-lc";
    public static final String WORDNET_DICT_DIR = "/Users/kuznetsov/LAB/workspace/WordNet-3.0/3.0/dict";// should be installed in <WORDNET_FOLDER>/3.0

    public static void checkWordnet(String[] args) throws IOException {
        String wordNetDict = WORDNET_DICT_DIR;
        if (args.length == 0) {
            System.out.println("1st arg - input file with verbs; 2d arg - output file with hypernyms");
            System.exit(0);
        }
        String inputFile = args[1];
        String outputFile = args[2];

        WordNetDatabase database = WordNetDatabase.getFileInstance();
        System.setProperty("wordnet.database.WORDNET_FOLDER", wordNetDict);
        System.out.println("WORDNET created");

        List<String> docs1 = Files.readAllLines(Paths.get(inputFile));
        CSVWriter csvWriter = new CSVWriter(new BufferedWriter((new FileWriter(new File(outputFile)))), ';');
        docs1.forEach(word -> {
            Synset[] synsets = database.getSynsets(word, SynsetType.VERB);
            String hypernim = "";
            if (synsets != null && synsets.length > 0) {
                VerbSynset synset = (VerbSynset) synsets[0];
                VerbSynset[] hypernims = synset.getHypernyms();
                if (hypernims != null && hypernims.length > 0) {
                    hypernim = hypernims[0].getWordForms()[0];
                }
            }
            String[] line = {word, hypernim};
            csvWriter.writeNext(line);
        });

        csvWriter.close();

    }

    public static void main(String[] args) throws IOException, CorruptConfigFileException, WrongWordspaceTypeException {
        if (args.length > 4 && "text".equals(args[3])) discoTextSimilarity(args);
        else
            discoSimilarity(args);
    }

    public static void discoSimilarity(String[] args) throws IOException, CorruptConfigFileException, WrongWordspaceTypeException {

        // first command line argument is path to the DISCO word space directory
        if (args.length == 0) {
            System.out.println("1st arg - input file with verbs; 2d arg - output file with similarity matrix; 3 - path to DISCO ");
            System.exit(0);
        }
        String inputFile = args[0];
        String outputFile = args[1];
        String discoDir=args[2];
        DISCO.SimilarityMeasure similarity= DISCO.SimilarityMeasure.COSINE;
        if (args.length>3)
            similarity= DISCO.SimilarityMeasure.KOLB;
        DISCO disco;
        try {
            disco = new DISCO(discoDir, true);
        } catch (FileNotFoundException | CorruptConfigFileException ex) {
            System.out.println("Error creating DISCO instance: " + ex);
            return;
        }

        if (disco.wordspaceType != DISCO.WordspaceType.SIM) {
            System.out.println("The word space " + discoDir + " is not of type SIM!");
            return;
        }
        System.out.println("DISCO created");
        List<String> docs1 = Files.readAllLines(Paths.get(inputFile));
        int n = docs1.size();
        String[][] simMatrix = new String[n][n];
        for (int i = 0; i < n; i++) {
            System.out.print(" " + i);
            for (int j = i; j < n; j++) {
                String text1 = docs1.get(i);
                String text2 = docs1.get(j);
                float simScore = disco.semanticSimilarity(text1, text2, similarity);//secondOrderSimilarity(text1, text2)/2;//
                if (simScore < 0 || simScore>1 ) System.out.println("ERROR :" + simScore + " " + text1 + " " + text2);
                simMatrix[i][j] = String.valueOf(simScore);
                simMatrix[j][i] = simMatrix[i][j];
            }
        }
        CSVWriter csvWriter = new CSVWriter(new BufferedWriter((new FileWriter(new File(outputFile)))), ';');
        for (int i = 0; i < n; i++) {
            csvWriter.writeNext(simMatrix[i]);
        }
        csvWriter.close();
    }

    public static void discoTextSimilarity(String[] args) throws IOException, CorruptConfigFileException, WrongWordspaceTypeException {

        // first command line argument is path to the DISCO word space directory
        String discoDir = DISCO_DIR;
        if (args.length == 0) {
            System.out.println("TEXT 1st arg - input file with verbs; 2d arg - output file with similarity matrix");
            System.exit(0);
        }
        String inputFile = args[0];
        String outputFile = args[1];
        DISCO disco;
        try {
            disco = new DISCO(discoDir, true);
        } catch (FileNotFoundException | CorruptConfigFileException ex) {
            System.out.println("Error creating DISCO instance: " + ex);
            return;
        }

        if (disco.wordspaceType != DISCO.WordspaceType.SIM) {
            System.out.println("The word space " + discoDir + " is not of type SIM!");
            return;
        }
        System.out.println("DISCO created");
        List<String> docs1 = Files.readAllLines(Paths.get(inputFile));
        int n = docs1.size();
        String[][] simMatrix = new String[n][n];

        IntStream.range(0, n)
                //.parallel()
                .forEach(i -> {
                    simMatrix[i][i] = "1";
                    for (int j = i+1; j < n; j++) {
                        String text1 = docs1.get(i);
                        String text2 = docs1.get(j);
                        TextSimilarity textSimilarity = new TextSimilarity();
                        try {
                            float simScore = textSimilarity.textSimilarity(text1, text2, disco, DISCO.SimilarityMeasure.KOLB);
                            simMatrix[i][j] = String.valueOf(simScore);
                            simMatrix[j][i] = simMatrix[i][j];
                        } catch (CorruptConfigFileException | IOException e) {
                            e.printStackTrace();
                        }
                    }
                    System.out.println(".");
                });
        CSVWriter csvWriter = new CSVWriter(new BufferedWriter((new FileWriter(new File(outputFile)))), ';');
        for (int i = 0; i < n; i++) {
            csvWriter.writeNext(simMatrix[i]);
        }
        csvWriter.close();
    }

    public static void main2(String[] args) throws IOException, CorruptConfigFileException {
        String discoDir = DISCO_DIR;
        if (args.length == 0) {
            System.out.println("1st arg - verbs file, 2d arg - output file with aliases");
            return;
        }
        String inputFile = args[1];
        String outputFile = args[2];
        if (args.length == 0) {
            System.out.println("1st arg - verbs file, 2d arg - output file with similarity matrix");
            return;
        }
        String wordNetDict = WORDNET_DICT_DIR;
        DISCO disco;
        try {
            disco = new DISCO(discoDir, true);
        } catch (FileNotFoundException | CorruptConfigFileException ex) {
            System.out.println("Error creating DISCO instance: " + ex);
            return;
        }
        if (disco.wordspaceType != DISCO.WordspaceType.SIM) {
            System.out.println("The word space " + discoDir + " is not of type SIM!");
            return;
        }
        System.out.println("DISCO created");
        System.setProperty("wordnet.database.WORDNET_FOLDER", wordNetDict);
        WordNetDatabase database = WordNetDatabase.getFileInstance();
        System.out.println("WORDNET created");

        List<String> verbs = Files.readAllLines(Paths.get(inputFile));
        CSVWriter csvWriter = new CSVWriter(new BufferedWriter((new FileWriter(new File(outputFile)))), ';');
        int maxlen = 10;
        verbs.forEach(v -> {
            System.out.println();
            System.out.print(v);

            Synset[] synset = database.getSynsets(v, SynsetType.VERB);
//            String top =  Arrays.stream(synset).flatMap(s ->  Arrays.stream(s.getWordForms())).forEach(
//
//            );
            if (synset != null) {
                int max = 0;
                String w = "";
                for (int i = 0; i < synset.length; i++) {
                    String[] wf = synset[i].getWordForms();
                    for (int j = 0; j < wf.length; j++) {
                        String word = wf[j];
                        try {
                            int freq = disco.frequency(word);
                            if (max < freq) {
                                max = freq;
                                w = word;
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                String[] line = {v, w};
                csvWriter.writeNext(line);
            }

        });
        csvWriter.close();
    }

}
