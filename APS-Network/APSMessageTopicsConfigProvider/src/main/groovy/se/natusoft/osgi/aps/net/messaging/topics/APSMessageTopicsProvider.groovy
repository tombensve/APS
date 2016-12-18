package se.natusoft.osgi.aps.net.messaging.topics

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import se.natusoft.docutations.NotNull
import se.natusoft.docutations.Nullable
import se.natusoft.osgi.aps.api.misc.json.JSONErrorHandler
import se.natusoft.osgi.aps.api.misc.json.model.JSONArray
import se.natusoft.osgi.aps.api.misc.json.model.JSONObject
import se.natusoft.osgi.aps.api.misc.json.model.JSONString
import se.natusoft.osgi.aps.api.misc.json.model.JSONValue
import se.natusoft.osgi.aps.api.misc.json.service.APSJSONService
import se.natusoft.osgi.aps.api.net.messaging.service.APSMessageTopics
import se.natusoft.osgi.aps.net.messaging.topics.config.TopicConfig
import se.natusoft.osgi.aps.tools.APSLogger
import se.natusoft.osgi.aps.tools.annotation.activator.Initializer
import se.natusoft.osgi.aps.tools.annotation.activator.Managed
import se.natusoft.osgi.aps.tools.annotation.activator.OSGiService
import se.natusoft.osgi.aps.tools.annotation.activator.OSGiServiceProvider

/**
 * Provides configured topics by reading JSON as described by APSMessageTopics API.
 */
@SuppressWarnings("GroovyUnusedDeclaration")
@CompileStatic
@TypeChecked
@OSGiServiceProvider
class APSMessageTopicsProvider implements APSMessageTopics {

    //
    // Private Members
    //

    private Map<String, APSMessageTopics.APSTopic> _topics

    private synchronized boolean loaded = false

    //
    // Used services
    //

    @OSGiService
    private APSJSONService apsjsonService

    @Managed(loggingFor = "aps-message-topics-provider")
    private APSLogger logger

    //
    // Methods
    //

    @Initializer
    void init() {
        // We need to run in a thread here since we are accessing a service which might not yet be deployed, and
        // in that case it will not be deployed until after we return from this method since it is called during
        // activator startup.
        Thread.start {
            String jsonConfig = TopicConfig.managed.get().topicConfigJson.string
            ByteArrayInputStream jsonStream = new ByteArrayInputStream(jsonConfig.bytes)

            JSONValue value = this.apsjsonService.readJSON(jsonStream, new JSONErrorHandler() {
                @Override
                void warning(String message) {
                    logger.warn(message)
                }
                @Override
                void fail(String message, Throwable cause) throws RuntimeException {
                    logger.error(message, cause)
                }
            })

            if (value != null) {
                this._topics = new HashMap<>()

                JSONObject topicConfObj = value as JSONObject

                JSONArray topics = topicConfObj.getValue("topics") as JSONArray
                topics.getAsList(JSONObject.class).each { JSONObject topic ->
                    JSONString topicName = topic.getValue("topic") as JSONString

                    APSMessageTopics.APSTopic.Provider topicModel = new APSMessageTopics.APSTopic.Provider(name: topicName.toString())

                    JSONArray protocolsJson = topic.getValue("protocols") as JSONArray
                    protocolsJson.getAsList(JSONString.class).each { JSONString protocol ->
                        if (!protocol.toString().trim().startsWith("%%")) {
                            topicModel.addProtocol(new URI(protocol.toString()))
                        }
                    }

                    this._topics.put(topicName.toString(), topicModel)
                }
            }

            this.loaded = true
        }
    }

    /**
     * This is to ensure we are not trying to read things before they are loaded, which can happen since the loading
     * is done in another thread. We however limit the waiting time and throw an IllegalStateException if the data
     * does not become available in a reasonable time (one second!).
     */
    private void ensureContentLoaded() {
        int count = 0
        while (!this.loaded) {
            synchronized (this) { wait(10) }
            ++count
            if (count > 100) {
                throw new IllegalStateException("Topics have failed to load!")
            }
        }
    }

    /**
     * Returns a specific named topic configuration.
     *
     * @param name The name of the topic to get.
     */
    @Override
    @Nullable
    APSMessageTopics.APSTopic getTopic(@NotNull String name) {
        ensureContentLoaded()
        this._topics?.get(name)
    }

    /**
     * @return All topics.
     */
    @Override
    @Nullable
    List<APSMessageTopics.APSTopic> getTopics() {
        ensureContentLoaded()
        this._topics?.collect { it.value }
    }
}
