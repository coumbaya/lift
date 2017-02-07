package mylift;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class for configuring LIFT execution parameters, using as input the user
 * defined options
 *
 * @author Nassopoulos Georges
 * @version 1.0
 * @since 2016-12-15
 */
public class Configuration {

    /* LIFT: loading traces in DB (monetDB or couchDB) */
    //enabling the load ing of subqueries and their answers in DB
    public static boolean loadDB;
    //setting the DB name
    public static String nameDB;
    //enabling "couchdDB" for LIFT (by default its "couchDB")
    public static boolean setCouchDB;
    //enabling "monetDB" for LIFT (by default its "couchDB")
    public static boolean setMonetDB;
    //reset existing DB
    public static boolean resetDB;
    //seting the path to the execution trace to be loaded
    public static String logPath;
    //setting couchDB collection name
    public static String collectionName = "DFWEBINSPECTOR";

    /* LIFT deduction's features */
    // window time interval, in seconds, defining the DB slice used as input for
    // LIFT, from the first to last captured subquery
    public static int sliceWin;
    // window time interval, in seconds, defining the maximum temporal gap value
    // between two triple patterns, to consider them as potentially part of the same join
    public static int gapWin;
    //load LDF traces generated with "webinspector" tool (by default "webInspector")
    public static boolean webInsTraces;
    //load LDF traces generated as "xml response" when evaluating each selector
    //(by default "webInspector")
    public static boolean xmlRespTraces;
    // enable or not LDF "graphReduction"
    public static boolean graphReduction;
    //verbose option for more detailed output
    public static boolean verbose;
    //enabling extraction of joins, for isolated execution traces
    public static boolean isolatedExec;
    //enabling statistics of recall/precision, for concurrent execution traces 
    public static boolean concurentExec;

