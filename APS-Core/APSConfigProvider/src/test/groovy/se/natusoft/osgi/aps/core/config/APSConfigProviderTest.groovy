package se.natusoft.osgi.aps.core.config

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.junit.Test
import se.natusoft.osgi.aps.api.core.config.APSConfig
import se.natusoft.osgi.aps.api.core.filesystem.service.APSFilesystemService
import se.natusoft.osgi.aps.api.messaging.APSMessage
import se.natusoft.osgi.aps.api.messaging.APSMessageService
import se.natusoft.osgi.aps.api.messaging.APSMessageSender
import se.natusoft.osgi.aps.model.APSResult
import se.natusoft.osgi.aps.core.lib.StructMap
import se.natusoft.osgi.aps.test.tools.OSGIServiceTestTools
import se.natusoft.osgi.aps.tools.APSActivator
import se.natusoft.osgi.aps.tools.APSLogger
import se.natusoft.osgi.aps.tools.annotation.activator.Initializer
import se.natusoft.osgi.aps.tools.annotation.activator.Managed
import se.natusoft.osgi.aps.tools.annotation.activator.OSGiService

import java.util.concurrent.TimeUnit

import static se.natusoft.osgi.aps.model.APSProperties.props

@CompileStatic
@TypeChecked
class APSConfigProviderTest extends OSGIServiceTestTools {

    static boolean ok = false

    @Test
    void testConfigProvider() throws Exception {

        deploy 'aps-vertx-provider' with new APSActivator() from(
                'se.natusoft.osgi.aps',
                'aps-vertx-provider',
                '1.0.0'
        )

        deploy 'aps-vertx-cluster-datastore-service-provider' with new APSActivator() from(
                'se.natusoft.osgi.aps',
                'aps-vertx-cluster-datastore-service-provider',
                '1.0.0'
        )

        deploy 'aps-vertx-event-bus-messaging-provider' with new APSActivator() from(
                'se.natusoft.osgi.aps',
                'aps-vertx-event-bus-messaging-provider',
                '1.0.0'
        )

//        deploy 'aps-execution-service-provider' with new APSActivator() from(
//                'se.natusoft.osgi.aps',
//                'aps-execution-service-provider',
//                '1.0.0'
//        )

        System.setProperty( APSFilesystemService.CONF_APS_FILESYSTEM_ROOT, "target/config" )

        deploy 'aps-filesystem-service-provider' with new APSActivator() from(
                'se.natusoft.osgi.aps',
                'aps-filesystem-service-provider',
                '1.0.0'
        )

        deploy 'aps-config-provider' with new APSActivator() from 'target/classes'

        hold() whilst { !ok } maxTime 5 unit TimeUnit.SECONDS go()

    }
}

@CompileStatic
@TypeChecked
class ConfigUser {

    // lookup is non blocking safe ...
    @OSGiService(nonBlocking = true)
    private APSMessageService<Map<String, Object>> messageService

    @Managed
    private APSLogger logger

    private StructMap config

    @Initializer
    void init() {
        this.messageService.subscribe( props() + APSMessageService.TARGET >> ( APSConfig.APS_CONFIG_AVAILABLE_ADDRESS_START + "testConfig" ) ) {
            APSMessage<Map<String, Object>> apsMsg ->

                Map<String, Object> message = apsMsg.content()

                Map<String, Object> cfg = message [ 'apsConfig' ] as Map<String, Object>
                this.config = new StructMap( cfg )
        }

        this.messageService.sender(

                props() + APSMessageService.TARGET >> ( APSConfig.APS_CONFIG_AVAILABLE_ADDRESS_START + "testConfig" )

        ) { APSMessageSender<Map<String, Object>> sender ->

            sender.send( [ apsConfigId: "testConfig" as Object ] ) { APSResult res ->
                this.logger.error( "Failed to request 'testConfig'!", res.failure() )
            }
        }
    }
}

