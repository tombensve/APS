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
 */
@CompileStatic
@TypeChecked
trait ValidTargetTrait {

    /** The target id to validate against. Should be set in constructor. */
    String vttTargetId

    /** Set this to true to support "all:" target id. */
    boolean vttSupportsAll = false

    /**
     * Sets the target id making sure it ends with ":".
     *
     * @param id The id to set with or without ending ":".
     */
    void setVttTargetId( String id) {
        this.vttTargetId = id
        if (!this.vttTargetId.endsWith( ":" )) {
            this.vttTargetId += ":"
        }
    }

    /**
     * This checks if provided target is valid and if so proceeds with the operation.
     *
     * @param target Target to validate.
     * @param go Closure to call on valid target.
     */
    @SuppressWarnings( "DuplicatedCode" )
    void validTarget( String target, Closure go ) {
        if ( target.startsWith( this.vttTargetId ) ) {
            target = target.substring( this.vttTargetId.length() )

            go.call( target )
        }
        else if (this.vttSupportsAll && target.startsWith( "all:" )) {
            target = target.substring( 4 )

            go.call( target )
        }
    }
}
