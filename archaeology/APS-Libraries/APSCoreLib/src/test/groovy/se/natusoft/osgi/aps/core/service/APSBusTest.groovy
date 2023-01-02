package se.natusoft.osgi.aps.core.service

import groovy.transform.CompileStatic
import org.junit.Test
import se.natusoft.osgi.aps.activator.APSActivator
import se.natusoft.osgi.aps.activator.annotation.BundleStop
import se.natusoft.osgi.aps.activator.annotation.Initializer
import se.natusoft.osgi.aps.activator.annotation.Managed
import se.natusoft.osgi.aps.activator.annotation.OSGiService
import se.natusoft.osgi.aps.api.messaging.APSBus
import se.natusoft.osgi.aps.api.messaging.APSMessagingException
import se.natusoft.osgi.aps.core.lib.MapJsonLoader
import se.natusoft.osgi.aps.core.lib.MapJsonSchemaValidator
import se.natusoft.osgi.aps.exceptions.APSValidationException
import se.natusoft.osgi.aps.runtime.APSRuntime
import se.natusoft.osgi.aps.runtime.APSTestResults
import se.natusoft.osgi.aps.types.APSResult
import se.natusoft.osgi.aps.types.APSUUID
import se.natusoft.osgi.aps.types.ID
import se.natusoft.osgi.aps.util.APSLogger
import se.natusoft.osgi.aps.util.SyncedValue

import java.util.concurrent.TimeUnit

import static se.natusoft.osgi.aps.util.APSExecutor.concurrent
import static se.natusoft.osgi.aps.util.APSTools.waitFor

@CompileStatic
class APSBusTest extends APSRuntime {

    public static int testCount = 0

    public static APSTestResults testResults = new APSTestResults()

    @Test
    void test() {

        // Just to test the timeout override.
        System.setProperty( "aps.request.timeout", "10" )

        deploy 'aps-core-lib' with new APSActivator() from "APS-Libraries/APSCoreLib/target/classes"
        deploy 'aps-core-lib-test' with new APSActivator() from "APS-Libraries/APSCoreLib/target/test-classes"

        hold() whilst { testCount < 3 } maxTime 4 unit TimeUnit.SECONDS exceptionOnTimeout true go()

        undeploy 'aps-core-lib-test'
        undeploy 'aps-core-lib'

        // There will be no messages if testResult.testOK is true!
        testResults.printMessages()

        assert testResults.testOK

        shutdown()
    }
}

@CompileStatic
@SuppressWarnings( "unused" )
// Instantiated and injected via reflection by APSActivator
class ShouldWork {

    private SyncedValue<Boolean> receivedMessageWork = new SyncedValue<>(false)

    @OSGiService( nonBlocking = true )
    private APSBus bus

    @Initializer
    void toTest() {

        ID subId = new APSUUID()

        this.bus.subscribe( subId, "local:test", null ) { Map<String, Object> message ->

            this.receivedMessageWork.value = true

            System.out.println message.toString()

            APSBusTest.testResults.trAssertEquals( "test", message[ 'content' ][ 'msgType' ] )
            APSBusTest.testResults.trAssertEquals( "qaz", message[ 'content' ][ 'value' ] )
        }

        this.bus.send(
                "local:test",
                [
                        aps    : [
                        ],
                        content: [
                                msgType: "test",
                                value  : "qaz"
                        ]
                ] as Map<String, Object>
        ) { APSResult res ->
            APSBusTest.testResults.trAssertTrue( res.success() )
        }

        // Wait for receivedMessageWork to become true or timeout. The timeout is a full
        // second, so this should work on extremely slow machines also!
        waitFor( 5, 1000 ) { this.receivedMessageWork.value }

        APSBusTest.testResults.trAssertTrue( this.receivedMessageWork.value )

        // Unsubscribe and send again. send should fail!

        this.bus.unsubscribe( subId )

        this.bus.send(
                "local:test",
                [
                        aps    : [
                        ],
                        content: [
                                msgType: "no-receiver-test",
                                value  : "zaq"
                        ]
                ] as Map<String, Object>
        ) { APSResult res ->
            APSBusTest.testResults.trAssertFalse( res.success() )
            APSBusTest.testResults.trAssertEquals( "No subscribers!", res.failure().getMessage() )
        }

        APSBusTest.testCount++
        println "ShouldWork: testCount: ${ APSBusTest.testCount }"
    }
}

