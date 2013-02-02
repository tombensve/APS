/* 
 * 
 * PROJECT
 *     Name
 *         APS Configuration Service Provider
 *     
 *     Code Version
 *         0.9.0
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
 *         2013-02-02: Created!
 *         
 */
package se.natusoft.osgi.aps.core.config.service;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import se.natusoft.osgi.aps.api.core.config.ManagedConfig;
import se.natusoft.osgi.aps.api.core.config.service.APSConfigService;
import se.natusoft.osgi.aps.tools.APSLogger;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Dictionary;

public class APSConfigServiceExtender implements BundleListener {
    //
    // Private Members
    //

    /** Our internal APSConfigService instance. */
    private APSConfigService configService = null;

    /** The logger to log to. */
    APSLogger logger = null;

    //
    // Constructors
    //

    /**
     * Creates a new APSConfigServiceExtender.
     *
     * @param configService The internal config service instance.
     * @param logger The logger to log to.
     */
    public APSConfigServiceExtender(APSConfigService configService, APSLogger logger) {
        this.configService = configService;
        this.logger = logger;
    }

    //
    // Methods
    //

    /**
     * Receives notification that a bundle has had a lifecycle change.
     *
     * @param event The <code>BundleEvent</code>.
     */
    @Override
    public void bundleChanged(BundleEvent event) {
        if (event.getType() == BundleEvent.STARTED) {
            handleBundleStart(event.getBundle());
        } else if (event.getType() == BundleEvent.STOPPED) {
            handleBundleStop(event.getBundle());
        }
    }

    /**
     * Handles config registration and injection for bundle config classes.
     *
     * @param bundle The bundle to handle.
     */
    public void handleBundleStart(Bundle bundle) {
        Dictionary<String, String> headers = bundle.getHeaders();
        String configClasses = headers.get("APS-Configs");
        if (configClasses != null) {
            for (String configClass : configClasses.split(",")) {
                configClass = configClass.trim();

                try {
                    Class cfgClass = bundle.loadClass(configClass);
                    this.configService.registerConfiguration(cfgClass, false);
                    this.logger.info("Registered configuration class '" + configClass + "' for bundle '" +
                            bundle.getSymbolicName() + "'.");

                    // Lets find the first public static instance of the configuration in the class.
                    Field confInstField = null;
                    Field managedConfigField = null;
                    for (Field field : cfgClass.getDeclaredFields()) {
                        if (Modifier.isPublic(field.getModifiers()) && Modifier.isStatic(field.getModifiers())) {
                            if (field.getType().equals(cfgClass)) {
                                confInstField = field;
                            } else if (field.getType().equals(ManagedConfig.class)) {
                                managedConfigField = field;
                            }
                        }
                    }

                    try {
                        if (confInstField != null) {
                            confInstField.set(null, this.configService.getConfiguration(cfgClass));
                            this.logger.info(
                                    "Injected configuration instance '" + configClass + "' into '" +
                                            confInstField.getName() + "' for bundle '" + bundle.getSymbolicName() + "'."
                            );
                        }
                        if (managedConfigField != null) {
                            ManagedConfig managedConfig = (ManagedConfig) managedConfigField.get(null);
                            if (managedConfig != null) {
                                managedConfig.serviceProviderAPI.setConfigInstance(this.configService.getConfiguration(cfgClass));
                                managedConfig.serviceProviderAPI.setManaged();
                                this.logger.info(
                                        "Injected managed configuration instance '" + configClass + "' into '" +
                                                managedConfigField.getName() + "' for bundle '" + bundle.getSymbolicName() + "'."
                                );
                            }
                        }

                    } catch (IllegalAccessException iae) {
                        // This should theoretically never happen since we have already determined
                        // that the field is both public and static.
                        this.logger.error(
                                "Failed to set configuration instance of type '" + configClass + "' in " +
                                        "bundle '" + bundle.getSymbolicName() + "' of name '" + confInstField.getName() + "'!",
                                iae
                        );
                    }


                } catch (ClassNotFoundException cnfe) {
                    this.logger.error(
                            "Bundle '" + bundle.getSymbolicName() + "' has defined '" + configClass + "' " +
                                    "as an APS configuration class, but it cannot be loaded from the bundle!",
                            cnfe
                    );
                }

            }
        }
    }

    /**
     * Handles config deregistration for bundle config classes.
     *
     * @param bundle
     */
    public void handleBundleStop(Bundle bundle) {
        Dictionary<String, String> headers = bundle.getHeaders();
        String configClasses = headers.get("APS-Configs");
        if (configClasses != null) {
            for (String configClass : configClasses.split(",")) {
                configClass = configClass.trim();

                try {
                    Class cfgClass = bundle.loadClass(configClass);
                    this.configService.unregisterConfiguration(cfgClass);
                    this.logger.info(
                            "Unregistered configuration '" + configClass + "' for bundle '" +
                                    bundle.getSymbolicName() + "'!"
                    );

                } catch (ClassNotFoundException cnfe) {
                    // This should already have happened at start of bundle and been logged then.
                    // so we keep quiet now.
                }
            }
        }
    }
}
