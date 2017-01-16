package mylift;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * Class for detecting nested loops between CTPs, by eventually splitting a
 * candidate triple pattern if its in mappings i.e., constant values where
 * pushed from more than one patterns.
 *
 * Optionally, if "graphReduction" is enabled and before initiating the nested
 * loop detection, merge "count" CTPs with their corresponding "inner" CTPs that
 * are produced during nested loops
 *
 * @author Nassopoulos Georges
 * @version 1.0
 * @since 2016-12-15
 */
public class NestedLoopDetection {

    /*  Deduced Triple Pattern (DTP)'s id - oriented hash/set maps used in the code */
    // match each DTP's id, to its serial id (order of deduction)
    public static HashMap<Integer, List<String>> mapDTPoSerialID;
    // match each DTP's id, to LDF servers that recieved it
    public static HashMap<Integer, List<String>> mapDTPoLDFServer;
    // match each service DTP's id to its corresponding variable of
    //"service-type" mappings-answers, that are injected to another inner LDF
    public static HashMap<String, List<String>> mapDedLDFToServiceLDFAns;
    // match each deduced "membership-type" LDF to its corresponding "signature"
    // i.e., the variable of coresponding "service-type" LDF to which answers 
    // can be found in hashMap "mapDedLDFToServiceLDFAns"
    public static HashMap<Integer, String> mapDedLDFToServiceSignature;

    public NestedLoopDetection() {

        mapDTPoSerialID = new HashMap<>();
        mapDTPoLDFServer = new HashMap<>();
        mapDedLDFToServiceLDFAns = new HashMap<>();
        mapDedLDFToServiceSignature = new HashMap<>();
    }

    /**
     * Search all possible nested loops between CTPs, by possibly splitting an
     * inner if answers came from different outer CTPs, and identify the set of
     * DTPs. Optionally, before Nested Loop Detection, merge "count" CTPs with
     * their corresponding CTP evaluated through nested loop.
     *
     * @return the set of DTP with its serial id
     */
    public HashMap<Integer, List<String>> initNestedDetection() {

        long startTime = 0;
        long finishTime = 0;

        //Merge "count" CTP with the corresponding, evaluated through nested loop
        if (Configuration.graphReduction) {
            CtpExtraction.mapCtpToSerialID = reduceCTPs(CtpExtraction.mapCtpToSerialID);
        }

        //Start Nested Loop Detecion
        mapDTPoSerialID = getNestedLoops();

      //  BasicUtilis.printInfo("\n\t\t\t\t###################### (Elapsed time; "
        //        + (double) ((double) finishTime - (double) startTime) / 1000000000 + " seconds) ###################### \n\n\n");
        return mapDTPoSerialID;
    }

