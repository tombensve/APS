package se.natusoft.osgi.aps.model;

import se.natusoft.docutations.Nullable;

/**
 * Generic handler api inspired by Vertx.
 *
 * @param <T> The type of a potential value to handle.
 */
public interface APSHandler<T> {

    /**
     * Does the handling.
     *
     * @param value A value to handle.
     */
    void handle(@Nullable T value);
}
