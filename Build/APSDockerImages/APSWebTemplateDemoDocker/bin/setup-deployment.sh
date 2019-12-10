#!/usr/bin/env bash

# This will download and install the web template demo app under bundles.
# This is run on maven build of this project.

dl=$(dirname $0)/../../bin/downloadJar.sh
bundles=$(dirname $0)/../imgsrc/aps-platform-deployment/bundles
#deps=$(dirname $0)/../imgsrc/aps-platform-deployment/dependencies
#bin=$(dirname $0)/../imgsrc/aps-platform-deployment/bin

if [[ ! -d ${bundles} ]]; then
    mkdir ${bundles}
fi
#if [[ ! -d ${deps} ]]; then
#    mkdir ${deps}
#fi

${dl} se.natusoft.osgi.aps  aps-web-template  1.0.0  ${bundles}
