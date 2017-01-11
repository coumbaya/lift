package myLIFT;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.net.URLDecoder;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import static myLIFT.Main.collectionName;
import static myLIFT.Main.enableFragm;
import static myLIFT.Main.setMonetDB;
import static myLIFT.Main.verbose;

/**
 * Class for loading "capture.log" into corresponding, Document for CouchDB or
 * Table for MonetDB
 *
 * @author Nassopoulos Georges
 * @version 1.0
 * @since 2016-03-19
 */
public class SaveInDB {

    CouchDBManag myDB;
    MonetDBManag myMDB;
    BasicUtilis myBasUtils;

    public static HashMap<String, String> mapAuthorityToPrefix = new HashMap<>();

    //Index of document in which input file will be loaded, for CouchDB
    private int indxDoc;

    public SaveInDB(CouchDBManag db) {

        myDB = db;
        myBasUtils = new BasicUtilis();
    }

    public SaveInDB(MonetDBManag db) {

        myMDB = db;
        myBasUtils = new BasicUtilis();
    }

    /**
     * Parse and count a source file's total number of lines
     *
     * @param fileName path to source log file
     * @return total number of log lines
     * @throws java.io.FileNotFoundException
     */
    public int lineNumberCount(String fileName) throws FileNotFoundException {

        int linesNumber = 0;

        /* Calculate maxLineNumber and  linePercentage */
        try (LineNumberReader lnr = new LineNumberReader(new FileReader(new File(fileName)))) {
            lnr.skip(Long.MAX_VALUE);

            linesNumber = lnr.getLineNumber();

        } catch (IOException e) {

            e.printStackTrace(System.out);
        }

        return linesNumber;
    }

    /**
     * This method loads "capture.log" into the DB, for "CouchDB" or "MonetDB"
     *
     * @param logPath path to capture.log
     * @throws java.io.FileNotFoundException
     * @throws java.sql.SQLException
     */
    public void loadQuersAndAns(String logPath) throws IOException, FileNotFoundException, SQLException {

        if (!setMonetDB) {

            myDB.createfirstDocument("endpointsAnswers" + collectionName);
        }

        if (!enableFragm) {

            loadTotalLog(logPath);
        } else {

            if (false) {
                loadFragmentLog(logPath);

            } else {
                loadFragmentLogBis(logPath);
            }

        }
    }

