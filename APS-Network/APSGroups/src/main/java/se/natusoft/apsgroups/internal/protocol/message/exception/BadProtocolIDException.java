/* 
 * 
 * PROJECT
 *     Name
 *         APS APSNetworkGroups
 *     
 *     Code Version
 *         0.9.0
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
 *     Tommy Svensson (tommy@natusoft.se)
 *         Changes:
 *         2012-12-28: Created!
 *         
 */
package se.natusoft.apsgroups.internal.protocol.message.exception;

/**
 * This is thrown when a packet with an unknown protocol id is seen. This will most probably mean
 * that there are more than one protocol communicating on the same multicast address and port.
 */
public class BadProtocolIDException extends Exception {
    //
    // Private Members
    //

    private int protocolId = -1;

    //
    // Constructors
    //

    /**
     * Creates a new BadProtocolIDException.
     *
     * @param protocolId The bad protocol id.
     */
    public BadProtocolIDException(int protocolId) {
        this.protocolId = protocolId;
    }

    //
    // Methods
    //

    /**
     * @return The bad protocol id.
     */
    public int getProtocolId() {
        return this.protocolId;
    }
}
