/* 
 * 
 * PROJECT
 *     Name
 *         APS Cluster Service Sync Service
 *     
 *     Code Version
 *         1.0.0
 *     
 *     Description
 *         This is an implementation of APSSyncService that uses APSClusterService to do the synchronization with.
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
 *         2014-08-25: Created!
 *         
 */
package se.natusoft.osgi.aps.sync.message;

import java.io.Serializable;

/**
 * This is a message sent for synchronization.
 */
public class SyncMessage<SyncType> implements Serializable {

    //
    // Private Members
    //

    /** The actual synchronization data. */
    private SyncType syncData;

    /** The type of the message. */
    private MessageType messageType = MessageType.SYNC;

    //
    // Constructors
    //

    /**
     * Creates a new SyncMessage.
     *
     * @param syncData The data to sync.
     */
    public SyncMessage(SyncType syncData) {
        this.syncData = syncData;
    }

    /**
     * Creates a new SyncMessage.
     *
     * @param messageType The type of the sync message.
     */
    public SyncMessage(MessageType messageType) {
        this.messageType = messageType;
    }

    //
    // Methods
    //

    /**
     * Returns the sync data part of the message.
     */
    public SyncType getSyncData() {
        return this.syncData;
    }

    /**
     * Returns the type of the message.
     */
    public MessageType getMessageType() {
        return this.messageType;
    }

    //
    // Inner Classes
    //

    public static enum MessageType {
        SYNC,
        MEMBER_REFRESH
    }
}
