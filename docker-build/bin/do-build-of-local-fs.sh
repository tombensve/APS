#!/usr/bin/env bash

# Resolve absolute project root.
cd $(dirname $0)/../..
absoluteProjectRoot=$(PWD)

# Run build
docker run -v ~/apsbuildm2:/m2repo -v ${absoluteProjectRoot}:/source-code aps-build-image /source-code/docker-build/bin/container/run-build.sh
