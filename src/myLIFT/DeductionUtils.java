package myLIFT;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import static myLIFT.Deduction.cntCONSTJOINTotal;
import static myLIFT.Deduction.cntCONSTJOINTrPo;
import static myLIFT.Deduction.cntEGTotal;
import static myLIFT.Deduction.cntEGTrPo;
import static myLIFT.Deduction.cntNESLOOPTotal;
import static myLIFT.Deduction.cntNESLOOPTrPo;
import static myLIFT.Deduction.cntSYMHASHTotal;
import static myLIFT.Deduction.cntSYMHASHTrPo;
import static myLIFT.Deduction.mapAnsEntryToAllSignatures;
import static myLIFT.Deduction.mapAnsEntryToListValues;
import static myLIFT.Deduction.mapAnsIDToLogClQuery;
import static myLIFT.Deduction.mapAnsIDtoEntry;
import static myLIFT.Deduction.mapAnsSingatureToAllValues;
import static myLIFT.Deduction.mapDTPtoCANCELofEG;
import static myLIFT.Deduction.mapGroundTruthPairs;
import static myLIFT.Deduction.mapLogClQueryToAnsEntry;
import static myLIFT.Deduction.mapLogClQueryToTimestamp;
import static myLIFT.Deduction.mapTruePositivePairs;
import static myLIFT.Deduction.totalPairs;
import static myLIFT.Deduction.truePositivesPairs;
import static myLIFT.LoadFromDB.mapCTPtoFILTERwithBoundJ;
import static myLIFT.NestedLoopDetection.allCTPs;
import static myLIFT.NestedLoopDetection.mapCTPToEndpsSrc;
import static myLIFT.NestedLoopDetection.mapCTPToFinishTime;
import static myLIFT.NestedLoopDetection.mapCTPToStartTime;
import static myLIFT.NestedLoopDetection.mapCTPtoConstants;
import static myLIFT.NestedLoopDetection.mapCTPtoDedGraph;
import static myLIFT.NestedLoopDetection.mapCTPtoQuerySrc;
import static myLIFT.NestedLoopDetection.mapDTPToAnyJoin;
import static myLIFT.NestedLoopDetection.mapDTPToDeducedID;
import static myLIFT.NestedLoopDetection.mapDTPtoExclGroup;
import static myLIFT.NestedLoopDetection.mapDTPtoInnerQuery;
import static myLIFT.NestedLoopDetection.mapDTPtoJoinBGP;
import static myLIFT.NestedLoopDetection.mapBGPtoConfidence;
import static myLIFT.NestedLoopDetection.mapCTPtoAnswTotal;
import static myLIFT.NestedLoopDetection.mapDTPToEndpsSrc;
import static myLIFT.NestedLoopDetection.mapDTPToFinishTime;
import static myLIFT.NestedLoopDetection.mapDTPToStartTime;
import static myLIFT.NestedLoopDetection.mapDTPofEGNested;
import static myLIFT.NestedLoopDetection.mapDTPtoAlternatives;
import static myLIFT.NestedLoopDetection.mapDTPtoAnswTotal;
import static myLIFT.NestedLoopDetection.mapPairExclGroup;
import static myLIFT.NestedLoopDetection.mapPairToRelation;
import static myLIFT.NestedLoopDetection.notnullJoinBGPs;
import static myLIFT.Main.inverseMapping;
import static myLIFT.Main.setMonetDB;
import static myLIFT.Main.windowJoin;

/**
 * This class implementes help/complementary functions for deduction algorithm
 *
 * @author Nassopoulos Georges
 * @version 1.0
 * @since 2016-03-19
 */
public class DeductionUtils {

    BasicUtilis myBasUtils;
    MonetDBManag myMonet;

    public static List<List<String>> DTPCandidates;
    public static List<List<List<String>>> setDTPbasedAnswers;
    public static List<Integer> listBGPsremoveAnswer;

    public DeductionUtils() {
   
        myBasUtils = new BasicUtilis();
        myMonet = new MonetDBManag();    
        
        DTPCandidates = new LinkedList<>();
        setDTPbasedAnswers = new LinkedList<>();
        listBGPsremoveAnswer = new LinkedList<>();
    }

    /**
     * Get triple pattern's variables
     *
     * @param triplePat triple pattern passed in input
     * @return all input triple pattern's variables
     */
    public List<String> getTPVariables(List<String> triplePat) {

        List<String> vars = new LinkedList<>();

        for (int i = 0; i < triplePat.size(); i++) {

            if (triplePat.get(i).contains("?")) {

                vars.add(triplePat.get(i));
            }
        }

        return vars;
    }

    /**
     * Get a new triple pattern in a specific format. This function is used, as
     * constants in answers are not captured in adequate format (i.e., a Literal
     * is not included in double quotes and an IRI is not included in "<" and ">").
     *
     * @param rawTP original tp, not respecting the adequate format
     * @return clean triple pattern in a cleaned format
     */
    public List<String> getCleanTP(List<String> rawTP) {

        List<String> triplePat = new LinkedList<>();
        String tmpLiteral = "";

        for (int i = 0; i < 3; i++) {

            if (i == 1) {

                triplePat.add(rawTP.get(i));
            } else {

                if (rawTP.get(i).contains("\"") && !rawTP.get(i).contains("http") 
                        && !rawTP.get(i).contains("?")) {

                    tmpLiteral = rawTP.get(i).substring(1, rawTP.get(i).length() - 1);
                    triplePat.add("\'" + tmpLiteral + "\'");

                } else if (rawTP.get(i).contains("http") && !rawTP.get(i).contains("<")
                        && !rawTP.get(i).contains(">")) {
                    triplePat.add("<" + rawTP.get(i) + ">");
                } else {
                    triplePat.add(rawTP.get(i));
                }

            }
        }

        return triplePat;
    }

