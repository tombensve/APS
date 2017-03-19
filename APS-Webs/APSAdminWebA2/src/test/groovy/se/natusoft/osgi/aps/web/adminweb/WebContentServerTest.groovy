package se.natusoft.osgi.aps.web.adminweb

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import io.vertx.groovy.core.Vertx
import org.junit.Test
import se.natusoft.osgi.aps.tools.APSLogger
import se.natusoft.osgi.aps.tools.reactive.Consumer
import se.natusoft.osgi.aps.net.messaging.models.config.TestConfigList
import se.natusoft.osgi.aps.net.messaging.models.config.TestConfigValue
import se.natusoft.osgi.aps.net.vertx.config.VertxConfig
import se.natusoft.osgi.aps.test.tools.OSGIServiceTestTools
import se.natusoft.osgi.aps.tools.APSActivator
import se.natusoft.osgi.aps.web.adminweb.config.ContentServerConfig

import static java.util.concurrent.TimeUnit.MILLISECONDS
import static java.util.concurrent.TimeUnit.SECONDS

@CompileStatic
@TypeChecked
class WebContentServerTest extends OSGIServiceTestTools {

    private static APSLogger logger = new APSLogger(APSLogger.PROP_LOGGING_FOR, "WebContentServerTest")

    public static Consumer.Consumed<Vertx> vertx = null

    @Test
    void testWebContentServer() throws Exception {
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

        } from 'se.natusoft.osgi.aps', 'aps-vertx-provider', '1.0.0'

        deploy 'web-content-server' with new APSActivator() with {
            ContentServerConfig config = new ContentServerConfig()

            TestConfigList<ContentServerConfig.ConfigValue> entries = new TestConfigList<>()
            config.optionsValues = entries

            config
        } using '/se/natusoft/osgi/aps/web/adminweb/WebContentServer.class'

        try {
            logger.info "Waiting 5 seconds for server to come up."
            hold() maxTime 5L unit SECONDS go()

            logger.info "Calling server ..."

            getAndVerify("", "index.html")
            getAndVerify("index.html", "index.html")
            getAndVerify("adminweb-bundle.js", "adminweb-bundle.js")
            getAndVerify("shim.min.js", "shim.min.js")
            getAndVerify("sockjs.min.js", "sockjs.min.js")
            getAndVerify("vertx-eventbus.js", "vertx-eventbus.js")
            try {
                getAndVerify("nonexistent", null)
                throw new Exception("A FileNotFoundException was expected!")
            }
            // You don't get an HTTP status from java.net.URL! It throws exceptions instead.
            catch (FileNotFoundException ignore) {
                logger.info "Correctly got a FileNotFoundException for 'nonexistent'!"
            }
        }
        catch (Exception e) {
            shutdown()
            throw e
        }
        finally {
            shutdown()
            hold() maxTime 500 unit MILLISECONDS go() // Give Vertx time to shut down.
        }
    }

    private static void getAndVerify(String serverFile, String localFile) throws Exception {
        URL url = new URL("http://localhost:9080/apsadminweb/${serverFile}")
        String fileFromServer = loadFromStream(url.openStream())
        if (localFile != null) {
            String fileFromLocal = loadFromStream(System.getResourceAsStream("/webContent/${localFile}"))
            assert fileFromServer == fileFromLocal
            logger.info "${serverFile} served correctly!"
        }
    }

    private static String loadFromStream(InputStream readStream) {
        StringBuilder sb = new StringBuilder()
        BufferedReader reader = new BufferedReader(new InputStreamReader(readStream))

        String line = reader.readLine()
        while (line != null) {
            sb.append(line)
            sb.append('\n')
            line = reader.readLine()
        }

        reader.close()

        return sb.toString()
    }
}
