# Application Platform Services (APS)

OSGi Application Platform Services - A "smorgasbord" of OSGi services that focuses on ease of use and good enough functionality for many but wont fit all. It can be seen as osgi-ee-light-and-easy. The services are of platform type: configuration, database, JPA, etc, with companion web applications for administration.

All services that require some form of administration have an admin web application for that, that plugs into the general apsadminweb admin web application.

All administrations web applications are WABs and thus require that the OSGi server supports WABs.

Another point of APS is to be as OSGi server independent as possible, but as said above the admin web applications do need support for WABs.

APS is made using basic OSGi functionality and is not using blueprint and other fancy stuff! Each bundle has an activator that does setup, creates trackers, loggers, and manually dependency injects them into the service providers it publishes.

## Features

### Current

* A configuration service that works with annotated configuration models where each config value can be described/documented. The configuration model can be structured with sub models that there can be one or many of. Each top level configuration model registered with the configuration service will be available for publishing in the admin web. The configuration service also supports different configuration environments and allows for configuration values to be different for different configuration environments, but doesn´t require them to be.

* A filesystem service that provides a persistent filesystem outside of the OSGi server. The configuration service makes use of this to store configurations. Each client can get its own filesystem area, and can´t access anything outside of its area.

* A platform service that simply identifies the local installation and provides a description of it. It is basically a read only service that provides configured information about the installation.

* A JPA service that is easier and more clearly defined than the osgi-ee JPA API, and allows for multiple JPA contexts. It works as an extender picking up persistence.xml whose defined persistence unit name can then be looked up using the service. A client can only lookup its own persistence units. It is based on OpenJPA.

* A data source service. Only provides connection information, no pooling (OpenJPA provides its own pooling)!

* External protocol extender that allows more or less any OSGi service to be called remotely using any deployed protocol service and transport. Currently provides JSONRPC 1.0 & 2.0, JSONHTTP, and JSONREST protocols, and an http transport. Protocols have a defined service API whose implementations can just be dropped in to make them available. Transport providers can make use of any deployed protocol. The APSExternalProtocolService now provides support for REST services where there is a method for post, put, get,and delete, and the http transport makes use of this in conjunction with any protocol that indicates it can support REST like JSONREST.

* A group service that can send data to each member over transport safe multicast.

* A service discovery service using the group service.

* A session service (not http!). This is used by apsadminweb to keep a session among several different administration web applications.

* An administration web service to which administration web applications can register themselves with an url and thus be available in the .../apsadminweb admin gui.

* A user service. Provides basic user management including roles/groups. Is accompanied with a admin GUI (plugnis into apsadminweb) for administration of users. (org.osgi.service.useradmin.UserAdmin felt uncomplete. It did not provide what I wanted).

* A user authentication service. This does nothing more that authenticating a user and have a really simple API. APS provides an implementation that makes use of the user service, but it is easy to make another implementation that authenticates against an LDAP for example or something else. The Admin web applications uses the authentication service for authenticating admin users.

* A far better service tracker that does a better job at handling services coming and going. Supports service availability wait and timeout and can be wrapped as a proxy to the service. Instead of returning null it throws an exception if no service becomes available within the timeout, and is thus much easier to handle.

### Planned

* An implementation of the standard OSGi LogService since not all servers provide one.

* A log veiwer web application supporting reqular expression filters on log information and a live log view. This is waiting on Vaadin 7.1 which will support server push. Another alternative is to go pure GWT and use Errai for this, but I rather continue with Vaadin having all admin webs looking and feeling the same.

* Synchronizing configurations between installations so that all configuration for all configuration environments can be edited in one place and automatically be distributed to each installation. This would also make it possible to configure installations without deployed admin webs.

* Anything else relevant I come up with and consider fun to do :-).

### Ideas

* A JCR (Java Content Repository) service and a content publishing GUI (following the general APS ambition - reasonable functionality and flexibility, ease of use. Will fit many, but not everyone).