    /**
     * Get a triple pattern, where its subject or object has a specific constant
     * value. This function is used in order to find to which original triple 
     * patterns in the log, a Candidate Triple Pattern (CTP) correspond.
     *
     * @param candidateTP CTP to which different original triple patterns corespond
     * @param value new IRI/Literal to set as the triple pattern constant value
     * @return the original triple pattern
     */
    public List<String> getNewRawTP(List<String> candidateTP, String value) {

        List<String> newTP = new LinkedList<>();

        for (int i = 0; i < 3; i++) {

            if ((i == 0 || i == 2) && (!candidateTP.get(i).contains("?"))) {

                if (value.contains("?")) {

                    newTP.add(value);
                } else if (!value.contains("http") && !value.contains("?")) {

                    if (!value.contains("\'")) {

                        newTP.add("\'" + value + "\'");
                    } else {
                        newTP.add(value);
                    }

                } else if (value.contains("http") && !value.contains("?")) {
                    if (!value.contains("<") && !value.contains(">")) {

                        newTP.add("<" + value + ">");
                    } else {
                        newTP.add(value);
                    }
                }
            } else {
                newTP.add(candidateTP.get(i));
            }
        }

        return newTP;
    }

    /**
     * Get all constants (i.e., IRIs/Literals) of a CTP, if there exist in their
     * object or subjects, that will be used during inverse mapping.
     *
     * @param inxCTP index of candidate TP
     * @return list of constant values to be matched during inverse mapping
     */
    public List<String> getValuesFromCTP(int inxCTP) {

        List<String> allCandidateTPVals = new LinkedList<>();
        List<String> currCTP = new LinkedList<>();

        for (int i = 0; i < 4; i++) {

            currCTP.add(DTPCandidates.get(inxCTP).get(i));
        }

        allCandidateTPVals = mapCTPtoConstants.get(currCTP);

        return allCandidateTPVals;
    }

    /**
     * Get final candidate variable, to be identified for a deduced triple pattern,
     * in a clean format.
     *
     * @param currCandVar input variable to be cleaned
     * @return cleaned variable
     */
    public String getCleanFinalVar(String currCandVar) {

        String mapVariable = "";

        //add "?" in the variable
        if (!currCandVar.contains("?")) {

            mapVariable = "?" + currCandVar;
        } else {

            mapVariable = currCandVar;
        }

        //remove "_" part of bound type queries
        if (mapVariable.contains("_")) {

            mapVariable = mapVariable.substring(0, mapVariable.indexOf("_"));
        }

        return mapVariable;
    }

    /**
     * Get the constant value of a triple pattern, if there exist one, on its 
     * subject or object.
     *
     * @param triplePat input triple pattern
     * @return constant value to be returned
     */
    public String getConstantVal(List<String> triplePat) {

        String constVal = "";

        if (!triplePat.get(0).contains("?")) {

            constVal = triplePat.get(0);
        } else if (!triplePat.get(2).contains("?")) {

            constVal = triplePat.get(2);
        }

        return constVal;
    }

    /**
     * Get index of constant value of a triple pattern, if there exist one, on its
     * subject or object.
     *
     * @param triplePat input triple pattern
     * @return index of constant value to be returned
     */
    public int getIndxConstant(List<String> triplePat) {

        int inxConst = -1;

        if (triplePat.get(0).contains("?") && (triplePat.get(2).contains("<")
                || triplePat.get(2).contains("\"") || triplePat.get(2).contains("'"))) {

            inxConst = 2;
        }
        if (triplePat.get(2).contains("?") && (triplePat.get(0).contains("<")
                || triplePat.get(0).contains("\"") || triplePat.get(0).contains("'"))) {

            inxConst = 0;
        }

        return inxConst;
    }

    /**
     * Get original variable if a CTP, when there is only one, only on its subject
     * or object.
     *
     * @param triplePat input triple pattern
     * @return index of original variable to be returned
     */
    public String getOriginalVar(List<String> triplePat) {

        String getOriginalVar = "";

        if (triplePat.get(0).contains("?") && !triplePat.contains("?")) {
            getOriginalVar = triplePat.get(0);
        }

        if (!triplePat.get(0).contains("?") && triplePat.get(2).contains("?")) {
            getOriginalVar = triplePat.get(2);
        }

        return getOriginalVar;
    }

    /**
     * Get list of exact matching CTPs (i.e., subject, predicate, object). If it
     * is equal to "null", then it is the first time we identify this new candidate 
     * pattern. This method is also used when we have to distinguish identical CTPs 
     * (i.e., same subject, predicate, object) when these are identified in relative 
     * distant timestamps not covered by the employed gap (i.e., Tjoin)
     *
     * @param myList list of candidate triple patterns
     * @param subject tp's subject
     * @param predicate tp's predicate
     * @param object tp's object
     * @return list of indexes of all matching CTPs
     */
    public List<Integer> getIdemCTPs(List<List<String>> myList, String subject, String predicate, String object) {

        List<Integer> idPatrns = new LinkedList<>();
        int idPat = -1;

        for (List<String> listStr : myList) {

            idPat++;

            for (int i = 0; i < 3; i++) {

                if (listStr.get(i).contains(subject) && listStr.get(i + 1).equalsIgnoreCase(predicate) 
                        && listStr.get(i + 2).contains(object)) {

                    idPatrns.add(idPat);
                }

            }

        }

        return idPatrns;
    }

    /**
     * Get list of derived matching CTP (i.e., same subject and predicate or same
     * predicate and subject. Idem, it is also used to identify CTPs captured in 
     * relative distant timestamps not covered by the employed gap Tjoin (i.e. Gap)
     * This function is necessairy to identify a NLBJ, made by FedX.
     *
     * @param myList list of candidate triple patterns
     * @param element1 first tp's entity
     * @param idELement1 first tp's position (subject, predicate or object)
     * @param element2 second tp's entity
     * @param idELement2 second tp's position (subject, predicate or object)
     * @return list of indexes of all matching CTPs
     */
    public List<Integer> getDerivedCTPs(List<List<String>> myList, String element1,
            Integer idELement1, String element2, Integer idELement2) {

        int cpt = 0;
        int idPat = -1;
        boolean flagElement1 = false, flagElement2 = false;
        List<Integer> idPatrns = new LinkedList<>();

        for (List<String> listStr : myList) {

            idPat++;
            cpt = 0;

            if (!(listStr.get(0).contains("?") && listStr.get(2).contains("?"))) {
                for (int i = 0; i < 3; i++) {
                    if (listStr.get(i).equalsIgnoreCase(element1) && !flagElement1 
                            && (idELement1 == i)) {

                        cpt++;
                    }
                    if (listStr.get(i).equalsIgnoreCase(element2) && !flagElement2 && 
                            (idELement2 == i)) {

                        cpt++;
                    }

                    // we do not concider a candidate TP with two variables, as match
                    if (cpt == 2) {

                        if (!myBasUtils.elemInListEquals(idPatrns, idPat)) {

                            idPatrns.add(idPat);
                        }

                    }

                }
            }
        }

        return idPatrns;
    }
    
    
    /**
     * Set basic info, about the existance of a new Deduced Triple pattern
     *
     * @param currTP the new deduced triple pattern
     */
    public void setDTPbasicInfo(List<String> currTP) {

        if (mapDTPToDeducedID.get(currTP) == null) {
            mapDTPToDeducedID.put(currTP, mapDTPToDeducedID.size() + 1);

        }

        if (mapDTPToAnyJoin.get(mapDTPToDeducedID.get(currTP)) == null) {
            mapDTPToAnyJoin.put(mapDTPToDeducedID.get(currTP), -1);

        }
    }

