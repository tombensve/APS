/* 
 * 
 * PROJECT
 *     Name
 *         APS Tools Library
 *     
 *     Code Version
 *         1.0.0
 *     
 *     Description
 *         Provides a library of utilities, among them APSServiceTracker used by all other APS bundles.
 *         
 * COPYRIGHTS
 *     Copyright (C) 2012 by Natusoft AB All rights reserved.
 *     
 * LICENSE
 *     Apache 2.0 (Open Source)
 *     
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *     
 *       http://www.apache.org/licenses/LICENSE-2.0
 *     
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *     
 * AUTHORS
 *     tommy ()
 *         Changes:
 *         2017-01-05: Created!
 *         
 */
package se.natusoft.osgi.aps.tools.util;

import java.io.IOException;
import java.net.UnknownHostException;

/**
 * This is a utility that lets clients do multiple tries to connect with waiting time between before failing.
 */
public class ClientMultiTryConnection<T> implements ClientConnection<T> {
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
    public ClientMultiTryConnection(int maxTries, long delay, ClientConnection<T> clientConnection) {
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
