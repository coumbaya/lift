package myLIFT;

import com.fourspaces.couchdb.Document;
import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static myLIFT.Main.collectionName;
import static myLIFT.Main.enableFragm;
import static myLIFT.Main.setCouchDB;
import static myLIFT.Main.setMonetDB;
import static myLIFT.Main.simpleExecution;
import static myLIFT.Main.testConcExam;

/**
 * Class for FETA deduction (Phases: "LogClean", "GraphConstruction",
 * "NestedLoopDetection", "SameConcept/SameAs and "NotNullJoin" heuristics
 *
 * @author Nassopoulos Georges
 * @version 1.0
 * @since 2016-03-19
 */
public class Deduction {

    CouchDBManag myDB;
    MonetDBManag myMDB;
    BasicUtilis myBasUtils;
    LoadFromDB myDedCLean;
    NestedLoopDetection myDedInverseMap;
    CandidatExtract myLDF;

    public static Document docAnswers;
    // set of deduced graphs, where each node is a LogClean query's ID
    public static List<List<Integer>> dedGraphSelect;
    //
    public static HashMap<String, String> mapEndpointToName;

    //******************************Hash Maps concerning Log Clean queries******************************//
    // map each LogClean query to its original entry timestamp in HH:MM:SS format
    public static HashMap<Integer, String> mapLogClQueryToTimestamp;
    // map each LogClean query ID to all answer entries IDs
    public static HashMap<Integer, List<Integer>> mapLogClQueryToAnsEntry;
    // map each LogClean query to all its triple pattern's entitites
    public static HashMap<Integer, List<String>> mapLogClQueryToAllTPEnts;
    // map each LogClean query to its associated graph => saves a lot of time in "Graph Construction" module
    public static HashMap<Integer, Integer> mapLogClQueryToDedGraph;
    // map each LogClean query to its original timestamm in seconds
    public static HashMap<Integer, Integer> mapLogClQueryToTimeSecs;
    // map each LogClean query to its original timestamm in seconds
    public static HashMap<Integer, List<String>> mapLogClQueryToProjVars;

    //******************************Hash Maps concerning Answer entry id's******************************//
    // map each answer id entry with its associated entry information tuple
    public static Map<Integer, List<String>> mapAnsIDtoEntry;
    // map each answer id entry to its timestamp in seconds
    public static HashMap<Integer, Integer> mapAnsIDToTimeSecs;
    // map each answer entry to all its SELECT query entities
    public static Map<Integer, List<String>> mapAnsIDToQueryEnts;
    // map each answer entry to all its SELECT query's projected variables
    public static Map<Integer, List<String>> mapAnsIDToQueryProjVars;
    // map each answer entry to its respective LogClean query 
    public static HashMap<Integer, String> mapAnsIDToLogClQuery;
    //
    public static HashMap<String, List<String>> mapAnsEntryToListValues;
    //
    public static HashMap<String, List<String>> mapAnsEntryToAllSignatures;
    //
    public static HashMap<String, List<String>> mapAnsSingatureToAllValues;

    public static HashMap<List<String>, Integer> mapDTPtoCANCELofEG;

    //*****************************GroundTRUTH******************************//
    public static HashMap<List<String>, List<String>> mapGroundTruthHashMaps;
    public static Map<List<List<String>>, Integer> mapGroundTruthPairs;
    public static Map<List<List<String>>, Integer> mapTruePositivePairs;
    public static Map<List<String>, Integer> mapGroundTruthTPs;
    public static Map<List<String>, Integer> mapObservedTPs;
    public static int groundTruthPairs;
    public static int totalPairs;
    public static int truePositivesPairs;
    public static int totalTPs;
    public static int truePositivesTPs;
    public static int groundTruthTPs;

    public static int cntEGTotal;
    public static int cntSYMHASHTotal;
    public static int cntCONSTJOINTotal;
    public static int cntNESLOOPTotal;
    public static int cntEGTrPo;
    public static int cntSYMHASHTrPo;
    public static int cntCONSTJOINTrPo;
    public static int cntNESLOOPTrPo;

