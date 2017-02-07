package mylift;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

/**
 * Class with basic hel functions
 *
 * @author Nassopoulos Georges
 * @version 1.0
 * @since 2016-12-15
 */
public class BasicUtilis {

    private static final int MIN_IN_SEC = 60;
    private static final int HOUR_IN_SEC = 3600;

    // enum with triple pattern entites
    public enum TriplePattern {

        SUBJECT(0), PREDICATE(1), OBJECT(2);

        private final int value;

        private TriplePattern(int value) {

            this.value = value;
        }

        public int getValue() {

            return value;
        }
    }

    /**
     * Convert timeStamp from HH:MM:SS format into total number of seconds
     *
     * @param time input timestamp in HH:MM:SS format
     * @return time in total number of seconds
     */
    public static int getTimeInSec(String time) {

        if (time.contains(".")) {

            time = time.substring(0, time.indexOf("."));
        }

        int timeInSec = 0;
        int hour = 0, min = 0, second = 0;

        hour = Integer.parseInt(time.substring(0, 2));
        min = Integer.parseInt(time.substring(3, 5));
        second = Integer.parseInt(time.substring(6, 8));

        timeInSec = hour * HOUR_IN_SEC + min * MIN_IN_SEC + second;

        return timeInSec;
    }

    /**
     * Check if a "searchItem" is equal to any elemnt of a list of elements
     *
     * @param listOfElems input list of elements
     * @param searchItem item to be searched
     * @return true if it exists
     */
    public static boolean elemInListEquals(List<Integer> listOfElems, int searchItem) {

        for (int item : listOfElems) {
            if (item == searchItem) {

                return true;
            }
        }

        return false;
    }

    public static boolean elemInListEquals(List<String> listOfElems, String searchItem) {

        for (String item : listOfElems) {
            if (item.equalsIgnoreCase(searchItem)) {

                return true;
            }
        }

        return false;
    }

    public static boolean elemInListEqualsCaseSen(List<String> listOfElems, String searchItem) {

        for (String item : listOfElems) {
            if (item.equals(searchItem)) {

                return true;
            }
        }

        return false;
    }

    /**
     * Check if a "searchItem" is contained into any element of a list of
     * elements
     *
     * @param listOfElems input list of elements
     * @param searchItem element to be search to be searched
     * @return true if it exists
     */
    public static boolean elemInListContained(List<String> listOfElems, String searchItem) {

        for (String str : listOfElems) {
            if (str.contains(searchItem)) {

                return true;
            }
        }

        return false;
    }

    /**
     * First "insertToMap" type function, that inserts into an exisitng key a
     * new value, or creates a new key and initialize it with this new value
     *
     * @param map hash map structure
     * @param keyMap intput key into which valueMap will be added
     */
    public static void insertToMap(HashMap<String, Integer> map, String keyMap) {

        if (map.get(keyMap) == null) {

            map.put(keyMap, 1);
        }
    }

    public static void insertToMap(HashMap<Integer, List<String>> map, String valueMap, int keyMap) {

        List<String> value = null;
        List newList = null;

        if (map.get(keyMap) == null) {

            newList = new LinkedList<>();
            newList.add(valueMap);
            map.put(keyMap, newList);
        } else {

            value = map.get(keyMap);

            if (!elemInListContained(value, valueMap)) {

                value.add(valueMap);
            }
        }
    }

    public static void insertToMap1(HashMap<Integer, List<List<String>>> map, List<String> valueMap, int keyMap) {

        List<List<String>> value = null;
        List<List<String>> newList = null;

        if (map.get(keyMap) == null) {

            newList = new LinkedList<>();
            newList.add(valueMap);
            map.put(keyMap, newList);
        } else {

            value = map.get(keyMap);
            value.add(valueMap);
        }
    }

    public static void insertToMap2(HashMap<Integer, List<Integer>> map, int valueMap, int keyMap) {

        List<Integer> value = null;
        List newList = null;

        if (map.get(keyMap) == null) {

            newList = new LinkedList<>();
            newList.add(valueMap);
            map.put(keyMap, newList);
        } else {

            value = map.get(keyMap);
            value.add(valueMap);
        }
    }

