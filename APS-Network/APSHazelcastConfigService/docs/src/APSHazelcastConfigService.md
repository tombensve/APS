# APS Hazelcast Configuration Service
This service deliver a populated Hazelcast Config object. It provides APS configurations mimicing many of the Hazelcast configuration objects. The APS configurations can be edited with the APS configuration admin web (http://host:port/apsadminweb, select "Configuration" tab, then Network/Hazelcast). 

Multiple named configuration instances can be defined and in the service and instance is looked up using the name. 

All APS Hazelcast configurations are configuration environment specific!

The APS configurations does not cover all Hazelcast configs at the moment. Only what I have decided is probably the most common used. The Hazelcast configuration possibilities are rather large. There is however one config field called _configFile_. In this field an XML configuration file available on the server (possibly via network filesystem) or an http or ftp URL to a configuration file can be specified. This is loaded first, then any other configurations specified in the config gui will complement/override what is in the config file. The configuration file can be left blank also if what is available in the gui is enough. 

The service will check if a configuration gui field is empty before copying it to its counterpart Hazelcast config. This will leave you with a Hazelcast default if a config gui field is left blank.

Anyone wanting more of Hazelcasts config in this gui are welcome to help in providing it ([https://github.com/tombensve/aps](https://github.com/tombensve/aps)) :-). 

Here is a list of the configurations provided:

* Network

   * Interfaces

   * Multicast

   * TCPIP

* Group

* Lists

* Sets

* Maps

* Queues

* Topics

* Listeners (1)

* Stores (2)


(1) In Hazelcast there are several different types of listeners. They all look the same configuration wise but use different configuration classes. The APS gui config only have one "listeners" config list for all listeners and each entry has a name. The name is used to reference a listener in each configuration gui that defines listeners. This was a way of slimming the GUI a bit.

(2) The handlig of stores are the same as the handlig of listeners. All types of stores are defined in stores with a name that is referenced in each config defining a store.

## Service Use

Well, the service API looks like this:

    public interface HazelcastConfigService {    
        Config getConfigInstance(String name);
    }

which should be self explanatory :-)

Do note that Hazelcast offers stores and listeners as both configuratble fully qualified classes to direct implementations or factories, but also allows for setting instances directly. The GUI only supports the first kind for obvious reasons, but don't think you have to use those! You can as easily add your listeners in code after getting the Config instance.
