/**
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
 *     tommy ()
 *         Changes:
 *         2018-05-26: Created!
 *         
 * This package is for general models not connected to a specific function.
 *
 * ## Vertx
 * This package has reactive style APIs. These are heavily inspired by Vertx!
 *
 * At first I considered using the Vertx APIs directly, but that would hardcode Vertx into APS.
 * Yes, currently APS is heavily based on Vertx, but it goes heavily against my inner reflexes
 * to hardcode dependencies, and this is also OSGi code which makes it trivially easy to
 * embed and conceal things with public APIs for functionality. The APS-APIs bundle have
 * very few external dependencies.
 *
 */
package se.natusoft.osgi.aps.model;
