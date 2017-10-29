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
 *         2017-10-29: Created!
 *
 * ------------------------------------------------------------------------------------------------------------------
 *
 * "pubcon" is short for "publish" and "consume". It is a variant of "pubsub", but I didn't like the "subscribe" part
 * since it sounds quite specific. I see it as being able to publish something, and to consume something published.
 * Yes, to consume something you most likely need to subscribe to the consumed information in some way. Pubsub still
 * puts some kind of picture in peoples minds, and this both does and does not deliver that picture.
 *
 * The goal here is to make things very, very simple and reusable for any similar situation no matter what it is.
 * Consumers will register themselves as OSGi services and will be called with the consumed information. The OSGi
 * service properties provide specification of what it wants to consume. The publishers make use of a service tracker
 * to find the consumers interested in their information.
 */
 package se.natusoft.osgi.aps.api.pubcon;