    /**
     * Identify in a nested iteration over the CTP set, all joins between triple
     * pattern pairs.
     *
     * @return set of Deduced Triple Patterns (DTP)
     */
    public HashMap<Integer, List<String>> getNestedLoops() {

        List<String> ctpOuter = null;
        List<String> ctpInner = null;
        List<Integer> currDTPs = new LinkedList<>();
        HashMap<Integer, Integer> mapCTPtoDTPs = new HashMap<>();
        HashMap<Integer, List< List<String>>> mapMatchedCandLDFtoNewCandidate = new HashMap<>();

        //init each CTP with its matching DTP (possibly more than one)
        for (int currKey : CtpExtraction.mapCtpToSerialID.keySet()) {
            mapCTPtoDTPs.put(currKey, -1);
        }

        //Start nested loop detection
        for (int countOuter : CtpExtraction.mapCtpToSerialID.keySet()) {

            ctpOuter = CtpExtraction.mapCtpToSerialID.get(countOuter);

            // Identify outer CTP as a DTP, if it has not any inner mapping, i.e.
            // pushed constants
         //    if (BasicUtilis.elemInListContained(ctpOuter, "http://dbpedia.org/resource/Belgium")) {
            //      
            //     int haha=0;
            //   }
            currDTPs = setDTPInfo(countOuter, mapCTPtoDTPs, currDTPs);

            //then, for all other inner candiCTPs
            for (int countInner : CtpExtraction.mapCtpToSerialID.keySet()) {

                ctpInner = CtpExtraction.mapCtpToSerialID.get(countInner);

                // Check if we skip current pair join comparison, when
                //  (i) inner CTP has not any "inner" mappings i.e., pushed constants, or
                //  (ii) when temporal distance between outerCTP or innerCTP is
                //      not covered by the "gap" value
                if (skipJoin(countOuter, countInner)) {

                    continue;
                }

                // Or identify, if there exist, a nested loop join between the two CTPs
                currDTPs = checkCTPjoin(currDTPs, mapCTPtoDTPs, mapMatchedCandLDFtoNewCandidate, countInner, countOuter);

                /*    if (BasicUtilis.elemInListContained(ctpInner, "http://dbpedia.org/resource/Belgium") || BasicUtilis.elemInListContained(ctpOuter, "http://dbpedia.org/resource/Belgium")) {
                
                 System.out.println("************************************** ");
                 System.out.println("outer TP: "+ctpOuter);
                 System.out.println("inner TP: "+ctpInner);
                 System.out.println("current DTPs: "+mapDTPoSerialID);
                 System.out.println("current DTPs ids: "+currDTPs);
                 System.out.println("************************************** ");
                 }*/
                // If "graphReduction" enabled, check in addition if the "count" 
                // triple pattern of the outer CTP, is joined with inner CTP
                //if(Configuration.graphReduction)
            }

        }

        System.out.println("current DTPs: " + mapDTPoSerialID);
        return mapDTPoSerialID;
    }

    /**
     * Skip the current outer CTP if it has only "in mappings" i.e., constants,
     * and thus cannnot be used as an outer CTP into a nested loop.
     *
     * @return already identified DTPs
     */
    private List<Integer> setDTPInfo(int outerCtpID, HashMap<Integer, Integer> mapCTPtoDTPs, List<Integer> alreadyDeducedCTP) {

        List<String> outerCTP = CtpExtraction.mapCtpToSerialID.get(outerCtpID);
        List<String> deducedTP = new LinkedList<>(outerCTP.subList(0, 3));
        int currID = 0;

        if (!BasicUtilis.elemInListContained(outerCTP, "POSSIBLY") || !BasicUtilis.elemInListEquals(alreadyDeducedCTP, outerCtpID)) {

            currID = BasicUtilis.getValueMap(CtpExtraction.mapCtpToSerialID, outerCTP);
            mapDTPoLDFServer.put(mapDTPoSerialID.size() + 1, CtpExtraction.mapCtpToLDFServer.get(currID));
            mapDTPoSerialID.put(mapDTPoSerialID.size() + 1, deducedTP);
            alreadyDeducedCTP.add(outerCtpID);
            mapCTPtoDTPs.put(outerCtpID, mapDTPoSerialID.size());
        }

        return alreadyDeducedCTP;

    }

    /**
     * Set Deduced Triple Pattern (DTP)'s id from inner Candidate LDF, and
     * deduced pairJoin from identifed nested loop between outer and inner LDFs
     *
     * @param ctpOuter outer candidate LDF
     * @param ctpInner inner candidate LDF
     * @param deducedIDOuter outer candidate LDF's corresponding Deduced Triple
     * Pattern (DTP)'s id id
     * @param indexPushPosition
     * @return
     */
    private List<String> seJoinInfo(List<String> ctpOuter, List<String> ctpInner, int deducedIDOuter, int indexPushPosition) {

        List<String> deducedTP = getNewDTP(ctpInner, ctpOuter, deducedIDOuter, indexPushPosition);
        int currID = BasicUtilis.getValueMap(CtpExtraction.mapCtpToSerialID, ctpOuter);

        mapDTPoLDFServer.put(mapDTPoSerialID.size() + 1, CtpExtraction.mapCtpToLDFServer.get(currID));
        mapDTPoSerialID.put(mapDTPoSerialID.size() + 1, deducedTP);

        return deducedTP;
    }

