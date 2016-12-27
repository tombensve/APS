package se.natusoft.osgi.aps.net.messaging.topics

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import se.natusoft.docutations.NotNull
import se.natusoft.docutations.Nullable
import se.natusoft.osgi.aps.api.core.config.event.APSConfigChangedEvent
import se.natusoft.osgi.aps.api.core.config.event.APSConfigChangedListener
import se.natusoft.osgi.aps.api.net.messaging.service.APSMessageTopicsService
import se.natusoft.osgi.aps.net.messaging.topics.config.TopicConfig
import se.natusoft.osgi.aps.tools.APSLogger
import se.natusoft.osgi.aps.tools.annotation.activator.Initializer
import se.natusoft.osgi.aps.tools.annotation.activator.Managed
import se.natusoft.osgi.aps.tools.annotation.activator.OSGiServiceProvider

/**
 * Provides configured topics by reading JSON as described by APSMessageTopics API.
 */
@SuppressWarnings("GroovyUnusedDeclaration")
@CompileStatic
@TypeChecked
@OSGiServiceProvider
class APSMessageTopicsServiceProvider implements APSMessageTopicsService, APSConfigChangedListener {

    //
    // Private Members
    //

    private Map<String, APSMessageTopicsService.APSTopic> _topics = null

    private List<APSMessageTopicsService.TopicsUpdatedListener> topicListeners = new LinkedList<>()

    //
    // Used services
    //

    @Managed(loggingFor = "aps-message-topics-provider")
    private APSLogger logger

    //
    // Methods
    //

    /**
     * This is to ensure we are not trying to read things before they are loaded, which can happen since the loading
     * is done in another thread. We however limit the waiting time and throw an IllegalStateException if the data
     * does not become available in a reasonable time (one second!).
     */
    private synchronized void loadContent() {
        TopicConfig topicConfig = TopicConfig.managed.get()

        this._topics = new HashMap<>()
        topicConfig.topics.each { TopicConfig.Topic topic ->
            APSMessageTopicsService.APSTopic apsTopic =
                    new APSMessageTopicsService.APSTopic.Provider(name: topic.name.string, protocol: topic.protocol.string)
            this._topics.put(apsTopic.name, apsTopic)
        }
    }

    @Initializer
    init() {
        Thread.start {
            loadContent()
            TopicConfig.managed.get().addConfigChangedListener(this)
        }
    }

    /**
     * Event listener callback when event occurs.
     *
     * @param event information about the event.
     */
    synchronized void apsConfigChanged(APSConfigChangedEvent event) {
        this._topics = null
        loadContent()
        triggerTopicsUpdatedEvent()
    }

    /**
     * Returns a specific named topic configuration.
     *
     * @param name The name of the topic to get.
     */
    @Override
    @Nullable
    APSMessageTopicsService.APSTopic getTopic(@NotNull String name) {
        this._topics?.get(name)
    }

    /**
     * @return All topics.
     */
    @Override
    @Nullable
    List<APSMessageTopicsService.APSTopic> getTopics() {
        this._topics?.collect { it.value }
    }

    /**
     * Adds a listener for updates to the topics.
     *
     * @param listener The listener to add.
     */
    @Override
    void addTopicsUpdatedListener(APSMessageTopicsService.TopicsUpdatedListener listener) {
        this.topicListeners += listener
    }

    /**
     * Removes a listener from receiving updates of changed topics.
     *
     * @param listener The listener to remove.
     */
    @Override
    void removeTopicsUpdatedListener(APSMessageTopicsService.TopicsUpdatedListener listener) {
        this.topicListeners -= listener
    }

    /**
     * Updates topics listeners.
     */
    private void triggerTopicsUpdatedEvent() {
        Thread.start {
            this.topicListeners.each { APSMessageTopicsService.TopicsUpdatedListener listener ->
                try {
                    listener.topicsUpdated()
                }
                catch (Exception e) {
                    logger.error("Failed updating topics listener!", e)
                }
            }
        }
    }
}
