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
package se.natusoft.osgi.aps.api.core.config.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A configuration class extending APSConfig must also have this annotation.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface APSConfigDescription {
        
    /** 
     * The version of the configuration. The reason for this is to allow multiple configurations 
     * of the same config concurrently. You only need to bump the version if you have non backwards
     * compatible changes. Just adding new values to a configuration does not need a new version.
     */
    String version();

    /**
     * Optional previous version. If this is provided the new config version can be preloaded with values
     * from the previous configuration (for all backwards compatible values). If this is not provided the
     * new version will start empty (but any annotated defaults will apply).
     */
    String prevVersion() default "";

    /**
     * The identifier for the configuration. Needs to be unique.
     */
    String configId();

    /**
     * A group this config belongs to. The group can be structured into sub groups with dot '.' notation.
     */
    String group() default "";
    
    /** A description of the configuration. */
    String description();
        
}
