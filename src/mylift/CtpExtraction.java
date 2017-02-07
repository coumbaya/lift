package mylift;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class for extraction the set of Candidate Triple Patterns (CTP) that seem to
 * be part of the same nested loop implementation, having either constant value
 * on their subject, object or on both
 *
 * @author Nassopoulos Georges
 * @version 1.0
 * @since 2016-12-15
 */
public class CtpExtraction {

    private final CouchDBManag myCouchDB;
    private final MonetDBManag myMonetDB;

    /* Candidate Triple Pattern - oriented hash/set maps */
    // match each CTP to its serial id (order of identification)
    public static HashMap<Integer, List<String>> mapCtpToSerialID;
    // match each CTP's id, to corresponding log entries
    public static HashMap<Integer, List<Integer>> mapCtpToLogEntries;
    // match each CTP's id, to LDF servers that recieved it
    public static HashMap<Integer, List<String>> mapCtpToLDFServer;
    // match each CTP's id, to corresponding output fragment' values (i.e., out mappings)
    public static HashMap<Integer, List<String>> mapCtpToOutMapsSubject;
    // match each CTP's id, to corresponding output fragment' values (i.e., out mappings)
    public static HashMap<Integer, List<String>> mapCtpToOutMapsObject;
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
    
    public static boolean flagDum=false;
    
      int count1=0;
        int count2=0;

    public CtpExtraction(CouchDBManag myCouch, MonetDBManag myMonet) {

        myCouchDB = myCouch;
        myMonetDB = myMonet;

        mapConstToAnsOccurs = new HashMap<>();
        mapCtpToOutMapsSubject = new HashMap<>();
        mapCtpToOutMapsObject = new HashMap<>();
        mapCtpToSerialID = new HashMap<>();
        mapCtpToLDFServer = new HashMap<>();
        mapCtpToLogEntries = new HashMap<>();
        mapCTPtoVersionsGap = new HashMap<>();
        mapCtpToTimeSecs = new HashMap<>();
        mapCtpToInMapsSubj = new HashMap<>();
        mapCtpToInMapsObj = new HashMap<>();
    }

