#!/usr/bin/env bash
# Common version to be reused. Do the following in sub projects:
# ---------------------------------------------------------------
# imageName=$(cat $(dirname $0)/../ImageName)
#
# $(dirname $0)/../../bin/clean-deployment.sh "${imageName}" $(dirname $0)/..

imageName=$1
projectRoot=$2

docker rmi --force ${imageName}

if [[ $(ls -A ${projectRoot}/imgsrc/aps-platform-deployment/bundles/) != "" ]]; then
    rm ${projectRoot}/imgsrc/aps-platform-deployment/bundles/*.jar
fi
if [[ $(ls -A ${projectRoot}/imgsrc/aps-platform-deployment/dependencies/) != "" ]]; then
    rm ${projectRoot}/imgsrc/aps-platform-deployment/dependencies/*.jar
fi
