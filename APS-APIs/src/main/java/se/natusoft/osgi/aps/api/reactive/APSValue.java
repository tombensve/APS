package se.natusoft.osgi.aps.api.reactive;

import se.natusoft.docutations.NotNull;

/**
 * This represents a possibly asynchronously delivered value. Inspired by Vertx even though I changed name format.
 *
 * @param <T> The value type.
 */
public interface APSValue<T> {

    /**
     * @return The held value.
     */
    @NotNull
    T value();

    /**
     * Factory method to provide an APSValue wrapped value.
     *
     * @param value The value to wrap.
     * @param <T> The type of the value.
     *
     * @return An APSValue wrapped value.
     */
    static <T> APSValue<T> value(T value ) {
        return new Provider<T>( value );
    }

    /**
     * Default and very simple implementation.
     *
     * @param <T> The value type.
     */
    class Provider<T> implements APSValue<T> {
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