    /**
     * Set new info for a new exclusive group
     *
     * @param outerDTP
     * @param innerDTP
     * @param newEGpair
     * @param newEGpair2
     * @param curEGpair
     */
    public void setNewEGInfo(List<String> outerDTP, List<String> innerDTP, List<List<String>> newEGpair, List<List<String>> newEGpair2, int curEGpair) {

        int currDTPsize = mapDTPToDeducedID.size();

        if (mapDTPToDeducedID.get(outerDTP) == null) {

            mapDTPToDeducedID.put(outerDTP, currDTPsize);
            mapDTPToAnyJoin.put(currDTPsize, 1);
        }

        currDTPsize = mapDTPToDeducedID.size();
        if (mapDTPToDeducedID.get(innerDTP) == null) {

            mapDTPToDeducedID.put(innerDTP, currDTPsize);
            mapDTPToAnyJoin.put(currDTPsize, 1);
        }

        if (myBasUtils.getListsIntersec(getTPVariables(innerDTP), getTPVariables(outerDTP)).size() > 0) {

            mapDTPtoExclGroup.put(outerDTP, curEGpair + 1);
            mapDTPtoExclGroup.put(innerDTP, curEGpair + 1);

            int EGsize = mapPairExclGroup.size();

            if (mapPairExclGroup.get(newEGpair) == null && mapPairExclGroup.get(newEGpair2) == null) {

                mapPairExclGroup.put(newEGpair, EGsize);
                mapPairExclGroup.put(newEGpair2, EGsize);
                totalPairs++;
                cntEGTotal++;
            }

            if (mapGroundTruthPairs.get(newEGpair) != null && mapTruePositivePairs.get(newEGpair) == null) {

                mapTruePositivePairs.put(newEGpair, 1);
                mapTruePositivePairs.put(newEGpair2, 1);
                cntEGTrPo++;
                truePositivesPairs++;
            }
            
            System.out.println("\t-------------------------------EXCLUSIVE GROUP [no " + curEGpair + "]----------------------------------------------");
            System.out.println("\t\t[Outer TP] " + outerDTP.get(0) + " " + outerDTP.get(1) + " " + outerDTP.get(2));
            System.out.println("\t\t[Inner TP] " + innerDTP.get(0) + " " + innerDTP.get(1) + " " + innerDTP.get(2));
            System.out.println();
            mapPairToRelation.put(newEGpair, "exclusiveGroup");
            setPairJoinToBGP(newEGpair, (float) 1.0);
        }

        outerDTP = getCleanTP(outerDTP);
        innerDTP = getCleanTP(innerDTP);
        myBasUtils.insertToMap4(mapDTPtoAnswTotal, outerDTP, new LinkedList<String>());
        myBasUtils.insertToMap4(mapDTPtoAnswTotal, innerDTP, new LinkedList<String>());
    }

    /**
     * Set hash map info about a new candidate triple pattern CTP (e.g. answers,
     * start and finish time)
     *
     * @param triplePat the new candidate triple pattern
     * @param constantVal the constant value to be searched later
     * @param indxLogCleanQuery the LogClean query to which this CTP is
     * contained
     * @param indxLogQueryDedGraph the deduced graph to which the LogClean query
     * is contained
     * @param strDedQueryId BUUUUUUUUUUUUUUUUUUUUUUUG
     * @param lastExistingTPIndex BUUUUUUUUUUUUUUUUUUUG
     */
    public void setNewCTPInfo(List<String> triplePat, String constantVal,
            int indxLogCleanQuery, int indxLogQueryDedGraph, String strDedQueryId, String lastExistingTPIndex) {

        DTPCandidates.add(new LinkedList<String>());

        if (!triplePat.get(0).contains("?")) {
            DTPCandidates.get(DTPCandidates.size() - 1).add(triplePat.get(0));
        } else {

            DTPCandidates.get(DTPCandidates.size() - 1).add(triplePat.get(0) + lastExistingTPIndex);
        }
        DTPCandidates.get(DTPCandidates.size() - 1).add(triplePat.get(1));
        if (!triplePat.get(2).contains("?")) {
            DTPCandidates.get(DTPCandidates.size() - 1).add(triplePat.get(2));

        } else {
            
            DTPCandidates.get(DTPCandidates.size() - 1).add(triplePat.get(2) + lastExistingTPIndex);
        }

        DTPCandidates.get(DTPCandidates.size() - 1).add(strDedQueryId);
        myBasUtils.insertToMap4(mapCTPtoConstants, DTPCandidates.get(DTPCandidates.size() - 1), constantVal);
        myBasUtils.insertToMap(mapCTPtoDedGraph, indxLogQueryDedGraph, DTPCandidates.size() - 1);
        mapCTPToFinishTime.put(DTPCandidates.size() - 1, mapLogClQueryToTimestamp.get(indxLogCleanQuery));
        mapCTPToStartTime.put(DTPCandidates.size() - 1, mapLogClQueryToTimestamp.get(indxLogCleanQuery));
        myBasUtils.insertToMap(mapCTPtoQuerySrc, triplePat, indxLogCleanQuery);
    }

