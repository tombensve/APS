package se.natusoft.osgi.aps.net.vertx

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import io.vertx.core.Vertx
import io.vertx.ext.web.Router
import org.junit.Test
import se.natusoft.osgi.aps.api.pubcon.APSConsumer
import se.natusoft.osgi.aps.net.vertx.api.APSVertx
import se.natusoft.osgi.aps.net.vertx.api.VertxConsumer
import se.natusoft.osgi.aps.test.tools.OSGIServiceTestTools
import se.natusoft.osgi.aps.tools.APSActivator
import se.natusoft.osgi.aps.tools.APSLogger
import se.natusoft.osgi.aps.tools.annotation.activator.Managed
import se.natusoft.osgi.aps.tools.annotation.activator.OSGiProperty
import se.natusoft.osgi.aps.tools.annotation.activator.OSGiServiceProvider

import static java.util.concurrent.TimeUnit.MILLISECONDS
import static java.util.concurrent.TimeUnit.SECONDS

@CompileStatic
@TypeChecked
class APSVertxProviderTest extends OSGIServiceTestTools {

    public static Vertx vertx = null
    public static Router router = null

    @Test
    void reactiveAPITest() throws Exception {
        // Most of the unfamiliar constructs here are provided by OSGiServiceTestTools and groovy DSL features.

        println "============================================================================"
        println "DO NOTE: All the RED colored output comes from Vertx! It is not something "
        println "that have failed! Vertx have just chosen this color for their log output!"
        println "============================================================================"

        deploy 'aps-vertx-provider' with new APSActivator() using '/se/natusoft/osgi/aps/net/vertx/APSVertxProvider.class'

        deploy 'vertx-consumer-svc' with new APSActivator() using '/se/natusoft/osgi/aps/net/vertx/VertxConsumerService.class'

        try {

            hold() whilst { vertx == null } maxTime 15L unit SECONDS go()

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
@OSGiServiceProvider( properties = [
        @OSGiProperty( name = "consumed", value = "vertx"),
        @OSGiProperty( name = APSVertx.HTTP_SERVICE_NAME, value = "test")
] )
@CompileStatic
@TypeChecked
// Important: Service interface must be the first after "implements"!! Otherwise serviceAPIs=[Consumer.class] must be specified
// in @OSGiServiceProvider annotation.
class VertxConsumerService extends VertxConsumer implements APSConsumer<Vertx> {

    @Managed(loggingFor = "Test:VertxConsumerService")
    APSLogger logger

    // Note that this only registers callbacks! The callbacks themselves will not be called until the
    // service have been published. This will not happen util after all injections are done. Thereby
    // this.logger.info(...) will always work.
    VertxConsumerService() {
        this.onVertxAvailable = { Vertx vertx ->
            this.logger.info("Received Vertx instance! [${vertx}]")
            APSVertxProviderTest.vertx = vertx
        }
        this.onVertxRevoked = {
            this.logger.info("Vertx instance revoked!")
        }
        this.onRouterAvailable = { Router router ->
            this.logger.info("Received Router instance! [${router}]")
            APSVertxProviderTest.router = router
        }
        this.onError = { String message ->
            this.logger.error(message)
        }
    }
}

