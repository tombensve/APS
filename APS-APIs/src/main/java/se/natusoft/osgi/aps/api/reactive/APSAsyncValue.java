package se.natusoft.osgi.aps.api.reactive;

import se.natusoft.docutations.NotNull;

/**
 * This represents an asynchronously delivered value. Inspired by Vertx even though I changed name format.
 *
 * @param <T> The value type.
 */
public interface APSAsyncValue<T> {

    /**
     * @return The held value.
     */
    @NotNull
    T value();

    /**
     * Default and very simple implementation.
     *
     * @param <T> The value type.
     */
    class Provider<T> implements APSAsyncValue<T> {
        private transient T value;

        /**
         * Creates a new Provider.
         *
         * @param value The value to hold.
         */
        public Provider(@NotNull T value) {
            this.value = value;
        }

        /**
         * @return The held value.
         */
        @NotNull
        public T value() {
            return this.value;
        }
    }
}
