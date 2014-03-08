/* 
 * 
 * PROJECT
 *     Name
 *         APS APIs
 *     
 *     Code Version
 *         0.10.0
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
 * This is specified on methods that can be called remotely. It can be used purely for documentation,
 * but in case of a REST API using the JSONREST protocol part of the APSStreamedJSONRPCProtocolProvider
 * bundle this is needed and the REST method that applies must be specified.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface APSRemoteService {
    /**
     * This needs to be provided if you are providing a REST API using JSONREST protocol of the
     * APSStreamedJSONRPCProtocolProvider bundle.
     */
    APSRESTCallable.HttpMethod httpMethod() default APSRESTCallable.HttpMethod.NONE;
}
