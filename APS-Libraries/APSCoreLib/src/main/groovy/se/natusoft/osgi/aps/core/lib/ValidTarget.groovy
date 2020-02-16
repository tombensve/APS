/*
 *
 * PROJECT
 *     Name
 *         APS Core Lib
 *
 *     Code Version
 *         1.0.0
 *
 *     Description
 *         This library is made in Groovy and thus depends on Groovy, and contains functionality that
 *         makes sense for Groovy, but not as much for Java.
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
 *         2019-08-17: Created!
 *
 */
package se.natusoft.osgi.aps.core.lib

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked

/**
 * Validates the id part of a target (id:address) and calls the passed closure if valid.
 *
 * This also supports "all:"!
 *
 * Note that constants cannot be used for keys of the Map<String, Object> config when doing
 * a constant Map definition. You have to ask the Groovy people why!
 */
@CompileStatic
@TypeChecked
class ValidTarget {

    // These are constants
    public static final String TARGET_ID = "targetId"
    public static final String SUPPORTS_ALL = "supportsAll"

    /**
     * This checks if provided target is valid and if so proceeds with the operation.
     *
     * @param target Target to validate.
     * @param go Closure to call on valid target.
     */
    @SuppressWarnings( "DuplicatedCode" )
    static boolean onValid( Map<String, Object> config, String target, Closure go ) {
        boolean supportsAll = false
        if (config[SUPPORTS_ALL] != null) {
            supportsAll = config[SUPPORTS_ALL]
        }
        onValid( config[TARGET_ID] as String,  supportsAll, target, go)
    }

    /**
     * This checks if provided target is valid and if so proceeds with the operation.
     *
     * @param supportedTargetId The id that is valid in this case.
     * @param target Target to validate.
     * @param go Closure to call on valid target.
     */
    @SuppressWarnings( "DuplicatedCode" )
    static boolean onValid( String supportedTargetId, String target, Closure go ) {
        onValid( supportedTargetId, false, target, go )
    }

    /**
     * This checks if provided target is valid and if so proceeds with the operation.
     *
     * @param supportedTargetId The id that is valid in this case.
     * @param supportsAll If true then the "all:" target is also supported.
     * @param target Target to validate.
     * @param go Closure to call on valid target.
     */
    @SuppressWarnings( "DuplicatedCode" )
    static boolean onValid( String supportedTargetId, boolean supportsAll, String target, Closure go ) {
        boolean valid = false

        if ( target.startsWith( supportedTargetId ) ) {
            target = target.substring( supportedTargetId.length() )
            valid = true
            go.call( target )
        }
        else if (supportsAll && target.startsWith( "all:" )) {
            target = target.substring( 4 )
            valid = true
            go.call( target )
        }

        valid
    }
}
