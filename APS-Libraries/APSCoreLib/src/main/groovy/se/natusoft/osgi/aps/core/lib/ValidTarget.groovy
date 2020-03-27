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

/**
 * Validates the id part of a target (id:address) and calls the passed closure if valid.
 *
 * This also supports "all:"!
 *
 * Note that constants cannot be used for keys of the Map<String, Object> config when doing
 * a constant Map definition. You have to ask the Groovy people why!
 */
@CompileStatic
class ValidTarget {

    /**
     * This checks if provided target is valid and if so proceeds with the operation.
     *
     * @param supportedTargetIds A space separated list of supported target ids.
     * @param target Target to validate.
     * @param go Closure to call on valid target.
     */
    @SuppressWarnings( "DuplicatedCode" )
    static boolean onValid( String supportedTargetIds, String target, Closure go ) {
        onValid( supportedTargetIds, false, target, go )
    }

    /**
     * This checks if provided target is valid and if so proceeds with the operation.
     *
     * @param supportedTargetIds A space separated list of supported target ids.
     * @param supportsAll If true then the "all:" target is also supported.
     * @param target Target to validate.
     * @param go Closure to call on valid target.
     */
    @SuppressWarnings( "DuplicatedCode" )
    static boolean onValid( String supportedTargetIds, boolean supportsAll, String target, Closure go ) {
        boolean valid = false

        String targetId = target.split( ":" )[0]
        if (supportedTargetIds.contains( targetId )) {
            target = target.substring( targetId.length() )
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
