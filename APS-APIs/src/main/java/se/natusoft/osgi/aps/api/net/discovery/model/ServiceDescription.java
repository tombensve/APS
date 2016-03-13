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
 *     Tommy Svensson (tommy@natusoft.se)
 *         Changes:
 *         2012-12-30: Created!
 *
 */
package se.natusoft.osgi.aps.api.net.discovery.model;

import java.time.LocalDateTime;

/**
 * Describes a service.
 */
public interface ServiceDescription {

    /**
     * A short description of the service.
     */
    String getDescription();

    /**
     * An id/name of the service.
     */
    String getServiceId();

    /**
     * The version of the service.
     */
    String getVersion();

    /**
     * The targetHost of the service.
     */
    String getServiceHost();

    /**
     * The targetPort of the service.
     */
    int getServicePort();

    /**
     * The protocol used over the network. Valid values are "TCP", "UDP", and "MULTICAST"
     */
    String getNetworkProtocol();

    /**
     * The protocol of the service.
     */
    String getServiceProtocol();

    /**
     * An optional URL to the service.
     */
    String getServiceURL();

    /**
     * This can be anything that classifies the entry as something more specific. Like a group or queue or whatever.
     */
    String getClassifier();

    /**
     * Describes the content type expected/delivered by the service. For example "XML", "JSON", "Binary:Java:Serialized"
     */
    String getContentType();

    /**
     * Returns the time of last update as a Date. This basically tells the age of the entry.
     */
    LocalDateTime getLastUpdated();

}
