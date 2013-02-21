# APSSimpleUserServiceAuthServiceProvider

This provides an APSAuthService that uses the APSSimpleUserService to authenticate users. It only supports password authentication. The ”/apsadminweb” APS administration web and APSExtProtocolHTTPTransportProvider uses APSAuthService to authenticate. If you don’t have another implementation of this service you can use this and also deploy APSSimpleUserService.

