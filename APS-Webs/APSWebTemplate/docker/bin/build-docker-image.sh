#!/usr/bin/env bash
droot=$(dirname $0)/..

imageName=$(cat ${droot}/image-name.txt)
projectRoot=$(dirname $0)/../..

check=$(docker images | grep ${imageName})
if [[ "${check}" == "" ]]; then
    docker build -t ${imageName} ${projectRoot}
else
    echo "Docker image '${imageName}' already exists! Removing old first!"
    docker image rm --force ${imageName}
    docker build -t ${imageName} ${projectRoot}
fi
