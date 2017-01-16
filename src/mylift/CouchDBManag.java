package mylift;

import com.fourspaces.couchdb.Database;
import com.fourspaces.couchdb.Document;
import com.fourspaces.couchdb.Session;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import net.sf.json.JSONObject;

/**
 * Class for interacting with CouchDB
 *
 * @author Nassopoulos Georges
 * @version 1.0
 * @since 2016-12-15
 */
public class CouchDBManag {

    // database instance
    private Database currDB;
    // session with CouchDB
    private Session dbSession;
    // Current Document of "endpointsAnswers" with log of entries
    private Document myDoc;
    // Current entry id for "myDoc": we start from 2 as we have 2
    // entries by default in a CouchDB document, "_id" and "_rev" 
    private int idEntAns;
    // Current id of Document of List "myDoc"
    private int idDoc;
    // each Document entry has 6 keys, except "_id" and "_rev"
    private final int SIZE_DOCUMENT = 6;
    // List of Documents stored in database, (i.e., "endpointsAnswers")
    private final List<Document> myListDocs;

    // enum with correspondance between key id and semantics
    private enum entryInfo {

        IPADDR(0), RESPTIME(1), LDFSERVER(2), INJECTVALS(3), FRAGMENT(4), REQTIME(5), QUERY(6);

        private final int value;

        private entryInfo(int value) {

            this.value = value;
        }

        private int getValue() {

            return value;
        }
    }

    public CouchDBManag() {

        idEntAns = 2;
        myListDocs = new LinkedList<>();
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

    /**
     * Open a new session with the DB, using username and password
     *
     * @param localhostAddr IP address host of couchdb server
     * @param localPort couchdb server port, by default "5984"
     * @param userName user name 
     * @param password password
     */
    public void openSession(String localhostAddr, int localPort, String userName, String password) {

        dbSession = new Session(localhostAddr, localPort, userName, password);
    }

    /**
     * Get the already created DB
     *
     * @param dbName the name of DB to be loaded
     */
    public void getDB(String dbName) {

        if (dbSession == null) {

            openSession("localhost", 5984);
        }

        currDB = dbSession.getDatabase(dbName);
    }

    /**
     * Create a new DB
     *
     * @param dbName the name of DB to be created
     */
    public void createDB(String dbName) {

        currDB = dbSession.createDatabase(dbName);
    }

    /**
     * Reset an exisiting DB
     *
     * @param dataBase DB name to be reset
     */
    public void resetDB(String dataBase) {

        if (Configuration.verbose) {

            BasicUtilis.printInfo("reCreateDataBase");
        }

        deleteDB(dataBase);
        createDB(dataBase);
    }

    /**
     * Delete an existing DB
     *
     * @param dbName DB name to reset
     */
    public void deleteDB(String dbName) {

        if (!dbName.equals("")) {

            if (dbSession.deleteDatabase(dbName) == false) {
                BasicUtilis.printInfo("Error: Database could not be deleted");
            }
        } else {
            BasicUtilis.printInfo("Error: Database is empty");
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

        if (myListDocs.add(myDoc) == false) {

            BasicUtilis.printInfo("Error: Document could not be added in the DB");
        }

    }

    /**
     * Add a new Document "endpointsAnswers" into current list of Documents
     *
     * @param currDoc the name of current Document that will be added
     */
    public void addDocument(String currDoc) {

        if (myListDocs.add(currDB.getDocument(currDoc)) == false) {

            BasicUtilis.printInfo("Error: Document could not be added in the DB");
        }

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
     * @param injectedVals values injected into the requested fragment
     * @param answerFrag answers values of the requested fragment
     * @param ReqTime query's reception time
     * @param query query engine's request query
     * @param indexDoc index of the Document to which the entry will be stored
     */
    public void saveEntryLDF(String clientIpAddress, String RespTIme,
            String endpointPort, String injectedVals, String answerFrag, String ReqTime, String query, int indexDoc) {

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

        saveFinalDocument(myListDocs.get(0));
    }

    /**
     * Default save entry function of CouchDB: "saveDocAnswers" call this
     * function once they the number of entries has been treated
     *
     * @param currDoc name of Document into which entries will be saved
     */
    public void saveFinalDocument(Document currDoc) {

        if (Configuration.verbose) {

            BasicUtilis.printInfo("saveFinalDocument");
            BasicUtilis.printInfo(currDoc.getId());
        }

        currDB.saveDocument(currDoc);
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

        entryClient = (JSONObject) InitLift.docAnswers.getJSONObject(String.valueOf(idEntry));
        Entry.add(0, entryClient.get("InjectedVals").toString());
        Entry.add(1, entryClient.get("SPARQLEndpointPort").toString());
        Entry.add(2, entryClient.get("ClientIpAddress").toString());
        Entry.add(3, entryClient.get("ReceptionTime").toString());
        Entry.add(4, entryClient.get("RequestQuery").toString());
        Entry.add(5, entryClient.get("AnswerFragmemnts").toString());

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
        Entry = InitLift.mapAnsIDtoEntry.get(idEntry);

        if (Entry.size() == SIZE_DOCUMENT) {

            entryInformation.add(Entry.get(entryInfo.LDFSERVER.getValue()));
            entryInformation.add(Entry.get(entryInfo.RESPTIME.getValue()));
            entryInformation.add(Entry.get(entryInfo.FRAGMENT.getValue()));
            entryInformation.add(Entry.get(entryInfo.INJECTVALS.getValue()));
            entryInformation.add(Entry.get(entryInfo.IPADDR.getValue()));
            entryInformation.add(Entry.get(entryInfo.REQTIME.getValue()));
        }

        return entryInformation;
    }

    /**
     * Parse every answer string, and match all answer entities (IRIs/Literals)
     * to the corresponding hashMaps
     *
     */
    public void setAnswerStringToMaps() {

        List<String> entryInformation = null;

        for (Object key : InitLift.docAnswers.keySet()) {

            if (key.toString().contains("_")) {

                continue;
            }

            entryInformation = getAnswerEntryLDF(key.toString());
            InitLift.mapAnsIDtoEntry.put(Integer.parseInt(key.toString()), entryInformation);
        }
    }

}