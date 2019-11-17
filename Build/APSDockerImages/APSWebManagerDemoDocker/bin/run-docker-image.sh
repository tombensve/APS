#!/usr/bin/env bash

DNS=192.168.72.1 # Needs to be modified!
IMAGE=aps-runtime-java11

docker run -p 8880:8880 --dns ${DNS} ${IMAGE} aps-platform/bin/run.sh
