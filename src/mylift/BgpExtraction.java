package mylift;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class for creating and showing BGPs i.e., sets of triple patterns, using as
 * input the set of Deduced Triple Patterns (DTPs)
 *
 * @author Nassopoulos Georges
 * @version 1.0
 * @since 2016-12-15
 */
public class BgpExtraction {

    FileWriter writerJoins;
    BufferedWriter bufferWritterJoins;
    FileWriter writerBGPs;
    BufferedWriter bufferWritterBGPs;

    /* Precision/recall-oriented hash/set maps used in the code */
    // List of ground truth pair-joins and their occurances
    public static Map<List<List<String>>, Integer> mapGroundTruthPairsLDF;
    // List of identified ground truth pair-joins
    public static Map<List<List<String>>, Integer> mapPairJoinToOccurs;
    // total join pairs identified
    public static int totalPairs;
    // true positives join pairs identified
    public static int truePositivesPairs;
    // all BGPs identified
    public static int numBGP;

    public BgpExtraction() {

        mapGroundTruthPairsLDF = new HashMap<>();
        mapPairJoinToOccurs = new HashMap<>();
        totalPairs = 0;
        truePositivesPairs = 0;
        numBGP = 0;
    }

    /**
     * Initialize BGP construction and print BGPs
     *
     * @param mapDTPtoSerialID map of DTPs with their corresponding seral ID
     */
    public void initBgpExtract(HashMap<Integer, List<String>> mapDTPtoSerialID) {

        List<List<Integer>> allDeducedBGPs = null;

        //create BGP output files and deduced joined pairs
        try {

            writerBGPs = new FileWriter("deducedBGPs_" + Configuration.nameDB + "_gap=" + Configuration.gapWin + ".txt", false);
            bufferWritterBGPs = new BufferedWriter(writerBGPs);

            //For a concurrent execution, save pairs of "ground truth" joins
            if (Configuration.isolatedExec) {

                writerJoins = new FileWriter("truePositivePairs" + Configuration.nameDB + ".txt", false);
                bufferWritterJoins = new BufferedWriter(writerJoins);

                //   setGroundTruthPairs();
            } // For concurrent excecution, load pairs of "ground truth" joins
            else if (Configuration.concurentExec) {

                getGroundTruthPairs();
            }

        } catch (Exception ex) {

            Logger.getLogger(BgpExtraction.class.getName()).log(Level.SEVERE, null, ex);
        }

        // construct BGP sets from Deduced Triple Patterns (DTPs), their serial ids
        // and their possible injected vals
        allDeducedBGPs = constructBGPs(mapDTPtoSerialID);

        //print deduced bgps with 
        printBgpSets(allDeducedBGPs);

        // For concurrent execution, generate precison/recall statistics
        if (Configuration.concurentExec) {

            generateGNUFinal();
        }

        // print BGPs
        showAllBGPs(allDeducedBGPs, mapDTPtoSerialID);
    }

    /**
     * Construct all possible deduced BGPs using the set of Deduced Triple
     * Patterns (DTP), i.e., their serial ID and the key word "INJECTED_FROM",
     * in order to identify between each pair of join.
     *
     * @param mapDTPset map of DTPs with their serial IDs
     * @return list of deduced bgps, with ids of DTPs
     */
    private List<List<Integer>> constructBGPs(HashMap< Integer, List<String>> mapDTPset) {

        List<List<Integer>> allBGPs = new LinkedList<>();
        HashMap<Integer, Integer> mapDTPtoBGP = new HashMap<>();

        //init maps regarding each deduced LDF and corresponding graph/bgp 
        for (int dedLDF : mapDTPset.keySet()) {

            mapDTPtoBGP.put(dedLDF, -1);
        }
        
        //Init the first deduced graph with the first identified deduced LDF
        allBGPs.add(new LinkedList<>());
        allBGPs.get(0).add(1);
        mapDTPtoBGP.put(1, allBGPs.size() - 1);

        for (int outerDTP : mapDTPset.keySet()) {

               
            if(CtpExtraction.flagDum){
                allBGPs.add(new LinkedList<>());
                allBGPs.get(0).add(outerDTP);
                mapDTPtoBGP.put(outerDTP, 0);
                continue;
            }
            
            // Check if current DTP can be added in an existing BGP
            if (mapDTPtoBGP.get(outerDTP) == -1) {
                addInExistBGP(allBGPs, outerDTP, mapDTPset, mapDTPtoBGP);
            }
            
            //if not, create a new BGP for current DTP
            if (mapDTPtoBGP.get(outerDTP) == -1) {

                allBGPs.add(new LinkedList<>());
                allBGPs.get(allBGPs.size() - 1).add(outerDTP);
                mapDTPtoBGP.put(outerDTP, allBGPs.size() - 1);
            }
         

            // for the rest of DTPs that have not been matched into a BGP,
            // try to see if they can be added into an existng one
            for (int innerDTP : mapDTPset.keySet()) {

                if (mapDTPtoBGP.get(innerDTP) == -1) {

                    if (testNestedLoop(mapDTPset.get(outerDTP), outerDTP, mapDTPset.get(innerDTP), innerDTP)) {

                        allBGPs.get(mapDTPtoBGP.get(outerDTP)).add(innerDTP);
                        mapDTPtoBGP.put(innerDTP, mapDTPtoBGP.get(outerDTP));
                    }
                }

            }

        }

        return allBGPs;
    }

