# APSConfigService

This is not the simple standard OSGi service configurations, but more an application config that can also be used for services. It supports structured configurations including lists of items and lists of subconfigurations. Code that uses the configuration provide one or more configuration classes with config items. These are registered with the config service, which makes them editable/publishable though and admin web app. After registration an instance of the config can be gotten containing published or defaul values. Alternatively the config class is specified with a fully qualified name in the _APS-Configs:_ MANIFEST.MF entry. In this case the configuration service acts as an extender and automatically registers and provides an instance of the config for you, without having to call the config service.

## Configuration Environments

The APSConfigService supports different configuration environments. The idea is to define one config environment per installation. Configuration values can either be configuration environment specific or the same for all environments. See @ConfigItemDescription below for more information on specifying configuration environment specific values.

## Making a config class

Here is an example:

        @APSConfigDescription(
            version="1.0",
            configId="se.natusoft.aps.exmple.myconfig",
            group="examples",  
            description="An example configuration model"
        )
        public class MyConfig extends APSConfig {
            
        @APSConfigItemDescription(
            description="Example of simple value.",
        )
        public APSConfigValue simpleValue;
            
            @APSConfigItemDescription(
                description="Example of list value."
            )
            public APSConfigValueList listValue;
            
            @APSConfigItemDescription(
                description="One instance of MySubConfig model."
            )
            public MySubConfig mySubConfig;
            
            @APSConfigItemDescription(
                description="Multiple instances of MySubConfig model."
            )
            public APSConfigList<MySubConfig> listOfMySubConfigs;
            
            @APSConfigDescription(
                version="1.0",
                configId="se.natusoft.aps.example.myconfig.mysubconfig",
                description="Example of a subconfig model. Does not have to be inner class!"
            )
            public static class MySubConfig extends APSConfig {
                
             @APSConfigItemDescription(
                    description="Description of values."
                )
                public APSConfigValueList listOfValues;
                
                @APSConfigItemDescription(
                 description="Description of another value."
                )
                public APSConfigValue anotherValue;
            }
        }

### The config values 

Now you might be wondering, why not an interface, and why _public_ and why _APSConfigValue_, _APSConfigValueList_, and _APSConfigList_?

The reason for not using an interface and provide a java.lang.reflect.Proxy implementation of it is that OSGi has separate class loaders for each bundle. This means a service cannot proxy an interface provided by another bundle. Well, there are ways to go around that, but I did not want to do that unless that was the only option available. In this case it wasn’t. Therefore I use the above listed APS*Value classes as value containers. They are public so that they can be accessed and set by the APSConfigService. When you get the main config class instance back from the service all values will have valid instances. Each APS*Value has an internal reference to its config value in the internal config store. So if the value is updated this will be immediately reflected since it is referencing the one and only instance of it in the config store.

All config values are strings! All config values are stored as strings. The __APSConfigValue__ container however have _toBoolean()_, _toDate()_, _toDouble()_, _toFloat()_, _toInt()_, _toLong()_, _toByte()_, _toShort()_, and _toString()_ methods on it.

The __APSConfigList<Type>__ container is an _java.lang.Iterable_ of <Type> type objects. The <Type> cannot however be anything. When used directly in a config model it must be <Type extends APSConfig>. That is, you can only specify other config models extending APSConfig. The only exception to that is __APSConfigValueList__ which is defined as:

        public interface APSConfigValueList extends APSConfigList<APSConfigValue> {} 

* Use __APSConfigValue__ for plain values.

* Use __APSConfigValueList__ for a list of plain values.

* Use __* extends APSConfig__ for a subconfig model.

* Use __APSConfigList<* extends APSConfig>__ for a list of subconfig models.

### The config annotations

The following 3 annotations are available for use on configuration models.

#### @APSConfigDescription

        @APSConfigDescription(
            version="1.0",
            configId="se.natusoft.aps.exmple.myconfig",
            group="docs.examples",
            description="An example configuration model"
        )

This is an annotation for a configuration model.

__version__ - The version of the config model. This is required.

__configId__ - The unique id of the configuration model. Use same approch as for packages. This is required.

__group__ - This specifies a group or rather a tree branch that the config belongs under. This is only used by the configuration admin web app to render a tree of configuration models. This is optional.

__description__ - This describes the configuration model.

#### @APSConfigItemDescription

        @APSConfigItemDescription(
            description="Example of simple value.",
            datePattern="yyMMdd",  
            environmentSpecific=true/false,  
            isBoolean=true/false,  
            validValues={"high", "medium", "low"}, 
        )

This is an annotation for a configuration item whithin a configuration model.

__description__ - This describes the configuration value. The configuration admin web app uses this to explain the configuration value to the person editing the configuration. This is required.

__datePattern__ - This is a date pattern that will be passed to SimpleDateFormat to convert the date in the string value to a java.util.Date object and is used by the _toDate()_ method of APSConfigValue. This date format will also be displayed in the configuration admin web app to hint at the date format to the person editing the configuration. The configuration admin web app will also use a calendar field if this is available. The calendar field has a complete calendar popup that lets you choose a date. This is optional.

__environmentSpecific__ - This indicates that the config value can have different values depending on which config environment is active. This defaults to false in which case the value will apply to all config environments. This is optional.

__isBoolean__ - This indicates that the config value is of boolean type. This is used by the configuration admin web app to turn this into a checkbox rather than a text field. This defaults to false and is this optional.

__validValues__ - This is an array of strings ( {"...", ..., "..."} ) containing the only valid values for this config value. This is used by the configuration admin web app to provide a dropdown menu of the alternatives rather than a text field. This defaults to {} and is thus optional.

