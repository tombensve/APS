/* 
 * 
 * PROJECT
 *     Name
 *         APS APIs
 *     
 *     Code Version
 *         0.11.0
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
 *         2012-02-01: Created!
 *         
 */
package se.natusoft.osgi.aps.api.external.extprotocolsvc.model;

/**
 * A listener for externally available services. Please note that this means that the service
 * is available for potential external protocol exposure! For it to be truly available there
 * also has to be a protocol and transport available. It is probably only transports that are
 * interested in this information!
 */
public interface APSExternalProtocolListener {

    /**
     * This gets called when a new externally available service becomes available.
     *
     * @param service The fully qualified name of the newly available service.
     * @param version The version of the service.
     */
    public void externalServiceAvailable(String service, String version);

    /**
     * This gets called when an externally available service no longer is available.
     *
     * @param service The fully qualified name of the service leaving.
     * @param version The version of the service.
     */
    public void externalServiceLeaving(String service, String version);

    /**
     * This gets called when a new protocol becomes available.
     *
     * @param protocolName The name of the protocol.
     * @param protocolVersion The version of the protocol.
     */
    public void protocolAvailable(String protocolName, String protocolVersion);

    /**
     * This gets called when a new protocol is leaving.
     *
     * @param protocolName The name of the protocol.
     * @param protocolVersion The version of the protocol.
     */
    public void protocolLeaving(String protocolName, String protocolVersion);

}
