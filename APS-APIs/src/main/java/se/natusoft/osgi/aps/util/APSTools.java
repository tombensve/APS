package se.natusoft.osgi.aps.util;

import java.time.Instant;
import java.util.function.BooleanSupplier;

/**
 * Static tool methods.
 */
public class APSTools {

    /**
     * Waits for some condition. Will block thread.
     *
     * @param millis Number of milliseconds to wait before checking condition again.
     * @param max Max number of milliseconds to wait for condition.
     * @param condition This condition needs to return true for the wait to be over.
     *
     * @throws InterruptedException Due to doing thread.sleep(...)!
     */
    public static void waitFor( long millis, long max, BooleanSupplier condition ) throws InterruptedException {

        Instant start = Instant.now();
        Instant timeout = start.plusMillis( max );

        while ( !condition.getAsBoolean() && Instant.now().isBefore( timeout ) ) {
            Thread.sleep( millis );
        }
    }

    /**
     * Waits for some condition. Will block thread. Sleeps 500 ms between each check.
     *
     * @param max Max number of milliseconds to wait for condition.
     * @param condition This condition needs to return true for the wait to be over.
     *
     * @throws InterruptedException Due to doing thread.sleep(...)!
     */
    public static void waitFor( long max, BooleanSupplier condition ) throws InterruptedException {

        Instant start = Instant.now();
        Instant timeout = start.plusMillis( max );

        while ( !condition.getAsBoolean() && Instant.now().isBefore( timeout ) ) {
            Thread.sleep( 500 );
        }
    }

    /**
     * Waits for some condition. Will block thread. This will wait forever until condition
     * becomes true.
     *
     * @param condition This condition needs to return true for the wait to be over.
     *
     * @throws InterruptedException Due to doing thread.sleep(...)!
     */
    public static void waitFor( BooleanSupplier condition ) throws InterruptedException {

        while ( !condition.getAsBoolean() ) {
            Thread.sleep( 500 );
        }
    }
}
