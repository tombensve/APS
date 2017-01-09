/* 
 * 
 * PROJECT
 *     Name
 *         APS Vertx TCP Messaging Provider
 *     
 *     Code Version
 *         1.0.0
 *     
 *     Description
 *         Provides messaging over TCP/IP using Vert.x Net service.
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
 *         2017-01-04: Created!
 *         
 */
package se.natusoft.osgi.aps.net.messaging.vertx.api

/**
 * This is a service API for providers of options to provide an implementation of.
 */
interface APSVertxTCPMessagingOptions {

    /**
     * @return A Map containing Vert.x Net server options. Contents: name, value
     */
    Map<String, Object> getServerOptions()

    /**
     * @return A Map containing Vert.x Net client options. Contents: name, value.
     */
    Map<String, Object> getClientOptions()

    /**
     * @return Mappings between topic and the URI for the topic.
     */
    Map<String, String> getTopicToURIMapping()
}
