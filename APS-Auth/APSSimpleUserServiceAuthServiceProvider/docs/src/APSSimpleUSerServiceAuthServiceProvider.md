#APSAuthService

This is a very simple little service that only does authentication of users. This service is currently used by the APS administration web (/apsadminweb) and APSExtProtocolHTTPTransportProvider for remote calls to services over http. 

The idea behind this service is that it should be easy to provide an implementation of this that uses whatever authentication scheme you want/need. If you have an LDAP server you want to authenticate against for example, provide an implementation that looks up and authenticates the user against the LDAP server. 

See this a little bit like an authentication plugin. 

The APS web applications that use this only uses password authentication.

## APSSimpleUserServiceAuthServiceProvider

This provides an APSAuthService that uses the APSSimpleUserService to authenticate users. It only supports password authentication. If you don't have your own implementation of APSAuthService then you can deploy this one along with APSSimpleUserService, and probably APSUserAdminWeb. 

**Please note** however that the standard implementation of APSSimpleUserService can register several instances with an "instance=name" property where name is unique for each instance, and each instance can reference a different data source. This is configured under _persistence/dsrefs_ in the configuration. If no instances are configured an instance of "aps-admin-web" will be created by default. If instances are configured the default will not be created. And now the the point: APSSimpleuserServiceAuthServiceProvider will as of now track the "aps-admin-web" instance of APSSimpleUserService! If no such instance is configured it will fail after a timeout of not finding a service!

## API
