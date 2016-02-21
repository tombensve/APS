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
 *         2013-03-29: Created!
 *
 */
package se.natusoft.osgi.aps.api.external.extprotocolsvc.model;

/**
 * This is a special variant of APSExternallyCallable that supports a HTTP REST call.
 *
 * This is only available when a service have zero or one method whose name starts with
 * put, zero or one method whose name starts with post, and so on. There has to be at
 * least one method of put, post, get or delete.
 *
 * APSExternalProtocolService can provide an instance of this is a service matches the
 * criteria.
 *
 * This is only of use for HTTP transports! aps-ext-protocol-http-transport-provider
 * does make use of this for protocols that indicate they support REST.
 */
public interface APSRESTCallable extends APSExternallyCallable {

    /**
     * @return true if the service supports the PUT method.
     */
	public boolean supportsPut();

    /**
     * @return true if the service supports the POST method.
     */
	public boolean supportsPost();

    /**
     * @return true if the service supports the GET method.
     */
	public boolean supportsGet();

    /**
     * @return true if the service supports the DELETE method.
     */
	public boolean supportsDelete();

    /**
     * This selects the method to call with this callable.
     *
     * @param method The selected method to call.
     */
	public void selectMethod(HttpMethod method);

    /**
     * This defines the valid choices for selectMethod(...).
     */
	public static enum HttpMethod {
        NONE,
		PUT,
		POST,
		GET,
		DELETE
	}
}
