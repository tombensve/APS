package se.natusoft.osgi.aps.api.net.messaging.util;

import se.natusoft.osgi.aps.api.net.messaging.exception.APSMessageException;
import se.natusoft.osgi.aps.api.net.messaging.messages.APSMessage;
import se.natusoft.osgi.aps.api.net.messaging.service.APSMessageService;
import se.natusoft.osgi.aps.api.net.messaging.service.APSExtendedMessageService;

import java.util.LinkedList;
import java.util.List;

/**
 * A APSMessageService wrapper that provides a slightly higher level API, and deals
 * with APSMessage extensions, like APSJSONMessage or whatever messaging type you create.
 *
 * Any APSMessage extension need to set and get bytes on/from APSMessage.
 */
public class APSMessageServiceWrapper<Message extends APSMessage> implements APSExtendedMessageService.Listener {
    //
    // Private Members
    //

    /** The APSBasicMessageService to wrap. */
    private APSMessageService messageService = null;

    /** Factory for creating messages. */
    private MessageFactory<Message> messageFactory;

    /** The messaging listeners. */
    private List<Listener<Message>> listeners = new LinkedList<>();

    //
    // Constructors
    //

    /**
     * Creates a new APSMessageUtil.
     *
     * @param messageService The APSMessageService to use for sending and receiving messages.
     * @param messageFactory Factory for creating new messaging instances.
     */
    public APSMessageServiceWrapper(APSMessageService messageService, MessageFactory<Message> messageFactory) {
        this.messageService = messageService;
        this.messageFactory = messageFactory;
    }

    //
    // Methods
    //

    /**
     * The defined groups made available by this service. That is, the group names that can be passed
     * to sendMessage(...) and readMessage(...) without being guaranteed to throw an exception.
     */
    public List<String> providedGroups() {
        return this.messageService.providedGroups();
    }

    /**
     * Sends a messaging.
     *
     * @param group The messaging group.
     * @param message The actual messaging to send.
     *
     * @throws APSMessageException on failure.
     *
     * @return true if the messaging was sent.
     */
    public boolean sendMessage(String group, Message message) throws APSMessageException {
        return this.messageService.sendMessage(group, message.getData());
    }

    /**
     * Reads a messaging.
     *
     * @param group The messaging group to read from.
     * @param timeout The amount of milliseconds to wait for messaging to become available.
     *
     * @return The messaging bytes or null on timeout.
     *
     * @throws APSMessageException on any failure.
     */
    public Message readMessage(String group, int timeout) throws APSMessageException {
        byte[] data = this.messageService.readMessage(group, timeout);
        Message msg = this.messageFactory.createMessage();
        msg.setData(data);
        return msg;
    }

    /**
     * Adds a messaging listener.
     *
     * @param listener The listener to add.
     */
    public synchronized void addListener(Listener<Message> listener) {
        this.listeners.add(listener);
    }

    /**
     * Removes a listener.
     *
     * @param listener The listener to remove.
     */
    public synchronized void removeListener(Listener<Message> listener) {
        this.listeners.remove(listener);
    }

    /**
     * This is called when a messaging is received.
     *
     * @param group   The messaging group.
     * @param message The messaging.
     */
    @Override
    public void messageReceived(String group, byte[] message) {
        Message msg = this.messageFactory.createMessage();
        msg.setData(message);
        for (Listener<Message> listener : this.listeners) {
            listener.messageReceived(group, msg);
        }
    }

    //
    // Inner Classes
    //

    /**
     * Factory for creating a new messaging.
     *
     * @param <Message> The messaging type.
     */
    public static interface MessageFactory<Message extends APSMessage> {

        /**
         * Creates a new messaging.
         */
        Message createMessage();
    }

    /**
     * This needs to be implemented by messaging listeners.
     *
     * @param <Message> The messaging type.
     */
    public static interface Listener<Message extends APSMessage> {

        /**
         * Message deliver.
         *
         * @param Group The group of the messaging.
         * @param message The messaging.
         */
        void messageReceived(String Group, Message message);
    }
}
