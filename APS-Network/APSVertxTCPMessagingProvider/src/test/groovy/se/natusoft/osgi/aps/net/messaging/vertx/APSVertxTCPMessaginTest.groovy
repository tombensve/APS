package se.natusoft.osgi.aps.net.messaging.vertx

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.junit.Test
import se.natusoft.docutations.NotNull
import se.natusoft.osgi.aps.api.net.messaging.service.APSMessageService
import se.natusoft.osgi.aps.api.net.messaging.service.APSSubscriber
import se.natusoft.osgi.aps.net.messaging.models.config.TestConfigList
import se.natusoft.osgi.aps.net.messaging.models.config.TestConfigValue
import se.natusoft.osgi.aps.net.messaging.vertx.api.APSVertxTCPMessagingOptions
import se.natusoft.osgi.aps.net.messaging.vertx.config.VertxConfig
import se.natusoft.osgi.aps.test.tools.OSGIServiceTestTools
import se.natusoft.osgi.aps.tools.APSActivator
import se.natusoft.osgi.aps.tools.APSLogger
import se.natusoft.osgi.aps.tools.annotation.activator.Initializer
import se.natusoft.osgi.aps.tools.annotation.activator.Managed
import se.natusoft.osgi.aps.tools.annotation.activator.OSGiService
import se.natusoft.osgi.aps.tools.annotation.activator.OSGiServiceProvider

@CompileStatic
@TypeChecked
class APSVertxTCPMessagingTest extends OSGIServiceTestTools {

    /**
     * This will be set to true by MsgSender if response to request comes back as expected. This flag is used
     * because the MsgSender runs in a different thread than JUnit so failed asserts in it will not be seen
     * by JUnit. This flag will however be set to true on success.
     */
    public static synchronized boolean success = false

    @Test
    void runTest() throws Exception {

        // Deliberately deploying in dependency-wise wrong order since in a real deployment the order of deployment is undetermined.

        deploy 'aps-vertx-tcp-messaging-provider' with new APSActivator() from 'APS-Network/APSVertxTCPMessagingProvider/target/classes'

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

        deploy 'aps-tcp-options-provider' with new APSActivator() using '/se/natusoft/osgi/aps/net/messaging/vertx/APSVertxTCPMessagingOptionsProvider.class'

        deploy 'msg-member-one' with new APSActivator() using '/se/natusoft/osgi/aps/net/messaging/vertx/MsgMemberOne.class'

        deploy 'msg-member-two' with new APSActivator() using '/se/natusoft/osgi/aps/net/messaging/vertx/MsgMemberTwo.class'

        deploy 'msg-member-three' with new APSActivator() using '/se/natusoft/osgi/aps/net/messaging/vertx/MsgMemberThree.class'


        // Wait for and then validate result. Even if in this case a cluster of one member is created, it takes
        // some time. On a real slow machine these time values might need to be larger.
        try {
            Thread.sleep(500)

            assert success , "  .:: SEE ERROR MESSAGES HIGHER UP! ::.  "
        }
        finally {
            shutdown()
            Thread.sleep(500) // Give Vertx time to shut down.
        }
    }

}

/**
 * Provides configuration for the tested service. This is an OSGi service since the APSVertxTCPMessagingProvider will
 * lookup this service to get its configuration. This is deployed as its own bundle.
 */
@SuppressWarnings("GroovyUnusedDeclaration")
@OSGiServiceProvider
class APSVertxTCPMessagingOptionsProvider implements APSVertxTCPMessagingOptions {

    /**
     * @return A Map containing Vert.x Net server options. Contents: name, value
     */
    @Override
    Map<String, Object> getServerOptions() {
        return [ : ]
    }

    /**
     * @return A Map containing Vert.x Net client options. Contents: name, value.
     */
    @Override
    Map<String, Object> getClientOptions() {
        return [
            connectTimeout: 10000,
            reconnectAttempts: 10,
            reconnectInterval: 500
        ]
    }