    /**
     * Check if two DTPs are joined through a nested loop and save new join set
     * into file
     *
     * @param ouetDTP outer DTP
     * @param outerKey outer DTP's id
     * @param innerDTP inner DTP
     * @param innerKey inner DTP's id
     * @return true if they are joined through a nested loop
     */
    private boolean testNestedLoop(List<String> ouetDTP, int outerKey, List<String> innerDTP, int innerKey) {

        boolean ret = false;
        int innerInjVar = -1;
        List<List<String>> currJoin = new LinkedList<>();
        List<List<String>> symJoin = new LinkedList<>();


        for (int j = 0; j < innerDTP.size(); j++) {
            if (innerDTP.get(j).contains("INJECTED") && !innerDTP.get(j).contains("POSSIBLY")) {

                // If inner variable has the key word "INJECTED", this indicates the DTP'id that pushed the value
                innerInjVar = Integer.parseInt(innerDTP.get(j).substring(innerDTP.get(j).indexOf("LDF_") + 4, innerDTP.get(j).indexOf(")")));
                if (innerInjVar == outerKey) {

                    currJoin.add(ouetDTP);
                    currJoin.add(innerDTP);

                    symJoin.add(currJoin.get(1));
                    symJoin.add(currJoin.get(0));

                    setJoinInfo(currJoin, symJoin);

                    return true;
                }
            }
        }

        return ret;
    }

