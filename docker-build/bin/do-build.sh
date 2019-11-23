#!/usr/bin/env bash

echo "Recreating new container ..."
$(dirname $0)/create-build-image.sh
echo "Done."

# Resolve absolute project root.
cd $(dirname $0)/../..
absoluteProjectRoot=$(PWD)

# Run build. This requires /source-code to be available within docker image! It needs to be copied there
# by the Dockerfile. There is a commented out line in Dockerfile which can be uncommented, and then this
# script can be run to build container local copy of source. This is faster, but requires recreating
# image on every source change.
docker run -v ~/apsbuildm2:/m2repo aps-build-image /source-code/docker-build/bin/container/run-build.sh
