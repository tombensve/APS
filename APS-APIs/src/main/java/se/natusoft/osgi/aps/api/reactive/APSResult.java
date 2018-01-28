package se.natusoft.osgi.aps.api.reactive;

import se.natusoft.osgi.aps.exceptions.APSException;

/**
 * This represents a result, and will not be made available until there is a result or a failure.
 *
 * @param <T> The expected result type.
 */
public interface APSResult<T> {

    /**
     * @return true on success, false otherwise. If false then failure() should return an Exception.
     */
    boolean success();

    /**
     * @return An Exception if success() returns false, null otherwise.
     */
    Exception failure();

    /**
     * This is called on success with the result.
     */
    void onSuccess(APSHandler<APSValue<T>> handler);

    /**
     * This is called on failure with the cause exception.
     */
    void onFailure(APSHandler<Exception> handler);

    /**
     * @return The result if success() returns true, null otherwise.
     */
    APSValue<T> result();

    /**
     * A success result factory method. Java have great problems calling this due to not being able to figure out T.
     * Use successj(...) instead for Java. Groovy handles this one fine.
     *
     * @param value The result value.
     * @param <T> The result type.
     *
     * @return An APSResult instance holding a success status and the provided value.
     */
    static <T> APSResult<T> success(T value) {
        return new Provider<>(new APSValue.Provider<>(value));
    }

    /**
     * A success result factory method for Java. The above one works fine for Groovy.
     *
     * @param value The result value.
     * @param <T> The result type.
     *
     * @return An APSResult instance holding a success status and the provided value.
     */
    static <T> APSResult<T> successj(Object value) {
        //noinspection unchecked
        return new Provider<>(new APSValue.Provider<>((T)value));
    }

    /**
     * A failure result factory method.
     *
     * @param e The Exception that caused the failure.
     * @param <T> The result type.
     *
     * @return An APSResult instance holding a failure status and the provided Exception.
     */
    static <T> APSResult<T> failure(Throwable e) {
        return new Provider<>(e);
    }

    /**
     * Provides a default implementation.
     *
     * @param <T> The type of the result value.
     */
    class Provider<T> implements APSResult<T> {

        private APSValue<T> result;
        private Exception exception;

        /**
         * Provide a success result.
         *
         * @param result The success result value.
         */
        public Provider(APSValue<T> result) {
            this.result = result;
        }

        /**
         * Provide a failure result.
         *
         * @param exception The Exception that caused the failure.
         */
        public Provider(Throwable exception) {
            if (Exception.class.isAssignableFrom(this.exception.getClass())) {
                this.exception = (Exception)this.exception;
            }
            else {
                this.exception = new APSException(this.exception.getMessage(), this.exception);
            }
        }

        /**
         * @return true on success, false otherwise. If false then failure() should return an Exception.
         */
        @Override
        public boolean success() {
            return this.exception == null;
        }

        /**
         * @return An Exception if success() returns false, null otherwise.
         */
        @Override
        public Exception failure() {
            return this.exception;
        }

        /**
         * @return The result if success() returns true, null otherwise.
         */
        @Override
        public APSValue<T> result() {
            return this.result;
        }

        /**
         * If the result was a success the provided handler will be executed with the result.
         *
         * @param handler The handler to execute on success.
         */
        public void onSuccess(APSHandler<APSValue<T>> handler) {
            if (this.result != null) {
                handler.handle(this.result);
            }
        }

        /**
         * If the result was a failure the provided handler will be executed with the exception.
         *
         * @param handler The handler to execute on failure.
         */
        public void onFailure(APSHandler<Exception> handler) {
            if (this.exception != null) {
                handler.handle(this.exception);
            }
        }
    }
}
