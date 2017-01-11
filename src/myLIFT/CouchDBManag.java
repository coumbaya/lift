package myLIFT;

import com.fourspaces.couchdb.Database;
import com.fourspaces.couchdb.Document;
import com.fourspaces.couchdb.Session;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import net.sf.json.JSONObject;

import static myLIFT.Deduction.mapAnsIDtoEntry;
import static myLIFT.Main.verbose;
import static myLIFT.Deduction.docAnswers;
import static myLIFT.Deduction.mapEndpointToName;
import static myLIFT.Main.enableFragm;
import static myLIFT.Main.engineName;
import static myLIFT.Main.traceGen;

/**
 * Class for interacting with CouchDB
 *
 * @author Nassopoulos Georges
 * @version 1.0
 * @since 2016-03-19
 */
public class CouchDBManag {

    private Database db;
    private Session dbSession;

    BasicUtilis myBasUtils;

    FileWriter writerCapt;
    // List of Documents stored in database, (i.e., "endpointsAnswers")
    public static List<Document> myListDocs;
    // Current Document of "endpointsAnswers" with log of entries in capture.log
    private Document myDoc;
    // Current entry id for "myDoc": we start from 2 as we have 2
    // entries by default in a CouchDB document, "_id" and "_rev" 
    private int idEntAns = 2;
    // Current id of Document of List "myDoc"
    private int idDoc;

    public CouchDBManag() throws IOException {

        myListDocs = new LinkedList<>();
        myBasUtils = new BasicUtilis();

        if (traceGen) {

            writerCapt = new FileWriter("capture.log");
        }
    }

    /**
     * Open a new session with the DB
     *
     * @param localhostAddr IP address host of couchdb server
     * @param localPort couchdb server port, by default "5984"
     */
    public void openSession(String localhostAddr, int localPort) {

        dbSession = new Session(localhostAddr, localPort);
    }

    public void openSession2(String localhostAddr, int localPort, String userName, String password) {

        dbSession = new Session(localhostAddr, localPort, userName, password);
    }

    /**
     * Get the already created DB
     *
     * @param dbName the name of DB to be loaded
     */
    public void getDatabase(String dbName) {

        db = dbSession.getDatabase(dbName);
    }

    /**
     * Create a new DB
     *
     * @param dbName the name of DB to be created
     */
    public void createDatabase(String dbName) {

        db = dbSession.createDatabase(dbName);
    }

    /**
     * Reset an exisiting DB
     *
     * @param dataBase DB name to be reset
     */
    public void resetDB(String dataBase) {

        if (verbose) {

            System.out.println("reCreateDataBase");
        }

        deleteDatabase(dataBase);
        createDatabase(dataBase);
    }

    /**
     * Delete an existing DB
     *
     * @param dbName DB name to reset
     */
    public void deleteDatabase(String dbName) {

        if (!dbName.equals("")) {

            dbSession.deleteDatabase(dbName);
        }

    }

    /**
     * Create the fist Document where entries will be loaded
     *
     * @param currDoc the name of first Document that will be created
     */
    public void createfirstDocument(String currDoc) {

        myDoc = new Document();
        myDoc.setId(currDoc);
        myListDocs.add(myDoc);
    }

    /**
     * Add a new Document "endpointsAnswers" into current list of Documents
     *
     * @param currDoc the name of current Document that will be added
     */
    public void addDocument(String currDoc) {

        myListDocs.add(db.getDocument(currDoc));
    }

    /**
     * Get the the current Docment from the list of all Documents
     *
     * @param currDoc the name of current Document that will be returned
     * @return the Document that was passed as parameter
     */
    public Document getDocument(String currDoc) {

        return myListDocs.get(indexOfDocument(currDoc));
    }

    /**
     * Get the id of the current Docment from the list of all Documents
     *
     * @param currDoc the name of current Document that will be returned
     * @return the Document that was passed as parameter
     */
    public int indexOfDocument(String currDoc) {

        for (int i = 0; i < myListDocs.size(); i++) {
            if (myListDocs.get(i).getId().equals(currDoc)) {

                return i;
            }
        }
        return -1;
    }

    /**
     * Get all created Documents
     *
     * @return list of Documents
     */
    public List<Document> getDocList() {

        return myListDocs;
    }

    /**
     * Get the id of the current Docment from the list of all Documents
     *
     * @return id of the current Doument
     */
    public int getIdDoc() {

        return idDoc;
    }

