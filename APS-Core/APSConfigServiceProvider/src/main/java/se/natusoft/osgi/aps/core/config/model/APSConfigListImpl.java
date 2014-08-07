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

import se.natusoft.osgi.aps.api.core.config.APSConfig;
import se.natusoft.osgi.aps.api.core.config.model.APSConfigList;
import se.natusoft.osgi.aps.api.core.config.model.admin.APSConfigReference;
import se.natusoft.osgi.aps.api.core.config.model.admin.APSConfigValueEditModel;
import se.natusoft.osgi.aps.api.core.config.service.APSConfigException;
import se.natusoft.osgi.aps.core.config.model.admin.APSConfigEditModelImpl;

import static se.natusoft.osgi.aps.core.config.model.StaticUtils.*;

import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.Stack;

/**
 * This represents a list of sub configuration classes.
 *
 * @see APSConfig for more information.
 *
 * @param <APSConfigSubclass> A subclass of APSConfig (an annotated configuration class).
 */
public class APSConfigListImpl<APSConfigSubclass extends APSConfig> implements APSConfigList<APSConfigSubclass>, APSConfigRefConsumer {
    //
    // Private Members
    //

    /** The configuration model representing this value. */
    private APSConfigEditModelImpl configModel = null;

    /** Factory for creating ASPConfig* objects. It also makes available related objects needed when creating these objects. */
    private APSConfigObjectFactory configObjectFactory = null;

    /** Provides the active config environment. */
    private ConfigEnvironmentProvider configEnvironmentProvider = null;

    /** Provides the config value storage. */
    private ConfigValueStoreProvider configValueStoreProvider = null;

    private APSConfigReference ref = null;

    //
    // Constructors
    //

    /**
     * Creates a new APSConfigValueImpl instance.
     *
     * @param configModel The configuration model representing this value.
     * @param configObjectFactory Factory for creating APSConfig* objects.
     * @param configEnvironmentProvider Provides the active config environment.
     * @param configValueStoreProvider Provides the config value storage.
     */
    public APSConfigListImpl(
            APSConfigEditModelImpl configModel,
            APSConfigObjectFactory configObjectFactory,
            ConfigEnvironmentProvider configEnvironmentProvider,
            ConfigValueStoreProvider configValueStoreProvider
    ) {
        this.configModel = configModel;
        this.configObjectFactory = configObjectFactory;
        this.configEnvironmentProvider = configEnvironmentProvider;
        this.configValueStoreProvider = configValueStoreProvider;
    }

    //
    // Methods
    //

    /**
     * Ensures and returns a reference to the config value represented by this instance.
     */
    private APSConfigReference getRef() {
        this.ref = ensureRef(this.ref, this.configModel);
        return this.ref;
    }

    /**
     * Returns the value at the specified index.
     *
     * @param index The index to return value form.
     */
    @Override
    public APSConfigSubclass get(int index) {
        int size = size();
        if (index >= size) {
            throw new IndexOutOfBoundsException("Tried to get value #" + index + " out of total " + size + " values!");
        }

        APSConfigReference gref = getRef().index(index).configEnvironment(configEnvironmentProvider.getActiveConfigEnvironment());

        //noinspection unchecked
        APSConfigEditModelImpl<APSConfigSubclass> acem =
            new APSConfigEditModelImpl(
                this.configModel.getConfigClass(),
                this.configModel.getParent(),
                this.configObjectFactory
            );
        APSConfigSubclass inst = acem.getInstance();

        for (Field f : inst.getClass().getDeclaredFields()) {
            try {
                Object fieldInst = f.get(inst);
                if (fieldInst instanceof APSConfigRefConsumer) {
                    APSConfigRefConsumer crc = (APSConfigRefConsumer)fieldInst;
                    crc.setConfigReference(gref.copy());
                }
            }
            catch (IllegalAccessException iae) {
                throw new APSConfigException("Failed to get configuration field instance!", iae);
            }
        }

        return inst;
    }

    /**
     * @return The number of entries in the list.
     */
    @Override
    public int size() {
        try {
            return Integer.valueOf(
                    this.configValueStoreProvider
                            .getConfigValueStore()
                            .getConfigValue(
                                    toImpl(
                                            getRef()
                                            .configEnvironment(configEnvironmentProvider.getActiveConfigEnvironment())
                                    )
                                    .getValueKey()
                                    .getSizeKey()
                            )
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
    public Iterator<APSConfigSubclass> iterator() {
        return new APSConfigValueIterator(size());
    }

    /**
     * Receives a config value reference.
     *
     * @param ref The reference received.
     */
    @Override
    public void setConfigReference(APSConfigReference ref) {
        this.ref = ref._(this.configModel);
    }

    //
    // Inner Classes
    //

    /**
     * Provides an iterator over APSConfigValue values.
     */
    private class APSConfigValueIterator implements Iterator<APSConfigSubclass> {
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
        public APSConfigSubclass next() {
            return APSConfigListImpl.this.get(this.current++);
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
