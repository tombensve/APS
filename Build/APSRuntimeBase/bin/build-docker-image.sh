#!/usr/bin/env bash

name=$(cat $(dirname $0)/../ImageName)

$(dirname $0)/../../bin/build-docker-image.sh "${name}" $(dirname $0)/..
