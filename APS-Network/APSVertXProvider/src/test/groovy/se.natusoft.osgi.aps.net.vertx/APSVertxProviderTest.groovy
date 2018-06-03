package se.natusoft.osgi.aps.net.vertx

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import io.vertx.core.Vertx
import io.vertx.core.eventbus.EventBus
import io.vertx.core.shareddata.SharedData
import io.vertx.ext.web.Router
import org.junit.Test
import org.osgi.framework.ServiceReference
import se.natusoft.osgi.aps.activator.APSActivator
import se.natusoft.osgi.aps.activator.annotation.Initializer
import se.natusoft.osgi.aps.activator.annotation.Managed
import se.natusoft.osgi.aps.activator.annotation.OSGiService
import se.natusoft.osgi.aps.test.tools.OSGIServiceTestTools
import se.natusoft.osgi.aps.tracker.APSServiceTracker
import se.natusoft.osgi.aps.util.APSLogger

import static java.util.concurrent.TimeUnit.SECONDS

@CompileStatic
@TypeChecked
class APSVertxProviderTest extends OSGIServiceTestTools {

    public static Vertx vertx = null
    public static Router router = null
    public static EventBus eventBus = null
    public static SharedData sharedData = null

    @Test
    void reactiveAPITest() throws Exception {
        // Most of the unfamiliar constructs here are provided by OSGiServiceTestTools and groovy DSL features.

        deployConfigManager() {

            deploy 'aps-vertx-provider' with new APSActivator() from 'APS-Network/APSVertxProvider/target/classes'
        }

        hold() maxTime 2L unit SECONDS go()

        deploy 'vertx-client' with new APSActivator() using '/se/natusoft/osgi/aps/net/vertx/VertxClient.class'

        try {
            hold() whilst { vertx == null } maxTime 6L unit SECONDS go()
            hold() whilst { router == null } maxTime 6L unit SECONDS go()
            hold() whilst { eventBus == null } maxTime 6L unit SECONDS go()
            hold() whilst { sharedData == null } maxTime 6L unit SECONDS go()

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
@TypeChecked
class VertxClient {

    @Managed( loggingFor = "Test:VertxClient" )
    private APSLogger logger

    @OSGiService( additionalSearchCriteria = "(vertx-object=Vertx)", timeout = "10 sec" )
    APSServiceTracker<Vertx> vertxTracker

    @OSGiService( additionalSearchCriteria = "(&(vertx-object=Router)(vertx-router=test))", timeout = "10 sec" )
    APSServiceTracker<Router> routerTracker

    @OSGiService( additionalSearchCriteria = "(vertx-object=EventBus)", timeout = "10 sec" )
    APSServiceTracker<EventBus> eventBusTracker

    @OSGiService( additionalSearchCriteria = "(vertx-object=SharedData)", timeout = "10 sec" )
    APSServiceTracker<SharedData> sharedDataTracker

    @Initializer
    void init() {
        this.logger.info "In VertxClient.init()!"

        // Non blocking way of getting a service. If we inject it as a proxy of Vertx instead of the tracker
        // then the first call might block if the service is not yet available. This can cause deadlocks if it
        // is done on bundle startup in an @Initializer method!

        this.vertxTracker.onActiveServiceAvailable = this.&onVertxAvailable
        this.vertxTracker.onServiceLeaving = this.&onVertxLeaving

        // Here we listen to the active, which ever works.
        this.routerTracker.onActiveServiceAvailable { Router router, ServiceReference routerRef ->

            APSVertxProviderTest.router = router
            this.logger.info "Got router!"

        }.onServiceLeaving { ServiceReference sr, Class api ->

            this.logger.info "Router leaving!"
        }

        this.eventBusTracker.onActiveServiceAvailable { EventBus eventBus, ServiceReference busRef ->

            APSVertxProviderTest.eventBus = eventBus
            this.logger.info "Got event bus!"
        }

        this.sharedDataTracker.onActiveServiceAvailable { SharedData sharedData, ServiceReference busRef ->

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