    public Deduction(List<Document> listDocument, CouchDBManag db) throws IOException {

        myDB = db;
        docAnswers = null;

        myDedCLean = new LoadFromDB(listDocument, db);
        myDedInverseMap = new NestedLoopDetection();
        myBasUtils = new BasicUtilis();
        myLDF = new CandidatExtract();

        dedGraphSelect = new LinkedList<>();

        mapAnsIDtoEntry = new TreeMap<>();
        mapAnsIDToLogClQuery = new HashMap<>();
        mapAnsIDToQueryEnts = new TreeMap<>();
        mapAnsIDToTimeSecs = new HashMap<>();
        mapAnsEntryToListValues = new HashMap<>();
        mapLogClQueryToAnsEntry = new HashMap<>();
        mapLogClQueryToDedGraph = new HashMap<>();
        mapLogClQueryToTimeSecs = new HashMap<>();
        mapLogClQueryToAllTPEnts = new HashMap<>();
        mapLogClQueryToTimestamp = new HashMap<>();
        mapLogClQueryToProjVars = new HashMap<>();
        mapAnsIDToQueryProjVars = new HashMap<>();

        mapGroundTruthPairs = new HashMap<>();
        mapGroundTruthTPs = new HashMap<>();
        mapTruePositivePairs = new HashMap<>();
        mapObservedTPs = new HashMap<>();
        truePositivesPairs = 0;
        totalPairs = 0;
        totalTPs = 0;
        groundTruthPairs = 0;
        truePositivesTPs = 0;
        groundTruthTPs = 0;
        mapEndpointToName = new HashMap<>();

        mapAnsEntryToAllSignatures = new HashMap<>();
        mapAnsSingatureToAllValues = new HashMap<>();
        mapDTPtoCANCELofEG = new HashMap<>();
        mapGroundTruthHashMaps = new HashMap<>();
    }

    public Deduction() throws IOException {

        docAnswers = null;
        dedGraphSelect = new LinkedList<>();
        myBasUtils = new BasicUtilis();
        myLDF = new CandidatExtract();

        mapAnsIDToTimeSecs = new HashMap<>();
        mapAnsIDToQueryEnts = new TreeMap<>();
        mapAnsEntryToListValues = new HashMap<>();
        mapLogClQueryToTimestamp = new HashMap<>();
        mapLogClQueryToDedGraph = new HashMap<>();
        mapLogClQueryToAllTPEnts = new HashMap<>();
        mapLogClQueryToTimeSecs = new HashMap<>();
        mapLogClQueryToProjVars = new HashMap<>();
        mapAnsIDToQueryProjVars = new HashMap<>();

        mapGroundTruthPairs = new HashMap<>();
        mapGroundTruthTPs = new HashMap<>();
        mapTruePositivePairs = new HashMap<>();
        mapObservedTPs = new HashMap<>();

        truePositivesPairs = 0;
        groundTruthPairs = 0;
        truePositivesTPs = 0;
        groundTruthTPs = 0;
        totalPairs = 0;
        totalTPs = 0;

        cntEGTotal = 0;
        cntSYMHASHTotal = 0;
        cntCONSTJOINTotal = 0;
        cntNESLOOPTotal = 0;
        cntEGTrPo = 0;
        cntSYMHASHTrPo = 0;
        cntCONSTJOINTrPo = 0;
        cntNESLOOPTrPo = 0;

        mapEndpointToName = new HashMap<>();
        mapDTPtoCANCELofEG = new HashMap<>();

        mapAnsEntryToAllSignatures = new HashMap<>();
        mapAnsSingatureToAllValues = new HashMap<>();
        mapGroundTruthHashMaps = new HashMap<>();
    }

