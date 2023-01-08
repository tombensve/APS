package se.natusoft.aps.core.api

import groovy.transform.CompileStatic
import groovyjarjarantlr4.v4.runtime.misc.Nullable

/**
 * Generic handler api inspired by Vert.x.
 *
 * @param <T> The type of a potential value to handle.
 */
@CompileStatic
interface APSHandler<T> {

    /**
     * Does the handling.
     *
     * @param value A value to handle.
     */
    void handle( @Nullable T value )
}
