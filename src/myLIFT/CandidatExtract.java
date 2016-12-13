package myLIFT;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import static myLIFT.Main.LDFgraphReduction;
import static myLIFT.Main.nameDB;
import static myLIFT.Main.simpleExecution;
import static myLIFT.Main.webInsTraces;
import static myLIFT.Main.windowJoin;

/**
 *
 * @author root
 */
public class CandidatExtract {

    BasicUtilis myBasUtils;

    /* General hash/set maps used in the code */
    // save all possible predicates used by current LDF servers' set  
    public static HashMap<String, Integer> mapLDFpredicates;
    // save all occurances of each constant (IRI/literal)
    public static HashMap<String, Integer> mapAnswerToOccurs;
    // match each predicate IRI's authority to the corresponding prefix
    public static HashMap<String, String> mapAuthorityToPrefix;
    // match each predicate complite IRI  into its authority
    public static HashMap<String, String> mapPrefixToAuthority;

    /* Candidate LDF - oriented hash/set maps used in the code */
    // match each candidate LDF, to its serial id (order of identification)
    public static HashMap<List<String>, Integer> mapCandLDFToSerialID;
    // match each candidate LDF, to corresponding log entries
    public static HashMap<List<String>, List<Integer>> mapCandLDFToLogEntries;
    // match each candidate LDF, to LDF servers that recieved it
    public static HashMap<Integer, List<String>> mapCandLDFToLDFServer;
    // match each candidate LDF, to corresponding (possible) injected values
    public static HashMap<Integer, List<String>> mapCandLDFToInjectVals;
    // match each candidate LDF, to corresponding answers' values (i.e., mappings)
    public static HashMap<Integer, List<String>> mapCandLDFToAnswerOrig;
    // match each deduced LDF, to all its corresponding timeSecs(total number of secs)
    public static HashMap<Integer, List<Integer>> mapCandDFToTimeSecs;
    // match a candidate LDF, with all different versions identified, when  
    // window join (i.e., gap) is not big enough to put them in only one
    public static HashMap<List<String>, Integer> mapCandLDFToVersionsTjoin;

    /*  Deduced LDF - oriented hash/set maps used in the code */
    // match each deduced LDF, to its serial id (order of deduction)
    public static HashMap<Integer, List<String>> mapDedLDFToSerialID;
    // match each deduced LDF, to LDF servers that recieved it
    public static HashMap<Integer, List<String>> mapDedLDFToLDFServer;
    // match each deduced "membership-type" LDF to its corresponding variable of
    //"service-type" mappings-answers, that are injected to another inner LDF 
    public static HashMap<String, List<String>> mapDedLDFToServiceLDFAns;
    // match each deduced "membership-type" LDF to its corresponding "signature"
    // i.e., the variable of coresponding "service-type" LDF to which answers 
    // can be found in hashMap "mapDedLDFToServiceLDFAns"
    public static HashMap<Integer, String> mapDedLDFToServiceSignature;
    // save all LDF servers not participating in nested loop 
    // (i.e., service LDF and deduced LDF of UNION-like queres)
    public static HashMap<List<String>, Integer> mapDedLDFNotInNESTED;
    // match each deduced LDF not participating in a nested loop, to LDF servers 
    // that recieved it
    public static HashMap<List<String>, List<String>> mapDedLDFNotInNESTEDServer;
    // match each deduced LDF, to its corresponding BGP
    public static HashMap<Integer, Integer> mapDedLDFToDedGraph;

    /* Precision/recall-oriented hash/set maps used in the code */
    // List of ground truth pair-joins and their occurances
    public static Map<List<List<String>>, Integer> mapGroundTruthPairsLDF;
    // List of identified ground truth pair-joins
    public static Map<List<List<String>>, Integer> mapPairJoinToOccurs;
    // total pairs identified
    public static int totalPairs;
    // true positives identified
    public static int truePositivesPairs;
    // BGPs identified
    public static int numBGP;

    public static HashMap<List<String>, Double> mapDeducdLDFToCondifence;
    public static HashMap<List<String>, String> mapDeducdLDFToPushedPercentage;

    public static HashMap<String, Integer> mapConstToAnsOccurs;

    // match each candidate LDF, to corresponding (possible) injected values
    public static HashMap<Integer, List<String>> mapCandLDFToInjectValsSUbject;
    public static HashMap<Integer, List<String>> mapCandLDFToInjectValsObject;

    FileWriter writer;
    BufferedWriter bufferWritter;
    FileWriter writerBGP;
    BufferedWriter bufferWritterBGP;

    public CandidatExtract() throws IOException {

        myBasUtils = new BasicUtilis();

        mapConstToAnsOccurs = new HashMap<>();
        mapAnswerToOccurs = new HashMap<>();
        mapAuthorityToPrefix = new HashMap<>();
        mapPrefixToAuthority = new HashMap<>();
        mapLDFpredicates = new HashMap<>();

        mapCandLDFToAnswerOrig = new HashMap<>();
        mapCandLDFToSerialID = new HashMap<>();
        mapCandLDFToInjectVals = new HashMap<>();
        mapCandLDFToLDFServer = new HashMap<>();
        mapCandLDFToLogEntries = new HashMap<>();
        mapCandLDFToVersionsTjoin = new HashMap<>();

        mapDedLDFToSerialID = new HashMap<>();
        mapDedLDFToLDFServer = new HashMap<>();
        mapDedLDFToServiceLDFAns = new HashMap<>();
        mapDedLDFToServiceSignature = new HashMap<>();
        mapDedLDFNotInNESTED = new HashMap<>();
        mapDedLDFNotInNESTEDServer = new HashMap<>();

        mapDedLDFToDedGraph = new HashMap<>();
        mapCandDFToTimeSecs = new HashMap<>();

        mapGroundTruthPairsLDF = new HashMap<>();
        mapPairJoinToOccurs = new HashMap<>();

        mapDeducdLDFToCondifence = new HashMap<>();
        mapDeducdLDFToPushedPercentage = new HashMap<>();

        mapCandLDFToInjectValsSUbject = new HashMap<>();
        mapCandLDFToInjectValsObject = new HashMap<>();

        totalPairs = 0;
        truePositivesPairs = 0;
        numBGP = 0;

        //  if(singl)
        {
        }

        String reduction = "";

        if (LDFgraphReduction) {
            reduction = "_with_graphReduction";
        } else {

            reduction = "_no_graphReduction";
        }

        writer = new FileWriter("truePositivePairs" + nameDB + reduction + ".txt", false);
        bufferWritter = new BufferedWriter(writer);

        writerBGP = new FileWriter("deducedBGPs/deducedBGPs_" + nameDB + "_gap=" + windowJoin + reduction + ".txt", false);
        bufferWritterBGP = new BufferedWriter(writerBGP);
    }

    /**
     * Parse information of the current log entry and match it to the
     * correspoding (existing or new) LDF Candidate.
     *
     * @param logEntryID log entry id, to be parsed
     * @param querySelector current query/selector
     * @param selectorAns complete answer fragment in string format
     * @param injectionString json string, containing all injected vars and vals
     * @param LDFserver current LDF server responding to query/selector
     * @param timeStamp time in total number of seconds, of current log entry
     * @throws java.io.UnsupportedEncodingException
     */
    public void setEntryToLDFCand(int logEntryID, String querySelector, String selectorAns,
            String injectionString, String LDFserver, int timeStamp) throws UnsupportedEncodingException {

        List<String> refinedLDFCand = null;
        List<String> singleUnitySelector = null;
        List<String> allDistAns = null;
        List<String> allVars = null;
        List<String> selectorUnities = null;
        List<String> allInjectVals = null;

        if (querySelector.contains("A_River_Runs_Throug") && querySelector.contains("director")) {
            int azrzar = 0;
        }

        if (querySelector.contains("Brad Pitt")) {
            int azrzar = 0;
        }

        if (selectorAns.contains("Across")) {
            int azrzar = 0;
        }
                          //     http://dbpedia.org/resource/Florence_Airport,_Peretola 

        /*      if (querySelector.contains("Artist")) {
         int azrzar = 0;
         }

         if (querySelector.contains("Brad Pitt")) {
         int azrzar = 0;
         }
           
         if (querySelector.contains("Angelina Jolie")) {
         int azrzar = 0;
         }
      
             
         if (querySelector.contains("dbpedia-owl:Artist")) {
         int azrzar = 0;
         }
                  
           
         if (querySelector.contains("dbpprop:starring")) {
         int azrzar = 0;
         }
            
         if(querySelector.endsWith("/2014/en?subject=")) {
         int azraz=0;
         }    */
        // BUUUUUUUUUUUUUUUUG
        if (querySelector.contains("subject") || querySelector.contains("predicate") || querySelector.contains("object")) {

        //Get all selector's variables (subject, predicate or/and object)
            //   List<String> allVars = getVarsFromSelector(injectionString);
            allVars = getVarsFromSelectorBis(querySelector);

    //   System.out.println("***********************");
    //   System.out.println("VARS: " + allVars);
            //Get all selector unities (concerning to subject, predicate or/and object)
            //  List<String> selectorUnities = getUnitiesFromSelector(querySelector, allVars);
            selectorUnities = getUnitiesFromSelectorBis(querySelector);

     // System.out.println("hhhhhhhhhhhhhhhhhhhhhhhhhh: "+querySelector);
    //  System.out.println("HAAAAAAAAAAAAAAAAAAAAAA: "+selectorUnities);
            //  System.out.println("Unities: "+selectorUnities);
            //Get, if there exist, list of injected values in the selector
            allInjectVals = getInjectedFromSelectorBis(allVars, selectorUnities);
                     //   System.out.println("Injected vals: " + allInjectVals);

            allInjectVals = addAllShortIRIs(allInjectVals);

        // BUUUUUUUUUUUUUUUUUUUUUG
            if ((allInjectVals.size() != 0 && allVars.size() != 0) || !querySelector.contains("?subject=&predicate=&object=")) {
                if (querySelector.contains("Mrs._Smith") || querySelector.contains("Louise")) {
                    int qsdqdqs = 0;

                }

        //Get all distinct answers from selector string answer
                if (webInsTraces) {

                    allDistAns = getDistAnsFromSelector(selectorAns, allInjectVals, selectorUnities);
                } else {

                    allDistAns = getDistAnsFromSelectorBis(querySelector, allInjectVals, selectorAns);
                }
        //

                if (myBasUtils.elemInListContained(allInjectVals, "http://dbpedia.org/ontology/Artist") && allVars.size() >= 3) {

                    int ids = 0;
                }

                //Get corespondig (new or existing) LDF candidate from current selector
                if (selectorUnities.size() == 1) {

                    //Get, in this case, the single unity selector (only predicate, subject or object)
                    singleUnitySelector = getSingleUnitySelector(selectorUnities, allVars);
            // System.out.println("xxxxxxxxxxXXXX: "+querySelector);
                    //System.out.println("FFFFFFFFFFFFFF: "+singleUnitySelector);
                    refinedLDFCand = new LinkedList<>(singleUnitySelector.subList(0, 3));
                } else {
            //or get LDF candidate from selector with 2 or 3 unities
                    //   refinedLDFCand = getRefinedCandLDF(allVars, selectorUnities, allInjectVals);
                    //            refinedLDFCand = getRefinedCandLDF(allVars, selectorUnities, allInjectVals);
                    refinedLDFCand = getRefinedCandLDFBis(allVars, selectorUnities, allInjectVals);
                }

      //  System.out.println("Selector: " + querySelector);  
                //  System.out.println("Candidate LDF: "+refinedLDFCand);
                //  System.out.println("THe whole fragment: " + selectorAns);
                //   System.out.println("Injected values: " + allInjectVals);
                if (querySelector.contains("Mrs._Smith") || querySelector.contains("Louise")) {
                    // System.out.println("Only Answers: " + allDistAns);            
                }

       // System.out.println("Only Answers: " + allDistAns);
                if (refinedLDFCand.size() >= 2 && refinedLDFCand.get(1).contains("http://dbpedia.org/ontology/birthPlace")
                        && !refinedLDFCand.get(2).contains("object")) {
                    int haha = 0;
                }

                if (refinedLDFCand.size() == 2) {
                    int haha = 0;
                }
                /*   for(int i=0; i<allInjectVals.size();i++){
                 if(mapAnswerToOccurs.get(allInjectVals.get(i))!=null){
                 int azeaze =mapAnswerToOccurs.get(allInjectVals.get(i));
                 }

                 }*/

                if (myBasUtils.elemInListContained(allDistAns, "http://dbpedia.org/resource/Alcobendas_CF")) {
                    int azaz = 0;
                }
                if (myBasUtils.elemInListContained(allDistAns, "Alcobendas_CF")) {
                    int azaz = 0;
                }

                // Set or update, new LDF candidate info
                setLDFCandHashInfo(refinedLDFCand, allDistAns, allInjectVals, logEntryID, LDFserver, timeStamp, selectorUnities);

            }
        }

        //show information of current captured entry
        // showDetailedEntryInfo(logEntryID, allVars, allInjectVals, allDistAns, querySelector, selectorAns);
    }

    /**
     * Get all variables, from the json string contaning all selector's
     * variables and unities. This function also returns the "key-word"
     * ?predicate which corresponds to the LDF's predicate, if there exist.
     *
     * @param injectionString json string, containing injected vars and vals
     * @return list of all selector's variables
     */
    public List<String> getVarsFromSelector(String injectionString) {

        List<String> listVars = new LinkedList<>();
        String tmpVar = "";
        String tmpAnsw = "";

        //each varable's name begin after key-word "name"
        if (injectionString.contains("name")) {

            tmpAnsw = injectionString;
            for (int i = 0; i < tmpAnsw.length(); i++) {
                if (tmpAnsw.indexOf("\"name\": \"") > 0) {

                    tmpAnsw = tmpAnsw.substring(tmpAnsw.indexOf("\"name\": \"") + 9);
                    tmpVar = tmpAnsw.substring(0, tmpAnsw.indexOf("\""));

                    /* BUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUG page */
                    //must capture it somme how....
                    if (!tmpVar.equalsIgnoreCase("page")) {

                        listVars.add(tmpVar);
                    }

                }

                if (!tmpAnsw.contains("\"name\": \"")) {

                    break;
                }

            }

        }

        return listVars;
    }

