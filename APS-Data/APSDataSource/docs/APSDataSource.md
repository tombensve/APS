# APSDataSource

This is a service that provides named data source definitions. It does __not__ provided pooled _javax.sql.DataSource_ instances!! It only provides definitions with connection url, driver name, user and password. This service can be used by other services that provide DataSource pooling for example. The APSSimpleUserServiceProvider makes use of this service by looking up ”APSSimpleUserServiceDS” passing the information on to the APSJPAService in its properties. Not everything can make use of an _javax.sql.DataSource_, but everything can make use of the information provided by this service.

The actual data source definitions are configured in the _/apsadminweb_ under configuration group ”persistence”. 

## APIs

The complete APS javadoc can be found at [http://apidoc.natusoft.se/APS/](http://apidoc.natusoft.se/APS/).

