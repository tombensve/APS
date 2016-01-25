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
import se.natusoft.osgi.aps.api.core.config.model.APSConfigList;
import se.natusoft.osgi.aps.api.core.config.model.APSConfigValue;
import se.natusoft.osgi.aps.api.core.config.model.APSConfigValueList;
import se.natusoft.osgi.aps.api.core.config.model.admin.APSConfigValueStore;
import se.natusoft.osgi.aps.exceptions.APSRuntimeException;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
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
 * Here is an example of a configuration class (The ´ chars are there to inhibit javadoc to interpret these
 * lines as javadoc annotations!):
 *
 *     ´@APSConfigDescription(description="My configuration")
 *      public class MyConfig extends APSConfig {
 *
 *         ´@APSConfigItemDescription(description="The url to the service to call",
 *             defaultValue={ @APSDefaultValue(value="http://userservice.local:1234/login", configEnv="...") })
 *          public APSConfigValue serviceURL;
 *
 *         ´@APSConfigItemDescription(description="...")
 *          public APSConfigValueList somethingThatNeedsMany;
 *
 *         ´@APSConfigItemDescription(description="...", environmentSpecific=true)
 *          public EndpointConfig endpoint;
 *
 *         ´@APSConfigItemDescription(description="...")
 *          public APSConfigList&lt;EndpointConfig&gt; endpoints;
 *
 *         ´@APSConfigDescription(description="my other config")
 *          public static class EndpointConfig extends APSConfig {
 *
 *             ´@APSConfigItemDescription(description="...")
 *              public APSConfigValue name;
 *
 *             ´@APSConfigItemDescription(description="...")
 *              public APSConfigValue host;
 *
 *             ´@APSConfigItemDescription(description="...")
 *              public APSConfigValue port;
 *          }
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
    private List<APSConfigChangedListener> listeners = new LinkedList<>();

    //
    // Methods
    //

    /**
     * Links the passed instance to this instance. They will both be referencing the same values and listeners.
     *
     * @param to The instance to link to.
     */
    protected void link(APSConfig to) {
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
     * Returns true if the specified listener is already added.
     *
     * @param listener The listener to test.
     *
     * @return true if the listener is already added, false otherwise.
     */
    public synchronized boolean hasConfigChangedListener(APSConfigChangedListener listener) {
        return this.listeners.contains(listener);
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
     * Support method to lookup a configuration value using a string of dot separated value names.
     *
     * Example of simple references (see example at top of class):
     *
     *     serviceUrl
     *     endpoint.host
     *
     * When navigating down an APSConfigList you have to specify which specific value you want to return:
     *
     *     endpoints.name=nisse
     *
     * For the APSConfig entries in 'endpoints', return the instance whose 'name' is "nisse".
     *
     * **DO NOTE:** If the above looked like this:
     *
     *     endpoints.name
     *
     * Then 'endpoints' would be returned as an APSConfigList instance and the name part ignored.
     *
     * @param ref The config value dot notation.
     *
     * @return An APSConfigValue, APSConfigValueList, APSConfigList or null if not found.
     */
    public Object lookup(String ref) {
        // PLEASE NOTE: Due to the complexity of setting up a test for this, this is tested in
        // APSConfigServiceProvider since it already have tests for configs. Unfortunately this means
        // that if this code is screwed up you will not know it until you've also build APSConfigServiceProvider.

        String lookupValue = null;
        String[] refAndValue = ref.split("=");
        if (refAndValue.length == 2) {
            lookupValue = refAndValue[1];
        }
        String refPart = refAndValue[0];
        String[] refParts = refPart.split("\\.");
        String thisRef = refParts[0];

        Object found = null;
        for (Field field : getClass().getDeclaredFields()) {
            if (Modifier.isPublic(field.getModifiers())) {
                if (
                    APSConfigValue.class.isAssignableFrom(field.getType()) ||
                    APSConfigList.class.isAssignableFrom(field.getType()) ||
                    APSConfigValueList.class.isAssignableFrom(field.getType()) ||
                    APSConfig.class.isAssignableFrom(field.getType())
                ) {
                    if (field.getName().equals(thisRef)) {
                        try {
                            found = field.get(this);
                            break;
                        } catch (IllegalAccessException iae) {
                            // This should not happen, and I have nowhere to log it here. So just spit on stderr.
                            System.err.println("The field '" + getClass().getSimpleName() + "." + field.getName() + "' " +
                                    "was not accessible!");
                        }
                    }
                }
            }
        }

        if (found != null) {
            if (APSConfigValueList.class.isAssignableFrom(found.getClass())) {
                if (refParts.length == 1) throw new IllegalArgumentException("Bad reference: '" + ref + "'!");
                for (APSConfigValue value : (APSConfigValueList)found) {
                    if (value.getString().equals(refParts[1])) {
                        found = value;
                        break;
                    }
                }
            }
            else if (APSConfigList.class.isAssignableFrom(found.getClass()) && lookupValue != null) {
                if (refParts.length == 1) throw new IllegalArgumentException("Bad reference: '" + ref + "'!");
                String newRef = makeNewRef(refParts, 1);
                @SuppressWarnings("unchecked") APSConfigList<APSConfig> configList = (APSConfigList<APSConfig>)found;
                found = null;
                for (APSConfig apsConfig : configList) {
                    Object acFound = apsConfig.lookup(newRef);
                    if (acFound != null && APSConfigValue.class.isAssignableFrom(acFound.getClass()) &&
                            ((APSConfigValue)acFound).getString().equals(lookupValue)) {
                        found = acFound;
                        break;
                    }
                }
            }
            else if (APSConfig.class.isAssignableFrom(found.getClass())) {
                if (refParts.length == 1) throw new IllegalArgumentException("Bad reference: '" + ref + "'!");
                found = ((APSConfig)found).lookup(makeNewRef(refParts, 1));
            }
        }

        return found;
    }

    /**
     * This is a support method.
     *
     * It takes the specified APSConfig class and finds a public static field of type ManagedConfig in it.
     * If found get() is called on it to get a root APSConfig subclass instance. lookup(nameRef) is then
     * done on this instance. An APSConfigList is expected as result or an exception is thrown. The APSConfigList
     * is then looped through extracting the value of the field having the name of the last part of nameRef
     * and adding it to a List which is then returned.
     *
     * So basically it supports specifying a field in an APSConfigList&lt;APSConfig&gt; whose values will
     * be returned. These values can then later be used to lookup a specific entry in that APSConfigList.
     *
     * **Note:** APSActivator in aps-tools-lib uses this method (though reflection!).
     *
     * @param configClass A class extending APSConfig.
     * @param nameRef A reference to a value in this config (see lookup(ref) above). The last part of the
     *                reference must be a field in an APSConfig subclass in an APSConfigList or this will
     *                thrown an exception.
     * @return A list of String names extracted from config.
     */
    public static List<String> getNamedConfigEntryNames(Class<APSConfig> configClass, String nameRef) {
        // Find managed instance.
        ManagedConfig<APSConfig> managed = null;
        for (Field field : configClass.getDeclaredFields()) {
            if (field.getType().equals(ManagedConfig.class)) {
                if (Modifier.isPublic(field.getModifiers()) || Modifier.isStatic(field.getModifiers())) {
                    try {
                        //noinspection unchecked
                        managed = (ManagedConfig<APSConfig>)field.get(null);
                    }
                    catch (IllegalAccessException iae) {
                        throw new APSRuntimeException("Could not access a public static field of type ManageConfig in " +
                                configClass.getName() + "!", iae);
                    }
                    break;
                }
            }
        }

        if (managed == null) throw new APSRuntimeException("Bad config class! No 'public static ManagedConfig' field was found!");

        APSConfig config = managed.get();
        Object lookupRes = config.lookup(nameRef);
        if (!APSConfigList.class.isAssignableFrom(lookupRes.getClass()))
            throw new APSRuntimeException("The 'nameRef' does not refer to a value in an APSConfigList object!");

        List<String> result = new LinkedList<>();
        String[] refParts = nameRef.split("\\.");
        String fieldName = refParts[refParts.length - 1];
        APSConfigList configList = (APSConfigList)lookupRes;
        for (Object entry : configList) {
            try {
                Field field = entry.getClass().getDeclaredField(fieldName);
                result.add(field.get(entry).toString());
            }
            catch (NoSuchFieldException nsfe) {
                throw new APSRuntimeException("Last part of nameRef ('" + fieldName + "') does not reference a field in '" +
                        entry.getClass().getName() + "'!", nsfe);
            }
            catch (IllegalAccessException iae) {
                throw new APSRuntimeException("'" + fieldName + "' in '" + entry.getClass() + "' is not public!", iae);
            }
        }

        return result;
    }

    private String makeNewRef(String[] oldRef, int start) {
        StringBuilder newRef = new StringBuilder();
        String dot = "";
        for (int i = start; i < oldRef.length; i++) {
            newRef.append(dot);
            newRef.append(oldRef[i]);
            dot = ".";
        }
        return newRef.toString();
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