    /**
     * @return Mappings between topic and the URI for the topic.
     */
    @Override
    Map<String, String> getTopicToURIMapping() {
        return [
                testOne:   "tcp://localhost:13987?inst=4#in,tcp://localhost:13988/?inst=2#out,tcp://localhost:13989/#out",
                testTwo:   "tcp://localhost:13988?inst=2#in,tcp://localhost:13987/#out,tcp://localhost:13989/#out",
                testThree: "tcp://localhost:13989/#in,tcp://localhost:13988/#out,tcp://localhost:13987/#out"
        ]
    }
}

@SuppressWarnings("GroovyUnusedDeclaration")
@CompileStatic
@TypeChecked
class MsgMemberOne implements APSSubscriber {

    @OSGiService( timeout = "15 sec" )
    private APSMessageService msgService

    @Managed( loggingFor = "msg-member-one" )
    private APSLogger logger

    private List<String> received = []

    @Initializer
    void init() {
        // Note: Since the APSVertxTCPMessagingProvider does not register as an OSGi service until the cluster is upp and
        // Vert.x is ready, this call will just wait for the service to become available and then call it, since APSActivator
        // injects an APSServiceTracker wrapped service implementation. You can see the timeout on the @OSGiService annotation
        // above.

        this.msgService.subscribe "testOne", this

        this.logger.info "======== The warnings you'll se in the output are intentional! ========"

        this.msgService.publish "testOne" , "Tommy"
    }

    @Override
    void subscription( @NotNull String topic, @NotNull Object message ) {
        this.logger.info "Message received: [${message}]  Type: ${message.class}"

        this.received << message.toString()

        if ( this.received.size() == 2 ) {
            assert this.received.contains( "Hello Tommy from msg-member-two" )
            assert this.received.contains( "Hello Tommy from msg-member-three" )
            APSVertxTCPMessagingTest.success = true
            this.logger.info "SUCCESS!! ALL EXPECTED MESSAGES HAVE ARRIVED!"
        }
    }
}

@SuppressWarnings("GroovyUnusedDeclaration")
@CompileStatic
@TypeChecked
class MsgMemberTwo implements APSSubscriber {

    @OSGiService( timeout = "15 sec" )
    private APSMessageService msgService

    @Managed( loggingFor = "msg-member-two" )
    private APSLogger logger

    @Initializer
    void init() {
        // Note: Since the APSVertxTCPMessagingProvider does not register as an OSGi service until the cluster is upp and
        // Vert.x is ready, this call will just wait for the service to become available and then call it, since APSActivator
        // injects an APSServiceTracker wrapped service implementation. You can see the timeout on the @OSGiService annotation
        // above.

        this.msgService.subscribe "testTwo", this
    }

    @Override
    void subscription( @NotNull String topic, @NotNull Object message ) {
        this.logger.info( "Message received: [${message}]" )

        if ( message.toString() == "Tommy" ) {
            this.msgService.publish "testTwo", "Hello ${message} from msg-member-two"
        }
    }
}

@SuppressWarnings("GroovyUnusedDeclaration")
@CompileStatic
@TypeChecked
class MsgMemberThree implements APSSubscriber {

    @OSGiService( timeout = "15 sec" )
    private APSMessageService msgService

    @Managed( loggingFor = "msg-member-three" )
    private APSLogger logger

    @Initializer
    void init() {
        // Note: Since the APSVertxTCPMessagingProvider does not register as an OSGi service until the cluster is upp and
        // Vert.x is ready, this call will just wait for the service to become available and then call it, since APSActivator
        // injects an APSServiceTracker wrapped service implementation. You can see the timeout on the @OSGiService annotation
        // above.

        this.msgService.subscribe "testThree", this
    }

    @Override
    void subscription( @NotNull String topic, @NotNull Object message ) {
        this.logger.info( "Message received: [${message}]" )

        if ( message.toString() == "Tommy" ) {
            this.msgService.publish "testThree" , "Hello ${message} from msg-member-three"
        }
    }
}

