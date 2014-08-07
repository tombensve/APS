/*
 *
 * PROJECT
 *     Name
 *         APS APIs
 *     
 *     Code Version
 *         0.11.0
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
 *         2011-05-15: Created!
 *
 */
package se.natusoft.osgi.aps.api.core.config;

import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import se.natusoft.osgi.aps.api.core.config.event.APSConfigChangedEvent;
import se.natusoft.osgi.aps.api.core.config.event.APSConfigChangedListener;
import se.natusoft.osgi.aps.api.core.config.model.admin.APSConfigValueStore;

import java.util.Dictionary;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;

/**
 * The Application-Platform-Services configuration service allows services to register configurations
 * defining the structure and description of the configuration information. A configuration
 * is a class extending this class and annotated with @APSConfigDescription and @APSConfigItemDescription for each configuration
 * value which should be public and of type APSConfigValue or another subclass of this.
 *
 * Please note that all newer version definitions must be backwards compatible for the version handling
 * to work. Also note that you can have different published values for different versions of the definition.
 *
 * Here is an example of a configuration class (The ` chars are there to inhibit javadoc to interpret these
 * lines as javadoc annotations!):
 *
 *     ´@APSConfigDescription(version=1, description="My configuration")
 *      public class MyConfig extends APSConfig {
 *
 *         ´@APSConfigItemDescription(description="The url to the service to call",
 *             defaultValue={ @APSDefaultValue(value="http://userservice.local:1234/login", configEnv="...") })
 *          public <b>APSConfigValue</b> serviceURL;
 *
 *         ´@APSConfigItemDescription(description="...")
 *          public <b>APSConfigValueList</b> somethingThatNeedsMany;
 *
 *         ´@APSConfigItemDescription(description="...", environmentSpecific=true)
 *          public <b>MyOtherConfig</b> myOtherConfig;
 *
 *         ´@APSConfigItemDescription(description="...")
 *          public <b>APSConfigList&lt;MyOtherConfig&gt;</b> myOtherConfigs;
 *
 *         ...
 *     }
 *
 * As can be seen above in the example the following types are allowed for a configuration class:
 *
 * * __APSConfigValue__ - One config value.
 * * __APSConfigValueList__ - List of config values.
 * * __? extends APSConfig__ - Sub-config class. Works just like a main config class which is registered with APSConfigService.
 * * __APSConfigList&lt;? extends APSConfig&gt;__ - List of sub-config class.
 *
 * Please note that the 2 list classes: APSConfigValueList and APSConfigList both implement Iterable and can thereby be
 * used in for loops like this:
 *
 *     ´@APSConfigDescription(version=2, description="Web services config")
 *      public class ExtConfig extends APSConfig {
 *
 *         ´@APSConfigItemDescription(description="...")
 *         public APSConfigValueList endpoints;
 *
 *         ...
 *
 *      }
 *      _____________________________________________________________________
 *
 *      for (APSConfigValue endpoint : extConfig.endpoints) {
 *          ...
 *      }
 *
 * **Yes**, you can replace any value in a config class instance. **No**, you should not do that if you want it to work!
 *
 * The reason for this somewhat strange config API is that this allows for populated instances of these config classes to be
 * provided without using java.lang.reflect.Proxy. The Proxy does not work very well in an OSGi environment with different
 * classloaders for each bundle. If you want to go bald quickly try to Proxy an interface provided by another bundle!
 */
public class APSConfig implements ManagedService {
    //
    // Private Members
    //

    /** The configuration values. */
    private APSConfigValueStore configValues = null;

    /** The event listeners on this config object. */
    private List<APSConfigChangedListener> listeners = new LinkedList<APSConfigChangedListener>();

    //
    // Methods
    //

    /**
     * Copies this instance to the passed copy.
     *
     * @param to The instance to copy to.
     */
    protected void copy(APSConfig to) {
        to.configValues = this.configValues;
        to.listeners = this.listeners;
    }

    /**
     * Sets the backing configuration store.
     *
     * @param configValues The backing configuration store to set.
     */
    public void setConfiguration(APSConfigValueStore configValues) {
        this.configValues = configValues;
    }

    /**
     * Adds a config changed event listener to this config.
     */
    public synchronized void addConfigChangedListener(APSConfigChangedListener listener) {
        this.listeners.add(listener);
    }

    /**
     * Removes a previously added config changed event listener from this config.
     */
    public synchronized void removeConfigChangedListener(APSConfigChangedListener listener) {
        this.listeners.remove(listener);
    }

    /**
     * Returns the current listeners.
     */
    private synchronized List<APSConfigChangedListener> getListeners() {
        List<APSConfigChangedListener> listenersCopy = new LinkedList<>();
        listenersCopy.addAll(this.listeners);
        return listenersCopy;
    }

    /**
     * Sends an event to each registered listener.
     *
     * @param configId The id of the changed config.
     */
    public void triggerConfigChangedEvent(String configId) {
        APSConfigChangedEvent event = new APSConfigChangedEvent(configId);
        for (APSConfigChangedListener listener : getListeners()) {
            listener.apsConfigChanged(event);
        }
    }

    /**
     * __Standard OSGi javadoc:__
     *
     * Update the configuration for a Managed Service.
     *
     * When the implementation of `updated(Dictionary)` detects any
     * kind of error in the configuration properties, it should create a new
     * `ConfigurationException` which describes the problem. This
     * can allow a management system to provide useful information to an
     * administrator.
     *
     * If this method throws any other `Exception`, the
     * Configuration Admin service must catch it and should log it.
     *
     * The Configuration Admin service must call this method asynchronously
     * which initiated the callback. This implies that implementors of Managed
     * Service can be assured that the callback will not take place during
     * registration when they execute the registration in a synchronized method.
     *
     * **APS additions:**
     *
     * This allows for using APS configurations for a standard OSGi service config.
     * The service must implement ManagedService. The same API here is just a convenience
     * so that the service implementation can pass the callback on to the configuration
     * object.
     *
     * Please note that if you use a structured configuration with sub configuration objects
     * then the keys are going to be quite strange compared to configurations for other
     * services. Even for a flat configuration structure the keys are going to be a bit
     * different, but comprehensible. When the configuration is registered with the
     * _APSConfigService_ there is a flag called "forService". If that is true then the
     * configuration properties will also be registered with the standard OSGi config
     * service. That might also be done even if it is false, but when true it is
     * guaranteed. This allows other configuration tools to be used to update the
     * configuration or even other bundles to provide configuration runtime.
     *
     * In general the _APSConfigService_ is intended for application wide config. A configuration
     * can be used by more than one bundle and more than one service. _APSConfigService_ configurations
     * are not bound to anything.
     *
     * @param properties A copy of the Configuration properties, or null.
     *
     * @throws org.osgi.service.cm.ConfigurationException when the update fails.
     */
    @Override
    public void updated(Dictionary properties) throws ConfigurationException {
        if (this.configValues != null) {
            Enumeration keyEnum = properties.keys();
            while (keyEnum.hasMoreElements()) {
                String key = keyEnum.nextElement().toString();
                String value = properties.get(key).toString();
                configValues.setConfigValue(key, value);
            }
        }
    }
}
