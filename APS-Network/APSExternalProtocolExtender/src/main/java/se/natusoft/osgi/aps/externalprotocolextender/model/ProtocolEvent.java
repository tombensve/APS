/* 
 * 
 * PROJECT
 *     Name
 *         APS External Protocol Extender
 *     
 *     Code Version
 *         1.0.0
 *     
 *     Description
 *         This does two things:
 *         
 *         1) Looks for "APS-Externalizable: true" MANIFEST.MF entry in deployed bundles and if found and bundle status is
 *         ACTIVE, analyzes the service API and creates an APSExternallyCallable wrapper for each service method and
 *         keeps them in memory until bundle state is no longer ACTIVE. In addition to the MANIFEST.MF entry it has
 *         a configuration of fully qualified service names that are matched against the bundles registered services
 *         for which an APSExternallyCallable wrapper will be created.
 *         
 *         2) Registers an APSExternalProtocolExtenderService making the APSExternallyCallable objects handled available
 *         to be called. Note that APSExternallyCallable is an interface extending java.util.concurrent.Callable.
 *         This service is used by other bundles making the service available remotely trough some protocol like
 *         JSON for example.
 *         
 *         This extender is a middleman making access to services very easy to expose using whatever protocol you want.
 *         Multiple protocol bundles using the APSExternalProtocolExtenderService can be deployed at the same time making
 *         services available through more than one protocol.
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
 *         2012-02-03: Created!
 *         
 */
package se.natusoft.osgi.aps.externalprotocolextender.model;

/**
 * For passing new/leaving protocols on the bus to the member sitting on the listeners.
 */
public class ProtocolEvent {
    //
    // Private Members
    //
    
    /** true if the protocol is available, false if it is leaving. */
    private boolean available = true;
    
    /** The name of the protocol. */
    private String name = null;

    /** The version of the protocol */
    private String version = null;

    //
    // Constructors
    //

    /**
     * Creates a new ProtocolEvent.
     * 
     * @param available true if available, false if leaving.
     * @param name The name of the protocol.
     * @param version The version of the protocol
     */
    public ProtocolEvent(boolean available, String name, String version) {
        this.available = available;
        this.name = name;
        this.version = version;
    }
    
    //
    // Methods
    //

    /**
     * Returns true if the protocol is available, false if it is leaving.
     */
    public boolean isAvailable() {
        return this.available;
    }

    /**
     * Returns the name of the protocol.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Returns the version of the protocol.
     */
    public String getVersion() {
        return this.version;
    }
}
