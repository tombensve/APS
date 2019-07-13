package se.natusoft.osgi.aps.tools.groovy.lib

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.junit.Test
import se.natusoft.osgi.aps.core.lib.messages.WellDefinedMessage
import se.natusoft.osgi.aps.core.lib.messaging.APSBus
import se.natusoft.osgi.aps.exceptions.APSValidationException
import se.natusoft.osgi.aps.types.APSResult
import se.natusoft.osgi.aps.types.APSUUID
import se.natusoft.osgi.aps.types.ID
import se.natusoft.osgi.aps.util.MapBuilder

import static org.junit.Assert.*

@CompileStatic
@TypeChecked
class APSLocalInMemoryBusTest {

    private boolean receivedMessageWork = false
    private boolean receivedMessageFail = false

    private APSBus bus = new APSBus()

    @Test
    void shouldWork() {
        ID subId = new APSUUID()

        this.bus.subscribe( subId, "local:test", null ) { Map<String, Object> message ->

            this.receivedMessageWork = true

            System.out.println( message.toString() )

            assertEquals( "test", message.get( "msgType" ).toString() )
            assertEquals( "qaz", message.get( "value" ) )

        }

        this.bus.send(
                "local:test",
                MapBuilder.map(
                        "msgType:", "test",
                        "value:", "qaz"
                )
        ) { APSResult res ->
            assertTrue( res.success() )
        }

        assertTrue( this.receivedMessageWork )

        this.bus.unsubscribe( subId )

        this.bus.send(
                "local:test",
                MapBuilder.map(
                        "msgType:", "no-receiver-test",
                        "value:", "zaq"
                )
        ) { APSResult res ->
            assertFalse( res.success() )
            assertEquals( "No subscribers!", res.failure().getMessage() )
        }

    }

    @Test
    void shouldFail() {
        ID subId = new APSUUID()

        // This will not receive anything since target does not start with "local:"!
        this.bus.subscribe( subId, "test", { res ->

            System.out.println( res.failure().toString() )

            assertSame( res.failure().getClass(), APSValidationException.class )
            assertEquals( "'target' does not start with 'local'!", res.failure().getMessage() )
        },
                null // Will never ever be called in this case!
        )

        this.bus.send(
                "test",
                MapBuilder.map(
                        "msgType:", "test",
                        "value:", "qaz"
                )
        ) { res ->
            assertFalse( res.success() )
            assertEquals( "'target' does not start with 'local'!", res.failure().getMessage() )
        }

        assertFalse( this.receivedMessageFail )
    }

    @Test
    void testRequest() {

        // Setup a service to call.

        ID subId = new APSUUID()

        this.bus.subscribe( subId, "local:testService", null ) { Map<String, Object> message ->

            //assert new WellDefinedMessage( message ).isValid(  )
            WellDefinedMessage.INSTANCE.validate( message )

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

            WellDefinedMessage respMsg = new WellDefinedMessage( validate: true, message: [
                    header : [
                            type   : "serviceResponse",
                            version: 1.0
                    ],
                    content: [
                            response: data
                    ]
            ] as Map<String, Object> )

            assert respMsg.isValid()

            this.bus.send( replyAddress, respMsg ) { APSResult result ->

                assert result.success()
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


            assert response[ 'content' ][ 'response' ] == "Something important!"

            // This is of course not a normal thing to do!! But since this is
            // a test and we don't want to remove the subscriber before we called
            // it, we have to wait until now to do it.
            //
            // In real life this would be done on bundle shutdown of bundle providing
            // service.
            this.bus.unsubscribe( subId )
        }

    }
}
