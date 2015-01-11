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
 *         2015-01-11: Created!
 *         
 */
package se.natusoft.osgi.aps.net.messaging.messages

import se.natusoft.osgi.aps.api.net.messaging.messages.APSBinaryMessage
import se.natusoft.osgi.aps.api.net.messaging.types.APSDataPacket
import se.natusoft.osgi.aps.api.net.messaging.types.APSSyncDataEvent
import se.natusoft.osgi.aps.api.net.messaging.types.APSSyncEvent

/**
 * This is message that delivers synchronization data.
 */
class SyncDataMessage extends APSBinaryMessage {

    public static final String MESSAGE_TYPE = "syncData"

    //
    // Properties
    //

    /** The sync event to send. */
    APSSyncDataEvent syncEvent

    //
    // Constructors
    //

    public SyncDataMessage() {
        type = MESSAGE_TYPE
    }

    //
    // Methods
    //

    /**
     * Reads the content bytes into local model data.
     *
     * @param in The DataInputStream to read from.
     */
    public void read(DataInputStream inStream) throws IOException {
        super.read(inStream)
        APSSyncDataEvent.Default se = new APSSyncDataEvent.Default()
        se.timestamp = inStream.readLong()
        se.key = inStream.readUTF()
        int noBytes = inStream.readInt();
        byte[] bytes = new byte[noBytes]
        inStream.read(bytes)
        se.content = new APSDataPacket.Default(bytes)
        this.syncEvent = se
    }

    /**
     * Writes the model data into the content bytes.
     *
     * @param outStream The DataOutputStream to write to.
     */
    public void write(DataOutputStream outStream) throws IOException {
        if (this.syncEvent == null) throw new IOException("No APSSyncEvent available to write!")
        super.write(outStream)
        outStream.writeLong(this.syncEvent.timestamp)
        outStream.writeUTF(this.syncEvent.key)
        outStream.writeInt(this.syncEvent.content.content.length)
        outStream.write(this.syncEvent.content.content)
    }

}