    /**
     * Update hash map info about an existing candidate triple pattern CTP
     *
     * @param triplePat the new candidate triple pattern
     * @param constantVal the constant value to be searched later
     * @param indxLogCleanQuery the LogClean query to which this CTP is
     * contained
     * @param indxLogQueryDedGraph the deduced graph to which the LogClean query
     * is contained
     * @param indxCTP index of the existing CTP
     */
    public void updateCTPInfo(List<String> triplePat, String constantVal, int indxLogCleanQuery, int indxLogQueryDedGraph, int indxCTP) {

        myBasUtils.insertToMap4(mapCTPtoConstants, DTPCandidates.get(indxCTP), constantVal);
        mapCTPToFinishTime.put(indxCTP, mapLogClQueryToTimestamp.get(indxLogCleanQuery));
        myBasUtils.insertToMap(mapCTPtoQuerySrc, triplePat, indxLogCleanQuery);
    }

    /**
     * Set hash map info about a new deduced triple pattern DTP
     *
     * @param currTP the new deduced triple pattern
     * @param indCTP index of the existing CTP
     */
    public void setDTPHashInfo(List<String> currTP, int indCTP) {

        int currDTPsize = mapDTPToDeducedID.size();
        List<String> endpoints = null;
        List<String> originalTP = getCleanTP(DTPCandidates.get(indCTP));

        if (mapDTPtoExclGroup.get(currTP) == null) {

            mapDTPToDeducedID.put(currTP, currDTPsize);
            mapDTPToAnyJoin.put(currDTPsize, -1);
        }

        if (mapDTPtoJoinBGP.get(currTP) == null) {

            mapDTPtoJoinBGP.put(currTP, -1);
        }

        endpoints = mapCTPToEndpsSrc.get(originalTP);
        
        //BUUUUUUG
        if (endpoints == null) {
            endpoints = new LinkedList<>();
            endpoints.add("8700");
        }

        myBasUtils.insertToMap3(mapDTPToEndpsSrc, endpoints, currTP);
        mapDTPToFinishTime.put(currTP, myBasUtils.getTimeInSec(mapCTPToFinishTime.get(indCTP)));
        mapDTPToStartTime.put(currTP, myBasUtils.getTimeInSec(mapCTPToStartTime.get(indCTP)));
    }
    
    
    /**
     * Particular function for FedX traces. Once identifying a DTP as at the
     * same time NLBJ to NLFO, match all corresponding structures from the one DTP
     * to another
     *
     * @param NLBJtp deduced triple pattern, with a NLBJ implementation
     * @param mapNLBJTPtoValues BUUUUUUUUUUUUUUUUG
     * @param associatedAnswers answer values associated to deduced triple pattern, with a NLBJ implementation
     */
    public void setDTPinfoToAnother(List<String> NLBJtp, HashMap<List<String>, List<String>> mapNLBJTPtoValues, List<String> associatedAnswers) {

        List<String> tripletNew = new LinkedList<>(NLBJtp.subList(0, 3));
        List<String> tripletTMP = new LinkedList<>(mapNLBJTPtoValues.get(NLBJtp).subList(0, 3));
        
        setDTPbasicInfo(tripletNew);
        mapDTPtoAnswTotal.put(NLBJtp, associatedAnswers);
        mapDTPToEndpsSrc.put(tripletNew, mapDTPToEndpsSrc.get(tripletTMP));
        mapDTPToStartTime.put(tripletNew, mapDTPToStartTime.get(tripletTMP));
        mapDTPToFinishTime.put(tripletNew, mapDTPToFinishTime.get(tripletTMP));
        int currentSizeDTPtoAlternatives = mapDTPtoAlternatives.size();
        mapDTPtoAlternatives.put(tripletTMP, currentSizeDTPtoAlternatives);
        mapDTPtoAlternatives.put(tripletNew, currentSizeDTPtoAlternatives);
    }

    
    /**
     * Particular function for FedX traces. Save alternative pairs of DTPs,
     * corresponding to alternative matching vars. These pairs, do not count as
     * identified joins from FETA.
     *
     * @param mapCandVarToMatchedVals map a candidate var to matching values to
     * a CTP
     * @param indxCTP index of current CTP
     */
    public void setDTPtoAlternatives(HashMap<String, List<String>> mapCandVarToMatchedVals, int indxCTP) {

        List<String> newDedTPOuter = null;
        List<String> newDedTPInner = null;
        List<String> valueskeyOuter = null;
        List<String> valueskeyInner = null;
        List<List<String>> newPairTPs = null;
        int currentSizeDTPtoAlternatives = mapDTPtoAlternatives.size();

        for (String keyOuter : mapCandVarToMatchedVals.keySet()) {

            valueskeyOuter = mapCandVarToMatchedVals.get(keyOuter);
            String cleanVariableOuter = "?" + keyOuter.substring(0, keyOuter.indexOf("_"));
            newDedTPOuter = getNewRawTP(DTPCandidates.get(indxCTP), cleanVariableOuter);

            for (String keyInner : mapCandVarToMatchedVals.keySet()) {

                if (keyInner.equalsIgnoreCase(keyOuter)) {
                    continue;
                }

                String cleanVariableInner = "?" + keyInner.substring(0, keyInner.indexOf("_"));
                valueskeyInner = mapCandVarToMatchedVals.get(keyInner);

                if (!Collections.disjoint(valueskeyOuter, valueskeyInner)) {

                    newDedTPInner = getNewRawTP(DTPCandidates.get(indxCTP), cleanVariableInner);
                    newPairTPs = Arrays.asList(newDedTPOuter, newDedTPInner);
                    mapDTPtoAlternatives.put(newDedTPOuter, currentSizeDTPtoAlternatives + 1);
                    mapDTPtoAlternatives.put(newDedTPInner, currentSizeDTPtoAlternatives + 1);
                    setPairJoinToBGP(newPairTPs, (float) 1.0);
                    mapPairToRelation.put(newPairTPs, "alternativeMappings");
                }

            }
        }
    }

    /**
     * Cancel a Deduced Triple Pattern, and all associated hash maps
     *
     * @param currDTP the new deduced triple pattern
     */
    public void cancelDTP(List<String> currDTP) {

        mapDTPtoCANCELofEG.put(currDTP, 1);
        int currID = mapDTPToDeducedID.get(currDTP);
        mapDTPToAnyJoin.remove(currID);
        mapDTPToDeducedID.remove(currDTP);
        mapDTPToEndpsSrc.remove(currDTP);
        mapDTPToFinishTime.remove(currDTP);
        mapDTPToStartTime.remove(currDTP);
        mapDTPtoAnswTotal.remove(currDTP);
        mapDTPtoJoinBGP.remove(currDTP);
    }