__defaultValue__ - This is an array of @APSDefaultValue annotations. Se the description of this annotation below. This allows not only for providing a default value, but for providing a default value per config environment (which is why there is an array of @APSDefaultValue annotations!). Thus you can deliver pre configured configuration for all configuration environments. If a config environment is not specified for a default value then it applies for all configuration environments. Some configuration values are better off without default values, like hosts and ports for other remote services. The application/server maintenance people responsible for an installation in general knows this information better than the developers.

#### @APSDefaultValue

        @APSDefaultValue {
            configEnv="production",
            value="15"
        }

__configEnv__ - This specifies the configuration environment this default value applies to. "default" means all/any configuration environment and is the default value if not specified.

__value__ - This is the default value of the configuration value for the configuration environment specified by configEnv.

### Auto managed configurations

It is possible to let the APSConfigService act as an extender and automatically register and setup config instances on bundle deploy by adding the __APS-Configs:__ MANIFEST.MF header and a comma separated list of fully qualified names of config models. There are two variants of how to define the auto managed instance.

__Warning__: Auto managed configurations cannot ever be accessed during bundle activation in default activation thread! If the activation code starts a new thread then it is OK to access auto managed configuration in that thread, but only with variant 2! (the thread have to put itself to sleep until the configuration becomes managed. This is described below).

#### Variant 1: A simple non instantiated static member of config model type

Example:

        @APSConfigDescription(
            version="1.0",
            configId="se.natusoft.aps.exmple.myconfig",
            group="examples",
            description="An example configuration model"
        )
        public class MyConfig extends APSConfig {
            
        -->  public static MyConfig myConfig;  <--
            
            @APSConfigItemDescription(
                description="Example of simple value.",
            )
            public APSConfigValue simpleValue;
            
            @APSConfigItemDescription(
                description="Example of list value."
            )
            public APSConfigValueList listValue;
            ...
            

To access this variant of managed config do:

        MyConfig.myConfig.simpleValue.toString()/toInt()/toDouble()/...
        

__A warning__: This variant does not provide any support for determining if the configuration has become managaged yet. If you access it to early it will be null. Therefore you should only use this variant if you know it will become managed before it is referenced. The other variant allows you to check and wait for a config to become managed.

#### Variant 2: A static instantiated ManagedConfig&lt;ConfigModel&gt; member. 

Example:

        @APSConfigDescription(
            version="1.0",
            configId="se.natusoft.aps.exmple.myconfig",
            group="examples",
            description="An example configuration model"
            )
        public class MyConfig extends APSConfig {
            
        public static final ManagedConfig<MyConfig> managed = new ManagedConfig<MyConfig>();
            
            @APSConfigItemDescription(
                description="Example of simple value.",
            )
            public APSConfigValue simpleValue;
            
            @APSConfigItemDescription(
                description="Example of list value."
            )
            public APSConfigValueList listValue;
            ...
            

There is a possibility that code started in a bundle, especially threads might start running before the config has become managed. In such cases the following will solve that:

        if (!MyConfig.managed.isManaged()) {
            MyConfig.managed.waitUntilManaged();
        }

Do not ever do this during start() of a Bundle activator! That would cause a never ending dead-lock!

To access this variant of managed config do:

        MyConfig.managed.get().simpleValue.toString()/toInt()/toDouble()/...
        

## API Usages

### The configuration service usage

The APSConfigService API looks like this:

        public interface APSConfigService {
            void registerConfiguration(Class<? extends APSConfig> configClass, boolean forService) throws APSConfigException;
            void unregisterConfiguration(Class<? extends APSConfig> configClass);
            <Config extends APSConfig> Config getConfiguration(Class<Config> configClass) throws APSConfigException;
        }

On bundle start you register the configuration. On bundle stop you unregister it. Inbetween you access it. It is a good idea to call getConfiguration(...) after register on bundle start and the pass this instance to your services, etc.

If the _forServices_ flag is _true_ then this configuration will also be registered in the standard OSGi configuration service. Please be warned however that APSConfigService stores its configuration values in properties files, but with rather complex keys. For non structured, flat configurations it might make some sense to register it with the standard osgi service also, but in most cases there is no point in doing this. I'm not even sure why I have this option!

_Please note_ that if you are using managed configs (see above) then you never need to call this service API, not even lookup/track the APSConfigService!

### The configuration admin service usage

The APSconfigAdminService only needs to be used if you implement a configuration editor. APSConfigAdminWeb uses this API for example. See the javadoc for the API.

## The complete APS API

The complete APS javadoc can be found at [http://apidoc.natusoft.se/APS/](http://apidoc.natusoft.se/APS/).

## A word of advice

It is quite possible to make config structures of great complexity. __DON'T!__ Even if it seems manageable from a code perspective it might not be that from a admin perspective. Keep it simple always apply!

## Administration

The configurations managed by the APS config service can be synchronized among a group of installations. To do this you need to enable synchronization in the _aps/config_ node in the config admin web, and also specify a group name that you want to synchronize with. All installations having the same group name will synch configuration with each other. The synchronization uses the APSSync service so an implementation of this must be deployed for synchronization to work. There are currently 2 implementations provided, one using APSGroups (multicast only) and one using RabbitMQ (which of course also requires a RabbitMQ installation).

## APSConfigAdminWeb screenshots

![Config environment screenshot](http://download.natusoft.se/Images/APS/APS-Core/APSConfigServiceProvider/docs/images/config-env.png)

![Config environment help screenshot](http://download.natusoft.se/Images/APS/APS-Core/APSConfigServiceProvider/docs/images/config-env-help.png)

![Config screenshot](http://download.natusoft.se/Images/APS/APS-Network/APSGroups/docs/images/groups-config-1.png)

![Config list item screenshot](http://download.natusoft.se/Images/APS/APS-Core/APSConfigServiceProvider/docs/images/config-list.png)