* Support for being able to redeploy a web application and services live without loosing session nor user transactions. With OSGi it should be teoretically possible. For a limited number of redeployments at least. It is very easy to run into the "perm gen space" problem, but according to Frank Kieviet ([Classloader leaks: The dreaded permgen space](http://frankkieviet.blogspot.se/2006/10/classloader-leaks-dreaded-permgen-space.html)) it is caused by bad code and can be avoided.

### What is new in version 0.9.2

* Small bug fixes.

* APSActivator has been added to aps-tools-lib and can be used as bundle activator. It uses annotations to register services and inject tracked services and other things.

* A service can now be registered with an _aps-externalizable_ property with value _true_ to be made externally available by aps-external-protocol-extender.

### What is new in version 0.9.1

* Now have full REST support in aps-external-protocol-extender and aps-ext-protocol-http-transport-provider.

* Documentation have been cleaned up a bit.

## Requirements

The administration web application(s) are currently WABs and thus require a server supporting WAB deployments. I have developed/tested this on Glassfish and Virgo. I am however considering seeing if it is possible to also support both Glassfish and JBoss JEE WAR to OSGi bridges. They are unfortunately very server specific since there are no such standard. Other than that all services are basic OSGi services and should theoretically run in any R4 compatible OSGi server.

## Pre Setup

The Filesystem service is part of the core and used by other services. It should preferably have its filesystem root outside of the server installation. The BundleContext.getDataFile(String) returns a path within the deploy cache and is only valid for as long a a bundle is deployed. The point with the FilesystemService is to have a more permanent filesystem outside of the application server installation. To provide the FilesystemService root the following system property have to be set and available in the JVM instance:

            aps.filesystem.root=<root>

How to do this differs between servers. In Glassfish you can supply system properties with its admin gui.

If this system property is not set the default root will be BundleContext.getFile(). This can work for development setup, but not for more serious installations!

After this path has been setup and the server started, all other configuration can be done in http://.../apsadminweb/.

__Please note__ that the /apsadminweb by default require no login! This so that _"Configurations tab_,_Configurations/persistence/datasources"_ can be used to setup a datasource called "APSSimpleUserServiceDS" needed by APSSimpleUserService. If you use the provided APSAuthService implementation that uses APSSimpleUserService then you need to configure this datasource before APSSimpleUserService can be used. See the documentation for APSSimpleUserService further down in this document for more information on the datasource configuration. After that is setup go to _"Configurations tab_,_Configurations/aps/adminweb"_ and enable the "requireauthentication" config. After having enabled this and saved, do a browser refresh and then provide userid and password when prompted.

## Javadoc

The complete javadoc for all services can be found at [http://apidoc.natusoft.se/APS](http://apidoc.natusoft.se/APS).

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
            
        -->  public static final ManagedConfig<MyConfig> managed = new ManagedConfig<MyConfig>();  <--
            
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

The configurations managed by the APS config service can be synchronized among a group of installations. To do this you need to enable synchronization in the _aps/config_ node in the config admin web, and also specify a group name that you want to synchronize with. All installations having the same group name will synch configuration with each other. The synchronization uses the APSGroups service so this must be deployed for synchronization to work.

## APSConfigAdminWeb screenshots

![Config environment screenshot](http://download.natusoft.se/Images/APS/APS-Core/APSConfigServiceProvider/docs/images/config-env.png)

![Config environment help screenshot](http://download.natusoft.se/Images/APS/APS-Core/APSConfigServiceProvider/docs/images/config-env-help.png)

![Config screenshot](http://download.natusoft.se/Images/APS/APS-Network/APSGroups/docs/images/groups-config-1.png)

![Config list item screenshot](http://download.natusoft.se/Images/APS/APS-Core/APSConfigServiceProvider/docs/images/config-list.png)

# APSFilesystemService

This provides a filesystem for writing and reading files. This filesystem resides outside of the OSGi server and is for longterm storage, which differs from BundleContext.getDataFile() which resides within bundle deployment. The APSFilesystemService also does not return a File object! It priovides a file area for each unique owner name that is accessed through an API that cannot navigate nor access any files outside of this area. The ”owner” name should be either an application name or a bundle name if it is only used by one bundle.

The APSConfigService uses the APSFilesystemService to store its configurations.

## Setup

The _aps.filesystem.root_ system property must be set to point to a root where this service provides its file areas. This is either passed to the JVM at server startup or configured withing the server. Glassfish allows you to configure properties within its admin gui. Virgo does not. If this is not provided the service will use BundleContext.getDataFile(".") as the root, which will work for testing and playing around, but should not be used for more serious purposes since this is not a path with a long term availability.

## The service

The service allows you to create or get an APSFilesystem object. From that object you can create/read/delete directories (represented by APSDirectory) and files (represented by APSFile). You can get readers, writers, input streams and output streams from files. All paths are relative to the file area represented by the APSFilesystem object.

The javadoc for the [APSFilesystemService](http://apidoc.natusoft.se/APS/se/natusoft/osgi/aps/api/core/filesystem/service/APSFilesystemService.html).

## The APIs for this service

public _interface_ __APSDirectory__ extends  APSFile    [se.natusoft.osgi.aps.api.core.filesystem.model] {

This represents a directory in an _APSFilesystem_.

Use this to create or get directories and files and list contents of directories.

Personal comment: I do prefer the term "folder" over "directory" since I think that is less ambiguous, but since Java uses the term "directory" I decided to stick with that name.

__APSDirectory createDir(String name) throws IOException__

Returns a newly created directory with the specified name.

_Parameters_

> _name_ - The name of the directory to create. 

_Throws_

> _IOException_ - on any failure. 

__APSDirectory createDir(String name, String duplicateMessage) throws IOException__

Returns a newly created directory with the specified name.

_Parameters_

> _name_ - The name of the directory to create. 

> _duplicateMessage_ - The exception message if directory already exists. 

_Throws_

> _IOException_ - on any failure. 

__APSFile createFile(String name) throws IOException__

Creates a new file in the directory represented by the current _APSDirectory_.

_Parameters_

> _name_ - The name of the file to create. 

_Throws_

> _IOException_ - on failure. 

__APSDirectory getDir(String dirname) throws FileNotFoundException__

Returns the specified directory.

_Parameters_

> _dirname_ - The name of the directory to enter. 

_Throws_

> _FileNotFoundException_

__APSFile getFile(String name)__

Returns the named file in this directory.

_Parameters_

> _name_ - The name of the file to get. 

__void recursiveDelete() throws IOException__

Performs a recursive delete of the directory represented by this _APSDirectory_ and all subdirectories and files.

_Throws_

> _IOException_ - on any failure. 

__String[] list()__

_See_

> java.io.File.list()

__APSFile[] listFiles()__

_See_

> java.io.File.listFiles()

}

----

    

public _interface_ __APSFile__   [se.natusoft.osgi.aps.api.core.filesystem.model] {

This represents a file in an _APSFilesystemService_ provided filesystem. It provides most of the API of _java.io.File_ but is not a _java.io.File_! It never discloses the full path in the host filesystem, only paths relative to its _APSFilesystem_ root.

Use the createInputStream/OutputStream/Reader/Writer to read and write the file.

__InputStream createInputStream() throws IOException__

Creates a new _InputStream_ to this file.

_Throws_

> _IOException_

__OutputStream createOutputStream() throws IOException__

Creates a new _OutputStream_ to this file.

_Throws_

> _IOException_

__Reader createReader() throws IOException__

Creates a new _Reader_ to this file.

_Throws_

> _IOException_

__Writer createWriter() throws IOException__

Creates a new _Writer_ to this file.

_Throws_

> _IOException_

__Properties loadProperties() throws IOException__

If this file denotes a properties file it is loaded and returned.

_Throws_

> _IOException_ - on failure or if it is not a properties file. 

__void saveProperties(Properties properties) throws IOException__

If this file denotes a properties file it is written with the specified properties.

_Parameters_

> _properties_ - The properties to save. 

_Throws_

> _IOException_ - on failure or if it is not a properties file. 

__APSDirectory toDirectory()__

If this _APSFile_ represents a directory an _APSDirectory_ instance will be returned. Otherwise _null_ will be returned.

__APSFile getAbsoluteFile()__

_See_

> java.io.File.getAbsoluteFile()

__String getAbsolutePath()__

Returns the absolute path relative to filesystem root.

__APSFile getCanonicalFile() throws IOException__

_See_

> java.io.File.getCanonicalFile()

__String getCanonicalPath() throws IOException__

_See_

> java.io.File.getCanonicalPath()

__String getParent()__

_See_

> java.io.File.getParent()

__APSDirectory getParentFile()__

_See_

> java.io.File.getParentFile()

__String getPath()__

_See_

> java.io.File.getPath()

__boolean renameTo(APSFile dest)__

_See_

> java.io.File.renameTo(File)

__String getName()__

_See_

> java.io.File.getName()

__boolean canRead()__

_See_

> java.io.File.canRead()

__boolean canWrite()__

_See_

> java.io.File.canWrite()

__boolean exists()__

_See_

> java.io.File.exists()

__boolean exists(String name)__

Checks if the named file/directory exists.

_Returns_

> true or false.

_Parameters_

> _name_ - The name to check. 

__boolean isDirectory()__

_See_

> java.io.File.isDirectory()

__boolean isFile()__

_See_

> java.io.File.isFile()

__boolean isHidden()__

_See_

> java.io.File.isHidden()

__long lastModified()__

_See_

> java.io.File.lastModified()

__long length()__

_See_

> java.io.File.length()

__boolean createNewFile() throws IOException__

_See_

> java.io.File.createNewFile()

__boolean delete()__

_See_

> java.io.File.delete()

__void deleteOnExit()__

_See_

> java.io.File.deleteOnExit()

__String toString()__

Returns a string representation of this _APSFile_.

__File toFile()__

This API tries to hide the real path and don't allow access outside of its root, but sometimes you just need the real path to pass on to other code requiring it. This provides that. Use it only when needed!

_Returns_

> A File object representing the real/full path to this file.

}

----

    

public _interface_ __APSFilesystem__   [se.natusoft.osgi.aps.api.core.filesystem.model] {

This represents an _APSFilesystemService_ filesystem.

__APSDirectory getDirectory(String path) throws IOException__

Returns a folder at the specified path.

_Parameters_

> _path_ - The path of the folder to get. 

_Throws_

> _IOException_ - on any failure, specifically if the specified path is not a folder or doesn't exist. 

__APSFile getFile(String path)__

Returns the file or folder of the specified path.

_Parameters_

> _path_ - The path of the file. 

__APSDirectory getRootDirectory()__

Returns the root directory.

}

----

    

public _interface_ __APSFilesystemService__   [se.natusoft.osgi.aps.api.core.filesystem.service] {

This provides a filesystem for use by services/applications. Each filesystem has its own root that cannot be navigated outside of.

Services or application using this should do something like this in their activators:

        APSFilesystemService fss;
        APSFilesystem fs;
        
        if (fss.hasFilesystem("my.file.system")) {
            fs = fss.getFilsystem("my.file.system");
        }
        else {
            fs = fss.createFilesystem("my.file.system");
        }



__APSFilesystem createFilesystem(String owner) throws IOException__

Creates a new filesystem for use by an application or service. Where on disk this filesystem resides is irrelevant. It is accessed using the "owner", and will exist until it is removed.

_Parameters_

> _owner_ - The owner of the filesystem or rather a unique identifier of it. Consider using application or service package. 

_Throws_

> _IOException_ - on any failure. An already existing filesystem for the "owner" will cause this exception. 

__boolean hasFilesystem(String owner)__

Returns true if the specified owner has a filesystem.

_Parameters_

> _owner_ - The owner of the filesystem or rather a unique identifier of it. 

__APSFilesystem getFilesystem(String owner) throws IOException__

Returns the filesystem for the specified owner.

_Parameters_

> _owner_ - The owner of the filesystem or rather a unique identifier of it. 

_Throws_

> _IOException_ - on any failure. 

__void deleteFilesystem(String owner) throws IOException__

Removes the filesystem and all files in it.

_Parameters_

> _owner_ - The owner of the filesystem to delete. 

_Throws_

> _IOException_ - on any failure. 

}

----

    

# APSPlatformService

This is a trivial little service that just returns meta data about the specific platform installation.

The returned information is configured in the _/apsadminweb_.

## APIs

public _class_ __PlatformDescription__   [se.natusoft.osgi.aps.api.core.platform.model] {

This model provides information about a platform installation.







__public PlatformDescription()__

Creates a new PlatformDescription.

__public PlatformDescription(String identifier, String type, String description)__

Creates a new PlatformDescription.

_Parameters_

> _identifier_ - An identifying name for the platform. 

> _type_ - The type of the platform, for example "Development", "SystemTest". 

> _description_ - A short description of the platform instance. 

__public String getIdentifier()__

Returns the platform identifier.

__public String getType()__

Returns the type of the platform.

__public String getDescription()__

Returns the description of the platform.

}

----

    

public _interface_ __APSPlatformService__   [se.natusoft.osgi.aps.api.core.platform.service] {

Provides information about the platform instance.

__public PlatformDescription getPlatformDescription()__

Returns a description of the platform instance / installation.

}

----

    

# APSJSONLib

This is a library (exports all its packages and provides no service) for reading and writing JSON. It can also write a JavaBean object as JSON and take a JSON value or inputstream containing JSON and produce a JavaBean.

This basically provides a class representing each JSON type: JSONObject, JSONString, JSONNumber, JSONBoolean, JSONArray, JSONNull, and a JSONValue class that is the common base class for all the other. Each class knows how to read and write the JSON type it represents. Then there is a JavaToJSON and a JSONToJava class with static methods for converting back and forth. This mapping is very primitive. There has to be one to one between the JSON and the Java objects.

## Changes  0.10.0 _readJSON(...)_ in the __JSONValue__ base class now throws JSONEOFException (extends IOException) on EOF. The reason for this is that internally it reads characters which cannot return -1 or any non JSON data valid char to represent EOF. Yes, it would be possible to replace _char_ with _Character_, but that will have a greater effect on existing code using this lib. If an JSONEOFException comes and is not handled it is still very much more clear what happened than a NullPointerException would be!  

## APIs

Complete javadocs can be found at [http://apidoc.natusoft.se/APSJSONLib/](http://apidoc.natusoft.se/APSJSONLib/).

public _class_ __JSON__   [se.natusoft.osgi.aps.json] {

This is the official API for reading and writing JSON values.

__public static JSONValue read(InputStream jsonIn, JSONErrorHandler errorHandler) throws IOException__

Reads any JSON object from the specified _InputStream_.

_Returns_

> A JSONValue subclass. Which depends on what was found on the stream.

_Parameters_

> _jsonIn_ - The InputStream to read from. 

> _errorHandler_ - An implementation of this interface should be supplied by the user to handle any errors during JSON parsing. 

_Throws_

> _IOException_ - on any IO failures. 

__public static void write(OutputStream jsonOut, JSONValue value) throws IOException__

Writes a _JSONValue_ to an _OutputStream_. This will write compact output by default.

_Parameters_

> _jsonOut_ - The OutputStream to write to. 

> _value_ - The value to write. 

_Throws_

> _IOException_ - on failure. 

__public static void write(OutputStream jsonOut, JSONValue value, boolean compact) throws IOException__

Writes a _JSONValue_ to an _OutputStream_.

_Parameters_

> _jsonOut_ - The OutputStream to write to. 

> _value_ - The value to write. 

> _compact_ - If true the written JSON is made very compact and hard to read but produce less data. 

_Throws_

> _IOException_

}

----

    

public _class_ __JSONArray__ extends  JSONValue  [se.natusoft.osgi.aps.json] {

This class is based on the structure defined on http://www.json.org/.

This represents the "array" diagram on the above mentioned web page:

                      _______________________
                     /                       \
                     |                       |
        |_____ ([) __/_______ (value) _______\__ (]) _____|
        |              /                   \              |
                       |                   |
                       \_______ (,) _______/

@author Tommy Svensson



__public JSONArray()__

Creates a new JSONArray for wrinting JSON output.

__public JSONArray(JSONErrorHandler errorHandler)__

Creates a new JSONArray for reading JSON input and writing JSON output.

_Parameters_

> _errorHandler_





__public void addValue(JSONValue value)__

Adds a value to the array.

_Parameters_

> _value_ - The value to add. 

__public List<JSONValue> getAsList()__

Returns the array values as a List.

__public <T extends JSONValue> List<T> getAsList(Class<T> type)__

Returns the array values as a list of a specific type.

_Returns_

> A list of specified type if type is the same as in the list.

_Parameters_

> _type_ - The class of the type to return values as a list of. 

> _<T>_ - One of the JSONValue subclasses. 





}

----

    

public _class_ __JSONBoolean__ extends  JSONValue    [se.natusoft.osgi.aps.json] {

This class is based on the structure defined on [http://www.json.org/](http://www.json.org/).

@author Tommy Svensson



__public JSONBoolean(boolean value)__

Creates a new JSONBoolean instance for writing JSON output.

_Parameters_

> _value_ - The value for this boolean. 

__public JSONBoolean(JSONErrorHandler errorHandler)__

Creates a new JSONBoolean instance for reading JSON input or writing JSON output.

_Parameters_

> _errorHandler_



__public void setBooleanValue(boolean value)__

Sets the value of this boolean.

_Parameters_

> _value_ - The value to set. 

__public boolean getAsBoolean()__

Returns the value of this boolean.

__public String toString()__

Returns the value of this boolean as a String.





}

----

    

public _class_ __JSONEOFException__ extends  IOException  }  [se.natusoft.osgi.aps.json] {

Thrown if a JSON structure is tried to be read from a stream that has no more data.

}

----

    

public _interface_ __JSONErrorHandler__   [se.natusoft.osgi.aps.json] {

This is called on warnings or failures.

@author Tommy Svensson

__void warning(String message)__

Warns about something.

_Parameters_

> _message_ - The warning message. 

__void fail(String message, Throwable cause) throws RuntimeException__

Indicate failure.

_Parameters_

> _message_ - The failure message. 

> _cause_ - The cause of the failure. Can be null! 

_Throws_

> _RuntimeException_ - This method must throw a RuntimeException. 

}

----

    

public _class_ __JSONNull__ extends  JSONValue    [se.natusoft.osgi.aps.json] {

This class is based on the structure defined on [http://www.json.org/](http://www.json.org/).

@author Tommy Svensson

__public JSONNull()__

Creates a new JSONNull instance for writing JSON output.

__public JSONNull(JSONErrorHandler errorHandler)__

Creates a new JSONNull instance for reading JSON input or writing JSON output.

_Parameters_

> _errorHandler_



__public String toString()__

_Returns_

> as String.





}

----

    

public _class_ __JSONNumber__ extends  JSONValue    [se.natusoft.osgi.aps.json] {

This class is based on the structure defined on http://www.json.org/.

This represents the "number" diagram on the above mentioned web page:

                                              ______________________
                                             /                      \
                                             |                      |
        |_|______________ (0) _______________/__ (.) ___ (digit) ___\_________________________|_|
        | | \       /  \                    /         /           \  \                      / | |
            |       |  |                   /          \___________/  |                      |
            \_ (-) _/  \_ (digit 1-9) ____/_______                   |                      |
                                       /          \                  |                      |
                                       \_ (digit) /           _ (e) _|                      |
                                                             |_ (E) _|           ___________|
                                                             |        _ (+) _   /           |
                                                             \_______/_______\__\_ (digit) _/
                                                                     \_ (-) _/

@author Tommy Svesson



__public JSONNumber(Number value)__

Creates a new JSONNumber instance for writing JSON output.

_Parameters_

> _value_ - The numeric value. 

__public JSONNumber(JSONErrorHandler errorHandler)__

Creates a new JSONNumber instance for reading JSON input or writing JSON output.

_Parameters_

> _errorHandler_ - The error handle to use. 



__public Number toNumber()__

Returns the number as a Number.

__public double toDouble()__

Returns the number as a double value.

__public float toFloat()__

Returns the number as a float value.

__public int toInt()__

Returns the number as an int value.

__public long toLong()__

Returns the number as a long value.

__public short toShort()__

Returns the number as a short value.

__public byte toByte()__

Returns the number as a byte value.

__public String toString()__

_Returns_

> number as String.

__public Object to(Class type)__

Returns the number as a value of the type specified by the type parameter.

_Parameters_

> _type_ - The type of the returned number. 





}

----

    

public _class_ __JSONObject__ extends  JSONValue    [se.natusoft.osgi.aps.json] {

This class is based on the structure defined on http://www.json.org/.

It represents the "object" diagram on the above mentioned web page:

                     ________________________________________
                    /                                        \
        |___ ({) __/_____ (string) ____ (:) ____ (value) _____\___ (}) ____|
        |           /                                        \             |
                    \__________________ (,) _________________/

This is also the starting point.

To write JSON, create a new _JSONObject_ (`new JSONObject()`) and call `addProperty(name`,`value)` for children. Then do jsonObj.writeJSON(outputStream)`.`

To read JSON, create a new _JSONObject_ (`new JSONObject(jsonErrorHandler)`) and then do `jsonObj.readJSON(inputStream)`. Then use `getProperty(name)` to extract children.

@author Tommy Svensson



__public JSONObject()__

Creates a JSONObject instance for writing JSON output.

__public JSONObject(JSONErrorHandler errorHandler)__

Creates a new JSONObject instance for reading JSON input or writing JSON output.

_Parameters_

> _errorHandler_





__public Set<JSONString> getPropertyNames()__

Returns the names of the available properties.

__public JSONValue getProperty(JSONString name)__

Returns the named property.

_Parameters_

> _name_ - The name of the property to get. 

__public JSONValue getProperty(String name)__

Returns the named property.

_Parameters_

> _name_ - The name of the property to get. 

__public void addProperty(JSONString name, JSONValue value)__

Adds a property to this JSONObject instance.

_Parameters_

> _name_ - The name of the property. 

> _value_ - The property value. 

__public void addProperty(String name, JSONValue value)__

Adds a property to this JSONObject instance.

_Parameters_

> _name_ - The name of the property. 

> _value_ - The property value. 





}

----

    

public _class_ __JSONString__ extends  JSONValue    [se.natusoft.osgi.aps.json] {

This class is based on the structure defined on http://www.json.org/.

This represents the "string" diagram on the above mentioned web page:

                   ___________________________________________________________
                  /    ____________________________________________________   \
                  |   /                                                    \  |
        |___ (") _|___|___ (*1)                                        ____|__|_ (") ___|
        |           \                                                   /               |
                     |                                                  |
                     \__ (\) ___ (") (quotation mark) __________________|
                             |__ (\) (reverse solidus) _________________|
                             |__ (/) (solidus) _________________________|
                             |__ (b) (backspace) _______________________|
                             |__ (f) (formfeed) ________________________|
                             |__ (n) (newline) _________________________|
                             |__ (r) (carriage return) _________________|
                             |__ (t) (orizontal tab) ___________________|
                             \__ (u) (4 hexadecimal digits) ____________/
        
        *1: Any UNICODE character except " or \ or control character

@author Tommy Svensson



__public JSONString(String value)__

Creates a new JSONString for writing JSON output.

_Parameters_

> _value_ - The value of this JSONString. 

__public JSONString(JSONErrorHandler errorHandler)__

Creates a new JSONString for reading JSON input and writing JSON output.

_Parameters_

> _errorHandler_















}

----

    

public _abstract_ _class_ __JSONValue__   [se.natusoft.osgi.aps.json] {

This class is based on the structure defined on http://www.json.org/.

This is a base class for all other JSON* classes. It represents the "value" diagram on the above mentioned web page:

                                                          Subclasses
                                                          ----------
        |________________ (STRING) ________________|      JSONString
        |  |_____________ (NUMBER) _____________|  |      JSONNumber
           |_____________ (OBJECT) _____________|         JSONObject
           |_____________ (ARRAY)  _____________|         JSONArray
           |_____________ (true)   _____________|     \__ JSONBoolean
           |_____________ (false)  _____________|     /
           \_____________ (null)   _____________/         JSONNull

@author Tommy Svensson





__protected JSONValue()__

Creates a new JSONValue.

__protected JSONValue(JSONErrorHandler errorHandler)__

Creates a new JSONValue

__protected abstract void readJSON(char c, JSONReader reader) throws IOException__

This will read the vale from an input stream.

_Returns_

> the last character read.

_Parameters_

> _c_ - The first character already read from the input stream. 

> _reader_ - The reader to read from. 

_Throws_

> _IOException_ - on IO failure. 

__protected abstract void writeJSON(JSONWriter writer, boolean compact) throws IOException__

This will write the data held by this JSON value in JSON format on the specified stream.

_Parameters_

> _writer_ - A JSONWriter instance to write with. 

> _compact_ - If true write the JSON as compact as possible. false means readable, indented. 

_Throws_

> _IOException_ - On IO failure. 

__protected JSONErrorHandler getErrorHandler()__

_Returns_

> The user supplied error handler.







__protected void warn(String message)__

Provide a warning.

_Parameters_

> _message_ - The warning message. 

__protected void fail(String message, Throwable cause)__

Fails the job.

_Parameters_

> _message_ - The failure message. 

> _cause_ - An eventual cause of the failure. Can be null. 

__protected void fail(String message)__

Fails the job.

_Parameters_

> _message_ - The failure message. 

__public void readJSON(InputStream is) throws IOException__

This will read the value from an input stream.

_Parameters_

> _is_ - The input stream to read from. 

_Throws_

> _IOException_ - on IO failure. 

__public void writeJSON(OutputStream os) throws IOException__

This writes JSON to the specified OutputStream.

_Parameters_

> _os_ - The outoutStream to write to. 

_Throws_

> _IOException_ - on IO failure. 

__public void writeJSON(OutputStream os, boolean compact) throws IOException__

This writes JSON to the specified OutputStream.

_Parameters_

> _os_ - The outoutStream to write to. 

> _compact_ - If true write JSON as compact as possible. If false write it readable with indents. 

_Throws_

> _IOException_ - on IO failure. 





















__protected JSONReader(PushbackReader reader, JSONErrorHandler errorHandler)__

Creates a new JSONReader instance.

_Parameters_

> _reader_ - The PushbackReader to read from. 

> _errorHandler_ - The handler for errors. 

__protected char getChar() throws IOException__

Returns the next character on the specified input stream, setting EOF state checkable with isEOF().

_Throws_

> _IOException_ - on IO problems. 

__protected char getChar(boolean handleEscapes) throws IOException__

Returns the next character on the specified input stream, setting EOF state checkable with isEOF().

_Parameters_

> _handleEscapes_ - If true then \* escape character are handled. 

_Throws_

> _IOException_ - on IO problems. 

__protected void ungetChar(char c) throws IOException__

Unreads the specified character so that the next call to getNextChar() will return it again.

_Parameters_

> _c_ - The character to unget. 

__protected char skipWhitespace(char c) throws IOException__

Skips whitespace returning the first non whitespace character. This also sets the EOF flag.

_Parameters_

> _c_ - The first char already read from the input stream. 

_Throws_

> _IOException_

__protected char skipWhitespace() throws IOException__

Skips whitespace returning the first non whitespace character. This also sets the EOF flag.

_Throws_

> _IOException_



__protected char readUntil(String until, char c, StringBuilder sb, boolean handleEscapes) throws IOException__

Reads until any of a specified set of characters occur.

_Returns_

> 

_Parameters_

> _until_ - The characters to stop reading at. The stopping character will be returned unless EOF. 

> _c_ - The first preread character. 

> _sb_ - If not null read characters are added to this. The stopping character will not be included. 

> _handleEscapes_ - True if we are reading a string that should handle escape characters. 

_Throws_

> _IOException_

__protected char readUntil(String until, StringBuilder sb, boolean string) throws IOException__

Reads until any of a specified set of characters occur.

_Parameters_

> _until_ - The characters to stop reading at. The stopping character will be returned unless EOF. 

> _sb_ - If not null read characters are added to this. The stopping character will not be included. 

> _string_ - True if we are rading a string that should be escaped. 

_Throws_

> _IOException_

__protected char readUntil(String until, StringBuilder sb) throws IOException__

Reads until any of a specified set of characters occur.

_Parameters_

> _until_ - The characters to stop reading at. The stopping character will be returned unless EOF. 

> _sb_ - If not null read characters are added to this. The stopping character will not be included. 

_Throws_

> _IOException_

__protected boolean checkValidChar(char c, String validChars)__

Returns true if c is one of the characters in validChars.

_Parameters_

> _c_ - The character to check. 

> _validChars_ - The valid characters. 

__protected void assertChar(char a, char e, String message)__

Asserts that char a equals expected char c.

_Parameters_

> _a_ - The char to assert. 

> _e_ - The expected value. 

> _message_ - Failure message. 

__protected void assertChar(char a, String expected, String message)__

Asserts that char a equals expected char c.

_Parameters_

> _a_ - The char to assert. 

> _expected_ - String of valid characters. 

> _message_ - Failure message. 

protected _static_ _class_ __JSONWriter__   [se.natusoft.osgi.aps.json] {

For subclasses to use in writeJSON(JSONWriter writer).



__protected JSONWriter(Writer writer)__

Creates a new JSONWriter instance.

_Parameters_

> _writer_ - The writer to write to. 

__protected void write(String json) throws IOException__

Writes JSON output.

_Parameters_

> _json_ - The JSON output to write. 

_Throws_

> _IOException_ - on IO failure. 

__protected void writeln(String json) throws  IOException__

Writes JSON output plus a newline.

_Parameters_

> _json_ - The JSON output to write. 

_Throws_

> _IOException_

}

----

    

public _class_ __BeanInstance__   [se.natusoft.osgi.aps.json.tools] {

This wraps a Java Bean instance allowing it to be populated with data using _setProperty(String_,_Object)_ methods handling all reflection calls.



__public BeanInstance(Object modelInstance)__

Creates a new ModelInstance.

_Parameters_

> _modelInstance_ - The model instance to wrap. 

__public Object getModelInstance()__

Returns the test model instance held by this object.

__public List<String> getSettableProperties()__

Returns a list of settable properties.

__public List<String> getGettableProperties()__

Returns a list of gettable properties.

__public void setProperty(String property, Object value) throws JSONConvertionException__

Sets a property

_Parameters_

> _property_ - The name of the property to set. 

> _value_ - The value to set with. 

_Throws_

> _JSONConvertionException_ - on any failure to set the property. 

__public Object getProperty(String property) throws JSONConvertionException__

Returns the value of the specified property.

_Returns_

> The property value.

_Parameters_

> _property_ - The property to return value of. 

_Throws_

> _JSONConvertionException_ - on failure (probably bad property name!). 



__public Class getPropertyType(String property) throws JSONConvertionException__

Returns the type of the specified property.

_Returns_

> The class representing the property type.

_Parameters_

> _property_ - The property to get the type for. 

_Throws_

> _JSONConvertionException_ - if property does not exist. 

}

----

    

public _class_ __JavaToJSON__   [se.natusoft.osgi.aps.json.tools] {

Takes a JavaBean and produces a JSONObject.

__public static JSONObject convertObject(Object javaBean) throws JSONConvertionException__

Converts a JavaBean object into a _JSONObject_.

_Returns_

> A JSONObject containing all values from the JavaBean.

_Parameters_

> _javaBean_ - The JavaBean object to convert. 

_Throws_

> _JSONConvertionException_ - on converting failure. 

__public static JSONObject convertObject(JSONObject jsonObject, Object javaBean) throws JSONConvertionException__

Converts a JavaBean object into a _JSONObject_.

_Returns_

> A JSONObject containing all values from the JavaBean.

_Parameters_

> _jsonObject_ - The jsonObject to convert the bean into or null for a new JSONObject. 

> _javaBean_ - The JavaBean object to convert. 

_Throws_

> _JSONConvertionException_ - on converting failure. 

__public static JSONValue convertValue(Object value)__

Converts a value from a java value to a _JSONValue_.

_Returns_

> The converted JSONValue.

_Parameters_

> _value_ - The java value to convert. It can be one of String, Number, Boolean, null, JavaBean, or an array of those. 

}

----

    

public _class_ __JSONConvertionException__ extends  RuntimeException    [se.natusoft.osgi.aps.json.tools] {

This exception is thrown on failure to convert from JSON to Java or Java to JSON.

Almost all exceptions within the APS services and libraries extend either _APSException_ or _APSRuntimeException_. I decided to just extend RuntimeException here to avoid any other dependencies for this library since it can be useful outside of APS and can be used as any jar if not deployed in OSGi container.

__public JSONConvertionException(final String message)__

Creates a new _JSONConvertionException_.

_Parameters_

> _message_ - The exception message 

__public JSONConvertionException(final String message, final Throwable cause)__

Creates a new _JSONConvertionException_.

_Parameters_

> _message_ - The exception message 

> _cause_ - The cause of this exception. 

}

----

    

public _class_ __JSONToJava__   [se.natusoft.osgi.aps.json.tools] {

Creates a JavaBean instance and copies data from a JSON value to it.

The following mappings are made in addition to the expected ones:

* _JSONArray_ only maps to an array property.

* Date properties in bean are mapped from _JSONString_ "yyyy-MM-dd HH:mm:ss".

* Enum properties in bean are mapped from _JSONString_ which have to contain enum constant name.

__public static <T> T convert(InputStream jsonStream, Class<T> javaClass) throws IOException, JSONConvertionException__

Returns an instance of a java class populated with data from a json object value read from a stream.

_Returns_

> A populated instance of javaClass.

_Parameters_

> _jsonStream_ - The stream to read from. 

> _javaClass_ - The java class to instantiate and populate. 

_Throws_

> _IOException_ - on IO failures. 

> _JSONConvertionException_ - On JSON to Java failures. 

__public static <T> T convert(String json, Class<T> javaClass) throws IOException, JSONConvertionException__

Returns an instance of a java class populated with data from a json object value read from a String containing JSON.

_Returns_

> A populated instance of javaClass.

_Parameters_

> _json_ - The String to read from. 

> _javaClass_ - The java class to instantiate and populate. 

_Throws_

> _IOException_ - on IO failures. 

> _JSONConvertionException_ - On JSON to Java failures. 

__public static <T> T convert(JSONValue json, Class<T> javaClass) throws JSONConvertionException__

Returns an instance of java class populated with data from json.

_Returns_

> A converted Java object.

_Parameters_

> _json_ - The json to convert to java. 

> _javaClass_ - The class of the java object to convert to. 

_Throws_

> _JSONConvertionException_ - On failure to convert. 







}

----

    

public _class_ __SystemOutErrorHandler__ implements  JSONErrorHandler    [se.natusoft.osgi.aps.json.tools] {

A simple implementation of _JSONErrorHandler_ that simply displays messages on System.out and throws a _RuntimeException_ on fail. This is used by the tests. In a non test case another implementation is probably preferred.





}

----

    

# APSToolsLib

This is a library of utilities including a service tracker that beats the (beep) out of the default one including exception rather than null response, timeout specification, getting a proxied service implementation that automatically uses the tracker, allocating a service, calling it, and deallocating it again. This makes it trivially easy to handle a service being restarted or redeployed. It also includes a logger utility that will lookup the standard log service and log to that if found.

This bundle provides no services. It just makes all its packages public. Every bundle included in APS makes use of APSToolsLib so it must be deployed for things to work.

Please note that this bundle has no dependencies! That is, it can be used as is without requireing any other APS bundle.

## APSServiceTracker

This does the same thing as the standard service tracker included with OSGi, but does it better with more options and flexibility. One of the differences between this tracker and the OSGi one is that this throws an _APSNoServiceAvailableException_ if the service is not available. Personally I think this is easier to work with than having to check for a null result.

There are several variants of constructors, but here is an example of one of the most used ones within the APS services:

        APSServiceTracker<Service> tracker = 
            new APSServiceTracker<Service>(context, Service.class, "20 seconds");
        tracker.start();
        

Note that the third argument, which is a timeout can also be specified as an int in which case it is always in miliseconds. The string variant supports the a second word of "sec[onds]" and "min[utes]" which indicates the type of the first numeric value. "forever" means just that and requires just one word. Any other second words than those will be treated as milliseconds. The APSServiceTracker also has a set of constants for the timeout string value:

        public static final String SHORT_TIMEOUT = "3 seconds";
        public static final String MEDIUM_TIMEOUT = "30 seconds";
        public static final String LARGE_TIMEOUT = "2 minutes";
        public static final String VERY_LARGE_TIMEOUT = "5 minutes";
        public static final String HUGE_LARGE_TIMEOUT = "10 minutes";
        public static final String NO_TIMEOUT = "forever";

On bundle stop you should do:

        tracker.stop(context);
        

So that the tracker unregisters itself from receiving bundle/service events.

### Services and active service

The tracker tracks all instances of the service being tracked. It however have the notion of an active service. The active service is the service instance that will be returned by allocateService() (which is internally used by all other access methods also). On startup it will be the first service instance received. It will keep tracking other instances comming in, but as long as the active service does not go away it will be the one used. If the active service goes away then the the one that is at the beginning of the list of the other tracked instances will become active. If that list is empty there will be no active, which will trigger a wait for a service to become available again if allocateService() is called.

### Providing a logger

You can provide an APSLogger (see further down about APSLogger) to the tracker:

        tracker.setLogger(apsLogger);
        

When available the tracker will log to this.

### Tracker as a wrapped service

The tracker can be used as a wrapped service:

        Service service = tracker.getWrappedService();
        

This gives you a proxied _service_ instance that gets the real service, calls it, releases it and return the result. This handles transparently if a service has been restarted or one instance of the service has gone away and another came available. It will wait for the specified timeout for a service to become available and if that does not happen the _APSNoServiceAvailableException_ will be thrown. This is of course a runtime exception which makes the service wrapping possible without loosing the possibility to handle the case where the service is not available.

### Using the tracker in a similar way to the OSGi standard tracker

To get a service instance you do:

        Service service = tracker.allocateService();
        

Note that if the tracker has a timeout set then this call will wait for the service to become available if it is currently not available until an instance becomes available or the timeout time is reached. It will throw _APSNoServiceAvailableException_ on failure in any case.

When done with the service do:

        tracker.releaseService();

### Accessing a service by tracker callback

There are a few variants to get a service instance by callback. When the callbacks are used the actual service instance will only be allocated during the callback and then released again.

#### onServiceAvailable

This will result in a callback when any instance of the service becomes available. If there is more than one service instance published then there will be a callback for each.

            tracker.onServiceAvailable(new OnServiceAvailable<Service>() {
                @Override
                public void onServiceAvailable(Service service, ServiceReference serviceReference) throws Exception {
                    // Do something.
                }
            });

#### onServiceLeaving

This will result in a callback when any instance of the service goes away. If there is more than one service instance published the there will be a callback for each instance leaving.

            onServiceLeaving(new OnServiceLeaving<Service>() {
        
                @Override
                public void onServiceLeaving(ServiceReference service, Class serviceAPI) throws Exception {
                    // Handle the service leaving.
                }
            });

Note that since the service is already gone by this time you don't get the service instance, only its reference and the class representing its API. In most cases both of these parameters are irellevant.

#### onActiveServiceAvailable

This does the same thing as onServiceAvailable() but only for the active service. It uses the same _OnServiceAvailable_ interface.

#### onActiveServiceLeaving

This does the same thing as onServiceLeaving() but for the active service. It uses the same _OnServiceLeaving_ interface.

#### withService

Runs the specified callback providing it with a service to use. This will wait for a service to become available if a timeout has been provided for the tracker.

Don't use this in an activator start() method! onActiveServiceAvailable() and onActiveServiceLeaving() are safe in a start() method, this is not!

            tracker.withService(new WithService<Service>() {
                @Override
                public void withService(Service service, Object... args) throws Exception {
                    // do something here.
                }
            }, arg1, arg2);

If you don't have any arguments this will also work:

            tracker.withService(new WithService<Service>() {
                @Override
                public void withService(Service service) throws Exception {
                    // do something here
                }
            });

#### withServiceIfAvailable

This does the same as withService(...) but without waiting for a service to become available. If the service is not available at the time of the call the callback will not be called. No exception is thrown by this!

#### withAllAvailableServices

This is used exactly the same way as withService(...), but the callback will be done for each tracked service instance, not only the active.

#### onTimeout (since 0.9.3)

This allows for a callback when the tracker times out waiting for a service. This callback will be called just before the _APSNoServiceAvailableException_ is about to be thrown.

        tracker.onTimeout(new OnTimeout() {
            @Override
            public void onTimeout() {
                // do something here
            }
        }); 

## APSLogger

This provides logging functionality. The no args constructor will log to System.out by default. The OutputStream constructor will logg to the specified output stream by default.

The APSLogger can be used by just creating an instance and then start using the info(...), error(...), etc methods. But in that case it will only log to System.out or the provided OutputStream. If you however do this:

        APSLogger logger = new APSLogger();
        logger.start(context);
        

then the logger will try to get hold of the standard OSGi LogService and if that is available log to that. If the log service is not available it will fallback to the OutputStream.

If you call the `setServiceRefrence(serviceRef);` method on the logger then information about that service will be provied with each log.

## APSActivator

This is a BundleActivator implementation that uses annotations to register services and inject tracked services. Any bundle can use this activator by just importing the _se.natusoft.osgi.aps.tools_ package.

This is actually a rather trivial class that just scans the bundle for classes and inspects all classes for annotations and act on them. Most methods are protected making it easy to subclass this class and expand on its functionality.

__Please note__ that it does _class.getDeclaredFields()_ and _class.getDeclaredMethods()_! This means that it will only see the bottom class of an inheritance hiearchy!

The following annotations are available:

__@OSGiServiceProvider__ - This should be specified on a class that implements a service interface and should be registered as an OSGi service. _Please note_ that the first declared implemented interface is used as service interface unless you specify serviceAPIs={Svc.class, ...}.

        public @interface OSGiProperty {
            String name();
            String value();
        }
        
        public @interface OSGiServiceInstance {
        
            /** Extra properties to register the service with. */
            OSGiProperty[] properties() default {};
        
            /** The service API to register instance with. If not specified the first implemented interface will be used. */
            Class[] serviceAPIs() default {};
        }
        
        public @interface OSGiServiceProvider {
        
            /** Extra properties to register the service with. */
            OSGiProperty[] properties() default {};
        
            /** The service API to register instance with. If not specified the first implemented interface will be used. */
            Class[] serviceAPIs() default {};
        
            /** This can be used as an alternative to properties() and also supports several instances. */
            OSGiServiceInstance[] instances() default {};
        
            /**
             * This can be used as an alternative and will instantiate the specified factory class which will deliver
             * one set of Properties per instance.
             */
            Class<? extends APSActivator.InstanceFactory> instanceFactoryClass() default APSActivator.InstanceFactory.class;
        
            /**
             * If true this service will be stared in a separate thread. This means the bundle start
             * will continue in parallel and that any failures in startup will be logged, but will
             * not stop the bundle from being started. If this is true it wins over required service
             * dependencies of the service class. Specifying this as true allows you to do things that
             * cannot be done in a bunde activator start method, like calling a service tracked by 
             * APSServiceTracker, without causing a deadlock.
             */
            boolean threadStart() default false;
        
        }

__@OSGiService__ - This should be specified on a field having a type of a service interface to have a service of that type injected, and continuously tracked. Any call to the service will throw an APSNoServiceAvailableException (runtime) if no service has become available before the specified timeout. It is also possible to have APSServiceTracker as field type in which case the underlying configured tracker will be injected instead.

If _required=true_ is specified and this field is in a class annotated with _@OSGiServiceProvider_ then the class will not be registered as a service until the service dependency is actually available, and will also be unregistered if the tracker for the service does a timeout waiting for a service to become available. It will then be reregistered again when the dependent service becomes available again. Please note that unlike iPOJO the bundle is never stopped on dependent service unavailability, only the actual service is unregistered as an OSGi service. A bundle might have more than one service registered and when a dependency that is only required by one service goes away the other service is still available.

        public @interface OSGiService {
        
            /** The timeout for a service to become available. Defaults to 30 seconds. */
            String timeout() default "30 seconds";
        
            /** Any additional search criteria. Should start with '(' and end with ')'. Defaults to none. */
            String additionalSearchCriteria() default "";
        
            /** If set to true the service using this service will not be registered until the service becomes available. */
            boolean required() default false;
        
        }

__@Managed__ - This will have an instance managed and injected. There will be a unique instance for each name specified with the default name of "default" being used if none is specified. There are 2 field types handled specially: BundleContext and APSLogger. A BundleContext field will get the bundles context injected. For an APSLogger instance the 'loggingFor' annotation property can be specified. Please note that any other type must have a default constructor to be instantiated and injected!

        public @interface Managed {
            /**
             * The name of the instance to inject. If the same is used in multiple classes the same instance will
             * be injected.
             */
            String name() default "default";
        
            /**
             * A label indicating who is logging. If not specified the bundle name will be used. This is only
             * relevant if the injected type is APSLogger.
             */
            String loggingFor() default "";
        }

__@BundleStart__ - This should be used on a method and will be called on bundle start. The method should take no arguments. If you need a BundleContext just inject it with @APSInejct. The use of this annotation is only needed for things not supported by this activator. Please note that a method annotated with this annotation can be static (in which case the class it belongs to will not be instantiaded -- due to this!). You can provide this annotation on as many methods in as many classes as you want. They will all be called (in the order classes are discovered in the bundle).

        public @interface BundleStart {
        
            /**
             * If true the start method will run in a new thread. Any failures in this case will not fail
             * the bundle startup, but will be logged.
             */
            boolean thread() default false;
        }

__@BundleStop__ - This should be used on a method and will be called on bundle stop. The method should take no arguments. This should probably be used if @APSBundleStart is used. Please note that a method annotated with this annotation can be static!

        public @interface BundleStop {}

### Usage as BundleActivator

The _APSActivator_ class has 2 constructors. The default constructor without arguments are used for BundleActivator usage. In this case you just specify this class as your bundles activator, and then use the annotations described above. Thats it!

### Other Usage

Since the activator usage will manage and create instances of all annotated classes this will not always work in all situations. One example is web applications where the web container is responsible for creating servlets. If you specifiy APSActivator as an activator for a WAB bundle and then use the annotations in a servlet then APSActivator will have a managed instance of the servlet, but it will not be the same instance as the web contatiner will run.

Therefore APSActivator has another constructor that takes a vararg of instances: `public APSActivator(Object..`.`instances)`. There is also a `public void addManagedInstance(Object instance)` method. These allow you to add an already existing instance to be managed by APSActivator. In addition to the provided existing instances it will still scan the bundle for classes to manage. It will however not double manage any class for which an existing instance of has already been provided. Any annotated class for which existing instances has not been provided will be instantiated by APSActivator.

__Please note__ that if you create an instance of APSActivator in a servlet and provide the servlet instance to it and start it (you still need to do _start(BundleContext)_ and _stop(BundleContext)_ when used this way!), then you need to catch the close of the servlet and do _stop_ then.

There are 2 support classes in _APSWebTools_:

* APSVaadinOSGiApplication - This is subclassed by your Vaading application.

* APSOSGiSupport - You create an instance of this in a servlet and let your servlet implement the _APSOSGiSupportCallbacks_ interface which is then passed to the constructor of APSOSGiSupport.

Both of these creates and manages an APSActivator internally and catches shutdown to take it down. They also provide other utilities like providing the BundleContext. See _APSWebTools_ for more information.

## APSContextWrapper

This provides a static wrap(...) method:

        Service providedService = APSContextWrapper.wrap(serviceProvider, Service.class);
        

where _serviceProvider_ is an instance of a class that implements _Service_. The resulting instance is a java.lang.reflect.Proxy implementation of _Service_ that ensures that the _serviceProvider_ ClassLoader is the context class loader during each call to all service methods that are annotated with @APSRunInBundlesContext annotation in _Service_. The wrapped instance can then be registered as the OSGi service provider.

Normally the threads context class loader is the original service callers context class loader. For a web application it would be the web containers context class loader. If a service needs its own bundles class loader during its execution then this wrapper can be used.

## ID generators

There is one interface:

        /**
         * This is a generic interface for representing IDs. 
         */
        public interface ID extends Comparable<ID> {
        
            /**
             * Creates a new unique ID.
             *
             * @return A newly created ID.
             */
            public ID newID();
        
            /**
             * Tests for equality.
             *
             * @param obj The object to compare with.
             *
             * @return true if equal, false otherwise.
             */
            @Override
            public boolean equals(Object obj);
        
            /**
             * @return The hash code.
             */
            @Override
            public int hashCode();
        
        }

that have 2 implementations:

* IntID - Produces int ids.

* UUID - Produces java.util.UUID Ids.

## Javadoc

The javadoc for this can be found at [http://apidoc.natusoft.se/APSToolsLib/](http://apidoc.natusoft.se/APSToolsLib/).

# APSWebTools

This is not an OSGi bundle! This is a plain jar containing utilities for web applications. Specifically APS administration web applications. This jar has to be included in each web application that wants to use it.

Among other things it provides support for being part of the APS administration web login (APSAdminWebLoginHandler). Since the APS administration web is built using Vaadin it has Vaadin support classes. APSVaadinOSGiApplication is a base class used by all APS administration webs.

## APIs

The following are the APIs for a few selected classes. The complete javadoc for this library can be found at [http://apidoc.natusoft.se/APSWebTools/](http://apidoc.natusoft.se/APSWebTools/).

----

public _class_ __APSAdminWebLoginHandler__ extends  APSLoginHandler  implements  APSLoginHandler.HandlerInfo    [se.natusoft.osgi.aps.tools.web] {

This is a login handler to use by any admin web registering with the _APSAdminWeb_ to validate that there is a valid login available.



__public APSAdminWebLoginHandler(BundleContext context)__

Creates a new _APSAdminWebLoginHandler_.

_Parameters_

> _context_ - The bundle context. 

__public void setSessionIdFromRequestCookie(HttpServletRequest request)__

Sets the session id from a cookie in the specified request.

_Parameters_

> _request_ - The request to get the session id cookie from. 

__public void setSessionIdFromRequestCookie(CookieTool.CookieReader cookieReader)__

Sets the session id from a cookie in the specified request.

_Parameters_

> _cookieReader_ - The cookie reader to get the session id cookie from. 

__public void saveSessionIdOnResponse(HttpServletResponse response)__

Saves the current session id on the specified response.

_Parameters_

> _response_ - The response to save the session id cookie on. 

__public void saveSessionIdOnResponse(CookieTool.CookieWriter cookieWriter)__

Saves the current session id on the specified response.

_Parameters_

> _cookieWriter_ - The cookie writer to save the session id cookie on. 









}

----

    

public _class_ __APSLoginHandler__ implements  LoginHandler    [se.natusoft.osgi.aps.tools.web] {

This class validates if there is a valid logged in user and also provides a simple login if no valid logged in user exists.

This utility makes use of APSAuthService to login auth and APSSessionService for session handling. Trackers for these services are created internally which requires the shutdown() method to be called when no longer used to cleanup.

The bundle needs to import the following packages for this class to work:

         se.natusoft.osgi.aps.api.auth.user;version="[0.9,2)",
         se.natusoft.osgi.aps.api.misc.session;version="[0.9,2)"





















__protected void setHandlerInfo(HandlerInfo handlerInfo)__

Sets the handler info when not provided in constructor.

_Parameters_

> _handlerInfo_ - The handler info to set. 

__public void shutdown()__

Since this class internally creates and starts service trackers this method needs to be called on shutdown to cleanup!

__public String getLoggedInUser()__

This returns the currently logged in user or null if none are logged in.



__public boolean hasValidLogin()__

Returns true if this handler sits on a valid login.

__public boolean login(String userId, String pw)__

Logs in with a userid and a password.

This method does not use or modify any internal state of this object! It only uses the APSAuthService that this object sits on. This allows code sitting on an instance of this class to use this method for validating a user without having to setup its own service tracker for the _APSAuthService_ when this object is already available due to the code also being an _APSAdminWeb_ member. It is basically a convenience.

_Returns_

> true if successfully logged in, false otherwise.

_Parameters_

> _userId_ - The id of the user to login. 

> _pw_ - The password of the user to login. 

__public boolean login(String userId, String pw, String requiredRole)__

Logs in with a userid and a password, and a required role.

This method does not use or modify any internal state of this object! It only uses the APSAuthService that this object sits on. This allows code sitting on an instance of this class to use this method for validating a user without having to setup its own service tracker for the _APSAuthService_ when this object is already available due to the code also being an _APSAdminWeb_ member. It is basically a convenience.

_Returns_

> a valid User object on success or null on failure.

_Parameters_

> _userId_ - The id of the user to login. 

> _pw_ - The password of the user to login. 

> _requiredRole_ - If non null the user is required to have this role for a successful login. If it doesn't null will 

public _static_ _interface_ __HandlerInfo__   [se.natusoft.osgi.aps.tools.web] {

Config values for the login handler.

__String getSessionId()__

_Returns_

> An id to an APSSessionService session.

__void setSessionId(String sessionId)__

Sets a new session id.

_Parameters_

> _sessionId_ - The session id to set. 

__String getUserSessionName()__

_Returns_

> The name of the session data containing the logged in user if any.

__String getRequiredRole()__

_Returns_

> The required role of the user for it to be considered logged in.

}

----

    

public _interface_ __LoginHandler__   [se.natusoft.osgi.aps.tools.web] {

This is a simple API for doing a login.

__public boolean hasValidLogin()__

Returns true if this handler sits on a valid login.

__boolean login(String userId, String pw)__

Logs in with a userid and a password.

_Returns_

> true if successfully logged in, false otherwise.

_Parameters_

> _userId_ - The id of the user to login. 

> _pw_ - The password of the user to login. 

__public void shutdown()__

If the handler creates service trackers or other things that needs to be shutdown when no longer used this method needs to be called when the handler is no longer needed.

}

----

    

# APSAuthService

This is a very simple little service that only does authentication of users. This service is currently used by the APS administration web (/apsadminweb) and APSExtProtocolHTTPTransportProvider for remote calls to services over http.

The idea behind this service is that it should be easy to provide an implementation of this that uses whatever authentication scheme you want/need. If you have an LDAP server you want to authenticate against for example, provide an implementation that looks up and authenticates the user against the LDAP server.

See this a little bit like an authentication plugin.

The APS web applications that use this only uses password authentication.

## APSSimpleUserServiceAuthServiceProvider

This provides an APSAuthService that uses the APSSimpleUserService to authenticate users. It only supports password authentication. If you don't have your own implementation of APSAuthService then you can deploy this one along with APSSimpleUserService, and probably APSUserAdminWeb.

__Please note__ however that the standard implementation of APSSimpleUserService can register several instances with an "instance=name" property where name is unique for each instance, and each instance can reference a different data source. This is configured under _persistence/dsrefs_ in the configuration. If no instances are configured an instance of "aps-admin-web" will be created by default. If instances are configured the default will not be created. And now the the point: APSSimpleuserServiceAuthServiceProvider will as of now track the "aps-admin-web" instance of APSSimpleUserService! If no such instance is configured it will fail after a timeout of not finding a service!

## API

public _interface_ __APSAuthService<Credential>__   [se.natusoft.osgi.aps.api.auth.user] {

This is intended to be used as a wrapper to other means of authentication. Things in APS that needs authentication uses this service.

Implementations can lookup the user in an LDAP for example, or use some other user service.

APS supplies an _APSSimpleUserServiceAuthServiceProvider_ that uses the _APSSimpleUserService_ to authenticate. It is provided in its own bundle.

__Properties authUser(String userId, Credential credentials, AuthMethod authMethod) throws APSAuthMethodNotSupportedException__

This authenticates a user. A Properties object is returned on successful authentication. null is returned on failure. The Properties object returned contains misc information about the user. It can contain anything or nothing at all. There can be no assumptions about its contents!

_Returns_

> User properties on success, null on failure.

_Parameters_

> _userId_ - The id of the user to authenticate. 

> _credentials_ - What this is depends on the value of AuthMethod. It is up to the service implementation to resolve this. 

> _authMethod_ - This hints at how to interpret the credentials. 

_Throws_

> _APSAuthMethodNotSupportedException_ - If the specified authMethod is not supported by the implementation. 

__Properties authUser(String userId, Credential credentials, AuthMethod authMethod, String role) throws APSAuthMethodNotSupportedException__

This authenticates a user. A Properties object is returned on successful authentication. _null_ is returned on failure. The Properties object returned contains misc information about the user. It can contain anything or nothing at all. There can be no assumptions about its contents!

_Returns_

> User properties on success, null on failure.

_Parameters_

> _userId_ - The id of the user to authenticate. 

> _credentials_ - What this is depends on the value of AuthMethod. It is up to the service implementation to resolve this. 

> _authMethod_ - This hints at how to interpret the credentials. 

> _role_ - The specified user must have this role for authentication to succeed. Please note that the APS admin webs will pass "apsadmin" for the role. The implementation might need to translate this to another role. 

_Throws_

> _APSAuthMethodNotSupportedException_ - If the specified authMethod is not supported by the implementation. 

__AuthMethod[] getSupportedAuthMethods()__

Returns an array of the AuthMethods supported by the implementation.

public _static_ _enum_ __AuthMethod__   [se.natusoft.osgi.aps.api.auth.user] {

This hints at how to use the credentials.

__NONE__

Only userid is required.

__PASSWORD__

toString() on the credentials object should return a password.

__KEY__

The credential object is a key of some sort.

__CERTIFICATE__

The credential object is a certificate of some sort.

__DIGEST__

The credential object is a digest password.

__SSO__

The credential object contains information for participating in a single sign on.

}

----

    

# APSSimpleUserService

This is an simple, easy to use service for handling logged in users. It provides two services: APSSimpleUserService and APSSimpleUserServiceAdmin. The latter handles all creation, editing, and deletion of roles and users. This service in itself does not require any authentication to use! Thereby you have to trust all code in the server! The APSUserAdminWeb WAB bundle however does require a user with role _apsadmin_ to be logged in or it will simply repsond with a 401 (UNAUTHORIZED).

So why this and not org.osgi.service.useradmin ? Well, maybe I'm just stupid, but _useradmin_ does not make sense to me. It seems to be missing things, specially for creating. You can create a role, but you cannot create a user. There is no obvious authentication of users. Maybee that should be done via the credentials Dictionary, but what are the expected keys in there ? APSSimpleUserService is intended to make user and role handling simple and clear.

## Basic example

To login a user do something like this:

        APSSimpleUserService userService ...
        ...
        User user = userService.getUser(userId);
        if (user == null) {
            throw new AuthException("Bad login!");
        }
        if (!userService.authenticateUser(user, password, APSSimpleUserService.AUTH_METHOD_PASSWORD)) {
            throw new AuthException("Bad login!");
        }
        ...
        if (user.isAuthenticated() && user.hasRole("apsadmin")) {
            ...
        }
        

## Setup

The following SQL is needed to create the database tables used by the service.

        /*
         * This represents one role.
         */
        create table role (
          /* The id and key of the role. */
          id varchar(50) not null primary key,
        
          /* A short description of what the role represents. */
          description varchar(200),
        
          /* 1 == master role, 0 == sub-role. */
          master int
        );
        
        /*
         * This represents one user.
         */
        create table svcuser (
          /* User id and also key. */
          id varchar(50) not null primary key,
        
          /* For the provided implementation this is a password. */
          auth varchar(2000),
        
          /*
           * The service stores string properties for the user here as one long string.
           * These are not meant to be searchable only to provide information about the
           * user.
           *
           * You might want to adapt this size to the amount of data you will be adding
           * to a user.
           */
          user_data varchar(4000)
        );
        
        /*
         * A user can have one or more roles.
         */
        create table user_role (
          user_id varchar(50) not null,
          role_id varchar(50) not null,
          primary key (user_id, role_id),
          foreign key (user_id) references svcuser (id),
          foreign key (role_id) references role (id)
        );
        
        /*
         * A role can have one ore more sub-roles.
         */
        create table role_role (
          master_role_id varchar(50) not null,
          role_id varchar(50) not null,
          primary key (master_role_id, role_id),
          foreign key (master_role_id) references role (id),
          foreign key (role_id) references role (id)
        );
        
        /*
         * ---- This part is mostly an example ----
         * WARNING: You do however need a role called 'apsadmin' to be able to login to
         * /apsadminweb! The name of the user having that role does not matter. As long
         * as it is possible to login to /apsadminweb new roles and users can be created
         * there.
         */
        
        /* The following adds an admin user. */
        insert into role VALUES ('apsadmin', 'Default admin for APS', 1);
        insert into svcuser VALUES ('apsadmin', 'admin', '');
        insert into user_role VALUES ('apsadmin', 'apsadmin');
        
        /* This adds a role for non admin users. */
        insert into role VALUES ('user', 'Plain user', 1);
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        

<!--
  
The above empty lines are required due to a bug in iText used to render the PDF. It will move the picture to the next page completely out of context since text after the picture will then come before it. 
-->
After the tables have been created you need to configure a datasource for it in /apsadminweb configuration tab:

![Picture of datasource config gui.](http://download.natusoft.se/Images/APS/APS-Auth/APSSimpleUserServiceProvider/docs/images/DataSourceConfig.png)

Please note that the above picture is just an example. The data source name _APSSimpleUserServiceDS_ in this example should be configured in the _persistence/dsRefs_ config where you provide a name and a datasource reference. The service will be looking up the entry with that name, and use the specified datasource! For example:

        name: aps-admin-web
        dsRef: APSSimpleUserServiceDS

This example happens to be the default if no instances have been configured and is required if you want to use authentication for the APS admin web. You should probably define your own instance if you are going to use this service. The _dsRef_ part is exactly the same name as defined in the data source configuration (_persistence/datasources_).

The rest of the datasource entry in the picture above depends on your database and where it is running. Also note that the "(default)" after the field names in the above picture are the name of the currently selected configuration environment. This configuration is configuration environment specific. You can point out different database servers for different environments for example.

When the datasource is configured and saved then you can go to _"Configuration tab_,_Configurations/aps/adminweb"_ and enable the "requireauthentication" config. __If you do this before setting up the datasource and you have choosen to use the provided implementation of APSAuthService that uses APSSimpleUserService to login then you will be completely locked out!__

## Troubleshooting

If you have managed to lock yourself out of /apsadminweb as described above then I suggest editing the _APSFilesystemService root_/filesystems/se.natusoft.osgi.aps.core.config.service.APSConfigServiceProvider/apsconfig-se.natusoft.aps.adminweb-1.0.properties file and changeing the following line:

        se.natusoft.aps.adminweb_1.0_requireauthentication=true
        

to _false_ instead. Then restart the server. Also se the APSFilesystemService documentation for more information. The APSConfigService is using that service to store its configurations.

## JDBC Drivers

There is a catch with OSGi and its classpath isolation. The _APSSimpleUserService_ makes use of the _APSJPAService_ whose implementation _APSOpenJPAProvider_ cheats OSGi a bit by using _MultiBundleClassLoader_ (is available in aps-tools-library bundle) and merges the service classpath with the client classpath which is a requirement for the JPA framework to work (it needs access to both framework code in the service classpath and client entities in the client classpath). This also has the side effect that the client can provide a JDBC driver in its bundle. The _APSSimpleUserService_ do provide a JDBC driver for _Derby 10.9.1.0_.

Another catch with this is that users of _APSSimpleUserService_ are not part of this collective classpath and can thereby not make drivers available in their bundles, or at least not right off, there is however a workaround to this. There is a natsty way that you can pass on the client bundle class loader right through the _APSSimpleUserService_ to _APSJPAService_ by creating an instance of _MultiBundleClassLoader_ and set it as context classloader:

        MultiBundleClassLoader mbClassLoader = new MultiBundleClassLoader(bundleContext.getBundle());
        Thread.currentThread().setContextClassLoader(mbClassLoader);

Do this before the first call to _APSSimpleUserService_. The _APSJPAService_ will check if the current context class loader is a MultiBundleClassLoader and if so extract the bundles from it and add to its own MultiBundleClassLoader. This way you have extended the classpath that the JPA framework will se to 3 bundles: aps-openjpa-provider, aps-simple-user-service-provider, and your client bundle, which can then contain a JDBC driver.

The catches are unfortunately not over yet! You also need to configure your own instance of _APSSimpleUserService_ with its own data source in the configuration, and your client needs to add the name of this configuration to the tracker for the _APSSimpleUserService_ :

        APSServiceTracker<APSSimpleUserService> userServiceTracker = 
            new APSServiceTracker<>(bundleContext, APSSimpleUserService.class, "(instance=instName)", "30 seconds");

or

        @OSGiService(additionalSearchCriteria="(instance=instName)", timeout="30 seconds")
        APSSimpleUserService userService;

where _instName_ is whatever name you gave the instance in the configuration. Then try to have only one bundle call this service since each different bundle calling the service will extend the service classpath with that bundle!

## APIs

public _interface_ __APSSimpleUserService__   [se.natusoft.osgi.aps.api.auth.user] {

This is the API of a simple user service that provide basic user handling that will probably be enough in many cases, but not all.

Please note that this API does not declare any exceptions! In the case of an exception being needed the APSSimpleUserServiceException should be thrown. This is a runtime exception.

__public static final String AUTH_METHOD_PASSWORD = "password"__

Password authentication method for authenticateUser().

__public Role getRole(String roleId)__

Gets a role by its id.

_Returns_

> A Role object representing the role or null if role was not found.

_Parameters_

> _roleId_ - The id of the role to get. 

__public User getUser(String userId)__

Gets a user by its id.

_Returns_

> A User object representing the user or null if userId was not found.

_Parameters_

> _userId_ - The id of the user to get. 

__public boolean authenticateUser(User user, Object authentication, String authMethod)__

Authenticates a user using its user id and user provided authentication.

_Returns_

> true if authenticated, false otherwise. If true user.isAuthenticated() will also return true.

_Parameters_

> _user_ - The User object representing the user to authenticate. 

> _authentication_ - The user provided authentication data. For example if AuthMethod is AUTH\_METHOD\_PASSWORD 

> _authMethod_ - Specifies what authentication method is wanted. 

}

----

    

public _interface_ __APSSimpleUserServiceAdmin__ extends  APSSimpleUserService    [se.natusoft.osgi.aps.api.auth.user] {

Admin API for APSSimpleUserService.

__public RoleAdmin createRole(String name, String description)__

Creates a new role.

_Returns_

> a new Role object representing the role.

_Parameters_

> _name_ - The name of the role. This is also the key and cannot be changed. 

> _description_ - A description of the role. This can be updated afterwards. 

__public void updateRole(Role role)__

Updates a role.

_Parameters_

> _role_ - The role to update. 

__public void deleteRole(Role role)__

Deletes a role.

_Parameters_

> _role_ - The role to delete. This will likely fail if there are users still having this role! 

__public List<RoleAdmin> getRoles()__

Returns all available roles.

__public UserAdmin createUser(String id)__

Creates a new user. Please note that you get an empty user back. You probably want to add roles and also possibly properties to the user. After you have done that call _updateUser(user)_.

_Returns_

> A User object representing the new user.

_Parameters_

> _id_ - The id of the user. This is key so it must be unique. 

__public void updateUser(User user)__

Updates a user.

_Parameters_

> _user_ - The user to update. 

__public void deleteUser(User user)__

Deletes a user.

_Parameters_

> _user_ - The user to delete. 

__public List<UserAdmin> getUsers()__

Returns all users.

__public void setUserAuthentication(User user, String authentication)__

Sets authentication for the user.

_Parameters_

> _user_ - The user to set authentication for. 

> _authentication_ - The authentication to set. 

}

----

    

public _class_ __APSAuthMethodNotSupportedException__ extends  APSRuntimeException    [se.natusoft.osgi.aps.api.auth.user.exceptions] {

This is thrown by APSAuthService when the implementation does not support the selected auth method.

__public APSAuthMethodNotSupportedException(String message)__

Creates a new APSAuthMethodNotSupportedException instance.

_Parameters_

> _message_ - The exception message. 

__public APSAuthMethodNotSupportedException(String message, Throwable cause)__

Creates a new APSAuthMethodNotSupportedException instance.

_Parameters_

> _message_ - The exception message. 

> _cause_ - The exception that is the cause of this one. 

}

----

    

public _class_ __APSSimpleUserServiceException__ extends  APSRuntimeException    [se.natusoft.osgi.aps.api.auth.user.exceptions] {

Indicates a problem with the APSSimpleUserService.

__public APSSimpleUserServiceException(String message)__

Creates a new APSSimpleUserServiceException instance.

_Parameters_

> _message_ - The exception message. 

__public APSSimpleUserServiceException(String message, Throwable cause)__

Creates a new APSSimpleUserServiceException instance.

_Parameters_

> _message_ - The exception message. 

> _cause_ - The cause of the exception. 

}

----

    

public _interface_ __Role__ extends  Comparable<Role>    [se.natusoft.osgi.aps.api.auth.user.model] {

This defines a role.

__public String getId()__

_Returns_

> The id of the role.

__public String getDescription()__

_Returns_

> A description of the role.

__public boolean hasRole(String roleName)__

Returns true if the role has the specified sub role name.

_Parameters_

> _roleName_ - The name of the role to check for. 

__boolean isMasterRole()__

_Returns_

> true if this role is a master role. Only master roles can be added to users.

}

----

    

public _interface_ __RoleAdmin__ extends  Role    [se.natusoft.osgi.aps.api.auth.user.model] {

Provides update API for Role.

__public void setDescription(String description)__

Changes the description of the role.

_Parameters_

> _description_ - The new description. 

__public List<Role> getRoles()__

Returns all sub roles for this role.

__public void addRole(Role role)__

Adds a sub role to this role.

_Parameters_

> _role_ - The role to add. 

__public void removeRole(Role role)__

Removes a sub role from this role.

_Parameters_

> _role_ - The role to remove. 

__public void setMasterRole(boolean masterRole)__

Sets whether this is a master role or not.

_Parameters_

> _masterRole_ - true for master role. 

}

----

    

public _interface_ __User__ extends  Comparable<User>    [se.natusoft.osgi.aps.api.auth.user.model] {

This defines a User.

__public String getId()__

Returns the unique id of the user.

__public boolean isAuthenticated()__

Returns true if this user is authenticated.

__public boolean hasRole(String roleName)__

Returns true if the user has the specified role name.

_Parameters_

> _roleName_ - The name of the role to check for. 

__public Properties getUserProperties()__

This provides whatever extra information about the user you want. How to use this is upp to the user of the service. There are some constants in this class that provide potential keys for the user properties.

Please note that the returned properties are read only!

__public static final String USER_NAME = "name"__

Optional suggestion for user properties key.

__public static final String USER_PHONE = "phone"__

Optional suggestion for user properties key.

__public static final String USER_PHONE_WORK = "phone.work"__

Optional suggestion for user properties key.

__public static final String USER_PHONE_HOME = "phone.home"__

Optional suggestion for user properties key.

__public static final String USER_EMAIL = "email"__

Optional suggestion for user properties key.

}

----

    

public _interface_ __UserAdmin__ extends  User    [se.natusoft.osgi.aps.api.auth.user.model] {

Provides update API for the User.

__public List<Role> getRoles()__

Returns all roles for this user.

__public void addRole(Role role)__

Adds a role to this user.

_Parameters_

> _role_ - The role to add. 

__public void removeRole(Role role)__

Removes a role from this user.

_Parameters_

> _role_ - The role to remove. 

__public void addUserProperty(String key, String value)__

Adds a user property.

_Parameters_

> _key_ - The key of the property. 

> _value_ - The value of the property. 

__public void removeUserProperty(String key)__

Removes a user property.

_Parameters_

> _key_ - The key of the property to remove. 

__public void setUserProperties(Properties properties)__

Sets properties for the user.

To update the user properties either first do _getProperties()_, do your changes, and then call this method with the changed properties or just use the _addUserProperty()_ and _removeUserProperty()_ methods.

_Parameters_

> _properties_ - The properties to set. 

}

----

    

<!--
  
    This is a source file that will be generated into pdf, html and markdown complete with javadoc input
    and put directly under docs.
-->
# APSDataSource

This is a service that provides named data source definitions. It does __not__ provide pooled _javax.sql.DataSource_ instances! It only provides definitions with connection url, driver name, user and password. This service can be used by other services that provide DataSource pooling for example. The APSSimpleUserServiceProvider makes use of this service by looking up ”APSSimpleUserServiceDS” passing the information on to the APSJPAService in its properties. Not everything can make use of an _javax.sql.DataSource_, but everything can make use of the information provided by this service.

The actual data source definitions are configured in the _/apsadminweb_ under configuration group ”persistence”.

## APIs

The complete APS javadoc can be found at [http://apidoc.natusoft.se/APS/](http://apidoc.natusoft.se/APS/).

public _interface_ __DataSourceDef__   [se.natusoft.osgi.aps.api.data.jdbc.model] {

This represents information required for setting upp a JDBC data source.

__String getName()__

_Returns_

> The name of this data source definition. This information is optional and can return null!

__String getConnectionURL()__

_Returns_

> The JDBC connection URL. Ex: jdbc:provider://host:port/database[;properties].

__String getConnectionDriveName()__

_Returns_

> The fully qualified class name of the JDBC driver to use.

__String getConnectionUserName()__

_Returns_

> The name of the database user to login as.

__String getConnectionPassword()__

_Returns_

> The password for the database user.

}

----

    

public _interface_ __APSDataSourceDefService__   [se.natusoft.osgi.aps.api.data.jdbc.service] {

This service provides lookup of configured data source definitions. These can be used to setup connection pools, JPA, ...

__DataSourceDef lookupByName(String name)__

Looks up a data source definition by its configured name.

_Returns_

> A DataSourceDef or null if name was not valid.

_Parameters_

> _name_ - The name to lookup. 

__List<DataSourceDef> getAllDefinitions()__

_Returns_

> All available definitions.

}

----

    

# APSJPAService

This provides JPA to services and applications. It has a slightly more OSGi friendly API than the org.osgi.service.jpa.EntityManagerFactoryBuilder. The APSOpenJPAProvider however returns an APSJPAService instance that also implements EntityManagerFactoryBuilder. For some reason I haven’t figured out yet, it cannot be registered as a service with the EntityManagerFactoryBuilder interface! The bundle fails to deploy if that is done.

The provided service is using OpenJPA. The service works partly as an extender inspecting deployed bundles for a META-INF/persistence.xml file. When found this is read and some setup is done already there. The persistenceUnitName from the persistence.xml file is used to connect the client later with its configuration. When a JPA using bundle is shut down its JPA setup is automatically cleaned.

Here is an example of usage:

            private APSJPAEntityManagerProvider emp = null;
            ...
            private APSJPAEntityManagerProvider getEMP() {
                if (this.emp == null || !this.emp.isValid()) {
                    DataSourceDef dsDef = this.dataSourceDefService.lookupByName(”MyDS");
                    if (dsDef == null) {
                        throw new SomeException("Could not find an ’MyDs’ in 'persistence/datasources' configuration!");
                    }
                    Map<String, String> props = new HashMap<String, String>();
                    props.put("javax.persistence.jdbc.user", dsDef.getConnectionUserName());
                    props.put("javax.persistence.jdbc.password", dsDef.getConnectionPassword());
                    props.put("javax.persistence.jdbc.url", dsDef.getConnectionURL());
                    props.put("javax.persistence.jdbc.driver", dsDef.getConnectionDriveName());
                    this.emp = this.jpaService.initialize(this.bundleContext, ”myPersistenceUnitName”, props);
                }
                return this.emp;
            }
            ...
            EntityManager em = getEMP().createEntityManager();
            em.getTransaction().begin();
        
            try {
                RoleEntity role = new RoleEntity(id);
                role.setDescription(description);
                em.persist(role);
                em.getTransaction().commit();
            }
            catch (RuntimeException re) {
                em.getTransaction().rollback();
                throw re;
            }
            finally {
                em.close();
            }
                

This code example handles the APSJPAService having been restared or redeployed. When `emp.isValid()` returns false then all you need to do is to call `jpaService.initialize(...)` again. The rest is just POJPA (Plain Old JPA :-)).

## APIs

public _interface_ __APSJPAService__   [se.natusoft.osgi.aps.api.data.jpa.service] {

This service allows an JPA _EntityManager_ to be gotten for a persistent unit name.

So why is this done this way ? Why is not an _EntityManagerFactory_ returned?

The answer to that is that the _EntityManagerFactory_ is internal to the service who is responsible for creating it and for closing it at sometime (stopping of bundle). The client only needs an _EntityManager_ for which the client is responsible after its creation.

The creation of the _EntityManagerFactory_ is delayed until the call to _initialize(...)_. Creating the EMF along with the persistence provider at persistence bundle discovery would limit database connection properties to the persistence.xml file which is less than optimal to put it mildly. This way a client can make use of the _APSDataSourceDefService_ to get the JDBC properties which it can pass along to this service.

The default provider implementation of this service uses OpenJPA which provides its own connection pooling.

__APSJPAEntityManagerProvider initialize(BundleContext bundleContext, String persistenceUnitName, Map<String, String> props) throws APSResourceNotFoundException__

Initializes and returns a provider from the specified properties.

_Returns_

> A configured EntityManager.

_Parameters_

> _bundleContext_ - The context of the client bundle. It is used to locate its persistence provider. 

> _persistenceUnitName_ - The name of the persistent unit defined in persistence.xml. 

> _props_ - Custom properties to configure database, etc. 

public _static_ _interface_ __APSJPAEntityManagerProvider__   [se.natusoft.osgi.aps.api.data.jpa.service] {

Once you get this it is valid until the _APSJPAService_ is stopped (which will happen if the service is redeployed!).

__public boolean isValid()__

Returns true if this instance is valid. If not call APSJPAService.initialize(...) again to get a new instance. It will be invalid if the APSJPAService provider have been restarted.

__EntityManager createEntityManager()__

Creates a new _EntityManager_. You are responsible for closing it!

Please note that the _EntityManager_ caches all referenced entities. If you keep and reuse it for a longer time it can use more memory. For example at [http://docs.jboss.org/ejb3/app-server/tutorial/extended_pc/extended.html](http://docs.jboss.org/ejb3/app-server/tutorial/extended_pc/extended.html) it says that "Usually, an _EntityManager_ in JBoss EJB 3.0 lives and dies within a JTA transaction". This indicates how long-lived the _EntityManager_ should preferably be.

_Returns_

> A configured EntityManager.

__EntityManagerFactory getEntityManagerFactory()__

Returns the underlying _EntityManagerFactory_. This will return null if isValid() return false!

Be very careful what you do with this! It is managed by this service!

}

----

    

# APSJSONService

This provides exactly the same functionallity as APSJSONLib. It actually wraps the library as a service. The reason for that is that I wanted to be able to redeploy the library without forcing a redeploy of the Bunde using it. A redeploy of the library will force a redeploy of this service, but not the client of this service. The APS clients of this service uses APSServiceTracker wrapped as a service and thus handles this service leaving and returning without having to care about it.

This service and the library existrs for internal use. It is here and can be used by anyone, but in most cases like serializing java beans back and forth to JSON (which this can do) Jacksson would still be a better choice and offers more flexibility. In the long run I’m going to see if I can replace the internal use of this with Jacksson as well.

# APSResolvingBundleDeployer

This is a bundle deployer that is intended as an alternative to the server provided deployer.

This bundle deployer will try to automatically resolve deploy dependencies. It does this by having a fail threshold. If the deploy of a bundle fails it just keeps quiet and put the bundle at the end of the list of bundles to deploy. It updates the try count for the bundle however. Next time the bundle is up for deploy it might have the dependencies it needs and will deploy. If not it goes back to the end of the list again and its retry count is incremented again. This repeats until the retry count reaches the threshold value in which case an error is logged and the bundle will not be attempted to be deployed again unless it gets a new timestamp on disk.

Glassfish does something similar, but Virgo fails completely unless bundles are deployed in the correct order. You have to provide a par file for Virgo to deploy correctly.

There is one catch to using this deployer: It does not handle WAB bundles! Neither Glassfish nor Virgo seems to handle WAB deployment using the OSGi extender pattern. If they did they would recognize a WAB being deployed even though it is deployed by this deployer and handle it. They dont!

## Configuration

The following configuration is available for this deployer. Edit this in /apsadminweb ”Configurations” tab under the _aps_ node.

__deployDirectory__ - The directory to deploy bundles from. All bundles in this directory will be attempted to be deployed.

__failThreshold__ - The number of failed deploys before giving upp. The more bundles and the more dependencies among them the higher the value should be. The default value is 8.

# APSSessionService

This service provides session storage functionality. You can create a session, get an existing session by its id, and close a session. Each session can hold any number of named objects.

Why a session service ? To begin with, this is not an HttpSession! That said, it was created to handle a single session among several web applications. This for the APS administration web which are made up of several web applications working toghether. This is explained in detail in the APSAdminWeb documentation.

## APIs

public _interface_ __APSSession__   [se.natusoft.osgi.aps.api.misc.session] {

This represents an active session.

__String getId()__

_Returns_

> The id of this session.

__boolean isValid()__

_Returns_

> true if this session is still valid.

__void saveObject(String name, Object object)__

Saves an object in the session. Will do nothing if the session is no longer valid.

_Parameters_

> _name_ - The name to store the object under. 

> _object_ - An object to store in the session. 

__Object retrieveObject(String name)__

Returns a object stored under the specified name or null if no object is stored under that name.

If isValid() returns false then this will always return null.

_Parameters_

> _name_ - The name of the object to get. 

}

----

    

public _interface_ __APSSessionService__   [se.natusoft.osgi.aps.api.misc.session] {

This is not a http session! It is a simple session that can be used by any code running in the same OSGi server.











__APSSession createSession(int timeoutInMinutes)__

Creates a new session.

_Parameters_

> _timeoutInMinutes_ - The timeout in minutes. 

__APSSession createSession(String sessionId, int timeoutInMinutes)__

Creates a new session.

The idea behind this variant is to support distributed sessions. The implementation must use a session id that is unique enough to support this. The APS implementation uses java.util.UUID.

_Parameters_

> _sessionId_ - The id of the session to create. 

> _timeoutInMinutes_ - The timeout in minutes. 

__APSSession getSession(String sessionId)__

Looks up an existing session by its id.

_Returns_

> A valid session having the specified id or null.

_Parameters_

> _sessionId_ - The id of the session to lookup. 

__void closeSession(String sessionId)__

Closes the session represented by the specified id. After this call APSSession.isValid() on an _APSSession_ representing this session will return false.

_Parameters_

> _sessionId_ - The id of the session to close. 

}

----

    

# APSDiscoveryService

This is actually a service directory that also multicasts on the network to find other instances of itself and makes the services in other instances available also.

A _service_ here means anything that can be called either with an URL or a host and port. It does not specifically indicate what type of service it is. Each published service however has an id that can be used to better identify it. Basically clients of services have to know the id of the service they want and by that know what it is and how to talk to it.

## APIs

public _class_ __APSDiscoveryPublishException__ extends  APSRuntimeException    [se.natusoft.osgi.aps.api.net.discovery.exception] {

Thrown on service publish problems.

__public APSDiscoveryPublishException(String message)__

Creates a new _APSDiscoveryPublishException_ instance.

_Parameters_

> _message_ - The exception message. 

__public APSDiscoveryPublishException(String message, Throwable cause)__

Creates a new _APSDiscoveryPublishException_ instance.

_Parameters_

> _message_ - The exception message. 

> _cause_ - The cause of this exception. 

}

----

    

public _interface_ __ServiceDescription__   [se.natusoft.osgi.aps.api.net.discovery.model] {

Describes a service.

__String getDescription()__

A short description of the service.

__String getServiceId()__

An id/name of the service.

__String getVersion()__

The version of the service.

__String getServiceHost()__

The targetHost of the service.

__int getServicePort()__

The targetPort of the service.

__String getServiceURL()__

An optional URL to the service.

}

----

    

public _class_ __ServiceDescriptionProvider__ implements  ServiceDescription    [se.natusoft.osgi.aps.api.net.discovery.model] {

Describes a service.













__public ServiceDescriptionProvider()__

Creates a new ServiceDescirption.

__public String toString()__

Returns a string representation of this object.

__public String getDescription()__

A short description of the service.

__public void setDescription(String description)__

Sets a short description of the service.

_Parameters_

> _description_ - The description to set. 



__public void setServiceId(String serviceId)__

Sets the id of the service.

_Parameters_

> _serviceId_ - The service id to set. 



__public void setVersion(String version)__

Sets the version of the service.

_Parameters_

> _version_ - The version to set. 



__public void setServiceHost(String serviceHost)__

Sets the targetHost of the service.

_Parameters_

> _serviceHost_ - The service targetHost to set. 



__public void setServicePort(int servicePort)__

Sets the targetPort of the service.

_Parameters_

> _servicePort_ - The service targetPort to set. 



____

Sets an url to the service.

_Parameters_

> _serviceURL_ - The service url to set. 





}

----

    

public _interface_ __APSSimpleDiscoveryService__   [se.natusoft.osgi.aps.api.net.discovery.service] {

A network service discovery.

__public List<ServiceDescription> getRemotelyDiscoveredServices()__

Returns all remotely discovered services.

__public List<ServiceDescription> getLocallyRegisteredServices()__

Returns the locally registered services.

__public List<ServiceDescription> getAllServices()__

Returns all known services, both locally registered and remotely discovered.

__public List<ServiceDescription> getService(String serviceId, String version)__

Returns all discovered services with the specified id.

_Parameters_

> _serviceId_ - The id of the service to get. 

> _version_ - The version of the service to get. 

__public void publishService(ServiceDescription service) throws APSDiscoveryPublishException__

Publishes a local service. This will announce it to other known APSSimpleDiscoveryService instances.

_Parameters_

> _service_ - The description of the servcie to publish. 

_Throws_

> _APSDiscoveryPublishException_ - on problems to publish (note: this is a runtime exception!). 

__public void unpublishService(ServiceDescription service) throws APSDiscoveryPublishException__

Recalls the locally published service, announcing to other known APSSimpleDiscoveryService instances that this service is no longer available.

_Parameters_

> _service_ - The service to unpublish. 

_Throws_

> _APSDiscoveryPublishException_ - on problems to publish (note: this is a runtime exception!). 

}

----

    

# APSExternalProtocolExtender

This is an OSGi bundle that makes use of the OSGi extender pattern. It listens to services being registered and unregistered and if the services bundles _MANIFEST.MF_ contains `APS-Externalizable: true` the service is made externally available. If the _MANIFEST.MF_ contains `APS-Externalizable: false` however making the service externally available is forbidden. A specific service can also be registered containing an _aps-externalizable_ property with value _true_ to be externalizable. This overrides any other specification.

The exernal protocol extender also provides a configuration where services can be specified with their fully qualified name to be made externally available. If a bundle however have specifically specified false for the above manifest entry then the config entry will be ignored.

So, what is meant by _made externally available_ ? Well what this bundle does is to analyze with reflection all services that are in one way or the other specified as being externalizable (manifest or config) and for all callable methods of the service an _APSExternallyCallable_ object will be created and saved locally with the service name. _APSExternallyCallable_ extends _java.util.concurrent.Callable_, and adds the possibility to add parameters to calls and also provides meta data for the service method, and the bundle it belongs to. There is also an _APSRESTCallable_ that extends _APSExternallyCallable_ and also takes an http method and maps that to a appropriate service method.

## The overall structure

The complete picture for making services externally callable looks like this:

![EPERelations.png](http://download.natusoft.se/Images/APS/APS-Network/APSExternalProtocolExtender/docs/images/EPERelations.png)

This bundle provides the glue between the services and the protocols. Transports and protocols have to be provided by other bundles.

The flow is like this:

1. Transport gets some request and an InputStream.

2. Transport gets some user selected protocol (The APSExtProtocolHTTPTransportProvider allows specification of both protocol, protocol version, and service to call in the URL).

3. Transport calls _APSExternalProtocolService_ to get requested protocol.

4. Transport calls protocol to parse InputStream and it returns an _RPCRequest_.

5. Transport uses the information in the RPCRequest to call a service using _APSExternalProtocolService_.

6. Transport takes the result from the call and passes to the protocol along with an OutputStream to write response on.

## APSExternalProtocolService

This bundle registers an _APSExternalProtocolService_ that will provide all _APSExternallyCallable_ instances (or rather copies of them since you can modify the one you get back by providing arguments). This service also provides getters for available remote protocols and you can register with it to receive information about chages for services and protocols.

### Protocols

There is a base API for protocols: _RPCProtocol_. APIs for different types of protocols should extend this. The protocol type APIs are service APIs and services implementing them must be provided by other bundles. This bundle looks for and keeps track of all such service providers.

The _StreamedRPCProtocol_ extends _RPCProtocol_ and provides a method for parsing a request from an _InputStream_ returning an _RPCRequest_ object. This request object contains the name of the service, the method, and the parameters. This is enough for using _APSExternalProtocolService_ to do a call to the service. The request object is also used to write the call response on an OutputStream. There is also a method to write an error response.

It is the responsibility of the transport provider to use a protocol to read and write requests and responses and to use the request information to call a service method. An exception is the case of http transports supporting REST that must take the responibility for returning an http status.

### Getting information about services and protocols.

A transport provider can register themselves with the _APSExternalProtocolService_ by implementing the _APSExternalProtocolListener_ interface. They will then be notified when a new externalizable service becomes available or is leaving and when a protocol becomes available or is leaving.

## WARNING - Non backwards compatible changes!

This version have non backwards compatible changes! _StreamedRPCProtocol_ have changed in parameters for _parseRequest(...)_ and _isRest()_ is gone. _RPCProtocol_ have changes in parameters for crateRPCError(...). The error code is now gone. These changes was a necessity! The old was really bad and tried to solve REST support in a very stupid way. It is now handled very much more elegantly without any special support for it with _is_methods!

The _APSExtProtocolHTTPTransportProvider_ now checks if an _RPCError_ (returned by createRPCError(...)) object actually is an _HTTPError_ subclass providing an HTTP error code to return.

_parseRequest(...)_ parameters now also contain the class of the service and a new RequestIntention enum. The service class is only for inspecting methods for annoations or other possible meta data. The JSONREST protocol for example uses this to find annotations indicating GET, PUT, DELETE, etc methods, which is far more flexible than the old solution of requiring a get(), put(), etc method. The RequestIntention enum provides the following values: CREATE, READ, UPDATE, DELETE, UNKNOWN. That is CRUD + UNKNOWN. It will be UNKNOWN if the transport cannot determine such information. These are basically to support REST protocols without being too HTTP specific. Other transports can possible also make use of them.

## See also

_APSExtProtocolHTTPTransportProvider_ - Provides a HTTP transport.

_APSStreamedJSONRPCProtocolProvider_ - Provides version 1.0 and 2.0 of JSONRPC, JSONHTTP and JSONREST.

## APIs

public _interface_ __APSExternalProtocolService__   [se.natusoft.osgi.aps.api.external.extprotocolsvc] {

This service makes the currently available externalizable services available for calling. It should be used by a bundle providing an externally available way of calling a service (JSON over http for example) to translate and forward calls to the local service. The locally called service is not required to be aware that it is called externally.

__Never cache any result of this service!__ Always make a new call to get the current state. Also note that it is possible that the service represented by an APSExternallyCallable have gone away after it was returned, but before you do call() on it! In that case an APSNoServiceAvailableException will be thrown. Note that you can register as an APSExternalProtocolListener to receive notifications about externalizable services coming and going, and also protocols coming and going to keep up to date with the current state of things.

__public Set<String> getAvailableServices()__

Returns all currently available services.

__public List<APSExternallyCallable> getCallables(String serviceName) throws RuntimeException__

Returns all APSExternallyCallable for the named service object.

_Parameters_

> _serviceName_ - The name of the service to get callables for. 

_Throws_

> _RuntimeException_ - If the service is not available. 

__public Set<String> getAvailableServiceFunctionNames(String serviceName)__

Returns the names of all available functions of the specified service.

_Parameters_

> _serviceName_ - The service to get functions for. 

__public APSExternallyCallable getCallable(String serviceName, String serviceFunctionName)__

Gets an APSExternallyCallable for a specified service name and service function name.

_Returns_

> An APSExternallyCallable instance or null if the combination of service and serviceFunction is not available.

_Parameters_

> _serviceName_ - The name of the service object to get callable for. 

> _serviceFunctionName_ - The name of the service function of the service object to get callable for. 

__public List<RPCProtocol> getAllProtocols()__

_Returns_

> All currently deployed providers of RPCProtocol.

__public RPCProtocol getProtocolByNameAndVersion(String name, String version)__

Returns an RPCProtocol provider by protocol name and version.

_Returns_

> Any matching protocol or null if nothing matches.

_Parameters_

> _name_ - The name of the protocol to get. 

> _version_ - The version of the protocol to get. 

__public List<StreamedRPCProtocol> getAllStreamedProtocols()__

_Returns_

> All currently deployed providers of StreamedRPCProtocol.

__public StreamedRPCProtocol getStreamedProtocolByNameAndVersion(String name, String version)__

Returns a StreamedRPCProtocol provider by protocol name and version.

_Returns_

> Any matching protocol or null if nothing matches.

_Parameters_

> _name_ - The name of the streamed protocol to get. 

> _version_ - The version of the streamed protocol to get. 

__public void addExternalProtocolListener(APSExternalProtocolListener externalServiceListener)__

Add a listener for externally available services.

_Parameters_

> _externalServiceListener_ - The listener to add. 

__public void removeExternalProtocolListener(APSExternalProtocolListener externalServiceListener)__

Removes a listener for externally available services.

_Parameters_

> _externalServiceListener_ - The listener to remove. 

}

----

    

public _interface_ __APSExternallyCallable<ReturnType>__ extends  Callable<ReturnType>    [se.natusoft.osgi.aps.api.external.extprotocolsvc.model] {

This API represents one callable service method.

__public String getServiceName()__

_Returns_

> The name of the service this callable is part of.

__public String getServiceFunctionName()__

_Returns_

> The name of the service function this callable represents.

__public DataTypeDescription getReturnDataDescription()__

_Returns_

> A description of the return type.

__public List<ParameterDataTypeDescription> getParameterDataDescriptions()__

_Returns_

> A description of each parameter type.

__public Bundle getServiceBundle()__

_Returns_

> The bundle the service belongs to.

__public Class getServiceClass()__

Returns the class of the service implementation.

__ReturnType call(Object... arguments) throws Exception__

Calls the service method represented by this APSExternallyCallable.

_Returns_

> The return value of the method call if any or null otherwise.

_Parameters_

> _arguments_ - Possible arguments to the call. 

_Throws_

> _Exception_ - Any exception the called service method threw. 

}

----

    

public _interface_ __APSExternalProtocolListener__   [se.natusoft.osgi.aps.api.external.extprotocolsvc.model] {

A listener for externally available services. Please note that this means that the service is available for potential external protocol exposure! For it to be truly available there also has to be a protocol and transport available. It is probably only transports that are interested in this information!

__public void externalServiceAvailable(String service, String version)__

This gets called when a new externally available service becomes available.

_Parameters_

> _service_ - The fully qualified name of the newly available service. 

> _version_ - The version of the service. 

__public void externalServiceLeaving(String service, String version)__

This gets called when an externally available service no longer is available.

_Parameters_

> _service_ - The fully qualified name of the service leaving. 

> _version_ - The version of the service. 

__public void protocolAvailable(String protocolName, String protocolVersion)__

This gets called when a new protocol becomes available.

_Parameters_

> _protocolName_ - The name of the protocol. 

> _protocolVersion_ - The version of the protocol. 

__public void protocolLeaving(String protocolName, String protocolVersion)__

This gets called when a new protocol is leaving.

_Parameters_

> _protocolName_ - The name of the protocol. 

> _protocolVersion_ - The version of the protocol. 

}

----

    

public _interface_ __APSRESTCallable__ extends  APSExternallyCallable    [se.natusoft.osgi.aps.api.external.extprotocolsvc.model] {

This is a special variant of APSExternallyCallable that supports a HTTP REST call.

This is only available when a service have zero or one method whose name starts with put, zero or one method whose name starts with post, and so on. There has to be at least one method of put, post, get or delete.

APSExternalProtocolService can provide an instance of this is a service matches the criteria.

This is only of use for HTTP transports! aps-ext-protocol-http-transport-provider does make use of this for protocols that indicate they support REST.

__public boolean supportsPut()__

_Returns_

> true if the service supports the PUT method.

__public boolean supportsPost()__

_Returns_

> true if the service supports the POST method.

__public boolean supportsGet()__

_Returns_

> true if the service supports the GET method.

__public boolean supportsDelete()__

_Returns_

> true if the service supports the DELETE method.

__public void selectMethod(HttpMethod method)__

This selects the method to call with this callable.

_Parameters_

> _method_ - The selected method to call. 

public _static_ _enum_ __HttpMethod__   [se.natusoft.osgi.aps.api.external.extprotocolsvc.model] {

This defines the valid choices for selectMethod(...).

}

----

    



__APSRESTCallable.HttpMethod httpMethod() default APSRESTCallable.HttpMethod.NONE__

This needs to be provided if you are providing a REST API using JSONREST protocol of the APSStreamedJSONRPCProtocolProvider bundle.

}

----

    

public _class_ __APSRESTException__ extends  APSRuntimeException    [se.natusoft.osgi.aps.api.net.rpc.errors] {

This is a special exception that services can throw if they are intended to be available as REST services through the aps-external-protocol-extender + aps-ext-protocol-http-transport-provider. This allows for better control over status codes returned by the service call.



__public APSRESTException(int httpStatusCode)__

Creates a new _APSRESTException_.

_Parameters_

> _httpStatusCode_ - The http status code to return. 

__public APSRESTException(int httpStatusCode, String message)__

Creates a new _APSRESTException_.

_Parameters_

> _httpStatusCode_ - The http status code to return. 

> _message_ - An error message. 

__public int getHttpStatusCode()__

Returns the http status code.

}

----

    

public _enum_ __ErrorType__   [se.natusoft.osgi.aps.api.net.rpc.errors] {

This defines what I think is a rather well though through set of error types applicable for an RPC call. No they are not mine, they come from Matt Morley in his JSONRPC 2.0 specification at [http://jsonrpc.org/spec.html](http://jsonrpc.org/spec.html).

I did however add the following:

* SERVICE_NOT_FOUND - Simply because this can happen in this case!

* AUTHORIZATION_REQUIRED - This is also a clear possibility.

* BAD_AUTHORIZATION

__PARSE_ERROR__

Invalid input was received by the server. An error occurred on the server while parsing request data.

__INVALID_REQUEST__

The request data sent is not a valid.

__METHOD_NOT_FOUND__

The called method does not exist / is not available.

__SERVICE_NOT_FOUND__

The called service does not exist / is not available.

__INVALID_PARAMS__

The parameters to the method are invalid.

__INTERNAL_ERROR__

Internal protocol error.

__SERVER_ERROR__

Server related error.

__AUTHORIZATION_REQUIRED__

Authorization is required, but none was supplied.

__BAD_AUTHORIZATION;__

Bad authorization was supplied.

}

----

    

public _interface_ __HTTPError__ extends  RPCError    [se.natusoft.osgi.aps.api.net.rpc.errors] {

Extends _RPCError_ with an HTTP status code. HTTP transports can make use of this information.

__public int getHttpStatusCode()__

_Returns_

> Returns an http status code.

}

----

    

public _interface_ __RPCError__   [se.natusoft.osgi.aps.api.net.rpc.errors] {

This represents an error in servicing an RPC request.

__public ErrorType getErrorType()__

The type of the error.

__public String getErrorCode()__

A potential error code.

__public String getMessage()__

Returns an error message. This is also optional.

__public boolean hasOptionalData()__

True if there is optional data available. An example of optional data would be a stack trace for example.

__public String getOptionalData()__

The optional data.

}

----

    

public _class_ __RequestedParamNotAvailableException__ extends  APSException    [se.natusoft.osgi.aps.api.net.rpc.exceptions] {

This exception is thrown when a parameter request cannot be fulfilled.

__public RequestedParamNotAvailableException(String message)__

Creates a new _RequestedParamNotAvailableException_ instance.

_Parameters_

> _message_ - The exception message. 

__public RequestedParamNotAvailableException(String message, Throwable cause)__

Creates a new _RequestedParamNotAvailableException_ instance.

_Parameters_

> _message_ - The exception message. 

> _cause_ - The cause of this exception. 

}

----

    

public _abstract_ _class_ __AbstractRPCRequest__ implements  RPCRequest    [se.natusoft.osgi.aps.api.net.rpc.model] {

This provides a partial implementation of RPCRequest.













__public AbstractRPCRequest(String method)__

Creates a new AbstractRPCRequest.

_Parameters_

> _method_ - The method to call. 

__public AbstractRPCRequest(RPCError error)__

Creates a new AbstractRPCRequest.

_Parameters_

> _error_ - An RPCError indicating a request problem, most probably of ErrorType.PARSE_ERROR type. 

__public AbstractRPCRequest(String method, Object callId)__

Creates a new AbstractRPCRequest.

_Parameters_

> _method_ - The method to call. 

> _callId_ - The callId of the call. 

__protected Map<String, Object> getNamedParameters()__

_Returns_

> The named parameters.

__protected List<Object> getParameters()__

_Returns_

> The sequential parameters.







__public void setServiceQName(String serviceQName)__

Sets the fully qualified name of the service to call. This is optional since not all protocol delivers a service name this way.

_Parameters_

> _serviceQName_ - The service name to set. 









__public void addParameter(Object parameter)__

Adds a parameter. This is mutually exclusive with addParameter(name, parameter)!

_Parameters_

> _parameter_ - The parameter to add. 

}

----

    

public _enum_ __RequestIntention__   [se.natusoft.osgi.aps.api.net.rpc.model] {

The intention of a request.

}

----

    

public _interface_ __RPCRequest__   [se.natusoft.osgi.aps.api.net.rpc.model] {

This represents a request returned by protocol implementations.

__boolean isValid()__

Returns true if this request is valid. If this returns false all information except _getError()_ is __invalid__, and _getError()_ should return a valid _RPCError_ object.

__RPCError getError()__

Returns an _RPCError_ object if `isValid() == false`, _null_ otherwise.

__String getServiceQName()__

Returns a fully qualified name of service to call. This will be null for protocols where service name is not provided this way. So this cannot be taken for given!

__String getMethod()__

Returns the method to call. This can return _null_ if the method is provided by other means, for example a REST protocol where it will be part of the URL.

__boolean hasCallId()__

Returns true if there is a call id available in the request.

A call id is something that is received with a request and passed back with the response to the request. Some RPC implementations will require this and some wont.

__Object getCallId()__

Returns the method call call Id.

A call id is something that is received with a request and passed back with the response to the request. Some RPC implementations will require this and some wont.

__int getNumberOfParameters()__

Return the number of parameters available.

__<T> T getIndexedParameter(int index, Class<T> paramClass) throws RequestedParamNotAvailableException__

Returns the parameter at the specified index.

_Returns_

> The parameter object or null if indexed parameters cannot be delivered.

_Parameters_

> _index_ - The index of the parameter to get. 

> _paramClass_ - The expected class of the parameter. 

_Throws_

> _RequestedParamNotAvailableException_ - if requested parameter is not available. 

}

----

    

public _interface_ __RPCProtocol__   [se.natusoft.osgi.aps.api.net.rpc.service] {

This represents an RPC protocol provider. This API is not enough in itself, it is a common base for different protocols.

__String getServiceProtocolName()__

_Returns_

> The name of the provided protocol.

__String getServiceProtocolVersion()__

_Returns_

> The version of the implemented protocol.

__String getRequestContentType()__

_Returns_

> The expected content type of a request. This should be verified by the transport if it has content type availability.

__String getResponseContentType()__

_Returns_

> The content type of the response for when such can be provided.

__String getRPCProtocolDescription()__

_Returns_

> A short description of the provided service. This should be in plain text.

__RPCError createRPCError(ErrorType errorType, String message, String optionalData, Throwable cause)__

Factory method to create an error object.

_Returns_

> An RPCError implementation or null if not handled by the protocol implementation.

_Parameters_

> _errorType_ - The type of the error. 

> _message_ - An error message. 

> _optionalData_ - Whatever optional data you want to pass along or null. 

> _cause_ - The cause of the error. 

}

----

    

public _interface_ __StreamedRPCProtocol__ extends  RPCProtocol    [se.natusoft.osgi.aps.api.net.rpc.service] {

This represents an RPC protocol provider that provide client/service calls with requests read from an InputStream or having parameters passes as strings and responses written to an OutputStream.

HTTP transports can support both _parseRequests(...)_ and _parseRequest(...)_ while other transports probably can handle only _parseRequests(...)_. __A protocol provider can return null for either of these!__ Most protocol providers will support _parseRequests(...)_ and some also _parseRequest(...)_.

__List<RPCRequest> parseRequests(String serviceQName, Class serviceClass, String method, InputStream requestStream, RequestIntention requestIntention) throws IOException__

Parses a request from the provided InputStream and returns 1 or more RPCRequest objects.

_Returns_

> The parsed requests.

_Parameters_

> _serviceQName_ - A fully qualified name to the service to call. This can be null if service name is provided on the stream. 

> _serviceClass_ - The class of the service to call. Intended for looking for method annotations! Don't try to be "smart" here! 

> _method_ - The method to call. This can be null if method name is provided on the stream. 

> _requestStream_ - The stream to parse request from. 

> _requestIntention_ - The intention of the request (CRUD + UNKNOWN). 

_Throws_

> _IOException_ - on IO failure. 

__RPCRequest parseRequest(String serviceQName, Class serviceClass, String method, Map<String, String> parameters, RequestIntention requestIntention) throws IOException__

Provides an RPCRequest based on in-parameters. This variant supports HTTP transports.

Return null for this if the protocol does not support this!

_Returns_

> The parsed requests.

_Parameters_

> _serviceQName_ - A fully qualified name to the service to call. This can be null if service name is provided on the stream. 

> _serviceClass_ - The class of the service to call. Intended for looking for method annotations! Don't try to be "smart" here! 

> _method_ - The method to call. This can be null if method name is provided on the stream. 

> _parameters_ - parameters passed as a 

> _requestIntention_ - The intention of the request (CRUD + UNKNOWN). 

_Throws_

> _IOException_ - on IO failure. 

__void writeResponse(Object result, RPCRequest request, OutputStream responseStream) throws IOException__

Writes a successful response to the specified OutputStream.

_Parameters_

> _result_ - The resulting object of the RPC call or null if void return. If is possible a non void method also returns null! 

> _request_ - The request this is a response to. 

> _responseStream_ - The OutputStream to write the response to. 

_Throws_

> _IOException_ - on IO failure. 

__boolean writeErrorResponse(RPCError error, RPCRequest request, OutputStream responseStream) throws IOException__

Writes an error response.

_Returns_

> true if this call was handled and an error response was written. It returns false otherwise.

_Parameters_

> _error_ - The error to pass back. 

> _request_ - The request that this is a response to. 

> _responseStream_ - The OutputStream to write the response to. 

_Throws_

> _IOException_ - on IO failure. 

}

----

    

# APSExtProtocolHTTPTransportProvider

This provides an http transport for simple remote requests to OSGi services that have "APS-Externalizable: true" in their META-INF/MANIFEST.MF. This follows the OSGi extender pattern and makes any registered OSGi services of bundles having the above manifest entry available for remote calls over HTTP. This transport makes use of the aps-external-protocol-extender which exposes services with the above mentioned manifest entry with each service method available as an APSExternallyCallable.The aps-ext-protocol-http-transport-provider for example acts as a mediator between the protocol implementations and aps-external-protocol-extender for requests over HTTP.

Please note that depending on protocol not every service method will be callable. It depends on its arguments and return value. It mostly depends on how well the protocol handles types and can convert between the caller and the service.

This does not provide any protocol, only transport! For services to be able to be called at least one protocol is needed. Protocols are provided by providing an implementation of se.natusoft.osgi.aps.api.net.rpc.service.StreamedRPCProtocol and registering it as an OSGi service. The StreamedRPCProtocol API provides a protocol name and protocol version getter which is used to identify it. A call to an RPC service looks like this:

&nbsp; &nbsp; &nbsp; &nbsp;http://host:port/apsrpc/_protocol_/_version_[/_service_][/_method_]

_protocol_ - This is the name of the protocol to use. An implementation of that protocol must of course be available for this to work. If it isn't you will get a 404 back! The protocol service (RPCProtocol<-StreamedRPCProtocol) provides a name for each protocol. It is this name that is referenced.

_version_ - This is the version of the protocol. If this doesn't match any protocols available you will also get a 404 back.

_service_ - This is the service to call. Depending on the protocol you might not need this. But for protocols that only provide method in the stream data like JSONRPC for example, then this is needed. When provided it has to be a fully qualified service interface class name.

_method_ - This is the method to call. The need for this also depends on the protocol. A REST protocol would need it. The JSONRPC protocol does not. When this is specified in the URL then it will be used even if the protocol provides the method in the request! Please note that a method can be specified on two ways:

* method(type,...)

* method

The method will be resolved in that order. The parameter type specifying version is required when there are several methods with the same name but different parameters. The method name only will give you the last one in that case.

## Examples

See examples under the __APSStreamedJSONRPCProtocolProvider__ section.

## Authentication

Authentication for services are provided in 2 ways. Both require a userid and a password and both validate the user using the APSAuthService.

The 2 alternatives are:

* http://.../apsrpc/__auth:user:password__/protocol/...

* Basic HTTP authentication using header: 'Authorization: Basic {_base 64 encoded user:password_}’.

One of these will be required if the _requireAuthentication_ configuration have been enabled.

## The help web

Opening the _http://.../apsrpc/_help/_ URL will give you a web page that provides a lot of information. This page requires authentication since it register itself with the APSAdminWeb (/apsadminweb) as ”Remote Services” and appears there as a tab, and thus joins in with the APSAdminWeb authentication.

In addition to much of the same information as in this documentation it also lists all protocols tracked by the _APSExternalProtocolExtender_ with their name, version, description, and other properties. Next it lists all services that _APSExternalProtocolExtender_ provides as callable. Each of these services are a link that can be clicked. Clicking on a service will show all the methods of the service and then list the call url for each method per protocol. Each method listed is also a link, and clicking that link will give you a page where you can provide arguments and then press execute to call the service. The result will be displayed as JSON on the same page. This is very useful for testing and debugging services.

## See Also

Also look at the documentation for APSExternalProtocolExtender.

# APSGroups

Provides network groups where named groups can be joined as members and then send and receive data messages to the group. This is based on multicast and provides a verified multicast delivery with acknowledgements of receive to the sender and resends if needed. The sender will get an exception if not all members receive all data. Member actuality is handled by members announcing themselves relatively often and will be removed when an announcement does not come in expected time. So if a member dies unexpectedly (network goes down, etc) its membership will resolve rather quickly. Members also tries to inform the group when they are doing a controlled exit.

Please note that this does not support streaming! That would require a far more complex protocol. APSGroups waits in all packets of a message before delivering the message.

## OSGi service usage

The APSGroupsService can be used as an OSGi service and as a standalone library. This section describes the service.

### Getting the service

        APSServiceTracker<APSGroupService> apsGroupsServiceTracker = 
            new APSServiceTracker<APSGroupsService>(bundleContext, APSConfigService.class,
            APSServiceTracker.LARGE_TIMEOUT);
        APSGroupsService apsGroupsService = apsGroupsServiceTracker.getWrappedService();
        

### Joining a group

        GroupMember groupMember = apsGroupsService.joinGroup("mygroup");
        

### Sending a message

To send a message you create a message, get its output stream and write whatever you want to send on that output stream, close it and then send it. _Note_ that since the content of the message is any data you want, all members of the groups must know how the data sent looks like. In other words, you have to define your own message protocol for your messages. Note that you can wrap the OutputStream in an ObjectOutputStream and serialize any java object you want.

        Message message = groupMember.createNewMessage();
        OutputStream msgDataStream = message.getOutputStream();
        try {
            ...
            msgDataStream.close();
            groupMember.sendMessage(message);
        }
        catch (IOException ioe) {
            ...
        }
        

Note that the `groupMember.sendMessage(message)` does throw an IOException on failure to deliver the message to all members.

### Receiving a message

To receive a message you have to register a message listener with the GroupMember object.

        MessageListener msgListener = new MyMsgListener();
        groupMember.addMessageListener(myMsgListener); 

and then handle received messages:

        public class MyMsgListener implements MessageListener {
            public void messageReceived(Message message) {
                InputStream msgDataStream = message.getInputStream();
                ...
            }
        }

### Leaving a group

        apsGroupsService.leaveGroup(groupMember);
        

## Library usage

The bundle jar file can also be used as a library outside of an OSGi server, with an API that has no other dependencies than what is in the jar. The API is then slightly different, and resides under the se.natusoft.apsgroups package.

### Setting up

        APSGroups apsGroups = new APSGroups(config, logger);
        apsGroups.connect();
        

The config passed as argument to APSGroups will be explained further down under "Configuration".

The _logger_ is an instance of an implementation of the APSGroupsLogger interface. Either you provide your own implementation of that or your use the APSGroupsSystemOutLogger implementation.

### Joining a group

        GroupMember groupMember = apsGroups.joinGroup("mygroup");

### Sending and receiving messages

Sending and receiving works exactly like the OSGi examples above.

### Leaving a group

        apsGroups.leaveGroup(groupMember);

### Shutting down

        apsGroups.disconnect();
        

## Net time

All APSGroups instances connected will try to sync their time. I call this synced time "net time".

It works like this: When an APSGroups instance comes up it waits a while for NET_TIME packets. If it gets such a packet then it enters receive mode and takes the time in the received NET_TIME packet and stores a diff to that time and local time. This diff can then be used to translate back and forth between local and net time. If no such packet arrives in expected time it enters send mode and starts sending NET_TIME packets itself using its current net time. If a NET_TIME packet is received when in send mode it directly goes over to listen mode. If in listen mode and no NET_TIME packet comes in reasonable time it goes over to send mode. So among all instances on the network only one is responsible for sending NET_TIME. If that instance leaves then there might be a short fight for succession, but it will resolve itself rather quickly.

The GroupMember contains a few _create*_ methods to produce a _NetTime_ object instance. See the API further down for more information on these.

## Configuration

### OSGi service 

The OSGi service provides a configuration model that gets managed by the APSConfigService. It can be configured in the APS adminweb (http://host:port/apsadminweb/). Here are some screenshots of the config admin:

![/apsconfigadmin web gui for configuring APSGroups 1](http://download.natusoft.se/Images/APS/APS-Network/APSGroups/docs/images/groups-config-1.png) ![/apsconfigadmin web gui for configuring APSGroups 2](http://download.natusoft.se/Images/APS/APS-Network/APSGroups/docs/images/groups-config-2.png) ![/apsconfigadmin web gui for configuring APSGroups 3](http://download.natusoft.se/Images/APS/APS-Network/APSGroups/docs/images/groups-config-3.png) ![/apsconfigadmin web gui for configuring APSGroups 4](http://download.natusoft.se/Images/APS/APS-Network/APSGroups/docs/images/groups-config-4.png)

As can bee seen in the above screenshots transports need to be configured for communication to work. If you only need to talk to members on the same subnet the multicast transport is enough! The multicast transport makes sure that all transmitted data is received by all known group members. It will do resends if required, and throw an exception on failure of any member to acknowledge all sent packets.

If you need to talk to members on a different subnet then you need to use the TCP transports. Note that there are 2 of these: _TCP_SENDER_, and _TCP_RECEIVER_. One receiver must be configured and can receive messages from anyone. A sender is needed for each APSGroups installation you want to talk to, and should point to the receiver of that installation. Note that for a receiver you only need to specify a port. The host part is ignored by the receiver.

### Library 

The library wants an implementation of the APSGroupsConfig interface as its first argument to APSGroups(config, logger) constructor. Either you implement your own or use the APSGroupsConfigProvider implementation. This is a plain java bean with both setters and getters for the config values. It comes with quite reasonable default values. It contains exactly the same properties as shown in the screenshots above.

## APIs

public _interface_ __APSGroupsInfoService__   [se.natusoft.osgi.aps.api.net.groups.service] {

This service provides information about current groups and members.

__List<String> getGroupNames()__

Returns the names of all available groups.

__List<String> getGroupMembers(String groupName)__

Returns a list of member ids for the specified group.

_Parameters_

> _groupName_ - The name of the group to get member ids for. 

__List<String> getGroupsAndMembers()__

Returns a list of "groupName : groupMember" for all groups and members.

}

----

    

public _interface_ __APSGroupsService__   [se.natusoft.osgi.aps.api.net.groups.service] {

A service that lets clients send data reliable to all members of a group on any host. There is no limit on the size of the data sent, but that said I wouldn't send MB:s of data!

__GroupMember joinGroup(String name) throws IOException__

Joins a group.

_Returns_

> A GroupMember that provides the API for sending and receiving data in the group.

_Parameters_

> _name_ - The name of the group to join. 

_Throws_

> _java.io.IOException_ - The unavoidable one! 

__GroupMember joinGroup(String name, Properties memberUserData) throws IOException__

Joins a group.

_Returns_

> A GroupMember that provides the API for sending and receiving data in the group.

_Parameters_

> _name_ - The name of the group to join. 

> _memberUserData_ - Data provided by users of the service. 

_Throws_

> _java.io.IOException_ - The unavoidable one! 

__void leaveGroup(GroupMember groupMember) throws IOException__

Leaves as member of group.

_Parameters_

> _groupMember_ - The GroupMember returned when joined. 

_Throws_

> _java.io.IOException_ - The unavoidable one! 

}

----

    

public _interface_ __GroupMember__   [se.natusoft.osgi.aps.api.net.groups.service] {

This is the API for _APSGroupsService_ members received when they join a group. It is used to send and receive data messages to/from the group.

__void addMessageListener(MessageListener listener)__

Adds a listener for incoming messages.

_Parameters_

> _listener_ - The listener to add. 

__void removeMessageListener(MessageListener listener)__

Removes a listener for incoming messages.

_Parameters_

> _listener_ - The listener to remove. 

__Message createNewMessage()__

Creates a new Message to send. Use the sendMessage() method when ready to send it.

__void sendMessage(Message message) throws IOException__

Sends a previously created message to all current members of the group. If this returns without an exception then all members have received the message.

_Parameters_

> _message_ - The message to send. 

_Throws_

> _java.io.IOException_ - On failure to reach all members. 

__UUID getMemberId()__

_Returns_

> The ID of the member.

__List<String> getMemberInfo()__

Returns information about members.

__List<Properties> getMembersUserProperties()__

Returns the user properties for the members.

__NetTime getNow()__

_Returns_

> The current time as net time.

__NetTime createFromNetTime(long netTimeMillis)__

Creates from milliseconds in net time.

_Parameters_

> _netTimeMillis_ - The net time milliseconds to create a NetTime for. 

__NetTime createFromNetTime(Date netTimeDate)__

Creates from a Date in net time.

_Parameters_

> _netTimeDate_ - The Date in net time to create a NetTime for. 

__NetTime createFromLocalTime(long localTimeMillis)__

Creates from milliseconds in local time.

_Parameters_

> _localTimeMillis_ - The local time milliseconds to create a NetTime for. 

__NetTime createFromLocalTime(Date localTimeDate)__

Creates from a Date in local time.

_Parameters_

> _localTimeDate_ - The Date in local time to create a NetTime for. 

}

----

    

public _interface_ __Message__   [se.natusoft.osgi.aps.api.net.groups.service] {

This represents a complete message containing any data you want to send to the group. You provide the message with data using the _OutputStream_, and read message data using the _InputStream_.

__OutputStream getOutputStream()__

Returns an _OutputStream_ to write message on. Multiple calls to this will return the same _OutputStream_!

__InputStream getInputStream()__

Returns an _InputStream_ for reading the message. Multiple calls to this will return new _InputStream_:s starting from the beginning!

__UUID getId()__

Returns the id of this message.

__String getMemberId()__

_Returns_

> id of member as a string.

__String getGroupName()__

_Returns_

> The name of the group this message belongs to.

}

----

    

public _interface_ __MessageListener__   [se.natusoft.osgi.aps.api.net.groups.service] {

For listening on messages from the group.

__public void messageReceived(Message message)__

Notification of received message.

_Parameters_

> _message_ - The received message. 

}

----

    

public _interface_ __NetTime__ extends  Serializable    [se.natusoft.osgi.aps.api.net.groups.service] {

This represents a common network time between members for handling date and time data. The net time is synchronized between all members. Each receiver of net time diffs it with local time and stores the diff so that they can convert to/from local/net time.

__public long getNetTime()__

Returns the number of milliseconds since January 1, 1970 in net time.

__public Date getNetTimeDate()__

Returns the net time as a Date.

__public Calendar getNetTimeCalendar()__

Returns the net time as a Calendar.

__public Calendar getNetTimeCalendar(Locale locale)__

Returns the net time as a Calendar.

_Parameters_

> _locale_ - The locale to use. 

__public Date getLocalTimeDate()__

Converts the net time to local time and returns as a Date.

__public Calendar getLocalTimeCalendar()__

Converts the net time to local time and returns as a Calendar.

__public Calendar getLocalTimeCalendar(Locale locale)__

Converts the net time to local time and returns as a Calendar.

_Parameters_

> _locale_ - The locale to use. 

}

----

    

# APS Message Service Sync Service Provider

As this long name suggests this service provides an implementation of APSSyncService using APSMessageService to do the synchronization.

## APSSyncService API

public _interface_ __APSSyncService__   [se.natusoft.osgi.aps.api.net.sync.service] {

Defines a data synchronization service.

__Please note__ that this API is very similar to the APSSimpleMessageService! There are differences in implementations between synchronization and sync, reusing the same API would be confusing and also require services to register extra properties to identify type of service provided.

__SyncGroup joinSyncGroup(String name)__

Joins a synchronization group.

_Returns_

> joined group.

_Parameters_

> _name_ - The name of the group to join. 

__String getName()__

Returns the name of the group.

__Message createMessage()__

Creates a new message.

__Message createMessage(byte[] content)__

Creates a new message.

_Parameters_

> _content_ - The content of the message. 

__void sendMessage(Message message) throws APSSyncException__

Sends a message.

_Parameters_

> _message_ - The message to send. 

_Throws_

> _se.natusoft.osgi.aps.api.net.sync.service.APSSyncService.APSSyncException_

__void addMessageListener(Message.Listener listener)__

Adds a listener to received messages.

_Parameters_

> _listener_ - The listener to add. 

__void removeMessageListener(Message.Listener listener)__

Removes a listener from receiving messages.

_Parameters_

> _listener_ - The listener to remove. 

__void addReSyncListener(ReSyncListener reSyncListener)__

Adds a resynchronization listener.

_Parameters_

> _reSyncListener_ - The listener to add. 

__void removeReSyncListener(ReSyncListener reSyncListener)__

Removes a Resynchronization listener.

_Parameters_

> _reSyncListener_ - The listener to remove. 

__void reSyncAll()__

Triggers a re-synchronization between all data and all members.

__void leaveSyncGroup()__

Leaves the synchronization group.

public _interface_ __ReSyncListener__   [se.natusoft.osgi.aps.api.net.sync.service] {

This is called when a total re-synchronization is requested.

__void reSyncAll(SyncGroup group)__

Request that all data be sent again.

_Parameters_

> _group_ - The group making the request. 

public _interface_ __Message__   [se.natusoft.osgi.aps.api.net.sync.service] {

This represents a message to send/receive.

__SyncGroup getSyncGroup()__

Returns the sync group the message belongs to.

__void setBytes(byte[] content)__

Sets the message content bytes overwriting any previous content.

_Parameters_

> _content_ - The content to set. 

__byte[] getBytes()__

Returns the message content bytes.

__OutputStream getOutputStream()__

Returns an OutputStream for writing message content. This will replace any previous content.

__InputStream getInputStream()__

Returns an InputStream for reading the message content.

public _interface_ __Listener__   [se.natusoft.osgi.aps.api.net.sync.service] {

This needs to be implemented to receive messages.

__void receiveMessage(Message message)__

Called when a message is received.

_Parameters_

> _message_ - The received message. 

public _class_ __Provider__ implements  Message    [se.natusoft.osgi.aps.api.net.sync.service] {

A simple default implementation of message.

__public Provider(SyncGroup group)__

Creates a new Provider.

_Parameters_

> _group_ - The group the message belongs to. 











public _static_ _class_ __APSSyncException__ extends  APSRuntimeException    [se.natusoft.osgi.aps.api.net.sync.service] {

Thrown on sendMessage(). Please note that this is a runtime exception!

__public APSSyncException(String message)__

Creates a new _APSSyncException_.

_Parameters_

> _message_ - The exception message. 

__public APSSyncException(String message, Throwable cause)__

Creates a new _APSSyncException_.

_Parameters_

> _message_ - The exception message. 

> _cause_ - The cause of this exception. 

}

----

    

# APS Net Time Servcie Provider

This provides a service for converting time between a common network time and local time. The actual net time is provided by APSGroupsService which must be running for this service provider to work. This also means that it will only work between hosts on the same subnet and multicast must be supported on that subnet.

The idea with this service is that no matter what the local host time is, time critical data passed on the network can be reasonably compared between hosts, by agreeing on a common _now_ time and the diff between local time and this common time.

As said above, this implementation have limitations!

## APSNetTimeService

[Javadoc](http://apidoc.natusoft.se/APS/se/natusoft/osgi/aps/api/net/time/service/APSNetTimeService.html)

public _interface_ __APSNetTimeService__   [se.natusoft.osgi.aps.api.net.time.service] {

This service provides network neutral time. Even with NTP it is difficult to keep the same time on different servers. This service creates a network timezone and broadcasts the network time. It supports converting local time to network time and converting network time to local time.

Please note that the network time will not be accurate down to milliseconds, but will be reasonable correct for most usages.

__public long netToLocalTime(long netTime)__

Converts from net time to local time.

_Returns_

> local time.

_Parameters_

> _netTime_ - The net time to convert. 

__public Date netToLocalTime(Date netTime)__

Converts from net time to local time.

_Returns_

> local time.

_Parameters_

> _netTime_ - The net time to convert. 

__public long localToNetTime(long localTime)__

Converts from local time to net time.

_Returns_

> net time.

_Parameters_

> _localTime_ - The local time to convert. 

__public Date localToNetTime(Date localTime)__

Converts from local time to net time.

_Returns_

> net time.

_Parameters_

> _localTime_ - The local time to convert. 

}

----

    

public _interface_ __APSSimpleMessageService__   [se.natusoft.osgi.aps.api.net.messaging.service] {

This is as the name suggests a simple message service where a named group can be joined to send messages to all members of that group.

Yes, this API is very similar to APSGroups, but that API is a bit too explicit for the provided implementation. This is very simple and generic.

__MessageGroup joinMessageGroup(String name) throws APSMessageException__

Joins a message group.

_Returns_

> A MessageGroup instance used to send and receive messages to/from the group.

_Parameters_

> _name_ - The name of the message group to join. 

_Throws_

> _APSMessageException_ - On nay failure to join. 

public _interface_ __MessageGroup__   [se.natusoft.osgi.aps.api.net.messaging.service] {

This represents a specific message group.

__String getName()__

Returns the name of the group.

__Message createMessage()__

Creates a new message.

__Message createMessage(byte[] content)__

Creates a new message.

_Parameters_

> _content_ - The content of the message. 

__void sendMessage(Message message) throws APSMessageException__

Sends a message.

_Parameters_

> _message_ - The message to send. 

_Throws_

> _APSMessageException_ - depending on implementation! 

__void addMessageListener(Message.Listener listener)__

Adds a listener to receive messages.

_Parameters_

> _listener_ - The listener to add. 

__void removeMessageListener(Message.Listener listener)__

Removes a listener from receiving messages.

_Parameters_

> _listener_ - The listener to remove. 

__void leave()__

Leaves the group. This should always be done in case the implementation needs to do any cleanup.

public _interface_ __Message__   [se.natusoft.osgi.aps.api.net.messaging.service] {

This represents a message to send/receive.

__void setBytes(byte[] content)__

Sets the message content bytes overwriting any previous content.

_Parameters_

> _content_ - The content to set. 

__byte[] getBytes()__

Returns the message content bytes.

__OutputStream getOutputStream()__

Returns an OutputStream for writing message content. This will replace any previous content.

__InputStream getInputStream()__

Returns an InputStream for reading the message content.

public _interface_ __Listener__   [se.natusoft.osgi.aps.api.net.messaging.service] {

This needs to be implemented to receive messages.

__void receiveMessage(String groupName, Message message)__

Called when a message is received.

_Parameters_

> _groupName_ - The name of the group that delivered the message. 

> _message_ - The received message. 

public _class_ __Provider__ implements  Message    [se.natusoft.osgi.aps.api.net.messaging.service] {

A simple default implementation of message.

__public Provider()__

Creates a new Provider.









public _static_ _class_ __APSMessageException__ extends  APSRuntimeException    [se.natusoft.osgi.aps.api.net.messaging.service] {

Thrown on sendMessage(). Please note that this is a runtime exception!

__public APSMessageException(String message)__

Creates a new _APSMessageException_.

_Parameters_

> _message_ - The exception message. 

__public APSMessageException(String message, Throwable cause)__

Creates a new _APSMessageException_.

_Parameters_

> _message_ - The exception message. 

> _cause_ - The cause of this exception. 

}

----

    

public _interface_ __APSSyncService__   [se.natusoft.osgi.aps.api.net.sync.service] {

Defines a data synchronization service.

__Please note__ that this API is very similar to the APSSimpleMessageService! There are differences in implementations between synchronization and sync, reusing the same API would be confusing and also require services to register extra properties to identify type of service provided.

__SyncGroup joinSyncGroup(String name)__

Joins a synchronization group.

_Returns_

> joined group.

_Parameters_

> _name_ - The name of the group to join. 

__String getName()__

Returns the name of the group.

__Message createMessage()__

Creates a new message.

__Message createMessage(byte[] content)__

Creates a new message.

_Parameters_

> _content_ - The content of the message. 

__void sendMessage(Message message) throws APSSyncException__

Sends a message.

_Parameters_

> _message_ - The message to send. 

_Throws_

> _se.natusoft.osgi.aps.api.net.sync.service.APSSyncService.APSSyncException_

__void addMessageListener(Message.Listener listener)__

Adds a listener to received messages.

_Parameters_

> _listener_ - The listener to add. 

__void removeMessageListener(Message.Listener listener)__

Removes a listener from receiving messages.

_Parameters_

> _listener_ - The listener to remove. 

__void addReSyncListener(ReSyncListener reSyncListener)__

Adds a resynchronization listener.

_Parameters_

> _reSyncListener_ - The listener to add. 

__void removeReSyncListener(ReSyncListener reSyncListener)__

Removes a Resynchronization listener.

_Parameters_

> _reSyncListener_ - The listener to remove. 

__void reSyncAll()__

Triggers a re-synchronization between all data and all members.

__void leaveSyncGroup()__

Leaves the synchronization group.

public _interface_ __ReSyncListener__   [se.natusoft.osgi.aps.api.net.sync.service] {

This is called when a total re-synchronization is requested.

__void reSyncAll(SyncGroup group)__

Request that all data be sent again.

_Parameters_

> _group_ - The group making the request. 

public _interface_ __Message__   [se.natusoft.osgi.aps.api.net.sync.service] {

This represents a message to send/receive.

__SyncGroup getSyncGroup()__

Returns the sync group the message belongs to.

__void setBytes(byte[] content)__

Sets the message content bytes overwriting any previous content.

_Parameters_

> _content_ - The content to set. 

__byte[] getBytes()__

Returns the message content bytes.

__OutputStream getOutputStream()__

Returns an OutputStream for writing message content. This will replace any previous content.

__InputStream getInputStream()__

Returns an InputStream for reading the message content.

public _interface_ __Listener__   [se.natusoft.osgi.aps.api.net.sync.service] {

This needs to be implemented to receive messages.

__void receiveMessage(Message message)__

Called when a message is received.

_Parameters_

> _message_ - The received message. 

public _class_ __Provider__ implements  Message    [se.natusoft.osgi.aps.api.net.sync.service] {

A simple default implementation of message.

__public Provider(SyncGroup group)__

Creates a new Provider.

_Parameters_

> _group_ - The group the message belongs to. 











public _static_ _class_ __APSSyncException__ extends  APSRuntimeException    [se.natusoft.osgi.aps.api.net.sync.service] {

Thrown on sendMessage(). Please note that this is a runtime exception!

__public APSSyncException(String message)__

Creates a new _APSSyncException_.

_Parameters_

> _message_ - The exception message. 

__public APSSyncException(String message, Throwable cause)__

Creates a new _APSSyncException_.

_Parameters_

> _message_ - The exception message. 

> _cause_ - The cause of this exception. 

}

----

    

# APSStreamedJSONRPCProtocolProvider

This provides JSONRPC protocol. It provides both version 1.0 and 2.0 of the protocol. It requires a transport that uses it and services provided by aps-external-protocol-extender to be useful.

JSONRPC version 1.0 protocol as described at [http://json-rpc.org/wiki/specification](http://json-rpc.org/wiki/specification).

JSONRPC version 2.0 protocol as describved at [http://jsonrpc.org/spec.html](http://jsonrpc.org/spec.html).

JSONHTTP version 1.0 which is not any standard protocol at all. It requires both service name and method name on the url, and in case of HTTP GET or DELETE also arguments as ?params=arg:...:arg where values are strings or primitives. For POST, and PUT a JSON array of values need to be written on the stream.

JSONREST version 1.0 extending JSONHTTP will make the http transport always map methods annotated with @RESTGET, @RESTPUT, @RESTPOST, and @RESTDELETE to the corresponding http methods. This also does not require a method to be specified on the URL, and will ignore any specified method.

Personally I think that JSONRPC 2.0 is far more flexible than REST.

## Examples

Here is some examples calling services over http with diffent protocols using curl (_requires aps-ext-protocol-http-transport-provider.jar and the called services to be deployed_,_and specified as externalizable via configuration_ (Network/service/external-protocol-extender)):

        curl --data '{"jsonrpc": "2.0", "method": "getPlatformDescription", "params": [], "id": 1}' http://localhost:8080/apsrpc/JSONRPC/2.0/se.natusoft.osgi.aps.api.core.platform.service.APSPlatformService 

yields:

        {"id": 1, "result": {"description": "My personal development environment.", "type": "Development", "identifier": "MyDev"}, "jsonrpc": "2.0"}
            

while

        curl --get http://localhost:8080/apsrpc/JSONHTTP/1.0/se.natusoft.osgi.aps.api.core.platform.service.APSPlatformService/getPlatformDescription
        

yields

        {"description": "My personal development environment.", "type": "Development", "identifier": "MyDev"}
           

and

         curl --get http://localhost:8080/apsrpc/JSONHTTP/1.0/se.natusoft.osgi.aps.api.misc.session.APSSessionService/createSession\(Integer\)?params=5
         

yields

        {"id": "6d25d646-11fc-44c3-b74d-29b3d5c94920", "valid": true}
        

In this case we didn't just use _createSession_ as method name, but _createSession(Integer)_ though with parentheses escaped to not confuse the shell. This is because there is 2 variants of createSession: createSession(String, Integer) and createSession(Integer). If we don't specify clearly we might get the wrong one and in this case that happens and will fail due to missing second parameter. Also note the _params=5_. On get we cannot pass any data on the stream to the service, we can only pass parameters on the URL which is done by specifying url parameter _params_ with a colon (:) separated list of parameters as value. In this case only String and primitives are supported for parameters.

These examples only works if you have disabled the _requireAuthentication_ configuration (network/rpc-http-transport).

## See also

Se the documentation for _APSExtProtocolHTTPTransportProvider_ for an HTTP transport through which these protocols can be used.

Se the documentation for _APSExternalProtocolExtender_ for a description of how services are made available and what services it provides for transport providers.

# APSAdminWeb

![APSAdminWeb screenshot](http://download.natusoft.se/Images/APS/APS-Webs/APSAdminWeb/docs/images/APSAdminWeb.png)

This is a web app for administration of APS. It is really only a shell for different administraion webs. It relys on the _aps-admin-web-service-provider_ bundle which publishes the _APSAdminWebService_. Other bundles providing administration web apps register themselves with this service and for each registration APSAdminWeb creates a tab in its gui. Se _APIs_ further down for the APSAdminService API. Clicking on ”Refresh” will make APSAdminWeb reload the admin webs registered in _APSAdminWebService_.

The APSAdminWeb is accessed at __http://host:port/apsadminweb__. What you see there depends on what other admin webs are deployed. Anybody can make an admin web and register it with the _APSAdminWebService_. The admin webs delivered with APS are mainly done using Vaadin. This is in no way a requirement for an admin web. An admin web app can be made in any way what so ever. A side effect of this is that different tabs might have different look and feel. But I choose flexibility over beauty!

The following APS bundles provides a tab in APSAdminWeb:

* _aps-config-admin-web.war_ - Allows advanced configuration of bundles/services using APSConfigService.

* _aps-user-admin-web-war_ - Administration of users and groups for APSSimpleUserService.

* _aps-ext-protocol-http-transport-provider.war_ - Provides a web gui with help for setting up and calling services remotely, and also shows all available services and allows calling them from the web gui for testing/debugging purposes.

## Authentication

If _”Configuration tab_,_Configurations/aps/adminweb/requireauthentication”_ property is enabled then the APSAdminWeb requires a login to be accessed. A userid and a password will be asked for. The entered information will be validated by the APSAuthService. The _aps-simple-user-service-auth-service-provider.jar_ bundle provides an implementation of this service that uses the _APSSimpleUserService_ service. The APSAuthService is however simple enough to implement yourself to provide login to whatever you want/need.

![APSAdminWeb login screenshot](http://download.natusoft.se/Images/APS/APS-Webs/APSAdminWeb/docs/images/APSAdminWeb-login.png)

## Making an admin web participating in the APSAdminWeb login.

There is an APSSessionService that was made just for handling this. It is not a HTTP session, just a service hanling sessions. It is provided by the _aps-session-service-provider.jar_ bundle. When a session is created you get a session id (an UUID) that needs to be passed along to the other admin webs through a cookie. _APSWebTools_ (_aps-web-tools.jar_ (not a bundle!)) provides the APSAdminWebLoginHandler class implementing the LoginHandler interface and handles all this for you.

You need to provide it with a BundleContext on creation since it will be calling both the _APSAuthService_ and _APSSessionService_:

        this.loginHandler = new APSAdminWebLoginHandler(bundleContext);
        

Then to validate that there is a valid login do:

        this.loginHandler.setSessionIdFromRequestCookie(request);
        if (this.loginHandler.hasValidLogin()) {
            ...
        }
        else {
            ...
        }

## APSAdminWebService APIs

public _interface_ __APSAdminWebService__   [se.natusoft.osgi.aps.apsadminweb.service] {

This service registers other specific administration web applications to make them available under a common administration gui.

__public void registerAdminWeb(AdminWebReg adminWebReg) throws IllegalArgumentException__

Registers an admin web application.

_Parameters_

> _adminWebReg_ - Registration information for the admin web. 

_Throws_

> _IllegalArgumentException_ - if the admin web has already been registered or if it is using the 

__public void unregisterAdminWeb(AdminWebReg adminWebReg)__

Unregisters a previously registered admin web. This is failsafe. If it has not been registered nothing happens.

_Parameters_

> _adminWebReg_ - Registration information for the admin web. Use the same as registered with. 

__public List<AdminWebReg> getRegisteredAdminWebs()__

_Returns_

> All currently registered admin webs.

}

----

    

public _class_ __AdminWebReg__   [se.natusoft.osgi.aps.apsadminweb.service.model] {

This model holds information about a registered admin web application.









__public AdminWebReg(String name, String version, String description, String url)__

Creates a new AdminWebReg instance.

_Parameters_

> _name_ - A (short) name of the admin web. 

> _version_ - The version of the admin web. 

> _description_ - A longer description of the admin web. 

> _url_ - The deployment url of the admin web. 

__public String getName()__

_Returns_

> The (short) name of the admin web.

__public String getVersion()__

_Returns_

> The version of the admin web.

__public String getDescription()__

_Returns_

> The description of the admin web.

__public String getUrl()__

_Returns_

> The deployment url of the admin web.







}

----

    

# APSConfigAdminWeb

![APSConfigAdminWeb screenshot](http://download.natusoft.se/Images/APS/APS-Webs/APSConfigAdminWeb/docs/images/APSConfigWeb-simple-small.png) ![APSConfigAdminWeb screenshot](http://download.natusoft.se/Images/APS/APS-Webs/APSConfigAdminWeb/docs/images/APSConfigWeb-structured-small.png)

This allows editing configurations registered with the _APSConfigService_. Configurations are only available in the APSConfigAdminWeb while the bundle providing the configuration model are deployed. The actual saved configurations live on disk and remains after a bundle is stopped. It will be available again when the bundle is started again. But the bundle have to be running and regisitering its configuration with the _APSConfigService_ for them to be editable in this admin app!

As can be seen in the screenshots above it provides a simpler gui for simple configs, and a more advanced gui for structured configurations containing list of other configuration models.

## Config Environments

Under this node all available configuration environments are listed. Right clicking on the node will drop down a menu alternative to create a new configuration environment. Right clicking on a configuration environment pops up a menu that allows it to be set as active configuration environment or to delete the configuration environment. Just clicking on a configuration environment allows it to be edited on the right side. The active configuration environment cannot however be edited, only viewed.

## Configurations

This tree cannot be edited. What is here is the configurations registered by bundles. They can be selected to edit the selected configuration to the right. The screenshots above shows 2 examples of such. Please note that the screenshots were taken on a Mac with Mountain Lion and thus does not show scrollbars unless scrolling. The right side of the second screenshot where things are slightly cutoff at the bottom are scrollable!

On top of the right side box there is a dropdown menu that shows/selects the configuration environment you are editing configuration values for. Only configuration values that are marked in the configuration model as being configuration environment specific will get different values per configuration environement. Those values that are configuration environment specific are identified by having the configuration environment in parentesis after the configuration value key. If you switch the configuration environment in the top dropdown menu you will se that these values change.

Boolean configuration values will be shown as checkboxes. Date configuration values will have a date field where the user can write a date or click the button on the end to bring upp a calendar to select from. Date configuration values can also specify the date format (as described [here](http://docs.oracle.com/javase/7/docs/api/java/text/SimpleDateFormat.html)) in the configuration model. This is used to display the date in the field and parse any entered date. So different date fields can have different formats!

The configuration models are annotated and provide descriptions of the values which are shown in the gui to make it easy for the person doing the configuration to know what the configuration is about.

As soon as the configuration changes are saved they become active. The code using the configurations doesn’t need to do anything. The next reference to a configuration value will return the new value.

## See also

Also se the APSConfigService documentation.

# APSUserAdminWeb

![APSUserAdminWeb screenshot](http://download.natusoft.se/Images/APS/APS-Webs/APSUserAdminWeb/docs/images/APSUserAdminWeb.png)

APSUserAdminWeb provides user and group administration for the _APSSimpleUserService_.

Users are splitt into groups of the first character in the userid to make them a little bit easier to find if there are many. So all userids starting with ’a’ or ’A’ will be under Users/A and so on.

Right click on the _Users_ node to create a new user.

Right click on the _Roles_ node to create a new role.

__Warning:__ For the roles it is fully possible to create circular dependencies! __Dont!__ (There is room for improvement on this point!)

There is not anything more to say about this. It should be selfexplanatory!

# Licenses

<!--
  Created by CodeLicenseManager
-->
## Project License

____[Apache Software License version 2.0](http://www.apache.org/licenses/LICENSE-2.0.html)____

## Third Party Licenses

____[OSGi Specification License version 2.0](http://www.osgi.org/Specifications/Licensing)____

The following third party products are using this license:

* [org.osgi.compendium-4.2.0-null](http://www.osgi.org/)

* [org.osgi.core-4.2.0-null](http://www.osgi.org/)

____[GNU Public License version v2](http://www.gnu.org/licenses/gpl-2.0.html)____

The following third party products are using this license:

* [MarkdownDoclet-3.0-null](http://code.google.com/p/markdown-doclet/)

____[Apache Software License version 2.0](http://www.apache.org/licenses/LICENSE-2.0.txt)____

The following third party products are using this license:

* [annotations-13.0](http://www.jetbrains.org)

* [vaadin-server-7.1.14](http://vaadin.com)

* [vaadin-client-compiled-7.1.14](http://vaadin.com)

* [vaadin-client-7.1.14](http://vaadin.com)

* [vaadin-push-7.1.14](http://vaadin.com)

* [vaadin-themes-7.1.14](http://vaadin.com)

* [openjpa-all-2.2.0](http://www.apache.org/licenses/LICENSE-2.0.txt)

* [derbyclient-10.9.1.0](http://db.apache.org/derby/)

____[Eclipse Public License - v version 1.0](http://www.eclipse.org/legal/epl-v10.html)____

The following third party products are using this license:

* [javax.persistence-2.0.0](http://www.eclipse.org/eclipselink)

____[Day Specification version License](http://www.day.com/dam/day/downloads/jsr283/day-spec-license.htm)____

The following third party products are using this license:

* [jcr-2.0](http://www.day.com)

____[CDDL + GPLv2 with classpath version exception](https://glassfish.dev.java.net/nonav/public/CDDL+GPL.html)____

The following third party products are using this license:

* [javax.servlet-api-3.0.1](http://servlet-spec.java.net)

* [javaee-web-api-6.0](http://java.sun.com/javaee/6/docs/api/index.html)

<!--
  CLM
-->
## Apache License version 2.0, January 2004

[http://www.apache.org/licenses/](http://www.apache.org/licenses/)

__TERMS AND CONDITIONS FOR USE__,__REPRODUCTION__,__AND DISTRIBUTION__

1. Definitions.

"License" shall mean the terms and conditions for use, reproduction, and distribution as defined by Sections 1 through 9 of this document.

"Licensor" shall mean the copyright owner or entity authorized by the copyright owner that is granting the License.

"Legal Entity" shall mean the union of the acting entity and all other entities that control, are controlled by, or are under common control with that entity. For the purposes of this definition, "control" means (i) the power, direct or indirect, to cause the direction or management of such entity, whether by contract or otherwise, or (ii) ownership of fifty percent (50%) or more of the outstanding shares, or (iii) beneficial ownership of such entity.

"You" (or "Your") shall mean an individual or Legal Entity exercising permissions granted by this License.

"Source" form shall mean the preferred form for making modifications, including but not limited to software source code, documentation source, and configuration files.

"Object" form shall mean any form resulting from mechanical transformation or translation of a Source form, including but not limited to compiled object code, generated documentation, and conversions to other media types.

"Work" shall mean the work of authorship, whether in Source or Object form, made available under the License, as indicated by a copyright notice that is included in or attached to the work (an example is provided in the Appendix below).

"Derivative Works" shall mean any work, whether in Source or Object form, that is based on (or derived from) the Work and for which the editorial revisions, annotations, elaborations, or other modifications represent, as a whole, an original work of authorship. For the purposes of this License, Derivative Works shall not include works that remain separable from, or merely link (or bind by name) to the interfaces of, the Work and Derivative Works thereof.

"Contribution" shall mean any work of authorship, including the original version of the Work and any modifications or additions to that Work or Derivative Works thereof, that is intentionally submitted to Licensor for inclusion in the Work by the copyright owner or by an individual or Legal Entity authorized to submit on behalf of the copyright owner. For the purposes of this definition, "submitted" means any form of electronic, verbal, or written communication sent to the Licensor or its representatives, including but not limited to communication on electronic mailing lists, source code control systems, and issue tracking systems that are managed by, or on behalf of, the Licensor for the purpose of discussing and improving the Work, but excluding communication that is conspicuously marked or otherwise designated in writing by the copyright owner as "Not a Contribution."

"Contributor" shall mean Licensor and any individual or Legal Entity on behalf of whom a Contribution has been received by Licensor and subsequently incorporated within the Work.

1. Grant of Copyright License. Subject to the terms and conditions of this License, each Contributor hereby grants to You a perpetual, worldwide, non-exclusive, no-charge, royalty-free, irrevocable copyright license to reproduce, prepare Derivative Works of, publicly display, publicly perform, sublicense, and distribute the Work and such Derivative Works in Source or Object form.

2. Grant of Patent License. Subject to the terms and conditions of this License, each Contributor hereby grants to You a perpetual, worldwide, non-exclusive, no-charge, royalty-free, irrevocable (except as stated in this section) patent license to make, have made, use, offer to sell, sell, import, and otherwise transfer the Work, where such license applies only to those patent claims licensable by such Contributor that are necessarily infringed by their Contribution(s) alone or by combination of their Contribution(s) with the Work to which such Contribution(s) was submitted. If You institute patent litigation against any entity (including a cross-claim or counterclaim in a lawsuit) alleging that the Work or a Contribution incorporated within the Work constitutes direct or contributory patent infringement, then any patent licenses granted to You under this License for that Work shall terminate as of the date such litigation is filed.

3. Redistribution. You may reproduce and distribute copies of the Work or Derivative Works thereof in any medium, with or without modifications, and in Source or Object form, provided that You meet the following conditions:

   1. You must give any other recipients of the Work or Derivative Works a copy of this License; and

   2. You must cause any modified files to carry prominent notices stating that You changed the files; and

   3. You must retain, in the Source form of any Derivative Works that You distribute, all copyright, patent, trademark, and attribution notices from the Source form of the Work, excluding those notices that do not pertain to any part of the Derivative Works; and

   4. If the Work includes a "NOTICE" text file as part of its distribution, then any Derivative Works that You distribute must include a readable copy of the attribution notices contained within such NOTICE file, excluding those notices that do not pertain to any part of the Derivative Works, in at least one of the following places: within a NOTICE text file distributed as part of the Derivative Works; within the Source form or documentation, if provided along with the Derivative Works; or, within a display generated by the Derivative Works, if and wherever such third-party notices normally appear. The contents of the NOTICE file are for informational purposes only and do not modify the License. You may add Your own attribution notices within Derivative Works that You distribute, alongside or as an addendum to the NOTICE text from the Work, provided that such additional attribution notices cannot be construed as modifying the License.

You may add Your own copyright statement to Your modifications and may provide additional or different license terms and conditions for use, reproduction, or distribution of Your modifications, or for any such Derivative Works as a whole, provided Your use, reproduction, and distribution of the Work otherwise complies with the conditions stated in this License.

4. Submission of Contributions. Unless You explicitly state otherwise, any Contribution intentionally submitted for inclusion in the Work by You to the Licensor shall be under the terms and conditions of this License, without any additional terms or conditions. Notwithstanding the above, nothing herein shall supersede or modify the terms of any separate license agreement you may have executed with Licensor regarding such Contributions.

5. Trademarks. This License does not grant permission to use the trade names, trademarks, service marks, or product names of the Licensor, except as required for reasonable and customary use in describing the origin of the Work and reproducing the content of the NOTICE file.

6. Disclaimer of Warranty. Unless required by applicable law or agreed to in writing, Licensor provides the Work (and each Contributor provides its Contributions) on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied, including, without limitation, any warranties or conditions of TITLE, NON-INFRINGEMENT, MERCHANTABILITY, or FITNESS FOR A PARTICULAR PURPOSE. You are solely responsible for determining the appropriateness of using or redistributing the Work and assume any risks associated with Your exercise of permissions under this License.

7. Limitation of Liability. In no event and under no legal theory, whether in tort (including negligence), contract, or otherwise, unless required by applicable law (such as deliberate and grossly negligent acts) or agreed to in writing, shall any Contributor be liable to You for damages, including any direct, indirect, special, incidental, or consequential damages of any character arising as a result of this License or out of the use or inability to use the Work (including but not limited to damages for loss of goodwill, work stoppage, computer failure or malfunction, or any and all other commercial damages or losses), even if such Contributor has been advised of the possibility of such damages.

8. Accepting Warranty or Additional Liability. While redistributing the Work or Derivative Works thereof, You may choose to offer, and charge a fee for, acceptance of support, warranty, indemnity, or other liability obligations and/or rights consistent with this License. However, in accepting such obligations, You may act only on Your own behalf and on Your sole responsibility, not on behalf of any other Contributor, and only if You agree to indemnify, defend, and hold each Contributor harmless for any liability incurred by, or claims asserted against, such Contributor by reason of your accepting any such warranty or additional liability.

__END OF TERMS AND CONDITIONS__

### APPENDIX: How to apply the Apache License to your work.

To apply the Apache License to your work, attach the following boilerplate notice, with the fields enclosed by brackets "[]" replaced with your own identifying information. (Don't include the brackets!) The text should be enclosed in the appropriate comment syntax for the file format. We also recommend that a file or class name and description of purpose be included on the same "printed page" as the copyright notice for easier identification within third-party archives.

        Copyright [yyyy] [name of copyright owner]
        
        Licensed under the Apache License, Version 2.0 (the "License");
        you may not use this file except in compliance with the License.
        You may obtain a copy of the License at
        
            http://www.apache.org/licenses/LICENSE-2.0
        
        Unless required by applicable law or agreed to in writing, software
        distributed under the License is distributed on an "AS IS" BASIS,
        WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
        See the License for the specific language governing permissions and
        limitations under the License.

<!--
  
  This was created by CodeLicenseManager
-->
## Day Specification version License

Day Management AG ("Licensor") is willing to license this specification to you ONLY UPON THE CONDITION THAT YOU ACCEPT ALL OF THE TERMS CONTAINED IN THIS LICENSE AGREEMENT ("Agreement"). Please read the terms and conditions of this Agreement carefully.

Content Repository for JavaTM Technology API Specification ("Specification") Version: 2.0 Status: FCS Release: 10 August 2009

Copyright 2009 Day Management AG Barf&#252;sserplatz 6, 4001 Basel, Switzerland. All rights reserved.

NOTICE; LIMITED LICENSE GRANTS

1. License for Purposes of Evaluation and Developing Applications.

Licensor hereby grants you a fully-paid, non-exclusive, non-transferable, worldwide, limited license (without the right to sublicense), under Licensor's applicable intellectual property rights to view, download, use and reproduce the Specification only for the purpose of internal evaluation. This includes developing applications intended to run on an implementation of the Specification provided that such applications do not themselves implement any portion(s) of the Specification.

1. License for the Distribution of Compliant Implementations. Licensor also grants you a perpetual, non-exclusive, non-transferable, worldwide, fully paid-up, royalty free, limited license (without the right to sublicense) under any applicable copyrights or, subject to the provisions of subsection 4 below, patent rights it may have covering the Specification to create and/or distribute an Independent Implementation of the Specification that: (a) fully implements the Specification including all its required interfaces and functionality; (b) does not modify, subset, superset or otherwise extend the Licensor Name Space, or include any public or protected packages, classes, Java interfaces, fields or methods within the Licensor Name Space other than those required/authorized by the Specification or Specifications being implemented; and (c) passes the Technology Compatibility Kit (including satisfying the requirements of the applicable TCK Users Guide) for such Specification ("Compliant Implementation"). In addition, the foregoing license is expressly conditioned on your not acting outside its scope. No license is granted hereunder for any other purpose (including, for example, modifying the Specification, other than to the extent of your fair use rights, or distributing the Specification to third parties).

2. Pass-through Conditions. You need not include limitations (a)-(c) from the previous paragraph or any other particular "pass through" requirements in any license You grant concerning the use of your Independent Implementation or products derived from it. However, except with respect to Independent Implementations (and products derived from them) that satisfy limitations (a)-(c) from the previous paragraph, You may neither: (a) grant or otherwise pass through to your licensees any licenses under Licensor's applicable intellectual property rights; nor (b) authorize your licensees to make any claims concerning their implementation's compliance with the Specification.

3. Reciprocity Concerning Patent Licenses. With respect to any patent claims covered by the license granted under subparagraph 2 above that would be infringed by all technically feasible implementations of the Specification, such license is conditioned upon your offering on fair, reasonable and non-discriminatory terms, to any party seeking it from You, a perpetual, non-exclusive, non-transferable, worldwide license under Your patent rights that are or would be infringed by all technically feasible implementations of the Specification to develop, distribute and use a Compliant Implementation.

4. Definitions. For the purposes of this Agreement: "Independent Implementation" shall mean an implementation of the Specification that neither derives from any of Licensor's source code or binary code materials nor, except with an appropriate and separate license from Licensor, includes any of Licensor's source code or binary code materials; "Licensor Name Space" shall mean the public class or interface declarations whose names begin with "java", "javax", "javax.jcr" or their equivalents in any subsequent naming convention adopted by Licensor through the Java Community Process, or any recognized successors or replacements thereof; and "Technology Compatibility Kit" or "TCK" shall mean the test suite and accompanying TCK User's Guide provided by Licensor which corresponds to the particular version of the Specification being tested.

5. Termination. This Agreement will terminate immediately without notice from Licensor if you fail to comply with any material provision of or act outside the scope of the licenses granted above.

6. Trademarks. No right, title, or interest in or to any trademarks, service marks, or trade names of Licensor is granted hereunder. Java is a registered trademark of Sun Microsystems, Inc. in the United States and other countries.

7. Disclaimer of Warranties. The Specification is provided "AS IS". LICENSOR MAKES NO REPRESENTATIONS OR WARRANTIES, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO, WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, NON-INFRINGEMENT (INCLUDING AS A CONSEQUENCE OF ANY PRACTICE OR IMPLEMENTATION OF THE SPECIFICATION), OR THAT THE CONTENTS OF THE SPECIFICATION ARE SUITABLE FOR ANY PURPOSE. This document does not represent any commitment to release or implement any portion of the Specification in any product.

The Specification could include technical inaccuracies or typographical errors. Changes are periodically added to the information therein; these changes will be incorporated into new versions of the Specification, if any. Licensor may make improvements and/or changes to the product(s) and/or the program(s) described in the Specification at any time. Any use of such changes in the Specification will be governed by the then-current license for the applicable version of the Specification.

1. Limitation of Liability. TO THE EXTENT NOT PROHIBITED BY LAW, IN NO EVENT WILL LICENSOR BE LIABLE FOR ANY DAMAGES, INCLUDING WITHOUT LIMITATION, LOST REVENUE, PROFITS OR DATA, OR FOR SPECIAL, INDIRECT, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF OR RELATED TO ANY FURNISHING, PRACTICING, MODIFYING OR ANY USE OF THE SPECIFICATION, EVEN IF LICENSOR HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.

2. Report. If you provide Licensor with any comments or suggestions in connection with your use of the Specification ("Feedback"), you hereby: (i) agree that such Feedback is provided on a non-proprietary and non-confidential basis, and (ii) grant Licensor a perpetual, non-exclusive, worldwide, fully paid-up, irrevocable license, with the right to sublicense through multiple levels of sublicensees, to incorporate, disclose, and use without limitation the Feedback for any purpose related to the Specification and future versions, implementations, and test suites thereof.

<!--
  
  This was created by CodeLicenseManager
-->
## Eclipse Public License - v version 1.0

Eclipse Public License - v 1.0

THE ACCOMPANYING PROGRAM IS PROVIDED UNDER THE TERMS OF THIS ECLIPSE PUBLIC LICENSE ("AGREEMENT"). ANY USE, REPRODUCTION OR DISTRIBUTION OF THE PROGRAM CONSTITUTES RECIPIENT'S ACCEPTANCE OF THIS AGREEMENT.

1. DEFINITIONS

"Contribution" means:

a) in the case of the initial Contributor, the initial code and documentation distributed under this Agreement, and b) in the case of each subsequent Contributor: i) changes to the Program, and ii) additions to the Program; where such changes and/or additions to the Program originate from and are distributed by that particular Contributor. A Contribution 'originates' from a Contributor if it was added to the Program by such Contributor itself or anyone acting on such Contributor's behalf. Contributions do not include additions to the Program which: (i) are separate modules of software distributed in conjunction with the Program under their own license agreement, and (ii) are not derivative works of the Program.

"Contributor" means any person or entity that distributes the Program.

"Licensed Patents" mean patent claims licensable by a Contributor which are necessarily infringed by the use or sale of its Contribution alone or when combined with the Program.

"Program" means the Contributions distributed in accordance with this Agreement.

"Recipient" means anyone who receives the Program under this Agreement, including all Contributors.

1. GRANT OF RIGHTS

a) Subject to the terms of this Agreement, each Contributor hereby grants Recipient a non-exclusive, worldwide, royalty-free copyright license to reproduce, prepare derivative works of, publicly display, publicly perform, distribute and sublicense the Contribution of such Contributor, if any, and such derivative works, in source code and object code form.

b) Subject to the terms of this Agreement, each Contributor hereby grants Recipient a non-exclusive, worldwide, royalty-free patent license under Licensed Patents to make, use, sell, offer to sell, import and otherwise transfer the Contribution of such Contributor, if any, in source code and object code form. This patent license shall apply to the combination of the Contribution and the Program if, at the time the Contribution is added by the Contributor, such addition of the Contribution causes such combination to be covered by the Licensed Patents. The patent license shall not apply to any other combinations which include the Contribution. No hardware per se is licensed hereunder.

c) Recipient understands that although each Contributor grants the licenses to its Contributions set forth herein, no assurances are provided by any Contributor that the Program does not infringe the patent or other intellectual property rights of any other entity. Each Contributor disclaims any liability to Recipient for claims brought by any other entity based on infringement of intellectual property rights or otherwise. As a condition to exercising the rights and licenses granted hereunder, each Recipient hereby assumes sole responsibility to secure any other intellectual property rights needed, if any. For example, if a third party patent license is required to allow Recipient to distribute the Program, it is Recipient's responsibility to acquire that license before distributing the Program.

d) Each Contributor represents that to its knowledge it has sufficient copyright rights in its Contribution, if any, to grant the copyright license set forth in this Agreement.

1. REQUIREMENTS

A Contributor may choose to distribute the Program in object code form under its own license agreement, provided that:

a) it complies with the terms and conditions of this Agreement; and

b) its license agreement:

i) effectively disclaims on behalf of all Contributors all warranties and conditions, express and implied, including warranties or conditions of title and non-infringement, and implied warranties or conditions of merchantability and fitness for a particular purpose;

ii) effectively excludes on behalf of all Contributors all liability for damages, including direct, indirect, special, incidental and consequential damages, such as lost profits;

iii) states that any provisions which differ from this Agreement are offered by that Contributor alone and not by any other party; and

iv) states that source code for the Program is available from such Contributor, and informs licensees how to obtain it in a reasonable manner on or through a medium customarily used for software exchange.

When the Program is made available in source code form:

a) it must be made available under this Agreement; and

b) a copy of this Agreement must be included with each copy of the Program.

Contributors may not remove or alter any copyright notices contained within the Program.

Each Contributor must identify itself as the originator of its Contribution, if any, in a manner that reasonably allows subsequent Recipients to identify the originator of the Contribution.

1. COMMERCIAL DISTRIBUTION

Commercial distributors of software may accept certain responsibilities with respect to end users, business partners and the like. While this license is intended to facilitate the commercial use of the Program, the Contributor who includes the Program in a commercial product offering should do so in a manner which does not create potential liability for other Contributors. Therefore, if a Contributor includes the Program in a commercial product offering, such Contributor ("Commercial Contributor") hereby agrees to defend and indemnify every other Contributor ("Indemnified Contributor") against any losses, damages and costs (collectively "Losses") arising from claims, lawsuits and other legal actions brought by a third party against the Indemnified Contributor to the extent caused by the acts or omissions of such Commercial Contributor in connection with its distribution of the Program in a commercial product offering. The obligations in this section do not apply to any claims or Losses relating to any actual or alleged intellectual property infringement. In order to qualify, an Indemnified Contributor must: a) promptly notify the Commercial Contributor in writing of such claim, and b) allow the Commercial Contributor to control, and cooperate with the Commercial Contributor in, the defense and any related settlement negotiations. The Indemnified Contributor may participate in any such claim at its own expense.

For example, a Contributor might include the Program in a commercial product offering, Product X. That Contributor is then a Commercial Contributor. If that Commercial Contributor then makes performance claims, or offers warranties related to Product X, those performance claims and warranties are such Commercial Contributor's responsibility alone. Under this section, the Commercial Contributor would have to defend claims against the other Contributors related to those performance claims and warranties, and if a court requires any other Contributor to pay any damages as a result, the Commercial Contributor must pay those damages.

1. NO WARRANTY

EXCEPT AS EXPRESSLY SET FORTH IN THIS AGREEMENT, THE PROGRAM IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, EITHER EXPRESS OR IMPLIED INCLUDING, WITHOUT LIMITATION, ANY WARRANTIES OR CONDITIONS OF TITLE, NON-INFRINGEMENT, MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE. Each Recipient is solely responsible for determining the appropriateness of using and distributing the Program and assumes all risks associated with its exercise of rights under this Agreement , including but not limited to the risks and costs of program errors, compliance with applicable laws, damage to or loss of data, programs or equipment, and unavailability or interruption of operations.

1. DISCLAIMER OF LIABILITY

EXCEPT AS EXPRESSLY SET FORTH IN THIS AGREEMENT, NEITHER RECIPIENT NOR ANY CONTRIBUTORS SHALL HAVE ANY LIABILITY FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING WITHOUT LIMITATION LOST PROFITS), HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OR DISTRIBUTION OF THE PROGRAM OR THE EXERCISE OF ANY RIGHTS GRANTED HEREUNDER, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.

1. GENERAL

If any provision of this Agreement is invalid or unenforceable under applicable law, it shall not affect the validity or enforceability of the remainder of the terms of this Agreement, and without further action by the parties hereto, such provision shall be reformed to the minimum extent necessary to make such provision valid and enforceable.

If Recipient institutes patent litigation against any entity (including a cross-claim or counterclaim in a lawsuit) alleging that the Program itself (excluding combinations of the Program with other software or hardware) infringes such Recipient's patent(s), then such Recipient's rights granted under Section 2(b) shall terminate as of the date such litigation is filed.

All Recipient's rights under this Agreement shall terminate if it fails to comply with any of the material terms or conditions of this Agreement and does not cure such failure in a reasonable period of time after becoming aware of such noncompliance. If all Recipient's rights under this Agreement terminate, Recipient agrees to cease use and distribution of the Program as soon as reasonably practicable. However, Recipient's obligations under this Agreement and any licenses granted by Recipient relating to the Program shall continue and survive.

Everyone is permitted to copy and distribute copies of this Agreement, but in order to avoid inconsistency the Agreement is copyrighted and may only be modified in the following manner. The Agreement Steward reserves the right to publish new versions (including revisions) of this Agreement from time to time. No one other than the Agreement Steward has the right to modify this Agreement. The Eclipse Foundation is the initial Agreement Steward. The Eclipse Foundation may assign the responsibility to serve as the Agreement Steward to a suitable separate entity. Each new version of the Agreement will be given a distinguishing version number. The Program (including Contributions) may always be distributed subject to the version of the Agreement under which it was received. In addition, after a new version of the Agreement is published, Contributor may elect to distribute the Program (including its Contributions) under the new version. Except as expressly stated in Sections 2(a) and 2(b) above, Recipient receives no rights or licenses to the intellectual property of any Contributor under this Agreement, whether expressly, by implication, estoppel or otherwise. All rights in the Program not expressly granted under this Agreement are reserved.

This Agreement is governed by the laws of the State of New York and the intellectual property laws of the United States of America. No party to this Agreement will bring a legal action under this Agreement more than one year after the cause of action arose. Each party waives its rights to a jury trial in any resulting litigation.

<!--
  
  This was created by CodeLicenseManager
-->
## GNU Public License version v2

                    GNU GENERAL PUBLIC LICENSE
                       Version 2, June 1991
        
         Copyright (C) 1989, 1991 Free Software Foundation, Inc.,
         51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
         Everyone is permitted to copy and distribute verbatim copies
         of this license document, but changing it is not allowed.
        
                        Preamble
        
          The licenses for most software are designed to take away your
        freedom to share and change it.  By contrast, the GNU General Public
        License is intended to guarantee your freedom to share and change free
        software--to make sure the software is free for all its users.  This
        General Public License applies to most of the Free Software
        Foundation's software and to any other program whose authors commit to
        using it.  (Some other Free Software Foundation software is covered by
        the GNU Lesser General Public License instead.)  You can apply it to
        your programs, too.
        
          When we speak of free software, we are referring to freedom, not
        price.  Our General Public Licenses are designed to make sure that you
        have the freedom to distribute copies of free software (and charge for
        this service if you wish), that you receive source code or can get it
        if you want it, that you can change the software or use pieces of it
        in new free programs; and that you know you can do these things.
        
          To protect your rights, we need to make restrictions that forbid
        anyone to deny you these rights or to ask you to surrender the rights.
        These restrictions translate to certain responsibilities for you if you
        distribute copies of the software, or if you modify it.
        
          For example, if you distribute copies of such a program, whether
        gratis or for a fee, you must give the recipients all the rights that
        you have.  You must make sure that they, too, receive or can get the
        source code.  And you must show them these terms so they know their
        rights.
        
          We protect your rights with two steps: (1) copyright the software, and
        (2) offer you this license which gives you legal permission to copy,
        distribute and/or modify the software.
        
          Also, for each author's protection and ours, we want to make certain
        that everyone understands that there is no warranty for this free
        software.  If the software is modified by someone else and passed on, we
        want its recipients to know that what they have is not the original, so
        that any problems introduced by others will not reflect on the original
        authors' reputations.
        
          Finally, any free program is threatened constantly by software
        patents.  We wish to avoid the danger that redistributors of a free
        program will individually obtain patent licenses, in effect making the
        program proprietary.  To prevent this, we have made it clear that any
        patent must be licensed for everyone's free use or not licensed at all.
        
          The precise terms and conditions for copying, distribution and
        modification follow.
        
                    GNU GENERAL PUBLIC LICENSE
           TERMS AND CONDITIONS FOR COPYING, DISTRIBUTION AND MODIFICATION
        
          0. This License applies to any program or other work which contains
        a notice placed by the copyright holder saying it may be distributed
        under the terms of this General Public License.  The "Program", below,
        refers to any such program or work, and a "work based on the Program"
        means either the Program or any derivative work under copyright law:
        that is to say, a work containing the Program or a portion of it,
        either verbatim or with modifications and/or translated into another
        language.  (Hereinafter, translation is included without limitation in
        the term "modification".)  Each licensee is addressed as "you".
        
        Activities other than copying, distribution and modification are not
        covered by this License; they are outside its scope.  The act of
        running the Program is not restricted, and the output from the Program
        is covered only if its contents constitute a work based on the
        Program (independent of having been made by running the Program).
        Whether that is true depends on what the Program does.
        
          1. You may copy and distribute verbatim copies of the Program's
        source code as you receive it, in any medium, provided that you
        conspicuously and appropriately publish on each copy an appropriate
        copyright notice and disclaimer of warranty; keep intact all the
        notices that refer to this License and to the absence of any warranty;
        and give any other recipients of the Program a copy of this License
        along with the Program.
        
        You may charge a fee for the physical act of transferring a copy, and
        you may at your option offer warranty protection in exchange for a fee.
        
          2. You may modify your copy or copies of the Program or any portion
        of it, thus forming a work based on the Program, and copy and
        distribute such modifications or work under the terms of Section 1
        above, provided that you also meet all of these conditions:
        
            a) You must cause the modified files to carry prominent notices
            stating that you changed the files and the date of any change.
        
            b) You must cause any work that you distribute or publish, that in
            whole or in part contains or is derived from the Program or any
            part thereof, to be licensed as a whole at no charge to all third
            parties under the terms of this License.
        
            c) If the modified program normally reads commands interactively
            when run, you must cause it, when started running for such
            interactive use in the most ordinary way, to print or display an
            announcement including an appropriate copyright notice and a
            notice that there is no warranty (or else, saying that you provide
            a warranty) and that users may redistribute the program under
            these conditions, and telling the user how to view a copy of this
            License.  (Exception: if the Program itself is interactive but
            does not normally print such an announcement, your work based on
            the Program is not required to print an announcement.)
        
        These requirements apply to the modified work as a whole.  If
        identifiable sections of that work are not derived from the Program,
        and can be reasonably considered independent and separate works in
        themselves, then this License, and its terms, do not apply to those
        sections when you distribute them as separate works.  But when you
        distribute the same sections as part of a whole which is a work based
        on the Program, the distribution of the whole must be on the terms of
        this License, whose permissions for other licensees extend to the
        entire whole, and thus to each and every part regardless of who wrote it.
        
        Thus, it is not the intent of this section to claim rights or contest
        your rights to work written entirely by you; rather, the intent is to
        exercise the right to control the distribution of derivative or
        collective works based on the Program.
        
        In addition, mere aggregation of another work not based on the Program
        with the Program (or with a work based on the Program) on a volume of
        a storage or distribution medium does not bring the other work under
        the scope of this License.
        
          3. You may copy and distribute the Program (or a work based on it,
        under Section 2) in object code or executable form under the terms of
        Sections 1 and 2 above provided that you also do one of the following:
        
            a) Accompany it with the complete corresponding machine-readable
            source code, which must be distributed under the terms of Sections
            1 and 2 above on a medium customarily used for software interchange; or,
        
            b) Accompany it with a written offer, valid for at least three
            years, to give any third party, for a charge no more than your
            cost of physically performing source distribution, a complete
            machine-readable copy of the corresponding source code, to be
            distributed under the terms of Sections 1 and 2 above on a medium
            customarily used for software interchange; or,
        
            c) Accompany it with the information you received as to the offer
            to distribute corresponding source code.  (This alternative is
            allowed only for noncommercial distribution and only if you
            received the program in object code or executable form with such
            an offer, in accord with Subsection b above.)
        
        The source code for a work means the preferred form of the work for
        making modifications to it.  For an executable work, complete source
        code means all the source code for all modules it contains, plus any
        associated interface definition files, plus the scripts used to
        control compilation and installation of the executable.  However, as a
        special exception, the source code distributed need not include
        anything that is normally distributed (in either source or binary
        form) with the major components (compiler, kernel, and so on) of the
        operating system on which the executable runs, unless that component
        itself accompanies the executable.
        
        If distribution of executable or object code is made by offering
        access to copy from a designated place, then offering equivalent
        access to copy the source code from the same place counts as
        distribution of the source code, even though third parties are not
        compelled to copy the source along with the object code.
        
          4. You may not copy, modify, sublicense, or distribute the Program
        except as expressly provided under this License.  Any attempt
        otherwise to copy, modify, sublicense or distribute the Program is
        void, and will automatically terminate your rights under this License.
        However, parties who have received copies, or rights, from you under
        this License will not have their licenses terminated so long as such
        parties remain in full compliance.
        
          5. You are not required to accept this License, since you have not
        signed it.  However, nothing else grants you permission to modify or
        distribute the Program or its derivative works.  These actions are
        prohibited by law if you do not accept this License.  Therefore, by
        modifying or distributing the Program (or any work based on the
        Program), you indicate your acceptance of this License to do so, and
        all its terms and conditions for copying, distributing or modifying
        the Program or works based on it.
        
          6. Each time you redistribute the Program (or any work based on the
        Program), the recipient automatically receives a license from the
        original licensor to copy, distribute or modify the Program subject to
        these terms and conditions.  You may not impose any further
        restrictions on the recipients' exercise of the rights granted herein.
        You are not responsible for enforcing compliance by third parties to
        this License.
        
          7. If, as a consequence of a court judgment or allegation of patent
        infringement or for any other reason (not limited to patent issues),
        conditions are imposed on you (whether by court order, agreement or
        otherwise) that contradict the conditions of this License, they do not
        excuse you from the conditions of this License.  If you cannot
        distribute so as to satisfy simultaneously your obligations under this
        License and any other pertinent obligations, then as a consequence you
        may not distribute the Program at all.  For example, if a patent
        license would not permit royalty-free redistribution of the Program by
        all those who receive copies directly or indirectly through you, then
        the only way you could satisfy both it and this License would be to
        refrain entirely from distribution of the Program.
        
        If any portion of this section is held invalid or unenforceable under
        any particular circumstance, the balance of the section is intended to
        apply and the section as a whole is intended to apply in other
        circumstances.
        
        It is not the purpose of this section to induce you to infringe any
        patents or other property right claims or to contest validity of any
        such claims; this section has the sole purpose of protecting the
        integrity of the free software distribution system, which is
        implemented by public license practices.  Many people have made
        generous contributions to the wide range of software distributed
        through that system in reliance on consistent application of that
        system; it is up to the author/donor to decide if he or she is willing
        to distribute software through any other system and a licensee cannot
        impose that choice.
        
        This section is intended to make thoroughly clear what is believed to
        be a consequence of the rest of this License.
        
          8. If the distribution and/or use of the Program is restricted in
        certain countries either by patents or by copyrighted interfaces, the
        original copyright holder who places the Program under this License
        may add an explicit geographical distribution limitation excluding
        those countries, so that distribution is permitted only in or among
        countries not thus excluded.  In such case, this License incorporates
        the limitation as if written in the body of this License.
        
          9. The Free Software Foundation may publish revised and/or new versions
        of the General Public License from time to time.  Such new versions will
        be similar in spirit to the present version, but may differ in detail to
        address new problems or concerns.
        
        Each version is given a distinguishing version number.  If the Program
        specifies a version number of this License which applies to it and "any
        later version", you have the option of following the terms and conditions
        either of that version or of any later version published by the Free
        Software Foundation.  If the Program does not specify a version number of
        this License, you may choose any version ever published by the Free Software
        Foundation.
        
          10. If you wish to incorporate parts of the Program into other free
        programs whose distribution conditions are different, write to the author
        to ask for permission.  For software which is copyrighted by the Free
        Software Foundation, write to the Free Software Foundation; we sometimes
        make exceptions for this.  Our decision will be guided by the two goals
        of preserving the free status of all derivatives of our free software and
        of promoting the sharing and reuse of software generally.
        
                        NO WARRANTY
        
          11. BECAUSE THE PROGRAM IS LICENSED FREE OF CHARGE, THERE IS NO WARRANTY
        FOR THE PROGRAM, TO THE EXTENT PERMITTED BY APPLICABLE LAW.  EXCEPT WHEN
        OTHERWISE STATED IN WRITING THE COPYRIGHT HOLDERS AND/OR OTHER PARTIES
        PROVIDE THE PROGRAM "AS IS" WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED
        OR IMPLIED, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
        MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE.  THE ENTIRE RISK AS
        TO THE QUALITY AND PERFORMANCE OF THE PROGRAM IS WITH YOU.  SHOULD THE
        PROGRAM PROVE DEFECTIVE, YOU ASSUME THE COST OF ALL NECESSARY SERVICING,
        REPAIR OR CORRECTION.
        
          12. IN NO EVENT UNLESS REQUIRED BY APPLICABLE LAW OR AGREED TO IN WRITING
        WILL ANY COPYRIGHT HOLDER, OR ANY OTHER PARTY WHO MAY MODIFY AND/OR
        REDISTRIBUTE THE PROGRAM AS PERMITTED ABOVE, BE LIABLE TO YOU FOR DAMAGES,
        INCLUDING ANY GENERAL, SPECIAL, INCIDENTAL OR CONSEQUENTIAL DAMAGES ARISING
        OUT OF THE USE OR INABILITY TO USE THE PROGRAM (INCLUDING BUT NOT LIMITED
        TO LOSS OF DATA OR DATA BEING RENDERED INACCURATE OR LOSSES SUSTAINED BY
        YOU OR THIRD PARTIES OR A FAILURE OF THE PROGRAM TO OPERATE WITH ANY OTHER
        PROGRAMS), EVEN IF SUCH HOLDER OR OTHER PARTY HAS BEEN ADVISED OF THE
        POSSIBILITY OF SUCH DAMAGES.
        
                     END OF TERMS AND CONDITIONS
        
                How to Apply These Terms to Your New Programs
        
          If you develop a new program, and you want it to be of the greatest
        possible use to the public, the best way to achieve this is to make it
        free software which everyone can redistribute and change under these terms.
        
          To do so, attach the following notices to the program.  It is safest
        to attach them to the start of each source file to most effectively
        convey the exclusion of warranty; and each file should have at least
        the "copyright" line and a pointer to where the full notice is found.
        
            <one line to give the program's name and a brief idea of what it does.>
            Copyright (C) <year>  <name of author>
        
            This program is free software; you can redistribute it and/or modify
            it under the terms of the GNU General Public License as published by
            the Free Software Foundation; either version 2 of the License, or
            (at your option) any later version.
        
            This program is distributed in the hope that it will be useful,
            but WITHOUT ANY WARRANTY; without even the implied warranty of
            MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
            GNU General Public License for more details.
        
            You should have received a copy of the GNU General Public License along
            with this program; if not, write to the Free Software Foundation, Inc.,
            51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
        
        Also add information on how to contact you by electronic and paper mail.
        
        If the program is interactive, make it output a short notice like this
        when it starts in an interactive mode:
        
            Gnomovision version 69, Copyright (C) year name of author
            Gnomovision comes with ABSOLUTELY NO WARRANTY; for details type `show w'.
            This is free software, and you are welcome to redistribute it
            under certain conditions; type `show c' for details.
        
        The hypothetical commands `show w' and `show c' should show the appropriate
        parts of the General Public License.  Of course, the commands you use may
        be called something other than `show w' and `show c'; they could even be
        mouse-clicks or menu items--whatever suits your program.
        
        You should also get your employer (if you work as a programmer) or your
        school, if any, to sign a "copyright disclaimer" for the program, if
        necessary.  Here is a sample; alter the names:
        
          Yoyodyne, Inc., hereby disclaims all copyright interest in the program
          `Gnomovision' (which makes passes at compilers) written by James Hacker.
        
          <signature of Ty Coon>, 1 April 1989
          Ty Coon, President of Vice
        
        This General Public License does not permit incorporating your program into
        proprietary programs.  If your program is a subroutine library, you may
        consider it more useful to permit linking proprietary applications with the
        library.  If this is what you want to do, use the GNU Lesser General
        Public License instead of this License.

## OSGi Specification License, Version 2.0.

License Grant

OSGi Alliance (“OSGi”) hereby grants you a fully-paid, non-exclusive, non-transferable, worldwide, limited license (without the right to sublicense), under OSGi’s applicable intellectual property rights to view, download, and reproduce this OSGi Specification (“Specification”) which follows this License Agreement (“Agreement”). You are not authorized to create any derivative work of the Specification. However, to the extent that an implementation of the Specification would necessarily be a derivative work of the Specification, OSGi also grants you a perpetual, non-exclusive, worldwide, fully paid-up, royalty free, limited license (without the right to sublicense) under any applicable copyrights, to create and/or distribute an implementation of the Specification that: (i) fully implements the Specification including all its required interfaces and functionality; (ii) does not modify, subset, superset or otherwise extend the OSGi Name Space, or include any public or protected packages, classes, Java interfaces, fields or methods within the OSGi Name Space other than those required and authorized by the Specification. An implementation that does not satisfy limitations (i)-(ii) is not considered an implementation of the Specification, does not receive the benefits of this license, and must not be described as an implementation of the Specification. An implementation of the Specification must not claim to be a compliant implementation of the Specification unless it passes the OSGi Compliance Tests for the Specification in accordance with OSGi processes. “OSGi Name Space” shall mean the public class or interface declarations whose names begin with “org.osgi" or any recognized successors or replacements thereof.

OSGi Participants (as such term is defined in the OSGi Intellectual Property Rights Policy) have made non-assert and licensing commitments regarding patent claims necessary to implement the Specification, if any, under the OSGi Intellectual Property Rights Policy which is available for examination on the OSGi public web site (www.osgi.org).

No Warranties and Limitation of Liability

THE SPECIFICATION IS PROVIDED "AS IS," AND OSGi AND ANY OTHER AUTHORS MAKE NO REPRESENTATIONS OR WARRANTIES, EXPRESS OR IMPLIED, INCLUDING, BUT NOT LIMITED TO, WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, NON-INFRINGEMENT, OR TITLE; THAT THE CONTENTS OF THE SPECIFICATION ARE SUITABLE FOR ANY PURPOSE; NOR THAT THE IMPLEMENTATION OF SUCH CONTENTS WILL NOT INFRINGE ANY THIRD PARTY PATENTS, COPYRIGHTS, TRADEMARKS OR OTHER RIGHTS. OSGi AND ANY OTHER AUTHORS WILL NOT BE LIABLE FOR ANY DIRECT, INDIRECT, SPECIAL, INCIDENTAL OR CONSEQUENTIAL DAMAGES ARISING OUT OF ANY USE OF THE SPECIFICATION OR THE PERFORMANCE OR IMPLEMENTATION OF THE CONTENTS THEREOF.

Covenant Not to Assert

As a material condition to this license you hereby agree, to the extent that you have any patent claims which are necessarily infringed by an implementation of the Specification, not to assert any such patent claims against the creation, distribution or use of an implementation of the Specification.

General

The name and trademarks of OSGi or any other Authors may NOT be used in any manner, including advertising or publicity pertaining to the Specification or its contents without specific, written prior permission. Title to copyright in the Specification will at all times remain with OSGi.

No other rights are granted by implication, estoppel or otherwise.

