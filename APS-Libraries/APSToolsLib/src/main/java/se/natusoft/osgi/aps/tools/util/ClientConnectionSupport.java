package se.natusoft.osgi.aps.tools.util;

import java.io.IOException;
import java.net.UnknownHostException;

/**
 * This is a utility that lets clients do multiple tries to connect with waiting time between before failing.
 */
public class ClientConnectionSupport<T> {
    //
    // Private Members
    //

    /** The connection to make. */
    private final ClientConnection<T> clientConnection;

    /** The maximum number of tries to do before failing. */
    private final int maxTries;

    /** The delay between each try. */
    private final long delay;

    //
    // Constructors
    //

    /**
     * Creates a new ClientConnectionSupport.
     *
     * @param maxTries The maximum number of tries to make before failing.
     * @param delay The delay between each try in milliseconds.
     * @param clientConnection The connection to make.
     */
    public ClientConnectionSupport(int maxTries, long delay, ClientConnection<T> clientConnection) {
        this.maxTries = maxTries;
        this.delay = delay;
        this.clientConnection = clientConnection;
    }

    //
    // Methods
    //

    /**
     * Executes the connection.
     *
     * @throws IOException on failure.
     */
    public T connect() throws IOException {
        int currentCount = 0;

        T result = null;

        while (currentCount < this.maxTries) {
            try {
                result = this.clientConnection.connect();
                break;
            }
            catch (UnknownHostException uhe) {
                throw uhe;
            }
            catch (IOException ioe) {
                ++currentCount;
                if (currentCount >= this.maxTries) {
                    throw ioe;
                }
                try {
                    Thread.sleep(this.delay);
                }
                catch (InterruptedException ie) {
                    throw new IOException("Failed to do Thread.sleep(delay)!", ie);
                }
            }
        }

        return result;
    }
}