    /**
     * Save a new entry into current Document "endpointsAnswers"
     *
     * Every entry is represented as five-tuple of the form
     *
     * <IdEntry, Answer, CLientTCPport, SPARQLEndpoint, ReceptionTime, ClientIPAdress>
     *
     * Each entry is saved as a Document into the Document "endpointsAnswers"
     *
     * @param clientIpAddress query engine's IPAddress
     * @param RespTIme query engine's reception time
     * @param endpointPort virtuoso's sender endpoint port
     * @param answer endpoint's answer, in jason format for SPARQL queries
     * @param ReqTime query's reception time
     * @param query query engine's requested query
     * @param indexDoc index of the Document to which the entry will be stored
     */
    public void saveEntryAnswers(String clientIpAddress, String RespTIme,
            String endpointPort, String answer, String ReqTime, String query, int indexDoc) {

        if (verbose) {

            System.out.println("--------------------------------------------");
        }

        Document doc = new Document();

        doc.setId(Integer.toString(idEntAns));
        doc.put("ClientIpAddress", clientIpAddress);
        doc.put("ClientTCPport", RespTIme);
        doc.put("SPARQLEndpointPort", endpointPort);
        doc.put("Answer", answer);
        doc.put("ReceptionTime", ReqTime);
        doc.put("RequestQuery", query);

        myListDocs.get(0).put(Integer.toString(myListDocs.get(0).size() + 1), doc);
        idEntAns++;
        saveFinalDocument(myListDocs.get(0));
    }

    /**
     * Save a new entry into current Document "endpointsAnswers"
     *
     * Every entry is represented as five-tuple of the form
     *
     * <IdEntry, Answer, CLientTCPport, SPARQLEndpoint, ReceptionTime, ClientIPAdress>
     *
     * Each entry is saved as a Document into the Document "endpointsAnswers"
     *
     * @param clientIpAddress query engine's IPAddress
     * @param RespTIme query engine's reception time
     * @param endpointPort virtuoso's sender endpoint port
     * @param injectedVals values injected into the requested fragment
     * @param answerFrag answers values of the requested fragment
     * @param ReqTime query's reception time
     * @param query query engine's request query
     * @param indexDoc index of the Document to which the entry will be stored
     */
    public void saveEntryLDF(String clientIpAddress, String RespTIme,
            String endpointPort, String injectedVals, String answerFrag, String ReqTime, String query, int indexDoc) {

        if (verbose) {

            System.out.println("--------------------------------------------");
        }

        Document doc = new Document();

        doc.setId(Integer.toString(idEntAns));
        doc.put("ClientIpAddress", clientIpAddress);
        doc.put("ClientTCPport", RespTIme);
        doc.put("SPARQLEndpointPort", endpointPort);
        doc.put("InjectedVals", injectedVals);
        doc.put("AnswerFragmemnts", answerFrag);
        doc.put("ReceptionTime", ReqTime);
        doc.put("RequestQuery", query);

        myListDocs.get(0).put(Integer.toString(myListDocs.get(0).size() + 1), doc);
        idEntAns++;
        
         //saveFinalDocument(myListDocs.get(0));
        saveFinalDocument(myListDocs.get(0));
    }

    /**
     * Default save entry function of CouchDB: "saveDocAnswers" call this
     * function once they the number of entries has been treated
     *
     * @param currDoc name of Document into which entries will be saved
     */
    public void saveFinalDocument(Document currDoc) {

        if (verbose) {

            System.out.println("saveFinalDocument");
            System.out.println(currDoc.getId());
        }

        db.saveDocument(currDoc);
    }

    /**
     * Get all information of an answer entry, from "endpointsAnswers"
     *
     * @param idEntry the current answer entry
     * @return all specific entry's information
     */
    public List<String> getAnswerEntry(String idEntry) {

        List<String> Entry = new LinkedList<>();
        JSONObject entryClient = null;

        try {

            entryClient = (JSONObject) docAnswers.getJSONObject(String.valueOf(idEntry));
            Entry.add(0, entryClient.get("Answer").toString());
            Entry.add(1, entryClient.get("SPARQLEndpointPort").toString());
            Entry.add(2, entryClient.get("ClientIpAddress").toString());
            Entry.add(3, entryClient.get("ReceptionTime").toString());
            Entry.add(4, entryClient.get("RequestQuery").toString());
            Entry.add(5, entryClient.get("ClientTCPport").toString());

        } catch (Exception ex) {
            System.out.println(ex);
        }

        return Entry;
    }

    /**
     * Get all information of an answer entry, from "endpointsAnswers"
     *
     * @param idEntry the current answer entry
     * @return all specific entry's information
     */
    public List<String> getAnswerEntryLDF(String idEntry) {

        List<String> Entry = new LinkedList<>();
        JSONObject entryClient = null;

        try {

            entryClient = (JSONObject) docAnswers.getJSONObject(String.valueOf(idEntry));
            Entry.add(0, entryClient.get("InjectedVals").toString());
            Entry.add(1, entryClient.get("SPARQLEndpointPort").toString());
            Entry.add(2, entryClient.get("ClientIpAddress").toString());
            Entry.add(3, entryClient.get("ReceptionTime").toString());
            Entry.add(4, entryClient.get("RequestQuery").toString());
            Entry.add(5, entryClient.get("AnswerFragmemnts").toString());

        } catch (Exception ex) {
            System.out.println(ex);
        }

        return Entry;
    }

