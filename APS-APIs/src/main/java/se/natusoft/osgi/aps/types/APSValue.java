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
