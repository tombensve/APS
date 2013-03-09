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

>  This represents information required for setting upp a JDBC data source. 

__String getName()__

>  

_Returns_

> The name of this data source definition. This information is optional and can return null!

__String getConnectionURL()__

>  

_Returns_

> The JDBC connection URL. Ex: jdbc:provider://host:port/database[;properties].

__String getConnectionDriveName()__

>  

_Returns_

> The fully qualified class name of the JDBC driver to use.

__String getConnectionUserName()__

>  

_Returns_

> The name of the database user to login as.

__String getConnectionPassword()__

>  

_Returns_

> The password for the database user.

}

----

    

public _interface_ __APSDataSourceDefService__   [se.natusoft.osgi.aps.api.data.jdbc.service] {

>  This service provides lookup of configured data source definitions. These can be used to setup connection pools, JPA, ... 

__DataSourceDef lookupByName(String name)__

>  Looks up a data source definition by its configured name.  

_Returns_

> A DataSourceDef or null if name was not valid.

_Parameters_

> _name_ - The name to lookup. 

__List<DataSourceDef> getAllDefinitions()__

>  

_Returns_

> All available definitions.

}

----

    

