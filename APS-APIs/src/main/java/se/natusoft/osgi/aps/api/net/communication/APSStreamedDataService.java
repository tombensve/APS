/* 
 * 
 * PROJECT
 *     Name
 *         APS APIs
 *     
 *     Code Version
 *         1.0.0
 *     
 *     Description
 *         Provides the APIs for the application platform services.
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
 *         2016-07-02: Created!
 *         
 */
package se.natusoft.osgi.aps.api.net.communication;

import se.natusoft.osgi.aps.constants.APS;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Provides and OutputStream and an InputStream for communicating with some other service.
 *
 * How that is done is irrelevant.
 */
public interface APSStreamedDataService extends APS {

    //
    // Service Registration Property Keys. These are suggested, others can be used.
    //

    /** Some specific type of the connection. Can for example be protocol, like TCP. */
    String TYPE = "type";

    /** A boolean property indicating that the stream is plain as sent, all the way. */
    String TYPE_PLAIN = "transport.plain";

    /** A boolean property indicating that the stream is secured during transmission. */
    String TYPE_SECURE = "transport.secure";


    //
    // Methods
    //

    /**
     * Connects to a configured named connection.
     *
     * @param name The name of the configured connection to connect to.
     *
     * @return An instance of APSStreamedDataConnection representing the configured named connection.
     *
     * @throws IOException on any connection failure.
     */
    APSStreamedDataConnection connect(String name) throws IOException;

    //
    // Inner Classes
    //

    /**
     * This represents a specific connection.
     */
    interface APSStreamedDataConnection {
        /**
         * @return For writing to the TCP socket.
         */
        OutputStream getOutputStream() throws IOException;

        /**
         * @return For reading the TCP socket.
         */
        InputStream getInputStream() throws IOException;
    }
}
