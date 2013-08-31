/* 
 * 
 * PROJECT
 *     Name
 *         APS APIs
 *     
 *     Code Version
 *         0.9.3
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
 *     Tommy Svensson (tommy@natusoft.se)
 *         Changes:
 *         2012-12-28: Created!
 *         
 */
package se.natusoft.osgi.aps.api.core.config;

import se.natusoft.osgi.aps.exceptions.APSRuntimeException;

/**
 * A static instance of this can be defined in _APSConfig_ subclasses if the config is auto
 * managed by the config service provider. It provides some utility methods.
 */
public class ManagedConfig<Config> {
    //
    // Private Members
    //

    public ConfigServiceAPI serviceProviderAPI = null;

    /**
     * If this config is managed automatically by APS-Configs manifest entry then
     * this will be set to true when the service provider is managing the config.
     */
    private boolean managed = false;

    /** The managed config instance. */
    private Config configInstance;

    //
    // Constructors
    //

    /**
     * Creates a new ManagedConfig instance.
     */
    public ManagedConfig() {
        this.serviceProviderAPI = new ConfigServiceAPI();
    }

    //
    // Methods
    //

    /**
     * Returns true if this config is managed by the config service provider.
     */
    public synchronized boolean isManaged() {
        return this.managed;
    }

    /**
     * Waits for this config to become managed. This will never happen if the config subclass has not
     * been specified with APS-Configs in bundle manifest!
     */
    public synchronized void waitUtilManaged() {
        try {
            while (!this.managed) {
                wait();
            }
        }
        catch (InterruptedException ie) {
            if (!isManaged()) {
                throw new APSRuntimeException("waitUntilManaged() unexpectedly interrupted!", ie);
            }
        }
    }

    /**
     * This call is for the configuration service to inform that it is now managing the config.
     *
     * Only set this yourself if you like things to be hard and difficult.
     */
    private synchronized void doSetManaged() {
        ManagedConfig.this.managed = true;
        try {
            notifyAll();
        }
        catch (IllegalMonitorStateException imse) {
            imse.printStackTrace();
        }
    }

    /**
     * @return The managed config instance.
     */
    public Config get() {
        return this.configInstance;
    }

    //
    // For config service use only! This API is semi hidden to lessen the tempt of stupidity.
    //

    public class ConfigServiceAPI {
        /**
         * This call is for the configuration service to inform that it is now managing the config.
         *
         * Only set this yourself if you like things to be hard and difficult.
         */
        public void setManaged() {
            doSetManaged();
        }

        /**
         * For the service provider to use for setting a managed instance.
         *
         * Only set this yourself if you want lesser non working functionality!
         *
         * @param config The config instance to set.
         */
        public void setConfigInstance(Config config) {
            ManagedConfig.this.configInstance = config;
        }
    }
}