    public Deduction(List<Document> listDocument, MonetDBManag db) throws IOException {

        myMDB = db;
        docAnswers = null;
        dedGraphSelect = new LinkedList<>();

        mapAnsIDToLogClQuery = new HashMap<>();
        myDedCLean = new LoadFromDB(listDocument, db);
        myDedInverseMap = new NestedLoopDetection();
        myBasUtils = new BasicUtilis();
        myLDF = new CandidatExtract();

        mapAnsIDtoEntry = new TreeMap<>();
        mapAnsIDToTimeSecs = new HashMap<>();
        mapAnsIDToQueryEnts = new TreeMap<>();
        mapAnsEntryToListValues = new HashMap<>();
        mapLogClQueryToAnsEntry = new HashMap<>();
        mapLogClQueryToDedGraph = new HashMap<>();
        mapLogClQueryToTimeSecs = new HashMap<>();
        mapLogClQueryToAllTPEnts = new HashMap<>();
        mapLogClQueryToTimestamp = new HashMap<>();
        mapLogClQueryToProjVars = new HashMap<>();
        mapAnsIDToQueryProjVars = new HashMap<>();

        mapGroundTruthPairs = new HashMap<>();
        mapGroundTruthTPs = new HashMap<>();
        mapTruePositivePairs = new HashMap<>();
        mapObservedTPs = new HashMap<>();

        truePositivesPairs = 0;
        groundTruthPairs = 0;
        truePositivesTPs = 0;
        groundTruthTPs = 0;
        totalPairs = 0;
        totalTPs = 0;

        mapEndpointToName = new HashMap<>();
        mapAnsEntryToAllSignatures = new HashMap<>();
        mapAnsSingatureToAllValues = new HashMap<>();
        mapDTPtoCANCELofEG = new HashMap<>();
        mapGroundTruthHashMaps = new HashMap<>();
    }

    /**
     * Init the deduction algorithm: get DB ("CouchDB" or "MonetDB") and
     * collection (or table respectively) to be used
     *
     * @param myDataBase the name of DB that will used for deduction
     */
    public void initDeduction(String myDataBase) {

        if (!setMonetDB && setCouchDB) {

            myDB.getDatabase(myDataBase);
        }

    }

    /**
     * Implement FETA deduction algorithm :
     *
     * (1)[LogCLean] Merge all "same" queries and identify all distinct queries
     *
     * (2)[CommonJoinCondition] Construct a graph of all Log Clean queries,
     * based on their common join condition (i.e., their projected variables or
     * constants on their subjects/objects)
     *
     * (3)[NestedLoopDetection] First, extract all Candidate Triple Patterns
     * (CTPs) and identify all their constant values (i.e., IRIs or Literals) on
     * their subject or object position. Then, either inverse map all CTPs
     * constant values as the inner part of a nested loop implemention and match
     * them as answers of a previously Deduced Triple Pattern (DTP), or identify
     * the CTP directly as a DTP.
     *
     * (4)[SameConcept/SameAS and NotNullJoin] First, group DTPs with same
     * variable names or constants and then, for those not related in a nested
     * loop implementation, associate DTPs based on their answers simulating a
     * symhash join operation
     *
     * Eventually, apply sequential mining using using MINEPI algorithm
     *
     * (5)[SequentialMining] Apply the MINEPI algo with constraints simulating,
     * if chosen, FETA heuristics in order to confirm in parallel FETA's results
     *
     * @param deductionWindow comparison time, defining the DB slice
     * @throws java.io.IOException
     * @throws java.net.URISyntaxException
     * @throws java.lang.InterruptedException
     * @throws java.sql.SQLException
     * @throws java.lang.InstantiationException
     * @throws java.lang.IllegalAccessException
     */
    public void deductionAlgo(int deductionWindow) throws IOException, URISyntaxException, InterruptedException, SQLException, InstantiationException, IllegalAccessException {

        long startTime = 0;
        long finishTime = 0;
        startTime = System.nanoTime();

        matchEndpointToName();
        System.out.println("[START] ---> Load log in RAM and create indexes");

        //load all collection in RAM, when couchdb is used, in order to optimize time execution
        loadAnsInRAM();

        finishTime = System.nanoTime();

        System.out.println("[FINISH] ---> Load log in RAM and create indexes (Elapsed time; " + (startTime - finishTime) / 1000000000 + " seconds)");
        System.out.println();
        System.out.println("-----------------------------------------------------------------------------------------------------------------");
        System.out.println("-----------------------------------------------------------------------------------------------------------------");
        System.out.println();

        if (!testConcExam) {

            getGroundTruthPairs();
            getGroundTruthTPs();
            getGroundTruthHashMaps();
        }

        myLDF.setAuthorityToPrefix();
        myLDF.setPrefixToAuthority();
        myLDF.setLDFpredicates();

        //Apply "LogClean" heuristic
        myDedCLean.LogClean(deductionWindow);

        /* System.out.println("Current engine: " + myContext.engine);
         myContext.engine = "FedX";
         System.out.println("New engine: " + myContext.engine);*/
        if (!enableFragm) {


            //If we do not stop FETA deduction to "CommonJoinCondition" heuristic
            if (!simpleExecution) {

                //Apply "NestedLoopDetection" heuristic 
                myDedInverseMap.NestedLoopDetection(deductionWindow);
                System.out.println();
                System.out.println("-----------------------------------------------------------------------------------------------------------------");
                System.out.println("-----------------------------------------------------------------------------------------------------------------");
                System.out.println();
                startTime = System.nanoTime();
                System.out.println("[START] ---> SameConcept/Same As and NotNullJoin heuristics");

                finishTime = System.nanoTime();
                System.out.println("[FINISH] ---> SameConcept/Same As and NotNullJoin heuristics (Elapsed time: " + (startTime - finishTime) / 1000000000 + " seconds)");

            }

        } else {

            System.out.println();
            System.out.println("-----------------------------------------------------------------------------------------------------------------");
            System.out.println("-----------------------------------------------------------------------------------------------------------------");
            System.out.println();
            startTime = System.nanoTime();

            System.out.println("[START] ---> Linked Data Fragments deduction");

         //   myLDF.removeRedundancyLDFInfo();
            //   myLDF.setInfoToCandidateLDFs();
            startTime = System.nanoTime();
            myLDF.showCandidateLDFs();

            finishTime = System.nanoTime();
            System.out.println("***************showCandidateLDFs: " + (finishTime - startTime));
            startTime = System.nanoTime();

            myLDF.searchNestedLoops();

            finishTime = System.nanoTime();
            System.out.println("***************searchNestedLoops: " + (finishTime - startTime));

            startTime = System.nanoTime();
            myLDF.showDeducedLDFs();
            finishTime = System.nanoTime();
            System.out.println("***************showDeducedLDFs: " + (finishTime - startTime));

            startTime = System.nanoTime();
            //  myLDF.showSingleOccurLDFs();
            myLDF.showNotInNestedLoopLDFs();

            finishTime = System.nanoTime();
            System.out.println("***************showNotInNestedLoopLDFs: " + (finishTime - startTime));

            /*if(!simpleExecution){
                
              myLDF.generateGNUFinal();
            }*/
            finishTime = System.nanoTime();
            System.out.println("[FINISH] ---> Linked Data Fragments deduction (Elapsed time: " + (startTime - finishTime) / 1000000000 + " seconds)");
        }

    }

