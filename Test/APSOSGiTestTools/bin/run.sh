#!/usr/bin/env bash

if [[ $1 == "--platformDir" ]]; then
    root=$(dirname $0)
    platformDir=$2
    jars=""
    colon=""
    for jar in ${platformDir}/dependencies/*.jar
    do
        jars=${jars}${colon}${jar}
        colon=":"
    done
    for jar in ${platformDir}/bundles/*.jar
    do
        jars=${jars}${colon}${jar}
    done
    java --class-path ${root}/../target/aps-osgi-test-tools-1.0.0.jar:${jars} se.natusoft.osgi.aps.platform.APSPlatformRunner --bundleDir ${platformDir}/bundles
else
    echo "Bad options! Must specify --platformDir <platform dir>"
fi
