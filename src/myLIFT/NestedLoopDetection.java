package myLIFT;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import static myLIFT.Deduction.dedGraphSelect;
import static myLIFT.Deduction.mapLogClQueryToAllTPEnts;
import static myLIFT.Deduction.mapLogClQueryToDedGraph;
import static myLIFT.Deduction.mapLogClQueryToTimeSecs;
import static myLIFT.Deduction.totalPairs;
import static myLIFT.LoadFromDB.mapCTPtoFILTERwithBoundJ;
import static myLIFT.LoadFromDB.queries;
import static myLIFT.DeductionUtils.DTPCandidates;
import static myLIFT.Main.inverseBestMap;
import static myLIFT.Main.inverseMapping;
import static myLIFT.Main.inverseThresh;
import static myLIFT.Main.windowJoin;

/**
 * Class for "NestedLoopDetection" heuristic, first by extracting all Candidate
 * Triple Patterns (CTPs) and either inverse map all CTPs constant values as the
 * inner part of a nested loop implemention with a previously Deduced Triple
 * Pattern (DTP), or identify the CTP directly as a DTP.
 *
 * @author Nassopoulos Georges
 * @version 1.0
 * @since 2016-03-19
 */
public class NestedLoopDetection {

    BasicUtilis myBasUtils;
    DeductionUtils myDedUtils;

    private boolean flagSTOPNONPROJECTEDvars = false;

    //******************************Hash Maps concerning Candidate Triple Patterns (CTPs)******************************//
    // list of all Candidate Triple Patterns
    public static List<List<String>> allCTPs;
    // concering the deduced graph of queries that contain the CTP
    public static HashMap<Integer, Integer> mapCTPtoDedGraph;
    // concerning source queries containing it
    public static HashMap<List<String>, List<Integer>> mapCTPtoQuerySrc;
    // concerning source queries' endpoints
    public static HashMap<List<String>, List<String>> mapCTPToEndpsSrc;
    // concerning constant values (IRIs or Litarals) to be searched in inverse mapping
    public static HashMap<List<String>, List<String>> mapCTPtoConstants;
    // concerning first query's timestamp
    public static HashMap<Integer, String> mapCTPToStartTime;
    // concerning latest query's timestamp
    public static HashMap<Integer, String> mapCTPToFinishTime;
    // concerning source queries' answers, for CTP variables
    public static HashMap<List<String>, List<String>> mapCTPtoAnswTotal;

    //******************************Hash Maps concerning Deduced Triple Patterns (DTP)******************************//   
    // concerning to its serial deduction id 
    public static HashMap<List<String>, Integer> mapDTPToDeducedID;
    // concerning source queries containing it, possible inner nested loop part
    public static HashMap<Integer, List<Integer>> mapDTPtoInnerQuery;
    // concerning queries injecting values as constants, certified as an outer nested loop part
    public static HashMap<Integer, List<Integer>> mapDTPtoOuterQuery;
    // concerning source queries' endpoints
    public static HashMap<List<String>, List<String>> mapDTPToEndpsSrc;
    // concerning first query's timestamp
    public static HashMap<List<String>, Integer> mapDTPToStartTime;
    // concerning latest query's timestamp
    public static HashMap<List<String>, Integer> mapDTPToFinishTime;
    // concerning source answers, for deduced free (i.e. not hidden) variables
    public static HashMap<List<String>, List<String>> mapDTPtoAnswTotal;
    // concerning matched answers, for variables matched during inverse mapping 
    public static HashMap<List<String>, List<String>> mapDTPtoAnsInverseMap;
    // concerning NotNullJoin BGP containing it 
    public static HashMap<List<String>, Integer> mapDTPtoJoinBGP;
    // concerning Exclusive Group containing it 
    public static HashMap<List<String>, Integer> mapDTPtoExclGroup;
    // concerning the BGP to which a DTP belongs
    public static HashMap<Integer, Integer> mapDTPToAnyJoin;
    // concerning alternative DTPs, with differents vars for common values
    public static HashMap<List<String>, Integer> mapDTPtoAlternatives;
    // concerning DTP that participate in an exclusive group
    public static HashMap<List<String>, Integer> mapDTPofEGNested;
    // concerning DTP that participate in a bound join implementation
    public static HashMap<List<String>, Integer> mapSrcTPtoBoundJoin;
    // concerning DTP that participate in a single triple pattern query
    public static HashMap<List<String>, Integer> mapSrcTPtoSingleTPQuery;

    //******************************Hash Maps concerning joins between Deduced Triple Patterns******************************//  
    // concerning an Exclusive Group
    public static HashMap<List<List<String>>, Integer> mapPairExclGroup;
    // concerning each list of temporal exclusive groups
    public static HashMap<Integer, List<String>> mapTmpEGtoAllTPs;
    // concerning which exclusive groups are going to be canceled (nested loop with exclusive groups)
    public static HashMap<Integer, Integer> mapEGtoCancel;
    // concerning occurances of exclusive group (and nested loop with exclusive groups)
    public static HashMap<List<String>, Integer> mapEGtoOccurs;
    // concerning type of join: "Alternative inverese mapping vars", "exclusive groups", "nested loop" or "symmetric hash"
    public static HashMap<List<List<String>>, String> mapPairToRelation;
    // affecting a BGP graph to a level of confidence
    public static HashMap<Integer, Float> mapBGPtoConfidence;
    // concerning all deduced join BGPs
    public static List<List<List<String>>> notnullJoinBGPs;

    public NestedLoopDetection() {

        myDedUtils = new DeductionUtils();
        myBasUtils = new BasicUtilis();

        allCTPs = null;
        mapCTPtoQuerySrc = new HashMap<>();
        mapCTPtoConstants = new HashMap<>();
        mapCTPtoAnswTotal = new HashMap<>();
        mapCTPToFinishTime = new HashMap<>();
        mapCTPToStartTime = new HashMap<>();
        mapCTPToEndpsSrc = new HashMap<>();
        mapCTPtoDedGraph = new HashMap<>();

        mapDTPToDeducedID = new HashMap<>();
        mapDTPToFinishTime = new HashMap<>();
        mapDTPToStartTime = new HashMap<>();
        mapDTPtoJoinBGP = new HashMap<>();
        mapDTPtoInnerQuery = new HashMap<>();
        mapDTPtoOuterQuery = new HashMap<>();
        mapDTPtoExclGroup = new HashMap<>();
        mapDTPtoAlternatives = new HashMap<>();
        mapDTPToAnyJoin = new HashMap();
        mapDTPToEndpsSrc = new HashMap<>();
        mapDTPtoAnswTotal = new HashMap<>();
        mapDTPtoAnsInverseMap = new HashMap<>();
        mapDTPofEGNested = new HashMap<>();
        mapSrcTPtoBoundJoin = new HashMap<>();
        mapSrcTPtoSingleTPQuery = new HashMap<>();

        mapPairExclGroup = new HashMap<>();
        mapTmpEGtoAllTPs = new HashMap<>();
        mapEGtoOccurs = new HashMap<>();
        mapEGtoCancel = new HashMap<>();
        notnullJoinBGPs = new LinkedList<>();
        mapPairToRelation = new HashMap<>();
        mapBGPtoConfidence = new HashMap<>();
    }

