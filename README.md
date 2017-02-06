# LIFT

LInked data Fragment Tracking (LIFT): Extracting Basic Graph Patterns from Triple Pattern Fragment Logs


## LIFT experiments


LIFT experiments are available [here](https://github.com/coumbaya/lift/blob/master/experiments.md), using as input:

(1) traces of queries in http://client.linkeddatafragments.org, each executed one by one, and,<br>
(2) traces of DBpedia LDF server's real log, from [USEWOD](http://usewod.org/data-sets.html) dataset, for the period of 14th October 2014-27th February 2015.


## Install LIFT's dependencies

LIFT is implemented in Java 1.7 and known to run on Debian GNU/Linux and OS X. In order to install packages and dependencies related to LIFT, you need to execute  **installDepends.py**. This script will install: 
   
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


## How to use LIFT

 1. __Capture traces of executed queries:__
   
      Queries are evaluated over the Web application client http://client.linkeddatafragments.org, using the **Google chrome** navigator. The traces of executed queries are captured in "har" format 
      using the Web inspector tool (More utils-> Developement Utils-> Network Traffic-> (Right clck) Save as har with Content).

 2. __Save traces into a database:__
      
     Traces are loaded into a DBMS of your choice, using `$ java -jar "lift.jar" -[option]` and arguments:
	
	`--load` or `-l <path_to_trace>`: for loading a trace into the DB

	`--systemDB` or `-s <dbms_to_use>`: for setting "couchDB" or "monetDB" system (by default "couchDB")

	`--nameDB` or `-n <db_name>`: with the DB name

     E.g., `$ java -jar "lift.jar" -l ~/brad_pitt_traces.har -n "query1database" `

 3. __Run LIFT:__
      
     LIFT is executed, using `$ java -jar "lift.jar" -[option]` and arguments:
	
	`--gapWindow` or `-g <window_in_seconds>`: for setting the maximum joinable window interval gap between two subqueries or triple patterns (by default 3600 seconds)

	`--nameDB` or `-n <db_name>`: with the DB name

     E.g., `$ java -jar "lift.jar" -n "query1database" -g 360 `

## FAQ

 1. __The DB storage system crashes, what to do?:__


       (i) If you are using couchdb, run:

       _linux_: `sudo service couchdb restart`

       _mac os_:   
		`sudo launchctl unload -w /System/Library/LaunchDaemons/couchdb.plist` <br>
                `sudo launchctl load -w /System/Library/LaunchDaemons/couchdb.plist`


       (ii) If you are using monetdb, run:

       _linux_: `sudo service monetdb5-sql restart`

       _mac os_:<br>
		`sudo launchctl unload -w /System/Library/LaunchDaemons/monetdb5-sql.plist` <br>
                `sudo launchctl load -w /System/Library/LaunchDaemons/monetdb5-sql.plist`


       If the database still not responds, try to restart your computer. If this also do not do the job, reinstall the script "dependencies.py" for the corresponding DBMS.

       For both storage systems, **do not install multiple versions**, it may create collisions. For couchdb, use the same version than the script "dependencies.py" i.e., Apache-CouchDB-1.6.1.

 2. __Why can not I load traces captured with other navigators, than Google Chrome?__

      The problem is with the response encoding. Answers must be captured in **ASCII** and not **Base64** format. There exist "decoding" plugins in other brownsers, but they can not be applied directly to the _webinspector_ tool.


## About and Contact

LIFT was developed at University of Nantes as an ongoing academic effort. You can contact the current maintainers by email at georges.nassopoulos[at]etu[dot]univ-nantes[dot]fr.