    /**
     * Set info concerning "NotNullJoin", for a Deduced Triple Pattern (DTP)
     *
     * @param currDTP the new deduced triple pattern
     */
    public void setMapsNotNullJoin(List<String> currDTP) {

        if (mapDTPtoJoinBGP.get(currDTP) == null) {

            mapDTPtoJoinBGP.put(currDTP, -1);
        }

        if (mapDTPToDeducedID.get(currDTP) != null) {
            if (mapDTPToAnyJoin.get(mapDTPToDeducedID.get(currDTP)) == -1) {

                mapDTPToAnyJoin.put(mapDTPToDeducedID.get(currDTP), 1);
            }
        }
    }

    /**
     * Get list of TPs that are pairwise joined to a specific triple pattern
     *
     * @param allPairJoins all deduced pairWise joins
     * @param currDTP current deduced triple pattern
     * @return list of all DTPs to which currDTP are joined
     */
    public List<List<String>> getListJoinedTPs(List<List<List<String>>> allPairJoins, List<String> currDTP) {

        List<List<String>> matchedList = new LinkedList<>();
        List<List<List<String>>> toRemove = new LinkedList<>();

        for (List<List<String>> list : allPairJoins) {

            if ((list.get(0).get(0).equals(currDTP.get(0))
                    && list.get(0).get(1).equals(currDTP.get(1))
                    && list.get(0).get(2).equals(currDTP.get(2)))) {

                matchedList.add(list.get(1));
                toRemove.add(list);

            } else if ((list.get(1).get(0).equals(currDTP.get(0))
                    && list.get(1).get(1).equals(currDTP.get(1))
                    && list.get(1).get(2).equals(currDTP.get(2)))) {

                matchedList.add(list.get(0));
                toRemove.add(list);

            }

        }

        return matchedList;
    }

    /**
     * Add a new triple pattern in the same "NotNullJoin" BGP
     *
     * @param newDTP new deduced triple pattern to be added
     * @param indxBGP index of BGP to which ewDTP will be added
     * @param confidence updated confidence level of current BGP
     */
    public void addTPinBGP(List<String> newDTP, int indxBGP, float confidence) {

        float currentConf = -1;

        notnullJoinBGPs.get(indxBGP).add(newDTP);
        mapDTPtoJoinBGP.put(newDTP, indxBGP);
        currentConf = mapBGPtoConfidence.get(indxBGP);
        mapBGPtoConfidence.put(indxBGP, currentConf * confidence);
    }

    /**
     * For each pair of joined triple patterns, we add the one which not belong
     * to a BGP (i.e., srcTP) to the BGP graph of the other (i.e., destTP)
     *
     * @param destTP triple pattern belonging to a BGP
     * @param srcTP triple pattern not belonging to a BGP
     * @param confidence updated confidence level of current BGP
     */
    public void associateTPsInBGP(List<String> destTP, List<String> srcTP, float confidence) {

        List<List<String>> tmpGraph = new LinkedList<>();
        int tmpIndxMerge = -1;

        addTPinBGP(srcTP, mapDTPtoJoinBGP.get(destTP), confidence);
        tmpGraph = getListJoinedTPs(setDTPbasedAnswers, destTP);
        tmpIndxMerge = mapDTPtoJoinBGP.get(destTP);

        for (int i = 0; i < tmpGraph.size(); i++) {
            if (!listInListContain(notnullJoinBGPs.get(tmpIndxMerge), tmpGraph.get(i))) {

                addTPinBGP(tmpGraph.get(i), mapDTPtoJoinBGP.get(destTP), confidence);
            }

        }
    }

    /**
     * Combine two BGPs, of two respective triple patterns that are now joined
     *
     * @param innerTP outer triple pattern of the current pairWise join
     * @param outerTP inner triple pattern of the current pairWise join
     * @param confidence updated confidence level of current BGP
     */
    public void fetchAndAddTPsInBGP(List<String> innerTP, List<String> outerTP, float confidence) {

        List<List<String>> tmpGraph = new LinkedList<>();
        int tmpIndxRemove = -1;
        int tmpIndxMerge = -1;
        float tmpConfidene = -1;

        tmpGraph = notnullJoinBGPs.get(mapDTPtoJoinBGP.get(innerTP));
        tmpConfidene = mapBGPtoConfidence.get(mapDTPtoJoinBGP.get(innerTP));
        tmpIndxRemove = mapDTPtoJoinBGP.get(innerTP);
        tmpIndxMerge = mapDTPtoJoinBGP.get(outerTP);

        for (int i = 0; i < tmpGraph.size(); i++) {
            if (!listInListContain(notnullJoinBGPs.get(tmpIndxMerge), tmpGraph.get(i))) {

                addTPinBGP(tmpGraph.get(i), mapDTPtoJoinBGP.get(outerTP), confidence * tmpConfidene);
            }

        }

        if (!myBasUtils.elemInListEquals(listBGPsremoveAnswer, tmpIndxRemove)) {

            listBGPsremoveAnswer.add(tmpIndxRemove);
        }
    }
 /**
     * Check if a list of elements is contained into a list of lists of elemnts
     *
     * @param listOfLists input list of lists of elements
     * @param searchList list of elements to be searched
     * @return true if "searchList" is contained into "listOfLists"
     */
    public boolean listInListContain(List<List<String>> listOfLists, List<String> searchList) {

        for (List<String> list : listOfLists) {

            if (list.get(0).equals(searchList.get(0))
                    && list.get(1).equals(searchList.get(1))
                    && list.get(2).equals(searchList.get(2))) {

                return true;
            }

            if (list.size() == 6) {

                if (list.get(3).equals(searchList.get(0))
                        && list.get(4).equals(searchList.get(1))
                        && list.get(5).equals(searchList.get(2))) {

                    return true;
                }
            }

        }

        return false;
    }

