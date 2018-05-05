package se.natusoft.osgi.aps.model;

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
    T content();

    default void content(@NotNull T val) { throw new UnsupportedOperationException("content(T val) Is not supported by this implementation!"); }

    default APSObject<T> toAPSObject() {
        return new APSObject<>(content());
    }

    /**
     * Factory method to provide an APSValue wrapped value.
     *
     * @param value The value to wrap.
     * @param <T> The type of the value.
     *
     * @return An APSValue wrapped value.
     */
    static <T> APSValue<T> newValue(T value ) {
        return new Provider<>( value );
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
        @Override
        @NotNull
        public T content() {
            return this.value;
        }

        /**
         * Updates the value.
         *
         * @param val The value to set.
         */
        @Override
        public void content(@NotNull T val) {
            this.value = val;
        }
    }
}
