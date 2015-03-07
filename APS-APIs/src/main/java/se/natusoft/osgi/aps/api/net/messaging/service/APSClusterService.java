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
package se.natusoft.osgi.aps.api.net.messaging.service;

import se.natusoft.osgi.aps.api.net.messaging.types.APSCommonDateTime;

import java.util.List;
import java.util.Map;

/**
 * All of APSMessageService, APSSynchronizedMapService, and APSSyncService are part of some cluster which
 * they communicate with. This represents that cluster in a general form.
 *
 * Since APSClusterService extends APSMessageService both are provided by the same bundle. The sync services can also
 * be part of the same bundle, but can also be implemented on top of APSMessageService.
 */
public interface APSClusterService extends APSMessageService {

    /**
     * Returns the name of the cluster.
     */
    String getName();

    /**
     * If the implementation has the notion of a master and this node is the master then
     * true is returned. In all other cases false is returned.
     */
    boolean isMasterNode();

    /**
     * Returns the network common DateTime that is independent of local machine times.
     */
    APSCommonDateTime getCommonDateTime();

}