    public List<String> getVarsFromSelectorBis(String injectionString) {

        List<String> listVars = new LinkedList<>();
        String tmpVar = "";
        String tmpAnsw = "";
        String injectionStringShort = injectionString;

        if (injectionString.contains("&page")) {
            injectionStringShort = injectionString.substring(0, injectionString.indexOf("&page"));
        }

        //each varable's name begin after key-word "name"
        if (injectionStringShort.contains("subject=")) {

            if (injectionStringShort.contains("&predicate")) {

                tmpVar = injectionStringShort.substring(injectionStringShort.indexOf("subject=") + 8, injectionStringShort.indexOf("&predicate"));
                if (!tmpVar.equalsIgnoreCase("")) {

                    listVars.add("subject");
                }
            } else {
                tmpVar = injectionStringShort.substring(injectionStringShort.indexOf("subject=") + 8);
                if (!tmpVar.equalsIgnoreCase("")) {

                    listVars.add("subject");
                }
            }
        }

        if (injectionStringShort.contains("predicate=")) {

            if (injectionStringShort.contains("&object")) {

                tmpVar = injectionStringShort.substring(injectionStringShort.indexOf("predicate=") + 10, injectionStringShort.indexOf("&object"));
                if (!tmpVar.equalsIgnoreCase("")) {

                    listVars.add("predicate");
                }
            } else {
                tmpVar = injectionStringShort.substring(injectionStringShort.indexOf("predicate=") + 10);
                if (!tmpVar.equalsIgnoreCase("")) {

                    listVars.add("predicate");
                }
            }
        }

        if (injectionStringShort.contains("object=")) {

            tmpVar = injectionStringShort.substring(injectionStringShort.indexOf("object=") + 7);
            if (!tmpVar.equalsIgnoreCase("")) {

                listVars.add("object");
            }

        }

        return listVars;
    }

    /**
     * Get all variables, from the json string contaning all selector's
     * variables and unities. This function also returns the "key-word"
     * ?predicate which corresponds to the LDF's predicate, if there exist.
     *
     * @param injectionString json string, containing injected vars and vals
     * @return list of all selector's variables
     */
    public List<String> getUnitiesFromSelectorBis(String injectionString) throws UnsupportedEncodingException {

        List<String> listVars = new LinkedList<>();
        String tmpVar = "";
        String tmpAnsw = "";
        String injectionStringShort = injectionString;

        if (injectionString.contains("&page")) {
            injectionStringShort = injectionString.substring(0, injectionString.indexOf("&page"));
        }

        //each varable's name begin after key-word "name"
        if (injectionStringShort.contains("subject=")) {

            if (injectionStringShort.contains("&predicate")) {

                tmpVar = injectionStringShort.substring(injectionStringShort.indexOf("subject=") + 8, injectionStringShort.indexOf("&predicate"));
                if (!tmpVar.equalsIgnoreCase("")) {

                    if (tmpVar.contains(":") && !tmpVar.contains("http")) {

                        listVars.add(URLDecoder.decode(getCompleteIRI(tmpVar), "UTF-8"));
                    } else {

                        // listVars.add(URLDecoder.decode(tmpVar, "UTF-8"));
                        listVars.add(tmpVar);
                    }

                }
            } else {
                tmpVar = injectionStringShort.substring(injectionStringShort.indexOf("subject=") + 8);
                if (!tmpVar.equalsIgnoreCase("")) {

                    if (tmpVar.contains(":") && !tmpVar.contains("http")) {

                        listVars.add(URLDecoder.decode(getCompleteIRI(tmpVar), "UTF-8"));
                    } else {

                        // listVars.add(URLDecoder.decode(tmpVar, "UTF-8"));
                        listVars.add(tmpVar);

                    }

                }
            }
        }

        if (injectionStringShort.contains("predicate=")) {

            if (injectionStringShort.contains("&object")) {

                tmpVar = injectionStringShort.substring(injectionStringShort.indexOf("predicate=") + 10, injectionStringShort.indexOf("&object"));
                if (!tmpVar.equalsIgnoreCase("")) {

                    if (tmpVar.contains(":") && !tmpVar.contains("http")) {

                        listVars.add(URLDecoder.decode(getCompleteIRI(tmpVar), "UTF-8"));
                    } else {

                //  listVars.add(URLDecoder.decode(tmpVar, "UTF-8"));
                        listVars.add(tmpVar);

                    }
                }
            } else {
                tmpVar = injectionStringShort.substring(injectionStringShort.indexOf("predicate=") + 10);
                if (!tmpVar.equalsIgnoreCase("")) {

                    if (tmpVar.contains(":") && !tmpVar.contains("http")) {

                        listVars.add(URLDecoder.decode(getCompleteIRI(tmpVar), "UTF-8"));
                    } else {

                        //  listVars.add(URLDecoder.decode(tmpVar, "UTF-8"));
                        listVars.add(tmpVar);

                    }
                }
            }
        }

        if (injectionStringShort.contains("object=")) {

            tmpVar = injectionStringShort.substring(injectionString.indexOf("object=") + 7);
            if (!tmpVar.equalsIgnoreCase("")) {

                if (tmpVar.contains(":") && !tmpVar.contains("http")) {

                    listVars.add(URLDecoder.decode(getCompleteIRI(tmpVar), "UTF-8"));
                } else {

                    // listVars.add(URLDecoder.decode(tmpVar, "UTF-8"));
                    listVars.add(tmpVar);

                }
            }

        }

        /*BUUUUUUUUUUUUUUUUUUUG*/
        List<String> listVarsFinal = new LinkedList<>();

        for (int i = 0; i < listVars.size(); i++) {

            String tmp = listVars.get(i);
            tmp = tmp.replaceAll(",_", "_");
            tmp = tmp.replaceAll("__", "_");
            listVarsFinal.add(tmp);
        }

        return listVarsFinal;
    }

    /**
     * Get all selector's unities (i.e.,IRIs or Literals), concerning predicate,
     * subject and/or object
     *
     * @param querySelector query/selector in string format
     * @param allVars all variables present in the query/selector
     * @return list of query/selector's unities
     */
    public List<String> getUnitiesFromSelector(String querySelector, List<String> allVars) {

        List<String> queryElements = new LinkedList<>();
        String currElements = "";

        //remove "page" subset string, from querySelector string 
        if (querySelector.indexOf("page=") > 0) {

            querySelector = querySelector.substring(0, querySelector.indexOf("page="));
        }

        if (allVars != null) {

            for (int i = 0; i < allVars.size(); i++) {

                currElements = "";
                //for every selector's variable, find the the corresponding value
                if (i < allVars.size() - 1) {

                    currElements = querySelector.substring(querySelector.indexOf(allVars.get(i)) + allVars.get(i).length() + 1,
                            querySelector.indexOf(allVars.get(i + 1)));
                } else {

                    currElements = querySelector.substring(querySelector.indexOf(allVars.get(i)) + allVars.get(i).length() + 1);
                }

                currElements = currElements.replaceAll(",", "");
                currElements = currElements.replaceAll("\"", "");

                queryElements.add(currElements);
            }
        }

        queryElements = myBasUtils.refineList(queryElements);

        return queryElements;
    }

    /**
     * Get all injected values of the selector, concerning an "subject" or/and
     * "object" or both of them (i.e., for membership selectors)
     *
     * @param allVars all query/selector's variables
     * @param querySelectorUnities list of query/selector's unities
     * @return all query/selector's injected vals
     */
    public List<String> getInjectedFromSelector(List<String> allVars, List<String> querySelectorUnities) {

        String currInjectValue = "";
        List<String> injectVals = new LinkedList<>();

        for (int k = 0; k < allVars.size(); k++) {

            if (!allVars.get(k).contains("predicate") && allVars.size() > 1) {

                currInjectValue = querySelectorUnities.get(k);
                /*BUUUUUUUUUUUUUUUUUUG*/
                if (querySelectorUnities.get(k).contains("http")) {

                    currInjectValue = currInjectValue.replaceAll(",", "");
                }

                //check that current selector unity is previously returned as an 
                //answer in two cases:
                // (a) if its an IRI and neither detailed or its shorten value
                // (b) if its an Literal, it current value
                if (!(mapAnswerToOccurs.get(currInjectValue) == null
                        && (!currInjectValue.contains("http")
                        || (mapAnswerToOccurs.get(getShortIRI(currInjectValue)) == null)))) {

                    injectVals.add(currInjectValue);
                }

            }
        }

        return injectVals;
    }

    public List<String> getInjectedFromSelectorBis(List<String> allVars, List<String> querySelectorUnities) {

        String currInjectValue = "";

        List<String> injectVals = new LinkedList<>();

        boolean found = false;

        for (int k = 0; k < allVars.size(); k++) {

            if (!allVars.get(k).contains("predicate")) {

                found = false;

                currInjectValue = querySelectorUnities.get(k);
                currInjectValue = currInjectValue.replaceAll(",_", "_");
                currInjectValue = currInjectValue.replaceAll("__", "_");

                if (currInjectValue.contains("http://dbpedia.org/resource/Alcobendas_CF")) {

                    int qsdqsd = 0;
                }

            //     System.out.println(mapConstToAnsOccurs);
                for (String key : mapConstToAnsOccurs.keySet()) {

                    if (key.equalsIgnoreCase(currInjectValue)) {
                        found = true;
                        break;
                    }
                }

                if (currInjectValue.contains("well-known") || currInjectValue.contains("google") || currInjectValue.contains("#person")) {
                    found = true;
                }
                if (!found && mapConstToAnsOccurs.get(currInjectValue) == null) {

                    continue;
                } else if (!found) {

                    continue;
                } /*  if(currInjectValue.contains("http://dbpedia.org/ontology/Airport")||currInjectValue.contains("http://dbpedia.org/ontology/Artist")
                 ||currInjectValue.contains("http://dbpedia.org/ontology/Award")||currInjectValue.contains("http://dbpedia.org/class/yago/Carpenters")
                 ||currInjectValue.contains("http://dbpedia.org/resource/Category:American_male_film_actors")
                 ||currInjectValue.contains("http://dbpedia.org/resource/Plant")||currInjectValue.contains("http://dbpedia.org/ontology/Device")
                 ||currInjectValue.contains("http://dbpedia.org/ontology/Event")||currInjectValue.contains("Belgium")
                 ||currInjectValue.contains("http://dbpedia.org/ontology/Settlement")||currInjectValue.contains("http://dbpedia.org/ontology/SoccerClub")
                 ||currInjectValue.contains("http://dbpedia.org/ontology/MythologicalFigure")||currInjectValue.contains("http://www.w3.org/2000/01/rdf-schema#Class")
                 ||currInjectValue.contains("http://www.w3.org/2002/07/owl#Ontology") ||currInjectValue.contains("http://xmlns.com/foaf/0.1/Person")
                 ||currInjectValue.contains("Jansen")||currInjectValue.contains("Janssen")||currInjectValue.contains("Jansen")||currInjectValue.contains("Etienne Vermeersch")){
                    
                    
                 
                 // Jansen Janssen
                 continue;
                 }*/ else {
                    injectVals.add(currInjectValue);
                }

                    //  if(mapAnswerToOccurs.get(currInjectValue)==null){ }
            }
        }

        return injectVals;
    }

    /**
     * Get all distinct answers, of the current selector
     *
     * @param selectorAns query/selector's answer fragment in string format
     * @param injectedVal list of injected values of the currrent query/selector
     * @param querySelectorUnities list of query/selector's unities
     * @return list of all query/selector's distinct answers
     */
    public List<String> getDistAnsFromSelector(String selectorAns, List<String> injectedVal, List<String> querySelectorUnities) {

        List<String> allTPunities = new LinkedList<>();
        List<String> allTPunitiesFinal = new LinkedList<>();
        List<String> allSelectorUnities = null;
        String ansAsTP = "", tmpFullAns = "";

        //find selector's answer string subset, containing the answers
        if (selectorAns.indexOf("hydra:property rdf:object\\n}") > 0) {

            tmpFullAns = selectorAns.substring(selectorAns.indexOf("hydra:property rdf:object\\n}") + 30);

            if (tmpFullAns.indexOf("\\n<http://fragments") > 0) {

                ansAsTP = tmpFullAns.substring(0, tmpFullAns.indexOf("\\n<http://fragments"));
            } else if (tmpFullAns.indexOf("\\n<http://data.linkeddatafragments") > 0) {

                ansAsTP = tmpFullAns.substring(0, tmpFullAns.indexOf("\\n<http://data.linkeddatafragments"));
            }
        }

        if (ansAsTP.contains("Alcobendas_CF")) {

            int qsdqsd = 0;
        }

   //     Florence_Airport,_Peretola
        // System.out.println(ansAsTP);
        allTPunities = getUnitiesFromSelectorAns(ansAsTP);

        //For all IRI answer untities, we check:
        // (a) if the current value, is not the same with the injected values 
        //     (shorten or detailed), then 
        // (b) And only then, for either a IRI or Literal, save this current value
        allSelectorUnities = addAllShortIRIs(querySelectorUnities);
        //     as already identified, for possible injection
        for (int i = 0; i < allTPunities.size(); i++) {

            if (!myBasUtils.elemInListEqualsExact(allSelectorUnities, allTPunities.get(i))
                    && !myBasUtils.elemInListEqualsExact(allSelectorUnities, getShortIRI(allTPunities.get(i)))) {

              //  System.out.println(injectedVal);
                //    System.out.println(allTPunities.get(i));
                //BUUUUUUUUUUUUUUUUUUUUG
                String refString = allTPunities.get(i);
                refString = refString.replace("__", "_&_");
             //   System.out.println(refString);

                //   if(!myBasUtils.elemInListContained(injectedVal, refString))
                {

                    allTPunitiesFinal.add(refString);

                    if (refString.contains("Alcobendas_CF")) {

                        int qsdqsd = 0;
                    }
              //  mapConstToAnsOccurs.put(refString, 1);

                    myBasUtils.insertToMap(mapConstToAnsOccurs, refString);
               //                 System.out.println(mapConstToAnsOccurs);

                    //Identify a short IRI constant  in its detailed form
                    if (!refString.contains("http") && refString.contains(":")) {

                        myBasUtils.insertToMap(mapConstToAnsOccurs, getCompleteIRI(refString));
                        allTPunitiesFinal.add(getCompleteIRI(refString));
                    } else if (refString.contains("http")) {

                        myBasUtils.insertToMap(mapConstToAnsOccurs, getShortIRI(refString));
                        allTPunitiesFinal.add(getShortIRI(refString));

                    }

                    mapAnswerToOccurs.put(refString, 1);
                }

                /*
                 if(!myBasUtils.elemInListContained(injectedVal, allTPunities.get(i))){
                    
                 allTPunitiesFinal.add(allTPunities.get(i));
                 mapAnswerToOccurs.put(allTPunities.get(i), 1);
                 }
                
                 */
            }

        }

        return allTPunitiesFinal;
    }

