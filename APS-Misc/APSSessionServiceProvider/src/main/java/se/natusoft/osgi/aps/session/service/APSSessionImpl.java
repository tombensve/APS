/* 
 * 
 * PROJECT
 *     Name
 *         APS Session Service Provider
 *     
 *     Code Version
 *         0.10.0
 *     
 *     Description
 *         Provides an OSGi server wide session.
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
 *         2012-09-08: Created!
 *         
 */
package se.natusoft.osgi.aps.session.service;

import se.natusoft.osgi.aps.api.misc.session.APSSession;
import se.natusoft.osgi.aps.api.misc.session.APSSessionService;
import se.natusoft.osgi.aps.session.config.SessionConfig;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Provides an implementation of APSSession.
 */
public class APSSessionImpl implements APSSession {
    //
    // Private Members
    //

    /** The id of the session. */
    private UUID sessionId = null;

    /** The timeout of this session. */
    private int timeout = APSSessionService.NO_TIMEOUT;

    /** Timestamp of last usage. */
    private Date lastUsed = null;

    /** On object stored in the session by clients. */
    private Map<String, Object> sessionObjects = new HashMap<String, Object>();


    //
    // Constructors
    //

    /**
     * Creates a new APSSessionImpl.
     *
     * @param sessionId The id of this session.
     * @param timeout The timeout of the session.
     */
    public APSSessionImpl(UUID sessionId, int timeout) {
        this.sessionId = sessionId;
        this.lastUsed = new Date();
        this.timeout = timeout;
        if (this.timeout == APSSessionService.DEFAULT_TIMEOUT) {
            this.timeout = SessionConfig.get.timeout.toInt();
        }
    }

    //
    // Methods
    //

    /**
     * Refreshes the last used timestamp on this session.
     */
    public void refreshTimestamp() {
        this.lastUsed = new Date();
    }

    /**
     * Invalidates this session even if it has not timed out.
     */
    public void invalidate() {
        this.lastUsed = null;
        this.sessionObjects = null;
    }

    /**
     * @return The id of this session.
     */
    @Override
    public String getId() {
        refreshTimestamp();
        return this.sessionId.toString();
    }

    /**
     * @return true if this session is still valid.
     */
    @Override
    public boolean isValid() {
        if (this.lastUsed != null) {
            Date now = new Date();
            Date lastUsedPlusTimeout = new Date(this.lastUsed.getTime() + ((1000 * 60) * this.timeout));

            return now.getTime() <= lastUsedPlusTimeout.getTime();
        }

        return false;
    }

    /**
     * Saves an object in the session. Will do nothing if the session is no longer valid.
     *
     * @param name The name to store the object under.
     * @param object An object to store in the session.
     */
    @Override
    public void saveObject(String name, Object object) {
        if (isValid()) {
            this.sessionObjects.put(name, object);
            refreshTimestamp();
        }
    }

    /**
     * Returns the named object or null if it does not exist.
     *
     * @param name The name of the object to get.
     */
    @Override
    public Object retrieveObject(String name) {
        if (isValid()) {
            refreshTimestamp();
            return isValid() ? this.sessionObjects.get(name) : null;
        }

        return null;
    }
}
