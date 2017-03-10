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

import java.util.Properties;

/**
 * This is an API to publish as an OSGi service and be called back with some object.
 *
 * This is backwards from the more common way of services. Its like a bad Kevin Costner film, if you serve it, objects will come.
 * No, it will not be dead objects! Hmm ... maybe bad comparison. How about the Hollywood principle: Don't call us, we call you!
 *
 * Instead of tracking a service and call it to get something, you publish a service implementing this interface and it will get
 * called with something for you. So the service becomes a client instead and calls you back with the information you want. This
 * to work better with reactive APIs like Vert.x.
 */
public interface Consumer<ConsumedType> {

    /**
     * Specific options for the consumer.
     */
    Properties consumerOptions();

    /**
     * Called with requested object type when available.
     *
     * @param consumed The consumed object.
     */
    void onConsumedAvailable(Consumed<ConsumedType> consumed);

    /**
     * Called when there is a failure to deliver requested object.
     */
    void onConsumedUnavailable();

    /**
     * Called if/when a previously made available object is no longer valid.
     */
    void onConsumedRevoked();

    /**
     * Wraps the provided object and provides possibility to release it.
     *
     * @param <HeldType> The type of the held object.
     */
    interface Consumed<HeldType> {
        /**
         * @return The actual object.
         */
        HeldType get();

        /**
         * Releases the object.
         */
        void release();

        /**
         * Helper implementation.
         *
         * @param <ProviderHeldType> The type held.
         */
        class ConsumedProvider<ProviderHeldType> implements Consumed<ProviderHeldType> {
            private ProviderHeldType object;

            public ConsumedProvider(ProviderHeldType object) {
                this.object = object;
            }

            /**
             * @return The actual object.
             */
            @Override
            public ProviderHeldType get() {
                return object;
            }

            /**
             * Releases the object.
             */
            @Override
            public void release() {}
        }
    }

    /**
     * Utility partial implementation of Consumer.
     *
     * @param <DefaultConsumedType> The type to consume.
     */
    abstract class DefaultConsumer<DefaultConsumedType> implements Consumer<DefaultConsumedType> {

        private Properties options;

        /**
         * A default implementation of Consumer missing only 'void onConsumedAvailable(Consumed<ConsumedType> consumed);'.
         */
        public DefaultConsumer() {
            Properties loadOpts = new Properties();
            loadOptions(loadOpts);
            if (!loadOpts.isEmpty()) {
                this.options = loadOpts;
            }
        }

        /**
         * For overriding.
         */
        @SuppressWarnings("WeakerAccess")
        protected void loadOptions(Properties options) {}

        /**
         * Specific options for the consumer.
         */
        @Override
        public Properties consumerOptions() {
            return this.options;
        }

        /**
         * Called when there is a failure to deliver requested object.
         */
        @Override
        public void onConsumedUnavailable() {}

        /**
         * Called if/when a previously made available object is no longer valid.
         */
        @Override
        public void onConsumedRevoked() {}

    }
}
