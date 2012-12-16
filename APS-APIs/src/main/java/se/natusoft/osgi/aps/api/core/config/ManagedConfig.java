package se.natusoft.osgi.aps.api.core.config;

import se.natusoft.osgi.aps.exceptions.APSRuntimeException;

/**
 * A static instance of this can be defined in APSConfig subclasses if the config is auto
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
     * <p/>
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
         * <p/>
         * Only set this yourself if you like things to be hard and difficult.
         */
        public void setManaged() {
            doSetManaged();
        }

        /**
         * For the service provider to use for setting a managed instance.
         * <p/>
         * Only set this yourself if you want lesser non working functionality!
         *
         * @param config The config instance to set.
         */
        public void setConfigInstance(Config config) {
            ManagedConfig.this.configInstance = config;
        }
    }
}
