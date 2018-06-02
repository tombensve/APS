/* 
 * 
 * PROJECT
 *     Name
 *         APS Vertx Event Bus Messaging Provider
 *     
 *     Code Version
 *         1.0.0
 *     
 *     Description
 *         Provides an implementation of APSMessageService using Vert.x event bus.
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
 *         2018-05-28: Created!
 *         
 */
package se.natusoft.osgi.aps.net.vertx

class AddressResolver {

    @SuppressWarnings("GrMethodMayBeStatic")
    protected String resolveAddress(String destination) {
        // TODO: lookup destination in config and change to configured address if match found. If no match use destination.
        return destination
    }

}
