package myLIFT;

import static java.lang.Math.abs;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import static myLIFT.Deduction.mapAnsEntryToAllSignatures;
import static myLIFT.Deduction.mapAnsEntryToListValues;
import static myLIFT.Deduction.mapAnsSingatureToAllValues;

/**
 * Class with basic help functions
 *
 * @author Nassopoulos Georges
 * @version 1.0
 * @since 2016-03-19
 */
public class BasicUtilis {

    /**
     * Convert timeStamp from HH:MM:SS format, into total number of seconds
     *
     * @param time input timestamp in HH:MM:SS format
     * @return time in total number of seconds
     */
    public int getTimeInSec(String time) {

        if (time.contains(".")) {

            time = time.substring(0, time.indexOf("."));
        }

        int timeInSec = 0;
        int hour = 0, min = 0, second = 0;

        hour = Integer.parseInt(time.substring(0, 2));
        min = Integer.parseInt(time.substring(3, 5));
        second = Integer.parseInt(time.substring(6, 8));

        timeInSec = hour * 3600 + min * 60 + second;

        return timeInSec;
    }

    /**
     * Convert a timestamp from total number secondes into HH:MM:SS format
     *
     * @param timeInSec input in total number of seconds
     * @return time timestamp in HH:MM:SS format
     */
    public String getTimeToString(int timeInSec) {

        String resultTime = "";
        int hour = 0, min = 0, second = 0;
        int intVal = abs(timeInSec);

        hour = intVal / 3600;
        min = (intVal % 3600) / 60;
        second = intVal % 60;

        resultTime = String.format("%02d:%02d:%02d", hour, min, second);

        return resultTime;
    }

    /**
     * Check if a "searchItem" is equal to any elemnt of a list of elements
     *
     * @param listOfElems input list of elements
     * @param searchItem item to be searched
     * @return true if it exists
     */
    public boolean elemInListEquals(List<Integer> listOfElems, int searchItem) {

        for (int item : listOfElems) {
            if (item == searchItem) {

                return true;
            }
        }

        return false;
    }

    public boolean elemInListEquals(List<String> listOfElems, String searchItem) {

        for (String item : listOfElems) {
            if (item.equalsIgnoreCase(searchItem)) {

                return true;
            }
        }

        return false;
    }
    
