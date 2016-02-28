#!/usr/bin/env bash

# With thanks to: https://gist.github.com/tinkerware/cf0c47bb69bf42c2d740
updateSystem() {
    apt-get -y -q update
    apt-get -y -q upgrade
    apt-get -y -q install software-properties-common htop
}

# With thanks to: https://gist.github.com/tinkerware/cf0c47bb69bf42c2d740
installJava() {
    add-apt-repository ppa:webupd8team/java
    apt-get -y -q update
    echo oracle-java8-installer shared/accepted-oracle-license-v1-1 select true | sudo /usr/bin/debconf-set-selections
    apt-get -y -q install oracle-java8-installer
    update-java-alternatives -s java-8-oracle
}

installKaraf() {
    cd /home/vagrant
    mkdir server
    cd server

        wget http://apache.mirrors.spacedump.net/karaf/4.0.2/apache-karaf-4.0.2.tar.gz
        tar xvf apache-karaf-4.0.2.tar.gz
        ln -s apache-karaf-4.0.2 apache-karaf

        ./apache-karaf/bin/start
        sleep 8
        ./apache-karaf/bin/client feature:install webconsole
        ./apache-karaf/bin/client feature:install war
        sleep 10
        ./apache-karaf/bin/stop now

        # We don't want the server running when doing this, and we don't want to do this before
        # we have setup the features since many of the deployed bundles will not work before
        # that.
        cd apache-karaf
            rm -rf deploy
            ln -s /vagrant/vagrant/deploy deploy
        cd ..

    cd ..

    # This script runs as root, but I'm lazy and wan't to run my test karaf as vagrant, so I change ownership
    # of the installation.
    chown -R vagrant:vagrant server

    ln -s /vagrant/vagrant/filesystems/${node}/.apsHome

    # Make sure we have debug even if we restart and forget the "debug" param.
    sudo -u vagrant -i echo "export KARAF_DEBUG=true" >> /home/vagrant/.profile

    # Now we can start the server again, and we start it as user "vagrant".
    sudo -u vagrant -i /home/vagrant/server/apache-karaf/bin/start debug
}

setupBin() {
    cd /home/vagrant
    ln -s /vagrant/vagrant/bin
    chmod 755 bin/clean_karaf.sh
}

# Since I do a lot of downloads while testing this config I'm using Squid, a caching proxy. I don't want to get
# banned from a site for downloading the same thing over and over taking bandwidth :-)
# A tip to anyone else using SquidMan on Mac OS X: Make sure you don't run it on a port already used by something else!
# It will not complain and pretend that it is running, and it might be, but it is not listening on any port ...
export http_proxy="http://192.168.1.17:9999/"

updateSystem
installJava
installKaraf
setupBin