    /**
     * Skip the current comparison of a pair of CTPs, when (a) the inner CTP has
     * not any input mappings, i.e., no pushed constants, or, (b) when the
     * temporal distance between first and last timestamps of inner and outer
     * CTPs are not covered by the "gap
     *
     * @param outerCtpID id of outer CTP
     * @param innerCtpID id of inner CTP
     * @return
     */
    private boolean skipJoin(int outerCtpID, int innerCtpID) {

        List<String> currCTP = CtpExtraction.mapCtpToSerialID.get(innerCtpID);

        if (outerCtpID == innerCtpID
                || (!BasicUtilis.checkIfTemporalyJoinable(CtpExtraction.mapCtpToTimeSecs.get(outerCtpID), CtpExtraction.mapCtpToTimeSecs.get(innerCtpID), Configuration.gapWin))
                || (!isLDFpreceeding(CtpExtraction.mapCtpToLogEntries.get(outerCtpID), CtpExtraction.mapCtpToLogEntries.get(innerCtpID)))
                || (!BasicUtilis.elemInListEquals(currCTP, "POSSIBLY_INJECTED"))) {

            return true;
        }

        return false;
    }

    /**
     * 7
     *
     * @param alreadyDeducedCTP
     * @param mapCTPtoDTPs
     * @param mapMatchedCandLDFtoNewCandidate
     * @param countInner
     * @param countOuter
     * @return
     */
    private List<Integer> checkCTPjoin(List<Integer> alreadyDeducedCTP, HashMap<Integer, Integer> mapCTPtoDTPs,
            HashMap<Integer, List< List<String>>> mapMatchedCandLDFtoNewCandidate, int countInner, int countOuter) {

        List<String> ctpOuterMaps = CtpExtraction.mapCtpToOutMaps.get(countOuter);
        List<String> pushedVals = null;
        List<String> ctpInnerMapsSubj = CtpExtraction.mapCtpToInMapsSubj.get(countInner);
        List<String> ctpInnerMapsObj = CtpExtraction.mapCtpToInMapsObj.get(countInner);
        List<String> ctpOuter = CtpExtraction.mapCtpToSerialID.get(countOuter);
        List<String> ctpInner = CtpExtraction.mapCtpToSerialID.get(countInner);
        int subject = BasicUtilis.TriplePattern.SUBJECT.getValue();
        int object = BasicUtilis.TriplePattern.OBJECT.getValue();
        List<String> tmpInnerCTP = null;
        int indexPushPosition = -1;

        if (ctpInnerMapsSubj != null && (pushedVals = BasicUtilis.getListsIntersec(ctpOuterMaps, ctpInnerMapsSubj)).size() > 0) {

            indexPushPosition = 0;
        } else if (ctpInnerMapsObj != null && (pushedVals = BasicUtilis.getListsIntersec(ctpOuterMaps, ctpInnerMapsObj)).size() > 0) {

            indexPushPosition = 2;
        }

        //compare outer's orginal answer vals with inner's injected vals, for possible intersection
        if (pushedVals.size() > 0) {

            if (!(ctpInner.get(subject).contains("POSSIBLY") && ctpInner.get(object).contains("POSSIBLY"))) {
                seJoinInfo(ctpOuter, ctpInner, mapCTPtoDTPs.get(countOuter), indexPushPosition);

            } else {
                tmpInnerCTP = getNewDTP(ctpInner, ctpOuter, mapCTPtoDTPs.get(countOuter), indexPushPosition);

                if (mapMatchedCandLDFtoNewCandidate.get(countInner) != null) {

                    for (int z = 0; z < mapMatchedCandLDFtoNewCandidate.get(countInner).size(); z++) {

                        List<String> newInnerCTP = getNewCTP(tmpInnerCTP, mapMatchedCandLDFtoNewCandidate.get(countInner).get(z));
                        seJoinInfo(ctpOuter, newInnerCTP, mapCTPtoDTPs.get(countOuter), indexPushPosition);

                    }
                }

                BasicUtilis.insertToMap1(mapMatchedCandLDFtoNewCandidate, tmpInnerCTP, countInner);
            }

            mapCTPtoDTPs.put(countInner, mapDTPoSerialID.size());
            alreadyDeducedCTP.add(countInner);

        }

        return alreadyDeducedCTP;
    }