    public List<String> getDistAnsFromSelectorBis(String selectorQuery, List<String> selectorUnities, String answers) {

        List<String> allTPunities = new LinkedList<>();
        List<String> allTPunitiesFinal = new LinkedList<>();
        List<String> allSelectorUnities = null;
        String tmpFullAns = "";
        String tmp = "";

        if (answers.contains("ld")) {
            int aaze = 0;
        }

        /*        if (answers.contains("[")) {

         String[] arrayEntities = answers.split("]");
             
         if(injectedVal.size()==0){
                
         for (String str : arrayEntities) {
         if (str.contains("[") && str.contains("]")) {

         tmp = str.substring(str.indexOf("[") + 1, str.indexOf("]"));

         } else if (str.contains("[")) {
         tmp = str.substring(str.indexOf("[") + 1);

         }
         String newStaf = getShortIRI(tmp);

         if (!tmp.equalsIgnoreCase("")) {
         if (!myBasUtils.elemInListContained(allTPunitiesFinal, tmp)) {

         allTPunities.add(tmp);
         }

         if (!myBasUtils.elemInListContained(allTPunitiesFinal, newStaf)) {
         allTPunities.add(newStaf);
         }
         }

         }
         }
            
            
         else {
         for (int i = 0; i < injectedVal.size(); i++) {

         for (String str : arrayEntities) {
         if (str.contains("[") && str.contains("]")) {

         tmp = str.substring(str.indexOf("[") + 1, str.indexOf("]"));

         } else if (str.contains("[")) {
         tmp = str.substring(str.indexOf("[") + 1);

         }
         String newStaf = getShortIRI(tmp);

         if (!tmp.equalsIgnoreCase("")) {
         if (!myBasUtils.elemInListContained(injectedVal, tmp) && !myBasUtils.elemInListContained(injectedVal, newStaf)) {

         if (!myBasUtils.elemInListContained(allTPunitiesFinal, tmp)) {

         allTPunities.add(tmp);
         }

         if (!myBasUtils.elemInListContained(allTPunitiesFinal, newStaf)) {
         allTPunities.add(newStaf);
         }

         } else if (selectorQuery.contains("abstract")) {
         if (!myBasUtils.elemInListContained(allTPunitiesFinal, tmp)) {

         allTPunities.add(tmp);
         }

         if (!myBasUtils.elemInListContained(allTPunitiesFinal, newStaf)) {
         allTPunities.add(newStaf);
         }
         }
         }

         }

         } 
         }


        

         }*/
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
                if (myBasUtils.elemInListContained(selectorUnities, currStr) || myBasUtils.elemInListContained(allTPunities, currStr)) {
                    continue;
                }
                if (currStr.contains("http")) {

                    if (myBasUtils.elemInListContained(selectorUnities, getShortIRI(currStr)) || myBasUtils.elemInListContained(allTPunities, getShortIRI(currStr))) {
                        continue;
                    }
                    allTPunities.add(getShortIRI(currStr));
                }

                allTPunities.add(currStr);

            }

        }

        allTPunities = myBasUtils.refineList(allTPunities);

        for (int i = 0; i < allTPunities.size(); i++) {

            if (allTPunities.get(i).contains("####")) {
                continue;
            }

            myBasUtils.insertToMap(mapConstToAnsOccurs, allTPunities.get(i));
                    //   mapConstToAnsOccurs.put(allTPunities.get(i), 1);

            //Identify a short IRI constant  in its detailed form
            if (!allTPunities.get(i).contains("http") && allTPunities.get(i).contains(":")) {

                myBasUtils.insertToMap(mapConstToAnsOccurs, getCompleteIRI(allTPunities.get(i)));
                allTPunitiesFinal.add(getCompleteIRI(allTPunities.get(i)));

            } else if (allTPunities.get(i).contains("http")) {

                myBasUtils.insertToMap(mapConstToAnsOccurs, getShortIRI(allTPunities.get(i)));
                allTPunitiesFinal.add(getShortIRI(allTPunities.get(i)));

            }

            /*                  myBasUtils.insertToMap(mapConstToAnsOccurs, refString);
             //                 System.out.println(mapConstToAnsOccurs);

                
             //Identify a short IRI constant  in its detailed form
             if(!refString.contains("http")&&refString.contains(":")){
                    
             myBasUtils.insertToMap(mapConstToAnsOccurs, getCompleteIRI(refString));
             allTPunitiesFinal.add(getCompleteIRI(refString));
             }
                    
             else if(refString.contains("http")){
                    
             myBasUtils.insertToMap(mapConstToAnsOccurs, getShortIRI(refString));
             allTPunitiesFinal.add(getShortIRI(refString));

             }*/
            //    allSelectorUnities = addAllShortIRIs(querySelectorUnities);
            if (mapAnswerToOccurs.get(allTPunities.get(i)) == null) {

                mapAnswerToOccurs.put(allTPunities.get(i), 1);
            } else {
                mapAnswerToOccurs.put(allTPunities.get(i), mapAnswerToOccurs.get(allTPunities.get(i)) + 1);
            }

        }

        if (answers.contains("Brad")) {
            allTPunities.add("http://dbpedia.org/resource/Brad_Pitt");
        }

        allTPunities = myBasUtils.sortAndRemoveRedundancy(allTPunities);
        return allTPunities;
    }

    /**
     * Get, if there exist, the corresponding LDF candidate from a single unity
     * selector (i.e., a selector with only predicate, subject or object).
     *
     * @param querySelectorUnities list of query/selector unities
     * @param allVars all query/selector's varibales
     * @return LDF candidate format
     */
    public List<String> getSingleUnitySelector(List<String> querySelectorUnities, List<String> allVars) {

        List<String> singleUnitySelector = new LinkedList<>();
        String completeIRI = "";

        //Case 1: if there is only a predicate at the selector
        if (myBasUtils.elemInListEquals(allVars, "predicate")) {

            singleUnitySelector.add("subject");
            if (querySelectorUnities.get(0).contains(":") && !querySelectorUnities.get(0).contains("http")) {
                singleUnitySelector.add(getCompleteIRI(querySelectorUnities.get(0)));
            } else {
                singleUnitySelector.add(querySelectorUnities.get(0));

            }
            singleUnitySelector.add("object");
        } //Case 2: If there is only a subject at the selector 
        else if (myBasUtils.elemInListEquals(allVars, "subject")) {

            if (querySelectorUnities.get(0).contains(":") && !querySelectorUnities.get(0).contains("http")) {
                singleUnitySelector.add(getCompleteIRI(querySelectorUnities.get(0)));
            } else {
                singleUnitySelector.add(querySelectorUnities.get(0));

            }            //unknown predicate 
            singleUnitySelector.add("?p");
            singleUnitySelector.add("object");

        } //Case 3: If there is only an object  at the selector 
        else if (myBasUtils.elemInListEquals(allVars, "object")) {

            singleUnitySelector.add("subject");
            //unknown predicate 
            singleUnitySelector.add("?p");
            if (querySelectorUnities.get(0).contains(":") && !querySelectorUnities.get(0).contains("http")) {
                singleUnitySelector.add(getCompleteIRI(querySelectorUnities.get(0)));
            } else {
                singleUnitySelector.add(querySelectorUnities.get(0));

            }
        }

        return singleUnitySelector;
    }

    /**
     * Get candidate LDF , in a refined triple pattern format. Where a subject,
     * predicate or object can be the corresponding "keyword", or a constant or
     * "POSSIBLY_INJECTED"
     *
     * @param allVars all variables present in the query/selector
     * @param querySelectorUnities list of query/selector's unities
     * @param injectedVals list of injected values, of the current LDF candidate
     * @return candidate LDF in triple pattern format
     */
    public List<String> getRefinedCandLDFBis(List<String> allVars, List<String> querySelectorUnities, List<String> injectedVals) {

        List<String> newRefCandLDF = new LinkedList<>();
        String currUnity = "";
        boolean flagSkipSubject = false;
        boolean flagSkipObject = false;

        if (myBasUtils.elemInListContained(injectedVals, "http://dbpedia.org/ontology/Artist")) {
            int arz = 0;
        }

        ///BUUUUUUUUUUUUUUUUUUUUUG
        //Redo by checking if a subject or object have not been returned before
        if (injectedVals.size() == 2 && injectedVals.get(0).contains("ontology") && !injectedVals.get(1).contains("ontology")) {

            flagSkipSubject = true;
        }
        if (injectedVals.size() == 2 && !injectedVals.get(0).contains("ontology") && injectedVals.get(1).contains("ontology")) {

            flagSkipObject = true;
        }

        if (injectedVals.size() == 1 && injectedVals.get(0).contains("ontology") && myBasUtils.elemInListContained(allVars, "subject")) {

            flagSkipSubject = true;
        }
        if (injectedVals.size() == 1 && injectedVals.get(0).contains("ontology") && myBasUtils.elemInListContained(allVars, "object")) {

            flagSkipObject = true;
        }

        if (injectedVals.size() == 1 && !injectedVals.get(0).equalsIgnoreCase("") && !injectedVals.get(0).contains("http") && myBasUtils.elemInListContained(allVars, "object") && mapAnswerToOccurs.get(injectedVals.get(0)) == null) {

            flagSkipObject = true;
        }

        if (injectedVals.size() == 1 && !injectedVals.get(0).equalsIgnoreCase("") && !injectedVals.get(0).contains("http") && myBasUtils.elemInListContained(allVars, "subject") && mapAnswerToOccurs.get(injectedVals.get(0)) == null) {

            flagSkipSubject = true;
        }

        if (!allVars.isEmpty()) {

            //BUUUUUUUUUUUUUUUUUUUUUUG
            // [1]: Get refined "subject" unity, if there exist, and which can 
            // only be at the first or second element of the list of selector's unities
            if (flagSkipSubject) {
                newRefCandLDF.add(injectedVals.get(0));

            } else {
                newRefCandLDF.add(getRefinedLDUnity(injectedVals, allVars, querySelectorUnities.get(0), "subject"));

            }

            // [2]: Get refined "predicate" unity, if there exist and which can 
            // be at the first or second element of the list of selector's unities
            if (allVars.get(0).contains("predicate")) {

                currUnity = querySelectorUnities.get(0);
            } else if (allVars.get(1).contains("predicate")) {

                // transform shortenIRI into detailed, only for predicates
                if (querySelectorUnities.get(1).contains(":") && !querySelectorUnities.get(1).contains("http")) {
                    currUnity = getCompleteIRI(querySelectorUnities.get(1));

                } else {

                    currUnity = querySelectorUnities.get(1);
                }

            }

            newRefCandLDF.add(getRefinedLDUnity(injectedVals, allVars, currUnity, "predicate"));

            // [3]: Get refined "object" unity, if there exist and which can 
            // be at the second or third element of the list of selector's unities
            if (allVars.get(1).contains("object")) {

                currUnity = querySelectorUnities.get(1);
            } else if (allVars.size() == 3 && allVars.get(2).contains("object")) {

                currUnity = querySelectorUnities.get(2);
            } else {

                currUnity = "";
            }

            if (flagSkipObject) {
                //     System.out.println("WHaaaaaaat: ");
                if (injectedVals.size() == 1) {
                    newRefCandLDF.add(injectedVals.get(0));
                } else if (injectedVals.size() == 2) {
                    newRefCandLDF.add(injectedVals.get(1));
                }

            } else {
                newRefCandLDF.add(getRefinedLDUnity(injectedVals, allVars, currUnity, "object"));

            }

        }

        return newRefCandLDF;
    }

    /**
     * Get candidate LDF , in a refined triple pattern format. Where a subject,
     * predicate or object can be the corresponding "keyword", or a constant or
     * "POSSIBLY_INJECTED"
     *
     * @param allVars all variables present in the query/selector
     * @param querySelectorUnities list of query/selector's unities
     * @param injectedVals list of injected values, of the current LDF candidate
     * @return candidate LDF in triple pattern format
     */
    public List<String> getRefinedCandLDF(List<String> allVars, List<String> querySelectorUnities, List<String> injectedVals) {

        List<String> newRefCandLDF = new LinkedList<>();
        String currUnity = "";

        if (!allVars.isEmpty()) {

            // [1]: Get refined "subject" unity, if there exist, and which can 
            // only be at the first or second element of the list of selector's unities
            newRefCandLDF.add(getRefinedLDUnity(injectedVals, allVars, querySelectorUnities.get(0), "subject"));

            // [2]: Get refined "predicate" unity, if there exist and which can 
            // be at the first or second element of the list of selector's unities
            if (allVars.get(0).contains("predicate")) {

                currUnity = querySelectorUnities.get(0);
            } else if (allVars.get(1).contains("predicate")) {

                currUnity = querySelectorUnities.get(1);
            }

            newRefCandLDF.add(getRefinedLDUnity(injectedVals, allVars, currUnity, "predicate"));

            // [3]: Get refined "object" unity, if there exist and which can 
            // be at the second or third element of the list of selector's unities
            if (allVars.get(1).contains("object")) {

                currUnity = querySelectorUnities.get(1);
            } else if (allVars.size() == 3 && allVars.get(2).contains("object")) {

                currUnity = querySelectorUnities.get(2);
            } else {

                currUnity = "";
            }

            newRefCandLDF.add(getRefinedLDUnity(injectedVals, allVars, currUnity, "object"));

        }

        return newRefCandLDF;
    }

    /**
     * Get refined unity, i.e., subject predicate or object, of LDF Candidate
     * depending on existance of unity and original selector's unity is possibly
     * injected or not
     *
     * @param injectedVals list of injected values, ot the current LDF candidate
     * @param allVars all variables present in the query/selector
     * @param currUnity current unity selector
     * @param unityType type of unity selector
     * @return refined unity
     */
    public String getRefinedLDUnity(List<String> injectedVals, List<String> allVars, String currUnity, String unityType) {

        String refinedUnity = "";

        //If there is the "key word", we are looking for
        if (myBasUtils.elemInListEquals(allVars, unityType)) {

            //If current unity is possibly injected, return "POSSIBLY_INJECTED"
            if (myBasUtils.elemInListEquals(injectedVals, currUnity)) {

                refinedUnity = "POSSIBLY_INJECTED";
            } // Else, return the unity 
            else if (!myBasUtils.elemInListEquals(injectedVals, currUnity)) {
                refinedUnity = currUnity;
            }
        } //else return the current "key word", as unity
        else if (!myBasUtils.elemInListEquals(allVars, unityType)) {

            refinedUnity = unityType;
        }

        return refinedUnity;
    }

    /**
     * Set all information of current log entry to corresponding LDF candidate
     * (exisitng or new), concerning entry id, injected values, answer values
     *
     * @param currCandLDF current LDF candidate
     * @param injectVals all injected values of log entry's selector
     * @param allAns list of distinct answers of current log entry's fragments
     * @param logEntryID current log entry id
     * @param LDFserver current LDF server responding to selector
     * @param timeStamp time in total number of seconds, of current log entry
     */
    public void setLDFCandHashInfo(List<String> currCandLDF, List<String> allAns,
            List<String> injectVals, int logEntryID, String LDFserver, int timeStamp, List<String> selectorUnities) {

        if (!currCandLDF.isEmpty()) {

            //Identify new candidate LDF  or an new version of an existing one
            // that differs because of "windowJoin" or "gap"
            if (mapCandLDFToSerialID.get(currCandLDF) == null) {

                mapCandLDFToSerialID.put(currCandLDF, mapCandLDFToSerialID.size() + 1);
                myBasUtils.insertToMap2(mapCandDFToTimeSecs, timeStamp, mapCandLDFToSerialID.get(currCandLDF));
            } else {

                currCandLDF = getLatestVersionLDFCand(currCandLDF, timeStamp);
                if (mapCandLDFToSerialID.get(currCandLDF) == null) {

                    mapCandLDFToSerialID.put(currCandLDF, mapCandLDFToSerialID.size() + 1);
                }

                myBasUtils.insertToMap2(mapCandDFToTimeSecs, timeStamp, mapCandLDFToSerialID.get(currCandLDF));
            }

            // match current log entry to Candidate LDF
            myBasUtils.insertToMap(mapCandLDFToLogEntries, currCandLDF, logEntryID);
            // match current LDF server to Candidate LDF
            myBasUtils.insertToMap(mapCandLDFToLDFServer, LDFserver, mapCandLDFToSerialID.get(currCandLDF));

            // match current (possibly) injected values to Candidate LDF
            for (int i = 0; i < injectVals.size(); i++) {

                myBasUtils.insertToMap(mapCandLDFToInjectVals, injectVals.get(i), mapCandLDFToSerialID.get(currCandLDF));
            }

            if (currCandLDF.get(0).contains("POSSIBLY") && currCandLDF.get(2).contains("POSSIBLY")) {

                myBasUtils.insertToMap(mapCandLDFToInjectValsSUbject, selectorUnities.get(0), mapCandLDFToSerialID.get(currCandLDF));
                myBasUtils.insertToMap(mapCandLDFToInjectValsObject, selectorUnities.get(2), mapCandLDFToSerialID.get(currCandLDF));
            }

            // match current answer values to Candidate LDF   
            for (int i = 0; i < allAns.size(); i++) {

                if (!myBasUtils.elemInListEquals(injectVals, allAns.get(i)) && !myBasUtils.elemInListEquals(injectVals, getShortIRI(allAns.get(i)))) {

                    myBasUtils.insertToMap(mapCandLDFToAnswerOrig, allAns.get(i), mapCandLDFToSerialID.get(currCandLDF));
                }

            }

          //  System.out.println("-----------------------------------------------------");
            // System.out.println("\t Candidate pattern: "+currCandLDF);
            // System.out.println("\t Original answers: \n \t \t "+mapCandLDFToAnswerOrig.get(mapCandLDFToSerialID.get(currCandLDF)));
            // System.out.println("\t Injected answers: \n \t \t "+injectVals);
        }

    }

    /**
     * Get latest version of LDF Candidate which depends on current timeStamp,
     * list of existing timestamps and the user-defined "windowJoin" or "gap"
     *
     * @param originalLDFversion original identified LDF candidate version
     * @param newTimestamp timeStamp of current log entry
     * @return latest version of LDF Candidate
     */
    public List<String> getLatestVersionLDFCand(List<String> originalLDFversion, int newTimestamp) {

        int latestIndx = -1;
        String latestVersion = "";
        List<String> newLDFvers = new LinkedList<>(originalLDFversion.subList(0, originalLDFversion.size()));
        List<String> tmpLDFvers = new LinkedList<>(originalLDFversion.subList(0, originalLDFversion.size()));

        // Check if it can be added in the original version of the LDF candidate
        if (mapCandLDFToVersionsTjoin.get(originalLDFversion) == null) {

            // If true, then return the original LDF candidate
            if (checkTemporalDistance(mapCandDFToTimeSecs.get(mapCandLDFToSerialID.get(originalLDFversion)), newTimestamp)) {

                return tmpLDFvers;
            }

            // or create the first version of LDF Candidate, which depends on "gap"
            mapCandLDFToVersionsTjoin.put(originalLDFversion, 1);
            latestVersion = "vol_1";
            newLDFvers.add(latestVersion);
        } // Or else, check if it can be added in different versions of this LDF Candidate
        else {

            latestIndx = mapCandLDFToVersionsTjoin.get(originalLDFversion);

            for (int i = 1; i <= latestIndx; i++) {

                tmpLDFvers = new LinkedList<>(originalLDFversion.subList(0, originalLDFversion.size()));
                latestVersion = "vol_" + Integer.toString(i);
                tmpLDFvers.add(latestVersion);

                // If true, then return the current version of the LDF candidate
                if (checkTemporalDistance(mapCandDFToTimeSecs.get(mapCandLDFToSerialID.get(tmpLDFvers)), newTimestamp)) {

                    return tmpLDFvers;
                }
            }

            // or create a new  version of LDF Candidate
            latestIndx += 1;
            mapCandLDFToVersionsTjoin.put(originalLDFversion, latestIndx);
            latestVersion = "vol_" + Integer.toString(latestIndx);
            newLDFvers.add(latestVersion);
        }

        return newLDFvers;
    }

    /**
     * Check if a new timestamp is close enough to list of timestamps of an
     * existing LDF Candidate, based on the "windowJoin" or "gap"
     *
     * @param existingTimeStamps list of existing timestamps
     * @param newTimeStamp new timestamp, to be compared
     * @return true, if it is close enough
     */
    public boolean checkTemporalDistance(List<Integer> existingTimeStamps, int newTimeStamp) {

        for (int currTime : existingTimeStamps) {
            if ((newTimeStamp - currTime) >= 0 && (newTimeStamp - currTime) <= windowJoin) {

                return true;
            }
        }

        return false;
    }

    /**
     * Show extracted info of every captured log entry (i.e., variable, injected
     * values and answers)
     *
     * @param logEntryID current answer entry id
     * @param allVars all variables present in the query/selector
     * @param injectedVals all injected values of current log entry
     * @param allAns all distinct answers of current log entry
     * @param querySelector query/selector in string format
     * @param selectoAns
     */
    public void showDetailedEntryInfo(int logEntryID, List<String> allVars,
            List<String> injectedVals, List<String> allAns, String querySelector, String selectoAns) {

        System.out.println("***************************");
        System.out.println("LDF entry: " + logEntryID + " with ressource: " + querySelector);

        for (int i = 0; i < allVars.size(); i++) {

            if (!allVars.get(i).contains("predicate")) {

                System.out.println("\t-------------------------------------");
                //System.out.println("\t Variable: " + allVars.get(i));
                // System.out.println("\t Injected Value: " + injectedVals);
                //  System.out.println("\t Answer Values: " + selectoAns);
            }

        }
    }

    /**
     * Get all triples' unities (i.e., subject, predicate and object) from the
     * answer in string format, as it sent from the LDF server
     *
     * @param selectorAnsFragment complete answer fragment in string format
     * @return list of triples' unities
     */
    public List<String> getUnitiesFromSelectorAns(String selectorAnsFragment) {

        boolean flagURI = false, flagLiteral = false;
        boolean flagDoublecotes = false, flagPredicatePref = false;
        int countQuotes = 0;
        List<String> queryUnities = new LinkedList<>();
        List<String> queryUnitiesRefned = new LinkedList<>();
        String valueURI = "", valueLiteral = "", valuePrefix = "";
        String tmpAns = preProcessSelectorAns(selectorAnsFragment);

        for (int i = 0; i < tmpAns.length(); i++) {

            //Case it is a IRI
            if ((tmpAns.charAt(i) == '<' || flagURI)) {

                if (tmpAns.charAt(i) == '>' || tmpAns.charAt(i) == '|') {
                    if (tmpAns.charAt(i) != '|') {

                        valueURI += tmpAns.charAt(i);
                    }

                    queryUnities.add(valueURI);
                    flagURI = false;
                    valueURI = "";
                } else {

                    flagURI = true;
                    valueURI += tmpAns.charAt(i);
                }
            } //Case it is a literal
            else if ((((tmpAns.charAt(i) == '\"') || ((tmpAns.charAt(i) == '\'') && !flagDoublecotes)) || flagLiteral)) {

                if (tmpAns.charAt(i) == '\"') {

                    countQuotes++;
                }

                if (((tmpAns.charAt(i) == '\"') || (((tmpAns.charAt(i) == '\'' && countQuotes == 0) || tmpAns.charAt(i) == '|'
                        || (tmpAns.charAt(i) == ',' && countQuotes == 2)) && !flagDoublecotes))
                        && flagLiteral && tmpAns.charAt(i + 1) != '@') {

                    if ((tmpAns.charAt(i) == '\"' || tmpAns.charAt(i) == '\'') && flagDoublecotes) {
                        flagDoublecotes = false;
                    }

                    flagLiteral = false;

                    if (tmpAns.charAt(i) != '|' && (tmpAns.charAt(i) != ',' && countQuotes < 2)) {

                        valueLiteral += tmpAns.charAt(i);
                    }

                    countQuotes = 0;
                    queryUnities.add(valueLiteral);
                    valueLiteral = "";

                } else {

                    flagLiteral = true;
                    valueLiteral += tmpAns.charAt(i);
                }
            } //Case it is a IRI but with a PREFIX
            else if (((Character.isLetter(tmpAns.charAt(i))) || flagPredicatePref)) {

                if ((tmpAns.charAt(i) == ' ' || tmpAns.charAt(i) == '}' || tmpAns.charAt(i) == '|') && flagPredicatePref) {

                    flagPredicatePref = false;
                    queryUnities.add(valuePrefix);
                    valuePrefix = "";
                } else {

                    flagPredicatePref = true;
                    valuePrefix += tmpAns.charAt(i);
                }
            }
        }

        // get list of refined query's unities
        queryUnitiesRefned = getProccessedUntities(queryUnities);
        queryUnitiesRefned = myBasUtils.refineList(queryUnitiesRefned);

        return queryUnitiesRefned;
    }

    /**
     * Pre-process the fragment answer, to identify easily all answer unities
     *
     * @param selectorAnsFragment answer fragment in string format
     * @return processed fragment answer
     */
    public String preProcessSelectorAns(String selectorAnsFragment) {

        selectorAnsFragment = selectorAnsFragment.replaceAll(".\\\\n", "|");
        selectorAnsFragment = selectorAnsFragment.replaceAll("\\\\n", " ");
        selectorAnsFragment = selectorAnsFragment.replaceAll("\\\\\"", "\"");
        selectorAnsFragment = selectorAnsFragment.replaceAll("\\\\\\\\\"", "");

        if (selectorAnsFragment.endsWith(".")) {

            selectorAnsFragment = selectorAnsFragment.substring(0, selectorAnsFragment.length() - 1);
            selectorAnsFragment += "|";
        }

        selectorAnsFragment = selectorAnsFragment + " ";

        return selectorAnsFragment;
    }

    /**
     * Remove and refine elements of list of answer unities
     *
     * @param allAnsFragment all distinct answer fragment's unities
     * @return refined answer's list of unities
     */
    public List<String> getProccessedUntities(List<String> allAnsFragment) {

        List<String> queryUnitiesRefned = new LinkedList<>();
        String currEntity = "", prevEntity = "";

        for (int i = 0; i < allAnsFragment.size(); i++) {

            currEntity = allAnsFragment.get(i);

            if (currEntity.equals("dbpedia:BARACK_OBAMA")) {
                int dqsdqs = 0;
            }
            if (currEntity.contains("&")) {
                int eaez = 0;
            }
            currEntity = currEntity.replaceAll("|", "");
            currEntity = currEntity.replaceAll(",_", "_");

         //   currEntity = currEntity.replaceAll(",", "");
            if (currEntity.endsWith(",")) {
                currEntity = currEntity.substring(0, currEntity.length() - 1);
            }
            //BUUUUUUUUUUUUUUUUUUUUUUUG
           /*  if (currEntity.startsWith("\"")) {
             currEntity = currEntity.replaceAll("\"", "");
             }*/

            //Remove predicates from answer fragment's unities 
            if ((mapLDFpredicates.get(currEntity) == null)
                    || (prevEntity.equalsIgnoreCase("a") && !myBasUtils.elemInListEquals(queryUnitiesRefned, currEntity))) {

                //BUUUUUUUUUUUUUUUUUUUUUUUUG
                currEntity = currEntity.replace("__", "_");
                queryUnitiesRefned.add(currEntity);
            }

            prevEntity = currEntity;
        }

        return queryUnitiesRefned;
    }

    /**
     * Add in list of injected values of a LDF Candidate, for each complete IRI,
     * the corresponding shorten version using prefixes
     *
     * @param injectedValues initial list of injected values
     * @return list of injected values, extended with prefixes' short versions
     */
    public List<String> addAllShortIRIs(List<String> injectedValues) {

        List<String> extendedList = new LinkedList<>();
        String shortCutIRI = "";

        if (injectedValues != null) {

            extendedList = new LinkedList<>(injectedValues.subList(0, injectedValues.size()));

            for (int i = 0; i < injectedValues.size(); i++) {

                shortCutIRI = getShortIRI(injectedValues.get(i));

                if (!shortCutIRI.equalsIgnoreCase("")) {

                    extendedList.add(shortCutIRI);
                }

                extendedList.add(injectedValues.get(i));
            }
        }

        return extendedList;
    }

    /**
     * Get for a specific complete IRI, the corresponding version using prefixes
     *
     * @param completeIRI complete IRI
     * @return shorten IRI using prefixes
     */
    public String getShortIRI(String completeIRI) {

        String subAuthority = "", subRessource = "", shortCutIRI = "";

        if (completeIRI.contains("http")) {

            subAuthority = "";
            subRessource = "";
            shortCutIRI = "";

            if (!completeIRI.contains("#")) {

                subAuthority = completeIRI.substring(0, completeIRI.lastIndexOf("/") + 1);
                subRessource = completeIRI.substring(completeIRI.lastIndexOf("/") + 1);
            } else {

                subAuthority = completeIRI.substring(0, completeIRI.lastIndexOf("#") + 1);
                subRessource = completeIRI.substring(completeIRI.lastIndexOf("#") + 1);
            }

            if (!subAuthority.equalsIgnoreCase("")) {

                if (mapAuthorityToPrefix.get(subAuthority) != null) {

                    shortCutIRI = mapAuthorityToPrefix.get(subAuthority) + subRessource;
                }

            }

        }

        return shortCutIRI;
    }

    /**
     * Get for a specific complete IRI, the corresponding version using prefixes
     *
     * @param shortIRI
     * @return shorten IRI using prefixes
     */
    public String getCompleteIRI(String shortIRI) {

        String completeIRI = "", shortPrefix = "";

        shortPrefix = shortIRI.substring(0, shortIRI.indexOf(":") + 1);

        completeIRI = mapPrefixToAuthority.get(shortPrefix) + shortIRI.substring(shortIRI.indexOf(":") + 1);

        return completeIRI;
    }

    /**
     * Show all LDF candidates with injected values and answers,
     *
     * @throws java.io.IOException
     */
    public void showCandidateLDFs() throws IOException {

        //For a concurrent execution, get pairs of ground truth 
        if (!simpleExecution) {

            getGroundTruthPairs();
        }

        System.out.println("\n\t----------Candidates LD fragment----------\n");

        int count = 1;

        for (List<String> currLDFfrag : mapCandLDFToSerialID.keySet()) {

            System.out.println("\t\t LDF no [" + count + "]: \"" + currLDFfrag.get(0) + " "
                    + currLDFfrag.get(1) + " " + currLDFfrag.get(2));

            count++;
        }

        System.out.println("\n\t----------Candidates LD fragment----------\n");
    }

    /**
     * Search for every candidate LDF, a possible matching between its injected
     * values with the extention (i.e., bounds) of a prevously evaluated LDF.
     *
     * But first, merege possible "service" LDFs with corresponding "membership"
     *
     * This function simulates a nested loop implementation.
     *
     * @throws java.io.IOException
     */
    public void searchNestedLoops() throws IOException {

        List<String> deducedLDF = null;
        List<String> currLDFCandOuter = null;
        List<String> currLDFCandInner = null;
        List<String> currAnsOrigOuter = null;
        List<String> currAnsInjectInner = null;
        List<String> currAnsInjectInnerSubject = null;
        List<String> currAnsInjectInnerObject = null;

        List<List<String>> pairJoin = null;
        List<String> currInjVals = null;
        List<Integer> alreadyMatchedLDFCand = new LinkedList<>();
        HashMap<Integer, Integer> mapMatchedCandLDFtoDed = new HashMap<>();
        HashMap<Integer, List< List<String>>> mapMatchedCandLDFtoNewCandidate = new HashMap<>();

        boolean flagDoubleNestesed = false;

        int indexPushPosition = -1;
        // remove redundancy in  candidate LDF hash maps
        removeRedundancyLDFInfo();
        // BUUUUUUUUUUUUUUUUUG:  serialise candidate hasmaps, if needed it 
        HashMap<Integer, List<String>> mapSerialLDFCandidate = sortMapBasedOnID(mapCandLDFToSerialID);
        //Merge service LDF with corresponding membership, if there exists

        if (LDFgraphReduction) {

            mapSerialLDFCandidate = refineLDFCandList(mapSerialLDFCandidate);
        }

        //init each candidate LDF with its deduced LDF ID
        for (int currKey : mapSerialLDFCandidate.keySet()) {
            mapMatchedCandLDFtoDed.put(currKey, -1);
        }

        //Start nested loop detection
        for (int countOuter : mapSerialLDFCandidate.keySet()) {

            deducedLDF = new LinkedList<>();
            currLDFCandOuter = mapSerialLDFCandidate.get(countOuter);

            //If current outer LDF is not suseptible to be part of annother nestedLoop, then 
            //identify directly as a deduced LDF    
            if (!myBasUtils.elemInListContained(currLDFCandOuter, "POSSIBLY")) {

                alreadyMatchedLDFCand.add(countOuter);
                currLDFCandOuter = mapSerialLDFCandidate.get(countOuter);
                mapDedLDFToLDFServer.put(mapDedLDFToSerialID.size() + 1, mapCandLDFToLDFServer.get(mapCandLDFToSerialID.get(currLDFCandOuter)));
                mapDedLDFToSerialID.put(mapDedLDFToSerialID.size() + 1, currLDFCandOuter);
                mapMatchedCandLDFtoDed.put(countOuter, mapDedLDFToSerialID.size());
                mapDeducdLDFToCondifence.put(currLDFCandOuter, 1.0);
                mapDeducdLDFToPushedPercentage.put(currLDFCandOuter, "(whithout poushed values)");

            } // or current outer LDF is never been identified as part of an inner nested Loop
            else if (!myBasUtils.elemInListEquals(alreadyMatchedLDFCand, countOuter)) {

                alreadyMatchedLDFCand.add(countOuter);
                currLDFCandOuter = mapSerialLDFCandidate.get(countOuter);
                currInjVals = mapCandLDFToInjectVals.get(countOuter);
                deducedLDF = getDeducedLDFFromOriginal(currLDFCandOuter, currInjVals);
                mapDedLDFToLDFServer.put(mapDedLDFToSerialID.size() + 1, mapCandLDFToLDFServer.get(mapCandLDFToSerialID.get(currLDFCandOuter)));
                mapDedLDFToSerialID.put(mapDedLDFToSerialID.size() + 1, deducedLDF);

                mapDeducdLDFToCondifence.put(deducedLDF, 1.0);
                mapDeducdLDFToPushedPercentage.put(currLDFCandOuter, "(whithout poushed values)");

                mapMatchedCandLDFtoDed.put(countOuter, mapDedLDFToSerialID.size());
            }

            //then, for all other inner candidates LDFs ...
            for (int countInner : mapSerialLDFCandidate.keySet()) {

                flagDoubleNestesed = false;

                currLDFCandInner = mapSerialLDFCandidate.get(countInner);

                if (myBasUtils.elemInListContained(currLDFCandInner, "starring") && myBasUtils.elemInListContained(currLDFCandOuter, "starring")) {
                    continue;
                }

                if (countOuter != countInner) {

                    //Skip current inner LDF candidate if
                    // (a) outer and innerLDF are not joinable, because of Tjoin, or
                    // (b) outer is not preceding innerLDF, or
                    // (c) inner LDF Candidate has not any injected value
                    if ((!checkIfTemporalyJoinable(mapCandDFToTimeSecs.get(countOuter), mapCandDFToTimeSecs.get(countInner)))
                            || (!isLDFpreceeding(mapCandLDFToLogEntries.get(currLDFCandOuter), mapCandLDFToLogEntries.get(currLDFCandInner)))
                            || (!myBasUtils.elemInListEquals(currLDFCandInner, "POSSIBLY_INJECTED"))) {

                        continue;
                    }

                    currAnsOrigOuter = mapCandLDFToAnswerOrig.get(countOuter);

                    if (countOuter == 3 && countInner == 5) {
                        int azeze = 0;

                    }

                    if (myBasUtils.elemInListEquals(currAnsOrigOuter, "http://dbpedia.org/resource/Alcobendas_CF")) {
                        int azaz = 0;
                    }
                    if (myBasUtils.elemInListEquals(currAnsOrigOuter, "Alcobendas_CF")) {
                        int azaz = 0;
                    }

                    List<String> commonVals = null;
                    if ((currLDFCandInner.get(0).contains("POSSIBLY") && currLDFCandInner.get(2).contains("POSSIBLY"))
                            || ((currLDFCandInner.get(0).contains("INJECTED") && !currLDFCandInner.get(0).contains("POSSIBLY")) && currLDFCandInner.get(2).contains("POSSIBLY"))
                            || (currLDFCandInner.get(0).contains("POSSIBLY") && (currLDFCandInner.get(2).contains("INJECTED") && !currLDFCandInner.get(2).contains("POSSIBLY")))) {

                        int eazeaz = 0;
                        currAnsInjectInnerSubject = mapCandLDFToInjectValsSUbject.get(countInner);
                        currAnsInjectInnerObject = mapCandLDFToInjectValsObject.get(countInner);

                        if (myBasUtils.elemInListEquals(currAnsOrigOuter, "http://dbpedia.org/resource/Alcobendas_CF")) {
                            int azaz = 0;
                        }

                        if (myBasUtils.elemInListEquals(currAnsInjectInnerObject, "http://dbpedia.org/resource/Alcobendas_CF")) {
                            int azaz = 0;
                        }

                        flagDoubleNestesed = true;

                        commonVals = getListsIntersec(currAnsOrigOuter, currAnsInjectInnerSubject);
                        if (commonVals.size() == 0) {
                            commonVals = getListsIntersec(currAnsOrigOuter, currAnsInjectInnerObject);
                            if (commonVals.size() > 0) {
                                System.out.println("------------------------------------------------------------------");
                                System.out.println("----------------Outer: " + currLDFCandOuter);
                                System.out.println("----------------Inner: " + currLDFCandInner);
                                if (currLDFCandInner.get(1).contains("http://dbpedia.org/ontology/birthPlace")) {

                                    System.out.println("(((((((((((((((((((((((((((((((((((injected in object: " + currAnsInjectInnerObject);
                                }

                                indexPushPosition = 2;

                            }
                        } else {
                            System.out.println("------------------------------------------------------------------");
                            System.out.println("----------------Outer: " + currLDFCandOuter);
                            System.out.println("----------------Inner: " + currLDFCandInner);
                            System.out.println("----------------injected in subject: ");
                            indexPushPosition = 0;

                        }

                        /*    if(indexPushPosition==0){
                            
                         myBasUtils.insertToMap(mapDedLDFToSerialID, "subject", countInner);
                         }
                         else if(indexPushPosition==2){
                          
                         myBasUtils.insertToMap(mapDedLDFToSerialID, "object", countInner);  
                         }
                         */
                    } else {

                        if (currLDFCandInner.get(0).contains("POSSIBLY")) {
                            indexPushPosition = 0;
                        } else if (currLDFCandInner.get(2).contains("POSSIBLY")) {
                            indexPushPosition = 2;
                        }

                        currAnsInjectInner = mapCandLDFToInjectVals.get(countInner);
                        if (currLDFCandInner.get(1).contains("http://dbpedia.org/ontology/birthPlace")) {

                            //     System.out.println("HAAAAAAAAAAAAAA: "+currAnsInjectInner);
                        }
                        commonVals = getListsIntersec(currAnsOrigOuter, currAnsInjectInner);
                    }

                 //   commonVals= getListsIntersec(currAnsOrigOuter, currAnsInjectInner);
                    //compare outer's orginal answer vals with inner's injected vals, for possible intersection
                    if (commonVals.size() > 0) {

                        System.out.println("\t *************************************************************************************************************************************");
                        System.out.println("\t outer LDF: " + currLDFCandOuter + " and key: " + countOuter);
                        //System.out.println("\t\t with source mappings size: " + currAnsOrigOuter.size()+"\n \t\t"+currAnsOrigOuter);
                        // System.out.println("\t\t with pushed mappings size: " + currAnsInjectInner.size()+"\n \t\t"+currAnsInjectInner);
                        System.out.println("\t inner LDF: " + currLDFCandInner + " and key: " + countInner);
                        // System.out.println("\t\t with answers origin: " + currAnsOrigOuter);
                        // System.out.println("\t\t with commonVals : " + commonVals);

                        if (commonVals.size() == 4) {
                            //    System.out.println(commonVals);
                        }
                        System.out.println("\t\t with matching fragmnet: " + commonVals.size() + "/" + currAnsOrigOuter.size());

                        double confidence = (double) (commonVals.size()) / (double) (currAnsOrigOuter.size());
                        String percentageDescription = "(fraction of pushed values: " + Integer.toString(commonVals.size()) + "/" + Integer.toString(currAnsOrigOuter.size()) + " )";

                        if (commonVals.size() < currAnsOrigOuter.size()) {
                            if ((currAnsOrigOuter.size() - commonVals.size()) <= (currAnsOrigOuter.size() % 30)) {
                                confidence = 1;
                                percentageDescription = "(fraction of pushed values: " + Integer.toString(currAnsOrigOuter.size()) + "/" + Integer.toString(currAnsOrigOuter.size()) + " )";

                            }
                        }
                        System.out.println("\t\t with nested loop confidence: " + confidence);

                        if (!flagDoubleNestesed) {
                            setDeducedPairLDF(currLDFCandOuter, currLDFCandInner, mapMatchedCandLDFtoDed.get(countOuter), confidence, percentageDescription, indexPushPosition);

                        } else if (flagDoubleNestesed) {
                            List<String> tmpInnerCTP = getDeducedLDF(currLDFCandInner, currLDFCandOuter, mapMatchedCandLDFtoDed.get(countOuter), indexPushPosition);

                            if (mapMatchedCandLDFtoNewCandidate.get(countInner) != null) {

                                for (int z = 0; z < mapMatchedCandLDFtoNewCandidate.get(countInner).size(); z++) {

                                    List<String> newInnerCTP = getNewInnerCTP(tmpInnerCTP, mapMatchedCandLDFtoNewCandidate.get(countInner).get(z));
                                    setDeducedPairLDF(currLDFCandOuter, newInnerCTP, mapMatchedCandLDFtoDed.get(countOuter), confidence, percentageDescription, indexPushPosition);

                                }
                            }

                            myBasUtils.insertToMap1(mapMatchedCandLDFtoNewCandidate, tmpInnerCTP, countInner);

                        }

                        mapMatchedCandLDFtoDed.put(countInner, mapDedLDFToSerialID.size());
                        alreadyMatchedLDFCand.add(countInner);

//mapMatchedCandLDFtoNewCandidate.put(countInner, deducedLDF);
                    } // If current outer LDF is a membership-type, then search for
                    // a possble intersection of orginal answer vals with inner's
                    // injected vals
                    else if (mapDedLDFToServiceSignature.get(countOuter) != null) {

                        if (getListsIntersec(mapDedLDFToServiceLDFAns.get(mapDedLDFToServiceSignature.get(countOuter)), currAnsInjectInner).size() > 0) {

                            System.out.println("*******************Mebership join **********************************");
                            System.out.println("\t outer LDF: " + currLDFCandOuter + " and key: " + countOuter);
                            System.out.println("\t inner LDF: " + deducedLDF + " and key: " + countInner);
                            System.out.println("*******************Mebership join **********************************");
                            if (!flagDoubleNestesed) {
                                setDeducedPairLDF(currLDFCandOuter, currLDFCandInner, mapMatchedCandLDFtoDed.get(countOuter), 1.0, "(none)", indexPushPosition);

                            } else if (mapMatchedCandLDFtoNewCandidate.get(countInner) != null && flagDoubleNestesed) {
                                List<String> tmpInnerCTP = getDeducedLDF(currLDFCandInner, currLDFCandOuter, mapMatchedCandLDFtoDed.get(countOuter), indexPushPosition);
                                if (mapMatchedCandLDFtoNewCandidate.get(countInner) != null) {
                                    for (int z = 0; z < mapMatchedCandLDFtoNewCandidate.get(countInner).size(); z++) {
                                        List<String> newInnerCTP = getNewInnerCTP(tmpInnerCTP, mapMatchedCandLDFtoNewCandidate.get(countInner).get(z));
                                        setDeducedPairLDF(currLDFCandOuter, newInnerCTP, mapMatchedCandLDFtoDed.get(countOuter), 1.0, "(none)", indexPushPosition);

                                    }
                                }

                                myBasUtils.insertToMap1(mapMatchedCandLDFtoNewCandidate, tmpInnerCTP, countInner);

                            }

                            mapMatchedCandLDFtoDed.put(countInner, mapDedLDFToSerialID.size());
                            alreadyMatchedLDFCand.add(countInner);
                        }
                    }

                }

            }

        }

        mapDedLDFToSerialID.size();

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
    public HashMap<Integer, List<String>> refineLDFCandList(HashMap<Integer, List<String>> mapRawCandidates) {

        List<Integer> mergedService = null;
        HashMap<Integer, List<String>> mapRefined = null;

        mergedService = mergeServiceMembership(mapRawCandidates);

        mapRefined = getRefinedLDFCands(mapRawCandidates, mergedService);

        return mapRefined;
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
    public List<Integer> mergeServiceMembership(HashMap<Integer, List<String>> mapRawCandidates) {

        List<String> currLDFCandOuter = null;
        List<String> currLDFCandInner = null;
        List<Integer> tmpOuter = null;
        List<Integer> tmpInner = null;
        List<Integer> mergedService = new LinkedList<>();
        String outerSubj = "", outerPred = "", outerObj = "";
        String innerSubj = "", innerPred = "", innerObj = "";

        // For all outer candidate LDFs that are possibly "Service"
        for (int countOuter : mapRawCandidates.keySet()) {

            currLDFCandOuter = mapRawCandidates.get(countOuter);

            outerSubj = currLDFCandOuter.get(0);
            outerPred = currLDFCandOuter.get(1);
            outerObj = currLDFCandOuter.get(2);

            // in case the outer LDF Candidate, has not a "POSSIBLY_INJECTED" 
            // to be the inner part of a nested loop
            if (!myBasUtils.elemInListEquals(currLDFCandOuter, "POSSIBLY_INJECTED")) {

                // For all inner candidate LDFs that can possibly be the inner part 
                //of a nested loop
                for (int countInner : mapRawCandidates.keySet()) {

                    currLDFCandInner = mapRawCandidates.get(countInner);

                    innerSubj = currLDFCandInner.get(0);
                    innerPred = currLDFCandInner.get(1);
                    innerObj = currLDFCandInner.get(2);

                    //If outer and inner LDF Candidate habe the same predicate
                    if (currLDFCandInner.get(1).equalsIgnoreCase("http://www.w3.org/1999/02/22-rdf-syntax-ns#type")) {
                        int urer = 0;
                    }

                    if (countOuter != countInner && innerPred.equalsIgnoreCase(outerPred)) {

                        if (myBasUtils.elemInListEquals(currLDFCandInner, "POSSIBLY_INJECTED")) {

                            //Carefull: and also common object or subject ellement
                            // which must not be an "object" or "subject"
                            //         if ((innerObj.equalsIgnoreCase(outerObj) && !innerObj.equalsIgnoreCase("object"))
                            //    || (innerSubj.equalsIgnoreCase(outerSubj) && !outerSubj.equalsIgnoreCase("subject"))) {
                            if ((innerObj.equalsIgnoreCase(outerObj))
                                    || (innerSubj.equalsIgnoreCase(outerSubj))) {

                                System.out.println("***************Meeeeeeeeeeeeeerged, not removed*************************");
                                System.out.println("\t Outer LDF: " + currLDFCandOuter);
                                System.out.println("\t inner LDF: " + currLDFCandInner);
                                System.out.println("***************Meeeeeeeeeeeeeerged, not removed*************************");

                                mergedService.add(countOuter);
                                tmpOuter = mapCandDFToTimeSecs.get(countOuter);
                                tmpInner = mapCandDFToTimeSecs.get(countInner);
                                tmpOuter.addAll(tmpInner);

                                mapCandDFToTimeSecs.put(countInner, tmpOuter);
                                mapDedLDFToServiceLDFAns.put(Integer.toString(countInner) + "_subj", mapCandLDFToAnswerOrig.get(countOuter));
                                mapDedLDFToServiceSignature.put(countInner, Integer.toString(countInner) + "_subj");
                            }
                        }

                    }

                }
            }

        }

        return mergedService;
    }

    /**
     * Get deduced LDF, from corresponding candidadte LDF with "constant values"
     * that were not matched in any nested loop
     *
     * @param candLDF current candidate LDF
     * @param currInjVals list of constant values, that were not injected
     * @return deduced LDF in original format
     */
    public List<String> getDeducedLDFFromOriginal(List<String> candLDF, List<String> currInjVals) {

        List<String> deducedLDF = new LinkedList<>();
        String constVal = "";

        // The constant value, will be the one with minimum occurances 
        if (myBasUtils.elemInListEquals(candLDF, "POSSIBLY_INJECTED")) {

            constVal = getMinOccurances(currInjVals);
        }

        for (int i = 0; i < candLDF.size(); i++) {
            if (candLDF.get(i).equalsIgnoreCase("POSSIBLY_INJECTED")) {

                deducedLDF.add(constVal);
            } else {

                deducedLDF.add(candLDF.get(i));
            }
        }

        return deducedLDF;
    }

    /**
     * Check if outer and inner LDF candidates are temporaly joinables
     *
     * @param outerTimeStamps outer LDF candidate's timestamps
     * @param innerTimeStamps inner LDF candidate's timestamps
     * @return true if they are actually temporaly joinable
     */
    public boolean checkIfTemporalyJoinable(List<Integer> outerTimeStamps, List<Integer> innerTimeStamps) {

        for (int outerTime : outerTimeStamps) {
            for (int innerTime : innerTimeStamps) {

                if ((innerTime - outerTime >= 0) && (innerTime - outerTime) <= windowJoin) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Get the constant with minimal occurances, from list of injection values
     *
     * @param allInjectVals list of injected values
     * @return constant with minimal occurances
     */
    public String getMinOccurances(List<String> allInjectVals) {

        String minOuccured = "";

        for (int i = 0; i < allInjectVals.size(); i++) {

            if (allInjectVals.size() > 1 && (mapAnswerToOccurs.get(allInjectVals.get(i)) != null || mapAnswerToOccurs.get(getShortIRI(allInjectVals.get(i))) != null)) {
            } else {
                minOuccured = allInjectVals.get(i);
            }
        }

        if (allInjectVals.size() == 2) {

            if (getShortIRI(allInjectVals.get(0)).equalsIgnoreCase(allInjectVals.get(1))) {

                minOuccured = allInjectVals.get(0);
            } else if (getShortIRI(allInjectVals.get(1)).equalsIgnoreCase(allInjectVals.get(0))) {
                minOuccured = allInjectVals.get(1);
            }
        }

        return minOuccured;
    }

    /**
     * Set Deduced LDF from inner Candidate LDF, and deduced pairJoin from
     * identifed nested loop between outer and inner LDFs
     *
     * @param currLDFCandOuter outer candidate LDF
     * @param currLDFCandInner inner candidate LDF
     * @param deducedIDOuter outer candidate LDF's corresponding deduced LDF id
     * @throws IOException
     */
    public List<String> setDeducedPairLDF(List<String> currLDFCandOuter, List<String> currLDFCandInner, int deducedIDOuter, double confidence, String pushedPercentage, int indexPushPosition) throws IOException {

        List<String> deducedLDF = getDeducedLDF(currLDFCandInner, currLDFCandOuter, deducedIDOuter, indexPushPosition);

        mapDeducdLDFToCondifence.put(deducedLDF, confidence);
        mapDeducdLDFToPushedPercentage.put(deducedLDF, pushedPercentage);

        List< List<String>> pairJoin = getJoinPair(deducedLDF, mapDedLDFToSerialID.get(deducedIDOuter));

        if (simpleExecution) {

            writePairToFile(pairJoin);
        } else {

            countDeducedLDFOccurs(pairJoin);
        }

        if (!myBasUtils.elemInListContained(deducedLDF, "POSSIBLY")) {

            mapDedLDFToLDFServer.put(mapDedLDFToSerialID.size() + 1, mapCandLDFToLDFServer.get(mapCandLDFToSerialID.get(currLDFCandOuter)));
            mapDedLDFToSerialID.put(mapDedLDFToSerialID.size() + 1, deducedLDF);
        }

        return deducedLDF;
    }

    /**
     * Append in the file of identified pairJoins, this current join
     *
     * @param pairJoin current pair join
     * @throws IOException
     */
    public void writePairToFile(List<List<String>> pairJoin) throws IOException {

        bufferWritter.write(pairJoin.get(0).get(0) + " " + pairJoin.get(0).get(1) + " " + pairJoin.get(0).get(2) + ", " + pairJoin.get(1).get(0) + " " + pairJoin.get(1).get(1) + " " + pairJoin.get(1).get(2) + "\n");
    }

    /**
     * Count occurances of deduced LDF, based on groundTruth pairs occurances
     *
     * @param pairJoin pair of
     */
    public void countDeducedLDFOccurs(List<List<String>> pairJoin) {

        if (mapGroundTruthPairsLDF.get(pairJoin) != null) {

            if (mapPairJoinToOccurs.get(pairJoin) == null) {

                mapPairJoinToOccurs.put(pairJoin, 1);
                mapPairJoinToOccurs.put(getSymLDF(pairJoin), 1);

                truePositivesPairs++;
            } else if (mapPairJoinToOccurs.get(pairJoin) < mapGroundTruthPairsLDF.get(pairJoin)) {
                truePositivesPairs++;

                int occurs1 = mapPairJoinToOccurs.get(pairJoin);
                int occurs2 = mapPairJoinToOccurs.get(getSymLDF(pairJoin));
                mapPairJoinToOccurs.put(pairJoin, occurs1 + 1);
                mapPairJoinToOccurs.put(getSymLDF(pairJoin), occurs2 + 1);
            }

        }

        totalPairs++;
    }

    /**
     * Get symmetric pair of joined LDFs
     *
     * @param currJoin original pair join
     * @return symmetric pair of joined LDFs
     */
    public List<List<String>> getSymLDF(List<List<String>> currJoin) {

        List<List<String>> symJoin = new LinkedList<>();

        symJoin.add(currJoin.get(1));
        symJoin.add(currJoin.get(0));

        return symJoin;
    }

    /**
     * Get refined pair of joins, to be compared
     *
     * @param outerLDF outer deduced LDF, in detailed format
     * @param innerLDF inner deduced LDF, in detailed format
     * @return pair pf refined joined LDFs
     */
    public List<List<String>> getJoinPair(List<String> outerLDF, List<String> innerLDF) {

        List<List<String>> pairJoin = new LinkedList<>();
        List<String> outerFinal = new LinkedList<>();
        List<String> innerFinal = new LinkedList<>();

        outerFinal = getCleanLDF(outerLDF);
        innerFinal = getCleanLDF(innerLDF);

        pairJoin.add(outerFinal);
        pairJoin.add(innerFinal);

        return pairJoin;
    }

    /**
     * Get a deduced LDF in a simplier format
     *
     * @param currLDF current deduced LDF
     * @return refined LDF
     */
    public List<String> getCleanLDF(List<String> currLDF) {

        List<String> outerFinal = new LinkedList<>();

        for (int i = 0; i < currLDF.size(); i++) {

            if (i == 0) {

                if (currLDF.get(i).contains("INJECTED")) {

                    outerFinal.add("injected");
                } else if (currLDF.get(i).equalsIgnoreCase("subject")) {

                    outerFinal.add("subject");
                } else {

                    outerFinal.add(currLDF.get(i));
                }

            } else if (i == 2) {

                if (currLDF.get(i).contains("INJECTED")) {

                    outerFinal.add("injected");
                } else if (currLDF.get(i).contains("object")) {
                    outerFinal.add("object");
                } else {

                    outerFinal.add(currLDF.get(i));
                }

            } else {

                outerFinal.add(currLDF.get(i));
            }

        }

        return outerFinal;
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
    public HashMap<Integer, List<String>> getRefinedLDFCands(HashMap<Integer, List<String>> mapRawCandidates, List<Integer> mergedService) {

        HashMap<Integer, List<String>> mapRefined = new HashMap<>();
        HashMap<Integer, List<String>> mapLDFCandidateToInjectFinal = new HashMap<>();
        HashMap<Integer, List<String>> mapLDFCandidateToInjectFinalSubject = new HashMap<>();
        HashMap<Integer, List<String>> mapLDFCandidateToInjectFinalObject = new HashMap<>();
        HashMap<Integer, List<String>> mapLDFCandidateToAnswerOrigFinal = new HashMap<>();
        HashMap<List<String>, Integer> mapLDFCandidateSerialFinal = new HashMap<>();
        HashMap<String, List<String>> mapDedLDFToServiceLDFAnsFinal = new HashMap<>();
        HashMap<Integer, String> mapDedLDFToServiceSignatureFinal = new HashMap<>();
        HashMap<Integer, List<Integer>> mapCandDFToTimeSecsFinal = new HashMap<>();

        String currServSign = "";
        int countNew = 0;

        for (int countOuter : mapRawCandidates.keySet()) {

            //If candidate LDF
            if (!myBasUtils.elemInListEquals(mergedService, countOuter)) {

                countNew++;
                mapRefined.put(countNew, mapRawCandidates.get(countOuter));

                if (mapCandLDFToInjectValsSUbject.get(countOuter) != null && mapCandLDFToInjectValsObject.get(countOuter) != null) {

                    mapLDFCandidateToInjectFinalSubject.put(countNew, mapCandLDFToInjectValsSUbject.get(countOuter));
                    mapLDFCandidateToInjectFinalObject.put(countNew, mapCandLDFToInjectValsObject.get(countOuter));
                }

                mapLDFCandidateToInjectFinal.put(countNew, mapCandLDFToInjectVals.get(countOuter));
                mapLDFCandidateToAnswerOrigFinal.put(countNew, mapCandLDFToAnswerOrig.get(countOuter));
                mapCandDFToTimeSecsFinal.put(countNew, mapCandDFToTimeSecs.get(countOuter));
                mapLDFCandidateSerialFinal.put(mapRawCandidates.get(countOuter), countNew);

                // save mappings of the corresponding "service" LDF candidate, 
                // to this corresponding "membership-type" LDF candidate
                if (mapDedLDFToServiceSignature.get(countOuter) != null) {

                    currServSign = mapDedLDFToServiceSignature.get(countOuter).substring(mapDedLDFToServiceSignature.get(countOuter).indexOf("_"));
                    mapDedLDFToServiceLDFAnsFinal.put(Integer.toString(countNew) + currServSign, mapDedLDFToServiceLDFAns.get(mapDedLDFToServiceSignature.get(countOuter)));
                    mapDedLDFToServiceSignatureFinal.put(countNew, Integer.toString(countNew) + currServSign);
                }

            }
        }

        mapCandLDFToInjectVals = new HashMap<>();
        mapCandLDFToInjectValsSUbject = new HashMap<>();
        mapCandLDFToInjectValsObject = new HashMap<>();
        mapCandLDFToAnswerOrig = new HashMap<>();
        mapCandLDFToSerialID = new HashMap<>();
        mapDedLDFToServiceLDFAns = new HashMap<>();
        mapDedLDFToServiceSignature = new HashMap<>();
        mapCandDFToTimeSecs = new HashMap<>();

        mapCandDFToTimeSecs = (HashMap<Integer, List<Integer>>) mapCandDFToTimeSecsFinal.clone();
        mapCandLDFToInjectVals = (HashMap<Integer, List<String>>) mapLDFCandidateToInjectFinal.clone();
        mapCandLDFToInjectValsObject = (HashMap<Integer, List<String>>) mapLDFCandidateToInjectFinalObject.clone();
        mapCandLDFToInjectValsSUbject = (HashMap<Integer, List<String>>) mapLDFCandidateToInjectFinalSubject.clone();
        mapCandLDFToAnswerOrig = (HashMap<Integer, List<String>>) mapLDFCandidateToAnswerOrigFinal.clone();
        mapCandLDFToSerialID = (HashMap<List<String>, Integer>) mapLDFCandidateSerialFinal.clone();
        mapDedLDFToServiceLDFAns = (HashMap<String, List<String>>) mapDedLDFToServiceLDFAnsFinal.clone();
        mapDedLDFToServiceSignature = (HashMap<Integer, String>) mapDedLDFToServiceSignatureFinal.clone();

        return mapRefined;
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
    public boolean isLDFpreceeding(List<Integer> outerLogEntries, List<Integer> innerLogEntries) {

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
     * Get LDF in deduced format:
     *
     * <orig_or_injected_subject_var> <predicate> <orig_or_injected_object_var>
     *
     * @param LDFCandInner inner LDF Candidate of the nested loop
     * @param LDFCandOuter outer LDF Candidate of the nested loop
     * @param indxOuter index/id of outer LDF Candidate
     * @return LDF in deduced format
     */
    public List<String> getDeducedLDF(List<String> LDFCandInner, List<String> LDFCandOuter, int indxOuter, int indexPushPosition) {

        List<String> deduecedLDF = new LinkedList<>();
        String matchingVar = "";

        //Create deduced variable of nested loop
        if (LDFCandOuter.get(0).contains("subject")) {

            matchingVar = "INJECTED_FROM_" + LDFCandOuter.get(0) + "_LDF_" + Integer.toString(indxOuter);

        } else if (LDFCandOuter.get(2).contains("object")) {

            matchingVar = "INJECTED_FROM_" + LDFCandOuter.get(2) + "_LDF_" + Integer.toString(indxOuter);
        } else if (!(LDFCandOuter.get(0).contains("subject") && LDFCandOuter.get(2).contains("object"))) {

            matchingVar = "INJECTED_FROM_LDF_" + Integer.toString(indxOuter);
        }

        // Create new deduced LDF
        for (int i = 0; i < LDFCandInner.size(); i++) {

            if (LDFCandInner.get(i).contains("POSSIBLY_INJECTED") && !matchingVar.equalsIgnoreCase("") && indexPushPosition == i) {

                deduecedLDF.add(matchingVar);
            } else {

                deduecedLDF.add(LDFCandInner.get(i));
            }
        }

        return deduecedLDF;
    }

    /**
     * Convert candidate LDFs from <List<String>,int>, into <int,List<String>>
     *
     * @param inputMap raw candidate LDF map
     * @return sorted candidate LDF map, on integer
     */
    public HashMap<Integer, List<String>> sortMapBasedOnID(HashMap< List<String>, Integer> inputMap) {

        int cnt = 1;
        List<Integer> alreadySeen = new LinkedList<>();
        HashMap<Integer, List<String>> sortedMap = new HashMap<>();

        while (cnt <= inputMap.size()) {

            for (List<String> currLDFCandOuter : inputMap.keySet()) {

                if (inputMap.get(currLDFCandOuter) == cnt) {

                    sortedMap.put(cnt, currLDFCandOuter);
                    cnt++;
                    if (!alreadySeen.contains(cnt)) {

                        alreadySeen.add(cnt);
                    }
                }
            }
        }

        return sortedMap;
    }

    /**
     * Show all deduced LDFs and identify those that do not participated in any
     * nested loop
     *
     * @throws java.io.IOException
     */
    public void showDeducedLDFs() throws IOException {

        List<String> currLDFfrag = null;
        List<List<Integer>> allDedGraph = null;
        List<String> currLDFfragFinal = null;
        bufferWritter.close();

        HashMap< Integer, List<String>> mapLDFDeducedFinal = (HashMap< Integer, List<String>>) mapDedLDFToSerialID.clone();

        bufferWritterBGP.write("\t----------Deduced BGPs----------\n\n");
        System.out.println("\n\t\t############################");
        System.out.println("\t\t############################\n");
        System.out.println("\t----------Deduced BGPs----------\n");

        // get all deduced graphs from nested loops between deduced LDFs
        allDedGraph = constructGraphs(mapLDFDeducedFinal);

        numBGP = allDedGraph.size();

        for (int l = 0; l < allDedGraph.size(); l++) {

            //Get all LDF's not participating in any nested loop
            if (allDedGraph.get(l).size() == 1) {

                for (Integer curLDF : mapLDFDeducedFinal.keySet()) {

                    if (myBasUtils.elemInListEquals(allDedGraph.get(l), curLDF)) {

                        if (mapDedLDFNotInNESTED.get(mapLDFDeducedFinal.get(curLDF)) == null) {

                            mapDedLDFNotInNESTEDServer.put(mapLDFDeducedFinal.get(curLDF), mapCandLDFToLDFServer.get(curLDF));
                            mapDedLDFNotInNESTED.put(mapLDFDeducedFinal.get(curLDF), mapDedLDFNotInNESTED.size() + 1);
                        }
                    }

                }
            } //Or else show all deduced BGPs 
            else {

                double BGPconfidece = 1.0;

                for (Integer curLDF : mapLDFDeducedFinal.keySet()) {

                    if (myBasUtils.elemInListEquals(allDedGraph.get(l), curLDF)) {

                        currLDFfrag = mapLDFDeducedFinal.get(curLDF);
                        currLDFfragFinal = getLDFfinal(currLDFfrag);

                        //   System.out.println("currLDFfrag: "+currLDFfrag);
                        BGPconfidece = BGPconfidece * mapDeducdLDFToCondifence.get(currLDFfrag);

                    }
                }

                DecimalFormat df = new DecimalFormat("#.##");

                // bufferWritterBGP.write("\t\t BGP [no" + (l + 1) + "] with confidence: "+df.format(BGPconfidece)+ "]\n");
              //  System.out.println("\t\t BGP [no" + (l + 1) + "] with confidence: "+df.format(BGPconfidece));              
                bufferWritterBGP.write("\t\t BGP [no" + (l + 1) + "] + \n");

                System.out.println("\t\t BGP [no" + (l + 1) + "]");

                for (Integer curLDF : mapLDFDeducedFinal.keySet()) {

                    if (myBasUtils.elemInListEquals(allDedGraph.get(l), curLDF)) {

                        currLDFfrag = mapLDFDeducedFinal.get(curLDF);
                        currLDFfragFinal = getLDFfinal(currLDFfrag);

                        double LDFcondifence = mapDeducdLDFToCondifence.get(currLDFfrag);

                        String confidenceDescr = mapDeducdLDFToPushedPercentage.get(currLDFfrag);

                        DecimalFormat df2 = new DecimalFormat("#.##");

                        //System.out.println("XXXXX: "+currLDFfragFinal);
                        // System.out.println("zzzzz: "+currLDFfrag);
                        bufferWritterBGP.write("\t\t\t Deduced LDF_" + curLDF + ": " + currLDFfragFinal.get(0) + "     " + currLDFfragFinal.get(1) + "     " + currLDFfragFinal.get(2) + "\n");

                        System.out.println("\t\t\t Deduced LDF_" + curLDF + ": " + currLDFfragFinal.get(0) + "     " + currLDFfragFinal.get(1) + "     " + currLDFfragFinal.get(2));
                        // bufferWritterBGP.write("\t\t\t\t received @" + mapDedLDFToLDFServer.get(curLDF)+" with confidence: "+df2.format(LDFcondifence)+" "+confidenceDescr+"\n");

                        bufferWritterBGP.write("\t\t\t\t received @" + mapDedLDFToLDFServer.get(curLDF) + "\n");

                        if (confidenceDescr == null) {
                            confidenceDescr = "(whithout poushed values)";
                        }
                        System.out.println("\t\t\t\t received @" + mapDedLDFToLDFServer.get(curLDF));

                       // System.out.println("\t\t\t\t received @" + mapDedLDFToLDFServer.get(curLDF)+" with confidence: "+df2.format(LDFcondifence)+" "+confidenceDescr);
                        // System.out.println(mapCandLDFToAnswerOrig.get(curLDF));
                    }
                }
            }

        }

        bufferWritterBGP.write("\n\t----------Deduced BGPs----------" + "\n");

        System.out.println("\n\t----------Deduced BGPs----------");
    }

    /**
     *
     * @param ldfdeduced
     * @return
     */
    public List<String> getLDFfinal(List<String> ldfdeduced) {

        List<String> currLDFfragFinal = new LinkedList<>();

        for (int i = 0; i < ldfdeduced.size(); i++) {

            if (i == 1) {
                currLDFfragFinal.add(ldfdeduced.get(i));
            } else if (ldfdeduced.get(i).equalsIgnoreCase("subject")) {
                currLDFfragFinal.add("?s");

            } else if (ldfdeduced.get(i).equalsIgnoreCase("object")) {

                currLDFfragFinal.add("?o");

            } else if (ldfdeduced.get(i).contains("INJECTED_FROM_")) {

                if (ldfdeduced.get(i).contains("subject")) {

                    currLDFfragFinal.add("INJECTEDsubj(" + (ldfdeduced.get(i).substring(ldfdeduced.get(i).indexOf("LDF"))) + ")");

                } else if (ldfdeduced.get(i).contains("object")) {
                    currLDFfragFinal.add("INJECTEDobj(" + (ldfdeduced.get(i).substring(ldfdeduced.get(i).indexOf("LDF"))) + ")");

                } else {
                    currLDFfragFinal.add("INJECTEDsubj(" + (ldfdeduced.get(i).substring(ldfdeduced.get(i).indexOf("LDF"))) + ")");

                }

            } else {
                currLDFfragFinal.add(ldfdeduced.get(i));
            }

        }

        return currLDFfragFinal;

    }

    /**
     * Show only predicate LDFs
     *
     * @throws java.io.IOException
     */
    public void showNotInNestedLoopLDFs() throws IOException {

        int count = 1;
        List<String> currLDFfragFinal = new LinkedList<>();

        bufferWritterBGP.write("\n\t\t############################" + "\n");
        bufferWritterBGP.write("\t\t############################\n" + "\n");

        bufferWritterBGP.write("\t----------Single LDFs (not participating in any Nested Loop)----------\n" + "\n");

        System.out.println("\n\t\t############################");
        System.out.println("\t\t############################\n");
        System.out.println("\t----------Single LDFs (not participating in any Nested Loop)----------\n");

        count = 1;
        for (List<String> currLDFfrag : mapDedLDFNotInNESTED.keySet()) {

            currLDFfragFinal = getLDFfinal(currLDFfrag);

            bufferWritterBGP.write("\t\t\t  Deduced LDF_" + count + ": " + currLDFfragFinal.get(0) + "    " + currLDFfragFinal.get(1) + "     " + currLDFfragFinal.get(2) + "\n");

            System.out.println("\t\t\t  Deduced LDF_" + count + ": " + currLDFfragFinal.get(0) + "    " + currLDFfragFinal.get(1) + "     " + currLDFfragFinal.get(2));
            Double LDFcondifence = mapDeducdLDFToCondifence.get(currLDFfrag);

            DecimalFormat df2 = new DecimalFormat("#.##");

            //bufferWritterBGP.write("\t\t\t\t  received @" + mapDedLDFNotInNESTEDServer.get(currLDFfrag)+"with confidence: "+df2.format(LDFcondifence)+"\n");
            // System.out.println("\t\t\t\t  received @" + mapDedLDFNotInNESTEDServer.get(currLDFfrag)+"with confidence: "+df2.format(LDFcondifence));
            bufferWritterBGP.write("\t\t\t\t  received @" + mapDedLDFNotInNESTEDServer.get(currLDFfrag) + "\n");
            System.out.println("\t\t\t\t  received @" + mapDedLDFNotInNESTEDServer.get(currLDFfrag));
            count++;
        }

        bufferWritterBGP.write("\t----------Single LDFs (not participating in any Nested Loop)----------\n" + "\n");

        System.out.println("\t----------Single LDFs (not participating in any Nested Loop)----------\n");
        bufferWritterBGP.close();
        writerBGP.close();

        // For concurrent execution, generate precison/recall statistics
        if (!simpleExecution) {

            generateGNUFinal();
        }
    }

    /**
     * Set all predicates' prefixes that are present in the LDF servers
     */
    public void setLDFpredicates() {

        mapLDFpredicates.put("dc11:rights", 1);
        mapLDFpredicates.put("dbpedia-owl:Image", 1);
        mapLDFpredicates.put("foaf:thumbnail", 1);
        mapLDFpredicates.put("rdfs:label", 1);
        mapLDFpredicates.put("dbpedia-owl:Actor", 1);
        mapLDFpredicates.put("dbpedia-owl:AdministrativeRegion", 1);
        mapLDFpredicates.put("dbpedia-owl:AdultActor", 1);
        mapLDFpredicates.put("dbpedia-owl:Agent", 1);
        mapLDFpredicates.put("dbpedia-owl:Agglomeration", 1);
        mapLDFpredicates.put("dbpedia-owl:Aircraft", 1);
        mapLDFpredicates.put("dbpedia-owl:Airline", 1);
        mapLDFpredicates.put("dbpedia-owl:Airport", 1);
        mapLDFpredicates.put("dbpedia-owl:starring", 1);
        mapLDFpredicates.put("dbpedia-owl:AcademicJournal", 1);
        mapLDFpredicates.put("dbpedia-owl:director", 1);
        mapLDFpredicates.put("dbpedia-owl:Activity", 1);
        mapLDFpredicates.put("a", 1);
        mapLDFpredicates.put("dbpedia-owl:Airline", 1);
        mapLDFpredicates.put("dbpedia-owl:Airline", 1);
        mapLDFpredicates.put("foaf:surname", 1);
    }

    /**
     * Set each complete predicate IRI to corresponding prefix
     */
    public void setAuthorityToPrefix() {

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
     * Set each prefixe to corresponding complete predicate IRI
     */
    public void setPrefixToAuthority() {

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

    /**
     * Construct all possible deduced graphs/bgps of Deduced LDFs, based on key
     * word "INJECTED_FROM" between each pair of deduced LDfs
     *
     * @param mapLDFDeducedFinal map of deduced LDFs
     * @return list of deduced graphs/bgps of ids of deduced LDFs
     * @throws IOException
     */
    public List<List<Integer>> constructGraphs(HashMap< Integer, List<String>> mapLDFDeducedFinal) throws IOException {

        List<List<Integer>> dedGraph = new LinkedList<>();

        //init maps regarding each deduced LDF and corresponding graph/bgp 
        for (int dedLDF : mapLDFDeducedFinal.keySet()) {

            mapDedLDFToDedGraph.put(dedLDF, -1);
        }

        //Init the first deduced graph with the first identified deduced LDF
        dedGraph.add(new LinkedList<Integer>());
        dedGraph.get(0).add(1);
        mapDedLDFToDedGraph.put(1, dedGraph.size() - 1);

        for (int dedLDFOuter : mapLDFDeducedFinal.keySet()) {

            // Check if current deduced LDF can be added in an existing graph
            checkIfCanAddInDedGraph(dedGraph, dedLDFOuter, mapLDFDeducedFinal);

            //if not, create a new graph for current deduced LDF
            if (mapDedLDFToDedGraph.get(dedLDFOuter) == -1) {

                dedGraph.add(new LinkedList<Integer>());
                dedGraph.get(dedGraph.size() - 1).add(dedLDFOuter);
                mapDedLDFToDedGraph.put(dedLDFOuter, dedGraph.size() - 1);
            }

            // for the rest of deudced LDF that have not been matched into a graph
            // try to see if they can be added into an existng one
            for (int dedLDFInner : mapLDFDeducedFinal.keySet()) {

                if (mapDedLDFToDedGraph.get(dedLDFInner) == -1) {

                    if (testNestedLoop(mapLDFDeducedFinal.get(dedLDFOuter), dedLDFOuter, mapLDFDeducedFinal.get(dedLDFInner), dedLDFInner)) {

                        dedGraph.get(mapDedLDFToDedGraph.get(dedLDFOuter)).add(dedLDFInner);
                        mapDedLDFToDedGraph.put(dedLDFInner, mapDedLDFToDedGraph.get(dedLDFOuter));
                    }
                }

            }
        }
        //print deduced graphs/bgps
        printGraph(dedGraph);

        return dedGraph;
    }

    /**
     * Check if the current deduced LDF can be added in a existing graph
     *
     * @param dedGraph list of all deduced graph/bgps
     * @param currLDFID current deduced LDF's id
     * @param mapLDFDeducedFinal map of deduced LDFs
     * @return true if current LDF has added in an existing graph
     */
    public boolean checkIfCanAddInDedGraph(List<List<Integer>> dedGraph, int currLDFID, HashMap< Integer, List<String>> mapLDFDeducedFinal) {

        boolean flagFoundGraph = false;
        int tmpIDquery = -1;
        List<Integer> matchedGraphs = new LinkedList<>();

        //For every deduced graph
        for (int i = 0; i < dedGraph.size(); i++) {

            flagFoundGraph = false;

            //From the last to the first graph's dediced LDF id
            for (int j = dedGraph.get(i).size() - 1; j >= 0; j--) {

                if (flagFoundGraph) {
                    break;
                }

                //If streaming option is not enabled, we add the graph 
                //to the first matching graph
                tmpIDquery = dedGraph.get(i).get(j);

                if (tmpIDquery != currLDFID && !Objects.equals(mapDedLDFToDedGraph.get(currLDFID), mapDedLDFToDedGraph.get(tmpIDquery))) {

                    if (testNestedLoop(mapLDFDeducedFinal.get(currLDFID), currLDFID, mapLDFDeducedFinal.get(tmpIDquery), tmpIDquery)) {

                        matchedGraphs.add(i);
                        flagFoundGraph = true;
                    }
                }

            }

        }

        //For all matched graphs, merged to the first all deduced IDs of all the others
        for (int h = 0; h < matchedGraphs.size(); h++) {
            if (h == 0) {

                if (!Objects.equals(mapDedLDFToDedGraph.get(currLDFID), matchedGraphs.get(0))) {
                    dedGraph.get(matchedGraphs.get(h)).add(currLDFID);
                    mapDedLDFToDedGraph.put(currLDFID, matchedGraphs.get(h));
                    flagFoundGraph = true;
                }

            } else {

                int k = matchedGraphs.get(h);
                dedGraph.get(matchedGraphs.get(0)).addAll(dedGraph.get(k));
            }
        }

        //Then, for all matched graphs, delete all graphs except the first one
        if (matchedGraphs.size() > 1) {
            for (int h = 1; h < matchedGraphs.size(); h++) {

                int k = matchedGraphs.get(h);
                dedGraph.remove(k);
            }
        }

        //reset for all deduced IDs, the coresponding map concerning the deduced graph
        if (matchedGraphs.size() > 0) {
            for (int i = 0; i < dedGraph.size(); i++) {

                for (int j = 0; j < dedGraph.get(i).size(); j++) {

                    mapDedLDFToDedGraph.put((dedGraph.get(i).get(j)), i);
                }
            }
        }

        return flagFoundGraph;
    }

    /**
     * Check if two deduced LDF are joined through a nested loop
     *
     * @param outerLDF outer deduced LDF to be compared
     * @param outerKey outer deduced LDF's id
     * @param innerLDF inner query to be compared
     * @param innerKey inner deduced LDF's id
     * @return true if they are joined through a nested loop
     */
    public boolean testNestedLoop(List<String> outerLDF, int outerKey, List<String> innerLDF, int innerKey) {

        boolean ret = false;
        int innerInjVar = -1;

        for (int j = 0; j < innerLDF.size(); j++) {
            if (innerLDF.get(j).contains("INJECTED_FROM_")) {

                innerInjVar = Integer.parseInt(innerLDF.get(j).substring(innerLDF.get(j).indexOf("_LDF_") + 5));
                if (innerInjVar == outerKey) {
                    return true;
                }
            }
        }

        return ret;
    }

    /**
     * Print each graph's deduced LDFs' ids
     *
     * @param dedGraph all deduced graphs to be printed
     */
    public void printGraph(List<List<Integer>> dedGraph) {

        int size = 0;

        for (int i = 0; i < dedGraph.size(); i++) {

            size = 0;
            Collections.sort(dedGraph.get(i));
            System.out.println("\t Deduced Graph No " + (i + 1));
            System.out.print("\t [ ");

            for (int m = 0; m < dedGraph.get(i).size(); m++) {

                if (m != dedGraph.get(i).size() - 1) {

                    System.out.print(dedGraph.get(i).get(m) + ", ");
                } else {

                    System.out.print(dedGraph.get(i).get(m));
                }

                size += 2 + dedGraph.get(i).get(m).toString().length();
                if (size > 100) {

                    size = 0;
                    System.out.println("");
                    System.out.print("\t  ");
                }
            }

            System.out.print(" ]");
            System.out.println();
        }

        System.out.println();
    }

    /**
     * Compare two lists of strings, for finding their intersection's elements
     *
     * @param outerList outer list to be compared
     * @param innerList inner list to be compared
     * @return intersection of the two lists
     */
    public List<String> getListsIntersec(List<String> outerList, List<String> innerList) {

        List<String> matchedValuesOuter = new LinkedList<>();
        List<String> matchedValuesInner = new LinkedList<>();

        if (outerList.size() >= innerList.size()) {
            matchedValuesOuter = myBasUtils.refineList(outerList);
            matchedValuesInner = myBasUtils.refineList(innerList);

        } else if (outerList.size() < innerList.size()) {

            matchedValuesOuter = myBasUtils.refineList(innerList);
            matchedValuesInner = myBasUtils.refineList(outerList);
        }

        matchedValuesOuter.retainAll(matchedValuesInner);

        return matchedValuesOuter;
    }

    /**
     * Set all info to corresponding LDF candidate:
     *
     * <orig_or_unkonw_subject_var> <predicate> <orig_or_unkonw_object_var>
     *
     */
    public void removeRedundancyLDFInfo() {

        for (List<String> currLDFfrag : mapCandLDFToSerialID.keySet()) {

            List<Integer> tmpAllTimeStamps = myBasUtils.sortAndRemoveRedundancy2(mapCandDFToTimeSecs.get(mapCandLDFToSerialID.get(currLDFfrag)));
            mapCandDFToTimeSecs.put(mapCandLDFToSerialID.get(currLDFfrag), tmpAllTimeStamps);

            List<String> tmpAllLDFServers = myBasUtils.sortAndRemoveRedundancy(mapCandLDFToLDFServer.get(mapCandLDFToSerialID.get(currLDFfrag)));
            mapCandLDFToLDFServer.put(mapCandLDFToSerialID.get(currLDFfrag), tmpAllLDFServers);

            List<String> tmpInjectVals = myBasUtils.sortAndRemoveRedundancy(addAllShortIRIs(mapCandLDFToInjectVals.get(mapCandLDFToSerialID.get(currLDFfrag))));
            mapCandLDFToInjectVals.put(mapCandLDFToSerialID.get(currLDFfrag), tmpInjectVals);

            List<String> tmpOriganlAns = myBasUtils.sortAndRemoveRedundancy(addAllShortIRIs(mapCandLDFToAnswerOrig.get(mapCandLDFToSerialID.get(currLDFfrag))));
            mapCandLDFToAnswerOrig.put(mapCandLDFToSerialID.get(currLDFfrag), tmpOriganlAns);
        }

    }

    /**
     * Get apairs of ground truth pair joins, and their occurances, from input
     * file
     *
     * @throws FileNotFoundException
     * @throws IOException
     */
    public void getGroundTruthPairs() throws FileNotFoundException, IOException {

        String fileName = "groundTruth.txt";
        BufferedReader br = null;
        String sCurrentLine;
        List<String> outerTP = new LinkedList<>();
        List<String> innerTP = new LinkedList<>();
        List<List<String>> tmpPair = new LinkedList<>();
        List<List<String>> tmpPair2 = new LinkedList<>();
        int cntPairs = 0;

        System.out.println("*******START: Ground Truth joinedpairs********");
        try {

            br = new BufferedReader(new FileReader(fileName));

            while ((sCurrentLine = br.readLine()) != null) {

                String s = sCurrentLine;
                String[] array = s.split(", ");
                outerTP = new LinkedList<>();
                innerTP = new LinkedList<>();

                //BUUUUUUUUUUUUG
                for (String str : array) {

                    String[] array2 = s.split(" ");

                    for (String str2 : array2) {
                        if (str2.contains(",")) {
                            str2 = str2.substring(0, str2.indexOf(","));
                        }
                        if (str2.contains("_") && !str2.contains("http") && !str2.contains("predicate")) {
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

                if (mapGroundTruthPairsLDF.get(tmpPair) == null && mapGroundTruthPairsLDF.get(tmpPair2) == null) {

                    mapGroundTruthPairsLDF.put(tmpPair, 1);
                    mapGroundTruthPairsLDF.put(tmpPair2, 1);
                    cntPairs++;

                    System.out.println("\t Join pair no[" + cntPairs + "]: " + tmpPair);
                } else {

                    int occurs1 = mapGroundTruthPairsLDF.get(tmpPair);
                    int occurs2 = mapGroundTruthPairsLDF.get(tmpPair2);
                    mapGroundTruthPairsLDF.replace(tmpPair, occurs1 + 1);
                    mapGroundTruthPairsLDF.replace(tmpPair2, occurs2 + 1);

                    cntPairs++;
                    System.out.println("\t Join pair no[" + cntPairs + "]: " + tmpPair);
                }

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
     * Calculate precision/recall of ground truth pairs
     *
     * @throws IOException
     */
    public void generateGNUFinal() throws IOException {

        // number of groundTruthPaires
        int groundTruthPairs = 0;
        // 2, counting the symmetric pair
        // groundTruthPairs = (mapGroundTruthPairsLDF.size()) / 2;
        for (List<List<String>> curkey : mapGroundTruthPairsLDF.keySet()) {

            groundTruthPairs += mapGroundTruthPairsLDF.get(curkey);
        }

        groundTruthPairs = groundTruthPairs / 2;

        missedGroundTruthPairs();

        if (!simpleExecution) {
            System.out.println("****************************LDF statistics: *************************");

            System.out.println("\t [a] All different BGPs: " + numBGP + "\n");
            System.out.println("\t [b] All different pairs: " + totalPairs + "\n");
            System.out.println("\t \t true positives pairs: " + truePositivesPairs + "\n");

            float answer = ((float) truePositivesPairs) / totalPairs;
            String out = String.format("%.2f", answer);
            System.out.println("\t [1] Precision in deduced pairJoins: " + out + "\n");

            answer = ((float) truePositivesPairs) / groundTruthPairs;
            out = String.format("%.2f", answer);
            System.out.println("\t [2] Recall in deduced pairJoins: " + out + "\n");

            System.out.println("****************************LDF statistics: *************************");
        }

    }

    /**
     * Show missed ground truth pairs
     */
    public void missedGroundTruthPairs() {

        System.out.println("****************************Pair joins missed *************************");

        for (List<List<String>> currObs : mapGroundTruthPairsLDF.keySet()) {

            if (mapPairJoinToOccurs.get(currObs) == null) {

                System.out.println("\t missed pair " + currObs + "\n");
            }

        }

        System.out.println("****************************Pair joins missed *************************");

    }

    private List<String> getNewInnerCTP(List<String> tmpLDFCandInner, List<String> previousLDFCandInner) {

        List<String> finalCTPinner = new LinkedList<>();

        for (int i = 0; i < 3; i++) {

            if (tmpLDFCandInner.get(i).contains("POSSIBLY") && !previousLDFCandInner.get(i).contains("POSSIBLY")) {
                finalCTPinner.add(previousLDFCandInner.get(i));
            } else if (!tmpLDFCandInner.get(i).contains("POSSIBLY") && previousLDFCandInner.get(i).contains("POSSIBLY")) {
                finalCTPinner.add(tmpLDFCandInner.get(i));
            } else {
                finalCTPinner.add(tmpLDFCandInner.get(i));
            }

        }

        return finalCTPinner;
    }

    /**
     * @param myMap
     * @return
     */
    /*  public HashMap< Integer, List<String>> refineDeducedLDF2(HashMap< Integer, List<String>> myMap) {

     HashMap< Integer, List<String>> tmpMap = new HashMap<>();

     for (int indxLDF : myMap.keySet()) {

     List<String> myDedLDF = myMap.get(indxLDF);
     List<String> newDefLDF = new LinkedList<>();

     for (int i = 0; i < myDedLDF.size(); i++) {

     String currEntity = myDedLDF.get(i);

     newDefLDF.add(currEntity);
     }
     tmpMap.put(indxLDF, newDefLDF);
     }

     return tmpMap;
     }*/
}