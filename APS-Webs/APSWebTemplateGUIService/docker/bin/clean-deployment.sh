#!/usr/bin/env bash
droot=$(dirname $0)/..

imageName=$(cat ${droot}/image-name.txt)
projectRoot=$(dirname $0)/../..

docker rmi --force ${imageName}

if [[ $(ls -A ${projectRoot}/imgsrc/aps-platform-deployment/bundles/) != "" ]]; then
    rm ${projectRoot}/imgsrc/aps-platform-deployment/bundles/*.jar
fi
if [[ $(ls -A ${projectRoot}/imgsrc/aps-platform-deployment/dependencies/) != "" ]]; then
    rm ${projectRoot}/imgsrc/aps-platform-deployment/dependencies/*.jar
fi
