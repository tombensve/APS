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
 *         2011-10-16: Created!
 *
 */
package se.natusoft.osgi.aps.api.net.discovery.service;


import se.natusoft.osgi.aps.api.net.discovery.exception.APSDiscoveryException;

import java.util.Properties;
import java.util.Set;

/**
 * A network service discovery.
 *
 * There a many such services available in general, a bit less from a java perspective, but the intention
 * with this is not to compete with any of the others, but to provide an extremely simple way to discover
 * remote services in an as simple an primitive way as possible. Basically a way to have multiple hosts
 * running APS based code find each other easily, may it be by simple configuration or by multicast or
 * TCP, or wrapping some other service.
 */
public interface APSSimpleDiscoveryService {

    //
    // Methods
    //

    /**
     * On a null filter all services are returned. The filter is otherwise of LDAP type: (&(this=that)(something=pizza)).
     *
     * @param filter The filter to narrow the results.
     */
    Set<Properties> getServices(String filter);

    /**
     * Publishes a local service. This will announce it to other known APSSimpleDiscoveryService instances.
     *
     * @param serviceProps This is a set of properties describing the service. There are some suggested
     *                     keys in DiscoveryKeys for general compatibility.
     *
     * @throws APSDiscoveryException on problems to publish (note: this is a runtime exception!).
     */
    void publishService(Properties serviceProps) throws APSDiscoveryException;

    /**
     * Recalls the locally published service, announcing to other known APSSimpleDiscoveryService instances that this
     * service is no longer available.
     *
     * @param serviceProps The same service properties used to publish service.
     *
     * @throws APSDiscoveryException on problems to publish (note: this is a runtime exception!).
     */
    void unpublishService(Properties serviceProps) throws APSDiscoveryException;

    /**
     * Recalls the locally published service, announcing to other known APSSimpleDiscoveryService instances that this
     * service is no longer available.
     *
     * @param unpublishFilter An LDAP type filter that matches an entry or entries to unpublish. Any non locally published
     *                        services cauth in the filter will be ignored.
     *
     * @throws APSDiscoveryException on problems to publish (note: this is a runtime exception!).
     */
    void unpublishService(String unpublishFilter) throws APSDiscoveryException;
}
