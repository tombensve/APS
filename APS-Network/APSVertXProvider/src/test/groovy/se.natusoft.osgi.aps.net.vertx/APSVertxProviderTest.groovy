package se.natusoft.osgi.aps.net.vertx

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import io.vertx.groovy.core.Vertx
import org.junit.Test
import se.natusoft.osgi.aps.api.reactive.Consumer
import se.natusoft.osgi.aps.net.messaging.models.config.TestConfigList
import se.natusoft.osgi.aps.net.messaging.models.config.TestConfigValue
import se.natusoft.osgi.aps.net.vertx.api.VertxConsumer
import se.natusoft.osgi.aps.net.vertx.config.VertxConfig
import se.natusoft.osgi.aps.test.tools.OSGIServiceTestTools
import se.natusoft.osgi.aps.tools.APSActivator
import se.natusoft.osgi.aps.tools.APSLogger
import se.natusoft.osgi.aps.tools.annotation.activator.Managed
import se.natusoft.osgi.aps.tools.annotation.activator.OSGiProperty
import se.natusoft.osgi.aps.tools.annotation.activator.OSGiServiceProvider

import static java.util.concurrent.TimeUnit.MILLISECONDS
import static java.util.concurrent.TimeUnit.SECONDS;

@CompileStatic
@TypeChecked
class APSVertxProviderTest extends OSGIServiceTestTools {

    public static Consumer.Consumed<Vertx> vertx = null

    @Test
    void reactiveAPITest() throws Exception {
        // Most of the unfamiliar constructs here are provided by OSGiServiceTestTools and groovy DSL features.

        println "============================================================================"
        println "DO NOTE: All the RED colored output comes from Vertx! It is not something "
        println "that have failed! Vertx have just chosen this color for their log output!"
        println "============================================================================"

        deploy 'aps-vertx-provider' with new APSActivator() with {

            VertxConfig config = new VertxConfig()
            TestConfigList<VertxConfig.VertxConfigValue> entries = new TestConfigList<>()

            VertxConfig.VertxConfigValue entry = new VertxConfig.VertxConfigValue()
            entry.name = new TestConfigValue(value: "workerPoolSize")
            entry.value = new TestConfigValue(value: "40")
            entry.type = new TestConfigValue(value: "Int")

            entries.configs.add(entry)

            config.optionsValues = entries

            config

        } using '/se/natusoft/osgi/aps/net/vertx/APSVertxProvider.class'

        deploy 'vertx-consumer-svc' with new APSActivator() using '/se/natusoft/osgi/aps/net/vertx/VertxConsumerService.class'

        try {

            hold() whilst { vertx == null } maxTime 5L unit SECONDS go()

            assert vertx != null
            assert vertx.get() != null

        }
        finally {
            if (vertx != null) vertx.release()
            shutdown()
            hold() maxTime 500 unit MILLISECONDS go() // Give Vertx time to shut down.
        }

    }

}

@SuppressWarnings("GroovyUnusedDeclaration")
@OSGiServiceProvider( properties = [ @OSGiProperty( name = "consumed", value = "vertx") ] )
@CompileStatic
@TypeChecked
// Important: Service interface must be the first after "implements"!! Otherwise serviceAPIs=[Consumer.class] must be specified
// in @OSGiServiceProvider annotation.
class VertxConsumerService extends VertxConsumer implements Consumer<Vertx>  {

    @Managed(loggingFor = "Test:VertxConsumerService")
    APSLogger logger

    VertxConsumerService() {
        this.onVertxAvailable = { Consumer.Consumed<Vertx> vertx ->
            this.logger.info("VertxConsumerService onAvailable closure called! [${vertx}]")
            APSVertxProviderTest.vertx = vertx
        }
        this.onVertxUnavilable = {
            this.logger.error("No vertx instance available!")
            throw new Exception("Failure, no vertx service available!")
        }
        this.onVertxRevoked = {
            this.logger.info("Vertx instance revoked!")
        }
    }
}

