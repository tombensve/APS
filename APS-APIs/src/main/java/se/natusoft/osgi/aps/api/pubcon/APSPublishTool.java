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
 *         2017-10-29: Created!
 *
 */
package se.natusoft.osgi.aps.api.pubcon;

import org.osgi.framework.BundleContext;
import se.natusoft.osgi.aps.tools.APSServiceTracker;

import java.util.Map;

/**
 * This provides an implementation of APSPublisher as a pure utility. There is absolutely no requirement
 * to use this. As a publisher it is possible to just use APSServiceTracker directly and call apsConsume(...)
 * on them.
 *
 * This implementation makes use of APSServiceTracker to track consumers. Any publish(...)
 * call will be called on all tracked consumers.
 *
 * Do note that there are some limitations to using this tool. The user will never know who is receiving
 * the published data, which in most cases are completely OK.
 */
public class APSPublishTool<Published> implements APSPublisher<Published> {

    //
    // Private Members
    //

    private APSServiceTracker<APSConsumer> tracker;
    private Mode mode;

    //
    // Constructors
    //

    /**
     * Best way to create an APSPublishTool instance for Java.
     *
     * @param context The OSGi BundleContext. Needed to lookup consumers.
     * @param ldapQueryString The OSGi service lookup ldap type query string to get correct consumers.
     * @param mode The mode of operation: Only to currently known consumers, or to currently known and future consumers.
     */
    @SuppressWarnings("unchecked")
    public APSPublishTool(BundleContext context, String ldapQueryString, Mode mode) {
        this.tracker = new APSServiceTracker(context, APSConsumer.class, ldapQueryString, "2 min");
        this.mode = mode;
    }

    //
    // Methods
    //

    /**
     * Starts the publisher.
     *
     * @return this
     */
    public APSPublishTool start() {
        this.tracker.start();
        return this;
    }

    /**
     * Stops the publisher.
     */
    public void stop() {
        this.tracker.stop();
    }

    /**
     * In all cases but one this will publish to all currently known consumers. The exception is if the
     * APSPublisher.META_ENABLE_STATE_PROPERTY key is available in the meta data. When that happens
     *
     * @param data The published data.
     * @param meta Meta data about the published data.
     */
    @SuppressWarnings("unchecked")
    @Override
    public void apsPublish(Published data, Map<String, String> meta) {
        if (this.mode == Mode.CURRENT_AND_FUTURE) {
            this.tracker.onServiceAvailable((service, serviceRef) -> ((APSConsumer)service).apsConsume(data, meta));
        }
        else {
            this.tracker.withAllAvailableServices((service, args) -> ((APSConsumer) service).apsConsume(data, meta));
        }
    }

    public enum Mode {
        /** For mass publishing like messages. */
        ONLY_CURRENT,

        /**
         * For publishing of long lived objects, that are possibly revokable in the future. Do note that in this
         * case, if a service goes away and later comes back again it will trigger a call to apsConsume(...) with
         * latest data without the publisher having to call apsPublish again! This will go on until stop() is called.
         * This is because APSServiceTracker.onServiceAvailable(-callback-) is used in this mode. It will call the
         * callback whenever a new service is seen for as long as the service tracker is active.
         */
        CURRENT_AND_FUTURE
    }
}
