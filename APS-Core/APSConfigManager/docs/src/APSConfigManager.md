## APSConfigManager

This bunlde listens for other bundles and checks for:

- `APS-Config-Id` A unique id for a configuration to manage.
- `APS-Config-Schema` Points to a bundle relative JSON file containing an APS JSON schema describing the configuration file. See aps-core-lib documentation for more information on the schema format.
- `APS-Config-Default-Resource` Points to a bundle relative JSON file folowing the schema and containging default configuration values.

For each bundle with an `APS-Config-Id` an instance of `APSConfig` is created loaded with current config from disk or cluster, and the default config file. If any value is not available in the loaded config the default config will be used instead for that value.
The created `APSConfig` is then published as an OSGi service and the following lookup property: `(apsConfigId=\<config id\>)`, whitch should be used to lookup the config, like this:

    @OSGiService( additionalSearchCriteria = "(apsConfigId=moon-whale-service-config)", nonBlocking = true )
    private APSServiceTracker<APSConfig> configTracker

    private APSConfig config

    @Initializer
    void init() {
        this.configTracker.onActiveServiceAvailable { APSConfig config, ServiceReference configRef ->
            this.config = config
            ...
        }
        ...
    }

This way you wait for the configuration to become available before you use it without blocking thread.

`APSConfig`extends `Map\<String, Object\>` and can this be used as a Map, but it also has _lookup(String key)_ method that takes a structured key (implemented by StructMap from aps-core-lib) to lookup a value. There is also a _provide( String structPath, Object value )_ method that is intended for configuration editors to use to update config values. An update of config value will update cluster and inform other nodes of change.

