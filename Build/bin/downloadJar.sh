#!/usr/bin/env bash

## Downloads a jar.
##
## Args:
##  $1 : group
##  $2 : artifact
##  $3 : version
##  $4 : target dir

if [[ "${4}" == "" ]]; then
    cat ${0} | egrep "^##" | sed -e 's/##//g'
    exit -1
else
    if [[ -f "${4}/${2}-${3}.jar" ]]
    then
        echo "${4}/${2}-${3}.jar already exists!"
    else
        # Try local ~/.m2 first!
        #ls -l ~/.m2/repository/$(echo $1 | sed -e 's/\./\//g')/${2}/${3}/${2}-${3}.jar

        if [ -f ~/.m2/repository/$(echo $1 | sed -e 's/\./\//g')/${2}/${3}/${2}-${3}.jar ]; then
            cp ~/.m2/repository/$(echo $1 | sed -e 's/\./\//g')/${2}/${3}/${2}-${3}.jar ${4}/${2}-${3}.jar
        else
            url="https://jcenter.bintray.com/$(echo $1 | sed -e 's/\./\//g')/${2}/${3}/${2}-${3}.jar"
            echo ${url}
            curl -L -o "${4}/${2}-${3}.jar" ${url}
        fi
    fi
fi
