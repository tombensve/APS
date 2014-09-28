/* 
 * 
 * PROJECT
 *     Name
 *         APS JGroups Cluster Service Provider
 *     
 *     Code Version
 *         1.0.0
 *     
 *     Description
 *         This provides an implementation of APSClusterService using JGroups to provide the
 *         cluster functionality.
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
 *         2014-09-06: Created!
 *         
 */
package se.natusoft.osgi.aps.net.cluster.service

import groovy.transform.CompileStatic
import org.jgroups.Address
import org.jgroups.JChannel
import org.jgroups.Message as JGMessage
import org.jgroups.Receiver
import org.jgroups.View
import org.jgroups.protocols.*
import org.jgroups.protocols.pbcast.GMS
import org.jgroups.protocols.pbcast.NAKACK
import org.jgroups.protocols.pbcast.STABLE
import org.jgroups.stack.ProtocolStack
import se.natusoft.osgi.aps.api.net.sharing.exception.APSSharingException
import se.natusoft.osgi.aps.api.net.sharing.service.APSClusterInfoService
import se.natusoft.osgi.aps.api.net.sharing.service.APSClusterService
import se.natusoft.osgi.aps.tools.APSLogger
import se.natusoft.osgi.aps.tools.annotation.activator.Managed
import se.natusoft.osgi.aps.tools.annotation.activator.OSGiProperty
import se.natusoft.osgi.aps.tools.annotation.activator.OSGiServiceProvider

/**
 * Provides an implementation of APSClusterService using JGroups to implement.
 */
@CompileStatic
@OSGiServiceProvider(serviceAPIs = [APSClusterService.class, APSClusterInfoService.class],
        properties = [@OSGiProperty(name = "impl", value = "JGroups")])
public class APSJGroupsClusterServiceProvider<Message, State> implements APSClusterService<Message, State>, APSClusterInfoService {

    //
    // Private Members
    //

    @Managed(loggingFor = "aps-jgroups-cluster-service-provider")
    private APSLogger logger

    /** Active clusters. */
    private Map<UUID, APSClusterService.APSCluster<Message>> clusters = new HashMap<>()

    //
    // Methods
    //

    /**
     * Joins the named cluster.
     *
     * @param name The cluster to join.
     * @param member The member to receive cluster messages.
     */
    @Override
    public APSClusterService.APSCluster joinCluster(
            String name,
            APSClusterService.APSClusterMember<Message, State> member
    ) throws APSSharingException {
                                                                          // This fails to compile for some strange reason!
        APSClusterProvider<Message, State> cp = new APSClusterProvider<>()//(name: name, member: member)
        cp.name = name
        cp.member = member
        cp.id = UUID.randomUUID()
        this.clusters.put(cp.id, cp)

        if (cp instanceof APSClusterService.APSClusterReceivingMember) {
            ((APSClusterService.APSClusterReceivingMember)cp).setAPSCluster(cp)
        }

        cp.connect()

        return cp
    }

    /**
     * Removes the cluster with the specified id.
     *
     * @param id The id to remove.
     */
    private void removeCluster(UUID id) {
        this.clusters.remove(id)
    }

    /**
     * Returns information about the current clusters.
     */
    public String getClusterInfo() {
        String ci = new String("{\n    clusters: [\n")

        String ccomma = ""
        this.clusters.each { Map.Entry<UUID,APSClusterService.APSCluster> cluster ->
            ci += "        " + ccomma + "{\n"
            ci += "            id: \"" + cluster.key + "\"\n"
            ci += "            members: ["
            String acomma = ""
            ((APSClusterProvider)cluster.value).view.members.each { Address address ->
                ci += acomma + "\"" + address.toString() + "\""
                acomma = ", "
            }
            ci += "]\n"
            ci += "        }\n"

            ccomma = ", "
        }

        return ci + "\n    ]\n}"
    }

    /**
     * Provides an implementation of APSCluster.
     */
    public class APSClusterProvider<Message, State> implements APSClusterService.APSCluster<Message>, Receiver {
        //
        // Private Members
        //

        /** Our communication channel. */
        private JChannel channel

        /** Will be set to true if a blocked() call is received and to false again on unblock(). */
        private boolean blocked = false

        private List<Message> blockedMessages = Collections.synchronizedList(new LinkedList<>())

        //
        // Properties
        //

        /** Our cluster view. */
        View view

        /** The name of this cluster instance. */
        String name

        /** The member. */
        APSClusterService.APSClusterMember<Message, State> member

        /** The internal id of this member. */
        UUID id

        //
        // Methods
        //

        /**
         * Returns true if we are in blocked mode.
         */
        private synchronized boolean isBlocked() {
            return this.blocked
        }

        /**
         * Connects this cluster provider.
         * @throws APSSharingException
         */
        protected void connect() throws APSSharingException {
            try {
                this.channel = new JChannel(false)
                // TODO: Make configurable
                ProtocolStack stack = new ProtocolStack()
                this.channel.setProtocolStack(stack)
                stack.addProtocol(new UDP().setValue("bind_addr",
                        InetAddress.getLocalHost())) // Note: "localhost" is called loopback by InetAddress so this is not 127.0.0.1!
                        .addProtocol(new PING())
                        .addProtocol(new MERGE3())
                        .addProtocol(new FD_SOCK())
                        .addProtocol(new FD_ALL().setValue("timeout", 12000)
                        .setValue("interval", 3000))
                        .addProtocol(new VERIFY_SUSPECT())
                        .addProtocol(new BARRIER())
                        .addProtocol(new NAKACK())
                        .addProtocol(new UNICAST2())
                        .addProtocol(new STABLE())
                        .addProtocol(new GMS())
                        .addProtocol(new UFC())
                        .addProtocol(new MFC())
                        .addProtocol(new FRAG2())
                stack.init()

                this.channel.setReceiver this
                this.channel.setName this.name + "-" + InetAddress.localHost.hostName + "-" + hashCode()
                this.channel.connect(this.name)
            }
            catch (Exception e) {
                throw new APSSharingException("Connection failed!", e)
            }
        }

