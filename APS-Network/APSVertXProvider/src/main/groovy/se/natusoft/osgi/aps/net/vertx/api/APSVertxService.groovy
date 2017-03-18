/*
 *
 * PROJECT
 *     Name
 *         APS VertX Provider
 *
 *     Code Version
 *         1.0.0
 *
 *     Description
 *         This service provides configured Vertx instances allowing multiple services to use the same Vertx instance.
 *
 *         This service also provides for multiple instances of VertX by associating an instance with a name. Everyone
 *         asking for the same name will get the same instance.
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
 *         2017-01-01: Created!
 *
 */
package se.natusoft.osgi.aps.net.vertx.api

import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import io.vertx.groovy.core.Vertx

/**
 * This service provides configured Vertx instances allowing multiple services to use the same Vertx instance.
 *
 * This service also provides for multiple instances of VertX by associating an instance with a name. Everyone
 * asking for the same name will get the same instance.
 *
 * An alternative to using this service is to publish a service implementing APSToolsLib/se.natusoft.osgi.aps.tools.reactive.Consumer<Vertx>.
 * It will be called with the Vertx instance when available.
 */
interface APSVertxService {

    /** The Consumer property name for providing an instance name. */
    String NAMED_INSTANCE = "named.instance"

    /** The Consumer property for requesting a named HTTP server instance. */
    String HTTP_SERVICE_NAME = "http.service.name"

    /** Names the default instance. */
    String DEFAULT_INST = "default"

    /**
     * Returns The Groovy Vert.x instance for the specified name. Do getDelegate() on this to get the Java Vertx instance.
     *
     * Only get this once per service and then keep a local copy!
     *
     * @param name The name of the instance to get.
     */
    void useGroovyVertX(String name, Handler<AsyncResult<Vertx>> result)

    /** After having called useGroovyVertX(...) in a bundle, call this when shutting down! */
    void releaseGroovyVertX(String name)
}
