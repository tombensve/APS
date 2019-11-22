#!/usr/bin/env bash
# This should be run within the container!

export PATH=$PATH:/node-v12.13.1-linux-x64/bin
echo $PATH

echo "== NPM ========"
which npm
echo "==============="

cd /source-code
../apache-maven/bin/mvn clean install

