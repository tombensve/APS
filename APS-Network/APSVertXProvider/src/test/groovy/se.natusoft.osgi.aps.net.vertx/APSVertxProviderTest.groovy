package se.natusoft.osgi.aps.net.vertx

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import io.vertx.groovy.core.Vertx
import org.junit.Test
import se.natusoft.osgi.aps.api.reactive.DataConsumer
import se.natusoft.osgi.aps.constants.APS
import se.natusoft.osgi.aps.net.messaging.models.config.TestConfigList
import se.natusoft.osgi.aps.net.messaging.models.config.TestConfigValue
import se.natusoft.osgi.aps.net.vertx.config.VertxConfig
import se.natusoft.osgi.aps.test.tools.OSGIServiceTestTools
import se.natusoft.osgi.aps.tools.APSActivator
import se.natusoft.osgi.aps.tools.APSLogger
import se.natusoft.osgi.aps.tools.annotation.activator.Managed
import se.natusoft.osgi.aps.tools.annotation.activator.OSGiProperty
import se.natusoft.osgi.aps.tools.annotation.activator.OSGiServiceProvider

class APSVertxProviderTest extends OSGIServiceTestTools {

    public static DataConsumer.DataHolder<Vertx> vertx = null

    @Test
    void reactiveAPITest() throws Exception {

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

        } with new APSActivator() using '/se/natusoft/osgi/aps/net/vertx/APSVertxProvider.class'

        deploy 'vertx-consumer-svc' with new APSActivator() using '/se/natusoft/osgi/aps/net/vertx/VertxConsumerService.class'

        try {
            int count = 0
            while ( vertx == null ) {
                synchronized ( this ) {
                    wait( 200 )
                }
                ++count
                if ( count > 40 ) break
            }

            assert vertx != null
            assert vertx.use() != null

        }
        finally {
            if (vertx != null) vertx.release()
            shutdown()
            Thread.sleep(500) // Give Vertx time to shut down.
        }

    }

}

@SuppressWarnings("GroovyUnusedDeclaration")
@OSGiServiceProvider(
        properties = [
                @OSGiProperty( name = APS.Service.Provider, value = "aps-vertx-provider:test" ),
                @OSGiProperty( name = APS.Service.Category, value = APS.Value.Service.Category.Network ),
                @OSGiProperty( name = APS.Service.Function, value = APS.Value.Service.Function.Messaging )
        ]
)
@CompileStatic
@TypeChecked
class VertxConsumerService extends DataConsumer.DataConsumerProvider<Vertx> implements DataConsumer<Vertx> {

    @Managed
    APSLogger logger

    /**
     * Called when there is updated data available.
     *
     * @param data The new data.
     */
    @Override
    void onDataAvailable(DataConsumer.DataHolder<Vertx> vertx) {
        this.logger.info("VertxConsumerService.onDataAvailable(...) called!")
        APSVertxProviderTest.vertx = vertx
    }

    /**
     * Called when there is a failure to deliver requested instance.
     *
     * Haven't found a way to make Vertx fail yet, so this will never be called.
     */
    @Override
    void onDataUnavailable() {
        logger.error("No vertx instance available!")
        throw new Exception("Failure, no vertx service available!")
    }
}

