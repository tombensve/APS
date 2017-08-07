#!/usr/bin/env bash
#
# This scripts stops karaf, cleans the deployments, starts karaf again, and then sets up web and webconsole.
#

unset KARAF_DEBUG
echo "Stopping Karaf ..."
/home/vagrant/server/apache-karaf/bin/stop now
echo "Waiting for Karaf to stop ..."
sleep 10
echo "Removing deployment data ..."
rm -rf /home/vagrant/server/apache-karaf/data

export KARAF_DEBUG=true
echo "Starting Karaf ..."
/home/vagrant/server/apache-karaf/bin/start
echo "Waiting for Karaf to start ..."
sleep 8
echo "Installing web features ..."
/home/vagrant/server/apache-karaf/bin/client feature:install webconsole
/home/vagrant/server/apache-karaf/bin/client feature:install war

echo "Karaf redeployed fresh!"
cat /home/vagrant/server/apache-karaf/data/log/karaf.log
echo "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~"
tail -f /home/vagrant/server/apache-karaf/data/log/karaf.log
