/*
 *
 * PROJECT
 *     Name
 *         APS APIs
 *     
 *     Code Version
 *         0.9.1
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

// The &nbsp; makes javadoc not se the @ characters since it only reacts to them when they are the first thing on the line.
/**
 * The Application-Platform-Services configuration service allows services to register configurations
 * defining the structure and description of the configuration information. A configuration
 * is a class extending this class and annotated with @APSConfigDescription and @APSConfigItemDescription for each configuration
 * value which should be public and of type APSConfigValue or another subclass of this.
 * <p>
 * Please note that all newer version definitions must be backwards compatible for the version handling
 * to work. Also note that you can have different published values for different versions of the definition.
 * <p>
 * Here is an example of a configuration class:
 * <div style="border:2px solid; border-radius:8px; width:99%;background-color:#eeeeee;color:black">
 * <pre>
 * &nbsp;    @APSConfigDescription(version=1, description="My configuration")
 * &nbsp;    public class MyConfig extends APSConfig {
 * &nbsp;
 * &nbsp;        @APSConfigItemDescription(description="The url to the service to call",
 * &nbsp;            defaultValue={ @APSDefaultValue(value="http://userservice.local:1234/login", configEnv="...") })
 * &nbsp;        public <b>APSConfigValue</b> serviceURL;
 * &nbsp;
 * &nbsp;        @APSConfigItemDescription(description="...")
 * &nbsp;        public <b>APSConfigValueList</b> somethingThatNeedsMany;
 * &nbsp;
 * &nbsp;        @APSConfigItemDescription(description="...", environmentSpecific=true)
 * &nbsp;        public <b>MyOtherConfig</b> myOtherConfig;
 * &nbsp;
 * &nbsp;        @APSConfigItemDescription(description="...")
 * &nbsp;        public <b>APSConfigList&lt;MyOtherConfig&gt;</b> myOtherConfigs;
 * &nbsp;
 * &nbsp;        ...
 * &nbsp;    }
 * </pre>
 * </div>
 * <p/>
 * As can be seen above in the example the following types are allowed for a configuration class:
 * <ul>
 *     <li>APSConfigValue - One config value.</li>
 *     <li>APSConfigValueList - List of config values.</li>
 *     <li>? extends APSConfig - Sub-config class. Works just like a main config class which is registered with APSConfigService.</li>
 *     <li>APSConfigList&lt;? extends APSConfig&gt; - List of sub-config class.</li>
 * </ul>
 * <p/>
 * Please note that the 2 list classes: APSConfigValueList and APSConfigList both implement Iterable and can thereby be
 * used in for loops like this:
 * <pre>
 * <div style="border:2px solid; border-radius:8px; width:99%;background-color:#eeeeee;color:black">
 *
 * &nbsp;    @APSConfigDescription(version=2, description="Web services config")
 * &nbsp;    public class ExtConfig extends APSConfig {
 * &nbsp;
 * &nbsp;        @APSConfigItemDescription(description="...")
 * &nbsp;        public APSConfigValueList endpoints;
 * &nbsp;
 * &nbsp;        ...
 * &nbsp;
 * &nbsp;    }
 * </div>
 * <div style="border:2px solid; border-radius:8px; width:99%;background-color:#eeeeee;color:black">
 *
 * &nbsp;        for (APSConfigValue endpoint : extConfig.endpoints) {
 * &nbsp;            ...
 * &nbsp;        }
 * </div>
 * </pre>
 * <b>Yes</b>, you can replace any value in a config class instance. <b>No</b>, you should not do that if you want it to work!
 * <p/>
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
    public void addConfigChangedListener(APSConfigChangedListener listener) {
        this.listeners.add(listener);
    }

    /**
     * Removes a previously added config changed event listener from this config.
     */
    public void removeConfigChangedListener(APSConfigChangedListener listener) {
        this.listeners.remove(listener);
    }

    /**
     * Sends an event to each registered listener.
     *
     * @param configId The id of the changed config.
     */
    public void triggerConfigChangedEvent(String configId) {
        APSConfigChangedEvent event = new APSConfigChangedEvent(configId);
        for (APSConfigChangedListener listener : this.listeners) {
            listener.apsConfigChanged(event);
        }
    }

    /**
     * <b>Standard OSGi javadoc:</b>
     * <p/>
     * Update the configuration for a Managed Service.
     * <p/>
     * <p/>
     * When the implementation of <code>updated(Dictionary)</code> detects any
     * kind of error in the configuration properties, it should create a new
     * <code>ConfigurationException</code> which describes the problem. This
     * can allow a management system to provide useful information to an
     * administrator.
     * <p/>
     * <p/>
     * If this method throws any other <code>Exception</code>, the
     * Configuration Admin service must catch it and should log it.
     * <p/>
     * The Configuration Admin service must call this method asynchronously
     * which initiated the callback. This implies that implementors of Managed
     * Service can be assured that the callback will not take place during
     * registration when they execute the registration in a synchronized method.
     * <p/>
     * <b>APS additions:</b>
     * </p>
     * This allows for using APS configurations for a standard OSGi service config.
     * The service must implement ManagedService. The same API here is just a convenience
     * so that the service implementation can pass the callback on to the configuration
     * object.
     * <p/>
     * Please note that if you use a structured configuration with sub configuration objects
     * then the keys are going to be quite strange compared to configurations for other
     * services. Even for a flat configuration structure the keys are going to be a bit
     * different, but comprehensible. When the configuration is registered with the
     * APSConfigService there is a flag called "forService". If that is true then the
     * configuration properties will also be registered with the standard OSGi config
     * service. That might also be done even if it is false, but when true it is
     * guaranteed. This allows other configuration tools to be used to update the
     * configuration or even other bundles to provide configuration runtime.
     * <p/>
     * In general the APSConfigService is intended for application wide config. A configuration
     * can be used by more than one bundle and more than one service. APSConfigService configurations
     * are not bound to anything.
     *
     * @param properties A copy of the Configuration properties, or
     *                   <code>null</code>.
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
