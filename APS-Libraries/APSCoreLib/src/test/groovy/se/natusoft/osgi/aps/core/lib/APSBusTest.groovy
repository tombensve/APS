package se.natusoft.osgi.aps.core.lib

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.junit.Test
import se.natusoft.osgi.aps.activator.APSActivator
import se.natusoft.osgi.aps.activator.annotation.BundleStop
import se.natusoft.osgi.aps.activator.annotation.Initializer
import se.natusoft.osgi.aps.activator.annotation.OSGiService
import se.natusoft.osgi.aps.api.messaging.APSBus
import se.natusoft.osgi.aps.exceptions.APSValidationException
import se.natusoft.osgi.aps.test.tools.APSOSGIServiceTestTools
import se.natusoft.osgi.aps.test.tools.APSTestResults
import se.natusoft.osgi.aps.types.APSResult
import se.natusoft.osgi.aps.types.APSUUID
import se.natusoft.osgi.aps.types.ID

import java.util.concurrent.TimeUnit

@CompileStatic
@TypeChecked
class APSBusTest extends APSOSGIServiceTestTools {

    public static int testCount = 0

    public static APSTestResults testResults = new APSTestResults()

    @Test
    void test() {

        deploy 'aps-core-lib' with new APSActivator() from "APS-Libraries/APSCoreLib/target/classes"
        deploy 'aps-core-lib-test' with new APSActivator() from "APS-Libraries/APSCoreLib/target/test-classes"

        hold() whilst { testCount < 3 } maxTime 4 unit TimeUnit.SECONDS exceptionOnTimeout true go()

        undeploy 'aps-core-lib-test'
        undeploy 'aps-core-lib'

        // There will be no messages if testResult.testOK is true!
        testResults.printMessages()

        assert testResults.testOK
    }
}

@CompileStatic
@TypeChecked
@SuppressWarnings( "unused" )
// Instantiated and injected via reflection by APSActivator
class ShouldWork {

    private boolean receivedMessageWork = false

    @OSGiService( nonBlocking = true )
    private APSBus bus

    @Initializer
    void toTest() {

        ID subId = new APSUUID()

        this.bus.subscribe( subId, "local:test", null ) { Map<String, Object> message ->

            this.receivedMessageWork = true

            System.out.println message.toString()

            APSBusTest.testResults.trAssertEquals( "test", message[ 'msgType' ] )
            APSBusTest.testResults.trAssertEquals( "qaz", message[ 'value' ] )
        }

        this.bus.send(
                "local:test",
                [
                        msgType: "test",
                        value  : "qaz"
                ] as Map<String, Object>
        ) { APSResult res ->
            APSBusTest.testResults.trAssertTrue( res.success() )
        }

        // Note: This only works because we only have one bus router here: APSLocalInMemoryBus,
        // which does not thread!! THAT IS IN GENERAL NOT AN EXPECTATION THAT CAN BE MADE!!
        // But in this case the subscribe handler will be called before send returns.

        APSBusTest.testResults.trAssertTrue( this.receivedMessageWork )

        this.bus.unsubscribe( subId )

        this.bus.send(
                "local:test",
                [
                        msgType: "no-receiver-test",
                        value  : "zaq"
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
@TypeChecked
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

            APSBusTest.testResults.trAssertSame( res.failure().getClass(), APSValidationException.class )
            APSBusTest.testResults.trAssertEquals( "'target' does not start with 'local'!", res.failure().getMessage() )
        },
                null // Will never ever be called in this case!
        )

        this.bus.send(
                "test",
                [
                        msgType: "test",
                        value  : "qaz"
                ] as Map<String, Object>
        ) { res ->
            APSBusTest.testResults.trAssertFalse( res.success() )
            APSBusTest.testResults.trAssertEquals( "'target' does not start with 'local'!", res.failure().getMessage() )
        }

        APSBusTest.testCount++
        println "ShouldFail: testCount: ${ APSBusTest.testCount }"
    }
}

@CompileStatic
@TypeChecked
@SuppressWarnings( "unused" )
// Instantiated and injected via reflection by APSActivator
class TestRequest {

    @OSGiService( nonBlocking = true )
    private APSBus bus

    private ID subId = new APSUUID()

    private MapJsonSchemaValidator schemaValidator = new MapJsonSchemaValidator(
            validStructure: MapJsonLoader.loadMapJson( "aps/messages/APSMessageSchema.json", this.class.classLoader )
    )

    private boolean validateMessage(Map<String, Object> message) {
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

        // Setup a service to call.

        this.bus.subscribe( this.subId, "local:testService", null ) { Map<String, Object> message ->

            //assert that message is valid.
            APSBusTest.testResults.trAssertTrue( validateMessage( message ) )

            String data = message[ 'content' ][ 'data' ]

            // NOTE: Groovy allows you to use dot access on Map:s, like: 'message.content'. The
            // problem with this is that the Map is defined as Map<String, Object>, so the return
            // type of content is 'Object' so you cannot continue to reference even tough it is really
            // a Map. But Groovy has another variant: ['name']. This seems to assume Map since with
            // this you can continue to reference down the Map. You will however not get any compiler
            // support. It will fail runtime if you make a bad reference! I think Javascript objects
            // can be referenced with a similar syntax. Maybe the Groovy guys picked inspiration from
            // JS :-).

            String replyAddress = message[ 'header' ][ 'replyAddress' ]

            Map<String, Object> respMsg = [
                    header : [
                            type   : "serviceResponse",
                            version: 1.0
                    ],
                    content: [
                            response: data
                    ]
            ] as Map<String, Object>

            APSBusTest.testResults.trAssertTrue( validateMessage( respMsg ) )

            this.bus.send( replyAddress, respMsg ) { APSResult result ->

                APSBusTest.testResults.trAssertTrue( result.success() )
            }

        }

        // Call service

        this.bus.request( "local:testService", [
                header : [
                        type   : "serviceRequest",
                        version: 1.0
                ],
                content: [
                        data: "Something important!"
                ]
        ] as Map<String, Object> ) { APSResult result ->
            assert result.success()
        } { Map<String, Object> response ->

            APSBusTest.testResults.trAssertTrue( response[ 'content' ][ 'response' ] == "Something important!" )
        }

        APSBusTest.testCount++
        println "TestRequest: testCount: ${ APSBusTest.testCount }"

    }

    @BundleStop
    void shutdown() {
        println ">>>>>>>>>>>>>>>>>>> Stopping bundle <<<<<<<<<<<<<<<<<<<<"

        this.bus.unsubscribe( subId )
    }
}