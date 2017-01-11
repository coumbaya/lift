package myLIFT;

import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static myLIFT.Deduction.mapAnsIDtoEntry;
import static myLIFT.Main.nameDB;
import static myLIFT.Main.verbose;

/**
 * Class for interacting with MonetDB
 *
 * @author TRISTAN Jarry, PICHAUD Thibaut, Nassopoulos Georges
 * @version 1.0
 * @since 2016-03-19
 */
public class MonetDBManag {

    BasicUtilis myBasUtils;

    // Current id of Table
    private int IDAns;

    ResultSet rs;
    Statement st;

    public MonetDBManag() {

        myBasUtils = new BasicUtilis();
        IDAns = 1;
    }

    /**
     * Open a new session with the MonetDB
     *
     * @param dbIPAddress address of local or distant monetDB server
     * @param pass current DB's password
     * @param user current DB's user name
     * @throws SQLException
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    public void openSession(String dbIPAddress, String user, String pass) throws SQLException, InstantiationException, IllegalAccessException {

        //make sure the ClassLoader has the monetDB JDBC driver loaded
        try {

            Class.forName("nl.cwi.monetdb.jdbc.MonetDriver").newInstance();
        } catch (ClassNotFoundException e) {

            System.out.println(e);
        }

        // request a Connection to a monetDB server running on 'localhost'
        Connection con = DriverManager.getConnection(dbIPAddress, user, pass);
        st = con.createStatement();
    }

    /**
     * This method save a new entry into table "endpointsAnswers"
     *
     * Every entry is represented as five-tuple of the form
     * <IdEntry, Answer, CLientTCPport, SPARQLEndpoint, ReceptionTime, ClientIPAdress>
     *
     * @param table table in which entries will be saved
     * @param clientIpAddress query engine's IPAddress
     * @param reqTime query engine's reception time
     * @param endpointPort virtuoso's endpoint port
     * @param answer endpoint's answer, sent in json format for SPARQL queries
     * @param resTime qeury engine's reception time
     * @param query query engine's request query
     * @param indexEntry new entry to be created with above info
     * @throws SQLException
     * @throws java.io.IOException
     */
    public void saveEntryAnswers(String table, String clientIpAddress, String reqTime,
            String endpointPort, String answer, String resTime, String query, int indexEntry) throws SQLException, IOException {

        if (verbose) {

            System.out.println("--------------------------------------------");
        }

        if (query.contains("\'")) {

            query = query.replace("\'", "\"");
        }

        if (answer.contains("\'")) {

            answer = answer.replace("\'", "\"");
        }

        st.executeUpdate("INSERT INTO tableQrsAndAns" + nameDB + "" + " VALUES ('" + IDAns + "','"
                + clientIpAddress + "', '" + reqTime + "', '" + endpointPort + "', '" + answer + "', '" + resTime + "', '" + query + "');");
        IDAns++;
    }

    /**
     * Delete a table in Monet DB
     *
     * @param dbname name of the new DB to be created
     * @throws SQLException
     */
    public void createDB(String dbname) throws SQLException {

        System.out.println("Create DB" + dbname);

        try {

            Runtime.getRuntime().exec("monetdb create " + dbname + "");
            Runtime.getRuntime().exec("monetdb release " + dbname + "");
        } catch (IOException ex) {

            System.out.println(ex);
        }

        System.out.println("Document Createed: " + dbname);
    }

    /**
     * Reset existing table "tableQrsAndAns" into DB
     *
     * @throws SQLException
     */
    public void resetMDB() throws SQLException {

        System.out.println("reCreateDataBase");

        st.executeUpdate("DROP TABLE feta.tableQrsAndAns" + nameDB + ";\n");
        IDAns = 1;
        System.out.println("Table deleted: tableQrsAndAns" + nameDB);

        st.executeUpdate("CREATE TABLE tableQrsAndAns" + nameDB + " "
                + "(ID VARCHAR(1000), "
                + "ClientIPAddress VARCHAR(10000), "
                + " ClientTCPport VARCHAR(10000), "
                + " SPARQLEndpointPort VARCHAR(10000), "
                + " Answer VARCHAR(1410065408), "
                + " ReceptionTime VARCHAR(10000), "
                + " RequestQuery VARCHAR(100000))");

        System.out.println("Table added: tableQrsAndAns" + nameDB);
    }

    /**
     * Create table "tableQrsAndAns", in the existing monetDB's DB
     *
     * @throws SQLException
     */
    public void createMDB() throws SQLException {

        IDAns = 1;

        st.executeUpdate("CREATE TABLE tableQrsAndAns" + nameDB + " "
                + "(ID VARCHAR(1000), "
                + "ClientIPAddress VARCHAR(10000), "
                + " ClientTCPport VARCHAR(10000), "
                + " SPARQLEndpointPort VARCHAR(10000), "
                + " Answer VARCHAR(1410065408), "
                + " ReceptionTime VARCHAR(10000), "
                + " RequestQuery VARCHAR(100000))");

        System.out.println("Table added: tableQrsAndAns" + nameDB);
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

        } catch (SQLException ex) {

            Logger.getLogger(MonetDBManag.class.getName()).log(Level.SEVERE, null, ex);
        }

        return count;
    }

    /**
     * Get all specific entry's information, from "tableQrsAndAns" table
     *
     * @param idEntry
     * @return specific entry's information
     * @throws SQLException
     */
    public List<String> getEntryAnswers(int idEntry) throws SQLException {

        List<String> entryInformation = new LinkedList<>();

        rs = st.executeQuery("SELECT * FROM feta.tableQrsAndAns" + nameDB + " WHERE ID = '" + idEntry + "'");

        while (rs.next()) {

            //String id = rs.getString("idEntry");
            String ip = rs.getString("ClientIPAddress");
            String tcp = rs.getString("ClientTCPport");
            String ep = rs.getString("SPARQLEndpointPort");
            String ans = rs.getString("Answer");
            String rt = rs.getString("ReceptionTime");
            String rq = rs.getString("RequestQuery");

            entryInformation.add(ans);
            entryInformation.add(ep);
            entryInformation.add(ip);
            entryInformation.add(rt);
            entryInformation.add(rq);
            entryInformation.add(tcp);
        }

        return entryInformation;
    }

    /**
     * Parse every answer string, and match all answer entities (IRIs/Literals)
     * to the corresponding hashMaps
     *
     * @throws java.io.IOException
     * @throws java.net.URISyntaxException
     * @throws java.sql.SQLException
     */
    public void setAnswerStringToMaps() throws IOException, URISyntaxException, SQLException {

        List<String> entryInformation = null;
        String Answer = "", requestQuery = "";
        int monetDBSize = getTableSize("tableQrsAndAns" + nameDB);

        for (int i = 1; i < monetDBSize; i++) {

            entryInformation = getEntryAnswers(i);
            mapAnsIDtoEntry.put(i, entryInformation);

            if (!entryInformation.isEmpty()) {

                entryInformation = mapAnsIDtoEntry.get(i);
                if (!entryInformation.isEmpty()) {

                    Answer = entryInformation.get(0);
                    requestQuery = entryInformation.get(4);
                    myBasUtils.setVarsToAnswEntities(i, requestQuery, Answer);
                }

            }

        }
    }

}