    /**
     * After a preprocessing phase of extracting all Candidate Triple Patterns
     * (CTP) from all queries, we infer nested loops for two types of
     * implementation:
     *
     * (i) "bound Join": we match CTP constant values (IRIs/Literals) to mapping
     * variables of previously deduced TPs and identify hidden JOIN variables
     *
     * (ii) "filter options". we identify subquerys' filter options as answers
     * of a previously evaluated subquery, and their respective CTPs. The
     * difference with "bound join" is that join variables are not hidden, but
     * confirmed
     *
     * In other case, we identify directly the CTP as a DTP
     *
     * @param windowSlice deduction window, defining DB slice
     * @throws java.sql.SQLException
     * @throws java.lang.InstantiationException
     * @throws java.lang.IllegalAccessException
     */
    public void NestedLoopDetection(int windowSlice) throws SQLException, InstantiationException, IllegalAccessException {

        long startTime = 0;
        long finishTime = 0;
        long elapsedTime = 0;
        List<String> allCTPconsts = null;
        List<String> tmpCTP = null;

        long tmpTime = System.nanoTime();
        System.out.println("[START] ---> Triple Pattern's extraction");
        System.out.println();
        allCTPs = getCTPsfromGraphs(dedGraphSelect);
        System.out.println("[FINISH] ---> Triple Pattern's extraction (Elapsed time: " + (System.nanoTime() - tmpTime) / 1000000000 + " seconds)");
        System.out.println();
        System.out.println("-----------------------------------------------------------------------------------------------------------------");
        System.out.println("-----------------------------------------------------------------------------------------------------------------");
        System.out.println();
        startTime = System.nanoTime();
        System.out.println("[START] ---> NestedLoopDetection heuristic");
        System.out.println();

        //Try to match every CTP's constant value, (IRI/Literal) to reveal hidden
        //variables, or, directly match a CTP (when inverseMapping is disabled 
        //or CTP's subject and object are variables)
        for (int i = 0; i < allCTPs.size(); i++) {

            allCTPconsts = myBasUtils.sortAndRemoveRedundancy(myDedUtils.getValuesFromCTP(i));
            tmpCTP = myDedUtils.getCleanTP(allCTPs.get(i));

            if (!inverseMapping) {

                myDedUtils.setDTPHashInfo(tmpCTP, i);
                allCTPconsts = myDedUtils.getValuesFromCTP(i);
                flagSTOPNONPROJECTEDvars = myDedUtils.setDTPtoSrcAns(i, allCTPconsts, allCTPs.get(i), flagSTOPNONPROJECTEDvars);
            } else {

                getMatchVarsOfCTP(i);
            }

        }

        //Get FILTER values, for ANAPSID trace's inner subqueres of NLFO
        checkNLFOJoin();

        //Cancel exclusive groups, if they are identified as a NLEG implementation of FedX
        cancelJoinsNLEGJoin();

        //Search for possible double NLBJ implementation of FedX on subject and object 
        //The second NLBJ is implemented as FILTER option
        checkNLBJwithFilter();

        finishTime = System.nanoTime();
        elapsedTime = finishTime - startTime;
        System.out.println();
        System.out.println("[FINISH] ---> NestedLoopDetection heuristic (Elapsed time: " + elapsedTime / 1000000000 + " seconds)");
    }

    /**
     * Extract all Candidate Triple Patterns (CTPs) from all deduced graphs
     *
     * @param listOfDedGraphs input of all deduced graphs
     * @return list of Candidate Triple Patterns (CTP)
     */
    public List<List<String>> getCTPsfromGraphs(List<List<Integer>> listOfDedGraphs) {

        List<String> queryTriplets = null;
        int count = 0;

        for (int i = 0; i < listOfDedGraphs.size(); i++) {

            Collections.sort(listOfDedGraphs.get(i));
            for (int j = 0; j < listOfDedGraphs.get(i).size(); j++) {

                queryTriplets = mapLogClQueryToAllTPEnts.get(listOfDedGraphs.get(i).get(j) - 1);
                getCTPfromQuery(queryTriplets, i, listOfDedGraphs.get(i).get(j) - 1);

            }
        }

        System.out.println();
        System.out.println("\t================ Candidate Triple Patterns (CTPs)================");
        System.out.println();

        for (int i = 0; i < DTPCandidates.size(); i++) {

            count++;
            System.out.println("\t\t\tCTP no [" + count + "] " + DTPCandidates.get(i).get(0) + " "
                    + DTPCandidates.get(i).get(1) + " " + DTPCandidates.get(i).get(2) + " ");
        }

        System.out.println();
        System.out.println("\t================ Candidate Triple Patterns (CTPs)================");
        System.out.println();

        return DTPCandidates;
    }

