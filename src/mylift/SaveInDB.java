package mylift;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.net.URLDecoder;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class for loading the input execution trace generated with WebInspector or
 * Selector XML response, for the real log trace of USEWOD into CouchDB or
 * MonetDB
 *
 * @author Nassopoulos Georges
 * @version 1.0
 * @since 2016-12-15
 */
public class SaveInDB {

    private CouchDBManag couchDB;
    private MonetDBManag monetDB;

    public SaveInDB(CouchDBManag db) {

        couchDB = db;
    }

    public SaveInDB(MonetDBManag db) {

        monetDB = db;
    }

    /**
     * Initialize loading of input trace, into "CouchDB" or "MonetDB" DB,
     * generate either with "WebInsepctor" or as "Selector XML response"
     *
     * @param fileName (path to source) log file
     */
    public void initLoading(String fileName) {

        String collectionName = "DFWEBINSPECTOR";
        // collectionName = "DFXML";

        if (Configuration.setCouchDB) {

            couchDB.createfirstDocument("endpointsAnswers" + collectionName);
        }

        if (Configuration.webInsTraces) {

            loadLogWebInsp(fileName);
        } else {

            try {

                loadLogXmlSelectorRes(fileName);
            } catch (Exception ex) {

                Logger.getLogger(SaveInDB.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * Load traces generated as XML response to each selector (i.e., subquery),
     * into MonetDB or CouchDB
     *
     * @param fileName (path to source) log file
     */
    private void loadLogXmlSelectorRes(String fileName) {

        BasicUtilis.printInfo("Document Loading...");

        /* file parsing's variables */
        String sCurrentLine;
        int ligneNumber = 0;
        int cntPacts = 0;
        BufferedReader br = null;

        /* DB entry's variables  */
        String ipClient = "NOT_KNOWN", receptTime = "",
                ldfServer = "dbpediaLDF", reqTime = "", queryXDec = "", queryASCII = "", allAnswers = "";

        /* Information parsing's variables  */
        final String regExTime = "########## timestamp:";
        final String regExSelector = "########## selector:";
        final String regExData = "########## only data fragments:";
        final String prefixSubj = "####subject= { ";
        final String prefixPred = "####predicate= ";
        final String prefixObj = "####object= { ";
        final String endPacket = "***********************";
        List<String> allSubjectAns = new LinkedList<>();
        List<String> allPredicateAns = new LinkedList<>();
        List<String> allObjectAns = new LinkedList<>();
        List<String> allSelectorEntitie = new LinkedList<>();
        boolean newPacket = false, startData = false;

        BasicUtilis.printInfo("Total numbers of lines" + lineNumberCount(fileName));

        try {

            br = new BufferedReader(new FileReader(fileName));

            while ((sCurrentLine = br.readLine()) != null) {

                if (cntPacts == 19000) {

                    break;
                }

                ligneNumber++;

                // Catch first packet
                if (!newPacket && sCurrentLine.contains(endPacket)) {

                    newPacket = true;
                } // catch end of current packet and init variables
                else if (newPacket && sCurrentLine.contains(endPacket)) {

                    //Then we save all information, as new entry in the DB
                    allAnswers = allSubjectAns.toString() + allPredicateAns.toString() + allObjectAns.toString();
                    setEntryInfo(cntPacts, ldfServer, reqTime, ipClient, receptTime, allAnswers, queryASCII);

                    cntPacts++;
                    reqTime = "";
                    queryXDec = "";
                    queryASCII = "";
                    allAnswers = "";
                    startData = false;
                    newPacket = false;
                    allSubjectAns = new LinkedList<>();
                    allPredicateAns = new LinkedList<>();
                    allObjectAns = new LinkedList<>();
                    allSubjectAns.add(prefixSubj);
                    allPredicateAns.add(prefixPred);
                    allObjectAns.add(prefixObj);

                    if(Configuration.verbose) {
                       
                        BasicUtilis.printInfo("currentLine " + ligneNumber);  
                    }
                } // For the current identified packet
                else if (newPacket) {

                    //capture request time
                    if (reqTime.isEmpty() && sCurrentLine.contains(regExTime)) {

                        reqTime = sCurrentLine.substring(sCurrentLine.indexOf(regExTime) + regExTime.length() + 1);
                    } //capture request subquery (i.e., selector)
                    else if (queryXDec.isEmpty() && sCurrentLine.contains(regExSelector)) {

                        queryXDec = sCurrentLine.substring(sCurrentLine.indexOf(regExSelector) + regExSelector.length() + 1);
                        BasicUtilis.printInfo(queryXDec);
                        queryASCII = queryXDec;
                        allSelectorEntitie = BasicUtilis.getUnitiesFromSelector(queryASCII);

                    } //capture start of response fragment
                    else if (sCurrentLine.contains(regExData)) {

                        startData = true;
                    } //capture suite of response fragment
                    else if (startData && !sCurrentLine.isEmpty()) {

                        allAnswers += sCurrentLine;
                        allSubjectAns.addAll(BasicUtilis.getMappings(sCurrentLine, allSelectorEntitie, 0));
                        allPredicateAns.addAll(BasicUtilis.getMappings(sCurrentLine, allSelectorEntitie, 1));
                        allObjectAns.addAll(BasicUtilis.getMappings(sCurrentLine, allSelectorEntitie, 2));
                    }
                }

            }
        } catch (Exception ex) {

            Logger.getLogger(SaveInDB.class.getName()).log(Level.SEVERE, null, ex);
        } finally {

            if (br != null) {

                try {

                    br.close();
                } catch (Exception ex) {

                    Logger.getLogger(SaveInDB.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

        BasicUtilis.printInfo("******************************************");
        BasicUtilis.printInfo("Total number of captured packets " + cntPacts);
        BasicUtilis.printInfo("******************************************");
    }

    /**
     * Load traces generated as XML response to each selector (i.e., subquery),
     * into MonetDB or CouchDB
     *
     * @param fileName (path to source) log file
     */
    private void loadLogWebInsp(String fileName) {

        BasicUtilis.printInfo("Document Loading...");

        /* file parsing's variables */
        String sCurrentLine;
        int totalNumbers = lineNumberCount(fileName);
        int cntPacts = 0;
        int ligneNumber = 0;
        BufferedReader br = null;

        /* DB entry's variables  */
        String ipClient = "NOT_KNOWN", receptTime = "",
                ldfServer = "", reqTime = "", queryASCII = "", ansFragment = "";

        /* Information parsing's variables  */
        final String regExTime = " GMT";
        final String regExSelector = "\"url\"";
        final String regLDFServer = "\"name\": \"Host\",";
        final String regExData = "mimeType";
        boolean flagLDFServer = false;
        boolean flagAnsFrag = false;
        boolean flagPacket = false;

        BasicUtilis.printInfo("Total numbers of lines" + totalNumbers);

        try {

            try {

                br = new BufferedReader(new FileReader(fileName));
                while ((sCurrentLine = br.readLine()) != null) {

                    ligneNumber++;
                    
                    
                    if(cntPacts==1001){
                   //     break;
                    }

                    //Identify start of a new packet (answer/query)
                    if (sCurrentLine.contains("startedDateTime")) {

                        if (flagPacket) {

                            if (ansFragment.length() > 0) {

                                ldfServer = "";
                                String tmpLDFServer = ansFragment.substring(ansFragment.indexOf("Triple Pattern Fragment of the \'") + 32);
                                ldfServer = tmpLDFServer.substring(0, tmpLDFServer.indexOf(("\'")));
                            }

                            //Then we save all information, as new entry in the DB
                            setEntryInfo(cntPacts, ldfServer, reqTime, ipClient, receptTime, ansFragment, queryASCII);
                            cntPacts++;
                            ansFragment = "";
                            ldfServer = "";
                            reqTime = "";
                            queryASCII = "";
                            flagLDFServer = false;
                            flagAnsFrag = false;
                        } else {

                            flagPacket = true;
                        }

                    if(Configuration.verbose) {
                       
                        BasicUtilis.printInfo("currentLine " + ligneNumber);  
                    }
                        //its difficult to catch time, we use the first "-"
                        reqTime = sCurrentLine.substring(sCurrentLine.indexOf("-") + 7, sCurrentLine.indexOf("."));

                    } // For the current identified packet
                    else if (flagPacket) {

                        // capture subquery i.e., selector
                        if (sCurrentLine.contains(regExSelector)) {

                            queryASCII = sCurrentLine.substring(sCurrentLine.indexOf(":") + 3, sCurrentLine.indexOf("\","));
                            queryASCII = URLDecoder.decode(queryASCII, "UTF-8");
                        }

                        // capture LDF server's name in two steps (lines in the trace)
                        if (sCurrentLine.contains(regLDFServer)) {

                            flagLDFServer = true;
                        }

                        if (flagLDFServer && sCurrentLine.contains("\"value\": \"")) {

                            ldfServer = sCurrentLine.substring(sCurrentLine.indexOf(": \"") + 3, sCurrentLine.length() - 1);
                        }

                        //capture LDF servers's answer in two steps (lines in the trace)
                        if (sCurrentLine.contains(regExData)) {

                            flagAnsFrag = true;
                        }

                        if (flagAnsFrag && sCurrentLine.contains("text")) {

                            ansFragment = sCurrentLine.substring(sCurrentLine.indexOf("\"text\": \"") + 9, sCurrentLine.length() - 3);
                            flagAnsFrag = false;
                        }

                        //capture subquery request time
                        if (sCurrentLine.contains(regExTime)) {

                            
                            String tmpTime = sCurrentLine.substring(sCurrentLine.indexOf(": \"") + 3, sCurrentLine.length() - 1);
                          //  System.out.println(sCurrentLine);
                            receptTime = tmpTime.substring(tmpTime.indexOf(":") - 2, tmpTime.indexOf(regExTime) - 1);
                        }

                    }

                }
            } catch (Exception ex) {

                Logger.getLogger(SaveInDB.class.getName()).log(Level.SEVERE, null, ex);
            }

        } finally {
            if (br != null) {

                try {

                    br.close();
                } catch (Exception ex) {

                    Logger.getLogger(SaveInDB.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

        }

        BasicUtilis.printInfo("******************************************");
        BasicUtilis.printInfo("Total number of captured packets " + cntPacts);
        BasicUtilis.printInfo("******************************************");
    }

    /**
     * Store each packet info, with requested ressource and answer of LDF server,
     * either in "CouchDB" or "MonetDB"
     * 
     * @param cntPacts current id of captured packet
     * @param ldfServer LDF server receiving the request
     * @param reqTime requested timestamp
     * @param ipClient ip address of client
     * @param receptTime reception time
     * @param allAnswers all answer fragments
     * @param queryASCII requested query in ASCII format
     */
    public void setEntryInfo(int cntPacts, String ldfServer, String reqTime, String ipClient,
            String receptTime, String allAnswers, String queryASCII) {

        if (Configuration.verbose) {

            BasicUtilis.printInfo("--------------------------------------");
            BasicUtilis.printInfo("No of packet captured: " + cntPacts);
            BasicUtilis.printInfo("LDF server: " + ldfServer);
            BasicUtilis.printInfo("reqTime: " + reqTime);
            BasicUtilis.printInfo("ipClient: " + ipClient);
            BasicUtilis.printInfo("receptTime: " + receptTime);
            BasicUtilis.printInfo("All answer fragments: " + allAnswers);
            BasicUtilis.printInfo("query ASCII: " + queryASCII);
            BasicUtilis.printInfo("---------------------------------------");
        }

        if (Configuration.setMonetDB) {

            monetDB.saveEntryLDF(Configuration.nameDB, cntPacts, ipClient, ldfServer, queryASCII,
                    reqTime, allAnswers, receptTime);
        }

       else if (Configuration.setCouchDB) {

            couchDB.saveEntryLDF(ipClient,
                    receptTime + Integer.toString(cntPacts), ldfServer, "",
                    allAnswers, reqTime, queryASCII, cntPacts);
        }
    }

    /**
     * Parse and count a source file's total number of lines
     *
     * @param fileName path to source log file
     * @return total number of log lines
     */
    private int lineNumberCount(String fileName) {

        LineNumberReader lnr = null;
        int linesNumber = 0;

        try {

            lnr = new LineNumberReader(new FileReader(new File(fileName)));
            lnr.skip(Long.MAX_VALUE);
            linesNumber = lnr.getLineNumber();
        } catch (Exception ex) {

            Logger.getLogger(SaveInDB.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {

                lnr.close();
            } catch (Exception ex) {

                Logger.getLogger(SaveInDB.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return linesNumber;
    }

}