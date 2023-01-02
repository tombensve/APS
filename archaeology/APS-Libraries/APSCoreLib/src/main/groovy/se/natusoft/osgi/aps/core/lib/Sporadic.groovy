package se.natusoft.osgi.aps.core.lib

import groovy.transform.CompileStatic

/**
 * Executes something in specified intervals until a specified criteria.
 *
 * Usage: Sporadic.until { ... }.interval( 5 ).exec { ... }
 */
@CompileStatic
class Sporadic {

    private int sec
    private Closure<Boolean> criteria

    private Sporadic( Closure<Boolean> criteria ) {
        this.criteria = criteria
    }

    Sporadic interval( int sec ) {
        this.sec = sec
        this
    }

    void exec( Closure todo ) {
        while ( this.criteria.call() ) {
            todo.call()
            Thread.sleep( this.sec * 1000 )
        }
    }

    static Sporadic until( Closure<Boolean> criteria ) {
        new Sporadic(criteria)
    }
}