    /**
     * Append this pair in the file of identified joins
     *
     * @param pairJoin current pair join
     * @param symJoin symmetric pair join
     */
    private void setJoinInfo(List<List<String>> pairJoin, List<List<String>> symJoin) {

        if (Configuration.isolatedExec) {

            try {

                bufferWritterJoins.write(pairJoin.get(0).get(0) + " " + pairJoin.get(0).get(1) + " " + pairJoin.get(0).get(2) + ", "
                        + pairJoin.get(1).get(0) + " " + pairJoin.get(1).get(1) + " " + pairJoin.get(1).get(2) + "\n");
            } catch (Exception ex) {

                Logger.getLogger(BgpExtraction.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else if (Configuration.concurentExec) {

            if (mapGroundTruthPairsLDF.get(pairJoin) != null) {

                if (mapPairJoinToOccurs.get(pairJoin) == null) {

                    mapPairJoinToOccurs.put(pairJoin, 1);
                    mapPairJoinToOccurs.put(symJoin, 1);
                    truePositivesPairs++;
                } else if (mapPairJoinToOccurs.get(pairJoin) < mapGroundTruthPairsLDF.get(pairJoin)) {

                    truePositivesPairs++;
                    int occurs1 = mapPairJoinToOccurs.get(pairJoin);
                    int occurs2 = mapPairJoinToOccurs.get(symJoin);
                    mapPairJoinToOccurs.put(pairJoin, occurs1 + 1);
                    mapPairJoinToOccurs.put(symJoin, occurs2 + 1);
                }

            }

            totalPairs++;
        }
    }

    /**
     * Check if the current DTP can be added in a existing BGP
     *
     * @param allBGPs list of all deduced BGPs
     * @param currDTPid current DTP's id
     * @param mapIDToDTP map of DTPs and their ids
     * @param mapDTPtoBGP map of DTPs to their BGPs
     * @return true if current DTP has added in an existing BGP
     */
    private boolean addInExistBGP(List<List<Integer>> allBGPs, int currDTPid,
            HashMap< Integer, List<String>> mapIDToDTP, HashMap<Integer, Integer> mapDTPtoBGP) {

        boolean flagFoundGraph = false;
        int tmpIDquery = -1;
        List<Integer> matchedGraphs = new LinkedList<>();

        //For every deduced graph
        for (int i = 0; i < allBGPs.size(); i++) {

            flagFoundGraph = false;

            //From the last to the first graph's dediced LDF id
            for (int j = allBGPs.get(i).size() - 1; j >= 0; j--) {

                if (flagFoundGraph) {
                    break;
                }

                //If streaming option is not enabled, we add the graph 
                //to the first matching graph
                tmpIDquery = allBGPs.get(i).get(j);

                if (tmpIDquery != currDTPid && !Objects.equals(mapDTPtoBGP.get(currDTPid), mapDTPtoBGP.get(tmpIDquery))) {

                    if (testNestedLoop(mapIDToDTP.get(currDTPid), currDTPid, mapIDToDTP.get(tmpIDquery), tmpIDquery)) {

                        matchedGraphs.add(i);
                        flagFoundGraph = true;
                    }
                }

            }

        }

        //For all matched graphs, merged to the first all deduced IDs of all the others
        for (int h = 0; h < matchedGraphs.size(); h++) {

            if (h == 0) {

                if (!Objects.equals(mapDTPtoBGP.get(currDTPid), matchedGraphs.get(0))) {
                    allBGPs.get(matchedGraphs.get(h)).add(currDTPid);
                    mapDTPtoBGP.put(currDTPid, matchedGraphs.get(h));
                    flagFoundGraph = true;
                }

            } else {

                int k = matchedGraphs.get(h);
                allBGPs.get(matchedGraphs.get(0)).addAll(allBGPs.get(k));
            }
        }

        //Then, for all matched graphs, delete all graphs except the first one
        if (matchedGraphs.size() > 1) {
            for (int h = 1; h < matchedGraphs.size(); h++) {

                int k = matchedGraphs.get(h);
                allBGPs.remove(k);
            }
        }

        //reset for all deduced IDs, the coresponding map concerning the deduced graph
        if (matchedGraphs.size() > 0) {
            for (int i = 0; i < allBGPs.size(); i++) {

                for (int j = 0; j < allBGPs.get(i).size(); j++) {

                    mapDTPtoBGP.put((allBGPs.get(i).get(j)), i);
                }
            }
        }

        return flagFoundGraph;
    }

    /**
     * Print each BGPs with their DTP's ids, into a specific format
     *
     * @param allBGPs all deduced graphs to be printed
     */
    private void printBgpSets(List<List<Integer>> allBGPs) {

        int size = 0;

        for (int i = 0; i < allBGPs.size(); i++) {

            size = 0;
            Collections.sort(allBGPs.get(i));
            System.out.println("\t\t\t\t\t\t\t Deduced Graph No " + (i + 1));
            System.out.print("\t\t\t\t\t\t\t [ ");

            for (int j = 0; j < allBGPs.get(i).size(); j++) {

                if (j != allBGPs.get(i).size() - 1) {

                    System.out.print(allBGPs.get(i).get(j) + ", ");
                } else {

                    System.out.print(allBGPs.get(i).get(j));
                }

                size += 2 + allBGPs.get(i).get(j).toString().length();

                if (size > 100) {

                    size = 0;
                    System.out.print("\n\t\t\t\t\t\t\t  ");
                }
            }

            System.out.print(" ]\n");
        }

        System.out.println();
    }

    /**
     * Show all BGPs and their triple patterns, both size>1 or size=1
     *
     * @param allBGPs all deduced BGPs with DTP's serial ids
     * @param mapIDtoDTP map of DTP with their serial ids
     */
    private void showAllBGPs(List<List<Integer>> allBGPs, HashMap< Integer, List<String>> mapIDtoDTP) {

        List<String> currDTP = null;
        int count = 1;

        try {

            if (Configuration.isolatedExec) {

                bufferWritterJoins.close();
            }

            bufferWritterBGPs.write("\t----------Deduced BGPs----------\n\n");
            BasicUtilis.printInfo("\t----------Deduced BGPs----------\n");

            //Show all deduced BGPs of size > 1
            for (int i = 0; i < allBGPs.size(); i++) {

                if (allBGPs.get(i).size() > 1) {

                    bufferWritterBGPs.write("\t\t BGP [no" + (i + 1) + "] + \n");
                    BasicUtilis.printInfo("\t\t BGP [no" + (i + 1) + "]");

                    for (Integer currDTPid : mapIDtoDTP.keySet()) {

                        if (BasicUtilis.elemInListEquals(allBGPs.get(i), currDTPid)) {
           
                            currDTP = mapIDtoDTP.get(currDTPid);

                                             
                                              
                if (currDTP.get(0).contains("POSSIBLY")||currDTP.get(1).contains("POSSIBLY")||currDTP.get(2).contains("POSSIBLY")) {

                    continue;
                }

                            bufferWritterBGPs.write("\t\t\t Deduced LDF_" + currDTPid + ": "
                                    + currDTP.get(0) + "     " + currDTP.get(1) + "     " + currDTP.get(2) + "\n");
                            BasicUtilis.printInfo("\t\t\t Deduced LDF_" + currDTPid + ": "
                                    + currDTP.get(0) + "     " + currDTP.get(1) + "     " + currDTP.get(2));
                            bufferWritterBGPs.write("\t\t\t\t received @" + NestedLoopDetection.mapDTPoLDFServer.get(currDTPid) + "\n");
                            BasicUtilis.printInfo("\t\t\t\t received @" + NestedLoopDetection.mapDTPoLDFServer.get(currDTPid));
                        }
                    }
                }

            }

            bufferWritterBGPs.write("\n\t----------Deduced BGPs----------\n" + "\n");
            BasicUtilis.printInfo("\n\t----------Deduced BGPs----------\n");

            bufferWritterBGPs.write("\t----------Single triple patterns (not participating in any Nested Loop)----------\n" + "\n");
            BasicUtilis.printInfo("\t----------Single triple patterns (not participating in any Nested Loop)----------\n");

            //Get all BGPs of size = 1 
            for (int l = 0; l < allBGPs.size(); l++) {

                if (allBGPs.get(l).size() == 1) {

                    for (Integer curLDF : mapIDtoDTP.keySet()) {

                        if (BasicUtilis.elemInListEquals(allBGPs.get(l), curLDF)) {
                            
                if (currDTP.get(0).contains("POSSIBLY")||currDTP.get(1).contains("POSSIBLY")||currDTP.get(2).contains("POSSIBLY")) {

                    continue;
                }
                            currDTP = mapIDtoDTP.get(curLDF);

                            bufferWritterBGPs.write("\t\t\t  Deduced LDF_" + count + ": "
                                    + currDTP.get(0) + "    " + currDTP.get(1) + "     " + currDTP.get(2) + "\n");
                            System.out.println("\t\t\t  Deduced LDF_" + count + ": "
                                    + currDTP.get(0) + "    " + currDTP.get(1) + "     " + currDTP.get(2));

                            bufferWritterBGPs.write("\t\t\t\t  received @" + CtpExtraction.mapCtpToLDFServer.get(curLDF) + "\n");
                            System.out.println("\t\t\t\t  received @" + CtpExtraction.mapCtpToLDFServer.get(curLDF));
                            count++;
                        }

                    }
                }

            }

            bufferWritterBGPs.write("\n\t----------Single triple patterns (not participating in any Nested Loop)----------\n\n" + "\n");
            BasicUtilis.printInfo("\n\t----------Single triple patterns (not participating in any Nested Loop)----------\n");

            bufferWritterBGPs.close();
            writerBGPs.close();
        } catch (Exception ex) {

            Logger.getLogger(BgpExtraction.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Get ground truth pair joins, and their occurances, from input file
     *
     */
    private void getGroundTruthPairs() {

        String fileName = "groundTruth.txt";
        BufferedReader br = null;
        String sCurrentLine;
        List<String> outerTP = new LinkedList<>();
        List<String> innerTP = new LinkedList<>();
        List<List<String>> currJoin = new LinkedList<>();
        List<List<String>> symJoin = new LinkedList<>();
        int cntPairs = 0;
        int pairOccurs = 0;
        int symmOccurs = 0;

        BasicUtilis.printInfo("******* START: Ground Truth  pair joins ********");
        try {

            try {

                br = new BufferedReader(new FileReader(fileName));

                while ((sCurrentLine = br.readLine()) != null) {

                    String s = sCurrentLine;
                    String[] pairString = s.split(", ");
                    outerTP = new LinkedList<>();
                    innerTP = new LinkedList<>();

                    //For each pair join in the form of a string
                    for (String currPairStr : pairString) {

                        // split it in two triple pattern joins
                        String[] tpString = currPairStr.split(" ");

                        // and for each triple pattern in form of string, identify each entity
                        for (String currEntity : tpString) {

                            //remove "comma" character
                            if (currEntity.contains(",")) {

                                currEntity = currEntity.substring(0, currEntity.indexOf(","));
                            }

                            //replace special character of "_" as "space" character
                            if (currEntity.contains("_") && !currEntity.contains("http") && !currEntity.contains("predicate")) {

                                currEntity = currEntity.replace("_", " ");
                            }

                            if (outerTP.size() < 3) {

                                outerTP.add(currEntity);
                            } else if (innerTP.size() < 3) {

                                innerTP.add(currEntity);
                            }

                        }
                    }

                    currJoin = Arrays.asList(innerTP, outerTP);
                    symJoin = Arrays.asList(outerTP, innerTP);

                    //identify current ground truth join pair (and its symmetric)
                    if (mapGroundTruthPairsLDF.get(currJoin) == null && mapGroundTruthPairsLDF.get(symJoin) == null) {

                        pairOccurs = 1;
                        symmOccurs = 1;
                    } else {

                        pairOccurs = mapGroundTruthPairsLDF.get(currJoin) + 1;
                        symmOccurs = mapGroundTruthPairsLDF.get(symJoin) + 1;
                    }

                    mapGroundTruthPairsLDF.put(currJoin, pairOccurs);
                    mapGroundTruthPairsLDF.put(symJoin, symmOccurs);
                    cntPairs++;
                    BasicUtilis.printInfo("\t Join pair no[" + cntPairs + "]: " + currJoin);

                }
            } catch (Exception ex) {

                Logger.getLogger(BasicUtilis.class.getName()).log(Level.SEVERE, null, ex);
            }
        } finally {

            if (br != null) {

                try {

                    br.close();
                } catch (Exception ex) {

                    Logger.getLogger(BgpExtraction.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

        }

        BasicUtilis.printInfo("******* FINISH: Ground Truth  pair joins ********");
    }

    /**
     * Calculate recall/recall of ground truth pairs, for concurrent execution
     *
     */
    private void generateGNUFinal() {

        int groundTruthPairs = 0;
        float precision = 0;
        float recall = 0;
        String outString = "";

        for (List<List<String>> curkey : mapGroundTruthPairsLDF.keySet()) {

            groundTruthPairs += mapGroundTruthPairsLDF.get(curkey);
        }

        groundTruthPairs = groundTruthPairs / 2;

        missedGroundTruthPairs();

        if (!Configuration.isolatedExec) {

            BasicUtilis.printInfo("**************************** START: LIFT statistics: *************************");

            BasicUtilis.printInfo("\t [a] All different BGPs: " + numBGP + "\n");
            BasicUtilis.printInfo("\t [b] All different pairs: " + totalPairs + "\n");
            BasicUtilis.printInfo("\t \t true positives pairs: " + truePositivesPairs + "\n");

            precision = ((float) truePositivesPairs) / totalPairs;
            outString = String.format("%.2f", precision);
            BasicUtilis.printInfo("\t [1] Precision in deduced pairJoins: " + outString + "\n");

            recall = ((float) truePositivesPairs) / groundTruthPairs;
            outString = String.format("%.2f", recall);
            BasicUtilis.printInfo("\t [2] Recall in deduced pairJoins: " + outString + "\n");

            BasicUtilis.printInfo("**************************** FINISH: LIFT statistics: *************************");
        }

    }

    /**
     * Show missed ground truth pair joins
     */
    private void missedGroundTruthPairs() {

        BasicUtilis.printInfo("****************************Pair joins missed *************************");

        for (List<List<String>> currPair : mapGroundTruthPairsLDF.keySet()) {

            if (mapPairJoinToOccurs.get(currPair) == null) {

                BasicUtilis.printInfo("\t missed pair " + currPair + "\n");
            }

        }

        BasicUtilis.printInfo("****************************Pair joins missed *************************");
    }

}