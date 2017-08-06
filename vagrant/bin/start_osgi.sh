#!/bin/sh

unset KARAF_DEBUG
echo "Stopping Karaf ..."
/home/vagrant/server/apache-karaf/bin/stop now
echo "Waiting for Karaf to stop ..."
sleep 10

/home/vagrant/server/apache-karaf/bin/start -debug
tail -f /home/vagrant/server/apache-karaf/data/log/karaf.log