    /**
     * Remove special characters from list of strings. For literals, we remove
     * characters "\'" and "\"". For IRIs, we remove "<" and ">"
     *
     * @param myListSrc list of raw string elements
     * @return refined list
     */
    public static List<String> refineList(List<String> myListSrc) {

        List<String> refList = new LinkedList<>();
        int startInd = -1;
        int stopInd = -1;

        for (String str : myListSrc) {

            //BUUUUUUUUUUUUUUUUUUUUUUUUUUUG
            if (str.length() == 1) {
                continue;
            }

            if (str.startsWith("<") && str.endsWith(">")) {

                str = str.substring(str.indexOf("<") + 1, str.indexOf(">"));
            }
            if (str.startsWith("\"") && str.endsWith("\"")) {

                startInd = str.indexOf("\"") + 1;
                stopInd = str.substring(startInd).indexOf("\"");

                str = str.substring(startInd, stopInd + 1);
            }
            if (str.startsWith("\'") && str.endsWith("\'")) {

                startInd = str.indexOf("\'") + 1;
                stopInd = str.substring(startInd).indexOf("\'");
                str = str.substring(startInd, stopInd + 1);
            }

            refList.add(str);
        }

        return refList;
    }

    /**
     * Remove redundancy and short a list of elements
     *
     * @param inputList list to be sorted
     * @return sorted list
     */
    public static List<String> sortAndRemoveRedundancy(List<String> inputList) {

        HashSet hs = new HashSet();
        hs.addAll(inputList);
        inputList.clear();
        inputList.addAll(hs);
        Collections.sort(inputList);
        return inputList;
    }

