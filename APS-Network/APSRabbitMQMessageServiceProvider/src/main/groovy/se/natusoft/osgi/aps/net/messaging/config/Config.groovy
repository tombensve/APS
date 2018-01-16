/*
 *
 * PROJECT
 *     Name
 *         APS RabbitMQ SimpleMessageService Provider
 *
 *     Code Version
 *         1.0.0
 *
 *     Description
 *         Provides an implementation of APSMessageService using RabbitMQ Java Client.
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
 *         2013-09-01: Created!
 *
 */
package se.natusoft.osgi.aps.net.messaging.config

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked

/**
 * Configuration for RabbitMQ connections.
 */
@CompileStatic
@TypeChecked
final class Config {

    // Temporary config solution.
    static final Map<String, Serializable> config = [
            host:     "localhost",
            port:     5672,
            user:     "guest",
            password: "guest",
            virtualHost: "",

            timeout: 0,

            instances: [
                    default: [
                            name: "default",
                            exchange: "",
                            exchangeType: "fanout",
                            queue: "",
                            routingKey: ""
                    ]
            ]
    ]

}
