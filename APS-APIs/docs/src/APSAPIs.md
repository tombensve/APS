# aps-apis

This contains the general APIs for standard services that other maven modules should implement. The APIs are actually one of the main points of APS. Its goal is to define trivially easy to use APIs for different things. Any needed complexity should be hidden within the API implementation and users should only have to deal with the simple API.

The project do provide a lot of implementations of the APIs. They are in 2 categories:

1. Intended to be deployed and used as is (ex: aps-vertx-provider).

2. A "default" implementation that can be copied and modified / configured to own need. 

For (1) there is of course nothing to stop it from being treated as (2) :-).

## Tools

### APSServiceLocator

This provides static methods:

    List<T> apsServices( Class<T> api);

Returns a list of services implementing the specified API. The list can of course be empty!

    T apsService( Class<T> api);

Returns first service found or null.

    List<T> apsServiceByAnnotation( Class<T> serviceApi, Class<Annotation> annotation );

Return services that has the specified annotation on them, which allows for filtering of services. This was inspired by ServiceLoader javadoc example. Use any annotations. 


### APSLogService

Use this service to log. How things are logged depends on the implementation provided.

