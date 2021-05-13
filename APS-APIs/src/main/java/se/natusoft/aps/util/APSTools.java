package se.natusoft.aps.util;

import se.natusoft.aps.exceptions.APSInvalidException;

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
     * @param max Will timeout at this many milliseconds.
     * @param condition This condition needs to return true for the wait to be over.
     *
     * @throws InterruptedException Due to doing thread.sleep(...)!
     */
    public static void waitFor( long millis, long max, BooleanSupplier condition )
            throws InterruptedException {

        Instant start = Instant.now();
        Instant timeout = start.plusMillis( max );

        while ( !condition.getAsBoolean() && Instant.now().isBefore( timeout ) ) {
            Thread.sleep( millis );
        }
    }

    /**
     * Waits for some condition. Will block thread. Sleeps 100 ms between each check.
     *
     * @param max Will timeout at this many milliseconds.
     * @param condition This condition needs to return true for the wait to be over.
     *
     * @throws InterruptedException Due to doing thread.sleep(...)!
     */
    public static void waitFor( long max, BooleanSupplier condition )
            throws InterruptedException {

        waitFor( 100, max, condition );
    }

    /**
     * Waits for some condition. Will block thread. This is meant as a no timeout, but
     * will timeout in 10 minutes!
     *
     * @param condition This condition needs to return true for the wait to be over.
     *
     * @throws InterruptedException Due to doing thread.sleep(...)!
     */
    public static void waitFor( BooleanSupplier condition )
            throws InterruptedException {

        waitFor( 100, 1000 * 60 * 10, condition );
        if ( !condition.getAsBoolean() ) throw new APSInvalidException(
                "10 minute timeout occurred! This is not supposed to happen ...");
    }
}
