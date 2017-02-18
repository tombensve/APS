package se.natusoft.osgi.aps.api.reactive;

import java.util.Properties;

/**
 * This is an API to publish as an OSGi service and be called back with some data.
 *
 * This is backwards from the more common way of services. It like a bad Kevin Costner film, if you serve it data will come :-)
 * Instead of tracking a service and call it to get data, you publish a service implementing this interface and it will get
 * called with data for you. So the service becomes a client instead and calls you back with the information you want. This
 * to work better with reactive APIs like Vert.x.
 */
public interface DataConsumer<DataType> {

    /**
     * Specific options for the consumer.
     */
    Properties options();

    /**
     * Called with requested instance type when available.
     *
     * @param data The received data.
     */
    void onDataAvailable(DataHolder<DataType> data);

    /**
     * Called when there is a failure to deliver requested instance.
     */
    void onDataUnavailable();

    /**
     * Wraps the provided instance and provides possibility to release it.
     * @param <IDataType>
     */
    interface DataHolder<IDataType> {
        /**
         * @return The actual instance.
         */
        IDataType use();

        /**
         * Releases the instance.
         */
        void release();

        /**
         * Helper implementation.
         *
         * @param <IPDataType> The type held.
         */
        class DataHolderProvider<IPDataType> implements DataHolder<IPDataType> {
            private IPDataType data;

            public DataHolderProvider(IPDataType data) {
                this.data = data;
            }

            /**
             * @return The actual instance.
             */
            @Override
            public IPDataType use() {
                return data;
            }

            /**
             * Releases the instance.
             */
            @Override
            public void release() {}
        }
    }

    /**
     * Utility partial implementation of DataConsumer.
     *
     * @param <DCPDataType> The type to consume.
     */
    abstract class DataConsumerProvider<DCPDataType> implements DataConsumer<DCPDataType> {

        private Properties options;

        public DataConsumerProvider() {
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