    /**
     * Set each triple pattern of a new pairWise join, to corresponding BGPs. If
     * both do not belong to any BGP, initialize a new one with both.
     *
     * @param pairJoin new pairWise join
     * @param confidence confidence level of the pairWise join
     */
    public void setPairJoinToBGP(List<List<String>> pairJoin, float confidence) {

        List< List<String>> tmppairTPs = new LinkedList<>();
        List<String> innerTP = new LinkedList<>(pairJoin.get(0).subList(0, 3));
        List<String> outerTP = new LinkedList<>(pairJoin.get(1).subList(0, 3));

        tmppairTPs.add(outerTP);
        tmppairTPs.add(innerTP);

        setMapsNotNullJoin(outerTP);
        setMapsNotNullJoin(innerTP);

        if (notnullJoinBGPs.isEmpty() || (mapDTPtoJoinBGP.get(outerTP) == -1 && mapDTPtoJoinBGP.get(innerTP) == -1)) {

            setDTPbasedAnswers.add(tmppairTPs);
            notnullJoinBGPs.add(tmppairTPs);
            mapDTPtoJoinBGP.put(tmppairTPs.get(0), notnullJoinBGPs.size() - 1);
            mapDTPtoJoinBGP.put(tmppairTPs.get(1), notnullJoinBGPs.size() - 1);
            mapBGPtoConfidence.put(notnullJoinBGPs.size() - 1, (float) confidence);
        } else {

            if (mapDTPtoJoinBGP.get(outerTP) == -1 && mapDTPtoJoinBGP.get(innerTP) != -1) {

                associateTPsInBGP(innerTP, outerTP, confidence);
            } else if (mapDTPtoJoinBGP.get(outerTP) != -1 && mapDTPtoJoinBGP.get(innerTP) == -1) {

                associateTPsInBGP(outerTP, innerTP, confidence);
            } else if (mapDTPtoJoinBGP.get(outerTP) != -1 && !Objects.equals(mapDTPtoJoinBGP.get(outerTP), mapDTPtoJoinBGP.get(innerTP))) {

                fetchAndAddTPsInBGP(innerTP, outerTP, confidence);
            }

        }
    }

    /**
     * Check if the current relation (i.e., "symhash", "constant" or
     * "nestedLoop"), is covered by the window gap (i.e., Tjoin).
     *
     * @param outerTP outer triple pattern, of the pairWise join
     * @param innerTP inner triple pattern, of the pairWise join
     * @param pairJoin new pairWise join
     * @param confidence confidence level of the pairWise join
     * @param relation type of the pairWise join
     * @param forcePair force join for a nested loop with exclusive groups
     */
    public void pairJoinRelation(List<String> outerTP, List<String> innerTP, List<List<String>> pairJoin, double confidence, String relation, boolean forcePair) {

        int startOuter = 100000000;
        int finishOuter = 100000000;
        boolean flagTimeJoinable = false;
        int startInner = 100000000;
        int finishInner = 100000000;
        
        if (!forcePair) {

            startOuter = mapDTPToStartTime.get(outerTP);
            finishOuter = mapDTPToFinishTime.get(outerTP);
            startInner = mapDTPToStartTime.get(innerTP);
            finishInner = mapDTPToFinishTime.get(innerTP);

            //buuuuuuuuuuuuuuuug to many conditions
            if (((startInner - finishOuter <= windowJoin) && (startInner - finishOuter > 0)
                    || (startInner < finishOuter && (startInner - finishOuter > 0)))) {
                flagTimeJoinable = true;

            } else if (((finishOuter - startInner <= windowJoin) && (finishOuter - startInner > 0)
                    || (finishOuter < startInner && (finishOuter - startInner > 0)))) {

                flagTimeJoinable = true;

            } else if ((startInner >= startOuter) && (finishInner <= finishOuter)) {

                flagTimeJoinable = true;

            } else if ((startOuter >= startInner) && (finishOuter <= finishInner)) {

                flagTimeJoinable = true;
            } else if ((startOuter -startInner <= windowJoin)&&startOuter -startInner>0) {

                flagTimeJoinable = true;
            }
            
            else if ((startInner-startOuter<=windowJoin)&& startInner-startOuter>0) {

                flagTimeJoinable = true;
            }
            
             else if ((finishInner < startInner) && (finishOuter < finishInner) && finishOuter == startInner) {

                flagTimeJoinable = true;
            }
            
            else if ((startOuter<=startInner)&&(startInner<=finishOuter)) {

                flagTimeJoinable = true;
            }
            
            else if ((startInner<=startOuter)&&(startOuter<=finishInner)) {

                flagTimeJoinable = true;
            }
            
        }

        List<String> tmpOuterShort = new LinkedList<>(outerTP.subList(0, 3));
        List<String> tmpInnerShort = new LinkedList<>(innerTP.subList(0, 3));

        if (flagTimeJoinable || forcePair) {

            totalPairs++;

            if (relation.contains("symhash")) {
                
                cntSYMHASHTotal++;
            } else if (relation.contains("constant")) {
                
                cntCONSTJOINTotal++;
            } else if (relation.contains("nestedLoop")) {
                
                cntNESLOOPTotal++;
            }

            List<List<String>> seenPair = Arrays.asList(tmpInnerShort, tmpOuterShort);
            List<List<String>> seenPair2 = Arrays.asList(tmpOuterShort, tmpInnerShort);

            if (mapGroundTruthPairs.get(seenPair) != null || mapTruePositivePairs.get(seenPair2) != null) {

                mapTruePositivePairs.put(seenPair, 1);
                mapTruePositivePairs.put(seenPair2, 1);
                truePositivesPairs++;

                if (relation.contains("symhash")) {
                    cntSYMHASHTrPo++;
                } else if (relation.contains("constant")) {
                    cntCONSTJOINTrPo++;

                } else if (relation.contains("nestedLoop")) {
                    cntNESLOOPTrPo++;
                }

            } else {

                // System.out.println("BUUUUUUUG: "+pair);
            }

            int currDTPsize = mapDTPToDeducedID.size();
            if (mapDTPToDeducedID.get(outerTP) == null) {

                mapDTPToDeducedID.put(outerTP, currDTPsize);
                mapDTPToAnyJoin.put(currDTPsize, 1);
            }
            currDTPsize = mapDTPToDeducedID.size();

            if (mapDTPToDeducedID.get(innerTP) == null) {

                mapDTPToDeducedID.put(innerTP, currDTPsize);
                mapDTPToAnyJoin.put(currDTPsize, 1);
            }

            System.out.println("\t\t\t================ BINGO deduced JOIN: " + relation + " ================");
            System.out.println("\t\t\t\t\t[Outer DTP] " + outerTP.get(0) + " " + outerTP.get(1) + " " + outerTP.get(2) + ", in interval [" + startOuter + ", " + finishOuter + "]");
            System.out.println("\t\t\t\t\t[Inner DTP] " + innerTP.get(0) + " " + innerTP.get(1) + " " + innerTP.get(2) + ", in interval [" + startInner + ", " + finishInner + "]");

            System.out.println();
            setPairJoinToBGP(pairJoin, (float) confidence);
            mapPairToRelation.put(pairJoin, relation);
        } else {

            System.out.println("\t\t\t________________ MISSED deduced JOIN because of Tjoin: " + relation + " ________________");
            System.out.println("\t\t\t\t\t[Outer DTP] " + outerTP.get(0) + " " + outerTP.get(1) + " " + outerTP.get(2) + ", in interval [" + startOuter + ", " + finishOuter + "]");
            System.out.println("\t\t\t\t\t[Inner DTP] " + innerTP.get(0) + " " + innerTP.get(1) + " " + innerTP.get(2) + ", in interval [" + startInner + ", " + finishInner + "]");

            System.out.println();
        }

    }

