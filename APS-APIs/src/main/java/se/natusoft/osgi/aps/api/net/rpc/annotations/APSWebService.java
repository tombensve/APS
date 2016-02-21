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
 *         2014-03-08: Created!
 *
 */
package se.natusoft.osgi.aps.api.net.rpc.annotations;

import se.natusoft.osgi.aps.api.external.extprotocolsvc.model.APSRESTCallable;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This marks a method as a web service of REST type. This is purely documentative!!
 *
 * **Do note** that web "serviceability" is provided through cooperation of:
 *
 * - _aps-external-protocol-extender_: This picks up and manages published services that is specified as
 *   "APS-Externalizable". Any method annotated with this annotation is automatically made "externalizable".
 *   This bundle makes use of the OSGi extender pattern.
 *
 * - _aps-streamed-json-rpc-protocol-provider_: Provides _StreamedRPCProtocol_ implementations for Simple JSON, JSON REST,
 *   JSON RPC 1.0 (not completely correct!), JSON RPC 2.0.
 *
 * - _aps-ext-protocol-http-transport-provider_: Provides http transport and makes use of the above 2 bundles to do calls.
 *
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.METHOD)
public @interface APSWebService {}
