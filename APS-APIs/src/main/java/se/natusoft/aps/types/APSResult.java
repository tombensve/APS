/*
 *
 * PROJECT
 *     Name
 *         APS APIs
 *
 *     Code Version
 *         1.0.0
 *
 *     Description
 *         Provides the APIs for the application platform services.
 *
 * COPYRIGHTS
 *     Copyright (C) 2012 by Natusoft AB All rights reserved.
 *
 * LICENSE
 *     Apache 2.0 (Open Source)
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 * AUTHORS
 *     tommy ()
 *         Changes:
 *         2018-05-26: Created!
 *
 */
package se.natusoft.aps.types;

import se.natusoft.docutations.NotNull;
import se.natusoft.docutations.Nullable;
import se.natusoft.aps.exceptions.APSException;

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
    @SuppressWarnings("unused")
    void onSuccess( APSHandler<APSValue<T>> handler );

    /**
     * This is called on failure with the cause exception.
     */
    @SuppressWarnings("unused")
    void onFailure( APSHandler<Exception> handler );

    /**
     * @return The result if success() returns true, null otherwise.
     */
    APSValue<T> result();

    /**
     * @return Returns an optional indicator of what this result comes from.
     */
    @SuppressWarnings("unused")
    String resultProvider();

    /**
     * A success result factory method. Java have great problems calling this due to not being able to figure out T.
     * Use successj(...) instead for Java. Groovy handles this one fine.
     *
     * @param value The result value.
     * @param <T>   The result type.
     *
     * @return An APSResult instance holding a success status and the provided value.
     */
    static <T> APSResult<T> success( T value ) {
        return new Provider<>( new APSValue.Provider<>( value ) );
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
    static <T> APSResult<T> successp( T value, String provider ) {
        return new Provider<>( new APSValue.Provider<>( value ), provider );
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
    static <T> APSResult<T> successj( Object value ) {
        //noinspection unchecked
        return new Provider<>( new APSValue.Provider<>( (T) value ) );
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
    static <T> APSResult<T> successjp( Object value, String provider ) {
        //noinspection unchecked
        return new Provider<>( new APSValue.Provider<>( (T) value ), provider );
    }

    /**
     * A failure result factory method.
     *
     * @param e   The Exception that caused the failure.
     * @param <T> The result type.
     *
     * @return An APSResult instance holding a failure status and the provided Exception.
     */
    static <T> APSResult<T> failure( Throwable e ) {
        return new Provider<>( e );
    }

    /**
     * A failure result factory method.
     *
     * @param e   The Exception that caused the failure.
     * @param <T> The result type.
     *
     * @return An APSResult instance holding a failure status and the provided Exception.
     */
    static <T> APSResult<T> failure( Throwable e, String provider ) {
        return new Provider<>( e , provider );
    }

    /**
     * Convenience failure handler caller that do catch Exceptions thrown by called handler.
     *
     * @param handler The handler to call.
     * @param e The exception that indicated the fail.
     * @param <T> The result type.
     */
    static <T> void failure( APSHandler<T> handler, Throwable e) {
        try {
            //noinspection unchecked
            handler.handle( (T)new Provider<T>( e ) );
        }
        catch ( Throwable t ){
            t.printStackTrace( System.err );
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
    static <T> void failureResult( @Nullable APSHandler<APSResult<T>> handler, @NotNull APSException e ) {
        if ( handler != null ) {
            handler.handle( APSResult.failure( e ) );
        } else {
            throw e;
        }
    }

    /**
     * Provides a default implementation.
     *
     * @param <T> The type of the result value.
     */
    class Provider<T> implements APSResult<T> {

        private APSValue<T> result;
        private Exception exception;
        private String provider;

        /**
         * Provide a success result.
         *
         * @param result The success result value.
         */
        public Provider( APSValue<T> result ) {
            this.result = result;
        }

        /**
         * Provide a failure result.
         *
         * @param exception The Exception that caused the failure.
         */
        public Provider( Throwable exception ) {
            if ( Exception.class.isAssignableFrom( exception.getClass() ) ) {
                this.exception = (Exception) exception;
            } else {
                this.exception = new APSException( exception.getMessage(), exception );
            }
        }

        /**
         * Provide a success result.
         *
         * @param result The success result value.
         * @param provider Where the result comes from.
         */
        public Provider( APSValue<T> result, String provider ) {
            this.result = result;
            this.provider = provider;
        }

        /**
         * Provide a failure result.
         *
         * @param exception The Exception that caused the failure.
         * @param provider Where the result comes from.
         */
        public Provider( Throwable exception, String provider ) {
            if ( Exception.class.isAssignableFrom( exception.getClass() ) ) {
                this.exception = (Exception) exception;
            } else {
                this.exception = new APSException( exception.getMessage(), exception );
            }

            this.provider = provider;
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
         * @return Returns an optional indicator of what this result comes from.
         */
        public String resultProvider() {
            return this.provider;
        }

        /**
         * If the result was a success the provided handler will be executed with the result.
         *
         * @param handler The handler to execute on success.
         */
        public void onSuccess( APSHandler<APSValue<T>> handler ) {
            if ( this.result != null ) {
                handler.handle( this.result );
            }
        }

        /**
         * If the result was a failure the provided handler will be executed with the exception.
         *
         * @param handler The handler to execute on failure.
         */
        public void onFailure( APSHandler<Exception> handler ) {
            if ( this.exception != null ) {
                handler.handle( this.exception );
            }
        }
    }
}
