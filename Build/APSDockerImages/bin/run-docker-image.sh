#!/usr/bin/env bash
# Common version to be reused. Do the following in sub projects:
# ---------------------------------------------------------------
# imageName=$(cat $(dirname $0)/../ImageName)
#
# $(dirname $0)/../../bin/run-docker-image.sh "${imageName}" "container name" "8880:8880"

imageName=$1
containerName=$2
expose=$3

DNS=$($(dirname $0)/resolve-dns.sh)

docker run -p ${expose} --dns ${DNS} --name ${containerName} ${imageName} aps-platform/bin/run.sh
