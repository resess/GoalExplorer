package st.cs.uni.saarland.de.saveData;

import com.opencsv.CSVWriter;
import de.linguatools.disco.CorruptConfigFileException;
import de.linguatools.disco.WrongWordspaceTypeException;
import edu.sussex.nlp.jws.JWS;
import edu.sussex.nlp.jws.JiangAndConrath;
import edu.sussex.nlp.jws.Lin;
import edu.sussex.nlp.jws.Resnik;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.TreeMap;


// 'TestExamples': how to use Java WordNet::Similarity
// David Hope, 2008
public class UseWordnet {
    public static final String WORDNET_DIR = "/Users/kuznetsov/LAB/workspace/WordNet-3.0";// should be installed in <WORDNET_FOLDER>/3.0

    public static void leskSimilarity(String[] args) throws IOException, CorruptConfigFileException, WrongWordspaceTypeException {

        JWS ws = new JWS(WORDNET_DIR, "3.0");
        if (args.length == 0) {
            System.out.println("1st arg - input file with verbs; 2d arg - output file with similarity matrix");
            System.exit(0);
        }
        // first command line argument is path to the DISCO word space directory
        String inputFile = args[1];//"/Users/kuznetsov/LAB/workspace/backstage/results0303/labels_verbs_unique.txt";
        String outputFile = args[2];//"/Users/kuznetsov/LAB/workspace/backstage/results0303/labels_matrix_worndet.txt";//args[2];

        List<String> docs1 = Files.readAllLines(Paths.get(inputFile));
        int n = docs1.size();
        String[][] simMatrix = new String[n][n];

        //AdaptedLeskTanimotoNoHyponyms lesk = ws.getAdaptedLeskTanimotoNoHyponyms();
        Resnik lesk = ws.getResnik();
        for (int i = 0; i < n; i++) {
            System.out.print(" " + i);
            for (int j = i; j < n; j++) {
                System.out.print(".");
                String word1 = docs1.get(i);
                String word2 = docs1.get(j);
                double simScore = lesk.max(word1, word2, "v");
                simMatrix[i][j] = String.valueOf(simScore);
                simMatrix[j][i] = simMatrix[i][j];
            }
            System.out.println(" ");
        }
        CSVWriter csvWriter = new CSVWriter(new BufferedWriter((new FileWriter(new File(outputFile)))), ';');
        for (int i = 0; i < n; i++) {
            csvWriter.writeNext(simMatrix[i]);
        }
        csvWriter.close();
    }

    public static void main(String[] args) throws WrongWordspaceTypeException, IOException, CorruptConfigFileException {
        leskSimilarity(args);
    }

    public static void test() {

        String dir = WORDNET_DIR;
        JWS ws = new JWS(dir, "3.0");

//   Option 2 : specify the version of WordNet you want to use and the particular IC file that you wish to apply
        //JWS ws = new JWS(WORDNET_FOLDER, "3.0", "ic-bnc-resnik-add1.dat");


// 2. EXAMPLES OF USE:

// 2.1 [JIANG & CONRATH MEASURE]
        JiangAndConrath jcn = ws.getJiangAndConrath();
        System.out.println("Jiang & Conrath\n");
// all senses
        TreeMap<String, Double> scores1 = jcn.jcn("apple", "banana", "n");            // all senses
        //TreeMap<String, Double> 	scores1	=	jcn.jcn("apple", 1, "banana", "n"); 	// fixed;all
        //TreeMap<String, Double> 	scores1	=	jcn.jcn("apple", "banana", 2, "n"); 	// all;fixed
        for (String s : scores1.keySet())
            System.out.println(s + "\t" + scores1.get(s));
// specific senses
        System.out.println("\nspecific pair\t=\t" + jcn.jcn("apple", 1, "banana", 1, "n") + "\n");
// max.
        System.out.println("\nhighest score\t=\t" + jcn.max("apple", "banana", "n") + "\n\n\n");


// 2.2 [LIN MEASURE]
        Lin lin = ws.getLin();
        System.out.println("Lin\n");
// all senses
        TreeMap<String, Double> scores2 = lin.lin("apple", "banana", "n");            // all senses
        //TreeMap<String, Double> 	scores2	=	lin.lin("apple", 1, "banana", "n"); 	// fixed;all
        //TreeMap<String, Double> 	scores2	=	lin.lin("apple", "banana", 2, "n"); 	// all;fixed
        for (String s : scores2.keySet())
            System.out.println(s + "\t" + scores2.get(s));
// specific senses
        System.out.println("\nspecific pair\t=\t" + lin.lin("apple", 1, "banana", 1, "n") + "\n");
// max.
        System.out.println("\nhighest score\t=\t" + lin.max("apple", "banana", "n") + "\n\n\n");

// ... and so on for any other measure
    }
} // eof
