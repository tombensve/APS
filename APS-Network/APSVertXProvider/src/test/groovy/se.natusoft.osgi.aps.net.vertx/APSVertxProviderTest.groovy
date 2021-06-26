package se.natusoft.osgi.aps.net.vertx

import groovy.transform.CompileStatic
import io.vertx.core.Vertx
import io.vertx.core.eventbus.EventBus
import io.vertx.core.shareddata.SharedData
import io.vertx.ext.web.Router
import org.junit.Test
import org.osgi.framework.ServiceReference
import se.natusoft.osgi.aps.activator.APSActivator
import se.natusoft.osgi.aps.activator.annotation.Initializer
import se.natusoft.osgi.aps.activator.annotation.Managed
import se.natusoft.osgi.aps.activator.annotation.APSPlatformService
import se.natusoft.osgi.aps.runtime.APSRuntime
import se.natusoft.osgi.aps.tracker.APSServiceTracker
import se.natusoft.osgi.aps.util.APSLogger

import static java.util.concurrent.TimeUnit.SECONDS

@CompileStatic
class APSVertxProviderTest extends APSRuntime {

    public static Vertx vertx = null
    public static Router router = null
    public static EventBus eventBus = null
    public static SharedData sharedData = null

    @Test
    void runTest() throws Exception {
        // Most of the unfamiliar constructs here are provided by APSRuntime and groovy DSL features.

        try {

            // Instead of reading from content of jar file on disk we provide the same result hardcoded.

            // Instead of reading bundle content from target/classes we provide hardcoded paths here
            // due to that the docker filesystem lags behind, probably due to its versioning filesystem,
            // so when this is executed in a docker container the target/classes path is empty. But
            // with this hardcoded content of bundle we get around that.
            //
            // This however have the downside of having to update this if content is changed in the jar.

            deployConfigAndVertxPlusDeps( vertxDeployer( null ) {
                deploy 'aps-vertx-provider' with new APSActivator() manifest_from this.getClass(  ) using'/se/natusoft/osgi/aps/net/vertx/APSAmqpBridgeBusRouter.class',
                '/se/natusoft/osgi/aps/net/vertx/APSVertxProvider.class',
                '/se/natusoft/osgi/aps/net/vertx/MessageSenderProvider.class',
                '/se/natusoft/osgi/aps/net/vertx/MessageSubscriberProvider.class',
                '/se/natusoft/osgi/aps/net/vertx/APSMessageProvider.class',
                '/se/natusoft/osgi/aps/net/vertx/APSVertxBusRouter.class',
                '/se/natusoft/osgi/aps/net/vertx/APSVertxProvider$1.class',
                '/se/natusoft/osgi/aps/net/vertx/util/RecursiveJsonObjectMap.class'

            } )


        }
        catch ( Exception e ) {
            e.printStackTrace( System.err )
        }

        hold() maxTime 2L unit SECONDS go()

        deploy 'vertx-client' with new APSActivator() using '/se/natusoft/osgi/aps/net/vertx/VertxClient.class'

        try {

            hold() whilst {
                vertx == null || router == null || eventBus == null || sharedData == null
            } maxTime 10L unit SECONDS go()

            assert vertx != null
            println ">>>> Got Vertx!"
            assert router != null
            println ">>>> Got router!"
            assert eventBus != null
            println ">>>> Got event bus!"
            assert sharedData != null
            println ">>>> Got shared data!"

        }
        finally {
            shutdown()
            hold() maxTime 3 unit SECONDS go() // Give Vertx time to shut down.
        }
    }
}

@SuppressWarnings( "GroovyUnusedDeclaration" )
@CompileStatic
class VertxClient {

    @Managed( loggingFor = "Test:VertxClient" )
    private APSLogger logger

    @APSPlatformService( additionalSearchCriteria = "(vertx-object=Vertx)", timeout = "10 sec" )
    APSServiceTracker<Vertx> vertxTracker

    @APSPlatformService( additionalSearchCriteria = "(&(vertx-object=Router)(vertx-router=default))", timeout = "10 sec" )
    APSServiceTracker<Router> routerTracker

    @APSPlatformService( additionalSearchCriteria = "(vertx-object=EventBus)", timeout = "10 sec" )
    APSServiceTracker<EventBus> eventBusTracker

    @APSPlatformService( additionalSearchCriteria = "(vertx-object=SharedData)", timeout = "10 sec" )
    APSServiceTracker<SharedData> sharedDataTracker

    @Initializer
    void init() {
        this.logger.info "In VertxClient.init()!"

        // Non blocking way of getting a service.
        //
        // One way not demonstrated here is to add nonBlocking=true to the @OSGiService
        // annotation. This also requires a reactive API for the service, that is void
        // return and callbacks for any result. In this case calls are cached until service
        // becomes available and executed then.

        // This demonstrates one way of handling callbacks. This coerces method reference to interface impl.
        this.vertxTracker.onActiveServiceAvailable = this.&onVertxAvailable
        this.vertxTracker.onActiveServiceLeaving = this.&onVertxLeaving

        // This demonstrates another using a closure.
        this.routerTracker.onActiveServiceAvailable { Router router, ServiceReference routerRef ->

            APSVertxProviderTest.router = router
            this.logger.info "Got router!"

        }.onServiceLeaving { ServiceReference sr, Class api ->

            this.logger.info "Router leaving!"
        }

        // If you think this looks better, it is also possible! This is actually the same as this.vertxTracker
        // above, but with a closure instead.
        this.eventBusTracker.onActiveServiceAvailable = { EventBus eventBus, ServiceReference busRef ->

            APSVertxProviderTest.eventBus = eventBus
            this.logger.info "Got event bus!"
        }

        this.sharedDataTracker.onActiveServiceAvailable = { SharedData sharedData, ServiceReference busRef ->

            APSVertxProviderTest.sharedData = sharedData
            this.logger.info "Got shared data!"
        }
    }

    private void onVertxAvailable( Vertx vertx, ServiceReference vertxRef ) {

        APSVertxProviderTest.vertx = vertx
        this.logger.info "Got vertx!"
    }

    private void onVertxLeaving( ServiceReference vertxRef, Class vertxAPIClass ) {

        this.logger.info "Vertx going away!"
    }
}