    public void matchEndpointToName() {

        mapEndpointToName.put("8700", "drugBank");
        mapEndpointToName.put("8701", "kegg");
        mapEndpointToName.put("8702", "chebi");
        mapEndpointToName.put("8703", "geonames");
        mapEndpointToName.put("8704", "nyTimesNews");
        mapEndpointToName.put("8705", "jamendo");
        mapEndpointToName.put("8706", "swdf");
        mapEndpointToName.put("8707", "lmdb");
        mapEndpointToName.put("8709", "dbpediaInstanceTypes");
        mapEndpointToName.put("8710", "dbpediaInfoBox");
        mapEndpointToName.put("8711", "dbpediaLabels");
        mapEndpointToName.put("8712", "dbpediaArticles");
        mapEndpointToName.put("8713", "dbpediaCategoryLabels");
        mapEndpointToName.put("8714", "dbpediaGeoCoordinates");
        mapEndpointToName.put("8715", "dbpediaImages");
        mapEndpointToName.put("8716", "dbpediaSkos");
        mapEndpointToName.put("8717", "dbpediaPerson");
        mapEndpointToName.put("8718", "dbpediaNYTimes");
        mapEndpointToName.put("8719", "dbpediaLGD");
    }

    /**
     * Load to RAM all Answer traces, in order to minimize interaction with DB
     *
     * @throws java.sql.SQLException
     * @throws java.io.IOException
     * @throws java.net.URISyntaxException
     */
    public void loadAnsInRAM() throws SQLException, IOException, URISyntaxException {

        if (setMonetDB) {

            //if(!enableFragm)
            {

                myMDB.setAnswerStringToMaps();
            }
        } else {
            // myDB.addDocument("endpointsAnswersMXANAPSID");

            myDB.addDocument("endpointsAnswers" + collectionName);
            docAnswers = myDB.getDocument("endpointsAnswers" + collectionName);
            //  if(!enableFragm)
            {

                myDB.setAnswerStringToMaps();
            }
        }
    }

