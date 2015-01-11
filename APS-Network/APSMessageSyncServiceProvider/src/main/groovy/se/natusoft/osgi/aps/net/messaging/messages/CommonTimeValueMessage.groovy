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

/**
 * A message containing a new common time value. If this is received and the receiving member
 * consider itself the time master it should immediately resign as master. The time master
 * is responsible for sending this every 5 minutes. If this message has not been received
 * within 5 minutes then the member should do a random sleep between 0 and 10 seconds and
 * if no new CommonTimeValueMessage have been received by then it should consider itself
 * the new time master and start sending these instead.
 */
class CommonTimeValueMessage extends APSBinaryMessage {

    public static final String MESSAGE_TYPE = "commonTimeValue"

    //
    // Properties
    //

    /** A date time value. */
    long dateTime

    //
    // Constructors
    //

    public CommonTimeValueMessage() {
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
        this.dateTime = inStream.readLong()
    }

    /**
     * Writes the model data into the content bytes.
     *
     * @param outStream The DataOutputStream to write to.
     */
    public void write(DataOutputStream outStream) throws IOException {
        super.write(outStream)
        outStream.writeLong(this.dateTime)
    }

}
