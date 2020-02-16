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
    void handle(@Nullable T value);

    /**
     * Convenience method to actually call handler with value in APSExecutor thread pool.
     *
     * @param handler The handler to call.
     * @param value The value for the handler.
     * @param <T> The type of the value.
     */
    static <T> void parallel( APSHandler<T> handler, T value ) {

            submit( handler ).handle( value );
    }

    /**
     * Submits handler to APSExecutor thread pool.
     *
     * WARNING: This has side effects when referencing values outside of executed closure! The closure
     * is no longer in the same context!
     *
     * @param handler The original handler to execute in thead pool.
     * @param <T> The type of the result value.
     * @return Wrapped handler.
     */
    static <T> APSHandler<T> submit(APSHandler<T> handler) {
        return new HandlerWrapper<>( handler );
    }

    /**
     * Note that it is not until handler.handle(...) is called that we should submit the handler since
     * it is first then that we get a value to the handler.
     *
     * @param <T> The type of the value to handle.
     */
    class HandlerWrapper<T> implements APSHandler<T> {

        /**
         * This is a state flag. When true this instance will be submitted to the thread pool.
         * When false the wrapped handler will be called. APSExecutor will always pass null as
         * value, but we cannot use that to determine if we are called by APSExecutor since
         * it is possible for the value to be null in the first call also. Thereby we need this
         * flag to determine our state.
         */
        private boolean submit = true;

        /** We need to save the value from the first call since the second called by APSExecutor will only pass null! */
        private T value;

        /** The real handler to call when being run by APSExecutor thread pool. */
        private APSHandler<T> realHandler;

        public HandlerWrapper( APSHandler<T> realHandler) {
            this.realHandler = realHandler;
        }

        /**
         * Does the handling.
         *
         * @param value A value to handle.
         */
        @Override
        public void handle( T value ) {
            if (this.submit) {
                this.submit = false;
                this.value = value;
                APSExecutor.submit(this);
            }
            else {
                this.submit = true; // Allows for multiple calls to this handler.
                this.realHandler.handle( this.value );
            }
        }
    }
}
