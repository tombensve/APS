#!/bin/sh
# We reuse maven surefire test but wil longer timeout (-Prun-backend). This gives reuse of setup classpath
# avoiding having 2 setups of the same thing that have to be in sync. The slightly negative side of this is
# that surefire seems to think something is wrong if a test been running for to long and kills it with an
# error.

# Test if Java 10: Groovy problems! There is a 2.5 version that support Java 10, but it is not available
# in maven central nor jcenter for some strange reason. You can download and install locally, but that is
# not good enough. I'm sticking to Java8 for now.
#export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-10.0.2.jdk/Contents/Home

rm -rf .vertx
mvn -T 1 -Prun-backend test
