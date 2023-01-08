package se.natusoft.aps.core.api

import groovy.transform.CompileStatic
import groovyjarjarantlr4.v4.runtime.misc.NotNull
import groovyjarjarantlr4.v4.runtime.misc.Nullable
import se.natusoft.aps.core.exceptions.APSException

/**
 * This represents a result, and will not be made available until there is a result or a failure.
 *
 * @param <T> The expected result type.
 */
@CompileStatic
interface APSResult<T> {

    /**
     * @return true on success, false otherwise. If false then failure() should return an Exception.
     */
    boolean success()

    /**
     * @return An Exception if success() returns false, null otherwise.
     */
    Exception failure()

    /**
     * This is called on success with the result.
     */
    @SuppressWarnings("unused")
    void onSuccess( APSHandler<APSValue<T>> handler )

    /**
     * This is called on failure with the cause exception.
     */
    @SuppressWarnings("unused")
    void onFailure( APSHandler<Exception> handler )

    /**
     * @return The result if success() returns true, null otherwise.
     */
    APSValue<T> result()

    //
    // Convenience Factory Methods
    //

    /**
     * A success result factory method. Java have great problems calling this due to not being able to figure out T.
     * Use successj(...) instead for Java. Groovy handles this one fine.
     *
     * @param value The result value.
     * @param <T>   The result type.
     *
     * @return An APSResult instance holding a success status and the provided value.
     */
    default <T> APSResult<T> success( T value ) {
        return new Provider<>( new APSValue.Provider<>( value ) )
    }

    /**
     * A success result factory method. Java have great problems calling this due to not being able to figure out T.
     * Use successj(...) instead for Java. Groovy handles this one fine.
     *
     * @param value The result value.
     * @param <T>   The result type.
     *
     * @return An APSResult instance holding a success status and the provided value.
     */
    @SuppressWarnings("unused")
    default <T> APSResult<T> successp( T value, String provider ) {
        return new Provider<>( new APSValue.Provider<>( value ), provider )
    }

    /**
     * A success result factory method for Java. The above one works fine for Groovy.
     *
     * @param value The result value.
     * @param <T>   The result type.
     *
     * @return An APSResult instance holding a success status and the provided value.
     */
    @SuppressWarnings("unused")
    default <T> APSResult<T> successj( Object value ) {
        //noinspection unchecked
        return new Provider<>( new APSValue.Provider<>( (T) value ) )
    }

    /**
     * A success result factory method for Java. The above one works fine for Groovy.
     *
     * @param value The result value.
     * @param <T>   The result type.
     *
     * @return An APSResult instance holding a success status and the provided value.
     */
    @SuppressWarnings("unused")
    default <T> APSResult<T> successjp( Object value, String provider ) {
        //noinspection unchecked
        return new Provider<>( new APSValue.Provider<>( (T) value ), provider )
    }

    /**
     * A failure result factory method.
     *
     * @param e   The Exception that caused the failure.
     * @param <T> The result type.
     *
     * @return An APSResult instance holding a failure status and the provided Exception.
     */
    default <T> APSResult<T> failure( Throwable e ) {
        return new Provider<>( e )
    }

    /**
     * A failure result factory method.
     *
     * @param e   The Exception that caused the failure.
     * @param <T> The result type.
     *
     * @return An APSResult instance holding a failure status and the provided Exception.
     */
    default <T> APSResult<T> failure( Throwable e, String provider ) {
        return new Provider<>( e , provider )
    }

    /**
     * Convenience failure handler caller that do catch Exceptions thrown by called handler.
     *
     * @param handler The handler to call.
     * @param e The exception that indicated the fail.
     * @param <T> The result type.
     */
    default <T> void failure( APSHandler<T> handler, Throwable e) {
        try {
            //noinspection unchecked
            handler.handle( (T)new Provider<T>( e ) )
        }
        catch ( Throwable t ){
            t.printStackTrace( System.err )
        }
    }

    /**
     * A failure result handling method. This will call the handler with exception in result
     * if handler is non null. If handler is null the exception will be throws instead.
     *
     * @param handler The handler to call with exception result or null.
     * @param e       The exception that is the result.
     * @param <T>     The type of the result value, which in this case in not provided (null) due to failure.
     */
    @SuppressWarnings("unused")
    default <T> void failureResult( @Nullable APSHandler<APSResult<T>> handler, @NotNull APSException e ) {

        if ( handler != null ) {
            handler.handle( failure( e ) )
        } else {
            throw e
        }
    }

    //
    // Inner Support Classes
    //

    /**
     * Provides a default implementation.
     *
     * @param <T> The type of the result value.
     */
    static class Provider<T> implements APSResult<T> {

        private APSValue<T> result
        private Exception exception
        private String provider

        /**
         * Provide a success result.
         *
         * @param result The success result value.
         */
        Provider( APSValue<T> result ) {
            this.result = result
        }

        /**
         * Provide a failure result.
         *
         * @param exception The Exception that caused the failure.
         */
        Provider( Throwable exception ) {
            if ( Exception.class.isAssignableFrom( exception.getClass() ) ) {
                this.exception = (Exception) exception;
            } else {
                this.exception = new APSException( exception.getMessage(), exception )
            }
        }

        /**
         * Provide a success result.
         *
         * @param result The success result value.
         * @param provider Where the result comes from.
         */
        Provider( APSValue<T> result, String provider ) {
            this.result = result;
            this.provider = provider
        }

        /**
         * Provide a failure result.
         *
         * @param exception The Exception that caused the failure.
         * @param provider Where the result comes from.
         */
        Provider( Throwable exception, String provider ) {
            if ( Exception.class.isAssignableFrom( exception.getClass() ) ) {
                this.exception = (Exception) exception;
            } else {
                this.exception = new APSException( exception.getMessage(), exception )
            }

            this.provider = provider
        }

        /**
         * @return true on success, false otherwise. If false then failure() should return an Exception.
         */
        @Override
        boolean success() {
            return this.exception == null
        }

        /**
         * @return An Exception if success() returns false, null otherwise.
         */
        @Override
        Exception failure() {
            return this.exception
        }

        /**
         * @return The result if success() returns true, null otherwise.
         */
        @Override
        APSValue<T> result() {
            return this.result
        }

        /**
         * @return Returns an optional indicator of what this result comes from.
         */
        String resultProvider() {
            return this.provider
        }

        /**
         * If the result was a success the provided handler will be executed with the result.
         *
         * @param handler The handler to execute on success.
         */
        void onSuccess( APSHandler<APSValue<T>> handler ) {
            if ( this.result != null ) {
                handler.handle( this.result )
            }
        }

        /**
         * If the result was a failure the provided handler will be executed with the exception.
         *
         * @param handler The handler to execute on failure.
         */
        void onFailure( APSHandler<Exception> handler ) {
            if ( this.exception != null ) {
                handler.handle( this.exception )
            }
        }
    }
}
