package mylift;

import com.fourspaces.couchdb.Document;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class for LIFT deduction. Phases:
 *
 * (a) "Candidate Triple Patterns (CTP) Extraction":
 *
 * Merge of single triple pattern queries in the log, that seems to evaluate the
 * same triple pattern during a nested loop join
 *
 * (b) "Graph reduction" (Optional):
 *
 * Optionally merge Candidate Triple Patterns, that seems to be part of the same
 * execution plan, i.e., (i) "count" triple patterns that are sent to decide the
 * join ordering of a query evaluation, and, (ii) decomposed triple patterns
 * that coorespond to nested loop implementation of a specific "count" triple
 * pattern
 *
 * (c) "Nested loop detection":
 *
 * Detection of nested loops between candidate triple patterns, by eventually
 * splitting some falesly merged triple pattern in the same CTP
 *
 * (d) "Bgp extraction":
 *
 * Construction of BGP sets of triple patterns, based on deduced joins of the
 * previous step
 *
 * @author Nassopoulos Georges
 * @version 1.0
 * @since 2016-12-15
 */
public class InitLift {

    private CouchDBManag myCouchDB;
    private MonetDBManag myMonetDB;
    private SaveInDB load;
    private CtpExtraction myCandExtr;
    private final NestedLoopDetection myNestDet;
    private final BgpExtraction myBGPext;

    /**
     * ********** Hash Maps concerning log queries **********************
     */
    // map each id entry, with its associated information tuple
    public static Map<Integer, List<String>> mapAnsIDtoEntry;
    // Document of Couchdb data base
    public static Document docAnswers;
    // match each IRI's authority to the corresponding prefix
    public static HashMap<String, String> mapAuthorityToPrefix;
    // match each prefix to the correspponding authority
    public static HashMap<String, String> mapPrefixToAuthority;
    // host of MonetDB database
    private final String HOST_MONETDB = "jdbc:monetdb://localhost/demo";
    // host of CouchDB database
    private final String HOST_COUCHDB = "localhost";
    // port of CouchDB database
    private final int PORT_COUCHDB = 5984;
    // username of MonetDB
    private final String USERNAME = "feta";
    // password of MonetDB
    private final String PASSWORD = "feta";
    // command to initialize MonetDB
    private final String INIT_MONETDB = "monetdbd start $HOME/myMONETDB/";
    
    
    /* Entry -oriented hash maps */
     
    //  
    
    
    // match each CTP to its serial id (order of identification)
    public static HashMap<Integer, List<String>> mapCtpToSerialID;
    // match each CTP's id, to corresponding log entries
    public static HashMap<Integer, List<Integer>> mapCtpToLogEntries;
    // match each CTP's id, to LDF servers that recieved it
    public static HashMap<Integer, List<String>> mapCtpToLDFServer;
    // match each CTP's id, to corresponding output fragment' values (i.e., out mappings)
    public static HashMap<Integer, List<String>> mapCtpToOutMaps;
    // match each CTP's id, to all its corresponding timeSecs (total number in seconds)
    public static HashMap<Integer, List<Integer>> mapCtpToTimeSecs;
    // match each CTP's id, to corresponding (possible) injected values in the subject position
    public static HashMap<Integer, List<String>> mapCtpToInMapsSubj;
    // match each CTP's id, to corresponding (possible) injected values in the subject position
    public static HashMap<Integer, List<String>> mapCtpToInMapsObj;
    // match a CTP's id, with all its different versions, that are deduced when the
    // window join (i.e., gap) is not big enough to put them in only one
    public static HashMap<List<String>, Integer> mapCTPtoVersionsGap;
    // match each constant value to its oncurrences
    public static HashMap<String, Integer> mapConstToAnsOccurs;
    
    

    public InitLift() {

        myCouchDB = null;
        docAnswers = null;

        myNestDet = new NestedLoopDetection();
        myBGPext = new BgpExtraction();

        mapAnsIDtoEntry = new TreeMap<>();
        mapAuthorityToPrefix = new HashMap<>();
        mapPrefixToAuthority = new HashMap<>();
    }

