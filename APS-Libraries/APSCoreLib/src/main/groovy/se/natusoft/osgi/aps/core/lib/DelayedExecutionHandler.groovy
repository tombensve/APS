package se.natusoft.osgi.aps.core.lib

/**
 * Helper to collect things to execute at a later time.
 *
 * @param <T> The executable type to execute later.
 */
class DelayedExecutionHandler<T> extends LinkedList<T> {

    void execute( Closure executor ) {
        while ( !this.isEmpty() ) {
            executor( this.removeFirst() )
        }
    }
}
