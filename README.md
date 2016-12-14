# LIFT

LInked data Fragment Tracking

## Install LIFT's dependencies

LIFT is implemented in Java 1.7 and known to run on Debian GNU/Linux and OS X. In order to install packages and dependencies related to LIFT, you need to execute **installDepends<os-system>.sh**. This script will install:

   1. justniffer: Network TCP Packet Sniffer
   
        http://justniffer.sourceforge.net/

      Justniffer is a network protocol analyser that captures network traffic and produces logs in a customized way, 
      can emulate Apache web server log files, track response times and extract all "intercepted" files from the HTTP 
      traffic.
      
      We use this tool, in order to capture answers of triple pattern queries (i.e., selecors), when re executing the real log of DBpedia from USEWOD. 
      You may create your own captures, using the command:
      
      `$ sudo justniffer -i eth0 -l "%request%request.timestamp%response%response.timestamp"`
      
      **NB**; In order to ensure capturing all queries and their answers, you must keep sniffing the TCP traffic even for some tens of seconds after the end of the execution of your federated queries. 
   
   2. CouchDB: A Database for the Web
   
        http://couchdb.apache.org/

      Apache CouchDB is a document-oriented NoSQL database, which is implemented in the concurrency-oriented language 
      Erlang and uses JSON to store data, JavaScript as its query language using MapReduce indexes, and 
      regular HTTP 7 for an API. 
      
      This DB system is used to store LIFT's input logs (from individual or federation of LDF servers).
   
   3. monetDB: The column-store pioneer
      
        https://www.monetdb.org/Home

      MonetDB is a full-fledged relational column-oriented DBMS, that supports the SQL:2003 
      standard, provide client interfaces (e.g. ODBC and JDBC), as well as application programming interfaces for
      various languages (C, Python, Java, Ruby, Perl, and PHP).
   
      This is an alternative DB system used to store LIFT's input logs (from individual or federation of LDF servers).

**Note**Traces of queries executed in the web browser, (e.g., Google Chrome), we used the preinstal tool WebInspector (More utils-> Developement Utils-> Network Traffic)

## Run LIFT

In order to execute LIFT, you must run the command:

`$ java -jar "myLIFT.jar" -[option]`

The first step, is to load the captured trace into a database, of your DBMS choice:

`--load` or `-l <path_to_trace>`: for loading a trace into the DB

`--resetDB` or `-r`: for resetting an existing DB

`--systemDB` or `-s <dbms_to_use>`: for setting "couchDB" or "monetDB" system (by default "couchDB")

`--nameDB` or `-n <db_name>`: with the DB name

Then, you can launch LIFT's deduction algorithm:

`--setWinSlice` or `-ws <window_in_seconds>`: for setting the maximum temporal distance between first and last subquery, defining the input DB slice (by default 3600 seconds)

`--setWinJoin` or `-wj <window_in_seconds>`: for setting the maximum joinable window interval gap between two subqueries or triple patterns (by default 3600 seconds)

## Testing LIFT with queries' traces in http://client.linkeddatafragments.org

In order to test LIFT's functionality, you can use traces from queries of the menu in http://client.linkeddatafragments.org. 

1. In directory [**query_traces_isolated**](https://github.com/coumbaya/lift/tree/master/experiments_with_client.linkeddatafragments.org/query_traces_isolated) you find traces of queries executed in isolation, each in the same LDF server.
2. In directory [**query_traces_concurrency**](https://github.com/coumbaya/lift/tree/master/experiments_with_client.linkeddatafragments.org/query_traces_concurency) you find traces of queries of the same collection executed in concurrence, each in the same LDF server.

LIFT's experiment results, with traces of queries in http://client.linkeddatafragments.org, are available [here](https://github.com/coumbaya/feta/blob/master/experiments_with_client.linkeddatafragments.org.md)

You can simulate your own concurrent execution trace and generate your suffle with our **traceMixer** programm, availble  [here](https://github.com/coumbaya/traceMixer)


## About and Contact

LIFT was developed at University of Nantes as an ongoing academic effort. You can contact the current maintainers by email at georges.nassopoulos[at]etu[dot]univ-nantes[dot]fr.

