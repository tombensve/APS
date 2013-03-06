#APSAuthService

This is a very simple little service that only does authentication of users. This service is currently used by the APS administration web (/apsadminweb) and APSExtProtocolHTTPTransportProvider for remote calls to services over http. 

The idea behind this service is that it should be easy to provide an implementation of this that uses whatever authentication scheme you want/need. If you have an LDAP server you want to authenticate against for example, provide an implementation that looks up and authenticates the user against the LDAP server. 

See this a little bit like an authentication plugin. 

The APS web applications that use this only uses password authentication.

## APSSimpleUserServiceAuthServiceProvider

This provides an APSAuthService that uses the APSSimpleUserService to authenticate users. It only supports password authentication. If you don’t have your own implementation of APSAuthService then you can deploy this one along with
APSSimpleUserService, and probably APSUserAdminWeb.

## API
