#!/usr/bin/env bash
# This is a convenience to run the container. This is never called during build.

imageName=$(cat $(dirname $0)/../ImageName)

$(dirname $0)/../../bin/run-docker-image.sh "${imageName}" "aps-web-template-demo" "8880:8880"
