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
 *     tommy ()
 *         Changes:
 *         2017-02-19: Created!
 *
 */
package se.natusoft.osgi.aps.api.reactive;

/**
 * This is an API to publish as an OSGi service and be called back with some object.
 *
 * This is backwards from the more common way of services. Its like a bad Kevin Costner film, if you serve it, objects will come.
 * No, it will not be dead objects! Hmm ... maybe bad comparison. How about the Hollywood principle: Don't call us, we call you!
 *
 * Instead of tracking a service and call it to get something, you publish a service implementing this interface and it will get
 * called with something for you. So the service becomes a client instead and calls you back with the information you want. This
 * to work better with reactive APIs like Vert.x.
 *
 * You should make use of OSGi service registration properties to tell publishers exactly what object your are interested in.
 */
public interface ManagedObjectConsumer<ConsumedType> {

    /** Use this as key for what you want to consume. */
    String CONSUMED_TYPE = Consumer.CONSUMED_TYPE;

    /**
     * Called with requested object type when available.
     *
     * @param status The status of this call.
     * @param consumed The consumed object.
     */
    void consume(ManagedStatus status, ManagedObject<ConsumedType> consumed);

}
