package se.natusoft.osgi.aps.net.vertx

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import io.vertx.core.Vertx
import io.vertx.core.eventbus.EventBus
import io.vertx.ext.web.Router
import org.junit.Test
import org.osgi.framework.ServiceReference
import se.natusoft.osgi.aps.test.tools.OSGIServiceTestTools
import se.natusoft.osgi.aps.tools.APSActivator
import se.natusoft.osgi.aps.tools.APSLogger
import se.natusoft.osgi.aps.tools.APSServiceTracker
import se.natusoft.osgi.aps.tools.annotation.activator.Initializer
import se.natusoft.osgi.aps.tools.annotation.activator.Managed
import se.natusoft.osgi.aps.tools.annotation.activator.OSGiService

import static java.util.concurrent.TimeUnit.MILLISECONDS
import static java.util.concurrent.TimeUnit.SECONDS

@CompileStatic
@TypeChecked
class APSVertxProviderTest extends OSGIServiceTestTools {

    public static Vertx vertx = null
    public static Router router = null
    public static EventBus eventBus = null

    @Test
    void reactiveAPITest() throws Exception {
        // Most of the unfamiliar constructs here are provided by OSGiServiceTestTools and groovy DSL features.

        println "============================================================================"
        println "DO NOTE: All the RED colored output comes from Vertx! It is not something "
        println "that have failed! Vertx have just chosen this color for their log output!"
        println "============================================================================"

        deploy 'aps-vertx-provider' with new APSActivator() using '/se/natusoft/osgi/aps/net/vertx/APSVertxProvider.class'

        deploy 'vertx-client' with new APSActivator() using '/se/natusoft/osgi/aps/net/vertx/VertxClient.class'

        try {
            println ">>>>> " + new Date()
            hold() whilst { vertx == null } maxTime 6L unit SECONDS go()
            hold() whilst { router == null } maxTime 6L unit SECONDS go()
            hold() whilst { eventBus == null } maxTime 6L unit SECONDS go()
            println "<<<<< " + new Date()

            assert vertx != null
            assert router != null

        }
        finally {
            shutdown()
            hold() maxTime 500 unit MILLISECONDS go() // Give Vertx time to shut down.
        }
    }

}

@SuppressWarnings("GroovyUnusedDeclaration")
@CompileStatic
@TypeChecked
class VertxClient {

    @Managed(loggingFor = "Test:VertxClient")
    private APSLogger logger

    @OSGiService(additionalSearchCriteria = "(vertx-object=Vertx)", timeout="10 sec")
    APSServiceTracker<Vertx> vertxTracker

    @OSGiService(additionalSearchCriteria = "(&(vertx-object=Router)(vertx-router=test))", timeout="10 sec")
    APSServiceTracker<Router> routerTracker

    @OSGiService(additionalSearchCriteria = "(vertx-object=EventBus)", timeout="10 sec")
    APSServiceTracker<EventBus> eventBusTracker

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
    }

    private void onVertxAvailable(Vertx vertx, ServiceReference vertxRef) {
        APSVertxProviderTest.vertx = vertx
        this.logger.info "Got vertx!"
    }

    private void onVertxLeaving(ServiceReference vertxRef, Class vertxAPIClass) {
        this.logger.info "Vertx going away!"
    }
}

