import platform
import os
import subprocess
import shutil

operating_system = platform.system()

#database = int(raw_input("Choose DB (1 for MonetDB, 2 for CouchDB or 3 for both): "))
database = 3

resetDB="y"

def executeCmd(cmd):

	p = os.system(cmd)


def installMonetDB():

	if operating_system == "Linux":


		monetdbversion = os.popen("monetdb -v").read()

		if "MonetDB Database Server" in monetdbversion:
			print("Monetdb already installed, version: "+monetdbversion)
			resetDB = int(raw_input("Do you want to trancate existing version? (y/n): "))

		if resetDB == "y":

			print("installation monetdb sql client")

			codeName = os.popen("lsb_release -c").read()

			executeCmd("sudo touch /etc/apt/sources.list.d/monetdb.list")

			f = open('/etc/apt/sources.list.d/monetdb.list', 'w')

			if "trusty" in codeName:
				f.write( "%s \n %s" % ("deb http://dev.monetdb.org/downloads/deb/ trusty monetdb", 
						 "deb-src http://dev.monetdb.org/downloads/deb/ trusty monetdb"))

				f.close()
			if "precise" in codeName:
				f.write("%s \n %s" % ("deb http://dev.monetdb.org/downloads/deb/ precise monetdb", 
						 "deb-src http://dev.monetdb.org/downloads/deb/ precise monetdb"))

				f.close()
			if "vivid" in codeName:
				f.write("%s \n %s" % ("deb http://dev.monetdb.org/downloads/deb/ vivid monetdb", 
						 "deb-src http://dev.monetdb.org/downloads/deb/ vivid monetdb"))

				f.close()
			if "jessie" in codeName:
				f.write("%s \n %s" % ("deb http://dev.monetdb.org/downloads/deb/ jessie monetdb", 
						 "deb-src http://dev.monetdb.org/downloads/deb/ jessie monetdb"))

				f.close()
			if "utopic" in codeName:
				f.write("%s \n %s" % ("deb http://dev.monetdb.org/downloads/deb/ utopic monetdb", 
						 "deb-src http://dev.monetdb.org/downloads/deb/ utopic monetdb"))

				f.close()

			executeCmd("wget --output-document=- https://www.monetdb.org/downloads/MonetDB-GPG-KEY | sudo apt-key add -")
			executeCmd("sudo apt-get install monetdb5-sql monetdb-client")

		        executeCmd("cd $HOME")
			f = open('/.monetdb', 'w')
			f.write("%s \n %s \n %s" % ("user=monetdb", 
			 	"password=monetdb", "language=sql"))
			f.close()

			executeCmd("export DOTMONETDBFILE=~/.monetdb")
	
			f = open('/.bashrc', 'a+')
			f.write("%s" % ("export DOTMONETDBFILE=~/.monetdb"))
			f.close()

	if operating_system == "Darwin":

		monetdbversion = os.popen("monetdb -v").read()

		if "MonetDB Database Server" in monetdbversion:
			print("Monetdb already installed, version: "+monetdbversion)
			resetDB = int(raw_input("Do you want to trancate existing version? (y/n): "))

		if resetDB == "y":

			print("installation monetDB")

			executeCmd("xcode-select --install")
			executeCmd("ruby -e \"$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/master/install)\"")
			executeCmd("brew install monetdb")
			executeCmd("brew install wget")

			f = open(os.path.expanduser('~/.monetdb'), 'w+')
			f.write("%s\n%s\n%s" % ("user=monetdb", "password=monetdb", "language=sql"))
			f.close()

			f = open(os.path.expanduser('~/.bashrc'), 'a+')
			f.write("export DOTMONETDBFILE=~/.monetdb")
			executeCmd("source ~/.bashrc")
			f.close()
	
	if resetDB == "y":

		executeCmd("monetdbd create $HOME/myMONETDB")
		executeCmd("sudo chmod 777 -R  $HOME/myMONETDB")
		executeCmd("monetdbd start $HOME/myMONETDB")
		executeCmd("monetdb create demo")
		executeCmd("monetdb release demo")

		f = open('monetdb.sql', 'w')

		f.write( "%s\n%s\n%s\n%s\n%s\n%s\n%s\n%s\n%s\n" %("CREATE USER \"feta\" WITH PASSWORD 'feta' NAME 'FETA Explorer' SCHEMA \"sys\";", 
			"CREATE SCHEMA \"feta\" AUTHORIZATION \"feta\";",
			"ALTER USER \"feta\" SET SCHEMA \"feta\";",
			"CREATE TABLE test (id int, data varchar(30));", 
			"INSERT INTO test VALUES (2, 'geard');", 
			"SELECT * from test;", 
			"CREATE TABLE feta.test2 (id int, data varchar(30));",
			"INSERT INTO feta.test2 VALUES (2, 'geard');",
			"SELECT * from feta.test2;"))
	
		f.close()

		print("end write test.sql")
		executeCmd("mclient -d demo -u monetdb  < monetdb.sql")
		os.remove('monetdb.sql')


def installCouchDB():

	resetDB="y"

	couchdbversion = os.popen("couchdb -V").read()

	if "couchdb - Apache CouchDB " in couchdbversion:
		print("Couchdb already installed, version: "+couchdbversion)
		resetDB = int(raw_input("Do you want to trancate existing version? (y/n): "))

	if resetDB == "y":

		print("Install CouchDB")

		if operating_system == "Linux":

			executeCmd("sudo apt-get install couchdb -y")
			executeCmd("curl localhost:5984")

		if operating_system == "Darwin":

			executeCmd("wget https://dl.bintray.com/apache/couchdb/mac/1.6.1/Apache-CouchDB-1.6.1.zip")
			executeCmd("unzip Apache-CouchDB-1.6.1.zip > /dev/zero")
			executeCmd("chmod 777 Apache\ CouchDB.app")
			executeCmd("sudo mv  Apache\ CouchDB.app ~/Applications")
		shutil.rmtree('Apache-CouchDB-1.6.1')
	
if database == 1 or database == 3:

	installMonetDB()	

if database == 1 or database == 3:

	installCouchDB()