         public boolean elemInListEqualsExact(List<String> listOfElems, String searchItem) {

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
    public boolean elemInListContained(List<String> listOfElems, String searchItem) {
        
        for (String str : listOfElems) {
            if (str.contains(searchItem)) {

                return true;
            }
        }

        return false;
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
     * First "insertToMap" type function, that inserts into an exisitng key a
     * new value, or creates a new key and initialize it with this new value
     *
     * @param map hash map structure
     * @param valueMap input value to be added
     * @param keyMap intput key into which valueMap will be added
     */
    public void insertToMap(HashMap<String, List<String>> map, String valueMap, String keyMap) {

        List<String> valueList = null;
        List newList = null;

        if (map.get(keyMap) != null) {

            valueList = map.get(keyMap);
            valueList.add(valueMap);
        } else {

            newList = new LinkedList<>();
            newList.add(valueMap);
            map.put(keyMap, newList);
        }
    }

    public void insertToMap(HashMap<Integer, Integer> map, int valueMap, int keyMap) {

        int value = -1;

        if (map.get(keyMap) == null) {

            map.put(keyMap, valueMap);
        } else {

            value = map.get(keyMap);

            if (value == valueMap) {

                System.out.println("Problem matching key answer to query, key already used!!!");
            }
        }
    }

    public void insertToMap(HashMap<String, List<String>> map, List<String> valueMap, String keyMap) {

        List<String> valueList = null;
        List newList = null;

        if (map.get(keyMap) == null) {

            newList = new LinkedList<>();
            newList.addAll(valueMap);
            map.put(keyMap, newList);
        } else {

            valueList = map.get(keyMap);
            valueList.addAll(valueMap);
        }
    }
    
     public void insertToMap(HashMap<String, Integer> map, String keyMap) {


        if (map.get(keyMap) == null) {

            map.put(keyMap, 1);
        } 
    }


    public void insertToMap(HashMap<Integer, List<String>> map, String valueMap, int keyMap) {

        List<String> value = null;
        List newList = null;

        if (map.get(keyMap) == null) {

            newList = new LinkedList<>();
            newList.add(valueMap);
            map.put(keyMap, newList);
        } else {

            value = map.get(keyMap);
            
            if(!elemInListContained(value, valueMap)){
                
                 value.add(valueMap);
            }
        }
    }
    
        public void insertToMap3(HashMap<Integer, List<String>> map, List<String> valueMap, int keyMap) {

        List<String> value = null;
        List newList = null;

        if (map.get(keyMap) == null) {

            newList = new LinkedList<>();
            newList.add(valueMap);
            map.put(keyMap, newList);
        } else {

            value = map.get(keyMap);
            value.addAll(valueMap);
        }
    }

    public void insertToMap(HashMap<List<String>, List<String>> map, String valueMap, List<String> keyMap) {

        List<String> value = null;
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

    public void insertToMap(HashMap<List<String>, List<Integer>> map, List<String> keyMap, int valueMap) {

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

    public void insertToMap1(HashMap<List<String>, List<List<String>>> map, List<String> valueMap, List<String> keyMap) {

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
    
        public void insertToMap1(HashMap<Integer, List<List<String>>> map, List<String> valueMap, int keyMap) {

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

    public void insertToMap2(HashMap<Integer, List<Integer>> map, int valueMap, int keyMap) {

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

    public void insertToMap2(HashMap<Integer, List<Integer>> map, List<Integer> valueMap, int keyMap) {

        List<Integer> value = null;
        List<Integer> newList = null;

        if (map.get(keyMap) == null) {

            newList = new LinkedList<>();
            newList.addAll(valueMap);
            map.put(keyMap, newList);
        } else {

            value = map.get(keyMap);
            value.addAll(valueMap);
        }
    }

    public void insertToMap3(HashMap<List<String>, List<String>> map, List<String> valueMap, List<String> keyMap) {

        List<String> value = null;
        List newList = null;

        if (map.get(keyMap) != null) {

            value = map.get(keyMap);
            value.addAll(valueMap);
        } else {

            newList = new LinkedList<>();
            newList.addAll(valueMap);
            map.put(keyMap, newList);
        }
    }

    public void insertToMap4(HashMap<List<String>, List<String>> map, List<String> keyMap, String valueMap) {

        List<String> value = null;
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

    public void insertToMap4(HashMap<List<String>, List<String>> map, List<String> keyMap, List<String> valueMap) {

        List<String> value = null;
        List newList = null;

        if (map.get(keyMap) == null) {

            newList = new LinkedList<>();
            newList.addAll(valueMap);
            map.put(keyMap, newList);
        } else {

            value = map.get(keyMap);
            value.addAll(valueMap);
        }
    }

    /**
     * Remove oparators from a query, before extracting all entities (e.g.
     * FILTER, LIMIT and UNION)
     *
     * @param query intput query to be refined
     * @return string query without any operators
     */
    public String removeQueryOperators(String query) {

        String refinedQuery = query;

        // remove new line character
        if (refinedQuery.contains("\r\n")) {

            refinedQuery = refinedQuery.replaceAll(("\r\n"), " ");
        }

        //Remove FILTER entities from query
        while (refinedQuery.contains("FILTER")) {

            if (!refinedQuery.substring(refinedQuery.indexOf("FILTER"), refinedQuery.indexOf(")") + 2).contains("=")) {

                refinedQuery = refinedQuery.replace(refinedQuery.substring(refinedQuery.indexOf("FILTER"), refinedQuery.indexOf(")") + 2), "");
            } //Remove FILTER options, for FedX type traces
            else if (!refinedQuery.contains("||")) {

                refinedQuery = refinedQuery.replace(refinedQuery.substring(refinedQuery.indexOf("FILTER"), refinedQuery.indexOf(")") + 1), "");
            } //Remove FILTER options, for ANAPSID type traces
            else if (refinedQuery.contains("||")) {

                refinedQuery = refinedQuery.substring(0, refinedQuery.indexOf("FILTER") - 2) + "}";
                break;
            }
        }

        // Ignore the LIMIT/OFFSET part from query
        if (refinedQuery.contains(" LIMIT")) {

            refinedQuery = refinedQuery.substring(0, refinedQuery.indexOf(" LIMIT"));
        }

        // Ignore UNION oparators from query       
        if (refinedQuery.contains("UNION")) {

            refinedQuery = refinedQuery.replace("UNION", " ");
        }

        return refinedQuery;
    }

    /**
     * Get, for a specific tmpAns, all triple pattern's unities (IRI, variable
     * or Literal) depending on the second argument "typeEntities":
     *
     * (i) '1' is for variables
     *
     * (ii) '2' is for IRIs
     *
     * (iii) '3' is for Literals
     *
     * (iv) '4' for all above
     *
     * Argument "details" defines if we ignore or not, the part "_#number" of
     * variables when BoundJoin is used
     *
     * @param query input query
     * @param typeEntities defines the entity type(s) to be extracted
     * @param detailBound defines the detail of boundJoin variables
     * @return all tmpAns entities into a list
     */
    public List<String> getQueryEntities(String query, int typeEntities, boolean detailBound) {

        boolean flagVariable = false, flagURI = false, flagLiteral = false, flagPredicatePref = false;
        int cntDoublecotes = 0, cntSinglecotes = 0;
        boolean flagStartTPs = false;
        String valueURI = "", valueLiteral = "", valueVariable = "", valuePrefix = "";
        List<String> queryUnities = new LinkedList<>();

        String tmpQuery = removeQueryOperators(query);

        for (int i = 0; i < tmpQuery.length(); i++) {

            // Start parsing triple patterns
            if (tmpQuery.charAt(i) == '{' || flagStartTPs) {
                flagStartTPs = true;

                //Case it is a variable
                if (((tmpQuery.charAt(i) == '?') || (tmpQuery.charAt(i) == '$') || flagVariable)
                        && (typeEntities == 1 || typeEntities == 4)) {

                    if (tmpQuery.charAt(i) == ' ' || tmpQuery.charAt(i) == '}') {

                        flagVariable = false;
                        queryUnities.add(valueVariable);
                        valueVariable = "";

                    } else {

                        flagVariable = true;
                        valueVariable += tmpQuery.charAt(i);
                    }
                } //Case it is a IRI
                else if ((tmpQuery.charAt(i) == '<' || flagURI) && (typeEntities == 2 || typeEntities == 4)) {
                    if (tmpQuery.charAt(i) == '>') {

                        valueURI += tmpQuery.charAt(i);
                        queryUnities.add(valueURI);
                        flagURI = false;
                        valueURI = "";
                    } else {

                        flagURI = true;
                        valueURI += tmpQuery.charAt(i);
                    }
                } //Case it is a literal
                else if (((tmpQuery.charAt(i) == '\"') || ((tmpQuery.charAt(i) == '\'')) || flagLiteral)
                        && (typeEntities == 3 || typeEntities == 4)) {

                    if ((tmpQuery.charAt(i) == '\"') && cntSinglecotes == 0) {

                        cntDoublecotes++;
                    } else if ((tmpQuery.charAt(i) == '\'') && cntDoublecotes == 0) {

                        cntSinglecotes++;
                    }

                    if (cntDoublecotes == 2 || cntSinglecotes == 2) {

                        cntDoublecotes = 0;
                        cntSinglecotes = 0;
                        flagLiteral = false;
                        valueLiteral += tmpQuery.charAt(i);
                        queryUnities.add(valueLiteral);
                        valueLiteral = "";
                    } else {

                        flagLiteral = true;
                        valueLiteral += tmpQuery.charAt(i);
                    }
                } //Case it is a IRI but with a PREFIX
                else if (((Character.isLetter(tmpQuery.charAt(i))) || flagPredicatePref) && (typeEntities == 2 || typeEntities == 4)) {

                    if ((tmpQuery.charAt(i) == ' ' || tmpQuery.charAt(i) == '}') && flagPredicatePref) {

                        flagPredicatePref = false;
                        queryUnities.add(valuePrefix);
                        valuePrefix = "";
                    } else {

                        flagPredicatePref = true;
                        valuePrefix += tmpQuery.charAt(i);
                    }
                }
            }
        }

        //remove uncessairy characters from query entities
        for (int i = 0; i < queryUnities.size(); i++) {

            //If the user want to ignore the part "_#number" of variables when BoundJoin is used
            if (!detailBound && queryUnities.get(i).contains("_") && !queryUnities.get(i).contains("http")) {

                queryUnities.set(i, queryUnities.get(i).substring(0, queryUnities.get(i).indexOf("_")));
            }
            // remove new line character
            if (queryUnities.get(i).contains("\n")) {
                queryUnities.set(i, queryUnities.get(i).substring(0, queryUnities.get(i).indexOf("\n")));

            }

        }

        return queryUnities;
    }

    /**
     * Remove special characters from list of strings. For literals, we remove
     * characters "\'" and "\"". For IRIs, we remove "<" and ">"
     *
     * @param myListSrc list of raw string elements
     * @return refined list
     */
    public List<String> refineList(List<String> myListSrc) {

        List<String> refList = new LinkedList<>();
        int startInd = -1;
        int stopInd = -1;

            //   System.out.println(myListSrc);
        for (String str : myListSrc) {
            
         //   str=str.replaceAll("&", "");
            
             if(str.equals("dbpedia:BARACK_OBAMA")){
                int  dqsdqs=0;
            }
            
            //BUUUUUUUUUUUUUUUUUUUUUUUUUUUG
            if(str.length()==1){
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
     * Get the list of the projected variables of a SELECT query
     *
     * @param query input string query
     * @return list of projected variables
     */
    public List<String> getProjVars(String query) {

        String subquery = "";
        List<String> projVars = new LinkedList<>();

        if (query.contains("SELECT")) {

            // get all variables of a query
            if (query.contains("SELECT *")) {

                projVars = getQueryEntities(query, 1, true);
            } else {

                //subpart of a query from start to end of projected variables
                subquery = query.substring(query.indexOf("?"), query.indexOf("WHERE") - 1);
                String[] array = subquery.split(" ");
                projVars.addAll(Arrays.asList(array));
            }

        }
        //remove uncessairy characters from query entities
        for (int i = 0; i < projVars.size(); i++) {

            //If the user want to ignore the part "_#number" of variables when BoundJoin is used
            if (projVars.get(i).contains("_")) {

                projVars.set(i, projVars.get(i).substring(0, projVars.get(i).indexOf("_")));
            }
        }

        return projVars;
    }

    /**
     * Get the list of FILTER values in a SELECT squery
     *
     * @param query tmpAns to be parsed
     * @return the list of FILTER values
     */
    public List<String> getFILTERvals(String query) {

        int indxFilter = query.indexOf("FILTER ((");
        String subquery = "";
        List<String> allFilterValues = new LinkedList<>();

        if (indxFilter != -1) {

            subquery = query.substring(indxFilter + 9, query.indexOf("))") + 1);
            String[] arrayOuter = subquery.split("=");
            for (String strOuter : arrayOuter) {

                if (strOuter.contains(")")) {

                    allFilterValues.add(strOuter.substring(0, strOuter.indexOf(")")));
                }

            }

        }

        return allFilterValues;
    }

    /**
     * Get the list of the filter variables of a SELECT FILTER query
     *
     * @param query input query
     * @return the list of filter variables
     */
    public String getFILTERvar(String query) {

        String var = "";
        int indxFilter = query.indexOf("FILTER ((");
        int indxInverseVarStart = -1;
        int indxINverseVarStop = -1;

        if (indxFilter != -1) {

            //Capture variable of FILTER option
            indxInverseVarStart = indxFilter + 9;
            indxINverseVarStop = query.indexOf("=");
            var = query.substring(indxInverseVarStart, indxINverseVarStop);
        }

        return var;
    }

    /**
     * Convert the query sent from an Xdecimal into an ASCII format
     *
     * @param queryEx query in Xdecimal format
     * @return query in ASCII format
     */
    public String convExToASCII(String queryEx) {

        char firstLineChar;
        String hex = "";
        StringBuilder output;
        String queryAscii = "";
        int index2 = 0;

        for (int i = index2; i < queryEx.length(); i++) {

            firstLineChar = queryEx.charAt(i);
            if (firstLineChar == '%') {
                //Ignore a new line
                if (queryEx.substring(i).startsWith("%0A+")) {

                    i = i + 4;

                } //ignore spaces
                else if (queryEx.substring(i).startsWith("%0A%0A")) {

                    i = i + 5;

                } //catch other alpharithmetic characters except alphabetic characters 
                else {

                    hex = "";
                    output = new StringBuilder();
                    hex = queryEx.substring(i + 1, i + 3);
                    output.append((char) Integer.parseInt(hex, 16));
                    queryAscii += output.toString();
                    i += 2;
                }
                // replace '+' with space
            } else if (firstLineChar == '+') {

                queryAscii += " ";
            } else if (firstLineChar == '&') {
                //http key word to ignore
                if (queryEx.substring(i + 1).startsWith("infer")) {

                    break;
                }
            } else {
                //catch alphabetic characters 
                queryAscii += Character.toString(queryEx.charAt(i));
            }
        }

        return queryAscii;
    }

    /**
     * Get all variables of a answer string, which is captured in json format
     *
     * @param answer json format answer string to be parsed
     * @return list of answer's variables
     */
    public List<String> getAnswerVars(String answer) {

        List<String> listValues = new LinkedList<>();

        if (answer.contains("results")) {
            //subpart of a query from start to end of projected variables
            String subAnsw = answer.substring(answer.indexOf("\"vars\": [\"") + 10, answer.indexOf("\"] },"));
            String[] array = subAnsw.split(", ");

            for (String strOuter : array) {

                listValues.add(strOuter.replaceAll("\\\"", ""));
            }

            //remove uncessairy characters from query entities
            for (int i = 0; i < listValues.size(); i++) {

                //If the user want to ignore the part "_#number" of variables when BoundJoin is used
                if (listValues.get(i).contains("_")) {

                    listValues.set(i, listValues.get(i).substring(0, listValues.get(i).indexOf("_")));
                }
            }

        }

        return listValues;
    }

    /**
     * Remove redundancy and short a list of elements
     *
     * @param inputList list to be sorted
     * @return sorted list
     */
    public List<String> sortAndRemoveRedundancy(List<String> inputList) {

        HashSet hs = new HashSet();
        hs.addAll(inputList);
        inputList.clear();
        inputList.addAll(hs);
        Collections.sort(inputList);
        return inputList;
    }

    public List<Integer> sortAndRemoveRedundancy2(List<Integer> inputList) {

        HashSet hs = new HashSet();
        hs.addAll(inputList);
        inputList.clear();
        inputList.addAll(hs);
        Collections.sort(inputList);
        return inputList;
    }

    /**
     * Get all distinct IRIs or Literals, contained in answer of json format
     * string, for a given variable as input
     *
     * @param answer input answer of json format
     * @param variable input variable
     * @return list of answers, for variable passed as parameter
     */
    public List<String> getDistAnsPerVar(String answer, String variable) {

        int idxValue = 0;
        String ansCopy = answer;
        String strValue = "";
        List<String> listValues = new LinkedList<>();

        if (variable.contains("?")) {

            variable = variable.substring(variable.indexOf("?") + 1);
        }

        variable = "\"" + variable;
        if (ansCopy.contains("results")) {

            ansCopy = ansCopy.substring(ansCopy.indexOf("results"));
        }

        int i = ansCopy.indexOf(variable);

        while (i >= 0) {

            ansCopy = ansCopy.substring(i);
            idxValue = ansCopy.indexOf("value\"");

            if (idxValue + 9 > ansCopy.length()) {
                break;
            }

            ansCopy = ansCopy.substring(idxValue + 9, ansCopy.length());

            if (!ansCopy.contains("\"")) {
                break;
            }

            strValue = ansCopy.substring(0, ansCopy.indexOf("\""));
            listValues.add(strValue);
            ansCopy = ansCopy.substring(ansCopy.indexOf("\""));
            i = ansCopy.indexOf(variable);
        }

        return listValues;
    }

    /**
     * Match each string answer's variable to corresponding values. Each list of
     * values are matched to a variable in the form of a unique
     * "signature/string:"
     *
     * \<mapping_variable\>_\<predicate_of_tp\>_\<position_in_tp\>, where:
     *
     * (i) "\<mapping_variable\>_", is the variable to which values are bounded
     *
     * (ii) "\<predicate_of_tp\>", is the predicate of the triple pattern in
     * which the variable appears
     *
     * (iii) "\<position_in_tp\>", is the posostion in which the variable
     * appears (subject, predicate or object)
     *
     * @param key answer in jason format string
     * @param requestQuery corresponding query to answer
     * @param Answer answer string in jason string format
     */
    public void setVarsToAnswEntities(int key, String requestQuery, String Answer) {

        String newKey = "", originalVarPosition = "", newKeyDetailed = "";
        List<String> answerEntities = null;
        List<String> matchQueryExtrVars = getAnswerVars(Answer);
        List<String> allTPs = new LinkedList<>();
        List<String> signature = null;
        HashMap<List<String>, List<String>> mapCurrQuerySignature = new HashMap<>();

        if (matchQueryExtrVars.size() > 0 && Answer.contains("value")) {

            //for all TPs in the tmpAns
            allTPs = getQueryEntities(requestQuery, 4, false);

            for (int i = 0; i < allTPs.size(); i += 3) {

                //find all different signature/strings templates
                signature = new LinkedList<>();

                if (allTPs.get(i).contains("?")) {

                    signature.add(allTPs.get(i));
                }

                signature.add(allTPs.get(i + 1));

                if (allTPs.get(i + 2).contains("?")) {

                    signature.add(allTPs.get(i + 2));
                }

                if (mapCurrQuerySignature.get(signature) == null) {

                    mapCurrQuerySignature.put(signature, null);
                }

            }

            //For all projected variables  (i.e., they return values) find
            // to which signature type the current trilpe pattern belong
            //Note: a tp may have variables in both subject and predicate
            for (int u = 0; u < matchQueryExtrVars.size(); u++) {

                answerEntities = new LinkedList<>();
                for (List<String> currMotiv : mapCurrQuerySignature.keySet()) {

                    if (elemInListEquals(currMotiv, "?" + matchQueryExtrVars.get(u))) {
                        newKeyDetailed = "";

                        //If variable is in possition of tp's "subject"
                        if (currMotiv.get(0).contains("?")) {

                            String tmpVar = currMotiv.get(0).substring(1, currMotiv.get(0).length());

                            if (matchQueryExtrVars.get(u).equalsIgnoreCase(tmpVar)) {

                                originalVarPosition = "subject";
                                newKeyDetailed = Integer.toString(key) + "_" + matchQueryExtrVars.get(u) + "_" + currMotiv.get(1) + "_" + originalVarPosition;
                            }
                        }

                        //Or If variable is in possition of tp's "object"
                        if (currMotiv.get(1).contains("?")) {

                            String tmpVar = currMotiv.get(1).substring(1, currMotiv.get(1).length());

                            if (matchQueryExtrVars.get(u).equalsIgnoreCase(tmpVar)) {

                                originalVarPosition = "object";
                                newKeyDetailed = Integer.toString(key) + "_" + matchQueryExtrVars.get(u) + "_" + currMotiv.get(1) + "_" + originalVarPosition;
                            }
                        }

                        //If variable is in possition of tp's "object"  besides position "subject"
                        if (currMotiv.size() == 3 && currMotiv.get(2).contains("?")) {

                            String tmpVar = currMotiv.get(2).substring(1, currMotiv.get(2).length());

                            if (matchQueryExtrVars.get(u).equalsIgnoreCase(tmpVar)) {

                                originalVarPosition = "object";
                                newKeyDetailed = Integer.toString(key) + "_" + matchQueryExtrVars.get(u) + "_" + currMotiv.get(1) + "_" + originalVarPosition;
                            }

                        }

                        if (!newKeyDetailed.equalsIgnoreCase("")) {

                            insertToMap(mapAnsEntryToAllSignatures, newKeyDetailed, Integer.toString(key));
                        }

                    }
                }

                newKey = Integer.toString(key) + matchQueryExtrVars.get(u);
                answerEntities = getDistAnsPerVar(Answer, matchQueryExtrVars.get(u));

                if (newKey.contains("_")) {

                    newKey = newKey.substring(0, newKey.indexOf("_"));
                }
                if (newKey.contains("?")) {

                    newKey = newKey.substring(newKey.indexOf("?") + 1);
                }

                Collections.sort(answerEntities);
                insertToMap(mapAnsEntryToListValues, answerEntities, newKey);
                insertToMap(mapAnsSingatureToAllValues, answerEntities, newKeyDetailed);
            }

        } else {

            insertToMap(mapAnsEntryToAllSignatures, "NoAnswersToQuery", Integer.toString(key));
        }

    }

}