    /**
     *
     * @param tmpLDFCandInner
     * @param previousLDFCandInner
     * @return
     */
    private List<String> getNewCTP(List<String> tmpLDFCandInner, List<String> previousLDFCandInner) {

        List<String> newCTP = new LinkedList<>();

        int subject = BasicUtilis.TriplePattern.SUBJECT.getValue();
        int object = BasicUtilis.TriplePattern.OBJECT.getValue();

        for (int i = 0; i < 3; i++) {

            if (tmpLDFCandInner.get(i).contains("POSSIBLY") && !previousLDFCandInner.get(i).contains("POSSIBLY")) {

                newCTP.add(previousLDFCandInner.get(i));
            } else if (!tmpLDFCandInner.get(i).contains("POSSIBLY") && previousLDFCandInner.get(i).contains("POSSIBLY")) {

                newCTP.add(tmpLDFCandInner.get(i));
            } else {

                newCTP.add(tmpLDFCandInner.get(i));
            }

        }

        return newCTP;
    }

    /**
     *
     * @param LDFCandInner
     * @param LDFCandOuter
     * @param indxOuter
     * @param indexPushPosition
     * @return
     */
    private List<String> getNewDTP(List<String> LDFCandInner, List<String> LDFCandOuter, int indxOuter, int indexPushPosition) {

        List<String> deduecedTP = new LinkedList<>();
        String matchingVar = "";
        int subject = BasicUtilis.TriplePattern.SUBJECT.getValue();
        int object = BasicUtilis.TriplePattern.OBJECT.getValue();
        //Create deduced variable of nested loop
        if (LDFCandOuter.get(subject).contains("subject")) {

            matchingVar = "INJECTEDsubj(LDF_" + Integer.toString(indxOuter) + ")";

        } else if (LDFCandOuter.get(object).contains("object")) {

            matchingVar = "INJECTEDobj(LDF_" + Integer.toString(indxOuter) + ")";

        }

        // Create new Deduced Triple Pattern (DTP)'s id
        for (int i = 0; i < LDFCandInner.size(); i++) {

            if (LDFCandInner.get(i).contains("POSSIBLY_INJECTED") && !matchingVar.equalsIgnoreCase("") && indexPushPosition == i) {

                deduecedTP.add(matchingVar);
            } else {

                deduecedTP.add(LDFCandInner.get(i));
            }
        }

        return deduecedTP;
    }

    /**
     * Refine list of LDF candidates for every "service" LDF Candidate (i.e.,
     * like "ASK" queries with metadata and control info), in two cases:
     *
     * (a) Merge the "service" LDF candidate with it corresponding "memership"
     * LDF candidate (i.e., their answers and log entries), or else
     *
     * (b) Remove "service" LDF candidate selectors if it does not participate
     * in any nested loop
     *
     * @param mapRawCandidates raw hashMap of all LDF candidates
     * @return refined hashMap of LDF candidates
     */
    private HashMap<Integer, List<String>> reduceCTPs(HashMap<Integer, List<String>> mapRawCandidates) {

        List<Integer> removedCountCtps = mergeCountAndRealTPs(mapRawCandidates);
        HashMap<Integer, List<String>> graphReduced = resetCTPhashMaps(mapRawCandidates, removedCountCtps);

        return graphReduced;
    }

