#!/usr/bin/env bash

imageName=$(cat $(dirname $0)/../ImageName)

$(dirname $0)/../../bin/clean-deployment.sh "${imageName}" $(dirname $0)/..

