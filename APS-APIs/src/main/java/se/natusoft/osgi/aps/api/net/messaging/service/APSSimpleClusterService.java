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
 *         2016-02-27: Created!
 *         
 */
package se.natusoft.osgi.aps.api.net.messaging.service;

import se.natusoft.osgi.aps.api.net.util.APSBox;
import se.natusoft.osgi.aps.api.net.util.TypedData;

import java.util.*;

/**
 * This service defines a synchronized cluster.
 */
public interface APSSimpleClusterService {

    /**
     * Multiple providers of this service can be deployed at the same time. Using this property
     * when registering services for a provider allows clients to lookup a specific provider.
     */
    String APS_CLUSTER_SERVICE_PROVIDER = "aps-cluster-service-provider";

    /**
     * This should be implemented by any code wanting to receive cluster updates.
     */
    interface UpdateListener {
        /**
         * Receives an updated value.
         *
         * @param clusterName The name of the cluster the updated data belongs to.
         * @param name The name of the updated data.
         * @param data The updated data.
         */
        void clusterUpdated(String clusterName, String name, TypedData data);
    }

    /**
     * Creates/updates a value in a cluster.
     *
     * @param clusterName The name of a cluster to store in.
     * @param name The name of the value to store.
     * @param typedData The value to store.
     *
     * @throws IllegalArgumentException on any problem with clusterName.
     */
    void provideData(String clusterName, String name, TypedData typedData);

    /**
     * Gets a value stored in a named cluster. Returns null if it does not exists.
     *
     * @param clusterName The name of the cluster to get data from.
     * @param name The name of the cluster data to get.
     */
    TypedData retrieveData(String clusterName, String name);

    /**
     * Adds an update listener.
     *
     * @param clusterName The name of the cluster to listen for changes in.
     * @param updateListener The update listener to add.
     */
    void addUpdateListener(String clusterName, UpdateListener updateListener);

    /**
     * Removes an update listener.
     *
     * @param clusterName The name of the cluster to remove update listener from.
     * @param updateListener The listener to remove.
     */
    void removeUpdateListener(String clusterName, UpdateListener updateListener);

    //
    // Inner Classes
    //

    /**
     * Support base for implementations. This is of course entirely optional!
     */
    abstract class AbstractAPSSimpleClusterService implements APSSimpleClusterService {

        //
        // Private Members
        //

        /** The listeners. */
        Map<String, List<UpdateListener>> listeners = Collections.synchronizedMap(new HashMap<>());

        //
        // Methods
        //

        /**
         * Adds an update listener.
         *
         * @param clusterName The name of the cluster to add listener for.
         * @param updateListener The update listener to add.
         */
        @Override
        public void addUpdateListener(String clusterName, UpdateListener updateListener) {
            List<UpdateListener> clisteners = this.listeners.get(clusterName);
            if (clisteners == null) {
                clisteners = new LinkedList<>();
                this.listeners.put(clusterName, clisteners);
            }
            clisteners.add(updateListener);
        }

        /**
         * Removes an update listener.
         *
         * @param clusterName The name of the cluster to remove listener from.
         * @param updateListener The listener to remove.
         */
        @Override
        public void removeUpdateListener(String clusterName, UpdateListener updateListener) {
            List<UpdateListener> clisteners = this.listeners.get(clusterName);
            if (clisteners != null) {
                clisteners.remove(updateListener);
            }
        }

        /**
         * Updates all listeners.
         *
         * @param clusterName The name of the cluster the updated data belongs to.
         * @param name The name of the updated data.
         * @param data The actual data.
         */
        protected void updateListeners(String clusterName, String name, TypedData data) {
            List<UpdateListener> clisteners = this.listeners.get(clusterName);
            if (clisteners != null) {
                for (UpdateListener listener : clisteners) {
                    listener.clusterUpdated(clusterName, name, data);
                }
            }

        }

        /**
         * Returns the listeners.
         *
         * @param clusterName The name of the cluster to get listeners for.
         */
        protected List<UpdateListener> getListeners(String clusterName) {
            return this.listeners.get(clusterName);
        }
    }
}
