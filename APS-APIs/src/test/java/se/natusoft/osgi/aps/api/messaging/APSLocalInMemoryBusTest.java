package se.natusoft.osgi.aps.api.messaging;

import org.junit.Test;

import static org.junit.Assert.*;

import se.natusoft.osgi.aps.exceptions.APSValidationException;
import se.natusoft.osgi.aps.types.APSUUID;
import se.natusoft.osgi.aps.types.ID;
import se.natusoft.osgi.aps.util.MapBuilder;

public class APSLocalInMemoryBusTest {

    private boolean receivedMessageWork = false;
    private boolean receivedMessageFail = false;

    @Test
    public void shouldWork() {
        ID subId = new APSUUID();

        APSLocalInMemoryBus.ROUTER.subscribe( subId, "local:test", null, ( message ) -> {
            this.receivedMessageWork = true;

            System.out.println( message.toString() );

            assertEquals( "test", message.get( "msgType" ) );
            assertEquals( "qaz", message.get( "value" ) );

        } );

        APSLocalInMemoryBus.ROUTER.send(
                "local:test",
                MapBuilder.map(
                        "msgType:", "test",
                        "value:", "qaz"
                ),
                ( res ) -> {
                    assertTrue( res.success() );
                }
        );

        assertTrue( this.receivedMessageWork );

        APSLocalInMemoryBus.ROUTER.unsubscribe( subId );

        APSLocalInMemoryBus.ROUTER.send(
                "local:test",
                MapBuilder.map(
                        "msgType:", "no-receiver-test",
                        "value:", "zaq"
                ),
                ( res ) -> {
                    assertFalse( res.success() );
                    assertEquals( "No subscribers!", res.failure().getMessage() );
                }
        );
    }

    @Test
    public void shouldFail() {
        ID subId = new APSUUID();

        // This will not receive anything since target does not start with "local:"!
        APSLocalInMemoryBus.ROUTER.subscribe( subId, "test",
                ( res ) -> {

                    System.out.println( res.failure().toString() );

                    assertSame( res.failure().getClass(), APSValidationException.class );
                    assertEquals( "'target' does not start with 'local:'!", res.failure().getMessage() );
                },
                null // Will never ever be called in this case!
        );

        APSLocalInMemoryBus.ROUTER.send(
                "test",
                MapBuilder.map(
                        "msgType:", "test",
                        "value:", "qaz"
                ),
                ( res ) -> {
                    assertFalse( res.success() );
                    assertEquals( "'target' does not start with 'local:'!", res.failure().getMessage() );
                }
        );

        assertFalse( this.receivedMessageFail );
    }
}