    /**
     * Parse query triplets of each suquery and identify new CTP or update info
     * of an exisitng one
     *
     * @param queryTriplets all query's triplets in form of entities
     * @param dedGraphId deduced graph of queries, containing this query
     * @param indxLogCleanQuery index of current query
     */
    public void getCTPfromQuery(List<String> queryTriplets, int dedGraphId, int indxLogCleanQuery) {

        int indxValue = -1;
        int indxLogQueryDedGraph = mapLogClQueryToDedGraph.get(indxLogCleanQuery);
        int indxNewTPDedGraph = -1;
        List<String> tmpTripletClean = null;
        List<Integer> allIdPats = null;
        List<Integer> deducedTPnotCoveredTimestamp = new LinkedList<>();
        String strDedQueryId = Integer.toString(dedGraphId);
        boolean flagTriplePatternOutOfTimeRange = false;

        //For all triple patterns
        for (int f = 0; f < queryTriplets.size(); f += 3) {

            tmpTripletClean = myDedUtils.getCleanTP(new LinkedList<>(queryTriplets.subList(f, f + 3)));

            //Check if query is an Exclusive Group
            checkTPinEG(queryTriplets, tmpTripletClean, indxLogCleanQuery);

            //Check if query is a Bound Join implementation or it's a single TP query
            checkSingleTPorBoundJ(queryTriplets, tmpTripletClean, indxLogCleanQuery);

            //[CASE A] When both subjects and objects are variables, or inverseMapping is disabled
            if (tmpTripletClean.get(0).contains("?") && tmpTripletClean.get(2).contains("?") || !inverseMapping) {

                //A_(i) It's the frist time we see this CTP
                allIdPats = myDedUtils.getIdemCTPs(DTPCandidates, tmpTripletClean.get(0), tmpTripletClean.get(1), tmpTripletClean.get(2));

                if (allIdPats.isEmpty()) {

                    myDedUtils.setNewCTPInfo(tmpTripletClean, "", indxLogCleanQuery, indxLogQueryDedGraph, strDedQueryId, "");
                    myDedUtils.setTPtoSrcAns(tmpTripletClean, indxLogCleanQuery, "", DTPCandidates.size() - 1);
                } //A_(ii) It's not the first time we identify it
                else {

                    //Then, we must be sure that it's not an existing CTP
                    for (int l = allIdPats.size() - 1; l >= 0; l--) {

                        indxNewTPDedGraph = mapCTPtoDedGraph.get(allIdPats.get(l));

                        //First we check it belongs to the same graph with previous identified CTP
                        if (indxNewTPDedGraph != indxLogQueryDedGraph) {

                            flagTriplePatternOutOfTimeRange = true;
                            myDedUtils.setNewCTPInfo(tmpTripletClean, tmpTripletClean.get(0), indxLogCleanQuery, indxLogQueryDedGraph, strDedQueryId, "_" + allIdPats.size());
                            myDedUtils.setTPtoSrcAns(tmpTripletClean, indxLogCleanQuery, "", DTPCandidates.size() - 1);
                            deducedTPnotCoveredTimestamp.add(DTPCandidates.size() - 1);
                            break;
                        } //if not, it's a new CTP (we distinguish them with "_#number")
                        //This happens when Tjoin is not big enough to merge some subqueries
                        //with same characteristics
                        else {

                            myDedUtils.updateCTPInfo(tmpTripletClean, "", indxLogCleanQuery, indxLogQueryDedGraph, allIdPats.get(l));
                            myDedUtils.setTPtoSrcAns(tmpTripletClean, indxLogCleanQuery, "", allIdPats.get(l));

                            if ((DTPCandidates.get(allIdPats.get(l)).get(0).contains("?") && DTPCandidates.get(allIdPats.get(l)).get(0).contains("_"))
                                    || (DTPCandidates.get(allIdPats.get(l)).get(2).contains("?") && DTPCandidates.get(allIdPats.get(l)).get(2).contains("_"))) {

                                deducedTPnotCoveredTimestamp.add(allIdPats.get(l));
                            }

                            break;
                        }

                    }

                }

            } //If subject or object is a constant, we repeat the procedure depending 
            //on if it is a Single TP or part of BoundJoin
            else {

                if (inverseMapping) {

                    indxValue = myDedUtils.getIndxConstant(tmpTripletClean);
                }

                setOrUpdateCTPList(tmpTripletClean, indxValue, strDedQueryId, indxLogCleanQuery);
            }

        }

        //check for  an exclusive group relation between CTP
        //It could be a EG or NLEG
        if (queryTriplets.size() >= 6 && !flagTriplePatternOutOfTimeRange && !queries.get(indxLogCleanQuery).contains("UNION")) {

            if (checkEGJoin(queryTriplets)) {
                for (int i = 0; i < deducedTPnotCoveredTimestamp.size(); i++) {
                    if (mapDTPToAnyJoin.get(deducedTPnotCoveredTimestamp.get(i)) == null) {

                        mapDTPToDeducedID.put(DTPCandidates.get(deducedTPnotCoveredTimestamp.get(i)), deducedTPnotCoveredTimestamp.get(i));
                        mapDTPToAnyJoin.put(deducedTPnotCoveredTimestamp.get(i), -1);
                    }
                }
            }
        }

    }

    /**
     * Check if identified CTP is part of an EG
     *
     * @param queryTriplets all query's triplets in form of entities
     * @param currTP current triple pattern
     * @param indxLogCleanQuery index of current query
     */
    public void checkTPinEG(List<String> queryTriplets, List<String> currTP, int indxLogCleanQuery) {

        if (queryTriplets.size() > 6 && !queries.get(indxLogCleanQuery).contains("UNION") && !queries.get(indxLogCleanQuery).contains("_0")) {

            if (!currTP.get(0).contains("?") || !currTP.get(2).contains("?")) {

                mapDTPofEGNested.put(currTP, 1);
            }

        }
    }

    /**
     * Particular function for FedX traces. Check if identified CTP is part of
     * an bound join
     *
     * @param queryTriplets all query's triplets in form of entities
     * @param currTP current triple pattern
     * @param indxLogCleanQuery index of current query
     */
    public void checkSingleTPorBoundJ(List<String> queryTriplets, List<String> currTP, int indxLogCleanQuery) {

        //save all RAW triple patterns participating into a bound JOIN
        if (queries.get(indxLogCleanQuery).contains("UNION") && queries.get(indxLogCleanQuery).contains("_0")) {

            mapSrcTPtoBoundJoin.put(currTP, 1);
        }

        if (queryTriplets.size() == 3 && !queries.get(indxLogCleanQuery).contains("UNION") && !queries.get(indxLogCleanQuery).contains("_0")) {

            if (!currTP.get(0).contains("?") || !currTP.get(2).contains("?")) {

                mapSrcTPtoSingleTPQuery.put(currTP, 1);
            }

        }
    }