    /**
     * Parse log entries to identify the CTP set, and print it
     *
     * @param gapWindow maximum gap time between two triple patterns
     */
    public void initExtractionCTP(int gapWindow) {

        long startTime = 0;
        long finishTime = 0;

        try {
            
            startTime = System.nanoTime();
            parseDBEntries();
            showCTPs();

            InitLift.mapAnsIDtoEntry.clear();
            System.gc();

            finishTime = System.nanoTime();
            BasicUtilis.printInfo("\n\t\t\t\t###################### (Elapsed time; "
                    + (double) ((double) finishTime - (double) startTime) / 1000000000 + " seconds) ###################### \n");
        } catch (Exception ex) {

            Logger.getLogger(CtpExtraction.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    /**
     * Parse ever log entry and update the CTP set
     *
     */
    private void parseDBEntries() {

        List<String> timeStamps = new LinkedList<>();

        // Scan all entries of the database traces, for MonetDB or CouchDB
        if (Configuration.setMonetDB) {

            timeStamps = parseMonetDB();

        } else if (Configuration.setCouchDB) {

            timeStamps = parseCouchDB();
        }

        if (Configuration.verbose) {
            BasicUtilis.printInfo("\n\t================ Time interval of DB log used as LIFT input: [" + timeStamps.get(0) + ", " + timeStamps.get(1) + "] "
                    + "and duration: " + (BasicUtilis.getTimeInSec(timeStamps.get(0)) - BasicUtilis.getTimeInSec(timeStamps.get(1))) + " seconds ================");
        }
    }

    /**
     *
     * @return
     */
    public List<String> parseMonetDB() {

        int monetSize = -1;
        int timeInSec = 0;
        List<String> entryInfo = null;
        String LDFServer = "", IpAddress = "", Time = "", Query = "", Answ = "";
        String startTimeQuery = "";
        String stopTimeQuery = "";
        List<String> timeStamps = new LinkedList<>();

        monetSize = myMonetDB.getTableSize("tableQrsAndAns" + Configuration.nameDB);

        for (int i = 1; i < monetSize; i++) {

            try {
                entryInfo = myMonetDB.getEntryAnswers(i);

                if (!entryInfo.isEmpty()) {

                    IpAddress = entryInfo.get(2);
                    LDFServer = entryInfo.get(1);
                    Query = entryInfo.get(4).replaceAll(("\r\n"), " ");
                    Time = entryInfo.get(3);
                    Answ = entryInfo.get(5);

                    if (!(Query.contains("subject") || Query.contains("predicate") || Query.contains("object"))) {
                        continue;
                    }

                    if (startTimeQuery.isEmpty()) {
                        startTimeQuery = Time;
                        timeStamps.add(startTimeQuery);
                    }

                    if (stopTimeQuery.isEmpty()) {
                        stopTimeQuery = Time;
                        timeStamps.add(stopTimeQuery);
                    }

                    timeInSec = BasicUtilis.getTimeInSec(Time);
                    convertEntryToCTP(i, Query, Answ, LDFServer, timeInSec);
                }
            } catch (Exception ex) {

                Logger.getLogger(CtpExtraction.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return timeStamps;
    }

    /**
     *
     * @return
     */
    public List<String> parseCouchDB() {

        int timeInSec = 0;
        List<String> entryInfo = null;
        String LDFServer = "", IpAddress = "", Time = "", Query = "", Answ = "";
        String startTimeQuery = "";
        String stopTimeQuery = "";
        List<String> timeStamps = new LinkedList<>();

        for (int key : InitLift.mapAnsIDtoEntry.keySet()) {

            entryInfo = myCouchDB.getEntryAnswerLogHashMap(InitLift.mapAnsIDtoEntry, key);

            if (!entryInfo.isEmpty()) {

                try {

                    IpAddress = entryInfo.get(0);
                    LDFServer = entryInfo.get(1);
                    Query = entryInfo.get(2).replaceAll(("\r\n"), " ");
                    Time = entryInfo.get(3);
                    Answ = entryInfo.get(5);

                    // BUUUUUUUUUUUUUUUUG
                    if (!(Query.contains("subject") || Query.contains("predicate") || Query.contains("object"))) {
                        continue;
                    }

                    if (startTimeQuery.isEmpty()) {
                        startTimeQuery = Time;
                        timeStamps.add(startTimeQuery);
                    }

                    if (stopTimeQuery.isEmpty()) {
                        stopTimeQuery = Time;
                        timeStamps.add(stopTimeQuery);
                    }

                    timeInSec = BasicUtilis.getTimeInSec(Time);
                    convertEntryToCTP(key, Query, Answ, LDFServer, timeInSec);
                } catch (Exception ex) {

                    Logger.getLogger(CtpExtraction.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

        return timeStamps;
    }

    /**
     * Update CTP set using the information of the current log entry
     *
     * @param logEntryID log entry id, to be parsed
     * @param query current query/selector
     * @param ansFragment complete answer fragment in string format
     * @param LDFserver current LDF server responding to query/selector
     * @param timeStamp time in total number of seconds, of current log entry
     */
    private void convertEntryToCTP(int logEntryID, String query, String ansFragment, String LDFserver, int timeStamp) {

        List<String> ctpFormated = null;
        List<List<String>> allOutputMaps = null;
        List<String> outputSubj = null;
        List<String> outputObj = null;
        List<String> allQueryUnities = null;
        List<String> allInjectVals = null;

        if (query.contains("Belgium")) {
            count1++;
        }

       if (query.contains("India")) {
            count2++;
        }
       
       
         if (query.contains("http://dbpedia.org/resource/01.002_Fighter_Squadron_%22Storks%22")) {
           int azrz=0;
        }
       
         if (ansFragment.contains("Zena_Stein")) {
           int azrz=0;
        }
         
      //   System.out.println("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX "+logEntryID);
       
       
       if(count1>0&&count2>0)
           flagDum=true;
       
            
        //Get all selector unities (concerning to subject, predicate or/and object)
        allQueryUnities = BasicUtilis.getUnitiesFromSelector(query);

        //Get, if there exist, list of injected values in the selector
        allInjectVals = getInjectFromQuery(allQueryUnities);

        //Get all distinct answers from whole fragment string
        if (Configuration.webInsTraces) {
            outputObj = getMapsWebInspector(ansFragment, allInjectVals, allQueryUnities);
        } else if (Configuration.xmlRespTraces) {
            outputObj = getMapsRealTraces(query, allInjectVals, ansFragment);
        }

        //Get corespondig (new or existing) LDF candidate from current selector
        ctpFormated = getCtpFormat(allQueryUnities, allInjectVals);

        //    BasicUtilis.printInfo("***********************");
        //    BasicUtilis.printInfo("VARS: " + allVars);
        //  BasicUtilis.printInfo("query: "+query);
        //   BasicUtilis.printInfo("Candidate LDF: "+refinedLDFCand);
        //   BasicUtilis.printInfo("Unities: "+selectorUnities);
        //    BasicUtilis.printInfo("Injected vals: " + allInjectVals);
        //   BasicUtilis.printInfo("THe whole fragment: " + ansFragment);
        //  BasicUtilis.printInfo("Only Answers: " + allDistAns);
        // Set or update, new LDF candidate info
        setCtpHashInfo(ctpFormated, outputObj, outputObj, logEntryID, LDFserver, timeStamp, allQueryUnities);
    }

    /**
     * Get injected values of current selector, i.e., triple pattern query
     *
     * @param queryUnities unities of current query
     * @return list of injected values
     */
    private List<String> getInjectFromQuery(List<String> queryUnities) {

        List<String> injectVals = new LinkedList<>();
        int subject=BasicUtilis.TriplePattern.SUBJECT.getValue();
        int object=BasicUtilis.TriplePattern.OBJECT.getValue();
        
        // We skip predicate IRI (k=1), as it is never considered "injected
        checkIfOccured(injectVals, queryUnities.get(subject));
        injectVals.add("");
        checkIfOccured(injectVals, queryUnities.get(object));

        BasicUtilis.addAllShortIRIs(injectVals);

        return injectVals;
    }

    /**
     *
     * @param injectVals
     * @param currUnity
     */
    private void checkIfOccured(List<String> injectVals, String currUnity) {

        String currInjectValue = "";
        boolean occured = false;

        /*BUUUUUUUUUUUUUUUUG*/
        currInjectValue = currUnity;
        currInjectValue = currInjectValue.replaceAll(",_", "_");
        currInjectValue = currInjectValue.replaceAll("__", "_");

        
        if(currInjectValue.equals("http://dbpedia.org/resource/BARACK_OBAMA")){
            
            int oazeaz=0;
        }
        
        for (String key : mapConstToAnsOccurs.keySet()) {

         if(key.equalsIgnoreCase("http://dbpedia.org/resource/Barack_Obama")){
            
            int oazeaz=0;
        }
            // ignore lower or upper case
            if (key.equalsIgnoreCase(currInjectValue)) {
                occured = true;
                break;
            }
        }

        // We consider, for specific values, that are never never pushed from previous selectors
        // but are already known from the LDF client
        if (currInjectValue.contains("well-known") || currInjectValue.contains("google") || currInjectValue.contains("#person")) {
            occured = true;
        }

        if (occured) {
            injectVals.add(currInjectValue);
        } else {
            injectVals.add("");
        }

    }

    /**
     * Get all distinct answers, of the current selector
     *
     * @param ansFragment query/selector's answer fragment in string format
     * @param injectedVal list of injected values of the currrent query/selector
     * @param querySelectorUnities list of query/selector's unities
     * @return list of all query/selector's distinct answers
     */
    private List<String> getMapsWebInspector(String ansFragment, List<String> injectedVal, List<String> querySelectorUnities) {

        List<String> allSelectorUnities = null;
        List<String> allTPunitiesFinal = new LinkedList<>();
        List<String> allTPunities = BasicUtilis.getUnitiesFromSelectorAns(ansFragment);

        //For all IRI answer untities, we check:
        // if the current value, is not the same with the injected values 
        // (shorten or detailed), then and only then, for either a IRI or Literal, save this current value
        allSelectorUnities = BasicUtilis.addAllShortIRIs(querySelectorUnities);

        //update occurances of answers
        allTPunitiesFinal = updateConstOccurs(allTPunities, allSelectorUnities);

        return allTPunitiesFinal;
    }

    /**
     *
     * @param allTPunities
     * @param allSelectorUnities
     */
    public List<String> updateConstOccurs(List<String> allTPunities, List<String> allSelectorUnities) {

        List<String> allTPunitiesFinal = new LinkedList<>();

        for (int i = 0; i < allTPunities.size(); i++) {

            if (!BasicUtilis.elemInListEqualsCaseSen(allSelectorUnities, allTPunities.get(i))
                    && !BasicUtilis.elemInListEqualsCaseSen(allSelectorUnities, BasicUtilis.getShortIRI(allTPunities.get(i)))) {

                allTPunitiesFinal.add(allTPunities.get(i));

                if (allTPunities.get(i).contains("Alcobendas_CF")) {

                    int qsdqsd = 0;
                }
                BasicUtilis.insertToMap(mapConstToAnsOccurs, allTPunities.get(i));
                if (!allTPunities.get(i).contains("http") && allTPunities.get(i).contains(":")) {

                    BasicUtilis.insertToMap(mapConstToAnsOccurs, BasicUtilis.getCompleteIRI(allTPunities.get(i)));
                    allTPunitiesFinal.add(BasicUtilis.getCompleteIRI(allTPunities.get(i)));
                } else if (allTPunities.get(i).contains("http")) {

                    BasicUtilis.insertToMap(mapConstToAnsOccurs, BasicUtilis.getShortIRI(allTPunities.get(i)));
                    allTPunitiesFinal.add(BasicUtilis.getShortIRI(allTPunities.get(i)));
                }
            }

        }

        return allTPunitiesFinal;
    }

    /**
     *
     * @param selectorQuery
     * @param selectorUnities
     * @param answers
     * @return
     */
    private List<String> getMapsRealTraces(String selectorQuery, List<String> selectorUnities, String answers) {

        List<String> allTPunities = new LinkedList<>();
        List<String> allTPunitiesFinal = new LinkedList<>();
        String[] arrayEntities = answers.split("]");

        for (String str : arrayEntities) {

            String[] ansPerPosition = str.split(", ");

            for (String currStr : ansPerPosition) {

                if (currStr.contains("####")) {
                    continue;
                }

                if (currStr.startsWith("\"[") && currStr.contains("http")) {

                    currStr = currStr.substring(2);
                } else if (currStr.startsWith("\"") && currStr.contains("http")) {

                    currStr = currStr.substring(1);
                } else if (currStr.startsWith("[") && currStr.contains("http")) {

                    currStr = currStr.substring(1);
                }

                currStr = currStr.replaceAll("&amp;", "&");
                currStr = currStr.replaceAll(",_", "_");

                if (BasicUtilis.elemInListContained(selectorUnities, currStr) || BasicUtilis.elemInListContained(allTPunities, currStr)) {

                    continue;
                }
                if (currStr.contains("http")) {

                    if (BasicUtilis.elemInListContained(selectorUnities, BasicUtilis.getShortIRI(currStr))
                            || BasicUtilis.elemInListContained(allTPunities, BasicUtilis.getShortIRI(currStr))) {
                        continue;
                    }

                    allTPunities.add(BasicUtilis.getShortIRI(currStr));
                }

                allTPunities.add(currStr);
            }

        }

        allTPunities = BasicUtilis.refineList(allTPunities);

        for (int i = 0; i < allTPunities.size(); i++) {

            if (allTPunities.get(i).contains("####")) {
                continue;
            }

            BasicUtilis.insertToMap(mapConstToAnsOccurs, allTPunities.get(i));
            //Identify a short IRI constant  in its detailed form
            if (!allTPunities.get(i).contains("http") && allTPunities.get(i).contains(":")) {

                BasicUtilis.insertToMap(mapConstToAnsOccurs, BasicUtilis.getCompleteIRI(allTPunities.get(i)));
                allTPunitiesFinal.add(BasicUtilis.getCompleteIRI(allTPunities.get(i)));
            } else if (allTPunities.get(i).contains("http")) {

                BasicUtilis.insertToMap(mapConstToAnsOccurs, BasicUtilis.getShortIRI(allTPunities.get(i)));
                allTPunitiesFinal.add(BasicUtilis.getShortIRI(allTPunities.get(i)));
            }
        }

        allTPunities = BasicUtilis.sortAndRemoveRedundancy(allTPunities);
        return allTPunities;
    }

    /**
     * Get CTP in a refined format. Where a subject, predicate or object can be
     * the corresponding "keyword", or a constant or "POSSIBLY_INJECTED"
     *
     * @param queryUnities list of query unities
     * @param injectedVals list of injected values, of the current query
     * @return candidate CTP format
     */
    private List<String> getCtpFormat(List<String> queryUnities, List<String> injectedVals) {

        List<String> ctpFormat = new LinkedList<>();

        // if the selector unity in the current position is possibly injected, then 
        // it must appear with the corresponding key word
        for (int i = 0; i < queryUnities.size(); i++) {

            if (!injectedVals.get(i).equals("")) {
                ctpFormat.add("POSSIBLY_INJECTED");
            } else {
                ctpFormat.add(queryUnities.get(i));
            }
        }

        return ctpFormat;
    }

    /**
     * Set all information of current log entry to corresponding CTP (exisitng
     * or new), concerning entry id, injected values, answer values
     *
     * @param currCtp current CTP format
     * @param injectVals all injected values of log entry's query
     * @param outputSubj list of distinct answers of current log entry's fragments
     * @param logEntryID current log entry id
     * @param LDFserver current LDF server responding to query
     * @param timeStamp time in total number of seconds, of current log entry
     * @param queryUnities
     */
    private void setCtpHashInfo(List<String> currCtp, List<String> outputSubj, List<String> outputObj,
            int logEntryID, String LDFserver, int timeStamp, List<String> queryUnities) {

        int newID = -1;
        int currID = -1;

        //Identify new Candidate Triple Pattern (CTP) or an new version of an 
        // existing one that differs because of "gap"
        if (BasicUtilis.getValueMap(mapCtpToSerialID, currCtp) == -1) {

            newID = mapCtpToSerialID.size() + 1;
            mapCtpToSerialID.put(newID, currCtp);
        } else {

            currCtp = getCtpGapVol(currCtp, timeStamp);

            if (BasicUtilis.getValueMap(mapCtpToSerialID, currCtp) == -1) {

                newID = mapCtpToSerialID.size() + 1;
                mapCtpToSerialID.put(newID, currCtp);
            }

        }

        currID = BasicUtilis.getValueMap(mapCtpToSerialID, currCtp);

        BasicUtilis.insertToMap2(mapCtpToTimeSecs, timeStamp, currID);
        // match current log entry to CTP
        BasicUtilis.insertToMap2(mapCtpToLogEntries, logEntryID, currID);
        // match current LDF server to CTP
        BasicUtilis.insertToMap(mapCtpToLDFServer, LDFserver, currID);

        // match current (possibly) injected values to CTP
        if (currCtp.get(0).contains("POSSIBLY")) {
            BasicUtilis.insertToMap(mapCtpToInMapsSubj, queryUnities.get(0), currID);
        }

        if (currCtp.get(2).contains("POSSIBLY")) {
            BasicUtilis.insertToMap(mapCtpToInMapsObj, queryUnities.get(2), currID);
        }

        // match current answer values to CTP
        for (int i = 0; i < outputObj.size(); i++) {

            BasicUtilis.insertToMap(mapCtpToOutMapsSubject, outputObj.get(i), currID);
        }
    }

    /**
     * Get a new version of an existing CTP, if it's timepstamp is not convered
     * by the gap value
     *
     * @param originalCtp original identified CTP
     * @param newTimestamp timeStamp of current log entry
     * @return new version of CTP
     */
    private List<String> getCtpGapVol(List<String> originalCtp, int newTimestamp) {

        int newIndx = -1;
        int currID = -1;
        String latestVersion = "";
        List<String> newCtpVol = new LinkedList<>(originalCtp.subList(0, originalCtp.size()));
        List<String> oldCtpVol = new LinkedList<>(originalCtp.subList(0, originalCtp.size()));

        // Check if it can be added in the original version of the LDF candidate
        if (mapCTPtoVersionsGap.get(originalCtp) == null) {

            currID = BasicUtilis.getValueMap(mapCtpToSerialID, originalCtp);
            // If true, then return the original LDF candidate
            if (BasicUtilis.checkTemporalDistance(mapCtpToTimeSecs.get(currID), newTimestamp, Configuration.gapWin)) {
                return oldCtpVol;
            }

            newIndx = 1;
        } // Or else, check if it can be added in different versions of this LDF Candidate
        else {

            newIndx = mapCTPtoVersionsGap.get(originalCtp);

            for (int i = 1; i <= newIndx; i++) {

                oldCtpVol = new LinkedList<>(originalCtp.subList(0, originalCtp.size()));
                latestVersion = "vol_" + Integer.toString(i);
                oldCtpVol.add(latestVersion);
                currID = BasicUtilis.getValueMap(mapCtpToSerialID, oldCtpVol);

                // If true, then return the current version of the LDF candidate
                if (BasicUtilis.checkTemporalDistance(mapCtpToTimeSecs.get(currID), newTimestamp, Configuration.gapWin)) {
                    return oldCtpVol;
                }

            }

            // or create a new  version of LDF Candidate
            newIndx += 1;
        }

        mapCTPtoVersionsGap.put(originalCtp, newIndx);
        latestVersion = "vol_" + Integer.toString(newIndx);
        newCtpVol.add(latestVersion);

        return newCtpVol;
    }

    /**
     * Show the set of CTP
     *
     */
    private void showCTPs() {

        int count = 1;
        List<String> currCtp = null;

        BasicUtilis.printInfo("\n\t----------Candidate Triple Pattern(CTP) set----------\n");
        for (Integer currCTP : mapCtpToSerialID.keySet()) {

            currCtp = mapCtpToSerialID.get(currCTP);
            BasicUtilis.printInfo("\t\t CTP no [" + count + "]: \"" + currCtp.get(0) + " "
                    + currCtp.get(1) + " " + currCtp.get(2));

            count++;
        }

        BasicUtilis.printInfo("\n\t----------Candidate Triple Pattern(CTP) set----------\n");
    }

}