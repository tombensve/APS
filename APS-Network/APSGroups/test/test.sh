#!/bin/sh

root=`dirname ${0}`/../../..

cp=${root}/APS-Network/APSGroups/target/classes

java -cp ${cp} se.natusoft.apsgroups.CmdLineTestShell
