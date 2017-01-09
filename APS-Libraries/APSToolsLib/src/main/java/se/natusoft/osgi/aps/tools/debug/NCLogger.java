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
package se.natusoft.osgi.aps.tools.debug;

import java.io.IOException;
import java.net.Socket;

/**
 * This class connects to an IP address and a  port and sends all output to that destination.
 *
 * This is intended to be used in conjunction with nc (net cat) like: nc -k -l localhost 10999
 */
public class NCLogger {

    //
    // Private Members
    //

    private String host;

    private int port;

    //
    // Constructors
    //

    /**
     * Creates a new NCLogger instance.
     *
     * @param host The host to write to.
     * @param port The port to write to.
     */
    public NCLogger(String host, int port) {
        this.host = host;
        this.port = port;
    }

    //
    // Methods
    //

    /**
     * Logs a line of text.
     *
     * @param text The text to log.
     */
    public void log(String text) {
        try {
            Socket socket = new Socket(this.host, this.port);
            socket.setSoTimeout(2000);
            socket.getOutputStream().write(text.getBytes());
            socket.getOutputStream().write("\n".getBytes());
            socket.close();
        }
        catch (IOException ignore) {}
    }

}
