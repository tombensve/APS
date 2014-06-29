/* 
 * 
 * PROJECT
 *     Name
 *         APS External Protocol Extender
 *     
 *     Code Version
 *         0.11.0
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
 *         2012-01-04: Created!
 *         
 */
package se.natusoft.osgi.aps.externalprotocolextender.model;

/**
 * The reason for the service data.
 */
public enum ServiceDataReason {

    SERVICE_AVAILABLE,
    SERVICE_LEAVING,
    INFORMATION_REQUEST,
    EVENT
}