    /**
     * Get all answer entry information, from the Answer Log hash map
     *
     * @param ansDocMap the input "endpointsAnswers" hashMap
     * @param idEntry the current answer entry
     * @return all specific entry's information
     */
    public List<String> getEntryAnswerLogHashMap(Map<Integer, List<String>> ansDocMap, int idEntry) {

        List<String> Entry = new LinkedList<>();
        List<String> entryInformation = new LinkedList<>();
        Entry = mapAnsIDtoEntry.get(idEntry);

        if (Entry.size() == 6) {

            entryInformation.add(Entry.get(2));
            entryInformation.add(Entry.get(1));

            int indexStartLIMIT = Entry.get(4).indexOf("LIMIT");

            if (indexStartLIMIT > 0) {

                entryInformation.add(Entry.get(4).substring(0, indexStartLIMIT - 1));
            } else {

                entryInformation.add(Entry.get(4));
            }

            entryInformation.add(Entry.get(3));

            if (enableFragm) {

                entryInformation.add(Entry.get(0));
                entryInformation.add(Entry.get(5));
            }
        }

        return entryInformation;
    }

    /**
     * Parse every answer string, and match all answer entities (IRIs/Literals)
     * to the corresponding hashMaps
     *
     * @throws java.io.IOException
     * @throws java.net.URISyntaxException
     */
    public void setAnswerStringToMaps() throws IOException, URISyntaxException {

        List<String> entryInformation = null;
        String Answer = "", requestQuery = "";

        for (Object key : docAnswers.keySet()) {

            try {

                if (key.toString().contains("_")) {

                    continue;
                }

                if (enableFragm) {

                    entryInformation = getAnswerEntryLDF(key.toString());
                } else {

                    entryInformation = getAnswerEntry(key.toString());
                }
                if (traceGen) {

                    addEntryInTrace(entryInformation);
                }

                mapAnsIDtoEntry.put(Integer.parseInt(key.toString()), entryInformation);

                if (!entryInformation.isEmpty()) {

                    entryInformation = mapAnsIDtoEntry.get(Integer.parseInt(key.toString()));
                    if (!entryInformation.isEmpty()) {

                        Answer = entryInformation.get(0);
                        requestQuery = entryInformation.get(4);
                        myBasUtils.setVarsToAnswEntities(Integer.parseInt(key.toString()), requestQuery, Answer);
                    }

                }

            } catch (NumberFormatException e) {

                System.out.println(e);
            }

        }
    }

    public void addEntryInTrace(List<String> entryInformation) throws IOException, URISyntaxException {

        if (engineName.contains("FedX")) {

            writerCapt.write("POST /sparql/ HTTP/1.1\n");
            writerCapt.write("Content-Type: application/x-www-form-urlencoded; charset=utf-8\n");
            writerCapt.write("Accept: application/x-binary-rdf-results-table;q=0.8\n");
            writerCapt.write("Accept: application/sparql-results+xml\n");
            writerCapt.write("Accept: application/sparql-results+json;q=0.8\n");
            writerCapt.write("Accept: text/tab-separated-values;q=0.8\n");
            writerCapt.write("Accept: text/csv;q=0.8\n");
            writerCapt.write("User-Agent: Jakarta Commons-HttpClient/3.1\n");
            writerCapt.write("Host: 172.16.9.15:" + entryInformation.get(1) + "\n");
            writerCapt.write("Content-Length: \n");
            writerCapt.write("\n");
        }

        URI uri = new URI("http", null, "localhost", 8900,
                "/sparql/", "default-graph-uri=&query=" + entryInformation.get(4) + "&format=json&timeout=0&debug=on", null);

        writerCapt.write("queryLn=SPARQL&query=" + uri.toString().substring(uri.toString().indexOf("&query=") + 7, uri.toString().indexOf("&format=json")) + "&infer=falseHTTP/1.1 200 OK\n");

        if (engineName.contains("FedX")) {

            writerCapt.write("Server: Virtuoso/06.01.3127 (Linux) x86_64-unknown-linux-gnu\n");
            writerCapt.write("Connection: Keep-Alive\n");
            writerCapt.write("Date: Wed, 02 Dec 2015 " + entryInformation.get(3) + " GMT\n");
            writerCapt.write("Accept-Ranges: bytes\n");
            writerCapt.write("X-SPARQL-default-graph: http://localhost:" + entryInformation.get(1) + "/" + mapEndpointToName.get(entryInformation.get(1)) + "\n");
            writerCapt.write("Content-Type: application/sparql-results+json\n");
            writerCapt.write("Content-Length: \n");
            writerCapt.write("\n");
            writerCapt.write("\n");
        }

        writerCapt.write(entryInformation.get(0));
        writerCapt.write("\n");

    }

}