    /**
     * Identify new CTP or update existing CTP info
     *
     * @param currTP current triple pattern
     * @param indxConstValue index of constant value of a triple pattern,
     * subject or object
     * @param strDedQueryId BUUUUUUUUUUUUUUUUUG
     * @param indxLogCleanQuery index of current query
     */
    public void setOrUpdateCTPList(List<String> currTP, int indxConstValue, String strDedQueryId, int indxLogCleanQuery) {

        int indxLogQueryDedGraph = mapLogClQueryToDedGraph.get(indxLogCleanQuery);
        int indxNewTPDedGraph = -1;
        List<Integer> allIdPats = new LinkedList<>();
        String constantVal = myDedUtils.getConstantVal(currTP);

        //First, check for matching to existing CTP on two out of three entities 
        //and differning on subject or object
        if (inverseMapping) {
            switch (indxConstValue) {

                case 0:

                    allIdPats = myDedUtils.getDerivedCTPs(DTPCandidates, currTP.get(1), 1, currTP.get(2), 2);
                    break;
                case 1:

                    allIdPats = myDedUtils.getDerivedCTPs(DTPCandidates, currTP.get(0), 0, currTP.get(2), 2);
                    break;
                case 2:

                    allIdPats = myDedUtils.getDerivedCTPs(DTPCandidates, currTP.get(0), 0, currTP.get(1), 1);
                    break;
            }
        } else {

            allIdPats = myDedUtils.getIdemCTPs(DTPCandidates, currTP.get(0), currTP.get(1), currTP.get(2));
        }

        //A_(i) It's the frist time we see this CTP
        if (allIdPats.isEmpty()) {

            myDedUtils.setNewCTPInfo(currTP, constantVal, indxLogCleanQuery, indxLogQueryDedGraph, strDedQueryId, "");
            myDedUtils.setTPtoSrcAns(currTP, indxLogCleanQuery, "", DTPCandidates.size() - 1);
        } //A_(ii) It's not the first time we identify it
        else if (allIdPats.size() > 0) {

            //Then, we must be sure that it's not an existing CTP
            for (int l = allIdPats.size() - 1; l >= 0; l--) {

                indxLogQueryDedGraph = mapLogClQueryToDedGraph.get(indxLogCleanQuery);
                indxNewTPDedGraph = mapCTPtoDedGraph.get(allIdPats.get(l));
                int startTime = myBasUtils.getTimeInSec(mapCTPToStartTime.get(allIdPats.get(l)));
                int stopTime = myBasUtils.getTimeInSec(mapCTPToFinishTime.get(allIdPats.get(l)));

                //First we check their timestamp's intervals are cover by Tjoin
                if (indxNewTPDedGraph != indxLogQueryDedGraph && (!(mapLogClQueryToTimeSecs.get(indxLogCleanQuery) - startTime <= windowJoin)
                        || !(mapLogClQueryToTimeSecs.get(indxLogCleanQuery) - stopTime <= windowJoin))) {

                    myDedUtils.setNewCTPInfo(currTP, constantVal, indxLogCleanQuery, indxLogQueryDedGraph, strDedQueryId, "_" + Integer.toString(allIdPats.size() + 1));
                    myDedUtils.setTPtoSrcAns(currTP, indxLogCleanQuery, "", DTPCandidates.size() - 1);
                    break;
                } //if not, it's a new CTP (we distinguish them with "_#number")
                else {

                    if (DTPCandidates.get(allIdPats.get(l)).get(0).contains("_")) {

                        myDedUtils.updateCTPInfo(DTPCandidates.get(allIdPats.get(l)), constantVal, indxLogCleanQuery, indxLogQueryDedGraph, allIdPats.get(l));
                        myDedUtils.setTPtoSrcAns(DTPCandidates.get(allIdPats.get(l)), indxLogCleanQuery, "", allIdPats.get(l));
                    } else {

                        myDedUtils.updateCTPInfo(currTP, constantVal, indxLogCleanQuery, indxLogQueryDedGraph, allIdPats.get(l));
                        myDedUtils.setTPtoSrcAns(currTP, indxLogCleanQuery, "", allIdPats.get(l));
                    }

                    break;
                }

            }

        }
    }

    /**
     * Particular function for FedX traces. Identify exclusive groups and
     * combine respective DTPs and at the same time check for possible Nested
     * Loop with Exclusive Groups implementation.
     *
     * @param queryTriplets tps of a query, in form of query entities
     * @return true, if query are previously identified as an exclusive group
     */
    public boolean checkEGJoin(List<String> queryTriplets) {

        boolean flagEG = false;
        boolean foundEG = false;
        List<List<String>> newEGpair = null;
        List<List<String>> newEGpair2 = null;
        List<String> innerDTP = null;
        List<String> outerDTP = null;
        List<String> currEG = null;

        if (queryTriplets.size() >= 6) {

            for (int key : mapTmpEGtoAllTPs.keySet()) {

                currEG = mapTmpEGtoAllTPs.get(key);
                int commElems = candidateTPcomElems(currEG, queryTriplets);

                if (currEG.size() == queryTriplets.size()) {

                    // the second condition, is used to capture Nested Loop with EG operator, made by FedX
                    if (commElems == currEG.size() || commElems == currEG.size() - 1) {

                        if (commElems == currEG.size() - 1) {

                            mapEGtoCancel.put(key, 1);
                            mapEGtoOccurs.put(currEG, 2);
                        }

                        foundEG = true;
                        break;
                    }

                }

            }

            //If it's the first time we see this EG or NLEG, we save each pairWise join as EG
            if (!foundEG) {

                int indEG = mapTmpEGtoAllTPs.size();
                mapTmpEGtoAllTPs.put(indEG, queryTriplets);
                mapEGtoOccurs.put(currEG, 1);

                for (int i = 0; i < queryTriplets.size(); i += 3) {

                    outerDTP = myDedUtils.getCleanTP(new LinkedList<>(queryTriplets.subList(i, i + 3)));
                    for (int f = i + 3; f < queryTriplets.size(); f += 3) {

                        flagEG = true;
                        innerDTP = myDedUtils.getCleanTP(new LinkedList<>(queryTriplets.subList(f, f + 3)));
                        newEGpair = Arrays.asList(innerDTP, outerDTP);
                        newEGpair2 = Arrays.asList(outerDTP, innerDTP);
                        myDedUtils.setNewEGInfo(outerDTP, innerDTP, newEGpair, newEGpair2, indEG);
                    }

                }
            }

        }

        return flagEG;
    }
    
    
    /**
     * This is a particular function, for FedX traces. find commont elements of
     * two candidate triple patterns, in order to check if they can be merged to
     * one CTP
     *
     * @param outerList outer list to be compared
     * @param innerList inner list to be compared
     * @return number of common elements
     */
    public int candidateTPcomElems(List<String> outerList, List<String> innerList) {

        int comElems = 0;

        if (outerList.size() != innerList.size()) {

            return comElems;
        }

        for (int i = 0; i < outerList.size(); i++) {

            if (outerList.get(i).equalsIgnoreCase(innerList.get(i))) {

                comElems++;
            } else if (outerList.get(i).contains("?") && innerList.get(i).contains("?")) {

                comElems--;
            }

        }

        return comElems;
    }

   

