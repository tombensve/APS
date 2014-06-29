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
package se.natusoft.osgi.aps.api.core.config.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This is used to provide a description of each configuration property.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface APSConfigItemDescription {
   
    /** The description of the config value */
    String description();

    /**
     * A date pattern for date values. If you specify this and then do _toDate()_ on the _APSConfigValue_ then this
     * pattern will be used to parse the date value. Configuration editors can also make use of this information
     * when setting date values. Setting this to anything indicates that this is a date value.
     */
    String datePattern() default "";

    /**
     * Returns true if the configuration item is environment specific. If true the configuration environment
     * should also be given for default values.
     */
    boolean environmentSpecific() default false;
   
    /** The default value for the config value. */
    APSDefaultValue[] defaultValue() default {};

    /** This should be set to true if the config value is a boolean. */
    boolean isBoolean() default false;

    /** Valid values should be specified here to limit input to one of these values. Empty array means any value is valid. */
    String[] validValues() default {};
}
