/*
 *
 * PROJECT
 *     Name
 *         APS Configuration Service Provider
 *     
 *     Code Version
 *         0.11.0
 *     
 *     Description
 *         A more advanced configuration service that uses annotated interfaces to
 *         describe and provide access to configuration. It supports structured
 *         configuration models.
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
 *     Tommy Svensson (tommy@natusoft.se)
 *         Changes:
 *         2012-02-19: Created!
 *
 */
package se.natusoft.osgi.aps.core.config.model;

import se.natusoft.osgi.aps.api.core.config.model.APSConfigValue;
import se.natusoft.osgi.aps.api.core.config.model.APSConfigValueList;
import se.natusoft.osgi.aps.api.core.config.model.admin.APSConfigValueEditModel;
import se.natusoft.osgi.aps.core.config.model.APSConfigValueImpl.APSConfigIndexedValueImpl;

import java.util.Iterator;

/**
 * Provides an implementation of APSConfigValueList for handling a list of APSConfigValue objects.
 */
public class APSConfigValueListImpl implements APSConfigValueList {
    //
    // Private Members
    //

    /** The configuration definition model representing this value. */
    private APSConfigValueEditModel configValueEditModel = null;

    /** The configuration values to get our configuration value from. */
    private ConfigValueStoreProvider configValuesProvider = null;

    /** Provides the active config environment. */
    private ConfigEnvironmentProvider configEnvProvider = null;

    //
    // Constructors
    //

    /**
     * Creates a new APSConfigValueImpl instance.
     *
     * @param configValueEditModel The configuration model representing this value.
     * @param configValuesProvider Provides configuration value store.
     * @param configEnvProvider Provides the currently active configuration environment.
     */
    public APSConfigValueListImpl(APSConfigValueEditModel configValueEditModel,
                                  ConfigValueStoreProvider configValuesProvider,
                                  ConfigEnvironmentProvider configEnvProvider) {
        this.configValueEditModel = configValueEditModel;
        this.configValuesProvider = configValuesProvider;
        this.configEnvProvider = configEnvProvider;
    }

    //
    // Methods
    //

    /**
     * Returns the value at the specified index.
     *
     * @param index The index to return value form.
     */
    @Override
    public APSConfigValue get(int index) {
        int size = size();
        if (index >= size) {
            throw new IndexOutOfBoundsException("Tried to get " + index + "th value out of total " + size + " values!");
        }
        return new APSConfigIndexedValueImpl(configValueEditModel, configValuesProvider, configEnvProvider, index);
    }

    /**
     * @return The number of entries in the list.
     */
    @Override
    public int size() {
        try {
            return Integer.valueOf(
                    this.configValuesProvider.getConfigValueStore().
                            getConfigValue(this.configValueEditModel.getManyValueSizeKey(this.configEnvProvider.getActiveConfigEnvironment()))
            );
        }
        catch (NumberFormatException nfe) {
            // In case a size has not yet been set, it means the "list" is empty!
            return 0;
        }
    }

    /**
     * Returns true if this list is emtpy.
     */
    @Override
    public boolean isEmpty() {
        return size() <= 0;
    }

    /**
     * Returns an iterator over a set of elements of type T.
     *
     * @return an Iterator.
     */
    @Override
    public Iterator<APSConfigValue> iterator() {
        return new APSConfigValueIterator(size());
    }

    //
    // Inner Classes
    //

    /**
     * Provides an iterator over APSConfigValue values.
     */
    private class APSConfigValueIterator implements Iterator<APSConfigValue> {
        //
        // Private Members
        //

        /** The current index. */
        private int current = 0;

        /** The maximum index of the "list" we iterate over. */
        private int max = -1;

        //
        // Constructors
        //

        /**
         * Creates a new APSConfigValueIterator instance.
         *
         * @param size The size of the list to iterate over.
         */
        public APSConfigValueIterator(int size) {
            this.max = size - 1;
        }

        //
        // Methods
        //

        /**
         * Returns <tt>true</tt> if the iteration has more elements. (In other
         * words, returns <tt>true</tt> if <tt>next</tt> would return an element
         * rather than throwing an exception.)
         *
         * @return <tt>true</tt> if the iterator has more elements.
         */
        @Override
        public boolean hasNext() {
            return this.current <= max;
        }

        /**
         * Returns the next element in the iteration.
         *
         * @return the next element in the iteration.
         *
         * @throws java.util.NoSuchElementException
         *          iteration has no more elements.
         */
        @Override
        public APSConfigValue next() {
            return APSConfigValueListImpl.this.get(this.current++);
        }

        /**
         * Removes from the underlying collection the last element returned by the
         * iterator (optional operation).  This method can be called only once per
         * call to <tt>next</tt>.  The behavior of an iterator is unspecified if
         * the underlying collection is modified while the iteration is in
         * progress in any way other than by calling this method.
         *
         * @throws UnsupportedOperationException if the <tt>remove</tt>
         *                                       operation is not supported by this Iterator.
         * @throws IllegalStateException         if the <tt>next</tt> method has not
         *                                       yet been called, or the <tt>remove</tt> method has already
         *                                       been called after the last call to the <tt>next</tt>
         *                                       method.
         */
        @Override
        public void remove() {
            throw new UnsupportedOperationException("Sorry, remove is not supported for APSConfigValueList.iterator()!");
        }
    }

}