    /**
     * Implement inverse mapping, by matching a CTP's constant values with
     * answers of previously evaluated mapping variables of DTPs
     *
     * @param indxCTP index of candidate triple pattern
     * @throws java.sql.SQLException
     */
    public void getMatchVarsOfCTP(int indxCTP) throws SQLException {

        int sourceGraph = 0;
        boolean flagSkipPattern = false;
        List<String> tpClean = null;
        List<String> allCTPVals = myDedUtils.getValuesFromCTP(indxCTP);
        List<String> tmpPattern = myDedUtils.getCleanTP(allCTPs.get(indxCTP));
        HashMap<String, List<String>> mapCandVarToMatchedVals = new HashMap<>();
        HashMap<String, List<String>> mapCandVarToAllAnsMaps = new HashMap<>();
        flagSTOPNONPROJECTEDvars = false;
        System.out.println("\t####### CTP No [" + (indxCTP + 1) + "] ####### ===> values to match: " + allCTPVals.size());
        System.out.print("\t\t" + DTPCandidates.get(indxCTP).get(0) + " " + DTPCandidates.get(indxCTP).get(1) + " " + DTPCandidates.get(indxCTP).get(2) + "\"");

        //If both subjects and objects are variables, skip matching
        if (DTPCandidates.get(indxCTP).get(0).contains("?") && DTPCandidates.get(indxCTP).get(2).contains("?")) {

            System.out.println("\t\t\tCandidate Pattern skipped, no values to match");
            myDedUtils.setDTPHashInfo(myDedUtils.getCleanTP(DTPCandidates.get(indxCTP)), indxCTP);
            flagSkipPattern = true;
        }

        //Else search for matching variables
        if (!flagSkipPattern) {

            for (int i = 0; i < allCTPVals.size(); i++) {

                tpClean = myDedUtils.getNewRawTP(tmpPattern, allCTPVals.get(i));
                mapDTPToFinishTime.put(tpClean, myBasUtils.getTimeInSec(mapCTPToFinishTime.get(indxCTP)));
                mapDTPToStartTime.put(tpClean, myBasUtils.getTimeInSec(mapCTPToStartTime.get(indxCTP)));
            }

            //find matching vars, and also exactly which CTP values are matched
            myDedUtils.searchMatchingVars(mapCandVarToAllAnsMaps, mapCandVarToMatchedVals, allCTPVals);

            // sort and remove rendundancy of lists
            for (String matchVar : mapCandVarToMatchedVals.keySet()) {

                myBasUtils.sortAndRemoveRedundancy(mapCandVarToMatchedVals.get(matchVar));
            }

            for (String matchVar : mapCandVarToAllAnsMaps.keySet()) {

                myBasUtils.sortAndRemoveRedundancy(mapCandVarToAllAnsMaps.get(matchVar));
            }

        }

        //Process matching variables, to see which will be finaly validated
        processMatchVars(mapCandVarToMatchedVals, mapCandVarToAllAnsMaps,
                flagSkipPattern, indxCTP, sourceGraph, allCTPVals);
    }

    /**
     * Process matching variables, to see which will be finaly validated.
     *
     * @param mapCandVarToMatchedVals map a candidate var to matching values to
     * a CTP
     * @param mapCandVarToAllAnsMaps map a candidate var to all its answers
     * @param flagSkipCTP boolean value to skip inverese mapping of a cuuretn tp
     * @param indxCTP index of current CTP
     * @param srcGraph deduced graph of queries, containing the CTP
     * @param allCTPvals all CTP values to be matched
     */
    public void processMatchVars(HashMap<String, List<String>> mapCandVarToMatchedVals,
            HashMap<String, List<String>> mapCandVarToAllAnsMaps,
            boolean flagSkipCTP, int indxCTP, int srcGraph, List<String> allCTPvals) {

        if (indxCTP == 18) {

            int araz = 0;
        }

        List<String> removeKeys = null;
        List<String> tmpPattern = null;
        List<String> newCandVals = new LinkedList<>();
        List<String> cleanCANDTP = myDedUtils.getCleanTP(DTPCandidates.get(indxCTP));

        //Confirm or reject variables based on matching threshold OR/AND best matching choice
        removeKeys = confirmMatchVars(mapCandVarToMatchedVals, mapCandVarToAllAnsMaps, indxCTP);

        for (int k = 0; k < removeKeys.size(); k++) {

            mapCandVarToMatchedVals.remove(removeKeys.get(k));
        }

        //Identify EG as NLEG and combine alternative variables and match variables to corresponding hash maps
        refineDTPmatchVars(mapCandVarToMatchedVals, mapCandVarToAllAnsMaps, indxCTP, srcGraph);

        //Match a CTP directly to a DTP, when no vars matched during inverse mapping
        if (mapCandVarToMatchedVals.isEmpty()) {

            //If values to match correspond to CTP part of a BoundJoin, that we
            //could not match because of a small Tjoin
            if (allCTPvals == null || (allCTPvals != null && allCTPvals.size() > 3)) {

                tmpPattern = myDedUtils.getCleanTP(allCTPs.get(indxCTP));
                if (mapCTPtoQuerySrc.get(tmpPattern) != null) {

                    if (mapDTPToDeducedID.get(tmpPattern) == null) {

                        mapDTPToDeducedID.put(tmpPattern, mapDTPToDeducedID.size() + 1);
                    }

                    // System.out.println(mapDTPToDeducedID.get(newDedTP));
                    myBasUtils.insertToMap2(mapDTPtoInnerQuery, mapCTPtoQuerySrc.get(tmpPattern), mapDTPToDeducedID.get(tmpPattern));
                }

                //If CTP's subject and object are variables, we have already match
                //this as a DTP
                if (!flagSkipCTP) {

                    myDedUtils.setDTPHashInfo(tmpPattern, indxCTP);
                    allCTPvals = myDedUtils.getValuesFromCTP(indxCTP);
                    flagSTOPNONPROJECTEDvars = myDedUtils.setDTPtoSrcAns(indxCTP, allCTPvals, tmpPattern, flagSTOPNONPROJECTEDvars);
                }

            } //If values to match correspond to singleTPs that surely are not part
            //of a BoundJoin, then match them directly as DTPs
            else if (allCTPvals.size() <= 3) {

                for (int i = 0; i < allCTPvals.size(); i++) {

                    tmpPattern = myDedUtils.getNewRawTP(allCTPs.get(indxCTP), allCTPvals.get(i));

                    if (mapCTPtoQuerySrc.get(tmpPattern) != null) {

                        if (mapDTPToDeducedID.get(tmpPattern) == null) {

                            mapDTPToDeducedID.put(tmpPattern, mapDTPToDeducedID.size() + 1);
                        }

                        // System.out.println(mapDTPToDeducedID.get(newDedTP));
                        myBasUtils.insertToMap2(mapDTPtoInnerQuery, mapCTPtoQuerySrc.get(tmpPattern), mapDTPToDeducedID.get(tmpPattern));
                    }

                    if (!flagSkipCTP) {

                        myDedUtils.setDTPHashInfo(tmpPattern, indxCTP);
                        allCTPvals = myDedUtils.getValuesFromCTP(indxCTP);
                        newCandVals = new LinkedList<>();
                        newCandVals.add(allCTPvals.get(i));
                        flagSTOPNONPROJECTEDvars = myDedUtils.setDTPtoSrcAns(indxCTP, allCTPvals, tmpPattern, flagSTOPNONPROJECTEDvars);
                    }
                }
            }

        }

        //In any case, we must capture source endpoints of each source TP of a CTP
        //and if there is no matching variable, save each source TPs as a DTP
        for (int i = 0; i < allCTPvals.size(); i++) {

            tmpPattern = myDedUtils.getNewRawTP(allCTPs.get(indxCTP), allCTPvals.get(i));
            myBasUtils.insertToMap3(mapDTPToEndpsSrc, mapCTPToEndpsSrc.get(cleanCANDTP), tmpPattern);
        }

    }

