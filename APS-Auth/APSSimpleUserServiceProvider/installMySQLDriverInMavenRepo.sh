#!/bin/sh
#
# Installs the MySQL driver in the local maven repository so that it can be 
# depended on with scope compile and thus be included in the bundle.
#
mvn install:install-file -Dfile=lib/mysql-connector-java-5.1.26-bin.jar -DgroupId=com.mysql -DartifactId=mysqldriver -Dversion=5.1.26 -Dpackaging=jar
