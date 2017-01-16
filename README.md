# LIFT

LInked data Fragment Tracking (LIFT): Extracting Basic Graph Patterns from Triple Pattern Fragment Logs


## LIFT experiments


LIFT experiments are available [here](https://github.com/coumbaya/lift/blob/master/experiments.md), using as input:

(1) traces of queries in http://client.linkeddatafragments.org, each executed one by one, and,<br>
(2) traces of DBpedia LDF server's real log, from [USEWOD](http://usewod.org/data-sets.html) dataset, for the period of 14th October 2014-27th February 2015.


## Install LIFT's dependencies

LIFT is implemented in Java 1.7 and known to run on Debian GNU/Linux and OS X. In order to install packages and dependencies related to LIFT, you need to execute **installDepends.sh** or **installDepends.py**. This script will install: 
   
   1. CouchDB: A Database for the Web
   
        http://couchdb.apache.org/

      Apache CouchDB is a document-oriented NoSQL database, which is implemented in the concurrency-oriented language 
      Erlang and uses JSON to store data, JavaScript as its query language using MapReduce indexes, and 
      regular HTTP 7 for an API. 
      
      This DB system is used to store LIFT's input logs (from individual or federation of LDF servers).
   
   2. monetDB: The column-store pioneer
      
        https://www.monetdb.org/Home

      MonetDB is a full-fledged relational column-oriented DBMS, that supports the SQL:2003 
      standard, provide client interfaces (e.g. ODBC and JDBC), as well as application programming interfaces for
      various languages (C, Python, Java, Ruby, Perl, and PHP).
   
      This is an alternative DB system used to store LIFT's input logs (from individual or federation of LDF servers).



**Note**: Traces of queries executed over TPF servers in http://client.linkeddatafragments.org, were executed in the web browser (e.g., Google Chrome) and captured with the tool WebInspector (More utils-> Developement Utils-> Network Traffic).

## Run LIFT

In order to execute LIFT, you must run the command:

`$ java -jar "lift.jar" -[option]`

The first step, is to load the captured trace into a DBMS database, of your choice:

`--load` or `-l <path_to_trace>`: for loading a trace into the DB

`--resetDB` or `-r`: for resetting an existing DB

`--systemDB` or `-s <dbms_to_use>`: for setting "couchDB" or "monetDB" system (by default "couchDB")

`--nameDB` or `-n <db_name>`: with the DB name

Then, you can launch LIFT's deduction algorithm:

`--setWinJoin` or `-wj <window_in_seconds>`: for setting the maximum joinable window interval gap between two subqueries or triple patterns (by default 3600 seconds)


## About and Contact

LIFT was developed at University of Nantes as an ongoing academic effort. You can contact the current maintainers by email at georges.nassopoulos[at]etu[dot]univ-nantes[dot]fr.

