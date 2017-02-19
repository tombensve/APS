package se.natusoft.osgi.aps.api.reactive;

import java.util.Properties;

/**
 * This is an API to publish as an OSGi service and be called back with some object.
 *
 * This is backwards from the more common way of services. Its like a bad Kevin Costner film, if you serve it, objects will come.
 * No, it will not be dead objects! Hmm ... maybe bad comparison. How about the Hollywood principle: Don't call us, we call you!
 *
 * Instead of tracking a service and call it to get something, you publish a service implementing this interface and it will get
 * called with something for you. So the service becomes a client instead and calls you back with the information you want. This
 * to work better with reactive APIs like Vert.x.
 *
 * I have to admit to having a great difficulty in coming up with a good name for this! I've renamed it a few times! Since all
 * instances of something (try to ignore that there are primitives for the moment) are an instance of java.lang.Object, I
 * now decided to use the term 'Object' to mean anything/something/whatever. This API is used to consume anything/something/
 * whatever. It can be pure data, it can be functionality, it doesn't really matter what it is. It is something that something
 * else is interested in, whatever it is.
 */
public interface ObjectConsumer<ObjectType> {

    /**
     * Specific options for the consumer.
     */
    Properties options();

    /**
     * Called with requested object type when available.
     *
     * @param object The received object.
     */
    void onObjectAvailable(ObjectHolder<ObjectType> object);

    /**
     * Called when there is a failure to deliver requested object.
     */
    void onObjectUnavailable();

    /**
     * Wraps the provided object and provides possibility to release it.
     *
     * @param <IObjectType> The type of the held object.
     */
    interface ObjectHolder<IObjectType> {
        /**
         * @return The actual object.
         */
        IObjectType use();

        /**
         * Releases the object.
         */
        void release();

        /**
         * Helper implementation.
         *
         * @param <IPObjectType> The type held.
         */
        class ObjectHolderProvider<IPObjectType> implements ObjectHolder<IPObjectType> {
            private IPObjectType object;

            public ObjectHolderProvider(IPObjectType object) {
                this.object = object;
            }

            /**
             * @return The actual object.
             */
            @Override
            public IPObjectType use() {
                return object;
            }

            /**
             * Releases the object.
             */
            @Override
            public void release() {}
        }
    }

    /**
     * Utility partial implementation of ObjectConsumer.
     *
     * @param <DCPObjectType> The type to consume.
     */
    abstract class ObjectConsumerProvider<DCPObjectType> implements ObjectConsumer<DCPObjectType> {

        private Properties options;

        public ObjectConsumerProvider() {
            Properties loadOpts = new Properties();
            loadOptions(loadOpts);
            if (!loadOpts.isEmpty()) {
                this.options = loadOpts;
            }
        }

        /**
         * For overriding.
         */
        protected void loadOptions(Properties options) {}

        /**
         * Specific options for the consumer.
         */
        @Override
        public Properties options() {
            return this.options;
        }

    }
}
