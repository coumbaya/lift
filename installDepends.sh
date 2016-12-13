#!/bin/sh

sudo apt-get update
echo "installation justinfer"
sudo add-apt-repository ppa:oreste-notelli/ppa 
sudo apt-get install justniffer


echo "Choose DB (1 for MonetDB, 2 for CouchDB or 3 for both): "
read database
if [ "$database" = "1" ] || [ "$database" = "3" ]; then
        
	echo "installation monetdb sql client"

	codeName="$(lsb_release -c)"

	sudo touch /etc/apt/sources.list.d/monetdb.list
	if [[ $codeName == *"trusty"* ]]
	then
	       echo "deb http://dev.monetdb.org/downloads/deb/ trusty monetdb
	      	     deb-src http://dev.monetdb.org/downloads/deb/ trusty monetdb" > /etc/apt/sources.list.d/monetdb.list
	fi

	elif [[ $codeName == *"precise"* ]]
	then
	       echo "deb http://dev.monetdb.org/downloads/deb/ precise monetdb
	      	     deb-src http://dev.monetdb.org/downloads/deb/ precise monetdb" > /etc/apt/sources.list.d/monetdb.list
	fi

	elif [[ $codeName == *"vivid"* ]]
	then
	       echo "deb http://dev.monetdb.org/downloads/deb/ vivid monetdb
	      	     deb-src http://dev.monetdb.org/downloads/deb/ vivid monetdb" > /etc/apt/sources.list.d/monetdb.list
	fi

	elif [[ $codeName == *"jessie"* ]]
	then
	       echo "deb http://dev.monetdb.org/downloads/deb/ jessie monetdb
	      	     deb-src http://dev.monetdb.org/downloads/deb/ jessie monetdb" > /etc/apt/sources.list.d/monetdb.list
	fi

	elif [[ $codeName == *"utopic"* ]]
	then
	       echo "deb http://dev.monetdb.org/downloads/deb/ utopic monetdb
	      	     deb-src http://dev.monetdb.org/downloads/deb/ utopic monetdb" > /etc/apt/sources.list.d/monetdb.list
	fi

	wget --output-document=- https://www.monetdb.org/downloads/MonetDB-GPG-KEY | sudo apt-key add -
	sudo apt-get install monetdb5-sql monetdb-client

cat << EOF > $HOME/.monetdb
user=monetdb
password=monetdb
language=sql
EOF

	export DOTMONETDBFILE=$HOME/.monetdb
	echo 'export DOTMONETDBFILE=$HOME/.monetdb' >> $HOME/.bashrc

	monetdbd create $HOME/myMONETDB
	sudo chmod 777 -R  $HOME/myMONETDB
	monetdbd start $HOME/myMONETDB
	monetdb create demo
	monetdb release demo
        user="feta"
        pass="feta"

cat << EOF > test.sql
CREATE USER "$user" WITH PASSWORD '$pass' NAME 'FETA Explorer' SCHEMA "sys";
CREATE SCHEMA "$user" AUTHORIZATION "$user";
ALTER USER "$user" SET SCHEMA "$user";
CREATE TABLE test (id int, data varchar(30));
INSERT INTO test VALUES (2, 'geard');
SELECT * from test;
CREATE TABLE $user.test2 (id int, data varchar(30));
INSERT INTO $user.test2 VALUES (2, 'geard');
SELECT * from $user.test2;
EOF

	mclient -d demo < test.sql
fi

if [ "$database" = "2"] || [ "$database" = "3" ]; then
    sudo apt-get install couchdb -y
    curl localhost:5984
fi
