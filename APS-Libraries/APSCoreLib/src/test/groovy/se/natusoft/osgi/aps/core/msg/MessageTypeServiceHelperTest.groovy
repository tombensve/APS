package se.natusoft.osgi.aps.core.msg

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.junit.Test
import org.osgi.framework.ServiceReference
import se.natusoft.docutations.AutoInstantiated
import se.natusoft.docutations.DependencyInjected
import se.natusoft.osgi.aps.activator.APSActivator
import se.natusoft.osgi.aps.activator.annotation.Initializer
import se.natusoft.osgi.aps.activator.annotation.Managed
import se.natusoft.osgi.aps.activator.annotation.OSGiService
import se.natusoft.osgi.aps.api.messaging.APSBus
import se.natusoft.osgi.aps.runtime.APSRuntime
import se.natusoft.osgi.aps.tracker.APSServiceTracker
import se.natusoft.osgi.aps.types.APSHandler
import se.natusoft.osgi.aps.types.APSResult
import se.natusoft.osgi.aps.util.APSLogger
import se.natusoft.osgi.aps.util.Failures
import static se.natusoft.osgi.aps.util.APSExecutor.submit as parallel

import java.util.concurrent.TimeUnit

@CompileStatic
@TypeChecked
class MessageTypeServiceHelperTest extends APSRuntime {

    public static boolean gotService = false

    public static Failures fails = new Failures()

    @Test
    void testMSH() {

        deployConfigAndVertxPlusDeps()

        APSLogger logger = new APSLogger().setLoggingFor( "testMSH" )

        logger.info( "€€€€€€ Deployed Config and Vertx plus deps!" )

        deploy "MSHTestService" with new APSActivator() using( "/se/natusoft/osgi/aps/core/msg/MSHTestService" +
                ".class" )
        logger.info "€€€€€€ Deployed MSHTestService!"

        deploy "MSHTestClient" with new APSActivator() using( "/se/natusoft/osgi/aps/core/msg/MSHTestClient.class" )
        logger.info "€€€€€€ Deployed MSHTestClient!"

        hold() whilst { !gotService } maxTime( 8 ) unit( TimeUnit.SECONDS ) go()

        // Depending on if service or client gets started first there might be no listener of address sent to.
        // This is entirely OK. So we filter out those fails.
        Failures filteredFails = fails.filter( "(NO_HANDLERS,-1)" )
        if ( filteredFails.hasFailures() ) {
            filteredFails.showFailures()
            assert false
        }
        assert gotService
    }
}

@AutoInstantiated
@DependencyInjected
@SuppressWarnings( "unused" )
@CompileStatic
@TypeChecked
class MSHTestService {

    @OSGiService
    private APSServiceTracker<APSBus> apsBusTracker
    private APSBus bus

    @Managed( loggingFor = "MSHTestService" )
    private APSLogger logger

    private MessageTypeServiceHelper serviceHelper

    @Initializer
    void init() {
        this.apsBusTracker.onActiveServiceAvailable = { APSBus _apsBus, ServiceReference sref ->
            this.bus = _apsBus

            this.serviceHelper = new MessageTypeServiceHelper(
                    helperForServiceId: "service/mshTestService",
                    bus: this.bus,
                    logger: this.logger,
                    resultHandler: { APSResult<?> res ->

                        if ( !res.success() ) {
                            MessageTypeServiceHelperTest.fails << res.failure()
                            //this.logger.error( res.failure().message, res.failure() )
                        }
                        else {

                            parallel { provideService() }
                        }
                    } as APSHandler<APSResult<?>>
            )

            // Note that even though provideCheckService() do return "this" we cannot chain "provideCheckService()"
            // above since it results in the result handler above being called and it calls provideService()
            // which references this.serviceHelper which will then not have been assigned yet ...
            // The builder pattern is usually quite nice, but in combination with reactive callbacks it can
            // get messy :-).
            this.serviceHelper.provideCheckService()

            this.logger.info "MSHTestService initializer done!"

        }

        this.apsBusTracker.onActiveServiceLeaving = { ServiceReference sref, Class api ->
            this.bus = null
            this.serviceHelper.stop()
        }
    }

    private void provideService() {

        this.serviceHelper.listenToServiceMessages() { APSResult<?> res ->

            if ( MessageTypeServiceHelperTest.fails.assertAPSResult( res ) ) {
                this.logger.info ">>>>>> Now listening!"
                announceService()
                this.logger.info ">>>>>> Service announced!"
            }

        } { Map<String, Object> message ->

            this.logger.info "###### Received: ${message}"

            parallel { handleServiceMessage( message ) }
        }
    }

    private void announceService() {

        this.serviceHelper.announceService { APSResult<?> res ->

            if ( MessageTypeServiceHelperTest.fails.assertAPSResult( res ) ) {

                this.logger.info ">>>>>> Announcement successful!"
            }

        }
    }

    @SuppressWarnings( "GrMethodMayBeStatic" )
    private void handleServiceMessage( Map<String, Object> message ) {

        assert message != null
        logger.info ">>>>>> Received: ${message}"
    }
}

@AutoInstantiated
@DependencyInjected
@SuppressWarnings( "unused" )
@CompileStatic
@TypeChecked
class MSHTestClient {

    @OSGiService
    private APSServiceTracker<APSBus> apsBusTracker
    private APSBus bus

    @Managed( loggingFor = "MSHTestService" )
    private APSLogger logger

    private MessageTypeServiceHelper serviceHelper

    @Initializer
    void init() {

        this.apsBusTracker.onActiveServiceAvailable = { APSBus _apsBus, ServiceReference sref ->
            this.bus = _apsBus

            this.serviceHelper = new MessageTypeServiceHelper(
                    bus: this.bus,
                    logger: this.logger
            ).onServiceAvailable( "service/mshTestService" ) { APSResult<?> res ->
                this.logger.info "@@@@@@ Service available!"

                if ( MessageTypeServiceHelperTest.fails.assertAPSResult( res ) ) {

                    parallel {
                        this.bus.send( "cluster:service/mshTestService", [
                                aps    : [
                                        version: 1.0,
                                        type   : "mshTestServiceCall",
                                        from   : "mshTestClient"
                                ],
                                content: [
                                        for: "Test"
                                ]
                        ] as Map<String, Object> ) { APSResult<?> res2 ->
                            // We never ever get here!!! That should not be possible! We should always get here
                            // independent of success or failure.
                            if ( MessageTypeServiceHelperTest.fails.assertAPSResult( res2 ) ) {

                                MessageTypeServiceHelperTest.gotService = true
                            }
                        }
                    }
                }
            }
        }

        this.apsBusTracker.onActiveServiceLeaving = { ServiceReference sref, Class api ->
            this.bus = null
            this.serviceHelper.stop()
        }
    }
}