    /**
     * Identify list of LDF candidates of type "service" ( i.e. like "ASK"
     * queries with metadata and control info) and merge each one with its
     * corresponding "memership" LDF candidate
     *
     * @param mapRawCandidates hashMap of all LDF candidates
     * @return list of "Service" LDF Candidates, merged with corresponfing
     * Membership
     */
    private List<Integer> mergeCountAndRealTPs(HashMap<Integer, List<String>> mapRawCandidates) {

        List<String> ctpOuter = null;
        List<String> ctpInner = null;
        List<Integer> tmpOuter = null;
        List<Integer> tmpInner = null;
        List<Integer> removedCntTPs = new LinkedList<>();
        String outerSubj = "", outerPred = "", outerObj = "";
        String innerSubj = "", innerPred = "", innerObj = "";

        int subject = BasicUtilis.TriplePattern.SUBJECT.getValue();
        int predicate = BasicUtilis.TriplePattern.PREDICATE.getValue();

        int object = BasicUtilis.TriplePattern.OBJECT.getValue();

        // For all outer candidate LDFs that are possibly "Service"
        for (int countOuter : mapRawCandidates.keySet()) {

            ctpOuter = mapRawCandidates.get(countOuter);

            outerSubj = ctpOuter.get(subject);
            outerPred = ctpOuter.get(predicate);
            outerObj = ctpOuter.get(object);

            // in case the outer LDF Candidate, has not a "POSSIBLY_INJECTED" 
            // to be the inner part of a nested loop
            if (!BasicUtilis.elemInListEquals(ctpOuter, "POSSIBLY_INJECTED")) {

                // For all inner candidate LDFs that can possibly be the inner part 
                //of a nested loop
                for (int countInner : mapRawCandidates.keySet()) {

                    ctpInner = mapRawCandidates.get(countInner);

                    innerSubj = ctpInner.get(subject);
                    innerPred = ctpInner.get(predicate);
                    innerObj = ctpInner.get(object);

                    if (BasicUtilis.elemInListEquals(ctpInner, "POSSIBLY_INJECTED")) {

                        //Carefull: and also common object or subject ellement
                        // which must not be an "object" or "subject"
                        if (countOuter < countInner && innerPred.equalsIgnoreCase(outerPred)
                                && (innerObj.equalsIgnoreCase(outerObj) || innerSubj.equalsIgnoreCase(outerSubj))) {

                            removedCntTPs.add(countOuter);
                            tmpOuter = CtpExtraction.mapCtpToTimeSecs.get(countOuter);
                            tmpInner = CtpExtraction.mapCtpToTimeSecs.get(countInner);
                            tmpOuter.addAll(tmpInner);
                            CtpExtraction.mapCtpToTimeSecs.put(countInner, tmpOuter);
                            mapDedLDFToServiceLDFAns.put(Integer.toString(countInner) + "_subj", CtpExtraction.mapCtpToOutMaps.get(countOuter));
                            mapDedLDFToServiceSignature.put(countInner, Integer.toString(countInner) + "_subj");
                        }
                    }

                }
            }

        }

        return removedCntTPs;
    }

    /**
     * Find if an outer LDF candidate preceeds an inner Candidate. This is used
     * because of the "streaming" LDF evaluation, where injection is "paged" in
     * several rounds
     *
     * @param outerLogEntries outer LDF candidate's log entries
     * @param innerLogEntries inner LDF candidate's log entries
     * @return true if outer LDF candidate preceeds the inner
     */
    private boolean isLDFpreceeding(List<Integer> outerLogEntries, List<Integer> innerLogEntries) {

        boolean flagPreceds = false;

        for (int i = 0; i < innerLogEntries.size(); i++) {

            for (int j = 0; j < outerLogEntries.size(); j++) {

                if (innerLogEntries.get(i) > outerLogEntries.get(j)) {

                    return true;
                }
            }
        }

        return flagPreceds;
    }

