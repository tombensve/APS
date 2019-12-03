#!/usr/bin/env bash
name=aps-runtime-java11

if [[ $1 == "--replace" ]]; then
    docker rmi --force ${name}
fi

check=$(docker images | grep ${name})
if [[ "${check}" == "" ]]; then
    docker build -t ${name} $(dirname $0)/..
else
    echo "Docker image '${name}' already exists! Removing old first!"
    docker image rm --force ${name}
    docker build -t ${name} $(dirname $0)/..
fi
