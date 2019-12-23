#!/usr/bin/env bash -x
droot=$(dirname $0)/..

imageName=$(cat ${droot}/image-name.txt)
projectRoot=$(dirname $0)/../..

containerName="aps-web-template-demo"
expose="8880:8880"

DNS=$($(dirname $0)/resolve-dns.sh)

if [[ $(docker container ls --all | grep "${containerName}") ]]; then
	docker container restart ${containerName}
else
    docker run -p ${expose} --dns ${DNS} --name ${containerName} ${imageName} aps-platform/bin/run.sh
fi
