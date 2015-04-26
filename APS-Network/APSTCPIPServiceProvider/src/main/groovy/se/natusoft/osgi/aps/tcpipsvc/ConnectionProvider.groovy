/*
 *
 * PROJECT
 *     Name
 *         APS TCPIP Service NonSecure Provider
 *
 *     Code Version
 *         1.0.0
 *
 *     Description
 *         Provides a nonsecure implementation of APSTCPIPService.
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
 *         2015-04-11: Created!
 *
 */
package se.natusoft.osgi.aps.tcpipsvc

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked

/**
 * This represents a provider for an UDP, Multicast or TCP connection.
 */
@CompileStatic
@TypeChecked
interface ConnectionProvider {

    /**
     * Defines the types of providers.
     */
    public static enum Type {
        TCP,
        UDP,
        Multicast
    }

    /**
     * Defines the direction of a provider.
     */
    public static enum Direction {
        Read,
        Write
    }

    /**
     * Starts the provider.
     *
     * @throws IOException
     */
    void start() throws IOException

    /**
     * Stops the provider.
     *
     * @throws IOException
     */
    void stop() throws IOException

    /**
     * This method is called when configuration have been updated.
     */
    void configChanged() throws IOException

    /**
     * Returns the type of the connection.
     */
    ConnectionProvider.Type getType();
    //      ^-- Make IDEA happy!

    /**
     * Returns the direction of the connection.
     */
    Direction getDirection();
}
