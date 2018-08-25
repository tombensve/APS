#!/bin/sh
#export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-10.0.2.jdk/Contents/Home
rm -rf .vertx
mvn -Prun-backend test