    /**
     * Get refined list of LDF candidates, by removing Service LDF Candidates
     * that were merged with membership queries and reset corresponding hash
     * maps
     *
     * @param mapRawCandidates hashMap of all LDF candidates
     * @param mergedService list of Service LDFs merged with Membership
     * @return refined hashMap of LDF candidates
     */
    private HashMap<Integer, List<String>> resetCTPhashMaps(HashMap<Integer, List<String>> mapRawCandidates, List<Integer> mergedService) {

        HashMap<Integer, List<String>> reducedGraph = new HashMap<>();
        HashMap<Integer, List<String>> tmpCtpToInMapsSubj = new HashMap<>();
        HashMap<Integer, List<String>> tmpCtpToInMapsObj = new HashMap<>();
        HashMap<Integer, List<String>> tmpCtpToOutMaps = new HashMap<>();
        HashMap<Integer, List<String>> mapCtpToSerialDs = new HashMap<>();

        HashMap<String, List<String>> mapDedLDFToServiceLDFAnsFinal = new HashMap<>();
        HashMap<Integer, String> mapDedLDFToServiceSignatureFinal = new HashMap<>();
        HashMap<Integer, List<Integer>> mapCandDFToTimeSecsFinal = new HashMap<>();

        String currServSign = "";
        int countNew = 0;

        for (int countOuter : mapRawCandidates.keySet()) {

            //If candidate LDF
            if (!BasicUtilis.elemInListEquals(mergedService, countOuter)) {

                countNew++;
                reducedGraph.put(countNew, mapRawCandidates.get(countOuter));

                if (CtpExtraction.mapCtpToInMapsSubj.get(countOuter) != null && CtpExtraction.mapCtpToInMapsObj.get(countOuter) != null) {

                    tmpCtpToInMapsSubj.put(countNew, CtpExtraction.mapCtpToInMapsSubj.get(countOuter));
                    tmpCtpToInMapsObj.put(countNew, CtpExtraction.mapCtpToInMapsObj.get(countOuter));
                }

                tmpCtpToOutMaps.put(countNew, CtpExtraction.mapCtpToOutMaps.get(countOuter));
                mapCandDFToTimeSecsFinal.put(countNew, CtpExtraction.mapCtpToTimeSecs.get(countOuter));
                mapCtpToSerialDs.put(countNew, mapRawCandidates.get(countOuter));

                // save mappings of the corresponding "service" LDF candidate, 
                // to this corresponding "membership-type" LDF candidate
                if (mapDedLDFToServiceSignature.get(countOuter) != null) {

                    currServSign = mapDedLDFToServiceSignature.get(countOuter).substring(mapDedLDFToServiceSignature.get(countOuter).indexOf("_"));
                    mapDedLDFToServiceLDFAnsFinal.put(Integer.toString(countNew) + currServSign, mapDedLDFToServiceLDFAns.get(mapDedLDFToServiceSignature.get(countOuter)));
                    mapDedLDFToServiceSignatureFinal.put(countNew, Integer.toString(countNew) + currServSign);
                }

            }
        }

        CtpExtraction.mapCtpToInMapsSubj = new HashMap<>();
        CtpExtraction.mapCtpToInMapsObj = new HashMap<>();
        CtpExtraction.mapCtpToOutMaps = new HashMap<>();
        CtpExtraction.mapCtpToSerialID = new HashMap<>();
        mapDedLDFToServiceLDFAns = new HashMap<>();
        mapDedLDFToServiceSignature = new HashMap<>();
        CtpExtraction.mapCtpToTimeSecs = new HashMap<>();

        CtpExtraction.mapCtpToTimeSecs = (HashMap<Integer, List<Integer>>) mapCandDFToTimeSecsFinal.clone();
        CtpExtraction.mapCtpToInMapsObj = (HashMap<Integer, List<String>>) tmpCtpToInMapsObj.clone();
        CtpExtraction.mapCtpToInMapsSubj = (HashMap<Integer, List<String>>) tmpCtpToInMapsSubj.clone();
        CtpExtraction.mapCtpToOutMaps = (HashMap<Integer, List<String>>) tmpCtpToOutMaps.clone();
        CtpExtraction.mapCtpToSerialID = (HashMap<Integer, List<String>>) mapCtpToSerialDs.clone();
        mapDedLDFToServiceLDFAns = (HashMap<String, List<String>>) mapDedLDFToServiceLDFAnsFinal.clone();
        mapDedLDFToServiceSignature = (HashMap<Integer, String>) mapDedLDFToServiceSignatureFinal.clone();

        return reducedGraph;
    }

}