    /**
     * Confirm candidate variables or cancel them, based on matching threshold
     * "inverseThresh" and if it is enabled the best matching var (i.e.,
     * inverseBestMap)
     *
     * @param mapCandVarToMatchedVals map a candidate var to matching values to
     * a CTP
     * @param mapCandVarToAllAnsMaps map a candidate var to all its answers
     * @param indxCTP index of current CTP
     * @return all confirmed matching variables
     */
    public List<String> confirmMatchVars(HashMap<String, List<String>> mapCandVarToMatchedVals,
            HashMap<String, List<String>> mapCandVarToAllAnsMaps, int indxCTP) {

        System.out.println("\t\t\t\t\t ================ CONFIRM or REJECT matched vars ================");
        List<String> valuesCandVar = null;
        List valuesCandVarAns = null;
        List<String> removeKeys = new LinkedList<>();
        double maxMatchValue = 0;

        for (String keyOuter : mapCandVarToMatchedVals.keySet()) {

            valuesCandVarAns = mapCandVarToAllAnsMaps.get(keyOuter);
            valuesCandVar = mapCandVarToMatchedVals.get(keyOuter);
            double percentageMatchec = (double) valuesCandVar.size() / valuesCandVarAns.size();

            if (maxMatchValue < percentageMatchec) {

                maxMatchValue = percentageMatchec;
            }

            if (valuesCandVarAns != null && valuesCandVar != null) {

                //BUUUUUUUUUUUUUUUUUG
                if ((percentageMatchec < inverseThresh && !(Double.toString(percentageMatchec).contains("E")))
                        || (DTPCandidates.get(indxCTP).get(1).contains("?") && percentageMatchec < 0.10)
                        || (valuesCandVar.size() == 1 && percentageMatchec < 0.04)) {

                    if (!myBasUtils.elemInListEquals(removeKeys, keyOuter)) {

                        System.out.println("\t\t\t\t\t\t CANCEL var: ?" + keyOuter.substring(0, keyOuter.indexOf("_")) + ", while actually mathced: " + (percentageMatchec * 100) + " %");
                        removeKeys.add(keyOuter);
                    }
                } else {
                    if (!inverseBestMap) {
                        System.out.println("\t\t\t\t\t\t VALIDATE candidate var: ?" + keyOuter.substring(0, keyOuter.indexOf("_")) + ", while actually mathced: " + (percentageMatchec * 100) + " %");

                    }
                }

            } //BUUUUUUUUUUUUUUUUUG
            else if (valuesCandVar == null) {
                removeKeys.add(keyOuter);
            } else {
                if (!inverseBestMap) {
                    System.out.println("\t\t\t\t\t\t VALIDATE var: ?" + keyOuter.substring(0, keyOuter.indexOf("_")) + ", while actually mathced: " + (percentageMatchec * 100) + " %");

                }
            }

        }

        if (inverseBestMap) {

            for (String keyOuter : mapCandVarToMatchedVals.keySet()) {

                valuesCandVarAns = mapCandVarToAllAnsMaps.get(keyOuter);
                valuesCandVar = mapCandVarToMatchedVals.get(keyOuter);
                double percentageMatchec = (double) valuesCandVar.size() / valuesCandVarAns.size();

                if (valuesCandVarAns != null && valuesCandVar != null) {

                    if (maxMatchValue > percentageMatchec) {

                        if (!myBasUtils.elemInListEquals(removeKeys, keyOuter)) {

                            System.out.println("\t\t\t\t\t\t CANCEL as not best matching var: ?" + keyOuter.substring(0, keyOuter.indexOf("_")) + ", while actually mathced: " + (percentageMatchec * 100) + " %");
                            removeKeys.add(keyOuter);
                        }
                    } else {

                        System.out.println("\t\t\t\t\t\t VALIDATE candidate var: ?" + keyOuter.substring(0, keyOuter.indexOf("_")) + ", while actually mathced: " + (percentageMatchec * 100) + " %");
                    }

                } //BUUUUUUUUUUUUUUUUUG
                else if (valuesCandVar == null) {
                    if (!myBasUtils.elemInListEquals(removeKeys, keyOuter)) {
                        removeKeys.add(keyOuter);
                    }

                } else {

                    System.out.println("\t\t\t\t\t\t VALIDATE var: ?" + keyOuter.substring(0, keyOuter.indexOf("_")) + ", while actually mathced: " + (percentageMatchec * 100) + " %");
                }

            }
        }

        System.out.println("\t\t\t\t\t ================ CONFIRM or REJECT matched vars ================");

        return removeKeys;
    }

