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
 *         2015-01-09: Created!
 *
 */
package se.natusoft.osgi.aps.api.net.messaging.types;

import se.natusoft.osgi.aps.codedoc.Optional;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.UUID;

/**
 * This represents a base message that can be sent and received. Extend this for more detailed messages.
 */
public interface APSMessage {

    /**
     * Returns the complete message as a byte array.
     */
    byte[] getBytes();

    /**
     * Sets the message bytes.
     *
     * @param bytes The bytes to set.
     */
    void setBytes(byte[] bytes);

    /**
     * Returns the UUID of the sender.
     */
    UUID getSenderUUID();

    /**
     * Sets the sender UUID.
     *
     * @param senderUUID The UUID to set.
     */
    void setSenderUUID(UUID senderUUID);

    //
    // Default Provider
    //

    /**
     * Default implementation of APSMessage that can be used when you only want to deal with bytes.
     */
    static abstract class Default implements APSMessage {

        private byte[] bytes;

        /**
         * Returns the complete message as a byte array.
         */
        @Override
        public byte[] getBytes() {
            return this.bytes;
        }

        /**
         * Sets the message bytes.
         *
         * @param bytes The bytes to set.
         */
        @Override
        public void setBytes(byte[] bytes) {
            this.bytes = bytes;
        }
    }
}
