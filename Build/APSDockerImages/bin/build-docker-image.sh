#!/usr/bin/env bash
# Common version to be reused. Do the following in sub projects:
# ---------------------------------------------------------------
# imageName=$(cat $(dirname $0)/../ImageName)
#
# $(dirname $0)/../../bin/build-docker-image.sh "${imageName}" $(dirname $0)/..

imageName=$1
projectRoot=$2

if [[ $1 == "--replace" ]]; then
    docker rmi --force ${imageName}
fi

check=$(docker images | grep ${imageName})
if [[ "${check}" == "" ]]; then
    docker build -t ${imageName} ${projectRoot}
else
    echo "Docker image '${imageName}' already exists! Removing old first!"
    docker image rm --force ${imageName}
    docker build -t ${imageName} ${projectRoot}
fi
