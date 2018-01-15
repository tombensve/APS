package se.natusoft.osgi.aps.api.messaging;

import se.natusoft.osgi.aps.api.reactive.APSHandler;

import java.util.Map;

/**
 * This provides a full service API for both sending and receiving messages.
 *
 * This API is broken up into parts: APSPublisher, APSSender, and APSSubscriber.
 * It is fully possible to provide services by publishing 2 or 3 of these without
 * this main API as OSGi services. For APSSender and APSPublisher they then need
 * to be preconfigured. Due to the niceness of OSGi you can publish multiple
 * preconfigured instances and have clients find the correct one.
 *
 * So in other words, using this full API is optional.
 *
 * @param <Message> The data type published or subscribed to.
 */
public interface APSMessageService<Message> extends APSSubscriber<Message> {

    //
    // Constants
    //

    /** A target for messages. Can be a topic, and address or something else.  */
    String TARGET = "aps-msg-target";

    //
    // Methods
    //

    /**
     * Returns a publisher to publish with.
     *
     * The handler will receive an implementation of APSPublisher from which several publishings
     * can be done using the same params.
     *
     * @param properties Properties for the publisher. These are implementation specific.
     * @param handler Will be called with the APSPublisher to use for publishing messages.
     */
    void publisher(Map<String, String> properties, APSHandler<APSPublisher<Message>> handler);

    /**
     * Returns a sender to send with. Depending on implementation the APSSender instance returned can possibly
     * be an APSReplyableSender that allows for providing a subscriber for a reply to the sent message.
     *
     * The handler will receive an implementation of APSSender from which several messages can be sent
     * using the same params.
     *
     * @param properties Properties for the sender. These are implementation specific.
     * @param handler will be called with the APSSender to use for sending messages.
     */
    void sender(Map<String, String> properties, APSHandler<APSSender<Message>> handler);

}
