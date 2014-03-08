/* 
 * 
 * PROJECT
 *     Name
 *         APS External Protocol Extender
 *     
 *     Code Version
 *         0.10.0
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
 *         2012-01-01: Created!
 *         
 */
package se.natusoft.osgi.aps.externalprotocolextender.pub.config;

import se.natusoft.osgi.aps.api.core.config.APSConfig;
import se.natusoft.osgi.aps.api.core.config.annotation.APSConfigDescription;
import se.natusoft.osgi.aps.api.core.config.annotation.APSConfigItemDescription;
import se.natusoft.osgi.aps.api.core.config.model.APSConfigList;
import se.natusoft.osgi.aps.api.core.config.model.APSConfigValue;

/**
 * Configuration for external protocol support.
 */
@APSConfigDescription(
        configId = "se.natusoft.osgi.aps.external-protocol-extender",
        group = "network.service",
        description = "Allows configuration of local services to be made available externally even when they do not " +
                      "specify the 'APS-Externalizable: true' manifest entry. Those that do specify this manifest and set " +
                      "'APS-Externalizable: false' will be ignored even if they are specified in this configuration! " +
                      "So services can refuse to be made externally available, and some probably should! <p/>" +
                      "Please note that this only makes it possible for the specified services to be made available " +
                      "externally! There also must be one or more bundles deployed that makes use of the APSExternalProtocolService " +
                      "and makes service calls possible through some external protocol for the services to actually become " +
                      "available.",
        version = "1.0.0"
)
public class APSExternalProtocolConfig extends APSConfig {

    @APSConfigItemDescription(description = "List of services to be made externally available.")
    public APSConfigList<ExternalizableService> externalizableServices;

    @APSConfigDescription(
            configId = "se.natusoft.osgi.aps.externalprotocolextender.externalizable.service",
            description = "This specifies registered service APIs to make available externally when they are available locally.",
            version = "1.0.0"
    )
    public static class ExternalizableService extends APSConfig {

        @APSConfigItemDescription(description = "A fully qualified name of a service interface to make externally available.")
        public APSConfigValue serviceQName;

        @APSConfigItemDescription(description = "The version of the service to make externally available.")
        public APSConfigValue serviceVersion;
    }
}
