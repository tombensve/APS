#!/usr/bin/env bash

updateSystem() {
    echo "==========================================================="
    echo "  Updating System!"
    echo "==========================================================="

    apt-get -y -q update
    #apt-get -y -q upgrade # In current version (16.04) this requires terminal interaction with at the moment non available user!
}

installJava() {
    echo "==========================================================="
    echo "  Installing Java 8!"
    echo "==========================================================="

    add-apt-repository ppa:webupd8team/java
    apt-get -y -q install default-jdk
    echo "JAVA_HOME=\"/usr/lib/jvm/java-8-openjdk-amd64\"" >>/etc/environment
}

installKaraf() {
    echo "==========================================================="
    echo "  Installing Karaf!"
    echo "==========================================================="

    cd /home/vagrant
    mkdir server
    cd server

        # http://apache.mirrors.spacedump.net/karaf/4.1.1/apache-karaf-4.1.1.tar.gz

        KARAF_VERSION=4.1.1
        wget http://apache.mirrors.spacedump.net/karaf/${KARAF_VERSION}/apache-karaf-${KARAF_VERSION}.tar.gz
        tar xvf apache-karaf-${KARAF_VERSION}.tar.gz
        ln -s apache-karaf-${KARAF_VERSION} apache-karaf

        ./apache-karaf/bin/start
        sleep 8
        ./apache-karaf/bin/client feature:install webconsole
        ./apache-karaf/bin/client feature:install war
        sleep 10
        ./apache-karaf/bin/stop now  # ---+
        sleep 10 #                        |
        #                                 V
        # We don't want the server running when doing the following, and we don't want to do this before
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

    # ${node} is set in bootstrap-n<n>.sh before calling vagrant-bootstrap.sh.
    ln -s /vagrant/vagrant/filesystems/${node}/.apsHome

    # Setup JAVA_HOME
    sudo -u vagrant -i echo "export JAVA_HOME=\"/usr/lib/jvm/java-8-openjdk-amd64\"" >> /home/vagrant/.profile

    # Make sure we have debug even if we restart and forget the "debug" param.
    sudo -u vagrant -i echo "export KARAF_DEBUG=true" >> /home/vagrant/.profile

    # Now we can start the server again, and we start it as user "vagrant".
    sudo -u vagrant -i /home/vagrant/server/apache-karaf/bin/start
}

setupBin() {
    echo "==========================================================="
    echo "  Setting up /home/vagrant!"
    echo "==========================================================="

    cd /home/vagrant
    ln -s /vagrant/vagrant/bin
    chmod 755 bin/clean_karaf.sh
}

# Since I do a lot of downloads while testing this config I'm using Squid, a caching proxy. I don't want to get
# banned from a site for downloading the same thing over and over taking bandwidth :-)
# A tip to anyone else using SquidMan on Mac OS X: Make sure you don't run it on a port already used by something else!
# It will not complain and pretend that it is running, and it might be, but it is not listening on any port ...
export http_proxy="http://192.168.1.17:9999/" # Note that this is explicitly for my machine!

updateSystem
installJava
installKaraf
setupBin