    /**
     * Particular function for FedX traces. Match to particular var "?o" values
     * that are passed as FILTER options, for an inner bound subquery
     *
     * @param query inner bound subquery in String format
     * @param allTPs list of elements of the inner bound subquery
     */
    public void setCTPtoFilterBoundVals(String query, List<String> allTPs) {

        for (int i = 0; i < allTPs.size(); i += 3) {

            List<String> triplet = new LinkedList<>(allTPs.subList(i, i + 3));
            List<String> filterBoundPattern = new LinkedList<>();
            List<String> key = null;
            String value = "";

            if (allTPs.get(i).contains("?")) {

                filterBoundPattern.add(allTPs.get(i));
            }

            filterBoundPattern.add(allTPs.get(i + 1));

            if (allTPs.get(i + 2).contains("?")) {

                filterBoundPattern.add(allTPs.get(i + 2));
            }

            int index = query.indexOf(filterBoundPattern.get(0));
            query = query.substring(index, query.length());
            value = query.substring(query.indexOf("= ") + 2, query.indexOf(" )"));

            for (List<String> keyCTP : mapCTPtoFILTERwithBoundJ.keySet()) {

                if (myBasUtils.elemInListEquals(keyCTP, filterBoundPattern.get(0)) && myBasUtils.elemInListEquals(keyCTP, filterBoundPattern.get(1))) {

                    key = keyCTP;
                }
            }

            if (key == null) {

                myBasUtils.insertToMap(mapCTPtoFILTERwithBoundJ, value, triplet);
            } else if (!myBasUtils.elemInListEquals(mapCTPtoFILTERwithBoundJ.get(key), value)) {

                myBasUtils.insertToMap(mapCTPtoFILTERwithBoundJ, value, key);
            }

        }
    }

    /**
     * This function implements inverse mapping. Find all vars that match to
     * constant values of a Candidate Triple pattern.
     *
     * @param mapCandVarToAllAnswers map a candidate var to all its answers
     * @param mapCandVarToMatchedVals map a candidate var to matching values to
     * a CTP
     * @param allCTPconstants all constants of a CTP to be matched
     * @throws SQLException
     */
    public void searchMatchingVars(HashMap<String, List<String>> mapCandVarToAllAnswers,
            HashMap<String, List<String>> mapCandVarToMatchedVals, List<String> allCTPconstants) throws SQLException {

        String Answer = "", OriginalQuery = "";
        String mapVar = "";
        boolean flagAnswerMatch = false;
        List<String> entryInformation = null;
        List<String> currAnsVals = null;
        List<String> currCTPValsMatched = null;
        List<String> answerEntryVars = new LinkedList<>();

        for (int key : mapAnsIDtoEntry.keySet()) {

            if (setMonetDB) {

                entryInformation = myMonet.getEntryAnswers(key);
            } else {

                entryInformation = mapAnsIDtoEntry.get(key);
            }

            flagAnswerMatch = false;

            if (!entryInformation.isEmpty()) {

                Answer = entryInformation.get(0);
                OriginalQuery = entryInformation.get(4);

                if (OriginalQuery.contains("?s ?p ?o") || (Answer.contains("\"s\"") && Answer.contains("\"p\"") && Answer.contains("\"o\""))) {

                    continue;
                }

                if (!Answer.contains("boolean") || Answer.contains("results") && Answer.contains("value")) {

                    answerEntryVars = myBasUtils.getAnswerVars(Answer);
                    answerEntryVars = myBasUtils.sortAndRemoveRedundancy(answerEntryVars);

                    for (int y = 0; y < answerEntryVars.size(); y++) {

                        if (answerEntryVars.get(y).contains("predicate")) {

                            continue;
                        }

                        for (String currSignature : mapAnsEntryToAllSignatures.get(Integer.toString(key))) {

                            if (currSignature.contains("NoAnswersToQuery") || currSignature.contains("predicate")) {
                                continue;
                            }

                            mapVar = currSignature.substring(currSignature.indexOf("_") + 1, currSignature.length());
                            mapVar = "?" + mapVar.substring(0, mapVar.indexOf("_"));
                            String mapAnsVar = answerEntryVars.get(y);

                            if (!mapAnsVar.contains("?")) {

                                mapAnsVar = "?" + mapAnsVar;
                            }

                            if (mapVar.equalsIgnoreCase(mapAnsVar)) {

                                currAnsVals = mapAnsSingatureToAllValues.get(currSignature);

                                //BUUUUG of exclusive groups and signatures
                                if (currAnsVals == null) {
                                    continue;
                                }
                                currCTPValsMatched = myBasUtils.getListsIntersec(currAnsVals, allCTPconstants);

                                //Buuuuuuuuuug
                                if (!currCTPValsMatched.isEmpty()) {

                                    flagAnswerMatch = true;
                                }

                                if (flagAnswerMatch) {

                                    String subKey = currSignature.substring(currSignature.indexOf("_") + 1, currSignature.length());
                                    myBasUtils.insertToMap(mapCandVarToAllAnswers, currAnsVals, subKey);
                                    myBasUtils.insertToMap(mapCandVarToMatchedVals, currCTPValsMatched, subKey);
                                }
                            }
                        }

                    }
                }
            }

        }
    }

