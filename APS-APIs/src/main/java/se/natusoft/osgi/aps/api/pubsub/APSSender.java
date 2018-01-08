package se.natusoft.osgi.aps.api.pubsub;

public interface APSSender<Message> {

    /**
     * Sends a message. This usually goes to one receiver. See implementaion documentation for more information.
     *
     * @param message The message to send.
     */
    APSSender<Message> send(Message message);

}
