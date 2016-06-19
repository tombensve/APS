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
 *         2016-04-04: Created!
 *
 */
package se.natusoft.osgi.aps.api.net.discovery;

/**
 * This contains a set of keys that can be used with the APSSimpleDiscoveryService.
 */
public class DiscoveryKeys {

    /** A name of the service. */
    public static final String NAME = "name";

    /** The version of the service. */
    public static final String VERSION = "version";

    /** An URI as used by APSTcpipService. */
    public static final String APS_URI = "apsURI";

    /** A URL for accessing the service. */
    public static final String URL = "url";

    /** The port of the service. */
    public static final String PORT = "port";

    /** The host of the service. */
    public static final String HOST = "host";

    /** Some description of the type of the content provided by the service. */
    public static final String CONTENT_TYPE = "contentType";

    /** An informative description of the service. */
    public static final String DESCRIPTION = "description";

    /** This is used by APSClusterService to announce cluster members. */
    public static final String APS_CLUSTER_NAME = "apsClusterName";

    /** The protocol of the service, like TCP, UDP, Multicast */
    public static final String PROTOCOL = "protocol";

    /** A timestamp of when the entry was last updated. */
    public static final String LAST_UPDATED = "lastUpdated";
}
