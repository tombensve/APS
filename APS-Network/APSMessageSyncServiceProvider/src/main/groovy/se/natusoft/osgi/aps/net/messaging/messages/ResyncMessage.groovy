/*
 *
 * PROJECT
 *     Name
 *         APS Message Sync Service Provider
 *     
 *     Code Version
 *         1.0.0
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
package se.natusoft.osgi.aps.net.messaging.messages

import se.natusoft.osgi.aps.api.net.messaging.messages.APSBinaryMessage
import se.natusoft.osgi.aps.api.net.messaging.types.APSReSyncEvent

/**
 * A message that asks for a resynchronization of all data. All should answer this.
 */
class ResyncMessage extends APSBinaryMessage {

    public static final String MESSAGE_TYPE = "resync"

    public static final String ALL_KEYS = ""

    //
    // Properties
    //

    /** The key to sync or all keys if "". */
    APSReSyncEvent.Default resyncEvent

    //
    // Constructors
    //

    public ResyncMessage() {
        type = MESSAGE_TYPE
    }

    //
    // Methods
    //

    /**
     * Returns true if all keys should be resynced.
     */
    public boolean isAllKeys() {
        return this.resyncEvent.key.isEmpty()
    }

    /**
     * Reads the content bytes into local model data.
     *
     * @param in The DataInputStream to read from.
     */
    @Override
    public void read(DataInputStream inStream) throws IOException {
        super.read(inStream)
        this.resyncEvent = new APSReSyncEvent.Default()
        this.resyncEvent.key = inStream.readUTF()
    }

    /**
     * Writes the model data into the content bytes.
     *
     * @param outStream The DataOutputStream to write to.
     */
    @Override
    public void write(DataOutputStream outStream) throws IOException {
        super.write(outStream)
        outStream.writeUTF(this.resyncEvent.key)
    }

}
