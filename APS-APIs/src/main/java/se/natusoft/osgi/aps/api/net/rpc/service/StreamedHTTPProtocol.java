/* 
 * 
 * PROJECT
 *     Name
 *         APS APIs
 *     
 *     Code Version
 *         0.9.1
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
 *         2013-03-16: Created!
 *         
 */
package se.natusoft.osgi.aps.api.net.rpc.service;

/**
 * This is a marker interface indicating that the protocol is really assuming a HTTP transport and is
 * expecting to be able to return http status codes. This also means it will be returning an HTTPError
 * (which extends RPCError) from _createError(...)_.
 *
 * It might be difficult for non HTTP transports to support this kind of protocol, and such should
 * probably ignore these protocols. For example a REST implementation of this protocol will not be
 * writing any error response back, but rather expect the transport to deliver the http status code
 * it provides. A non HTTP transport will not be able to know how to communicate back errors in
 * this case since it will not know anything about the protocol itself.
 */
public interface StreamedHTTPProtocol extends StreamedRPCProtocol {

    /**
     * @return true if the protocol supports REST.
     */
    boolean supportsREST();
}