    /**
     *
     * @throws FileNotFoundException
     * @throws IOException
     */
    public void getGroundTruthPairs() throws FileNotFoundException, IOException {

        String fileName = "groundTruthPairs.txt";
        BufferedReader br = null;
        String sCurrentLine;

        List<String> outerTP = new LinkedList<>();
        List<String> innerTP = new LinkedList<>();
        int cntPairs = 0;

        List<List<String>> tmpPair = new LinkedList<>();
        List<List<String>> tmpPair2 = new LinkedList<>();

        System.out.println("*******START: Ground Truth joinedpairs********");
        try {

            br = new BufferedReader(new FileReader(fileName));

            while ((sCurrentLine = br.readLine()) != null) {

                String s = sCurrentLine;
                String[] array = s.split(", ");
                outerTP = new LinkedList<>();
                innerTP = new LinkedList<>();
                int i = 0;

                //BUUUUUUUUUUUUG
                for (String str : array) {

                    String[] array2 = s.split(" ");

                    for (String str2 : array2) {
                        if (str2.contains(",")) {
                            str2 = str2.substring(0, str2.indexOf(","));
                        }
                        if (str2.contains("_") && !str2.contains("http")) {
                            str2 = str2.replace("_", " ");
                        }
                        if (outerTP.size() < 3) {

                            outerTP.add(str2);
                        } else if (innerTP.size() < 3) {
                            innerTP.add(str2);
                        }

                    }
                }

                tmpPair = Arrays.asList(innerTP, outerTP);
                tmpPair2 = Arrays.asList(outerTP, innerTP);
                mapGroundTruthPairs.put(tmpPair, 1);
                cntPairs++;
                System.out.println("\t Join pair no[" + cntPairs + "]: " + tmpPair);
                mapGroundTruthPairs.put(tmpPair2, 1);
            }

        } finally {
            try {
                if (br != null) {

                    br.close();
                }
            } catch (IOException ex) {

                ex.printStackTrace(System.out);
            }
        }

        System.out.println("*******FINISH: Ground Truth joinedpairs********");
    }

    /**
     *
     * @throws FileNotFoundException
     * @throws IOException
     */
    public void getGroundTruthTPs() throws FileNotFoundException, IOException {

        String fileName = "groundTruthTPs.txt";
        BufferedReader br = null;
        String sCurrentLine;

        int cntTPs = 0;

        List<String> outerTP = new LinkedList<>();
        System.out.println("*******START: Ground Truth tps********");
        try {

            br = new BufferedReader(new FileReader(fileName));

            while ((sCurrentLine = br.readLine()) != null) {

                outerTP = new LinkedList<>();

                String s = sCurrentLine;
                String[] array = s.split(" ");

                int i = 0;
                for (String str : array) {

                    if (str.contains("_") && !str.contains("http")) {

                        str = str.replace("_", " ");
                    }

                    outerTP.add(str);
                }

                cntTPs++;
                System.out.println("\t Original TP no[" + cntTPs + "]: " + outerTP);

                mapGroundTruthTPs.put(outerTP, 1);
            }

        } finally {
            try {
                if (br != null) {

                    br.close();
                }
            } catch (IOException ex) {

                ex.printStackTrace(System.out);
            }
        }

        System.out.println("*******FINISH: Ground Truth tps********");
    }

    /**
     * Load from a text file in JSON format, all previously produced random
     * timestamps of every Answer entry
     *
     */
    public void getGroundTruthHashMaps() {

        Gson gson = new Gson();

        List<List<List<String>>> objQueries = new LinkedList<>();

        try {

            BufferedReader br = new BufferedReader(
                    new FileReader("mapAnswersOfDTPs.txt"));

            objQueries = (List<List<List<String>>>) gson.fromJson(br, Object.class);

        } catch (FileNotFoundException e) {
        }

        for (List<List<String>> key : objQueries) {
            mapGroundTruthHashMaps.put(key.get(0), key.get(1));
        }

    }

}