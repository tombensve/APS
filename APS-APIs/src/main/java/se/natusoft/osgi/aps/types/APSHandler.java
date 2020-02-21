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
package se.natusoft.osgi.aps.types;

import se.natusoft.docutations.Nullable;
import se.natusoft.osgi.aps.util.APSExecutor;

/**
 * Generic handler api inspired by Vertx.
 *
 * @param <T> The type of a potential value to handle.
 */
public interface APSHandler<T> {

    /**
     * Does the handling.
     *
     * @param value A value to handle.
     */
    void handle( @Nullable T value );

    /**
     * Provides a wrapped handler that by default will submit handler to APSExecutor.
     *
     * If you want the handler to be executed directly, just pass handler without using
     * this utility method, or do: APSHandler.handler( res -> { ... } ).direct() from java
     * or APSHandler.handler { res -> ... }.direct() from Groovy.
     *
     * Do note that this is intended to be used by the caller that provides the handler
     * callback, not the callee calling the handler.
     *
     * @param handler The actual handler to execute with result.
     * @param <T>     The type of the value to handle.
     *
     * @return A wrapped handler that in most cases will submit the handler call
     * to APSExecutor thread pool.
     */
    static <T> InternalHandlerWrapper<T> provide( APSHandler<T> handler ) {
        return new InternalHandlerWrapper<>( handler );
    }

    /**
     * This is for code having received an APSHandler to call back. This will check if
     * the handler is already wrapped to submit callback to APSExecutor and if not wrap it
     * and then call the handler. Note that this will not happen if the caller have said no
     * to thread pool. This allows the caller to choose.
     *
     * @param handler The handler to call.
     * @param value The value to pass to handler.
     * @param <T> The type of the value.
     */
    static <T> void result( APSHandler<T> handler, T value ) {
        InternalHandlerWrapper<T> ihw =
                handler instanceof APSHandler.InternalHandlerWrapper ? (InternalHandlerWrapper<T>) handler : provide( handler );
        ihw.handle( value );
    }

    /**
     * This wraps an actual APSHandler implementing APSHandler API itself.
     *
     * By default this will push the handler call to APSExecutor thread pool
     * on `handle(value)` call. But doing `new HandlerMgr(handler).direct()`
     * the passed handler will just be called directly on handle(). This is
     * supported just to be consistent in usage, but not wrapping the handler
     * with this is also a valid choice when you don't want it to be submitted
     * to APSExecutor.
     *
     * @param <T> The type of the value for the handler call.
     */
    class InternalHandlerWrapper<T> implements APSHandler<T> {

        private APSHandler<T> handler;

        private boolean concurrent = true;

        public InternalHandlerWrapper( APSHandler<T> handler ) {
            this.handler = handler;
        }

        @SuppressWarnings("unused")
        public InternalHandlerWrapper<T> direct() {
            this.concurrent = false;
            return this;
        }

        @SuppressWarnings("unused")
        public InternalHandlerWrapper<T> concurrent() {
            this.concurrent = true;
            return this;
        }

        /**
         * Does the handling.
         *
         * @param value A value to handle.
         */
        @Override
        public void handle( T value ) {
            if ( this.concurrent ) {
                APSExecutor.submit( () -> this.handler.handle( value ) );
            } else {
                this.handler.handle( value );
            }
        }
    }
}