    /**
     * For all confirmed matched vars, identify EG as NLEG, combine alternative
     * DTPs and identify not matched values as distinct DTPs.
     *
     * @param mapCandVarToMatchedVals map a candidate var to matching values to
     * a CTP
     * @param mapCandVarToAllAnsMaps map a candidate var to all its answers
     * @param indxCTP index of current CTP
     * @param srcGraph deduced graph of queries, containing the CTP
     */
    public void refineDTPmatchVars(HashMap<String, List<String>> mapCandVarToMatchedVals,
            HashMap<String, List<String>> mapCandVarToAllAnsMaps, int indxCTP, int srcGraph) {

        List<String> valueskeyOuter = null;
        List<String> valuesCandAnswMapOuter = null;
        List<String> newDedTP = null;
        List<String> cloneTP = null;
        List<String> originalTP = myDedUtils.getCleanTP(DTPCandidates.get(indxCTP));
        List<String> allCTPVals = myDedUtils.getValuesFromCTP(indxCTP);
        HashMap<List<String>, Integer> mapMatchedConstant = new HashMap<>();
        String originalVar = myDedUtils.getOriginalVar(DTPCandidates.get(indxCTP));

        //Init each CTP value as "non-mathced"
        for (int i = 0; i < allCTPVals.size(); i++) {

            List<String> srcTPofCTP = myDedUtils.getNewRawTP(DTPCandidates.get(indxCTP), allCTPVals.get(i));
            mapMatchedConstant.put(srcTPofCTP, -1);
        }

        //For all confirmed variables, 
        for (String keyOuter : mapCandVarToMatchedVals.keySet()) {

            String cleanVariable = "?" + keyOuter.substring(0, keyOuter.indexOf("_"));
            newDedTP = myDedUtils.getNewRawTP(DTPCandidates.get(indxCTP), cleanVariable);
            valueskeyOuter = mapCandVarToMatchedVals.get(keyOuter);
            valuesCandAnswMapOuter = mapCandVarToAllAnsMaps.get(keyOuter);
            List<String> tmpTP = myDedUtils.getCleanTP(newDedTP);

            //match each source TP to its corresponding DTPs, matching vars and marked as "matched"
            for (int i = 0; i < valuesCandAnswMapOuter.size(); i++) {

                List<String> rawTP = myDedUtils.getNewRawTP(originalTP, valuesCandAnswMapOuter.get(i));
                setEGasNLEG(rawTP, newDedTP);

                if (mapMatchedConstant.get(rawTP) != null) {
                    mapMatchedConstant.put(rawTP, 1);
                }
            }

            //Finally, match a confirmed DTP to source values and answers
            for (int i = 0; i < valueskeyOuter.size(); i++) {

                List<String> rawTP = myDedUtils.getNewRawTP(originalTP, valueskeyOuter.get(i));

                if (mapMatchedConstant.get(rawTP) != null) {
                    mapMatchedConstant.put(rawTP, 1);
                }

                if (newDedTP.size() == 3) {

                    cloneTP = new LinkedList<>(newDedTP.subList(0, newDedTP.size()));
                    cloneTP.add(originalVar);

                    if (mapCTPtoAnswTotal.get(rawTP) != null) {

                        myBasUtils.insertToMap3(mapCTPtoAnswTotal, mapCTPtoAnswTotal.get(rawTP), cloneTP);
                    }
                }

            }

            myDedUtils.setDTPHashInfo(newDedTP, indxCTP);

            if (mapCTPtoQuerySrc.get(DTPCandidates.get(indxCTP)) != null) {
                // System.out.println(mapDTPToDeducedID.get(newDedTP));
                myBasUtils.insertToMap2(mapDTPtoInnerQuery, mapCTPtoQuerySrc.get(DTPCandidates.get(indxCTP)), mapDTPToDeducedID.get(newDedTP));
            }

            tmpTP.add(cleanVariable);
            myBasUtils.insertToMap4(mapDTPtoAnsInverseMap, tmpTP, valuesCandAnswMapOuter);
            myBasUtils.insertToMap4(mapDTPtoAnswTotal, tmpTP, valuesCandAnswMapOuter);
            flagSTOPNONPROJECTEDvars = myDedUtils.setDTPtoSrcAns(indxCTP, valueskeyOuter, newDedTP, flagSTOPNONPROJECTEDvars);
        }

        //For confirmed vars, identify all alternative DTPs
        //which do not count as deduced joins
        myDedUtils.setDTPtoAlternatives(mapCandVarToMatchedVals, indxCTP);

        //For CTP values that are not matched them finally to a variable
        for (List<String> raw : mapMatchedConstant.keySet()) {

            int val = mapMatchedConstant.get(raw);

            if (val == -1) {

                for (String keyOuter : mapCandVarToMatchedVals.keySet()) {

                    String cleanVariable = "?" + keyOuter.substring(0, keyOuter.indexOf("_"));
                    newDedTP = myDedUtils.getNewRawTP(DTPCandidates.get(indxCTP), cleanVariable);
                    mapMatchedConstant.put(raw, 1);
                }

            }
        }

    }

    /**
     * Particular function for FedX traces. Cancel a join identified as an EG
     * and convert it into a Nested Loop with EG.
     *
     * @param rawTP source triple pattern, part of the current NLEG
     * implementation
     * @param newDedTP deduced triple pattern for the current NLEG
     * implementation
     */
    public void setEGasNLEG(List<String> rawTP, List<String> newDedTP) {

        List<String> currEGouterTP = null;
        List<String> currEGinnerTP = null;
        List<List<String>> pairEGtoNested = new LinkedList<>();

        for (int keyEG : mapTmpEGtoAllTPs.keySet()) {

            List<String> currEG = mapTmpEGtoAllTPs.get(keyEG);
            if ((mapEGtoOccurs.get(currEG) != null && mapEGtoOccurs.get(currEG) > 1)
                    || (mapEGtoOccurs.get(currEG) != null && mapEGtoOccurs.get(currEG) == 1)) {

                for (int k = 0; k < currEG.size(); k += 3) {

                    currEGouterTP = new LinkedList<>(currEG.subList(k, k + 3));
                    if (currEGouterTP.equals(rawTP) && !currEGouterTP.contains("ontology")) {

                        //Identify a EG as a nested loop with EG
                        for (int l = k + 3; l < currEG.size(); l += 3) {

                            currEGinnerTP = new LinkedList<>(currEG.subList(l, l + 3));
                            pairEGtoNested = Arrays.asList(newDedTP, currEGinnerTP);
                            myDedUtils.pairJoinRelation(newDedTP, currEGinnerTP, pairEGtoNested, 1, "nestedLoop", true);
                        }

                    }

                }
            }

        }

    }

    /**
     * Particular function for ANAPPSID traces. Captute filter option values, as
     * possible inverse mapping values of a NLFO
     */
    public void checkNLFOJoin() {

        String extractedVar = "";
        String currQuery = "";
        List<Integer> sourceQueries = null;
        List<String> tmpTP = null;
        List<String> extractedVals = null;

        for (List<String> key : mapCTPtoQuerySrc.keySet()) {

            sourceQueries = mapCTPtoQuerySrc.get(key);

            for (int i = 0; i < sourceQueries.size(); i++) {

                currQuery = queries.get(sourceQueries.get(i));

                //Buuuuuuuuuuuuuug
                if (currQuery.contains("mass")) {
                    continue;
                }

                if (currQuery.contains("filter") || currQuery.contains("FILTER")) {

                    extractedVals = myBasUtils.getFILTERvals(currQuery);
                    extractedVar = myBasUtils.getFILTERvar(currQuery);

                    if (extractedVals.size() >= 1) {

                        if (key.get(0).equalsIgnoreCase(extractedVar) || key.get(2).equalsIgnoreCase(extractedVar)) {

                            tmpTP = myDedUtils.getCleanTP(key);
                            tmpTP.add(extractedVar);
                            myBasUtils.insertToMap4(mapDTPtoAnsInverseMap, tmpTP, extractedVals);
                        }
                    }

                }

            }
        }

    }

