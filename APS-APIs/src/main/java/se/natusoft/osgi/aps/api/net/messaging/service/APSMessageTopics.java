package se.natusoft.osgi.aps.api.net.messaging.service;

import se.natusoft.docutations.NotNull;

import java.net.URI;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * The following is a JSON topic routing configuration that should be used for defining topic from and to.
 *
 * {
 *     version: "2.4",
 *     topics: [
 *         {
 *             comment: [
 *                 "This defines all communication for one topic. This example is a bit extreme since",
 *                 "it does a lot of protocols/transports in one topic, but it just serves to demonstrate",
 *                 "how things can be setup, and I was a bit lazy and had bad imagination, OK!"
 *             ],
 *             topic: "discovery",
 *             transports: [
 *                 "%% These are URIs specifying transport type (protocol), source/destination and direction.",
 *                 "%% There will be APSTransport services that provide one or more of these protocol/transports",
 *                 "%% and the job will be delegated to first such found.",
 *
 *                 "multicast://all-systems.mcast.net:1234#out",
 *                 "multicast://all-systems.mcast.net:1234#in",
 *                 "tcp://host1:5678#out",
 *                 "tcp://host2:5678#out",
 *                 "tcp://host0:5678#in",
 *                 "udp://host3:9012#out",
 *                 "udp://host0:9012#in",
 *                 "amqp://rabbit1:1234/discovery#out",
 *                 "amqp://rabbit1:1234/discovery#in",
 *                 "jms://hornet1:1234#out",
 *                 "jms://hornet1:1234#in",
 *                 "pigeon://stockholm?cage=1#out",
 *                 "pigeon://visby?cage=3#in"
 *             ]
 *         },
 *         {
 *             ...
 *         }
 *     ]
 * }
 *
 * How this configuration is read and where it is read from is up to the implementation of this service. It could be read
 * from a file, or be specified as one string in APSConfigurationService. In any case every implementation should use this
 * JSON specification in one way or another.
 *
 * An implementation of this service will provide the configuration for a managing bundle to lookup and publish services for.
 * There should be one APSTopicPublisher service for each topic. When a message is received on a topic then the message should
 * be passed on to all instances found of APSTopicSubscriber with an APS.Messaging.Topic property of the same topic as the publisher.
 *
 * The bundle that provides this service can also be the one that provides the APSTopicPublisher services and calls APSTopicSubscriber
 * when data on a topic arrives. It should still provide this configuration as a service also.
 */
public interface APSMessageTopics {

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
         * @return All protocols used by the topic as URIs.
         */
        List<URI> getProtocols();

        class Provider implements APSTopic {
            //
            // Private Members
            //

            /** The name of the topic. */
            private String name;

            /** The protocols for this topic. */
            private List<URI> protocols = new LinkedList<>();

            //
            // Constructors
            //

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
             * @param protocols The protocols for the topic.
             */
            public Provider(@NotNull List<URI> protocols) {
                this.protocols.addAll(protocols);
            }

            //
            // Methods
            //

            /**
             * @return The name of the topic.
             */
            public String getName() {
                return this.name;
            }

            /**
             * @return Returns the protocols for this Topic.
             */
            public List<URI> getProtocols() {
                return this.protocols;
            }

            /**
             * Adds a protocol to this topic.
             *
             * @param protocol The protocol to add.
             */
            public Provider addProtocol(URI protocol) {
                this.protocols.add(protocol);
                return this;
            }
        }
    }

    class Provider implements APSMessageTopics {
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
            List<APSTopic> rtopics = new LinkedList<>();
            for (String name : this.topics.keySet()) {
                rtopics.add(this.topics.get(name));
            }

            return rtopics;
        }
    }
}
