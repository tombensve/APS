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
 * A message requesting the common time. This is for when the service has just started
 * and don't want to wait until the next CommonTimeValueMessage to get the common time
 * value. When the current time master sees this message it will immediately send out
 * a CommonTimeValueMessage. This is just to get new members up to speed as quickly
 * as possible.
 */
class CommonTimeRequestMessage extends APSBinaryMessage {

    public static final String MESSAGE_TYPE = "commonTimeRequest"

    //
    // Constructors
    //

    public CommonTimeRequestMessage() {
        type = MESSAGE_TYPE
    }

}
