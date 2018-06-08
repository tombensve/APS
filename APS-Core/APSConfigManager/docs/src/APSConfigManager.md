## APSConfigManager

This bunlde listens for other bundles and checks for the following MANIFEST.MF entries:

- `APS-Config-Id` A unique id for a configuration to manage.
- `APS-Config-Schema` Points to a bundle relative JSON file containing an APS JSON schema, describing the configuration file. See aps-core-lib documentation for more information on the schema format.
- `APS-Config-Default-Resource` Points to a bundle relative JSON file folowing the schema and containging default configuration values.

For each bundle with an `APS-Config-Id` an instance of `APSConfig` is created loaded with current config from disk or cluster, or the default config file. If the config is new and has no previous configuration stored then the contents of the default configuration will be stored.

The created `APSConfig` is then published as an OSGi service and the following lookup property: `(apsConfigId=\<config id\>)`. 

There are 2 ways to get configuration:

Since the configuration is published as an OSGi "service", a service tracker can be used to look it up. The APSServiceTracker has some special features that makes it easier and it can also be provided by APSActivator:

    @OSGiService( additionalSearchCriteria = "(apsConfigId=my-config-id)", nonBlocking = true )
    private APSServiceTracker<APSConfig> configTracker

    @Managed
    private BundleContext context
    
    private APSConfig config

    @Initializer
    void init() {
        this.configTracker.onActiveServiceAvailable { APSConfig config, ServiceReference configRef ->
            this.config = this.context.getService(configRef);
            ...
        }
    
        this.configTracker.onActiveServiceLeaving { ServiceReference configRef, Class type ->
            this.config = null
            ...
        }
        ...
    }

Note that the first argument which actually is the APSConfig instance you want, cannot be saved. It only exists within the callback block. Thereby we must allocate a new usage by using the OSGi BundleContext to get it. This should of course be released again when your code no longer needs the configuration object.

The other and easier way to get configuration is to let APSActivator give it to you:

    private APSConfig config
    
    @ConfigListener(apsConfigId = "my-config-id")
    void config( APSConfig config ) {
        if (config != null) {
            this.config = config
            ...
        }
        else {
            this.config = null
        }
    }

This is much easier, but actually does the same as the first example. The difference is that APSActivator does it for you. Note that the method will be called with a null value if the configuration is unpublished as an OSGi service! This will however only happen if the config is unpublished before aps-config-manager and the listening bundle shuts down. So basically your code must expect and handle a null, but cannot demand to allways get a null on shutdown.

Note that in both these cases you wait for the configuration to become available. The following would also be possible and can seem simpler:

    @OSGiService
    private APSConfig config

But in this case you will be blocking the thread if it is accessed before it is available. If this was done in code called from bundle activator then there would be a problem! The other two ways are completely non blocking and reactive in that you get it when it is available. 

Whatever the code is doing, it can start doing it when the config is available. If it needs to do something that isn't quick, then it should work in another thread since the config listener call made by APSActivator needs
to return rather quickly. Here is a suggestion: `APSExecutor.submit { ... }`. This will then be submitted to a thread pool with as many threads as there are cores in the machine. APSActivator can also inject an `ExecutionService` that is backed by a thread pool for use by the bundle only. `APSExecutor` provides one thread pool for all bundles to share and are intended for shorter jobs.

### MapJSON

Configurations are JSON documents. `APSConfig`extends `Map\<String, Object\>` and can thus be used as a Map representation of the JSON data, but it also has `lookup(String key)` method that takes a structured key (implemented by StructMap from aps-core-lib) to lookup a value. There is also a `provide( String structPath, Object value )` method that is intended for configuration editors to use to update config values. An update of config value will update cluster and inform other nodes of the change. See documentation for StructMap in aps-core-lib for more information.