    /**
     * This method gets all the input arguments and treats every case
     *
     * @param args
     */
    public void setParameteres(final String[] args) {

        try {

            initVariables();

            if (args.length > 0) {

                BasicUtilis.printInfo("User defined parameters of LIFT: ");
            }

            for (int i = 0; i < args.length; i++) {

                switch (args[i]) {
                    case "--verbose":
                    case "-v":
                        verbose = true;
                        break;
                    case "--load":
                    case "-l":
                        loadDB = true;
                        logPath = args[i + 1];
                        if (args[i + 1].startsWith("-") || args[i + 1].equalsIgnoreCase("")) {

                            BasicUtilis.printInfo("Please give the exeution trace path, to be loaded: ");
                            System.exit(-1);
                        }
                        continue;
                    case "--resetDB":
                    case "-r":
                        resetDB = true;
                        break;
                    case "--nameDB":
                    case "-n":
                        nameDB = args[i + 1];

                        if (nameDB.contains("/")) {
                            nameDB = nameDB.substring(nameDB.indexOf("couchdb/") + 8, nameDB.indexOf("."));
                        }
                        if (args[i + 1].startsWith("-") || args[i + 1].equalsIgnoreCase("")) {

                            BasicUtilis.printInfo("Please give the DB's name: ");
                            System.exit(-1);
                        }
                        continue;
                    case "--couchDB":
                    case "-c":
                        setCouchDB = true;
                        break;
                    case "--monetDB":
                    case "-m":
                        setMonetDB = true;
                        break;
                    case "--webInterface":
                    case "-w":
                        webInsTraces = true;
                        collectionName = "DFWEBINSPECTOR";
                        break;
                    case "--xmlResponse":
                    case "-x":
                        webInsTraces = false;
                        collectionName = "DFXML";
                        break;
                    case "--gapWindow":
                    case "-gW":
                        gapWin = Integer.parseInt(args[i + 1]);
                        if (args[i + 1].startsWith("-") || args[i + 1].equalsIgnoreCase("")) {

                            BasicUtilis.printInfo("Please give a valid window join (gap) value: ");
                            System.exit(-1);
                        }
                        break;
                    case "--sliceWindow":
                    case "-sW":
                        sliceWin = Integer.parseInt(args[i + 1]);
                        if (args[i + 1].startsWith("-") || args[i + 1].equalsIgnoreCase("")) {

                            BasicUtilis.printInfo("Please give a valid window slice value: ");
                            System.exit(-1);
                        }
                        break;
                    case "--graphReduction":
                    case "-gr":
                        graphReduction = true;
                        break;
                    case "--help":
                    case "-h":
                        usage();
                        break;
                    default:
                        BasicUtilis.printInfo("Unknow argument");
                }

            }

            showGeneralInfo();
        } catch (Exception ex) {

            Logger.getLogger(Configuration.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /*
     * This method inits all program's options, objects and paths to be used
     */
    private void initVariables() {

        loadDB = false;
        resetDB = false;
        setCouchDB = true;
        setMonetDB = false;
        logPath = "queryRebutal.har";
        nameDB = "tot";
        collectionName = "DFWEBINSPECTOR";
        webInsTraces = true;
        xmlRespTraces = false;

        sliceWin = 3600;
        gapWin = 3600;
        verbose = true;
        isolatedExec = false;
        concurentExec = false;
        graphReduction = false;
    }

    /**
     * Print a usage menu, when it is passed as argument or when an arguments
     * are not syntaxically correct when they are passed
     */
    private void usage() {

        BasicUtilis.printInfo(" Usage : --load or -l <path_to_capture>: for loading a capture into the DB");
        BasicUtilis.printInfo("         --resetDB or -r: for reseting an existing DB");
        BasicUtilis.printInfo("         --nameDB or -n: for setting DB name");
        BasicUtilis.printInfo("         --couchDB or -c: for setting \"couchDB\" DB system (by default \"couchDB\")");
        BasicUtilis.printInfo("         --monetDB or -m: for setting \"monetDB\" DB systems (by default \"couchDB\")");
        BasicUtilis.printInfo("         --webInterface or -w: for loading \"webinspector\" generated traces (by default \"webinspector\")");
        BasicUtilis.printInfo("         --xmlResponse or -x: for loading \"xml-response\" generated traces (by default \"webinspector\")");
        BasicUtilis.printInfo("         --sliceWindow or -sW <(start, end)>: for setting the start and end entry of the DB log, used for LIFT deduction "
                + "defining DB slice (by default \"infinity\")");
        BasicUtilis.printInfo("         --gapWindow or -gW <window_in_seconds>: for setting the maximum gap interval between two triple patterns"
                + " (by default 3600 seconds)");
        BasicUtilis.printInfo("         --graphReduction or -gr: to enable graphReduction heuristic to remove \"count\" triple patterns (by default not used)");
        BasicUtilis.printInfo("         --verbose or -v: to enable more detailed output");
        BasicUtilis.printInfo("         --help or -h: for showing help");
    }

    /**
     * Show general info about user defined arguments, before launching LIFT
     */
    private void showGeneralInfo() {

        BasicUtilis.printInfo("------------------------------------- General information---------------------------------------------------------");
        BasicUtilis.printInfo("------------------------------------------------------------------------------------------------------------------\n");

        if (setCouchDB) {
            
            BasicUtilis.printInfo("\t Storage DB system: \"CouchDB\"");
        } else {

            BasicUtilis.printInfo("\t Storage DB system: \"MonetDB\"");
        }

        BasicUtilis.printInfo("\t Database name: \"" + nameDB + "\"");

        if (webInsTraces) {
            
            BasicUtilis.printInfo("\t Input type trace: \"webinspector\"");
        } else {

            BasicUtilis.printInfo("\t Input type trace: \"xml\"");
        }

        if (!loadDB) {

           // BasicUtilis.printInfo("\t Window slice, defining start/end entries of input DB: " + sliceWin + " seconds");
            BasicUtilis.printInfo("\t Window gap, defining maximum joinable "
                    + "temporal distance (for subqueries and triple patterns): " + gapWin + " seconds");
        }

        BasicUtilis.printInfo("\n-----------------------------------------------------------------------------------------------------------------");
        BasicUtilis.printInfo("-----------------------------------------------------------------------------------------------------------------\n");
    }

}
