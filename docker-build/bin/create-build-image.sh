#!/usr/bin/env bash
#
# This script creates a docker image for building the whole project
# within docker container.
#

# Constantish
image_name="aps-build-image"

# Move to right path
cd $(dirname $0)/../..
pwd

# Assumption:
# Since this is built with maven, a maven installation must exist
# somewhere and have the apache-maven/bin/mvn command on the PATH.
#
# The path of the mvn command is then used to resolve the maven
# installation which is copied into the image. There are base
# images available already having a maven installation and seem
# to come from the maven project. There is currently however no
# such with a jdk greater than 8, and APS uses 11. Thereby we
# copy the local maven installation into the image.
#
mvnRoot=$(which mvn)
mvnRoot=$(dirname ${mvnRoot})
mvnRoot=$(dirname ${mvnRoot})

# Docker can only pull in file in the same directory as the
# Dockerfile and below, but not up. Thereby we need to copy
# the maven installation.
#
if [[ ! -d apache-maven ]]; then
    echo "Copying apache-maven ..."
    mkdir apache-maven
    cd apache-maven
    (cd ${mvnRoot};tar cf - *) | tar xf -
    cd ..
    echo "Done."
fi

# We also need node.js / npm to build the frontend. Haven't found any base image
# that contains both a JDK and Node.js ...
if [[ ! -d node-v12.13.1-linux-x64 ]]; then
    curl https://nodejs.org/dist/v12.13.1/node-v12.13.1-linux-x64.tar.xz | tar xvf -
fi

# Shutdown instance if running
if [[ $(docker ps | grep ${image_name}) ]]; then
    echo "Shutting down running image ..."
    cid=$(docker ps | grep ${image_name} | awk '{print $1}')
    docker stop ${cid}
    echo "Done."
fi

# Clean old image if it exist.
if [[ $(docker images | grep ${image_name}) ]]; then
    echo "Deleting old image ..."
    docker rmi --force ${image_name}
    echo "Done."
fi

# Start with a clean image.
mvn clean

# Create a new image.
echo "Building image ..."
docker build -t ${image_name} .
echo "Done building image."

# Clean the temporary dirs

echo "Cleaning temp apache-maven ..."
rm -rf ./apache-maven
echo "Done."

echo "Cleaning temp node.js"
rm -rf node-v12.13.1-linux-x64
echo "Done."