    /**
     * Set answers of free variables (i.e., not hidden) to corresponding CTPs
     *
     * @param currPattern current CTP
     * @param indxLogCleanQuery index of LogClean query from which ans will be
     * matched
     * @param variableOriginal original free var of the current CTP or DTP
     * @param indxPattern index of current CTP
     */
    public void setTPtoSrcAns(List<String> currPattern, int indxLogCleanQuery,
            String variableOriginal, int indxPattern) {

        String Answer = "", requestQuery = "", endpoint = "";
        List<String> matchQueryExtrVars = null;
        List<String> entryInformation = null;
        List<String> answerEntities = null;
        List<String> tmpTP = new LinkedList<>();
        List<String> tpVars = new LinkedList<>();
        List<Integer> key = mapLogClQueryToAnsEntry.get(indxLogCleanQuery);
        boolean flagPassed = false;

        if (variableOriginal.equals("")) {

            tpVars = getTPVariables(currPattern);
        } else {

            tpVars.add(variableOriginal);
        }

        if (key != null && key.size() > 0) {
            for (int k = 0; k < key.size(); k++) {

                entryInformation = mapAnsIDtoEntry.get(key.get(k));
                if (entryInformation != null && !entryInformation.isEmpty()) {

                    Answer = entryInformation.get(0);
                    endpoint = entryInformation.get(1);
                    requestQuery = entryInformation.get(4);

                    String indxQueryString = mapAnsIDToLogClQuery.get(key.get(k));
                    myBasUtils.insertToMap(mapCTPToEndpsSrc, endpoint, currPattern);
                    List<Integer> test = new LinkedList<>();
                    test.add(Integer.parseInt(indxQueryString));
                    mapDTPtoInnerQuery.put(indxPattern, test);

                    matchQueryExtrVars = myBasUtils.getProjVars(requestQuery);
                    if ((((Answer.contains("results")) && !Answer.contains("boolean")) || Answer.contains("value"))) {

                        for (int u = 0; u < matchQueryExtrVars.size(); u++) {

                            if (myBasUtils.elemInListContained(tpVars, matchQueryExtrVars.get(u))) {

                                answerEntities = mapAnsEntryToListValues.get(key.get(k) + (matchQueryExtrVars.get(u).substring(matchQueryExtrVars.get(u).indexOf("?") + 1)));

                                if (answerEntities == null) {

                                    answerEntities = new LinkedList<>();
                                }

                                tmpTP = getCleanTP(currPattern);
                                tmpTP.add(matchQueryExtrVars.get(u));
                                myBasUtils.insertToMap4(mapCTPtoAnswTotal, currPattern, answerEntities);
                                myBasUtils.insertToMap(mapCTPToEndpsSrc, endpoint, currPattern);

                                if (tmpTP.get(0).contains("?") && tmpTP.get(2).contains("?") || !inverseMapping) {
                                    myBasUtils.insertToMap4(mapDTPtoAnswTotal, tmpTP, answerEntities);
                                }

                                myBasUtils.insertToMap4(mapCTPtoAnswTotal, tmpTP, answerEntities);
                                myBasUtils.insertToMap(mapCTPToEndpsSrc, endpoint, tmpTP);
                                flagPassed = true;
                            }

                        }
                    }
                }

            }

            if (flagPassed == false) {

                answerEntities = new LinkedList<>();
                answerEntities.add("noneProjected");
                myBasUtils.insertToMap4(mapCTPtoAnswTotal, currPattern, answerEntities);
                myBasUtils.insertToMap(mapCTPToEndpsSrc, endpoint, currPattern);
            }
        }
    }

     /**
     * Match directly a DTP to its source variable's answers
     *
     * @param indxCTP index of current CTP
     * @param matchedCTPAns constant ba values of CTP, matched to current FTP
     * @param deducedTP triple pattern to which values will be affected
     * @param flagSkip boolean to skip this deduced triple pattern
     * @return true if the deduced triple pattern is skipped
     */
    public boolean setDTPtoSrcAns(int indxCTP, List<String> matchedCTPAns, List<String> deducedTP, boolean flagSkip) {

        List<String> tmpTP = null;
        List<String> answEntitiesOfTPinDTP = null;
        List<String> newDeducedTP = getCleanTP(deducedTP);
        List<List<String>> newPairTPsTmp = new LinkedList<>();
        List<String> outerTMP = new LinkedList<>();
        List<String> innerTMP = new LinkedList<>();
        String originalVAR = getTPVariables(DTPCandidates.get(indxCTP)).get(0);
        newDeducedTP.add(originalVAR);
    
        for (int l = 0; l < matchedCTPAns.size(); l++) {

            tmpTP = getCleanTP(getNewRawTP(allCTPs.get(indxCTP), matchedCTPAns.get(l)));
            answEntitiesOfTPinDTP = mapCTPtoAnswTotal.get(tmpTP);

            if (answEntitiesOfTPinDTP != null) {

                if (answEntitiesOfTPinDTP.size() > 0) {

                    myBasUtils.insertToMap4(mapDTPtoAnswTotal, newDeducedTP, answEntitiesOfTPinDTP);
                }

                if (myBasUtils.elemInListContained(answEntitiesOfTPinDTP, "noneProjected") && !flagSkip) {

                    flagSkip = true;
                    outerTMP = new LinkedList<>(deducedTP.subList(0, 3));
                    innerTMP = new LinkedList<>(tmpTP.subList(0, 3));
                    newPairTPsTmp = Arrays.asList(deducedTP, tmpTP);
                    newPairTPsTmp = Arrays.asList(outerTMP, innerTMP);

                    //Whaaaaaaaaaaat?
                    if (mapDTPofEGNested.get(outerTMP) == null && mapDTPofEGNested.get(innerTMP) == null) {

                        setPairJoinToBGP(newPairTPsTmp, (float) 1.0);
                    }
                }
            }
        }
    
         return flagSkip;
    }
    
}