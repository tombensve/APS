## aps-vertx-provider

The idea for this is to provide and manage all Vertx subservices. This currently provides a Vertx instance, HTTP server routers, and the eventbus. In the end it will provide TCP services also.

Note that this is not a service in itself! To get the Vertx instances you must publish an APSConsumer\<Object\> and provide the following properties with the service:

    consumed=vertx

To consume a http router then the following property must also be available:

    http-service-name=name

The name is a reference to a named configuration for aps-vertx-provider that provides the port to listen to.

When used in Groovy you cannot implement multiple APSConsumer of different types, I think Java do support that. To be compatible with Groovy (much of the APS code is in Groovy) this bundle exports a VertxConsumer trait that consumes an _Object_ and looks at what was received and passes it on to different registered closures.

When Vertx is available a vertx instance will be published with a meta status of _published_. If this bundle is taken down there will be a publish with a meta status of _revoked_ to indicate that the verx instance and its sub-services are no longer valid.

Here is an example of a consumer:

    @OSGiServiceProvider(properties = [
                @OSGiProperty(name = "consumed", value = "vertx"),
                @OSGiProperty(name = APSVertx.HTTP_SERVICE_NAME, value = "test")
        ])
        @CompileStatic
        @TypeChecked
        // Important: Service interface must be the first after "implements"!! Otherwise serviceAPIs=[APSConsumer.class] must be
        // specified in @OSGiServiceProvider annotation.
        class VertxConsumerService implements APSConsumer<Vertx>, VertxConsumer {

            @Managed(loggingFor = "Test:VertxConsumerService")
            APSLogger logger

            VertxConsumerService() {
                this.onVertxAvailable = { Vertx vertx ->
                    this.logger.info( "Received Vertx instance! [${vertx}]" )
                    ...
                }
                this.onVertxRevoked = {
                    this.logger.info( "Vertx instance revoked!" )
                    ...
                }
                this.onRouterAvailable = { Router router ->
                    this.logger.info( "Received Router instance! [${router}]" )
                    ...
                }
                this.onError = { String message ->
                    this.logger.error( message )
                }
            }
        }

