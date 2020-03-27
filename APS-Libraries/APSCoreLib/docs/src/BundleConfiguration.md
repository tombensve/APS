## Configuration for Bundles

### APSConfigLoader

This is a trivially easy way of getting configuration. Just do:

    Map<String, Object> config = APSConfigLoader.get("config-id")

Where there are 2 resource files under `apsconfig`:

    apsconfig/
        (config-id)-schema.json
        default-(config-id)-config.json

To provide a configuration that differs from bundle default, package:
 
    apsconfig/
        (config_id)-config.json

in a jar file and include in APS-Runtime under _dependencies_. This will override the default config file delivered with bundle. It is possible to include multiple bundles config files in the same jar of course. 

Do note that if `(config-id)-schema.json` is provided then the configuration file used will be validated against it. If no schema file is provided by the bundle then no validation will be done and whatever is in the config file will be loaded without error. It must of course be a JSON file or it will fail!
