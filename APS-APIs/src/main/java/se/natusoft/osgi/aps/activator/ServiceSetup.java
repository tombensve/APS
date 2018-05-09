/*
 *
 * PROJECT
 *     Name
 *         APS Tools Library
 *
 *     Code Version
 *         1.0.0
 *
 *     Description
 *         Provides a library of utilities, among them APSServiceTracker used by all other APS bundles.
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
 *         2015-01-18: Created!
 *
 */
package se.natusoft.osgi.aps.activator;

import se.natusoft.annotation.beanannotationprocessor.annotations.Bean;
import se.natusoft.annotation.beanannotationprocessor.annotations.Property;
import se.natusoft.osgi.aps.tools.apis.ServiceSetupBean;

import java.util.LinkedList;
import java.util.Properties;

/**
 * Used to provide configold for a service instance.
 */
@SuppressWarnings("PackageAccessibility")
@Bean(value = {
        @Property(name="serviceAPIs", type=LinkedList.class , generics=String.class, init="new java.util.LinkedList<>()", description=
                "The service APIs implemented by the service. Can be null in which case the first implemented interface will be used."),
        @Property(name="props", type= Properties.class, init="new java.util.Properties()", description=
                "The properties for a registered service instance. This can be empty, but that defeats the purpose of using this."),
        @Property(name="serviceInstance", type=Object.class, description=
                "The instance of the service.")
}, pure = true)
public class ServiceSetup extends ServiceSetupBean {}