    public void loadTotalLog(String logPath) throws FileNotFoundException, IOException, SQLException {

        //Name of CouchDB Document, where endpoints Answers will be stored
        if (!setMonetDB) {

            indxDoc = myDB.indexOfDocument("endpointsAnswers" + collectionName);
        }

        System.out.println("Document Loading...");
        // Path to "capture.log"
        String fileName = logPath;
        // Fille parser over the load file
        BufferedReader br = null;
        // Current parsed line of "capture.log"
        String sCurrentLine;
        // Current line number of apture.log
        int ligneNumber = 0;
        // Info to capture of each endpoint answer packet
        String answerMappings = "", ipClient = "172.16.8.89", portClient = "none",
                portEndpoint = "", time = "", queryXDec = "", queryASCII = "";
        int totalNumbers = lineNumberCount(fileName);
        int cntNumberPackets = 0;
        boolean flagPacket = false;
        boolean flagAnsw = false;
        boolean flagANAPSID = false;

        System.out.println("totalNumbers " + totalNumbers);

        try {

            br = new BufferedReader(new FileReader(fileName));

            while ((sCurrentLine = br.readLine()) != null) {

                ligneNumber++;

                //Identify start of a new packet (answer/query)
                // (i) If trace is generated with ANAPSID, query is posed with "HTTP GET"
                // (ii) If trace is generated with FedX, query is posed with "HTTP POST"
                if ((sCurrentLine.contains("POST")
                        || (sCurrentLine.contains("GET") && sCurrentLine.contains("/sparql/?query"))) && !flagPacket) {

                    flagAnsw = false;
                    flagPacket = true;
                    System.out.println("currentLine " + ligneNumber);

                    if ((sCurrentLine.contains("GET") && sCurrentLine.contains("/sparql/?query"))) {

                        flagANAPSID = true;
                    }

                    //Capture query, for ANAPSID traces
                    if (flagANAPSID && queryXDec.equals("")) {

                        queryXDec = sCurrentLine.substring(sCurrentLine.indexOf("?query") + 7, sCurrentLine.indexOf("&format"));
                        // queryASCII = myBasUtils.convExToASCII(queryXDec);
                        queryASCII = URLDecoder.decode(queryXDec, "UTF-8");
                    }

                } // For the current identified packet
                else if (flagPacket) {

                    // If we observe a new packet start
                    if (sCurrentLine.contains("POST")
                            || (sCurrentLine.contains("GET") && sCurrentLine.contains("/sparql/?query")) || (sCurrentLine.contains("GET"))) {

                        System.out.println("currentLine " + ligneNumber);

                        if ((sCurrentLine.contains("GET")) && !sCurrentLine.contains("/sparql/?query")) {

                            System.out.println("");
                            flagPacket = false;
                        }
                        if (verbose) {

                            System.out.println("--------------------------------------");
                            System.out.println("No of packet captured: " + cntNumberPackets);
                            System.out.println("portEndpoint: " + portEndpoint);
                            System.out.println("time: " + time);
                            System.out.println("ipClient: " + ipClient);
                            System.out.println("portClient: " + portClient);
                            System.out.println("answerMappings: " + answerMappings);
                            System.out.println("query XDecimal: " + queryXDec);
                            System.out.println("query ASCII: " + queryASCII);
                            System.out.println("---------------------------------------");
                        }

                        answerMappings += "\n";

                        //Then we save all information, as new entry in the DB
                        if (setMonetDB) {

                            myMDB.saveEntryAnswers("TableEntryAnswers", ipClient,
                                    portClient + Integer.toString(cntNumberPackets), portEndpoint, answerMappings, time, queryASCII, indxDoc);
                        } else {

                            myDB.saveEntryAnswers(ipClient,
                                    portClient + Integer.toString(cntNumberPackets), portEndpoint, answerMappings, time, queryASCII, cntNumberPackets);
                        }

                        cntNumberPackets++;
                        answerMappings = "";
                        portEndpoint = "";
                        time = "";
                        queryXDec = "";
                        queryASCII = "";
                        flagAnsw = false;
                        flagANAPSID = false;

                        //Capture query, for ANAPSID traces
                        if ((sCurrentLine.contains("GET") && sCurrentLine.contains("/sparql/?query"))) {

                            flagANAPSID = true;
                        }

                        if (flagANAPSID && queryXDec.equals("")) {

                            queryXDec = sCurrentLine.substring(sCurrentLine.indexOf("?query") + 7, sCurrentLine.indexOf("&format"));
                            queryASCII = URLDecoder.decode(queryXDec, "UTF-8");
                            // queryASCII = myBasUtils.convExToASCII(queryXDec);
                        }
                    } else {

                        //capture endpoint's port
                        if (sCurrentLine.contains("Host: ")) {

                            String tmpLine = sCurrentLine.substring(sCurrentLine.indexOf("Host: ") + 6);
                            portEndpoint = tmpLine.substring(tmpLine.indexOf(":") + 1);
                        } //capture endpoints"s reception time
                        else if (sCurrentLine.contains("Date:")) {

                            time = sCurrentLine.substring(sCurrentLine.indexOf("GMT") - 9, sCurrentLine.indexOf("GMT") - 1);
                        } //capture start of endpoint's answer or a subsequential line of this answer
                        else if (sCurrentLine.contains("\"head\"") || flagAnsw) {

                            flagAnsw = true;
                            answerMappings += sCurrentLine;
                        } //capture query, for FedX traces 
                        else if (sCurrentLine.contains("queryLn=SPARQL&query=") && !flagANAPSID) {

                            queryXDec = sCurrentLine.substring(sCurrentLine.indexOf("&query=") + 7, sCurrentLine.indexOf("&infer=false"));

                            queryASCII = URLDecoder.decode(queryXDec, "UTF-8");
                            // queryASCII = myBasUtils.convExToASCII(queryXDec);
                        }
                    }

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

        System.out.println("******************************************");
        System.out.println("Total number of captured packets " + cntNumberPackets);
        System.out.println("******************************************");
    }

    public void loadFragmentLog(String logPath) throws FileNotFoundException, IOException, SQLException {

        //Name of CouchDB Document, where endpoints Answers will be stored
        if (!setMonetDB) {

            indxDoc = myDB.indexOfDocument("endpointsAnswers" + collectionName);
        }

        System.out.println("Document Loading...");
        // Path to "capture.log"
        String fileName = logPath;
        // Fille parser over the load file
        BufferedReader br = null;
        // Current parsed line of "capture.log"
        String sCurrentLine;
        // Current line number of apture.log
        int ligneNumber = 0;
        // Info to capture of each endpoint answer packet
        String injectedValues = "", ipClient = "NOT_KNOWN", receptTime = "",
                ldfServer = "", reqTime = "", queryXDec = "", queryASCII = "", ansFragment = "";
        int totalNumbers = lineNumberCount(fileName);
        int cntNumberPackets = 0;
        boolean flagPacket = false;
        boolean flagHostFound = false;
        boolean flagInjecValues = false;
        boolean flagAnsFrag = false;
        boolean flagRectA = false;
        boolean flagRectB = false;

        System.out.println("totalNumbers " + totalNumbers);

        try {

            br = new BufferedReader(new FileReader(fileName));

            while ((sCurrentLine = br.readLine()) != null) {

                ligneNumber++;

                //Identify start of a new packet (answer/query)
                if (sCurrentLine.contains("startedDateTime")) {

                    if (flagPacket) {

                        System.out.println("currentLine " + ligneNumber);

                        if (ansFragment.length() > 0) {
                            ldfServer = "";
                            String tmpLDFServer = ansFragment.substring(ansFragment.indexOf("Triple Pattern Fragment of the \'") + 32);
                            ldfServer = tmpLDFServer.substring(0, tmpLDFServer.indexOf(("\'")));
                        }

                        if (verbose) {

                            System.out.println("--------------------------------------");
                            System.out.println("No of packet captured: " + cntNumberPackets);
                            System.out.println("LDF server: " + ldfServer);
                            System.out.println("reqTime: " + reqTime);
                            System.out.println("ipClient: " + ipClient);
                            System.out.println("receptTime: " + receptTime);
                            System.out.println("InjectedValues: " + injectedValues);
                            System.out.println("Answer fragments: " + ansFragment);
                            System.out.println("query XDecimal: " + queryXDec);
                            System.out.println("query ASCII: " + queryASCII);
                            System.out.println("---------------------------------------");
                        }

                        //Then we save all information, as new entry in the DB
                        if (setMonetDB) {

                            myMDB.saveEntryAnswers("TableEntryAnswers", ipClient,
                                    receptTime + Integer.toString(cntNumberPackets), ldfServer, injectedValues, reqTime, queryASCII, indxDoc);
                        } else {

                            myDB.saveEntryLDF(ipClient,
                                    receptTime + Integer.toString(cntNumberPackets), ldfServer, injectedValues, ansFragment, reqTime, queryASCII, cntNumberPackets);
                        }

                        cntNumberPackets++;
                        injectedValues = "";
                        ansFragment = "";
                        ldfServer = "";
                        reqTime = "";
                        queryXDec = "";
                        queryASCII = "";
                        flagHostFound = false;
                        flagInjecValues = false;
                        flagAnsFrag = false;
                    } else {

                        flagPacket = true;
                    }

                    System.out.println("currentLine " + ligneNumber);

                    //its difficult to catch time, we use the first "-"
                    reqTime = sCurrentLine.substring(sCurrentLine.indexOf("-") + 7, sCurrentLine.indexOf("."));

                } // For the current identified packet
                else if (flagPacket) {

                    //Capture triple pattern fragment, posed to the corresponding LDF server
                    if (sCurrentLine.contains("\"url\"")) {

                        queryXDec = sCurrentLine.substring(sCurrentLine.indexOf(":") + 3, sCurrentLine.indexOf("\","));
                        //queryASCII = myBasUtils.convExToASCII(queryXDec);
                        queryASCII = URLDecoder.decode(queryXDec, "UTF-8");
                    } else if (sCurrentLine.contains("\"name\": \"Host\",")) {

                        flagHostFound = true;
                    } //capture  LDF endpoint's name
                    else if (flagHostFound) {
                        if (sCurrentLine.contains("\"value\": \"")) {

                            ldfServer = sCurrentLine.substring(sCurrentLine.indexOf(": \"") + 3, sCurrentLine.length() - 1);
                        }

                        flagHostFound = false;
                    } else if (sCurrentLine.contains("cookies") && flagInjecValues) {

                        flagInjecValues = false;
                    } else if (sCurrentLine.contains("mimeType")) {

                        flagInjecValues = false;
                        flagAnsFrag = true;
                    } //capture LDF endpoint's answer
                    else if (flagAnsFrag && !flagInjecValues && sCurrentLine.contains("text")) {

                        ansFragment = sCurrentLine.substring(sCurrentLine.indexOf("\"text\": \"") + 9, sCurrentLine.length() - 3);

                        flagInjecValues = false;
                        flagAnsFrag = false;

                    } //capture possible injected value, on the current triple pattern fragment
                    else if (sCurrentLine.contains("queryString") || flagInjecValues) {

                        if (sCurrentLine.contains("\"queryString\": [],")) {

                            injectedValues = sCurrentLine.substring(sCurrentLine.indexOf(":") + 2, sCurrentLine.length() - 1);
                            flagInjecValues = false;
                        } else {

                            injectedValues = injectedValues + sCurrentLine;
                        }

                        if (!flagInjecValues) {

                            flagInjecValues = true;

                        }

                    } else if (sCurrentLine.contains("\"response\": {")) {

                        flagRectA = true;
                    } else if (flagRectA && sCurrentLine.contains("\"name\": \"Date\",")) {

                        flagRectB = true;
                    } //capture endpoints"s reception time
                    else if (flagRectB) {

                        if (sCurrentLine.contains("\"value\": \"")) {

                            String tmpTime = sCurrentLine.substring(sCurrentLine.indexOf(": \"") + 3, sCurrentLine.length() - 1);
                            receptTime = tmpTime.substring(tmpTime.indexOf(":") - 2, tmpTime.indexOf("GMT") - 1);
                            flagRectA = false;
                            flagRectB = false;
                        }
                    }

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

        System.out.println("******************************************");
        System.out.println("Total number of captured packets " + cntNumberPackets);
        System.out.println("******************************************");
    }

    /**
     *
     *
     * @param logPath
     * @throws FileNotFoundException
     * @throws IOException
     * @throws SQLException
     */
    public void loadFragmentLogBis(String logPath) throws FileNotFoundException, IOException, SQLException {

        //Name of CouchDB Document, where endpoints Answers will be stored
        if (!setMonetDB) {

            indxDoc = myDB.indexOfDocument("endpointsAnswers" + collectionName);
        }

        System.out.println("Document Loading...");
        String fileName = logPath;
        String sCurrentLine;
        String injectedValues = "", ipClient = "NOT_KNOWN", receptTime = "",
                ldfServer = "dbpediaLDF", reqTime = "", queryXDec = "", queryASCII = "", ansFragment = "", allAnswers = "";
        String regExTime = "########## timestamp:";
        String regExSelector = "########## selector:";
        String regExData = "########## only data fragments:";

        int ligneNumber = 0;
        int totalNumbers = lineNumberCount(fileName);
        int cntNumberPackets = 0;

        List<String> allSubjectAns = new LinkedList<>();
        allSubjectAns.add("####subject= { ");
        List<String> allPredicateAns = new LinkedList<>();
        allPredicateAns.add("####predicate= ");
        List<String> allObjectAns = new LinkedList<>();
        allObjectAns.add("####object= { ");
        List<String> allSelectorEntitie = new LinkedList<>();
        List<String> allFrags = new LinkedList<>();

        BufferedReader br = null;
        boolean newPacket = false;
        boolean startData = false;

        setAuthorityToPrefix();

        System.out.println("totalNumbers " + totalNumbers);

        try {

            br = new BufferedReader(new FileReader(fileName));

            while ((sCurrentLine = br.readLine()) != null) {

                /*BUUUUUUUUUUUUUUUUUUUUUUG*/
                if (cntNumberPackets == 19000) {

                    break;
                }

                ligneNumber++;

                // Catch first packet
                if (!newPacket && sCurrentLine.contains("***************************************************************************")) {

                    newPacket = true;
                } // catch end of current packet
                else if (newPacket && sCurrentLine.contains("***********************")) {

                    newPacket = false;
                    System.out.println("currentLine " + ligneNumber);

                    if (verbose) {

                        System.out.println("--------------------------------------");
                        System.out.println("No of packet captured: " + cntNumberPackets);
                        System.out.println("LDF server: " + ldfServer);
                        System.out.println("reqTime: " + reqTime);
                        System.out.println("ipClient: " + ipClient);
                        System.out.println("receptTime: " + receptTime);
                        System.out.println("InjectedValues: " + injectedValues);
                        System.out.println("All answer fragments: " + allAnswers);
                        System.out.println("Only subject's answer mappings: " + allSubjectAns);
                        System.out.println("Only predicate's answer mappings: " + allPredicateAns);
                        System.out.println("Only object's answer mappings: " + allObjectAns);
                        System.out.println("query XDecimal: " + queryXDec);
                        System.out.println("query ASCII: " + queryASCII);
                        System.out.println("---------------------------------------");
                    }

                    //Then we save all information, as new entry in the DB
                    if (setMonetDB) {

                        myMDB.saveEntryAnswers("TableEntryAnswers", ipClient,
                                receptTime + Integer.toString(cntNumberPackets), ldfServer, injectedValues, reqTime, queryASCII, indxDoc);
                    } else {

                        myDB.saveEntryLDF(ipClient,
                                receptTime + Integer.toString(cntNumberPackets), ldfServer, injectedValues, allSubjectAns.toString() + allPredicateAns.toString() + allObjectAns.toString(), reqTime, queryASCII, cntNumberPackets);
                    }

                    cntNumberPackets++;
                    injectedValues = "";
                    ansFragment = "";
                    reqTime = "";
                    queryXDec = "";
                    queryASCII = "";
                    allAnswers = "";
                    startData = false;
                    allSubjectAns = new LinkedList<>();
                    allPredicateAns = new LinkedList<>();
                    allObjectAns = new LinkedList<>();
                    System.out.println("currentLine " + ligneNumber);
                } // For the current identified packet
                else if (newPacket) {

                    if (reqTime.equalsIgnoreCase("") && sCurrentLine.contains(regExTime)) {

                        reqTime = sCurrentLine.substring(sCurrentLine.indexOf(regExTime) + regExTime.length() + 1);
                    } else if (queryXDec.equalsIgnoreCase("") && sCurrentLine.contains(regExSelector)) {

                        queryXDec = sCurrentLine.substring(sCurrentLine.indexOf(regExSelector) + regExSelector.length() + 1);
                        //queryASCII = myBasUtils.convExToASCII(queryXDec);
                        System.out.println(queryXDec);
                        queryASCII=queryXDec;
                       // queryASCII = URLDecoder.decode(queryXDec, "UTF-8");
                        allSelectorEntitie = getEntities(queryASCII);
                            // queryASCII=getEntities(queryASCII);

                    } else if (sCurrentLine.contains(regExData)) {

                        startData = true;
                    } else if (startData && !sCurrentLine.equalsIgnoreCase("")) {

                        allFrags = new LinkedList<>();
                        allAnswers += sCurrentLine;
                        allSubjectAns.addAll(getMappings(sCurrentLine, allSelectorEntitie, 0));
                        allPredicateAns.addAll(getMappings(sCurrentLine, allSelectorEntitie, 1));
                        allObjectAns.addAll(getMappings(sCurrentLine, allSelectorEntitie, 2));
                        // allObjectAns+=getMappings(sCurrentLine, allSelectorEntitie, 2);    
                    }
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

        System.out.println("******************************************");
        System.out.println("Total number of captured packets " + cntNumberPackets);
        System.out.println("******************************************");
    }

    /**
     *
     *
     * @param logPath
     * @throws FileNotFoundException
     * @throws IOException
     * @throws SQLException
     */
    public void loadFragmentLogBisOriginal(String logPath) throws FileNotFoundException, IOException, SQLException {

        //Name of CouchDB Document, where endpoints Answers will be stored
        if (!setMonetDB) {

            indxDoc = myDB.indexOfDocument("endpointsAnswers" + collectionName);
        }

        System.out.println("Document Loading...");
        // Path to "capture.log"
        String fileName = logPath;
        // Fille parser over the load file
        BufferedReader br = null;
        // Current parsed line of "capture.log"
        String sCurrentLine;
        // Current line number of apture.log
        int ligneNumber = 0;
        // Info to capture of each endpoint answer packet
        String injectedValues = "", ipClient = "NOT_KNOWN", receptTime = "",
                ldfServer = "dbpediaLDF", reqTime = "", queryXDec = "", queryASCII = "", ansFragment = "";
        int totalNumbers = lineNumberCount(fileName);
        int cntNumberPackets = 0;
        String allAnswers = "";
        setAuthorityToPrefix();

        List<String> allSubjectAns = new LinkedList<>();
        allSubjectAns.add("####subject= ");
        List<String> allPredicateAns = new LinkedList<>();
        allPredicateAns.add("####predicate= ");
        List<String> allObjectAns = new LinkedList<>();
        allObjectAns.add("####object= ");

       // String allSubjectAns="####subject= { ";      
        // String allObjectAns="####object= { "; 
        boolean newPacket = false;

        List<String> allSelectorEntitie = new LinkedList<>();

        System.out.println("totalNumbers " + totalNumbers);

        try {

            br = new BufferedReader(new FileReader(fileName));

            while ((sCurrentLine = br.readLine()) != null) {

                if (cntNumberPackets == 19000) {
                    int intazraz = 0;
                    break;
                }

                if (sCurrentLine.contains("[Log name")) {
                    continue;
                }

                ligneNumber++;

                if (!newPacket && sCurrentLine.contains("***************************************************************************")) {

                    newPacket = true;
                } else if (newPacket && sCurrentLine.contains("__________________________________________________________________________")) {

                    newPacket = false;
                    System.out.println("currentLine " + ligneNumber);

                    if (verbose) {

                        System.out.println("--------------------------------------");
                        System.out.println("No of packet captured: " + cntNumberPackets);
                        System.out.println("LDF server: " + ldfServer);
                        System.out.println("reqTime: " + reqTime);
                        System.out.println("ipClient: " + ipClient);
                        System.out.println("receptTime: " + receptTime);
                        System.out.println("InjectedValues: " + injectedValues);
                        System.out.println("All answer fragments: " + allAnswers);
                        System.out.println("Answer fragments: " + allSubjectAns + allObjectAns);
                        System.out.println("query XDecimal: " + queryXDec);
                        System.out.println("query ASCII: " + queryASCII);
                        System.out.println("---------------------------------------");
                    }

                    //Then we save all information, as new entry in the DB
                    if (setMonetDB) {

                        myMDB.saveEntryAnswers("TableEntryAnswers", ipClient,
                                receptTime + Integer.toString(cntNumberPackets), ldfServer, injectedValues, reqTime, queryASCII, indxDoc);
                    } else {

                        myDB.saveEntryLDF(ipClient,
                                receptTime + Integer.toString(cntNumberPackets), ldfServer, injectedValues, allSubjectAns.toString() + allObjectAns.toString(), reqTime, queryASCII, cntNumberPackets);
                    }

                    cntNumberPackets++;
                    injectedValues = "";
                 //   allSubjectAns = "";
                    //   allObjectAns = "";
                    ansFragment = "";
                    reqTime = "";
                    queryXDec = "";
                    queryASCII = "";
                    allAnswers = "";
                    allSubjectAns = new LinkedList<>();
                    allObjectAns = new LinkedList<>();
                    System.out.println("currentLine " + ligneNumber);

                } // For the current identified packet
                else if (newPacket) {

                    if (reqTime.equalsIgnoreCase("") && sCurrentLine.contains("########## ")) {

                        reqTime = sCurrentLine.substring(sCurrentLine.indexOf("########## ") + 11);
                    } else if (queryXDec.equalsIgnoreCase("") && sCurrentLine.contains("########## ")) {

                        queryXDec = sCurrentLine.substring(sCurrentLine.indexOf("########## ") + 11);
                        //queryASCII = myBasUtils.convExToASCII(queryXDec);
                        queryASCII = URLDecoder.decode(queryXDec, "UTF-8");

                        allSelectorEntitie = getEntities(queryASCII);
                          //  queryASCII=getEntities(queryASCII);

                    } else if (ansFragment.equalsIgnoreCase("") && sCurrentLine.contains("########## ")) {

                        String currFrag = sCurrentLine.substring(sCurrentLine.indexOf("########## ") + 11);

                        List<String> allFrags = new LinkedList<>();
                        allAnswers = currFrag;

                        allFrags.add(currFrag);

                        allSubjectAns = getMappings(currFrag, allSelectorEntitie, 0);
                        allObjectAns = getMappings(currFrag, allSelectorEntitie, 2);
                        // ansFragment = sCurrentLine.substring(sCurrentLine.indexOf("########## ")+11);
                    } else if (!ansFragment.equalsIgnoreCase("")) {

                        allAnswers += sCurrentLine;
                        allSubjectAns.addAll(getMappings(sCurrentLine, allSelectorEntitie, 0));
                        allObjectAns.addAll(getMappings(sCurrentLine, allSelectorEntitie, 2));
                        // allObjectAns+=getMappings(sCurrentLine, allSelectorEntitie, 2);    
                    }
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

        System.out.println("******************************************");
        System.out.println("Total number of captured packets " + cntNumberPackets);
        System.out.println("******************************************");
    }

    public List<String> getEntities(String originalSelect) {

        String currEntity = "";
        int cntEntities = 0;
        List<String> allEntities = new LinkedList<>();

        if (originalSelect.contains("subject")) {

            if (originalSelect.contains("&predicate=")) {

                currEntity = originalSelect.substring(originalSelect.indexOf("?subject=") + 9, originalSelect.indexOf("&predicate="));
            } else {
                currEntity = originalSelect.substring(originalSelect.indexOf("?subject=") + 9);
            }

          //  currEntity=originalSelect.substring(originalSelect.indexOf("?subject=")+9, originalSelect.indexOf("&predicate="));
            if (!currEntity.equalsIgnoreCase("")) {

                allEntities.add(currEntity);
            }
            cntEntities++;
        }

        if (originalSelect.contains("predicate")) {

            if (originalSelect.contains("&object=")) {

                currEntity = originalSelect.substring(originalSelect.indexOf("predicate=") + 10, originalSelect.indexOf("&object="));
            } else {
                currEntity = originalSelect.substring(originalSelect.indexOf("predicate=") + 10);
            }

            if (!currEntity.equalsIgnoreCase("")) {

                allEntities.add(currEntity);
            }
            cntEntities++;
        }

        /* if(originalSelect.contains("predicate")){
            
         if(originalSelect.contains("&object=")){
                
         currEntity=originalSelect.substring(originalSelect.indexOf("&predicate=")+11, originalSelect.indexOf("&object=")); 
         }
            
         else {
                
         currEntity=originalSelect.substring(originalSelect.indexOf("&predicate=")+11);  
         }
            
         allEntities+="--"+currEntity;
         cntEntities++;     
         }*/
        if (originalSelect.contains("object")) {

            currEntity = originalSelect.substring(originalSelect.indexOf("&object=") + 8);
            if (!currEntity.equalsIgnoreCase("")) {

                allEntities.add(currEntity);
            }
            cntEntities++;
        }

      //   allEntities=addAllShortIRIs(allEntities);

        /*   if(cntEntities==3){
         allEntities="";
         }*/
        return allEntities;
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
        mapAuthorityToPrefix.put("http://en.wikipedia.org/wiki/", "wikipedia:");
    }

    public List<String> getMappings(String currentTriple, List<String> allEntities, int position) {

        List<String> allMappings = new LinkedList<>();
        int count = 0;
        int numbOccurs = 0;
        boolean skip = false;

        if (currentTriple.contains("abstract")) {
            
            int araz = 0;
        }

        String[] arrayEntities = currentTriple.split(" ");

       /*  for (int i = 0; i < allEntities.size(); i++) {
            
            numbOccurs = 0;
            for (String str : arrayEntities) {
                String newStaf = getShortIRI(str);

                if (!str.equalsIgnoreCase("")) {
                    if (myBasUtils.elemInListContained(allEntities, str) || myBasUtils.elemInListContained(allEntities, newStaf)) {
                        numbOccurs++;

                        if (numbOccurs == 2) {

                            if (!str.equalsIgnoreCase(" ")) {

                                allMappings.add(str);
                            }
                            skip = true;
                            break;
                        }

                    }
                }

            }

        }*/

            for (String str : arrayEntities) {

                if (!str.equalsIgnoreCase("")) {

                    String newStaf = getShortIRI(str);

                    if (count == position && ( !myBasUtils.elemInListContained(allEntities, str) ||  !myBasUtils.elemInListContained(allEntities, newStaf)) ){

                            allMappings.add(str);
                    } 

                    count++;
                }

            }
                 //   allMappings=addAllShortIRIs(allMappings);
        return allMappings;
    }

}