        /**
         * Disconnects.
         */
        protected void disconnect() {
            this.channel.disconnect()
            this.channel.close()
        }

        /**
         * Shares a message with the cluster.
         *
         * @param message The message to share.
         *
         * @throws APSSharingException on failure.
         */
        @Override
        public void share(Message message) throws APSSharingException {
            if (!isBlocked()) {
                try {
                    JGMessage msg = new JGMessage()
                    msg.setObject(message)
                    this.channel.send(msg)
                }
                catch (Exception e) {
                    throw new APSSharingException("Failed to share message with cluster!", e)
                }
            }
            else {
                this.blockedMessages.add(message)
                logger.debug("Queued delayed message due to being blocked!")
            }
        }

        /**
         * Requests that current state be sent.
         *
         * @throws APSSharingException
         */
        @Override
        public void requestState() throws APSSharingException {
            try {
                this.channel.getState(null, 12000)
            }
            catch (Exception e) {
                throw new APSSharingException("Failed to request state!", e)
            }
        }

        /**
         * Leaves the cluster.
         */
        @Override
        public void leave() throws APSSharingException {
            try {
                disconnect()
            }
            catch (Exception e) {
                throw new APSSharingException("Failed to disconnect!", e)
            }
            finally {
                removeCluster(this.id)
            }
        }

        /**
         * Called when a message is received.
         *
         * @param msg The received message.
         */
        @Override
        public void receive(JGMessage msg) {
            Message message = (Message)msg.getObject(getClass().getClassLoader())
            if (message != null) {
                try {
                    this.member.shared(message)
                }
                catch (Throwable e) { // No, I really do not trust the member!
                    logger.error("shared(message) callback threw exception!", e)
                }
            }
            else {
                logger.error("Received null message!")
            }
        }

        /**
         * Allows an application to write a state through a provided OutputStream. After the state has
         * been written the OutputStream doesn't need to be closed as stream closing is automatically
         * done when a calling thread returns from this callback.
         *
         * @param output
         *           the OutputStream
         * @throws Exception
         *            if the streaming fails, any exceptions should be thrown so that the state requester
         *            can re-throw them and let the caller know what happened
         * @see java.io.OutputStream#close()
         */
        @Override
        public void getState(OutputStream output) throws Exception {
            State state = (State)this.member.shareState();
            ObjectOutputStream dos = new ObjectOutputStream(output)
            dos.writeObject(state)
            dos.flush()
        }

        /**
         * Allows an application to read a state through a provided InputStream. After the state has been
         * read the InputStream doesn't need to be closed as stream closing is automatically done when a
         * calling thread returns from this callback.
         *
         * @param input
         *           the InputStream
         * @throws Exception
         *            if the streaming fails, any exceptions should be thrown so that the state requester
         *            can catch them and thus know what happened
         * @see java.io.InputStream#close()
         */
        @Override
        public void setState(InputStream input) throws Exception {
            ObjectInputStream ois = new ObjectInputStream(input)
            State state = (State)ois.readObject()
            try {
                this.member.sharedState(state)
            }
            catch (Throwable t) {
                logger.error("Failed to share state with member!", t)
            }
        }

        /**
         * Called when a change in membership has occurred. No long running actions, sending of messages
         * or anything that could block should be done in this callback. If some long running action
         * needs to be performed, it should be done in a separate thread.
         * <p/>
         * Note that on reception of the first view (a new member just joined), the channel will not yet
         * be in the connected state. This only happens when {@link Channel#connect(String)} returns.
         */
        @SuppressWarnings("GroovyDocCheck")
        @Override
        public void viewAccepted(View new_view) {
            this.view = new_view;
        }

        /**
         * Called whenever a member is suspected of having crashed, but has not yet been excluded.
         */
        @Override
        public void suspect(Address suspected_mbr) {
            logger.error "Suspect member (maybee crashed!): ${suspected_mbr}"
        }

        /**
         * Called (usually by the FLUSH protocol), as an indication that the member should stop sending
         * messages. Any messages sent after returning from this callback might get blocked by the FLUSH
         * protocol. When the FLUSH protocol is done, and messages can be sent again, the FLUSH protocol
         * will simply unblock all pending messages. If a callback for unblocking is desired, implement
         * {@link org.jgroups.MembershipListener#unblock()}. Note that block() is the equivalent
         * of reception of a BlockEvent in the pull mode.
         */
        @Override
        public synchronized void block() {
            this.blocked = true
            logger.debug("Being blocked!")
        }

        /**
         * Called <em>after</em> the FLUSH protocol has unblocked previously blocked senders, and
         * messages can be sent again. This callback only needs to be implemented if we require a
         * notification of that.
         *
         * <p>
         * Note that during new view installation we provide guarantee that unblock invocation strictly
         * follows view installation at some node A belonging to that view . However, some other message
         * M may squeeze in between view and unblock callbacks.
         *
         * For more details see https://jira.jboss.org/jira/browse/JGRP-986
         *
         */
        @Override
        public synchronized void unblock() {
            this.blocked = false;

            logger.debug("Were unblocked! (" + this.blockedMessages.size() + " delayed messages in queue!)")

            while (!this.blockedMessages.isEmpty()) {
                Message message = (Message)this.blockedMessages.first()
                try {
                    share(message)
                    this.blockedMessages.remove(message)
                }
                catch (APSSharingException ase) {
                    logger.error("Failed to share delayed message!", ase)
                }
            }
        }
    }
}
