# FETA

A FEderated TrAcking System for the Linked Data

[1] Tracking Federated Queries in the Linked Data, Georges Nassopoulos, Patricia Serrano-Alvarado, Pascal Molli, Emmanuel Desmontils. Tracking Federated Queries in the Linked Data. [Research Report] LINA-University of Nantes. 2015. <hal-01187519>

## Install FETA's dependencies

FETA is implemented in Java 1.7 and known to run on Debian GNU/Linux and OS X. In order to install packages and dependencies related to FETA, you need to execute **installDepends<os-system>.sh**. This script will install:

   1. justniffer :Network TCP Packet Sniffer
   
        http://justniffer.sourceforge.net/

      Justniffer is a network protocol analyser that captures network traffic and produces logs in a customized way, 
      can emulate Apache web server log files, track response times and extract all "intercepted" files from the HTTP 
      traffic.
      
      We use this tool, in order to capture both queries and their answers. You may create your own captures, when   
      at the same time running queries with either FedX or Anapsid query engines. To do so, you need to run the command:
      
      `$ sudo justniffer -i eth0 -l "%request%request.timestamp%response%response.timestamp"`
      
      **NB**; In order to ensure capturing all queries and their answers, you must keep sniffing the TCP traffic even for some tens of seconds after the end of the execution of your federated queries. 
   
   2. CouchDB: A Database for the Web
   
        http://couchdb.apache.org/

      Apache CouchDB is a document-oriented NoSQL database, which is implemented in the concurrency-oriented language 
      Erlang and uses JSON to store data, JavaScript as its query language using MapReduce indexes, and 
      regular HTTP 7 for an API. 
      
      This DB system is used to store FETA's federated log.
   
   3. monetDB: The column-store pioneer
      
        https://www.monetdb.org/Home

      MonetDB is a full-fledged relational column-oriented DBMS, that supports the SQL:2003       standard, provide client interfaces (e.g. ODBC and JDBC), as well as application programming interfaces for            various languages (C, Python, Java, Ruby, Perl, and PHP).
   
      This is an alternative DB system used to store FETA's federated log.

## Run FETA

In order to execute FETA, you must run the command:

`$ java -jar "myFETA.jar" -[option]`

The first step, is to load the captured trace into a database, of your DBMS choice:

`--load` or `-l <path_to_capture>`: for loading a capture into the DB

`--resetDB` or `-r`: for resetting an existing DB

`--systemDB` or `-s <dbms_to_use>`: for setting "couchDB" or "monetDB" system (by default "couchDB")

`--nameDB` or `-n <db_name>`: with the DB name

Then, you can launch FETA's deduction algorithm:

`--inverseMap` or `-i <inverse_mapping_threshold>`: for enabling inverse mapping in "NestedLoopDetection" heuristic, necessary for FedX, and setting the minimum threshold to validate a matching

`--sameConcept` or `-c <path_to_endpoints_addresses>`: enabling "SameConcept/SameAs" heuristic and passing endpoints IP Addresses as argument

`--setWinSlice` or `-ws <window_in_seconds>`: for setting the maximum temporal distance between first and last subquery, defining the input DB slice (by default 1000000 seconds)

`--setWinJoin` or `-wj <window_in_seconds>`: for setting the maximum joinable window interval gap between two subqueries or triple patterns (by default 1000000 seconds)

## Testing FETA with FedBench queries' traces

In order to test FETA's functionality, you can use traces of FedBench's Cross Domain (CD) and Life Science (LS) collections, captured by using either FedX or Anapsid. 

1. In directory [**query_traces_isolated**](https://github.com/coumbaya/feta/tree/master/experiments_with_fedbench/query_traces_isolated) you find traces of queries executed in isolation.
2. In directory [**query_traces_concurrency**](https://github.com/coumbaya/feta/tree/master/experiments_with_fedbench/query_traces_concurency) you find traces of queries of the same collection executed in concurrence.

FETA's experiment results, for fedbench queries' traces, are available [here](https://github.com/coumbaya/feta/blob/master/experiments_with_fedbench.md)

You can simulate your own concurrent execution trace and generate your suffle with our **traceMixer** programm, availble  [here](https://github.com/coumbaya/traceMixer)


## About and Contact

FETA was developed at University of Nantes as an ongoing academic effort. You can contact the current maintainers by email at georges.nassopoulos[at]etu[dot]univ-nantes[dot]fr.

