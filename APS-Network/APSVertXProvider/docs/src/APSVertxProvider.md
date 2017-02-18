## aps-vertx-provider

This provides reusable instances of Vertx. The _APSVertxService_ API takes a name. For the same name you get the same instance. The other APS services using Vertx uses the name "default". You can specify any name. If an instance for the specified name does not exist it will be created. Note however that created instances are not stopped until the bundle is stopped!

**Note**: The returned Vertx instance is a Groovy instance! You can always do getDelegate() on it to get the orignal Java instance. Note however that all Vertx using APS services and a lot of the other APS services are implemented in Groovy. So the Groovy runtime (2.4.7+) must be deployed as a bundle for this and much of APS to work. APSToolsLib and APS-APIs are pure java. Most of the other bundles are dependent on Groovy.

### Usage

There are 2 ways to use aps-vertx-provider. 

### APSVertxService

Use the APSVertxService:

    public class MyAPSActivatorManagedClass {
        
        @Managed
        APSLogger logger
    
        @OSGiService
        private APSVertxService vertxService
        
        private Vertx vertx
    
        @Initializer
        public void init() {
            vertxService.useGroovyVertX("default", { AsyncResult<Vertx> result ->
                if ( result.succeeded() ) {
                    this.vertx = result.result()
                    ...
                }
                else {
                    this.logger.error("Failed to start Vertx!")
                }
        }
    
        ...
    }

Or use the Hollywood principle: Don't call us, we call you!

    @OSGiServiceProvider
    class VertxConsumerService extends DataConsumer.DataConsumerProvider<Vertx> implements DataConsumer<Vertx> {
    
        @Managed
        private APSLogger logger
    
        private DataConsumer.DataHolder<Vertx> vertx
    
        /**
         * Called when there is updated data available.
         *
         * @param data The new data.
         */
        @Override
        void onDataAvailable(DataConsumer.DataHolder<Vertx> vertx) {
            this.vertx = vertx
            
            EventBus eventBus = this.vertx.use().eventBus()
            ...
    
            this.vertx.release()
        }
    
        /**
         * Called when there is a failure to deliver requested instance.
         *
         * Haven't found a way to make Vertx fail yet, so this will never be called.
         */
        @Override
        void onDataUnavailable() {
            logger.error("No vertx instance available!")
        }
    }

It is the APSVertxService that also listens to all `DataConsumer<Vertx>` services and calls them back with a Vertx instance. The received object is wrapped and you have to call `.use()` on it to get the real Vertx instance. `.release()` says that you are done with the instance, and when the useage count reaches 0 APSVertxService will shut down the Vertx cluster again.

This service allows for multiple Bundles and services to use the same Vertx instance acting as part of one application.  
         