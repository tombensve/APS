#!/usr/bin/env bash
# This is executed on maven build of project.

imageName=$(cat $(dirname $0)/../ImageName)

$(dirname $0)/../../bin/build-docker-image.sh "${imageName}" $(dirname $0)/..
