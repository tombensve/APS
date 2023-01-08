package se.natusoft.aps.core.api

import groovy.transform.CompileStatic
import groovyjarjarantlr4.v4.runtime.misc.NotNull

/**
 * This represents a possibly asynchronously delivered value. Inspired by Vertx even
 * though I changed name format.
 *
 * @param <T> The value type.
 */
@CompileStatic
interface APSValue<T> {

    /**
     * @return The held value.
     */
    @NotNull
    T content();

    /**
     * Factory method to provide an APSValue wrapped value.
     *
     * @param value The value to wrap.
     * @param <T> The type of the value.
     *
     * @return An APSValue wrapped value.
     */
    default <T> APSValue<T> create(T value ) {
        return new Provider<>( value )
    }

    /**
     * Default and very simple implementation.
     *
     * @param <T> The value type.
     */
    static class Provider<T> implements APSValue<T> {
        private transient T value

        /**
         * Creates a new Provider.
         *
         * @param value The value to hold.
         */
        Provider(@NotNull T value) {
            this.value = value
        }

        /**
         * @return The held value.
         */
        @Override
        @NotNull
        T content() {
            return this.value
        }

        /**
         * Updates the value.
         *
         * @param val The value to set.
         */
        void content(@NotNull T val) {
            this.value = val
        }
    }
}