    /**
     * Get for a specific complete IRI, the corresponding version using prefixes
     *
     * @param completeIRI complete IRI
     * @return shorten IRI using prefixes
     */
    public static String getShortIRI(String completeIRI) {

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

                if (InitLift.mapAuthorityToPrefix.get(subAuthority) != null) {

                    shortCutIRI = InitLift.mapAuthorityToPrefix.get(subAuthority) + subRessource;
                }

            }

        }

        return shortCutIRI;
    }

    /**
     * Add in list of injected values of a LDF Candidate, for each complete IRI,
     * the corresponding shorten version using prefixes
     *
     * @param injectedValues initial list of injected values
     * @return list of injected values, extended with prefixes' short versions
     */
    public static List<String> addAllShortIRIs(List<String> injectedValues) {

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
     * @param shortIRI
     * @return shorten IRI using prefixes
     */
    public static String getCompleteIRI(String shortIRI) {

        String completeIRI = "", shortPrefix = "";

        shortPrefix = shortIRI.substring(0, shortIRI.indexOf(":") + 1);
        completeIRI = InitLift.mapPrefixToAuthority.get(shortPrefix) + shortIRI.substring(shortIRI.indexOf(":") + 1);

        return completeIRI;
    }

    /**
     * Get all triples' unities (i.e., subject, predicate and object) from the
     * answer in string format, as it sent from the LDF server
     *
     * @param selectorAnsFragment complete answer fragment in string format
     * @return list of triples' unities
     */
    public static List<String> getUnitiesFromSelectorAns(String selectorAnsFragment) {

        boolean flagURI = false, flagLiteral = false;
        boolean flagDoublecotes = false, flagPredicatePref = false;
        int countQuotes = 0;
        List<String> queryUnities = new LinkedList<>();
        List<String> queryUnitiesRefned = new LinkedList<>();
        String valueURI = "", valueLiteral = "", valuePrefix = "";
        String tmpAns = getCleanFragment(selectorAnsFragment);

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

        if (BasicUtilis.elemInListContained(queryUnities, "http://dbpedia.org/resource/01.002_Fighter_Squadron_%22Storks%22")) {

            int aazeaz = 0;
        }

        // get list of refined query's unities
        queryUnitiesRefned = getProccessedUntities(queryUnities);
        queryUnitiesRefned = refineList(queryUnitiesRefned);

        return queryUnitiesRefned;
    }

    /**
     * Pre-process the fragment answer, to identify easily all answer unities
     *
     * @param selectorAnsFragment answer fragment in string format
     * @return processed fragment answer
     */
    public static String getCleanFragment(String selectorAnsFragment) {

        String ansAsTP = "", tmpFullAns = "", shortAns = "", fullAns = "";

        //remove control from TPF
        if (selectorAnsFragment.contains("}\\n")) {

            tmpFullAns = selectorAnsFragment.substring(selectorAnsFragment.indexOf("}\\n") + 3);

            //fragments can be returned either before metadata
            if (tmpFullAns.indexOf("}\\n") > 0) {
                shortAns = tmpFullAns.substring(0, tmpFullAns.indexOf("}\\n"));
                fullAns = tmpFullAns.substring(tmpFullAns.indexOf("}\\n") + 3);
                ansAsTP = shortAns + fullAns;
            } //or after metadata
            else {
                if (tmpFullAns.indexOf(".\\n<http://fragments") > 0) {
                    ansAsTP = tmpFullAns.substring(0, tmpFullAns.indexOf(".\\n<http://fragments"));

                } else {
                    ansAsTP = tmpFullAns;

                }
            }

        }

        ansAsTP = ansAsTP.replaceAll(".\\\\n", "|");
        ansAsTP = ansAsTP.replaceAll("\\\\n", " ");
        ansAsTP = ansAsTP.replaceAll("\\\\\"", "\"");
        ansAsTP = ansAsTP.replaceAll("\\\\\\\\\"", "");

        if (ansAsTP.endsWith(".")) {

            ansAsTP = ansAsTP.substring(0, ansAsTP.length() - 1);
            ansAsTP += "|";
        }

        ansAsTP = ansAsTP + " ";

        return ansAsTP;
    }

    /**
     * Remove and refine elements of list of answer unities
     *
     * @param allAnsFragment all distinct answer fragment's unities
     * @return refined answer's list of unities
     */
    public static List<String> getProccessedUntities(List<String> allAnsFragment) {

        List<String> queryUnitiesRefned = new LinkedList<>();
        String currEntity = "";

        for (int i = 0; i < allAnsFragment.size(); i++) {

            currEntity = allAnsFragment.get(i);
            //Creates a lots of bugs
            currEntity = currEntity.replace("__", "_&_");
            //Creates a lots of bugs

            currEntity = currEntity.replaceAll("|", "");
            currEntity = currEntity.replaceAll(",_", "_");
            currEntity = currEntity.replace("__", "_");

            if (currEntity.contains("fragments.dbpedia")) {
                continue;
            }

            if (currEntity.endsWith(",")) {
                currEntity = currEntity.substring(0, currEntity.length() - 1);
            }

            if (!elemInListEquals(queryUnitiesRefned, currEntity)) {

                queryUnitiesRefned.add(currEntity);
            }

        }

        return queryUnitiesRefned;
    }

    /**
     *
     * @param inputMap
     * @param currCTP
     * @return
     */
    public static int getValueMap(HashMap< Integer, List<String>> inputMap, List<String> currCTP) {
        int value = -1;

        for (Integer curVal : inputMap.keySet()) {

            if (getListsIntersec(inputMap.get(curVal), currCTP).size() == currCTP.size()) {

                return curVal;
            }

        }

        return value;
    }

    /**
     * Compare two lists of strings, for finding their intersection's elements
     *
     * @param outerList outer list to be compared
     * @param innerList inner list to be compared
     * @return intersection of the two lists
     */
    public static List<String> getListsIntersec(List<String> outerList, List<String> innerList) {

        List<String> matchedValuesOuter = new LinkedList<>();
        List<String> matchedValuesInner = new LinkedList<>();

        if (outerList == null || innerList == null) {

            return matchedValuesOuter;
        }

        if (outerList.size() >= innerList.size()) {

            matchedValuesOuter = refineList(outerList);
            matchedValuesInner = refineList(innerList);

        } else if (outerList.size() < innerList.size()) {

            matchedValuesOuter = refineList(innerList);
            matchedValuesInner = refineList(outerList);
        }

        matchedValuesOuter.retainAll(matchedValuesInner);

        return matchedValuesOuter;
    }

    /**
     * Check if outer and inner LDF candidates are temporaly joinables
     *
     * @param outerTimeStamps outer LDF candidate's timestamps
     * @param innerTimeStamps inner LDF candidate's timestamps
     * @param windowJoin
     * @return true if they are actually temporaly joinable
     */
    public static boolean checkIfTemporalyJoinable(List<Integer> outerTimeStamps, List<Integer> innerTimeStamps, int windowJoin) {

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
     * Check if a new timestamp is close enough to list of timestamps of an
     * existing LDF Candidate, based on the "gapWin" or "gap"
     *
     * @param existingTimeStamps list of existing timestamps
     * @param newTimeStamp new timestamp, to be compared
     * @param windowJoin
     * @return true, if it is close enough
     */
    public static boolean checkTemporalDistance(List<Integer> existingTimeStamps, int newTimeStamp, int windowJoin) {

        for (int currTime : existingTimeStamps) {
            if ((newTimeStamp - currTime) >= 0 && (newTimeStamp - currTime) <= windowJoin) {

                return true;
            }
        }

        return false;
    }

    /**
     * Get all variables, from the json string contaning all selector's
     * variables and unities. This function also returns the "key-word"
     * ?predicate which corresponds to the LDF's predicate, if there exist.
     *
     * @param selector json string, containing injected vars and vals
     * @return list of all selector's variables
     */
    public static List<String> getUnitiesFromSelector(String selector) {

        List<String> allUnities = new LinkedList<>();
        List<String> allVars = new LinkedList<>();
        allVars.add("subject");
        allVars.add("predicate");
        allVars.add("object");
        List<String> allReg = new LinkedList<>();
        allReg.add("&predicate=");
        allReg.add("&object=");
        allReg.add("none");
        String currUnity = "", currSelector = selector;

        //remove "LDF server" and "page" information
        if (selector.contains("fragments")) {

            currSelector = selector.substring(selector.indexOf("?") + 1);
        }

        //remove "LDF server" and "page" information
        if (selector.contains("&page")) {

            currSelector = selector.substring(0, selector.indexOf("&page"));
        }

        // get all unities from each position in the selector knwowing that for 
        // each position, there is a different "regular expression" stop
        for (int i = 0; i < 3; i++) {

            currUnity = "";
            if (currSelector.contains(allVars.get(i) + "=")) {

                if (currSelector.contains(allReg.get(i))) {

                    currUnity = currSelector.substring(currSelector.indexOf(allVars.get(i) + "=") + allVars.get(i).length() + 1, currSelector.indexOf(allReg.get(i)));
                    currSelector = currSelector.substring(currSelector.indexOf(allReg.get(i)) + 1);
                } else {

                    currUnity = currSelector.substring(currSelector.indexOf(allVars.get(i) + "=") + allVars.get(i).length() + 1);
                }

            }

            if (!currUnity.equalsIgnoreCase("")) {

                //Creates a lots of bugs
                currUnity = currUnity.replace("__", "_&_");
                //Creates a lots of bugs
                allUnities.add(currUnity);
            } else {
                //Creates a lots of bugs
                currUnity = "";
                currUnity = allVars.get(i).replace("__", "_&_");
                //Creates a lots of bugs

                allUnities.add(currUnity);
            }
        }

        return allUnities;
    }

    /**
     *
     * @param currentTriple
     * @param allEntities
     * @param position
     * @return
     */
    public static List<String> getMappings(String currentTriple, List<String> allEntities, int position) {

        List<String> allMappings = new LinkedList<>();
        int count = 0;

        String[] arrayEntities = currentTriple.split(" ");
        for (String str : arrayEntities) {

            if (!str.equalsIgnoreCase("")) {

                String newStaf = getShortIRI(str);

                if (count == position && (!elemInListContained(allEntities, str)
                        || !elemInListContained(allEntities, newStaf))) {

                    allMappings.add(str);
                }

                count++;
            }

        }

        return allMappings;
    }

    /**
     *
     * @param output
     */
    public static void printInfo(String output) {

        System.out.println(output);
    }

}