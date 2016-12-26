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

    //
    // Inner classes
    //

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
    }
}
