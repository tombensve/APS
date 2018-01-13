package se.natusoft.osgi.aps.api.reactive;

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
     * @return The result if success() returns true, null otherwise.
     */
    APSValue<T> result();
    
    /**
     * A success result factory method.
     *
     * @param value The result value.
     * @param <T> The result type.
     *
     * @return An APSResult instance holding a success status and the provided value.
     */
    static <T> APSResult<T> success(T value) {
        return new Provider<T>().success(new APSValue.Provider<>(value));
    }

    /**
     * A failure result factory method.
     *
     * @param e The Exception that caused the failure.
     * @param <T> The result type.
     *
     * @return An APSResult instance holding a failure status and the provided Exception.
     */
    static <T> APSResult<T> failure(Exception e) {
        return new Provider<T>().failure(e);
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
        public APSResult<T> success(APSValue<T> result) {
            this.result = result;
            return this;
        }

        /**
         * Provide a failure result.
         *
         * @param exception The Exception that caused the failure.
         */
        public APSResult<T> failure(Exception exception) {
            this.exception = exception;
            return this;
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
    }
}
