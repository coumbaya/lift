package mylift;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class for interacting with MonetDB
 *
 * @author TRISTAN Jarry, PICHAUD Thibaut, Nassopoulos Georges
 *
 * @version 1.0
 * @since 2016-12-15
 */
public class MonetDBManag {

    
    // Current entry id of log Table
    private int IDAns;
    // Driver connected to MonetDB
    private final String MONETDB_DRIVER="nl.cwi.monetdb.jdbc.MonetDriver";
    // Statmeent of SQL request
    private Statement st;
    // Result set of SQL response
    private ResultSet rs;

    public MonetDBManag() {

        IDAns = 1;
    }

    /**
     * Open a new session with the MonetDB
     *
     * @param dbIPAddress address of local or distant monetDB server
     * @param pass current DB's password
     * @param user current DB's user name
     */
    public void openSession(String dbIPAddress, String user, String pass) {

        try {
            
            //make sure the ClassLoader has the monetDB JDBC driver loaded     
            Class.forName(MONETDB_DRIVER).newInstance();
            // request a Connection to a monetDB server running on 'localhost'
            Connection con = DriverManager.getConnection(dbIPAddress, user, pass);
            st = con.createStatement();
        } catch (Exception ex) {

            Logger.getLogger(MonetDBManag.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Delete a table in MonetDB
     *
     * @param dbname name of the new DB to be created
     */
    public void createDB(String dbname) {

        try {
            
            Runtime.getRuntime().exec("monetdb create " + dbname + "");
            Runtime.getRuntime().exec("monetdb release " + dbname + "");

            BasicUtilis.printInfo("Document Createed: " + dbname);
            
        } catch (Exception ex) {
            
            Logger.getLogger(MonetDBManag.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Reset existing table "tableQrsAndAns" into DB
     *
     */
    public void resetMDB() {

        try {
            
            st.executeUpdate("DROP TABLE feta." + Configuration.nameDB + ";\n");
            IDAns = 1;
            
            BasicUtilis.printInfo("Table deleted: " + Configuration.nameDB);

            st.executeUpdate("CREATE TABLE " + Configuration.nameDB + " "
                    + "(ID VARCHAR(1000), "
                    + " ClientIPAddress VARCHAR(10000), "
                    + " LDFserver VARCHAR(10000), "
                    + " RequestQuery VARCHAR(100000), "
                    + " AnsFragment VARCHAR(1410065408), "
                    + " RequestTime VARCHAR(10000), "
                    + " ReceptionTime VARCHAR(10000))");

            BasicUtilis.printInfo("Table added: " + Configuration.nameDB);
        } catch (Exception ex) {
            
            Logger.getLogger(MonetDBManag.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Create table "tableQrsAndAns", in the existing monetDB's DB
     *
     */
    public void createMDB() {

        try {
            
            IDAns = 1;
            st.executeUpdate("CREATE TABLE " + Configuration.nameDB + " "
                    + "(ID VARCHAR(1000), "
                    + " ClientIPAddress VARCHAR(10000), "
                    + " LDFserver VARCHAR(10000), "
                    + " RequestQuery VARCHAR(100000), "
                    + " AnsFragment VARCHAR(1410065408), "
                    + " RequestTime VARCHAR(10000), "
                    + " ReceptionTime VARCHAR(10000))");

            BasicUtilis.printInfo("Table added: " + Configuration.nameDB);
        } catch (Exception ex) {
            
            Logger.getLogger(MonetDBManag.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * This method save a new entry into table "logQueriesAnswers"
     *
     * Every entry is represented as five-tuple of the form
     * <IdEntry, Answer, CLientTCPport, SPARQLEndpoint, ReceptionTime, ClientIPAdress>
     *
     * @param table table in which entries will be saved
     * @param clientIpAddress client's IPAddress
     * @param reqTime queryTP request time
     * @param LDFserver LDF server
     * @param ansFragment server's answer, sent in fragment format
     * @param resTime server's response time
     * @param queryTP requested queryTP
     * @param indexEntry new entry to be created with above info
     */
    public void saveEntryLDF(String table,int indexEntry, String clientIpAddress,
            String LDFserver, String queryTP, String ansFragment, String resTime,  String reqTime) {

        try {
            
            // processing special characters in Strings 
            queryTP = queryTP.replace("\'", "\"");
            ansFragment = ansFragment.replace("\'", "\"");

            st.executeUpdate("INSERT INTO tableQrsAndAns" + Configuration.nameDB + "" + " VALUES ('" + IDAns + "','"
                    + clientIpAddress + "', '" + reqTime + "', '" + LDFserver + "', '" + ansFragment + "', '" + resTime + "', '" + queryTP + "');");
            IDAns++;
            
        } catch (Exception ex) {
            
            Logger.getLogger(MonetDBManag.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    
    /**
     * Get number of entries, for a specific Table
     *
     * @param tableName exisiting Table
     * @return number of lines
     */
    public int getTableSize(String tableName) {

        int count = 0;

        try {
            
            rs = st.executeQuery("SELECT COUNT(*) FROM feta." + tableName + "");
            
            while (rs.next()) {

                count = Integer.parseInt(rs.getString("L1"));
            }

        } catch (Exception ex) {

            Logger.getLogger(MonetDBManag.class.getName()).log(Level.SEVERE, null, ex);
        }

        return count;
    }

    /**
     * Get all specific entry's information, from "tableQrsAndAns" table
     *
     * @param idEntry
     * @return specific entry's information
     */
    public List<String> getEntryAnswers(int idEntry) {
        
        List<String> entryInformation = null;
        
        try {
            
            entryInformation = new LinkedList<>();
            rs = st.executeQuery("SELECT * FROM feta." + Configuration.nameDB + " WHERE ID = '" + idEntry + "'");

            while (rs.next()) {

                //String id = rs.getString("idEntry");
                String ipAddr = rs.getString("ClientIPAddress");
                String ldfServer = rs.getString("LDFserver");
                String query = rs.getString("RequestQuery");
                String answer = rs.getString("AnsFragment");
                String reqTime = rs.getString("RequestTime");
                String respTime = rs.getString("ReceptionTime");

                entryInformation.add(answer);
                entryInformation.add(ldfServer);
                entryInformation.add(ipAddr);
                entryInformation.add(query);
                entryInformation.add(reqTime);
                entryInformation.add(respTime);
            }
        } catch (Exception ex) {
            
            Logger.getLogger(MonetDBManag.class.getName()).log(Level.SEVERE, null, ex);
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
        int monetDBSize = getTableSize(Configuration.nameDB);

        for (int i = 1; i < monetDBSize; i++) {

            try {
                
                entryInformation = getEntryAnswers(i);
                InitLift.mapAnsIDtoEntry.put(i, entryInformation);
            } catch (Exception ex) {
                
                Logger.getLogger(MonetDBManag.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

}