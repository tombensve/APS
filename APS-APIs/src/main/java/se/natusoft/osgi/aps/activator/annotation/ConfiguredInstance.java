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
 *     Tommy Svensson (tommy.svensson@biltmore.se)
 *         Changes:
 *         2012-08-19: Created!
 *
 */
package se.natusoft.osgi.aps.activator.annotation;

import se.natusoft.osgi.aps.activator.APSActivator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This is a special annotation that only works in conjunction with APSConfigService and also requires
 * APS-APIs to be deployed. It does this using reflection so the aps-tools-lib-n.n.jar still does not
 * have any hard dependencies on anything else. But if you don't have these things when you deploy
 * then this annotation should not be used.
 *
 * ----
 *
 * This annotation should be used in a service implementation whose class is annotated with @OSGiServiceProvider.
 * One or more instances of this service will be created and registered as service instances depending on the
 * pointed out configuration.
 *
 * The whole service instances creation and service registrations will be done in a separate thread since
 * the activator needs to finnish quickly so that other services from other bundles can be started, like
 * the APSConfigService which we depend upon to provide configuration. The APSConfigService will actually
 * be checked if it is available and if not waited for before continuing with the setup of the service.
 *
 * **DO NOTE:**
 *
 * * That even if @OSGiServiceProvider.threadStart() is set to false, it will be treated as true
 *   when this annotation is used, otherwise you would be open for a deadlock.
 *
 * * That this **cannot** be used in conjunction with other instance specification alternatives of
 *   APSActivator annotations!
 *
 * ----
 *
 * This is from an APSActivator usage perspective the easiest and simples of all the instance setup
 * alternatives.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ConfiguredInstance {

    /**
     * This specifies an APSConfigService configuration APSConfig subclass containing
     * a static managed field of type Managed which will be used to reference the config.
     *
     * The static managed field of type Managed is not required by APSConfigService itself,
     * it is a utility to make config access easy. This function however **do require** the
     * use of a static field of type Managed.
     */
    Class configClass();

    /**
     * This is a dot notation path to the config value that decides the number of instances.
     *
     * It works like this:
     *
     * The path is made up of a field name in the root config class that is of type APSConfigList.
     * Then followed by a dot (.). Then followed by a field name of the APSConfig subclass returned
     * by the APSConfigList that represents a unique name of the config instance. This name is
     * injected into the String annotated with this annotation. The service code can then use this
     * name to lookup the correct configuration instance representing the service instance and then
     * pick other config values from the same config instance.
     *
     * The above is the most common scenario and you should stride to keep it like that, but that
     * said it is not the whole truth. The root config class can have a sub config class (i.e.
     * extends APSConfig) which in turn contains the APSConfigList. You can make it as deep as
     * you want, but there can only be one APSConfigList in the chain and it is the one having
     * the name to inject. So the part after the last dot is always the name reference.
     *
     * As you might have guessed, there will be a service instance created for each entry in the
     * APSConfigList.
     */
    String instNamePath();

    /**
     * The property key for the instance name in the OSGi service properties the service is
     * registered with. This is for clients to be able to lookup a specific instance of the service.
     */
    String instanceNamePropertyKey() default APSActivator.SERVICE_INSTANCE_NAME;
}