@CompileStatic
@SuppressWarnings( "unused" )
// Instantiated and injected via reflection by APSActivator
class ShouldFail {

    @OSGiService( nonBlocking = true )
    private APSBus bus

    @Initializer
    void toTest() {

        ID subId = new APSUUID()

        // This will not receive anything since target does not start with "local:"!
        this.bus.subscribe( subId, "test", { res ->

            //System.out.println( res.failure().toString() )

            APSBusTest.testResults.trAssertSame( res.failure().getClass(), APSMessagingException.class )
            APSBusTest.testResults.trAssertEquals( "No routers accepted target!", res.failure().getMessage() )
        },
                null // Will never ever be called in this case!
        )

        this.bus.send(
                "test",
                [
                        aps    : [],
                        content: [
                                msgType: "test",
                                value  : "qaz"
                        ]
                ] as Map<String, Object>
        ) { APSResult<?> res ->
            APSBusTest.testResults.trAssertFalse( res.success() )
            APSBusTest.testResults.trAssertEquals( "No routers accepted target 'test'!", res.failure().getMessage() )
        }

        APSBusTest.testCount++
        println "ShouldFail: testCount: ${ APSBusTest.testCount }"
    }
}

@CompileStatic
@SuppressWarnings( "unused" )
// Instantiated and injected via reflection by APSActivator
class TestRequest {

    @OSGiService( nonBlocking = true )
    private APSBus bus

    @Managed( loggingFor = "TestRequest" )
    private APSLogger logger

    private ID subId = new APSUUID()

    private MapJsonSchemaValidator schemaValidator = new MapJsonSchemaValidator(
            validStructure: MapJsonLoader.loadMapJson( "aps/messages/APSMessageSchema.json", this.class.classLoader )
    )

    private boolean validateMessage( Map<String, Object> message ) {
        boolean valid = true
        try {
            this.schemaValidator.validate( message )
        }
        catch ( APSValidationException ve ) {
            ve.printStackTrace( System.err )

            valid = false
        }

        valid
    }

    @Initializer
    void toTest() {

        // Call service

        // Run this in parallel since in a real situation the called service and the calling
        // client will not be on same thread, maybe even not on same machine!
        concurrent {

            this.logger.info ">>>>>>>>>> ABOUT TO REQUEST!"
            this.bus.request( "local:testService", [
                    aps    : [
                            type   : "serviceRequest",
                            version: 1.0
                    ],
                    content: [
                            data: "Something important!"
                    ]
            ] as Map<String, Object> ) { APSResult result ->

                APSBusTest.testResults.trAssertTrue( result.success() )

            } { Map<String, Object> response ->

                this.logger.info "#### GOT RESPONSE! ####"
                APSBusTest.testResults.trAssertTrue( response[ 'content' ][ 'response' ] == "Something important!" )
            }
        }

        // Simulate delay for subscribing service to start.
        Thread.sleep( 4000 )

        // Setup service

        this.bus.subscribe( this.subId, "local:testService", null ) { Map<String, Object> message ->

            //assert that message is valid.
            APSBusTest.testResults.trAssertTrue( validateMessage( message ) )

            String data = message[ 'content' ][ 'data' ]

            this.logger.info "<<<<<<<<<< Received: ${ data } >>>>>>>>>>"

            Map<String, Object> respMsg = [
                    aps    : [
                            type   : "serviceResponse",
                            version: 1.0
                    ],
                    content: [
                            response: data
                    ]
            ] as Map<String, Object>

            APSBusTest.testResults.trAssertTrue( validateMessage( respMsg ) )

            // Out of principle this is a job that should not be down within the message handler.
            concurrent {
                this.logger.info "€€€€€€€€€€€ REPLYING TO RECEIVED MESSAGE €€€€€€€€€"
                this.bus.reply( message, respMsg ) { APSResult result ->

                    APSBusTest.testResults.trAssertTrue( result.success() )
                }
            }

        }
        this.logger.info "<<<<<<<<<< SUBSCRIBED!"

        // We have to wait a little bit for the request to have time to call subscribe and
        // get a result.
        Thread.sleep( 3000 )

        APSBusTest.testCount++
        println "TestRequest: testCount: ${ APSBusTest.testCount }"

    }

    @BundleStop
    void shutdown() {
        println ">>>>>>>>>>>>>>>>>>> Stopping bundle <<<<<<<<<<<<<<<<<<<<"

        this.bus.unsubscribe( subId )
    }
}
