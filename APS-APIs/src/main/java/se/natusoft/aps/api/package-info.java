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
 * This package and subpackages contains the main APIs of APS. I've decided to use reactive APIs for
 * the flexibility of that. That is, almost all methods will have void return, and result values will
 * be passed forward to handlers.
 *
 * Some APIs are also fluent, or fluent-ish. The reason for not going with fluent to 100% is that with
 * the exception of the APIs more an more of the implementations are done in Groovy. Groovy have really
 * nice support of JavaBeans where you can use "property constructor"s. Example:
 *
 *     new Address(firstName: "Fic", lastName: "Tious", street: "somewhere st. 22", ...)
 *
 * And refer to properties as properties: `address.firstName`. It beats the fluent style with a few
 * characters :-). I do however let most setters return this in the fluent, or rather builder style.
 *
 * If you are familiar with Vertx (and other code done in reactive style) you will notice that the
 * APIs are similar. I've concluded that no matter what the implementation does, this kind of APIs
 * are more flexible and future proof.
 *
 * Since this is OSGi, and one of the APS requirements is that no bundle should have a startup order
 * dependency on any other bundle, the reactive API style really helps. When APSActivator injects
 * a Proxied implementation of the service interface supported by APSServiceTracker, and a service
 * is not yet available, calls can just be cached in a list and later executed when the service is
 * available. This also requires the `nonBlocking = true` on the `@OSGiService` annotation. _At the
 * moment the default is 'false', but this might change_.
 */
package se.natusoft.aps.api;
