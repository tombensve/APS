package se.natusoft.osgi.aps.tools.util;

import java.io.IOException;

/**
 * Implementations of this is passed to ClientConnectionSupport.
 */
public interface ClientConnection<T> {

    /**
     * This gets called on each connection try.
     *
     * @throws IOException
     */
    T connect() throws IOException;
}