    /**
     * Init the deduction algorithm:
     *
     * (i) load trace into the DB ("CouchDB" or "MonetDB"), or, (ii) init LIFT
     * deduction algorithm, for a specific database
     *
     */
    public void initProcessing() {

        try {

            // open session with either MonetDB or CouchDB
            if (Configuration.setMonetDB) {

                Process p = Runtime.getRuntime().exec(new String[]{"bash", "-c", INIT_MONETDB});
                myMonetDB = new MonetDBManag();
                myMonetDB.openSession(HOST_MONETDB, USERNAME, PASSWORD);
            } else if (Configuration.setCouchDB) {

                myCouchDB = new CouchDBManag();
                myCouchDB.openSession(HOST_COUCHDB, PORT_COUCHDB);
            }

            // either load a trace into the DB, or, init LIFT deduction algorithm
            if (Configuration.loadDB) {

                loadTraces();
            } else {

                   
                deductionAlgo(Configuration.sliceWin);
            }

        } catch (Exception ex) {

            Logger.getLogger(InitLift.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    /**
     * Load trace into DB, into MonetDB and CouchDB, by creating a new database
     * or reset an existing one
     *
     */
    private void loadTraces() {

        try {

            // for  
            if (Configuration.setMonetDB) {

                if (Configuration.resetDB) {
                    myMonetDB.resetMDB();
                } else {
                    myMonetDB.createDB(Configuration.nameDB);
                }

                load = new SaveInDB(myMonetDB);
                load.initLoading(Configuration.logPath);
            } else if (Configuration.setCouchDB) {

                if (Configuration.resetDB) {
                    myCouchDB.resetDB(Configuration.nameDB);
                } else {
                    myCouchDB.createDB(Configuration.nameDB);
                }

                load = new SaveInDB(myCouchDB);
                load.initLoading(Configuration.logPath);
            }

        } catch (Exception ex) {

            Logger.getLogger(InitLift.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    /**
     * Implement LIFT deduction algorithm: (1) CTPExtraction, (2) GraphReduction
     * (optional), (3) NestedLoopDetection, (4) BgpExtraction
     *
     * @param gapWindow maximum gap time between two triple patterns
     */
    private void deductionAlgo(int gapWindow) {

        HashMap<Integer, List<String>> mapDTPoSerialID = new HashMap<>();
        long finishTimeGlobal = 0;
        long startTimeGlobal = 0;

        try {

            startTimeGlobal = System.nanoTime();

            // initilize authority/prefix maps and other indexes
            setAuthorityToPrefix();
            setPrefixToAuthority();
            loadAnsInRAM();

            BasicUtilis.printInfo("[START] ---> LIFT deduction");
            // Extraction of Candidate Triple Pattern (CTP) set 
            myCandExtr = new CtpExtraction(myCouchDB, myMonetDB);
            myCandExtr.initExtractionCTP(gapWindow);

            // Identification of nested loop joins
            mapDTPoSerialID = myNestDet.initNestedDetection();

            // Extraction of BGP sets
            myBGPext.initBgpExtract(mapDTPoSerialID);
            System.gc();
            finishTimeGlobal = System.nanoTime();

            BasicUtilis.printInfo("[FINISH] ---> LIFT deduction (Elapsed time global: " + (finishTimeGlobal - startTimeGlobal) / 1000000000 + " seconds)");

        } catch (Exception ex) {

            Logger.getLogger(InitLift.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Load to RAM all Answer traces, in order to minimize interaction with DB
     *
     */
    private void loadAnsInRAM() {

       // BasicUtilis.printInfo("[START] ---> Load log in RAM and create indexes");
        try {

            if (Configuration.setMonetDB) {

                myMonetDB.setAnswerStringToMaps();
            } else if (Configuration.setCouchDB) {

                myCouchDB.getDB(Configuration.nameDB);
                myCouchDB.addDocument("endpointsAnswers" + Configuration.collectionName);
                docAnswers = myCouchDB.getDocument("endpointsAnswers" + Configuration.collectionName);
                myCouchDB.setAnswerStringToMaps();
            }

        } catch (Exception ex) {

            Logger.getLogger(InitLift.class.getName()).log(Level.SEVERE, null, ex);
        }

       // BasicUtilis.printInfo("\n[FINISH] ---> Load log in RAM and create indexes\n");

    }

    /**
     * Set each complete predicate IRI to corresponding prefix
     */
    private void setAuthorityToPrefix() {

        mapAuthorityToPrefix.put("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "rdf:");
        mapAuthorityToPrefix.put("http://www.w3.org/2000/01/rdf-schema#", "rdfs:");
        mapAuthorityToPrefix.put("http://www.w3.org/2002/07/owl#", "owl:");
        mapAuthorityToPrefix.put("http://www.w3.org/2004/02/skos/core#", "skos:");
        mapAuthorityToPrefix.put("http://www.w3.org/2001/XMLSchema#", "xsd:");
        mapAuthorityToPrefix.put("http://purl.org/dc/terms/", "dc:");
        mapAuthorityToPrefix.put("http://purl.org/dc/terms/", "dcterms:");
        mapAuthorityToPrefix.put("http://purl.org/dc/elements/1.1/", "dc11:");
        mapAuthorityToPrefix.put("http://xmlns.com/foaf/0.1/", "foaf:");
        mapAuthorityToPrefix.put("http://www.w3.org/2003/01/geo/wgs84_pos#", "geo:");
        mapAuthorityToPrefix.put("http://dbpedia.org/resource/", "dbpedia:");
        mapAuthorityToPrefix.put("http://dbpedia.org/ontology/", "dbpedia-owl:");
        mapAuthorityToPrefix.put("http://dbpedia.org/property/", "dbpprop:");
        mapAuthorityToPrefix.put("http://www.w3.org/ns/hydra/core#", "hydra:");
        mapAuthorityToPrefix.put("http://rdfs.org/ns/void#", "void:");
        mapAuthorityToPrefix.put("http://dbpedia.org/class/yago/", "dbpedia-class:");
        mapAuthorityToPrefix.put("http://data.linkeddatafragments.org/.well-known/genid/lov/", "ldf-lov:");
    }

    /**
     * Set each prefix to corresponding complete predicate IRI
     */
    private void setPrefixToAuthority() {

        mapPrefixToAuthority.put("rdf:", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
        mapPrefixToAuthority.put("rdfs:", "http://www.w3.org/2000/01/rdf-schema#");
        mapPrefixToAuthority.put("owl:", "http://www.w3.org/2002/07/owl#");
        mapPrefixToAuthority.put("skos:", "http://www.w3.org/2004/02/skos/core#");
        mapPrefixToAuthority.put("xsd:", "http://www.w3.org/2001/XMLSchema#");
        mapPrefixToAuthority.put("dc:", "http://purl.org/dc/terms/");
        mapPrefixToAuthority.put("dcterms:", "http://purl.org/dc/terms/");
        mapPrefixToAuthority.put("dc11:", "http://purl.org/dc/elements/1.1/");
        mapPrefixToAuthority.put("foaf:", "http://xmlns.com/foaf/0.1/");
        mapPrefixToAuthority.put("geo:", "http://www.w3.org/2003/01/geo/wgs84_pos#");
        mapPrefixToAuthority.put("dbpedia:", "http://dbpedia.org/resource/");
        mapPrefixToAuthority.put("dbpedia-owl:", "http://dbpedia.org/ontology/");
        mapPrefixToAuthority.put("dbpprop:", "http://dbpedia.org/property/");
        mapPrefixToAuthority.put("hydra:", "http://www.w3.org/ns/hydra/core#");
        mapPrefixToAuthority.put("void:", "http://rdfs.org/ns/void#");
        mapPrefixToAuthority.put("dbpedia-class:", "http://dbpedia.org/class/yago/");
        mapPrefixToAuthority.put("ldf-lov:", "http://data.linkeddatafragments.org/.well-known/genid/lov/");
    }

}