package se.natusoft.osgi.aps.discovery.service.protocol;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * API for protocol messages.
 */
public interface Protocol {

    /**
     * Reads from the stream into local data.
     *
     * @param dataStream The stream to read from.
     *
     * @throws IOException
     */
    void read(DataInputStream dataStream) throws IOException;

    /**
     * Writes from local data into specified stream.
     *
     * @param dataStream The stream to write to.
     *
     * @throws IOException
     */
    void write(DataOutputStream dataStream) throws  IOException;
}
