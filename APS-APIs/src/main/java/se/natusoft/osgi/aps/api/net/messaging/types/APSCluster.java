package se.natusoft.osgi.aps.api.net.messaging.types;

import se.natusoft.osgi.aps.api.net.messaging.exception.APSMessagingException;
import se.natusoft.osgi.aps.codedoc.Optional;

import java.util.List;
import java.util.Properties;
import java.util.UUID;

/**
 * This represents a specific cluster.
 */
public interface APSCluster {

    /**
     * Returns the name of this cluster.
     */
    public String getName();

    /**
     * Returns the id of this cluster.
     */
    public UUID getId();

    /**
     * Returns a list of cluster members.
     */
    List<Member> getMembers();

    /**
     * Returns the local member.
     */
    Member getLocalMember();

    /**
     * Returns the read only properties of this cluster.
     */
    Properties getProperties();

    /**
     * Sends a messaging.
     *
     * @param message The message to send.
     *
     * @throws se.natusoft.osgi.aps.api.net.messaging.exception.APSMessagingException on failure.
     *
     * @return true if the messaging was sent.
     */
    boolean sendMessage(APSMessage message) throws APSMessagingException;

    /**
     * Adds a listener for types.
     *
     * @param listener The listener to add.
     */
    void addMessageListener(Listener listener);

    /**
     * Removes a messaging listener.
     *
     * @param group The group listening to.
     * @param listener The listener to remove.
     */
    void removeMessageListener(String group, Listener listener);

    /**
     * Listener for types.
     */
    interface Listener {

        /**
         * This is called when a messaging is received.
         *
         * @param message The received message.
         */
        void messageReceived(APSMessage message);
    }

    /**
     * This represents a cluster member. All but the id is optional.
     */
    interface Member {

        /**
         * Returns an optional name of the member.
         */
        @Optional
        String getName();

        /**
         * Returns an optional host where the member resides.
         */
        @Optional
        String getHost();

        /**
         * Returns the unique id of the member.
         */
        UUID getId();

        /**
         * Default implementation of Member.
         */
        class Default implements Member {

            //
            // Private Members
            //

            /** The unique member id. */
            private UUID id;

            /** The (optional) host the member resides on. */
            private String host;

            /** The (optional) member name. */
            private String name;

            //
            // Constructors
            //

            /**
             * Creates a default implementation of APSCluster.Member.
             */
            public Default() {
                this.id = UUID.randomUUID();
            }

            /**
             * Creates a default implementation of APSCluster.Member.
             *
             * @param id The id of the member.
             * @param host The host the member resides on.
             * @param name The name of the member.
             */
            public Default(UUID id, String host, String name) {
                this.id = id;
                this.host = host;
                this.name = name;
            }

            //
            // Methods
            //

            /**
             * Sets the id of this Member.
             *
             * @param id The id to set.
             */
            public Default setId(UUID id) {
                this.id = id;
                return this;
            }

            /**
             * Sets the host the member resides on.
             *
             * @param host The host to set.
             */
            public Default setHost(String host) {
                this.host = host;
                return this;
            }

            /**
             * Sets the name of the member.
             *
             * @param name The name to set.
             */
            public Default setName(String name) {
                this.name = name;
                return this;
            }

            /**
             * Returns an optional name of the member.
             */
            @Override
            public String getName() {
                return this.name;
            }

            /**
             * Returns the host where the member resides.
             */
            @Override
            public String getHost() {
                return this.host;
            }

            /**
             * Returns the unique id of the member.
             */
            @Override
            public UUID getId() {
                return this.id;
            }
        }
    }
}
