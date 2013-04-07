#!/bin/sh
#
# Since the "versions-maven-plugin" does not fix the parent poms versions thus damaging the whole maven tree I have
# now tagged all relevant version tags with <!--APSVER--> before the version number. This is then used by this script
# to change the version number of all tagged versions in the pom.xml files in the project.
#

version="0.9.2"

for pom in `find . -name 'pom.xml' -print`
do
    echo "Updating ${pom} ..."
    cat ${pom} | sed 's%><!--APSVER-->.*<%><!--APSVER-->'${version}'<%g' > ${pom}.new
    mv ${pom} ${pom}.old
    mv ${pom}.new ${pom}
    rm ${pom}.old
done
