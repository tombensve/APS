#!/bin/sh
#
dir=`dirname $0`
cd ${dir}/../../..
cp=""
for jar in `find . -name *.jar | grep "target" | egrep -v "bundles" | egrep -v "WEB-INF"`;
do
	cp="${cp}:$jar"
done

java -cp $cp se.natusoft.osgi.aps.discovery.test.APSDiscoveryClient
