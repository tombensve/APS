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
 *         2017-01-01: Created!
 *         
 */
package se.natusoft.osgi.aps.api.net.messaging.service;

import se.natusoft.docutations.NotNull;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * This service should provide a mapping of topics and protocol implementations. This information should be providable in
 * some config. This is to be used in conjunction with APSRoutingMessageService.
 */
public interface APSMessageTopicsService {

    /**
     * Returns a specific named topic configuration.
     *
     * @param name The name of the topic to get.
     */
    APSTopic getTopic(String name);

    /**
     * @return All topics.
     */
    List<APSTopic> getTopics();

    /**
     * Adds a listener for updates to the topics.
     *
     * @param listener The listener to add.
     */
    void addTopicsUpdatedListener(TopicsUpdatedListener listener);

    /**
     * Removes a listener from receiving updates of changed topics.
     *
     * @param listener The listener to remove.
     */
    void removeTopicsUpdatedListener(TopicsUpdatedListener listener);

    //
    // Inner classes
    //

    interface TopicsUpdatedListener {
        /**
         * Indicates that the list of topics have been updaetd.
         */
        void topicsUpdated();
    }

    /**
     * Represents one specific topic.
     */
    interface APSTopic {

        /**
         * @return The name of the topic.
         */
        String getName();

        /**
         * @return returns the protocol to use for the actual messaging.
         */
        String getProtocol();

        class Provider implements APSTopic {
            //
            // Private Members
            //

            /** The name of the topic. */
            private String name;

            /** The protocol for this topic. */
            private String protocol;

            //
            // Constructors
            //

            /**
             * Creates a new Topic provider.
             */
            public Provider() {}

            /**
             * Creates a new Topic provider.
             *
             * @param name The name to the topic.
             */
            public Provider(@NotNull String name) {
                this.name = name;
            }

            /**
             * Creates a new Topic provider.
             *
             * @param name The name to the topic.
             * @param protocol The protocol to use for the name.
             */
            public Provider(@NotNull String name, String protocol) {
                this.name = name;
                this.protocol = protocol;
            }

            //
            // Methods
            //

            /**
             * @return The name of the topic.
             */
            @Override
            public String getName() {
                return this.name;
            }

            /**
             * Sets the name of the topic.
             *
             * @param name The name to set.
             */
            public void setName(String name) {
                this.name = name;
            }

            @Override
            public String getProtocol() {
                return this.protocol;
            }

            public void setProtocol(String protocol) {
                this.protocol = protocol;
            }

        }
    }

    class Provider implements APSMessageTopicsService {
        //
        // private Members
        //

        /** The available topics. */
        private Map<String, APSTopic> topics = new HashMap<>();

        /** Topics listeners. */
        private List<TopicsUpdatedListener> listeners = new LinkedList<>();

        //
        // Constructors
        //

        /**
         * Creates a new Provider.
         */
        public Provider() {}

        /**
         * Creates a new Provider.
         *
         * @param topics The topics to provide.
         */
        public Provider(List<APSTopic> topics) {
            for (APSTopic topic : topics) {
                this.topics.put(topic.getName(), topic);
            }
        }

        //
        // Methods
        //

        /**
         * Adds a topic.
         *
         * @param topic The topic to add.
         */
        public void addTopic(APSTopic topic) {
            this.topics.put(topic.getName(), topic);
        }

        /**
         * Returns a specific named topic configuration.
         *
         * @param name The name of the topic to get.
         */
        @Override
        public APSTopic getTopic(String name) {
            return this.topics.get(name);
        }

        /**
         * @return All topics.
         */
        @Override
        public List<APSTopic> getTopics() {
            List<APSTopic> _topics = new LinkedList<>();
            for (String name : this.topics.keySet()) {
                _topics.add(this.topics.get(name));
            }

            return _topics;
        }

        /**
         * Adds a listener for updates to the topics.
         *
         * @param listener The listener to add.
         */
        @Override
        public void addTopicsUpdatedListener(TopicsUpdatedListener listener) {
            this.listeners.add(listener);
        }

        /**
         * Removes a listener from receiving updates of changed topics.
         *
         * @param listener The listener to remove.
         */
        @Override
        public void removeTopicsUpdatedListener(TopicsUpdatedListener listener) {
            this.listeners.remove(listener);
        }

        /**
         * Informs all listeners that the list of topic have been updated.
         */
        protected void triggerTopicsUpdatedEvent() {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    for (TopicsUpdatedListener listener : listeners) {
                        try {
                            listener.topicsUpdated();
                        }
                        catch (Exception e) {
                            System.err.println(e.getMessage());
                        }
                    }
                }
            }).start();
        }
    }
}
