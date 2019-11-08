#!/bin/sh
# We reuse maven surefire test but wil longer timeout (-Prun-backend). This gives reuse of setup classpath
# avoiding having 2 setups of the same thing that have to be in sync. The slightly negative side of this is
# that surefire seems to think something is wrong if a test been running for to long and kills it with an
# error.

# If you are using IDEA and also have BashSupport installed, then remember to setup a Run configuration
# that does not require an IDEA build before running. After that you can just right click on the script
# in IDEA and then run it. Also when run though BashSupport it will get the folder that the script is in
# as CWD.

# Clean vert.x cache.
rm -rf .vertx

rm -f target/classes/webContent/static/css/*
rm -f target/classes/webContent/static/js/*

# Workaround: Rebuild frontend before backend. The maven build should already do that, but it does
# not seem to work ...
# Damn it!!!! This does not work either!! Even though I rebuild the frontend before even running the
# maven build which copies and includes the frontend code under src/main/resources/webContent, it gets
# the previous build, not this one!!!!!!! 1 + 1 does not even come anywhere close to 2 here ...
# src/main/js/aps-webmanager-frontend/build.sh

# Do run maven test with 'run-backend' profile. The -T 1 is to limit build to one thread, but doesn't
# seem to help. The jar with web content still gets the previous web content before frontend rebuild.
mvn -T 1 -Prun-backend test