    /**
     * Particular function for FedX traces. Cancle joins in which a DTP
     * participated, while evaluated with NLEG and falsely believed as an
     * exclusive group.
     */
    public void cancelJoinsNLEGJoin() {

        List<String> currEG = null;
        List<String> currOuterTP = null;

        for (int keyOuter : mapTmpEGtoAllTPs.keySet()) {

            if (mapEGtoCancel.get(keyOuter) != null) {

                currEG = mapTmpEGtoAllTPs.get(keyOuter);

                for (int i = 0; i < currEG.size(); i += 3) {

                    currOuterTP = new LinkedList<>(currEG.subList(i, i + 3));
                    //BUUUUUUUUUUUUG check if it is a ontology
                    if (!currOuterTP.get(0).contains("?") && !currOuterTP.get(0).contains("#")
                            || !currOuterTP.get(2).contains("?") && !currOuterTP.get(2).contains("#")) {

                        myDedUtils.cancelDTP(currOuterTP);
                        for (int j = i + 3; j < currEG.size(); j += 3) {

                            totalPairs--;
                        }
                    }

                }
            }

        }

    }

    /**
     * Particular function for FedX traces. Identify a nested loop with filter
     * option at the same time with a NLBJ. The filter variable is a particular
     * variable for FedX, namely "?o".
     *
     * @throws SQLException
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    public void checkNLBJwithFilter() throws SQLException, InstantiationException, IllegalAccessException {

        //maps a candidate var to CTP matched values
        HashMap<String, List<String>> mapCandVarToMatchedVals = new HashMap<>();
        //maps a candidate var to all answer values
        HashMap<String, List<String>> mapCandVarToAllAnsMaps = new HashMap<>();
        List<String> allFilterVals = null;
        HashMap<List<String>, List<String>> newDTPtoALtenativeTotalAns = new HashMap<>();
        HashMap<List<String>, List<String>> newDTPtoALtenativeInverseMap = new HashMap<>();
        List<List<String>> allNewDTPShort = null;

        for (List<String> keyFilterVar : mapCTPtoFILTERwithBoundJ.keySet()) {

            allFilterVals = mapCTPtoFILTERwithBoundJ.get(keyFilterVar);

            //If there is only one FILTER value of BOUND JOIN, skip matching
            if (allFilterVals.size() == 1) {

                continue;
            }

            myDedUtils.searchMatchingVars(mapCandVarToAllAnsMaps, mapCandVarToMatchedVals, allFilterVals);
            allNewDTPShort = new LinkedList<>();
            List<String> matchingValues = new LinkedList<>();

            for (String matchVar : mapCandVarToMatchedVals.keySet()) {

                // Identify particular "?o_#value" variable indicating a NLFO
                if (!matchVar.contains("o_") && mapCandVarToMatchedVals.get(matchVar).size() > 0) {

                    for (List<String> keyDTP : mapDTPtoAnswTotal.keySet()) {

                        if (keyDTP.get(1).equalsIgnoreCase(keyFilterVar.get(1))) {

                            //Case "?o_#value" is in the subject of the triple pattern
                            if (keyDTP.get(0).equalsIgnoreCase(keyFilterVar.get(0)) && keyDTP.get(0).equalsIgnoreCase("?o") && !keyDTP.get(3).equalsIgnoreCase("?o")) {

                                List<String> newDTP = Arrays.asList("?" + matchVar.substring(0, matchVar.indexOf("_")), keyDTP.get(1), keyDTP.get(2));
                                myDedUtils.setDTPbasicInfo(newDTP);
                                allNewDTPShort.add(newDTP);
                                newDTP = Arrays.asList("?" + matchVar.substring(0, matchVar.indexOf("_")), keyDTP.get(1), keyDTP.get(2), keyDTP.get(3));
                                matchingValues.addAll(mapCandVarToMatchedVals.get(matchVar));
                                newDTPtoALtenativeTotalAns.put(newDTP, keyDTP);

                            } //Case "?o_#value" is in the object of the triple pattern
                            else if (keyDTP.get(2).equalsIgnoreCase(keyFilterVar.get(2)) && keyDTP.get(2).equalsIgnoreCase("?o") && !keyDTP.get(3).equalsIgnoreCase("?o")) {

                                List<String> newDTP = Arrays.asList(keyDTP.get(0), keyDTP.get(1), "?" + matchVar.substring(0, matchVar.indexOf("_")));
                                myDedUtils.setDTPbasicInfo(newDTP);
                                allNewDTPShort.add(newDTP);
                                newDTP = Arrays.asList(keyDTP.get(0), keyDTP.get(1), "?" + matchVar.substring(0, matchVar.indexOf("_")), keyDTP.get(3));
                                newDTPtoALtenativeTotalAns.put(newDTP, keyDTP);
                                matchingValues.addAll(mapCandVarToMatchedVals.get(matchVar));
                            }
                        }
                    }

                }

                // Search all nestedl loop values of the new DTP, passed as FILTER options 
                // to the inner queries implementing the NLFO
                for (List<String> keyDTP : mapDTPtoAnsInverseMap.keySet()) {

                    for (List<String> newDTPfilter : allNewDTPShort) {

                        if ((keyDTP.get(0).equalsIgnoreCase("?o") && (newDTPfilter.get(2).equalsIgnoreCase(keyDTP.get(2)) && newDTPfilter.get(1).equalsIgnoreCase(keyDTP.get(1)))) || (keyDTP.get(2).equalsIgnoreCase("?o") && (newDTPfilter.get(0).equalsIgnoreCase(keyDTP.get(0)) && newDTPfilter.get(1).equalsIgnoreCase(keyDTP.get(1))))) {

                            List<String> newDTP = new LinkedList<>(newDTPfilter.subList(0, 3));

                            if (!myBasUtils.elemInListContained(newDTP, "?" + matchVar.substring(0, matchVar.indexOf("_")))) {

                                continue;
                            }

                            myDedUtils.setDTPbasicInfo(newDTP);
                            newDTP.add("?" + matchVar.substring(0, matchVar.indexOf("_")));
                            newDTPtoALtenativeInverseMap.put(newDTP, keyDTP);
                        }
                    }

                }

            }

            // Fetch answer values from th NLBJ to NLFO deduced triple pattern
            for (List<String> keyTotalNEW : newDTPtoALtenativeTotalAns.keySet()) {

                myDedUtils.setDTPinfoToAnother(keyTotalNEW, newDTPtoALtenativeTotalAns, mapDTPtoAnswTotal.get(newDTPtoALtenativeTotalAns.get(keyTotalNEW)));

            }

            for (List<String> keyTotalInvMaps : newDTPtoALtenativeInverseMap.keySet()) {

                myDedUtils.setDTPinfoToAnother(keyTotalInvMaps, newDTPtoALtenativeInverseMap, matchingValues);
            }

        }

    }

}