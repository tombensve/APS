package se.natusoft.osgi.aps.core.config

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.junit.Test
import se.natusoft.osgi.aps.activator.APSActivator
import se.natusoft.osgi.aps.activator.annotation.ConfigListener
import se.natusoft.osgi.aps.activator.annotation.Managed
import se.natusoft.osgi.aps.api.core.config.APSConfig
import se.natusoft.osgi.aps.api.core.filesystem.service.APSFilesystemService
import se.natusoft.osgi.aps.runtime.APSRuntime
import se.natusoft.osgi.aps.util.APSLogger

import java.util.concurrent.TimeUnit

import static se.natusoft.osgi.aps.util.APSExecutor.concurrent

@CompileStatic
@TypeChecked
class APSConfigurationTest extends APSRuntime {

    static boolean ok = false

    @Test
    void testConfigProvider() throws Exception {

        // Note that when testing you only need to deploy bundles that publishes services.
        // Pure libraries (all packages exported) are available anyhow in the JUnit classpath
        // since the APSRuntime does not provide separate class loaders for each
        // bundle. This is intentional.

        deploy 'aps-vertx-provider' with new APSActivator() from(
                'se.natusoft.osgi.aps',
                'aps-vertx-provider',
                '1.0.0'
        )

        hold() maxTime 2 unit TimeUnit.SECONDS go()

        deploy 'aps-vertx-cluster-datastore-service-provider' with new APSActivator() from(
                'se.natusoft.osgi.aps',
                'aps-vertx-cluster-datastore-service-provider',
                '1.0.0'
        )

        System.setProperty( APSFilesystemService.CONF_APS_FILESYSTEM_ROOT, "target/config" )

        deploy 'aps-filesystem-service-provider' with new APSActivator() from(
                'se.natusoft.osgi.aps',
                'aps-filesystem-service-provider',
                '1.0.0'
        )

        deploy 'aps-config-manager' with new APSActivator() from 'APS-Core/APSConfigManager/target/classes'

        deploy 'moon-whale-service' with new APSActivator() from 'APS-Core/APSConfigManager/target/test-classes'

        hold() whilst { !ok } maxTime 10 unit TimeUnit.SECONDS go()

        assert ok
    }
}

@CompileStatic
@TypeChecked
class MoonWhaleService {
    // Name inspired by a South Park episode ...

    @Managed
    private APSLogger logger

    private APSConfig config

    @ConfigListener(apsConfigId = "moon-whale-service-config")
    void config( APSConfig config ) {
        this.logger.info( "######## Config available! ########" )

        if (config != null) {
            this.config = config

            // Now, lets submit som work to a thread pool so that the callback can return.
            concurrent {
                Object value = this.config.lookup( "moonWhales.count" )
                assert value instanceof Number
                assert ( value as int ) == 22

                value = this.config.lookup( "local.relayWhales" )
                assert value instanceof Number
                assert ( value as int ) == 18

                value = this.config.lookup( "local.translatorWhales" )
                assert value instanceof Number
                assert ( value as int ) == 5

                APSConfigurationTest.ok = true
            }
        }
    }
}

